 package com.openmap.grupp1.database;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import com.google.android.gms.maps.model.LatLng;
 
 public class LocationMarker {
 	
 	String title;
 	double lat;
 	double lng;
 	LatLng loc;
 	String desc = "No description";
 	String[] tags;
<<<<<<< HEAD:src/com/openmap/grupp1/LocationPair.java
	
	public LocationPair(){
		
	}
	
	public LocationPair(String title, double lat, double lng, String desc, String[] tags){
=======
 	public LocationMarker(){
 		
 	}
 	public LocationMarker(String title, double lat, double lng, String desc, String[] tags){
>>>>>>> e4d91407096508bcf9ecc20e11485c9580fc3541:src/com/openmap/grupp1/database/LocationMarker.java
 		this.title	=	title;
 		this.lat	=	lat;
 		this.lng	=	lng;
 		this.loc	=	new LatLng(lat,lng);
 		this.desc	=	desc;
 		this.tags	=	tags;
 		
 	}
 	
 	
 
 	public LocationMarker(String title, LatLng loc, String desc, String[] tags){
 		this.title	=	title;
 		this.loc	=	loc;
 		this.lat	=	loc.latitude;
 		this.lng	=	loc.longitude;
 		this.desc	=	desc;
 		this.tags	=	tags;
 	}
 	
 	public LocationMarker(String title, double lat, double lng, String[] tags){
 		this.title	=	title;
 		this.lat	=	lat;
 		this.lng	=	lng;
 		this.loc	=	new LatLng(lat,lng);
 		this.tags	=	tags;
 	}
 
 	public LocationMarker(String title, LatLng loc, String[] tags){
 		this.title	=	title;
 		this.loc	=	loc;
 		this.lat	=	loc.latitude;
 		this.lng	=	loc.longitude;
 		this.tags	=	tags;
 	}
 
 	public ArrayList<NameValuePair> getPairsList(){
 		ArrayList<NameValuePair> nvpl = new ArrayList<NameValuePair>();
 		nvpl.add(new BasicNameValuePair("title", title));
 		nvpl.add(new BasicNameValuePair("lat", Double.toString(lat)));
 		nvpl.add(new BasicNameValuePair("lng", Double.toString(lng)));
 		nvpl.add(new BasicNameValuePair("desc", desc));
 
 		int i = 0;
 		for(String s : tags)
 			nvpl.add(new BasicNameValuePair("tag"+i++, s));
 
 		return nvpl;
 	}
 	
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public double getLatitute() {
 		return lat;
 	}
 
 	public void setLatitude(double lat) {
 		this.lat = lat;
 	}
 
 	public double getLongitude() {
 		return lng;
 	}
 
 	public void setLongitude(double lng) {
 		this.lng = lng;
 	}
 
 	public String getDescription() {
 		return desc;
 	}
 
 	public void setDescription(String desc) {
 		this.desc = desc;
 	}
 
 	public String[] getTags() {
 		return tags;
 	}
 
 	public void setTags(String[] tags) {
 		this.tags = tags;
 	}
 	
 	public LatLng getLatLng() {
 	 	return loc;
 	 }	 
 	 
 	 public void setLatLng(LatLng loc){
 	 	this.loc	=	loc;
 	 }
 	 
 	 public void setLatLng(){
 		 this.loc = new LatLng(this.lat,this.lng);
 	 }
 	
 }
