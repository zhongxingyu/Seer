 //
 // $Id$
 // 
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 // 
 // Created: 2006-04-21
 // Creator: Etienne Gauthier
 // Last modified: $Date$
 //
 // This program is free software; you can redistribute it and/or modify it under
 // the terms of the GNU General Public License as published by the Free Software
 // Foundation; either version 2 of the License, or (at your option) any later
 // version. This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 // details. You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software Foundation, Inc.,
 // 675 Mass Ave, Cambridge, MA 02139, USA.
 
 package wjhk.jupload2.filedata;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.Properties;
 
 import wjhk.jupload2.exception.JUploadException;
 import wjhk.jupload2.exception.JUploadExceptionTooBigFile;
 import wjhk.jupload2.exception.JUploadIOException;
 import wjhk.jupload2.policies.DefaultUploadPolicy;
 import wjhk.jupload2.policies.UploadPolicy;
 
 /**
  * This class contains all data and methods for a file to upload. The current
  * {@link wjhk.jupload2.policies.UploadPolicy} contains the necessary parameters
  * to personalize the way files must be handled. <BR>
  * <BR>
  * This class is the default FileData implementation. It gives the default
  * behaviour, and is used by {@link DefaultUploadPolicy}. It provides standard
  * control on the files choosen for upload.
  * 
  * @see FileData
  * @author Etienne Gauthier
  */
 public class DefaultFileData implements FileData {
 
     /**
      * The current upload policy.
      */
     UploadPolicy uploadPolicy;
 
     /**
      * the mime type list, coming from: http://www.mimetype.org/ Thanks to them!
      */
     public static Properties mimeTypes = null;
 
     // ///////////////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////////// Protected attributes
     // /////////////////////////////////////////////////////
     // ///////////////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Mime type of the file. It will be written in the upload HTTP request.
      */
     protected String mimeType = "application/octet-stream";
 
     // ///////////////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////////// Private attributes
     // ////////////////////////////////////////////////////////
     // ///////////////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * file is the file about which this FileData contains data.
      */
     private File file;
 
     /**
      * Cached file size
      */
     private long fileSize;
 
     /**
      * Cached file directory
      */
     private String fileDir;
 
     /**
      * cached root of this file
      */
     private String fileRoot = "";
 
     /**
      * Cached file modification time.
      */
     private Date fileModified;
 
     /**
      * Standard constructor
      * 
      * @param file The file whose data this instance will give.
      */
     public DefaultFileData(File file, File root, UploadPolicy uploadPolicy) {
         this.file = file;
         this.uploadPolicy = uploadPolicy;
         this.fileSize = this.file.length();
         this.fileDir = this.file.getAbsoluteFile().getParent();
         this.fileModified = new Date(this.file.lastModified());
         if (null != root)
             this.fileRoot = root.getAbsolutePath();
 
         // Let's load the mime types list.
         if (mimeTypes == null) {
             mimeTypes = new Properties();
             try {
                 mimeTypes.load(getClass().getResourceAsStream(
                         "/conf/mimetypes.properties"));
             } catch (IOException e) {
                 uploadPolicy.displayWarn("Unable to load the mime types list: "
                         + e.getMessage());
                 mimeTypes = null;
             }
         }
 
         // Let
         this.mimeType = mimeTypes.getProperty(getFileExtension().toLowerCase());
         if (this.mimeType == null) {
             this.mimeType = "application/octet-stream";
         }
     }
 
     /** @see FileData#beforeUpload() */
     public void beforeUpload() throws JUploadException {
         // Default : we check that the file is smalled than the maximum upload
         // size.
         if (getUploadLength() > this.uploadPolicy.getMaxFileSize()) {
             throw new JUploadExceptionTooBigFile(getFileName(),
                     getUploadLength(), this.uploadPolicy);
         }
     }
 
     /** @see FileData#getUploadLength() */
     @SuppressWarnings("unused")
     public long getUploadLength() throws JUploadException {
         return this.fileSize;
     }
 
     /** @see FileData#afterUpload() */
     public void afterUpload() {
         // Nothing to do here
     }
 
     /** @see FileData#getInputStream() */
     public InputStream getInputStream() throws JUploadException {
         // Standard FileData : we read the file.
         try {
             return new FileInputStream(this.file);
         } catch (FileNotFoundException e) {
             throw new JUploadIOException(e);
         }
     }
 
     /** @see FileData#getFileName() */
     public String getFileName() {
         return this.file.getName();
     }
 
     /** @see FileData#getFileExtension() */
     public String getFileExtension() {
         String name = this.file.getName();
         return name.substring(name.lastIndexOf('.') + 1);
     }
 
     /** @see FileData#getFileLength() */
     public long getFileLength() {
         return this.fileSize;
     }
 
     /** @see FileData#getLastModified() */
     public Date getLastModified() {
         return this.fileModified;
     }
 
     /** @see FileData#getDirectory() */
     public String getDirectory() {
         return this.fileDir;
     }
 
     /** @see FileData#getMimeType() */
     public String getMimeType() {
         return this.mimeType;
     }
 
     /** @see FileData#canRead() */
     public boolean canRead() {
         return this.file.canRead();
     }
 
     /** @see FileData#getFile() */
     public File getFile() {
         return this.file;
     }
 
     /**
      * @see wjhk.jupload2.filedata.FileData#getRelativeDir()
      */
     public String getRelativeDir() {
         if (null != this.fileRoot && (!this.fileRoot.equals(""))
                 && (this.fileDir.startsWith(this.fileRoot))) {
             int skip = this.fileRoot.length();
             if (!this.fileRoot.endsWith(File.separator))
                 skip++;
            if ((skip >= 0) && (skip < this.fileDir.length()))
                return this.fileDir.substring(skip);
         }
         return "";
     }
 }
