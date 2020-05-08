 package org.motechproject.ghana.national.messagegateway.domain;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.integration.Message;
 import org.springframework.integration.store.MessageGroup;
 import org.springframework.stereotype.Component;
 
 import java.util.Collection;
 
 @Component
 public class DeliveryTimeBasedExpiry implements ExpiryStrategy {

    private Logger logger = LoggerFactory.getLogger(DeliveryTimeBasedExpiry.class);

     @Override
     public int expireMessageGroups(MessageStore messageStore, long timeout) {
         int count = 0;
         for (MessageGroup group : messageStore) {
             final Boolean canTheMessageGroupBeDelivered = canTheFirstMessageBeDelivered(group);
             if (canTheMessageGroupBeDelivered != null && canTheMessageGroupBeDelivered) {
                 count++;
                try{
                    messageStore.expire(group);
                }catch (Exception e){
                    logger.error("Exception while expiring message group, " + group, e);
                }
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
