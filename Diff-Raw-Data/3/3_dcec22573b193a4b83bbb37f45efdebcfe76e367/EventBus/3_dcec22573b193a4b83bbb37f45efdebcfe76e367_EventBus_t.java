 package client.event;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Very simple event bus.
  * All observers can register for events and an
  * observable can publish (send) events. 
  * @author hajo
  *
  */
 public class EventBus {
 
     private static List<EventHandler> handlers = 
             new ArrayList<EventHandler>();
 
     /**
      * Register as an event handler
      * @param handler, object implementing EventHandler interface
      */
     public static void register(EventHandler handler) {
         handlers.add(handler);
     }
     
     /**
      * Unregister as an event handler
      * @param handler, object to remove
      */
     public static void unRegister(EventHandler handler) {
         handlers.remove(handler);
     }
     
     /**
      * Publish (send) events to all registered event handlers
      * (broadcast)
      * @param evt, the event to broadcast
      */
     public static void publish( Event evt ) {
         // Tracking all events
         System.out.println(evt);
        List<EventHandler> tmp = new ArrayList<EventHandler>(handlers);
        for( EventHandler evh : tmp){    	
         	evh.onEvent(evt);
         }
     }
 }
