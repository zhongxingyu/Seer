 package app.model;
 
 import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.type.TypeFactory;
 import com.mongodb.BasicDBObject;
 import org.mongojack.ObjectId;
 
 import javax.persistence.Id;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.core.MultivaluedMap;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Set;
 
 @JsonIgnoreProperties(ignoreUnknown = true)
 public class Location {
 
     @Id
     @ObjectId
     @FormParam("id")
     private String id;
 
     @FormParam("name")
     @JsonProperty("name")
     private String name;
 
     @FormParam("lat")
     @JsonProperty("lat")
     private String lat;
 
     @FormParam("lng")
     @JsonProperty("lng")
     private String lng;
 
     @FormParam("city")
     @JsonProperty("city")
     private String city;
 
     public String getCity() {
         return city;
     }
 
     public void setCity(String city) {
         this.city = city;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getLat() {
         return lat;
     }
 
     public void setLat(String lat) {
         this.lat = lat;
     }
 
     public String getLng() {
         return lng;
     }
 
     public void setLng(String lng) {
         this.lng = lng;
     }
 
     public Location() {
     }
 
     public static Location fromMultivaluedMap(MultivaluedMap<String, String> oldMap) {
         ObjectMapper m = new ObjectMapper();
         HashMap<String, String> newMap = new HashMap<String, String>();
 
         for (String string : oldMap.keySet()) {
            if (!string.equals("page") && !string.equals("rows") && !oldMap.getFirst(string).isEmpty()) {
                 newMap.put(string, oldMap.getFirst(string));
             }
         }
         return m.convertValue(newMap, Location.class);
     }
 
     private HashMap<String, String> asMap() {
         ObjectMapper m = new ObjectMapper();
         return m.convertValue(this, TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class));
     }
 
     public HashMap<String, String> asNotNullValuesMap() {
         HashMap<String, String> oldMap = asMap();
         HashMap<String, String> newMap = new HashMap<String, String>();
         for (String obj : oldMap.keySet()) {
             if (null != oldMap.get(obj)) {
                 newMap.put(obj, oldMap.get(obj));
             }
         }
         return newMap;
     }
 
     public HashMap<String, String> asNotNullAndNotEmptyValuesMap() {
         HashMap<String, String> oldMap = asMap();
         HashMap<String, String> newMap = new HashMap<String, String>();
         for (String obj : oldMap.keySet()) {
             if (null != oldMap.get(obj) && !oldMap.get(obj).isEmpty()) {
                 newMap.put(obj, oldMap.get(obj));
             }
         }
         return newMap;
     }
 
     public BasicDBObject asBasicDBObject() {
         return new BasicDBObject(this.asNotNullValuesMap());
     }
 }
