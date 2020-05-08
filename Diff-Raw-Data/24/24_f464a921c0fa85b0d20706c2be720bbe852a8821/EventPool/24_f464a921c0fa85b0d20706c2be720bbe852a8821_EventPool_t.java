 package ch9k.eventpool;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Multimap;
 import java.util.Iterator;
 
 /**
  * Distributes events across the application
  * @author Pieter De Baets
  */
 public class EventPool {
     
    private EventPool() {}
     
     public static EventPool getInstance() {
        return SingletonHolder.INSTANCE;
     }
     
    private static class SingletonHolder { 
         private static final EventPool INSTANCE = new EventPool();
    }

     private Multimap<String,EventListener> listeners = ArrayListMultimap.create();
 
     /**
      * Add a new Event-listener that will listen to a given set of events
      * as defined by the filter.
      * @param listener
      * @param filter
      */
     public void addListener(EventListener listener, EventFilter filter) {
         // TODO add eventfilter too, sometimes..
         for(String className : filter.getMatchedEventIds()) {
             listeners.put(className, listener);
         }
     }
 
     /**
      * Send an event through the system
      * @param event
      */
     public void raiseEvent(Event event) {
         EventHeritageIterator classIterator = new EventHeritageIterator(event.getClass());
         
         while(classIterator.hasNext()) {
             sendEvent(classIterator.next(), event);
         }
     }
 
     /**
      * Send an event through the network
      * (will also be broadcast in the local pool?)
      * @param networkEvent
      */
     public void raiseEvent(NetworkEvent networkEvent) {
 
     }
 
     private void sendEvent(String next, Event event) {
         for(EventListener listener : listeners.get(next)) {
             listener.handleEvent(event);
         }
     }
 }
