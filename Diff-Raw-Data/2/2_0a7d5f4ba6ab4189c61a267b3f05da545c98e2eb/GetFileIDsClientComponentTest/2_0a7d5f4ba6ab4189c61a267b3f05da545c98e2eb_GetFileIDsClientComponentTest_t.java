 /*
  * #%L
  * Bitmagasin integrationstest
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
 package org.bitrepository.access.getfileids;
 
 import java.math.BigInteger;
 import java.net.URL;
 import java.util.Date;
 import java.util.concurrent.TimeUnit;
 
 import javax.xml.bind.JAXBException;
 
 import org.bitrepository.access.AccessComponentFactory;
 import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
 import org.bitrepository.bitrepositoryelements.FileIDs;
 import org.bitrepository.bitrepositoryelements.FileIDsData;
 import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
 import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositoryelements.ResponseInfo;
 import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
 import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
 import org.bitrepository.clienttest.DefaultFixtureClientTest;
 import org.bitrepository.clienttest.TestEventHandler;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
 import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
 import org.bitrepository.protocol.fileexchange.TestFileStore;
 import org.bitrepository.protocol.mediator.CollectionBasedConversationMediator;
 import org.bitrepository.protocol.mediator.ConversationMediator;
 import org.bitrepository.protocol.messagebus.MessageBus;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * Test class for the 'GetFileIDsClient'.
  */
 public class GetFileIDsClientComponentTest extends DefaultFixtureClientTest {
 
     private TestGetFileIDsMessageFactory testMessageFactory;
     private TestFileStore pillar1FileStore;
 
     /**
      * Set up the test scenario before running the tests in this class.
      * @throws javax.xml.bind.JAXBException
      */
     @BeforeMethod(alwaysRun = true)
     public void setUp() throws JAXBException {
         // TODO getFileIDsFromFastestPillar settings
         if (useMockupPillar()) {
             testMessageFactory = new TestGetFileIDsMessageFactory(settings.getCollectionID());
             pillar1FileStore = new TestFileStore("Pillar1");
         }
         httpServer.clearFiles();
     }
 
     @Test(groups = {"regressiontest"})
     public void verifyGetFileIDsClientFromFactory() throws Exception {
         Assert.assertTrue(AccessComponentFactory.getInstance().createGetFileIDsClient(settings) 
                 instanceof ConversationBasedGetFileIDsClient, 
                 "The default GetFileClient from the Access factory should be of the type '" + 
                         ConversationBasedGetFileIDsClient.class.getName() + "'.");
     }
     
     @Test(groups = {"regressiontest"})
     public void getFileIDsDeliveredAtUrl() throws Exception {
         addDescription("Tests the delivery of fileIDs from a pillar at a given URL.");
         addStep("Initialise the variables for this test.", 
                 "EventManager and GetFileIDsClient should be instantiated.");
 
         String deliveryFilename = "TEST-FILE-IDS-DELIVERY.xml";
         FileIDs fileIDs = new FileIDs();
         fileIDs.getFileID().add(DEFAULT_FILE_ID);
         
         if(useMockupPillar()) {
             settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
             settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         }
 
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         GetFileIDsClient getFileIDsClient = createGetFileIDsClient();
 
         addStep("Ensure the delivery file isn't already present on the http server", 
         "Should be remove if it already exists.");
         httpServer.removeFile(deliveryFilename);
         URL deliveryUrl = httpServer.getURL(deliveryFilename);
 
         addStep("Request the delivery of the file ids of a file from the pillar(s). A callback listener should be supplied.", 
                 "A IdentifyPillarsForGetFileIDsRequest will be sent to the pillar(s).");
         getFileIDsClient.getFileIDs(settings.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs, 
                 deliveryUrl, testEventHandler, "TEST-AUDIT");
 
         IdentifyPillarsForGetFileIDsRequest receivedIdentifyRequestMessage = null;
         if (useMockupPillar()) {
             receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
                     IdentifyPillarsForGetFileIDsRequest.class);
             Assert.assertEquals(receivedIdentifyRequestMessage, 
                     testMessageFactory.createIdentifyPillarsForGetFileIDsRequest(receivedIdentifyRequestMessage, 
                             collectionDestinationID));
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IdentifyPillarsRequestSent);
 
         addStep("The pillar sends a response to the identify message.", 
                 "The callback listener should notify of the response and the client should send a GetFileIDsRequest "
                 + "message to the pillar"); 
 
         GetFileIDsRequest receivedGetFileIDsRequest = null;
         if (useMockupPillar()) {
             IdentifyPillarsForGetFileIDsResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetFileIDsResponse(
                     receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(identifyResponse);
             receivedGetFileIDsRequest = pillar1Destination.waitForMessage(GetFileIDsRequest.class);
             Assert.assertEquals(receivedGetFileIDsRequest, 
                     testMessageFactory.createGetFileIDsRequest(receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId));
         }
 
         for(int i = 0; i < settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
 
         addStep("The pillar sends a getFileIDsProgressResponse to the GetFileIDsClient.", 
                 "The GetFileIDsClient should notify about the response through the callback interface."); 
         if (useMockupPillar()) {
             GetFileIDsProgressResponse getFileIDsProgressResponse = testMessageFactory.createGetFileIDsProgressResponse(
                     receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(getFileIDsProgressResponse);
         }
         
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Progress);
 
         addStep("The resulting file is uploaded to the indicated url and the pillar sends a final response upload message", 
                 "The GetFileIDsClient notifies that the file is ready through the callback listener and the uploaded file is present.");
         if (useMockupPillar()) {
             GetFileIDsFinalResponse completeMsg = testMessageFactory.createGetFileIDsFinalResponse(
                     receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);
             
             ResultingFileIDs res = new ResultingFileIDs();
             res.setResultAddress(receivedGetFileIDsRequest.getResultAddress());
             completeMsg.setResultingFileIDs(res);
             
             messageBus.sendMessage(completeMsg);
         }
 
         addStep("Receive and validate event results for the pillar.", 
                 "Should be a FileIDsCompletePillarEvent with the ResultingFileIDs containing only the URL.");
         for(String pillar : settings.getCollectionSettings().getClientSettings().getPillarIDs()) {
             FileIDsCompletePillarEvent event = (FileIDsCompletePillarEvent) testEventHandler.waitForEvent();
             Assert.assertEquals(event.getType(), OperationEventType.PillarComplete);
             ResultingFileIDs resFileIDs = event.getFileIDs();
             Assert.assertNotNull(resFileIDs, "The ResultingFileIDs may not be null.");
             Assert.assertTrue(resFileIDs.getResultAddress().contains(deliveryUrl.toExternalForm()), 
                     "The resulting address'" + resFileIDs.getResultAddress() + "' should contain the argument address: '"
                     + deliveryUrl.toExternalForm() + "'");
             Assert.assertNull(resFileIDs.getFileIDsData(), "No FileIDsData should be returned.");
         }
         
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Complete);
     }
     
     @Test(groups = {"regressiontest"})
     public void getFileIDsDeliveredThroughMessage() throws Exception {
         addDescription("Tests the delivery of fileIDs from a pillar at a given URL.");
         addStep("Initialise the variables for this test.", 
                 "EventManager and GetFileIDsClient should be instantiated.");
 
         String deliveryFilename = "TEST-FILE-IDS-DELIVERY.xml";
         FileIDs fileIDs = new FileIDs();
         fileIDs.getFileID().add(DEFAULT_FILE_ID);
         
         if(useMockupPillar()) {
             settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
             settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         }
 
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         GetFileIDsClient getFileIDsClient = createGetFileIDsClient();
 
         addStep("Ensure the delivery file isn't already present on the http server", 
         "Should be remove if it already exists.");
         httpServer.removeFile(deliveryFilename);
 
         addStep("Request the delivery of the file ids of a file from the pillar(s). A callback listener should be supplied.", 
                 "A IdentifyPillarsForGetFileIDsRequest will be sent to the pillar(s).");
         getFileIDsClient.getFileIDs(settings.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs, 
                 null, testEventHandler, "TEST-AUDIT");
 
         IdentifyPillarsForGetFileIDsRequest receivedIdentifyRequestMessage = null;
         if (useMockupPillar()) {
             receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
                     IdentifyPillarsForGetFileIDsRequest.class);
             Assert.assertEquals(receivedIdentifyRequestMessage, 
                     testMessageFactory.createIdentifyPillarsForGetFileIDsRequest(receivedIdentifyRequestMessage, 
                             collectionDestinationID));
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IdentifyPillarsRequestSent);
 
         addStep("The pillar sends a response to the identify message.", 
                 "The callback listener should notify of the response and the client should send a GetFileIDsRequest "
                 + "message to the pillar"); 
 
         GetFileIDsRequest receivedGetFileIDsRequest = null;
         if (useMockupPillar()) {
             IdentifyPillarsForGetFileIDsResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetFileIDsResponse(
                     receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(identifyResponse);
             receivedGetFileIDsRequest = pillar1Destination.waitForMessage(GetFileIDsRequest.class);
             Assert.assertEquals(receivedGetFileIDsRequest, 
                     testMessageFactory.createGetFileIDsRequest(receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId));
         }
 
         for(int i = 0; i < settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
 
         addStep("The pillar sends a getFileIDsProgressResponse to the GetFileIDsClient.", 
                 "The GetFileIDsClient should notify about the response through the callback interface."); 
         if (useMockupPillar()) {
             GetFileIDsProgressResponse getFileIDsProgressResponse = testMessageFactory.createGetFileIDsProgressResponse(
                     receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(getFileIDsProgressResponse);
         }
         
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Progress);
 
         addStep("The resulting file is uploaded to the indicated url and the pillar sends a final response upload message", 
                 "The GetFileIDsClient notifies that the file is ready through the callback listener and the uploaded file is present.");
         if (useMockupPillar()) {
             GetFileIDsFinalResponse completeMsg = testMessageFactory.createGetFileIDsFinalResponse(
                     receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);
             
             ResultingFileIDs res = new ResultingFileIDs();
             FileIDsData fileIDsData = new FileIDsData();
             FileIDsDataItems fiddItems = new FileIDsDataItems();
             for(String fileID : receivedGetFileIDsRequest.getFileIDs().getFileID()) {
                 FileIDsDataItem fidItem = new FileIDsDataItem();
                fidItem.setCreationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
                 fidItem.setFileID(fileID);
                 fiddItems.getFileIDsDataItem().add(fidItem);
             }
             fileIDsData.setFileIDsDataItems(fiddItems);
             res.setFileIDsData(fileIDsData);
             completeMsg.setResultingFileIDs(res);
             
             messageBus.sendMessage(completeMsg);
         }
 
         addStep("Receive and validate event results for the pillar.", 
                 "Should be a FileIDsCompletePillarEvent with the ResultingFileIDs containing the list of fileids.");
         for(String pillar : settings.getCollectionSettings().getClientSettings().getPillarIDs()) {
             FileIDsCompletePillarEvent event = (FileIDsCompletePillarEvent) testEventHandler.waitForEvent();
             Assert.assertEquals(event.getType(), OperationEventType.PillarComplete);
             ResultingFileIDs resFileIDs = event.getFileIDs();
             Assert.assertNotNull(resFileIDs, "The ResultingFileIDs may not be null.");
             Assert.assertNull(resFileIDs.getResultAddress(), "The results should be sent back through the message, "
                     + "and therefore no resulting address should be returned.");
             Assert.assertNotNull(resFileIDs.getFileIDsData(), "No FileIDsData should be returned.");
             Assert.assertEquals(resFileIDs.getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size(),
                     fileIDs.getFileID().size(), "Response should contain same amount of fileids as requested.");
         }
         
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Complete);
     }
     
     @Test(groups = {"regressiontest"})
     public void noIdentifyResponses() throws Exception {
         addDescription("Tests the GetFileIDsClient handles lack of IdentifyPillarResponses gracefully.");
         addStep("Set the number of pillars for this SLA to 1 and a 3 second timeout for identifying pillar.", "");
         
         String deliveryFilename = "TEST-FILE-ID-DELIVERY.xml";
         FileIDs fileIDs = new FileIDs();
         fileIDs.getFileID().add(DEFAULT_FILE_ID);
         
         settings.getCollectionSettings().getClientSettings().setIdentificationTimeout(BigInteger.valueOf(3000));
 
         if(useMockupPillar()) {
             settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
             settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         }
 
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         GetFileIDsClient GetFileIDsClient = createGetFileIDsClient();
 
         addStep("Ensure the delivery file isn't already present on the http server", 
                 "Should be remove if it already exists.");
         httpServer.removeFile(deliveryFilename);
         URL deliveryUrl = httpServer.getURL(deliveryFilename);
 
         addStep("Request the delivery of the file id of a file from the pillar(s). A callback listener should be supplied.", 
                 "A IdentifyPillarsForGetFileIDsRequest will be sent to the pillar(s).");
         GetFileIDsClient.getFileIDs(settings.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs, 
                 deliveryUrl, testEventHandler, "TEST-AUDIT");
 
         if (useMockupPillar()) {
             collectionDestination.waitForMessage(IdentifyPillarsForGetFileIDsRequest.class);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IdentifyPillarsRequestSent);
 
         addStep("Wait for at least 3 seconds", "An IdentifyPillarTimeout event should be received");
         Assert.assertEquals(testEventHandler.waitForEvent( 4, TimeUnit.SECONDS).getType(), OperationEventType.Failed);
     }
 
     @Test(groups = {"regressiontest"})
     public void conversationTimeout() throws Exception {
         addDescription("Tests the GetFileIDClient handles lack of GetFileIDsResponses gracefully");
         addStep("Set the number of pillars for this Collection to 1 and a 3 second timeout for the conversation.", 
                 "Should not be able to fail here.");
 
         String deliveryFilename = "TEST-FILE-IDS-DELIVERY.xml";
         FileIDs fileIDs = new FileIDs();
         fileIDs.getFileID().add(DEFAULT_FILE_ID);
         
         settings.getCollectionSettings().getClientSettings().setOperationTimeout(BigInteger.valueOf(3000));
 
         if(useMockupPillar()) {
             settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
             settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         }
 
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         GetFileIDsClient GetFileIDsClient = createGetFileIDsClient();
 
         addStep("Ensure the delivery file isn't already present on the http server", 
                 "Should be remove if it already exists.");
         httpServer.removeFile(deliveryFilename);
         URL deliveryUrl = httpServer.getURL(deliveryFilename);
 
         addStep("Request the delivery of the file id of a file from the pillar(s). A callback listener should be supplied.", 
                 "A IdentifyPillarsForGetFileIDsRequest will be sent to the pillar(s).");
         GetFileIDsClient.getFileIDs(settings.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs, 
                 deliveryUrl, testEventHandler, "TEST-AUDIT");
 
         IdentifyPillarsForGetFileIDsRequest receivedIdentifyRequestMessage = null;
         if (useMockupPillar()) {
             receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
                     IdentifyPillarsForGetFileIDsRequest.class);
             Assert.assertEquals(receivedIdentifyRequestMessage, 
                     testMessageFactory.createIdentifyPillarsForGetFileIDsRequest(receivedIdentifyRequestMessage, 
                             collectionDestinationID));
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IdentifyPillarsRequestSent);
 
         addStep("The pillar sends a response to the identify message.", 
                 "The callback listener should notify of the response and the client should send a GetFileIDsRequest "
                 + "message to the pillar"); 
 
         GetFileIDsRequest receivedGetFileIDsRequest = null;
         if (useMockupPillar()) {
             IdentifyPillarsForGetFileIDsResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetFileIDsResponse(
                     receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(identifyResponse);
             receivedGetFileIDsRequest = pillar1Destination.waitForMessage(GetFileIDsRequest.class);
             Assert.assertEquals(receivedGetFileIDsRequest, 
                     testMessageFactory.createGetFileIDsRequest(receivedGetFileIDsRequest,PILLAR1_ID, pillar1DestinationId));
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
         
         addStep("Wait for at least 3 seconds", "An IdentifyPillarTimeout event should be received");
         Assert.assertEquals(testEventHandler.waitForEvent( 4, TimeUnit.SECONDS).getType(), OperationEventType.Failed);
     }
     
     @Test(groups = {"regressiontest"})
     public void testNoSuchFile() throws Exception {
         addDescription("Testing how a request for a non-existing file is handled.");
         addStep("Setting up variables and such.", "Should be OK.");
         
         String deliveryFilename = "TEST-FILE-IDS-DELIVERY.xml";
         FileIDs fileIDs = new FileIDs();
         fileIDs.getFileID().add(DEFAULT_FILE_ID);
         
         if(useMockupPillar()) {
             settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
             settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR1_ID);
         }
 
         TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
         GetFileIDsClient GetFileIDsClient = createGetFileIDsClient();
 
         addStep("Ensure the delivery file isn't already present on the http server", 
         "Should be remove if it already exists.");
         httpServer.removeFile(deliveryFilename);
         URL deliveryUrl = httpServer.getURL(deliveryFilename);
 
         addStep("Request the delivery of the file id of a file from the pillar(s). A callback listener should be supplied.", 
         "A IdentifyPillarsForGetFileIDsRequest will be sent to the pillar(s).");
         GetFileIDsClient.getFileIDs(settings.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs, 
                 deliveryUrl, testEventHandler, "TEST-AUDIT");
 
         IdentifyPillarsForGetFileIDsRequest receivedIdentifyRequestMessage = null;
         if (useMockupPillar()) {
             receivedIdentifyRequestMessage = collectionDestination.waitForMessage(
                     IdentifyPillarsForGetFileIDsRequest.class);
             Assert.assertEquals(receivedIdentifyRequestMessage, 
                     testMessageFactory.createIdentifyPillarsForGetFileIDsRequest(receivedIdentifyRequestMessage, 
                             collectionDestinationID));
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IdentifyPillarsRequestSent);
 
         addStep("The pillar sends a response to the identify message.", 
                 "The callback listener should notify of the response and the client should send a GetFileIDsRequest "
                 + "message to the pillar"); 
 
         GetFileIDsRequest receivedGetFileIDsRequest = null;
         if (useMockupPillar()) {
             IdentifyPillarsForGetFileIDsResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetFileIDsResponse(
                     receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
             messageBus.sendMessage(identifyResponse);
             receivedGetFileIDsRequest = pillar1Destination.waitForMessage(GetFileIDsRequest.class);
             Assert.assertEquals(receivedGetFileIDsRequest, 
                     testMessageFactory.createGetFileIDsRequest(receivedGetFileIDsRequest,PILLAR1_ID, pillar1DestinationId));
         }
 
         for(int i = 0; i < settings.getCollectionSettings().getClientSettings().getPillarIDs().size(); i++) {
             Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
         }
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
 
         addStep("Send a error that the file cannot be found.", "Should trigger a 'event failed'.");
         if (useMockupPillar()) {
             GetFileIDsFinalResponse completeMsg = testMessageFactory.createGetFileIDsFinalResponse(
                     receivedGetFileIDsRequest, PILLAR1_ID, pillar1DestinationId);
             
             ResponseInfo rfInfo = new ResponseInfo();
             rfInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND);
             rfInfo.setResponseText("No such file.");
             completeMsg.setResponseInfo(rfInfo);
             completeMsg.setResultingFileIDs(null);
             
             messageBus.sendMessage(completeMsg);
         }
 
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarFailed);
         Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Complete);
     }
 
     
     /**
      * Creates a new test GetFileIDsClient based on the supplied settings. 
      * 
      * Note that the normal way of creating client through the module factory would reuse components with settings from
      * previous tests.
      * @return A new GetFileIDsClient(Wrapper).
      */
     private GetFileIDsClient createGetFileIDsClient() {
         MessageBus messageBus = new ActiveMQMessageBus(settings.getMessageBusConfiguration());
         ConversationMediator conversationMediator = new CollectionBasedConversationMediator(settings);
         return new GetFileIDsClientTestWrapper(new ConversationBasedGetFileIDsClient(
                 messageBus, conversationMediator, settings), testEventManager);
     }
 }
