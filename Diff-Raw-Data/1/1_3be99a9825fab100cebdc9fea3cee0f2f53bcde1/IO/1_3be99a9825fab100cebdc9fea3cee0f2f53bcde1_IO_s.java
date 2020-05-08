 package com.mediware.arch;
 
 import java.util.ArrayList;
 
 import com.mediware.arch.Enums.mType;
 import com.mediware.arch.Enums.partition;
 
 /**
  * IO Partition head class.  Used in the exchange of messages between partitions.
  * 
  * @author John Anderson
  *
  */
 public class IO {
     
     private Inbox messageInbox;
     ArrayList<SMessage> messageScheduleList;
     
     
     public IO() {
 	
 		//Creates new inbox to be used
 		messageInbox = new Inbox();
 		       
 		messageScheduleList = new ArrayList<SMessage>();
 		
     }
     
     /**
      * 
      * @return Inbox
      */
     public Inbox getInbox() {
     	return messageInbox;
     }
     
     /**
      * Creates a message to be sent on the next frame
      * 
      * @param publisher
      * @param subscriber
      * @param args
      * @param messageType
      */
     public void createMessageToSend(partition publisher, partition[] subscriber, mData messageData, mType messageType) {
 	
 		Message newMessage = new Message(publisher, subscriber, messageData, messageType);
 		messageInbox.sendMessage(newMessage);
 		System.out.println("Message sent of type " + messageType.toString());
 		if(messageData.getArguments().length > 0) {
 		    System.out.println("Integer Arguments: "); 
 		    for(int i = 0; i < messageData.getArguments().length; i++) {
 			System.out.print(messageData.getArguments()[i] + " ");
 		    }
 		}
 		
 		if(messageData.getLabels().length > 0) {
 		    System.out.println("String Arguments: ");
 		    for(int i = 0; i < messageData.getLabels().length; i++) {
 			System.out.print(messageData.getLabels()[i] + " ");
 		    }
 		}
     }
     
     /**
      * Creates a message to be scheduled to send for the next (sendCount) frames
      * 
      * @param publisher
      * @param subscriber
      * @param args
      * @param messageType
      * @param sendCount
      */
     public void createScheduledMessage(partition publisher, partition[] subscriber, mData messageData, mType messageType, int sendCount) {
 		
 		Message newMessage = new Message(publisher, subscriber, messageData, messageType);
 		SMessage newSMessage = new SMessage(newMessage, sendCount);
 		
 		messageScheduleList.add(newSMessage);
 		
     }
     
     /**
      * Sends all scheduled messages for the specified partition
      * and decrements their sendCount.<br> 
      * if sendCount = 0 -> delete from schedule list.
      */
     public void scheduleMessages(partition p) {
 	
 		for(int i = 0; i < messageScheduleList.size(); i++) {
 		    if(messageScheduleList.get(i).m.getPublisher() == p)
 			messageInbox.sendMessage(messageScheduleList.get(i).m);
 		    	if(messageScheduleList.get(i).decrementSendCount()) {
 		    	    messageScheduleList.remove(i);
 		    	    i--;
 		    	}
 		
 		}
     }
     
     /**
      * Progresses the IO frame for the partition:<br>
      * Sends all messages scheduled by the partition
      * 
      * @param p
      * @return List of new message for which the partition is subscribed
      */
     public Message[] nextFrame(partition p) {
 		
 		scheduleMessages(p);
 		Message[] returnMessages = messageInbox.getNewMessages(p);
 		//messageInbox.emptyReadMessages();
 		return returnMessages;
     }
     
 
 }
