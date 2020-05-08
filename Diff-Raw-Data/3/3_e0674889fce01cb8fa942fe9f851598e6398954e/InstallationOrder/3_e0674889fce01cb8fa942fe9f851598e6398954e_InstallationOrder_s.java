 package com.google.code.qualitas.integration.api;
 
 import com.google.code.qualitas.engines.api.core.ProcessType;
 
 /**
  * The Class ProcessBundleInstallationOrder.
  */
 public class InstallationOrder {
 
     /** The bundle. */
     private byte[] bundle;
 
     /** The username. */
     private String username;
 
     /** The content type. */
     private String contentType;
 
     /** The process id. */
     private long processId;
 
     /** The process type. */
     private ProcessType processType;
 
     /**
      * Gets the bundle.
      * 
      * @return the bundle
      */
     public byte[] getBundle() {
         return bundle;
     }
 
     /**
      * Sets the bundle.
      * 
      * @param bundle
      *            the new bundle
      */
     public void setBundle(byte[] bundle) {
         this.bundle = bundle;
     }
 
     /**
      * Gets the username.
      * 
      * @return the username
      */
     public String getUsername() {
         return username;
     }
 
     /**
      * Sets the username.
      * 
      * @param username
      *            the new username
      */
     public void setUsername(String username) {
         this.username = username;
     }
 
     /**
      * Gets the content type.
      * 
      * @return the content type
      */
     public String getContentType() {
         return contentType;
     }
 
     /**
      * Sets the content type.
      * 
      * @param contentType
      *            the new content type
      */
     public void setContentType(String contentType) {
         this.contentType = contentType;
     }
 
     /**
      * Gets the process id.
      * 
      * @return the process id
      */
     public long getProcessId() {
         return processId;
     }
 
     /**
      * Sets the process id.
      * 
      * @param processId
      *            the new process id
      */
     public void setProcessId(long processId) {
         this.processId = processId;
     }
 
     /**
      * Gets the process type.
      * 
      * @return the process type
      */
     public ProcessType getProcessType() {
         return processType;
     }
 
     /**
      * Sets the process type.
      * 
      * @param processType
      *            the new process type
      */
     public void setProcessType(ProcessType processType) {
         this.processType = processType;
     }
}
