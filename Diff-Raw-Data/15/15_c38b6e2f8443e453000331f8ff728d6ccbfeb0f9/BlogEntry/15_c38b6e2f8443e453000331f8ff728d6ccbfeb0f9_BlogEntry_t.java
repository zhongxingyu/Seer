 package com.kt.kbp.blogger;
 
import java.io.Serializable;
 import java.util.Date;
 
public class BlogEntry implements Serializable {
 
	private static final long serialVersionUID = 8160213240317250757L;
 	private String id;
 	private Date datePublished;
 	private String title;
 	private String content;
 	
 	public BlogEntry(String id, Date datePublished, String title, String content) {
 		this.setId(id);
 		this.setDatePublished(datePublished);
 		this.setTitle(title);
 		this.setContent(content);
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public Date getDatePublished() {
 		return datePublished;
 	}
 
 	public void setDatePublished(Date datePublished) {
 		this.datePublished = datePublished;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}
 	
 }
