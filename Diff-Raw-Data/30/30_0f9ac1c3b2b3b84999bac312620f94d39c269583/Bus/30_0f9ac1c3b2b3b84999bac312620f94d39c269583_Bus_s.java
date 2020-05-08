 package com.example.excommonersebenelib;
 
 import com.google.android.gms.maps.model.LatLng;
 
 public class Bus {
	String id;
	Route route;
	double x,y;
	LatLng geoPoint;
 	
	public Bus(String id, Route route){
 		this.id = id;
		this.route = route;
 	}
 	
 	public void setLocation(double x,double y){
 		this.x = x;
 		this.y = y;
 		this.geoPoint = new LatLng(x, y);
 	}
 }
