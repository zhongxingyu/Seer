 package com.bingo.eatime.admin;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.bingo.eatime.core.Event;
 import com.bingo.eatime.core.EventManager;
 import com.bingo.eatime.core.Person;
 import com.bingo.eatime.core.PersonManager;
 import com.bingo.eatime.core.Restaurant;
 import com.bingo.eatime.core.RestaurantManager;
 import com.google.appengine.api.datastore.Key;
 
 public class EventAdminServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 8208066016833205326L;
 	
 	private static final Logger log = Logger.getLogger(EventAdminServlet.class.getName());
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
 		String action = req.getParameter("action");
 		String eventName = req.getParameter("name");
 		String restaurantKeyName = req.getParameter("restaurant");
 		String creatorUsername = req.getParameter("username");
 		String dateString = req.getParameter("date");
 		String[] invitesUsername = req.getParameterValues("invite");
 		
 		Date time = null;
 		if (dateString != null) {
			time = new Date(Long.valueOf(dateString));
 		}
 		
 		Person creator = null;
 		if (creatorUsername != null) {
 			creator = PersonManager.getPersonByUsername(creatorUsername);
 		}
 		
 		TreeSet<Person> invites = null;
 		if (invitesUsername != null) {
 			invites = PersonManager.getPeopleByUsername(Arrays.asList(invitesUsername));
 		}
 		
 		Restaurant restaurant = null;
 		if (restaurantKeyName != null) {
 			restaurant = RestaurantManager.getRestaurant(restaurantKeyName);
 		}
 		
 		Key eventKey = null;
 		if (action != null) {
 			if (action.equals("add") && eventName != null && restaurant != null && creator != null && time != null) {
 				Event event = Event.createEvent(eventName, restaurant, creator, time, invites);
 				eventKey = EventManager.addEvent(event);
 			}
 		}
 		
 		try {
 			PrintWriter writer = resp.getWriter();
 			if (eventKey != null) {
 				writer.println(String.valueOf(eventKey.getId()));
 			} else {
 				writer.println("-1");
 			}
 		} catch (IOException e) {
 			log.log(Level.SEVERE, "Cannot get print writer.", e);
 		}
 		
 	}
 
 }
