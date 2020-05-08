 package net.sfr.tv.jms.client.listener;
 
 import net.sfr.tv.jms.client.api.MessageListenerWrapper;
 import java.util.Enumeration;
 import javax.jms.BytesMessage;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.TextMessage;
 import org.apache.log4j.Logger;
 
 /**
  * A simple message listener, printing message content to logging facility.
  * In our case, it's STDOUT
  */
 public class LoggerMessageListener implements MessageListenerWrapper {
 
     private Logger LOGGER;
 
     private String name;
     
     private String outputType;
     
     public LoggerMessageListener() {
         name = LoggerMessageListener.class.getName();
         String loggerName = System.getProperty("listener.logger.name");
         if (loggerName != null && loggerName.trim().length() > 0) {
             LOGGER = Logger.getLogger(loggerName);
         } else {
             LOGGER = Logger.getLogger(LoggerMessageListener.class);
         }
         
        outputType = System.getProperty("listener.output.type", "FULL");
     }
     
     @Override
     public void onMessage(Message msg) {
 
         try {
 
             if (!outputType.equals("BODY")) {
                 LOGGER.info("Received message : ".concat(msg.getJMSMessageID() != null ? msg.getJMSMessageID() : "(unknown ID)").concat(", type : ").concat(msg.getJMSType() != null ? msg.getJMSType() : "(unknown type)").concat(", tstamp : ").concat(String.valueOf(msg.getJMSTimestamp())).concat(", redelivery ? ").concat(String.valueOf(msg.getJMSRedelivered())));
 
                 Enumeration enm = msg.getPropertyNames();
                 String prop;
                 String val;
                 Object oVal;
                 while (enm.hasMoreElements()) {
                     prop = (String) enm.nextElement();
                     try {
                         val = msg.getStringProperty(prop);
                         LOGGER.info("\t".concat(prop).concat(" : ").concat(val != null ? val : "null"));
                     } catch (JMSException ex) {
                         try {
                             oVal = msg.getObjectProperty(prop);
                             LOGGER.info("\t".concat(prop).concat(" : ").concat(oVal != null ? oVal.toString() : "null"));
                         } catch (JMSException ex2) {
                             LOGGER.error("Unable to retrieve value for ".concat(prop));
                         }
                     }
                 }
             }
             
             if (TextMessage.class.isAssignableFrom(msg.getClass())) {
                 String text = ((TextMessage) msg).getText();
                 LOGGER.info(text);
             } else if (BytesMessage.class.isAssignableFrom(msg.getClass())) {
                 BytesMessage bm = (BytesMessage) msg;
                 byte[] body = new byte[(int)bm.getBodyLength()];
                 bm.readBytes(body);
                 
                 LOGGER.info(new String(body));
             }
 
             // ACK the message to remove it from the queue.
             msg.acknowledge();
 
         } catch (JMSException ex) {
             if (IllegalStateException.class.isAssignableFrom(ex.getClass())) {
                 // Connection failure, try failover ?
                 
             }
             LOGGER.error(ex.getMessage(), ex);
         }
     }
 
     @Override
     public void release() {}
 
     @Override
     public String getName() {
         return name;
     }
 
     @Override
     public void setName(String value) {}
 }
