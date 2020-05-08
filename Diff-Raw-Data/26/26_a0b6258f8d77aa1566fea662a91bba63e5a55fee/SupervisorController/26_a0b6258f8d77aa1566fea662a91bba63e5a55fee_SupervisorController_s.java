 package uk.ac.cam.dashboard.controllers;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import uk.ac.cam.dashboard.models.Deadline;
 import uk.ac.cam.dashboard.models.User;
import uk.ac.cam.dashboard.queries.DeadlineQuery;
 
 import com.google.common.collect.ImmutableMap;
 
 @Path("api/dashboard/supervisor")
 public class SupervisorController extends ApplicationController {
 	
 	private User currentUser;
 	
 	// Logger
 	private static Logger log = LoggerFactory.getLogger(SupervisorController.class);
 	
 	// Index 
 	@GET @Path("/") 
 	@Produces(MediaType.APPLICATION_JSON)
 	public ImmutableMap<String, ?> indexSupervisor() {
 
 		currentUser = initialiseUser();
 		
 		log.debug("Returning JSON of user, user created deadlines and user created groups");
 		return ImmutableMap.of("user", currentUser.toMap(), "cdeadlines", currentUser.createdDeadlinesToMap(), "cgroups", currentUser.groupsToMap());
 	}
 }
