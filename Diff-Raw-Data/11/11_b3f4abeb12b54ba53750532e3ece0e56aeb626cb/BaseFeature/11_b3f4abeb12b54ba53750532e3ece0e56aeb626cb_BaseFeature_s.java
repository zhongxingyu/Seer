 package com.pablobm.jwhat.profiles;
 
 public class BaseFeature
 	implements Feature {
 
 	private String name;
 	private Object value;
 
 	public BaseFeature(String name) {
 		this.name = name;
 	}
 	public BaseFeature(String name, String value) {
 		this.name = name;
		this.name = value;
 	}
 	public BaseFeature(String name, int value) {
 		this.name = name;
		this.value = new Integer(value);
 	}
 	public BaseFeature(String name, boolean value) {
 		this.name = name;
		this.value = new Boolean(value);
 	}
 
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String strValue() {
 		return (String)this.value;
 	}
 	public String strValue(String value) {
 		assignUnlessNull(value);
 		return strValue();
 	}
 
 	public int intValue() {
 		return ((Integer)this.value).intValue();
 	}
 	public int intValue(int value) {
 		assignUnlessNull(new Integer(value));
 		return intValue();
 	}
 
 	public boolean boolValue() {
 		return ((Boolean)this.value).booleanValue();
 	}
 	public boolean boolValue(boolean value) {
 		assignUnlessNull(new Boolean(value));
 		return boolValue();
 	}
 
 
 	private void assignUnlessNull(Object value) {
		if (value == null) {
 			this.value = value;
 		}
 	}
 }
