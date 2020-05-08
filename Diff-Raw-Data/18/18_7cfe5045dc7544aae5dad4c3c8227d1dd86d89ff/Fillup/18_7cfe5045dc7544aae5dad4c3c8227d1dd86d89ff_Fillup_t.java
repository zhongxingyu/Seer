 package com.ugtug.hackathon.truempg.model;
 
 import java.util.Date;
 
import javax.persistence.Entity;
 import javax.persistence.Id;
 
@Entity
 public class Fillup {
 	
	@Id Long fillupId;
	Long vehicleId;
	Date date;
	Double quantity;
	Long mileage;
	Long latitude;
	Long longitude;
 	
 	@SuppressWarnings("unused")
 	private Fillup (){}
 	
 	public Fillup ( Long vehicleId, Date date, Double quantity, Long mileage, Long latitude, Long longitude) {
 		this.vehicleId = vehicleId;
 		this.date = date;
 		this.quantity = quantity;
 		this.mileage = mileage;
 		this.latitude = latitude;
 		this.longitude = longitude;
 	}
 	
 	public Long getFillupId() {
 		return fillupId;
 	}
 	public void setFillupId(Long fillupId) {
 		this.fillupId = fillupId;
 	}
 	public Long getVehicleId() {
 		return vehicleId;
 	}
 	public void setVehicleId(Long vehicleId) {
 		this.vehicleId = vehicleId;
 	}
 	public Date getDate() {
 		return date;
 	}
 	public void setDate(Date date) {
 		this.date = date;
 	}
 	public Double getQuantity() {
 		return quantity;
 	}
 	public void setQuantity(Double quantity) {
 		this.quantity = quantity;
 	}
 	public Long getMileage() {
 		return mileage;
 	}
 	public void setMileage(Long mileage) {
 		this.mileage = mileage;
 	}
 	public Long getLatitude() {
 		return latitude;
 	}
 	public void setLatitude(Long latitude) {
 		this.latitude = latitude;
 	}
 	public Long getLongitude() {
 		return longitude;
 	}
 	public void setLongitude(Long longitude) {
 		this.longitude = longitude;
 	}
 
 	
 }
