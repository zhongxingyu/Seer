 /*
  * #%L
  * Bitrepository Access
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
 /*
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
 package org.bitrepository.access.getchecksums;
 
 import java.math.BigInteger;
 import java.net.URL;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.concurrent.TimeUnit;
 import org.bitrepository.access.AccessComponentFactory;
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumType;
 import org.bitrepository.bitrepositoryelements.FileIDs;
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositoryelements.ResponseInfo;
 import org.bitrepository.bitrepositoryelements.ResultingChecksums;
 import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
 import org.bitrepository.client.DefaultFixtureClientTest;
 import org.bitrepository.client.TestEventHandler;
 import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.utils.FileIDsUtils;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * Test class for the 'GetFileClient'.
  */
 public class GetChecksumsClientComponentTest extends DefaultFixtureClientTest {
     private TestGetChecksumsMessageFactory testMessageFactory;
 
     private static final ChecksumSpecTYPE DEFAULT_CHECKSUM_SPECS;
     static {
         DEFAULT_CHECKSUM_SPECS = new ChecksumSpecTYPE();
         DEFAULT_CHECKSUM_SPECS.setChecksumSalt(null);
         DEFAULT_CHECKSUM_SPECS.setChecksumType(ChecksumType.MD5);
     }
 
     @BeforeMethod (alwaysRun=true)
     public void beforeMethodSetup() throws Exception {
        testMessageFactory = new TestGetChecksumsMessageFactory(componentSettings.getCollectionID());
     }
 
     @Test(groups = {"regressiontest"})
     public void verifyGetChecksumsClientFromFactory() throws Exception {
         Assert.assertTrue(AccessComponentFactory.getInstance().createGetChecksumsClient(componentSettings, securityManager,
                 TEST_CLIENT_ID) instanceof ConversationBasedGetChecksumsClient,
                 "The default GetFileClient from the Access factory should be of the type '" +
                         ConversationBasedGetChecksumsClient.class.getName() + "'.");
     }
 
     @Test(groups = {"regressiontest"})
     public void getChecksumsFromSinglePillar() throws Exception {
         addDescription("Tests that the client can retrieve checksums from a single pillar.");
        FileIDs fileIDs = FileIDsUtils.createFileIDs(DEFAULT_FILE_ID);
 
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         GetChecksumsClient getChecksumsClient = createGetCheckSumsClient();
 
         addStep("Request the delivery of the checksum of a file from pillar1.",
                 "A IdentifyPillarsForGetChecksumsRequest will be sent to the pillars and a IDENTIFY_REQUEST_SENT event" +
                         "should be generated.");
         Collection<String> pillar1AsCollection = new LinkedList<String>();
         pillar1AsCollection.add(PILLAR1_ID);
        getChecksumsClient.getChecksums(pillar1AsCollection, fileIDs,
                 DEFAULT_CHECKSUM_SPECS, null, testEventHandler, "TEST-AUDIT");
 
         IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                 IdentifyPillarsForGetChecksumsRequest.class);
        Assert.assertEquals(receivedIdentifyRequestMessage.getFileIDs(), fileIDs);
        Assert.assertEquals(receivedIdentifyRequestMessage.getChecksumRequestForExistingFile(), DEFAULT_CHECKSUM_SPECS);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Sends a response from pillar2.",
                 "This should be ignored.");
         IdentifyPillarsForGetChecksumsResponse identifyResponse2 = testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                 receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
         messageBus.sendMessage(identifyResponse2);
         testEventHandler.verifyNoEventsAreReceived();
 
         addStep("Sends a response from pillar1.",
                 "A getChecksumRequest should be sendt to pillar1 and the following events should be received: " +
                         "COMPONENT_IDENTIFIED, IDENTIFICATION_COMPLETE and REQUEST_SENT.");
 
         IdentifyPillarsForGetChecksumsResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(identifyResponse);
         GetChecksumsRequest receivedGetChecksumsRequest = pillar1Destination.waitForMessage(GetChecksumsRequest.class);
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
 
         addStep("Send a GetChecksumsFinalResponse to the client from pillar1",
                 "A COMPONENT_COMPLETE event should be generated with the resulting checksum. Finally a COMPLETE event" +
                         "should be received.");
         GetChecksumsFinalResponse completeMsg = testMessageFactory.createGetChecksumsFinalResponse(
                 receivedGetChecksumsRequest, PILLAR1_ID, pillar1DestinationId);
 
         ResultingChecksums res = new ResultingChecksums();
         res.setResultAddress(receivedGetChecksumsRequest.getResultAddress());
         completeMsg.setResultingChecksums(res);
 
         messageBus.sendMessage(completeMsg);
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
     }
 
     @Test(groups = {"regressiontest"})
     public void getChecksumsDeliveredAtUrl() throws Exception {
         addDescription("Tests the delivery of checksums from all pillars at a given URL.");
 
         String deliveryFilename = "TEST-CHECKSUM-DELIVERY.xml";
         FileIDs fileIDs = new FileIDs();
         fileIDs.setFileID(DEFAULT_FILE_ID);
 
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         GetChecksumsClient getChecksumsClient = createGetCheckSumsClient();
 
         addStep("Ensure the delivery file isn't already present on the http server",
                 "Should be remove if it already exists.");
         URL deliveryUrl = httpServer.getURL(deliveryFilename);
 
         addStep("Request the delivery of the checksum of a file from all pillars.",
                 "A IdentifyPillarsForGetChecksumsRequest should be sent and a IDENTIFY_REQUEST_SENT should be generated.");
         getChecksumsClient.getChecksums(null, fileIDs, null, deliveryUrl, testEventHandler, "TEST-AUDIT");
 
         IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage = null;
         receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                 IdentifyPillarsForGetChecksumsRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("The pillar sends a response to the identify message.",
                 "The callback listener should notify of the response and the client should send a GetChecksumsRequest "
                         + "message to the pillar");
 
         IdentifyPillarsForGetChecksumsResponse identifyResponse1 = testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(identifyResponse1);
         IdentifyPillarsForGetChecksumsResponse identifyResponse2 = testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                 receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
         messageBus.sendMessage(identifyResponse2);
         GetChecksumsRequest receivedGetChecksumsRequest1 = pillar1Destination.waitForMessage(GetChecksumsRequest.class);
         GetChecksumsRequest receivedGetChecksumsRequest2 = pillar2Destination.waitForMessage(GetChecksumsRequest.class);
 
         for(int i = 0; i < componentSettings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
 
         addStep("Sends a final response from each pillar",
                 "The GetChecksumsClient notifies that the file is ready through the callback listener and the uploaded file is present.");
         GetChecksumsFinalResponse completeMsg1 = testMessageFactory.createGetChecksumsFinalResponse(
                 receivedGetChecksumsRequest1, PILLAR1_ID, pillar1DestinationId);
         ResultingChecksums res = new ResultingChecksums();
         res.setResultAddress(receivedGetChecksumsRequest1.getResultAddress());
         completeMsg1.setResultingChecksums(res);
         messageBus.sendMessage(completeMsg1);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
 
         GetChecksumsFinalResponse completeMsg2 = testMessageFactory.createGetChecksumsFinalResponse(
                 receivedGetChecksumsRequest1, PILLAR2_ID, pillar2DestinationId);
         completeMsg2.setResultingChecksums(res);
         messageBus.sendMessage(completeMsg2);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
     }
 
     @Test(groups = {"regressiontest"})
     public void noIdentifyResponses() throws Exception {
         addDescription("Tests the GetChecksumsClient handles lack of IdentifyPillarResponses gracefully.");
         addStep("Define a 3 second timeout for identifying pillar.", "");
 
         componentSettings.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000));
 
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         GetChecksumsClient getChecksumsClient = createGetCheckSumsClient();
 
         FileIDs fileIDs = new FileIDs();
         fileIDs.setFileID(DEFAULT_FILE_ID);
 
         addStep("Call the getChecksums method.",
                 "A IdentifyPillarsForGetChecksumsRequest will be sent and a IDENTIFY_REQUEST_SENT should be received");
         getChecksumsClient.getChecksums(null, fileIDs,
                 DEFAULT_CHECKSUM_SPECS, null, testEventHandler, "TEST-AUDIT");
         collectionReceiver.waitForMessage(IdentifyPillarsForGetChecksumsRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Wait for at least 3 seconds", "An IdentifyPillarTimeout event should be received");
         Assert.assertEquals(testEventHandler.waitForEvent( 4, TimeUnit.SECONDS).getEventType(),
                 OperationEventType.IDENTIFY_TIMEOUT);
     }
 
     @Test(groups = {"regressiontest"})
     public void conversationTimeout() throws Exception {
         addDescription("Tests the GetChecksumClient handles lack of GetChecksumsResponses gracefully");
         addStep("Set a 3 second timeout for the conversation.", "");
 
         FileIDs fileIDs = new FileIDs();
         fileIDs.setFileID(DEFAULT_FILE_ID);
 
         componentSettings.getCollectionSettings().getClientSettings().setOperationTimeout(BigInteger.valueOf(3000));
 
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         GetChecksumsClient getChecksumsClient = createGetCheckSumsClient();
 
         addStep("Ensure the delivery file isn't already present on the http server",
                 "Should be remove if it already exists.");
 
         addStep("Request the delivery of the checksum of a file from the pillar(s). A callback listener should be supplied.",
                 "A IdentifyPillarsForGetChecksumsRequest will be sent to the pillar(s).");
         getChecksumsClient.getChecksums(null, fileIDs, null, null, testEventHandler, "TEST-AUDIT");
 
         IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                 IdentifyPillarsForGetChecksumsRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("The pillar sends a response to the identify message.",
                 "The callback listener should notify of the response and the client should send a GetChecksumsRequest "
                         + "message to the pillars");
 
         IdentifyPillarsForGetChecksumsResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(identifyResponse);
         IdentifyPillarsForGetChecksumsResponse identifyResponse2 = testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                 receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
         messageBus.sendMessage(identifyResponse2);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertNotNull(pillar1Destination.waitForMessage(GetChecksumsRequest.class));
         Assert.assertNotNull(pillar2Destination.waitForMessage(GetChecksumsRequest.class));
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
 
         addStep("Wait for at least 3 seconds", "An FAILED event should be generated");
         Assert.assertEquals(testEventHandler.waitForEvent( 4, TimeUnit.SECONDS).getEventType(), OperationEventType.FAILED);
     }
 
     @Test(groups = {"regressiontest"})
     public void testNoSuchFile() throws Exception {
         addDescription("Testing how a request for a non-existing file is handled.");
         addStep("Setting up variables and such.", "Should be OK.");
 
         FileIDs fileIDs = new FileIDs();
         fileIDs.setFileID(DEFAULT_FILE_ID);
 
         componentSettings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         componentSettings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
 
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         GetChecksumsClient getChecksumsClient = createGetCheckSumsClient();
 
         addStep("Request the delivery of the checksum of a file from the pillar(s). A callback listener should be supplied.",
                 "A IdentifyPillarsForGetChecksumsRequest will be sent to the pillar(s).");
         getChecksumsClient.getChecksums(null, fileIDs, null, null, testEventHandler, "TEST-AUDIT");
 
         IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage = null;
 
         receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(IdentifyPillarsForGetChecksumsRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("The pillar sends a response to the identify message.",
                 "The callback listener should notify of the response and the client should send a GetChecksumsRequest "
                         + "message to the pillar");
 
         GetChecksumsRequest receivedGetChecksumsRequest = null;
         IdentifyPillarsForGetChecksumsResponse identifyResponse =
                 testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                         receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(identifyResponse);
         receivedGetChecksumsRequest = pillar1Destination.waitForMessage(GetChecksumsRequest.class);
 
         for(int i = 0; i < componentSettings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
 
         addStep("Send a error that the file cannot be found.", "Should trigger a 'event failed'.");
         if (useMockupPillar()) {
             GetChecksumsFinalResponse completeMsg = testMessageFactory.createGetChecksumsFinalResponse(
                     receivedGetChecksumsRequest, PILLAR1_ID, pillar1DestinationId);
 
             ResponseInfo rfInfo = new ResponseInfo();
             rfInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
             rfInfo.setResponseText("No such file.");
             completeMsg.setResponseInfo(rfInfo);
             completeMsg.setResultingChecksums(null);
 
             messageBus.sendMessage(completeMsg);
         }
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
     }
 
     /**
      * Creates a new test GetCheckSumsClient based on the supplied settings. 
      *
      * Note that the normal way of creating client through the module factory would reuse components with settings from
      * previous tests.
      * @return A new GetFileClient(Wrapper).
      */
     private GetChecksumsClient createGetCheckSumsClient() {return new GetChecksumsClientTestWrapper(new ConversationBasedGetChecksumsClient(
             messageBus, conversationMediator, componentSettings, TEST_CLIENT_ID), testEventManager);
     }
 }
