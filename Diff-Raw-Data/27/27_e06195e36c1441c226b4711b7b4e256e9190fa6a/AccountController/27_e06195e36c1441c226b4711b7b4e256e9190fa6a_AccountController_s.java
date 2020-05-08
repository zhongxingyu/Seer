 package uk.ac.cam.dashboard.controllers;
 
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.hibernate.Session;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.dashboard.models.Settings;
 import uk.ac.cam.dashboard.models.User;
 import uk.ac.cam.dashboard.util.HibernateUtil;
 
 import com.google.common.collect.ImmutableMap;
 
 @Path("api/dashboard/account")
 @Produces(MediaType.APPLICATION_JSON)
 public class AccountController extends ApplicationController {
 
 	// Create the logger
 	private static Logger log = LoggerFactory
 			.getLogger(NotificationsController.class);
 
 	// Get current user from raven session
 	private User currentUser;
 
 	@GET
 	@Path("/")
 	public Map<String, ?> getAccountSettings() {
 		currentUser = initialiseUser();
 		return ImmutableMap.of("user", currentUser.getSettings());
 	}
 
 	@PUT @Path("/")
 	public Map<String, ?> changeAccountSettings(@QueryParam("signups") Boolean signups,
 												@QueryParam("questions") Boolean questions,
 												@QueryParam("handins") Boolean handins) {
 		currentUser = initialiseUser();
 		Session session = HibernateUtil.getTransactionSession();
 		Settings newUserSettings = currentUser.getSettings();
 		
 		try {
 			if (signups != null) newUserSettings.setSignupsOptIn(signups);
 			if (questions != null) newUserSettings.setQuestionsOptIn(questions);
 			if (handins != null) newUserSettings.setHandinsOptIn(handins);
 			
 			currentUser.setSettings(newUserSettings);
 			session.save(newUserSettings);
 			session.update(currentUser);
 		} catch (Exception e) {
 			return ImmutableMap.of("errors", e.getMessage());
 		}
 		
 		return ImmutableMap.of("redirectTo", "dasboard/account");
 	}
 }
