 package eu.alertproject.iccs.mlsensor.connector.producer;
 
 import eu.alertproject.iccs.events.api.Topics;
 import eu.alertproject.iccs.mlsensor.mail.api.MailService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jms.JmsException;
 import org.springframework.jms.core.JmsTemplate;
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 
 import javax.mail.MessagingException;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * User: fotis
  * Date: 07/12/11
  * Time: 21:03
  */
 @Service("realTimeMessagePublisher")
 public class ActiveMQRealTimeMessagePublisher implements MLRealTimeMessagePublisher{
 
     private Logger logger = LoggerFactory.getLogger(ActiveMQRealTimeMessagePublisher.class);
 
     @Autowired
     private JmsTemplate template;
 
     @Autowired
     MailService mailService;
 
 
     private int messageCount =0;
 
     private boolean realTimeEnabled = false;
 
     @Override
     @Scheduled(fixedDelay = 15000)
     public void readAndSend() {
         if(!realTimeEnabled){
             logger.trace("void readAndSend() Realtime is not enabled {}",realTimeEnabled);
         }
         logger.trace("void readAndSend()");
 
         try {
             List<SimpleMailMessage> unreadMessages = mailService.getUnreadMessages();
 
             for(SimpleMailMessage m : unreadMessages){
 
                 logger.trace("void sendMessage() {} ",m);
 
                     try {
                         template.send(
                                Topics.ALERT_MLSensor_Mail_New,
                                 new JavaxMailMessageCreator(m)
                         );
                         messageCount++;
                         logger.debug("Sending message {} ",messageCount);
                     } catch (JmsException e) {
                         logger.warn("Error sending message {} ",m);
                     }
 
 
             }
 
 
 
         } catch (MessagingException e) {
             logger.warn("Couldn't retrieve messages in real time",e);
         } catch (IOException e) {
             logger.warn("Couldn't retrieve messages in real time",e);
         }
 
 
     }
 
     @Override
     public void setRealTimeEnabled(boolean b) {
         this.realTimeEnabled = b;
     }
 }
