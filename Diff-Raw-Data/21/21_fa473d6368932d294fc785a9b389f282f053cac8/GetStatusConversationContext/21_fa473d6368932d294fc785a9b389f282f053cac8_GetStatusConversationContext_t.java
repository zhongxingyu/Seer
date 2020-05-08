 package org.bitrepository.access.getstatus.conversation;
 
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.protocol.conversation.ConversationContext;
 import org.bitrepository.protocol.eventhandler.EventHandler;
 import org.bitrepository.protocol.messagebus.MessageSender;
 
 public class GetStatusConversationContext extends ConversationContext {
 
     private final String clientID;
     
     public GetStatusConversationContext(Settings settings, MessageSender messageSender, EventHandler eventHandler,
             String auditTrailInformation, String clientID) {
        super(settings, messageSender, clientID, eventHandler, auditTrailInformation);
         this.clientID = clientID;
     }
 }
