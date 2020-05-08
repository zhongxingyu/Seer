 package com.api.model;
 
 public class TrainETA {
 	public int numericStationId;
 	public int numericStopId;
 	public String stationName;
 	public String stopDescription;
 	public int runNumber;
 	public String routeAbbr;
 	public String destinationStop;
 	public String trainDirection;
 	public String predictionGeneratedTimeStamp;
 	public String predictionArrivalTimeStamp;
 	public boolean approaching;
 	public boolean schedule;
 	public boolean faultDetected;
 	public boolean delayed;
 	public TrainETA(){
 		
 	}
 	public int getNumericStationId(){
 		return numericStationId;
 	}
 	public int getNumericStopId(){
 		return numericStopId;
 	}
 	public String getStationName(){
 		return stationName;
 	}
 	public String getStopDescription(){
 		return stopDescription;
 	}
 	public int getRunNumber(){
 		return runNumber;
 	}
 	public String getRouteAbbr(){
 		return routeAbbr;
 	}
 	public String getDestinationStop(){
 		return destinationStop;
 	}
 	public String getPredictionGeneratedTimeStamp(){
 		return predictionGeneratedTimeStamp;
 	}
 	public String getPredictionArrivialTimeStamp(){
		return predictionArrivalTimeStamp;
 	}
 }
