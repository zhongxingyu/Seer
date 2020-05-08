 // Track Module Main class 
 
 package trackmodel;
 
 import global.*;
 import java.util.logging.*;
 
 public class Track
 {
 	final static Logger log = Logger.getLogger(Track.class.getName());
 	
 	private double elevation;
 	private double grade;
 	private int speedLimit;
 	private int dispatchLimit;
 	private int authority;
 	private boolean occupied;
 	private boolean open;
 	private ID trackID;
 	private String trackInfo;
 	private TrackFault failure;
 	private Light trafficLight;
 	private double blockLength;
 	protected Track A;
 	protected Track B;
 	protected boolean direction = true;
 	
 	//Track module constructor	
 	public Track(double iElevate, double iGrade, int spLimit, double blkLen, ID trkID)
 	{
 		elevation = iElevate;
 		grade = iGrade;
 		speedLimit = spLimit;
 		dispatchLimit = speedLimit;
 		occupied = false;
 		trackID = trkID;
 		failure = TrackFault.NONE;
 		trafficLight = Light.NONE;
 		blockLength = blkLen;
 		open = true;
 		authority = 0;
 		log.config("Track " + trackID + ": Created");
 	}
 	
 	public void setFailure(TrackFault f) // set track to fail (set value to 1)
 	{
 		trackInfo = "info: track failed (" + f + ")";
 		failure = f;
 		log.info("Track " + trackID + ": " + trackInfo);
 	}
 	
 	public void setDispatchLimit(int dLimit) // set dispatcher speed limit
 	{
 		trackInfo = "info: dispatcher limit set to: " + dLimit;
 		dispatchLimit = dLimit;
 		log.info("Track " + trackID + ": " + trackInfo);
 	}
 	
 	public void setAuthority(int auth) // set authority
 	{
 		trackInfo = "info: authority set to: " + auth;
 		authority = auth;
 		log.info("Track " + trackID + ": " + trackInfo);
 	}
 	
 	public void setFix() // call this method to fix track
 	{
 		trackInfo = "info: track fixed";
 		failure = TrackFault.NONE;
 		log.info("Track " + trackID + ": " + trackInfo);
 	}
 	
 	public void setOccupied(Track from) // set block to occupied
 	{
		if(from.equals(A))
 		{
 			direction = false;
 		}
 		else
 		{
 			direction = true;
 		}
 		trackInfo = " info: track set to OCCUPIED";
 		occupied = true;
 		log.info("Track " + trackID + ": " + trackInfo);
 	}
 	
 	public void setUnoccupied()
 	{
 		trackInfo = " info: track set to UNOCCUPIED";
 		occupied = false;
 		log.info("Track " + trackID + ": " + trackInfo);
 	}
 	
 	public void setOpen(boolean iOpen) // set track to open
 	{
 		trackInfo = "info: track set to: " + iOpen;
 		open = iOpen;
 		log.info("Track " + trackID + ": " + trackInfo);
 	}
 	
 	public void setTrafficLight(Light lightState) // set traffic light
 	{
 		trackInfo = "info: traffic light set to: " + lightState;
 		trafficLight = lightState;
 		log.info("Track " + trackID + ": " + trackInfo);
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
         
 	public boolean isOpen()
 	{
 		return open;
 	}
         
 	public TrackFault isFailed()
 	{
 		return failure;
 	}
 	
 	public Track getNext()
 	{
 		return (direction ? A : B);
 	}
 	
 	public Track getNext(boolean dir)
 	{
 		direction = dir;
 		return getNext();
 	}
 	
 	public boolean getDirection()
 	{
 		return direction;
 	}
         
 	public int getInherentSpeedLimit() // returns track speed limit
 	{
 		trackInfo = "info: sent speed limit: " + speedLimit;
 		return speedLimit;
 	}
 	
 	public int getSpeedLimit()
 	{
 		return dispatchLimit;
 	}
         
 	public int getAuthority() // returns authority
 	{
 		trackInfo = "info: sent speed limit: " + authority;
 		return authority;
 	}
 
 	public double getElevation() // returns elevation
 	{
 		trackInfo = "info: sent elevation: " + elevation;
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
         
 	public double getBlockLength()
 	{
 		return blockLength;
 	}
         
         public Light getLightState()
         {
             return trafficLight;
         }
 }
