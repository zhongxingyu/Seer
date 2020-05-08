 package be.dolmen.proxy;
 
 import com.google.common.collect.MapMaker;
 
 public class MapProxy implements AbstractMap {
 
     private String fileName;
     private java.util.Map<String,String> hashtable = new MapMaker()
                                                                 .weakKeys()
                                                                .weakValues()
                                                                 .makeMap();
 
     private Map map;
 
     public MapProxy(String fileName)
     {
         this.fileName = fileName;
     }
 
     @Override
     public String find(String key) throws Exception
     {
         String value = get(key);
         if (value == null) {
             value = map().find(key);
             put(key, value);
         }
 
     	return value;
     }
 
     @Override
     public void add(String key, String value) throws Exception
     {
         map().add(key, value);
         put(key, value);
     }
 
     private synchronized String get(String key)
     {
     	return hashtable.get(key);
     }
 
     private synchronized void put(String key, String value)
     {
     	hashtable.put(key, value);
     }
 
     private synchronized Map map() {
         if (map == null) {
             map = new Map(fileName);
         }
         return map;
     }
 }
