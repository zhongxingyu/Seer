 package expensable.client.util;
 
 import java.util.HashMap;
 
 import com.google.common.collect.Maps;
 
 /**
  * Common utility methods for Tokenizers.
  */
 public class Tokenizers {
 
   /**
    * Converts a URL query string into a map.
    */
   public static HashMap<String, String> tokenizeQueryString(String qs) {
     HashMap<String, String> map = Maps.newHashMap();
     if (qs.startsWith("?")) {
      qs.substring(1);
     }
 
     String[] params = qs.split("&");
     for (String param : params) {
       String[] paramParts = param.split("=");
       String key;
       String value;
       if (paramParts.length >= 1) {
         key = paramParts[0];
         if (paramParts.length >= 2) {
           value = paramParts[1];
         } else {
           value = "";
         }
         map.put(key, value);
       }
     }
     return map;
   }
 
 }
