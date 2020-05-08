 package me.justshare.domain;
 
 import me.justshare.storage.AmazonKeys;
 
 /**
  * User: filippo@diotalevi.com
  * Date: Nov 17, 2010
  */
 public class SharedItem {
 
     private String fileKey;
     private String contentType;
     private String description;
 
     public SharedItem(String f, String c, String d) {
         this.fileKey = f;
         this.contentType = c;
         this.description = d;
     }
 
     public String getFileKey() {
         return fileKey;
     }
 
     public String getContentType() {
         return contentType;
     }
 
     public String getDescription() {
         return description;
     }
 
     public String getFileUrl() {
         return fileKey == null ? null : "http://"+ AmazonKeys.DOMAIN + ".s3.amazonaws.com/" + fileKey;
     }
     
 }
