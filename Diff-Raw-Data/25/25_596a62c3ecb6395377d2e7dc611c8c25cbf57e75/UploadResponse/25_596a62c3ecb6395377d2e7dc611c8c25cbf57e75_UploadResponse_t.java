 package com.lulu.publish.response;
 
 import java.util.Date;
 
 import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 
 /**
  *
  */
@JsonIgnoreProperties(ignoreUnknown = true)
 public class UploadResponse {
 
     @JsonProperty("fileId")
    private String fileId;
     @JsonProperty("createdDate")
     private Date createdDate;
     @JsonProperty("mimeType")
     private String mimeType;
     @JsonProperty("name")
     private String name;
     @JsonProperty("size")
     private Long size;
     @JsonProperty("updatedDate")
     private Date updatedDate;
 
 
     @Override
     public String toString() {
         return "UploadResponse{"
                 + "name=" + name
                 + "fileId=" + fileId
                 + '}';
     }
 
	public String getFileId() {
     	return fileId;
     }
 
	public void setFileId(String fileId) {
     	this.fileId = fileId;
     }
 
 	public Date getCreatedDate() {
     	return createdDate;
     }
 
 	public void setCreatedDate(Date createdDate) {
     	this.createdDate = createdDate;
     }
 
 	public String getMimeType() {
     	return mimeType;
     }
 
 	public void setMimeType(String mimeType) {
     	this.mimeType = mimeType;
     }
 
 	public String getName() {
     	return name;
     }
 
 	public void setName(String name) {
     	this.name = name;
     }
 
 	public Long getSize() {
     	return size;
     }
 
 	public void setSize(Long size) {
     	this.size = size;
     }
 
 	public Date getUpdatedDate() {
     	return updatedDate;
     }
 
 	public void setUpdatedDate(Date updatedDate) {
     	this.updatedDate = updatedDate;
     }
 }
