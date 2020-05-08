 
 package models.user;
 
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.Table;
 import com.avaje.ebean.annotation.Formula;
 import controllers.user.Type;
 import play.db.ebean.Model;
 import play.db.ebean.Model.Finder;
 import play.mvc.Content;
 import play.mvc.Result;
 import play.data.format.Formats;
 
 /**
  * The default abstract class for User.
  * @author Sander Demeester
 **/
 
 @Entity
 @Table(name="users")
 //@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
 public abstract class User extends Model{
 
 	@Id
     public String id;
     public String name;
     
     @Formats.DateTime(pattern = "MM/dd/yyyy")
     public Date birthdate;
     
     @Formats.DateTime(pattern = "MM/dd/yyyy")
     public Date registrationDate;
     public String prefLanguage;
     public String password;
     public String hash;
     public String telephone;
     public String address;
     public String email;
     
     @Enumerated(EnumType.STRING)
     public Gender gender;
     
     @Enumerated(EnumType.STRING)
     public Type type;
     
     public boolean active;
     
     public User(UserID id, Type loginType, String name){
    	this.id = id.geUserID();
     	this.type = loginType; 
     	this.name = name;
 
     }
     
     /**
      * A finder for User.
      * We will use this finder to execute specific sql query's.
      */
     public static Finder<Integer,User> find = new Model.Finder<Integer, User>(Integer.class,User.class);
 
     /**
      * Returns info about this user as a String.
      * @return Userinfo.
      */
     public String getUserInfo() {
         return null;
     }
 
     /**
      * Reset the password of this user. Is delegated to the
      * AuthenticationManager.
      */
     public void resetPassword(){
 
     }
 
     /**
      * Logs out the user. Is delegated to the AuthenticationManager.
      */
     public void logout(){
 
     }
     
     /*
      * Returns the landing page. Is to be implemented by the child classes
      *@return Landing Page
      */
     public abstract Content getLandingPage();
     
     /*
      * Returns the userID
      * @return userID
      */
     public UserID getID(){
     	//TODO
     	return null;
     }
     
     /*
      * Returns the statistics page
      * @return Statistics Page
      */
     public abstract Result showStatistics();
     
     /*
      * Returns the personal info page
      * @return Personal Info Page
      */
     public Result showPersonalInformation(){
     	return null;
     }
 
 
 }
