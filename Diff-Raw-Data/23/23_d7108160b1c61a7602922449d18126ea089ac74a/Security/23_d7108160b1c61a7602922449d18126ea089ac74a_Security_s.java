 package controllers;
 
 import utils.QaDB;
 import models.impl.User;
 
 /**
  * The Class Security.
  */
 public class Security extends Secure.Security{
 
 	/**
 	 * Authenticate.
 	 *
 	 * @param username the username
 	 * @param password the password
 	 * @return true, if successful
 	 */
 	static boolean authenticate(String username, String password) {
         return QaDB.findUserByName(username) != null;
     }
 	
 	/**
 	 * What to do when getting disconnected.
 	 */
 	static void onDisconnected() {
			//return to home
 			redirect("/");
 	}
 		
 	/**
 	 * This method returns the current connected username.
 	 *
 	 * @return the string
 	 */
     public static String connected() {
         return session.get("username");
     }
 
     /**
      * Indicate if a user is currently connected.
      *
      * @return  true if the user is connected
      */
     public static boolean isConnected() {
         return session.contains("username");
     }
 
 }
