 /**
  * 
  */
 package eu.indenica.common;
 
 import java.util.Collection;
 import java.util.concurrent.Semaphore;
 
 import ch.qos.logback.classic.Level;
 import ch.qos.logback.classic.Logger;
 import eu.indenica.events.Event;
 import eu.indenica.events.EventOne;
 
 /**
  * @author Christian Inzinger
  * 
  */
 public class TestUtils {
     private final static Logger LOG = (Logger) LoggerFactory.getLogger();
 
     /**
      * Set sensible logging levels, i.e. crank up our test logs, tone down
      * everything else.
      */
     public static void setLogLevels() {
         ((Logger) LoggerFactory.getLogger("root")).setLevel(Level.INFO);
         ((Logger) LoggerFactory.getLogger("eu.indenica")).setLevel(Level.TRACE);
     }
 
     /**
      * Creates a default event listener for {@link EventOne}
      * 
      * @param pubSub
      *            the messaging fabric to use
      * @param observedEvents
      *            a collection to put received events in
      * @param msgWaitLock
      *            a semaphore to lock for external synchronization
      */
     public static void
             createEventListener(final PubSub pubSub,
                     final Collection<Event> observedEvents,
                     final Semaphore msgWaitLock) {
         createEventListener(pubSub, new EventOne().getEventType(),
                 observedEvents, msgWaitLock);
     }
 
     /**
      * Creates an event listener for the specified event type.
      * 
      * @param pubSub
      *            the messaging fabric to use
      * @param eventType
      *            the event type to listen for
      * @param observedEvents
      *            a collection to put received events in
      * @param msgWaitLock
      *            a semaphore to lock for external synchronization
      */
     public static void createEventListener(final PubSub pubSub,
             final String eventType, final Collection<Event> observedEvents,
             final Semaphore msgWaitLock) {
         createEventListener(pubSub, null, eventType, observedEvents,
                 msgWaitLock);
     }
 
     /**
      * Creates an event listener for the specified event type.
      * 
      * @param pubSub
      *            the messaging fabric to use
      * @param source
      *            the component name to listen for
      * @param eventType
      *            the event type to listen for
      * @param observedEvents
      *            a collection to put received events in
      * @param msgWaitLock
      *            a semaphore to lock for external synchronization
      */
     public static void
             createEventListener(final PubSub pubSub, final String source,
                     final String eventType,
                     final Collection<Event> observedEvents,
                     final Semaphore msgWaitLock) {
         StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
         final String caller =
                 stackTrace[stackTrace[2].getMethodName().startsWith(
                         "createEvent") ? 3 : 2].getMethodName();
         pubSub.registerListener(new EventListener() {
             @Override
             public void eventReceived(String source, Event event) {
                 LOG.debug("{} - Received event {} in {} from {}", new Object[] {
                         caller, event, pubSub, source });
                if(observedEvents != null)
                    observedEvents.add(event);
                 msgWaitLock.release();
             }
         }, source, eventType);
     }
 }
