 package com.braintrust.shootmanager.model;
 
 import java.sql.Date;
 import java.util.List;
 
 public class Shoot extends Model {
 	public Shoot(int id) {
 		super(id);
 	}
 	
 	private int locationId;
 	private int photographerId;
 	private Date date;
 	private String weatherDesc;
 	private Location location;
 	private List<Equipment> equipment;
 	private List<Integer> equipmentIds;
 	private List<Integer> subjectIds;
 	
 	public Date getDate(){
 		return this.date;
 	}
 	
 	public void setDate(Date date){
 		this.date = date;
 	}
 	
 	public String getWeatherDesc(){
 		return this.weatherDesc;
 	}
 	
 	public void setWeatherDesc(String weatherDesc){
 		this.weatherDesc = weatherDesc;
 	}
 	
 	public int getLocationId(){
 		return this.locationId;
 	}
 	
 	public void setLocationId(int locationId){
 		this.locationId = locationId;
 	}
 	
 	public int getPhotographerId(){
 		return this.photographerId;
 	}
 	
 	public void setPhotographerId(int photographerId){
 		this.photographerId = photographerId;
 	}
 	
 	public Location getLocation(){
 		return this.location;
 	}
 	
 	public void setLocation(Location location){
 		this.location = location;
 	}
 	
 	public List<Equipment> getEquipment(){
 		return this.equipment;
 	}
 	
 	public void addEquipment(Equipment equipment){
 		this.equipment.add(equipment);
 	}
 	
 	public void addEquipment(List<Equipment> equipment){
 		this.equipment.addAll(equipment);
 	}
 	
 	public List<Integer> getEquipmentIds(){
 		return this.equipmentIds;
 	}
 	
 	public void addEquipmentId(int equipmentId){
 		this.equipmentIds.add(equipmentId);
 	}
 	
 	public List<Integer> getSubjectIds(){
 		return this.subjectIds;
 	}
 	
 	public void addSubjectId(int subjectId){
 		this.subjectIds.add(subjectId);
 	}
 	
 	
 }
