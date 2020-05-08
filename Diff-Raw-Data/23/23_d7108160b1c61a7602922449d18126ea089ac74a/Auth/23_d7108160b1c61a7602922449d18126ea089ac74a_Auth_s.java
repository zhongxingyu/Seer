 package controllers;
 
 import java.util.Collection;
 
 import edu.emory.mathcs.backport.java.util.LinkedList;
 
 import models.INotification;
 import models.impl.Notification;
 import models.impl.User;
 import play.Logger;
 import play.mvc.Controller;
 
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.With;
 import utils.QaDB;
 
 /**
  * The Class Auth.
  */
 public class Auth extends Controller{
 
 	/** The user. */
	static User user;
 
 	/**
 	 * Sets the logged in user.
 	 */
 	@Before
 	static void setUser() {
 		if (Security.isConnected()) {
			user = QaDB.findUserByName(Security.connected());
			
			
 			renderArgs.put("username", user.getName());
 			renderArgs.put("user", user);
 		}
 		
 		Collection<INotification> notifications = 
 			user != null ? 
 					user.getNotifications():
 						new LinkedList();
 		renderArgs.put("notifications", notifications);
 	}	
 	
 	
 }
