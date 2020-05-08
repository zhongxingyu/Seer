 package com.gwt.schoolviewer.client;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 public class SchoolValue implements Serializable {
 	String name;
 	ArrayList<String> values;
 	String location;
 	String district;
 	
	public SchoolValue(){}
	
 	public SchoolValue(String name, ArrayList<String> values, String location, String district) {
 		this.name = name;
 		this.values = values;
 		this.location = location;
 		this.district = district;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public ArrayList<String> getValues() {
 		return values;
 	}
 	public void setValues(ArrayList<String> values) {
 		this.values = values;
 	}
 	public String getLocation() {
 		return location;
 	}
 	public void setLocation(String location) {
 		this.location = location;
 	}
 	public String getDistrict() {
 		return district;
 	}
 	public void setDistrict(String district) {
 		this.district = district;
 	}
 }
