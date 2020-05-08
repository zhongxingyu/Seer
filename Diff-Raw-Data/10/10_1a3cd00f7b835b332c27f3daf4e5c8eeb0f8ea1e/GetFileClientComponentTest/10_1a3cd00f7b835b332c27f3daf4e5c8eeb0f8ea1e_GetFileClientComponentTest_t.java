 /*
  * #%L
  * bitrepository-access-client
  * 
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
 package org.bitrepository.access.getfile;
 
 import java.math.BigInteger;
 import java.net.URL;
 import java.util.concurrent.TimeUnit;
 import org.bitrepository.access.AccessComponentFactory;
 import org.bitrepository.bitrepositoryelements.FilePart;
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
 import org.bitrepository.bitrepositoryelements.TimeMeasureUnit;
 import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
 import org.bitrepository.client.TestEventHandler;
 import org.bitrepository.client.eventhandler.IdentificationCompleteEvent;
 import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 /**
  * Test class for the 'GetFileClient'.
  */
 public class GetFileClientComponentTest extends AbstractGetFileClientTest {
 
     private static FilePart NO_FILE_PART = null;
 
     @Test(groups = {"regressiontest"})
     public void verifyGetFileClientFromFactory() throws Exception {
         Assert.assertTrue(AccessComponentFactory.getInstance().createGetFileClient(componentSettings, securityManager, TEST_CLIENT_ID)
                 instanceof ConversationBasedGetFileClient,
                 "The default GetFileClient from the Access factory should be of the type '" +
                         ConversationBasedGetFileClient.class.getName() + "'.");
     }
 
     @Test(groups = {"regressiontest"})
     public void getFileFromSpecificPillarWithFilePart() throws Exception {
         addDescription("Tests that the GetClient client works for a single pillar " +
                 "participates. Also validate, that the 'FilePart' can be used.");
         addStep("Set the number of pillars to 1", "");
 
         componentSettings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         componentSettings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
 
         FilePart filePart = new FilePart();
         filePart.setPartLength(BigInteger.TEN);
         filePart.setPartOffSet(BigInteger.ONE);
 
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         GetFileClient client = createGetFileClient();
 
         String chosenPillar = componentSettings.getCollectionSettings().getClientSettings().getPillarIDs().get(0);
 
         addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.",
                 "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
         client.getFileFromSpecificPillar(DEFAULT_FILE_ID, filePart, httpServer.getURL(DEFAULT_FILE_ID),
                 chosenPillar, testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
         receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
         Assert.assertEquals(receivedIdentifyRequestMessage,
                 testMessageFactory.createIdentifyPillarsForGetFileRequest(receivedIdentifyRequestMessage,
                         collectionDestinationID, TEST_CLIENT_ID));
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("The pillar sends a response to the identify message.",
                 "The callback listener should notify of the response and the client should send a GetFileRequest message to " +
                         "the pillar");
 
         IdentifyPillarsForGetFileResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                 receivedIdentifyRequestMessage, chosenPillar, pillar1DestinationId);
         messageBus.sendMessage(identifyResponse);
         GetFileRequest receivedGetFileRequest = pillar1Destination.waitForMessage(GetFileRequest.class);
         Assert.assertEquals(receivedGetFileRequest,
                 testMessageFactory.createGetFileRequest(receivedGetFileRequest, filePart, chosenPillar,
                         pillar1DestinationId, TEST_CLIENT_ID));
 
         for(int i = 0; i < componentSettings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
 
         addStep("The pillar sends a getFile response to the GetClient.",
                 "The GetClient should notify about the response through the callback interface.");
         GetFileProgressResponse getFileProgressResponse = testMessageFactory.createGetFileProgressResponse(
                 receivedGetFileRequest, chosenPillar, pillar1DestinationId);
         messageBus.sendMessage(getFileProgressResponse);
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.PROGRESS);
 
         addStep("The file is uploaded to the indicated url and the pillar sends a final response upload message",
                 "The GetFileClient notifies that the file is ready through the callback listener and the uploaded " +
                         "file is present.");
 
         GetFileFinalResponse completeMsg = testMessageFactory.createGetFileFinalResponse(
                 receivedGetFileRequest, chosenPillar, pillar1DestinationId);
         messageBus.sendMessage(completeMsg);
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
     }
 
     @Test(groups = {"regressiontest"})
     public void chooseFastestPillarGetFileClient() throws Exception {
         addDescription("Set the GetClient to retrieve a file as fast as "
                 + "possible, where it has to choose between to pillars with "
                 + "different times. The messages should be delivered at the "
                 + "same time.");
         addStep("Create a GetFileClient configured to use a fast and a slow pillar.", "");
 
         String averagePillarID = "THE-AVERAGE-PILLAR";
         String fastPillarID = "THE-FAST-PILLAR";
         String slowPillarID = "THE-SLOW-PILLAR";
         componentSettings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         componentSettings.getCollectionSettings().getClientSettings().getPillarIDs().add(averagePillarID);
         componentSettings.getCollectionSettings().getClientSettings().getPillarIDs().add(fastPillarID);
         componentSettings.getCollectionSettings().getClientSettings().getPillarIDs().add(slowPillarID);
         GetFileClient client = createGetFileClient();
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
 
         addStep("Defining the variables for the GetFileClient and defining them in the configuration",
                 "It should be possible to change the values of the configurations.");
 
         addStep("Make the GetClient ask for fastest pillar.",
                 "It should send message to identify which pillars and a IdentifyPillarsRequestSent notification should be generated.");
         client.getFileFromFastestPillar(DEFAULT_FILE_ID, NO_FILE_PART, httpServer.getURL(DEFAULT_FILE_ID),
                 testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
         if (useMockupPillar()) {
             receivedIdentifyRequestMessage =
                     collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Three pillars send responses. First an average timeToDeliver, then a fast timeToDeliver and last a" +
                 " slow timeToDeliver.", "The client should send a getFileRequest to the fast pillar. " +
                 "The event handler should receive the following events: " +
                 "3 x PillarIdentified, a PillarSelected and a RequestSent");
 
         IdentifyPillarsForGetFileResponse averageReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                 receivedIdentifyRequestMessage, averagePillarID, pillar2DestinationId);
         TimeMeasureTYPE averageTime = new TimeMeasureTYPE();
         averageTime.setTimeMeasureUnit(TimeMeasureUnit.MILLISECONDS);
         averageTime.setTimeMeasureValue(BigInteger.valueOf(100L));
         averageReply.setTimeToDeliver(averageTime);
         messageBus.sendMessage(averageReply);
 
         IdentifyPillarsForGetFileResponse fastReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                 receivedIdentifyRequestMessage, fastPillarID, pillar1DestinationId);
         TimeMeasureTYPE fastTime = new TimeMeasureTYPE();
         fastTime.setTimeMeasureUnit(TimeMeasureUnit.MILLISECONDS);
         fastTime.setTimeMeasureValue(BigInteger.valueOf(10L));
         fastReply.setTimeToDeliver(fastTime);
         messageBus.sendMessage(fastReply);
 
         IdentifyPillarsForGetFileResponse slowReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                 receivedIdentifyRequestMessage, slowPillarID, pillar2DestinationId);
         TimeMeasureTYPE slowTime = new TimeMeasureTYPE();
         slowTime.setTimeMeasureValue(BigInteger.valueOf(1L));
         slowTime.setTimeMeasureUnit(TimeMeasureUnit.HOURS);
         slowReply.setTimeToDeliver(slowTime);
         messageBus.sendMessage(slowReply);
 
         GetFileRequest receivedGetFileRequest = pillar1Destination.waitForMessage(GetFileRequest.class);
         Assert.assertEquals(receivedGetFileRequest,
                 testMessageFactory.createGetFileRequest(receivedGetFileRequest, NO_FILE_PART, fastPillarID,
                         pillar1DestinationId, TEST_CLIENT_ID));
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         IdentificationCompleteEvent event = (IdentificationCompleteEvent) testEventHandler.waitForEvent();
         Assert.assertEquals(event.getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(event.getContributorIDs().get(0), fastPillarID);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
     }
 
     @Test(groups = {"regressiontest"})
     public void getFileClientWithIdentifyTimeout() throws Exception {
         addDescription("Verify that the GetFile works correct without receiving responses from all pillars.");
         addFixtureSetup("Set the a identification timeout to 3 seconds ");
         componentSettings.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000));
 
 
         addStep("Call getFile form fastest pillar.",
                 "A IDENTIFY_REQUEST_SENT should be generate and a identification request should be sent.");
         GetFileClient client = createGetFileClient();
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         client.getFileFromFastestPillar(DEFAULT_FILE_ID, NO_FILE_PART, httpServer.getURL(DEFAULT_FILE_ID),
                 testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage =
                 collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Send a identification response from pillar1.",
                 "A COMPONENT_IDENTIFIED event should be generated.");
 
         IdentifyPillarsForGetFileResponse identificationResponse1 = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(identificationResponse1);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
 
         addStep("Wait 3 seconds.",
                 "A IDENTIFY_TIMEOUT event should be generated, followed by a IDENTIFICATION_COMPLETE.");
         Assert.assertEquals(testEventHandler.waitForEvent(
                 3, TimeUnit.SECONDS).getEventType(), OperationEventType.IDENTIFY_TIMEOUT);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
 
         addStep("Verify that the client continues to the performing phase.",
                 "A REQUEST_SENT event should be generated and a GetFileRequest should be sent to pillar1.");
         GetFileRequest getFileRequest = pillar1Destination.waitForMessage(GetFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
 
         addStep("Send a final response upload message",
                 "A COMPONENT_COMPLETE event should be generated followed by at COMPLETE event.");
         GetFileFinalResponse completeMsg = testMessageFactory.createGetFileFinalResponse(
                 getFileRequest, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(completeMsg);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
 
     }
 
     @Test(groups = {"regressiontest"})
     public void noIdentifyResponse() throws Exception {
         addDescription("Tests the the GetFileClient handles lack of IdentifyPillarResponses gracefully  ");
         addStep("Set  a 3 second timeout for identifying pillar.", "");
 
         componentSettings.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000));
         GetFileClient client = createGetFileClient();
 
         addStep("Make the GetClient ask for fastest pillar.",
                 "It should send message to identify which pillar can respond fastest.");
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         client.getFileFromFastestPillar(DEFAULT_FILE_ID, NO_FILE_PART, httpServer.getURL(DEFAULT_FILE_ID),
                 testEventHandler);
         if (useMockupPillar()) {
             collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Wait for 5 seconds", "An IdentifyPillarTimeout event should be received followed by a FAILED event");
         Assert.assertEquals(testEventHandler.waitForEvent(5, TimeUnit.SECONDS).getEventType(), OperationEventType.IDENTIFY_TIMEOUT);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
     }
 
     @Test(groups = {"regressiontest"})
     public void conversationTimeout() throws Exception {
         addDescription("Tests the the GetFileClient handles lack of IdentifyPillarResponses gracefully  ");
         addStep("Set the number of pillars to 1 and a 3 second timeout for the conversation.", "");
 
         componentSettings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         componentSettings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         componentSettings.getReferenceSettings().getClientSettings().setConversationTimeout(BigInteger.valueOf(1000));
         GetFileClient client = createGetFileClient();
 
         addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.",
                 "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         client.getFileFromSpecificPillar( DEFAULT_FILE_ID, NO_FILE_PART, httpServer.getURL(DEFAULT_FILE_ID),
                 PILLAR1_ID, testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
         if (useMockupPillar()) {
             receivedIdentifyRequestMessage =
                     collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
             IdentifyPillarsForGetFileRequest expectedMessage =
                     testMessageFactory.createIdentifyPillarsForGetFileRequest(receivedIdentifyRequestMessage,
                             collectionDestinationID, TEST_CLIENT_ID);
             Assert.assertEquals(receivedIdentifyRequestMessage, expectedMessage);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("The pillar sends a response to the identify message.",
                 "The callback listener should notify of the response and the client should send a GetFileRequest message to " +
                         "the pillar");
 
         IdentifyPillarsForGetFileResponse identifyResponse =
                 testMessageFactory.createIdentifyPillarsForGetFileResponse(
                         receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         messageBus.sendMessage(identifyResponse);
         pillar1Destination.waitForMessage(GetFileRequest.class);
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
 
         addStep("Wait for 5 seconds", "An failed event should be generated followed by a FAILED event");
         Assert.assertEquals(testEventHandler.waitForEvent(5, TimeUnit.SECONDS).getEventType(), OperationEventType.FAILED);
     }
 
     @Test(groups = {"regressiontest"})
     public void testNoSuchFileSpecificPillar() throws Exception {
         addDescription("Testing how a request for a non-existing file is handled on a specific pillar request.");
         addStep("Define 1 pillar.", "");
         componentSettings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         componentSettings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         String fileName = "ERROR-NO-SUCH-FILE-ERROR";
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         URL url = httpServer.getURL(DEFAULT_FILE_ID);
 
         addStep("Call getFileFromSpecificPillar.",
                 "An identify request should be sent and an IdentifyPillarsRequestSent event should be generate");
         GetFileClient client = createGetFileClient();
 
         client.getFileFromSpecificPillar(fileName, NO_FILE_PART, url, PILLAR1_ID, testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage =
                 collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("The specified pillars sends a FILE_NOT_FOUND response",
                 "The client should generate 1 PillarIdentified event followed by a operation failed event.");
         IdentifyPillarsForGetFileResponse pillar1Response = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         pillar1Response.getResponseInfo().setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
         pillar1Response.getResponseInfo().setResponseText("File " +
                 receivedIdentifyRequestMessage.getFileID() + " not present on this pillar " + PILLAR1_ID);
         messageBus.sendMessage(pillar1Response);
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
     }
 
     @Test(groups = {"regressiontest"})
     public void testNoSuchFileMultiplePillars() throws Exception {
         addDescription("Testing how a request for a non-existing file is handled when all pillars miss the file.");
 
         String fileName = "ERROR-NO-SUCH-FILE-ERROR";
         GetFileClient client = createGetFileClient();
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         URL url = httpServer.getURL(DEFAULT_FILE_ID);
 
         addStep("Use the default 2 pillars.", "");
 
         addStep("Call getFileFromFastestPillar.",
                 "An identify request should be sent and a IdentifyPillarsRequestSent event should be generate");
         client.getFileFromFastestPillar(fileName, NO_FILE_PART, url, testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage =
                 collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Both pillars sends a FILE_NOT_FOUND response",
                 "The client should generate 2 PillarIdentified events followed by a Failed event.");
 
         IdentifyPillarsForGetFileResponse pillar1Response = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         pillar1Response.getResponseInfo().setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
         pillar1Response.getResponseInfo().setResponseText("File " +
                 receivedIdentifyRequestMessage.getFileID() + " not present on this pillar " );
         messageBus.sendMessage(pillar1Response);
 
         IdentifyPillarsForGetFileResponse pillar2Response = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                 receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
         pillar2Response.getResponseInfo().setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
         pillar2Response.getResponseInfo().setResponseText("File " +
                 receivedIdentifyRequestMessage.getFileID() + "not present on this pillar " );
         messageBus.sendMessage(pillar2Response);
 
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.FAILED);
     }
 
    @Test(groups = {"regressiontest"})
     public void getFileClientWithChecksumPillarInvolved() throws Exception {
         addDescription("Verify that the GetFile works correctly when a checksum pillar respond.");
 
         addStep("Call getFile form fastest pillar.",
                 "A IDENTIFY_REQUEST_SENT should be generate and a identification request should be sent.");
         GetFileClient client = createGetFileClient();
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         client.getFileFromFastestPillar(DEFAULT_FILE_ID, NO_FILE_PART, httpServer.getURL(DEFAULT_FILE_ID),
                 testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage =
                 collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Send a identification response from pillar1 with a REQUEST_NOT_SUPPORTED response code.",
                 "No events should be generated.");
 
         IdentifyPillarsForGetFileResponse identificationResponse1 = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         identificationResponse1.getResponseInfo().setResponseCode(ResponseCode.REQUEST_NOT_SUPPORTED);
         messageBus.sendMessage(identificationResponse1);
         testEventHandler.verifyNoEventsAreReceived();
 
         addStep("Send a identification response from pillar2 with a IDENTIFICATION_POSITIVE response code .",
                 "A component COMPONENT_IDENTIFIED event shouæd be generated followed by a IDENTIFICATION_COMPLETE.");
         IdentifyPillarsForGetFileResponse identificationResponse2 = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                 receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
         messageBus.sendMessage(identificationResponse2);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
 
         addStep("Verify that the client continues to the performing phase.",
                 "A REQUEST_SENT event should be generated and a GetFileRequest should be sent to pillar2.");
         GetFileRequest getFileRequest = pillar2Destination.waitForMessage(GetFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
 
         addStep("Send a final response upload message",
                 "A COMPONENT_COMPLETE event should be generated followed by at COMPLETE event.");
         GetFileFinalResponse completeMsg = testMessageFactory.createGetFileFinalResponse(
                 getFileRequest, PILLAR2_ID, pillar1DestinationId);
         messageBus.sendMessage(completeMsg);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
     }
 
     @Test(groups = {"regressiontest"})
     public void singleComponentFailure() throws Exception {
         addDescription("Verify that the GetFile reports a complete (not failed), in case of a failing component.");
 
         addStep("Call getFile from the fastest pillar.",
                 "A IDENTIFY_REQUEST_SENT should be generate and a identification request should be sent.");
         GetFileClient client = createGetFileClient();
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         client.getFileFromFastestPillar(DEFAULT_FILE_ID, NO_FILE_PART, httpServer.getURL(DEFAULT_FILE_ID),
                 testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage =
                 collectionReceiver.waitForMessage(IdentifyPillarsForGetFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Send a identification response from pillar1 with a IDENTIFICATION_NEGATIVE response code .",
                 "No events should be generated.");
 
         IdentifyPillarsForGetFileResponse identificationResponse1 = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
         identificationResponse1.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_NEGATIVE);
         messageBus.sendMessage(identificationResponse1);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_FAILED);
 
         addStep("Send a identification response from pillar2 with a IDENTIFICATION_POSITIVE response code .",
                 "A component COMPONENT_IDENTIFIED event shouæd be generated followed by a IDENTIFICATION_COMPLETE.");
         IdentifyPillarsForGetFileResponse identificationResponse2 = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                 receivedIdentifyRequestMessage, PILLAR2_ID, pillar2DestinationId);
         messageBus.sendMessage(identificationResponse2);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
 
         addStep("Verify that the client continues to the performing phase.",
                 "A REQUEST_SENT event should be generated and a GetFileRequest should be sent to pillar2.");
         GetFileRequest getFileRequest = pillar2Destination.waitForMessage(GetFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
 
         addStep("Send a final response upload message",
                 "A COMPONENT_COMPLETE event should be generated followed by at COMPLETE event.");
         GetFileFinalResponse completeMsg = testMessageFactory.createGetFileFinalResponse(
                 getFileRequest, PILLAR2_ID, pillar1DestinationId);
         messageBus.sendMessage(completeMsg);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
     }
 
     /**
      * Creates a new test GetFileClient based on the supplied componentSettings. 
      *
      * Note that the normal way of creating client through the module factory would reuse components with settings from
      * previous tests.
      * @return A new GetFileClient(Wrapper).
      */
     private GetFileClient createGetFileClient() {
         return new GetFileClientTestWrapper(new ConversationBasedGetFileClient(
                 messageBus, conversationMediator, componentSettings, TEST_CLIENT_ID), testEventManager);
     }
 }
