 package controllers;
 
 import play.libs.*;
 import play.*;
 import play.mvc.*;
 
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import org.bouncycastle.openssl.PasswordException;
 
 import models.*;
 import models.Calendar;
 
 @With(Secure.class)
 public	 class Application extends Controller {
 
 	private static Database db = Database.getInstance();
 	
     public static void index() {
     	showUserPage(Security.connected());
     }
     
     public static void logIn(String username, String password) {
     	System.out.println(username + " " + password);
     	try {
 	    	User user = db.getUserByName(username);
 	    	if (user.getPassword().equals(password))
 	    		System.out.println("Sucessfully logged in");
 	    	else throw new NoSuchElementException("Entered wrong password!");
 	    	showUserPage(username);
     	} catch (NoSuchElementException e) {
     		String msg = e.getMessage();
     		render(msg);
     	}
     }
     
     public static void showUserPage(String username){
     	User user = db.getUserByName(username);
    	User myself = db.getUserByName(Security.connected());
     	List<Calendar> calendars = user.getCalendars();
     	List<User> allUsers = db.getUsers();
     	render(myself, user, calendars, allUsers);
     }
     
     public static void showEvents(String username, String calendarname, String msg) {
     	System.out.println(username + " " + calendarname);
    	User myself = db.getUserByName(Security.connected());
     	User user = db.getUserByName(username);
     	Calendar calendar = user.getCalendarByName(calendarname);
    	Iterator<Event> events = calendar.getAllVisibleEvents(myself);
     	render(myself, user, calendar, events, msg);
     }
   
     
     public static void addEvent(String userName, String calendarName, String eventName, String eventStart, String eventEnd){
     	User user = db.getUserByName(userName);
     	Calendar calendar = user.getCalendarByName(calendarName);
     	String message = null;
     	//TODO: the error doesn't get thrown here, it's thrown @ compareTo()
     	try {
         	calendar.addEvent(eventName, eventStart, eventEnd, true);
     	} catch (Exception e){
     		message = e.getMessage();
     	}
     	showEvents(user.getName(), calendar.getName(), message);
     }
 
 }
