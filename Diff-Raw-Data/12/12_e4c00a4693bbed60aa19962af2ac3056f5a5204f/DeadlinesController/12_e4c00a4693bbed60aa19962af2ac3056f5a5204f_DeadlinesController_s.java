 package uk.ac.cam.dashboard.controllers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.hibernate.Session;
 import org.jboss.resteasy.annotations.Form;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.cl.dtg.ldap.LDAPObjectNotFoundException;
 import uk.ac.cam.cl.dtg.ldap.LDAPPartialQuery;
import uk.ac.cam.cl.dtg.ldap.LDAPQueryManager;
import uk.ac.cam.cl.dtg.ldap.LDAPUser;
 import uk.ac.cam.dashboard.exceptions.AuthException;
 import uk.ac.cam.dashboard.forms.CreateDeadlineForm;
 import uk.ac.cam.dashboard.forms.GetDeadlineForm;
 import uk.ac.cam.dashboard.models.Deadline;
 import uk.ac.cam.dashboard.models.DeadlineUser;
 import uk.ac.cam.dashboard.models.Group;
 import uk.ac.cam.dashboard.models.User;
 import uk.ac.cam.dashboard.queries.DeadlineQuery;
 import uk.ac.cam.dashboard.util.HibernateUtil;
import uk.ac.cam.dashboard.util.Mail;
import uk.ac.cam.dashboard.util.Strings;
 import uk.ac.cam.dashboard.util.Util;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.ImmutableMap;
 
 @Path("/api/deadlines")
 @Produces(MediaType.APPLICATION_JSON)
 public class DeadlinesController extends ApplicationController {
 	
 	// Create the logger
 	private static Logger log = LoggerFactory.getLogger(GroupsController.class);
 	
 	private User currentUser;
 	
 	// Get deadlines
 	@GET @Path("/")
 	public Map<String, ?> getSetDeadlines(@Form GetDeadlineForm deadlineForm) {
 		
 		return getDeadlines(deadlineForm, false);
 		
 	}
 	
 	@GET @Path("/archive")
 	public Map<String, ?> getArchivedSetDeadlines(@Form GetDeadlineForm deadlineForm) {
 		
 		return getDeadlines(deadlineForm, true);
 		
 	}
 	
 	public Map<String, ?> getDeadlines(GetDeadlineForm deadlineForm, boolean archived) {
 
 		try {
 			currentUser = validateUser();
 		} catch (AuthException e) {
 			return ImmutableMap.of("error", e.getMessage());
 		}
 		
 		ImmutableMap<String, List<String>> errors = deadlineForm.validate();
 
 		if (errors.isEmpty()) {
 			return deadlineForm.handle(currentUser, archived);
 		} else {
 			return ImmutableMap.of("formErrors", errors, "data", deadlineForm.toMap());
 		}
 		
 	}
 	
 	// Manage
 	@GET @Path("/{id}") 
 	public Map<String, ?> getDeadline(@PathParam("id") int id) {
 		
 		try {
 			currentUser = validateUser();
 		} catch (AuthException e) {
 			return ImmutableMap.of("error", e.getMessage());
 		}
 		
 	  	Deadline deadline = DeadlineQuery.get(id);
 	  	
 	  	if(!deadline.getOwner().equals(currentUser)){
 	  		return ImmutableMap.of("redirectTo", "deadlines");
 	  	}
 	  	
 		return ImmutableMap.of("deadline", deadline.toMap(), "deadlineEdit", deadline.toMap(), "users", deadline.usersToMap(), "errors", "undefined");		
 	}
 	
 	// Create
 	@POST @Path("/") 
 	public Map<String, ?> createDeadline(@Form CreateDeadlineForm deadlineForm) throws Exception {
 
 		try {
 			currentUser = validateUser();
 		} catch (AuthException e) {
 			return ImmutableMap.of("error", e.getMessage());
 		}
 		
 		ArrayListMultimap<String, String> errors = deadlineForm.validate();
 		ImmutableMap<String, List<String>> actualErrors = Util.multimapToImmutableMap(errors);
 		
 		if(errors.isEmpty()){
 			int id = deadlineForm.handleCreate(currentUser);
 			return ImmutableMap.of("redirectTo", "deadlines/"+id);
 		} else {
 			return ImmutableMap.of("deadline", deadlineForm.toMap(-1), "errors", actualErrors);
 		}
 	}
 	
 	// Update
 	@POST @Path("/{id}")
 	public Map<String, ?> updateDeadline(@Form CreateDeadlineForm deadlineForm, @PathParam("id") int id) {
 		
 		try {
 			currentUser = validateUser();
 		} catch (AuthException e) {
 			return ImmutableMap.of("error", e.getMessage());
 		}
 		
 		ArrayListMultimap<String, String> errors = deadlineForm.validate();
 		ImmutableMap<String, List<String>> actualErrors = Util.multimapToImmutableMap(errors);
 		
 		if(errors.isEmpty()){
 			deadlineForm.handleUpdate(currentUser, id);
 			return ImmutableMap.of("redirectTo", "deadlines/"+id);
 		} else {
 			return ImmutableMap.of("errors", actualErrors, "deadlineEdit", deadlineForm.toMap(id), "target", "edit");
 		}
 	}
 	
 	// Delete
 	@DELETE @Path("/{id}")
 	public Map<String, ?> deleteDeadline(@PathParam("id") int id) {
 
 		Session session = HibernateUtil.getTransactionSession();
 				
 		Deadline deadline = DeadlineQuery.get(id);
 	
 	  	session.delete(deadline);
 		
 		return ImmutableMap.of("success", "true", "id", id);
 		
 	}
 	
 	// Mark as complete/not complete
 	@PUT @Path("/{id}/complete")
 	public Map<String, ?> updateComplete(@PathParam("id") int id) {
 		
 		DeadlineUser d = DeadlineQuery.getDUser(id);
 		
 		if(d.getComplete()){ d.toggleComplete(false);
 		} else { d.toggleComplete(true); }
 
 		return d.toMap();
 	}
 	
 	// Mark as archived
 	@PUT @Path("/{id}/archive")
 	public Map<String, ?> updateArchive(@PathParam("id") int id) {
 		
 		DeadlineUser d = DeadlineQuery.getDUser(id);
 		
 		if(d.getArchived()){ d.toggleArchived(false);
 		} else { d.toggleArchived(true); }
 
 		return d.toMap();
 	}
 	
 	// Find users by crsid
 	@POST @Path("/queryCRSID")
 	public List<HashMap<String, String>> queryCRSId(@FormParam("q") String x) {
 		
 		List<HashMap<String, String>> matches = null;
 		try {
 			matches = LDAPPartialQuery.partialUserByCrsid(x);
 		} catch (LDAPObjectNotFoundException e){
 			log.error("Error performing LDAPQuery: " + e.getMessage());
 			return new ArrayList<HashMap<String, String>>();
 		}
 		
 		return matches;
 	}
 	
 	// Find groups
 	@POST @Path("/queryGroup")
 	public List<ImmutableMap<String, ?>> queryCRSID(@FormParam("q") String x) {
 		
 		try {
 			currentUser = validateUser();
 		} catch (AuthException e) {
 			return new ArrayList<ImmutableMap<String, ?>>();
 		}
 		
 		ArrayList<ImmutableMap<String,?>> matches = new ArrayList<ImmutableMap<String, ?>>();
 		
 		for(Group g : currentUser.getGroups()){
 				matches.add(ImmutableMap.of("group_id", g.getId(), "group_name", g.getTitle()));
 		}
 		
 		return matches;
 	}
 	
 }
