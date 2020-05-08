 package no.sb1.lpt;
 
 import static java.util.Collections.synchronizedMap;
 import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import no.sb1.lpt.model.Entity;
 
 public class Util {
    public static final String JSON_CONTENT_TYPE = APPLICATION_JSON + ";charset=UTF-8";
     public static final String JSONP_CONTENT_TYPE = "application/x-javascript";
 
     private static int idGenerated = 0;
     public static synchronized int generateId(){
         return idGenerated++;
     }
 
     public static <V extends Entity> Map<Integer, V> map(V... values) {
         Map<Integer, V> map = synchronizedMap(new HashMap<Integer, V>());
         for (V value : values) {
             map.put(value.id, value);
         }
         return map;
     }
 
     public static <T> T nullValue(T possiblyNull, T retIfNull){
         return possiblyNull == null ? retIfNull : possiblyNull;
     }
 }
