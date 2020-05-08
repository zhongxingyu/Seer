 package net.nexisonline.spade;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class SpadeConf {
 
     public static int getInt(Map<String, Object> node, String key, int defaultValue) {
        if(node==null || !node.containsKey(key))
             return defaultValue;
         return (Integer) node.get(key);
     }
     public static String getString(Map<String, Object> node, String key, String defaultValue) {
        if(node==null || !node.containsKey(key))
             return defaultValue;
         return (String) node.get(key);
     }
     public static double getDouble(Map<String, Object> node, String key, double defaultValue) {
        if(node==null || !node.containsKey(key))
             return defaultValue;
         return (Double) node.get(key);
     }
     @SuppressWarnings("unchecked")
     public static Map<String, Object> getNode(Map<String, Object> node,
             String key) {
        if(node==null || !node.containsKey(key))
             return new HashMap<String,Object>();
         return (Map<String,Object>) node.get(key);
     }
     
 }
