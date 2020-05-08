 package ru.gosuslugi.geps.ng.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * User: rnasyrov
  * Date: 08.01.13
  * Time: 12:48
  */
 
 public class KeyValueStore {
 
     private final static Map<String, String> store = new ConcurrentHashMap<String, String>();
     private static long id = 1;
 
    //TODO: load from file

     static {
         store.put("message:123",
                 "{\"messageId\":123, \"fromId\":4444, \"toId\":1, \"subject\":\"Hello World!\", \"text\":\"Amazing\", \"sendDate\":\"2013-01-08 16:14:00\",\"updateDate\":\"2013-01-08 16:14:00\"}");
         store.put("message:321",
                 "{\"messageId\":321, \"fromId\":4444, \"toId\":2, \"subject\":\"Star Wars\", \"text\":\"Episode 7\", \"sendDate\":\"2013-01-08 16:14:00\",\"updateDate\":\"2013-01-08 16:14:00\"}");
        store.put("message:544",
                "{\"messageId\":544, \"fromId\":4444, \"toId\":2, \"subject\":\"Обращения в службу поддержки\", \"text\":\"Добрый день\", \"sendDate\":\"2013-01-08 16:14:00\",\"updateDate\":\"2013-01-08 16:14:00\"}");
 
     }
 
     public static String get(String key) {
         return store.get(key);
     }
 
     public static void put(String key, String value) {
         store.put(key, value);
     }
 
     public static long getNextId() {
         return id++;
     }
 
     public static int getSize() {
         return store.size();
     }
 
     public static List<String> getByPrefix(String prefix) {
         List<String> result = new ArrayList<String>();
         for (String key : store.keySet()) {
             if (key.startsWith(prefix)) {
                 result.add(store.get(key));
             }
         }
         return result;
     }
 
 }
