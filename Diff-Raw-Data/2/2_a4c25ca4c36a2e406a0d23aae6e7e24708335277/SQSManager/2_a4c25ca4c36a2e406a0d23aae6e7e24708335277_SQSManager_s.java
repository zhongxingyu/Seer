 package pl.softwaremill.common.sqs;
 
 import com.xerox.amazonws.sqs2.Message;
 import com.xerox.amazonws.sqs2.MessageQueue;
 import com.xerox.amazonws.sqs2.SQSException;
 import com.xerox.amazonws.sqs2.SQSUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import pl.softwaremill.common.sqs.exception.SQSRuntimeException;
 import pl.softwaremill.common.sqs.util.Base64Coder;
 import pl.softwaremill.common.sqs.util.SQSAnswer;
 
 import java.io.*;
 
 import static pl.softwaremill.common.sqs.SQSConfiguration.*;
 
 /**
  * Class for sending messages to Amazon's Simple Queue Service
  * Configured via sqs.conf in jboss/server/profile/conf or classpath
  * AWSAccessKeyId= aws access key
  * SecretAccessKey= secret key
  *
  * @author Jaroslaw Kijanowski - jarek@softwaremill.pl
  *         Date: Aug 16, 2010
  * @author Adam Warski
  */
 public class SQSManager {
     private static final Logger log = LoggerFactory.getLogger(SQSManager.class);
 
     private static final int REDELIVERY_LIMIT = 10;
 
 
     /**
      * @param queue   the name of the SQS queue for which to set the timeout
      * @param timeout timeout in seconds for the whole queue (default is 30) - value is limited to 43200 seconds (12 hours)
      */
     public static void setQueueVisibilityTimeout(String queue, int timeout) {
         try {
             MessageQueue msgQueue = SQSUtils.connectToQueue(queue, AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
             msgQueue.setVisibilityTimeout(timeout);
         } catch (SQSException e) {
            throw new SQSRuntimeException("Could not setup SQS EMAIL_SQS_QUEUE: " + queue, e);
         }
     }
 
 
     /**
      * Sends a serializable message to an SQS queue using the Base64Coder util to encode it properly
      *
      * @param queue   the SQS queue the message is sent to
      * @param message a Serializable object
      */
     public static void sendMessage(String queue, Serializable message) {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
         try {
 
             ObjectOutputStream oos = new ObjectOutputStream(baos);
             oos.writeObject(message);
             oos.flush();
             oos.close();
             baos.close();
 
         } catch (IOException e) {
             throw new SQSRuntimeException("Could not create stream, SQS message not sent: ", e);
         }
 
         String encodedMessage = new String(Base64Coder.encode(baos.toByteArray()));
 
         log.debug("Serialized Message: " + encodedMessage);
 
         for (int i = 0; i < REDELIVERY_LIMIT; i++) {
             try {
                 MessageQueue msgQueue = SQSUtils.connectToQueue(queue, AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
                 String msgId = msgQueue.sendMessage(encodedMessage);
 
                 log.info("Sent message with id " + msgId + " to queue " + queue);
                 i = REDELIVERY_LIMIT;
 
             } catch (SQSException e) {
                 log.error("Colud not sent message to SQS queue: " + queue, e);
                 if (i == REDELIVERY_LIMIT) {
                     throw new SQSRuntimeException("Exceeded redelivery value: " + REDELIVERY_LIMIT + "; message not sent! ", e);
                 }
                 log.info("Retrying in 10 seconds");
                 try {
                     Thread.sleep(10000);
                 } catch (InterruptedException e1) {
                     e1.printStackTrace();
                 }
             }
         }
     }
 
     /**
      * Receives a message from a SQS queue and decodes it properly to an object using the Base64Coder util
      *
      * @param queue the SQS queue the message is received from
      * @return SQSAnswer holding an Object and the receipt handle for further processing
      *         or {@code null} if no message was available
      */
     public static SQSAnswer receiveMessage(String queue) {
         try {
 
             log.debug("Polling queue " + queue);
 
             MessageQueue msgQueue = SQSUtils.connectToQueue(queue, AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
 
             Message msg = msgQueue.receiveMessage();
 
             if (msg != null) {
                 String data = msg.getMessageBody();
 
                 ByteArrayInputStream bais = new ByteArrayInputStream(Base64Coder.decode(data));
                 Object answer;
 
                 try {
                     ObjectInputStream ois = new ObjectInputStream(bais);
                     answer = ois.readObject();
                 } catch (IOException e) {
                     throw new SQSRuntimeException("I/O exception.", e);
                 } catch (ClassNotFoundException e) {
                     throw new SQSRuntimeException("Class of the serialized object cannot be found.", e);
                 }
 
                 log.info("Got message from queue " + queue);
 
                 return new SQSAnswer(answer, msg.getReceiptHandle());
             } else {
                 return null;
             }
 
         } catch (SQSException e) {
             throw new SQSRuntimeException("Could not receive message from SQS queue: " + queue, e);
         }
     }
 
 
     /**
      * Deletes a message from a SQS queue
      *
      * @param receiptHandle handle of the message to be deleted
      * @param queue         SQS queue the message is held
      */
     public static void deleteMessage(String queue, String receiptHandle) {
         try {
             MessageQueue msgQueue = SQSUtils.connectToQueue(queue, AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
             msgQueue.deleteMessage(receiptHandle);
             log.debug("Deleted message in queue: " + queue);
         } catch (SQSException e) {
             throw new SQSRuntimeException("Could not delete message in queue " + queue + "! This will cause a redelivery.", e);
         }
     }
 
 
     /**
      * Resets a messages timeout in a SQS queue
      *
      * @param receiptHandle handle of the message to be reset
      * @param timeOut       new timeout to be set
      * @param queue         SQS queue the message is held
      */
     public static void setMessageVisibilityTimeout(String queue, String receiptHandle, int timeOut) {
         try {
             MessageQueue msgQueue = SQSUtils.connectToQueue(queue, AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
             msgQueue.setMessageVisibilityTimeout(receiptHandle, timeOut);
             log.debug("Set timeout to " + timeOut + " seconds in queue: " + queue);
         } catch (SQSException e) {
             throw new SQSRuntimeException("Could not reset timeout for message in queue " + queue + "! This will cause a delay in redelivery.", e);
         }
     }
 
 }
