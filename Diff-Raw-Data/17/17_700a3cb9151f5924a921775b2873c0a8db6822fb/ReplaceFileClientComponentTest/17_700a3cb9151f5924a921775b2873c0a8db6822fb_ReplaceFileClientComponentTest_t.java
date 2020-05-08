 /*
  * #%L
  * Bitrepository Access Client
  * 
  * $Id: PutFileClientComponentTest.java 626 2011-12-09 13:23:52Z jolf $
  * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-modifying-client/src/test/java/org/bitrepository/modify/putfile/PutFileClientComponentTest.java $
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
 package org.bitrepository.modify.replacefile;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.net.URL;
 
 import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumType;
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositoryelements.ResponseInfo;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
 import org.bitrepository.bitrepositorymessages.ReplaceFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.ReplaceFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
 import org.bitrepository.client.DefaultFixtureClientTest;
 import org.bitrepository.client.TestEventHandler;
 import org.bitrepository.common.utils.CalendarUtils;
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
  * Tests for the components of the ReplaceFileClient.
  * TODO need more test-cases, e.g. the User-Stories...
  */
 public class ReplaceFileClientComponentTest extends DefaultFixtureClientTest {
     private TestReplaceFileMessageFactory messageFactory;
     private File testFile;
     
     @BeforeMethod(alwaysRun=true)
     public void initialise() throws Exception {
         if(useMockupPillar()) {
             messageFactory = new TestReplaceFileMessageFactory(settings.getCollectionID());
         }
 
         testFile = new File("src/test/resources/test-files/", DEFAULT_FILE_ID);
     }
 
     @Test(groups={"regressiontest"})
     public void verifyReplaceFileClientFromFactory() {
         addDescription("Testing the initialization through the ModifyComponentFactory.");
         addStep("Use the ModifyComponentFactory to instantiate a ReplaceFileClient.", 
                 "It should be an instance of ConversationBasedReplaceFileClient");
         ReplaceFileClient rfc = ModifyComponentFactory.getInstance().retrieveReplaceFileClient(settings, securityManager, 
                 TEST_CLIENT_ID);
         Assert.assertTrue(rfc instanceof ConversationBasedReplaceFileClient, "The ReplaceFileClient '" + rfc 
                 + "' should be instance of '" + ConversationBasedReplaceFileClient.class.getName() + "'");
     }
 
     @Test(groups={"regressiontest"})
     public void replaceClientTester() throws Exception {
         addDescription("Tests the ReplaceFileClient. Makes a whole conversation for the replace client for a "
                 + "'good' scenario.");
         addStep("Initialise the number of pillars to one", "Should be OK.");
         
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         ReplaceFileClient replaceClient = createReplaceFileClient();
         
         String checksumOld = "123checksum321";
         String checksumNew = "321checksum123";
         ChecksumSpecTYPE checksumForPillar = new ChecksumSpecTYPE();
         checksumForPillar.setChecksumType(ChecksumType.MD5);
         ChecksumDataForFileTYPE checksumDataOldFile = new ChecksumDataForFileTYPE();
         checksumDataOldFile.setChecksumSpec(checksumForPillar);
         checksumDataOldFile.setChecksumValue(checksumOld.getBytes());
         checksumDataOldFile.setCalculationTimestamp(CalendarUtils.getEpoch());
         ChecksumDataForFileTYPE checksumDataNewFile = new ChecksumDataForFileTYPE();
         checksumDataNewFile.setChecksumSpec(checksumForPillar);
         checksumDataNewFile.setChecksumValue(checksumNew.getBytes());
         checksumDataNewFile.setCalculationTimestamp(CalendarUtils.getNow());
         
         ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
         checksumRequest.setChecksumType(ChecksumType.SHA1);
         
         URL address = httpServer.getURL(DEFAULT_FILE_ID);
         long size = new Long(testFile.length());
         
         addStep("Request a file to be replaced on the default pillar.", 
                 "A IdentifyPillarsForReplaceFileRequest should be sent to the pillar.");
         replaceClient.replaceFile(DEFAULT_FILE_ID, PILLAR1_ID, checksumDataOldFile, checksumRequest, 
                 address, size, checksumDataNewFile, checksumRequest, testEventHandler, null);
         
         IdentifyPillarsForReplaceFileRequest receivedIdentifyRequestMessage = null;
         if(useMockupPillar()) {
             receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
                     IdentifyPillarsForReplaceFileRequest.class);
             Assert.assertEquals(receivedIdentifyRequestMessage, 
                     messageFactory.createIdentifyPillarsForReplaceFileRequest(
                             receivedIdentifyRequestMessage.getCorrelationID(),
                             receivedIdentifyRequestMessage.getReplyTo(),
                             receivedIdentifyRequestMessage.getTo(), 
                             DEFAULT_FILE_ID, null, TEST_CLIENT_ID));
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);
         
         addStep("Make response for the pillar.", "The client receive the response, identify the pillar and send the request.");
         
         ReplaceFileRequest receivedReplaceFileRequest = null;
         if(useMockupPillar()) {
             IdentifyPillarsForReplaceFileResponse identifyResponse = messageFactory.createIdentifyPillarsForReplaceFileResponse(receivedIdentifyRequestMessage, 
                     PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(identifyResponse);
             receivedReplaceFileRequest = pillar1Destination.waitForMessage(ReplaceFileRequest.class);
             Assert.assertEquals(receivedReplaceFileRequest, 
                     messageFactory.createReplaceFileRequest(PILLAR1_ID, pillar1DestinationId,
                             receivedReplaceFileRequest.getReplyTo(),
                             receivedReplaceFileRequest.getCorrelationID(),
                             address.toExternalForm(), BigInteger.valueOf(size), DEFAULT_FILE_ID, null, TEST_CLIENT_ID,
                             checksumDataOldFile, checksumDataNewFile, checksumRequest));
         }
         
         addStep("Validate the steps of the ReplaceClient by going through the events.", "Should be 'PillarIdentified', "
                 + "'PillarSelected' and 'RequestSent'");
         for(int i = 0; i < settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.REQUEST_SENT);
         
         addStep("The pillar sends a progress response to the ReplaceClient.", "Should be caught by the event handler.");
         if(useMockupPillar()) {
             ReplaceFileProgressResponse putFileProgressResponse = messageFactory.createReplaceFileProgressResponse(
                     receivedReplaceFileRequest, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(putFileProgressResponse);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PROGRESS);
         
         addStep("Send a final response message to the ReplaceClient.", 
                 "Should be caught by the event handler. First a PillarComplete, then a Complete.");
         if(useMockupPillar()) {
             ReplaceFileFinalResponse replaceFileFinalResponse = messageFactory.createReplaceFileFinalResponse(
                     receivedReplaceFileRequest, PILLAR1_ID, pillar1DestinationId, checksumDataNewFile);
             messageBus.sendMessage(replaceFileFinalResponse);
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
     public void replaceClientIdentificationTimeout() throws Exception {
         addDescription("Tests the handling of a failed identification for the ReplaceClient");
         addStep("Initialise the number of pillars and the DeleteClient. Sets the identification timeout to 1 sec.", 
                 "Should be OK.");
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         settings.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(1000L));
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         ReplaceFileClient replaceClient = createReplaceFileClient();
         
         String checksumOld = "123checksum321";
         String checksumNew = "321checksum123";
         ChecksumSpecTYPE checksumForPillar = new ChecksumSpecTYPE();
         checksumForPillar.setChecksumType(ChecksumType.MD5);
         ChecksumDataForFileTYPE checksumDataOldFile = new ChecksumDataForFileTYPE();
         checksumDataOldFile.setChecksumSpec(checksumForPillar);
         checksumDataOldFile.setChecksumValue(checksumOld.getBytes());
         checksumDataOldFile.setCalculationTimestamp(CalendarUtils.getEpoch());
         ChecksumDataForFileTYPE checksumDataNewFile = new ChecksumDataForFileTYPE();
         checksumDataNewFile.setChecksumSpec(checksumForPillar);
         checksumDataNewFile.setChecksumValue(checksumNew.getBytes());
         checksumDataNewFile.setCalculationTimestamp(CalendarUtils.getNow());
         
         ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
         checksumRequest.setChecksumType(ChecksumType.SHA1);
         
         URL address = httpServer.getURL(DEFAULT_FILE_ID);
         long size = new Long(testFile.length());
         
         addStep("Request a file to be replaced on the default pillar.", 
                 "A IdentifyPillarsForReplaceFileRequest should be sent to the pillar.");
         replaceClient.replaceFile(DEFAULT_FILE_ID, PILLAR1_ID, checksumDataOldFile, checksumRequest, 
                 address, size, checksumDataNewFile, checksumRequest, testEventHandler, null);
         
         IdentifyPillarsForReplaceFileRequest receivedIdentifyRequestMessage = null;
         if(useMockupPillar()) {
             receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
                     IdentifyPillarsForReplaceFileRequest.class);
             Assert.assertEquals(receivedIdentifyRequestMessage, 
                     messageFactory.createIdentifyPillarsForReplaceFileRequest(
                             receivedIdentifyRequestMessage.getCorrelationID(),
                             receivedIdentifyRequestMessage.getReplyTo(),
                             receivedIdentifyRequestMessage.getTo(), 
                             DEFAULT_FILE_ID, null, TEST_CLIENT_ID));
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);
         
         addStep("Do not respond. Just await the timeout.", 
                 "Should make send a Failure event to the eventhandler.");
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_TIMEOUT);        
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.FAILED);        
     }
 
     @Test(groups={"regressiontest"})
     public void replaceClientOperationTimeout() throws Exception {
         addDescription("Tests the handling of a failed operation for the ReplaceClient");
         addStep("Initialise the number of pillars and the DeleteClient. Sets the operation timeout to 1 sec.", 
                 "Should be OK.");
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         settings.getCollectionSettings().getClientSettings().setOperationTimeout(BigInteger.valueOf(1000L));
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         ReplaceFileClient replaceClient = createReplaceFileClient();
         
         String checksumOld = "123checksum321";
         String checksumNew = "321checksum123";
         ChecksumSpecTYPE checksumForPillar = new ChecksumSpecTYPE();
         checksumForPillar.setChecksumType(ChecksumType.MD5);
         ChecksumDataForFileTYPE checksumDataOldFile = new ChecksumDataForFileTYPE();
         checksumDataOldFile.setChecksumSpec(checksumForPillar);
         checksumDataOldFile.setChecksumValue(checksumOld.getBytes());
         checksumDataOldFile.setCalculationTimestamp(CalendarUtils.getEpoch());
         ChecksumDataForFileTYPE checksumDataNewFile = new ChecksumDataForFileTYPE();
         checksumDataNewFile.setChecksumSpec(checksumForPillar);
         checksumDataNewFile.setChecksumValue(checksumNew.getBytes());
         checksumDataNewFile.setCalculationTimestamp(CalendarUtils.getNow());
         
         ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
         checksumRequest.setChecksumType(ChecksumType.SHA1);
         
         URL address = httpServer.getURL(DEFAULT_FILE_ID);
         long size = new Long(testFile.length());
         
         addStep("Request a file to be replaced on the default pillar.", 
                 "A IdentifyPillarsForReplaceFileRequest should be sent to the pillar.");
         replaceClient.replaceFile(DEFAULT_FILE_ID, PILLAR1_ID, checksumDataOldFile, checksumRequest, 
                 address, size, checksumDataNewFile, checksumRequest, testEventHandler, null);
         
         IdentifyPillarsForReplaceFileRequest receivedIdentifyRequestMessage = null;
         if(useMockupPillar()) {
             receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
                     IdentifyPillarsForReplaceFileRequest.class);
             Assert.assertEquals(receivedIdentifyRequestMessage, 
                     messageFactory.createIdentifyPillarsForReplaceFileRequest(
                             receivedIdentifyRequestMessage.getCorrelationID(),
                             receivedIdentifyRequestMessage.getReplyTo(),
                             receivedIdentifyRequestMessage.getTo(), 
                             DEFAULT_FILE_ID, null, TEST_CLIENT_ID));
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);
         
         addStep("Make response for the pillar.", "The client receive the response, identify the pillar and send the request.");
         
         ReplaceFileRequest receivedReplaceFileRequest = null;
         if(useMockupPillar()) {
             IdentifyPillarsForReplaceFileResponse identifyResponse = messageFactory.createIdentifyPillarsForReplaceFileResponse(receivedIdentifyRequestMessage, 
                     PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(identifyResponse);
             receivedReplaceFileRequest = pillar1Destination.waitForMessage(ReplaceFileRequest.class);
             Assert.assertEquals(receivedReplaceFileRequest, 
                     messageFactory.createReplaceFileRequest(PILLAR1_ID, pillar1DestinationId,
                             receivedReplaceFileRequest.getReplyTo(),
                             receivedReplaceFileRequest.getCorrelationID(),
                             address.toExternalForm(), BigInteger.valueOf(size), DEFAULT_FILE_ID, null, TEST_CLIENT_ID,
                             checksumDataOldFile, checksumDataNewFile, checksumRequest));
         }
         
         addStep("Validate the steps of the ReplaceClient by going through the events.", "Should be 'PillarIdentified', "
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
     public void replaceClientPillarFailed() throws Exception {
         addDescription("Tests the handling of a operation failure for the ReplaceClient. ");
         addStep("Initialise the number of pillars to one", "Should be OK.");
         
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         ReplaceFileClient replaceClient = createReplaceFileClient();
         
         String checksumOld = "123checksum321";
         String checksumNew = "321checksum123";
         ChecksumSpecTYPE checksumForPillar = new ChecksumSpecTYPE();
         checksumForPillar.setChecksumType(ChecksumType.MD5);
         ChecksumDataForFileTYPE checksumDataOldFile = new ChecksumDataForFileTYPE();
         checksumDataOldFile.setChecksumSpec(checksumForPillar);
         checksumDataOldFile.setChecksumValue(checksumOld.getBytes());
         checksumDataOldFile.setCalculationTimestamp(CalendarUtils.getEpoch());
         ChecksumDataForFileTYPE checksumDataNewFile = new ChecksumDataForFileTYPE();
         checksumDataNewFile.setChecksumSpec(checksumForPillar);
         checksumDataNewFile.setChecksumValue(checksumNew.getBytes());
         checksumDataNewFile.setCalculationTimestamp(CalendarUtils.getNow());
         
         ChecksumSpecTYPE checksumRequest = new ChecksumSpecTYPE();
         checksumRequest.setChecksumType(ChecksumType.SHA1);
         
         URL address = httpServer.getURL(DEFAULT_FILE_ID);
         long size = new Long(testFile.length());
         
         addStep("Request a file to be replaced on the default pillar.", 
                 "A IdentifyPillarsForReplaceFileRequest should be sent to the pillar.");
         replaceClient.replaceFile(DEFAULT_FILE_ID, PILLAR1_ID, checksumDataOldFile, checksumRequest, 
                 address, size, checksumDataNewFile, checksumRequest, testEventHandler, null);
         
         IdentifyPillarsForReplaceFileRequest receivedIdentifyRequestMessage = null;
         if(useMockupPillar()) {
             receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
                     IdentifyPillarsForReplaceFileRequest.class);
             Assert.assertEquals(receivedIdentifyRequestMessage, 
                     messageFactory.createIdentifyPillarsForReplaceFileRequest(
                             receivedIdentifyRequestMessage.getCorrelationID(),
                             receivedIdentifyRequestMessage.getReplyTo(),
                             receivedIdentifyRequestMessage.getTo(), 
                             DEFAULT_FILE_ID, null, TEST_CLIENT_ID));
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);
         
         addStep("Make response for the pillar.", "The client receive the response, identify the pillar and send the request.");
         
         ReplaceFileRequest receivedReplaceFileRequest = null;
         if(useMockupPillar()) {
             IdentifyPillarsForReplaceFileResponse identifyResponse = messageFactory.createIdentifyPillarsForReplaceFileResponse(receivedIdentifyRequestMessage, 
                     PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(identifyResponse);
             receivedReplaceFileRequest = pillar1Destination.waitForMessage(ReplaceFileRequest.class);
             Assert.assertEquals(receivedReplaceFileRequest, 
                     messageFactory.createReplaceFileRequest(PILLAR1_ID, pillar1DestinationId,
                             receivedReplaceFileRequest.getReplyTo(),
                             receivedReplaceFileRequest.getCorrelationID(),
                             address.toExternalForm(), BigInteger.valueOf(size), DEFAULT_FILE_ID, null, TEST_CLIENT_ID,
                             checksumDataOldFile, checksumDataNewFile, checksumRequest));
         }
         
         addStep("Validate the steps of the ReplaceClient by going through the events.", "Should be 'PillarIdentified', "
                 + "'PillarSelected' and 'RequestSent'");
         for(int i = 0; i < settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.REQUEST_SENT);
 
         addStep("Send a failed response message to the ReplaceClient.", 
                 "Should be caught by the event handler. First a PillarFailed, then a Complete.");
         if(useMockupPillar()) {
             ReplaceFileFinalResponse replaceFileFinalResponse = messageFactory.createReplaceFileFinalResponse(
                     receivedReplaceFileRequest, PILLAR1_ID, pillar1DestinationId, checksumDataNewFile);
             ResponseInfo ri = new ResponseInfo();
             ri.setResponseCode(ResponseCode.FAILURE);
             ri.setResponseText("Verifying that a failure can be understood!");
             replaceFileFinalResponse.setResponseInfo(ri);
             messageBus.sendMessage(replaceFileFinalResponse);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_FAILED);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPLETE);
     }
     
     /**
      * Creates a new test PutFileClient based on the supplied settings. 
      * 
      * Note that the normal way of creating client through the module factory would reuse components with settings from
      * previous tests.
      * @return A new PutFileClient(Wrapper).
      */
     private ReplaceFileClient createReplaceFileClient() {
         MessageBus messageBus = new ActiveMQMessageBus(settings.getMessageBusConfiguration(), securityManager);
         ConversationMediator conversationMediator = new CollectionBasedConversationMediator(settings, securityManager);
         return new ReplaceClientTestWrapper(new ConversationBasedReplaceFileClient(
                 messageBus, conversationMediator, settings, TEST_CLIENT_ID), testEventManager);
     }
 }
