 package mq;
 
 import org.apache.solr.common.SolrInputDocument;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.jms.JMSException;
 import java.io.*;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingDeque;
 
 public class SerializedPublisher {
 
   private static final Logger logger = LoggerFactory.getLogger(SerializedPublisher.class);
 
   private final String serializedFile;
   private final BlockingQueue<SolrInputDocument> publishQueue;
 
   public static void main(String[] args) throws JMSException, IOException {
 
     if (args.length != 3) {
      logger.error("incorrect args given. usage: " + QueueSerializer.class.getSimpleName() + " <broker host:port> <queue name> <serialized file>");
     }
 
     String brokerUrl = args[0];
     String queueName = args[1];
     String filename = args[2];
 
     QueueManager.SessionAndProducer sessionAndProducer = QueueManager.createSessionAndProducer(brokerUrl, queueName);
 
     BlockingQueue<SolrInputDocument> publishQueue = new LinkedBlockingDeque<SolrInputDocument>();
     SerializedPublisher serializedPublisher = new SerializedPublisher(filename, publishQueue);
     ProducerFromQueue<SolrInputDocument> producerFromQueue =
         new ProducerFromQueue<SolrInputDocument>(publishQueue, sessionAndProducer.session, sessionAndProducer.producer);
 
     new Thread(producerFromQueue).start();
 
     serializedPublisher.deserialize();
   }
 
   public SerializedPublisher(String serializedFile, BlockingQueue publishQueue) {
     this.serializedFile = serializedFile;
     this.publishQueue = publishQueue;
   }
 
   public void deserialize() throws IOException {
     FileInputStream fileInputStream = new FileInputStream(serializedFile);
     ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
 
     while(true) {
       SolrInputDocument solrInputDocument = null;
 
       try {
         solrInputDocument = (SolrInputDocument) objectInputStream.readObject();
       } catch (EOFException e) {
         logger.error("", e);
         break;
       } catch (OptionalDataException e) {
         logger.error("", e);
         if (e.eof) {
           logger.info("end of file");
           break;
         }
       } catch (ClassNotFoundException e) {
         logger.error("", e);
       } catch (IOException e) {
         logger.error("", e);
       }
 
       logger.info("adding " + solrInputDocument);
 
       try {
         publishQueue.put(solrInputDocument);
       } catch (InterruptedException e) {
         logger.error("", e);
       }
     }
   }
 }
