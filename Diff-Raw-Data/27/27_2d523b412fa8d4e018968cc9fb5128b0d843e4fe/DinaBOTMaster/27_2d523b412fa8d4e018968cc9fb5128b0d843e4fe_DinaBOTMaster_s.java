 package dinaBOT;
 
 import lejos.nxt.*;
 
 import java.util.Random;
 import java.lang.Math;
 
 import dinaBOT.navigation.*;
 import dinaBOT.mech.*;
 import dinaBOT.comm.*;
 import dinaBOT.detection.*;
 
 /**
  * The DinaBOTMaster is the main class the master brick. It <b>is</b> the robot. It contains the main() for the master.
  *
  * @author Alexandre Courtemanche, Francois Ouellet Delorme, Gabriel Olteanu, Severin Smith, Stepan Salenikovich, Vinh Phong Buu
  */
 public class DinaBOTMaster implements MechConstants {
 	
 	/* -- Static Variables -- */
 	
 	Motor left_motor = Motor.A;
 	Motor right_motor = Motor.B;
 	
 	/* -- Instance Variables -- */
 	
 	Odometer odometer;
 	Movement movement;
 	
 	BTMaster slave_connection;
 	
 	/**
 	 * This is the contructor for the DinaBOT master
 	 *
 	 */
 	public DinaBOTMaster() {
 		odometer = new ArcOdometer(left_motor, right_motor);
 		movement = new BasicMovement(odometer, left_motor, right_motor);
 		slave_connection = new BTMaster();
 	}
 	
 	/**
 	 * This is a testing method for odometry, navigation and movement
 	 *
 	 */
 	public void moveTest() {
 		//Configure your odometry
 		odometer.setDebug(false);
 		odometer.setPosition(new double[] {UNIT_TILE, UNIT_TILE*7, 0}, new boolean[] {true, true, false});
 		odometer.enableSnapping(true);
 		
 		//Pause so the user can remove his hand from the robot
 		try {
 			Thread.sleep(1000);
 		} catch(Exception e) {
 		
 		}
 		
 		
 		while(true) {
 			int[] x = {1,2,3,4,5,6,7,8,9,10,11,11,11,11,11,11,11,10,9,8,7,6,5,5,5,5,5,4,3,2,1,1,1};
 			int[] y = {7,7,7,7,7,7,7,7,7,7,7,6,5,4,3,2,1,1,1,1,1,1,1,2,3,4,5,5,5,5,5,6,7};
 			
 			for(int i = 0;i < x.length;i++) {
 				movement.goTo(x[i]*UNIT_TILE, y[i]*UNIT_TILE, 150);
 			}
 		}
 	}
 		
 	/**
 	 * This is demo method for our professor meeting on November 18th. It will ask for the input offsets for how much it will displace the pellet when 
 	 * it picks it up. It then does a 360 sweep around it to find the styrofoam pellet. When it finds it picks it up and displaces it for a certain offset 
 	 * value.
 	 */	
 	public void profDemo() {
 		
 		double newX=0, newY=0;
 		int TURN_SPEED = 50;
 		int TAP_DISTANCE = 10;
 		double[] foundBlockPos;
 		double SWEEP_OFFSET = Math.PI/2;
 		boolean foundBlock = false;
 		int offsetX = 0, offsetY = 0;
 		boolean enterPressed = false;
 		
 		BlockFinder blockFind = new BlockFinder(odometer, movement);
 		
 		LCD.clear();
 		//Keep trying to connect
 		while(!slave_connection.connect());
 		
 		LCD.clear();
 		LCD.drawString(offsetX + "   " + offsetY, 0,0);
 		
 		while( !enterPressed ) {
 			int buttonID = Button.waitForPress();
 			switch (buttonID) {
 				case Button.ID_LEFT:
 					offsetX--;
 					LCD.clear();
 					LCD.drawString(offsetX + "   " + offsetY, 0,0);
 					break;
 				case Button.ID_RIGHT:
 					offsetX++;
 					LCD.clear();
 					LCD.drawString(offsetX + "   " + offsetY, 0,0);
 					break;
 				case Button.ID_ENTER:
 					enterPressed = true;
 					break;
 			}
 		}
 		
 		try {
 			LCD.clear();
 			LCD.drawString("Loading...", 0,0);
 			Thread.sleep(1000);
 		}
 		catch (Exception e) {
 			LCD.clear();
 			LCD.drawString("Error sleeping.", 0, 0);
 		}
 		
 		LCD.clear();
 		LCD.drawString(offsetX + "   " + offsetY,0,0 );
 		
 		enterPressed = false;
 		
 		while( !enterPressed ) {
 			int buttonID = Button.waitForPress();
 			switch (buttonID) {
 				case Button.ID_LEFT:
 					offsetY--;
 					LCD.clear();
 					LCD.drawString(offsetX + "   " + offsetY,0,0 );
 					break;
 				case Button.ID_RIGHT:
 					offsetY++;
 					LCD.clear();
 					LCD.drawString(offsetX + "   " + offsetY, 0,0 );
 					break;
 				case Button.ID_ENTER:
 					enterPressed = true;
 					break;					
 			}
 		}
 		
 		try {
 			Thread.sleep(2000);
 			LCD.drawString("Loading main program...", 0,0);
 		}
 		catch (Exception e) {
 			LCD.clear();
 			LCD.drawString("Error sleeping.", 0, 0);
 		}
 		
 		//Continuously sweep for block
 		while (!foundBlock) {
 			foundBlock = blockFind.sweep(odometer.getPosition()[2]);
 			if(!foundBlock) {
 				movement.turnTo(odometer.getPosition()[2]+ SWEEP_OFFSET, TURN_SPEED);	
 			}
 		}
 		
 		//Once pellet is found
 		LCD.clear();
 		LCD.drawString("Pellet found!",0,0);
 		
 		/* Here is where the position of the pellet and the future position of the pellet are calculated
 		 */
 		
 		/*slave_connection.requestTap();
 		
 		movement.rotate(true, 100);
 
 		movement.goForward(MechConstants.BLOCK_DISTANCE, 150);
 		
 		slave_connection.requestTap();
 
 		slave_connection.requestPickup();*/
 		
 		tapTest();
 		
 		double [] startOdo = {0,0,0};
 		boolean [] boolOdo = {true,true,true};
 
 		odometer.setPosition(startOdo, boolOdo);
 		
 		Button.waitForPress();
 
 		movement.goTo((double)offsetX, (double)offsetY, 75);				
 		
 		slave_connection.openCage();
 		
 		movement.goForward(30, 75);
 		
 		slave_connection.closeCage();
 		
 		/*
 		newX = (( TAP_DISTANCE + TAP_DISTANCE + MechConstants.BLOCK_DISTANCE) * Math.sin(blockAngle)) + offsetX;
 		newY = (( TAP_DISTANCE + TAP_DISTANCE + MechConstants.BLOCK_DISTANCE) * Math.cos(blockAngle)) + offsetY;
 		
 		double newDistance = Math.sqrt( ( newX * newX ) + (newY * newY) );
 		
 		slave_connection.requestTap();
 		
 		movement.goForward(TAP_DISTANCE, 150);
 		
 		slave_connection.requestPickup();
 		
 		movement.turnTo( Math.atan(newX/newY), TURN_SPEED );
 		
 		movement.goForward(newDistance, 50);
 		
 		slave_connection.openCage();*/
 	
 	}
 	
 	/**
 	 * This is a testing method for block alignment using brick to brick communication (currently over bluetooth).
 	 *
 	 */
 	public void tapTest(){
 		
		slave_connection.connect();
 		
		movement.goForward(10, 200);
		movement.turn(75, 70);
		movement.turn(-150, 70);
		//movement.turn(75, 70);
 		if(slave_connection.requestTouch()) {
 			LCD.clear();
 			LCD.drawString("Success ...", 0, 0);
		}
 		
 		if(slave_connection.requestUntouch()) {
 			LCD.clear();
 			LCD.drawString("Success ...", 0, 0);
 		}
 		
		movement.goForward(4, 200);
 		if(slave_connection.requestTouch()) {
 			LCD.clear();
 			LCD.drawString("Success ...", 0, 0);
 		}
		
		movement.goForward(2, 200);
		if(slave_connection.requestPickup()) {
 			LCD.clear();
 			LCD.drawString("Success ...", 0, 0);
 		}
 		
 	}
 	
 	/**
 	 * This is a testing method for brick to brick communication (currently over bluetooth).
 	 *
 	 */
 	public void pickupTest() {
 		
 		try {
 			Thread.sleep(1000);
 		} catch(Exception e) {
 			
 		}
 		LCD.clear();
 		LCD.drawString("Trying to pickup ...", 0, 0);
 		if(slave_connection.requestPickup()) {
 			LCD.clear();
 			LCD.drawString("Success ...", 0, 0);
 		}
 		
 	}
 	
 	public void openAndCloseConnectTest() {
 		
 		try {
 			Thread.sleep(1000);
 		} catch(Exception e) {
 		}
 		
 		slave_connection.connect();
 
 		movement.goForward(40,150);
 		pickupTest();
 		//Button.waitForPress();
 		
 		slave_connection.disconnect();
 				
 		//Button.waitForPress();
 		movement.goForward(-40,150);
 		slave_connection.connect();
 		
 		slave_connection.openCage();
 		
 		movement.goForward(40,150);
 		
 		slave_connection.closeCage();
 	}
 	
 	public void goFetch(int distance) {
 		try {
 			Thread.sleep(1000);
 		} catch(Exception e) {
 		}
 		
 		slave_connection.connect();
 		
 		movement.goForward(distance,150);
 		
 		pickupTest();		
 		
 		movement.goForward(-distance,150);
 		
 		slave_connection.openCage();
 		
 		movement.goForward(distance,150);
 		
 		slave_connection.closeCage();
 	
 	
 	}
 	
 	// stepan's pathing and mapping test  WORKS!!
 	public void pathTest() {
 		Map mapper = new Map(odometer, 12, 45, UNIT_TILE);
 		Pathing pather = new ManhattanPather(mapper, movement);
 		double[][] path;
 		double[] position;
 				
 		odometer.enableSnapping(true);
 		odometer.setDebug(true);
 		
 		//Pause so the user can remove his hand from the robot
 		try {
 			Thread.sleep(1000);
 		} catch(Exception e) {
 		
 		}
 		
 		//position = odometer.getPosition();
 		
 		//obstacles
 		/*
 		mapper.editMap(2, 0, 2);
 		mapper.editMap(2, 1, 2);
 		mapper.editMap(3, 0, 2);
 		mapper.editMap(3, 1, 2);
 		
 		path = pather.generatePath(0.0, 0.0, 0, 3*UNIT_TILE, 3*UNIT_TILE);
 		
 		if( path != null) {
 			for(int i = 0; i < path.length; i++) {
 				movement.goTo(path[i][0], path[i][1], 150);
 			}
 		}
 		* */
 		
 		// go to 6,0
 		double[] end = new double[] {5*UNIT_TILE, 5*UNIT_TILE};
 		
 		position = odometer.getPosition();
 		
 		path = pather.generatePath( position[0], position[1], position[2], end[0], end[1]);
 		
 		if( path != null) {
 			for(int i = 0; i < path.length; i++) {
 				if( mapper.obstacleCheck() ) {
 					position = odometer.getPosition();
 					path = pather.generatePath( position[0], position[1], position[2], end[0], end[1]);
 					i = 0;
 				}
 				movement.goTo(path[i][0], path[i][1], 150);
 			}
 		}
 		
 		// go back to 0,0
 		end =  new double[] {0*UNIT_TILE, 0*UNIT_TILE};
 		
 		position = odometer.getPosition();
 		
 		path = pather.generatePath( position[0], position[1], position[2], end[0], end[1]);
 		
 		if( path != null) {
 			for(int i = 0; i < path.length; i++) {
 				if( mapper.obstacleCheck() ) {
 					position = odometer.getPosition();
 					path = pather.generatePath( position[0], position[1], position[2], end[0], end[1]);
 					i = 0;
 				}
 				movement.goTo(path[i][0], path[i][1], 150);
 			}
 		}	
 	}
 		
 	/**
 	 * This is where the static main method lies. This is where execution begins for the master brick
 	 *
 	 * @param args This is the command line args, this is irrelevent in the NXT
 	 */
 	public static void main(String[] args) {
 		//Add a convenient quit button
 		Button.ESCAPE.addButtonListener(new ButtonListener() {
 			public void buttonPressed(Button b) {
 			System.exit(0);
 			} 
 
 			public void buttonReleased(Button b) {
 			System.exit(0);
 			}
 		});
 
 		DinaBOTMaster dinaBOTmaster = new DinaBOTMaster(); //Instantiate the DinaBOT Master
 		//Run some tests
 	//	dinaBOTmaster.goFetch(40);
 		//dinaBOTmaster.tapTest();
 		dinaBOTmaster.profDemo();
 		
 		while(true); //Never quit
 	}
 	
 }
