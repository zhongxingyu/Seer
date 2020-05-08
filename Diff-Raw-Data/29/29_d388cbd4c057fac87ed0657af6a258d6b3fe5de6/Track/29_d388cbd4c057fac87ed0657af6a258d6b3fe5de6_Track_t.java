 // Track Module Main class 
 
 package trackmodel;
 
 import global.*;
 
 public class Track
 {
 	private double elevation;
 	private double grade;
 	private int speedLimit;
 	private int dispatchLimit;
 	private boolean occupied;
 	private boolean open;
 	private ID trackID;
 	private String trackInfo;
 	private TrackFault failure;
 	private int trafficLight;
 	private int blockLength;
 	protected Track A;
 	protected Track B;
 	protected boolean direction = true;
 	
 	//Track module constructor	
 	public Track(double iElevate, double iGrade, int spLimit, int blkLen, ID trkID)
 	{
 		elevation = iElevate;
 		grade = iGrade;
 		speedLimit = spLimit;
 		dispatchLimit = speedLimit;
 		occupied = false;
 		trackID = trkID;
		failure = TrackFault.NONE;
 		trafficLight = 0;
		blockLength = blkLen;
 		open = true;
 	}
 	
 	public void setFailure(TrackFault f) // set track to fail (set value to 1)
 	{
 		trackInfo = "info: track failed (" + f + ")";
 		failure = f;
 	}
 	
 	public void setDispatchLimit(int dLimit) // set dispatcher speed limit
 	{
 		trackInfo = "info: dispatcher limit set to: "+dLimit;
 		dispatchLimit = dLimit;
 	}
 	
 	public void setFix() // call this method to fix track
 	{
 		trackInfo = "info: track fixed";
		failure = TrackFault.NONE;
 	}
 	
 	public void setOccupied(boolean iOccupy, Track from) // set block to occupied
 	{
 		if(from.equals(B))
 		{
 			direction = true;
 		}
 		else
 		{
 			direction = false;
 		}
 		trackInfo = " info: track set to: "+iOccupy;
 		occupied = iOccupy;
 	}
 	
 	public void setOpen(boolean iOpen) // set track to open
 	{
 		trackInfo = "info: track set to: "+iOpen;
 		open = iOpen;
 	}
 	
 	public void setTrafficLight(int lightState) // set traffic light
 	{
 		trackInfo = "info: traffic light set to: "+lightState;
 		trafficLight = lightState;
 	}
         
 	public void setNext(Track t)
 	{
 		A = t;
 	}
 
 	public void setPrev(Track t)
 	{
 		B = t;
 	}
 	
 //---------------------------------------------------------------------------------------	
 	
 	public boolean isOccupied()
 	{
 		return occupied;
 	}
 	
 	public Track getNext()
 	{
		/*
 		if(direction)
 		{
 			return A;
 		}
 		else
 		{
 			return B;
 		}
		*/
		return (direction ? A : B);
 	}
         
 	public int getSpeedLimit() // returns track speed limit
 	{
 		trackInfo = "info: sent speed limit: "+speedLimit;
 		return speedLimit;
 	}
 
 	public double getElevation() // returns elevation
 	{
 		trackInfo = "info: sent elevation: "+elevation;
 		return elevation;
 	}
 	
 	public ID getID() // returns track ID
 	{
 		trackInfo = "info: sent track ID: "+trackID;
 		return trackID;
 	}
 	
 	public double getGrade() // returns grade
 	{
 		trackInfo = "info: sent grade: "+grade;
 		return grade;
 	}
 	
 	public String getTrackUpdate()
 	{
 		return trackInfo;
 	}
         
 	public int getBlockLength()
 	{
 		return blockLength;
 	}
 }
