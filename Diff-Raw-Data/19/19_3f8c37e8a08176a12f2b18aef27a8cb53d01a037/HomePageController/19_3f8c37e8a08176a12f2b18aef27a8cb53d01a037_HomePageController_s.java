 package uk.ac.cam.dashboard.controllers;
 
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
//Import the following for logging
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.dashboard.exceptions.AuthException;
 import uk.ac.cam.dashboard.forms.GetNotificationForm;
 //Import models
 import uk.ac.cam.dashboard.models.User;
 
 import com.google.common.collect.ImmutableMap;
 
 @Path("/api/")
 @Produces(MediaType.APPLICATION_JSON)
 public class HomePageController extends ApplicationController{
 	
 	// Logger
 	private static Logger log = LoggerFactory.getLogger(HomePageController.class);
 	
 	private User currentUser;
 	
 	@GET @Path("/")
 	public Map<String, ?> homePage() {
 		
 		try {
 			currentUser = validateUser();
 		} catch(AuthException e){
 			return ImmutableMap.of("error", e.getMessage());
 		}
 		
 		Map<String, String> userData = currentUser.getUserDetails();
 		
 		// Get notifications
 		GetNotificationForm notificationForm = new GetNotificationForm();
 		notificationForm.validate();
 		
 		return ImmutableMap.of("user", userData, "deadlines", currentUser.setDeadlinesToMap(), "userNotifications", notificationForm.handle(currentUser, false));
 	}
 
 }
