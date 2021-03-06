 package com.gfk.senbot.framework.context;
 
 public class PropertyOverwritePlaceholder {
 	
 	private String overwriteProp;
 	private final String defaultValue;
 
	public PropertyOverwritePlaceholder(String property, String overwriteShortHand) {
 		this.defaultValue = property;
		overwriteProp = System.getProperty(overwriteShortHand);
 	}
 	
 	public String getProperty() {
 		return overwriteProp == null ? defaultValue : overwriteProp ;
 	}
 
 }
