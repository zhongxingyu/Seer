 package com.group7.project;
 
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 
 //TODO: Finish off this object. We need a way to keep track of what the building contains.
 //		Boolean values are not at all the best way to do this
 
 public class Building
 {
 	String name;
 	LatLngBounds bounds;
 	//GeoPoint centrePoint;
 	
 	public Building(String name, LatLngBounds bounds)
 	{
 		this.name = name;
 		this.bounds = bounds;
 	}
 	
 	public LatLngBounds getBounds()
 	{
 		return bounds;
 	}
 	
 	public String getName()
 	{
 		return name;
 	}
 	
 	public LatLng getCenter()
 	{
 		double n = bounds.northeast.latitude;
        double e = bounds.northeast.longitude;
        double s = bounds.southwest.latitude;
        double w = bounds.southwest.longitude;
         
        double lat = ((n + s) / 2.0);
        double lon = ((e + w) / 2.0);
         
        return new LatLng(lat, lon);
 	}
 }
