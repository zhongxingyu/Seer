 package uk.ac.cam.signups.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import com.google.common.collect.ImmutableMap;
 import com.googlecode.htmleasy.RedirectException;
 import com.googlecode.htmleasy.ViewWith;
 
 
 
 
 
 
 
 
 

 //Import models
 import uk.ac.cam.signups.models.*;
 import uk.ac.cam.signups.util.HibernateUtil;
import uk.ac.cam.signups.util.UserLookupManager;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 //Import the following for logging
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Path("/")
 public class HomePageController extends ApplicationController{
 	
 	// Create the logger
 	private static Logger log = LoggerFactory.getLogger(HomePageController.class);
 	
 	private User user;
 	
 	@GET @Path("/")
 	public void localhostRedirect() {
 		throw new RedirectException("/app/#signapp/");
 	}
 	
 	// Index
 	@GET @Path("/signapp") 
 	@Produces(MediaType.APPLICATION_JSON)
 	public Map indexHomePage() {
 		
 		user = initialiseUser();
 		
 		// Upcoming deadlines
 		
 		// Upcoming events 
 		// move most of this code to a better location later
 		Session session = HibernateUtil.getTransactionSession();
 
 		ArrayList<Slot> slots = new ArrayList<Slot>();
 		Query getSlot = session.createQuery("from Slot where owner = :user").setParameter("user", user);
 		slots = (ArrayList<Slot>) getSlot.list();
 	  	Row row;
 	  	Event event;
 	  	List<ImmutableMap<String, ?>> events = new ArrayList<ImmutableMap<String,?>>();
 		for(Slot s : slots){
 			row = s.getRow();
 			event = row.getEvent();
 			events.add(ImmutableMap.of("name", event.getTitle(), "date", row.getCalendar().getTime().toString()));
 		}
 
 		// Get user details
 		log.debug("Index GET: Getting user details");
		
		ImmutableMap<String, ?> userMap = ulm.getAll();		
		
 		return ImmutableMap.of("user", userMap, "deadlines", user.getUserDeadlinesMap(), "events", events);
 	}
 	
 	// DOS Index
 	@GET @Path("signapp/DoS") @ViewWith("/soy/home_page.dos")
 	public Map dosHomePage() {
 		
 		// Initialise user
 		initialiseUser();
 		
 		// Does user have staff level access?
 		if(!isStaff()){
 			throw new RedirectException("/");
 		}
 		
 		return ImmutableMap.of();
 	}
 	
 	// Admin Index
 	@GET @Path("signapp/admin") @ViewWith("/soy/home_page.admin")
 	public Map adminHomePage() {
 		return ImmutableMap.of();
 	}
 	
 	// Authenticate staff
 	public boolean isStaff() {
 		try {
 			return (ulm.getStatus().equals("staff"));
 		} catch(NullPointerException e) {
 			log.error("User initialisation failed");
 			return false;
 		}
 	}
 }
