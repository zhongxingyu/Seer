 package pegasus.eventbus.amqp;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pegasus.eventbus.client.Envelope;
 import pegasus.eventbus.client.EnvelopeHandler;
 import pegasus.eventbus.client.EventHandler;
 import pegasus.eventbus.client.EventResult;
 
 /**
  * The default implementation of the EnvelopeHandler. When an event occurs on the bus, an EventEnvelopeHandler specific to that subscription is responsible for attempting to execute the the
  * EventHandler.
  * 
  * @author Ken Baltrinic (Berico Technologies)
  */
 class EventEnvelopeHandler implements EnvelopeHandler {
 
     private final Logger              LOG;
 
     private final AmqpEventManager    amqpEventManager;
     private final EventHandler<?>     eventHandler;
     private String                    eventSetName;
     private final ArrayList<Class<?>> handledTypes;
     private Method                    handlerMethod;
 
     public EventEnvelopeHandler(AmqpEventManager amqpEventManager, EventHandler<?> eventHandler) {
 
         this.amqpEventManager = amqpEventManager;
 
        LOG = LoggerFactory.getLogger(String.format("{}", EventEnvelopeHandler.class));
 
         LOG.trace("EventEnvelopeHandler instantiated for EventHandler of type {}", eventHandler.getClass().getName());
 
         this.eventHandler = eventHandler;
 
         handledTypes = new ArrayList<Class<?>>();
         for (Class<?> eventType : eventHandler.getHandledEventTypes()) {
             handledTypes.add(eventType);
         }
 
         LOG.trace("Locating the 'Genericized' handleEvent method in the list of EventHandler's methods.");
 
         for (Method method : eventHandler.getClass().getMethods()) {
             if (method.getName() == "handleEvent") {
 
                 LOG.trace("Found the 'handleEvent' method, saving a reference to it.");
 
                 handlerMethod = method;
                 break;
             }
         }
 
         // This should never actually happen
         if (handlerMethod == null) {
 
             LOG.error("EventHandler [{}] does not have a method called 'handleEvent', violating the contract of the EventHandler interface.", eventHandler.getClass().getName());
 
             throw new RuntimeException("eventHandler method not found on EvenHandler of type " + eventHandler.getClass());
         }
     }
 
     /**
      * An event has occurred on the Event Bus, and now it is time to handle the message Envelope. We first begin by determining whether we can actually handle the event with the provided EventHandler.
      * If the event can be handled, we attempt to deserialize the event and then provide it to the EventHandler. The result of the EventHandler is returned.
      * 
      * @param envelope
      *            The envelope that represents the message.
      * @return Resulting state of how the Event was handled.
      */
     @Override
     public EventResult handleEnvelope(Envelope envelope) {
 
         LOG.debug("Handling envelope of type [{}]", envelope.getEventType());
 
         EventResult result = EventResult.Failed;
 
         try {
             Object event = null;
 
             try {
                 String className = envelope.getEventType();
 
                 LOG.trace("Determining if the event type is a class on this Java process's classpath.");
 
                 Class<? extends Object> eventType = Class.forName(className);
 
                 LOG.trace("Event Class was found on classpath.");
 
                 LOG.trace("Determining if the EventHandler can handle the received Event Type.");
                 
                 if (handledTypes.contains(eventType)) {
                 	
                 	LOG.trace("The EventHandler can handle type, attempting to deserialize.");
                 	
                     event = this.amqpEventManager.getSerializer().deserialize(envelope.getBody(), eventType);
                     
                     LOG.trace("Event deserialized without error: {}", event);
                     
                 } else {
                 	
                 	LOG.trace("Event cannot be handled by this EventHandler [{}].  Failing the event.", 
                 			eventHandler.getClass().getName());
                 	
                 	return EventResult.Failed;
                 }
              
             } catch (Exception e) {
 
                 LOG.error("Could not handle event type with the supplied EventHandler (deserialization or forname exception).", e);
 
                 // re-throwing exception to be handled in parent catch
                 throw e;
             }
             
             LOG.trace("Adding envelope to envelopesBeingHandled map.");
 
             // NOTE: For performance sake, we are not synchronizing our
             // access to envelopesBeingHandled
             // Due to the nature of our keys the kinds of concerns
             // synchronizing defends against should
             // never occur. Rarely should any two threads ever be looking at
             // the same event. (This would
             // require that the handler spawn another thread and that thread
             // call respondTo. It is this
             // scenario that causes us not to just use ThreadLocal<Envelope>
             // here.) And in all cases,
             // never should there ever be the potential for an insert or
             // remove of the same event on
             // separate threads.
             this.amqpEventManager.getEnvelopesBeingHandled().put(event, envelope);
 
             try {
 
                 LOG.debug("Presenting the strongly-typed event to the EventHandler.");
 
                 result = (EventResult) handlerMethod.invoke(eventHandler, event);
 
                 if (result == EventResult.Failed) {
 
                     LOG.debug("EventHandler [{}] declared that it failed to handle the Event [{}].", eventHandler.getClass().getName(), event.getClass().getName());
 
                 } else {
 
                     LOG.debug("EventHandler [{}] successfully handled the Event [{}].", eventHandler.getClass().getName(), event.getClass().getName());
 
                 }
             } catch (Exception e) {
 
                 LOG.error("EventHandler failed to handle event (exception thrown in handler).", e);
 
             } finally {
 
                 LOG.trace("Removing envelope from envelopesBeingHandled map.");
 
                 this.amqpEventManager.getEnvelopesBeingHandled().remove(event);
             }
         } catch (Exception e) {
 
             LOG.error("Unable to handle message: {}", envelope, e);
 
         }
 
         return result;
     }
 
     @Override
     public String getEventSetName() {
         return eventSetName;
     }
 }
