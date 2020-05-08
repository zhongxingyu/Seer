 package org.motechproject.ghana.national.messagegateway.domain;
 
 import org.springframework.integration.Message;
 import org.springframework.integration.store.MessageGroup;
 import org.springframework.stereotype.Component;
 
 import java.util.Collection;
 
 @Component
 public class DeliveryTimeBasedExpiry implements ExpiryStrategy {
     @Override
     public int expireMessageGroups(MessageStore messageStore, long timeout) {
         int count = 0;
         for (MessageGroup group : messageStore) {
             final Boolean canTheMessageGroupBeDelivered = canTheFirstMessageBeDelivered(group);
             if (canTheMessageGroupBeDelivered != null && canTheMessageGroupBeDelivered) {
                 count++;
                messageStore.expire(group);
             }
         }
         return count;
     }
 
     private Boolean canTheFirstMessageBeDelivered(MessageGroup group) {
         final Collection<Message<?>> messages = group.getMessages();
         if (messages.size() > 0) {
             final Message firstMessage = messages.iterator().next();
             return ((DeliveryTimeAware) firstMessage.getPayload()).canBeDispatched();
         }
         return null;
     }
 }
