 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Web Questionnaires 2
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Enriko Käsper
  */
 
 package eionet.webq.dto;
 
 import java.util.Collection;
 import java.util.Date;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.time.DateFormatUtils;
 
 /**
  * Object for transferring {@link eionet.webq.dao.orm.UserFile} public data and related conversions into XML format for XForms. The
  * returned object contains also user-friendly formatted dates and links to download, delete and convert the file.
  *
  * @author Enriko Käsper
  */
 @XmlRootElement(name = "fileinfo")
 public class FileInfo {
 
     /** User file id. */
     @XmlElement(name = "fileId")
     private int fileId;
 
     /** File size in bytes. */
     @XmlElement(name = "sizeInBytes")
     private long sizeInBytes;
 
     /** File created. */
     @XmlElement(name = "createdDate")
     private Date createdDate;
 
     /** File last updated. */
     @XmlElement(name = "updatedDate")
     private Date updatedDate;
 
     /** File last downloaded. */
     @XmlElement(name = "downloadedDate")
     private Date downloadedDate;
 
     /** List of available conversions for the file. */
     Collection<Conversion> conversions;
 
     /** File is locally stored in WebQ. */
     private boolean isLocalFile;
 
     /** Path to download file. */
    private final static String FILE_DOWNLOAD_LINK = "/download/user_file?fileId=";
 
     /** Path to convert file. */
    private final static String FILE_CONVERSION_LINK = "/download/convert?fileId=";
 
     /** Path to delete file. */
    private final static String FILE_DELETE_LINK = "/remove/files?selectedUserFile=";
 
     /** Default date format used when formatting user-friendly dates. */
    private final static String USERFRIENDLY_DATE_FORMAT = "dd MMM yyyy HH:mm:ss";
 
     /**
      * No-arg default constructor required by {@link org.springframework.oxm.jaxb.Jaxb2Marshaller}.
      */
     public FileInfo() {
     }
 
     /**
      * Get user-friendly formatted file creation date.
      *
      * @return created date in String format
      */
     @XmlElement(name = "created")
     public String getCreated() {
         return (createdDate != null) ? DateFormatUtils.format(createdDate, USERFRIENDLY_DATE_FORMAT) : null;
     }
 
     /**
      * Get user-friendly formatted file last modified date.
      *
      * @return updated date in String format
      */
     @XmlElement(name = "updated")
     public String getUpdated() {
         return (updatedDate != null) ? DateFormatUtils.format(updatedDate, USERFRIENDLY_DATE_FORMAT) : null;
     }
 
     /**
      * Get user-friendly formatted file last downloaded date.
      *
      * @return downloaded date in String format
      */
     @XmlElement(name = "downloaded")
     public String getDownloaded() {
         return (downloadedDate != null) ? DateFormatUtils.format(downloadedDate, USERFRIENDLY_DATE_FORMAT) : null;
     }
 
     /**
      * Get user friendly formatted file size.
      *
      * @return file size as String
      */
     @XmlElement(name = "fileSize")
     public String getSize() {
         return FileUtils.byteCountToDisplaySize(sizeInBytes);
     }
 
     /**
      * URL to download the file.
      *
      * @return download URL
      */
     @XmlElement(name = "downloadLink")
     public String getDownloadLink() {
         return FILE_DOWNLOAD_LINK + fileId;
     }
 
     /**
      * URL to delete the file from database.
      *
      * @return deletion URL
      */
     @XmlElement(name = "deleteLink")
     public String getDeleteLink() {
         return FILE_DELETE_LINK + fileId;
     }
 
     /**
      * URL to convert the file. Conversion Id has to be appended.
      *
      * @return conversion URL
      */
     @XmlElement(name = "conversionLink")
     public String getConversionLink() {
         return FILE_CONVERSION_LINK + fileId + "&conversionId=";
     }
 
     /**
      * @return the conversions
      */
     public Collection<Conversion> getConversions() {
         return conversions;
     }
 
     /**
      * @return the isLocalFile
      */
     @XmlElement(name = "isLocalFile")
     public boolean isLocalFile() {
         return isLocalFile;
     }
 
     /**
      * @param conversions the conversions to set
      */
     @XmlElement(name = "conversion")
     public void setConversions(Collection<Conversion> conversions) {
         this.conversions = conversions;
     }
 
     /**
      * @param fileId the fileId to set
      */
     public void setFileId(int fileId) {
         this.fileId = fileId;
     }
 
     /**
      * @param sizeInBytes the sizeInBytes to set
      */
     public void setSizeInBytes(long sizeInBytes) {
         this.sizeInBytes = sizeInBytes;
     }
 
     /**
      * @param createdDate the createdDate to set
      */
     public void setCreatedDate(Date createdDate) {
         this.createdDate = createdDate;
     }
 
     /**
      * @param updatedDate the updatedDate to set
      */
     public void setUpdatedDate(Date updatedDate) {
         this.updatedDate = updatedDate;
     }
 
     /**
      * @param downloadedDate the downloadedDate to set
      */
     public void setDownloadedDate(Date downloadedDate) {
         this.downloadedDate = downloadedDate;
     }
 
     /**
      * @param isLocalFile the isLocalFile to set
      */
     public void setLocalFile(boolean isLocalFile) {
         this.isLocalFile = isLocalFile;
     }
 
 }
