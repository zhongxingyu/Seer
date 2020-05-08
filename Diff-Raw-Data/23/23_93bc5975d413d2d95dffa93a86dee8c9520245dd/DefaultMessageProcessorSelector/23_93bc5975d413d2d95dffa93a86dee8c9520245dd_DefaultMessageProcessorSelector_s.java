 /**
  * This file is part of Project Control Center (PCC).
  * 
  * PCC (Project Control Center) project is intellectual property of 
  * Dmitri Anatol'evich Pisarenko.
  * 
  * Copyright 2010, 2011 Dmitri Anatol'evich Pisarenko
  * All rights reserved
  *
  **/
 
 package co.altruix.pcc.impl.messageprocessorselector;
 
 import com.google.inject.Injector;
 
 import ru.altruix.commons.api.di.PccException;
import co.altruix.pcc.api.cdm.ImmediateSchedulingRequest;
 import co.altruix.pcc.api.cdm.PccMessage;
 import co.altruix.pcc.api.immediatereschedulingrequestprocessor.ImmediateSchedulingRequestMessageProcessor;
 import co.altruix.pcc.api.immediatereschedulingrequestprocessor.ImmediateSchedulingRequestMessageProcessorFactory;
 import co.altruix.pcc.api.messageprocessor.MessageProcessor;
 import co.altruix.pcc.api.messageprocessorselector.MessageProcessorSelector;
 
 /**
  * @author DP118M
  * 
  */
 class DefaultMessageProcessorSelector implements MessageProcessorSelector {
     private PccMessage message;
     private MessageProcessor processor;
     private ImmediateSchedulingRequestMessageProcessor immediateSchedulingRequestMessageProcessor;
 
     public void run() throws PccException {
         if (this.message == null) {
             this.processor = null;
        } else if (this.message instanceof ImmediateSchedulingRequest) {
             this.processor = this.immediateSchedulingRequestMessageProcessor;
         } else {
             this.processor = null;
         }
     }
 
     public void setMessage(final PccMessage aMessage) {
         this.message = aMessage;
     }
 
     public MessageProcessor getMessageProcessor() {
         return this.processor;
     }
 
     public void setInjector(final Injector aInjector) {
         if (aInjector != null) {
             final ImmediateSchedulingRequestMessageProcessorFactory factory =
                     aInjector
                             .getInstance(ImmediateSchedulingRequestMessageProcessorFactory.class);
             processor = factory.create();
         }
     }
 }
