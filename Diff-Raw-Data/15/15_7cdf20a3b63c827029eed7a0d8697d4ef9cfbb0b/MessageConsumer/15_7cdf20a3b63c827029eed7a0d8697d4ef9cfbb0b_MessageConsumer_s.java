 package com.amee.service.mq;
 
 import com.amee.service.mq.amqp.ConsumeConfig;
 import com.amee.service.mq.amqp.ExchangeConfig;
 import com.amee.service.mq.amqp.MessagingConfig;
 import com.amee.service.mq.amqp.QueueConfig;
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.QueueingConsumer;
 import com.rabbitmq.client.ShutdownSignalException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import java.io.IOException;
 
 public abstract class MessageConsumer implements Runnable, ApplicationContextAware {
 
     private final Log log = LogFactory.getLog(getClass());
 
     @Autowired
     private MessageService messageService;
 
     @Autowired
     private MessagingConfig messagingConfig;
 
     private Channel channel;
     private QueueingConsumer consumer;
     private Thread thread;
     private ApplicationContext applicationContext;
     private boolean stopping = false;
 
     @PostConstruct
     public synchronized void start() {
         log.info("start()");
         thread = new Thread(this);
         thread.start();
     }
 
     @PreDestroy
     public synchronized void stop() {
         log.info("stop()");
         stopping = true;
         if (thread != null) {
             thread.interrupt();
             thread = null;
         }
     }
 
     public void run() {
         log.info("run()");
         while (!Thread.currentThread().isInterrupted()) {
             try {
                 log.debug("run() waiting");
                 // Wait before first-run and subsequent retries.
                 Thread.sleep(messagingConfig.getRunSleep());
                 log.debug("run() starting");
                 // Start the Consumer and handle the deliveries.
                 startConsuming();
                 handleDeliveries();
                 // We got here if there is no channel or this was stopped.
                 log.debug("run() no channel or stopped");
             } catch (IOException e) {
                 log.warn("run() Caught IOException. We'll try restarting the consumer. Message was: " + e.getMessage());
             } catch (ShutdownSignalException e) {
                 log.warn("run() Caught ShutdownSignalException. We'll try restarting the consumer. Message was: " + e.getMessage());
             } catch (InterruptedException e) {
                 log.info("run() Interrupted.");
                 closeAndClear();
                 return;
             }
         }
     }
 
     private void startConsuming() throws IOException {
         log.debug("startConsuming()");
         // Ensure the channel is closed & consumer is cleared before starting.
         closeAndClear();
         // Try to get a channel.
         channel = messageService.consume(
                 getExchangeConfig(),
                 getQueueConfig(),
                 getBindingKey());
         // If we have a channel we're safe to configure the consumer.
         if (channel != null) {
             consumer = new QueueingConsumer(channel);
             channel.basicConsume(
                     getQueueConfig().getName(),
                     getConsumeConfig().isNoAck(),
                     getConsumeConfig().getConsumerTag(),
                     getConsumeConfig().isNoLocal(),
                     getConsumeConfig().isExclusive(),
                     consumer);
         }
     }
 
     private void handleDeliveries() throws IOException, InterruptedException {
         log.debug("handleDeliveries()");
         if (consumer != null) {
             while (!stopping) {
                 QueueingConsumer.Delivery delivery = consumer.nextDelivery(messagingConfig.getDeliveryTimeout());
                 if (delivery != null) {
                     log.debug("handleDeliveries() got delivery");
                     try {
                         handleDelivery(delivery);
                     } catch (MessagingException e) {
                         log.warn("handleDeliveries() Caught MessagingException: " + e.getMessage());
                     }
                     channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                 }
             }
             closeAndClear();
         }
     }
 
     private synchronized void closeAndClear() {
         log.debug("closeAndClear()");
         consumer = null;
         if (channel != null) {
             try {
                 channel.close();
             } catch (IOException e) {
                log.warn("closeChannel() Caught IOException whilst trying to close the channel. Message was: " + e.getMessage());
             }
             channel = null;
         }
     }
 
     public abstract void handleDelivery(QueueingConsumer.Delivery delivery);
 
     public abstract ExchangeConfig getExchangeConfig();
 
     public abstract QueueConfig getQueueConfig();
 
     public abstract ConsumeConfig getConsumeConfig();
 
     public abstract String getBindingKey();
 
     public ApplicationContext getApplicationContext() {
         return applicationContext;
     }
 
     public void setApplicationContext(ApplicationContext applicationContext) {
         this.applicationContext = applicationContext;
     }
 }
