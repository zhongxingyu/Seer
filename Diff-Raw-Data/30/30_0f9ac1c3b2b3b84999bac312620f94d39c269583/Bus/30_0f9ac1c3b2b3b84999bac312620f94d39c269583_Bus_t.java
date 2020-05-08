 package com.example.excommonersebenelib;
 
 import com.google.android.gms.maps.model.LatLng;
 
 public class Bus {
	public String id;
	public double x,y;
	public LatLng geoPoint;
 	
	public Bus(String id){
 		this.id = id;
	}
	
	public Bus(String id,double x, double y){
		this.id = id;
		setLocation(x,y);
 	}
 	
 	public void setLocation(double x,double y){
 		this.x = x;
 		this.y = y;
 		this.geoPoint = new LatLng(x, y);
 	}
 }
