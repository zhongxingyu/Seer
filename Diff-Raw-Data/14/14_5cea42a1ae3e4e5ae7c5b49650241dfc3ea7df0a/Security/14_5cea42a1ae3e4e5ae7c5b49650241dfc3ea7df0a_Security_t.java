 package controllers;
 
 import models.User;
 
 
 /**
  * Configuration of the Secure module.
  */
 public class Security extends Secure.Security {
 	
 	public static final String ADMIN_PROFILE_CHECK = "admin";
 	
 	/**
 	 * This method defines how to authenticate users.
 	 * @param login user name
 	 * @param password user password
	 * @return true if the user is authenticated, false otherwise
 	 */
 	static boolean authenticate(final String login, final String password) {
 		boolean result = User.count("byLoginAndPassword", login, password) != 0;
 		return result;
 	}
 
 	/**
 	 * Url to redirect after autentication.
 	 */
 	static void onAuthenticated() {
 		Application.index();
 	}
 	
 	static boolean check(String profile) {
 		if(ADMIN_PROFILE_CHECK.equals(profile) && Security.isConnected()) {
 			User user = User.find("byLogin", Security.connected()).first();
			return (user != null && user.isAdmin);
 		}
 		
 		return false;
 	}
 }
