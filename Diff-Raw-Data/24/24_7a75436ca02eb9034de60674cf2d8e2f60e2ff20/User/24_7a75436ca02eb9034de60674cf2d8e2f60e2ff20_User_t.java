 package models;
 
 import com.avaje.ebean.Page;
 import play.data.format.Formats;
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
 import utils.Utils;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Timestamp;
 import java.util.List;
 
 /**
  * Model class that maps to DB table user.
  * Contains a method to authenticate the user.
  *
  * Date: 16/10/13
  * Time: 12:59
  *
  * @author      Sav Balac
  * @version     %I%, %G%
  * @since       1.0
  */
 @Entity
 public class User extends Model {
 
     // Instance variables
     @Id public Long                 id;
 
     @Constraints.Required
     @Formats.NonEmpty
     public String                   username;
 
     @Constraints.Required
     @Formats.NonEmpty
     public String                   password;
 
     @Constraints.Required
     @Formats.NonEmpty
     public String                   email;
 
     @Constraints.Required
     @Formats.NonEmpty
     public String                   fullname;
 
     public Timestamp                lastlogin;
 
     @ManyToMany(mappedBy="users")
     public List<Group>              groups;
 
 
     /**
      * Generic query helper for entity User.
      */
     public static Finder<Long, User> find = new Finder<Long, User>(Long.class, User.class);
 
 
     /**
      * Returns a page of users.
      *
      * @param page          Page to display
      * @param pageSize      Number of users per page
      * @param sortBy        User property used for sorting
      * @param order         Sort order (either or asc or desc)
      * @param filter        Filter applied on group name
      * @param search        Search applied on fullname
      * @return Page<User>
      */
     public static Page<User> page(int page, int pageSize, String sortBy, String order, String filter, String search) {
 
         // Search on fullname, otherwise filter on group name if it's set
         Page p = null;
         if (search.isEmpty()) {
             if (filter.isEmpty()) { // Get all records
                 p = find.where()
                         .orderBy(sortBy + " " + order)
                         .findPagingList(pageSize)
                         .getPage(page);
             } else { // Filter
                 p = find.where()
                         .ilike("groups.name", "%" + filter + "%")
                         .orderBy(sortBy + " " + order)
                         .fetch("groups")
                         .findPagingList(pageSize)
                         .getPage(page);
             }
         } else { // Search
             p = find.where()
                     .ilike("fullname", "%" + search + "%")
                     .orderBy(sortBy + " " + order)
                     .findPagingList(pageSize)
                     .getPage(page);
         }
         return p;
     }
 
 
     /**
      * Saves or updates the user.
      * @throws Exception If there was a problem updating the DB
      */
     public void saveOrUpdate() throws Exception {
         // The id should be 0 for a new record
         if (id==null || id<=0) {
             // Check for duplicate username
             User user = User.find.where().eq("username", this.username).findUnique();
             if (user != null) {
                 throw new Exception("Username already exists");
             }
             save();
         } else {
             update();
         }
     }
 
 
     /**
      * Checks if the user is an admin user. This static method is called from static controller methods.
      *
      * @param username  The username to be checked
      * @return  boolean
      */
     public static boolean isAdminUser(String username) {
         return find.where()
                 .eq("username", username)
                 .eq("groups.name", "Admin")
                 .findRowCount() == 1;
     }
 
 
     /**
      * Checks if this user is an admin user or a manager.
      *
      * @return  boolean
      */
     public boolean isAdminOrManager() {
         return isAdmin() || isManager();
     }
 
 
     /**
      * Checks if this user is an admin user, a manager or a staff member.
      *
      * @return  boolean
      */
     public boolean isAdminOrManagerOrStaff() {
         return isAdmin() || isManager() || isStaff();
     }
 
 
     /**
      * Checks if this user is an admin user.
      *
      * @return  boolean
      */
     public boolean isAdmin() {
         return find.where()
                 .eq("username", username)
                 .eq("groups.name", "Admin")
                 .findRowCount() == 1;
     }
 
 
     /**
      * Checks if this user is a manager.
      *
      * @return  boolean
      */
     public boolean isManager() {
         return find.where()
                 .eq("username", username)
                 .eq("groups.name", "Manager")
                 .findRowCount() == 1;
     }
 
 
     /**
      * Checks if this user is a staff member.
      *
      * @return  boolean
      */
     public boolean isStaff() {
         return find.where()
                 .eq("username", username)
                 .eq("groups.name", "Staff")
                 .findRowCount() == 1;
     }
 
 
     /**
      * Returns the number of groups a user is assigned to (groups could be null).
      * @return      int
      */
     public int getNumGroups() {
         if (groups!=null) {
             return groups.size();
         } else {
             return 0;
         }
     }
 
 
     /**
      * Assigns a group to the user.
      *
      * @param group  The group to be added
      * @return       boolean
      * @throws       Exception    If there was a problem updating the DB
      */
     public void addGroup(Group group) throws Exception {
         try {
             // Add the group if it hasn't been already, set the instance variables and update
             if (!(groups.contains(group))) {
                 groups.add(group);
                 saveManyToManyAssociations("groups"); // Update the database
                 update();
             }
         }
         catch (Exception e) {
             Utils.eHandler("Analyst.addGroup()", e);
             throw e;
         }
     }
 
 
     /**
      * Deletes a group from the user.
      *
      * @param group  The group to be deleted
      * @throws       Exception    If there was a problem updating the DB
      */
     public void delGroup(Group group) throws Exception {
         try {
             // Remove the group if it exists, set the instance variables and update
             if (groups.contains(group)) {
                 groups.remove(group);
                 saveManyToManyAssociations("groups"); // Update the database
                 update();
             } else {
                 throw new Exception("Error: Group " + group.id + ", " + group.name + " not assigned to the user.");
             }
         }
         catch (Exception e) {
             Utils.eHandler("Analyst.delGroup()", e);
             throw e;
         }
     }
 
 
     /**
      * Deletes all groups from the user.
      * @throws Exception    If there was a problem updating the DB
      */
     public void delAllGroups() throws Exception {
         try {
             // Set the instance variable and update
             groups.clear();
             saveManyToManyAssociations("groups"); // Update the database
             update();
         }
         catch (Exception e) {
             Utils.eHandler("Analyst.delAllGroups()", e);
             throw e;
         }
     }
 
 
     /**
      * Authenticates the user. The hashed password is 64 characters long.
      * @param username Username
      * @param password Password to be hashed before checking
      * @return Result
      * @throws NoSuchAlgorithmException    If the hashing algorithm doesn't exist
      */
     public static User authenticate(String username, String password) throws NoSuchAlgorithmException {
         return find.where().eq("username", username)
                            .eq("password", Utils.hashString(password)).findUnique();
     }
 
 
 }
