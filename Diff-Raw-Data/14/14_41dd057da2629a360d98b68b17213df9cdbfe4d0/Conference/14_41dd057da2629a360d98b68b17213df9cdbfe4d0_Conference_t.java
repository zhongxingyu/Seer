 
 package model.conferences;
 
 import java.sql.Timestamp;
 import java.util.List;
 
 import model.database.Database;
 import model.users.User;
 
 /**
  * This class holds all the information for a conference.
  * 
  * @author Tim Mikeladze
  * @version 11-16-2013
  */
 public class Conference {
     
     /**
      * The id of the conference.
      */
     private int id;
     
     /**
      * The name of the conference.
      */
     private String name;
     
     /**
      * The location of the conference.
      */
     private String location;
     
     /**
      * The date of the conference.
      */
     private Timestamp date;
     
     /**
      * The id of the conferences Program Chair.
      */
     private int programChairID;
     
     /**
      * The conferences Program Chair.
      */
     private User programChair;
     
     /**
      * The number of authors in the conference.
      */
     private int authors;
     
     /**
      * The number of reviewers in the conference.
      */
     private int reviewers;
     
     /**
      * Create a conference object given and ID.
      * 
      * @param id the id of the conference
      * @return conference or null if not exists
      */
     public static Conference conferenceFromID(final int id) {
         Conference conference = null;
         List<Conference> results = Database.getInstance()
                                            .createQuery(
                                                   "SELECT c.ID, c.Name, c.Location, c.Date, cu.UserID AS ProgramChairID,"
                                                            + "(SELECT COUNT(1) FROM conference_users AS cu WHERE cu.ConferenceID = c.ID AND cu.PermissionID = 100) AS Reviewers,"
                                                            + "(SELECT COUNT(1) FROM conference_users AS cu WHERE cu.ConferenceID = c.ID AND cu.PermissionID = 200) AS Authors "
                                                           + "FROM conferences AS c JOIN conference_users AS cu ON c.ID = cu.ConferenceID AND cu.PermissionID = 400 "
                                                           + "WHERE c.ID = :id ORDER BY c.Date DESC")
                                            .addParameter("id", id)
                                            .executeAndFetch(Conference.class);
         if (Database.hasResults(results)) {
             conference = results.get(0);
         }
         return conference;
     }
     
     /**
      * Get the User object for the program chair.
      * 
      * @return the program chair's User object
      */
     public User getProgramChair() {
         return programChair == null ? User.userFromID(programChairID) : programChair;
     }
     
     /**
      * Gets the id.
      * 
      * @return the id
      */
     public int getID() {
         return id;
     }
     
     /**
      * Gets the name.
      * 
      * @return the name
      */
     public String getName() {
         return name;
     }
     
     /**
      * Gets the location.
      * 
      * @return the location
      */
     public String getLocation() {
         return location;
     }
     
     /**
      * Gets the date.
      * 
      * @return the date
      */
     public Timestamp getDate() {
         return date;
     }
     
     /**
      * Gets the program chair id.
      * 
      * @return the program chair id
      */
     public int getProgramChairID() {
         return programChairID;
     }
     
     /**
      * Gets the number of authors.
      * 
      * @return the number of authors
      */
     public int getAuthors() {
         return authors;
     }
     
     /**
      * Gets the number of reviewers.
      * 
      * @return the number of reviewers
      */
     public int getReviewers() {
         return reviewers;
     }
     
     /*
      * Gets the conferences toString.
      * 
      * returns All the relevant information on the conference.
      */
     @Override
     public String toString() {
         return "Conference [getProgramChair()=" + getProgramChair() + ", getID()=" + getID() + ", getName()=" + getName() + ", getLocation()="
                 + getLocation() + ", getDate()=" + getDate() + ", getProgramChairID()=" + getProgramChairID() + ", getAuthors()=" + getAuthors()
                 + ", getReviewers()=" + getReviewers() + "]";
     }
     
 }
