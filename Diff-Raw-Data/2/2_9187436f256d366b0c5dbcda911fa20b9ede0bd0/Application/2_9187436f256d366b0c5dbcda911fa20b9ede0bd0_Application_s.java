 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
         render();
     }
     
     //public static void addEvent()
     //{
     //	new Event(inputTitle, inputCategory, inputDescription, inputStartTime).save();
     //	render();
     //}
     
 	public static void eventForm()
 	{
 		render();
 	}
     
     public static void addEvent(
     	String inputTitle,
     	String inputCategory,
     	String inputDescription,
         String inputDate,
     	String inputStartTime,
         String inputDuration)
     {
     	new Event(inputTitle, inputCategory, inputDescription, inputDate, inputStartTime, inputDuration).save();
     	
 		System.out.println("Called " + inputTitle);	
     	render("@Application.index");
     }
     
     public static void filterEvents(String category, String location)
     {
        ArrayList<Event> events = Event.find("select e from Event e, Venue v where e.venue = v and v.location = ? and e.category = ?", location, category).fetch();
         render(events);
     }
     
     public static void getEvent(String title)
     {
         Event e = Event.find("byTitle", title).first();
         render(e);        
     }
     
     public static void getCategories()
     {
         ArrayList<String> categs = Event.find("select category from Event").fetch();
         render(categs);
     }
 }
