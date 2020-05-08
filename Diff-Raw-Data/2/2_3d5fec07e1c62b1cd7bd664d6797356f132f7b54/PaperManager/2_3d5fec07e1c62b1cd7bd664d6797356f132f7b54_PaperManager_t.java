 
 package model.papers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import model.conferences.ConferenceUser;
 import model.database.Database;
 import model.database.DatabaseException;
 import model.database.Errors;
 import model.permissions.Permission;
 import model.permissions.PermissionLevel;
 import model.util.FileHandler;
 
 import org.sql2o.data.Table;
 
 /**
  * This class manages papers for the conferences.
  * 
  * @author Mohammad Juma
  * @author Tim Mikeladze
  * @author Srdjan Stojcic
  * @version 11-18-2013
  */
 public class PaperManager {
     
     /**
      * The maximum number of papers that can be submitted to a conference.
      */
     private static final int MAX_PAPER_SUBMISSIONS = 4;
     
     private static final int MAX_REVIEW_ASSIGNMENTS = 4;
     
     private static final int MAX_SUB_PROGRAM_CHAIR_ASSIGNMENTS = 4;
     
     /**
      * Submits a paper to the conference.
      * 
      * @param conferenceID the conference id to submit to
      * @param authorID the author's id
      * @param title the paper title
      * @param description the paper description
      * @param file the paper's file
      * @throws DatabaseException the database exception
      * @throws IOException Signals that an I/O exception has occurred.
      */
     @Permission(level = 100)
     public static void submitPaper(final int conferenceID, final int authorID, final String title, final String description, final File file)
             throws DatabaseException, IOException {
         if (Database.hasResults(Database.getInstance()
                                        .createQuery("SELECT 1 FROM conferences WHERE NOW() > Date AND ID = :id")
                                         .addParameter("id", conferenceID)
                                         .executeAndFetchTable())) {
             throw new DatabaseException(Errors.PAST_CONFERENCE_DATE);
         }
         
         else if (MAX_PAPER_SUBMISSIONS > getNumberOfSubmittedPapers(conferenceID, authorID)) {
             Database.getInstance()
                     .createQuery(
                             "INSERT INTO papers (ConferenceID, AuthorID, Title, Description, SubmissionDate, File, FileExtension) VALUES (:conferenceID, :authorID, :title, :description, NOW(), :file, :fileExtension)")
                     .addParameter("conferenceID", conferenceID)
                     .addParameter("authorID", authorID)
                     .addParameter("title", title)
                     .addParameter("description", description)
                     .addParameter("file", FileHandler.convertFileToBytes(file))
                     .addParameter("fileExtension", FileHandler.getFileExtension(file))
                     .executeUpdate()
                     .getKey(Integer.class);
             Database.getInstance()
                     .createQuery(
                             "INSERT IGNORE INTO conference_users (ConferenceID, UserID, PermissionID) VALUES (:conferenceID, :userID, :permissionID)")
                     .addParameter("conferenceID", conferenceID)
                     .addParameter("userID", authorID)
                     .addParameter("permissionID", PermissionLevel.AUTHOR.getPermission())
                     .executeUpdate();
         }
         else {
             throw new DatabaseException(Errors.MAX_PAPER_SUBMISSIONS_EXCEEDED);
         }
     }
     
     /**
      * Removes the paper.
      * 
      * @param paperID the paper's id
      */
     @Permission(level = 100, strict = true)
     public static void removePaper(final int paperID, final int authorID) {
         Database.getInstance()
                 .createQuery("DELETE FROM papers WHERE ID = :paperID AND AuthorID = :authorID")
                 .addParameter("paperID", paperID)
                 .addParameter("authorID", authorID)
                 .executeUpdate();
     }
     
     /**
      * Gets all the papers that were assigned to a Subprogram Chair in a conference.
      * 
      * @param conferenceID the conference id
      * @param userID he user id of the Subprogram Chair
      * @return list of papers
      */
     @Permission(level = 300)
     public static List<Paper> getAssignedPapersForSubprogramChair(final int conferenceID, final int userID) {
         return getAssignedPapers(conferenceID, userID, PermissionLevel.SUBPROGRAM_CHAIR);
     }
     
     /**
      * Gets all the papers that were assigned to a reviewer in a conference.
      * 
      * @param conferenceID the conference id
      * @param userID The user id of the reviewer
      * @param permission the permission
      * @return The list of papers
      */
     @Permission(level = 200)
     public static List<Paper> getAssignedPapersForReviewer(final int conferenceID, final int userID, final PermissionLevel permission) {
         return getAssignedPapers(conferenceID, userID, PermissionLevel.REVIEWER);
     }
     
     /**
      * Gets the assigned papers for a user
      * 
      * @param conferenceID the conference id
      * @param userID the user id
      * @param permission the permission
      * @return the assigned papers
      */
     private static List<Paper> getAssignedPapers(final int conferenceID, final int userID, final PermissionLevel permission) {
         return Database.getInstance()
                        .createQuery(
                                "SELECT p.ConferenceID, p.ID AS PaperID, p.Title, p.Description, p.AuthorID, p.SubmissionDate, p.Status, p.Revised, p.FileExtension, p.File, p.RevisionDate, Recommended, CONCAT(u.Firstname, ' ', u.Lastname) AS Username FROM papers AS p JOIN assigned_papers AS a ON a.PaperID = p.ID JOIN users AS u ON u.ID = p.AuthorID WHERE p.ConferenceID = :conferenceID AND a.UserID = :userID AND a.PermissionID = :permissionID")
                        .addParameter("conferenceID", conferenceID)
                        .addParameter("userID", userID)
                        .addParameter("permissionID", permission.getPermission())
                        .executeAndFetch(Paper.class);
     }
     
     public static List<Paper> getAssignedPapersForUser(final int userID) {
         return Database.getInstance()
                        .createQuery(
                                "SELECT p.ConferenceID, p.ID AS PaperID, p.Title, p.Description, p.AuthorID, p.SubmissionDate, p.Status, p.Revised, p.FileExtension, p.File, p.RevisionDate, Recommended, CONCAT(u.Firstname, ' ', u.Lastname) AS Username FROM papers AS p JOIN assigned_papers AS a ON a.PaperID = p.ID JOIN users AS u ON u.ID = p.AuthorID WHERE a.UserID = :userID GROUP BY p.ID")
                        .addParameter("userID", userID)
                        .executeAndFetch(Paper.class);
     }
     
     /**
      * Assign paper to a user.
      * 
      * @param paperID the paper id
      * @param userID the user id
      * @param permission the user's permission level
      * @throws DatabaseException
      */
     @Permission(level = 300)
     public static void assignPaper(final int paperID, final int userID, final PermissionLevel permission) throws DatabaseException {
         if (getPaperAuthorID(paperID) != userID) {
             Database.getInstance()
                     .createQuery("INSERT IGNORE INTO assigned_papers (PaperID, UserID, PermissionID) VALUES (:paperID, :userID, :permissionID)")
                     .addParameter("paperID", paperID)
                     .addParameter("userID", userID)
                     .addParameter("permissionID", permission.getPermission())
                     .executeUpdate();
         }
         else {
             throw new DatabaseException(Errors.CANT_ASSIGN_PAPER);
         }
     }
     
     /**
      * Returns paper author ID.
      * 
      * @param paperID The paper's ID
      * @return Given paper's author ID
      * @throws DatabaseException
      */
     public static int getPaperAuthorID(final int paperID) throws DatabaseException {
         Table t = Database.getInstance()
                           .createQuery("SELECT AuthorID FROM papers WHERE ID = :paperID")
                           .addParameter("paperID", paperID)
                           .executeAndFetchTable();
         if (Database.hasResults(t)) {
             return t.rows()
                     .get(0)
                     .getInteger(0);
         }
         else {
             throw new DatabaseException(Errors.PAPER_DOES_NOT_EXIST);
         }
     }
     
     /**
      * Changes the status of a paper to accepted.
      * 
      * @param paperID the papers id
      */
     @Permission(level = 400)
     public static void acceptPaper(final int paperID) {
         setPaperStatus(paperID, true);
     }
     
     /**
      * Changes the status of the paper to rejected.
      * 
      * @param paperID the papers id
      */
     @Permission(level = 400)
     public static void rejectPaper(final int paperID) {
         setPaperStatus(paperID, false);
     }
     
     /**
      * Sets the papers status.
      * 
      * @param paperID the papers id
      * @param status the status of the paper
      */
     @Permission(level = 400)
     private static void setPaperStatus(final int paperID, final boolean status) {
         int paperStatus = status ? 2 : 1;
         Database.getInstance()
                 .createQuery("UPDATE papers SET Status = :paperStatus WHERE ID = :id")
                 .addParameter("paperStatus", paperStatus)
                 .addParameter("id", paperID)
                 .executeUpdate();
     }
     
     /**
      * Gets all the papers in a conference.
      * 
      * @param conferenceID the conferences id
      * @return the list of papers
      */
     @Permission(level = 400)
     public static List<Paper> getPapers(final int conferenceID) {
         return Database.getInstance()
                        .createQuery(
                                "SELECT p.ConferenceID, p.ID AS PaperID, p.Title, p.Description, p.AuthorID, p.SubmissionDate, p.Status, p.Revised, p.FileExtension, p.File, p.RevisionDate, p.Recommended, CONCAT(u.Firstname, ' ', u.Lastname) AS Username FROM papers AS p JOIN users AS u ON u.ID = p.AuthorID WHERE p.ConferenceID = :conferenceID")
                        .addParameter("conferenceID", conferenceID)
                        .executeAndFetch(Paper.class);
     }
     
     @Permission(level = 100)
     public static List<Paper> getAuthorsSubmittedPapers(final int authorID) {
         return Database.getInstance()
                        .createQuery(
                                "SELECT p.ConferenceID, p.ID AS PaperID, p.Title, p.Description, p.AuthorID, p.SubmissionDate, p.Status, p.Revised, p.FileExtension, p.File, p.RevisionDate, p.Recommended, CONCAT(u.Firstname, ' ', u.Lastname) AS Username FROM papers AS p JOIN users AS u ON u.ID = p.AuthorID WHERE p.AuthorID = :authorID")
                        .addParameter("authorID", authorID)
                        .executeAndFetch(Paper.class);
         
     }
     
     /**
      * Gets the number of papers an author has submitted to a conference.
      * 
      * @param conferenceID the conference id
      * @param authorID the authors id
      * @return number of papers author has submitted
      */
     public static int getNumberOfSubmittedPapers(final int conferenceID, final int authorID) {
         return Database.getInstance()
                        .createQuery("SELECT COUNT(1) FROM papers WHERE ConferenceID = :conferenceID AND AuthorID = :authorID")
                        .addParameter("conferenceID", conferenceID)
                        .addParameter("authorID", authorID)
                        .executeAndFetchTable()
                        .rows()
                        .get(0)
                        .getInteger(0);
     }
     
     public static List<ConferenceUser> getAssignedUsers(final int paperID) {
         return Database.getInstance()
                        .createQuery(
                                "SELECT cu.ConferenceID, cu.UserID, CONCAT(u.Firstname, ' ', u.Lastname) AS Username, cu.PermissionID FROM conference_users AS cu JOIN users AS u ON u.ID = cu.UserID JOIN papers AS p ON p.ConferenceID = cu.ConferenceID WHERE p.ID = :paperID ORDER BY cu.PermissionID DESC")
                        .addParameter("paperID", paperID)
                        .executeAndFetch(ConferenceUser.class);
     }
     
     @Permission(level = 300)
     public static void recommendPaper(final int paperID) {
         Database.getInstance()
                 .createQuery("UPDATE papers SET Recommended = 1 WHERE ID = :paperID")
                 .addParameter("paperID", paperID)
                 .executeUpdate();
     }
     
     @Permission(level = 100, strict = true)
     public static void reuploadPaper(final int paperID, final File file) throws IOException {
         Database.getInstance()
                 .createQuery("UPDATE papers SET File = :file, FileExtension = :fileExtension WHERE ID = :paperID")
                 .addParameter("file", FileHandler.convertFileToBytes(file))
                 .addParameter("fileExtension", FileHandler.getFileExtension(file))
                 .executeUpdate();
     }
     
 }
