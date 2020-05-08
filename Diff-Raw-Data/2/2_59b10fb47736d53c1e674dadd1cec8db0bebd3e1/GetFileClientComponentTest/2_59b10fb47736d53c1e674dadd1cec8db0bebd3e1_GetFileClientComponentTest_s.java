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
 import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
 import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
 import org.bitrepository.clienttest.DefaultFixtureClientTest;
 import org.bitrepository.clienttest.TestEventHandler;
 import org.bitrepository.protocol.bitrepositorycollection.MutableClientSettings;
 import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
 import org.bitrepository.protocol.eventhandler.PillarOperationEvent;
 import org.bitrepository.protocol.fileexchange.TestFileStore;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * Test class for the 'GetFileClient'.
  */
 public class GetFileClientComponentTest extends DefaultFixtureClientTest {
     private TestGetFileMessageFactory testMessageFactory;
     private TestFileStore pillar1FileStore;
     private TestFileStore pillar2FileStore;
     private MutableGetFileClientSettings getFileClientSettings;
 
     @BeforeMethod (alwaysRun=true)
     public void beforeMethodSetup() throws Exception {
         getFileClientSettings = new MutableGetFileClientSettings(settings);
         getFileClientSettings.setGetFileDefaultTimeout(1000);
 
         if (useMockupPillar()) {
             testMessageFactory = new TestGetFileMessageFactory(settings.getBitRepositoryCollectionID());
             pillar1FileStore = new TestFileStore("Pillar1");
             pillar2FileStore = new TestFileStore("Pillar2");
             // The following line is also relevant for non-mockup senarios, where the pillars needs to be initialized 
             // with content.
         }
         httpServer.clearFiles();
     }
 
     @Test(groups = {"regressiontest"})
     public void verifyGetFileClientFromFactory() throws Exception {
         Assert.assertTrue(AccessComponentFactory.getInstance().createGetFileClient(getFileClientSettings) 
                 instanceof SimpleGetFileClient, 
                 "The default GetFileClient from the Access factory should be of the type '" + 
                 SimpleGetFileClient.class.getName() + "'.");
     }
 
     @Test(groups = {"regressiontest"})
     public void getFileFromSpecificPillar() throws Exception {
         addDescription("Tests whether a specific message is sent by the GetClient, when only a single pillar " +
         "participates");
         addStep("Set the number of pillars for this SLA to 1", "");
 
         ((MutableClientSettings)getFileClientSettings).setPillarIDs(new String[] {PILLAR1_ID});
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         GetFileClient getFileClient = 
             new GetFileClientTestWrapper(AccessComponentFactory.getInstance().createGetFileClient(getFileClientSettings), 
                     testEventManager);
 
         addStep("Ensure the file isn't already present on the http server", "");
         httpServer.removeFile(DEFAULT_FILE_ID);
 
         addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.", 
         "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
         getFileClient.getFileFromSpecificPillar(
         		DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), PILLAR1_ID, testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
         if (useMockupPillar()) {
             receivedIdentifyRequestMessage = bitRepositoryCollectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
             Assert.assertEquals(receivedIdentifyRequestMessage, 
                     testMessageFactory.createIdentifyPillarsForGetFileRequest(receivedIdentifyRequestMessage, 
                             bitRepositoryCollectionDestinationID));
         }
 
         addStep("The pillar sends a response to the identify message.", 
                 "The callback listener should notify of the response and the client should send a GetFileRequest message to " +
         "the pillar"); 
 
         GetFileRequest receivedGetFileRequest = null;
         if (useMockupPillar()) {
             IdentifyPillarsForGetFileResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                     receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(identifyResponse);
             receivedGetFileRequest = pillar1Destination.waitForMessage(GetFileRequest.class);
             Assert.assertEquals(receivedGetFileRequest, 
                     testMessageFactory.createGetFileRequest(receivedGetFileRequest,PILLAR1_ID, pillar1DestinationId));
         }
 
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
 
         addStep("The pillar sends a getFile response to the GetClient.", 
         "The GetClient should notify about the response through the callback interface."); 
         if (useMockupPillar()) {
             GetFileProgressResponse getFileProgressResponse = testMessageFactory.createGetFileProgressResponse(
                     receivedGetFileRequest, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(getFileProgressResponse);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Progress);
 
         addStep("The file is uploaded to the indicated url and the pillar sends a final response upload message", 
                 "The GetFileClient notifies that the file is ready through the callback listener and the uploaded " +
         "file is present.");
         if (useMockupPillar()) {
             httpServer.uploadFile(pillar1FileStore.getInputstream(receivedGetFileRequest.getFileID()),
                     new URL(receivedGetFileRequest.getFileAddress()));
 
             GetFileFinalResponse completeMsg = testMessageFactory.createGetFileFinalResponse(
                     receivedGetFileRequest, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(completeMsg);
         }
 
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Complete);
         File expectedUploadFile = pillar1FileStore.getFile(DEFAULT_FILE_ID);
         httpServer.assertFileEquals(expectedUploadFile, receivedGetFileRequest.getFileAddress());
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
         ((MutableClientSettings)getFileClientSettings).setPillarIDs(
                 new String[] {averagePillarID, fastPillarID, slowPillarID});
         GetFileClient getFileClient = 
             new GetFileClientTestWrapper(AccessComponentFactory.getInstance().createGetFileClient(getFileClientSettings), 
                     testEventManager);
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
 
         addStep("Defining the variables for the GetFileClient and defining them in the configuration", 
         "It should be possible to change the values of the configurations.");
 
         addStep("Make the GetClient ask for fastest pillar.", 
         "It should send message to identify which pillars.");
         getFileClient.getFileFromFastestPillar(DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
         if (useMockupPillar()) {
             receivedIdentifyRequestMessage = 
                 bitRepositoryCollectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
         }
 
         addStep("Three pillars send responses. First an average timeToDeliver, then a fast timeToDeliver and last a" +
                 " slow timeToDeliver.", "The client should send a getFileRequest to the fast pillar. " +
                 		"The event handler should receive the following events: " +
                 		"3 x PillarIdentified, a PillarSelected and a RequestSent");
 
         if (useMockupPillar()) {
             IdentifyPillarsForGetFileResponse averageReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                     receivedIdentifyRequestMessage, averagePillarID, pillar2DestinationId);
             TimeMeasureTYPE averageTime = new TimeMeasureTYPE();
             averageTime.setTimeMeasureUnit("MILLISECONDS");
             averageTime.setTimeMeasureValue(BigInteger.valueOf(100L));
             averageReply.setTimeToDeliver(averageTime);
             messageBus.sendMessage(averageReply);
 
             IdentifyPillarsForGetFileResponse fastReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                     receivedIdentifyRequestMessage, fastPillarID, pillar1DestinationId);
             TimeMeasureTYPE fastTime = new TimeMeasureTYPE();
             fastTime.setTimeMeasureUnit("MILLISECONDS");
             fastTime.setTimeMeasureValue(BigInteger.valueOf(10L));
             fastReply.setTimeToDeliver(fastTime);
             messageBus.sendMessage(fastReply);
 
             IdentifyPillarsForGetFileResponse slowReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                     receivedIdentifyRequestMessage, slowPillarID, pillar2DestinationId);
             TimeMeasureTYPE slowTime = new TimeMeasureTYPE();
             slowTime.setTimeMeasureValue(BigInteger.valueOf(1L));
             slowTime.setTimeMeasureUnit("HOURS");
             slowReply.setTimeToDeliver(slowTime);
             messageBus.sendMessage(slowReply);
 
             GetFileRequest receivedGetFileRequest = pillar1Destination.waitForMessage(GetFileRequest.class);
             Assert.assertEquals(receivedGetFileRequest, 
                     testMessageFactory.createGetFileRequest(receivedGetFileRequest, fastPillarID, pillar1DestinationId));
         }
         
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
         PillarOperationEvent event = (PillarOperationEvent) testEventHandler.waitForEvent();
         Assert.assertEquals(event.getType(), OperationEventType.PillarSelected);
         Assert.assertEquals(event.getState(), fastPillarID);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
     }
     
     @Test(groups = {"regressiontest"})
     public void chooseFastestPillarGetFileClientWithIdentifyTimeout() throws Exception {
         addDescription("Verify that the FastestPillarGetFile works correct without receiving responses from all" +
         		"pillars.");
         addStep("Create a GetFileClient configured to use 3 pillars and a 3 second timeout for identifying pillar.", "");
 
         String averagePillarID = "THE-AVERAGE-PILLAR";
         String fastPillarID = "THE-FAST-PILLAR";
         String slowPillarID = "THE-SLOW-PILLAR";
         ((MutableClientSettings)getFileClientSettings).setPillarIDs(
                 new String[] {averagePillarID, fastPillarID, slowPillarID});
         ((MutableClientSettings)getFileClientSettings).setIdentifyPillarsTimeout(3000);
         GetFileClient getFileClient = 
             new GetFileClientTestWrapper(AccessComponentFactory.getInstance().createGetFileClient(getFileClientSettings), 
                     testEventManager);
 
         addStep("Make the GetClient ask for fastest pillar.",  
                 "It should send message to identify which pillar can respond fastest.");
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         getFileClient.getFileFromFastestPillar(DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
         if (useMockupPillar()) {
             receivedIdentifyRequestMessage = 
                 bitRepositoryCollectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
         }
 
         addStep("Two pillars send responses. First an average timeToDeliver, then a fast timeToDeliver.", 
         		"The client should send a getFileRequest to the fast pillar after 3 seconds. " +
                 		"The event handler should receive the following events: " +
                 		"2 x PillarIdentified, a identify timeout, a PillarSelected and a RequestSent event.");
 
         if (useMockupPillar()) {
             IdentifyPillarsForGetFileResponse averageReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                     receivedIdentifyRequestMessage, averagePillarID, pillar2DestinationId);
             TimeMeasureTYPE averageTime = new TimeMeasureTYPE();
             averageTime.setTimeMeasureUnit("MILLISECONDS");
             averageTime.setTimeMeasureValue(BigInteger.valueOf(100L));
             averageReply.setTimeToDeliver(averageTime);
             messageBus.sendMessage(averageReply);
 
             IdentifyPillarsForGetFileResponse fastReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                     receivedIdentifyRequestMessage, fastPillarID, pillar1DestinationId);
             TimeMeasureTYPE fastTime = new TimeMeasureTYPE();
             fastTime.setTimeMeasureUnit("MILLISECONDS");
             fastTime.setTimeMeasureValue(BigInteger.valueOf(10L));
             fastReply.setTimeToDeliver(fastTime);
             messageBus.sendMessage(fastReply);
 
             GetFileRequest receivedGetFileRequest = pillar1Destination.waitForMessage(
             		GetFileRequest.class, 5, TimeUnit.SECONDS );
             Assert.assertEquals(receivedGetFileRequest, 
                     testMessageFactory.createGetFileRequest(receivedGetFileRequest, fastPillarID, pillar1DestinationId));
         }
         
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IdentifyPillarTimeout);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
     }
     
     @Test(groups = {"regressiontest"})
     public void noIdentifyResponses() throws Exception {
         addDescription("Tests the th eGetFileClient handles lack of IdentifyPillarResponses gracefully  ");
         addStep("Set the number of pillars for this SLA to 1 and a 3 second timeout for identifying pillar.", "");
 
         ((MutableClientSettings)getFileClientSettings).setPillarIDs(new String[] {PILLAR1_ID});
         ((MutableClientSettings)getFileClientSettings).setIdentifyPillarsTimeout(3000);
         GetFileClient getFileClient = 
                 new GetFileClientTestWrapper(AccessComponentFactory.getInstance().createGetFileClient(getFileClientSettings), 
                         testEventManager);
         
         addStep("Make the GetClient ask for fastest pillar.",  
                 "It should send message to identify which pillar can respond fastest.");
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         getFileClient.getFileFromFastestPillar(DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), testEventHandler);
         if (useMockupPillar()) {
                 bitRepositoryCollectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
         }
         
         addStep("Wait for at least 3 seconds", "An IdentifyPillarTimeout event should be received");
         
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.NoPillarFound);
     }
     
 
     @Test(groups = {"testfirst"})
     public void conversationTimeout() throws Exception {
         addDescription("Tests the th eGetFileClient handles lack of IdentifyPillarResponses gracefully  ");
         addStep("Set the number of pillars for this SLA to 1 and a 3 second timeout for the conversation.", "");
 
         ((MutableClientSettings)getFileClientSettings).setPillarIDs(new String[] {PILLAR1_ID});
         ((MutableClientSettings)getFileClientSettings).setConversationTimeout(3000);
         GetFileClient getFileClient = 
                 new GetFileClientTestWrapper(AccessComponentFactory.getInstance().createGetFileClient(getFileClientSettings), 
                         testEventManager);
         
         addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.", 
         "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         getFileClient.getFileFromSpecificPillar(
                 DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), PILLAR1_ID, testEventHandler);
         IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
         if (useMockupPillar()) {
             receivedIdentifyRequestMessage = bitRepositoryCollectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
             Assert.assertEquals(receivedIdentifyRequestMessage, 
                     testMessageFactory.createIdentifyPillarsForGetFileRequest(receivedIdentifyRequestMessage, 
                             bitRepositoryCollectionDestinationID));
         }
 
         addStep("The pillar sends a response to the identify message.", 
                 "The callback listener should notify of the response and the client should send a GetFileRequest message to " +
         "the pillar"); 
         
         if (useMockupPillar()) {
             IdentifyPillarsForGetFileResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                     receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(identifyResponse);
             pillar1Destination.waitForMessage(GetFileRequest.class);
         }
 
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
         
         addStep("Wait for at least 3 seconds", "An failed event should be received");  
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Failed);
     }
 }
