 package models;
 
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 import com.avaje.ebean.Page;
 
 
 /**
  * Role entity managed by Ebean
  */
 @Entity 
 @Table(name="roles")
 public class Role extends Model {
 
     @Id
     public Long id;
     
     @Constraints.Required
     public String name;
     
     @Constraints.Required
     public String code;
     
     /**
      * Generic query helper for entity User with id Long
      */
     public static Model.Finder<Long,Role> find = new Model.Finder<Long,Role>(Long.class, Role.class);
 
     public static Map<String,String> options() {
         LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
         for(Role c: Role.find.orderBy("name").findList()) {
             options.put(c.id.toString(), c.name);
         }
         return options;
     }
     
 
     /**
      * Retrieve all users.
      */
     public static List<Role> findAll() {
         return find.all();
     }
 
     
     public String toString() {
        return "Role(" + name + ")";
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
     public static Page<Role> page(int page, int pageSize, String sortBy, String order, String filter) {
         return 
             find.where()
                 .ilike("name", "%" + filter + "%")
                 .orderBy(sortBy + " " + order)
                 .findPagingList(pageSize)
                 .getPage(page);
     }
 
 }
 
