 package controllers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 import play.*;
 import play.mvc.*;
 import play.data.*;
 import play.libs.Json;
 import play.data.Form;
 // http://www.playframework.com/documentation/api/2.0/java/index.html
 
 import models.*;
 
 import org.apache.commons.io.FileUtils;
 
 import org.joda.time.DateTime;
 
 // http://www.playframework.com/documentation/2.1.1/JavaJsonRequests
 // import org.codehaus.jackson.JsonNode;
 // import org.codehaus.jackson.node.ObjectNode;
 // import play.mvc.BodyParser;
 
 import com.avaje.ebean.ExpressionList;
 
 // http://www.playframework.com/documentation/2.1.1/JavaActions
 // http://stackoverflow.com/questions/14843365/get-request-parameter-with-play-framework
 // http://stackoverflow.com/questions/9808348/request-params-is-gone-in-play-framework-2-0
 public class Application extends Controller {
 
 	// http://arshaw.com/fullcalendar/docs/event_data/events_json_feed/
 	public static Result events() {
		String start_string = request().getQueryString("start");
		String end_string = request().getQueryString("end");
 		
 		ExpressionList<MyEvent> events = MyEvent.findDate.where();
		
		if (start_string != null){
			DateTime start= new DateTime(Long.parseLong(start_string));//test: 1357484400000
			events = events.gt("start_time", start);
		}
		if (end_string != null){
			DateTime end = new DateTime(Long.parseLong(end_string));//test: 1371306630000
			events = events.lt("end_time", end);
		} 	
 
 		List<MyEvent> eventList = events.findList();
		eventList = (start_string == null && end_string == null) ? MyEvent.find.all() : eventList;
         //System.out.println(eventList.toString());
 		Iterator<MyEvent> itr = eventList.iterator();
 		while (itr.hasNext()) {
 			System.out.println(itr.next().name); // itr.next()
 		}
 		return ok( eventList.toString() );
 	}
 
 }
