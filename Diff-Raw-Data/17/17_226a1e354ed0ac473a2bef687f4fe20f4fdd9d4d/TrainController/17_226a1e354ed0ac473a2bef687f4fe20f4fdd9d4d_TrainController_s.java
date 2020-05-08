 package traincontroller;
 
 import global.*;
 import trainmodel.*;
 import java.util.logging.*;
 import trackmodel.*;
 
 
 public class TrainController implements Runnable
 { 
     private Train train;
 	 private Track track; 
     private GreenSchedule G;
     private RedSchedule R;
     private String id;
     private boolean stopped;
     private boolean onSchedule;
     private boolean emergencyBraking;
     private boolean serviceBrake; 
     private boolean emergencyBrake;       //velocity - used in calcPower 
     private double setPoint;
     private double currentSpeed;          //Speed returned by Train Model 
     private double operatorSpeed;         //Speed entered by user 
     private double waysideSpeed;          //Speed sent by the wayside 
    private static double maxPower = 120000; //W
     private double power;                 //power sent to the  train
    private double init_power = 12000;       //W
     private double velocity;              //Trains current velocity 
     private double prev_d;                //previous d value
     private double prev_v;                //previous v value 
     private double d;                     //setPoint - currentSpeed
     private double v; 
     private double authority;    
     private static int clockRate;
     private Line scheduleLine;
     private int crew;
     private int ran = 1; 
     private String message;
 	 private int timewaited; 
 	 private double clockticValue;
 	
 	
     public TrainController(Line line, int crewCount, int clock, String idVal) 
     {
         scheduleLine = line;
         clockRate = clock;
         crew = crewCount;
         id = idVal;
         emergencyBrake = false; 
         train = new Train(scheduleLine, crew, id);
         message = "";
         switch (line)
         {
 			case GREEN:
 				G = new GreenSchedule(this);
 				break;
 			case RED:
 				R = new RedSchedule();
 				break;
 		}
     }
 		
     public void run() 
     {
         updateTrain();
         // updateSchedule();
     }
     
     public Train getTrain() 
     {
         return train;
     }
     
     public String getID()
     {
         return id;
     }
     
     public String getMessage() //coming to the next station - arriving departing 
     {
         return message;
     }
         
     public void setBraking(boolean b) //emergency brake
     {
         emergencyBrake = b;
     }
     
     public boolean getBraking()
     {
         return emergencyBrake;
     }
 
     public double calcPower(double currentSpeed, double operatorSpeed) 
     {
     		
     	//if authority is less than 1 - stop in that block 
         double trainPeriod = 0; //Sample period of train model = 60/clockrate/1000 
         double Kp = 2;          //proportional gain (Guess value)
         double Ki = 1;          //integral (Guess value)
 		
 		  prev_v = train.getVelocity(); 
 		  waysideSpeed = train.getSpeedLimit(); 
 		  authority = train.getAuthority(); 
 		  
 		power = 0;
 		
 		if (ran == 1) 
 		{
			power = init_power;
 			ran++;
 			return power;
 		}
         
 		if(authority < 1)
 		{
 			train.setServiceBrake(true); 
 			return 0;
 		}
 		
 		if(emergencyBraking == true) 
 		{
 			train.setEmergencyBrake(true);
 			return 0;
 		}   
         
         /*
         if (scheduleLine.equals(Line.GREEN)) 
         {
         		onSchedule = G.onSchedule();
         } 
         else if (scheduleLine.equals(Line.RED)) 
         {
         		onSchedule = R.onSchedule();
         }
 		*/
         
         onSchedule = true;
         if (onSchedule == false) //means off schedule 
         {
             setPoint = waysideSpeed;
         } 
         else 
         {
             setPoint = (waysideSpeed < operatorSpeed) ? waysideSpeed : operatorSpeed;
         }
 	
 		if(currentSpeed > setPoint)
 		{
 			train.setServiceBrake(true);
 			return 0;
 		}
 		else
 		{
 			/*power = Kp * d + Ki * v;
			if (power < maxPower) 
 			{
 				trainPeriod = (60 / clockRate) * .001; 
 				d = setPoint - currentSpeed;
 				v = prev_v + (trainPeriod / 2) * (d + prev_d);
 			}
			else if (power >= maxPower) 
 			{
				power = maxPower
 				// v = prev_v; //v= v(n-1)
 			}*/
 			train.setServiceBrake(false);
 			return maxPower;
 		}
 		
     }
 
     /*
      * Update method sends all updates to the train
      */
     public void updateTrain() 
     {
         //System.out.println("Inside TrainControllers Update Method.");
 		Transponder.Type info;
 		currentSpeed = train.getVelocity();
 		power = calcPower(currentSpeed, operatorSpeed);  
 		train.setPower(power); 
 		train.updateTrack(Math.floor(60/clockRate)*.001);
 		
 		
 		return;/*
         if (train.hasTransponder()) 
         {
             info = train.getTransponderInfo();
             if (info == Transponder.Type.UNDERGROUND) 
             {
                 train.setHeadLights();
             }
             if (info == Transponder.Type.STATION && trainStopped()) 
             {  
 				clockticValue = (60 / clockRate) * .001; 
 				timewaited += clockticValue; 
 				
 				train.openDoors();
 				if( timewaited >= 60 ) 
 				{   //After clock rate of 60secs close doors 
 					train.closeDoors();
 					timewaited = 0; 
 				}
 				//Signal that train goes to next station 
             }
         }*/
     }
 
     public void updateSchedule() 
     {
         scheduleLine = train.getLine();
         
         if (scheduleLine.equals(Line.GREEN)) 
         {
             G.greenSchedule();
         } 
         else if (scheduleLine.equals(Line.RED)) 
         {
             R.redSchedule();
         }
     }
 
     public boolean trainStopped() 
     {
         velocity = train.getVelocity();
         if (velocity == 0) {
             stopped = true;
         } 
         else 
         {
             stopped = false;
         }
         return stopped;
     }
 
     public double getClockRate() 
     {
         return clockRate;
     }
 
     public void setClockRate(int clock) 
     {
         clockRate = clock;
     }
 	
 		
 	public void setOperatorSpeed(double opSpeed) 
 	{
 		operatorSpeed = opSpeed; 
 	}
 
     
     public void remove() //send train to the train yard - modify schedule to go to the next station but not anywhere else 
     {
 /*		set some type of remove flag whenever flags set dont accept passengers 
 		continues on the every station */
 		//setPassengers 
         //Sending train to the train yard 
     }
 	
 	//Getting authority getAuthority
 	//Gettting speedLimit for the track  -- Setpoint can't be bigger than the current speedLimit for the track getSpeedLimit 
 }
