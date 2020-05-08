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
     
     public static void AvailableFormats() {
     	User myself = db.getUserByName(Security.connected());
     	render(myself);
     }
     
     public static void showUserPage(String username){
     	User user = db.getUserByName(username);
     	User myself = db.getUserByName(Security.connected());
     	List<Calendar> calendars = user.getCalendars();
     	List<User> allUsers = db.getUsers();
     	render(myself, user, calendars, allUsers);
     }
     
     public static void showEvents(String username, String calendarname, String msg) {
     	User myself = db.getUserByName(Security.connected());
     	User user = db.getUserByName(username);
     	Calendar calendar = user.getCalendarByName(calendarname);
     	Iterator<Event> events = calendar.getAllVisibleEvents(myself);
     	CalendarUtil cu = CalendarUtil.getInstanceToday();
     	List<Integer> lastMonthDates = cu.getLastMonthDates();
     	List<Integer> thisMonthDates = cu.getThisMonthDates();
     	List<Integer> nextMonthDates = cu.getNextMonthDates();
     	render(myself, user, calendar, events, lastMonthDates, 
     			thisMonthDates, nextMonthDates, msg , cu);
     }
     
     public static void showEditEvent(String username, String calendarname, String eventName) {
     	User myself = db.getUserByName(Security.connected());
     	User user = db.getUserByName(username);
     	Calendar calendar = user.getCalendarByName(calendarname);
     	Event e = calendar.getEventByName(eventName);
     	render(myself, user, calendar, e);
     	
     }
     
     public static void removeEvent(String username, String calendarname, String eventName) {
     	User myself = db.getUserByName(Security.connected());
     	User user = db.getUserByName(username);
     	Calendar calendar = user.getCalendarByName(calendarname);
     	Event e = calendar.getEventByName(eventName);
     	calendar.removeEvent(e);
     	showEvents(user.getName(), calendar.getName(), "Deleted " + eventName);
     }
     
     
     public static void addEvent(String oldEvent, String userName, String calendarName,
     		String eventName, String eventStart, String eventEnd, boolean isPrivate){
     	User user = db.getUserByName(userName);
     	Calendar calendar = user.getCalendarByName(calendarName);
     	String message = null;
     	try {
         	calendar.addEvent(eventName, eventStart, eventEnd, isPrivate);
         	if (oldEvent != null) {
         		Event e = calendar.getEventByName(oldEvent);
         		calendar.removeEvent(e);
         	}
     		System.out.println("removed " + oldEvent);
     	} catch (Exception e){
     		message = e.getMessage();
     	}
     	showEvents(user.getName(), calendar.getName(), message);
     }
 
 }
