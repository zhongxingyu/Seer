 package role;
 
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.SensorPort;
 import localization.LightLocalizer;
 import localization.USLocalizer;
 import bluetooth.StartCorner;
 import odometry.LCDInfo;
 import odometry.OdometryCorrection;
 import odometry.OdometryAngleCorrection;
<<<<<<< HEAD
=======

>>>>>>> 4fe5a82b2d7a09c9de15e09a9cc4bc93deab7b25
 
 /**
  * Specific role of defender
  * 
  * @author Team 13
  * 
  */
 public class Defender extends Robot {
 	private final double defensiveZoneX = 135;
 	private double defensiveZoneY;
 	private double defensiveLocalizationX, defensiveLocalizationY;
 
 	/**
 	 * Constructor - same as Robot constructor
 	 * 
 	 * @param catapultMotor
 	 * @param leftMotor
 	 * @param rightMotor
 	 * @param leftLightPort
 	 * @param centerLightPort
 	 * @param rightLightPort
 	 * @param USPort
 	 */
 	public Defender(NXTRegulatedMotor catapultMotor,
 			NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor,
 			SensorPort leftLightPort, SensorPort centerLightPort,
 			SensorPort rightLightPort, SensorPort USPort) {
 		super(catapultMotor, leftMotor, rightMotor, leftLightPort,
 				centerLightPort, rightLightPort, USPort);
 	}
 
 	@Override
 	public void play(StartCorner startingCorner, int bx, int by, int w1,
 			int w2, int d1, int goalX, int goalY) {
<<<<<<< HEAD
 		defensiveZoneY = goalY - (w2 * 30) - 15;
 		defensiveLocalizationX = goalX;
 		defensiveLocalizationY = defensiveZoneY - 15;
 		myCatapult.arm();
 		@SuppressWarnings("unused")
 		LCDInfo info = new LCDInfo(myOdometer, USSensor, leftSensor,
 				centerSensor, rightSensor);
 
 		localize(startingCorner);
 		postLocalize(startingCorner);
 		OdometryCorrection myOdometryCorrection = new OdometryCorrection(
 				myOdometer, centerSensor, leftMotor, rightMotor);
 		myOdometryCorrection.start();
 		myNav.navigateTo(defensiveZoneX, defensiveZoneY); // navigate
 															// to
 															// defensive
 															// zone
 		patrol();
=======
 		
 		myCatapult.arm(); // cannot take this out because of it will impair obstacle avoidance (USSensor)
 		LightLocalizer.doLocalization(myOdometer, myNav, centerSensor, leftMotor, rightMotor, startingCorner);
 		USLocalizer.doFallingEdgeLocalization(myOdometer, USSensor, myNav, leftMotor, rightMotor);
 		
 		switch (startingCorner) {
 		case BOTTOM_LEFT:
 			myOdometer.setPosition(new double [] {0.0, 0.0, 90.0}, new boolean [] {true, true, true});
 			myNav.travelTo2(15.0, 15.0);
 			myNav.turnTo(90.0, true);
 		case BOTTOM_RIGHT:
 			myOdometer.setPosition(new double [] {300.0, 0.0, 90.0}, new boolean [] {true, true, true});
 			myNav.travelTo2(285.0, 15.0);
 			myNav.turnTo(90.0, true);
 		case TOP_RIGHT:
 			myOdometer.setPosition(new double [] {300.0, 300.0, 270.0}, new boolean [] {true, true, true});
 			myNav.travelTo2(285.0, 285.0);
 			myNav.turnTo(270.0, true);
 		case TOP_LEFT:
 			myOdometer.setPosition(new double [] {0.0, 300.0, 270.0}, new boolean [] {true, true, true});
 			myNav.travelTo2(15.0, 285.0);
 			myNav.turnTo(270.0, true);
 		}
 		
		LCDInfo info = new LCDInfo(myOdometer, USSensor, leftSensor, centerSensor, rightSensor);
		//OdometryCorrection myOdometryCorrection = new OdometryCorrection(myOdometer, centerSensor, leftMotor, rightMotor);
		//myOdometryCorrection.start();
		
 		myNav.navigateTo((goalX * 30) - 15, goalY - ((w2 * 30) + 15)); //navigate to defensive zone
 		myNav.travelTo(goalX*30, goalY - ((w2 * 30))); //travel in front of the goal
 		
 		/* TESTING CORRECTION
 		LCDInfo info = new LCDInfo(myOdometer, USSensor, leftSensor, centerSensor, rightSensor);
 		OdometryCorrection myOdometryCorrection = new OdometryCorrection(myOdometer, centerSensor, leftMotor, rightMotor);
 		myOdometryCorrection.start();
 		myOdometer.setX(15.0);
 		myOdometer.setY(15.0);
 		myOdometer.setTheta(90.0);
 		myNav.navigateTo(75.0,75.0);
 		*/
>>>>>>> 4fe5a82b2d7a09c9de15e09a9cc4bc93deab7b25
 	}
 
 	private void patrol() {
 		while (true) {
 			for (int index = 0; index < 5; index++) {
 				myNav.navigateTo(195, defensiveZoneY);
 				myNav.navigateTo(105, defensiveZoneY);
 			}
 			myNav.navigateTo(defensiveZoneX, defensiveZoneY);
 			myNav.travelTo(defensiveZoneX - 15, defensiveZoneY - 15);
 			LightLocalizer.doLocalization(myOdometer, myNav, centerSensor,
 					leftMotor, rightMotor, defensiveLocalizationX,
 					defensiveLocalizationY);
 
 			// make sure the odometer has been set properly
 			myOdometer.setX(defensiveLocalizationX);
 			myOdometer.setY(defensiveLocalizationY);
 			myOdometer.setTheta(90.0);
 			
 
 		}
 
 	}
 }
