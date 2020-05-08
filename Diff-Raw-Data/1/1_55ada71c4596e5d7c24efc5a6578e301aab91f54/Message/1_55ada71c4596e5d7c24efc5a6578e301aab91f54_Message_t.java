 package org.touchirc.model;
 
 import java.util.Date;
 
 public class Message {
 
 	public final static int TYPE_MESSAGE = 0;
 	public final static int TYPE_NOTICE = 1;
 	public final static int TYPE_MENTION = 2;
 	
 	private String content;
 	private String author;
 	private int type;
 	private long timestamp;
 			
 	public Message(String text) {
 		this(text, null, TYPE_MESSAGE);
 	}
 	
 	public Message(String text, String author) {
 		this(text, author, TYPE_MESSAGE);
 	}
 	
 	public Message(String text, int type) {
 		this(text,null,type);
 	}
 
 	public Message(String text, String author, int type) {
 		this.content = text;
 		this.author = author;
 		this.content = text;
 		this.timestamp = new Date().getTime();
 	}
 	
 	public String getAuthor(){
 		return this.author;
 	}
 	
 	public String getMessage(){
 		return this.content;
 	}
 	
 	public int getType(){
 		return this.type;
 	}
 	
 	public long getTime(){
 		return this.timestamp;
 	}
 
 	public String toString(){
 		return "<" + this.author + "> " + this.content;
 	}	
 	
 }
