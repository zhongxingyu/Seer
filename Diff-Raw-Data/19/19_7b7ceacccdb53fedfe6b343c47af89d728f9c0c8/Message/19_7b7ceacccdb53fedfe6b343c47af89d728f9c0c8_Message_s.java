 package com.voxeo.ozone.web.client;
 
 public class Message {
 
	public enum Type { IN, OUT };
 	
 	public Message(String message, Type type) {
 		
 		this.message = message;
 		this.type = type;
 	}
 	
 	private String message;
 	private Type type;
 	
 	public String getMessage() {
 		return message;
 	}
 	public Type getType() {
 		return type;
 	}
 }
