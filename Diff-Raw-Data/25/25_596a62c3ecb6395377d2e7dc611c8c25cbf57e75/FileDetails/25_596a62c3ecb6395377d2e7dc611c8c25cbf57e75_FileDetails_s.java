 package com.lulu.publish.model;
 
 import org.codehaus.jackson.annotate.JsonProperty;
 
 public class FileDetails {
     public static final String MIME_TYPE_PDF = "application/x-pdf";
 
    private long fileId;
     private String mimetype;
     private String filename;
 
     @JsonProperty("file_id")
    public long getFileId() {
         return fileId;
     }
 
     @JsonProperty("file_id")
    public void setFileId(long fileId) {
         this.fileId = fileId;
     }
 
     public String getMimetype() {
         return mimetype;
     }
 
     public void setMimetype(String mimetype) {
         this.mimetype = mimetype;
     }
 
     public String getFilename() {
         return filename;
     }
 
     public void setFilename(String filename) {
         this.filename = filename;
     }
 
     @Override
     public String toString() {
         return "FileDetails{"
                 + "fileId=" + fileId
                 + ", mimetype='" + mimetype + '\''
                 + ", filename='" + filename + '\''
                 + '}';
     }
 }
