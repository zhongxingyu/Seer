 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.portletui.beans;
 
 import org.apache.commons.fileupload.DiskFileUpload;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.gridlab.gridsphere.portlet.PortletRequest;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * A <code>FileInputBean</code> provides a file upload element
  */
 public class FileInputBean extends InputBean implements TagBean {
 
     public static final int MAX_UPLOAD_SIZE = 1024 * 1024;
     public static final String TEMP_DIR = "/tmp";
 
     public static final String SUBMIT_STYLE = "portlet-form-button";
 
     public static String NAME = "fi";
 
     private FileItem savedFileItem = null;
 
     /**
      * Constructs a default file input bean
      */
     public FileInputBean() {
         super(NAME);
         this.cssStyle = SUBMIT_STYLE;
         this.inputtype = "file";
     }
 
     /**
      * Constructs a file input bean from a portlet request and bean identifier
      *
      * @param request the portlet request
      * @param beanId the bean identifier
      * @throws IOException if an I/O exception occurs
      */
     public FileInputBean(PortletRequest request, String beanId) throws IOException {
         super(NAME);
         this.cssStyle = SUBMIT_STYLE;
         this.inputtype = "file";
         this.request = request;
         this.beanId = beanId;
     }
 
     public FileInputBean(PortletRequest request, String beanId, FileItem fileItem) throws IOException {
         super(NAME);
         this.cssStyle = SUBMIT_STYLE;
         this.inputtype = "file";
         this.request = request;
         this.beanId = beanId;
         savedFileItem = fileItem;
     }
 
     /**
      * Returns the uploaded file name
      *
      * @return the uploaded file name
      */
     public String getFileName() {
         if (savedFileItem != null) {
             return savedFileItem.getName();
         } else {
             return "";
         }
     }
 
     /**
      * Returns the uploaded file size
      *
      * @return the uploaded file size
      */
     public long getFileSize() {
         if (savedFileItem != null) {
             return savedFileItem.getSize();
         } else {
             return 0;
         }
     }
 
     /**
      * Saves the file to the supplied file location path
      *
      * @param filePath the path to save the file
      * @throws IOException if an I/O error occurs saving the file
      */
     public void saveFile(String filePath) throws IOException {
 
        if (!filePath.endsWith("/")) filePath += "/";
 
         File file = new File(filePath);
 
         try {
             if (!file.exists()) file.createNewFile();
             if (savedFileItem != null) savedFileItem.write(file);
         } catch (Exception e) {
             throw new IOException("Unable to save file: " + e);
         }
     }
 
 
     /**
      * Returns with the InputStream of savedFileItem
      * @return InputStream
      * @throws IOException
      */
     public InputStream getInputStream() throws IOException {
         return (savedFileItem != null) ? savedFileItem.getInputStream() : null;
     }
 }
