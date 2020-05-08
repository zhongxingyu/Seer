 package k11.guildwars2eventviewer.DataClasses;
 
 import k11.guildwars2eventviewer.Callbacks.EventListCallback;
 import k11.guildwars2eventviewer.Callbacks.HTTPCallback;
 import k11.guildwars2eventviewer.Callbacks.WorldListCallback;
 import k11.guildwars2eventviewer.EventViewerApplication;
 import k11.guildwars2eventviewer.HTTPTask;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 
 /**
  * Created by bay on 5/20/13.
  */
 public class Event {
     public static final String ACTIVE = "Active";
     public static final String SUCCESS = "Success";
     public static final String FAIL = "Fail";
     public static final String WARMUP = "Warmup";
     public static final String PREPARATION = "Preparation";
 
     private EventName eventName;
     private MapName mapName;
     private WorldName worldName;
     private String state;
 
     Event() {
         eventName = new EventName();
         mapName = new MapName();
         worldName = new WorldName();
     }
 
     public EventName getEventName(EventViewerApplication application) {
         if (eventName == null) {
             return eventName;
         }
         else if (eventName.getName() == null) {
             Integer size = application.getEventNames().size();
             for (int i=0;i<size;i++) {
                 if (application.getEventNames().get(i).getId().equals(eventName.getId())) {
                     eventName.setName(application.getEventNames().get(i).getName());
                     return eventName;
                 }
             }
             return eventName; //TODO add proper handling, shouldn't ever run, will cause NullException errors
         }
         else {
             return eventName;
         }
     }
 
     public void setEventName(EventName eventName) {
         this.eventName = eventName;
     }
 
     public MapName getMapName(EventViewerApplication application) {
         if (mapName == null) {
             return mapName;
         }
         else if (mapName.getName() == null) {
             Integer size = application.getMapNames().size();
             for (int i=0;i<size;i++) {
                 if (application.getMapNames().get(i).getId() == mapName.getId()) {
                     mapName.setName(application.getMapNames().get(i).getName());
                     return mapName;
                 }
             }
             return mapName; //TODO add proper handling, shouldn't ever run, will cause NullException errors
         }
         else {
             return mapName;
         }
     }
 
     public void setMapName(MapName mapName) {
         this.mapName = mapName;
     }
 
     public WorldName getWorldName(EventViewerApplication application) {
         if (worldName == null) {
             return worldName;
         }
         else if (worldName.getName() == null) {
             Integer size = application.getWorldNames().size();
             for (int i=0;i<size;i++) {
                 if (application.getWorldNames().get(i).getId() == worldName.getId()) {
                     worldName.setName(application.getWorldNames().get(i).getName());
                     return worldName;
                 }
             }
             return worldName; //TODO add proper handling, shouldn't ever run, will cause NullException errors
         }
         else {
             return worldName;
         }
     }
 
     public void setWorldName(WorldName worldName) {
         this.worldName = worldName;
     }
 
     public String getState() {
         return state;
     }
 
     public void setState(String state) {
         this.state = state;
     }
 
    static public Event findEvent(ArrayList<Event> eventList, String eventID, EventViewerApplication application) {
         Integer size = eventList.size();
         for (int i=0;i<size;i++) {
             if (eventList.get(i).getEventName(application).getId().equals(eventID)) {
                 return eventList.get(i);
             }
         }
        return null;
     }
 
     public void parseJSON(JSONObject jsonObject) throws JSONException, ParseException {
         state = jsonObject.getString("state");
         eventName.setId(jsonObject.getString("event_id"));
         mapName.setId(jsonObject.getInt("map_id"));
         worldName.setId(jsonObject.getInt("world_id"));
     }
 
     static public void readList(EventViewerApplication application, Integer worldID, Integer mapID, String eventID, final EventListCallback callback) {
         String url = application.URL + "/events.json?";
         if (worldID != null) {
             url += "world_id=" + worldID.toString() + "&";
         }
         if (mapID != null) {
             url += "map_id=" + mapID.toString() + "&";
         }
         if (eventID != null) {
             url += "event_id=" + eventID + "&";
         }
         new HTTPTask("GET", url).execute(new HTTPCallback() {
 
             @Override
             public void success(String output) {
                 ArrayList<Event> eventList = new ArrayList<Event>();
                 try {
                     JSONObject jsonObject = new JSONObject(output);
                     JSONArray jsonArray = jsonObject.getJSONArray("events");
                     int length = jsonArray.length();
                     for (int i=0; i<length; i++) {
                         Event event = new Event();
                         event.parseJSON(jsonArray.getJSONObject(i));
                         eventList.add(event);
                     }
                     callback.success(eventList);
                 } catch (Exception e) {
                     callback.failure(e);
                 }
             }
 
             @Override
             public void failure(Exception e) {
                 callback.failure(e);
             }
 
         });
 
     }
 }
