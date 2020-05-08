 package com.api.model.cta.bus;
 
 import org.apache.commons.lang3.builder.ToStringBuilder;
 
 public class BusETA {
 	//TODO: ADD ERROR SUPPORT
 	private String timeStamp;
 	private String type; //new name?
 	private int numericStopId;
 	private String stopDescription;
 	private int numericVehicleId;
 	private int distanceToStopInFeet;
 	private int numericRouteId;
 	private String routeDirection;
 	private String routeDestination;
 	private String predictedArrival;
 	private int delayed;
	private String errorDescription;
 	
 	public BusETA() {
 		
 	}
 	
 	public String getTimeStamp() {
 		return timeStamp;
 	}
 	public void setTimeStamp(String timeStamp) {
 		this.timeStamp = timeStamp;
 	}
 	public String getType() {
 		return type;
 	}
 	public void setType(String type) {
 		this.type = type;
 	}
 	public int getNumericStopId() {
 		return numericStopId;
 	}
 	public void setNumericStopId(int numericStopId) {
 		this.numericStopId = numericStopId;
 	}
 	public String getStopDescription() {
 		return stopDescription;
 	}
 	public void setStopDescription(String stopDescription) {
 		this.stopDescription = stopDescription;
 	}
 	public int getNumericVehicleId() {
 		return numericVehicleId;
 	}
 	public void setNumericVehicleId(int numericVehicleId) {
 		this.numericVehicleId = numericVehicleId;
 	}
 	public int getDistanceToStopInFeet() {
 		return distanceToStopInFeet;
 	}
 	public void setDistanceToStopInFeet(int distanceToStopInFeet) {
 		this.distanceToStopInFeet = distanceToStopInFeet;
 	}
 	public int getNumericRouteId() {
 		return numericRouteId;
 	}
	
 	public void setNumericRouteId(int numericRouteId) {
 		this.numericRouteId = numericRouteId;
 	}
 	public String getRouteDirection() {
 		return routeDirection;
 	}
 	public void setRouteDirection(String routeDirection) {
 		this.routeDirection = routeDirection;
 	}
 	public String getRouteDestination() {
 		return routeDestination;
 	}
 	public void setRouteDestination(String routeDestination) {
 		this.routeDestination = routeDestination;
 	}
 	public String getPredictedArrival() {
 		return predictedArrival;
 	}
 	public void setPredictedArrival(String predictedArrival) {
 		this.predictedArrival = predictedArrival;
 	}
 	public int getDelayed() {
 		return delayed;
 	}
 	public void setDelayed(int delayed) {
 		this.delayed = delayed;
 	}
	public String getErrorDescription() {
		return this.errorDescription;
	}
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
 	
 	@Override
 	public String toString(){
 		return ToStringBuilder.reflectionToString(this);
 	}
 	
 
 	
 }
