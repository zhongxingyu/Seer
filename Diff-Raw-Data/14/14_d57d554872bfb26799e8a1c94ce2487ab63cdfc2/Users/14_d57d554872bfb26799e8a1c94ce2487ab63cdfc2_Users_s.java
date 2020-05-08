 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
import models.*;
 
 /**
  * The user class handles users and their metadata.
  */
 @With(Secure.class)
 public class Users extends Controller {
 
     /**
      * Lists all users in the database and renders the data to the view.
      */
     public static void index() {
       List<Users> users = User.findAll();
       render(users);
     }
 
 	/**
 	*
 	* Creates a user according to the given information
 	*
 	* @param username The username assocatied with the user to be created
 	* @param last_name The lastname of the user to be created
 	* @param first_name The firstname of the user to be created
 	* @param email The email of the user to be created 
 	* @param admin True if user is admin, false otherwise
 	*
 	*/
 	
 	
	public static void create (String username, String last_name, String first_name, String email, boolean admin) {
 	
	  User user= new user(username, lastname, first_name, email, admin); 
 	  user.save();
 	}
 
 	/**
 	*
 	*Deletes the requested user assocaiated with the userID
 	*
 	* @param userId id of the user to be deleted
 	*
 	*/
 	
 	
	public static void delete (long userId){
 	
	  User u= User.find("by_id", userId).first();
 	  u.delete();
 	
 	}
 	 
 }
