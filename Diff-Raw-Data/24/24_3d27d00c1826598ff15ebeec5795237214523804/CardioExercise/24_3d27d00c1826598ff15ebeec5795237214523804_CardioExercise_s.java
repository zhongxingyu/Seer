 package com.example.gomotion;
 import java.util.*;
 
 import android.location.Location;
 
 //Class to store running/walking/cycling sessions
 public class CardioExercise extends Exercise
 {
 	//Enumerates the types or session
     public enum CardioType{WALK, RUN, CYCLE};
 
     //Trip specific variables
     private int timeLength;
     private CardioType type;
     private double distance;    
     private LinkedList<Location> waypoints;
 
     public CardioExercise()
     {
     	
     }
     
     public CardioExercise(int timeLength, CardioType type, double distance, LinkedList<Location> waypoints)
     {
     	this.timeLength = timeLength;
     	this.type = type;
     	this.distance = distance;
     	this.waypoints = waypoints;
     }
 
	public CardioExercise(int ID, long timeStamp, long distance, int timeLength, CardioType type)
 	{
 		this.ID = ID;
 		this.timeStamp = timeStamp;
 		
 		this.timeLength = timeLength;
     	this.type = type;
     	this.distance = distance;
 	}
 
 	public int getTimeLength()
 	{
 		return timeLength;
 	}
 
 	public void setTimeLength(int timeLength)
 	{
 		this.timeLength = timeLength;
 	}
 
 	public CardioType getType()
 	{
 		return type;
 	}
 
 	public void setType(CardioType type)
 	{
 		this.type = type;
 	}
 
 	public double getDistance()
 	{
 		return distance;
 	}
 
 	public void setDistance(double distance)
 	{
 		this.distance = distance;
 	}
 
 	public LinkedList<Location> getWaypoints()
 	{
 		return waypoints;
 	}
 
 	public void setWaypoints(LinkedList<Location> waypoints)
 	{
 		this.waypoints = waypoints;
 	}
 
 	@Override
 	protected boolean saveToDatabase()
 	{
 		return Main.user.getDatabase().add(this);
 	}
     
     
     
 //    //Method to add a waypoint to wayPoints, gotten from android.location
 //    static private LocationManager locationManager;
 //    public void poll()
 //    {
 //    	if(locationManager == null)
 //    		locationManager =  (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 //    }
 //    
 //    //TODO: Getters and setters
 }
