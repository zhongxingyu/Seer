package com.otanga;
 
 import java.util.Map;
 
 public abstract class Profile {
 	
 	public String getUserId() {
 		return (String) this.getProperty("userId");
 	}
 	
 	/*
 	public String getUserType() {
 		return (String) this.getProperty("userType");
 	}
 	*/
 		
 	public abstract Map<String, Object> getProperties();
 	
 	public abstract boolean hasProperty(String propertyName);
 
 	public abstract Object getProperty(String propertyName);
 	
 	public abstract void setProperty(String propertyName, Object value);
 	
 	public abstract void removeProperty(String propertyName);
 	
 }
