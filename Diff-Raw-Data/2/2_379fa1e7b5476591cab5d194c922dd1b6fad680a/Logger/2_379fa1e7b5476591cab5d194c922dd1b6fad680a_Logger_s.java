 package mq;
 
 
 import ch.qos.logback.classic.BasicConfigurator;
 import org.slf4j.LoggerFactory;
 
 import javax.jms.*;
 
 public class Logger implements MessageListener {
 
   private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Logger.class);
 
   public static void main(String[] args) throws JMSException {
     logger.info("starting");
 
     if (args.length != 3) {
       printUsage();
       return;
     }
 
     String brokerUrl = args[0];
     String destination = args[1];
     String destinationType = args[2];
 
     Logger messageLogger = new Logger();
     if ("queue".equals(destinationType)) {
       DestinationManager.listenToQueue(brokerUrl, destination, messageLogger);
     } else if ("topic".equals(destinationType)) {
       DestinationManager.listenToTopic(brokerUrl, destination, messageLogger);
     } else {
       printUsage();
       return;
     }
   }
 
   public void onMessage(Message message) {
     TextMessage textMessage = (TextMessage) message;
     logger.info("received message " + textMessage);
     try {
       logger.info("text: " + textMessage.getText());
     } catch (JMSException e) {
       logger.error("", e);
     }
   }
 
   private static void printUsage() {
    System.out.println("usage: " + Logger.class.getSimpleName() + " <broker host:port> <queue name> {queue|topic}");
   }
 }
