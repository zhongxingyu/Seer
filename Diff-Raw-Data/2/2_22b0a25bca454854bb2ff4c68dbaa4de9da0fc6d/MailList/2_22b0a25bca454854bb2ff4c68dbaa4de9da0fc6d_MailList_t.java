 package com.unisender.entities;
 
 public class MailList {
 	private Integer id;
 	private String title;
 
 	public MailList(String title) {
 		this.id = 0;
 		this.title = title;
 	}
 
 	public MailList(Integer id) {
 		this.id = id;
 	}
 
 	public MailList(Integer id, String title) {
 		this.id = id;
 		this.title = title;
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	@Override
 	public String toString(){
		return Integer.toString(this.id);
 	}
 }
