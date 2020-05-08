 package com.nagazuka.mobile.android.goedkooptanken.model;
 
 import com.google.android.maps.GeoPoint;
 
 public class Place {
 	private double price = 0.0;
 	private double distance = 0.0;
 	private String name = "";
 	private String address = "";
 	private String town = "";
 	private String postalCode = "";
 	private String observationDate = "";
 	private GeoPoint point = null;
 
 	public Place() {
 	}
 
 	public Place(String name, String address, String postalCode, String town,
 			double price, double distance, String observationDate) {
 		super();
 		this.price = price;
 		this.distance = distance;
 		this.postalCode = postalCode;
 		this.town = town;
 		this.name = name;
 		this.address = address;
 		this.observationDate = observationDate;
 	}
 
 	public void setTown(String town) {
 		this.town = town;
 	}
 
 	public String getTown() {
 		return town;
 	}
 
 	public double getPrice() {
 		return price;
 	}
 
 	public void setPrice(double price) {
 		this.price = price;
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
 
 	public void setDistance(double distance) {
 		this.distance = distance;
 	}
 
 	public double getDistance() {
 		return distance;
 	}
 
 	public void setPostalCode(String postalCode) {
 		this.postalCode = postalCode;
 	}
 
 	public String getPostalCode() {
 		return postalCode;
 	}
 
 	public void setPoint(GeoPoint point) {
 		this.point = point;
 	}
 
 	public GeoPoint getPoint() {
 		return point;
 	}
 	
 	public String getSummary() {
 		String result = getAddress() + "\n";
 		result += getPostalCode() + "\n";
 		result += getTown() + "\n";
 		result += String.format("Literprijs: \u20AC %.2f\n", getPrice());
 		result += String.format("Geschatte afstand: %.2f km\n", getDistance());
		result += "Peildatum: " + getObservationDate() + "\n";
 		return result; 
 	}
 
 	public void setObservationDate(String observationDate) {
 		this.observationDate = observationDate;
 	}
 
 	public String getObservationDate() {
 		return observationDate;
 	}
 }
