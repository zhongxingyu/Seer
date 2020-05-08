 package uk.ac.cam.dashboard.controllers;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.core.Context;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.dashboard.models.User;
 import uk.ac.cam.dashboard.util.UserLookupManager;
 
import com.googlecode.htmleasy.RedirectException;

 public class ApplicationController {
 	// Logger
 	private static Logger log = LoggerFactory.getLogger(ApplicationController.class);
 	
 	// UserLookupManager for this user
 	protected UserLookupManager ulm;
 	
 	// Raven session
 	@Context
 	HttpServletRequest sRequest;
 	
 	protected User initialiseUser() throws RedirectException { 
 		
 		//log.debug("Getting crsid from raven");
 		
 		//sRequest = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
 		String crsid = (String) sRequest.getSession().getAttribute("RavenRemoteUser");
 
 		
 		if (crsid != null) {
 			ulm = UserLookupManager.getUserLookupManager(crsid);
 			return User.registerUser(crsid);
 		}
 		
 		throw new RedirectException("/app#dashboard/");
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
