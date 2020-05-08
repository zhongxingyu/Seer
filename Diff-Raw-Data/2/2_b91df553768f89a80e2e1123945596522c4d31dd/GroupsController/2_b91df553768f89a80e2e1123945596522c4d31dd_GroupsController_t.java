 package uk.ac.cam.dashboard.controllers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
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
 import uk.ac.cam.dashboard.forms.GroupForm;
 import uk.ac.cam.dashboard.models.Group;
 import uk.ac.cam.dashboard.models.User;
 import uk.ac.cam.dashboard.queries.GroupQuery;
 import uk.ac.cam.dashboard.queries.UserQuery;
 import uk.ac.cam.dashboard.util.HibernateUtil;
 import uk.ac.cam.dashboard.util.Util;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.ImmutableMap;
 
 @Path("/api/groups")
 @Produces(MediaType.APPLICATION_JSON)
 public class GroupsController extends ApplicationController {
 	
 		// Create the logger
 		private static Logger log = LoggerFactory.getLogger(GroupsController.class);
 		
 		// Get current user from raven session
 		private User currentUser;
 		
 		// Index
 		@GET @Path("/") 
 		public Map<String, ?> indexGroups() {
 			
 			try {
 				currentUser = validateUser();
 			} catch(AuthException e){
 				return ImmutableMap.of("error", e.getMessage());
 			}
 						
 			return ImmutableMap.of("user", currentUser.toMap(), "groups", currentUser.subscriptionsToMap());
 		}
 		
 		// Members
 		@GET @Path("/{id}/members") 
 		public Map<String, ?> groupMembers(@PathParam("id") int id) {
 
 			Group group = GroupQuery.get(id);
 	
 			List<HashMap<String, Object>> users = null;
 			try {
 				users = new ArrayList<HashMap<String, Object>>();
 				for(User u : UserQuery.all().byGroup(group).list()){
 					LDAPUser user = LDAPQueryManager.getUser(u.getCrsid());
 					HashMap<String, Object> userMap = user.getAll();
 					userMap.put("supervisor", Boolean.toString(u.getSettings().getSupervisor()));
 					users.add(userMap);
 				}
 			} catch(LDAPObjectNotFoundException e){
 				log.error("Error performing LDAPQuery: " + e.getMessage());
 				users = new ArrayList<HashMap<String, Object>>();
 			}
 			
 			return ImmutableMap.of("group", group.toMap(), "users", users);
 		}
 		
 		// Create
 		@POST @Path("/") 
 		public Map<String, ?> createGroup(@Form GroupForm groupForm) throws Exception {
 			
 			try {
 				currentUser = validateUser();
 			} catch(AuthException e){
 				return ImmutableMap.of("error", e.getMessage());
 			}
 			
 			ArrayListMultimap<String, String> errors = groupForm.validate();
 			ImmutableMap<String, List<String>> actualErrors = Util.multimapToImmutableMap(errors);
 			
 			if(errors.isEmpty()){
 				int id = groupForm.handle(currentUser);
 				return ImmutableMap.of("redirectTo", "groups/manage/"+id, "success", true);
 			} else {
 				return ImmutableMap.of("group", groupForm.toMap(-1), "errors", actualErrors, "users", groupForm.usersToMap(), "success", false);
 			}
 		}
 		
 		// Import 
 		@POST @Path("/import") 
 		public Map<String, ?> importGroup(@Form GroupForm groupForm) throws Exception {
 			
 			try {
 				currentUser = validateUser();
 			} catch(AuthException e){
 				return ImmutableMap.of("error", e.getMessage());
 			}
 			
 			ArrayListMultimap<String, String> errors = groupForm.validateImport();
 			ImmutableMap<String, List<String>> actualErrors = Util.multimapToImmutableMap(errors);
 			
 			if(errors.isEmpty()){
 				int id = groupForm.handleImport(currentUser);
 				return ImmutableMap.of("redirectTo", "groups/manage/"+id, "success", true);
 			} else {
 				return ImmutableMap.of("group", "undefined", "errors", actualErrors, "success", false);
 			}
 		}
 		
 		// Manage
 		@GET @Path("/manage/{id}") 
 		public Map<String, ?> getGroup(@PathParam("id") int id) {
 			
 			try {
 				currentUser = validateUser();
 			} catch(AuthException e){
 				return ImmutableMap.of("error", e.getMessage());
 			}
 			
 			Group group = GroupQuery.get(id);
 			
 		  	if(!group.getOwner().equals(currentUser)){
 		  		return ImmutableMap.of("redirectTo", "groups");
 		  	}
 
 			List<Map<String, Object>> users = null;
 				users = new ArrayList<Map<String, Object>>();
				for(User u : UserQuery.all().byGroup(group).list()){
 					users.add(u.getUserDetails());
 				}
 			
 			return ImmutableMap.of("group", group.toMap(), "errors", "undefined", "users", users);
 		}
 		
 		// Update
 		@POST @Path("/{id}")
 		public Map<String, ?> updateGroup(@Form GroupForm groupForm, @PathParam("id") int id) {	
 			
 			try {
 				currentUser = validateUser();
 			} catch(AuthException e){
 				return ImmutableMap.of("error", e.getMessage());
 			}			
 			
 			ArrayListMultimap<String, String> errors = groupForm.validate();
 			ImmutableMap<String, List<String>> actualErrors = Util.multimapToImmutableMap(errors);
 			
 			if(errors.isEmpty()){
 				Group group = groupForm.handleUpdate(currentUser, id);
 				List<Map<String, Object>> users = null;
 				users = new ArrayList<Map<String, Object>>();
 				for(User u : group.getUsers()){
 					users.add(u.getUserDetails());
 				}
 				return ImmutableMap.of("group", group.toMap(), "users", users, "errors", "undefined", "target", "statistics", "success", true);
 				
 			} else {
 				return ImmutableMap.of("group", groupForm.toMap(id), "users", groupForm.usersToMap(), "errors", actualErrors, "success", false);
 			}
 		}
 		
 		// Delete
 		@DELETE @Path("/{id}")
 		public Map<String, ?> deleteGroup(@PathParam("id") int id) {
 			
 			Session session = HibernateUtil.getTransactionSession();
 			
 			Group group = GroupQuery.get(id);
 
 		  	session.delete(group);
 			
 			return ImmutableMap.of("redirectTo", "/supervisor/");
 			
 		}		
 		
 		// Find users by crsid
 		@POST @Path("/queryCRSID")
 		public List<HashMap<String, String>> queryCRSId(@FormParam("q") String x) {
 						
 			if(x==null){
 				return new ArrayList<HashMap<String,String>>();
 			}
 			
 			List<HashMap<String, String>> matches = null;
 			try {
 				matches = LDAPPartialQuery.partialUserByCrsid(x);
 			} catch (LDAPObjectNotFoundException e){
 				log.error("Error performing LDAPQuery: " + e.getMessage());
 				return new ArrayList<HashMap<String, String>>();
 			}
 			
 			return matches;
 		}
 		
 		// Find users by surname
 		@POST @Path("/querySurname")
 		public List<HashMap<String, String>> querySurname(@FormParam("q") String x) {
 			
 			// Perform LDAP search
 			List<HashMap<String, String>> matches = null;
 			try {
 				matches = LDAPPartialQuery.partialUserBySurname(x);
 			} catch (LDAPObjectNotFoundException e){
 				log.error("Error performing LDAPQuery: " + e.getMessage());
 				return new ArrayList<HashMap<String, String>>();
 			}
 			
 			return matches;
 		}
 		
 		// Find groups from LDAP
 		@POST @Path("/queryGroup")
 		public List<HashMap<String, String>> queryGroup(@FormParam("q") String x) {
 			
 			List<HashMap<String, String>> matches = null;
 			try {
 				matches = LDAPPartialQuery.partialGroupByName(x);
 			} catch (LDAPObjectNotFoundException e){
 				log.error("Error performing LDAPQuery: " + e.getMessage());
 				return new ArrayList<HashMap<String, String>>();
 			}			
 			return matches;
 		}	
 }
