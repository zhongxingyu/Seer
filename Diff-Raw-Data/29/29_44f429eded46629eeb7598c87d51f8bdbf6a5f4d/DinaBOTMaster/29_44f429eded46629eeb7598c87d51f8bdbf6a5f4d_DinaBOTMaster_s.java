 package dinaBOT;
 
 import lejos.nxt.*;
 
 import java.lang.Math;
 
 import dinaBOT.comm.*;
 import dinaBOT.detection.*;
 import dinaBOT.mech.*;
 import dinaBOT.navigation.*;
 import dinaBOT.sensor.*;
 import dinaBOT.util.*;
 
 
 /**
  * The DinaBOTMaster is the main class the master brick. It <b>is</b> the robot. It contains the main() for the master.
  *
  * @author Alexandre Courtemanche, Francois Ouellet Delorme, Gabriel Olteanu, Severin Smith, Stepan Salenikovich, Vinh Phong Buu
 */
 public class DinaBOTMaster implements MechConstants, CommConstants, SearchPatterns {
 
 	/* -- Class Variables -- */
 
 	static final int CAGE_FULL = 1;
 
 	Motor left_motor = Motor.A;
 	Motor right_motor = Motor.B;
 
 	/* -- Instance Variables -- */
 
 	//Connection objects
 	BTMaster slave_connection;
 
 	//Basics movement objects
 	Odometer odometer;
 	Movement movement;
 
 	//High level navigation object
 	Map map;
 	Pathing pather;
 
 	Navigator navigator;
 
 	//Utilities
 	Localization localization;
 	BlockFinder blockFind;
 	DropOff dropper;
 
 	//Variables
 	int pallet_count;
 	boolean debug;
 
 	/**
 	 * This is the constructor for the DinaBOTMaster
 	 *
 	 * @param drop_x the x coordinate of the drop off (in tile units)
 	 * @param drop_y the y coordinate of the drop off (in tile units)
 	*/
 	public DinaBOTMaster(int drop_x, int drop_y) {
 		//Add a convenient quit button
 		Button.ESCAPE.addButtonListener(new ButtonListener() {
 			public void buttonPressed(Button b) {
 				System.exit(0);
 			}
 
 			public void buttonReleased(Button b) {
 				System.exit(0);
 			}
 		});
 
 		USSensor low = USSensor.low_sensor;
 		USSensor high = USSensor.high_sensor;
 
 		slave_connection = new BTMaster();
 
 		odometer = new ArcOdometer(left_motor, right_motor);
 		movement = new BasicMovement(odometer, left_motor, right_motor);
 
 		map = new Map(odometer, 9, 45, UNIT_TILE);
 		pather = new ManhattanPather(map, movement);
 
 		navigator = new Navigator(odometer, movement, map, pather);
 
 		localization = new Localization(odometer, movement);
 		blockFind = new BlockFinder(odometer, movement, map);
 		dropper = new DropOff(odometer, movement, slave_connection,localization, drop_x, drop_y);
 
 		debug = false;
 	}
 
 	public void connect() {
 		slave_connection.setDebug(true);
 		while(!slave_connection.connect());
 		slave_connection.setDebug(false);
 	}
 
 	/**
 	 * This is a heuristic method for block alignment using brick to brick communication (currently over bluetooth). It assumes the block is directly in front if the robot in an unknown orientation.
 	 *
 	*/
 	public void alignPallet() {
 		//Align contants (heuristically tuned)
 		double forward_distance = 5;
 		double rotate_angle = Math.PI/4;
 
 		//Alignement method
 		movement.goForward(forward_distance, SPEED_MED);
 
 		movement.turn(rotate_angle, SPEED_ROTATE);
 
 		movement.goForward(forward_distance, SPEED_MED);
 
 		slave_connection.request(HOLD);
 
 		movement.goForward(-forward_distance, SPEED_MED);
 
 		movement.turn(-rotate_angle, SPEED_ROTATE);
 
 		for(int i = 0;i < 2;i++) {
 			slave_connection.request(HOLD);
 			movement.goForward(-forward_distance, SPEED_MED);
 
 			slave_connection.request(RELEASE);
 
 			movement.goForward(forward_distance, SPEED_MED);
 		}
 	}
 
 	/**
 	 * Searches for a pellet in the immediate vicinity and picks it up if found (also increments pallet_count)
 	 *
 	*/
 	public void pickUpPallet() {
 		double[] initial_position = odometer.getPosition(); //Remember start position
 
 		odometer.enableSnapping(false); //Disable snapping for diagonal movement
 
 		slave_connection.request(RELEASE); //Put down claw
 
 		if(blockFind.sweep(odometer.getPosition()[2])) { //Perform sweep
 			if(debug) System.out.println("Picking up");
 
 			alignPallet(); //If successfull align pallet
 
 			map.stop(); //Temporarily disable map (stuff will pass in front of the sensor)
 			slave_connection.request(PICKUP); //Pickup
 			map.start(); //Reenable map
 
 			pallet_count++; //Increment pallet count
 
 			movement.goTo(initial_position[0], initial_position[1], SPEED_MED); //Return to start position
 			movement.turnTo(initial_position[2], SPEED_ROTATE);
 		} else {
 			map.stop(); //Temporarily disable map (stuff will pass in front of the sensor)
 			slave_connection.request(ARMS_UP); //Pickup
 			map.start(); //Reenable map
 	
 			movement.goTo(initial_position[0], initial_position[1], SPEED_MED); //Return to start position
 			movement.turnTo(initial_position[2], SPEED_ROTATE);
 		}
 
 		odometer.enableSnapping(true); //Renable snapping
 	}
 
 	/**
 	 * This method makes the robot move to the correct drop off point, depending on how it is positioned with respect to it.
 	 *
 	*/	
 	public void dropOff() {
 		
 		double[] start_position = odometer.getPosition();
		int[] dropCoords = dropper.getDropCoords();
 		debug = true;
 		int [] dropSetUpCoords = new int[2];
 		dropSetUpCoords[0] = constrain(roundToInt(start_position[0]), dropCoords[0]-1, dropCoords[0]+2);
 		dropSetUpCoords[1] = constrain(roundToInt(start_position[1]), dropCoords[1]-1, dropCoords[1]+2);
 		
		navigator.goTo((dropSetUpCoords[0] * UNIT_TILE, dropSetUpCoords[1] * UNIT_TILE, true);
 	}
 					   
 	/**
 	 * This is the "main" execution method of the robot. Here is the central thread of our program which should control everything.
 	 *
 	*/
 	public void run() {
 		//Setup
 		odometer.enableSnapping(true);
 		odometer.setDebug(false);
 		odometer.setPosition(new double[] {UNIT_TILE, UNIT_TILE, 0}, new boolean[] {true, true, true});
 
 		int[][] pattern = {
 			new int[] {7,7},
 			new int[] {1,1} // Go back to starting node
 		};

		//Getting drop off coordinates
 		//Assuming that the coordinate of the drop-off point is the bottom left node of the tile
		int[] dropCoords = dropper.getDropCoords();
 
 		//Consider the nodes around the drop off zone as obstacles.
 		map.editMap(dropCoords[0],dropCoords[1], 1);
 		map.editMap(dropCoords[0]+1,dropCoords[1], 1);
 		map.editMap(dropCoords[0],dropCoords[1]+1, 1);
 		map.editMap(dropCoords[0]+1,dropCoords[1]+1, 1);
 
 		if(debug) System.out.println("Starting...");
 
 		try {
 			Thread.sleep(1000);
 		} catch(Exception e) {
 
 		}
 
 		for(int i = 0;i < pattern.length;i++) { //For each node in search path
 
 			if(debug) System.out.println("Leg number: "+i);
 
 			int nav_status = navigator.goTo(pattern[i][0]*UNIT_TILE,pattern[i][1]*UNIT_TILE, false); //Start moving to node
 			while(nav_status > 0) { //Keep going till you get there
 				if(debug) System.out.println("Breaking for possible pellet");
 				pickUpPallet(); //Pick up pallet for interrupt
 				if(pallet_count == CAGE_FULL) {
 					if(debug) System.out.println("Run drop off...");
 					dropOff(); //This should return us to the same point
 					dropper.dropOff(1);
 				}
 				nav_status = navigator.goTo(pattern[i][0]*UNIT_TILE,pattern[i][1]*UNIT_TILE, false); //And keep moving to node
 			}
 
 			if(nav_status < 0) { //Make sure we exited sucess, not impossible path, this should trigger some sort of map reset
 				if(debug) System.out.println("Impossible Path .. ending");
 				return;
 			}
 		}
 
 		if(debug) System.out.println("Done");
 	}
 
 	/**
 	 * This is a testing method of coordinate correction. It moves in a cute little pattern
 	 *
 	*/
 	public void moveTest() {
 		odometer.setDebug(false);
 		odometer.setPosition(new double[] {3*UNIT_TILE/4,3*UNIT_TILE/4, Math.PI/2}, new boolean[] {true, true, true});
 		odometer.enableSnapping(true);
 		odometer.enableLateralSnapping(false);
 		
 		localization.localizeUS();
 		if(debug) System.out.println("US Done");
 
 		odometer.setDebug(false);
 		odometer.setPosition(new double[] {3*UNIT_TILE/4,3*UNIT_TILE/4, Math.PI/2}, new boolean[] {true, true, true});
 		odometer.enableSnapping(true);
 		odometer.enableLateralSnapping(false);
 		
 	
 		movement.goForward(UNIT_TILE, SPEED_SLOW);
 		movement.goForward(-UNIT_TILE, SPEED_SLOW);
 		movement.goForward(3*UNIT_TILE/4, SPEED_SLOW);
 		movement.turnTo(Math.PI/2, SPEED_ROTATE);
 		movement.goForward(-3*UNIT_TILE/4, SPEED_SLOW);
 		movement.turnTo(Math.PI/2, SPEED_ROTATE);
 		movement.goForward(UNIT_TILE/2, SPEED_SLOW);
 		movement.turnTo(Math.PI/2, SPEED_ROTATE);
 		movement.goForward(-3*UNIT_TILE/8, SPEED_SLOW);
 		movement.turnTo(Math.PI/2, SPEED_ROTATE);
 		movement.goForward(UNIT_TILE/4, SPEED_SLOW);
 		movement.turnTo(Math.PI/2, SPEED_ROTATE);
 		movement.goForward(-UNIT_TILE/4, SPEED_SLOW);
 		
 		movement.goTo(3*UNIT_TILE/4, UNIT_TILE, SPEED_SLOW);
 		
 		movement.turnTo(0, SPEED_ROTATE);
 		movement.goForward(UNIT_TILE, SPEED_SLOW);
 		movement.goForward(-UNIT_TILE, SPEED_SLOW);
 		movement.goForward(3*UNIT_TILE/4, SPEED_SLOW);
 		movement.turnTo(0, SPEED_ROTATE);
 		movement.goForward(-3*UNIT_TILE/4, SPEED_SLOW);
 		movement.turnTo(0, SPEED_ROTATE);
 		movement.goForward(UNIT_TILE/2, SPEED_SLOW);
 		movement.turnTo(0, SPEED_ROTATE);
 		movement.goForward(-3*UNIT_TILE/8, SPEED_SLOW);
 		movement.turnTo(0, SPEED_ROTATE);
 		movement.goForward(UNIT_TILE/4, SPEED_SLOW);
 		movement.turnTo(0, SPEED_ROTATE);
 		movement.goForward(-UNIT_TILE/4, SPEED_SLOW);
 		
 		movement.goTo(UNIT_TILE, UNIT_TILE, SPEED_SLOW);
 		odometer.enableLateralSnapping(true);
 	
 	}
 	
 	public void dropTest() {
 		odometer.setPosition(new double[] {UNIT_TILE*2,5,5.5 * UNIT_TILE, 0}, new boolean[] {true, true, true});
 		dropOff();
 		dropper.dropOff(1);
 
 	}
 
 	/**
 	 * This is where the static main method lies. This is where execution begins for the master brick
 	 *
 	 * @param args This is the command line args, this is irrelevant in the NXT
 	*/
 	public static void main(String[] args) {
 		//DO some drop off input stuff here
 
 		DinaBOTMaster dinaBOTmaster = new DinaBOTMaster(3, 3); //Instantiate the DinaBOT Master
 
 		//Run some tests
 		dinaBOTmaster.connect();
 		//dinaBOTmaster.run();
 		dinaBOTmaster.dropTest();
 		//dinaBOTmaster.moveTest();
 
 		while(true); //Never quit
 	}
 
 }
