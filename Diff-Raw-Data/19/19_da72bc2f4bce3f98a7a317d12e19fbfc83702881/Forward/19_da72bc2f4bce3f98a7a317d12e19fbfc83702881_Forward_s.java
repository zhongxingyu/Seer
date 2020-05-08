 package role;
 
 import odometry.LCDInfo;
 import odometry.Odometer;
 import lejos.nxt.LCD;
 import lejos.nxt.LightSensor;
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.SensorPort;
 import lejos.nxt.UltrasonicSensor;
 import lejos.robotics.navigation.DifferentialPilot;
 import lejos.util.Delay;
 import localization.LightLocalizer;
 import localization.USLocalizer;
 import bluetooth.StartCorner;
 import odometry.OdometryCorrection;
 import odometry.OdometryAngleCorrection;
 
 /**
  * Specific role of forward
  * 
  * @author Team 13
  * 
  */
 public class Forward extends Robot {
 	static final double CENTER_TO_RAMP = 6.5, LOADING_DISTANCE = 5,
 			
 			DISTANCE_TO_WALL = 22;
 	private double loadingTileX = 0, loadingTileY = 0, preciseLoadingX = 0,
 			preciseLoadingY = 0, loadingHeading = 0;
 
 	private double loadingLocalizationX, loadingLocalizationY;// coordinates
 																// of
 																// the
 																// grid
 																// intersection
 																// where
 																// the
 																// robot
 																// will
 																// localize
 																// before
 																// loading
 																// balls
 	public double leftFiringX, leftFiringY, leftPosX, leftPosY, leftFiringAngle, rightFiringX, rightFiringY, rightPosX, rightPosY, rightFiringAngle;
 
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
 	public Forward(NXTRegulatedMotor catapultMotor,
 			NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor,
 			SensorPort leftLightPort, SensorPort centerLightPort,
 			SensorPort rightLightPort, SensorPort USPort) {
 		super(catapultMotor, leftMotor, rightMotor, leftLightPort,
 				centerLightPort, rightLightPort, USPort);
 	}
 
 	@Override
 	public void play(StartCorner startingCorner, int bx, int by, int w1,
 			int w2, int d1, int goalX, int goalY) {
 
 		myCatapult.arm();
 		@SuppressWarnings("unused")
 		LCDInfo info = new LCDInfo(myOdometer, USSensor, leftSensor, centerSensor, rightSensor);
		
<<<<<<< HEAD
 		//localize(startingCorner);
=======
		localize(startingCorner);
>>>>>>> 4fe5a82b2d7a09c9de15e09a9cc4bc93deab7b25
 		postLocalize(startingCorner);
 
 		computeLoadingCoordinates(bx, by);
 		computeLoadingLocalizationCoords(bx, by);
 		computeFiringCoordinates(d1, goalX, goalY);
 		
 		//OdometryCorrection myOdometryCorrection = new OdometryCorrection(myOdometer, centerSensor, leftMotor, rightMotor);
 		//myOdometryCorrection.start();
 		
 		/* BALL LOADING SEQUENCE */
 		
 		// navigate to loading tile
 		myNav.navigateTo(loadingTileX, loadingTileY);
 	
 		// localize
 		myNav.travelTo(loadingLocalizationX, loadingLocalizationY);
 
 		LightLocalizer.doLocalization(myOdometer, myNav, centerSensor,
 				leftMotor, rightMotor, loadingLocalizationX,
 				loadingLocalizationY);
 
 		// make sure the odometer has been set properly
 		myOdometer.setX(loadingLocalizationX);
 		myOdometer.setY(loadingLocalizationY);
 		myOdometer.setTheta(90.0);
 
 		myNav.travelTo(preciseLoadingX, preciseLoadingY);
 		myNav.turnTo(loadingHeading, true);
 		loadFiveBalls();
<<<<<<< HEAD
 
 		// localize again (pushing the button fucks it up)
 		myNav.travelTo(loadingLocalizationX, loadingLocalizationY);
 		
 		LightLocalizer.doLocalization(myOdometer, myNav, centerSensor, leftMotor, rightMotor, loadingLocalizationX, loadingLocalizationY);
 		
 		// make sure the odometer has been set properly
 		myOdometer.setX(loadingLocalizationX);
 		myOdometer.setY(loadingLocalizationY);
 		myOdometer.setTheta(90.0);
 		
 //		/* LEFT SIDE FIRING  SEQUENCE */
 //		
 //		// navigate to firing area
 //		myNav.navigateTo(leftFiringX, leftFiringY);
 //		myNav.travelTo(leftPosX, leftPosY);
 //		
 //		// localize before shooting
 //		LightLocalizer.doLocalization(myOdometer, myNav, centerSensor, leftMotor, rightMotor, leftPosX, leftPosY); 
 //		// make sure the odometer has been set properly
 //		myOdometer.setX(leftPosX);
 //		myOdometer.setY(leftPosY);
 //		myOdometer.setTheta(90.0);
 //		myNav.turnTo(leftFiringAngle, true);
 //		
 //		shootFiveBallsCenter();
 //		
 //		// localize after shooting
 //		LightLocalizer.doLocalization(myOdometer, myNav, centerSensor, leftMotor, rightMotor, leftPosX, leftPosY); 
 //		// make sure the odometer has been set properly
 //		myOdometer.setX(leftPosX);
 //		myOdometer.setY(leftPosY);
 //		myOdometer.setTheta(90.0);
 		
 		/* RIGHT SIDE FIRING  SEQUENCE */
 		
 		/*
 		// navigate to firing area
 		myNav.navigateTo(rightFiringX, rightFiringY);
 		myNav.travelTo(rightPosX, rightPosY);
 		
 		// localize before shooting
 		LightLocalizer.doLocalization(myOdometer, myNav, centerSensor, leftMotor, rightMotor, rightPosX, rightPosY); 
 		// make sure the odometer has been set properly
 		myOdometer.setX(rightPosX);
 		myOdometer.setY(rightPosY);
 		myOdometer.setTheta(90.0);
 		myNav.turnTo(rightFiringAngle, true);
 		
 		shootFiveBallsCenter();
 		
 		// localize after shooting
 		LightLocalizer.doLocalization(myOdometer, myNav, centerSensor, leftMotor, rightMotor, rightPosX, rightPosY); 
 		// make sure the odometer has been set properly
 		myOdometer.setX(rightPosX);
 		myOdometer.setY(rightPosY);
 		myOdometer.setTheta(90.0);
 		*/
	
=======
>>>>>>> 4fe5a82b2d7a09c9de15e09a9cc4bc93deab7b25
 
 		// localize again (pushing the button fucks it up)
 		myNav.travelTo(loadingLocalizationX, loadingLocalizationY);
 		
 		LightLocalizer.doLocalization(myOdometer, myNav, centerSensor, leftMotor, rightMotor, loadingLocalizationX, loadingLocalizationY);
 		
 		// make sure the odometer has been set properly
 		myOdometer.setX(loadingLocalizationX);
 		myOdometer.setY(loadingLocalizationY);
 		myOdometer.setTheta(90.0);
 		
 		/* LEFT SIDE FIRING  SEQUENCE */
 		
 		// navigate to firing area
 		myNav.navigateTo(leftFiringX, leftFiringY);
 		myNav.travelTo(leftPosX, leftPosY);
 		
 		// localize before shooting
 		LightLocalizer.doLocalization(myOdometer, myNav, centerSensor, leftMotor, rightMotor, leftPosX, leftPosY); 
 		// make sure the odometer has been set properly
 		myOdometer.setX(leftPosX);
 		myOdometer.setY(leftPosY);
 		myOdometer.setTheta(90.0);
 		myNav.turnTo(leftFiringAngle, true);
 		
 		shootFiveBallsCenter();
 		
 		// localize after shooting
 		LightLocalizer.doLocalization(myOdometer, myNav, centerSensor, leftMotor, rightMotor, leftPosX, leftPosY); 
 		// make sure the odometer has been set properly
 		myOdometer.setX(leftPosX);
 		myOdometer.setY(leftPosY);
 		myOdometer.setTheta(90.0);
 		
 		/* SECOND LOADING SEQUENCE */
 		
 		/* RIGHT SIDE FIRING  SEQUENCE */
 		
 		/*
 		// navigate to firing area
 		myNav.navigateTo(rightFiringX, rightFiringY);
 		myNav.travelTo(rightPosX, rightPosY);
 		
 		// localize before shooting
 		LightLocalizer.doLocalization(myOdometer, myNav, centerSensor, leftMotor, rightMotor, rightPosX, rightPosY); 
 		// make sure the odometer has been set properly
 		myOdometer.setX(rightPosX);
 		myOdometer.setY(rightPosY);
 		myOdometer.setTheta(90.0);
 		myNav.turnTo(rightFiringAngle, true);
 		
 		shootFiveBallsCenter();
 		
 		// localize after shooting
 		LightLocalizer.doLocalization(myOdometer, myNav, centerSensor, leftMotor, rightMotor, rightPosX, rightPosY); 
 		// make sure the odometer has been set properly
 		myOdometer.setX(rightPosX);
 		myOdometer.setY(rightPosY);
 		myOdometer.setTheta(90.0);
 		*/
 		
 		/* RETURN HOME */
 		returnHome(startingCorner);
 	
 	}
 
 	/**
 	 * Compute loading coordinates Need to change values if the arena changes
 	 * 
 	 * @param bx
 	 *            - x-coordinate of ball dispenser in tiles
 	 * @param by
 	 *            - y-coordinate of ball dispenser in tiles
 	 */
 	private void computeLoadingCoordinates(int bx, int by) {
 		if (bx == -1) { // western wall
 			preciseLoadingX = bx * 30 + DISTANCE_TO_WALL;
 			preciseLoadingY = by * 30 + CENTER_TO_RAMP;
 			loadingHeading = 180;
 
 			loadingTileX = -15;
 			loadingTileY = (by * 30) - 15;
 		}
 
 		if (by == -1) { // southern wall
 			preciseLoadingX = bx * 30 - CENTER_TO_RAMP;
 			preciseLoadingY = by * 30 + DISTANCE_TO_WALL;
 			loadingHeading = 270;
 
 			loadingTileY = -15;
 			loadingTileX = (bx * 30) - 15;
 		}
 
 		if (bx == 11) { // eastern wall
 			preciseLoadingX = bx * 30 - DISTANCE_TO_WALL;
 			preciseLoadingY = by * 30 - CENTER_TO_RAMP;
 
 			loadingTileX = 315;
 			loadingTileY = (by * 30) - 15;
 			loadingHeading = 0;
 		}
 	}
 
 	/**
 	 * Compute loading localization coordinates
 	 * 
 	 * @param bx
 	 *            - x-coordinate of ball dispenser in tiles
 	 * @param by
 	 *            - y-coordinate of ball dispenser in tiles
 	 */
 	private void computeLoadingLocalizationCoords(int bx, int by) {
 		if (bx < 0) { // west wall
 			loadingLocalizationX = 30;
 			loadingLocalizationY = by * 30;
 		}
 
 		if (bx == 11) { // east wall
 			loadingLocalizationX = 270;
 			loadingLocalizationY = by * 30;
 		}
 
 		if (by == -1) { // southern wall
 			loadingLocalizationY = 30;
 			loadingLocalizationX = bx * 30;
 		}
 	}
 
 	private void loadFiveBalls() {
 		DifferentialPilot myPilot = new DifferentialPilot(5.36, 5.36, 16.32,
 				leftMotor, rightMotor, false);
 		myPilot.setTravelSpeed(5);
 		myPilot.travel(4.5);
 		Delay.msDelay(40000);
 		myPilot.travel(-10);
 	}
 
 	private void shootFiveBallsCenter() {
 		for (int i = 0; i < 6; i++) {
 			myCatapult.carry();
 			myCatapult.shootCenter();
 		}
 	}
 	
 	private void computeFiringCoordinates(int d1, int goalX, int goalY) {
 		if(d1==8){
 			leftFiringX = 45;
 			leftFiringY = 45;
 			rightFiringX = 255;
 			rightFiringY = 45;
 		}
 		else if(d1==7){
 			leftFiringX = 45;
 			leftFiringY = 75;
 			rightFiringX = 255;
 			rightFiringY = 75;
 		}
 		else if(d1<7){
 			leftFiringX = 45;
 			leftFiringY = 105;
 			rightFiringX = 255;
 			rightFiringY = 105;
 		}
 		
 		leftPosX = leftFiringX+15;
 		leftPosY = leftFiringY-15;
 		rightPosX = rightFiringX-15;
 		rightPosY = rightFiringY-15;
 		
 		leftFiringAngle = (Math.atan2(goalY - leftPosY, goalX - leftPosX)) * (180.0 / Math.PI);
 		if (leftFiringAngle < 0){
 			leftFiringAngle += 360.0;
 		}
 		rightFiringAngle = (Math.atan2(goalY - rightPosY, goalX - rightPosX)) * (180.0 / Math.PI);
 		if (rightFiringAngle < 0){
 			rightFiringAngle += 360.0;
<<<<<<< HEAD
=======
 		}
 	}
 	
 	public void returnHome(StartCorner startingCorner){
 		if (startingCorner == StartCorner.BOTTOM_LEFT) {
 			myNav.navigateTo(15.0, 15.0);
 			myNav.turnTo(90.0, true);
 		} else if (startingCorner == StartCorner.BOTTOM_RIGHT){
 			myNav.navigateTo(285.0, 15.0);
 			myNav.turnTo(90.0, true);
 		}
 		else if (startingCorner == StartCorner.TOP_RIGHT) {
 			myNav.navigateTo(285.0, 285.0);
 			myNav.turnTo(270.0, true);
 		}
 		else if (startingCorner == StartCorner.TOP_LEFT) {
 			myNav.navigateTo(15.0, 285.0);
 			myNav.turnTo(270.0, true);
>>>>>>> 4fe5a82b2d7a09c9de15e09a9cc4bc93deab7b25
 		}
 	}
 }
