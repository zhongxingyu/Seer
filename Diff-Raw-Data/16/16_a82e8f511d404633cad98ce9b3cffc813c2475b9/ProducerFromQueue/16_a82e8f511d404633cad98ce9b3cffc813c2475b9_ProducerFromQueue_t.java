 package mq;
 
import org.apache.solr.common.SolrInputDocument;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.jms.JMSException;
 import javax.jms.MessageProducer;
 import javax.jms.ObjectMessage;
 import javax.jms.Session;
 import java.io.Serializable;
 import java.util.concurrent.BlockingQueue;
 
import static mq.SerializedPublisher.POISON_PILL;

 public class ProducerFromQueue<T> implements Runnable {
 
   private static final Logger logger = LoggerFactory.getLogger(ProducerFromQueue.class);
   
   private final BlockingQueue<T> blockingQueue;
   private final Session session;
   private final MessageProducer messageProducer;
 
   public ProducerFromQueue(BlockingQueue<T> blockingQueue, Session session, MessageProducer producer) {
     this.blockingQueue = blockingQueue;
     this.session = session;
     this.messageProducer = producer;
   }
 
   @Override
   public void run() {
     while (true) {
       try {
         T input = blockingQueue.take();
 
        if (POISON_PILL.equals(input)) {
           // Poison pill.
           System.exit(0);
         }
 
         ObjectMessage objectMessage;
         try {
           objectMessage = session.createObjectMessage((Serializable)input);
           messageProducer.send(objectMessage);
         } catch (JMSException e) {
           logger.error("", e);
           continue;
         }
         logger.info(objectMessage.toString());
         
       } catch (InterruptedException e) {
         logger.error("", e);
       }
     }
   }
 }
