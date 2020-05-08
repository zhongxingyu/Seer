 /*
  * #%L
  * Bitrepository Reference Pillar
  * 
  * $Id: PutFileOnReferencePillarTest.java 589 2011-12-01 15:34:42Z jolf $
  * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/test/java/org/bitrepository/pillar/PutFileOnReferencePillarTest.java $
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
 package org.bitrepository.pillar.referencepillar;
 
 import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumType;
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
 import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.PutFileRequest;
 import org.bitrepository.common.utils.Base16Utils;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.pillar.messagefactories.PutFileMessageFactory;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * Tests the PutFile functionality on the ReferencePillar.
  */
 public class PutFileOnReferencePillarTest extends ReferencePillarTest {
     private PutFileMessageFactory msgFactory;
     String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
     String PUT_CHECKSUM = "940a51b250e7aa82d8e8ea31217ff267";
     Long FILE_SIZE = 1L;
     ChecksumDataForFileTYPE putCsData;
     ChecksumSpecTYPE csSpec;
     
     @BeforeMethod (alwaysRun=true)
     public void initialisePutFileTests() throws Exception {
         msgFactory = new PutFileMessageFactory(clientSettings, getPillarID(), pillarDestinationId);
         
         csSpec = new ChecksumSpecTYPE();
         csSpec.setChecksumType(ChecksumType.MD5);
         
         putCsData = new ChecksumDataForFileTYPE();
         putCsData.setCalculationTimestamp(CalendarUtils.getEpoch());
         putCsData.setChecksumSpec(csSpec);
         putCsData.setChecksumValue(Base16Utils.encodeBase16(PUT_CHECKSUM));
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void referencePillarPutFileTestSuccessCase() throws Exception {
         addDescription("Tests the put functionality of the checksum pillar for the successful scenario.");
         addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
         IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                 DEFAULT_FILE_ID, FILE_SIZE);
         messageBus.sendMessage(identifyRequest);
         
         addStep("Retrieve and validate the response getPillarID() the pillar.", "The pillar should make a response.");
         IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                 IdentifyPillarsForPutFileResponse.class);
         Assert.assertNotNull(receivedIdentifyResponse);
         Assert.assertEquals(receivedIdentifyResponse.getCorrelationID(), identifyRequest.getCorrelationID());
         Assert.assertEquals(receivedIdentifyResponse.getFrom(), getPillarID());
         Assert.assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
         Assert.assertEquals(receivedIdentifyResponse.getReplyTo(), pillarDestinationId);
         Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.IDENTIFICATION_POSITIVE);
         
         addStep("Create and send the actual Put message to the pillar.", 
                 "Should be received and handled by the pillar.");
         PutFileRequest putRequest = msgFactory.createPutFileRequest(putCsData, 
                 csSpec, FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE);
         putRequest.setCorrelationID(identifyRequest.getCorrelationID());
         messageBus.sendMessage(putRequest);
         
         addStep("Retrieve the ProgressResponse for the put request", "The put response should be sent by the pillar.");
         PutFileProgressResponse progressResponse = clientTopic.waitForMessage(PutFileProgressResponse.class);
         Assert.assertNotNull(progressResponse);
         Assert.assertEquals(progressResponse.getCorrelationID(), identifyRequest.getCorrelationID());
         Assert.assertEquals(progressResponse.getFileID(), DEFAULT_FILE_ID);
         Assert.assertEquals(progressResponse.getFrom(), getPillarID());
         Assert.assertEquals(progressResponse.getPillarID(), getPillarID());
         Assert.assertEquals(progressResponse.getReplyTo(), pillarDestinationId);
         Assert.assertEquals(progressResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.OPERATION_ACCEPTED_PROGRESS);
         
         addStep("Retrieve the FinalResponse for the put request", "The put response should be sent by the pillar.");
         PutFileFinalResponse finalResponse = clientTopic.waitForMessage(PutFileFinalResponse.class);
         Assert.assertNotNull(finalResponse);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.
                 OPERATION_COMPLETED);
         Assert.assertEquals(finalResponse.getCorrelationID(), putRequest.getCorrelationID());
         Assert.assertEquals(finalResponse.getFileID(), DEFAULT_FILE_ID);
         Assert.assertEquals(finalResponse.getFrom(), getPillarID());
         Assert.assertEquals(finalResponse.getPillarID(), getPillarID());
         Assert.assertEquals(finalResponse.getReplyTo(), pillarDestinationId);
         
         // validating the checksum
         Assert.assertEquals(finalResponse.getFileID(), DEFAULT_FILE_ID, "The FileID of this test.");
         Assert.assertNotNull(finalResponse.getChecksumDataForNewFile(), "The results should contain a ");
         ChecksumDataForFileTYPE receivedChecksumData = finalResponse.getChecksumDataForNewFile();
         Assert.assertNotNull(receivedChecksumData.getChecksumSpec());
         Assert.assertEquals(receivedChecksumData.getChecksumSpec(), putRequest.getChecksumRequestForNewFile(), 
                 "Should return the same type of checksum as requested.");
         
         addStep("Validate the content of the cache", "Should contain the checksum of the file");
         Assert.assertTrue(archive.hasFile(DEFAULT_FILE_ID));
         Assert.assertFalse(archive.getFile(DEFAULT_FILE_ID).length() == 0);        
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void referencePillarPutFileTestFailedDuplicateFileDuringIdentify() throws Exception {
         addDescription("Tests that the checksum pillar rejects putting a file, which already exists. During the identification fase");
         addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
         initializeArchiveWithEmptyFile();
         IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                 DEFAULT_FILE_ID, FILE_SIZE);
         messageBus.sendMessage(identifyRequest);
         
         addStep("Retrieve and validate the response getPillarID() the pillar.", "The pillar should make a response.");
         IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                 IdentifyPillarsForPutFileResponse.class);
         Assert.assertNotNull(receivedIdentifyResponse);
         Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.DUPLICATE_FILE_FAILURE);
         Assert.assertNotNull(receivedIdentifyResponse.getChecksumDataForExistingFile());
         
         addStep("Validate the content of the cache", "Should contain the checksum of the file");
         Assert.assertTrue(archive.hasFile(DEFAULT_FILE_ID));
         Assert.assertEquals(archive.getFile(DEFAULT_FILE_ID).length(), 0);        
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void referencePillarPutFileTestFailedDuplicateFileDuringOperation() throws Exception {
         addDescription("Tests that the checksum pillar rejects putting a file, which already exists. During the operation fase");
         initializeArchiveWithEmptyFile();
         addStep("Create and send the actual Put message to the pillar.", 
                 "Should be received and handled by the pillar.");
         PutFileRequest putRequest = msgFactory.createPutFileRequest(putCsData, 
                 csSpec, FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE);
         messageBus.sendMessage(putRequest);
         
         addStep("Retrieve the FinalResponse for the put request", "The put response should be sent by the pillar.");
         PutFileFinalResponse finalResponse = clientTopic.waitForMessage(PutFileFinalResponse.class);
         Assert.assertNotNull(finalResponse);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.
                 DUPLICATE_FILE_FAILURE);
         
         addStep("Validate the content of the cache", "Should contain the checksum of the file");
         Assert.assertTrue(archive.hasFile(DEFAULT_FILE_ID));
         Assert.assertEquals(archive.getFile(DEFAULT_FILE_ID).length(), 0);        
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void referencePillarPutFileTestMissingChecksum() throws Exception {
         addDescription("Tests that the checksum pillar rejects putting a file, which already exists. During the operation fase");
         componentSettings.getCollectionSettings().getProtocolSettings().setRequireChecksumForNewFileRequests(true);
         PutFileRequest putRequest = msgFactory.createPutFileRequest(null, csSpec, FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE);
         messageBus.sendMessage(putRequest);
         
         addStep("Retrieve the FinalResponse for the put request", "The put response should be sent by the pillar.");
         PutFileFinalResponse finalResponse = clientTopic.waitForMessage(PutFileFinalResponse.class);
         Assert.assertNotNull(finalResponse);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
         
         addStep("Validate the content of the cache", "Should contain the checksum of the file");
         Assert.assertFalse(archive.hasFile(DEFAULT_FILE_ID));
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void referencePillarPutFileTestBadChecksumArgument() throws Exception {
         addDescription("Tests the file will not be put if a bad checksum is given.");
         Assert.assertTrue(context.getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());
         
         ChecksumDataForFileTYPE badData = new ChecksumDataForFileTYPE();
         badData.setCalculationTimestamp(CalendarUtils.getEpoch());
         badData.setChecksumSpec(csSpec);
         badData.setChecksumValue(Base16Utils.encodeBase16("baabbbaaabba"));
 
         messageBus.sendMessage(msgFactory.createPutFileRequest(badData, 
                 csSpec, FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));
         PutFileFinalResponse finalResponse = clientTopic.waitForMessage(PutFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
         Assert.assertFalse(archive.hasFile(DEFAULT_FILE_ID));
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void referencePillarPutFileTestBadChecksumSpecGiven() throws Exception {
         addDescription("Tests the file will not be put if a bad checksum is given.");
         Assert.assertTrue(context.getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());
         
         ChecksumSpecTYPE badCsType = new ChecksumSpecTYPE();
         badCsType.setChecksumType(ChecksumType.OTHER);
         badCsType.setOtherChecksumType("NOT-EXISTING-TYPE");
         ChecksumDataForFileTYPE badData = new ChecksumDataForFileTYPE();
         badData.setCalculationTimestamp(CalendarUtils.getEpoch());
         badData.setChecksumSpec(badCsType);
         badData.setChecksumValue(Base16Utils.encodeBase16("baabbbaaabba"));
 
         messageBus.sendMessage(msgFactory.createPutFileRequest(badData, 
                 csSpec, FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));
         PutFileFinalResponse finalResponse = clientTopic.waitForMessage(PutFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
         Assert.assertFalse(archive.hasFile(DEFAULT_FILE_ID));
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void referencePillarPutFileTestBadChecksumSpecRequested() throws Exception {
         addDescription("Tests the file will not be put if a bad checksum is given.");
         Assert.assertTrue(context.getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());
         
         ChecksumSpecTYPE badCsType = new ChecksumSpecTYPE();
         badCsType.setChecksumType(ChecksumType.OTHER);
         badCsType.setOtherChecksumType("NOT-EXISTING-TYPE");
 
         messageBus.sendMessage(msgFactory.createPutFileRequest(putCsData, 
                 badCsType, FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));
         PutFileFinalResponse finalResponse = clientTopic.waitForMessage(PutFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
         Assert.assertFalse(archive.hasFile(DEFAULT_FILE_ID));
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void referencePillarPutFileTestBadURL() throws Exception {
         addDescription("Tests the ReferencePillars handling of a bad URL in the PutFile request.");
         Assert.assertTrue(context.getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());
         
         messageBus.sendMessage(msgFactory.createPutFileRequest(putCsData, 
                 csSpec, "http://localhost:1/error", DEFAULT_FILE_ID, FILE_SIZE));
         PutFileFinalResponse finalResponse = clientTopic.waitForMessage(PutFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.FILE_TRANSFER_FAILURE);
         Assert.assertFalse(archive.hasFile(DEFAULT_FILE_ID));
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void referencePillarPutFileTestTooLargeFileInIdentification() throws Exception {
         addDescription("Tests when the PutFile identification delivers a too large file.");
         messageBus.sendMessage(msgFactory.createIdentifyPillarsForPutFileRequest(DEFAULT_FILE_ID, Long.MAX_VALUE));
         IdentifyPillarsForPutFileResponse identifyResponse = clientTopic.waitForMessage(IdentifyPillarsForPutFileResponse.class);
         Assert.assertEquals(identifyResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.FAILURE);
         Assert.assertFalse(archive.hasFile(DEFAULT_FILE_ID));
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void referencePillarPutFileTestTooLargeFileInOperation() throws Exception {
         addDescription("Tests when the PutFile identification delivers a too large file.");
         messageBus.sendMessage(msgFactory.createPutFileRequest(putCsData, 
                 csSpec, FILE_ADDRESS, DEFAULT_FILE_ID, Long.MAX_VALUE));
         PutFileFinalResponse finalResponse = clientTopic.waitForMessage(PutFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.FAILURE);
         Assert.assertFalse(archive.hasFile(DEFAULT_FILE_ID));
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void referencePillarPutFileTestNoChecksumRequired() throws Exception {
         addDescription("Tests that it is possible to put without any checksums if the collection settings allows it.");
         context.getSettings().getCollectionSettings().getProtocolSettings().setRequireChecksumForDestructiveRequests(false);
         Assert.assertFalse(context.getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());
 
         messageBus.sendMessage(msgFactory.createPutFileRequest(null, 
                 null, FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));
         PutFileFinalResponse finalResponse = clientTopic.waitForMessage(PutFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.OPERATION_COMPLETED);
         Assert.assertTrue(archive.hasFile(DEFAULT_FILE_ID));
     }
 
     @Test( groups = {"regressiontest", "pillartest"})
     public void referencePillarPutFileTestNoFileIdOrFileSizeInIdentification() throws Exception {
         addDescription("Tests that it is possible to identify without the fileid or the filesize.");
 
         messageBus.sendMessage(msgFactory.createIdentifyPillarsForPutFileRequest(null, null));
         IdentifyPillarsForPutFileResponse identifyResponse = clientTopic.waitForMessage(IdentifyPillarsForPutFileResponse.class);
         Assert.assertEquals(identifyResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.IDENTIFICATION_POSITIVE);
     }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void referencePillarPutFileWithNullSize() throws Exception {
        addDescription("Tests that it is possible to identify and perform the PutFile operation without the filesize.");

        addStep("Test the Identify", "Should give positive response.");
        messageBus.sendMessage(msgFactory.createIdentifyPillarsForPutFileRequest(DEFAULT_FILE_ID, null));
        IdentifyPillarsForPutFileResponse identifyResponse = clientTopic.waitForMessage(IdentifyPillarsForPutFileResponse.class);
        Assert.assertEquals(identifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);
        
        addStep("Test the operation", "Should complete successfully.");
        messageBus.sendMessage(msgFactory.createPutFileRequest(null, 
                null, FILE_ADDRESS, DEFAULT_FILE_ID, null));
        PutFileFinalResponse finalResponse = clientTopic.waitForMessage(PutFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.OPERATION_COMPLETED);
        Assert.assertTrue(archive.hasFile(DEFAULT_FILE_ID));

    }
 }
