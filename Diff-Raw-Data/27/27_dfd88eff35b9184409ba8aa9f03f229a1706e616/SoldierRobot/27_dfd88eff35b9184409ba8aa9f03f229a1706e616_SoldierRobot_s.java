 package base;
 
 import java.util.ArrayList;
 
 import battlecode.common.Clock;
 import battlecode.common.Direction;
 import battlecode.common.GameActionException;
 import battlecode.common.MapLocation;
 import battlecode.common.RobotController;
 import battlecode.common.RobotType;
 
 public class SoldierRobot extends BaseRobot {
 	
 	public Platoon platoon;
 	
 	public SoldierState soldierState;
 	
 	// For mining
 	private MapLocation miningCenter;
 	private int miningRadius;
 	private int miningRadiusSquared;
 	private int miningMaxRadius;
 	
 	public boolean unassigned = true;
 	public ChannelType assignedChannel;
 	public MapLocation goalLoc;
 	
 	public MapLocation currentLocation;
 
 	
 	
 	
 
 	
 	
 	public SoldierRobot(RobotController rc) throws GameActionException {
 		super(rc);
 		
 		ChannelType channel = EncampmentJobSystem.findJob();
 		if (channel != null) {
 			assignedChannel = channel;
 			unassigned = false;
 			goalLoc = EncampmentJobSystem.goalLoc;
 			System.out.println("channel: " + channel);
 			System.out.println("goalLocx: " + goalLoc.x);
 			System.out.println("goalLocy: " + goalLoc.y);
 			EncampmentJobSystem.updateJobTaken(channel);
 		}
 	}
 	
 //	public SoldierRobot(RobotController rc, Platoon platoon) {
 //		super(rc);
 //		this.platoon = platoon;
 //	}
 
 	@Override
 	public void run() {
 		try {
 			if (Clock.getRoundNum() < 2500) {
 				if (NavSystem.navMode == NavMode.NEUTRAL) {
					NavSystem.setupSmartNav(new MapLocation(30, 27));
 				}
 				NavSystem.followWaypoints();
 //				if (soldierState != SoldierState.MINING_IN_CIRCLE) {
 //					setupCircleMining(new MapLocation(15, 27), 10);
 //				}
 //				if (rc.isActive()) {
 //					mineInCircle();
 //				}
 				
 			}
 //			} else {
 ////				if (NavSystem.navMode == NavMode.NEUTRAL) {
 ////					NavSystem.setupSmartNav(rc.senseEnemyHQLocation());
 ////				}
 ////				NavSystem.followWaypoints();
 //				rc.suicide();
 //				
 //				NavSystem.goToLocation(new MapLocation(10, 10));
 //				if (rc.getLocation().x == 10 && rc.getLocation().y == 10) {
 //					rc.suicide();
 //				}
 			
 			currentLocation = rc.getLocation();
 			
 			if (unassigned && rc.isActive()) {
 				if (NavSystem.navMode == NavMode.NEUTRAL) {
 					NavSystem.setupSmartNav(new MapLocation(10, 10));
 				}
 				NavSystem.followWaypoints();
 			
 				if (rc.getLocation().x == 10 && rc.getLocation().y == 10) {
 					rc.suicide();
 				}
 			} else { // is assigned to an encampment job
 				captureCode();
 			}
 						
 			
 		} catch (Exception e) {
 			System.out.println("caught exception before it killed us:");
 			System.out.println(rc.getRobot().getID());
 			e.printStackTrace();
 		}
 	}
 	
 	public Platoon getPlatoon() {
 		return this.platoon;
 	}
 	
 	/**
 	 * Set up a center MapLocation for mining in a circle
 	 * @param center
 	 */
 	private void setupCircleMining(MapLocation center, int maxRadius) {
 		soldierState = SoldierState.MINING_IN_CIRCLE;
 		miningCenter = center;
 		miningMaxRadius = maxRadius;
 		miningRadius = Constants.INITIAL_MINING_RADIUS;
 		miningRadiusSquared = miningRadius * miningRadius;
 	}
 	
 	/**
 	 * This method tells the soldier to mine in a circle (as set up by setupCircleMining())
 	 * @return true if we can still mine, and false if the circle radius has exceeded the maxMiningRadius
 	 * @throws GameActionException
 	 */
 	private boolean mineInCircle() throws GameActionException {
 		if (rc.isActive()) {
 			if (minesDenselyPacked(miningCenter, miningRadiusSquared)) {
 				// mines are fairly dense, so expand the circle in which to mine
 				miningRadius += Constants.MINING_RADIUS_DELTA;
 				if (miningRadius > miningMaxRadius) {
 					return false;
 				}
 				miningRadiusSquared = miningRadius * miningRadius;
 			}
 			if (rc.getLocation().distanceSquaredTo(miningCenter) >= miningRadiusSquared) {
 				// If we're too far from the center, move closer
 				NavSystem.goToLocation(miningCenter);
 			} else if (rc.getLocation().distanceSquaredTo(miningCenter) <= Math.pow(miningRadius - Constants.MINING_CIRCLE_DR_TOLERANCE, 2)) {
 				// If we're too close to the center, move away
 				NavSystem.goDirectionAndDefuse(rc.getLocation().directionTo(miningCenter).opposite());
 			} else {
 				// Lay a mine if possible
 				if (rc.senseMine(rc.getLocation()) == null) {
 					rc.layMine();
 				}
 				// Walk around the circle
 				Direction dir = rc.getLocation().directionTo(miningCenter).rotateLeft().rotateLeft(); // move counterclockwise around circle
 				NavSystem.goDirectionAndDefuse(dir);
 			}
 		}
 		return true;
 	}
 	
 	private void captureCode() throws GameActionException {
 		if (!unassigned) { // if assigned to something
 			EncampmentJobSystem.updateJobTaken(assignedChannel);
 		}
 		if (rc.isActive()) {
 			if (rc.senseEncampmentSquare(currentLocation) && currentLocation.equals(goalLoc)) {
 				rc.captureEncampment(RobotType.GENERATOR);
 			} else {
 				if (NavSystem.navMode == NavMode.BFSMODE) {
 					NavSystem.tryBFSNextTurn();
 				} else if (NavSystem.navMode == NavMode.GETCLOSER){
 					NavSystem.tryMoveCloser();
 				} else if (rc.getLocation().distanceSquaredTo(goalLoc) <= 8) {
 					NavSystem.setupGetCloser(goalLoc);
 					NavSystem.tryMoveCloser();
 				} else {
 					NavSystem.goToLocation(goalLoc);
 //					if (NavSystem.navMode == NavMode.NEUTRAL){
 //						NavSystem.setupSmartNav(goalLoc);
 //						NavSystem.followWaypoints();
 //					} else {
 //						NavSystem.followWaypoints();
 //					}
 				}
 					
 			}
 			
 		}
 	}
 	/**
 	 * Given a center MapLocation and a radiusSquared, returns true if the circle is densely packed with allied mines.
 	 * @param center
 	 * @param radiusSquared
 	 * @return
 	 */
 	private boolean minesDenselyPacked(MapLocation center, int radiusSquared) {
 		return rc.senseMineLocations(center, radiusSquared, rc.getTeam()).length >= (int)(2 * radiusSquared);
 	}
 	
 	private static void print2Darray(int[][] array) {
 		for (int i=0; i<5; i++) {
 			System.out.println("Array:");
 			System.out.println(array[i][0] + " " + array[i][1] + array[i][2] + " " + array[i][3] + " " + array[i][4]);
 		}
 	}
 }
