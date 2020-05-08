 package com.example.myauto.item;
 
import java.util.HashMap;
 
 public interface Item {
	static HashMap<String, String> itemData = new HashMap<String, String>();
	
 	public void setValueToProperty(String property, String value);
 	
 	public String getValueFromProperty(String property);
 }
