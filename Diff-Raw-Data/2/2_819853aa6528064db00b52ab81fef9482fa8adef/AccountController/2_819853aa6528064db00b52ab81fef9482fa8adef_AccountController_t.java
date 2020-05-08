 package uk.ac.cam.dashboard.controllers;
 
 import java.util.LinkedList;
 import java.util.List;
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
 import uk.ac.cam.dashboard.queries.NotificationQuery;
 import uk.ac.cam.dashboard.util.HibernateUtil;
 
 import com.google.common.collect.ImmutableMap;
 
 @Path("/api/account")
 @Produces(MediaType.APPLICATION_JSON)
 public class AccountController extends ApplicationController {
 
 	// Create the logger
 	private static Logger log = LoggerFactory.getLogger(NotificationsController.class);
 
 	// Get current user from raven session
 	private User currentUser;
 
 	@GET
 	@Path("/")
 	public Map<String, ?> getAccountSettings(@QueryParam("userId") String userId) {
 		
 		try {
 			currentUser = validateUser();
 		} catch (Exception e) {
 			return ImmutableMap.of("error", e.getMessage());
 		}
 		
 		return ImmutableMap.of("user", currentUser.getSettings(), "sidebar", getSidebarLinkHierarchy(currentUser));
 
 	}
 
 	@PUT @Path("/")
 	public Map<String, ?> changeAccountSettings(@QueryParam("signups") Boolean signups,
 												@QueryParam("questions") Boolean questions,
 												@QueryParam("handins") Boolean handins,
 												@QueryParam("userId") String userId) {
 		
 		try {
 			currentUser = validateUser();
 		} catch (Exception e) {
 			return ImmutableMap.of("error", e.getMessage());
 		}
 		
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
 			return ImmutableMap.of("error", e.getMessage());
 		}
 		
 		return ImmutableMap.of("redirectTo", "dashboard/account");
 	}
 		
 	private List<Object> getSidebarLinkHierarchy(User user) {
 		
 		Settings settings = user.getSettings();
 		
 		// Get notifications associated with sections
 		int signupsNotifications = NotificationQuery.all().byUser(user).inSection("signups").isRead(false).totalRows();
 		int questionsNotifications = NotificationQuery.all().byUser(user).inSection("questions").isRead(false).totalRows();
 		int handinsNotifications = NotificationQuery.all().byUser(user).inSection("handins").isRead(false).totalRows();
 		
 		int dashboardNotifications = NotificationQuery.all().byUser(user).inSection("dashboard").isRead(false).totalRows() + signupsNotifications 
 																											 + questionsNotifications 
 																											 + handinsNotifications;
 		
 		List<Object> sidebar = new LinkedList<Object>();
 		
 		// Dashboard
 		List<Object> dashboard = new LinkedList<Object>();
		dashboard.add(ImmutableMap.of("name", "Home", "link", "/dashboard/", "icon", "icon-globe", "iconType", 1, "notificationCount", 0));
 		dashboard.add(ImmutableMap.of("name", "Notifications", "link", "/dashboard/notifications", "icon", "icon-newspaper", "iconType", 1, "notificationCount", 0));
 		dashboard.add(ImmutableMap.of("name", "Deadlines", "link", "/dashboard/deadlines", "icon", "icon-ringbell", "iconType", 1, "notificationCount", 0));
 		dashboard.add(ImmutableMap.of("name", "Groups", "link", "/dashboard/groups", "icon", "icon-users", "iconType", 1, "notificationCount", 0));
 		if (user.getSupervisor()) dashboard.add(ImmutableMap.of("name", "Supervisor Homepage", "link", "/dashboard/supervisor", "icon", "icon-users", "iconType", 1, "notificationCount", 0));
 		
 		ImmutableMap.Builder<String, Object> dashboardMap = new ImmutableMap.Builder<String, Object>();
 		dashboardMap.put("name", "Dashboard");
 		dashboardMap.put("links", dashboard);
 		dashboardMap.put("section", "dashboard");
 		dashboardMap.put("icon", "a");
 		dashboardMap.put("iconType", 2);
 		dashboardMap.put("notificationCount", dashboardNotifications);
 		sidebar.add(dashboardMap.build());
 		
 		// Signups
 		if (settings.isSignupsOptIn()) {
 			List<Object> signups = new LinkedList<Object>();
 			signups.add(ImmutableMap.of("name", "Events", "link", "/signapp/events", "icon", "?", "iconType", 2, "notificationCount", 0));
 			signups.add(ImmutableMap.of("name", "Create new event", "link", "/signapp/events/new", "icon", "?", "iconType", 2, "notificationCount", 0));
 			signups.add(ImmutableMap.of("name", "Walker vision", "link", "/signapp/events/walkerVision", "icon", "?", "iconType", 2, "notificationCount", 0));
 			
 			ImmutableMap.Builder<String, Object> sidebarMap = new ImmutableMap.Builder<String, Object>();
 			sidebarMap.put("name", "Timetable/Signups");
 			sidebarMap.put("links", signups);
 			sidebarMap.put("section", "signups");
 			sidebarMap.put("icon", "P");
 			sidebarMap.put("iconType", 2);
 			sidebarMap.put("notificationCount", signupsNotifications);
 			sidebar.add(sidebarMap.build());
 		}
 		
 		// Questions
 		if (settings.isQuestionsOptIn()) {
 			List<Object> questions = new LinkedList<Object>();
 			questions.add(ImmutableMap.of("name", "Browse own content", "link", "/questions/users/me", "icon", "icon-file_open", "iconType", 1, "notificationCount", 0));
 			questions.add(ImmutableMap.of("name", "Browse questions", "link", "/questions/q/search", "icon", "icon-list", "iconType", 1, "notificationCount", 0));
 			questions.add(ImmutableMap.of("name", "Browse question sets", "link", "/questions/sets", "icon", "icon-list", "iconType", 1, "notificationCount", 0));
 			questions.add(ImmutableMap.of("name", "Create question", "link", "/questions/q/add", "icon", "icon-plus", "iconType", 1, "notificationCount", 0));
 			questions.add(ImmutableMap.of("name", "Create question set", "link", "/questions/sets/add", "icon", "icon-plus", "iconType", 1, "notificationCount", 0));
 			
 			ImmutableMap.Builder<String, Object> questionsMap = new ImmutableMap.Builder<String, Object>();
 			questionsMap.put("name", "Setting Work");
 			questionsMap.put("links", questions);
 			questionsMap.put("section", "questions");
 			questionsMap.put("icon", "a");
 			questionsMap.put("iconType", 2);
 			questionsMap.put("notificationCount", questionsNotifications);
 			sidebar.add(questionsMap.build());
 		}
 		
 		// Handins
 		if (settings.isHandinsOptIn()) {
 			List<Object> handins = new LinkedList<Object>();
 			handins.add(ImmutableMap.of("name", "Create bin", "link", "/handins/bins/create", "icon", ",", "iconType", 2, "notificationCount", 0));
 			handins.add(ImmutableMap.of("name", "Upload answers", "link", "/handins/bins", "icon", ",", "iconType", 2, "notificationCount", 0));
 			handins.add(ImmutableMap.of("name", "Mark answers", "link", "/handins/bins/marking", "icon", "C", "iconType", 2, "notificationCount", 0));
 			
 			ImmutableMap.Builder<String, Object> handinsMap = new ImmutableMap.Builder<String, Object>();
 			handinsMap.put("name", "Marking Work");
 			handinsMap.put("links", handins);
 			handinsMap.put("section", "handins");
 			handinsMap.put("icon", "F");
 			handinsMap.put("iconType", 2);
 			handinsMap.put("notificationCount", handinsNotifications);
 			sidebar.add(handinsMap.build());
 		}
 		
 		return sidebar;
 		
 	} 
 	
 }
