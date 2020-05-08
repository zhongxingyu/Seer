 package controllers;
 
 import models.Role;
 import models.User;
 import play.Logger;
 
 public class Security extends Secure.Security {
 
 	static boolean authenticate(String username, String password) {
 		Logger.debug("[authenticate] Start... auth code: %s", username);
 		if (username == null) {
 			return false;
 		}
 		User user = User.find("byAuthCode", username).first();
 		if (user == null) {
 			user = User.find("byNewAuthCode", username).first();
 			if (user != null) {
 				Logger.info("New auth code applied to user: %s", user.email);
 				user.authCode = user.newAuthCode;
 				user.newAuthCode = null;
 				user.save();
 			}
 		}
 		return user != null;
 	}
 
 	static User connectedUser() {
 		return User.find("byEmail", connected()).first();
 	}
 
 	static void onAuthenticated() {
 		Logger.info("User logged in: %s", connectedUser());
 	}
 
 	static boolean check(String role) {
 		Logger.debug("[Security.check] Start - role: %s", role);
 		User user = connectedUser();
 		if (user == null) {
 			Logger.debug("[Security.check] Failed - user not logged in");
 			return false;
 		}
 		if (Role.ADMIN.equals(role)) {
 			if (user.isAdmin) {
 				Logger.debug("[Security.check] OK - user: %s", user.email);
 			} else {
 				Logger.debug("[Security.check] Failed - user is not admin: %s", user.email);
 			}
 			return user.isAdmin;
 		}
 		Logger.debug("[Security.check] Failed - unknown role: %s", role);
 		return false;
 	}
 
 }
