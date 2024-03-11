import org.example.Contacts;
import org.example.DatabaseConnection;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GestionContacts {
    private List<Contacts> contacts;
    private List<String> groupes;
    private Map<String, List<Contacts>> contactsParGroupe;

    public GestionContacts() {
        contacts = new ArrayList<>();
        groupes = new ArrayList<>();
        contactsParGroupe = new HashMap<>();
    }

    public void creerContact() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Entrez l'ID du contact :");
        int id = scanner.nextInt();
        scanner.nextLine();

        // Vérifier si l'ID existe déjà dans la liste des contacts
        if (contacts.stream().anyMatch(c -> c.getId() == id)) {
            System.out.println("L'ID saisi est déjà utilisé pour un autre contact. Veuillez saisir un ID différent.");
            return;
        }

        System.out.println("Entrez le nom du contact :");
        String nom = scanner.nextLine();

        System.out.println("Entrez le prénom du contact :");
        String prenom = scanner.nextLine();

        System.out.println("Entrez le numéro de téléphone personnel du contact :");
        String telephone1 = scanner.nextLine();

        System.out.println("Entrez le numéro de téléphone professionnel du contact :");
        String telephone2 = scanner.nextLine();

        System.out.println("Entrez l'adresse du contact :");
        String adresse = scanner.nextLine();

        System.out.println("Entrez l'e-mail personnel du contact :");
        String emailPersonnel = scanner.nextLine();

        if (!emailPersonnel.endsWith("@gmail.com")) {
            System.out.println("L'e-mail personnel doit obligatoirement se terminer par @gmail.com");
            return;
        }

        System.out.println("Entrez l'e-mail professionnel du contact :");
        String emailProfessionnel = scanner.nextLine();

        if (!emailProfessionnel.endsWith("@etu.uae.ac.ma")) {
            System.out.println("L'e-mail académique doit obligatoirement se terminer par @etu.uae.ac.ma");
            return;
        }

        System.out.println("Entrez le genre du contact :");
        String genre = scanner.nextLine();

        // Créer le nouvel objet Contacts
        Contacts contact = new Contacts(id, nom, prenom, telephone1, telephone2, adresse, emailPersonnel,
                emailProfessionnel, genre);

        // Ajouter le contact à la base de données
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO contacts (id, nom, prenom, telephone1, telephone2, adresse, emailPersonnel, emailProfessionnel, genre) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, contact.getId());
            statement.setString(2, contact.getNom());
            statement.setString(3, contact.getPrenom());
            statement.setString(4, contact.getTelephone1());
            statement.setString(5, contact.getTelephone2());
            statement.setString(6, contact.getAdresse());
            statement.setString(7, contact.getEmailPersonnel());
            statement.setString(8, contact.getEmailProfessionnel());
            statement.setString(9, contact.getGenre());
            statement.executeUpdate();

            System.out.println("Le contact a été créé avec succès.");
        } catch (SQLException e) {
            System.out.println("Une erreur s'est produite lors de la création du contact : " + e.getMessage());
        }

        // Ajouter le contact à la liste des contacts
        contacts.add(contact);

        System.out.println("Le contact a été créé avec succès.");
    }

    public void afficherContactsParOrdreAlphabetique() {
        try (Connection connection = org.example.DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM contacts ORDER BY nom, prenom";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            System.out.println("----- Liste des contacts (par ordre alphabétique) -----");
            System.out.println("| ID |     Nom      |    Prénom    |   Téléphone 1  |   Téléphone 2  |    Adresse    |");
            System.out.println("-------------------------------------------------------");

            while (resultSet.next()) {
                System.out.printf("| %-2d | %-12s | %-12s | %-14s | %-14s | %-14s |%n",
                        resultSet.getInt("id"),
                        resultSet.getString("nom"),
                        resultSet.getString("prenom"),
                        resultSet.getString("telephone1"),
                        resultSet.getString("telephone2"),
                        resultSet.getString("adresse"));
            }

            System.out.println("---------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimerContact() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Entrez l'ID du contact à supprimer :");
        int id = scanner.nextInt();
        scanner.nextLine();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM contacts WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("Le contact a été supprimé avec succès.");
            } else {
                System.out.println("Aucun contact trouvé avec cet ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void modifierContact() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Entrez l'ID du contact à modifier :");
        int id = scanner.nextInt();
        scanner.nextLine();

        Contacts contactToModify = null;

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Vérifier si le contact existe dans la base de données
            String checkQuery = "SELECT * FROM contacts WHERE id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setInt(1, id);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                // Le contact existe dans la base de données
                // Créer un nouvel objet Contact pour le contact à modifier
                contactToModify = new Contacts();
                contactToModify.setId(resultSet.getInt("id"));
                contactToModify.setNom(resultSet.getString("nom"));
                contactToModify.setPrenom(resultSet.getString("prenom"));
                contactToModify.setTelephone1(resultSet.getString("telephone1"));
                contactToModify.setTelephone2(resultSet.getString("telephone2"));
                contactToModify.setAdresse(resultSet.getString("adresse"));
                contactToModify.setEmailPersonnel(resultSet.getString("emailPersonnel"));
                contactToModify.setEmailProfessionnel(resultSet.getString("emailProfessionnel"));
                contactToModify.setGenre(resultSet.getString("genre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (contactToModify != null) {
            // Le contact a été trouvé dans la base de données, il peut être modifié
            System.out.println("Entrez le nouveau nom du contact :");
            String nom = scanner.nextLine();

            System.out.println("Entrez le nouveau prénom du contact :");
            String prenom = scanner.nextLine();

            System.out.println("Entrez le nouveau numéro de téléphone personnel du contact :");
            String telephone1 = scanner.nextLine();

            System.out.println("Entrez le nouveau numéro de téléphone professionnel du contact :");
            String telephone2 = scanner.nextLine();

            System.out.println("Entrez la nouvelle adresse du contact :");
            String adresse = scanner.nextLine();

            System.out.println("Entrez le nouvel e-mail personnel du contact :");
            String emailPersonnel = scanner.nextLine();

            System.out.println("Entrez le nouvel e-mail professionnel du contact :");
            String emailProfessionnel = scanner.nextLine();

            System.out.println("Entrez le nouveau genre du contact :");
            String genre = scanner.nextLine();

            try (Connection connection = DatabaseConnection.getConnection()) {
                // Mettre à jour le contact dans la base de données
                String updateQuery = "UPDATE contacts SET nom = ?, prenom = ?, telephone1 = ?, telephone2 = ?, adresse = ?, emailPersonnel = ?, emailProfessionnel = ?, genre = ? WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(updateQuery);
                statement.setString(1, nom);
                statement.setString(2, prenom);
                statement.setString(3, telephone1);
                statement.setString(4, telephone2);
                statement.setString(5, adresse);
                statement.setString(6, emailPersonnel);
                statement.setString(7, emailProfessionnel);
                statement.setString(8, genre);
                statement.setInt(9, id);

                int rowsUpdated = statement.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("Le contact a été modifié avec succès.");
                } else {
                    System.out.println("Échec de la modification du contact.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Aucun contact trouvé avec cet ID.");
        }
    }

    public void rechercherContactParNom() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Entrez le nom du contact à rechercher :");
        String nom = scanner.nextLine();

        List<Contacts> contactsTrouves = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM contacts WHERE nom LIKE ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, "%" + nom + "%");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String contactNom = resultSet.getString("nom");
                String prenom = resultSet.getString("prenom");
                String telephone1 = resultSet.getString("telephone1");
                String telephone2 = resultSet.getString("telephone2");
                String adresse = resultSet.getString("adresse");
                String emailPersonnel = resultSet.getString("emailPersonnel");
                String emailProfessionnel = resultSet.getString("emailProfessionnel");
                String genre = resultSet.getString("genre");

                Contacts contact = new Contacts(id, contactNom, prenom, telephone1, telephone2, adresse,
                        emailPersonnel, emailProfessionnel, genre);
                contactsTrouves.add(contact);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!contactsTrouves.isEmpty()) {
            for (Contacts contact : contactsTrouves) {
                System.out.println(contact);
            }
        } else {
            System.out.println("Aucun contact trouvé avec ce nom.");
        }
    }

    public void rechercherContactParNumero() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Entrez le numéro à rechercher :");
        String numero = scanner.nextLine();

        List<Contacts> contactsTrouves = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM contacts WHERE telephone1 = ? OR telephone2 = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, numero);
            statement.setString(2, numero);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nom = resultSet.getString("nom");
                String prenom = resultSet.getString("prenom");
                String telephone1 = resultSet.getString("telephone1");
                String telephone2 = resultSet.getString("telephone2");
                String adresse = resultSet.getString("adresse");
                String emailPersonnel = resultSet.getString("emailPersonnel");
                String emailProfessionnel = resultSet.getString("emailProfessionnel");
                String genre = resultSet.getString("genre");

                Contacts contact = new Contacts(id, nom, prenom, telephone1, telephone2, adresse,
                        emailPersonnel, emailProfessionnel, genre);
                contactsTrouves.add(contact);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!contactsTrouves.isEmpty()) {
            for (Contacts contact : contactsTrouves) {
                System.out.println(contact);
            }
        } else {
            System.out.println("Aucun contact trouvé avec ce numéro.");
        }
    }

    public void creerGroupe() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Entrez le nom du groupe à créer :");
        String nomGroupe = scanner.nextLine();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String checkQuery = "SELECT COUNT(*) FROM groupes WHERE nomGroupe = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setString(1, nomGroupe);
            ResultSet checkResultSet = checkStatement.executeQuery();
            checkResultSet.next();
            int count = checkResultSet.getInt(1);

            if (count > 0) {
                System.out.println("Un groupe avec ce nom existe déjà.");
            } else {
                // Créer le groupe dans la base de données
                String insertQuery = "INSERT INTO groupes (nomGroupe) VALUES (?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.setString(1, nomGroupe);
                int rowsInserted = insertStatement.executeUpdate();

                if (rowsInserted > 0) {
                    System.out.println("Le groupe a été créé avec succès.");
                } else {
                    System.out.println("Échec de la création du groupe.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void ajouterContactAuGroupe() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Entrez le nom du groupe :");
        String nomGroupe = scanner.nextLine();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String checkGroupQuery = "SELECT COUNT(*) FROM groupes WHERE nomGroupe = ?";
            PreparedStatement checkGroupStatement = connection.prepareStatement(checkGroupQuery);
            checkGroupStatement.setString(1, nomGroupe);

            ResultSet groupResultSet = checkGroupStatement.executeQuery();
            groupResultSet.next();
            int groupCount = groupResultSet.getInt(1);

            if (groupCount > 0) {
                System.out.println("Entrez l'ID du contact à ajouter au groupe :");
                int contactId = scanner.nextInt();
                scanner.nextLine();

                String checkContactQuery = "SELECT COUNT(*) FROM contacts WHERE id = ?";
                PreparedStatement checkContactStatement = connection.prepareStatement(checkContactQuery);
                checkContactStatement.setInt(1, contactId);

                ResultSet contactResultSet = checkContactStatement.executeQuery();
                contactResultSet.next();
                int contactCount = contactResultSet.getInt(1);

                if (contactCount > 0) {
                    String addContactQuery = "INSERT INTO contactsParGroupe (nomGroupe, idContact) VALUES (?, ?)";
                    PreparedStatement addContactStatement = connection.prepareStatement(addContactQuery);
                    addContactStatement.setString(1, nomGroupe);
                    addContactStatement.setInt(2, contactId);

                    int rowsUpdated = addContactStatement.executeUpdate();

                    if (rowsUpdated > 0) {
                        System.out.println("Le contact a été ajouté au groupe avec succès.");
                    } else {
                        System.out.println("Échec de l'ajout du contact au groupe.");
                    }
                } else {
                    System.out.println("Aucun contact trouvé avec cet ID.");
                }
            } else {
                System.out.println("Aucun groupe trouvé avec ce nom.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void consulterContactsParGroupe() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Entrez le nom du groupe à consulter :");
        String nomGroupe = scanner.nextLine();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String checkGroupQuery = "SELECT COUNT(*) FROM groupes WHERE nomGroupe = ?";
            PreparedStatement checkGroupStatement = connection.prepareStatement(checkGroupQuery);
            checkGroupStatement.setString(1, nomGroupe);

            ResultSet groupResultSet = checkGroupStatement.executeQuery();
            groupResultSet.next();
            int groupCount = groupResultSet.getInt(1);

            if (groupCount > 0) {
                String selectContactsQuery = "SELECT contacts.* FROM contacts " +
                        "JOIN contactsParGroupe ON contacts.id = contactsParGroupe.idContact " +
                        "JOIN groupes ON contactsParGroupe.nomGroupe = groupes.nomGroupe " +
                        "WHERE groupes.nomGroupe = ?";
                PreparedStatement selectContactsStatement = connection.prepareStatement(selectContactsQuery);
                selectContactsStatement.setString(1, nomGroupe);

                ResultSet contactsResultSet = selectContactsStatement.executeQuery();
                List<Contacts> contactsList = new ArrayList<>();

                while (contactsResultSet.next()) {
                    int id = contactsResultSet.getInt("id");
                    String nom = contactsResultSet.getString("nom");
                    String prenom = contactsResultSet.getString("prenom");
                    String telephone1 = contactsResultSet.getString("telephone1");
                    String telephone2 = contactsResultSet.getString("telephone2");
                    String adresse = contactsResultSet.getString("adresse");
                    String emailPersonnel = contactsResultSet.getString("emailPersonnel");
                    String emailProfessionnel = contactsResultSet.getString("emailProfessionnel");
                    String genre = contactsResultSet.getString("genre");

                    Contacts contact = new Contacts(id, nom, prenom, telephone1, telephone2, adresse, emailPersonnel, emailProfessionnel, genre);
                    contactsList.add(contact);
                }

                if (!contactsList.isEmpty()) {
                    for (Contacts contact : contactsList) {
                        System.out.println(contact);
                    }
                } else {
                    System.out.println("Aucun contact trouvé dans ce groupe.");
                }
            } else {
                System.out.println("Aucun groupe trouvé avec ce nom.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimerGroupe() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Entrez le nom du groupe à supprimer :");
        String nomGroupe = scanner.nextLine();

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Vérifier si le groupe existe dans la table groupes
            String checkGroupQuery = "SELECT COUNT(*) FROM groupes WHERE nomGroupe = ?";
            PreparedStatement checkGroupStatement = connection.prepareStatement(checkGroupQuery);
            checkGroupStatement.setString(1, nomGroupe);

            ResultSet groupResultSet = checkGroupStatement.executeQuery();
            groupResultSet.next();
            int groupCount = groupResultSet.getInt(1);

            if (groupCount > 0) {
                // Supprimer les enregistrements liés au groupe dans la table contactspargroupe
                String deleteContactsQuery = "DELETE FROM contactspargroupe WHERE nomGroupe = ?";
                PreparedStatement deleteContactsStatement = connection.prepareStatement(deleteContactsQuery);
                deleteContactsStatement.setString(1, nomGroupe);
                deleteContactsStatement.executeUpdate();

                // Supprimer le groupe dans la table groupes
                String deleteGroupQuery = "DELETE FROM groupes WHERE nomGroupe = ?";
                PreparedStatement deleteGroupStatement = connection.prepareStatement(deleteGroupQuery);
                deleteGroupStatement.setString(1, nomGroupe);

                int rowsDeleted = deleteGroupStatement.executeUpdate();

                if (rowsDeleted > 0) {
                    System.out.println("Le groupe a été supprimé avec succès.");
                } else {
                    System.out.println("Échec de la suppression du groupe.");
                }
            } else {
                System.out.println("Aucun groupe trouvé avec ce nom.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void rechercherGroupe() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Entrez le nom du groupe à rechercher :");
        String nomGroupe = scanner.nextLine();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String checkGroupQuery = "SELECT COUNT(*) FROM groupes WHERE nomGroupe = ?";
            PreparedStatement checkGroupStatement = connection.prepareStatement(checkGroupQuery);
            checkGroupStatement.setString(1, nomGroupe);

            ResultSet groupResultSet = checkGroupStatement.executeQuery();
            groupResultSet.next();
            int groupCount = groupResultSet.getInt(1);

            if (groupCount > 0) {
                System.out.println("Le groupe existe.");
            } else {
                System.out.println("Aucun groupe trouvé avec ce nom.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void rechercherContactParNomPhonetique () {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Entrez le nom du contact à rechercher :");
        String nomContact = scanner.nextLine();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String searchQuery = "SELECT * FROM contacts WHERE SOUNDEX(nom) = SOUNDEX(?)";
            PreparedStatement searchStatement = connection.prepareStatement(searchQuery);
            searchStatement.setString(1, nomContact);

            ResultSet resultSet = searchStatement.executeQuery();

            if (resultSet.next()) {
                do {
                    int id = resultSet.getInt("id");
                    String nom = resultSet.getString("nom");
                    String prenom = resultSet.getString("prenom");
                    String telephone1 = resultSet.getString("telephone1");
                    String telephone2 = resultSet.getString("telephone2");
                    String adresse = resultSet.getString("adresse");
                    String emailPersonnel = resultSet.getString("emailPersonnel");
                    String emailProfessionnel = resultSet.getString("emailProfessionnel");
                    String genre = resultSet.getString("genre");

                    Contacts contact = new Contacts(id, nom, prenom, telephone1, telephone2, adresse,
                            emailPersonnel, emailProfessionnel, genre);
                    System.out.println(contact);
                } while (resultSet.next());
            } else {
                System.out.println("Aucun contact trouvé avec ce nom.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        GestionContacts gestionContacts = new GestionContacts();
        Scanner scanner = new Scanner(System.in);
        int choix;

        do {
            System.out.println("===================================");
            System.out.println("|     Gestion des contacts        |");
            System.out.println("===================================");
            System.out.println("| 1. Créer un contact             |");
            System.out.println("| 2. Afficher les contacts        |");
            System.out.println("|    par ordre alphabétique       |");
            System.out.println("| 3. Supprimer un contact         |");
            System.out.println("| 4. Modifier un contact          |");
            System.out.println("| 5. Rechercher un contact        |");
            System.out.println("|    par nom                      |");
            System.out.println("| 6. Rechercher un contact        |");
            System.out.println("|    par numéro                   |");
            System.out.println("| 7. Créer un groupe              |");
            System.out.println("| 8. Ajouter un contact à un      |");
            System.out.println("|    groupe                        |");
            System.out.println("| 9. Consulter les contacts d'un  |");
            System.out.println("|    groupe                        |");
            System.out.println("| 10. Supprimer un groupe         |");
            System.out.println("| 11. Rechercher un groupe        |");
            System.out.println("| 13. Rechercher un contact       |");
            System.out.println("|     par nom phonétique           |");
            System.out.println("| 0. Quitter                      |");
            System.out.println("===================================");

            System.out.println("Entrez votre choix :");
            choix = scanner.nextInt();
            scanner.nextLine();

            switch (choix) {
                case 1:
                    gestionContacts.creerContact();
                    break;
                case 2:
                    gestionContacts.afficherContactsParOrdreAlphabetique();
                    break;
                case 3:
                    gestionContacts.supprimerContact();
                    break;
                case 4:
                    gestionContacts.modifierContact();
                    break;
                case 5:
                    gestionContacts.rechercherContactParNom();
                    break;
                case 6:
                    gestionContacts.rechercherContactParNumero();
                    break;
                case 7:
                    gestionContacts.creerGroupe();
                    break;
                case 8:
                    gestionContacts.ajouterContactAuGroupe();
                    break;
                case 9:
                    gestionContacts.consulterContactsParGroupe();
                    break;
                case 10:
                    gestionContacts.supprimerGroupe();
                    break;
                case 11:
                    gestionContacts.rechercherGroupe();
                    break;

                case 13:
                    gestionContacts.rechercherContactParNomPhonetique();
                    break;
                case 0:
                    System.out.println("Au revoir !");
                    break;
                default:
                    System.out.println("Choix invalide. Veuillez réessayer.");
                    break;
            }

            System.out.println();
        } while (choix != 0);
    }


}
