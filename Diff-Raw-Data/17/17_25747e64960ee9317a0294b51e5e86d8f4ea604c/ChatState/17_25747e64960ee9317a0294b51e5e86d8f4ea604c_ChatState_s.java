 // ChatState
 //package cs149.chat;
 
 import java.util.LinkedList;
 
 public class ChatState {
     private static final int MAX_HISTORY = 32;
 
     private final String name;
     private final LinkedList<String> history = new LinkedList<String>();
     private long lastID = System.currentTimeMillis();
 
     public ChatState(final String name) {
 	this.name = name;
 	history.addLast("Hello " + name + "!");
     }
 
     public String getName() {
 	return name;
     }
 
     public synchronized void addMessage(final String msg) {
 	while (history.size() >= MAX_HISTORY) {
 	    history.remove();
 	}
 	history.add(msg);
 	lastID++;
 	this.notifyAll();
     }
 
     private static final int NUM_SECONDS_WAIT_FOR_NEW_MESSAGE = 15;
 
     public synchronized String recentMessages(long mostRecentSeenID) {
 
	long entryTime = System.currentTimeMillis();
 
	while (lastID <= mostRecentSeenID) {
 	    try {
		long timeout = entryTime + NUM_SECONDS_WAIT_FOR_NEW_MESSAGE*1000 - System.currentTimeMillis();
		if (timeout > 0) {
		    this.wait(timeout);
		} else {
		    break;
		}
 	    } catch (InterruptedException e) {
		e.printStackTrace();
 	    }
 	}
 
 	if (lastID > mostRecentSeenID) {
 	    String concatenatedString = "";
 	    long messageID = lastID - history.size();
 	    for (String message : history) {
 		messageID++;
 		if (messageID > mostRecentSeenID) {
 		    concatenatedString += messageID + ": " + message + "\n";
 		}
 	    }
 	    return concatenatedString;
 	} 
 
 	return "";
     }
 }
