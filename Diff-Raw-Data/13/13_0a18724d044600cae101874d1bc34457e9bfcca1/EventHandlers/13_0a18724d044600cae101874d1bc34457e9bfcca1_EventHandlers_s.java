 
 package com.google.code.geobeagle.xmlimport;
 
 import java.util.HashMap;
 
 public class EventHandlers {
     private final HashMap<String, EventHandler> mEventHandlers = new HashMap<String, EventHandler>();
 
     public void add(String extension, EventHandler eventHandler) {
         mEventHandlers.put(extension.toLowerCase(), eventHandler);
     }
 
     public EventHandler get(String filename) {
         int len = filename.length();
         String extension = filename.substring(Math.max(0, len - 4), len);
         return mEventHandlers.get(extension.toLowerCase());
     }
 }
