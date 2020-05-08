 package uk.ac.cam.dashboard.controllers;
 
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.dashboard.exceptions.AuthException;
 //Import models
 import uk.ac.cam.dashboard.models.User;
 
 import com.google.common.collect.ImmutableMap;
 
 @Path("/api/")
 @Produces(MediaType.APPLICATION_JSON)
 public class HomePageController extends ApplicationController{
 	
 	// Logger
 	private static Logger log = LoggerFactory.getLogger(HomePageController.class);
 	
 	private User currentUser;
 	
 	@GET @Path("/error")
 	public ImmutableMap<String, ?> accessDenied() {
 		return ImmutableMap.of();
 	}
 	
 	@GET @Path("/")
 	public Map<String, ?> homePage() {
 		
 		try {
 			currentUser = validateUser();
 		} catch(AuthException e){
 			return ImmutableMap.of("error", e.getMessage());
 		}
 		
 		Map<String, Object> userData = currentUser.getUserDetails();
 		
 		return ImmutableMap.of("user", userData, "supervisor", currentUser.getSupervisor(), "services", currentUser.getSettings().toMap());
 	}
 
 }
