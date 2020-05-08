 package uk.ac.cam.dashboard.controllers;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.core.Context;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.dashboard.models.User;
 import uk.ac.cam.dashboard.util.UserLookupManager;
 
 public class ApplicationController {
 	// Logger
 	private static Logger log = LoggerFactory.getLogger(ApplicationController.class);
 	
 	// UserLookupManager for this user
 	protected UserLookupManager ulm;
 	
 	// Raven session
 	@Context
 	HttpServletRequest sRequest;
 	
	protected User initialiseUser() {
 		
 		log.debug("Getting crsid from raven");
 		
		// String crsid = (String) sRequest.getSession().getAttribute("RavenRemoteUser");
		String crsid = "jd658";
 		
		ulm = UserLookupManager.getUserLookupManager(crsid);
 		
		return User.registerUser(crsid);
 	}
 	
 	// temporary for testing
 	protected User initialiseSpecifiedUser(String crsid) {
 		
 		// Create UserLookupManager for this user
 		log.debug("Creating userLookupManager");	
 		ulm = UserLookupManager.getUserLookupManager(crsid);
 		
 		// Register or return the user
 		return User.registerUser(crsid);
 	}
 	
 }
