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
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import net.dovemq.api.DoveMQMessage;
 import net.dovemq.broker.endpoint.PublisherContext.MessageContext;
 import net.dovemq.transport.endpoint.CAMQPEndpointPolicy;
 import net.dovemq.transport.endpoint.CAMQPMessageDispositionObserver;
 import net.dovemq.transport.endpoint.CAMQPSourceInterface;
 import net.dovemq.transport.endpoint.CAMQPTargetInterface;
 import net.dovemq.transport.endpoint.DoveMQMessageImpl;
 
 import org.apache.commons.lang.StringUtils;
 
 final class TopicRouter implements
         CAMQPMessageReceiver,
         CAMQPMessageDispositionObserver {
     private final String topicName;
 
     private final TopicRouterType routerType;
 
     private final Set<RoutingEvaluator> subscriberProxies = new CopyOnWriteArraySet<>();
 
     private final Map<Long, PublisherContext> publisherSinks = new ConcurrentHashMap<>();
 
     TopicRouter(String topicName, TopicRouterType routerType) {
         super();
         this.topicName = topicName;
         this.routerType = routerType;
     }
 
     /**
      * Acknowledge the completion of a message processing to a Publisher only
      * when all the subscribers have acked the message receipt.
      */
     @Override
     public void messageAckedByConsumer(DoveMQMessage message, CAMQPSourceInterface source) {
         long sourceId = ((DoveMQMessageImpl) message).getSourceId();
         PublisherContext publisherContext = publisherSinks.get(sourceId);
        if (publisherContext != null) {
            publisherContext.messageAckedByConsumer(message);
        }
     }
 
     /**
      * Processes a message received from the attached publisher(s).
      * First, put the message in the publisher-specific inFlightMessages map.
      * Then, get the message's routingEvaluationContext and call
      * {@link RoutingEvaluator#canMessageBePublished(Object)} to evaluate
      * the message to determine whether it can be routed to the subscriber.
      */
     @Override
     public void messageReceived(DoveMQMessage message, CAMQPTargetInterface publisher) {
         long deliveryId = ((DoveMQMessageImpl) message).getDeliveryId();
         if (subscriberProxies.isEmpty()) {
             publisher.acknowledgeMessageProcessingComplete(deliveryId);
             return;
         }
 
         long publisherId = publisher.getId();
         ((DoveMQMessageImpl) message).setSourceId(publisherId);
 
         PublisherContext publisherContext = publisherSinks.get(publisherId);
         MessageContext messageContext = publisherContext.addInFlightMessage(deliveryId, subscriberProxies.size());
 
         Object routingEvaluationContext = null;
         if (routerType == TopicRouterType.MessageTagFilter) {
             routingEvaluationContext = message.getApplicationProperty(DoveMQMessageImpl.ROUTING_TAG_KEY);
         } else if (routerType == TopicRouterType.Hierarchical) {
             routingEvaluationContext = message.getApplicationProperty(DoveMQMessageImpl.TOPIC_PUBLISH_HIERARCHY_KEY);
         }
 
         /**
          * Keep track of a count of how many subscribers the message could not be routed
          * to. Then, update the MessageContext accordingly, and notify the publisher if
          * there are no subscribers left to acknowledge.
          */
         int unroutedSubscriberCount = 0;
         for (RoutingEvaluator subscriberProxy : subscriberProxies) {
             if (subscriberProxy.canMessageBePublished(routingEvaluationContext)) {
                 subscriberProxy.getSubscriberProxy().sendMessage(message);
             } else {
                 unroutedSubscriberCount++;
             }
         }
 
         if (unroutedSubscriberCount > 0) {
             publisherContext.messageFilteredOutBySubscribers(deliveryId, messageContext, unroutedSubscriberCount);
         }
     }
 
     void subscriberAttached(CAMQPSourceInterface destination, CAMQPEndpointPolicy endpointPolicy) {
         if (routerType == TopicRouterType.MessageTagFilter) {
             Pattern messageFilterPattern = null;
             String messageFilterPatternString = endpointPolicy.getMessageFilterPattern();
             if (!StringUtils.isEmpty(messageFilterPatternString)) {
                 try {
                     messageFilterPattern = Pattern.compile(messageFilterPatternString);
                 }
                 catch (PatternSyntaxException ex) {
                     messageFilterPattern = null;
                 }
             }
             subscriberProxies.add(new RoutingMessageTagFilterEvaluator(messageFilterPattern, destination));
         }
         else if (routerType == TopicRouterType.Hierarchical) {
             String subscriptionTopicHierarchy = endpointPolicy.getSubscriptionTopicHierarchy();
             if (!StringUtils.isEmpty(subscriptionTopicHierarchy)) {
                 subscriberProxies.add(new RoutingTopicHeirarchyMatcher(subscriptionTopicHierarchy, destination));
             }
             else {
                 subscriberProxies.add(new RoutingTopicHeirarchyMatcher(topicName, destination));
             }
 
         }
         else {
             subscriberProxies.add(new RoutingEvaluator(destination));
         }
 
         destination.registerDispositionObserver(this);
     }
 
     void subscriberDetached(CAMQPSourceInterface targetProxy) {
         for (RoutingEvaluator subscriberContext : subscriberProxies) {
             if (subscriberContext.getSubscriberProxy() == targetProxy) {
                 subscriberProxies.remove(subscriberContext);
                 /*
                  * Notify PublisherContexts so that they are not waiting for
                  * in-flight messages to be acked.
                  */
                 Collection<PublisherContext> publisherContexts = publisherSinks.values();
                 long now = System.currentTimeMillis();
                 for (PublisherContext publisherSink : publisherContexts) {
                     publisherSink.subscriberDetached(subscriberContext.getTimeOfSubscription(), now);
                 }
                 return;
             }
         }
     }
 
     void publisherAttached(CAMQPTargetInterface sourceSink) {
         publisherSinks.put(sourceSink.getId(), new PublisherContext(sourceSink));
         sourceSink.registerMessageReceiver(this);
     }
 
     void publisherDetached(CAMQPTargetInterface sourceSink) {
         publisherSinks.remove(sourceSink.getId());
     }
 
     boolean isCompletelyDetached() {
         return (publisherSinks.isEmpty() && subscriberProxies.isEmpty());
     }
 
     String getTopicName() {
         return topicName;
     }
 
     TopicRouterType getRouterType() {
         return routerType;
     }
 }
