 package com.zotyo.accounts.entity;
 import java.util.Date;
 
 import javax.persistence.Basic;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 import javax.persistence.Transient;
 import javax.persistence.Id;
 import javax.persistence.Column;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 /**
  *
  * @author Zoli
  */
 
 @Entity
@Table(name = "accounts", uniqueConstraints={@UniqueConstraint(columnNames={"project", "entryname"})})
 public class AccountEntity {
 	@Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id")
     private Integer id;
 	
 	@Column(name = "project")
     private String project;
 	@Column(name = "entryname")
     private String entryname;
	
 	@Column(name = "url")
     private String url;
 	@Column(name = "username")
     private String username;
 	@Column(name = "password")
     private String password;
 	@Column(name = "tag")
     private String tag;
 	@Column(name = "last_modified")
 	@Temporal(TemporalType.TIMESTAMP)	
     private Date lastModified;
     
     @Transient
     private String password2;
     @Transient
     private String errorMessage;
 
     public AccountEntity() {
     }
 
     public AccountEntity(String project, String entryname) {
         this.project = project;
         this.entryname = entryname;
     }
     
     public AccountEntity(String project, String entryname, String url, String username,
             String password, String password2, String tag) {
         this.project = project;
         this.entryname = entryname;
         this.url = url;
         this.username = username;
         this.password = password;
         this.password2 = password2;
         this.tag = tag;
     }
     
     public Integer getId() {
     	return id;
     }
     
     public void setId(Integer id) {
     	this.id = id;
     }
     
     public String getEntryname() {
         return entryname;
     }
 
     public void setEntryname(String entryname) {
         this.entryname = entryname;
     }
 
     public Date getLastModified() {
         return lastModified;
     }
 
     public void setLastModified(Date lastModified) {
         this.lastModified = lastModified;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getPassword2() {
         return password2;
     }
 
     public void setPassword2(String password2) {
         this.password2 = password2;
     }
 
     public String getProject() {
         return project;
     }
 
     public void setProject(String project) {
         this.project = project;
     }
 
     public String getTag() {
         return tag;
     }
 
     public void setTag(String tag) {
         this.tag = tag;
     }
 
     public String getUrl() {
         return url;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getErrorMessage() {
         return errorMessage;
     }
 
     public void setErrorMessage(String errorMessage) {
         this.errorMessage = errorMessage;
     }
 
     public String toString() {
     	StringBuilder sb = new StringBuilder();
     	sb.append(this.project + " | ");
     	sb.append(this.entryname);
     	return sb.toString();
     }
     
     public String toStringDetailed() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("Project: " + this.project);
     	sb.append("\nEntry name: " + this.entryname);
     	sb.append("\nURL: " + this.url);
     	sb.append("\nUsername: " + this.username);
     	sb.append("\nPassword: " + this.password);
     	if (tag.length() > 0) {
     		sb.append("\nTag: " + this.tag);
     	}
     	sb.append("\nLast Modified: " + this.lastModified);
     	return sb.toString();
     }
 }
