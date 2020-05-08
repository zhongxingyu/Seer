 package com.dev.gis.connector.sunny;
 
 import java.util.ArrayList;
 import java.util.List;
 
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
 public class OfferFilterTemplate {
 	
 	
 	private List<ObjectValuePair> fuelPolicy = new ArrayList<ObjectValuePair>();;
 
 	private List<ObjectValuePair> stationLocTypes = new ArrayList<ObjectValuePair>();;
 
 	private List<ObjectValuePair> bodyStyles = new ArrayList<ObjectValuePair>();
 
 	private List<ObjectValuePair> fuelTypes = new ArrayList<ObjectValuePair>();
 	
 	private List<ObjectValuePair> suppliers = new ArrayList<ObjectValuePair>();
 	
 	
 	private ObjectValuePair automatic;
 	private ObjectValuePair airCodition;
 	
 	private MoneyAmount minPrice;
 	
 	private MoneyAmount maxPrice;
 	
 	
 	public List<ObjectValuePair> getStationLocTypes() {
 		return stationLocTypes;
 	}
 	public void setStationLocTypes(List<ObjectValuePair> stationLocTypes) {
 		this.stationLocTypes = stationLocTypes;
 	}
 	public ObjectValuePair getAutomatic() {
 		return automatic;
 	}
 	public void setAutomatic(ObjectValuePair automatic) {
 		this.automatic = automatic;
 	}
 	public ObjectValuePair getAirCodition() {
 		return airCodition;
 	}
 	public void setAirCodition(ObjectValuePair airCodition) {
 		this.airCodition = airCodition;
 	}
 	public List<ObjectValuePair> getBodyStyles() {
 		return bodyStyles;
 	}
 	public void setBodyStyles(List<ObjectValuePair> bodyStyles) {
 		this.bodyStyles = bodyStyles;
 	}
 	public List<ObjectValuePair> getFuelTypes() {
 		return fuelTypes;
 	}
 	public void setFuelTypes(List<ObjectValuePair> fuelTypes) {
 		this.fuelTypes = fuelTypes;
 	}
 	public List<ObjectValuePair> getFuelPolicy() {
 		return fuelPolicy;
 	}
 	public void setFuelPolicy(List<ObjectValuePair> fuelPolicy) {
 		this.fuelPolicy = fuelPolicy;
 	}
 	public List<ObjectValuePair> getSuppliers() {
 		return suppliers;
 	}
 	public void setSuppliers(List<ObjectValuePair> suppliers) {
 		this.suppliers = suppliers;
 	}
 	public MoneyAmount getMinPrice() {
 		return minPrice;
 	}
 	public void setMinPrice(MoneyAmount minPrice) {
 		this.minPrice = minPrice;
 	}
 	public MoneyAmount getMaxPrice() {
 		return maxPrice;
 	}
 	public void setMaxPrice(MoneyAmount maxPrice) {
 		this.maxPrice = maxPrice;
 	}
 
 }
