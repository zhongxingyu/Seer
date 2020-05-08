 /**
  * Copyright 2012 Tejeswar Das
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package net.dovemq.broker.endpoint;
 
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import net.dovemq.api.DoveMQMessage;
 import net.dovemq.transport.endpoint.CAMQPTargetInterface;
 import net.dovemq.transport.endpoint.DoveMQMessageImpl;
 
final class PublisherContext {
 
     static final class MessageContext {
         boolean sentInBetween(long beginTime, long endTime) {
             return ((contextCreationTime > beginTime) && (contextCreationTime < endTime));
         }
 
         MessageContext(int numSubscribers) {
             super();
             this.numSubscribers = new AtomicInteger(numSubscribers);
             contextCreationTime = System.currentTimeMillis();
         }
 
         final AtomicInteger numSubscribers;
 
         private final long contextCreationTime;
     }

    PublisherContext(CAMQPTargetInterface publisher) {
         super();
         this.publisher = publisher;
     }
 
     /**
      * Acknowledge the completion of a message processing to a Publisher only
      * when all the subscribers have acked the message receipt.
      */
     void messageAckedByConsumer(DoveMQMessage message) {
         long deliveryId = ((DoveMQMessageImpl) message).getDeliveryId();
         MessageContext msgContext = inFlightMessages.get(deliveryId);
         if (msgContext != null) {
             int numSubscribersLeftToAck = msgContext.numSubscribers.decrementAndGet();
 
             /*
              * All subscribers have acknowledged; Notify the publisher of
              * message processing completion
              */
             if (0 == numSubscribersLeftToAck) {
                 notifyPublisherOfMessageProcessingCompletion(deliveryId);
             }
         }
     }
 
     void messageFilteredOutBySubscribers(long deliveryId, MessageContext msgContext, int filteredSubscriberCount) {
         int numSubscribersLeftToAck = msgContext.numSubscribers.addAndGet(-filteredSubscriberCount);
         if (0 == numSubscribersLeftToAck) {
             notifyPublisherOfMessageProcessingCompletion(deliveryId);
         }
     }
 
     /**
      * Remove the message from the unacknowledged map, and notify Publisher of
      * message processing completion.
      *
      * @param deliveryId
      */
     void notifyPublisherOfMessageProcessingCompletion(long deliveryId) {
         if (null != inFlightMessages.remove(deliveryId)) {
             publisher.acknowledgeMessageProcessingComplete(deliveryId);
         }
     }
 
     MessageContext addInFlightMessage(long deliveryId, int subscriberCount) {
         MessageContext messageContext = new MessageContext(subscriberCount);
         inFlightMessages.put(deliveryId, messageContext);
         return messageContext;
     }
 
     void subscriberDetached(long subscriberAttachTime, long now) {
         Set<Long> inFlightMessageIds = inFlightMessages.keySet();
         for (long deliveryId : inFlightMessageIds) {
             MessageContext msgContext = inFlightMessages.get(deliveryId);
             if (msgContext.sentInBetween(subscriberAttachTime, now)) {
                 int numSubscribersLeftToAck = msgContext.numSubscribers.decrementAndGet();
 
                 /*
                  * All subscribers have acknowledged; Notify the publisher of
                  * message processing completion
                  */
                 if (0 == numSubscribersLeftToAck) {
                     notifyPublisherOfMessageProcessingCompletion(deliveryId);
                 }
             }
         }
     }
 
     private final CAMQPTargetInterface publisher;
     private final ConcurrentMap<Long, MessageContext> inFlightMessages = new ConcurrentHashMap<>();
 }
