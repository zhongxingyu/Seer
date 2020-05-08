 package models;
 
 import java.util.*;
 
 import javax.persistence.*;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 import play.data.validation.Constraints.*;
 import play.db.ebean.Model;
 import play.Logger;
 
 import securesocial.core.java.SecureSocial;
 import securesocial.core.Identity;
 
 import com.avaje.ebean.Expr;
 
 @Table(
 	    uniqueConstraints=
 	        @UniqueConstraint(columnNames={"username"}))
 @Entity
 public class User extends Model {
 
 	private static final long serialVersionUID = 1L;
 
     @Id
     public String id;
 
     public String provider;
 
     public String firstName;
 
     public String lastName;
 
     public String email;
 
     public String password;
 
     public String avatarUrl;
 
     public String privilege = PrivilegeLevel.USER;
 
 	@MinLength(5)
 	@MaxLength(20)
 	public String username;
 
 	public static Finder<String, User> find = new Finder<String, User>(
 			String.class, User.class);
 
     public String displayName() {
         if(username != null) {
             return username;
         } else if (firstName != null && lastName != null) { 
             return firstName + " " + lastName; 
         } else if (email != null) {
             return email;
         } else if (provider != null) {
             return "Unknown user via " + provider;
         } else {
             return "Unknown user";
         }
     }
 	
 	public static User findById(String id) {
 		return find.where().eq("id", id).findUnique();
 	}
     public static User findByEmail(String email) {
         return find.where().eq("email", email).findUnique();
     }
 
     public static User findByUsername(String username) {
         return find.where().eq("username", username).findUnique();
     }
 
     public static User currentUser(){
         Identity identity = SecureSocial.currentUser();
         User localUser = null;
         if (identity != null) {
            localUser = User.find.byId(identity.identityId().userId());
         }
         Logger.debug("identity: " + identity);
         return localUser;
     }
 
     public static boolean userSignedIn() {
         return currentUser() == null ? false : true;
     }
 
     /**
      * Returns a list of users related to the search query.
      * Will look at names and usernames.
      */
     public static List<User> searchUsers(String query) {
         List<User> result = new ArrayList<User>();
         for (String word : query.split("\\s")) {
            result.addAll(find.where()
                 .or(Expr.or(Expr.like("firstName", "%"+word+"%"), Expr.ilike("lastName", "%"+word+"%")), Expr.ilike("userName", "%"+word+"%"))
                .findList());
         }
         return result;
     }
 
     public String getAvatarUrl() {
         return avatarUrl != null ? avatarUrl : "https://sigil.cupcake.io/" + displayName();
     }
 
 	@Override
     public String toString() {
         return this.id + " - " + this.firstName;
     }
 }
