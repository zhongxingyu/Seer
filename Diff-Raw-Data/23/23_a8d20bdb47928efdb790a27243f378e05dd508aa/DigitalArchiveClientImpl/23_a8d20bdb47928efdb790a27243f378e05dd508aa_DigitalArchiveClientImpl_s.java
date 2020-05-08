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
 
import java.io.File;
 import java.util.List;
 import javax.xml.namespace.QName;
 import org.sola.common.FileUtility;
 import org.sola.services.boundary.wsclients.exception.WebServiceClientException;
 import org.sola.webservices.digitalarchive.DigitalArchive;
 import org.sola.webservices.digitalarchive.DigitalarchiveService;
 import org.sola.webservices.transferobjects.digitalarchive.DocumentBinaryTO;
 import org.sola.webservices.transferobjects.digitalarchive.DocumentTO;
 import org.sola.webservices.transferobjects.digitalarchive.FileInfoTO;
 
 /**
  * Implementation class for the {@linkplain DigitalArchiveClient} interface.
  */
 public class DigitalArchiveClientImpl extends AbstractWSClientImpl implements DigitalArchiveClient {
 
     private static final String NAMESPACE_URI = "http://webservices.sola.org/digitalarchive";
     private static final String LOCAL_PART = "digitalarchive-service";
     private FileStreamingClient fileStreamingService = null;
 
     /**
      * Creates a web service client class for the web service hosted at the
      * specified URL
      *
      * @param url The location of the service WSDL
      */
     public DigitalArchiveClientImpl(String url) {
         super(url, new QName(NAMESPACE_URI, LOCAL_PART));
     }
 
     protected DigitalArchive getPort() {
         return getPort(DigitalArchive.class, DigitalarchiveService.class);
     }
 
     @Override
     public void setFileStreamingService(FileStreamingClient fsService) {
         this.fileStreamingService = fsService;
     }
 
     private FileStreamingClient getFS() {
         return this.fileStreamingService;
     }
 
     @Override
     public boolean checkConnection() throws WebServiceClientException {
         boolean result = false;
         final String methodName = DigitalArchiveClient.CHECK_CONNECTION;
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
     public DocumentBinaryTO getDocument(String documentId) {
         DocumentBinaryTO result = null;
         final String methodName = DigitalArchiveClient.GET_DOCUMENT;
         try {
             beforeWebMethod(methodName, documentId);
             result = getPort().getDocument(documentId);
             String localFileName = FileUtility.generateFileName(result.getNr(),
                     result.getRowVersion(), result.getExtension());
             localFileName = getFS().download(result.getFileName(), localFileName);
             result.setFileName(localFileName);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, documentId);
         }
         return result;
     }
 
     @Override
     public DocumentTO saveDocument(DocumentTO documentTO) {
         DocumentTO result = null;
         final String methodName = DigitalArchiveClient.SAVE_DOCUMENT;
         try {
             beforeWebMethod(methodName, documentTO);
             result = getPort().saveDocument(documentTO);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, documentTO);
         }
         return result;
     }
 
     @Override
     public DocumentTO createDocument(DocumentBinaryTO documentBinaryTO) {
         DocumentTO result = null;
         final String methodName = DigitalArchiveClient.CREATE_DOCUMENT;
         try {
             beforeWebMethod(methodName, documentBinaryTO);
             String serverFileName = getFS().upload(documentBinaryTO.getFileName());
             documentBinaryTO.setFileName(serverFileName);
             result = getPort().createDocument(documentBinaryTO);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, documentBinaryTO);
         }
         return result;
     }
 
     @Override
     public DocumentTO createDocumentFromServer(DocumentTO documentTO, String fileName) {
         DocumentTO result = null;
         final String methodName = DigitalArchiveClient.CREATE_DOCUMENT_FROM_SERVER;
         try {
             beforeWebMethod(methodName, documentTO, fileName);
             result = getPort().createDocumentFromServer(documentTO, fileName);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, documentTO, fileName);
         }
         return result;
     }
 
     @Override
     public FileInfoTO getFileBinary(String fileName) {
         FileInfoTO result = null;
         final String methodName = DigitalArchiveClient.GET_FILE_BINARY;
         try {
             beforeWebMethod(methodName, fileName);
             result = getPort().getFileBinary(fileName);
             fileName = getFS().download(result.getName(), fileName);
             result.setName(fileName);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, fileName);
         }
         return result;
     }
 
     @Override
     public FileInfoTO getFileThumbnail(String fileName) {
         FileInfoTO result = null;
         final String methodName = DigitalArchiveClient.GET_FILE_THUMBNAIL;
         try {
             beforeWebMethod(methodName, fileName);
             result = getPort().getFileThumbnail(fileName);
            fileName = getFS().download("thumb" + File.separator + result.getName(),
                     result.getModificationDate().toGregorianCalendar().getTime().getTime()
                     + "_" + result.getName());
             // Use the modification date in the file name in case the file is updated. 
             result.setName(fileName);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, fileName);
         }
         return result;
     }
 
     @Override
     public List<FileInfoTO> getAllFiles() {
         List<FileInfoTO> result = null;
         final String methodName = DigitalArchiveClient.GET_ALL_FILES;
         try {
             beforeWebMethod(methodName);
             result = getPort().getAllFiles();
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result);
         }
         return result;
     }
 
     @Override
     public boolean deleteFile(String fileName) {
         boolean result = false;
         final String methodName = DigitalArchiveClient.DELETE_FILE;
         try {
             beforeWebMethod(methodName, fileName);
             result = getPort().deleteFile(fileName);
         } catch (Exception e) {
             processException(methodName, e);
         } finally {
             afterWebMethod(methodName, result, fileName);
         }
         return result;
     }
 }
