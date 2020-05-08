 /*
  * #%L
  * Bitmagasin integrationstest
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
 package org.bitrepository.pillar;
 
 import java.math.BigInteger;
 
 import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetFileRequest;
 import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
 import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.PutFileRequest;
 import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
 import org.bitrepository.protocol.ProtocolConstants;
 
 /**
  * Class for generating the messages for the Reference Pillar.
  */
 public class ReferencePillarMessageFactory {
     /** The settings for the pillar.*/
     final PillarSettings settings;
     
     // Constants
     /** The constant for the VERSION of the messages.*/
     private final BigInteger VERSION = BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION);
     /** The constant for the MIN_VERSION of the messages.*/
     private final BigInteger MIN_VERSION = BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION);
     
     /**
      * Package protected constructor.
      */
     ReferencePillarMessageFactory(PillarSettings pSettings) {
         settings = pSettings;
     }
     
     /**
      * Creates a IdentifyPillarsForGetFileResponse based on a 
      * IdentifyPillarsForGetFileRequest. The following fields are not inserted:
      * <br/> - TimeToDeliver
      * <br/> - AuditTrailInformation
      * 
      * @param msg The IdentifyPillarsForGetFileRequest to base the response on.
      * @return The response to the request.
      */
     public IdentifyPillarsForGetFileResponse createIdentifyPillarsForGetFileResponse(
             IdentifyPillarsForGetFileRequest msg) {
         IdentifyPillarsForGetFileResponse res 
                 = new IdentifyPillarsForGetFileResponse();
         res.setMinVersion(MIN_VERSION);
         res.setVersion(VERSION);
         res.setCorrelationID(msg.getCorrelationID());
         res.setFileID(msg.getFileID());
         res.setTo(msg.getReplyTo());
         res.setPillarID(settings.getPillarId());
         res.setBitRepositoryCollectionID(settings.getBitRepositoryCollectionID());
         res.setReplyTo(settings.getLocalQueue());
         
         return res;
     }
     
     /**
      * Creates a IdentifyPillarsForPutFileResponse based on a 
      * IdentifyPillarsForPutFileRequest. The following fields are not inserted:
      * <br/> - TimeToDeliver
      * <br/> - AuditTrailInformation
      * <br/> - PillarChecksumSpec
      * 
      * @param msg The IdentifyPillarsForPutFileRequest to base the response on.
      * @return A IdentifyPillarsForPutFileResponse from the request.
      */
     public IdentifyPillarsForPutFileResponse createIdentifyPillarsForPutFileResponse(
             IdentifyPillarsForPutFileRequest msg) {
         IdentifyPillarsForPutFileResponse res
                 = new IdentifyPillarsForPutFileResponse();
         res.setMinVersion(MIN_VERSION);
         res.setVersion(VERSION);
         res.setCorrelationID(msg.getCorrelationID());
         res.setTo(msg.getReplyTo());
         res.setBitRepositoryCollectionID(settings.getBitRepositoryCollectionID());
         res.setPillarID(settings.getPillarId());
         res.setReplyTo(settings.getLocalQueue());
         
         return res;
     }
     
     /**
      * Creates a GetFileResponse based on a GetFileRequest. Missing the 
      * following fields:
      * <br/> - AuditTrailInformation
      * <br/> - ChecksumsDataForBitRepositoryFile
      * <br/> - FileSize
      * <br/> - ProgressResponseInfo
      * 
      * @param msg The GetFileRequest to base the progress response on.
      * @return The GetFileProgressResponse based on the request.
      */
     public GetFileProgressResponse createGetFileProgressResponse(GetFileRequest msg) {
         GetFileProgressResponse res = new GetFileProgressResponse();
         res.setMinVersion(MIN_VERSION);
         res.setVersion(VERSION);
         res.setCorrelationID(msg.getCorrelationID());
         res.setFileAddress(msg.getFileAddress());
         res.setFileID(msg.getFileID());
         res.setFilePart(msg.getFilePart());
         res.setTo(msg.getReplyTo());
         res.setPillarID(settings.getPillarId());
         res.setBitRepositoryCollectionID(settings.getBitRepositoryCollectionID());
         res.setReplyTo(settings.getLocalQueue());
 
         return res;
     }
     
     /**
      * Creates a GetFileFinalResponse based on a GetFileRequest. Missing the 
      * following fields:
      * <br/> - AuditTrailInformation
      * <br/> - FinalResponseInfo
      * 
      * @param msg The GetFileRequest to base the final response on.
      * @return The GetFileFinalResponse based on the request.
      */
     public GetFileFinalResponse createGetFileFinalResponse(GetFileRequest msg) {
         GetFileFinalResponse res = new GetFileFinalResponse();
         res.setMinVersion(MIN_VERSION);
         res.setVersion(VERSION);
         res.setCorrelationID(msg.getCorrelationID());
         res.setFileID(msg.getFileID());
         res.setFileAddress(msg.getFileAddress());
         res.setFilePart(msg.getFilePart());
         res.setPillarID(settings.getPillarId());
         res.setBitRepositoryCollectionID(settings.getBitRepositoryCollectionID());
         res.setReplyTo(settings.getLocalQueue());
 
         return res;
     }
     
     /**
      * Creates a PutFileProgressResponse based on a PutFileRequest. Missing the 
      * following fields:
      * <br/> - AuditTrailInformation
      * <br/> - PillarChecksumSpec
      * <br/> - ProgressResponseInfo
      * 
      * @param msg The PutFileRequest to base the progress response on.
      * @return The PutFileProgressResponse based on the request.
      */
     public PutFileProgressResponse createPutFileProgressResponse(PutFileRequest msg) {
         PutFileProgressResponse res = new PutFileProgressResponse();
         res.setMinVersion(MIN_VERSION);
         res.setVersion(VERSION);
         res.setCorrelationID(msg.getCorrelationID());
         res.setFileAddress(msg.getFileAddress());
         res.setFileID(msg.getFileID());
         res.setTo(msg.getReplyTo());
         res.setPillarID(settings.getPillarId());
         res.setBitRepositoryCollectionID(settings.getBitRepositoryCollectionID());
         res.setReplyTo(settings.getLocalQueue());
         
         return res;
     }
 
     /**
      * Creates a PutFileFinalResponse based on a PutFileRequest. Missing the
      * following fields:
      * <br/> - AuditTrailInformation
      * <br/> - ChecksumsDataForNewFile
      * <br/> - FinalResponseInfo
      * <br/> - PillarChecksumSpec
      * 
      * @param msg The PutFileRequest to base the final response message on.
      * @return The PutFileFinalResponse message based on the request.
      */
     public PutFileFinalResponse createPutFileFinalResponse(PutFileRequest msg) {
         PutFileFinalResponse res = new PutFileFinalResponse();
         res.setMinVersion(MIN_VERSION);
         res.setVersion(VERSION);
         res.setCorrelationID(msg.getCorrelationID());
         res.setFileID(msg.getFileID());
         res.setFileAddress(msg.getFileAddress());
         res.setTo(msg.getReplyTo());
         res.setPillarID(settings.getPillarId());
         res.setBitRepositoryCollectionID(settings.getBitRepositoryCollectionID());
         res.setReplyTo(settings.getLocalQueue());
 
         return res;
     }
 }
