 package controllers;
 
 import models.User;
 import play.modules.gae.GAE;
 import play.mvc.Before;
 import play.mvc.Controller;
 
public class Security extends Controller {
  
 	public static boolean isConnected() {
 		return connected() != null;
 	}
 	
 	public static String connected() {
 		
 		if (GAE.getUser() != null) {
 			return GAE.getUser().getEmail();
 		} else {
 			return null;
 		}
 	}
	
	public static void login() {
		GAE.login("Application.index");
	}
	
	public static void logout() {
		GAE.logout("Application.index");
	}
 
 	public static class Secure extends Controller {
 		
 		@Before(unless="logout")
 		static void forceLogin() {
 			if (!GAE.isLoggedIn()) {
 				GAE.login();
 			}
 		}
 		
 		@Before(unless="logout")
 		static void registerNewUsers() {
 			if (GAE.isLoggedIn()) {
 				User existingUser = User.findByEmail(GAE.getUser().getEmail());
 				if (existingUser==null) {
 					User newUser = new User(GAE.getUser().getEmail());
 					newUser.insert();
 				}
 			}
 		}
 		
 		public static void logout() {
 			GAE.logout("/");
 		}
 	}
 	
 	public static class AdminOnly extends Controller {
 		
 		@Before
 		static void adminOnly() {
 			
 			if (!GAE.isLoggedIn()) {
 				GAE.login();
 			}
 			
 			if (!GAE.isAdmin()) {
 				forbidden();
 			}
 		}
 	}
 }
