 package com.ultrashare.model;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
import javax.persistence.JoinColumn;
 import javax.persistence.OneToMany;
 
 @Entity
 public class Upload {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
 
 	private String senderName;
 
 	private String senderEmail;
 
 	private String fileName;
 
 	private String fileContentType;
 
 	private Long fileSize;
 
 	private String recipients;
 
 	private Calendar creationDate;
 
 	private Long creationTimeInMillis;
 
 	private Boolean isAlreadyConfirmed;
 
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "id")
 	private List<Share> shares;
 
 	public Upload() {
 
 	}
 
 	public Upload(String senderName, String senderEmail, String fileName, String fileContentType, Long fileSize, String recipients) {
 		this.senderName = senderName;
 		this.senderEmail = senderEmail;
 		this.fileName = fileName;
 		this.fileContentType = fileContentType;
 		this.fileSize = fileSize;
 		this.recipients = recipients;
 		this.creationDate = Calendar.getInstance();
 		this.creationTimeInMillis = creationDate.getTimeInMillis();
 		this.isAlreadyConfirmed = false;
 		this.shares = new ArrayList<Share>();
 	}
 
 	public Long getConfirmationCode() {
 		return creationTimeInMillis + senderName.hashCode() + senderEmail.hashCode() + fileName.hashCode() + recipients.hashCode()
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
 
 	public String getFileContentType() {
 		return fileContentType;
 	}
 
 	public void setFileContentType(String fileContentType) {
 		this.fileContentType = fileContentType;
 	}
 
 	public Long getFileSize() {
 		return fileSize;
 	}
 
 	public void setFileSize(Long fileSize) {
 		this.fileSize = fileSize;
 	}
 
 	public String getRecipients() {
 		return recipients;
 	}
 
 	public void setRecipients(String recipients) {
 		this.recipients = recipients;
 	}
 
 	public Calendar getCreationDate() {
 		return creationDate;
 	}
 
 	public void setCreationDate(Calendar creationDate) {
 		this.creationDate = creationDate;
 	}
 
 	public Long getCreationTimeInMillis() {
 		return creationTimeInMillis;
 	}
 
 	public void setCreationTimeInMillis(Long creationTimeInMillis) {
 		this.creationTimeInMillis = creationTimeInMillis;
 	}
 
 	public Boolean getIsAlreadyConfirmed() {
 		return isAlreadyConfirmed;
 	}
 
 	public void setIsAlreadyConfirmed(Boolean isAlreadyConfirmed) {
 		this.isAlreadyConfirmed = isAlreadyConfirmed;
 	}
 
 	public List<Share> getShares() {
 		return shares;
 	}
 
 	public void setShares(List<Share> shares) {
 		this.shares = shares;
 	}
 }
