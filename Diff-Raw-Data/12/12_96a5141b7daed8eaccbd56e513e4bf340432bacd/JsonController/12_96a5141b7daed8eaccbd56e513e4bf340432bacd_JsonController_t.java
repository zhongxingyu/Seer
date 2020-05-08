 package controllers;
 
 import java.sql.Date;
 
 import play.*;
 import play.Logger;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 
 import models.Event;
 
 public class JsonController extends BaseController {
 	
	public static void getEventAsJson(long id) {
		Event event = Event.findById(id);
 		response.contentType = "application/json";
 		renderJSON(event);
 		
 	}
 	
 	public static void handleJsonBody(String body) {
		// curl -v -H "Content-Type: application/json" -X POST -d '{"title":"dfh","date":"Nov 30, 2012 12:00:00 AM","vegetarian_opt":false,"slots":-1,"details":"dhf","bookings":[],"id":1}' http://localhost:9000/event/json/create
 		Logger.info("content type: %s", request.contentType);
 	    Logger.info("json string: %s", body);
 	    Logger.info(request.params.toString());
 		Event event = new Gson().fromJson(body, Event.class);
 	    event.title = event.title + "new";
 	    event = new Event(event);
 	    event.save();
 	    AdminController.index(1);
 	}
 	
 }
