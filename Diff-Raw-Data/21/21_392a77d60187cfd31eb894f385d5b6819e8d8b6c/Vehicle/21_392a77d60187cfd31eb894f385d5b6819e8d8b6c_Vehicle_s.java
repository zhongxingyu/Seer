 package vehicle;
 
 import java.io.Serializable;
 
 public class Vehicle implements Serializable {
 
 	
 	
 	/**** Parameres used in simulation ****/
 	private long startTimeInSystem;
 	private long startTimeInLane;
 	
 	/**** Physical constant parameters ****/
 	private double distanceBetweenCrosses = 200; 	//[m]
 	private double acceleration = 1.5;				//[m/s^2]		(Corresponding to about 0-100 on 19 sec)
 	private double maxSpeed = 13;					//[m/s]			(Corresponding to about 50 km/h)		
 	
 	/**** Physical changing parameters ****/
 	private double currentSpeed = 0;
 	private double currentDistance = 0;
 	private boolean inQue = false;
 	private long lastUpdatedeTime = 0;
 
 	
 	public Vehicle() 
 	{
 		startTimeInSystem = System.currentTimeMillis();
 		lastUpdatedeTime = startTimeInSystem;
 		startTimeInLane = startTimeInSystem;
 		
 	}
 	
 	public void putVehicleInQue()
 	{
 		updateVehicleDistanceAndSpeed();
 		if(currentDistance < distanceBetweenCrosses)
 			inQue = true;
 	}
 	
 	public boolean transferVehicle()
 	{
 		updateVehicleDistanceAndSpeed();
 		if(currentDistance < distanceBetweenCrosses)
 			return false;
 		
 		if(inQue)
 			currentSpeed = 0;
 		
 		currentDistance = 0;
 		inQue = false;
 		lastUpdatedeTime = System.currentTimeMillis();
 		startTimeInLane = lastUpdatedeTime;
 		
 		return true;
 	}
 	
 	
 	private void updateVehicleDistanceAndSpeed()
 	{
 		long currentTime = System.currentTimeMillis();
 		double timeDifferenec = (double) (currentTime-lastUpdatedeTime)/1000;
 		lastUpdatedeTime = currentTime;
 		
 		double timeToReadMaxSpeed = (maxSpeed - currentSpeed)/acceleration;
 
 		if(currentSpeed == maxSpeed)																			//Car is already at max speed
 			currentDistance += maxSpeed * timeDifferenec;
 		else if(timeDifferenec < timeToReadMaxSpeed)															//Car has not and will not reached max speed
 		{
 			currentDistance += currentSpeed * timeDifferenec + 1/2*acceleration*timeDifferenec*timeDifferenec;
 			currentSpeed += acceleration*timeDifferenec;
 		}
 		else																									//Car has not but will reach max speed
 		{
 			double timeLeft = timeDifferenec - timeToReadMaxSpeed;
 			currentDistance += currentSpeed * timeToReadMaxSpeed + 1/2*acceleration*timeToReadMaxSpeed*timeToReadMaxSpeed;
 			currentDistance += maxSpeed * timeLeft;
 			currentSpeed = maxSpeed;
 		}		
 	}
 	
 	public long getWaitTime() 
 	{
 		updateVehicleDistanceAndSpeed();
 		return lastUpdatedeTime - startTimeInLane;
 	}
 }
