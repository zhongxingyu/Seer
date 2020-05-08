 package arch;
 
 import java.util.ArrayList;
 
 import arch.Enums.partition;
 
 /**
  * Inbox class for which shall by used by the partitions to send
  * and check messages between each other.
  * 
  * @author John Anderson
  *
  */
 public class Inbox {
     
     ArrayList<Message> mList;
     
     /**
      * Creates an empty Inbox to add messages to.
      */
     public Inbox() {
 	
 	mList = new ArrayList<Message>();
 	
     }
     
     /**
      * 
      * @param thisPartition
      * @return array of messages in inbox that are sent to the given subscriber
      */
     public Message[] getNewMessages(partition thisPartition) {
 	
 	ArrayList<Message> m = new ArrayList<Message>();
 	
 	for(int i = 0; i < mList.size(); i++) {
 	    
 	    if(mList.get(i).checkIsSubscriber(thisPartition)) {
 		m.add(mList.get(i));
 		mList.get(i).read = true;
 	    }
 	    
 	}
 	
 	Message[] a = new Message[m.size()];
 	return m.toArray(a);
 	
     }
     
     /**
      * Removes read messages from the inbox.<br>
      * This shall occur once every frame.
      */
     public void emptyReadMessages() {
 	
 	for(int i = 0; i < mList.size(); i++) {
 	    if(mList.get(i).read) {
 		mList.remove(i);
		i--;
 	    }
 	}
 	
     }
     
     /**
      * 
      * @return <i>true</i> if Inbox is empty
      */
     public boolean isEmpty() {
 	
 	return mList.size() == 0;
 	
     }
     
     /**
      * Adds a message to the inbox
      * @param newMessage
      */
     public void sendMessage(Message newMessage) {
 	mList.add(newMessage);
     }
 
     
 }
