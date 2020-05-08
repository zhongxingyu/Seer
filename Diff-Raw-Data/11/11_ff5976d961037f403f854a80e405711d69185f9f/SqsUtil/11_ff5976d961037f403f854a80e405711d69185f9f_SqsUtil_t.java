 package de.fhb.dlService;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.amazonaws.services.sqs.AmazonSQS;
 import com.amazonaws.services.sqs.model.CreateQueueRequest;
 import com.amazonaws.services.sqs.model.DeleteMessageRequest;
 import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
 import com.amazonaws.services.sqs.model.GetQueueUrlResult;
 import com.amazonaws.services.sqs.model.ListQueuesResult;
 import com.amazonaws.services.sqs.model.Message;
 import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
 import com.amazonaws.services.sqs.model.SendMessageRequest;
 
 /**
  * This class is a both entrypoint utility class which manages writing and reading out of AWS SQS
  * @author MaccaPC
  *
  */
 
 public class SqsUtil {
     /** The aws sqs credentials instance*/
    private AmazonSQS sqs = null;
 
     /**
      * The constructor
      * @param aSqs the sqs instance
      */
     public SqsUtil(AmazonSQS aSqs) {
         this.sqs = aSqs;
     }
 
     /**
      * Creates a SQS Queue.
      * @param aQueueName the queuename
      * @return the queueUrl
      */
     public String createQueue(String aQueueName) {
         CreateQueueRequest qRequest = new CreateQueueRequest(aQueueName);
         String aQueueUrl = sqs.createQueue(qRequest).getQueueUrl();
         return aQueueUrl;
     }
 
     /**
      * sends a Message to a named Queue
      * @param aQueueName
      * @param aMessage
      */
     public void sendSqsMessage(String aQueueName, String aMessage) {
         String aQueueUrl = getQueueUrl(aQueueName);
         sqs.sendMessage(new SendMessageRequest(aQueueUrl, aMessage));
     }
 
     /**
      * Gets the message from a named queue
     * @param aQueueName the given queue name
      * @return a list containing all received messages
      */
     public ArrayList<Message> getMessagefromAnotherEntrypoint(String aQueueName) {
         ReceiveMessageRequest aMessageRequest;
         String queueUrl = getQueueUrl(aQueueName);
         ArrayList<Message> resultMessagePoll = new ArrayList<Message>();
         List<Message> deleteMessage = null;
         try {
             do {
                 aMessageRequest = new ReceiveMessageRequest(queueUrl).withMaxNumberOfMessages(10);
                 deleteMessage = sqs.receiveMessage(aMessageRequest).getMessages();
                 resultMessagePoll.addAll(deleteMessage);
                 for (Message message : deleteMessage) {
                     deleteMessageAfterRead(aQueueName, message);
                 }
             } while (deleteMessage.size() != 0);
 
             return resultMessagePoll;
 
         } catch (Exception e) {
             // do nothing
         }
         return null;
     }
 
     /**
      * Gets the queueUrl
      * @param aQueueName the given queue name
      * @return the queueUrl
      */
    private String getQueueUrl(String aQueueName) {
         GetQueueUrlRequest queueUrlRequest = new GetQueueUrlRequest(aQueueName);
         GetQueueUrlResult queueUrlResult = sqs.getQueueUrl(queueUrlRequest);
         String queueUrl = queueUrlResult.getQueueUrl();
         return queueUrl;
     }
 
     /**
      * Deletes designated Message
      * @param aQueueName the queue name
     * @param aMessage the given message
      */
     public void deleteMessageAfterRead(String aQueueName, Message aMessage) {
         String aQueueUrl = getQueueUrl(aQueueName);
         sqs.deleteMessage(new DeleteMessageRequest(aQueueUrl, aMessage.getReceiptHandle()));
     }
 
     /**
      * Checks if a queue of a given name already exists
      * @param aQueueName the given queue name
      * @return true if queue found
      */
     public boolean checkIfQueueExist(String aQueueName) {
         ListQueuesResult listQueueRes = sqs.listQueues();
         List<String> queueUrlLst = listQueueRes.getQueueUrls();
         String targetQueueUrl = getQueueUrl(aQueueName);
         if (queueUrlLst != null) {
             for (String queueUrl : queueUrlLst) {
                 if (queueUrl.equals(targetQueueUrl)) {
                     return true;
                 }
             }
 
         }
         return false;
     }
 }
