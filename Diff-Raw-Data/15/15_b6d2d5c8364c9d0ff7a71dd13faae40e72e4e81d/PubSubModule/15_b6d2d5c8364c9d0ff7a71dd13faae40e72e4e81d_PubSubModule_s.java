 /**
  * Mule Publish/Subscribe Module
  *
  * Copyright 2013 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * This software is protected under international copyright law. All use of this software is
  * subject to MuleSoft's Master Subscription Agreement (or other master license agreement)
  * separately entered into in writing between you and MuleSoft. If such an agreement is not
  * in place, you may not use the software.
  */
 
 
 package org.mule.modules.pubsub;
 
 import com.google.common.collect.HashBasedTable;
 import com.google.common.collect.Table;
 import com.hazelcast.core.ITopic;
 import com.hazelcast.core.Message;
 import com.hazelcast.core.MessageListener;
 import com.mulesoft.mule.cluster.hazelcast.HazelcastManager;
 import org.mule.DefaultMuleEvent;
 import org.mule.DefaultMuleMessage;
 import org.mule.MessageExchangePattern;
 import org.mule.api.MuleContext;
 import org.mule.api.MuleEvent;
 import org.mule.api.MuleException;
 import org.mule.api.MuleMessage;
 import org.mule.api.annotations.Module;
 import org.mule.api.annotations.Processor;
 import org.mule.api.annotations.Source;
 import org.mule.api.annotations.param.Optional;
 import org.mule.api.callback.SourceCallback;
 import org.mule.construct.Flow;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.resource.spi.work.WorkException;
 import java.util.Map;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  * Mule Publish/Subscribe Module allows publish-subscribe-style communication between flows without requiring them
  * to explicitly register with one another (and thus be aware of each other). It is not a general-purpose publish-subscribe system,
  * nor is it intended for inter-app communication.
  *
  * @author MuleSoft, Inc.
  */
 @Module(name = "pubsub", schemaVersion = "1.0")
 public class PubSubModule {
     private static final Logger LOGGER = LoggerFactory.getLogger(PubSubModule.class);
 
     @Inject
     private MuleContext muleContext;
 
     /**
      * Table of subscribers by topic and subscriberId
      */
     private Table<String, String, MessageListener> subscribers;
 
     /**
      * Lock for reading and writing to the subscribers table
      */
     private final ReadWriteLock subscribersLock = new ReentrantReadWriteLock();
 
     @PostConstruct
     public void init() {
         this.subscribers = HashBasedTable.create();
     }
 
     /**
      * Subscribe for Mule events under the specified topic name
      * <p/>
      * {@sample.xml ../../../doc/pubsub-module.xml.sample pubsub:listener}
      *
      * @param topic    Name of the topic
      * @param callback flow to process
      */
     @Source(exchangePattern = MessageExchangePattern.ONE_WAY)
     public void listener(String topic, final SourceCallback callback) {
         ITopic hazelcastTopic = HazelcastManager.getInstance().getHazelcastInstance().getTopic(topic);
         hazelcastTopic.addMessageListener(new MessageListener() {
             @Override
             public void onMessage(Message message) {
                 MuleEvent event = (MuleEvent) message.getMessageObject();
 
                 // clone the message
                MuleMessage muleMessage = new DefaultMuleMessage(event.getMessage());
                 MuleEvent newEvent = new DefaultMuleEvent(muleMessage, event);
 
                 // process it
                 try {
                     callback.processEvent(newEvent);
                 } catch (MuleException e) {
                     LOGGER.error(e.getMessage(), e);
                 }
             }
         });
     }
 
     /**
      * Subscribe for Mule events under the specified topic name using
      * the specified subscriberId. This is useful when you dynamically
      * want to add new subscribers to events, if you subscription map
      * is static you should use <pubsub:listener> instead.
      * <p/>
      * {@sample.xml ../../../doc/pubsub-module.xml.sample pubsub:subscribe}
      *
      * @param topic        Name of the topic
      * @param subscriberId Identification of the subscriber, this is useful for later removing the subscriber
      * @param flow         Flow that will handle the events for the specified topic
      */
     @Processor
    public void subscribe(String topic, String subscriberId, final Flow flow) {
         subscribersLock.writeLock().lock();
 
         try {
             subscribers.put(topic, subscriberId, new MessageListener() {
                 @Override
                 public void onMessage(Message message) {
                     MuleEvent event = (MuleEvent) message.getMessageObject();
 
                     // clone the message
                    MuleMessage muleMessage = new DefaultMuleMessage(event.getMessage());
                     MuleEvent newEvent = new DefaultMuleEvent(muleMessage, event);
 
                     // process it
                     try {
                         flow.process(newEvent);
                     } catch (MuleException e) {
                         LOGGER.error(e.getMessage(), e);
                     }
                 }
             });
         } finally {
             subscribersLock.writeLock().unlock();
         }
 
         subscribersLock.readLock().lock();
 
         try {
             ITopic hazelcastTopic = HazelcastManager.getInstance().getHazelcastInstance().getTopic(topic);
             hazelcastTopic.addMessageListener(subscribers.get(topic, subscriberId));
         } finally {
             subscribersLock.readLock().unlock();
         }
     }
 
     /**
      * Remove all (or some depending on whenever you specified a topic or not) subscriptions
      * for the specified subscriberId
      * <p/>
      * {@sample.xml ../../../doc/pubsub-module.xml.sample pubsub:unsubscribe}
      *
      * @param subscriberId Identification of the subscriber, needs to be exactly the same that you used when subscribing
      * @param topic        Name of the topic
      */
     @Processor
     public void unsubscribe(String subscriberId, @Optional String topic) {
         if (validSubscriberId(subscriberId)) {
             if (topic != null) {
                 // only remove the specified topic
                 subscribersLock.writeLock().lock();
 
                 try {
                     MessageListener messageListener = subscribers.remove(topic, subscriberId);
                     ITopic hazelcastTopic = HazelcastManager.getInstance().getHazelcastInstance().getTopic(topic);
                     hazelcastTopic.removeMessageListener(messageListener);
                 } finally {
                     subscribersLock.writeLock().unlock();
                 }
             } else {
                 // remove all topics for that subscriber
                 subscribersLock.writeLock().lock();
 
                 try {
                     for (Map.Entry<String, MessageListener> entry : subscribers.column(subscriberId).entrySet()) {
                         ITopic hazelcastTopic = HazelcastManager.getInstance().getHazelcastInstance().getTopic(entry.getKey());
                         hazelcastTopic.removeMessageListener(entry.getValue());
                     }
                     subscribers.column(subscriberId).clear();
                 } finally {
                     subscribersLock.writeLock().unlock();
                 }
             }
         }
     }
 
     /**
      * Publishes an event to all registered subscribers.  This method will return
      * successfully after the event has been posted to all handlers, and
      * regardless of any exceptions thrown by handlers.
      * <p/>
      * {@sample.xml ../../../doc/pubsub-module.xml.sample pubsub:publish}
      *
      * @param topic     Name of the topic
      * @param muleEvent Mule event that will be dispatched to all registered subscribers
      * @throws WorkException Thrown when there is an error queuing the event dispatch
      */
     @Processor
     @Inject
     public void publish(String topic, MuleEvent muleEvent) throws WorkException {
         ITopic hazelcastTopic = HazelcastManager.getInstance().getHazelcastInstance().getTopic(topic);
         hazelcastTopic.publish(muleEvent);
     }
 
     private boolean validSubscriberId(String subsciberId) {
         subscribersLock.readLock().lock();
 
         try {
             return subscribers.containsColumn(subsciberId);
         } finally {
             subscribersLock.readLock().unlock();
         }
     }
 
     public void setMuleContext(MuleContext muleContext) {
         this.muleContext = muleContext;
     }
 }
