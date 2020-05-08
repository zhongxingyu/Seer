 package edu.mines.csci498.ybakos.lunchlist;
 
 public class Restaurant {
 
 	private String name = "";
 	private String address = "";
 	private String type = "";
 	private String notes = "";
 	
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public String getAddress() {
 		return address;
 	}
 	
 	public void setAddress(String address) {
 		this.address = address;
 	}
 	
	public void setNotes(String notes) {
 		this.notes = notes;
 	}
 	
 	public String getNotes() {
 		return notes;
 	}
 	
 	public String toString() {
 		return getName();
 	}
 
 }
