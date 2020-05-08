 package controllers;
  
 import java.util.Date;
 
 import ch.unibe.ese.calendar.User;
 import ch.unibe.ese.calendar.UserManager;
 import ch.unibe.ese.calendar.User.DetailedProfileVisibility;
import ch.unibe.ese.calendar.exceptions.UserAlreadyExistsException;
 import ch.unibe.ese.calendar.impl.UserManagerImpl;
 import ch.unibe.ese.calendar.util.EseDateFormat;
  
 public class Security extends Secure.Security {
 	
     static boolean authenticate(String username, String password) {
         UserManager um = UserManagerImpl.getInstance();
         User claimedUser = um.getUserByName(username);
         return (claimedUser != null ) && claimedUser.getPassword().equals(password);
     }
     
     public static void addUser(String username, String password) throws Throwable{
     	System.out.println(username + "-" + password);
     	
     	UserManager um = UserManagerImpl.getInstance();
    	try {
    		um.createUser(username, password, null, DetailedProfileVisibility.PUBLIC);
    	} catch (UserAlreadyExistsException e) {
    		error(e.getMessage());
    	}
    		Secure.login();
     }
 
 	public static void register() {
 		render();
 	}
 }
