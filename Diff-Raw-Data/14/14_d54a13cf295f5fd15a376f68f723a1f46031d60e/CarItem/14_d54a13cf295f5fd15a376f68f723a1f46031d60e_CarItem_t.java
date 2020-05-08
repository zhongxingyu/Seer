 package com.example.myauto.item;
 
import java.util.HashMap;
 import java.util.Observable;
 
 public class CarItem extends Observable implements Item{
	private HashMap<String, String> itemData = new HashMap<String, String>();
	
 	@Override
 	public void setValueToProperty(String property, String value) {
 		itemData.put(property, value);
 	}
 
 	@Override
 	public String getValueFromProperty(String property) {
 		String val = null;
 		if(itemData.containsKey(property)){
 			val = itemData.get(property);
 		}
 		return val;
 	}
 
 }
