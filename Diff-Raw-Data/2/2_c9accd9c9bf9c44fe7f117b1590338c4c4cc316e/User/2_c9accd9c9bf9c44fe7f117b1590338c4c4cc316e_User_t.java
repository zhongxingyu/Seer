 package models;
 
 import java.util.*;
 import javax.persistence.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 
 @Entity
 @Table(name="user")
 public class User extends Model {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	public Long id;
 	public int type_id;
 	@Constraints.Required
 	public String username;
 	@Constraints.Required
 	public String password;
 	//private String nontri_account;
 	private static final long serialVersionUID = 1L;
 
 	public static Model.Finder<String,User> find = new Model.Finder(String.class, User.class);
 
 	public User() {
 		super();
 	}
 
 	public User(String username, String password) {
 		super();
 		this.username = username;
 		this.password = password;
 	}
 
 	/**
 	 * Authenticate a User.
      	 */
     	public static User authenticate(String username, String password) {
     		return find.where()
                 	   .eq("username", username)
             		   .eq("password", password)
         		   .findUnique();
     	}
 
     	/**
      	 * Regis a User.
      	 */
     	public static boolean register(String username, String password) {
 		User u = find.where()
         	             .eq("username", username)
                 	     .findUnique();
		if (u == null && !username.equals("") && !password.equals("")) {
 			User newUser = new User(username, password);
 			newUser.save();
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 	public static User findByUsername(String username) {
 		return find.where()
                    .eq("username", username)
         		   .findUnique();
 	}
 
 }
