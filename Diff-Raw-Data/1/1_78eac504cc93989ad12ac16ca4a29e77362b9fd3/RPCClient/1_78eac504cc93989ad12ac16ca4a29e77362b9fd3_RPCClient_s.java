 package org.trianacode.TrianaCloud.Utils;
 
 import com.rabbitmq.client.AMQP.BasicProperties;
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.Connection;
 import com.rabbitmq.client.ConnectionFactory;
 import com.rabbitmq.client.QueueingConsumer;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.log4j.Logger;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: keyz
  * Date: 08/05/12
  * Time: 19:46
  * To change this template use File | Settings | File Templates.
  */
 public class RPCClient {
     private Logger logger = Logger.getLogger(this.getClass().toString());
 
     private Connection connection;
     private Channel channel;
     private String requestQueueName = "trianaloud_rpc_queue";
     private String replyQueueName;
     private QueueingConsumer consumer;
 
     public static final String GET_TASK = "gettask";
     public static final String RETURN_TASK = "rettask";
 
     public static final String NOTASK = "notask";
 
     public RPCClient() {
         try {
             ConnectionFactory factory = new ConnectionFactory();
             factory.setHost("zotac.toaster.dbyz.co.uk");
             factory.setVirtualHost("trianacloud");
             factory.setUsername("trianacloud");
             factory.setPassword("trianacloud");
             connection = factory.newConnection();
             channel = connection.createChannel();
 
             replyQueueName = channel.queueDeclare().getQueue();
             consumer = new QueueingConsumer(channel);
             channel.basicConsume(replyQueueName, true, consumer);
         } catch (Exception e) {
             logger.fatal("Error connecting to Rabbit while initialising RPCClient", e);
         }
     }
 
     private byte[] call(String message) throws Exception {
         return call(message.getBytes());
     }
 
     private byte[] call(byte[] message) throws Exception {
         byte[] response = null;
         String corrId = java.util.UUID.randomUUID().toString();
 
         BasicProperties props = new BasicProperties
                 .Builder()
                 .correlationId(corrId)
                 .replyTo(replyQueueName)
                 .build();
 
         channel.basicPublish("", requestQueueName, props, message);
 
         while (true) {
             QueueingConsumer.Delivery delivery = consumer.nextDelivery();
             if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                 response = delivery.getBody();
                 break;
             }
         }
 
         return response;
     }
 
     public Task getTask(List<String> plugins) {
         try {
             StringBuilder sb = new StringBuilder();
             sb.append(GET_TASK + "\n");
 
             for (String p : plugins) {
                 sb.append(p + "\n");
             }
 
             Task t = TaskOps.decodeTask(call(sb.toString()));
             return t;
         } catch (Exception e) {
             logger.error("Error getting new task", e);
             return null;
         }
     }
 
     public int sendCompleteTask(byte[] message) {
         try {
             byte[] method = RETURN_TASK.getBytes();
 
             byte[] ret = call(ArrayUtils.addAll(method, message));
 
             return new Integer(ret[0]);
         } catch (Exception e) {
             logger.error("Error sending complete task");
             return -1;
         }
     }
 
 
     public void close() throws Exception {
         connection.close();
     }
 }
