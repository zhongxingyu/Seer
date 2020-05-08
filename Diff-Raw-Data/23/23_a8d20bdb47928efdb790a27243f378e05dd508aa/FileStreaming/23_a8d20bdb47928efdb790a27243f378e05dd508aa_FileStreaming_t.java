 /**
  * ******************************************************************************************
  * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
  *       promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.services.boundary.ws;
 
 import com.sun.xml.ws.developer.StreamingAttachment;
import java.io.File;
 import javax.activation.DataHandler;
 import javax.annotation.Resource;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.xml.bind.annotation.XmlMimeType;
 import javax.xml.ws.WebServiceContext;
 import javax.xml.ws.soap.MTOM;
 import org.sola.common.FileUtility;
 import org.sola.services.common.ServiceConstants;
 import org.sola.services.common.faults.SOLAFault;
 import org.sola.services.common.faults.UnhandledFault;
 import org.sola.services.common.webservices.AbstractWebService;
 
 /**
  * Web Service Boundary class to expose methods for file streaming.
  *
  * <p>The Metro library contains a bug (see http://java.net/jira/browse/WSIT-1081) that prevents
  * file streaming over metro secured connections. Metro also consumes significant amounts of memory
  * when trying to encrypt large attachments. To avoid these bugs, this service is used unsecured and
  * does not retrieve from or update the SOLA database. Instead this service can be used to transfer
  * large files between the file systems of the client and application server. Other secured services
  * such as the Digital Archive Service, must retrieve or post files to the local file system for the
  * file streaming service.</p> <p>The service streams files directly to and from disk and has
  * minimal impact on the overall memory consumption of the application</p>
  */
 @MTOM
 @StreamingAttachment(parseEagerly = true, memoryThreshold = 4000000L)
 @WebService(serviceName = "filestreaming-service", targetNamespace = ServiceConstants.FILE_STREAMING_WS_NAMESPACE)
 public class FileStreaming extends AbstractWebService {
 
     @Resource
     private WebServiceContext wsContext;
 
     /**
      * Web method that can be used to validate if the web service is available.
      *
      * @return Always true
      */
     @WebMethod(operationName = "CheckConnection")
     public boolean CheckConnection() {
         return true;
     }
 
     /**
      * Streams a file up to the SOLA Application Server and stores it on the file system in the
      * documents cache.
      *
      * @param fileContent - The content of the file being streamed up.
      * @return The name of the file in the documents cache the file content is saved in.
      * @throws SOLAFault
      * @throws UnhandledFault
      */
     @WebMethod(operationName = "Upload")
     public String Upload(@WebParam(name = "fileContent")
             @XmlMimeType("application/octet-stream") DataHandler fileContent)
             throws SOLAFault, UnhandledFault {
 
         final Object[] result = {null};
         final DataHandler fileContentTmp = fileContent;
 
         runUnsecured(wsContext, new Runnable() {
 
             @Override
             public void run() {
                 result[0] = FileUtility.saveFileFromStream(fileContentTmp, null);
             }
         });
 
         return (String) result[0];
     }
 
     /**
      * Streams a file down from the SOLA Application Server to the client. The file must be on the
      * SOLA Application Server file system.
      *
      * @param pathFileName The path and file name of the file to retrieve from the SOLA Application
      * Server. If the file is in the documents cache, only the file name is required. If the file is
      * elsewhere, the full file path name is required.
      * @throws SOLAFault
      * @throws UnhandledFault
      */
     @WebMethod(operationName = "Download")
     public @XmlMimeType("application/octet-stream")
     DataHandler Download(
             @WebParam(name = "pathFileName") String pathFileName)
             throws SOLAFault, UnhandledFault {
 
         final Object[] result = {null};
        // Ticket #397 - As teh client can be installed on a different OS to the 
        // server, avoid using system dependent path separators by using
        // !! in the pathFileName instead. The services can hen replace this value
        // with the correct separator value. Note that replaceAll fails if File.separator
        // is // because this has special meaning for regex, so use replace instead.
        pathFileName = pathFileName.replace(FileUtility.alternatePathSeparator, File.separator); 
         final String tmpPathFileName = pathFileName;
 
         runUnsecured(wsContext, new Runnable() {
 
             @Override
             public void run() {
                 result[0] = FileUtility.getFileAsStream(tmpPathFileName);
             }
         });
 
         return (DataHandler) result[0];
     }
 }
