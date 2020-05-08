 package com.bingo.eatime.test;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.TreeSet;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.bingo.eatime.core.Category;
 import com.bingo.eatime.core.CategoryManager;
 import com.bingo.eatime.core.Event;
 import com.bingo.eatime.core.EventManager;
 import com.bingo.eatime.core.Person;
 import com.bingo.eatime.core.PersonManager;
 import com.bingo.eatime.core.Restaurant;
 import com.bingo.eatime.core.RestaurantManager;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.PhoneNumber;
 import com.google.appengine.api.datastore.PostalAddress;
 
 public class EaTimeDatabaseTestServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 2639400166713434665L;
 	
 	private static final Logger log = Logger.getLogger(EaTimeDatabaseTestServlet.class.getName());
 
 	private static final String TAG = "EaTimeDatabaseTestServlet";
 	private static final String TAG_SPLITTER = ": ";
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
 		try {
 			resp.setContentType("text/html");
 			PrintWriter writer = resp.getWriter();
 			writer.println("<html><head></head><body><p>Testing Database...</p></body></html>");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		testDb();
 	}
 
 	private void testDb() {
 		Key result = null;
 
 		Category categoryChinese = Category.createCategory("Chinese");
 		result = CategoryManager.addCategory(categoryChinese);
 
 		log.info("Add category " + categoryChinese + " " + result);
 
 		Category categoryJapanese = Category.createCategory("Japanese");
 		result = CategoryManager.addCategory(categoryJapanese);
 
 		log.info("Add category " + categoryJapanese + " " + result);
 
 		List<Category> restaurantHappyChinaCategories = new ArrayList<Category>();
 		restaurantHappyChinaCategories.add(categoryJapanese);
 		restaurantHappyChinaCategories.add(categoryChinese);
 		Restaurant restaurantHappyChina = Restaurant.createRestaurant(
 				"Happy China", restaurantHappyChinaCategories,
 				new PostalAddress(
 						"219 E State Street, West Lafayette, IN 47906"),
 				new PhoneNumber("765-743-1666"));
 		result = RestaurantManager.addRestaurant(restaurantHappyChina);
 
 		log.info("Add restaurant " + restaurantHappyChina + " " + result);
 
 		TreeSet<Category> returnHappyChinaCategories = CategoryManager
 				.getRestaurantCategories(restaurantHappyChina.getKey());
 		for (Category category : returnHappyChinaCategories) {
 			log.info(restaurantHappyChina + " are in " + category + ".");
 		}
 
 		TreeSet<Restaurant> categoryChineseRestaurants = CategoryManager
 				.getRestaurantsFromCategory(categoryChinese.getKey());
 
 		for (Restaurant restaurant : categoryChineseRestaurants) {
 			log.info(restaurant + " is in category " + categoryChinese);
 		}
 
 		Person me = Person.createPerson("kevin", "Kaiwen", "Xu", "kevin@kevxu.net");
 		Key myKey = PersonManager.addPerson(me);
 		
 		Person p1 = Person.createPerson("p1", "Random", "Guy", "randomguy1@example.com");
 		Key p1Key = PersonManager.addPerson(p1);
 		
		Person p2 = Person.createPerson("p2", "Random", "Guy 2", "randomguy2@example.com");
 		Key p2Key = PersonManager.addPerson(p2);
 		
 		List<Person> invites = new ArrayList<Person>();
 		invites.add(p1);
 		invites.add(p2);
 
 		Event sampleEvent = Event.createEvent("Sample Event",
 				restaurantHappyChina, me,
 				new Date(System.currentTimeMillis()), invites);
 		EventManager.addEvent(sampleEvent);
 		
 		TreeSet<Event> returnSampleEvents = EventManager.getEventsFromRestaurant(restaurantHappyChina.getKey());
 		for (Event event : returnSampleEvents) {
 			log.info("found event " + event);
 		}
 	}
 }
