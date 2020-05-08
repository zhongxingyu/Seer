 package org.jclarity.training.chapter11;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.TextMessage;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
 import org.springframework.stereotype.Component;
 
 @Component
 public class JmsMessageListener implements MessageListener { 
 
     private static final Logger LOGGER = LoggerFactory.getLogger(JmsMessageListener.class);
 
     @Autowired
     private AtomicInteger counter = null;
 
     /**
      * Example of an implementation of <code>MessageListener</code>.
      */
     @Override
     public void onMessage(Message message) {
         try {   
             int messageCount = message.getIntProperty(JmsMessageProducer.MESSAGE_COUNT);
             
             if (message instanceof TextMessage) {
                 TextMessage tm = (TextMessage)message;
                 String msg = tm.getText();
                 
                 LOGGER.info("Processed message '{}'. value={}", msg, messageCount);
                 
                 counter.incrementAndGet();
             }
         } catch (JMSException e) {
             LOGGER.error(e.getMessage(), e);
         }
     }
 
 }
