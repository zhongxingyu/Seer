 package com.amee.service.mq;
 
 import com.amee.service.mq.amqp.ConnectionConfig;
 import com.amee.service.mq.amqp.ExchangeConfig;
 import com.amee.service.mq.amqp.PublishConfig;
 import com.amee.service.mq.amqp.QueueConfig;
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.Connection;
 import com.rabbitmq.client.ConnectionFactory;
 import com.rabbitmq.client.ConnectionParameters;
 import com.rabbitmq.client.ShutdownSignalException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import javax.annotation.PreDestroy;
 import java.io.IOException;
 
 @Service
 public class MessageService {
 
     private final Log log = LogFactory.getLog(getClass());
 
     @Autowired
     private ConnectionConfig connectionConfig;
 
     @Autowired
     private ConnectionParameters connectionParameters;
 
     private Connection connection;
 
     @PreDestroy
     public synchronized void stop() {
         log.info("stop()");
         if (connection != null) {
             try {
                 connection.close();
             } catch (IOException e) {
                log.warn("closeChannel() Caught IOException whilst trying to close the connection. Message was: " + e.getMessage());
             }
         }
     }
 
     public void publish(
             ExchangeConfig exchangeConfig,
             PublishConfig publishConfig,
             String routingKey,
             Message message) {
         log.debug("publish() " + routingKey);
         try {
             // Try to get a channel.
             Channel channel = getChannel();
             if (channel != null) {
                 // Ensure exchange is declared.
                 channel.exchangeDeclare(
                         exchangeConfig.getName(),
                         exchangeConfig.getType(),
                         exchangeConfig.isPassive(),
                         exchangeConfig.isDurable(),
                         exchangeConfig.isAutoDelete(),
                         exchangeConfig.getArguments());
                 // Publish.
                 channel.basicPublish(
                         exchangeConfig.getName(),
                         routingKey,
                         publishConfig.isMandatory(),
                         publishConfig.isImmediate(),
                         publishConfig.getProperties(),
                         message.getMessage().getBytes());
                 // We're done with the channel.
                 channel.close();
             } else {
                 log.warn("publish() Unable to get a channel.");
             }
         } catch (IOException e) {
             log.warn("publish() Caught IOException: " + e.getMessage());
         } catch (ShutdownSignalException e) {
             log.warn("publish() Caught ShutdownSignalException: " + e.getMessage());
         } catch (MessagingException e) {
             log.warn("publish() Caught MessagingException: " + e.getMessage());
         }
     }
 
     public Channel consume(
             ExchangeConfig exchangeConfig,
             QueueConfig queueConfig,
             String bindingKey) throws IOException {
         log.debug("consume() " + bindingKey);
         // Try to get a channel.
         Channel channel = getChannel();
         if (channel != null) {
             // Ensure exchange is declared.
             channel.exchangeDeclare(
                     exchangeConfig.getName(),
                     exchangeConfig.getType(),
                     exchangeConfig.isPassive(),
                     exchangeConfig.isDurable(),
                     exchangeConfig.isAutoDelete(),
                     exchangeConfig.getArguments());
             // Ensure queue is declared.
             channel.queueDeclare(
                     queueConfig.getName(),
                     queueConfig.isPassive(),
                     queueConfig.isDurable(),
                     queueConfig.isExclusive(),
                     queueConfig.isAutoDelete(),
                     queueConfig.getArguments());
             // Ensure queue is bound.
             channel.queueBind(
                     queueConfig.getName(),
                     exchangeConfig.getName(),
                     bindingKey,
                     null);
         }
         return channel;
     }
 
     public Channel getChannel() throws IOException {
         try {
             return getConnection().createChannel();
         } catch (ShutdownSignalException e) {
             log.warn("getChannel() Caught ShutdownSignalException. We'll try to ignore once. Message was: " + e.getMessage());
             connection = null;
             return getConnection().createChannel();
         }
     }
 
     public Connection getConnection() throws IOException {
         if (connection == null) {
             synchronized (this) {
                 if (connection == null) {
                     connection = getConnectionFactory().newConnection(connectionConfig.getAddresses());
                 }
             }
         }
         return connection;
     }
 
     public ConnectionFactory getConnectionFactory() {
         return new ConnectionFactory(connectionParameters);
     }
 }
