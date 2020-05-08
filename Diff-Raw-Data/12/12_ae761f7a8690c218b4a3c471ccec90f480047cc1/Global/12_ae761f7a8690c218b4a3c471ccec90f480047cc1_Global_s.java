 /*
  * Global.java
  *
  * Copyright (c) 2009 FooBrew, Inc.
  */
 package org.j2free.util;
 
 import java.util.concurrent.ConcurrentHashMap;
 import net.jcip.annotations.ThreadSafe;
 
 /**
  * Basically, <tt>Global</tt> is just a static {@link ConcurrentHashMap}
  * for storing and getting values by key.
  *
  * @author Ryan Wilson
  */
 @ThreadSafe
 public class Global {
 
     private static final ConcurrentHashMap<String,Object> map =
             new ConcurrentHashMap<String,Object>();
 
     public static Object get(String key) {
         return map.get(key);
     }
 
     public static Object put(String key, Object val) {
         return map.put(key, val);
     }
 
     public static Object putIfAbsent(String key, Object val) {
         return map.putIfAbsent(key, val);
     }
 
     public static void clear() {
         map.clear();
     }
 
     public static Object remove(String key) {
         return map.remove(key);
     }
 
     public static boolean remove(String key, Object val) {
         return map.remove(key, val);
     }
 
     public static Object replace(String key, Object val) {
         return map.replace(key, val);
     }
 
    public static boolean replate(String key, Object old, Object val) {
         return map.replace(key, old, val);
     }
 }
