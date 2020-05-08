 package uk.ac.cam.dashboard.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.jboss.resteasy.annotations.Form;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.dashboard.forms.DeadlineForm;
 import uk.ac.cam.dashboard.models.Deadline;
 import uk.ac.cam.dashboard.models.Group;
 import uk.ac.cam.dashboard.models.User;
 
 import com.google.common.collect.ImmutableMap;
 import com.googlecode.htmleasy.RedirectException;
 
 @Path("/dashboard/deadlines")
 public class DeadlinesController extends ApplicationController {
 	
 	private User currentUser;
 	
 	// Logger
 	private static Logger log = LoggerFactory.getLogger(DeadlinesController.class);
 	
 	// Index 
 	@GET @Path("/") 
 	@Produces(MediaType.APPLICATION_JSON)
 	public ImmutableMap<String, ?> indexDeadlines() {
 
 		currentUser = initialiseUser();
 		
		return ImmutableMap.of("crsid", currentUser.getCrsid(), "deadlines", currentUser.getUserDeadlinesMap(), "cdeadlines", currentUser.getUserCreatedDeadlinesMap());
 	}
 	
 	// Create
 	@POST @Path("/") 
 	public void createGroup(@Form DeadlineForm deadlineForm) throws Exception {
 		currentUser = initialiseUser();
 		
 		int id = deadlineForm.handleCreate(currentUser);
 		
 		throw new RedirectException("/app/#signapp/deadlines");
 	}
 	
 
 //	// Edit
 //	@GET @Path("/{id}/edit") //@ViewWith("/soy/deadlines.edit")
 //	@Produces(MediaType.APPLICATION_JSON)
 //	public Map editDeadline(@PathParam("id") int id) {
 //		
 //		currentUser = initialiseUser();
 //		
 //	  	Deadline deadline = Deadline.getDeadline(id);
 //	  	
 //	  	if(deadline==null){
 //	  		//throw new RedirectException("/app/#signapp/deadlines/error/1");
 //	  		return ImmutableMap.of("redirect", "signapp/deadlines/error/1");
 //	  	}
 //	  	if(!deadline.getOwner().equals(currentUser)){
 //	  		//throw new RedirectException("/app/#signapp/deadlines/error/2");
 //	  		return ImmutableMap.of("redirect", "signapp/deadlines/error/2");
 //	  	}
 //		return deadline.toMap();		
 //	}
 	
 //	// Update
 //	@POST @Path("/{id}/edit")
 //	public void updateDeadline(@Form DeadlineForm deadlineForm, @PathParam("id") int id) {
 //		
 //		currentUser = initialiseUser();
 //		
 //		id = deadlineForm.handleUpdate(currentUser, id);
 //		
 //		throw new RedirectException("/app/#signapp/deadlines");
 //	}
 	
 	
 	// Delete
 	@DELETE @Path("/{id}")
 	public void deleteDeadline(@PathParam("id") int id) {
 
 		/*
 		if ( validateRequest() == Permissions.NO_PERMISSIONS ) {
 			throw new RedirectException(NotificationsController.class, "errorCallback");
 		} else if ( validateRequest() == Permissions.GLOBAL_API ) {
 			// A user must be provided
 			throw new RedirectException(NotificationsController.class, "errorCallback");
 		} else if ( validateRequest() == Permissions.GLOBAL_API_WITH_USER ) {
 			currentUser = initialiseSpecifiedUser(sRequest.getParameter("user"));
 		} else if ( validateRequest() == Permissions.USER_API ) {
 			currentUser = initialiseSpecifiedUser(sRequest.getParameter("user"));
 		} else if ( validateRequest() == Permissions.RAVEN_SESSION ) {
 			currentUser = initialiseUser();
 		}
 		*/
 
 		Deadline.deleteDeadline(id);
 		
 		throw new RedirectException("/app/#signapp/deadlines");
 	}
 	
 	// Errors
 	@GET @Path("/error/{type}") 
 	@Produces(MediaType.APPLICATION_JSON)
 	public Map deadlineErrors(@PathParam("type") int error){
 		
 		currentUser = initialiseUser();
 		
 		ImmutableMap<String, ?> errors = ImmutableMap.of("get", (error==1), "auth", (error==2), "noname", (error==3), "inpast", (error==4));
 
 		return ImmutableMap.of("crsid", currentUser.getCrsid(), "deadlines", currentUser.getUserDeadlinesMap(), "cdeadlines", currentUser.getUserCreatedDeadlinesMap(), "errors", errors);
 	}
 	
 	// Find groups AJAX
 	@POST @Path("/queryGroup")
 	@Produces(MediaType.APPLICATION_JSON)
 	public List queryCRSID(String q) {
 		currentUser = initialiseUser();
 		String crsid = currentUser.getCrsid();
 		
 		//Remove q= prefix
 		String x = q.substring(2);
 		
 		//List of group matches
 		ArrayList<ImmutableMap<String,?>> matches = new ArrayList<ImmutableMap<String, ?>>();
 		
 		//Get matching group names.. is this too slow? 
 		for(Group g : currentUser.getGroups()){
 			if(g.getTitle().contains(x)){
 				matches.add(ImmutableMap.of("group_id", g.getId(), "group_name", g.getTitle()));
 			}
 		}
 		
 		return matches;
 	}
 }
