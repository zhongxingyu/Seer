 package com.dafttech.eventmanager;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 public class Event {
     volatile private EventManager eventManager = null;
     volatile private EventType type = null;
     volatile private Object[] in = null;
     volatile private List<Object> out = new ArrayList<Object>();
     volatile private boolean done = false;
     volatile private boolean cancelled = false;
 
     protected Event(EventManager eventManager, EventType type, Object[] in) {
         this.eventManager = eventManager;
         this.type = type;
         this.in = in;
     }
 
     protected void schedule(List<EventListenerContainer> eventListenerContainerList) {
         type.onEvent(this);
         if (cancelled) return;
         if (eventListenerContainerList != null && eventListenerContainerList.size() > 0) {
             EventListenerContainer eventListenerContainer = null;
             for (Iterator<EventListenerContainer> i = eventListenerContainerList.iterator(); i.hasNext();) {
                 eventListenerContainer = i.next();
                 if (isFiltered(eventListenerContainer.eventListener, eventListenerContainer.getFilters())) {
                     try {
                         eventListenerContainer.method.invoke(eventListenerContainer.isStatic ? null
                                 : eventListenerContainer.eventListener, this);
                     } catch (IllegalAccessException e) {
                         e.printStackTrace();
                     } catch (IllegalArgumentException e) {
                         e.printStackTrace();
                     } catch (InvocationTargetException e) {
                         e.printStackTrace();
                     }
                     if (cancelled) return;
                 }
             }
         }
         done = true;
     }
 
     private final boolean isFiltered(Object eventListener, Object[][] eventFilters) {
         if (eventFilters.length == 0) return true;
         for (int i = 0; i < eventFilters.length; i++) {
             if (eventFilters[i].length > 0) {
                 try {
                     if (type.applyFilter(this, eventFilters[i], eventListener)) return true;
                 } catch (ArrayIndexOutOfBoundsException e) {
                 } catch (ClassCastException e) {
                 } catch (NullPointerException e) {
                 }
             }
         }
         return false;
     }
 
     /**
      * Returns the EventManager, that handles this Event
      * 
      * @return EventManager - the EventManager which handles this Event.
      */
     public final EventManager getEventManager() {
         return eventManager;
     }
 
     /**
      * Returns the EventType of this Event
      * 
      * @return EventType - the EventType this event is of.
      */
     public final EventType getEventType() {
         return type;
     }
 
     /**
      * Check if the Event is of the given EventType
      * 
      * @param eventType
      *            EventType - EventType to check for.
      * @return boolean - if the EventType was equal to the given one.
      */
     public final boolean isEventType(EventType eventType) {
         return type.equals(eventType);
     }
 
     /**
      * Cancel this EventStream to stop the process of calling all the other
      * EventListeners.
      */
     public final void cancel() {
         if (done) return;
         cancelled = true;
     }
 
     /**
      * Add objects to the output list.
      * 
      * @param obj
      *            Object - object to add to the output list.
      */
     public final void addOutput(Object obj) {
         out.add(obj);
     }
 
     /**
      * Check if the event is cancelled
      * 
      * @return boolean - true, if the event was cancelled.
      */
     public final boolean isCancelled() {
         return cancelled;
     }
 
     /**
      * Check, if all the data of an Async Event is collected.
      * 
      * @return boolean - true, if the event is done.
      */
     public final boolean isDone() {
         if (cancelled) return true;
         return done;
     }
 
     /**
      * Retrieve all objects given, when the event was called
      * 
      * @return Object[] - the objects
      */
     public final Object[] getInput() {
         return in;
     }
 
     /**
      * Retrieve a specific object given, when the event was called
      * 
      * @param num
      *            int - number of the object to request
      * @return Object - the requested object, or null if the number was out of
      *         range
      */
     public final Object getInput(int num) {
         if (num < 0 || num >= in.length) return null;
         return in[num];
     }
 
     /**
      * Retrieve a specific object given, when the event was called and cast it
      * to the given class
      * 
      * @param num
      *            int - number of the object to request
     * @param calst
      *            Class<T> the class to cast to
      * @return T - the requested object casted to T, or null if the number was
      *         out of range
      */
     @SuppressWarnings("unchecked")
     public final <T> T getInput(int num, Class<T> cast) {
         if (num < 0 || num >= in.length || !cast.isInstance(in[num])) return null;
         return (T) in[num];
     }
 
     /**
      * Use this to get all the objects out of the output list.
      * 
      * @return List<Object> - output list, or null if the event is not done.
      */
     public final List<Object> getOutput() {
         if (isDone()) return out;
         return null;
     }
 
     /**
      * Use this to get all the objects out of the output list, but sort out all
      * null values.
      * 
      * @return List<Object> - output list without null values, or null if the
      *         event is not done.
      */
     public final List<Object> getCleanOutput() {
         if (isDone()) {
             List<Object> cleanOut = new ArrayList<Object>(out);
             cleanOut.removeAll(Collections.singleton(null));
             return cleanOut;
         }
         return null;
     }
 
     @Deprecated
     public final String getType() {
         return type.name;
     }
 }
