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
 
 import java.math.BigInteger;
 import java.util.concurrent.TimeUnit;
 import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumType;
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositoryelements.ResponseInfo;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
 import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.PutFileRequest;
 import org.bitrepository.client.DefaultFixtureClientTest;
 import org.bitrepository.client.TestEventHandler;
 import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
 import org.bitrepository.common.utils.Base16Utils;
 import org.bitrepository.common.utils.TestFileHelper;
 import org.bitrepository.modify.ModifyComponentFactory;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 public class PutFileClientComponentTest extends DefaultFixtureClientTest {
     private TestPutFileMessageFactory messageFactory;
 
     @BeforeMethod(alwaysRun=true)
     public void initialise() throws Exception {
         messageFactory = new TestPutFileMessageFactory(settingsForCUT.getCollectionID());
 
 }
 
     @Test(groups={"regressiontest"})
     public void verifyPutClientFromFactory() {
         addDescription("Testing the initialization through the ModifyComponentFactory.");
         addStep("Use the ModifyComponentFactory to instantiate a PutFileClient.",
                 "It should be an instance of SimplePutFileClient");
         PutFileClient pfc = ModifyComponentFactory.getInstance().retrievePutClient(
                 settingsForCUT, securityManager, settingsForTestClient.getComponentID());
         Assert.assertTrue(pfc instanceof ConversationBasedPutFileClient, "The PutFileClient '" + pfc + "' should be instance of '"
                 + ConversationBasedPutFileClient.class.getName() + "'");
     }
 
     @Test(groups={"regressiontest"})
     public void normalPutFile() throws Exception {
         addDescription("Tests the PutClient. Makes a whole conversation for the put client for a 'good' scenario.");
         addFixtureSetup("Initialise the number of pillars to one");
 
         settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
 
         addStep("Ensure that the test-file is placed on the HTTP server.", "Should be removed an reuploaded.");
 
         addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.",
                 "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, 0,
                 null, null, testEventHandler, null);
 
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage =
                 collectionReceiver.waitForMessage(IdentifyPillarsForPutFileRequest.class);
             Assert.assertEquals(receivedIdentifyRequestMessage,
                     messageFactory.createIdentifyPillarsForPutFileRequest(
                             receivedIdentifyRequestMessage.getCorrelationID(),
                             receivedIdentifyRequestMessage.getReplyTo(),
                             receivedIdentifyRequestMessage.getTo(),
                             DEFAULT_FILE_ID,
                             0,
                             receivedIdentifyRequestMessage.getAuditTrailInformation(),
                             settingsForTestClient.getComponentID()
                     ));
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Make response for the pillar.", "The client should then send the actual PutFileRequest.");
 
         PutFileRequest receivedPutFileRequest = null;
             IdentifyPillarsForPutFileResponse identifyResponse = messageFactory
                     .createIdentifyPillarsForPutFileResponse(
                             receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(identifyResponse);
             receivedPutFileRequest = pillar1Receiver.waitForMessage(PutFileRequest.class, 10, TimeUnit.SECONDS);
             Assert.assertEquals(receivedPutFileRequest,
                     messageFactory.createPutFileRequest(
                             PILLAR1_ID, pillar1DestinationId,
                             receivedPutFileRequest.getReplyTo(),
                             receivedPutFileRequest.getCorrelationID(),
                             receivedPutFileRequest.getFileAddress(),
                             receivedPutFileRequest.getFileSize(),
                             DEFAULT_FILE_ID,
                             receivedPutFileRequest.getAuditTrailInformation(),
                             settingsForTestClient.getComponentID()
                     ));
 
         addStep("Validate the steps of the PutClient by going through the events.", "Should be 'PillarIdentified', "
                 + "'PillarSelected' and 'RequestSent'");
         for(int i = 0; i < settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
 
         addStep("The pillar sends a progress response to the PutClient.", "Should be caught by the event handler.");
             PutFileProgressResponse putFileProgressResponse = messageFactory.createPutFileProgressResponse(
                     receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(putFileProgressResponse);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.PROGRESS);
 
         addStep("Send a final response message to the PutClient.",
                 "Should be caught by the event handler. First a PartiallyComplete, then a Complete.");
             PutFileFinalResponse putFileFinalResponse = messageFactory.createPutFileFinalResponse(
                     receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(putFileFinalResponse);
         for(int i = 1; i < 2* settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             OperationEventType eventType = testEventHandler.waitForEvent().getEventType();
             Assert.assertTrue( (eventType == OperationEventType.COMPONENT_COMPLETE)
                     || (eventType == OperationEventType.PROGRESS),
                     "Expected either PartiallyComplete or Progress, but was: " + eventType);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
     }
 
     @Test(groups={"regressiontest"})
     public void noPillarsResponding() throws Exception {
         addDescription("Tests the handling of missing identification responses from all pillar");
         addFixtureSetup("Sets the identification timeout to 1 sec.");
 
         settingsForCUT.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(1000L));
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
         addStep("Request the putting of a file through the PutClient",
                 "A identification request should be dispatched.");
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, 0, null,
                 null, testEventHandler, null);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
         collectionReceiver.waitForMessage(IdentifyPillarsForPutFileRequest.class);
 
         addStep("Do not respond. Just await the timeout.",
                 "An IDENTIFY_TIMEOUT event should be generate, followed by a FAILED event.");
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_TIMEOUT);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
     }
 
     @Test(groups={"regressiontest"})
     public void onePillarRespondingWithPartialPutAllowed() throws Exception {
         addReference("<a href=https://sbforge.org/jira/browse/BITMAG-598>" +
                 "BITMAG-598 It should be possible to putFiles, even though only a subset of the pillars are available</a>");
         addDescription("Tests the handling of missing identification responses from one pillar, " +
                 "when partial put are allowed");
         addFixtureSetup("Sets the identification timeout to 3 sec and allow partial puts.");
 
         settingsForCUT.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000L));
         settingsForCUT.getReferenceSettings().getPutFileSettings().setPartialPutsAllow(true);
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
         addStep("Request the putting of a file through the PutClient",
                 "A identification request should be dispatched.");
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, 0, null,
                 null, testEventHandler, null);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage =
                 collectionReceiver.waitForMessage(IdentifyPillarsForPutFileRequest.class);
 
         addStep("Only send an identification response from one pillar.",
                 "An COMPONENT_IDENTIFIED event should be generate.");
         IdentifyPillarsForPutFileResponse identifyResponse = messageFactory.createIdentifyPillarsForPutFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(identifyResponse);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
 
         addStep("Await the timeout.", "An IDENTIFY_TIMEOUT events, a COMPONENT_FAILED " +
                 "event for the non-responding pillar and a IDENTIFICATION_COMPLETE event should be generated.");
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_TIMEOUT);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
 
         addStep("The client should proceed to send a putFileOperation request to the responding pillar.",
                 "A REQUEST_SENT event should be generated and a PutFileRequest should be received on the pillar.");
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
         PutFileRequest receivedPutFileRequest = pillar1Receiver.waitForMessage(PutFileRequest.class);
 
         addStep("Send a pillar complete event",
                 "The client should generate a COMPONENT_COMPLETE followed by a COMPLETE event");
         PutFileFinalResponse putFileFinalResponse = messageFactory.createPutFileFinalResponse(
                 receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(putFileFinalResponse);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
     }
 
     @Test(groups={"regressiontest"})
     public void onePillarRespondingWithPartialPutDisallowed() throws Exception {
         addDescription("Tests the handling of missing identification responses from one pillar, " +
                 "when partial put are allowed");
         addFixtureSetup("Sets the identification timeout to 3 sec and disallow partial puts.");
 
         settingsForCUT.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000L));
         settingsForCUT.getReferenceSettings().getPutFileSettings().setPartialPutsAllow(false);
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
         addStep("Request the putting of a file through the PutClient",
                 "A identification request should be dispatched.");
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, 0, null,
                 null, testEventHandler, null);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage =
                 collectionReceiver.waitForMessage(IdentifyPillarsForPutFileRequest.class);
 
         addStep("Only send an identification response from one pillar.",
                 "An COMPONENT_IDENTIFIED event should be generate.");
         IdentifyPillarsForPutFileResponse identifyResponse = messageFactory.createIdentifyPillarsForPutFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(identifyResponse);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
 
         addStep("Await the timeout.", "An IDENTIFY_TIMEOUT event ,COMPONENT_FAILED " +
                 "event for the non-responding pillar, an IDENTIFICATION_COMPLETE and " +
                 "lastly a OperationEventType.FAILED event should be generated.");
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_TIMEOUT);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
 
     }
 
     @Test(groups={"regressiontest"})
     public void putClientOperationTimeout() throws Exception {
         addDescription("Tests the handling of a failed operation for the PutClient");
         addStep("Initialise the number of pillars and the PutClient. Sets the operation timeout to 1 sec.",
                 "Should be OK.");
         settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         settingsForCUT.getCollectionSettings().getClientSettings().setOperationTimeout(BigInteger.valueOf(1000L));
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
         addStep("Request the putting of a file through the PutClient",
                 "The identification message should be sent.");
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, 0, null,
                 null, testEventHandler, null);
 
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage =
                 collectionReceiver.waitForMessage(IdentifyPillarsForPutFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Make response for the pillar.", "The client should then send the actual PutFileRequest.");
         IdentifyPillarsForPutFileResponse identifyResponse = messageFactory
                 .createIdentifyPillarsForPutFileResponse(
                         receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(identifyResponse);
         pillar1Receiver.waitForMessage(PutFileRequest.class, 10, TimeUnit.SECONDS);
 
         addStep("Validate the steps of the PutClient by going through the events.", "Should be 'PillarIdentified', "
                 + "'PillarSelected' and 'RequestSent'");
         for(int i = 0; i < settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
 
         addStep("Do not respond. Just await the timeout.",
                 "Should make send a Failure event to the eventhandler.");
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
     }
 
     @Test(groups={"regressiontest"})
     public void putClientPillarOperationFailed() throws Exception {
         addDescription("Tests the handling of a operation failure for the PutClient. ");
         addStep("Initialise the number of pillars to one", "Should be OK.");
 
         settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
         addStep("Ensure that the test-file is placed on the HTTP server.", "Should be removed an reuploaded.");
 
         addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.",
                 "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, 0,
                 null, null, testEventHandler, "TEST-AUDIT-TRAIL");
 
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage =
                 collectionReceiver.waitForMessage(IdentifyPillarsForPutFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Send pillar response.", "The client should then send the actual PutFileRequest.");
         IdentifyPillarsForPutFileResponse identifyResponse = messageFactory.createIdentifyPillarsForPutFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(identifyResponse);
         PutFileRequest receivedPutFileRequest = pillar1Receiver.waitForMessage(PutFileRequest.class, 10, TimeUnit.SECONDS);
 
         addStep("Validate the steps of the PutClient by going through the events.",
                 "Should be 'PillarIdentified', 'PillarSelected' and 'RequestSent'");
         for(int i = 0; i < settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
 
         addStep("Send a failed response message to the PutClient.",
                 "Should be caught by the event handler. First a PillarFailed, then a Complete.");
             PutFileFinalResponse putFileFinalResponse = messageFactory.createPutFileFinalResponse(
                     receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
             ResponseInfo ri = new ResponseInfo();
             ri.setResponseCode(ResponseCode.FAILURE);
             ri.setResponseText("Verifying that a failure can be understood!");
             putFileFinalResponse.setResponseInfo(ri);
             messageBus.sendMessage(putFileFinalResponse);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
     }
 
     @Test(groups={"regressiontest"})
     public void fileExistsOnPillarNoChecksumFromPillar() throws Exception {
         addDescription("Tests that PutClient handles the presence of a file correctly, when the pillar doesn't return a " +
                 "checksum in the identification response. ");
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
         addStep("Call putFile.",
                 "A IdentifyPillarsForGetFileRequest will be sent to the pillar and a " +
                         "IDENTIFY_REQUEST_SENT should be generated.");
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, 0,
                 null, null, testEventHandler, "TEST-AUDIT-TRAIL");
 
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage =
                 collectionReceiver.waitForMessage(IdentifyPillarsForPutFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Send a DUPLICATE_FILE_FAILURE response without a checksum.",
                 "The client should generate the following events:'"
                 + OperationEventType.COMPONENT_FAILED + "', '"
                 + OperationEventType.FAILED + "'");
         IdentifyPillarsForPutFileResponse identifyResponse = messageFactory.createIdentifyPillarsForPutFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         ResponseInfo ri = new ResponseInfo();
         ri.setResponseCode(ResponseCode.DUPLICATE_FILE_FAILURE);
         ri.setResponseText("Testing the handling of 'DUPLICATE FILE' identification.");
         identifyResponse.setResponseInfo(ri);
         messageBus.sendMessage(identifyResponse);
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
     }
 
     @Test(groups={"regressiontest"})
     public void fileExistsOnPillarDifferentChecksumFromPillar() throws Exception {
         addDescription("Tests that PutClient handles the presence of a file correctly, when the pillar " +
                 "returns a checksum different from the file being put. ");
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
         addStep("Call putFile.",
                 "A IdentifyPillarsForGetFileRequest will be sent to the pillar and a " +
                         "IDENTIFY_REQUEST_SENT should be generated.");
         ChecksumDataForFileTYPE csClientData = TestFileHelper.getDefaultFileChecksum();
         csClientData.setChecksumValue(Base16Utils.encodeBase16("ba"));
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, 0,
                 csClientData, null, testEventHandler, "TEST-AUDIT-TRAIL");
 
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage =
                 collectionReceiver.waitForMessage(IdentifyPillarsForPutFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Send a DUPLICATE_FILE_FAILURE response with a random checksum.",
                 "The client should generate the following events:'"
                         + OperationEventType.COMPONENT_FAILED + "', '"
                         + OperationEventType.FAILED + "'");
         IdentifyPillarsForPutFileResponse identifyResponse = messageFactory.createIdentifyPillarsForPutFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         ResponseInfo ri = new ResponseInfo();
         ri.setResponseCode(ResponseCode.DUPLICATE_FILE_FAILURE);
         ri.setResponseText("Testing the handling of 'DUPLICATE FILE' identification.");
         identifyResponse.setResponseInfo(ri);
         ChecksumDataForFileTYPE csPillarData = TestFileHelper.getDefaultFileChecksum();
         csPillarData.setChecksumValue(Base16Utils.encodeBase16("aa"));
         identifyResponse.setChecksumDataForExistingFile(csPillarData);
         messageBus.sendMessage(identifyResponse);
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
     }
 
     @Test(groups={"regressiontest"})
     public void sameFileExistsOnOnePillar() throws Exception {
         addDescription("Tests that PutClient handles the presence of a file correctly, when the pillar " +
                 "returns a checksum equal the file being put (idempotent). ");
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
         addStep("Call putFile.",
                 "A IdentifyPillarsForGetFileRequest will be sent to the pillar and a " +
                         "IDENTIFY_REQUEST_SENT should be generated.");
         ChecksumDataForFileTYPE csClientData = TestFileHelper.getDefaultFileChecksum();
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, 0,
                 csClientData, null, testEventHandler, "TEST-AUDIT-TRAIL");
 
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage =
                 collectionReceiver.waitForMessage(IdentifyPillarsForPutFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Send a DUPLICATE_FILE_FAILURE response with a checksum equal to the one supplied to the client.",
                 "The client should generate the following events:'"
                         + OperationEventType.COMPONENT_COMPLETE);
         IdentifyPillarsForPutFileResponse identifyResponse = messageFactory.createIdentifyPillarsForPutFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         ResponseInfo ri = new ResponseInfo();
         ri.setResponseCode(ResponseCode.DUPLICATE_FILE_FAILURE);
         ri.setResponseText("Testing the handling of 'DUPLICATE FILE' identification.");
         identifyResponse.setResponseInfo(ri);
         ChecksumDataForFileTYPE csPillarData = TestFileHelper.getDefaultFileChecksum();
         identifyResponse.setChecksumDataForExistingFile(csPillarData);
         messageBus.sendMessage(identifyResponse);
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
 
         addStep("Send an identification response from the second pillar.",
                 "An COMPONENT_IDENTIFIED OperationEventType.IDENTIFICATION_COMPLETE and a event should be generate.");
         IdentifyPillarsForPutFileResponse identifyResponse2 = messageFactory.createIdentifyPillarsForPutFileResponse(
                 receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
         messageBus.sendMessage(identifyResponse2);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
 
         addStep("The client should proceed to send a putFileOperation request to the second pillar.",
                 "A REQUEST_SENT event should be generated and a PutFileRequest should be received on the pillar.");
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
         PutFileRequest receivedPutFileRequest = pillar2Receiver.waitForMessage(PutFileRequest.class);
 
         addStep("Send a pillar complete event",
                 "The client should generate a COMPONENT_COMPLETE followed by a COMPLETE event");
         PutFileFinalResponse putFileFinalResponse = messageFactory.createPutFileFinalResponse(
                 receivedPutFileRequest, PILLAR2_ID, pillar1DestinationId);
         messageBus.sendMessage(putFileFinalResponse);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
     }
 
     @Test(groups={"regressiontest"})
     public void fileExistsOnPillarChecksumFromPillarNoClientChecksum() throws Exception {
         addDescription("Tests that PutClient handles the presence of a file correctly, when the pillar " +
                 "returns a checksum but the putFile was called without a checksum. ");
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
         addStep("Call putFile.",
                 "A IdentifyPillarsForGetFileRequest will be sent to the pillar and a " +
                         "IDENTIFY_REQUEST_SENT should be generated.");
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, 0,
                 null, null, testEventHandler, "TEST-AUDIT-TRAIL");
 
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                 IdentifyPillarsForPutFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Send a DUPLICATE_FILE_FAILURE response with a random checksum.",
                 "The client should generate the following events:'"
                         + OperationEventType.COMPONENT_FAILED + "', '"
                         + OperationEventType.FAILED + "'");
         IdentifyPillarsForPutFileResponse identifyResponse = messageFactory.createIdentifyPillarsForPutFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         ResponseInfo ri = new ResponseInfo();
         ri.setResponseCode(ResponseCode.DUPLICATE_FILE_FAILURE);
         ri.setResponseText("Testing the handling of 'DUPLICATE FILE' identification.");
         identifyResponse.setResponseInfo(ri);
         ChecksumDataForFileTYPE csData = TestFileHelper.getDefaultFileChecksum();
         csData.setChecksumValue(Base16Utils.encodeBase16("aa"));
         identifyResponse.setChecksumDataForExistingFile(csData);
         messageBus.sendMessage(identifyResponse);
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
     }
 
     @Test(groups={"regressiontest"})
     public void saltedReturnChecksumsWithChecksumPillar() throws Exception {
         addDescription("Tests that PutClient handles the presence of a ChecksumPillar correctly, when a salted return" +
                 " checksum (which a checksum pillar can't provide) is requested. ");
         addReference("<a href=\"https://sbforge.org/jira/browse/BITMAG-677\">BITMAG-677" +
                 "put, replace and delete clients fails if return checksums are requested and a checksumpillar is " +
                 "involved</a>");
 
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
         addStep("Call putFile while requesting a salted checksum to be returned.",
                 "A IdentifyPillarsForGetFileRequest will be sent to the pillar and a " +
                         "IDENTIFY_REQUEST_SENT should be generated.");
         ChecksumSpecTYPE checksumSpecTYPE = new ChecksumSpecTYPE();
         checksumSpecTYPE.setChecksumType(ChecksumType.HMAC_MD5);
         checksumSpecTYPE.setChecksumSalt(Base16Utils.encodeBase16("aa"));
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, 0,
                 null, checksumSpecTYPE, testEventHandler, "TEST-AUDIT-TRAIL");
 
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                 IdentifyPillarsForPutFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Send an identification response with a PillarChecksumSpec element set, indicating that this is a " +
                 "checksum pillar.",
                 "An COMPONENT_IDENTIFIED event should be generate.");
         IdentifyPillarsForPutFileResponse identifyResponse = messageFactory.createIdentifyPillarsForPutFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         ChecksumSpecTYPE checksumSpecTYPEFromPillar = new ChecksumSpecTYPE();
         checksumSpecTYPEFromPillar.setChecksumType(ChecksumType.MD5);
         identifyResponse.setPillarChecksumSpec(checksumSpecTYPEFromPillar);
         messageBus.sendMessage(identifyResponse);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
 
         addStep("Send an normal identification response from pillar2.",
                 "An COMPONENT_IDENTIFIED event should be generate followed by a IDENTIFICATION_COMPLETE and a " +
                         "REQUEST_SENT. \nRequests for put files should be received, the one for the checksum pillar" +
                         "without a request for a return checksum, and the request to the normal pillar" +
                         "should specify that a salted checksum should be returned.");
         IdentifyPillarsForPutFileResponse identifyResponse2 = messageFactory.createIdentifyPillarsForPutFileResponse(
                 receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
         messageBus.sendMessage(identifyResponse2);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
         PutFileRequest receivedPutFileRequest1 = pillar1Receiver.waitForMessage(PutFileRequest.class);
         Assert.assertNull(receivedPutFileRequest1.getChecksumRequestForNewFile());
 
         PutFileRequest receivedPutFileRequest2 =
                 pillar2Receiver.waitForMessage(PutFileRequest.class);
         Assert.assertEquals(receivedPutFileRequest2.getChecksumRequestForNewFile(), checksumSpecTYPE);
 
     }
 
     @Test(groups={"regressiontest"})
     public void defaultReturnChecksumsWithChecksumPillar() throws Exception {
         addDescription("Tests that PutClient handles the presence of a ChecksumPillar correctly, when a return" +
                 " checksum of default type is requested (which a checksum pillar can provide). ");
 
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
         addStep("Call putFile while requesting a salted checksum to be returned.",
                 "A IdentifyPillarsForGetFileRequest will be sent to the pillar and a " +
                         "IDENTIFY_REQUEST_SENT should be generated.");
         ChecksumSpecTYPE checksumSpecTYPE = new ChecksumSpecTYPE();
         checksumSpecTYPE.setChecksumType(ChecksumType.MD5);
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, 0,
                 null, checksumSpecTYPE, testEventHandler, "TEST-AUDIT-TRAIL");
 
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                 IdentifyPillarsForPutFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Send an identification response with a PillarChecksumSpec element set, indicating that this is a " +
                 "checksum pillar.",
                 "An COMPONENT_IDENTIFIED event should be generate.");
         IdentifyPillarsForPutFileResponse identifyResponse = messageFactory.createIdentifyPillarsForPutFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         ChecksumSpecTYPE checksumSpecTYPEFromPillar = new ChecksumSpecTYPE();
         checksumSpecTYPEFromPillar.setChecksumType(ChecksumType.MD5);
         identifyResponse.setPillarChecksumSpec(checksumSpecTYPEFromPillar);
         messageBus.sendMessage(identifyResponse);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
 
         addStep("Send an normal identification response from pillar2.",
                 "An COMPONENT_IDENTIFIED event should be generate followed by a IDENTIFICATION_COMPLETE and a " +
                         "REQUEST_SENT. \nRequests for put files should be received, both requesting a return checksum " +
                         "of the default type  a request for a return checksum, and the request to the normal pillar" +
                         "should specify that a salted checksum should be returned.");
         IdentifyPillarsForPutFileResponse identifyResponse2 = messageFactory.createIdentifyPillarsForPutFileResponse(
                 receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
         messageBus.sendMessage(identifyResponse2);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
         PutFileRequest receivedPutFileRequest1 =
                 pillar1Receiver.waitForMessage(PutFileRequest.class);
         Assert.assertEquals(receivedPutFileRequest1.getChecksumRequestForNewFile(), checksumSpecTYPE);
 
         PutFileRequest receivedPutFileRequest2 =
                 pillar2Receiver.waitForMessage(PutFileRequest.class);
         Assert.assertEquals(receivedPutFileRequest2.getChecksumRequestForNewFile(), checksumSpecTYPE);
     }
 
     @Test(groups={"regressiontest"})
     public void noReturnChecksumsWithChecksumPillar() throws Exception {
         addDescription("Tests that PutClient handles the presence of a ChecksumPillar correctly, when no return" +
                 " checksum is requested.");
 
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
         addStep("Call putFile while requesting a salted checksum to be returned.",
                 "A IdentifyPillarsForGetFileRequest will be sent to the pillar and a " +
                         "IDENTIFY_REQUEST_SENT should be generated.");
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, 0,
                 null, null, testEventHandler, "TEST-AUDIT-TRAIL");
 
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                 IdentifyPillarsForPutFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Send an identification response with a PillarChecksumSpec element set, indicating that this is a " +
                 "checksum pillar.",
                 "An COMPONENT_IDENTIFIED event should be generate.");
         IdentifyPillarsForPutFileResponse identifyResponse = messageFactory.createIdentifyPillarsForPutFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         ChecksumSpecTYPE checksumSpecTYPEFromPillar = new ChecksumSpecTYPE();
         checksumSpecTYPEFromPillar.setChecksumType(ChecksumType.MD5);
         identifyResponse.setPillarChecksumSpec(checksumSpecTYPEFromPillar);
         messageBus.sendMessage(identifyResponse);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
 
         addStep("Send an normal identification response from pillar2.",
                 "An COMPONENT_IDENTIFIED event should be generate followed by a IDENTIFICATION_COMPLETE and a " +
                         "REQUEST_SENT. \nRequests for put files should be received, neither requesting a return " +
                         "checksum.");
         IdentifyPillarsForPutFileResponse identifyResponse2 = messageFactory.createIdentifyPillarsForPutFileResponse(
                 receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
         messageBus.sendMessage(identifyResponse2);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
         PutFileRequest receivedPutFileRequest1 = pillar1Receiver.waitForMessage(PutFileRequest.class);
         Assert.assertNull(receivedPutFileRequest1.getChecksumRequestForNewFile());
 
         PutFileRequest receivedPutFileRequest2 = pillar2Receiver.waitForMessage(PutFileRequest.class);
         Assert.assertNull(receivedPutFileRequest2.getChecksumRequestForNewFile());
     }
 
     @Test(groups={"regressiontest"})
     public void onePillarPutRetrySuccess() throws Exception {
         addReference("<a href=https://sbforge.org/jira/browse/BITMAG-810>" +
                 "BITMAG-810 Reference client should be able to retry failed file transfers</a>");
         addDescription("Tests the handling of a failed transmission when retry is allowed");
         addFixtureSetup("Sets the identification timeout to 3 sec, allow two retries and only register one pillar.");
 
         settingsForCUT.getReferenceSettings().getClientSettings().setOperationRetryCount(BigInteger.valueOf(2));
         settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
         addStep("Request the putting of a file through the PutClient",
                 "A identification request should be dispatched.");
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, 0, null,
                 null, testEventHandler, null);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage =
                 collectionReceiver.waitForMessage(IdentifyPillarsForPutFileRequest.class);
 
         addStep("Send an identification response from the one pillar.",
                 "An COMPONENT_IDENTIFIED event should be generate.");
         IdentifyPillarsForPutFileResponse identifyResponse = messageFactory.createIdentifyPillarsForPutFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(identifyResponse);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
 
         addStep("The client should proceed to send a putFileOperation request to the responding pillar.",
                 "A REQUEST_SENT event should be generated and a PutFileRequest should be received on the pillar.");
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
         PutFileRequest receivedPutFileRequest = pillar1Receiver.waitForMessage(PutFileRequest.class);
 
         addStep("Send a PutFileFinalResponse indicating a FILE_TRANSFER_FAILURE",
                 "The client should emit a warning event and generate new PutFileRequest for the pillar");
         PutFileFinalResponse putFileFinalResponse = messageFactory.createPutFileFinalResponse(
                 receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
         putFileFinalResponse.getResponseInfo().setResponseCode(ResponseCode.FILE_TRANSFER_FAILURE);
         messageBus.sendMessage(putFileFinalResponse);     
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.WARNING);
         
         addStep("A new PutFileRequest is send, pillar responds with success", "The client generates " +
         		"a COMPONENT_COMPLETE, followed by a COMPLETE event.");
         PutFileRequest receivedPutFileRequest2 = pillar1Receiver.waitForMessage(PutFileRequest.class);
         PutFileFinalResponse putFileFinalResponse2 = messageFactory.createPutFileFinalResponse(
                 receivedPutFileRequest2, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(putFileFinalResponse2);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
     }
 
     @Test(groups={"regressiontest"})
     public void onePillarPutRetryFailure() throws Exception {
         addReference("<a href=https://sbforge.org/jira/browse/BITMAG-810>" +
                 "BITMAG-810 Reference client should be able to retry failed file transfers</a>");
         addDescription("Tests that a putfile attempt failing due to FILE_TRANSFER_FAILURE " +
         		"is only attempted the maximum allowed attempts");
         addFixtureSetup("Sets the identification timeout to 3 sec, allow two retries and only register one pillar.");
 
         settingsForCUT.getReferenceSettings().getClientSettings().setOperationRetryCount(BigInteger.valueOf(2));
         settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settingsForCUT.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         PutFileClient putClient = createPutFileClient();
 
         addStep("Request the putting of a file through the PutClient",
                 "A identification request should be dispatched.");
         putClient.putFile(httpServer.getURL(DEFAULT_FILE_ID), DEFAULT_FILE_ID, 0, null,
                 null, testEventHandler, null);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
         IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage =
                 collectionReceiver.waitForMessage(IdentifyPillarsForPutFileRequest.class);
 
         addStep("Send an identification response from the one pillar.",
                 "An COMPONENT_IDENTIFIED event should be generate.");
         IdentifyPillarsForPutFileResponse identifyResponse = messageFactory.createIdentifyPillarsForPutFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(identifyResponse);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
 
         addStep("The client should proceed to send a putFileOperation request to the responding pillar.",
                 "A REQUEST_SENT event should be generated and a PutFileRequest should be received on the pillar.");
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
         PutFileRequest receivedPutFileRequest = pillar1Receiver.waitForMessage(PutFileRequest.class);
 
         addStep("Send a PutFileFinalResponse indicating a FILE_TRANSFER_FAILURE",
                 "The client should emit a warning event and generate new PutFileRequest for the pillar");
         PutFileFinalResponse putFileFinalResponse = messageFactory.createPutFileFinalResponse(
                 receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
         putFileFinalResponse.getResponseInfo().setResponseCode(ResponseCode.FILE_TRANSFER_FAILURE);
         messageBus.sendMessage(putFileFinalResponse);     
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.WARNING);
         
         addStep("Send a PutFileFinalResponse indicating a FILE_TRANSFER_FAILURE for the second put attempt",
                 "The client should emit a warning event and generate new PutFileRequest for the pillar");
         receivedPutFileRequest = pillar1Receiver.waitForMessage(PutFileRequest.class);
         putFileFinalResponse = messageFactory.createPutFileFinalResponse(
                 receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
         putFileFinalResponse.getResponseInfo().setResponseCode(ResponseCode.FILE_TRANSFER_FAILURE);
         messageBus.sendMessage(putFileFinalResponse);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.WARNING);
         
         addStep("Send a PutFileFinalResponse indicating a FILE_TRANSFER_FAILURE for the third put attempt",
                 "The client should emit a COMPONENT_FAILED event and fail the put operation");
         receivedPutFileRequest = pillar1Receiver.waitForMessage(PutFileRequest.class);
         putFileFinalResponse = messageFactory.createPutFileFinalResponse(
                 receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
         putFileFinalResponse.getResponseInfo().setResponseCode(ResponseCode.FILE_TRANSFER_FAILURE);
         messageBus.sendMessage(putFileFinalResponse);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
     }
 
         /**
         * Creates a new test PutFileClient based on the settings.
         *
         * Note that the normal way of creating client through the module factory would reuse components with settings from
         * previous tests.
         * @return A new PutFileClient(Wrapper).
         */
     private PutFileClient createPutFileClient() {
         return new PutFileClientTestWrapper(new ConversationBasedPutFileClient(
                 messageBus, conversationMediator, settingsForCUT, settingsForTestClient.getComponentID())
                 , testEventManager);
     }
 }
