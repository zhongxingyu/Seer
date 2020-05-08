 package se.chalmers.project14.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /*
  * Copyright (c) 2012 ICRL
  * See the file license.txt for copying permission.
  */
 
 /**
  * class that represent a door
  * 
  * @author tomassellden
  * 
  */
 public class Door {
 
 	private String doorCoordinates;
 	private String building;
 
 	public Door() {
 		// empty constructor
 	}
 
 	public Door(String doorCoordinates, String building) {
 		this.doorCoordinates = doorCoordinates;
 		this.building = building;
 	}
 
 	public void setDoor(String doorCoordinates) {
 		this.doorCoordinates = doorCoordinates;
 	}
 
 	public String getDoorCoordinates() {
 		return doorCoordinates;
 	}
 
 	public void setBuilding(String building) {
 		this.building = building;
 	}
 
 	public String getBuilding() {
 		return building;
 
 	}
 
 }
