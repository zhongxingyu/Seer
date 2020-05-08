 package com.pelzer.util.json;
 
 import java.lang.reflect.Type;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import com.google.gson.Gson;
 import com.google.gson.JsonParseException;
 import com.pelzer.util.Logging;
 
 /**
  * Converts POJOs + JSONObjects to/from JSON. JSONObjects are special, in that
  * this class can determine what type of class to deserialize, assuming the
  * class has been registered with a call to {@link #register(JSONObject)}
  */
 public class JSONUtil {
   private static Gson                                     gson          = new Gson();
   private static Map<String, Class<? extends JSONObject>> registrations = new HashMap<String, Class<? extends JSONObject>>();
   private static Logging.Logger                           log           = Logging.getLogger(JSONUtil.class);
   
   /** Uses GSON to serialize the given object into a JSON string. */
  public static String toJSON(Object obj) {
     return gson.toJson(obj);
   }
   
   /** Deserializes the given JSON into an instance of the given class. */
   public static <T> T fromJSON(String json, Class<T> classOfT) {
     return gson.fromJson(json, classOfT);
   }
   
   public static <T> T fromJSON(String json, Type typeOfT) {
     return gson.fromJson(json, typeOfT);
   }
   
   /**
    * Parses the given JSON and looks for the "_i" key, which is then looked up
    * against calls to {@link #register(JSONObject)}, and then returns the result
    * of {@link #fromJSON(String, Class)}
    */
   public static JSONObject fromJSON(String json) {
     int index1 = json.lastIndexOf("\"_i\":\"");
     if (index1 < 0)
       throw new JsonParseException("Unable to find _i key.");
     index1 += 6;
     int index2 = json.indexOf("\"", index1 + 1);
     if (index2 < 0)
       throw new JsonParseException("Unable to find end of _i value.");
     String id = json.substring(index1, index2);
     Class<? extends JSONObject> clazz = registrations.get(id);
     if (clazz == null)
       throw new JsonParseException("No registration for JSONObject message.identifier:" + id);
     return fromJSON(json, clazz);
   }
   
   /**
    * Registers the given JSONObject as a supported deserialization type so that
    * {@link #fromJSON(String)} can create instances magically. Idempotent.
    */
   public static synchronized void register(JSONObject type) {
     Class<? extends JSONObject> previous = registrations.put(type.getIdentifier(), type.getClass());
     if (previous != null && !previous.isInstance(type))
       throw new RuntimeException("Attempt to register conflicting classes for identifier: " + type.getIdentifier());
     if (previous == null)
       log.debug("Registered {} to {}", type.getIdentifier(), type.getClass());
   }
   
   /**
    * Returns a collection of the classes that have been registered to this util
    * at the time of the call. Do not modify this collection.
    */
   public static Collection<Class<? extends JSONObject>> getRegistrations() {
     return registrations.values();
   }
   
 }
