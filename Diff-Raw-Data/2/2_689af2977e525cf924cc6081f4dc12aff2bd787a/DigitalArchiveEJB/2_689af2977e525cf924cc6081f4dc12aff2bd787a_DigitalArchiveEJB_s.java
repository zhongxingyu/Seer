 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO). All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted
  * provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,this list of conditions
  * and the following disclaimer. 2. Redistributions in binary form must reproduce the above
  * copyright notice,this list of conditions and the following disclaimer in the documentation and/or
  * other materials provided with the distribution. 3. Neither the name of FAO nor the names of its
  * contributors may be used to endorse or promote products derived from this software without
  * specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.services.digitalarchive.businesslogic;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import java.util.logging.Level;
 import javax.annotation.security.RolesAllowed;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.imageio.ImageIO;
 import org.sola.common.DateUtility;
 import org.sola.common.FileUtility;
 import org.sola.common.RolesConstants;
 import org.sola.common.logging.LogUtility;
 import org.sola.services.common.ejbs.AbstractEJB;
 import org.sola.services.common.repository.CommonSqlProvider;
 import org.sola.services.digitalarchive.repository.entities.Document;
 import org.sola.services.digitalarchive.repository.entities.FileBinary;
 import org.sola.services.digitalarchive.repository.entities.FileInfo;
 
 /**
  * EJB to manage data in the document schema. Supports retrieving and saving digital documents
  * including functions to create a document from a file or generate a thumbnail image for a image
  * file. <p>The default Network Scan folder location is <b>user.home/sola/scan</b> where user.home
  * is the home folder of the user account running the Glassfish instance.</p>
  */
 @Stateless
 @EJB(name = "java:global/SOLA/DigitalArchiveEJBLocal", beanInterface = DigitalArchiveEJBLocal.class)
 public class DigitalArchiveEJB extends AbstractEJB implements DigitalArchiveEJBLocal {
 
     private File scanFolder;
     private File thumbFolder;
     private int thumbWidth;
     private int thumbHeight;
 
     /**
      * Configures the default network location to read scanned images as well as the default folder
      * to use for generating thumbnail images.
      */
     @Override
     protected void postConstruct() {
 
         // TODO: Implement reading config from DB
         // Set user's home folder
         scanFolder = new File(System.getProperty("user.home") + "/sola/scan");
         thumbFolder = new File(scanFolder.getAbsolutePath() + File.separatorChar + "thumb");
         thumbWidth = 225;
         thumbHeight = 322;
 
         // Init folder
         if (!scanFolder.exists()) {
             new File(scanFolder.getAbsolutePath()).mkdirs();
         }
 
         if (!thumbFolder.exists()) {
             new File(thumbFolder.getAbsolutePath()).mkdirs();
         }
     }
 
     /**
      * Retrieves the document for the specified identifier. This includes the document content (i.e
      * the digital file). <p>Requires the {@linkplain RolesConstants#SOURCE_SEARCH} role.</p>
      *
      * @param documentId Identifier of the document to retrieve
      */
     @Override
     @RolesAllowed(RolesConstants.SOURCE_SEARCH)
     public Document getDocument(String documentId) {
         Document result = null;
         if (documentId != null) {
             result = getRepository().getEntity(Document.class, documentId);
         }
         return result;
     }
 
     /**
      * Returns the meta information recorded for the document but does not retrieve the actual
      * document content.<p>Requires the {@linkplain RolesConstants#SOURCE_SEARCH} role.</p>
      *
      * @param documentId The id of the document to retrieve
      * @see
      */
     @Override
     @RolesAllowed(RolesConstants.SOURCE_SEARCH)
     public Document getDocumentInfo(String documentId) {
         Document result = null;
         if (documentId != null) {
             Map params = new HashMap<String, Object>();
             params.put(CommonSqlProvider.PARAM_WHERE_PART, Document.QUERY_WHERE_BYID);
             params.put("id", documentId);
             // Exclude the body field from the generated SELECT statement
             params.put(CommonSqlProvider.PARAM_EXCLUDE_LIST, Arrays.asList("body"));
             result = getRepository().getEntity(Document.class, params);
         }
         return result;
     }
 
     /**
      * Can be used to create a new document or save any updates to the details of an existing
      * document. <p>Requires the {@linkplain RolesConstants#SOURCE_SAVE} role.</p>
      *
      * @param document The document to create/save.
      * @return The document after the save is completed.
      * @see #createDocument(org.sola.services.digitalarchive.repository.entities.Document)
      */
     @Override
     @RolesAllowed(RolesConstants.SOURCE_SAVE)
     public Document saveDocument(Document document) {
         return getRepository().saveEntity(document);
     }
 
     /**
      * Can be used to create a new document. Also assigns the document number. <p>Requires the {@linkplain RolesConstants#SOURCE_SAVE}
      * role.</p>
      *
      * @param document The document to create.
      * @return The document after the save is completed.
      * @see #saveDocument(org.sola.services.digitalarchive.repository.entities.Document)
      * saveDocument
      * @see #createDocument(org.sola.services.digitalarchive.repository.entities.Document,
      * java.lang.String) createDocument
      * @see #allocateNr() allocateNr
      */
     @Override
     @RolesAllowed(RolesConstants.SOURCE_SAVE)
     public Document createDocument(Document document) {
         document.setNr(allocateNr());
         return saveDocument(document);
     }
 
     /**
      * Can be used to create a new document with the digital content obtained from the specified
      * file. Used to create documents from the network scan folder. After the digital file is
      * loaded, it is deleted from the network scan folder. <p>Requires the {@linkplain RolesConstants#SOURCE_SAVE}
      * role.</p>
      *
      * @param document The document to create.
      * @param fileName The filename of the digital file to save with the document.
      * @return The document after the save is completed.
      * @see #createDocument(org.sola.services.digitalarchive.repository.entities.Document)
      * createDocument
      */
     @Override
     @RolesAllowed(RolesConstants.SOURCE_SAVE)
     public Document createDocument(Document document, String fileName) {
         if (fileName == null || document == null) {
             return null;
         }
 
         // Check if file exists in scan folder name to exclude jumping from the folder
         String filePath = getFullFilePath(fileName, scanFolder);
         if (filePath == null) {
             return null;
         }
 
         // Get file from shared folder
         byte[] fileBytes = FileUtility.getFileBinary(filePath);
         if (fileBytes == null) {
             return null;
         }
 
        document.setExtension(FileUtility.getFileExtesion(fileName));
         document.setBody(fileBytes);
         document.setDescription(fileName);
         document = createDocument(document);
 
         // Delete the file from the network scan folder. 
         deleteFile(fileName);
 
         return document;
     }
 
     /**
      * Determines the number to assign to the document based on the date and the
      * <code>document.document_nr_seq</code> number sequence.
      *
      * @return The allocated document number.
      */
     private String allocateNr() {
         String datePart = "";
         String numPart = null;
 
         Map params = new HashMap<String, Object>();
         params.put(CommonSqlProvider.PARAM_SELECT_PART, Document.QUERY_ALLOCATENR);
         numPart = getRepository().getScalar(Long.class, params).toString();
 
         if (numPart != null) {
             // Prefix with 0 to get a 4 digit number.
             while (numPart.length() < 4) {
                 numPart = "0" + numPart;
             }
             if (numPart.length() > 4) {
                 numPart = numPart.substring(numPart.length() - 4);
             }
             datePart = DateUtility.simpleFormat("yyMM");
         } else {
             // Use the current datetime
             numPart = DateUtility.simpleFormat("yyMMddHHmmss");
         }
         return datePart + numPart;
     }
 
     /**
      * Loads the specified file from the Network Scan folder. <p>Requires the {@linkplain RolesConstants#SOURCE_SEARCH}
      * role.</p>
      *
      * @param fileName The name of the file to load
      * @return The binary file along with some attributes of the file
      */
     @Override
     @RolesAllowed(RolesConstants.SOURCE_SEARCH)
     public FileBinary getFileBinary(String fileName) {
         if (fileName == null || fileName.equals("")) {
             return null;
         }
 
         // Check if file exists in scan folder name to exclude jumping from the folder
         String filePath = getFullFilePath(fileName, scanFolder);
 
         if (filePath == null) {
             return null;
         }
 
         // Get file from shared folder
         byte[] fileBytes = FileUtility.getFileBinary(filePath);
         if (fileBytes == null) {
             return null;
         }
 
         File file = new File(filePath);
         FileBinary fileBinary = new FileBinary();
         fileBinary.setContent(fileBytes);
         fileBinary.setFileSize(file.length());
         fileBinary.setName(fileName);
         fileBinary.setModificationDate(new Date(file.lastModified()));
         return fileBinary;
     }
 
     /**
      * Loads the specified file from the Network Scan folder then generates a thumbnail image of the
      * file if one does not already exist. <p>Requires the {@linkplain RolesConstants#SOURCE_SEARCH}
      * role.</p>
      *
      * @param fileName The name of the file to load
      * @return A thumbnail image of the file
      * @see FileUtility#createImageThumbnail(java.lang.String, int, int)
      * FileUtility.createImageThumbnail
      */
     @Override
     @RolesAllowed(RolesConstants.SOURCE_SEARCH)
     public FileBinary getFileThumbnail(String fileName) {
         if (fileName == null || fileName.equals("")) {
             return null;
         }
 
         // Check if file exists in scan folder name to exclude jumping from the folder
         String thumbName = getThumbName(fileName);
 
         String filePath = getFullFilePath(thumbName, thumbFolder);
 
         if (filePath == null) {
             // Try to create
             if (!createThumbnail(fileName)) {
                 return null;
             }
             filePath = thumbFolder.getAbsolutePath() + File.separator + thumbName;
         }
 
         // Get thumbnail 
         byte[] fileBytes = FileUtility.getFileBinary(filePath);
         if (fileBytes == null) {
             return null;
         }
 
         File file = new File(filePath);
         FileBinary fileBinary = new FileBinary();
         fileBinary.setContent(fileBytes);
         fileBinary.setFileSize(file.length());
         fileBinary.setName(fileName);
         fileBinary.setModificationDate(new Date(file.lastModified()));
         return fileBinary;
     }
 
     /**
      * Construct thumbnail file name out of original file name
      *
      * @param fileName The name of original file to create thumbnail from
      */
     private String getThumbName(String fileName) {
         File tmpFile = new File(fileName);
         String thumbName = tmpFile.getName();
 
         if (thumbName.contains(".")) {
             thumbName = thumbName.substring(0, thumbName.lastIndexOf(".") - 1);
         }
 
         thumbName += ".jpg";
         return thumbName;
     }
 
     /**
      * Creates thumbnail image for the file in the shared folder
      *
      * @param fileName The name of the file in the shared folder
      */
     private boolean createThumbnail(String fileName) {
         if (fileName == null || fileName.equals("")) {
             return false;
         }
 
         // Check if file exists in scan folder name to exclude jumping from the folder
         String filePath = getFullFilePath(fileName, scanFolder);
         String thumbPath = thumbFolder.getAbsolutePath() + File.separator + getThumbName(fileName);
 
         if (filePath == null) {
             return false;
         }
 
         try {
             BufferedImage image = FileUtility.createImageThumbnail(filePath, thumbWidth, -1);
 
             if (image == null) {
                 return false;
             }
 
             File thumbFile = new File(thumbPath);
             if (thumbFile.exists()) {
                 thumbFile.delete();
             }
 
             ImageIO.write(image, "JPEG", thumbFile);
 
         } catch (IOException ex) {
             LogUtility.log(ex.getLocalizedMessage(), Level.SEVERE);
             return false;
         }
 
         return true;
     }
 
     /**
      * Retrieves the list of all files in the Network Scan Folder. Only meta data about the file is
      * returned. The content of the file is omitted to avoid transferring a large amount of file
      * data across the network. <p>Requires the {@linkplain RolesConstants#SOURCE_SEARCH} role.</p>
      */
     @Override
     @RolesAllowed(RolesConstants.SOURCE_SEARCH)
     public List<FileInfo> getAllFiles() {
         List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
         // Read folder
         // TODO: Make folder filtering for appropriate files
         if (scanFolder != null && scanFolder.isDirectory()) {
             for (String fileName : scanFolder.list()) {
                 File file = new File(scanFolder.getAbsolutePath() + File.separator + fileName);
                 if (file.exists() && file.isFile()) {
                     FileInfo fileInfo = new FileInfo();
                     fileInfo.setModificationDate(new Date(file.lastModified()));
                     fileInfo.setFileSize(file.length());
                     fileInfo.setName(fileName);
                     fileInfoList.add(fileInfo);
                 }
             }
         }
         // Sort list by modification date
         Collections.sort(fileInfoList, new FileInfoSorterByModificationDate());
         return fileInfoList;
     }
 
     /**
      * Deletes the specified file from the Network Scan folder. Also attempts to delete any
      * thumbnail for the file if one exists. <p>Requires the {@linkplain RolesConstants#SOURCE_SEARCH}
      * role.</p>
      *
      * @param fileName The name of the file to delete.
      * @return true if the file is successfully deleted.
      */
     @Override
     @RolesAllowed(RolesConstants.SOURCE_SAVE)
     public boolean deleteFile(String fileName) {
         if (fileName == null || fileName.equals("")) {
             return false;
         }
 
         // Check if file exists in scan folder name to exclude jumping from the folder
         String filePath = getFullFilePath(fileName, scanFolder);
         String thumbnailPath = getFullFilePath(fileName, thumbFolder);
 
         if (filePath == null) {
             return false;
         }
 
         // Delete file
         File file = new File(filePath);
 
         boolean result = false;
 
         try {
             result = file.delete();
             // try to delete thumbnail
             if (result && thumbnailPath != null) {
                 File thumbnail = new File(thumbnailPath + File.separator + fileName);
                 if (thumbnail.exists()) {
                     thumbnail.delete();
                 }
             }
         } catch (Exception e) {
             // Log the exception to indicate the file could not be deleted
             String msg = "Error deleting " + (result ? "thumbnail for file: " : "file: ");
             LogUtility.log(msg + filePath, e);
         }
 
         return result;
     }
 
     /**
      * Returns the full path for the requested file in the shared folder. Used to control the file
      * name exists in a given folder and doesn't contain any dangerous characters to jump out the
      * folder path.
      *
      * @param fileName File name in the shared folder
      * @param folder The folder to check for the file name
      * @return
      */
     private String getFullFilePath(String fileName, File folder) {
         if (folder != null && folder.isDirectory()) {
             for (String fName : folder.list()) {
                 if (fName.equals(fileName)) {
                     return folder.getAbsolutePath() + File.separator + fileName;
                 }
             }
         }
         return null;
     }
 }
