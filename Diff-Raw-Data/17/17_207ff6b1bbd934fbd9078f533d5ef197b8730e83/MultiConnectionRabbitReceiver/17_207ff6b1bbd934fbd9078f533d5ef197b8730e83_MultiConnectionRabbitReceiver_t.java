 package amp.rabbit.transport;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cmf.bus.IDisposable;
 import cmf.bus.IRegistration;
 import amp.bus.IEnvelopeReceivedCallback;
 import amp.rabbit.connection.IConnectionManager;
 import amp.rabbit.connection.IRabbitConnectionFactory;
 import amp.rabbit.dispatch.IListenerCloseCallback;
 import amp.rabbit.dispatch.RabbitListener;
 import amp.rabbit.topology.ConsumingRoute;
 import amp.rabbit.topology.RoutingInfo;
 
 /**
  * Based on the routing information, sets up listeners on the consuming routes.
  * 
  * NOTE:  This class will accept partial failures on the routes.  [For example,  if Broker B
  * isn't available and we can't establish a listener to it, it will be ignored and
  * we will continue trying to listen to brokers A & C]
  * 
  * @author jmccune
  *
  */
 public class MultiConnectionRabbitReceiver implements IDisposable{
 
 	private static final Logger LOG = LoggerFactory.getLogger(MultiConnectionRabbitReceiver.class);
 	
 	Collection<RabbitListener> listeners= new ArrayList<RabbitListener>();
 	
 	private IEnvelopeReceivedCallback envelopeHandler;
 	
 	/**
 	 * Creates a multi-channel/multi-connection receiver based on the given routingInfo.
 	 * @param connectionFactory is used to create the channels (backed by connections) for communication.
 	 * @param routingInfo contains the information of how many RabbitMq listeners & channels we will need.
 	 * @param registration is the object/key by which we register & unregister the listeners.
 	 * @param handler  is used to process whatever envelope/message comes in on the channels implied by the routingInfo.
 	 * @throws Exception  can occur when configuring/establishing the connections doesn't work. 
 	 */
 	public  MultiConnectionRabbitReceiver(IRabbitConnectionFactory connectionFactory, 
 			RoutingInfo routingInfo, IRegistration registration, IEnvelopeReceivedCallback handler) throws Exception {
 		
		if (handler == null || registration==null) {
 			throw new NullPointerException();
 		}
 		
 		this.envelopeHandler = handler;		
 		
 		//There are many possible routes
 		for (ConsumingRoute route: routingInfo.getConsumingRoutes()) {
 				
 			// Each route may talk to multiple brokers  (requiring multiple channels) 
 			// which should be over exactly one connection...
 			for (IConnectionManager channelSupplier : connectionFactory.getConnectionManagersFor(route)) {
 				
 				try {
 					
 					//Create one listener for each connection/broker to the given exchange.
 					RabbitListener listener =  createListener(registration, route, channelSupplier);
 					listeners.add(listener);
 				}
 				catch (Exception x) {
 					//Accept partial (or worst case complete) failure...
 					LOG.warn("Unable to establish a listener  for route: "+route);
 				}							
 			}			
 		}				
 	}
 	
 	
 	public void stopListening() {
 		for (RabbitListener listener: listeners) {
 			try {
 				listener.stopListening();
 			} catch (Exception x) {
 				LOG.error("Unable to stop listening for a particular Rabbit listener... " , x);
 			}
 		}
 	}
 	
 	
 	@Override
 	public void dispose() {
 
 		//TODO:  JM - I believe this is broken and doesn't work as desired-- need to verify & discuss with JRuiz.
 //		try {  this..dispose(); } catch (Exception ex) { }
 //
 //	    try {  topologyService.dispose(); } catch (Exception ex) { }
 //
 //        for (RabbitListener l : listeners.values()) {
 //	            try { l.dispose(); } catch (Exception ex) { }
 //	     }
 		
 	}
 
 
 	
 	protected RabbitListener createListener(IRegistration registration,
 			ConsumingRoute route, IConnectionManager channelSupplier) throws Exception {
 
 		// create a listener
 		RabbitListener listener = this.getListener(registration, route, channelSupplier);
 
 		// hook into the listener's events
 		listener.onEnvelopeReceived(envelopeHandler);
 
 		listener.onClose(new IListenerCloseCallback() {
 
 			/** How does this one happen? */
 			@Override
 			public void onClose(IRegistration registration, RabbitListener listener) {
 				listeners.remove(listener);
 				//TODO: >>> if (listeners.isEmpty())  --- then remove this from the parent...			
 			}			
 		});
 
 		listener.start();
 
 		return listener;
 	}
 	
 	
 	/**
      * Get a new Rabbit Listener for the provided registration and exchange.
      * This was pulled out as an extension point for deriving classes, as well as,
      * to make testing a little easier.
      * 
      * @param registration Handlers and hints
      * @param exchange Routing Information
      * @return Listener that will pull messages from the broker and call the handlers
      * on the registration.
      * @throws Exception 
      */
     protected RabbitListener getListener(IRegistration registration, ConsumingRoute route, IConnectionManager connectionMgr) throws Exception {
     		
         return new RabbitListener(registration, route, connectionMgr);
     }
 }
