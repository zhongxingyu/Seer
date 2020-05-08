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
 import org.joda.time.LocalDate;
 
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
     	render();
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
     		//TODO: 
     		Event ev = Event.findById(Long.parseLong(params.get("eventID")));
     		System.out.println(eventID);
     		ev.delete();
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
 	    ev.save();
 		index();
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
     		// format the event dates
     		if(e.getType().equalsIgnoreCase("Birthday") || e.getType().equalsIgnoreCase("Surprise"))
     			e.setDate(CalendarHelper.formatDateString(e.getDate()));
     		
     		if( ! (e.getType().equalsIgnoreCase("Surprise") && e.getCreatedFor() == user.getId()))
     			renderdEventList.add(e);
     		
     		
     	}
     	render(renderdEventList);
     }
 
     /**
      * When user click register which direct to register form
      */
     public static void register() {
     	render();
     }
     /**
      * Post: process register form
      */
     public static void registerForm(
     		@Required String username, 
     		@Required String firstname,
     		@Required String lastname, 
     		@Required @Email String email,
     		@Required String password, 
     		@Required @Equals("password") String confirmPassword, 
     		String button) {
     	
     	if (button.equals("Cancel")) {
     		index();
     		return;
     	}
     	
     	User user;
     	user = User.find("byUserName", username).first();
     	if (user != null) {
     		validation.addError("username", "The username already exists");
     	}
     	
     	user = null;
     	user = User.find("byEmail", email).first();
     	if (user != null) {
     		validation.addError("email", "The email already exists");
     	}
     	
     	// form validation	
     	if (validation.hasErrors()) {
     		params.flash();
     		validation.keep();
     		register();
     	}
     	else {
     		// save to database
     		//String hashedPassword = Crypto.passwordHash(password);
     		user = new User(username, firstname, lastname, email, password, 0);
     		user.save();
     		
     		// send Email 
     		SendEmails se = new SendEmails();
     		se.welcome(user);
     		
     		index();
     	}
     }
   
     /**
      * When user click 'forget password' link, direct user to the view to 
      * retrieve password
      */
     public static void forgetPassword() {
     	render();
     }
     /**
      * When user confirm to request password, process the request form
      */
     public static void requestPasswordForm (
     	@Required String username,  
 		@Required @Email String email,
 		String button) {
 
 		if (button.equals("Cancel")) {
 			index();
 			return;
 		}
 
 		User user = User.find("byUserName", username).first();
 		if (user == null)
 		{
 			validation.addError("username", "username doesn't exist");
 		}
 
 		// form validation	
 		if (validation.hasErrors()) {
 			params.flash();
 			validation.keep();
 			forgetPassword();
 		}
 		else {	
 			// send Email 
 			SendEmails se = new SendEmails();
 			se.sendPassword(user);
 
 			index();
 		}
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
      * This method is a helper method.
      * @return Logged in user as in object 
      */
     private static User getLogedInUser(){
     	String userName = session.get("username");
     	User user = User.find("byUserName", userName).first();
     	
     	System.out.println("USRER IS: " + user.getFirstName() + " " + user.getLastName());
     	
     	return user;
     }
 }
