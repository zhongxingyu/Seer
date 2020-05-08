 package controllers;
 
 import play.*;
 import play.data.binding.ParamNode;
 import play.data.validation.Equals;
 import play.data.validation.Required;
 import play.jobs.OnApplicationStart;
 import play.data.validation.*;
 import play.libs.Crypto;
 import play.libs.Mail;
 import play.mvc.*;
 import util.CalendarHelper;
 import util.SendEmails;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.HtmlEmail;
 import org.apache.commons.mail.SimpleEmail;
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import models.*;
 
 /** 
  * 
  * @author niau
  * Protect Main Application Controller 
  * To activate the protection e.g authentication please remove the comment from the following route 
  * *      / =               module:secure
  */
 
 @With(Secure.class)
 public class Application extends Controller {
 
 	@Before
 	static void addDefaults() {
 		String currentYear = CalendarHelper.getCurrentYear();
 		
 		 renderArgs.put("currentYear", currentYear);
 		
 		 
 	}
 
     public static void index() {
     	int notificationsNumebr = getNotificationList().size();
     	session.put("notificationsNumebr",notificationsNumebr);
     	User user = getLogedInUser();
     	render(user);
     }
     
     public static void createBirthdayEvent()
     {
     	List<User> users = User.findAll();
     	System.out.println(users);
     	render(users);
     }
     
     public static void createBirthdayEventForm(String name, String date, Long createdFor, String eventType)
     {
     	validation.required(name);
     	validation.required(date);
     	validation.required(createdFor);
     	validation.required(eventType);
     	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
 		try {
 			f.parse(date);
 		} catch (ParseException e) {
 			validation.addError("date", "Date is not in yyyy-MM-dd format");
 		}
 	    if(validation.hasErrors()) 
 	    {
 	          params.flash();
 	          validation.keep();
 	          createBirthdayEvent();
 	    }
 	    Event ev = new Event(name, date, createdFor, getLogedInUser().getId(), eventType);
 	    System.out.println(ev);
 	    ev.save();
 		index();
     }
     
     public static void editEvent(long eventID)
     {
     	Event ev = Event.findById(eventID);
     	List<User> users = User.findAll();
     	String userName = null;
     	for(User user:users)
     	{
     		if(user.getId() == ev.getCreatedFor());
     		{
     			userName = user.getFirstName() + " " + user.getLastName();
     		}
     	}
     	render(ev, users, userName);
     }
 	
 	public static void editEventForm(String name, String date, Long createdFor, String eventType, long eventID)
     {
 		
 		
 		if(params.get("deletebutton") != null)
     	{
 			System.out.println("Rcived: "+name+" "+date+" "+createdFor+" "+eventType+" "+eventID);
     		//TODO: 
     		Event ev = Event.findById(eventID);
     		System.out.println(eventID);
     		ev.delete();
     		calendarMonth();
     	}
     	
     	validation.required(name);
     	validation.required(date);
     	validation.required(createdFor);
     	validation.required(eventType);
     	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
 		try {
 			f.parse(date);
 		} catch (ParseException e) {
 			validation.addError("date", "Date is not in yyyy-MM-dd format");
 		}
 	    if(validation.hasErrors()) 
 	    {
 	          params.flash();
 	          validation.keep();
 	          createBirthdayEvent();
 	    }
 	    
 	    Event ev = Event.findById(eventID);
 	    ev.setName(name);
 	    ev.setType(eventType);
 	    ev.setDate(date);
 	    ev.save();
 	    calendarMonth();
     }
     
     /**
      * This method is responsible for rendering the Calendar monthly view..  
      */
     public static void calendarMonth()
     {
     	List<Event> events = Event.findAll();
     	User user = getLogedInUser(); 
     	List<Event> renderdEventList = new ArrayList<Event>();
     	for (Event e : events)
     	{
    		 //format the event dates
    		if((e.getType().equalsIgnoreCase("Birthday") || e.getType().equalsIgnoreCase("Surprise")) )
    			e.setDate(CalendarHelper.formatDateString(e.getDate()));
     		
     		if( ! (e.getType().equalsIgnoreCase("Surprise") && e.getCreatedFor() == user.getId()))
     			renderdEventList.add(e);
     		
     		
     	}
     	render(renderdEventList);
     }
  
     public static void EditUserNotificationDays(@Required int notificationDays){
     	User user; 
     	user = User.find("byUserName", Security.connected()).first();
     	validation.required(notificationDays);
     //	validation.min(notificationDays,1);
     	if(notificationDays<1){
     		validation.addError("NotificationDays", "has to be a number >1!");
     	}
     	if(validation.hasErrors()){
     		params.flash(); 
     		validation.keep();
     		EditUserNotificationDaysForm();
     	}else{
     	if (user!=null){
     		user.setNotificationDays(notificationDays);
     		user.save(); 
     		System.out.println("user: "+ user.getUserName() + "Updated with "+ user.getNotificationDays());
     	}
     	}
 
     	Application.EditUserNotificationDaysForm();
 
     }
     public static void EditUserNotificationDaysForm(){
     	User user; 
     	user = User.find("byUserName", Security.connected()).first();
     	String userName=user.getUserName();
     	int NotificationDays= user.getNotificationDays();
     	render(userName, NotificationDays); 
 
     }
     
     /**
      * This method responsible for rendering the notification list view page
      */
     public static void notificationView(){
     	List<Event> notificationList = getNotificationList(); 
     	
     	render(notificationList);
     }
     
     
     /**
      * Notification Action Method, This method will dispatch a list of events 
      * for the user to be notified based on his notification days setup he had chosen in his profile. 
      * 
      */
     public static List<Event> getNotificationList(){
     	
     	List<Event> events = Event.findAll();
     	List<Event> notificatioEvents = new ArrayList<Event>();
     	User user = getLogedInUser();
     
     	int days = 0 ;
     	
     	for(Event e : events){
     	
     		Days d = Days.daysBetween(DateTime.parse(e.getDate()), DateTime.parse(CalendarHelper.getTodayDate()));
     		days = d.getDays();
     		
     		 
     		if(Math.abs(days) <= user.getNotificationDays())
     		{
    			if( ! (e.getType().equalsIgnoreCase("Surprise") && e.getCreatedFor() == user.getId()))
    				notificatioEvents.add(e);
    			
    			
     		}
     	}
     	  
     	return notificatioEvents;
     	
     	
     }
     
     /** 
      * This method is a helper method.
      * @return Logged in user as in object 
      */
     private static User getLogedInUser() {
     	String userName = session.get("username");
     	User user = User.find("byUserName", userName).first();
     	
     	return user;
     }
 }
 
