 package com.piotrnowicki.btrace;
 
 import java.lang.ref.WeakReference;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class DataAccessor {
    private Logger log = Logger.getLogger(DataAccessor.class.getName());
 
    private Map<String, WeakReference<Data>> cache = new HashMap<String, WeakReference<Data>>();
 
     public Data getData(String key) {
         log.log(Level.INFO, "Fetching data by key: {0}", key);
 
         Data result;
 
         WeakReference<Data> entry = cache.get(key);
 
         if (entry != null && entry.get() != null) {
             result = entry.get();
         } else {
             result = new Data("Randomized content = " + Math.random());
             cache.put(key, new WeakReference<Data>(result));
         }
 
         return result;
     }
 }
