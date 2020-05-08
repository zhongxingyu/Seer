 /*
  * #%L
  * Bitmagasin modify client
  * *
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.modify.putfile;
 
 import java.math.BigInteger;
 import java.net.URL;
 
 import javax.jms.JMSException;
 
 import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.common.ArgumentValidator;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.modify.putfile.conversation.SimplePutFileConversation;
 import org.bitrepository.protocol.conversation.FlowController;
 import org.bitrepository.protocol.eventhandler.DefaultEvent;
 import org.bitrepository.protocol.eventhandler.EventHandler;
 import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
 import org.bitrepository.protocol.exceptions.OperationFailedException;
 import org.bitrepository.protocol.mediator.ConversationMediator;
 import org.bitrepository.protocol.messagebus.MessageBus;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A simple implementation of the PutClient.
  */
 public class ConversationBasedPutFileClient implements PutFileClient {
     /** The log for this class.*/
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     /** The mediator for the conversations for the PutFileClient.*/
     private final ConversationMediator conversationMediator;
     /** The message bus for communication.*/
     private final MessageBus bus;
     /** The settings. */
     private Settings settings;
     
     /**
      * Constructor.
      * @param messageBus The messagebus for communication.
      * @param settings The configurations and settings.
      */
     public ConversationBasedPutFileClient(MessageBus messageBus, ConversationMediator conversationMediator, 
             Settings settings) {
         ArgumentValidator.checkNotNull(messageBus, "messageBus");
         ArgumentValidator.checkNotNull(settings, "settings");
         this.conversationMediator = conversationMediator;;
         this.bus = messageBus;
         this.settings = settings;
     }
     
     @Override
     public void putFile(URL url, String fileId, long sizeOfFile, ChecksumDataForFileTYPE checksumForValidationAtPillar,
             ChecksumSpecTYPE checksumRequestsForValidation, EventHandler eventHandler, String auditTrailInformation) 
                     throws OperationFailedException {
         ArgumentValidator.checkNotNull(url, "URL url");
         ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
        ArgumentValidator.checkNotNegative(sizeOfFile, "long sizeOfFile");
         // TODO add potential regex from collection settings.
         
         try {
             SimplePutFileConversation conversation = new SimplePutFileConversation(bus, settings, url, fileId, 
                     BigInteger.valueOf(sizeOfFile), checksumForValidationAtPillar, checksumRequestsForValidation, 
                     eventHandler, new FlowController(settings), auditTrailInformation);
             conversationMediator.addConversation(conversation);
             conversation.startConversation();
         } catch (Exception e) {
             String msg = "Couldn't perform put for '" + fileId + "' at '" + url + "' due to the following error: '"
                     + e.getMessage() + "'.";
             log.error(msg, e);
             eventHandler.handleEvent(new DefaultEvent(OperationEventType.FAILED, msg, "Unknown"));
         }
     }
     
     @Override
     public void shutdown() {
         try {
             bus.close();
             //TODO Consider if we can kill possible timer object too, they might be lingering somewhere in the background
         } catch (JMSException e) {
             log.info("Error during shutdown of messagebus ", e);
         }
     }
 }
