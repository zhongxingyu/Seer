 package com.ultrashare.model;
 
 import java.util.Calendar;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 @Entity
 public class Upload {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
 
 	private String senderName;
 
 	private String senderEmail;
 
 	private String fileName;
 
 	private String recipients;
 
	private Calendar uploadDate;
 
 	private Boolean isAlreadyConfirmed;
 
 	public Upload() {
 
 	}
 
 	public Upload(String senderName, String senderEmail, String fileName, String recipients) {
 		this.senderName = senderName;
 		this.senderEmail = senderEmail;
 		this.fileName = fileName;
 		this.recipients = recipients;
		this.uploadDate = Calendar.getInstance();
 		this.isAlreadyConfirmed = false;
 	}
 
 	public Long getConfirmationCode() {
		return uploadDate.getTimeInMillis() + senderName.hashCode() + senderEmail.hashCode() + fileName.hashCode() + recipients.hashCode()
 				+ isAlreadyConfirmed.hashCode();
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getSenderName() {
 		return senderName;
 	}
 
 	public void setSenderName(String senderName) {
 		this.senderName = senderName;
 	}
 
 	public String getSenderEmail() {
 		return senderEmail;
 	}
 
 	public void setSenderEmail(String senderEmail) {
 		this.senderEmail = senderEmail;
 	}
 
 	public String getFileName() {
 		return fileName;
 	}
 
 	public void setFileName(String fileName) {
 		this.fileName = fileName;
 	}
 
 	public String getRecipients() {
 		return recipients;
 	}
 
 	public void setRecipients(String recipients) {
 		this.recipients = recipients;
 	}
 
	public Calendar getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(Calendar uploadDate) {
		this.uploadDate = uploadDate;
	}

 	public Boolean getIsAlreadyConfirmed() {
 		return isAlreadyConfirmed;
 	}
 
 	public void setIsAlreadyConfirmed(Boolean isAlreadyConfirmed) {
 		this.isAlreadyConfirmed = isAlreadyConfirmed;
 	}
 }
