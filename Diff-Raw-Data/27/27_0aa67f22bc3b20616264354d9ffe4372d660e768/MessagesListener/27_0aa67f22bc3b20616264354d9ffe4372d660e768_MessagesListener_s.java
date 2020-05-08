 package edu.hawaii.ihale.frontend.page.messages;
 
 import java.util.List;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleSystem;
 import edu.hawaii.ihale.api.ApiDictionary.SystemStatusMessageType;
 import edu.hawaii.ihale.api.repository.SystemStatusMessage;
 import edu.hawaii.ihale.api.repository.SystemStatusMessageListener;
 import edu.hawaii.ihale.frontend.SolarDecathlonApplication;
 
 /**
  * Listens for message events.
  * 
  * @author FrontEnd
  * 
  */
 public class MessagesListener extends SystemStatusMessageListener {
 
   @Override
   public void messageAdded(Long timestamp, IHaleSystem system, SystemStatusMessageType messageType,
       String msg) {
 
     // Convert into a status message object
     SystemStatusMessage statusMessage =
         new SystemStatusMessage(timestamp, system, messageType, msg);
 
     // All messages
     List<SystemStatusMessage> allMsgs = SolarDecathlonApplication.getMessages().getAllMessages();
 
     // Messages only pertaining to this system
     List<SystemStatusMessage> thisSystemMsgs =
        SolarDecathlonApplication.getMessages().getAllMessages();
 
    // If this is the first message, then remove the "No Messages" default message
    if (allMsgs.size() == 1) {
      allMsgs.remove(0);
    }
     
     // Keep master list bounded by 100
    else if (allMsgs.size() > 100) {
       allMsgs.remove(100);
     }
    
    if (thisSystemMsgs.size() == 1) {
      thisSystemMsgs.remove(0);
    }
     
     // Keep other lists bounded by 50
    else if (allMsgs.size() > 50) {
       allMsgs.remove(50);
     }
 
     // Append it to the front of the list that holds all messages (for the dashboard)
     allMsgs.add(0, statusMessage);
 
     // Adds the message to the correct system page (latest goes to the front of list)
     thisSystemMsgs.add(0, statusMessage);
 
   } // End message added method
 
 } // End Messages Listener Class
