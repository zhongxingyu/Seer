 package org.bitrepository.access.getstatus.conversation;
 
 import org.bitrepository.bitrepositorymessages.Message;
 import org.bitrepository.protocol.conversation.AbstractConversation;
 import org.bitrepository.protocol.conversation.ConversationEventMonitor;
 import org.bitrepository.protocol.conversation.ConversationState;
 import org.bitrepository.protocol.conversation.FinishedState;
 
 public class GetStatusConversation extends AbstractConversation {
 
     private final GetStatusConversationContext context;
     
     public GetStatusConversation(GetStatusConversationContext context) {
        super(context.messageSender, context.getConversationID(), null, null);
         this.context = context;
         context.setState(new IdentifyingContributorsForGetStatus(context));
     }
     
     @Override
     public void onMessage(Message message) {
         context.getState().handleMessage(message);
     }
 
     @Override
     public void startConversation() {
         context.getState().start();
     }
     
     @Override
     public void endConversation() {
         context.setState(new FinishedState(context));       
     }
     
     /**
      * Override to use the new context provided monitor.
      * @return The monitor for distributing update information
      */
     public ConversationEventMonitor getMonitor() {
         return context.getMonitor();
     }
 
     @Override
     public boolean hasEnded() {
         return context.getState() instanceof FinishedState;
     }
 
     @Override
     public ConversationState getConversationState() {
         // Only used to start conversation, which has been overloaded. This is because the current parent state isn't of
         // type ConversationState in the GetStatusCLient.
         return null;
     }
 }
