 package com.mray.brewerys;
 
 public class Brewery implements Company {
 	// Class Vars
 	String name;
 	String location;
 	
 	
 	public Brewery(String name, String location){
 		setName(name);
		//setLocation(location);
 	}
 	
 	@Override
 	public boolean setName(String name) {
 		this.name = name;	
 		return true;
 	}
 
 	@Override
 	public boolean setLocation() {
 		this.location = location;
 		return true;
 	}
 
 	@Override
 	public String getName() {
 		return this.name;
 	}
 
 	@Override
 	public String getLocation() {
 		return this.location;
 	}
 
 
 }
