 package controllers;
 
 import java.util.ListIterator;
 
 import models.Booking;
 import models.Event;
 import models.User;
 import play.Logger;
 import play.mvc.Http.StatusCode;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonParseException;
 
 public class JsonController extends BaseController {
 	
 	public static void event(long id) {
 		Event event = Event.findById(id);
 		if (event != null) {
 		for (int i = 0; i< event.bookings.size();i++) {
 			event.bookings.get(i).event = null;
 		}
 		response.contentType = "application/json";
 		renderJSON(event);
 		}
 		else {
 			response.status = 404;
 		}
 		
 	}
 	
 	public static void create(String body) {
 		// curl -v -H "Content-Type: application/json" -X POST -d '{"title":"dfh","date":"Nov 30, 2012 12:00:00 AM","vegetarian_opt":false,"slots":-1,"details":"dhf","bookings":[],"id":1}' http://localhost:9000/event
 		Logger.info("content type: %s", request.contentType);
 	    Logger.info("json string: %s", body);
 	    try{
 		Event event = new Gson().fromJson(body, Event.class);
 	    event.title = event.title + "new";
 	    event = new Event(event);
 	    event.save();
 	    response.status = StatusCode.CREATED;
 	    } catch (JsonParseException e) {
 	    	System.out.println("Konnte JsonString nicht parsen: "+e.getMessage());
 	    	response.status = 500;
 	    }
 	}
 	
 	public static void update(long id, String body) {
 		// curl -v -H "Content-Type: application/json" -X PUT -d '{"title":"dfhupdated","date":"Nov 29, 2012 12:00:00 AM","vegetarian_opt":false,"slots":-1,"details":"dhf","bookings":[],"id":1}' http://localhost:9000/event/1
 		Logger.info("content type: %s", request.contentType);
 	    Logger.info("json string: %s", body);
 		Event newEvent = new Gson().fromJson(body, Event.class);
 	    Event oldEvent = Event.findById(id);
 	    if (oldEvent != null) {
 	    	oldEvent.title = newEvent.title;
 	    	oldEvent.slots = newEvent.slots;
 	    	oldEvent.details = newEvent.details;
 	    	oldEvent.date = newEvent.date;
 	    	oldEvent.vegetarian_opt = newEvent.vegetarian_opt;
 	    	oldEvent.save();
 		    response.status = 204;
 	    }
 	    else {
 	    	response.status = 404;
 	    }
 	}
 	
 	public static void delete(long id) {
 		// curl -v -X DELETE http://localhost:9000/event/11
 		Event event = Event.findById(id);
 		if (event != null) {
 			ListIterator<Booking> iter = event.bookings.listIterator();
             while (iter.hasNext()){
         		Booking book = iter.next();
         		book.delete();
             }
     		event.delete();
             response.status = 204;
 		}
 		else {
 			response.status = 404;
 		}
 	}
 	
 	public static void createBooking(String body) {
 		// curl -v -H "Content-Type: application/json" -X POST -d '{"user":{"shortname":"we","isAdmin":false,"id":6},"event":{"title":"dfhupdated","date":"Nov 29, 2012 12:00:00 AM","vegetarian_opt":false,"slots":-1,"details":"dhf","bookings":[],"id":4},"id":6}' http://localhost:9000/booking
 		try {
 		Booking booking = new Gson().fromJson(body, Booking.class);
 		User user = User.find("byShortname", booking.user.shortname).first();
 		if (user == null){
 			user = new User(booking.user.shortname,false);
 			user.save();
 		}
 		Event event = Event.findById(booking.event.id);
 		if (event != null){
 		if (event.getUsers().contains(user.shortname) == false && user.shortname != ""){
 		Booking newbooking = new Booking(event, user, false);
 		newbooking.save();
 		event.bookings.add(booking);
 		event.save();
 		}
 		}
 		response.status = StatusCode.CREATED;
 		} 	catch (JsonParseException e) {
 	    	System.out.println("Konnte JsonString nicht parsen: "+e.getMessage());
 	    	response.status = 500;
 	    }
 	}
 	
 	
 }
