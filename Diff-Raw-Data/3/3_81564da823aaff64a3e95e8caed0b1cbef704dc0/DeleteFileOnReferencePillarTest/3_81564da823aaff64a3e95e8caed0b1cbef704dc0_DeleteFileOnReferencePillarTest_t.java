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
 
 import org.bitrepository.bitrepositoryelements.AlarmCode;
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositorymessages.AlarmMessage;
 import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
 import org.bitrepository.common.utils.TestFileHelper;
 import org.bitrepository.pillar.messagefactories.DeleteFileMessageFactory;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 /**
  * Tests the PutFile functionality on the ReferencePillar.
  */
 public class DeleteFileOnReferencePillarTest extends ReferencePillarTest {
     private DeleteFileMessageFactory msgFactory;
 
     @Override
     public void initializeCUT() {
         super.initializeCUT();
         msgFactory = new DeleteFileMessageFactory(collectionID, settingsForTestClient, getPillarID(),
                 pillarDestinationId);
     }
 
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarDeleteFileTestSuccessTest() throws Exception {
         addDescription("Tests the DeleteFile functionality of the reference pillar for the successful scenario.");
         addStep("Set up constants and variables.", "Should not fail here!");
 
         addStep("Create and send the identify request message.", 
                 "Should be received and handled by the pillar.");
         IdentifyPillarsForDeleteFileRequest identifyRequest =
                 msgFactory.createIdentifyPillarsForDeleteFileRequest(DEFAULT_FILE_ID);
         messageBus.sendMessage(identifyRequest);
         
         addStep("Retrieve and validate the response getPillarID() the pillar.", 
                 "The pillar should make a response.");
         IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                 IdentifyPillarsForDeleteFileResponse.class);
 
         Assert.assertNotNull(receivedIdentifyResponse);
         Assert.assertEquals(receivedIdentifyResponse.getCorrelationID(), identifyRequest.getCorrelationID());
         Assert.assertEquals(receivedIdentifyResponse.getFileID(), DEFAULT_FILE_ID);
         Assert.assertEquals(receivedIdentifyResponse.getFrom(), getPillarID());
         Assert.assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
         Assert.assertEquals(receivedIdentifyResponse.getReplyTo(), pillarDestinationId);
         Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                 ResponseCode.IDENTIFICATION_POSITIVE);
         
         addStep("Create and send the actual DeleteFile message to the pillar.", 
                 "Should be received and handled by the pillar.");
         DeleteFileRequest deleteFileRequest =
                 msgFactory.createDeleteFileRequest(EMPTY_FILE_CHECKSUM_DATA, null, DEFAULT_FILE_ID);
         messageBus.sendMessage(deleteFileRequest);
         
         addStep("Retrieve the ProgressResponse for the DeleteFile request", 
                 "The DeleteFile progress response should be sent by the pillar.");
         DeleteFileProgressResponse progressResponse = clientReceiver.waitForMessage(DeleteFileProgressResponse.class);
         Assert.assertNotNull(progressResponse);
         Assert.assertEquals(progressResponse.getCorrelationID(), deleteFileRequest.getCorrelationID());
         Assert.assertEquals(progressResponse.getFileID(), DEFAULT_FILE_ID);
         Assert.assertEquals(progressResponse.getFrom(), getPillarID());
         Assert.assertEquals(progressResponse.getPillarID(), getPillarID());
         Assert.assertEquals(progressResponse.getReplyTo(), pillarDestinationId);
         Assert.assertEquals(progressResponse.getResponseInfo().getResponseCode(),
                 ResponseCode.OPERATION_ACCEPTED_PROGRESS);
         
         addStep("Retrieve the FinalResponse for the DeleteFile request", 
                 "The DeleteFile response should be sent by the pillar.");
         DeleteFileFinalResponse finalResponse = clientReceiver.waitForMessage(DeleteFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
 
         Assert.assertNotNull(finalResponse);
         Assert.assertEquals(finalResponse.getCorrelationID(), deleteFileRequest.getCorrelationID());
         Assert.assertEquals(finalResponse.getFileID(), DEFAULT_FILE_ID);
         Assert.assertEquals(finalResponse.getFrom(), getPillarID());
         Assert.assertEquals(finalResponse.getPillarID(), getPillarID());
         Assert.assertEquals(finalResponse.getReplyTo(), pillarDestinationId);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(),
                 ResponseCode.OPERATION_COMPLETED);
         alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        Assert.assertEquals(audits.getCallsForAuditEvent(), 1, "Should create 1 audit for the delete operation.");
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarDeleteFileTestFailedNoSuchFile() throws Exception {
         addDescription("Tests the DeleteFile functionality of the reference pillar for the scenario " +
                 "when the file does not exist FILE_NOT_FOUND_FAILURE response.");
 
         addStep("Send a IdentifyPillarsForDeleteFileRequest with a fileID for a non-existing file.",
                 "Should generate a FILE_NOT_FOUND_FAILURE identification response.");
         messageBus.sendMessage(msgFactory.createIdentifyPillarsForDeleteFileRequest(NON_DEFAULT_FILE_ID));
         IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                 IdentifyPillarsForDeleteFileResponse.class);
         Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.FILE_NOT_FOUND_FAILURE);     
     }
 
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarDeleteFileTestFailedNoSuchFileInOperation() throws Exception {
         addDescription("Tests the DeleteFile functionality of the reference pillar for the scenario " +
                 "when the file does not exist.");
 
         addStep("Send a DeleteFileRequest with a fileID for a non-existing file.",
                 "Should generate a FILE_NOT_FOUND_FAILURE response.");
         messageBus.sendMessage(msgFactory.createDeleteFileRequest(
                 TestFileHelper.getDefaultFileChecksum(), null, NON_DEFAULT_FILE_ID));
         DeleteFileFinalResponse finalResponse = clientReceiver.waitForMessage(DeleteFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
     }
     
     @Test( groups = {"regressiontest", "pillartest"})
     public void pillarDeleteFileTestFailedWrongChecksum() throws Exception {
         addDescription("Tests the DeleteFile functionality of the reference pillar for scenario when a wrong "
                 + "checksum is given as argument.");
 
         addStep("Send a DeleteFileRequest with a invalid checksum for the initial file.",
                 "Should cause a EXISTING_FILE_CHECKSUM_FAILURE response to be sent and a .");
         DeleteFileRequest deleteFileRequest =
                 msgFactory.createDeleteFileRequest(TestFileHelper.getDefaultFileChecksum(), null, DEFAULT_FILE_ID);
         messageBus.sendMessage(deleteFileRequest);
         
         addStep("Retrieve the FinalResponse for the DeleteFile request",
                 "The DeleteFile faled response should be sent by the pillar and a alarm should be generated.");
         DeleteFileFinalResponse finalResponse = clientReceiver.waitForMessage(DeleteFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE);
         Assert.assertEquals(alarmReceiver.waitForMessage(AlarmMessage.class).getAlarm().getAlarmCode(),
                 AlarmCode.CHECKSUM_ALARM);
     }
     
 
     @Test( groups = {"regressiontest", "pillartest"})
     public void checksumPillarDeleteFileTestMissingChecksumArgument() throws Exception {
         addDescription("Tests that a missing 'ChecksumOnExistingFile' will not delete the file.");
         Assert.assertTrue(context.getSettings().getRepositorySettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());
         messageBus.sendMessage(msgFactory.createDeleteFileRequest(null, null, DEFAULT_FILE_ID));
         DeleteFileFinalResponse finalResponse = clientReceiver.waitForMessage(DeleteFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE);
         Assert.assertTrue(archives.hasFile(DEFAULT_FILE_ID, collectionID));
     }
 
     @Test( groups = {"regressiontest", "pillartest"})
     public void checksumPillarDeleteFileTestAllowedMissingChecksum() throws Exception {
         addDescription("Tests that a missing 'ChecksumOnExistingFile' will delete the file, when it has been allowed "
                 + "to perform destructive operations in the settings.");
         context.getSettings().getRepositorySettings().getProtocolSettings().setRequireChecksumForDestructiveRequests(false);
         Assert.assertFalse(context.getSettings().getRepositorySettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());
         messageBus.sendMessage(msgFactory.createDeleteFileRequest(null, null, DEFAULT_FILE_ID));
         DeleteFileFinalResponse finalResponse = clientReceiver.waitForMessage(DeleteFileFinalResponse.class);
         Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                 ResponseCode.OPERATION_COMPLETED);
         Assert.assertFalse(archives.hasFile(DEFAULT_FILE_ID, collectionID));
     }
 }
