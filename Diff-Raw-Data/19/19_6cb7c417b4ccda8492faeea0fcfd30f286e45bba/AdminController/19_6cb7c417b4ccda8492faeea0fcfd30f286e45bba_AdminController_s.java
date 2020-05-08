 package controllers;
 
 import play.*;
 import play.data.validation.Valid;
 import play.data.validation.Validation;
 import play.mvc.*;
 import play.data.validation.Error;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import models.*;
 
 public class AdminController extends BaseController{
 	
 	public static void index(int page) {
 		List<Event> events = getEventsPaginated(page);
 		long cpages = Event.count()/MAXEVENTS + 1;
         render(events, cpages, page);
     }
 	
 	public static void createEvent() {
 
     	String title = params.get("title");
     	String details = params.get("details");
     	 SimpleDateFormat formatter = new SimpleDateFormat(
                  "yyyy-MM-dd");
     	String date = params.get("date");
     	Date formattedDate;
 		try {
 			formattedDate = (Date)formatter.parse(params.get("date"));
 		} catch (ParseException e) {
 			formattedDate = null;
 			date = null;
 		}
 		int slots;
 		try{
 	    slots = Integer.parseInt(params.get("slots"));
 		} catch (NumberFormatException e)
 		{
 			slots = 0;
 		}
 		
 		boolean vegetarian;
 		if (params.get("vegetarian") == null){
 			vegetarian = false;
 		}
 		else{
 			vegetarian = true;
 		}
     	
         if (details == null)
         {
         	details = "";
         }
         
         if (slots == 0)
         {
         	slots = -1;
         }
         
     	// Create event
         Event event = new Event(title, details, formattedDate, slots, vegetarian);
         
         // Validate
         if (validation.valid(event).ok){
         	// Save
             event.save();
             index(1);
         }   
         else{
         	if(validation.hasErrors()) {
                 params.flash(); // add http parameters to the flash scope
                 validation.keep(); // keep the errors for the next request
                 
                 render(title, details, date, slots, vegetarian);
         	}
         }
     }
     
     public static void deleteEvent(long id, int page){
     	Event event = Event.findById(id);
     	ListIterator<Booking> iter = event.bookings.listIterator();
         while (iter.hasNext()){
         	iter.next();
         	iter.remove();
         }
     	event.delete();
     	index(page);
     	
     }
     
     public static void editEvent(long id){
     	Event event = Event.findById(id);
     	render(event);	
     }
     
     public static void updateEvent(long id, String title, String details, Date date, int slots, boolean vegetarian){
     	Event event = Event.findById(id);
     	
     	if (details == null)
         {
         	details = "";
         }
         
         if (slots == 0)
         {
         	slots = -1;
         }
     	
         Event eventtmp = new Event(title, details, date, slots, vegetarian);
         if(validation.valid(eventtmp).ok && event != null) {
         	event.title = title;
            	event.details = details;
            	event.date = date;
            	event.slots = slots;
           	event.vegetarian_opt = vegetarian;
            	event.save();
             index(1);
         }
         else{
         	if(validation.hasErrors()) {
                 params.flash(); // add http parameters to the flash scope
                 validation.keep(); // keep the errors for the next request
                 editEvent(event.id);
         	}
         }
     }
     
     
     
     public static void reports(Date start, Date end) {
     	List<Event> events = Event.findAll();
     	List<Event> events2 = new ArrayList();
     	SimpleDateFormat formatter = new SimpleDateFormat(
                 "yyyy-MM-dd");
     	
     	// Get beginning and end of the last month
     	if (start == null || end == null){
     	start = new Date();
     	end = new Date();
     	Calendar c = Calendar.getInstance();
 		c.setTime(start);
 		c.add(Calendar.MONTH, -1);
 	    c.set(Calendar.DAY_OF_MONTH, 1);
 	    c.set(Calendar.HOUR_OF_DAY, 0);   
 	    c.set(Calendar.MINUTE, 0);
 	    c.set(Calendar.SECOND, 0);                
 	    c.set(Calendar.MILLISECOND, 0); 
 	    start = c.getTime();
 	    int lastDate = c.getActualMaximum(Calendar.DATE);
 	    c.set(Calendar.DATE, lastDate); 
 	    end = c.getTime();
     	}
 	    
     	ListIterator<Event> iter = events.listIterator();
     	while (iter.hasNext()){
     		Event ev = iter.next();
     		if (start.compareTo(ev.date) <= 0 && ev.date.compareTo(end) <= 0){
     			events2.add(ev);
     		}
     	}
     	ListIterator<Event> iter2 = events2.listIterator();
     	Map<String,List<String>> users = new HashMap<String,List<String>>();
     	while (iter2.hasNext()){
     		Event event = iter2.next();
     		ListIterator<Booking> bookingIter = event.bookings.listIterator();
     		while (bookingIter.hasNext()){
 	    		Booking book = bookingIter.next();
 	    		User user = User.findById(book.UserID);
 	    		Date date = event.date;
 	   			String formattedDate = formatter.format(date);
 	    		List<String> dates = new ArrayList<String>();
 	    		if (users.containsKey(user.shortname)){
 	    		dates = users.get(user.shortname);
 	    		}
 				dates.add(formattedDate);
 	    		users.put(user.shortname, dates);
     		}
     	}
     	response.contentType = "text/csv";
     	response.setHeader("Content-Disposition",
     	"attachment;filename=Reports.csv");
     	render(users);
     }
     
     
     public static void manualReports() {
     render();
     }
 	
 
     
     public static void editBooking(long id){
     	Event event = Event.findById(id);
     	render(event);
     }
     
     public static void updateBooking(long id, String username, boolean vegetarian){
     	Event event = Event.findById(id);
     	User user = User.find("byShortname", username).first();
     	if (vegetarian == true){
     	if (user != null){
     		ListIterator<Booking> iter = event.bookings.listIterator();
             while (iter.hasNext()){
         		Booking book = iter.next();
         		if (book.Event == event && book.UserID == user.id){
        			iter.remove();
         		}
         		
         		}
         	event.save();
     	}
     	}
     	else {
     		if (user == null){
     			user = new User(username,false);
     			user.save();
     		}
     		Booking booking = new Booking(event, user.id, false);
     		if (event.getUsers().contains(username) == false && user.shortname != ""){
         	booking.save();
     		event.bookings.add(booking);
         	event.save();
     		}
     	}
     	index(1);
     	
     }
 	
 }
