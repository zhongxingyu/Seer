 package T07;
 
 import lejos.nxt.*;
 import lejos.util.Delay;
 
 /** class that focuses on searcher for the flag and getting a flag from a known location, this depends on role
  * 
  * @author asimps12
  *
  */
 public class Searcher {
 	
 	private Odometer odometer;
 	private Navigation navigation;
 	private TwoWheeledRobot robot;
 	private LightPoller middleLight;
 	private USPoller middlePoller;
 	private CommunicationController communicationController;
 	private int startX;
 	private int startY;
 	private int[][] field; // will store field information for use by the searcher algorithm
 	private int lightBeaconThreshold = 50; // minimum light value that exits the searching algorithm and moves robot towards the light source (will be close)
 	private int lightBeaconMaxValue; // light value that is detected when in front of the beacon (will be calibrated)
 	private int desiredBeaconDistance = 20; // distance that is desired from beacon to grabbing arm
 	
 	//Constructor
 	public Searcher(Odometer odometer, Navigation navigation, LightPoller middleLight, USPoller middlePoller,
			CommunicationController communicationController, int startX, int startY) {
 		this.odometer = odometer;
 		this.navigation = navigation;
 		this.middleLight = middleLight;
 		this.startX = startX;
 		this.startY = startY;
 		this.middlePoller = middlePoller;
 		this.robot = odometer.getTwoWheeledRobot();
 		this.communicationController = communicationController;
 	}
 	
 	// method that deals with finding the beacon, will need a sophisticated searching algorithm, may also need a seperate method to get
 	// get a beacon at a known location
 	public void findBeacon() {
 		// TODO: Ashley has an idea for the algorithm to implement, will introduce this but not for the demo.
 		
 		navigation.travelTo(216.72, 216.72); // simple movement for the demo
 		
 		while (robot.motorsMoving()) {
 			if (middleLight.getRawValue() > lightBeaconThreshold) { // TODO decide on a value that allows the search algorithm to be exited
 				while (communicationController.getLightSensorValue() < lightBeaconMaxValue && middlePoller.getFilteredData() > desiredBeaconDistance) { 
 					// TODO: check these values with testing
 					// TODO: for now we just move towards the light, will be improved after the demo
 					// Following code is simply approaching the flag, nothing more, from Ashley's Lab 5 code, will not be used after the demo
 
 					if (!robot.leftMotorMoving() && !robot.rightMotorMoving()) {
 						searchLight();
 
 						robot.moveForwardDistance(30);
 					}	
 				}
 				
 				// stops the motors after reaching required distance, otherwise the robot will continue until the end of the rotate
 				robot.stopLeftMotor();
 				robot.stopRightMotor();
 				
 				// signals that the robot has performed its required operation
 				Sound.twoBeeps();
 				Delay.msDelay(5000);
 			}
 		}
 	}
 	
 	public void searchLight() {
 		
 		// all the values that allow the storage of data for light locating
 		int i = 0;
 		int previousMax = 0;
 		int maxLightValue = 0;
 		double storedTheta = 0;
 		int[] lightValues = new int[25]; // TODO: check that the array doesn't overflow
 		double[] thetaValue = new double[25];
 		
 		// makes the robot do a 360 and detect all the light values around it
 		
 		robot.rotate(360);
 		
 		// stores the light values around it and then puts the light values with the corresponding theta
 		while (robot.leftMotorMoving() && robot.rightMotorMoving()) {
 			lightValues[i] = middleLight.getRawValue();
 			thetaValue[i] = odometer.getTheta();
 			Delay.msDelay(500);
 			i = i + 1;
 		}
 		
 		// previousMax stores the highest light value, storedTheta stores the corresponding theta
 		for (int j=0; j<i; j++){
 			
 			maxLightValue = lightValues[j];
 			
 			if (maxLightValue >= previousMax) {
 				previousMax = maxLightValue;
 				storedTheta = thetaValue[j];
 			}
 		}
 		
 		// turns to the theta with the highest light reading
 		navigation.turnTo(storedTheta);
 	}
 }
