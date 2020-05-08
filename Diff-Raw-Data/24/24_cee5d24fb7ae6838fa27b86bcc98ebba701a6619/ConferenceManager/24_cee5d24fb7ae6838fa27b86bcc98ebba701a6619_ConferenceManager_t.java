 
 package model.conferences;
 
 import java.sql.Timestamp;
 import java.util.List;
 
 import model.database.Database;
 import model.database.DatabaseException;
 import model.database.Errors;
 import model.permissions.Permission;
 import model.permissions.PermissionLevel;
 
 /**
  * The Class ConferenceManager.
  * 
  * @author Tim Mikeladze
  * @author Mohammad Juma
  * @version 11-03-2013
  */
 public class ConferenceManager {
     
     /**
      * Create a conference.
      * 
      * @param name the name of the conference
      * @param location the location of the conference
      * @param date the date the conference is to take place
      * @param programChairID the program chair id
      * @return returns the ID of the created conference
      */
     public static int createConference(final String name, final String location, final Timestamp date, final int programChairID) {
         String sqlDate = date.toString();
         int id = Database.getInstance()
                          .createQuery("INSERT INTO conferences (Name, Location, Date) VALUES (:name, :location, :date)")
                          .addParameter("name", name)
                          .addParameter("location", location)
                          .addParameter("date", sqlDate)
                          .executeUpdate()
                          .getKey(Integer.class);
         Database.getInstance()
                 .createQuery("INSERT INTO conference_users (ConferenceID, UserID, PermissionID) VALUES (:id, :userID, :permissionID)")
                 .addParameter("id", id)
                 .addParameter("userID", programChairID)
                 .addParameter("permissionID", PermissionLevel.PROGRAM_CHAIR.getPermission())
                 .executeUpdate();
         return id;
     }
     
     /**
      * Removes a conference from the database.
      * 
      * @param conferenceID The id of the conference to remove.
      */
     @Permission(level = 400)
     public static void removeConference(final int conferenceID) {
         Database.getInstance()
                 .createQuery("DELETE FROM conferences WHERE ID = :conferenceID")
                 .addParameter("conferenceID", conferenceID)
                 .executeUpdate();
         Database.getInstance()
                 .createQuery("DELETE FROM conference_users WHERE ID = :conferenceID")
                 .addParameter("conferenceID", conferenceID)
                 .executeUpdate();
     }
     
     /**
      * Adds a user to the conference.
      * 
      * @param conferenceID The id of the conference to add the user to
      * @param userID The id of the user being added
      * @param permission the permission
      * @throws DatabaseException the database exception
      */
     public static void addUserToConference(final int conferenceID, final int userID, final PermissionLevel permission) throws DatabaseException {
         if (conferenceExists(conferenceID)) {
             if (!userInConference(conferenceID, userID, permission)) {
                 Database.getInstance()
                         .createQuery(
                                 "INSERT IGNORE INTO conference_users (ConferenceID, UserID, PermissionID) VALUES (:conferenceID, :userID, :permissionID)")
                         .addParameter("conferenceID", conferenceID)
                         .addParameter("userID", userID)
                         .addParameter("permissionID", permission.getPermission())
                         .executeUpdate();
             }
             else {
                 throw new DatabaseException(Errors.USER_ALREADY_IN_CONFERENCE);
             }
         }
         else {
             throw new DatabaseException(Errors.CONFERENCE_DOES_NOT_EXIST);
         }
     }
     
     /**
      * Adds the reviewer to conference.
      * 
      * @param conferenceID the conference id
      * @param userID the user id
      * @throws DatabaseException the database exception
      */
     @Permission(level = 300)
     public static void addReviewerToConference(final int conferenceID, final int userID) throws DatabaseException {
         addUserToConference(conferenceID, userID, PermissionLevel.REVIEWER);
     }
     
     /**
      * Adds the sub program chair to conference.
      * 
      * @param conferenceID the conference id
      * @param userID the user id
      * @throws DatabaseException the database exception
      */
     @Permission(level = 400)
     public static void addSubProgramChairToConference(final int conferenceID, final int userID) throws DatabaseException {
         if (userInConference(conferenceID, userID, PermissionLevel.REVIEWER)) {
             addUserToConference(conferenceID, userID, PermissionLevel.SUBPROGRAM_CHAIR);
         }
         else {
             throw new DatabaseException(Errors.USER_NOT_REVIEWER);
         }
     }
     
     /**
      * Check if user in conference.
      * 
      * @param conferenceID the conference id
      * @param userID the user id
      * @param permission the permission
      * @return true, if successful
      */
     public static boolean userInConference(final int conferenceID, final int userID, final PermissionLevel permission) {
         return Database.hasResults(Database.getInstance()
                                            .createQuery(
                                                    "SELECT 1 FROM conference_users WHERE ConferenceID = :conferenceID AND UserID = :userID AND PermissionID = :permissionID")
                                            .addParameter("conferenceID", conferenceID)
                                            .addParameter("userID", userID)
                                            .addParameter("permissionID", permission.getPermission())
                                            .executeAndFetchTable());
     }
     
     /**
      * Removes a user from the conference.
      * 
      * @param conferenceID The id of the conference to remove the user from
      * @param userID The id of the user being removed
      */
     @Permission(level = 400)
     public static void removeUserFromConference(final int conferenceID, final int userID) {
         Database.getInstance()
                 .createQuery("DELETE FROM conference_users WHERE UserID = :userID AND ConferenceID = :conferenceID")
                 .addParameter("userID", userID)
                 .addParameter("conferenceID", conferenceID)
                 .executeUpdate();
     }
     
     /**
      * Checks if a given conference exists.
      * 
      * @param conferenceID conference id to check
      * @return returns true if conference exists
      */
     public static boolean conferenceExists(final int conferenceID) {
         return Database.hasResults(Database.getInstance()
                                            .createQuery("SELECT 1 FROM conferences WHERE ID = :conferenceID")
                                            .addParameter("conferenceID", conferenceID)
                                            .executeAndFetchTable());
     }
     
     /**
      * Gets a list of users in a conference represented as ConferenceUser objects.
      * 
      * @param id The id of the conference get users for
      * @return the list of users
      */
     @Permission(level = 300)
     public static List<ConferenceUser> getUsersInConference(final int id) {
         return Database.getInstance()
                        .createQuery(
                                "SELECT cu.ConferenceID, cu.UserID, CONCAT(u.Firstname, ' ', u.Lastname) AS Username, u.Firstname, u.Lastname, cu.PermissionID, (SELECT COUNT(1) FROM assigned_papers AS a WHERE a.UserID = cu.UserID AND PermissionID = 300) AS AssignedAsSubProgramChair FROM conference_users AS cu JOIN users AS u ON u.ID = cu.UserID WHERE cu.ConferenceID = :id ORDER BY cu.PermissionID DESC")
                        .addParameter("id", id)
                        .executeAndFetch(ConferenceUser.class);
     }
     
     @Permission(level = 300)
     public static List<ConferenceUser> getUsersInConference(final int id, final PermissionLevel permissionLevel) {
         return Database.getInstance()
                        .createQuery(
                                "SELECT cu.ConferenceID, cu.UserID, CONCAT(u.Firstname, ' ', u.Lastname) AS Username, u.Firstname, u.Lastname, cu.PermissionID, (SELECT COUNT(1) FROM assigned_papers AS a WHERE a.UserID = cu.UserID AND PermissionID = 300) AS AssignedAsSubProgramChair FROM conference_users AS cu JOIN users AS u ON u.ID = cu.UserID WHERE cu.ConferenceID = :id AND cu.PermissionID = :permission ORDER BY cu.PermissionID DESC")
                        .addParameter("id", id)
                        .addParameter("permission", permissionLevel.getPermission())
                        .executeAndFetch(ConferenceUser.class);
     }
     
     /**
      * Get a list of conferences represented by Conference objects.
      * 
      * @return returns List<Conference>
      */
     public static List<Conference> getConferences() {
         return Database.getInstance()
                        .createQuery(
                                "SELECT c.ID, c.Name, c.Location, c.Date, cu.UserID AS ProgramChairID, CONCAT(u.Firstname, ' ', u.Lastname) AS ProgramChair, "
                                       + "(SELECT COUNT(1) FROM conference_users AS cu WHERE cu.ConferenceID = c.ID AND cu.PermissionID = 200) AS Reviewers,"
                                       + "(SELECT COUNT(1) FROM conference_users AS cu WHERE cu.ConferenceID = c.ID AND cu.PermissionID = 100) AS Authors "
                                        + "FROM conferences AS c JOIN conference_users AS cu ON c.ID = cu.ConferenceID AND cu.PermissionID = 400 "
                                        + "JOIN users AS u ON u.ID = cu.UserID ORDER BY c.Date DESC")
                        .executeAndFetch(Conference.class);
     }
     
     /**
      * Get conferences in which a user is in.
      * 
      * @param userID the user id
      * @return list of conferences
      */
     public static List<Conference> getConferencesForUser(final int userID) {
         return Database.getInstance()
                        .createQuery(
                                "SELECT c.ID, c.Name, c.Location, c.Date, cu.UserID AS ProgramChairID, CONCAT(u.Firstname, ' ', u.Lastname) AS ProgramChair, "
                                       + "(SELECT COUNT(1) FROM conference_users AS cu WHERE cu.ConferenceID = c.ID AND cu.PermissionID = 200) AS Reviewers,"
                                       + "(SELECT COUNT(1) FROM conference_users AS cu WHERE cu.ConferenceID = c.ID AND cu.PermissionID = 100) AS Authors "
                                        + "FROM conferences AS c JOIN conference_users AS cu ON c.ID = cu.ConferenceID AND cu.PermissionID = 400 "
                                        + "JOIN users AS u ON u.ID = cu.UserID WHERE u.ID = :userID ORDER BY c.Date DESC")
                        .addParameter("userID", userID)
                        .executeAndFetch(Conference.class);
     }
 }
