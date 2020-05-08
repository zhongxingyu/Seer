 /*
  * Copyright (C) 2010, Zenoss Inc.  All Rights Reserved.
  */
 package org.zenoss.zep.impl;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
import org.zenoss.amqp.AmqpException;
import org.zenoss.amqp.Channel;
 import org.zenoss.protobufs.zep.Zep.ZepRawEvent;
 import org.zenoss.zep.EventProcessor;
 import org.zenoss.zep.annotations.TransactionalRollbackAllExceptions;
 
 public class RawEventQueueListener extends AbstractEventQueueListener {
 
     private static final Logger logger = LoggerFactory.getLogger(RawEventQueueListener.class);
 
     private EventProcessor eventProcessor;
    
    @Override
    protected void configureChannel(Channel channel) throws AmqpException {
        // Allow more messages to accummulate to be processed since we are multi-threaded and events are typically small
        channel.setQos(0, 100);
    }
 
     @Override
     protected String getQueueIdentifier() {
         return "$ZepZenEvents";
     }
 
     public void setEventProcessor(EventProcessor eventProcessor) {
         this.eventProcessor = eventProcessor;
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public void handle(com.google.protobuf.Message message) throws Exception {
         if (!(message instanceof ZepRawEvent)) {
             logger.warn("Unexpected message type: {}", message);
         } else {
             this.eventProcessor.processEvent((ZepRawEvent) message);
         }
     }
 }
