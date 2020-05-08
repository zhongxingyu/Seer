 package pegasus.eventbus.rabbitmq;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pegasus.eventbus.amqp.AmqpMessageBus;
 import pegasus.eventbus.amqp.RoutingInfo;
 
 import com.rabbitmq.client.AMQP.BasicProperties;
 import com.rabbitmq.client.Channel;
 
 import pegasus.eventbus.client.Envelope;
 import pegasus.eventbus.client.EnvelopeHandler;
 import pegasus.eventbus.rabbitmq.RabbitConnection.UnexpectedCloseListener;
 
 /**
  * RabbitMQ implementation of our AmqpMessageBus interface.
  * 
  * @author Ken Baltrinic (Berico Technologies)
  */
 public class RabbitMessageBus implements AmqpMessageBus, UnexpectedCloseListener {
 
     private static final Logger                    LOG                                = LoggerFactory.getLogger(RabbitMessageBus.class);
 
     final static String                            TOPIC_HEADER_KEY                   = "pegasus.eventbus.event.topic";
     final static String                            PUB_TIMESTAMP_HEADER_KEY           = "pegasus.eventbus.event.publication_timestamp";
 
     private RabbitConnection                       connection;
     private Channel                                commandChannel;
     private Map<String, Channel>                   consumerChannels                   = new HashMap<String, Channel>();
     private Set<UnexpectedConnectionCloseListener> unexpectedConnectionCloseListeners = new HashSet<UnexpectedConnectionCloseListener>();
 
     /**
      * Initialize Rabbit with the given connection parameters,
      * 
      * @param connectionParameters
      *            Connection Parameters
      */
     public RabbitMessageBus(RabbitConnection connection) {
 
         LOG.info("Instantiating the RabbitMQ Message Bus.");
 
         this.connection = connection;
         connection.attachUnexpectedCloseListener(this);
     }
 
     @Override
     public void start() {
 
         LOG.info("Starting the RabbitMQ Message Bus.");
 
         openConnectionToBroker();
 
     }
 
     private void openConnectionToBroker() {
         try {
 
             LOG.trace("Opening the connection.");
 
             if (!connection.isOpen()) {
                 connection.open();
             }
         } catch (IOException e) {
 
             LOG.error("Could not connect to RabbitMQ", e);
 
             throw new RuntimeException("Failed to open connection to RabbitMq: " + e.getMessage() + "See inner exception for details", e);
         }
 
         try {
 
             LOG.debug("Creating channel to AMQP broker.");
 
             // TODO: Need to replace this with a channel per thread model.
             this.commandChannel = connection.createChannel();
 
         } catch (IOException e) {
 
             LOG.error("Could not open an AMQP channel.", e);
 
             throw new RuntimeException("Failed to open AMQP channel: " + e.getMessage() + "See inner exception for details", e);
         }
     }
 
     /**
      * Close the active AMQP connection.
      */
     public void close() {
 
         LOG.info("Closing connection to the AMQP broker.");
 
         try {
 
             if (commandChannel.isOpen()) {
 
                 LOG.trace("Closing command channel.");
 
                 commandChannel.close();
             }
 
             if (connection.isOpen()) {
 
                 LOG.trace("Closing connection.");
 
                 connection.close();
             }
 
         } catch (IOException e) {
 
             LOG.error("Error occurred when trying to close connection to AMQP broker.", e);
         }
     }
 
     @Override
     public void attachUnexpectedConnectionCloseListener(UnexpectedConnectionCloseListener listener) {
         unexpectedConnectionCloseListeners.add(listener);
 
     }
 
     @Override
     public void detachUnexpectedConnectionCloseListener(UnexpectedConnectionCloseListener listener) {
         unexpectedConnectionCloseListeners.remove(listener);
     }
 
     private void notifyUnexpectedConnectionCloseListeners(boolean successfullyReopened) {
         for (UnexpectedConnectionCloseListener listener : unexpectedConnectionCloseListeners) {
             listener.onUnexpectedConnectionClose(successfullyReopened);
         }
     }
 
     /**
      * Implementation for connection CloseListener interface.
      */
     @Override
     public void onUnexpectedClose(boolean successfullyReopened) {
 
        LOG.debug("Unexpected connection close notice received.  successfullyReopened=" + successfullyReopened + " Notifying listeners.");
 
         notifyUnexpectedConnectionCloseListeners(successfullyReopened);
     }
 
     /**
      * Create a new AMQP exchange
      * 
      * @param exchange
      *            The exchange information
      */
     @Override
     public void createExchange(RoutingInfo.Exchange exchange) {
 
         LOG.debug("Creating the [{}] exchange.", exchange.getName());
 
         try {
             commandChannel.exchangeDeclare(exchange.getName(), exchange.getType().toString().toLowerCase(), exchange.isDurable());
         } catch (IOException e) {
 
             LOG.error("Could not create the [{}] exchange.", exchange.getName(), e);
 
             throw new RuntimeException("Failed to create exchange: " + e.getMessage() + "See inner exception for details", e);
         }
     }
 
     /**
      * Create a new AMQP queue
      * 
      * @param name
      *            Name of the queue
      * @param bindings
      *            The information necessary to bind the queue to exchanges
      * @param durable
      *            Is the queue durable?
      */
     @Override
     public void createQueue(String name, RoutingInfo[] bindings, boolean durable) {
 
         LOG.debug("Declaring queue [{}]; durable? = {}", name, durable);
 
         try {
 
         	Map<String, Object> params = new HashMap<String,Object>();
         	if(!durable){
         		//TODO: make this expiration configurable
         		//We are using expiration vs auto-delete as this will allow for connection drops over bad comms and not loose messages due to a deleted queue.
         		params.put("x-expires", 1000*60*30); //30-min timout in mills
         	}
             commandChannel.queueDeclare(name, durable, false, false, params);
 
         } catch (IOException e) {
 
             LOG.error("Could not declare queue {}", name, e);
 
             throw new RuntimeException("Failed to create queue: " + e.getMessage() + "See inner exception for details", e);
         }
         for (RoutingInfo binding : bindings) {
 
             LOG.debug("Binding queue [{}] to exchange [{}] with the routing key [{}]", new Object[] { name, binding.getExchange().getName(), binding.getRoutingKey() });
 
             try {
 
                 commandChannel.queueBind(name, binding.getExchange().getName(), binding.getRoutingKey());
 
             } catch (IOException e) {
 
                 LOG.error("Could not create binding for queue [{}] on exchange [{}] with expression [{}]", new Object[] { name, binding.getExchange().getName(), binding.getRoutingKey() }, e);
 
                 throw new RuntimeException("Failed to create binding: " + binding.getRoutingKey() + " on queue: " + name + " See inner exception for details", e);
             }
         }
     }
 
     /**
      * Delete a Queue
      * 
      * @param queueName
      *            Name of the Queue to remove
      */
     @Override
     public void deleteQueue(String queueName) {
 
         LOG.debug("Deleting queue [{}]", queueName);
 
         try {
 
             commandChannel.queueDelete(queueName);
 
         } catch (IOException e) {
 
             LOG.error("Could not delete queue [{}].", queueName, e);
 
             throw new RuntimeException("Failed to delete queue: " + e.getMessage() + "See inner exception for details", e);
         }
     }
 
     /**
      * Publish a message using the provided route.
      * 
      * @param route
      *            Information used to route the message
      * @param message
      *            Message to publish
      */
     @Override
     public void publish(RoutingInfo route, Envelope message) {
 
         LOG.debug("Publishing message of type [{}] on exchange [{}]", message.getEventType(), route.getExchange().getName());
 
         try {
 
             LOG.trace("Creating AMQP headers for the message being published.");
 
             Map<String, Object> headersOut = new HashMap<String, Object>();
 
             if (message.getTopic() != null) {
                 headersOut.put(TOPIC_HEADER_KEY, message.getTopic());
             }
 
             if (message.getTimestamp() != null) {
                 //AMQP Timestamp values only have 1 second resolution so we are using a Long custom header vice props.setTimestamp() to transmit the time stamp for our use.
             	//However we still set the AMQP timestamp property for interoperability reasons.
                 //See AMPQ 0-9-1 specification,  section 4.2.5.4 "Timestamps"
                headersOut.put(PUB_TIMESTAMP_HEADER_KEY, message.getTimestamp().getTime());
             }
 
             final Map<String, String> headersIn = message.getHeaders();
 
             for (String key : headersIn.keySet()) {
                 headersOut.put(key, headersIn.get(key));
             }
 
             LOG.trace("Building AMQP property set for the message being published.");
 
             BasicProperties props = new BasicProperties.Builder()
             	.messageId(message.getId() == null ? null : message.getId().toString())
                 .correlationId(message.getCorrelationId() == null ? null : message.getCorrelationId().toString())
                 .type(message.getEventType())
                 .replyTo(message.getReplyTo()).headers(headersOut)
                 .timestamp(message.getTimestamp())
                 .build();
 
             LOG.trace("Publishing the message on the bus.");
 
             commandChannel.basicPublish(route.getExchange().getName(), route.getRoutingKey(), props, message.getBody());
 
         } catch (IOException e) {
 
             LOG.error("Could not publish message on bus.", e);
 
             throw new RuntimeException("Failed to publish message: " + e.getMessage() + "See inner exception for details", e);
         }
 
     }
 
     static Envelope createEnvelope(final BasicProperties props, byte[] body) {
         LOG.trace("Creating the Envelope.");
 
         Envelope envelope = new Envelope();
 
         LOG.trace("Placing the headers from the message into the Envelope.");
 
         Map<String, Object> propHeaders = props.getHeaders();
         Map<String, String> headers = envelope.getHeaders();
         Long timestampMills = null;
         
 		if (propHeaders != null) {
             for (String key : propHeaders.keySet()) {
                 headers.put(key, propHeaders.get(key).toString());
             }
             
             timestampMills = (Long)propHeaders.get(PUB_TIMESTAMP_HEADER_KEY);
         }
 
         LOG.trace("Mapping AMQP specific properties to Envelope properties.");
 
         envelope.setBody(body);
         envelope.setId(props.getMessageId() == null ? null : UUID.fromString(props.getMessageId()));
         envelope.setCorrelationId(props.getCorrelationId() == null ? null : UUID.fromString(props.getCorrelationId()));
         envelope.setEventType(props.getType());
         envelope.setReplyTo(props.getReplyTo());
         envelope.setTimestamp(timestampMills == null ? null : new Date(timestampMills));
         envelope.setTopic(headers.get(TOPIC_HEADER_KEY));
 
         // We don't want our internally used headers to be a Header property of the envelope.
         headers.remove(TOPIC_HEADER_KEY);
         headers.remove(PUB_TIMESTAMP_HEADER_KEY);
         return envelope;
     }
 
     @Override
     public String beginConsumingMessages(final String queueName, final EnvelopeHandler consumer) {
 
         LOG.trace("Begin consuming messages for queue [{}] with an EnvelopeHandler of type [{}].", queueName, consumer.getClass().getCanonicalName());
 
         String consumerTag = queueName + ":" + UUID.randomUUID().toString();
 
         LOG.trace("ConsumerTag set to [{}].", consumerTag);
 
         final Channel consumerChannel;
         try {
 
             LOG.trace("Opening dedicated channel for ConsumerTag [{}].", consumerTag);
 
             consumerChannel = connection.createChannel();
 
             LOG.trace("Successfully opened dedicated channel for ConsumerTag [{}].", consumerTag);
 
             consumerChannels.put(consumerTag, consumerChannel);
 
         } catch (IOException e) {
 
             LOG.error("Could not create channel to consume messages on queue: [{}]", queueName, e);
 
             throw new RuntimeException("Could not create channel to consume messages on queue: " + queueName, e);
         }
 
         try {
 
             LOG.trace("Beginning basicConsume for ConsumerTag [{}].", consumerTag);
 
             consumerChannel.basicConsume(queueName, false, consumerTag, new EnvelopeHandlerBasedConsumer(consumerChannel, consumerTag, consumer));
 
             LOG.trace("Begun basicConsume for ConsumerTag [{}].", consumerTag);
 
         } catch (IOException e) {
 
             LOG.error("Failed to initiate basicConsume ConsumerTag [{}].", consumerTag, e);
 
             throw new RuntimeException("Failed to initiate basicConsume ConsumerTag: " + consumerTag, e);
         }
 
         return consumerTag;
     }
 
     @Override
     public void stopConsumingMessages(String consumerTag) {
         synchronized (consumerChannels) {
             Channel channel = consumerChannels.get(consumerTag);
             if (channel == null)
                 return;
             consumerChannels.remove(consumerTag);
             try {
                 channel.basicCancel(consumerTag);
             } catch (IOException e) {
                 LOG.error("Failed to cancel basicConsume for ConsumerTag: [{}]", consumerTag, e);
 
                 throw new RuntimeException("Failed to cancel basicConsume for ConsumerTag: " + consumerTag, e);
             }
         }
 
     }
 }
