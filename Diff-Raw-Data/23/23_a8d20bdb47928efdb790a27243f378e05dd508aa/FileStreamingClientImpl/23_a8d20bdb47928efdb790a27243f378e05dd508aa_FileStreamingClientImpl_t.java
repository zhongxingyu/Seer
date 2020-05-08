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
 package org.sola.services.boundary.wsclients;
 
 import com.sun.xml.ws.developer.JAXWSProperties;
 import com.sun.xml.ws.developer.StreamingAttachmentFeature;
import java.io.File;
 import javax.activation.DataHandler;
 import javax.xml.namespace.QName;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.soap.MTOMFeature;
 import org.sola.common.FileUtility;
 import org.sola.services.boundary.wsclients.exception.WebServiceClientException;
 import org.sola.webservices.filestreaming.FileStreaming;
 import org.sola.webservices.filestreaming.FilestreamingService;
 
 /**
  * Implementation class for the {@linkplain FileStreamingClient} interface.
  */
 public class FileStreamingClientImpl extends AbstractWSClientImpl implements FileStreamingClient {
 
     private static final String NAMESPACE_URI = "http://webservices.sola.org/filestreaming";
     private static final String LOCAL_PART = "filestreaming-service";
 
     /**
      * Creates a web service client class for the web service hosted at the specified URL
      *
      * @param url The location of the service WSDL
      */
     public FileStreamingClientImpl(String url) {
         super(url, new QName(NAMESPACE_URI, LOCAL_PART));
     }
 
     protected FileStreaming getPort() {
         return getPort(FileStreaming.class, FilestreamingService.class,
                 new MTOMFeature(), new StreamingAttachmentFeature(null, true, 4000000L));
     }
 
     /**
      * Sets the request context parameters for the port. Includes setting the username and password
      * for each request.
      *
      * @param port The web service port.
      */
     @Override
     protected void setContextParameters(BindingProvider port) {
         super.setContextParameters(port);
         port.getRequestContext().put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
     }
 
     @Override
     public boolean checkConnection() throws WebServiceClientException {
         boolean result = false;
         final String methodName = FileStreamingClient.CHECK_CONNECTION;
         try {
             beforeWebMethod(methodName);
             result = getPort().checkConnection();
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result);
         }
         return result;
     }
 
     @Override
     public String upload(String pathFileName) throws WebServiceClientException {
         String result = null;
         final String methodName = FileStreamingClient.UPLOAD;
         try {
             beforeWebMethod(methodName, pathFileName);
             DataHandler dataHandler = FileUtility.getFileAsStream(pathFileName);
             if (dataHandler != null) {
                 result = getPort().upload(dataHandler);
             }
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, pathFileName);
         }
         return result;
     }
 
     @Override
     public String download(String serverPathFileName, String localFileName) throws WebServiceClientException {
         String result = null;
         final String methodName = FileStreamingClient.DOWNLOAD;
         try {
             beforeWebMethod(methodName, serverPathFileName, localFileName);
             result = FileUtility.saveFileFromStream(getPort().download(serverPathFileName), localFileName);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, serverPathFileName, localFileName);
         }
         return result;
     }
 }
