 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.api;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import net.wgr.core.ReflectionUtils;
 import net.wgr.xenmaster.controller.BadAPICallException;
 import net.wgr.xenmaster.controller.Controller;
 import org.apache.log4j.Logger;
 
 /**
  * 
  * @created Dec 9, 2011
  * @author double-u
 */ 
 public class Event extends XenApiEntity {
 
     protected int id;
     protected Date timestamp;
     protected String eventClass;
     protected UUID subject;
     protected Operation operation;
     protected XenApiEntity snapshot;
     protected static int connectionIndex;
     // It's less expensive to keep this list in memory then to do file and ZIP IO
     protected static List<Class> apiEntityClasses;
 
     public Event() {
     }
 
     public Event(String ref, boolean autoFill) {
         super(ref, autoFill);
     }
 
     public Event(String ref) {
         super(ref);
     }
 
     public static void register(List<String> eventClasses) throws BadAPICallException {
         if (eventClasses == null) {
             eventClasses = new ArrayList<>();
         }
         Controller.dispatch("event.register", eventClasses);
     }
 
     public static void register() throws BadAPICallException {
         ArrayList<String> eventClasses = new ArrayList<>();
         eventClasses.add("*");
         Controller.dispatch("event.register", eventClasses);
     }
 
     public static void unregister(List<String> eventClasses) throws BadAPICallException {
         if (eventClasses == null) {
             eventClasses = new ArrayList<>();
         }
         Controller.dispatch("event.unregister", eventClasses);
     }
 
     public static List<Event> nextEvents() throws BadAPICallException {
         if (connectionIndex == 0) {
             connectionIndex = Controller.getLocal().getDispatcher().getConnections().requestNewConnection();
         }
         ArrayList<Event> events = new ArrayList<>();
         Object obj = Controller.dispatchOn("event.next", connectionIndex);
         Object[] result = (Object[]) obj;
         if (result == null) {
             return events;
         }
         for (Object o : result) {
             Map<String, Object> ev = (Map<String, Object>) o;
             Event event = new Event();
             event.fillOut(ev);
             event.setSnapshot(parseSnapshot(ev.get("class").toString(), (Map<String, Object>) ev.get("snapshot")));
             events.add(event);
         }
         return events;
     }
 
     protected static <T extends XenApiEntity> T parseSnapshot(String className, Map<String, Object> data) {
         try {
             Class<T> clazz = null;
             try {
                 if (apiEntityClasses == null) {
                     apiEntityClasses = ReflectionUtils.getClasses(packageName, Event.class);
                 }
                 
                 for (Class c : apiEntityClasses) {
                     if (className.toLowerCase().equals(c.getSimpleName().toLowerCase())) {
                         clazz = c;
                         break;
                     }
                 }
             } catch (ClassNotFoundException | IOException ex) {
                 Logger.getLogger(Event.class).error("Failed to list classes in package", ex);
             }
             if (clazz == null) {
                 return null;
             }
 
             Constructor<T> ctor = clazz.getConstructor();
             T newInstance = ctor.newInstance();
             newInstance.fillOut(data);
             return newInstance;
         } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InstantiationException | InvocationTargetException ex) {
             Logger.getLogger(Event.class).error("Failed to create snapshot entity", ex);
         }
 
         return null;
     }
 
     public String getEventClass() {
         return eventClass;
     }
 
     public void setEventClass(String eventClass) {
         this.eventClass = eventClass;
     }
 
     public Operation getOperation() {
         return operation;
     }
 
     public void setOperation(Operation operation) {
         this.operation = operation;
     }
 
     public UUID getSubject() {
         return subject;
     }
 
     public void setSubject(UUID subject) {
         this.subject = subject;
     }
 
     public XenApiEntity getSnapshot() {
         return snapshot;
     }
 
     protected void setSnapshot(XenApiEntity snapshot) {
         this.snapshot = snapshot;
     }
 
     public Date getTimestamp() {
         return timestamp;
     }
 
     public void setTimestamp(Date timestamp) {
         this.timestamp = timestamp;
     }
 
     @Override
     protected Map<String, String> interpretation() {
         HashMap<String, String> map = new HashMap<>();
         map.put("eventClass", "class");
         map.put("subject", "obj_uuid");
         return map;
     }
 
     public static enum Operation {
 
         ADD, MOD, DEL
     }
 }
