 package com.example.myauto.item;
 
 import java.util.Observable;
 
 public class CarItem extends Observable implements Item{

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
