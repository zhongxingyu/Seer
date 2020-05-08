 package amp.rabbit;
 
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import cmf.bus.Envelope;
 import cmf.bus.IDisposable;
 import cmf.bus.IEnvelopeFilterPredicate;
 import cmf.bus.IRegistration;
 import com.rabbitmq.client.AlreadyClosedException;
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.LongString;
 import com.rabbitmq.client.QueueingConsumer;
 import com.rabbitmq.client.QueueingConsumer.Delivery;
 import com.rabbitmq.client.ShutdownSignalException;
 import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import amp.bus.EnvelopeHelper;
 import amp.bus.IEnvelopeReceivedCallback;
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
     protected List<IOnConnectionErrorCallback> connectionErrorCallbacks
     		= new ArrayList<IOnConnectionErrorCallback>();
     
     protected Channel channel;
     protected Exchange exchange;
     protected Logger log;
     protected IRegistration registration;
     protected boolean shouldContinue;
     protected Thread threadImRunningOn = null;
 
 
 
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
      * Initialize the Listener with the Registration and Exchange
      * @param registration
      * @param exchange
      */
     public RabbitListener(IRegistration registration, Exchange exchange) {
 
         this.registration = registration;
         this.exchange = exchange;
 
         log = LoggerFactory.getLogger(this.getClass());
     }
     
     /**
      * Initialize the Listener with the Registration, Exchange, and Channel
      * @param registration
      * @param exchange
      * @param channel
      */
     public RabbitListener(IRegistration registration, Exchange exchange, Channel channel) {
 
         this.registration = registration;
         this.exchange = exchange;
         this.channel = channel;
 
         log = LoggerFactory.getLogger(this.getClass());
     }
 
 
 
     /**
      * Start listening on the supplied channel for messages.
      * @param channel AMQP Channel
      */
     public void start(Channel channel) {
     	
 		if (shouldContinue) {
 			
 			shouldContinue = false;
 			
 			try {
 				
 				Thread.sleep(150);
 				
 			} catch (InterruptedException e) {
 					
 				log.error("Thread was interrupted while it was trying to sleep.", e);
 			}
 		}
 		
 		this.channel = channel;
 		
 		this.start();
     }
     
     /**
      * Start listening on a new thread.  This won't work unless you
      * have set the Channel on the listener.
      */
     public void start() {
     		
 		if (this.channel == null) {
 			
 			log.error("Channel is null; cannot start.");
 			
 			return;
 			
 		} else {
 			
 			threadImRunningOn = new Thread(this);
 			
 			threadImRunningOn.start();
 		}
     }
     
     public void bind(IRegistration registration, Exchange exchange) {
     	
 		try {
 
 			this.createBinding(exchange.getRoutingKey());
     			
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
 	public void createBinding(String routingKey) throws IOException {
 		
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
      * Add an onConnection Error listener
      * @param callback listener
      */
     public void onConnectionError(IOnConnectionErrorCallback callback) {
     		connectionErrorCallbacks.add(callback);
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
      * Notify the OnConnectionError listeners of the connection error. 
      */
     protected void raise_onConnectionErrorEvent(){
     	
     		for (IOnConnectionErrorCallback callback : connectionErrorCallbacks){
     			try {
     				callback.onConnectionError(this);
     			} catch (Exception ex) {
     				log.error("An exception occurred while notifing listener of connection failure.", ex);
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
         
 		// Our flag to determine whether we are still looping
         shouldContinue = true;
 
         try {
     		// first, declare the exchange and queue
             this.bind(this.registration, this.exchange);
 
             // next(), create a basic consumer
             QueueingConsumer consumer = new QueueingConsumer(channel);
 
             // and tell it to start consuming messages, storing the consumer tag
             String consumerTag = channel.basicConsume(exchange.getQueueName(), false, consumer);
 
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
                     handleNextDelivery(result);
                 
                 } 
                 // If the thread is interrupted (perhaps by Thread.stop())
                 catch (InterruptedException interruptedException) {
                 	
             		log.error("Listener interrupted...", interruptedException);
                     
             		// Thread was some how interrupted.  Stop loop.
                     this.stopListening();
                 } 
                 // If RabbitMQ sends the shutdown signal
                 catch (ShutdownSignalException shutdownException) {
                 	
             		// Delegate the shutdown logic to handleShutdownSignalException method
             		boolean wasIntentional = handleShutdownSignalException(shutdownException);
                 
             		if (!wasIntentional) {
             			
             			// Stop.  Nothing else we can do, but there is a chance we can
                 		// recover.  Don't fire the onClose handlers, we're going to attempt to
                 		// resolve this issue.
             			return;
             			
             		} else {
             			
             			this.stopListening();
             		}
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
                 channel.basicCancel(consumerTag);
             
             } catch (IOException ex) {
             	
         		log.error("Exception occurred attempting to cancel consumption on Channel.", ex);
             }
         } 
         // This is intermittently thrown by the client if there is a TCP error.
         // We catch the exception, determine if the intent was to close the client anyway,
         // and if it is not, attempt to remedy the problem.
         catch (AlreadyClosedException acex) {
         	
     		if (shouldContinue) {
     			
     			log.error("Channel or Connection was closed before we could use it.", acex);
     			
     			raise_onConnectionErrorEvent();
     		}
         }
         // An exception has occurred attempting to consume from the channel
         catch (Exception ex) {
         	
             log.error("Caught an exception that will cause the listener to not listen for messages", ex);
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
     protected void handleNextDelivery(Delivery result) throws Exception {
     		
         log.debug("Got something.");
 
         EnvelopeHelper env = createEnvelopeFromDeliveryResult(result);
         
         log.debug("Incoming event headers: " + env.flatten());
 
         if (shouldRaiseEvent(registration.getFilterPredicate(), env.getEnvelope())) {
         	
             dispatchEnvelope(env.getEnvelope(), result.getEnvelope().getDeliveryTag());
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
     protected void dispatchEnvelope(Envelope envelope, long deliveryTag){
     		
 		RabbitEnvelopeDispatcher dispatcher =
     		new RabbitEnvelopeDispatcher(registration, envelope, channel, deliveryTag);
         
         raise_onEnvelopeReceivedEvent(dispatcher);
     }
     
     /**
      * If a ShutdownSignalException is thrown, we need to figure out why.
      * The RabbitMQ Client will intentionally throw this exception if the
      * application requests the connection closed.  If the application is closed
      * intentionally, raise the onClose event.
      *
      * @return True if the shutdown was intentional
      * @param ex
      */
     protected boolean handleShutdownSignalException(ShutdownSignalException ex){
     	
 		log.info("Reason for Shutdown: {}", ex.getReason());
 	
 		shouldContinue = false;
     	
 		if(ex.isInitiatedByApplication()) {
 			
 			log.info("Shutdown requested by application, stopping thread.");
 			
 			return true;
 		}
 		else {
 			
 			this.raise_onConnectionErrorEvent();
 			
 			return false;
 		}
     }
     
     /**
      * Stop listening for Messages
      */
     public void stopListening() {
     	
         log.debug("Enter stopListening");
         
         shouldContinue = false;
         
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
 }
