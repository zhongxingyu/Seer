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
 
 import java.io.File;
 import java.math.BigInteger;
 import java.net.URL;
 import java.util.concurrent.TimeUnit;
 
 import org.bitrepository.access.AccessComponentFactory;
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
 import org.bitrepository.bitrepositoryelements.TimeMeasureUnit;
 import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
 import org.bitrepository.client.TestEventHandler;
 import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
 import org.bitrepository.client.eventhandler.ContributorEvent;
 import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
 import org.bitrepository.protocol.fileexchange.TestFileStore;
 import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
 import org.bitrepository.client.conversation.mediator.ConversationMediator;
 import org.bitrepository.protocol.messagebus.MessageBus;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * Test class for the 'GetFileClient'.
  */
 public class GetFileClientComponentTest extends AbstractGetFileClientTest {
 
     private final String TEST_CLIENT_ID = "test-client"; 
     
     @BeforeMethod (alwaysRun=true)
     @Override
     public void beforeMethodSetup() throws Exception {
         super.beforeMethodSetup();
     }
 
     @Test(groups = {"regressiontest"})
     public void verifyGetFileClientFromFactory() throws Exception {
         Assert.assertTrue(AccessComponentFactory.getInstance().createGetFileClient(settings, securityManager, TEST_CLIENT_ID) 
                 instanceof CollectionBasedGetFileClient, 
                 "The default GetFileClient from the Access factory should be of the type '" + 
                         CollectionBasedGetFileClient.class.getName() + "'.");
     }
 
     @Test(groups = {"regressiontest"})
     public void getFileFromSpecificPillar() throws Exception {
         addDescription("Tests whether a specific message is sent by the GetClient, when only a single pillar " +
                 "participates");
         addStep("Set the number of pillars to 1", "");
 
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
 
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         GetFileClient client = createGetFileClient();
 
         String chosenPillar = settings.getCollectionSettings().getClientSettings().getPillarIDs().get(0);
 
         addStep("Ensure the file isn't already present on the http server", "");
         httpServer.removeFile(DEFAULT_FILE_ID);
         URL url = httpServer.getURL(DEFAULT_FILE_ID);
 
         addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.", 
                 "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
         client.getFileFromSpecificPillar(
                 DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), chosenPillar, testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
         if (useMockupPillar()) {
             receivedIdentifyRequestMessage = collectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
             Assert.assertEquals(receivedIdentifyRequestMessage, 
                     testMessageFactory.createIdentifyPillarsForGetFileRequest(receivedIdentifyRequestMessage, 
                             collectionDestinationID, TEST_CLIENT_ID));
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("The pillar sends a response to the identify message.", 
                 "The callback listener should notify of the response and the client should send a GetFileRequest message to " +
                 "the pillar"); 
 
         GetFileRequest receivedGetFileRequest = null;
         if (useMockupPillar()) {
             IdentifyPillarsForGetFileResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                     receivedIdentifyRequestMessage, chosenPillar, pillar1DestinationId);
             messageBus.sendMessage(identifyResponse);
             receivedGetFileRequest = pillar1Destination.waitForMessage(GetFileRequest.class);
             Assert.assertEquals(receivedGetFileRequest, 
                     testMessageFactory.createGetFileRequest(receivedGetFileRequest, chosenPillar, 
                             pillar1DestinationId, TEST_CLIENT_ID));
         }
 
         for(int i = 0; i < settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.REQUEST_SENT);
 
         addStep("The pillar sends a getFile response to the GetClient.", 
                 "The GetClient should notify about the response through the callback interface."); 
         if (useMockupPillar()) {
             GetFileProgressResponse getFileProgressResponse = testMessageFactory.createGetFileProgressResponse(
                     receivedGetFileRequest, chosenPillar, pillar1DestinationId);
             messageBus.sendMessage(getFileProgressResponse);
         }
 
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PROGRESS);
 
         addStep("The file is uploaded to the indicated url and the pillar sends a final response upload message", 
                 "The GetFileClient notifies that the file is ready through the callback listener and the uploaded " +
                 "file is present.");
         if (useMockupPillar()) {
             httpServer.uploadFile(pillar1FileStore.getFileAsInputstream(receivedGetFileRequest.getFileID()),
                     new URL(receivedGetFileRequest.getFileAddress()));
 
             GetFileFinalResponse completeMsg = testMessageFactory.createGetFileFinalResponse(
                     receivedGetFileRequest, chosenPillar, pillar1DestinationId);
             messageBus.sendMessage(completeMsg);
         }
 
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPLETE);
         if(useMockupPillar()) {
             File expectedUploadFile = pillar1FileStore.getFile(DEFAULT_FILE_ID);
             httpServer.assertFileEquals(expectedUploadFile, receivedGetFileRequest.getFileAddress());
         } else {
             httpServer.assertFileEquals(TestFileStore.DEFAULT_TEST_FILE, url.toExternalForm());
         }
 
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
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(averagePillarID);
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(fastPillarID);
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(slowPillarID);
         GetFileClient client = createGetFileClient();
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
 
         addStep("Defining the variables for the GetFileClient and defining them in the configuration", 
                 "It should be possible to change the values of the configurations.");
 
         addStep("Make the GetClient ask for fastest pillar.", 
                 "It should send message to identify which pillars and a IdentifyPillarsRequestSent notification should be generated.");
         client.getFileFromFastestPillar(DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
         if (useMockupPillar()) {
             receivedIdentifyRequestMessage = 
                     collectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Three pillars send responses. First an average timeToDeliver, then a fast timeToDeliver and last a" +
                 " slow timeToDeliver.", "The client should send a getFileRequest to the fast pillar. " +
                         "The event handler should receive the following events: " +
                 "3 x PillarIdentified, a PillarSelected and a RequestSent");
 
         if (useMockupPillar()) {
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
                     testMessageFactory.createGetFileRequest(receivedGetFileRequest, fastPillarID, 
                             pillar1DestinationId, TEST_CLIENT_ID));
         }
 
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
         ContributorEvent event = (ContributorEvent) testEventHandler.waitForEvent();
         Assert.assertEquals(event.getType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(event.getContributorID(), fastPillarID);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.REQUEST_SENT);
     }
 
     @Test(groups = {"regressiontest"})
     public void chooseFastestPillarGetFileClientWithIdentifyTimeout() throws Exception {
         addDescription("Verify that the FastestPillarGetFile works correct without receiving responses from all " +
                 "pillars.");
         addStep("Create a GetFileClient configured to use 3 pillars and a 3 second timeout for identifying pillar.", "");
 
         String averagePillarID = "THE-AVERAGE-PILLAR";
         String fastPillarID = "THE-FAST-PILLAR";
         String slowPillarID = "THE-SLOW-PILLAR";
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(averagePillarID);
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(fastPillarID);
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(slowPillarID);
         settings.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000));
         GetFileClient client = createGetFileClient();
 
         addStep("Make the GetClient ask for fastest pillar.",  
                 "It should send message to identify which pillar can respond fastest.");
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         client.getFileFromFastestPillar(DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
         if (useMockupPillar()) {
             receivedIdentifyRequestMessage = 
                     collectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Two pillars send responses. First an average timeToDeliver, then a fast timeToDeliver.", 
                 "The client should send a getFileRequest to the fast pillar after 3 seconds. " +
                         "The event handler should receive the following events: " +
                 "2 x PillarIdentified, a identify timeout, a PillarSelected and a RequestSent event.");
 
         if (useMockupPillar()) {
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
 
             GetFileRequest receivedGetFileRequest = pillar1Destination.waitForMessage(
                     GetFileRequest.class, 5, TimeUnit.SECONDS );
             Assert.assertEquals(receivedGetFileRequest, 
                     testMessageFactory.createGetFileRequest(receivedGetFileRequest, fastPillarID, 
                             pillar1DestinationId, TEST_CLIENT_ID));
         }
 
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_TIMEOUT);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.REQUEST_SENT);
     }
 
     @Test(groups = {"regressiontest"})
     public void noIdentifyResponse() throws Exception {
         addDescription("Tests the the GetFileClient handles lack of IdentifyPillarResponses gracefully  ");
         addStep("Set the number of pillars to 1 and a 3 second timeout for identifying pillar.", "");
 
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         settings.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000));
         GetFileClient client = createGetFileClient();
 
         addStep("Make the GetClient ask for fastest pillar.",  
                 "It should send message to identify which pillar can respond fastest.");
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         client.getFileFromFastestPillar(DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), testEventHandler);
         if (useMockupPillar()) {
             collectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("Wait for 5 seconds", "An IdentifyPillarTimeout event should be received");
 
         Assert.assertEquals(testEventHandler.waitForEvent( 5, TimeUnit.SECONDS).getType(), OperationEventType.IDENTIFY_TIMEOUT);
     }
 
     //@Test(groups = {"regressiontest"}) /* Disabled due to not being able to determine why the timeout don't occur */
     public void conversationTimeout() throws Exception {
         addDescription("Tests the the GetFileClient handles lack of IdentifyPillarResponses gracefully  ");
         addStep("Set the number of pillars to 1 and a 3 second timeout for the conversation.", "");
 
         //We need to use a different collection ID to avoid using a existing conversation mediator.
         String newCollectionID = "conversationTimeoutTest";
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         settings.getReferenceSettings().getClientSettings().setConversationTimeout(BigInteger.valueOf(1000));
         GetFileClient client = createGetFileClient();
         
         addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.", 
                 "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         client.getFileFromSpecificPillar( DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), PILLAR1_ID, 
                 testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
         if (useMockupPillar()) {
             receivedIdentifyRequestMessage = 
                     collectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
             IdentifyPillarsForGetFileRequest expectedMessage = 
                     testMessageFactory.createIdentifyPillarsForGetFileRequest(receivedIdentifyRequestMessage, 
                             collectionDestinationID, TEST_CLIENT_ID);
             Assert.assertEquals(receivedIdentifyRequestMessage, expectedMessage);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("The pillar sends a response to the identify message.", 
                 "The callback listener should notify of the response and the client should send a GetFileRequest message to " +
                 "the pillar"); 
 
         if (useMockupPillar()) {
             IdentifyPillarsForGetFileResponse identifyResponse = 
                     testMessageFactory.createIdentifyPillarsForGetFileResponse(
                             receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(identifyResponse);
             pillar1Destination.waitForMessage(GetFileRequest.class);
         }
 
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_IDENTIFIED);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFICATION_COMPLETE);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.REQUEST_SENT);
 
         addStep("Wait for 5 seconds", "An failed event should be received");  
         Assert.assertEquals(testEventHandler.waitForEvent(5, TimeUnit.SECONDS).getType(), OperationEventType.FAILED);
     }
 
     @Test(groups = {"regressiontest"})
     public void testNoSuchFileSpecificPillar() throws Exception {
         addDescription("Testing how a request for a non-existing file is handled on a specific pillar request.");
         addStep("Define 1 pillar.", "");     
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         String fileName = "ERROR-NO-SUCH-FILE-ERROR";
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         URL url = httpServer.getURL(DEFAULT_FILE_ID);
 
         addStep("Call getFileFromSpecificPillar.", 
                 "An identify request should be sent and an IdentifyPillarsRequestSent event should be generate");
         GetFileClient client = createGetFileClient();
 
         client.getFileFromSpecificPillar(fileName, url, PILLAR1_ID, testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = 
                 collectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
         addStep("The specified pillars sends a FILE_NOT_FOUND response", 
                 "The client should generate 1 PillarIdentified event followed by a operation failed event.");
         IdentifyPillarsForGetFileResponse pillar1Response = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                 receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId); 
         pillar1Response.getResponseInfo().setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
         pillar1Response.getResponseInfo().setResponseText("File " + 
                 receivedIdentifyRequestMessage.getFileID() + " not present on this pillar " + PILLAR1_ID);
         messageBus.sendMessage(pillar1Response);
 
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_FAILED);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.FAILED);       
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
         client.getFileFromFastestPillar(fileName, url, testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = 
                 collectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IDENTIFY_REQUEST_SENT);
 
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
 
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_FAILED);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.COMPONENT_FAILED);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.FAILED);       
     }
 
     /**
      * Creates a new test GetFileClient based on the supplied settings. 
      * 
      * Note that the normal way of creating client through the module factory would reuse components with settings from
      * previous tests.
      * @return A new GetFileClient(Wrapper).
      */
     private GetFileClient createGetFileClient() {
         MessageBus messageBus = new ActiveMQMessageBus(settings.getMessageBusConfiguration(), securityManager);
         ConversationMediator conversationMediator = new CollectionBasedConversationMediator(settings, securityManager);
         return new GetFileClientTestWrapper(new CollectionBasedGetFileClient(
                 messageBus, conversationMediator, settings, TEST_CLIENT_ID), testEventManager);
     }
 }
