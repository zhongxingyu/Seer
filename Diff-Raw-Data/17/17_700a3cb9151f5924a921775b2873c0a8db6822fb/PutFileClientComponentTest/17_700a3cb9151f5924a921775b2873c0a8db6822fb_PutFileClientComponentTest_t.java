 /*
  * #%L
  * Bitrepository Access Client
  * 
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
 
 import java.io.File;
 import java.math.BigInteger;
 import java.util.concurrent.TimeUnit;
 
 import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositoryelements.ResponseInfo;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
 import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.PutFileRequest;
 import org.bitrepository.client.DefaultFixtureClientTest;
 import org.bitrepository.client.TestEventHandler;
 import org.bitrepository.modify.ModifyComponentFactory;
 import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
 import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
 import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
 import org.bitrepository.client.conversation.mediator.ConversationMediator;
 import org.bitrepository.protocol.messagebus.MessageBus;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * Tests for the components of the PutFileClient.
  * TODO need more test-cases, e.g. the User-Stories...
  */
 public class PutFileClientComponentTest extends DefaultFixtureClientTest {
     private TestPutFileMessageFactory messageFactory;
     private File testFile;
     private long fileSize;
     
     @BeforeMethod(alwaysRun=true)
     public void initialise() throws Exception {
         if(useMockupPillar()) {
             messageFactory = new TestPutFileMessageFactory(settings.getCollectionID());
         }
 
         testFile = new File("src/test/resources/test-files/", DEFAULT_FILE_ID);
         fileSize = testFile.length();
     }
 
     @Test(groups={"regressiontest"})
     public void verifyPutClientFromFactory() {
         addDescription("Testing the initialization through the ModifyComponentFactory.");
         addStep("Use the ModifyComponentFactory to instantiate a PutFileClient.", 
                 "It should be an instance of SimplePutFileClient");
         PutFileClient pfc = ModifyComponentFactory.getInstance().retrievePutClient(settings, securityManager, TEST_CLIENT_ID);
         Assert.assertTrue(pfc instanceof ConversationBasedPutFileClient, "The PutFileClient '" + pfc + "' should be instance of '" 
                 + ConversationBasedPutFileClient.class.getName() + "'");
     }
 
     @Test(groups={"regressiontest"})
     public void putClientTester() throws Exception {
         addDescription("Tests the PutClient. Makes a whole conversation for the put client for a 'good' scenario.");
         addStep("Initialise the number of pillars to one", "Should be OK.");
 
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
 
         addStep("Ensure that the test-file is placed on the HTTP server.", "Should be removed an reuploaded.");
 
         addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.", 
                 "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, fileSize, 
                 (ChecksumDataForFileTYPE) null, (ChecksumSpecTYPE) null, testEventHandler, "TEST-AUDIT-TRAIL");
 
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage = null;
         if(useMockupPillar()) {
             receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
                     IdentifyPillarsForPutFileRequest.class);
             Assert.assertEquals(receivedIdentifyRequestMessage, 
                     messageFactory.createIdentifyPillarsForPutFileRequest(
                             receivedIdentifyRequestMessage.getCorrelationID(),
                             receivedIdentifyRequestMessage.getReplyTo(),
                             receivedIdentifyRequestMessage.getTo(),
                             DEFAULT_FILE_ID,
                             fileSize,
                             receivedIdentifyRequestMessage.getAuditTrailInformation(),
                             TEST_CLIENT_ID
                             ));
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Make response for the pillar.", "The client should then send the actual PutFileRequest.");
 
         PutFileRequest receivedPutFileRequest = null;
         if(useMockupPillar()) {
             IdentifyPillarsForPutFileResponse identifyResponse = messageFactory
                     .createIdentifyPillarsForPutFileResponse(
                             receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(identifyResponse);
             receivedPutFileRequest = pillar1Destination.waitForMessage(PutFileRequest.class, 10, TimeUnit.SECONDS);
             Assert.assertEquals(receivedPutFileRequest, 
                     messageFactory.createPutFileRequest(
                             PILLAR1_ID, pillar1DestinationId,
                             receivedPutFileRequest.getReplyTo(),
                             receivedPutFileRequest.getCorrelationID(),
                             receivedPutFileRequest.getFileAddress(),
                             receivedPutFileRequest.getFileSize(),
                             DEFAULT_FILE_ID,
                             receivedPutFileRequest.getAuditTrailInformation(),
                             TEST_CLIENT_ID
                             ));
         }
 
         addStep("Validate the steps of the PutClient by going through the events.", "Should be 'PillarIdentified', "
                 + "'PillarSelected' and 'RequestSent'");
         for(int i = 0; i < settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.REQUEST_SENT);
 
         addStep("The pillar sends a progress response to the PutClient.", "Should be caught by the event handler.");
         if(useMockupPillar()) {
             PutFileProgressResponse putFileProgressResponse = messageFactory.createPutFileProgressResponse(
                     receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(putFileProgressResponse);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PROGRESS);
 
         addStep("Send a final response message to the PutClient.", 
                 "Should be caught by the event handler. First a PartiallyComplete, then a Complete.");
         if(useMockupPillar()) {
             PutFileFinalResponse putFileFinalResponse = messageFactory.createPutFileFinalResponse(
                     receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(putFileFinalResponse);
         }
         for(int i = 1; i < 2* settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             OperationEventType eventType = testEventHandler.waitForEvent().getType();
             Assert.assertTrue( (eventType == OperationEventType.COMPONENT_COMPLETE)
                     || (eventType == OperationEventType.PROGRESS),
                     "Expected either PartiallyComplete or Progress, but was: " + eventType);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPLETE);
     }
     
     @Test(groups={"regressiontest"})
     public void putClientIdentificationTimeout() throws Exception {
         addDescription("Tests the handling of a failed identification for the PutClient");
         addStep("Initialise the number of pillars and the PutClient. Sets the identification timeout to 1 sec.", 
                 "Should be OK.");
 
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         settings.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(1000L));
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
         
         addStep("Request the putting of a file through the PutClient", 
                 "The identification message should be sent.");
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, fileSize, null, 
                 null, testEventHandler, null);
         
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage = null;
         if(useMockupPillar()) {
             receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
                     IdentifyPillarsForPutFileRequest.class);
             Assert.assertEquals(receivedIdentifyRequestMessage, 
                     messageFactory.createIdentifyPillarsForPutFileRequest(
                             receivedIdentifyRequestMessage.getCorrelationID(),
                             receivedIdentifyRequestMessage.getReplyTo(),
                             receivedIdentifyRequestMessage.getTo(),
                             DEFAULT_FILE_ID,
                             fileSize, 
                             receivedIdentifyRequestMessage.getAuditTrailInformation(),
                             TEST_CLIENT_ID
                             ));
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Do not respond. Just await the timeout.", 
                 "Should make send a Failure event to the eventhandler.");
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_TIMEOUT);        
     }
 
     @Test(groups={"regressiontest"})
     public void putClientOperationTimeout() throws Exception {
         addDescription("Tests the handling of a failed operation for the PutClient");
         addStep("Initialise the number of pillars and the PutClient. Sets the operation timeout to 1 sec.", 
                 "Should be OK.");
 
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         settings.getCollectionSettings().getClientSettings().setOperationTimeout(BigInteger.valueOf(1000L));
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
         
         addStep("Request the putting of a file through the PutClient", 
                 "The identification message should be sent.");
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, fileSize, null, 
                 null, testEventHandler, null);
         
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage = null;
         if(useMockupPillar()) {
             receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
                     IdentifyPillarsForPutFileRequest.class);
             Assert.assertEquals(receivedIdentifyRequestMessage, 
                     messageFactory.createIdentifyPillarsForPutFileRequest(
                             receivedIdentifyRequestMessage.getCorrelationID(),
                             receivedIdentifyRequestMessage.getReplyTo(),
                             receivedIdentifyRequestMessage.getTo(),
                             DEFAULT_FILE_ID,
                             fileSize,
                             receivedIdentifyRequestMessage.getAuditTrailInformation(),
                             TEST_CLIENT_ID
                             ));
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Make response for the pillar.", "The client should then send the actual PutFileRequest.");
         PutFileRequest receivedPutFileRequest = null;
         if(useMockupPillar()) {
             IdentifyPillarsForPutFileResponse identifyResponse = messageFactory
                     .createIdentifyPillarsForPutFileResponse(
                             receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(identifyResponse);
             receivedPutFileRequest = pillar1Destination.waitForMessage(PutFileRequest.class, 10, TimeUnit.SECONDS);
             Assert.assertEquals(receivedPutFileRequest, 
                     messageFactory.createPutFileRequest(
                             PILLAR1_ID, pillar1DestinationId,
                             receivedPutFileRequest.getReplyTo(),
                             receivedPutFileRequest.getCorrelationID(),
                             receivedPutFileRequest.getFileAddress(),
                             receivedPutFileRequest.getFileSize(),
                             DEFAULT_FILE_ID,
                             receivedPutFileRequest.getAuditTrailInformation(),
                             TEST_CLIENT_ID
                             ));
         }
 
         addStep("Validate the steps of the PutClient by going through the events.", "Should be 'PillarIdentified', "
                 + "'PillarSelected' and 'RequestSent'");
         for(int i = 0; i < settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.REQUEST_SENT);
 
         addStep("Do not respond. Just await the timeout.", 
                 "Should make send a Failure event to the eventhandler.");
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.FAILED);        
     }
     
     @Test(groups={"regressiontest"})
     public void putClientPillarOperationFailed() throws Exception {
         addDescription("Tests the handling of a operation failure for the PutClient. ");
         addStep("Initialise the number of pillars to one", "Should be OK.");
 
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
         addStep("Ensure that the test-file is placed on the HTTP server.", "Should be removed an reuploaded.");
 
         addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.", 
                 "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, fileSize, 
                 (ChecksumDataForFileTYPE) null, (ChecksumSpecTYPE) null, testEventHandler, "TEST-AUDIT-TRAIL");
 
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage = null;
         if(useMockupPillar()) {
             receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
                     IdentifyPillarsForPutFileRequest.class);
             Assert.assertEquals(receivedIdentifyRequestMessage, 
                     messageFactory.createIdentifyPillarsForPutFileRequest(
                             receivedIdentifyRequestMessage.getCorrelationID(),
                             receivedIdentifyRequestMessage.getReplyTo(),
                             receivedIdentifyRequestMessage.getTo(),
                             DEFAULT_FILE_ID,
                             fileSize,
                             receivedIdentifyRequestMessage.getAuditTrailInformation(),
                             TEST_CLIENT_ID
                             ));
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Make response for the pillar.", "The client should then send the actual PutFileRequest.");
 
         PutFileRequest receivedPutFileRequest = null;
         if(useMockupPillar()) {
             IdentifyPillarsForPutFileResponse identifyResponse = messageFactory
                     .createIdentifyPillarsForPutFileResponse(
                             receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(identifyResponse);
             receivedPutFileRequest = pillar1Destination.waitForMessage(PutFileRequest.class, 10, TimeUnit.SECONDS);
             Assert.assertEquals(receivedPutFileRequest, 
                     messageFactory.createPutFileRequest(
                             PILLAR1_ID, pillar1DestinationId,
                             receivedPutFileRequest.getReplyTo(),
                             receivedPutFileRequest.getCorrelationID(),
                             receivedPutFileRequest.getFileAddress(),
                             receivedPutFileRequest.getFileSize(),
                             DEFAULT_FILE_ID,
                             receivedPutFileRequest.getAuditTrailInformation(),
                             TEST_CLIENT_ID
                             ));
         }
 
         addStep("Validate the steps of the PutClient by going through the events.", "Should be 'PillarIdentified', "
                 + "'PillarSelected' and 'RequestSent'");
         for(int i = 0; i < settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.REQUEST_SENT);
 
         addStep("Send a failed response message to the PutClient.", 
                 "Should be caught by the event handler. First a PillarFailed, then a Complete.");
         if(useMockupPillar()) {
             PutFileFinalResponse putFileFinalResponse = messageFactory.createPutFileFinalResponse(
                     receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
             ResponseInfo ri = new ResponseInfo();
             ri.setResponseCode(ResponseCode.FAILURE);
             ri.setResponseText("Verifying that a failure can be understood!");
             putFileFinalResponse.setResponseInfo(ri);
             messageBus.sendMessage(putFileFinalResponse);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_FAILED);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPLETE);
     }
     
     @Test(groups={"regressiontest"})
     public void putClientPillarIdentificationFailed() throws Exception {
         addDescription("Tests the handling of a identification failure for the PutClient. ");
         addStep("Initialise the number of pillars to one", "Should be OK.");
 
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
         addStep("Ensure that the test-file is placed on the HTTP server.", "Should be removed an reuploaded.");
 
         addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.", 
                 "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, fileSize, 
                 (ChecksumDataForFileTYPE) null, (ChecksumSpecTYPE) null, testEventHandler, "TEST-AUDIT-TRAIL");
 
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage = null;
         if(useMockupPillar()) {
             receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
                     IdentifyPillarsForPutFileRequest.class);
             Assert.assertEquals(receivedIdentifyRequestMessage, 
                     messageFactory.createIdentifyPillarsForPutFileRequest(
                             receivedIdentifyRequestMessage.getCorrelationID(),
                             receivedIdentifyRequestMessage.getReplyTo(),
                             receivedIdentifyRequestMessage.getTo(),
                             DEFAULT_FILE_ID,
                             fileSize,
                             receivedIdentifyRequestMessage.getAuditTrailInformation(),
                             TEST_CLIENT_ID
                             ));
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Make bad identification response for the pillar.", "The client should handle the bad identification.");
 
         if(useMockupPillar()) {
             IdentifyPillarsForPutFileResponse identifyResponse = messageFactory
                     .createIdentifyPillarsForPutFileResponse(
                             receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
             
             // Set to failed
             ResponseInfo ri = new ResponseInfo();
             ri.setResponseCode(ResponseCode.DUPLICATE_FILE_FAILURE);
             ri.setResponseText("Testing the handling of 'DUPLICATE FILE' identification.");
             identifyResponse.setResponseInfo(ri);
             
             messageBus.sendMessage(identifyResponse);
         }
         
         addStep("The client handling the bad identification.", "Should go through the states '"
                 + OperationEventType.COMPONENT_FAILED + "', '" + OperationEventType.IDENTIFICATION_COMPLETE + "', '"
                 + OperationEventType.WARNING + "', '" + OperationEventType.COMPLETE + "'");
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_FAILED);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.FAILED);
         /*Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.WARNING);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPLETE);*/
     }
 
     /**
      * Creates a new test PutFileClient based on the supplied settings. 
      * 
      * Note that the normal way of creating client through the module factory would reuse components with settings from
      * previous tests.
      * @return A new PutFileClient(Wrapper).
      */
     private PutFileClient createPutFileClient() {
         MessageBus messageBus = new ActiveMQMessageBus(settings.getMessageBusConfiguration(), securityManager);
         ConversationMediator conversationMediator = new CollectionBasedConversationMediator(settings, securityManager);
         return new PutClientTestWrapper(new ConversationBasedPutFileClient(
                 messageBus, conversationMediator, settings, TEST_CLIENT_ID)
         , testEventManager);
     }
 }
