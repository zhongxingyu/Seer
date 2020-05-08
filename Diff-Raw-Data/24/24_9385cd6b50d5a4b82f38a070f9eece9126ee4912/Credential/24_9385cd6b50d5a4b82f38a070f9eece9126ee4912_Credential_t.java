 package models;
 
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 import com.avaje.ebean.Page;
 
 
 /**
  * User entity managed by Ebean
  */
 @Entity 
 @Table(name="credentials")
 public class Credential extends Model {
 
     @Id
     public Long id;
         
     @Constraints.Required
     public String password;
     
     /**
      * Generic query helper for entity User with id Long
      */
     public static Model.Finder<Long,Credential> find = new Model.Finder<Long,Credential>(Long.class, Credential.class);
 
 
     /**
      * Retrieve all users.
      */
     public static List<Credential> findAll() {
         return find.all();
     }
 
     /**
      * Retrieve a User from email.
      */
     public static Credential findByEmail(String email) {
         return find.where().eq("email", email).findUnique();
     }
     
     /**
      * Authenticate a User.
      */
     public static Credential authenticate(String email, String password) {
         return find.where()
             .eq("email", email)
             .eq("password", password)
             .findUnique();
     }
     
     // --
     
     public String toString() {
         return "User(" + id + ")";
     }
     
 
     /**
      * Return a page of user
      *
      * @param page Page to display
      * @param pageSize Number of users per page
      * @param sortBy User property used for sorting
      * @param order Sort order (either or asc or desc)
      * @param filter Filter applied on the name column
      */
     public static Page<Credential> page(int page, int pageSize, String sortBy, String order, String filter) {
         return 
             find.where()
                 .ilike("name", "%" + filter + "%")
                 .orderBy(sortBy + " " + order)
                 .findPagingList(pageSize)
                 .getPage(page);
     }
     
 
 }
 
