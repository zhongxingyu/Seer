 package controllers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import play.*; // Play Framework
 import play.mvc.*;
 import play.data.*;
 import play.db.ebean.*; // http://www.avaje.org/ebean/introquery.html
 import models.*;
 import views.html.*;
 import org.joda.time.format.*; // DateTimeFormatter http://joda-time.sourceforge.net/apidocs/
 import org.joda.time.DateTime; // http://joda-time.sourceforge.net/api-release/index.html
 import org.jsoup.*; // http://jsoup.org/apidocs/
 import org.jsoup.Connection.Method;
 import org.jsoup.nodes.*; // Document, Element
 import org.jsoup.select.Elements;
 import java.io.BufferedReader; // java i/o
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.nio.charset.Charset;
 import org.json.*; // json
 import org.json.simple.JSONValue;
 import com.restfb.json.*; // JsonObject, JsonArray, JsonTokener
 import com.google.gson.*;
 
 /**
  * The Seed class collects data from various sources on the web that will be saved to the database
  * in order to aid the scrapers.
 */
 
 public class Utilities extends Controller {
 
     private static DateTime parse_start_time(String start) { 
         return (start!=null && !start.isEmpty() && start != "null") ? new DateTime().parse(start) : null; 
     }
     private static DateTime parse_end_time(String end) { 
         return (end!=null && !end.isEmpty() && end != "null") ? new DateTime().parse(end) : null; 
     }
 
     public static MyEvent createEvent(FqlEvent event) {
         DateTime starttime = parse_start_time(event.start_time);
         DateTime endtime = parse_end_time(event.end_time);
         return new MyEvent(event.eid,event.name,event.creator,starttime,endtime,event.location,event.venue,event.description);
     }
 
     public static MyEvent createEvent(com.restfb.json.JsonObject event) {
 
         String str_eid = event.has("id") ? event.getString("id") : (event.has("eid") ? event.getString("eid") : "");
         Long eid = Long.valueOf(str_eid).longValue();
         String name = event.has("name") ? event.getString("name") : "";
        String creator = event.has("owner") ? event.getString("owner") : "";
         DateTime starttime = event.has("start_time") ? parse_start_time(event.getString("start_time")) : null;
         DateTime endtime = event.has("end_time") ? parse_end_time(event.getString("end_time")) : null;
         String location = event.has("location") ? event.getString("location") : "";
         String venue = event.has("venue") ? event.getString("venue") : "";
         String description = event.has("description") ? event.getString("description") : "";
 
         if (name.length() >=255 || creator.length() >= 255 || location.length() >= 255 || venue.length() >= 255) {
             System.out.println("error start");
             System.out.println(name);
             System.out.println(creator);
             System.out.println(location);
             System.out.println(venue);
             System.out.println("error end");
         }
 
         return new MyEvent(eid, name, creator, starttime, endtime, location, venue, description);
     }
 
     public static ArrayList getLocations() {
         ArrayList<String> locations = new ArrayList<String>();
         List<MyEvent> events = MyEvent.find.all();
         for (MyEvent event:events) { 
             if (event.location != null && !event.location.isEmpty()) {
                 locations.add(event.location);    
             }
         }
         return locations;
     }
 
     public static boolean saveOrUpdate(Long eid, MyEvent event) {
 
 
         try {
             if (MyEvent.findLong.byId(eid)==null && filter_event(event)) { 
                 event.save(); return true; 
             }
         } catch (Exception e) {
             System.out.println(e.getMessage());
         }
 
         return false;
     }
 
     private static final String[] BLOCKED_ORGANIZATIONS = new String[] {
         "7268844551"
     };
 
     private static final String[] BLOCKED_VENUES = new String[] {
         "17070917171",
         "356690111071371",
         "430645200319091",
         "265779566799135",
         "120569982856",
         "155288914484855",
         "161587597298994",
         "132449680200187",
         "253108394915",
         "110184922344060",
         "115288991957781",
         "143834612313652",
         "111268432255671",
         "44661328399",
         "442292685838282",
         "110432202311659",
         "115421351813697",
         "143964878955940"
     };    
 
     private static boolean filter_event(MyEvent event) {
         String jsontext = event.venue;
         try {
             String creator = event.creator;
             JSONObject json = new JSONObject(jsontext);
             String venueID = json.has("id") ? json.get("id").toString() : "";
             boolean blocked_venue = Arrays.asList(BLOCKED_VENUES).contains(venueID);
             boolean blocked_organization = Arrays.asList(BLOCKED_ORGANIZATIONS).contains(creator);
             if (blocked_venue || blocked_organization) return false;
         } catch (JSONException e) {
             System.out.println(e.getMessage());
         }
         return true;
     }
 
 }
 
 // DateTimeFormatter formatter;
 
 // DateTime start_dt;
 // if (jsonObject.getString("start_time").length()>19) formatter= DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
 // else if (jsonObject.getString("start_time").length()>10) formatter= DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
 // else formatter= DateTimeFormat.forPattern("yyyy-MM-dd");
 // if (jsonObject.getString("start_time").length()!=4) start_dt=formatter.parseDateTime(jsonObject.getString("start_time"));
 // else start_dt=null;
 
 // DateTime end_dt;
 // if (jsonObject.getString("end_time").length()>19)  formatter= DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
 // else if (jsonObject.getString("end_time").length()>10) formatter= DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
 // else formatter= DateTimeFormat.forPattern("yyyy-MM-dd");
 // if (jsonObject.getString("end_time").length()!=4) end_dt=formatter.parseDateTime(jsonObject.getString("end_time"));
 // else end_dt=null;
 
 
 
 // DateTime starttime = new DateTime();
 // starttime = starttime.parse(start);
 // return starttime;
 
 
 
 // DateTime endtime = new DateTime();
 // if (end!=null && !end.isEmpty()) endtime = endtime.parse(event.end_time);
 // else endtime=null;
 // return endtime;
