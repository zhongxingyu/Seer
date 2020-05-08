 /*
  * #%L
  * bitrepository-access-client
  * *
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
 package org.bitrepository.pillar.messagehandler;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
import org.bitrepository.bitrepositorydata.GetChecksumsResults;
 import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumsDataGroupedByChecksumSpec;
 import org.bitrepository.bitrepositoryelements.FileIDs;
 import org.bitrepository.bitrepositoryelements.ResponseCode;
 import org.bitrepository.bitrepositoryelements.ResponseInfo;
 import org.bitrepository.bitrepositoryelements.ResultingChecksums;
 import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.common.JaxbHelper;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.common.utils.ChecksumUtils;
 import org.bitrepository.pillar.ReferenceArchive;
 import org.bitrepository.protocol.FileExchange;
 import org.bitrepository.protocol.ProtocolComponentFactory;
 import org.bitrepository.protocol.messagebus.MessageBus;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class for performing the GetChecksums operation for this pillar.
  */
 public class GetChecksumsRequestHandler extends PillarMessageHandler<GetChecksumsRequest> {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
     
     /**
      * Constructor.
      * @param settings The settings for handling the message.
      * @param messageBus The bus for communication.
      * @param alarmDispatcher The dispatcher of alarms.
      * @param referenceArchive The archive for the data.
      */
     public GetChecksumsRequestHandler(Settings settings, MessageBus messageBus,
             AlarmDispatcher alarmDispatcher, ReferenceArchive referenceArchive) {
         super(settings, messageBus, alarmDispatcher, referenceArchive);
     }
     
     /**
      * Handles the messages for the GetChecksums operation.
      * @param message The GetChecksumsRequest message to handle.
      */
     public void handleMessage(GetChecksumsRequest message) {
         try {
             if(!validateMessage(message)) {
                 return;
             }
             
             sendInitialProgressMessage(message);
             List<ChecksumDataForChecksumSpecTYPE> checksumList = calculateChecksumResults(message);
             ResultingChecksums compiledResults = performPostProcessing(message, checksumList);
             sendFinalResponse(message, compiledResults);
         } catch (IllegalArgumentException e) {
             log.warn("Caught IllegalArgumentException. Message ", e);
             alarmDispatcher.handleIllegalArgumentException(e);
         } catch (RuntimeException e) {
             log.warn("Internal RunTimeException caught. Sending response for 'error at my end'.", e);
             ResponseInfo fri = new ResponseInfo();
             fri.setResponseCode(ResponseCode.OPERATION_FAILED);
             fri.setResponseText("Error: " + e.getMessage());
             sendFailedResponse(message, fri);
         }
     }
     
     /**
      * Method for validating the content of the GetChecksumsRequest message. 
      * Is it possible to perform the operation?
      * 
      * @param message The message to validate.
      * @return Whether it is valid.
      */
     private boolean validateMessage(GetChecksumsRequest message) {
         // Validate the message.
         validateBitrepositoryCollectionId(message.getCollectionID());
         validatePillarId(message.getPillarID());
         
         if(!validateFileIDs(message)) {
             return false;
         }
         if(!validateChecksum(message)) {
             return false;
         }
         
         log.debug("Message '" + message.getCorrelationID() + "' validated and accepted.");
         
         return true;
     }
     
     /**
      * Method for validating the FileIDs in the GetChecksumsRequest message.
      * Do we have the requested files?
      * This does only concern the specified files. Not 'AllFiles' or the parameter stuff.
      *  
      * @param message The message to validate the FileIDs of.
      * @return Whether all the specified files in FileIDs are present.
      */
     private boolean validateFileIDs(GetChecksumsRequest message) {
         // Validate the requested files
         FileIDs fileids = message.getFileIDs();
 
         // go through all the files and find any missing
         List<String> missingFiles = new ArrayList<String>();
         for(String fileID : fileids.getFileID()) {
             if(!archive.hasFile(fileID)) {
                 missingFiles.add(fileID);
             }
         }
         
         // if not missing, then all files have been found!
         if(!missingFiles.isEmpty()) {
             // report on the missing files
             String errText = "The following files are missing: '" + missingFiles + "'";
             log.warn(errText);
             // Then tell the mediator, that we failed.
             ResponseInfo fri = new ResponseInfo();
             fri.setResponseCode(ResponseCode.FILE_NOT_FOUND);
             fri.setResponseText(errText);
             sendFailedResponse(message, fri);
 
             return false;
         }
         
         return true;
     }
     
     /**
      * Method for validating the checksum algorithm in the GetChecksumsRequest message.
      * Do we access to the Algorithm?
      * Also validates whether an algorithm has been granted.
      * 
      * @param message The message to have its checksum algorithm validated.
      * @return Whether the algorithm is present and operational.
      */
     private boolean validateChecksum(GetChecksumsRequest message) {
         // validate the checksum function
         ChecksumSpecTYPE checksumSpec = message.getFileChecksumSpec();
         
         // validate that this non-mandatory field has been filled out.
         if(checksumSpec == null || checksumSpec.getChecksumType() == null) {
             String errText = "No checksumSpec in the request. Needs an algorithm to calculate checksums!";
             log.warn(errText);
             ResponseInfo fri = new ResponseInfo();
             fri.setResponseCode(ResponseCode.FAILURE);
             fri.setResponseText(errText);
             sendFailedResponse(message, fri);
             
             return false;
         }
         
         try {
             MessageDigest.getInstance(checksumSpec.getChecksumType());
         } catch (NoSuchAlgorithmException e) {
             String errText = "Could not instantiate the given messagedigester for calculating a checksum.";
             log.warn(errText, e);
             ResponseInfo fri = new ResponseInfo();
             fri.setResponseCode(ResponseCode.FAILURE);
             fri.setResponseText(errText);
             sendFailedResponse(message, fri);
             
             return false;
         }
         
         return true;
     }
     
     /**
      * Method for creating and sending the initial progress message for accepting the operation.
      * @param message The message to base the response upon.
      */
     private void sendInitialProgressMessage(GetChecksumsRequest message) {
         GetChecksumsProgressResponse pResponse = createProgressResponse(message);
         
         ResponseInfo prInfo = new ResponseInfo();
         prInfo.setResponseCode(ResponseCode.REQUEST_ACCEPTED);
         prInfo.setResponseText("Operation accepted. Starting to calculate checksums.");
         pResponse.setResponseInfo(prInfo);
 
         // Send the ProgressResponse
         log.info("Sending GetFileProgressResponse: " + pResponse);
         messagebus.sendMessage(pResponse);
     }
     
     /**
      * Method for calculating the checksum results requested.
      * @param message The message with the checksum request.
      * @return The list of results for the requested checksum.
      */
     private List<ChecksumDataForChecksumSpecTYPE> calculateChecksumResults(GetChecksumsRequest message) {
         log.debug("Starting to calculate the checksum of the requested files.");
         
         FileIDs fileids = message.getFileIDs();
         MessageDigest checksumAlgorithmDigester = null;
         try {
             checksumAlgorithmDigester = MessageDigest.getInstance(message.getFileChecksumSpec().getChecksumType());
         } catch(Exception e) {
             throw new RuntimeException("Could not retrieve the algorithm for the calculating the checksum.", e);
         }
         String salt = message.getFileChecksumSpec().getChecksumSalt();
         
         if(fileids.isSetAllFileIDs()) {
             log.debug("Calculating the checksum for all the files.");
             return calculateChecksumForAllFiles(checksumAlgorithmDigester, salt);
         }
         if(fileids.isSetParameterAddress()) {
             log.debug("Calculating the checksum for parameter: " + fileids.getParameterAddress());
             return calculateChecksumForParameter(checksumAlgorithmDigester, salt, fileids.getParameterAddress());
         }
         
         log.debug("Calculating the checksum for specified files: " + fileids.getFileID());
         List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
         for(String fileid : fileids.getFileID()) {
             File file = archive.getFile(fileid);
             ChecksumDataForChecksumSpecTYPE singleFileResult = new ChecksumDataForChecksumSpecTYPE();
             singleFileResult.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
             singleFileResult.setFileID(fileid);
             singleFileResult.setChecksumValue(ChecksumUtils.generateChecksum(file, checksumAlgorithmDigester, salt));
             
             res.add(singleFileResult);
         }
         
         return res;
     }
     
     /**
      * Method for calculating the checksum on all the files in the archive.
      * @param checksumAlgorithmDigester The digester with the requested algorithm for calculating the checksums.
      * @param salt The salt of the checksum.
      * @return The list of checksums for requested files. 
      */
     private List<ChecksumDataForChecksumSpecTYPE> calculateChecksumForAllFiles(
             MessageDigest checksumAlgorithmDigester, String salt) {
         List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
         
         // Go through every file in the archive, calculate the checksum and put it into the results.
         for(String fileid : archive.getAllFileIds()) {
             File file = archive.getFile(fileid);
             ChecksumDataForChecksumSpecTYPE singleFileResult = new ChecksumDataForChecksumSpecTYPE();
             singleFileResult.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
             singleFileResult.setFileID(fileid);
             singleFileResult.setChecksumValue(ChecksumUtils.generateChecksum(file, checksumAlgorithmDigester, salt));
             
             res.add(singleFileResult);
         }
 
         return res;
     }
     
     /**
      * Method for calculating the files specified by a parameter.
      * TODO implement me!
      * @param checksumAlgorithmDigester The digester with the requested algorithm for calculating the checksums.
      * @param salt The salt of the checksum.
      * @param parameterAddress The parameter for specifying files.
      * @return The list of checksums for requested files. 
      */
     private List<ChecksumDataForChecksumSpecTYPE> calculateChecksumForParameter(
             MessageDigest checksumAlgorithmDigester, String salt, String parameterAddress) {
         // TODO support the parameter stuff.
         throw new IllegalStateException("Method not implemented!");
     }
     
     /**
      * Performs the needed operations to complete the operation.
      * If the results should be uploaded to a given URL, then the results are packed into a file and uploaded, 
      * otherwise the results a put into the result structure.
      * 
      * @param message The message requesting the calculation of the checksums.
      * @param checksumList The list of requested checksums.
      * @return The result structure.
      */
     private ResultingChecksums performPostProcessing(GetChecksumsRequest message, 
             List<ChecksumDataForChecksumSpecTYPE> checksumList) {
         ResultingChecksums res = new ResultingChecksums();
         
         String url = message.getResultAddress();
         if(url != null && !url.isEmpty()) {
             try {
                 File fileToUpload = makeTemporaryChecksumFile(message, checksumList);
                 uploadFile(fileToUpload, url);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
             
             res.setResultAddress(url);
         } else {
             // Put the checksums into the result structure.
             for(ChecksumDataForChecksumSpecTYPE cs : checksumList) {
                 res.getChecksumDataItems().add(cs);
             }
         }
         
         return res;
     }
     
     /**
      * Method for creating a file containing the list of calculated checksums.
      * 
      * @param message The GetChecksumMessage requesting the checksum calculations.
      * @param checksumList The list of checksums to put into the list.
      * @return A file containing all the checksums in the list.
      * @throws Exception If something goes wrong, e.g. IOException or JAXBException.
      */
     private File makeTemporaryChecksumFile(GetChecksumsRequest message, 
             List<ChecksumDataForChecksumSpecTYPE> checksumList) throws Exception {
         // Create the temporary file.
         File checksumResultFile = File.createTempFile(message.getCorrelationID(), new Date().getTime() + ".cs");
         log.debug("Writing the list of checksums to the file '" + checksumResultFile + "'");
        
        // Create data format 
        GetChecksumsResults results = new GetChecksumsResults();
        results.setVersion(VERSION);
        results.setMinVersion(MIN_VERSION);
        results.setPillarID(settings.getReferenceSettings().getPillarSettings().getPillarID());
        results.setCollectionID(settings.getCollectionID());
        ChecksumsDataGroupedByChecksumSpec checksumData = new ChecksumsDataGroupedByChecksumSpec();
        checksumData.setChecksumSpec(message.getFileChecksumSpec());
        for(ChecksumDataForChecksumSpecTYPE cs : checksumList) {
            checksumData.getChecksumDataForChecksumSpec().add(cs);
        }
        results.setChecksumsDataGroupedByChecksumSpec(checksumData);
 
         // Print all the checksums safely (close the streams!)
         OutputStream is = null;
         try {
             is = new FileOutputStream(checksumResultFile);
            JaxbHelper jaxb = new JaxbHelper("xsd/", "BitRepositoryData.xsd");
            is.write(jaxb.serializeToXml(results).getBytes());
             is.flush();
         } finally {
             if(is != null) {
                 is.close();
             }
         }
         
         return checksumResultFile;
     }
     
     /**
      * Method for uploading a file to a given URL.
      * 
      * @param fileToUpload The File to upload.
      * @param url The location where the file should be uploaded.
      * @throws Exception If something goes wrong.
      */
     private void uploadFile(File fileToUpload, String url) throws Exception {
         URL uploadUrl = new URL(url);
         
         // Upload the file.
         log.debug("Uploading file: " + fileToUpload.getName() + " to " + url);
         FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange();
         fe.uploadToServer(new FileInputStream(fileToUpload), uploadUrl);
     }
     
     /**
      * Method for sending a final response reporting the success.
      * @param message The GetChecksumRequest to base the response upon.
      * @param results The results of the checksum calculations.
      */
     private void sendFinalResponse(GetChecksumsRequest message, ResultingChecksums results) {
         GetChecksumsFinalResponse fResponse = createFinalResponse(message);
         
         ResponseInfo fri = new ResponseInfo();
         fri.setResponseCode(ResponseCode.SUCCESS);
         fri.setResponseText("Successfully calculated the requested checksums.");
         fResponse.setResponseInfo(fri);
         fResponse.setResultingChecksums(results);
         
         // Send the FinalResponse
         log.info("Sending GetFileFinalResponse: " + fResponse);
         messagebus.sendMessage(fResponse);        
     }
     
     private void sendFailedResponse(GetChecksumsRequest message, ResponseInfo fri) {
         GetChecksumsFinalResponse fResponse = createFinalResponse(message);
         fResponse.setResponseInfo(fri);
         
         // Send the FinalResponse
         log.info("Sending GetFileFinalResponse: " + fResponse);
         messagebus.sendMessage(fResponse);        
     }
     
     /**
      * Method for creating a GetChecksumProgressResponse based on a request.
      * Missing arguments:
      * <br/> AuditTrailInformation
      * 
      * @param message The GetChecksumsRequest to base the results upon.
      * @return The GetChecksumsProgressResponse based on the GetChecksumsRequest.
      */
     private GetChecksumsProgressResponse createProgressResponse(GetChecksumsRequest message) {
         GetChecksumsProgressResponse res = new GetChecksumsProgressResponse();
         res.setMinVersion(MIN_VERSION);
         res.setVersion(VERSION);
         res.setCorrelationID(message.getCorrelationID());
         res.setFileChecksumSpec(message.getFileChecksumSpec());
         res.setFileIDs(message.getFileIDs());
         res.setResultAddress(message.getResultAddress());
         res.setTo(message.getReplyTo());
         res.setCollectionID(settings.getCollectionID());
         res.setPillarID(settings.getReferenceSettings().getPillarSettings().getPillarID());
         res.setReplyTo(settings.getReferenceSettings().getClientSettings().getReceiverDestination());
 
         return res;
     }
     
     /**
      * Method for creating the generic GetChecksumsFinalResponse based on a GetChecksumRequest.
      * Missing fields:
      * <br/> - AuditTrailInformation
      * <br/> - FinalResponseInfo
      * <br/> - ResultingChecksums
      * 
      * @param message The message to base the response upon and respond to.
      * @return The response for the generic GetChecksumRequest.
      */
     private GetChecksumsFinalResponse createFinalResponse(GetChecksumsRequest message) {
         GetChecksumsFinalResponse res = new GetChecksumsFinalResponse();
         res.setMinVersion(MIN_VERSION);
         res.setVersion(VERSION);
         res.setCorrelationID(message.getCorrelationID());
         res.setFileChecksumSpec(message.getFileChecksumSpec());
         res.setTo(message.getReplyTo());
         res.setCollectionID(settings.getCollectionID());
         res.setPillarID(settings.getReferenceSettings().getPillarSettings().getPillarID());
         res.setReplyTo(settings.getReferenceSettings().getClientSettings().getReceiverDestination());
         
         return res;
     }
 }
