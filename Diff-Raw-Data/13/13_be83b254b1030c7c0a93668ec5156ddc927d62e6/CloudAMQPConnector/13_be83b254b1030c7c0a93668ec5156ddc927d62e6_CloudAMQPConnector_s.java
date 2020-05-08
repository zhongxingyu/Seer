 package cloudamqp.connector;
 
 import org.mule.api.annotations.*;
 import org.mule.api.annotations.param.*;
 import org.mule.api.ConnectionException;
 import org.mule.api.ConnectionExceptionCode;
 import org.mule.api.callback.SourceCallback;
 
 import com.rabbitmq.client.*;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * CloudAMQP Cloud Connector
  *
  * @author 84codes AB
  */
 @Connector(name="cloudamqp", schemaVersion="1.0-SNAPSHOT", friendlyName = "CloudAMQP")
 public class CloudAMQPConnector
 {
   private static ConnectionFactory factory = new ConnectionFactory();
   private Connection conn;
   private Channel pubChannel;
 
   /**
    * Connect
    *
    * @param host A host name
    * @param vhost A vhost
    * @param username A username
    * @param password A password
    * @throws ConnectionException
    */
   @Connect
  public void connect(@ConnectionKey String host, String vhost, String username, String password) throws ConnectionException {
     try {
       factory.setHost(host);
       factory.setVirtualHost(vhost);
       factory.setUsername(username);
       factory.setPassword(password);
       conn = factory.newConnection();
       pubChannel = conn.createChannel();
     } catch (Exception e) {
      throw new ConnectionException(ConnectionExceptionCode.UNKNOWN, null, null, e);
     }
   }
 
   /**
    * Disconnect
    */
   @Disconnect
   public void disconnect() {
     try {
       pubChannel.close();
       conn.close();
     } catch (Exception e) {
     }
   }
 
   /**
    * Are we connected
    */
   @ValidateConnection
   public boolean isConnected() {
     return conn != null && conn.isOpen();
   }
 
   /**
    * Are we connected
    */
   @ConnectionIdentifier
   public String connectionId() {
     return "001";
   }
 
   /**
    * Publish processor
    *
    * {@sample.xml ../../../doc/CloudAMQP-connector.xml.sample cloudamqp:publish-message}
    *
    * @param queue Name of the queue to publish to
    * @param message Message to be published to the queue
    * @throws IOException If message cannot be deliviered
    * @return The same message
    */
   @Processor
   public String publishMessage(String queue, @Optional @Default("#[payload]") String message) throws java.io.IOException
   {
     byte[] bytes = message.getBytes();
     pubChannel.basicPublish("", queue, MessageProperties.PERSISTENT_TEXT_PLAIN, bytes);
     return message;
   }
 
   /**
    * Attempts to receive a message from the queue
    *
    * {@sample.xml ../../../doc/CloudAMQP-connector.xml.sample cloudamqp:receive-messages}
    *
    * @param queue Name of the queue to subscribe to
    * @param callback Callback to call when a new message is available.
    * @throws IOException If message cannot be received
    */
   @Source
   public void receiveMessages(String queue, final SourceCallback callback) throws java.io.IOException {
     Channel ch = conn.createChannel();
 
     boolean durable = true;
     boolean exclusive = false;
     boolean autoDelete = false;
     ch.queueDeclare(queue, durable, exclusive, autoDelete, null);
 
     boolean autoAck = false;
     ch.basicConsume(queue, autoAck, "", new DefaultConsumer(ch) {
       @Override
       public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws java.io.IOException
     {
       Map<String, Object> msgArgs = new HashMap<String, Object>();
       msgArgs.put("routingKey", envelope.getRoutingKey());
 
       long deliveryTag = envelope.getDeliveryTag();
       String message = new String(body);
       try {
         callback.process(message, msgArgs);
         this.getChannel().basicAck(deliveryTag, false);
       } catch (Exception e) {
         boolean requeue = false;
         this.getChannel().basicReject(deliveryTag, requeue);
         //throw e;
       }
     }
     });
   }
 
   /**
    * Publish topic processor
    *
    * {@sample.xml ../../../doc/CloudAMQP-connector.xml.sample cloudamqp:publish-topic-message}
    *
    * @param topic Name of the topic to publish
    * @param message Message to be published to the queue
    * @throws IOException If message cannot be deliviered
    * @return The same message
    */
   @Processor
   public String publishTopicMessage(String topic, @Optional @Default("#[payload]") String message) throws java.io.IOException
   {
     byte[] bytes = message.getBytes();
     pubChannel.basicPublish("amq.topic", topic, MessageProperties.PERSISTENT_TEXT_PLAIN, bytes);
     return message;
   }
 
   /**
    * Attempts to receive a message from the queue
    *
    * {@sample.xml ../../../doc/CloudAMQP-connector.xml.sample cloudamqp:receive-topic-messages}
    *
    * @param topic Name of the topic to subscribe to
    * @param queue Optional persistent queue name
    * @param callback Callback to call when a new message is available.
    * @throws IOException If message cannot be received
    */
   @Source
   public void receiveTopicMessages(String topic, @Optional @Default("") String queue, final SourceCallback callback) throws IOException {
     Channel ch = conn.createChannel();
     if (queue == null || queue == "") {
       queue = ch.queueDeclare().getQueue();
     } else {
       boolean durable = true;
       boolean exclusive = false;
       boolean autoDelete = false;
       ch.queueDeclare(queue, durable, exclusive, autoDelete, null);
     }
     ch.queueBind(queue, "amq.topic", topic);
 
     boolean autoAck = false;
     ch.basicConsume(queue, autoAck, "amq.topic", new DefaultConsumer(ch) {
       @Override
       public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException
     {
       Map<String, Object> msgArgs = new HashMap<String, Object>();
       msgArgs.put("routingKey", envelope.getRoutingKey());
 
       long deliveryTag = envelope.getDeliveryTag();
       String message = new String(body);
       try {
         callback.process(message, msgArgs);
         this.getChannel().basicAck(deliveryTag, false);
       } catch (Exception e) {
         boolean requeue = false;
         this.getChannel().basicReject(deliveryTag, requeue);
         //throw e;
       }
     }
     });
   }
 }
