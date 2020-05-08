 package models;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 import com.avaje.ebean.Page;
 
 
 /**
  * User entity managed by Ebean
  */
 @Entity 
 @Table(name="users")
 public class User extends Model {
 
     @Id
     public Long id;
     
     @Constraints.Required
     public String name;
     
     @Constraints.Required
     public String email;
     
    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
     public Credential credential;
 
     public String accountNumber;
     
     public String description;
     
     @ManyToMany(cascade=CascadeType.ALL)
     @JoinTable(name="users_roles", joinColumns=
             @JoinColumn(name="user_id", referencedColumnName="id"),
             inverseJoinColumns=
                 @JoinColumn(name="role_id", referencedColumnName="id"))
     public List<Role> roles = new ArrayList<Role>();
     
     /**
      * Generic query helper for entity User with id Long
      */
     public static Model.Finder<Long,User> find = new Model.Finder<Long,User>(Long.class, User.class);
 
     public static Map<String,String> options() {
         LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
         for(User c: User.find.orderBy("name").findList()) {
             options.put(c.id.toString(), c.name);
         }
         return options;
     }
     
 
     /**
      * Retrieve all users.
      */
     public static List<User> findAll() {
         return find.all();
     }
 
     /**
      * Retrieve a User from email.
      */
     public static User findByEmail(String email) {
         return find.where().eq("email", email).findUnique();
     }
     
     /**
      * Authenticate a User.
      */
     public static User authenticate(String email, String password) {
         return find.where()
             .eq("email", email)
             .eq("credential.password", password)
             .findUnique();
     }
     
     // --
     
     public String toString() {
         return name;
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
     public static Page<User> page(int page, int pageSize, String sortBy, String order, String filter) {
         return 
             find.where()
                 .ilike("name", "%" + filter + "%")
                 .orderBy(sortBy + " " + order)
                 .findPagingList(pageSize)
                 .getPage(page);
     }
     
     public Boolean hasRole(String code){
     	
     	for(Role role: roles){
     		if(role.code.equals(code)){
     			return Boolean.TRUE;
     		}
     	}
     	return Boolean.FALSE;
     }
 
 }
 
