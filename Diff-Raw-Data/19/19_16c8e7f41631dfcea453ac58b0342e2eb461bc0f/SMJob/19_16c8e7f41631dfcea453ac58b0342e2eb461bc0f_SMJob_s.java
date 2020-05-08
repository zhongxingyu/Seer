 package com.fifthfloor.gps.server.objects;
 
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import org.joda.time.DateTime;
 import org.vaadin.appfoundation.persistence.data.AbstractPojo;
 
 import com.fifthfloor.gps.helpers.MapsHelper;
 import com.fifthfloor.gps.server.MapReader;
 
 @Entity
 @Table(name = "appSMJob", uniqueConstraints = { @UniqueConstraint(columnNames = { "customername" }) })
 public class SMJob extends AbstractPojo {
 	private DateTime arrivaltime = null;
 	private String customername = "";
 	private boolean hasarrived = false;
 	private String location = "";
 	private String vehicle = "";
 	
 	public String getVehicle() {
 		return vehicle;
 	}
 
 	public void setVehicle(String vehicle) {
 		this.vehicle = vehicle;
 	}
 
 	public String getLocation() {
 		return location;
 	}
 
 	public void setLocation(String location) {
 		this.location = location;
 	}
 
 	public boolean isHasarrived() {
 		return hasarrived;
 	}
 
 	public void setHasarrived(boolean hasarrived) {
 		this.hasarrived = hasarrived;
 	}
 
 	public String getCustomername() {
 		return customername;
 	}
 
 	public void setCustomername(String customername) {
 		this.customername = customername;
 	}
 
 	public DateTime getArrivaltime() {
 		return arrivaltime;
 	}
 
 	public void setArrivaltime(DateTime arrivaltime) {
 		this.arrivaltime = arrivaltime;
 	}
 
 	private boolean alertsent = false;
 
 	public boolean isAlertsent() {
 		return alertsent;
 	}
 
 	public void setAlertsent(boolean alertsent) {
 		this.alertsent = alertsent;
 	}
 
 	public SMJob(DateTime at) {
 		arrivaltime = at;
 	}
 	
 	String time = "";
 	String name = "";
 	String techs = "";
 	String address = "";
 	public String getAddress() {
 		return address;
 	}
 
 	public void setAddress(String a) {
 		String[] sa = a.split(" ");
		String url = "http://maps.google.com/maps/api/geocode/xml?address="+sa[0]+"+"+sa[1]+"+"+sa[2]+"+"+sa[3]+"&sensor=false";
 		MapReader mr = new MapReader(url);
 		this.location = mr.getAddress();
 		this.address = address;
 	}
 
 	public String getTime() {
 		return time;
 	}
 
 	public void setTime(String time) {
 		this.time = time;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getTechs() {
 		return techs;
 	}
 
 	public void setTechs(String techs) {
 		this.techs = techs;
 	}
 
 	
 	public SMJob(){
 		
 	}
 	
 	public SMJob(String s){
 		String[] sa = s.split(";");
 		this.time = sa[0];
 		this.name = sa[1];
 		this.address = sa[2];
 		this.techs = sa[3];
 		
 	}
 
 }
