 package controllers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import play.*;
 import play.mvc.*;
 import play.data.*;
 import play.libs.Json;
 import play.data.Form;
 import models.*;
 import org.json.*;
 import org.json.simple.JSONValue;
 import org.apache.commons.io.FileUtils;
 import org.joda.time.DateTime;
 import com.avaje.ebean.ExpressionList;
 
 public class Application extends Controller {
 
     /**
      * The events controllers grabs a list of events from the database based on three parameters:
      * start, end, and query. The start and end parameters are unixtimestamps (in seconds) while
      * the query paramter is a string used to filter the events.
      * <p>
      * This controller adheres to the specifications laid out by fullCalendar, the javascript
      * Calendar used in the view. 
      * fullCalendar: http://arshaw.com/fullcalendar/docs/event_data/events_json_feed/
      * <p>
      * This controller is called by the /events url
      * @return Return a JSON string of events matching the input parameters.
      */
     public static Result events() {
         String start_string = request().getQueryString("start");
         String end_string = request().getQueryString("end");
         String query = request().getQueryString("query");
 
         ExpressionList<MyEvent> events = MyEvent.find.where();
 
         if (start_string != null) {
             DateTime start = new DateTime(Long.parseLong(start_string) * 1000);
             events = events.gt("start_time", start);
         }
         if (end_string != null) {
             DateTime end = new DateTime(Long.parseLong(end_string) * 1000);
            events = events.lt("start_time", end);
         }
         if (query != null) {
             events = events.ilike("name", "%"+query+"%");
         }
 
         List<MyEvent> eventList = events.findList();
         eventList = (start_string == null && end_string == null) ? MyEvent.find.all() : eventList;
 
         return ok( JSONValue.toJSONString(eventList) );
 
     }
 
 }
