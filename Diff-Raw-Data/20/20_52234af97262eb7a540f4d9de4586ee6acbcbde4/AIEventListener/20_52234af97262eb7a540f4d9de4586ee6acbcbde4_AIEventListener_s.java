 package ai.ilikeplaces.util;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventListener;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Ravindranath Akila
  * Date: 10/29/11
  * Time: 8:43 AM
  */
 abstract public class AIEventListener<T> implements EventListener {
 
     private static final boolean DEBUG_ENABLED = Loggers.DEBUG.isDebugEnabled();
     private static final String EVENT_TYPE = "Event Type: ";
     private static final String EVENT_TARGET = "Event Target: ";
     private static final String COMMA_SEP_1 = ", ";
     protected T criteria;
 
    protected AIEventListener() {
    }

     protected AIEventListener(final T criteria) {
         this.criteria = criteria;
     }
 
     @Override
     public void handleEvent(final Event evt) {
         onFire(log(evt));
     }
 
     /**
      * Override this method and avoid {@link #handleEvent(org.w3c.dom.events.Event)} to make debug logging transparent
      *
      * @param evt fired from client
      */
     protected void onFire(final Event evt) {
     }
 
     /**
      * @param event sent for logging if debug enabled
      * @return event sent
      */
     protected Event log(final Event event) {
         if (DEBUG_ENABLED) {
             if (event != null) {
                 Loggers.DEBUG.debug(EVENT_TYPE + event.getType() + COMMA_SEP_1 + EVENT_TARGET + ((Element) event.getCurrentTarget()).getAttribute(MarkupTag.GENERIC.id()));
             } else {
                 Loggers.DEBUG.debug(EVENT_TYPE + null + COMMA_SEP_1 + EVENT_TARGET + null);
             }
         }
         return event;
     }
 
 }
