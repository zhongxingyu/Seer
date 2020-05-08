 package com.nearme;
 
 /**
  * Class encapsulating a Point of Interest
  * 
  * @author twhume
  *
  */
 public class Poi {
 	private String name;
 	private double latitude;
 	private double longitude;
 	private PoiType type;
 	private int id;
 	
	public Poi() {};
	
 	public Poi(String n, double la, double lo, PoiType t, int i) {
 		this.name = n;
 		this.latitude = la;
 		this.longitude = lo;
 		this.type = t;
 		this.id = i;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public double getLatitude() {
 		return latitude;
 	}
 	public void setLatitude(double latitude) {
 		this.latitude = latitude;
 	}
 	public double getLongitude() {
 		return longitude;
 	}
 	public void setLongitude(double longitude) {
 		this.longitude = longitude;
 	}
 	public PoiType getType() {
 		return type;
 	}
 	public void setType(PoiType type) {
 		this.type = type;
 	}
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 	
 }
