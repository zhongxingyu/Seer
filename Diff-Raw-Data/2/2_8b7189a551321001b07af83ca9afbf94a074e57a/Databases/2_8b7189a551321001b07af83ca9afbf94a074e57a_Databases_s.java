 package com.teneke.songkickmaps;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.teneke.songkickmaps.model.Interval2D;
 import com.teneke.songkickmaps.model.QuadTree;
 import com.teneke.songkickmaps.model.Spot;
 
 public class Databases {
 
   private static HashMap<Long, JSONObject> venueDB =
       new HashMap<Long, JSONObject>();
 
   private static QuadTree<Spot> spotDB = new QuadTree<Spot>();
 
   public static void storeVenueToMemory(
       double lat,
       double lon,
       long id,
       JSONObject data) {
 
     Spot s = new Spot(lat, lon, id);
     spotDB.insert(s);
     try {
       if (data.get("metroArea") != JSONObject.NULL) {
         data.remove("metroArea");
       }
     } catch (Exception e) {
       // TODO Auto-generated catch block
       // e.printStackTrace();
     }
     venueDB.put(id, data);
   }
 
   public static void resetDatabases() {
 
     venueDB = new HashMap<Long, JSONObject>();
     spotDB = new QuadTree<Spot>();
 
   }
 
   public static void populateDatabases(int metroCode) throws JSONException {
 
     populateDatabases(metroCode, 1);
   }
 
   public static JSONObject getVenue(long id) {
 
     return venueDB.get(id);
   }
 
   public static JSONArray venueListByGeo(
       Double minLat,
       Double maxLat,
       Double minLon,
       Double maxLon) {
 
     // if (venueDB.size() == 0) {
     // try {
     // // populateDatabases(24426);
     // } catch (JSONException e) {
     // // TODO Auto-generated catch block
     // e.printStackTrace();
     // }
     // }
     Spot min = new Spot(minLat, minLon, 1);
     Spot max = new Spot(maxLat, maxLon, 1);
     Interval2D range = new Interval2D(min, max);
     LinkedList<Spot> spots = spotDB.query2D(range);
     JSONArray rv = new JSONArray();
     for (Spot s : spots) {
       rv.put(venueDB.get(s.getId()));
     }
     return rv;
   }
 
   public static void populateDatabases(int metroCode, int page)
       throws JSONException {
 
     JSONObject data = new JSONObject(SongKickAPI.getMetroData(metroCode, page));
     JSONObject resultsPage = data.getJSONObject("resultsPage");
     JSONObject results = resultsPage.getJSONObject("results");
     if (results.has("event")) {
       JSONArray events = results.getJSONArray("event");
       for (int i = 0; i < events.length(); i++) {
         JSONObject event = events.getJSONObject(i);
         JSONObject venue = event.getJSONObject("venue");
         if (venue != JSONObject.NULL && venue.get("lat") != JSONObject.NULL
             && venue.get("lng") != JSONObject.NULL) {
           long venueId = -1;
           try {
             venueId = venue.getLong("id");
           } catch (Exception e) {
            System.err.println("Error in parsing:" + venue);
           }
           if (venueId != -1 && getVenue(venueId) == null) {
             try {
               storeVenueToMemory(venue.getDouble("lat"),
                   venue.getDouble("lng"), venue.getLong("id"), venue);
             } catch (Exception e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
             }
           }
         }
       }
       page++;
       populateDatabases(metroCode, page);
     }
 
   }
 
   public static void main(String[] args) {
 
     try {
       populateDatabases(24426);
       JSONArray venues = venueListByGeo(51.4846, 51.5067, -0.1778, -0.1346);
       int i = 1;
     } catch (JSONException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   }
 }
