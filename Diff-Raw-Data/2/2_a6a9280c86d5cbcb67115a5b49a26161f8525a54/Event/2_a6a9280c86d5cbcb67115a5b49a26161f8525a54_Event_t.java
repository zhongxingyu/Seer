 package com.realmdata.metrics;
 
 import java.util.Date;
 import java.util.Collections;
 
 import org.apache.commons.lang.Validate;
 
 import org.json.simple.JSONAware;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONArray;
 
 import org.bukkit.entity.Player;
 import org.bukkit.Location;
 
 
 public class Event implements JSONAware {
     protected final JSONObject event = new JSONObject();
     
     public Event(String name) {
         this(name, (String[]) null);
     }
     public Event(String name, String... tags) {
         Validate.notNull(name, "Event name must not be null");
         
         event.put("time", ((double) new Date().getTime()) / 1000);
         event.put("name", name);
         if(tags != null && tags.length > 0) {
             JSONArray array = new JSONArray();
             Collections.addAll(array, tags);
             event.put("tags", array);
         }
         
     }
     
     public String toJSONString() {
         return event.toJSONString();
     }
     
     
     public Event setUser(Player player) {
         if(player == null) {
             event.remove("user");
             return this;
         }
        return setUser(Sessions.getSession(player), player.getName(), player.getAddress() == null ? null : player.getAddress().getAddress().getHostAddress());
     }
     public Event setUser(String session, String name, String ip) {
         JSONObject user = getOrPutObject(event, "user");
         putOrRemove(user, "session", session);
         putOrRemove(user, "name", name);
         putOrRemove(user, "ip", ip);
         return this;
     }
     
     public Event setPlace(Location location) {
         if(location == null) {
             event.remove("place");
             return this;
         }
         return setPlace(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
     }
     public Event setPlace(String name, Double x, Double y, Double z) {
         JSONObject place = getOrPutObject(event, "place");
         putOrRemove(place, "name", name);
         putOrRemove(place, "x", x);
         putOrRemove(place, "y", y);
         putOrRemove(place, "z", z);
         return this;
     }
     
     
     static JSONObject getOrPutObject(JSONObject object, Object key) {
         JSONObject value = (JSONObject) object.get(key);
         if(value == null) {
             value = new JSONObject();
             object.put(key, value);
         }
         return value;
     }
     static void putOrRemove(JSONObject object, Object key, Object value) {
         if(value == null) {
             object.remove(value);
         }
         else {
             object.put(key, value);
         }
     }
 }
