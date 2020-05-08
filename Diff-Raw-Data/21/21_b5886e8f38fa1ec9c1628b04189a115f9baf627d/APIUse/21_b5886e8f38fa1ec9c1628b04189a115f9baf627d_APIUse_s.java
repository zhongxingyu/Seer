 // Class for pulling JSON from the TransLoc API
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.security.MessageDigest;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.List;
 import java.lang.Math;
 
 import org.json.*;
 
 //import android.location.Location;
 
 
 public class APIUse {
 
     private static String GEOCODE = "http://maps.googleapis.com/maps/api/geocode/json?";
 
     public static boolean need_switch(double lat, double lng, HashMap stop) throws IOException, JSONException
     {
         double stop_lat = ((JSONObject)stop.get("location")).getDouble("lat");
         double stop_lng = ((JSONObject)stop.get("location")).getDouble("lng");
         double dist = RouteFinder.get_distance(lat, lng, stop_lat, stop_lng);
         if (dist > 1600) return true;
         //true if you need to switch, false otherwise
         return false;
     }
 
    public static ArrayList<HashMap> travel_choices(String end_address)
         throws IOException, JSONException {
         String ADDRESS = "address=" + end_address.replaceAll(" ", "+");
 
         URL url = new URL(GEOCODE + ADDRESS + "&sensor=true");
         URLConnection connection = url.openConnection();
 
         String line;
         StringBuilder builder = new StringBuilder();
         BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         while((line = reader.readLine()) != null) {
         builder.append(line);
         }
 
         JSONObject jObj = new JSONObject(builder.toString());
         JSONArray results = jObj.getJSONArray("results");
        ArrayList<JSONObject> ret = {results.getJSONObject(0),
                                     results.getJSONObject(1),
                                     results.getJSONObject(2)};
         return ret;
     }
 
     public static String[] travel_info(double lat, double longitude, JSONObject end_address)
         throws IOException, JSONException {
 
         double lt = end_address.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
         double ln = end_address.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
 
         ArrayList<HashMap> directions = RouteFinder.getDirections(lat, longitude, lt, ln);
         HashMap start = (HashMap)directions.get(0);
         HashMap route = (HashMap)directions.get(1);
         HashMap end   = (HashMap)directions.get(2);
         if (route.isEmpty()) {
             String ret[] = {"No shuttle servce."};  // None of the Big 4 are running
             return ret;
         }
         if (start.equals(end)) {
             String ret[] = {"Walking will be faster!"};
             return ret;
         }
 
         Trip trip = new Trip(start, route, end);
         String ret[]={(String)start.get("name"), (String)route.get("long_name"),
                       (String)end.get("name"), trip.I_EST.toString()};
         //return start name, route name, end name, arrival at initial stop
         return ret;
     }    
     /*
     public static String[] travel_info(double lat, double longitude, String end_address)
         throws IOException, JSONException {
         String ADDRESS = "address=" + end_address.replaceAll(" ", "+");
 
         URL url = new URL(GEOCODE + ADDRESS + "&sensor=true");
         URLConnection connection = url.openConnection();
 
         String line;
         StringBuilder builder = new StringBuilder();
         BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         while((line = reader.readLine()) != null) {
         builder.append(line);
         }
 
         JSONObject jObj = new JSONObject(builder.toString());
         double lt = jObj.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
         double ln = jObj.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
 
         ArrayList<HashMap> directions = RouteFinder.getDirections(lat, longitude, lt, ln);
         HashMap start = (HashMap)directions.get(0);
         HashMap route = (HashMap)directions.get(1);
         HashMap end   = (HashMap)directions.get(2);
         if (route.isEmpty()) {
             String ret[] = {"No shuttle servce."};  // None of the Big 4 are running
             return ret;
         }
         if (start.equals(end)) {
             String ret[] = {"Walking will be faster!"};
             return ret;
         }
 
         Trip trip = new Trip(start, route, end);
         String ret[]={(String)start.get("name"), (String)route.get("long_name"),
                       (String)end.get("name"), trip.I_EST.toString()};
         //return start name, route name, end name, arrival at initial stop
         return ret;
     }
     */
     public static String[] find_shuttle(double lat, double lng, String route_id) throws IOException, JSONException
     {
         //return closest stop, time estimate
         String id = TransLocAPI.getAgency("uchicago");
         ArrayList<HashMap> routes = TransLocAPI.getRoutes(id);
         HashMap route = new HashMap();
 
         for (HashMap temp_route : routes) {
             if (route_id.equals((String)temp_route.get("route_id"))) {
                 route = temp_route;
                 break;
             }
         }
 
         HashMap near_stop = RouteFinder.getNearestStopOnRoute(route, lat, lng);
         Trip trip = new Trip(near_stop, route, near_stop);
         String ret[] = {(String)near_stop.get("name"), trip.I_EST.toString()};
 
         return ret;
     }
 
     public static void main(String[] args) throws IOException, JSONException {
        String[] info = travel_info(41.791393,-87.599776,"5400 S Maryland Ave Chicago IL 60615");
        String[] shuttle = find_shuttle(41.791393,-87.599776,"8000576");
        System.out.println(info[0]);
        System.out.println(info[1]);
        System.out.println(info[2]);
        System.out.println(info[3]);
        System.out.println(shuttle[0]);
        System.out.println(shuttle[1]);
         return;
     }
 }
