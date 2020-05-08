 package models;
 
 import com.avaje.ebean.Page;
 import java.sql.Timestamp;
 import java.util.List;
 import javax.persistence.*;
 
 import play.data.format.Formats;
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 import play.db.ebean.Model.Finder; // Import Finder as sometimes Play! shows compilation error "not found: type Finder"
 import utils.Utils;
 
 /**
  * Model class that maps to DB table analyst.
  * Contains methods to get the list, save/update and add/delete desks.
  *
  * Date: 16/10/13
  * Time: 12:59
  *
  * @author      Sav Balac
  * @version     %I%, %G%
  * @since       1.0
  */
 @Entity
 @Table(name="analyst")
 public class Analyst extends Model {
 
     // Instance variables (Play! generates getters and setters)
     @Id public Long                 analystId;
     public String                   salutation;
 
     @Constraints.Required
     @Formats.NonEmpty
     public String                   firstname; // A required constraint will ensure form fields are entered
 
     @Constraints.Required
     @Formats.NonEmpty
     public String                   lastname;
 
     @OneToOne @JoinColumn(name="status_id")
     public Status                   status; // "status_id" is the name of the column in table analyst
 
     @OneToOne @JoinColumn(name="rank")
     public Rank                     rank; // "rank" is the name of the column in table analyst
 
     public String                   email;
     public String                   emailAlternate;
     public String                   paypalAccountEmail;
     public String                   mobile;
     public String                   phone;
     public String                   address1;
     public String                   address2;
     public String                   city;
     public String                   state;
     public String                   zip;
     public String                   country;
     public String                   countryOfResidence;
     public Boolean                  emailverified;
     public Boolean                  phoneVerified;
     public String                   wikiUsername;
     public String                   highriseAccount;
     public Boolean                  contractSigned;
 
     @Lob @Column(name="position_description", length = Utils.MYSQL_TEXT_BYTES)
     public String                   positionDescription;
 
     @Lob @Column(name="biography", length = Utils.MYSQL_TEXT_BYTES)
     public String                   biography;
 
     @Lob @Column(name="academic", length = Utils.MYSQL_TEXT_BYTES)
     public String                   academic;
 
     @Lob @Column(name="notes", length = Utils.MYSQL_TEXT_BYTES)
     public String                   notes;
 
     public Timestamp                createOn;
 
     public String                   skype;
 
     @Lob @Column(name="expertise", length = Utils.MYSQL_TEXT_BYTES)
     public String                   expertise;
 
     @Constraints.Required
     @OneToOne @JoinColumn(name="primary_desk")
     public Desk                     primaryDesk; // "primary_desk" is the name of the column in table analyst
 
     @OneToMany(cascade=CascadeType.PERSIST, mappedBy="analyst")
     public List<Note>               noteList;
 
    @ManyToMany (mappedBy="analysts")
     public List<Desk>               desks;
 
     @OneToOne @JoinColumn(name="profile_image")
     public S3File                   profileImage;
 
     @OneToOne @JoinColumn(name="cv_document")
     public S3File                   cvDocument;
 
 
     /**
      * Generic query helper for entity Analyst.
      */
     public static Finder<Long,Analyst> find = new Finder<Long,Analyst>(Long.class, Analyst.class);
 
 
     /**
      * Returns a page of analysts.
      *
      * @param page          Page to display
      * @param pageSize      Number of analysts per page
      * @param sortBy        Analyst property used for sorting
      * @param order         Sort order (either or asc or desc)
      * @param filter        Filter applied on primary desk
      * @param search        Search applied on lastname
      * @return Page<Analyst>
      */
     public static Page<Analyst> page(int page, int pageSize, String sortBy, String order, String filter, String search) {
 
         // Search on lastname, otherwise filter on primary desk
         Page p = null;
         if (search.isEmpty()) {
             if (filter.isEmpty()) { // Get all records
                 p = find.where()
                         .orderBy(sortBy + " " + order)
                         .findPagingList(pageSize)
                         .getPage(page);
             } else { // Filter
                 p = find.where()
                         .ilike("primaryDesk.name", "%" + filter + "%")
                         .orderBy(sortBy + " " + order)
                         .findPagingList(pageSize)
                         .getPage(page);
             }
         } else { // Search
             p = find.where()
                     .ilike("lastname", "%" + search + "%")
                     .orderBy(sortBy + " " + order)
                     .findPagingList(pageSize)
                     .getPage(page);
         }
         return p;
     }
 
 
     /**
      * Saves or updates the analyst.
      * @throws Exception If there was a problem updating the DB
      */
     public void saveOrUpdate() throws Exception {
         // The analyst id should be 0 for a new record
         if (analystId==null || analystId<=0) {
             save();
         } else {
             update();
         }
     }
 
 
     /**
      * Returns the full name of the analyst.
      * @return      String
      */
     public String getFullName() {
         return firstname + " " + lastname; // Both fields are not null in the DB and are required in the form
     }
 
 
     /**
      * Returns the number of desks an analyst is assigned to (desks could be null).
      * @return      int
      */
     public int getNumDesks() {
         if (desks!=null) {
             return desks.size();
         } else {
             return 0;
         }
     }
 
 
     /**
      * Assigns a desk to the analyst.
      *
      * @param desk  The desk to be added
      * @throws      Exception    If there was a problem updating the DB
      */
     public void addDesk(Desk desk) throws Exception {
         try {
             // Add the desk if it hasn't been already (and not the primary desk), set the instance variables and update
             if (!(desks.contains(desk) || desk.equals(primaryDesk))) {
                 desks.add(desk);
                 saveManyToManyAssociations("desks"); // Update the database
                 update();
             }
         }
         catch (Exception e) {
             Utils.eHandler("Analyst.addDesk(" + desk.deskId + ", " + desk.name + ")", e);
             throw e;
         }
     }
 
 
     /**
      * Deletes a desk from the analyst.
      *
      * @param desk  The desk to be deleted
      * @throws      Exception    If there was a problem updating the DB
      */
     public void delDesk(Desk desk) throws Exception {
         try {
             // Remove the desk if it exists, set the instance variables and update
             if (desks.contains(desk)) {
                 desks.remove(desk);
                 saveManyToManyAssociations("desks"); // Update the database
                 update();
             } else {
                 throw new Exception("Error: Desk " + desk.deskId + ", " + desk.name + " not assigned to the analyst.");
             }
         }
         catch (Exception e) {
             Utils.eHandler("Analyst.delDesk(" + desk.deskId + ", " + desk.name + ")", e);
             throw e;
         }
     }
 
 
     /**
      * Deletes all desks from the analyst.
      * @throws Exception    If there was a problem updating the DB
      */
     public void delAllDesks() throws Exception {
         try {
             // Set the instance variable and update
             desks.clear();
             saveManyToManyAssociations("desks"); // Update the database
             update();
         }
         catch (Exception e) {
             Utils.eHandler("Analyst.delAllDesks()", e);
             throw e;
         }
     }
 
 
     /**
      * Returns the number of notes that have been recorded against the analyst (noteList could be null).
      * @return      int
      */
     public int getNumNotes() {
         if (noteList!=null) {
             return noteList.size();
         } else {
             return 0;
         }
     }
 
 
     /**
      * Adds a note to the analyst.
      *
      * @param note  The note to be added
      * @throws      Exception    If there was a problem updating the DB
      */
     public void addNote(Note note) throws Exception {
         try {
             // Set the instance variable and update
             noteList.add(note);
             update();
         }
         catch (Exception e) {
             Utils.eHandler("Analyst.addNote(" + note.noteId + ", " + note.title + ")", e);
             throw e;
         }
     }
 
 
     /**
      * Deletes a note from the analyst.
      *
      * @param note  The note to be deleted
      * @throws      Exception    If there was a problem updating the DB
      */
     public void delNote(Note note) throws Exception {
         try {
             // Remove the note if it exists, set the instance variables and update
             if (noteList.contains(note)) {
                 noteList.remove(note);
                 update();
             } else {
                 throw new Exception("Error: Note " + note.noteId + ", " + note.title + " not assigned to the analyst.");
             }
         }
         catch (Exception e) {
             Utils.eHandler("Analyst.delNote(" + note.noteId + ", " + note.title + ")", e);
             throw e;
         }
     }
 
 
     /**
      * Deletes all notes from the analyst.
      *
      * @throws Exception    If there was a problem updating the DB
      */
     public void delAllNotes() throws Exception {
         try {
             // Set the instance variable and update
             noteList.clear();
             update();
         }
         catch (Exception e) {
             Utils.eHandler("Analyst.delAllNotes()", e);
             throw e;
         }
     }
 
 
 }
 
