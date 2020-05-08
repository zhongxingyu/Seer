 package controllers;
 
 import java.util.Date;
 
 import javax.persistence.Query;
 
 import models.Author;
 
 import play.db.jpa.JPA;
 
 public class Security extends Secure.Security {
 
 	/**
 	 * This method is called during the authentication process. This is where
 	 * you check if the user is allowed to log in into the system. This is the
 	 * actual authentication process against a third party system (most of the
 	 * time a DB).
 	 * 
 	 * @param username
 	 * @param password
 	 * @return true if the authentication process succeeded
 	 */
 	static boolean authenticate(String username, String password) {
 		// password-hash to be implemented.
 		Query q = JPA.em().createQuery(
 				"SELECT a.password FROM Author a WHERE a.name='" + username
 						+ "'");
 		if (q.getResultList().isEmpty())
 			return false;
 		else
			return q.getResultList().get(0).equals(password/*.hashCode()*/);
 	}
 
 	/**
 	 * This method is called after a successful authentication. You need to
 	 * override this method if you with to perform specific actions (eg. Record
 	 * the time the user signed in)
 	 */
 	static void onAuthenticated() {
 		// Stores the date of the last login.
 		Author a = Author.findByName(session.get("username"));
 		a.lastLogin = new Date();
 		a.save();
 	}
 
 }
