 package amp.rabbit.dispatch;
 
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.Semaphore;
 
 import cmf.bus.Envelope;
 import cmf.bus.IDisposable;
 import cmf.bus.IEnvelopeFilterPredicate;
 import cmf.bus.IRegistration;
 import com.rabbitmq.client.AlreadyClosedException;
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.LongString;
 import com.rabbitmq.client.QueueingConsumer;
 import com.rabbitmq.client.ShutdownListener;
 import com.rabbitmq.client.QueueingConsumer.Delivery;
 import com.rabbitmq.client.ShutdownSignalException;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import amp.messaging.EnvelopeHelper;
 import amp.bus.IEnvelopeReceivedCallback;
 import amp.rabbit.connection.IConnectionEventHandler;
 import amp.rabbit.connection.IConnectionManager;
 import amp.rabbit.topology.Exchange;
 
 
 public class RabbitListener implements IDisposable, Runnable {
     
 	public static int DELIVERY_INTERVAL = 100;
 
 
     /**
      * (Intentional) Close Listeners
      */
     protected List<IListenerCloseCallback> closeCallbacks
     		= new ArrayList<IListenerCloseCallback>();
     
     /**
      * Envelope Received Listeners
      */
     protected List<IEnvelopeReceivedCallback> envCallbacks 
     		= new ArrayList<IEnvelopeReceivedCallback>();
     
     /**
      * Connection Error Listeners
      */
     protected IConnectionManager connectionManager;
     protected Exchange exchange;
     protected Logger log;
     protected IRegistration registration;
     protected volatile boolean shouldContinue;
     protected volatile boolean isRunning;
     protected Thread threadImRunningOn = null;
     protected CountDownLatch threadStartSignal = new CountDownLatch(1);
     protected Semaphore isListeningSemaphore;
     protected boolean connectionClosed;
 
     /**
      * Get the Exchange this listener is listening to.
      * @return AMQP Exchange
      */
     public Exchange getExchange(){
         return this.exchange;
     }
 
     /**
      * Get the Registration this listener applies to.
      * @return EnvelopeBus Registration
      */
     public IRegistration getRegistration(){
         return this.registration;
     }
 
 
     /**
      * Initialize the Listener with the Registration, Exchange, and ConnectionManager
      * @param registration
      * @param exchange
      * @param connectionManager
      */
     public RabbitListener(IRegistration registration, Exchange exchange, IConnectionManager connectionManager) {
 
         this.registration = registration;
         this.exchange = exchange;
         this.connectionManager = connectionManager;
         connectionManager.addConnectionEventHandler(new ConnectionClosedHandler());
         this.isListeningSemaphore = new Semaphore(1);
 
         log = LoggerFactory.getLogger(this.getClass());
     }
 
     
     /**
      * Start listening on a new thread.  This won't work unless you
      * have set the Channel on the listener.
      */
     public void start() throws Exception {
     	startOnNewThread();
 
         threadStartSignal.await(30, TimeUnit.SECONDS);
     }
 
 	private void startOnNewThread() {
 		isRunning = true;
 		
 		threadImRunningOn = new Thread(this);
 		
 		threadImRunningOn.start();
 	}
     
     private void restart() {
     	if(isRunning){
             //Only restart if stop has not been called in the mean time (or conceivably, we never started to begin with).
     		startOnNewThread();
     	}
     }
     
     public void bind(Channel channel, IRegistration registration, Exchange exchange) {
     	
 		try {
 
 			this.createBinding(channel, exchange.getRoutingKey());
     			
 		} catch (IOException e) {
 			
 			log.error("There was an error binding the registration to the exchange.", e);
 		}
     }
     
     /**
      * Bind the channel to the Routing and Exchange configuration
      * @param routingKey
      * @throws IOException
      */
     @SuppressWarnings("unchecked")
 	public void createBinding(Channel channel, String routingKey) throws IOException {
 		
 		log.debug("Creating binding for {}", routingKey);
 		
 		channel.exchangeDeclare(
         		this.exchange.getName(), 
         		this.exchange.getExchangeType(), 
         		this.exchange.getIsDurable(),
         		this.exchange.getIsAutoDelete(), 
         		this.exchange.getArguments());
         
         channel.queueDeclare(
         		this.exchange.getQueueName(), 
         		this.exchange.getIsDurable(), 
         		false, 
         		this.exchange.getIsAutoDelete(),            
         		this.exchange.getArguments());
         
         channel.queueBind(
         		this.exchange.getQueueName(), 
         		this.exchange.getName(), 
         		routingKey,
         		this.exchange.getArguments());
 	}
     
     /**
      * Add an onClose listener
      * @param callback listener
      */
     public void onClose(IListenerCloseCallback callback) {
         closeCallbacks.add(callback);
     }
 
     /**
      * Add an onEnvelopeReceived listener
      * @param callback listener
      */
     public void onEnvelopeReceived(IEnvelopeReceivedCallback callback) {
         envCallbacks.add(callback);
     }
     
     /**
      * Notify the onClose listeners that the RabbitListener is no longer
      * accepting messages.
      * @param registration Registration paired to this listener.
      */
     protected void raise_onCloseEvent(IRegistration registration) {
         for (IListenerCloseCallback callback : closeCallbacks) {
             try {
             
         		callback.onClose(registration);
             
             } catch (Exception ex) {
             	
                 log.error("Caught an unhandled exception raising the listener close event", ex);
             }
         }
     }
 
     /**
      * Notify the EnvelopeReceived listeners of the newly received envelope
      * @param dispatcher Envelope Dispatcher that will allow listeners to dispatch the envelope.
      */
     protected void raise_onEnvelopeReceivedEvent(RabbitEnvelopeDispatcher dispatcher) {
 
         log.debug("Now giving dispatcher to registered callbacks");
 
         for (IEnvelopeReceivedCallback callback : envCallbacks) {
             try {
                 callback.handleReceive(dispatcher);
             } catch (Exception ex) {
                 log.error("Caught an unhandled exception raising the envelope received event", ex);
             }
         }
     }
     
    
     /**
      * Determines whether the incoming Envelope meets the criteria to actually fire
      * the Envelope handler.
      * @param filter Envelope Predicate with the logic that determines whether we want the message
      * @param env The Envelope we will be testing
      * @return true if we want the envelope, false if we don't
      */
     protected boolean shouldRaiseEvent(IEnvelopeFilterPredicate filter, Envelope env) {
         // if there's no filter, the client wants it. Otherwise, see if they want it.
         boolean clientWantsIt = null == filter ? true : filter.filter(env);
 
         log.debug("Client's transport filter returns: " + clientWantsIt);
         return clientWantsIt;
     }
     
     /**
      * Begins a loop waiting for messages from the broker.  This loop is broken
      * if the flag 'shouldContinue' is set to false (from 'stopListening()' or
      * 'dispose()').
      */
     protected void startListening(){
     	
 		log.debug("Enter startListening");
         
         //Don't start listining until last invocation has stopped.  
         try {
 			isListeningSemaphore.acquire();
 		} catch (InterruptedException e) {
 			log.warn("Interupted before aquire. Exiting startListening.");
 			return;
 		}
         log.debug("Proceeding...");
 
         // Our flag to determine whether we are still looping
         shouldContinue = true;
 
         try {
         	Channel channel = connectionManager.createChannel();
         	channel.addShutdownListener(new ChannelShutdownHandler());
         	
 	    	try{
         		// first, declare the exchange and queue
 	            this.bind(channel, this.registration, this.exchange);
 	
 	            // next(), create a basic consumer
 	            QueueingConsumer consumer = new QueueingConsumer(channel);
 	
 	            // and tell it to start consuming messages, storing the consumer tag
 	            String consumerTag = channel.basicConsume(exchange.getQueueName(), false, consumer);
 	
                     // signal that we have begun listening on this thread
                     threadStartSignal.countDown();
 	            log.debug("Will now continuously listen for events using routing key: {}",
 	            		exchange.getRoutingKey());
 	            
 	            // Loop until told to stop.
 	            while (shouldContinue) {
 	            	
 	                try {
 	                		
 	            		// Get next result from the broker.
 	            		Delivery result = consumer.nextDelivery(DELIVERY_INTERVAL);
 	            		
 	            		// If the result is null, loop again.
 	            		if (result == null){ continue; }
 	                		
 	            		// Handle the next message delivery
 	                    handleNextDelivery(channel, result);
 	                
 	                } 
 	                // If the thread is interrupted (perhaps by Thread.stop())
 	                catch (InterruptedException interruptedException) {
 	                	
 	            		log.error("Listener interrupted.  Will stop listening.", interruptedException);
 	                    
 	            		// Thread was some how interrupted.  Stop loop.
 	                    shouldContinue = false;
 	                } 
 	                // If RabbitMQ sends the shutdown signal
 	                catch (ShutdownSignalException shutdownException) {
 	            			
 	            		log.warn("Listener ShutdownSignalException. Will stop listening.", shutdownException);
 	                	shouldContinue = false;
 	                } 
 	                // Otherwise, some other error occurred, probably in the dispatcher,
 	                // so we will make note of the error and continue looping.
 	                catch (Exception ex) {
 	                    
 	            		log.warn("Caught an exception, but will not stop listening for messages", ex);
 	                }
 	            }
 	            
 	            // We've broken through the loop
 	            log.debug("No longer listening for events");
 	
 	            try {
 	            	
 	        		// Attempt to unregister the consumer with the channel
	            	if(channel.isOpen()){
	            		channel.basicCancel(consumerTag);
	            	}
 	            
 	            } catch (IOException ex) {
 	            	
 	        		log.error("Exception occurred attempting to cancel consumption on Channel.", ex);
 	            }
 	            
 	        } finally {
 	        	channel.close();
 	        }
         } 
         // This is intermittently thrown by the client if there is a TCP error.
         // We catch the exception, determine if the intent was to close the client anyway,
         // and if it is not, attempt to remedy the problem.
         catch (AlreadyClosedException acex) {
         	
     		if (shouldContinue) {
     			
     			log.error("Channel or Connection was closed before we could use it.", acex);
     		}
         }
         // An exception has occurred attempting to consume from the channel
         catch (Exception ex) {
         	
             log.error("Caught an exception that will cause the listener to not listen for messages", ex);
         } finally {
         	isListeningSemaphore.release();
         }
         
         // Let everyone know we've stopped listening
         raise_onCloseEvent(registration);
         
         log.debug("Leave startListening");
     }
     
     /**
      * Handle the next incoming message from the AMQP Broker.
      * 
      * @param result AMQP Message
      * @throws Exception Thread Interruption, Connection Error, etc.
      */
     protected void handleNextDelivery(Channel channel, Delivery result) throws Exception {
     		
         log.debug("Got something.");
 
         EnvelopeHelper env = createEnvelopeFromDeliveryResult(result);
         
         log.debug("Incoming event headers: " + env.flatten());
 
         if (shouldRaiseEvent(registration.getFilterPredicate(), env.getEnvelope())) {
         	
             dispatchEnvelope(channel, env.getEnvelope(), result.getEnvelope().getDeliveryTag());
         }
     }
     
     /**
      * Transform an AMQP message (Delivery) into a Envelope (well, really an EnvelopeHelper).
      * 
      * @param result Message coming from the AMQP Broker.
      * @return EnvelopeHelper with an initialized envelope
      */
     protected EnvelopeHelper createEnvelopeFromDeliveryResult(QueueingConsumer.Delivery result){
     		
 		EnvelopeHelper env = new EnvelopeHelper(new Envelope());
     		
         env.setReceiptTime(DateTime.now(DateTimeZone.UTC));
         env.setPayload(result.getBody());
 
         // Iterate through the AMQP headers and convert them into CMF headers
         for (Entry<String, Object> prop : result.getProperties().getHeaders().entrySet()) {
 
             try {
             
         		String key = prop.getKey();
         		byte[] valueBytes = ((LongString) prop.getValue()).getBytes();
         		String value = new String(valueBytes, "UTF-8");
 
         		env.setHeader(key, value);
             
             } catch (Exception ex) {
             	
                 log.debug("couldn't get property", ex);
             }
         }
     		
         return env;
     }
     
     /**
      * Dispatch the Envelope to all concerned listeners.
      * 
      * @param envelope Incoming Envelope
      * @param deliveryTag AMQP Delivery Tag
      */
     protected void dispatchEnvelope(Channel channel, Envelope envelope, long deliveryTag){
     		
 		RabbitEnvelopeDispatcher dispatcher =
     		new RabbitEnvelopeDispatcher(registration, envelope, channel, deliveryTag);
         
         raise_onEnvelopeReceivedEvent(dispatcher);
     }
     
     /**
      * Stop listening for Messages
      */
     public void stopListening() {
     	
         log.debug("Enter stopListening");
         
         shouldContinue = false;
         isRunning = false;
         
         log.debug("Leave stopListening");
     }
     
     @Override
     public void run() {
     		
 		log.info("Starting to listen for messages on a seperate thread.");
     	
         startListening();
     }
     
     public void setDeliveryInterval(int interval){
     	
 		DELIVERY_INTERVAL = interval;
     }
     
     @Override
     public void dispose() {
     		
 		log.info("Dispose called.");
     		
         stopListening();
     }
     
     @Override
     protected void finalize() {
     		
         dispose();
     }
     
     private class ChannelShutdownHandler implements ShutdownListener {
 
 		@Override
 		public void shutdownCompleted(ShutdownSignalException cause) {
             log.debug("Enter shutdownCompleted handler, isInitiatedByApplication: " + cause.isInitiatedByApplication());
             
             shouldContinue = false;
             
             //If we the shutdown wasn't deliberate on our part, attempt to restart
             if (!cause.isInitiatedByApplication())
             {
             	log.debug("Attempting restart on new channel.");
                 //Move to a background thread so that rabbit can raise the connection closed event if that is the cause.
                 new Thread( new Runnable() {
 					
 					@Override
 					public void run() {
 	                   try {
 						Thread.sleep(100); //Give rabbit a chance to raise the connection closed event.
 					} catch (InterruptedException e) {
 						log.warn("Thread interupted while awaiting connection shutdown signal.  Aborting channel restart.");
 						return;
 					} 
 	                    
                     if(connectionClosed)
                         log.debug("Connection is clossed; aborting restart attempt.");
                     else
                         //Now restart only if the connection is not closed.  Otherwise we will restart in the OnConnectionReconnected event.
                         restart();
  					}
 				}).start();
             }
             
             log.debug("Leave shutdownCompleted handler.");		
         }
     }
     
     private class ConnectionClosedHandler implements IConnectionEventHandler {
 
 		@Override
 		public void onConnectionClosed(boolean willAttemptToReconnect) {
 		   log.debug("Enter Handle_OnConnectionClosed, WillAttemptReopen: " + willAttemptToReconnect);
 		   connectionClosed = true;
 		   shouldContinue = false;
 		   log.debug("Leave Handle_OnConnectionClosed");
 		}
 
 		@Override
 		public void onConnectionReconnected() {
             log.debug("Enter Handle_OnConnectionReconnected");
             connectionClosed = false;
             restart();
             log.debug("Leave Handle_OnConnectionReconnected");
 		}
     }
 }
