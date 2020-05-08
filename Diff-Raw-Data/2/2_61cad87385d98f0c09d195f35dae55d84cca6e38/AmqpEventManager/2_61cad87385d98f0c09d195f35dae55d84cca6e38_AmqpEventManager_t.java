 package pegasus.eventbus.amqp;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.TimeoutException;
 
 import org.apache.commons.lang.time.StopWatch;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pegasus.eventbus.amqp.AmqpMessageBus.UnexpectedConnectionCloseListener;
 import pegasus.eventbus.client.Envelope;
 import pegasus.eventbus.client.EnvelopeHandler;
 import pegasus.eventbus.client.EventHandler;
 import pegasus.eventbus.client.EventManager;
 import pegasus.eventbus.client.Subscription;
 import pegasus.eventbus.client.SubscriptionToken;
 
 /**
  * An implementation of the Event Manager based on the AMQP specification.
  * 
  * @author Ken Baltrinic (Berico Technologies)
  */
 public class AmqpEventManager implements EventManager, UnexpectedConnectionCloseListener {
 
     // DO NOT change these values, they are based on the in the AMPQ spec.
     static final String                                AMQP_ROUTE_SEGMENT_DELIMITER = ".";
     static final String                                AMQP_ROUTE_SEGMENT_WILDCARD  = "#";
 
     protected static final Logger                      LOG                          = LoggerFactory.getLogger(AmqpEventManager.class);
 
     private final String                               clientName;
     private final AmqpMessageBus                       messageBus;
     private final TopologyManager                      topologyManager;
     private final Serializer                           serializer;
 
     private Map<SubscriptionToken, ActiveSubscription> activeSubscriptions          = new HashMap<SubscriptionToken, ActiveSubscription>();
     private Map<Object, Envelope>                      envelopesBeingHandled        = new HashMap<Object, Envelope>();
     private Set<StartListener>                         startListeners               = new HashSet<StartListener>();
     private Set<CloseListener>                         closeListeners               = new HashSet<CloseListener>();
     private Set<SubscribeListener>                     subscribeListeners           = new HashSet<SubscribeListener>();
     private Set<UnsubscribeListener>                   unsubscribeListeners         = new HashSet<UnsubscribeListener>();
 
     /**
      * Instantiate the EventManager from configuration.
      * 
      * @param configuration
      *            Configuration object for the Event Manager.
      */
     public AmqpEventManager(AmqpConfiguration configuration) {
 
         LOG.info("Instantiating the AMQP Event Manager.");
 
         this.clientName = configuration.getClientName();
         this.messageBus = configuration.getAmqpMessageBus();
         this.topologyManager = configuration.getTopologyManager();
         this.serializer = configuration.getSerializer();
     }
 
     /**
      * Start the Event Manager and notify all of the start listeners
      */
     @Override
     public void start() {
 
         LOG.info("Starting the AMQP Event Manager.");
 
         LOG.trace("Notifying all named members.");
 
         messageBus.attachUnexpectedConnectionCloseListener(this);
         messageBus.start();
         topologyManager.start(this);
 
         notifyStartListeners();
     }
 
     /**
      * Close the Event Manager and stop all active subscriptions.
      */
     @Override
     public void close() {
 
         LOG.info("Shutting down the Event Manager.");
 
         notifyCloseListeners(false);
 
         topologyManager.close();
 
         LOG.trace("Deactivating all subscriptions.");
 
         synchronized (activeSubscriptions) {
         	deactivateSubscriptions(activeSubscriptions.values(), false);
         	
         	activeSubscriptions.clear();
 		}
 
         LOG.trace("Closing the connection to the broker.");
 
         messageBus.close();
     }
 
     /**
      * Publish an Event on the Bus.
      * 
      * @param event
      *            Event (message) to publish.
      */
     @Override
     public void publish(Object event) {
 
         LOG.debug("Publishing event of type [{}] on the bus.", event.getClass().getName());
 
         publish(event, null, false);
     }
 
     /**
      * Actual implementation of publishing a message on the bus, taking into account the need for a reply, or optionally, the publishing of this message as a reply to another event.
      * 
      * @param event
      *            Event to publish
      * @param replyToQueue
      *            ReplyTo Queue
      * @param sendToReplyToQueue
      *            Is this message being sent as a reply?
      */
     private void publish(Object event, String replyToQueue, boolean sendToReplyToQueue) {
 
         LOG.trace("Publishing event of type [{}].  Expect Response? {}; Is this a reply? = {}", new Object[] { event.getClass().getName(), replyToQueue != null, sendToReplyToQueue });
 
         LOG.trace("Finding the correct routing info for event [{}]", event.getClass().getName());
 
         RoutingInfo route = topologyManager.getRoutingInfoForEvent(event.getClass());
         if (route == null) {
 
             LOG.error(String.format("No route found for event {}", event));
 
             throw new RuntimeException(String.format("Unknown route for event {}", event));
         }
 
         if (sendToReplyToQueue) {
 
             LOG.trace("Creating Routing Info for the ReplyTo queue.");
 
             route = new RoutingInfo(route.getExchange(), route.getRoutingKey() + AmqpEventManager.AMQP_ROUTE_SEGMENT_DELIMITER + replyToQueue);
 
         } else {
 
             LOG.trace("Asserting route actually exists.");
 
             ensureRouteExists(route);
         }
 
         LOG.trace("Serializing the event to byte array.");
 
         byte[] body = serializer.serialize(event);
 
         LOG.trace("Creating envelope.");
 
         Envelope envelope = new Envelope();
         envelope.setId(UUID.randomUUID());
         envelope.setTopic(route.getRoutingKey());
         envelope.setEventType(event.getClass().getCanonicalName());
         envelope.setReplyTo(replyToQueue);
         envelope.setTimestamp(Calendar.getInstance().getTime());
         envelope.setBody(body);
 
         LOG.trace("Publishing to the message bus instance.");
 
         messageBus.publish(route, envelope);
     }
 
     /**
      * Subscribe to all events the supplied handler is capable of handling.
      * 
      * @param handler
      *            Event Handler
      */
     @Override
     public SubscriptionToken subscribe(EventHandler<?> handler) {
 
         LOG.debug("Subscribing Handler [{}] to Event Types: {}", getTypeNameSafely(handler), joinEventTypesAsString(handler));
 
         return subscribe(handler, getNewQueueName(), false);
     }
 
     /**
      * Subscribe to all events on a known queue with the supplied Event Handler.
      * 
      * @param handler
      *            Event Handler
      * @param queueName
      *            Name of the Known Queue
      * @return Subscription Token used to unregister the handler
      */
     @Override
     public SubscriptionToken subscribe(EventHandler<?> handler, String queueName) {
 
         LOG.debug("Subscribing Handler [{}] to known queue [{}] for Event Types: {}", new Object[] { getTypeNameSafely(handler), queueName, joinEventTypesAsString(handler) });
 
         return subscribe(handler, queueName, true);
     }
 
     /**
      * Subscribe to all events the supplied handler is capable of handling.
      * 
      * @param handler
      *            Envelope Handler
      */
     @Override
     public SubscriptionToken subscribe(EnvelopeHandler handler) {
 
         LOG.debug("Subscribing Envelope Handler [{}]", getTypeNameSafely(handler));
 
         return subscribe(handler, getNewQueueName(), false);
     }
 
     /**
      * Subscribe to all events on a known queue with the supplied Event Handler.
      * 
      * @param queueName
      *            Name of the Known Queue
      * @param handler
      *            Envelope Handler
      * @return Subscription Token used to unregister the handler
      */
     @Override
     public SubscriptionToken subscribe(EnvelopeHandler handler, String queueName) {
 
         LOG.debug("Subscribing Envelope Handler [{}] to known queue [{}]", new Object[] { getTypeNameSafely(handler), queueName });
 
         return subscribe(handler, queueName, true);
     }
 
     /**
      * Subscribe to all events on a known queue with the supplied Event Handler.
      * 
      * @param handler
      *            Event Handler
      * @param queueName
      *            Name of the Known Queue
      * @param isDurable
      *            Is the Queue Durable
      * @return Subscription Token used to unregister the handler
      */
     private SubscriptionToken subscribe(EventHandler<?> handler, String queueName, boolean isDurable) {
 
         LOG.debug("Subscribing Handler [{}] to known queue [{}] (is durable? = {}) for Event Types: {}", new Object[] { getTypeNameSafely(handler), queueName, isDurable,
                 joinEventTypesAsString(handler) });
 
         Subscription subscription = new Subscription(handler, queueName);
         subscription.setIsDurable(isDurable);
         return subscribe(subscription);
     }
 
     /**
      * Subscribe to all events on a known queue with the supplied Event Handler.
      * 
      * @param handler
      *            Envelope Handler
      * @param queueName
      *            Name of the Known Queue
      * @param isDurable
      *            Is the Queue Durable
      * @return Subscription Token used to unregister the handler
      */
     private SubscriptionToken subscribe(EnvelopeHandler handler, String queueName, boolean isDurable) {
 
         LOG.debug("Subscribing Handler [{}] to known queue [{}] (is durable? = {}) for Event Types: {}", new Object[] { getTypeNameSafely(handler), queueName, isDurable });
 
         Subscription subscription = new Subscription(handler, queueName);
         subscription.setIsDurable(isDurable);
         return subscribe(subscription);
     }
 
     /**
      * IOC friendly way to register a subscription with the bus.
      * 
      * @param subscription
      *            Subscription to register.
      */
     public SubscriptionToken subscribe(Subscription subscription) {
 
         LOG.debug("New subscription registered with the Event Bus client");
 
         if (subscription == null) {
 
             LOG.error("Subscription may not be null.");
 
             throw new IllegalArgumentException("Subscription may not be null.");
         }
 
         return subscribe(subscription, AMQP_ROUTE_SEGMENT_WILDCARD);
     }
 
     /**
      * Does the dirty work of actually subscribing to a particular queue, registering the subscription with the EventManager's internal list, and producing the token components will need to unbind
      * their active subscriptions.
      * 
      * @param subscription
      *            Subscription to register.
      * @param routeSuffix
      *            Used to capture all messages in a particular namespace (wild card)
      * @return Subscription Token that can be used to unbind the handler from the bus.
      */
     private SubscriptionToken subscribe(Subscription subscription, String routeSuffix) {
 
         LOG.trace("Locating route information for the provided subscription.");
 
         RoutingInfo[] routes = null;
         if (subscription.getEventHandler() == null) {
             routes = topologyManager.getRoutingInfoForNamedEventSet(subscription.getEnvelopeHandler().getEventSetName());
         } else {
             routes = getRoutesBaseOnEventHandlerHandledTypes(subscription.getEventHandler(), routeSuffix);
         }
         if (routes == null || routes.length == 0) {
 
             LOG.error(String.format("No routes found for subscription {}", subscription));
 
             throw new RuntimeException(String.format("Unknown routes for subscription {}", subscription));
         }
 
         LOG.trace("{} routes found for subscription.", (routes != null) ? routes.length : 0);
 
         LOG.trace("Ensuring routes exist.");
 
         for (RoutingInfo route : routes) {
             ensureRouteExists(route);
         }
 
         String queueName = subscription.getQueueName() == null ? getNewQueueName() : subscription.getQueueName();
 
         LOG.trace("Pulling EnvelopeHandler from subscription.");
 
         EnvelopeHandler handler = subscription.getEnvelopeHandler();
 
         if (handler == null) {
 
             LOG.trace("EnvelopeHandler was null, creating default (EventEnvelopeHandler) instead.");
 
             handler = new EventEnvelopeHandler(this, subscription.getEventHandler());
         }
 
         LOG.trace("Creating new queue listener for subscription.");
 
         QueueListener queueListener = new QueueListener(messageBus, queueName, subscription.getIsDurable(), routes, handler);
 
         SubscriptionToken token = new SubscriptionToken();
         
         synchronized (activeSubscriptions) {
         	LOG.trace("Starting the queue listener.");
         	
         	queueListener.beginListening();
         	
         	LOG.trace("Adding new active subscription with token to the 'active subscriptions' list.");
         	
         	activeSubscriptions.put(token, new ActiveSubscription(queueName, subscription.getIsDurable(), queueListener));
 		}
 
         notifySubscribeListeners(token);
 
         LOG.trace("Returning subscription token.");
 
         return token;
     }
 
     private void resubscribeAllActiveSubscriptions() {
         LOG.info("Attempting to re-subscribe all active subscriptions.");
 
         synchronized (activeSubscriptions) {
         	for (ActiveSubscription subscription : activeSubscriptions.values()) {
         		subscription.getListener().beginListening();
         	}
 		}
     }
 
     /**
      * Determine the correct routing information based on the "handled types" provided by the EventHandler.
      * 
      * @param eventHandler
      *            EventHandler that provided the types that need to be mapped to routes
      * @param routeSuffix
      *            Suffix to append on route bindings
      * @return Array of Routes that apply to that Handler
      */
     private RoutingInfo[] getRoutesBaseOnEventHandlerHandledTypes(EventHandler<?> eventHandler, String routeSuffix) {
 
         LOG.trace("Getting routes handled by event handler [{}]", eventHandler.getClass().getName());
 
         ArrayList<RoutingInfo> routes = new ArrayList<RoutingInfo>();
 
         final Class<?>[] handledEventTypes = eventHandler.getHandledEventTypes();
 
         for (Class<?> eventType : handledEventTypes) {
 
             LOG.trace("Getting route for [{}]", eventType.getName());
 
             RoutingInfo route = topologyManager.getRoutingInfoForEvent(eventType);
             if (route == null) {
 
                 LOG.error(String.format("No route found for eventType {}", eventType.getName()));
 
                 throw new RuntimeException(String.format("Unknown route for eventType {}", eventType.getName()));
             }
 
             LOG.trace("Route: {}", route);
 
             // Assuming we want to ensure that we not only catch types that match the canonical class name
             // but also anything past it in the hierarchy. This is needed to support RPC routing keys which have
             // call-specific suffixes.
 
             if (routeSuffix == AMQP_ROUTE_SEGMENT_WILDCARD) {
                 routes.add(route);
             }
 
             route = new RoutingInfo(route.getExchange(), route.getRoutingKey() + AMQP_ROUTE_SEGMENT_DELIMITER + routeSuffix);
 
             routes.add(route);
         }
 
         LOG.trace("Found [{}] routes for event handler [{}]", routes.size(), eventHandler.getClass().getName());
 
         return routes.toArray(new RoutingInfo[0]);
     }
 
     /**
      * Get random queue name for this client
      * 
      * @return Random Queue Name
      */
     private String getNewQueueName() {
         return clientName + ":" + UUID.randomUUID().toString();
     }
 
     private HashSet<String> exchangesKnownToExist = new HashSet<String>();
 
     /**
      * Ensure the supplied route currently exists.
      * 
      * @param routingInfo
      *            Route Info
      */
     private void ensureRouteExists(RoutingInfo routingInfo) {
 
         LOG.trace("Ensuring route exists: {}", routingInfo);
 
         final String exchangeName = routingInfo.getExchange().getName();
 
         if (exchangesKnownToExist.contains(exchangeName)) {
 
             LOG.trace("Route already exists. Done.");
 
             return;
         }
 
         LOG.debug("Route did not exist, attempting to create exchange [{}] to ensure it exists on the bus.", routingInfo.getExchange().getName());
 
         messageBus.createExchange(routingInfo.getExchange());
 
         LOG.trace("Adding route to the known routes list.");
 
         exchangesKnownToExist.add(exchangeName);
     }
 
     /**
      * Handle the Responses to the supplied event with the provided Event Handler
      * 
      * @param event
      *            Event published
      * @param handler
      *            Event Handler taking care of responses.
      * @return Subscription Token that can be used to unbind the handler from the bus.
      */
     @Override
     public SubscriptionToken getResponseTo(Object event, EventHandler<?> handler) {
 
         LOG.debug("Publishing event of type [{}] and handling responses with [{}]", event.getClass().getName(), getTypeNameSafely(handler));
 
         String replyToQueueName = getNewQueueName();
 
         LOG.trace("Creating reply-to queue with name [{}].", replyToQueueName);
 
         Subscription subscription = new Subscription(handler);
         subscription.setQueueName(replyToQueueName);
 
         LOG.trace("Creating subscription to responses on the reply-to queue.");
 
         SubscriptionToken token = subscribe(subscription, replyToQueueName);
 
         LOG.trace("Blocking the active until consumer is registered.");
 
         while (!activeSubscriptions.get(token).listener.isCurrentlyListening()) {
             try {
 
                 // LOG.trace("RPC: Sleeping for 10ms");
 
                 Thread.sleep(10);
 
             } catch (InterruptedException e) {
 
                 LOG.error("Thread was interrupted when waiting for responses to the reply-to queue.", e);
 
                 break;
             }
         }
 
         LOG.trace("Publishing the event and waiting for responses.");
 
         publish(event, replyToQueueName, false);
 
         LOG.trace("Returning Subscription Token");
 
         return token;
     }
 
     /**
      * Handle the Responses to the supplied event, but return the result immediately (blocking until result has been received - RPC)
      * 
      * @param event
      *            Event published
      * @param timeoutMills
      *            Time to wait for result before quitting
      * @param responseTypes
      *            The expected response types
      * @return Return an event of the expected TResponse type
      */
     @Override
     public <TResponse> TResponse getResponseTo(Object event, int timeoutMills, Class<? extends TResponse>... responseTypes) throws InterruptedException, TimeoutException {
 
         LOG.debug("Publishing event of type [{}] and expecting a response of types [{}]", event.getClass().getName(), joinEventTypesAsString(responseTypes));
 
         LOG.trace("Registering a CallbackHandler for collecting the Responses");
 
         CallbackHandler<TResponse> handler = new CallbackHandler<TResponse>(responseTypes);
 
         SubscriptionToken token = getResponseTo(event, handler);
 
         try {
 
             LOG.debug("Waiting for response {}ms, then returning result.", timeoutMills);
 
             return waitForAndReturnResponse(handler, timeoutMills);
 
         } finally {
 
             LOG.debug("Unregistering subscription for getResponseTo");
 
             unsubscribe(token);
         }
     }
 
     /**
      * Waits for a response to occur (if the timeout doesn't occur first), then returns the result.
      * 
      * @param handler
      *            CallbackHandler that will collect the result
      * @param timeoutMills
      *            Time in milliseconds to wait for response.
      * @return The response from another service on the bus.
      * @throws InterruptedException
      * @throws TimeoutException
      */
     private <TResponse> TResponse waitForAndReturnResponse(CallbackHandler<TResponse> handler, int timeoutMills) throws InterruptedException, TimeoutException {
 
         int sleepInterval = Math.min(50, timeoutMills / 10);
 
         StopWatch watch = new StopWatch();
         watch.start();
 
         LOG.trace("Attempting to get response.");
 
         TResponse response = handler.getReceivedResponse();
 
         if (response == null) {
 
             LOG.trace("Response was null, looping until response received or timeout reached.");
         }
 
         while (watch.getTime() < timeoutMills && response == null) {
 
             LOG.trace("Still nothing, sleeping {}ms.", sleepInterval);
 
             Thread.sleep(sleepInterval);
 
             LOG.trace("Attempting to get Response from bus again.");
 
             response = handler.getReceivedResponse();
         }
 
         watch.stop();
 
         if (response == null) {
 
             LOG.error("Response was not received within the time specified.");
 
             throw new TimeoutException("Response was not received within the time specified.");
         }
 
         LOG.trace("Response of type [{}] received.", response.getClass().getName());
 
         return response;
     }
 
     /**
      * Respond to an event with another event.
      * 
      * @param originalRequest
      *            Original Event
      * @param response
      *            The Event used to Respond to the first.
      */
     @Override
     public void respondTo(Object originalRequest, Object response) {
 
         LOG.debug("Responding to event [{}] with event [{}]", originalRequest.getClass().getName(), response.getClass().getName());
 
         LOG.trace("Pulling original event's envelope.");
 
         Envelope originalRequestEnvelope = envelopesBeingHandled.get(originalRequest);
 
         if (originalRequestEnvelope.getReplyTo() == null) {
 
             LOG.warn("No reply-to address on the original event, publishing response as normal event.");
 
             publish(response);
 
         } else {
 
             LOG.trace("Publishing response [{}] to reply-to queue [{}].", response.getClass().getName(), originalRequestEnvelope.getReplyTo());
 
             publish(response, originalRequestEnvelope.getReplyTo(), true);
         }
     }
 
     /**
      * Unsubscribe a handler from a particular event using the Subscription Token.
      * 
      * @param token
      *            Subscription Token
      */
     @Override
     public void unsubscribe(SubscriptionToken token) {
         unsubscribe(token, false);
     }
 
     /**
      * Unsubscribe a handler from a particular event using the Subscription Token.
      * 
      * @param token
      *            Subscription Token
      * @param deleteQueue
      *            Should the queue be removed too?
      */
     @Override
     public void unsubscribe(SubscriptionToken token, boolean deleteQueue) {
 
     	LOG.debug("Unsubscribing handlers corresponding to this token: {}", token);
     	
     	synchronized (activeSubscriptions) {
     		ActiveSubscription subscription = activeSubscriptions.get(token);
     		
     		if (subscription == null) {
     			
     			LOG.error("The provided token does not refer to an active subscription of this event manager.");
     			
     			throw new IllegalStateException("The provided token does not refer to an active subscription of this event manager.");
     		}
     		
     		ArrayList<ActiveSubscription> subscriptions = new ArrayList<ActiveSubscription>();
     		
     		subscriptions.add(subscription);
     		
     		LOG.trace("Deactivating the handlers corresponding to the subscription (delete queue? = {})", deleteQueue);
     		
     		deactivateSubscriptions(subscriptions, deleteQueue);
     		
     		LOG.trace("Removing token from the 'active subscriptions' list.");
     		
     		activeSubscriptions.remove(token);
 		}
 
         notifyUnsubscribeListeners(token);
     }
 
     /**
      * Deactivate activate subscriptions listed in the provided subscriptions list (and optionally, delete the queues).
      * 
      * @param subscriptions
      *            List of subscriptions to be deactivated
      * @param deleteDurableQueues
      *            Should these queues be deleted?
      */
     private void deactivateSubscriptions(Collection<ActiveSubscription> subscriptions, boolean deleteDurableQueues) {
 
         LOG.debug("Deactivating subscriptions, stopping all listeners.");
 
         for (ActiveSubscription subscription : subscriptions) {
             subscription.getListener().StopListening();
         }
 
         boolean someListenersAreStillListening = !subscriptions.isEmpty();
 
         while (someListenersAreStillListening) {
             someListenersAreStillListening = false;
             for (ActiveSubscription subscription : subscriptions) {
                 if (subscription.getListener().isCurrentlyListening()) {
                     someListenersAreStillListening = true;
                     try {
 
                         LOG.trace("Some of the subscriptions are taking a while to shutdown.  Sleeping for 50ms.");
 
                         Thread.sleep(50);
                     } catch (InterruptedException e) {
                         LOG.debug("Thread [" + Thread.currentThread().getName() + "] interrupted in method AmqpEventManager.deactivateSubscriptions().");
                     }
                     break;
                 }
             }
         }
 
         if (deleteDurableQueues) {
 
             LOG.trace("Deleting all queues provided in the deactivated subscriptions list.");
         }
 
         for (ActiveSubscription subscription : subscriptions) {
             if (deleteDurableQueues || !subscription.getQueueIsDurable()) {
 
                 LOG.trace("Deleting queue [{}]", subscription.getQueueName());
 
                 messageBus.deleteQueue(subscription.getQueueName());
             }
         }
        
        // TODO: PEGA-730 BUG! We are not closing the subscription-specific amqp channel!
     }
 
     @Override
     public void onUnexpectedConnectionClose(boolean connectionSuccessfullyReopened) {
         if (connectionSuccessfullyReopened) {
             resubscribeAllActiveSubscriptions();
         } else {
             // TODO: we should invoke the close listeners with a flag to let them no that this is an unplanned close
             // and otherwise do any needed cleanup.
         }
     }
 
     /**
      * Joins a list of event types into a comma separated string
      * 
      * @param eventTypes
      *            List of event types
      * @return list of event types as a comma separated string
      */
     private static String getTypeNameSafely(Object obj) {
         return obj == null ? "NULL" : obj.getClass().getName();
     }
 
     private static String joinEventTypesAsString(EventHandler<?> handler) {
         if (handler == null)
             return "NULL";
         return joinEventTypesAsString(handler.getHandledEventTypes());
     }
 
     private static String joinEventTypesAsString(Class<?>[] eventTypes) {
         StringBuilder sb = new StringBuilder();
         for (Class<?> eventType : eventTypes) {
             sb.append(", ").append(eventType.getName());
         }
         return sb.substring(1);
     }
 
     public AmqpMessageBus getAmqpMessageBus() {
         return messageBus;
     }
 
     public Serializer getSerializer() {
         return serializer;
     }
 
     public Map<Object, Envelope> getEnvelopesBeingHandled() {
         return envelopesBeingHandled;
     }
 
     @Override
     public void attachStartListener(StartListener listener) {
         if (listener == null) {
             throw new IllegalArgumentException("Listener cannot be null.");
         }
         startListeners.add(listener);
     }
 
     @Override
     public void attachCloseListener(CloseListener listener) {
         if (listener == null) {
             throw new IllegalArgumentException("Listener cannot be null.");
         }
         closeListeners.add(listener);
     }
 
     @Override
     public void attachSubscribeListener(SubscribeListener listener) {
         if (listener == null) {
             throw new IllegalArgumentException("Listener cannot be null.");
         }
         subscribeListeners.add(listener);
     }
 
     @Override
     public void attachUnsubscribeListener(UnsubscribeListener listener) {
         if (listener == null) {
             throw new IllegalArgumentException("Listener cannot be null.");
         }
         unsubscribeListeners.add(listener);
     }
 
     @Override
     public void detachStartListener(StartListener listener) {
         if (listener == null) {
             throw new IllegalArgumentException("Listener cannot be null.");
         }
         startListeners.remove(listener);
     }
 
     @Override
     public void detachCloseListener(CloseListener listener) {
         if (listener == null) {
             throw new IllegalArgumentException("Listener cannot be null.");
         }
         closeListeners.remove(listener);
     }
 
     @Override
     public void detachSubscribeListener(SubscribeListener listener) {
         if (listener == null) {
             throw new IllegalArgumentException("Listener cannot be null.");
         }
         subscribeListeners.remove(listener);
     }
 
     @Override
     public void detachUnsubscribeListener(UnsubscribeListener listener) {
         if (listener == null) {
             throw new IllegalArgumentException("Listener cannot be null.");
         }
         unsubscribeListeners.remove(listener);
     }
 
     public void notifyStartListeners() {
 
         LOG.trace("Notifying all start listeners.");
 
         for (StartListener listener : startListeners) {
             listener.onStart();
         }
     }
 
     public void notifyCloseListeners(boolean unexpected) {
 
         LOG.trace("Notifying all close listeners.");
 
         for (CloseListener listener : closeListeners) {
             listener.onClose(unexpected);
         }
     }
 
     public void notifySubscribeListeners(SubscriptionToken subscriptionToken) {
 
         LOG.trace("Notifying all subscribe listeners.");
 
         for (SubscribeListener listener : subscribeListeners) {
             listener.onSubscribe(subscriptionToken);
         }
     }
 
     public void notifyUnsubscribeListeners(SubscriptionToken subscriptionToken) {
 
         LOG.trace("Notifying all unsubscribe listeners.");
 
         for (UnsubscribeListener listener : unsubscribeListeners) {
             listener.onUnsubscribe(subscriptionToken);
         }
     }
 
 }
