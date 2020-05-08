 package team162;
 
 import battlecode.common.Clock;
 import battlecode.common.Direction;
 import battlecode.common.GameActionException;
 import battlecode.common.MapLocation;
 import battlecode.common.Robot;
 import battlecode.common.RobotController;
 import battlecode.common.RobotInfo;
 import battlecode.common.RobotType;
 import battlecode.common.Team;
 
 public class SoldierRobot extends BaseRobot {
 	
 	public Platoon platoon;
 	
 	public SoldierState soldierState = SoldierState.NEW;
 	
 	// For mining
 	private MapLocation miningCenter;
 	private int miningRadius;
 	private int miningRadiusSquared;
 	private int miningMaxRadius;
 	
 	public boolean unassigned = true;
 	
 	public MapLocation currentLocation;
 	
 	public MapLocation HQLocation;
 	public MapLocation EnemyHQLocation;
 	
 	public MapLocation rallyPoint;
 	
 	public ChannelType powerChannel = ChannelType.HQPOWERLEVEL;
 	
 	public SoldierRobot(RobotController rc) throws GameActionException {
 		super(rc);
 		
 		NavSystem.init(this);
 		
 		HQLocation = rc.senseHQLocation();
 		EnemyHQLocation = rc.senseEnemyHQLocation();
 
 		rallyPoint = findRallyPoint();
 		
 		ChannelType channel = EncampmentJobSystem.findJob();
 		if (channel != null) {
 			unassigned = false;
 			EncampmentJobSystem.updateJobTaken();
 		}
 
 		// Set up mining in a circle?
 //		setupCircleMining(rallyPoint, 10);
 //		soldierState = SoldierState.RALLYING;
 	}
 	
 	@Override
 	public void run() {
 		try {
 			
 			DataCache.updateRoundVariables();
 			currentLocation = rc.getLocation(); // LEAVE THIS HERE UNDER ALL CIRCUMSTANCES
 			if (unassigned) {
 				
 				// Check if enemy nuke is half done
 				if (!enemyNukeHalfDone) {
 					Message message = BroadcastSystem.read(ChannelType.ENEMY_NUKE_HALF_DONE);
 					if (message.isValid && message.body == 1) {
 						enemyNukeHalfDone = true;
 					}
 				}
 				if (enemyNukeHalfDone) {
 					soldierState = SoldierState.ALL_IN;
 				}
 				
 				if (soldierState == SoldierState.NEW) {
 					// If we're standing on a mine close to our base, we should clear out the mine
 					Team mineTeam = rc.senseMine(rc.getLocation());
 					if (mineTeam != null && mineTeam != rc.getTeam()) {
 						soldierState = SoldierState.ESCAPE_HQ_MINES;
 					} else {
 						soldierState = SoldierState.RALLYING;
 					}
 				}
 				
 //				rc.setIndicatorString(0, soldierState.toString());
 				
 				switch (soldierState) {
 				case ESCAPE_HQ_MINES:
 					// We need to run away from the mines surrounding our base
 					Team mineTeam = rc.senseMine(rc.getLocation());
 					if (mineTeam != null && mineTeam != rc.getTeam()) {
 						// We need to run away from the mines surrounding our base
 						if (NavSystem.safeLocationAwayFromHQMines != null) {
 							NavSystem.goToLocationDontDefuseOrAvoidMines(NavSystem.safeLocationAwayFromHQMines);
 						} else {
 							NavSystem.goAwayFromHQEscapeMines(DataCache.ourHQLocation);
 						}
 					} else {
 						// No more mines, so clear out HQ mines
 						soldierState = SoldierState.CLEAR_OUT_HQ_MINES;
 					}
 					break;
 				case CLEAR_OUT_HQ_MINES:
 					// Clear out a path to the HQ
 					Team mineTeam1 = rc.senseMine(rc.getLocation());
 					if (mineTeam1 == null || mineTeam1 == rc.getTeam()) {
 						NavSystem.goToLocation(DataCache.ourHQLocation);
 					} else {
 						// We're done
 						soldierState = SoldierState.RALLYING;
 					}
 				case ALL_IN:
 					microCode();
 					break;
 					
 				case PUSHING: 
 					if (DataCache.numTotalEnemyRobots > 0) {
 						soldierState = SoldierState.FIGHTING;
 					} else {
 						pushCode();
 					}
 				case FIGHTING:
 					if (DataCache.numTotalEnemyRobots == 0) {
 						if (DataCache.numAlliedSoldiers < Constants.FIGHTING_NOT_ENOUGH_ALLIED_SOLDIERS) {
 							soldierState = SoldierState.RALLYING;
 						} else {
 							soldierState = SoldierState.PUSHING;
 						}
 						// Otherwise, just keep fighting
 					} else {
 						microCode();
 					}
 					break;
 				case RALLYING:
 					int hqPowerLevel = 10000;
 					Message message = BroadcastSystem.read(powerChannel);
 					if (message.isValid) {
 						hqPowerLevel = message.body;
 					}
 
 
 					// If there are enemies nearby, trigger FIGHTING SoldierState
 					if (DataCache.numTotalEnemyRobots > 0) {
 						soldierState = SoldierState.FIGHTING;
 					} else if (hqPowerLevel < 100) {
 						soldierState = SoldierState.PUSHING;
 					} else {
 						boolean layedMine = false;
 						if (rc.senseMine(currentLocation) == null) {
 							if (rc.isActive() && Util.Random() < 0.1) {
 								rc.layMine();
 								layedMine = true;
 							}
 						} 
 						if (!layedMine) {
 							NavSystem.goToLocation(rallyPoint);
 						}
 					}
 					break;
 				default:
 					break;
 				}
 			} else {
 				// This soldier has an encampment job, so it should go do that job
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
 //		soldierState = SoldierState.MINING_IN_CIRCLE;
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
 //		rc.setIndicatorString(0, "miningRadiusSquared " + miningRadiusSquared);
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
 	
 	private int getNumAlliedNeighbors() {
 		return rc.senseNearbyGameObjects(Robot.class, 2, rc.getTeam()).length;
 	}
 	
 	private int getNumAlliedNeighborsSquare(MapLocation square) {
 		return rc.senseNearbyGameObjects(Robot.class, square, 2, rc.getTeam()).length;
 
 	}
 
 	public void microCode() throws GameActionException {
 		Robot[] enemiesList = rc.senseNearbyGameObjects(Robot.class, 100000, rc.getTeam().opponent());
 		int[] closestEnemyInfo = getClosestEnemy(enemiesList);
 		MapLocation closestEnemyLocation = new MapLocation(closestEnemyInfo[1], closestEnemyInfo[2]);
 		int enemyDistSquared = closestEnemyLocation.distanceSquaredTo(rc.getLocation());
 
 		if (enemyDistSquared <= 2) { // if there is enemy in one dist
 //			Robot[] enemiesInOneDist = rc.senseNearbyGameObjects(Robot.class, 2, rc.getTeam().opponent());
 			// stay
 //			rc.setIndicatorString(0, "stay1");
 		} else if (enemyDistSquared == 16 || enemyDistSquared > 18){ // if no enemies in one, two, or three dist
 //			rc.setIndicatorString(0, "no enemies in 1,2,3 dist");
 //			rc.setIndicatorString(1, Integer.toString(DataCache.numNearbyEnemySoldiers));
 			if (DataCache.numNearbyEnemySoldiers == 0 || DataCache.numNearbyAlliedSoldiers >= 3 * DataCache.numNearbyEnemySoldiers) { 
 				// if no enemies in 5-dist or we outnumber them 5 to 1
 				NavSystem.goToLocation(closestEnemyLocation);
 			} else {
 				NavSystem.goToLocationAvoidMines(closestEnemyLocation);
 			}
 		} else { // enemies in two or three dist
 			double[] our23 = getEnemies2Or3StepsAway();
 			double[] enemy23 = getEnemies2Or3StepsAwaySquare(closestEnemyLocation, rc.getTeam().opponent());
 //			rc.setIndicatorString(2, our23[0] + " " + our23[1] + " " + our23[2]);
 //
 //			rc.setIndicatorString(1, enemy23[0] + " " + enemy23[1] + " " + enemy23[2]);
 			if (our23[1] > 0) { // closest enemy in 2 dist
 
 
 				if (enemy23[0] > 0) { // if enemy has dist 1
 					NavSystem.goToLocationAvoidMines(closestEnemyLocation);
 //					rc.setIndicatorString(0, "forward2");
 				} else {
 					if (enemy23[1] + enemy23[0] > our23[1] + our23[2]+1) {
 						// move forward
 						NavSystem.goToLocationAvoidMines(closestEnemyLocation);
 //						rc.setIndicatorString(0, "forward2");
 					} 
 					//        			else if (enemy23[0] + enemy23[1] + enemy23[2] > our23[1] + our23[2]+2) {
 					//                		rc.setIndicatorString(0, "back2.5");
 					//                		goAwayFromLocation(closestEnemyLocation);
 					//                		//back
 					//                	} 
 					else {
 						NavSystem.goAwayFromLocationAvoidMines(closestEnemyLocation);
 //						rc.setIndicatorString(0, "back2");
 					}
 				}
 
 			} else { // closest enemy is 3 dist
 				if (enemy23[0] > 0) {
 					NavSystem.goToLocationAvoidMines(closestEnemyLocation);
//					rc.setIndicatorString(0, "forward4");
 				} else if (enemy23[1] > 0) { // if enemy 2dist is > 0
 					int closestDist = 100;
 					int dist;
 					MapLocation closestAllyLocation = null;
 					Robot[] twoDistAllies = rc.senseNearbyGameObjects(Robot.class, closestEnemyLocation, 8, rc.getTeam());
 					for (Robot ally: twoDistAllies) {
 						RobotInfo arobotInfo = rc.senseRobotInfo(ally);
 						dist = arobotInfo.location.distanceSquaredTo(rc.getLocation());
 						if (dist<closestDist){
 							closestDist = dist;
 							closestAllyLocation = arobotInfo.location;
 						}
 					}
 
 					double[] ally23 = getEnemies2Or3StepsAwaySquare(closestAllyLocation, rc.getTeam());
 
 					if (enemy23[0] + enemy23[1] + enemy23[2] > ally23[1] + ally23[2]) {
 						NavSystem.goToLocationAvoidMines(closestEnemyLocation);
//						rc.setIndicatorString(0, "forward3");
 					} else {
//						rc.setIndicatorString(0, "stay3");
 					}
 				} else {
//					rc.setIndicatorString(0, "stay4");
 				}
 			}
 		}
 	}
 
 
 
 	private double[] getEnemies2Or3StepsAway() throws GameActionException {
 		double count1 = 0;
 		double count2 = 0;
 		double count3 = 0;
 		Robot[] enemiesInVision = rc.senseNearbyGameObjects(Robot.class, 18, rc.getTeam().opponent());
 		for (Robot enemy: enemiesInVision) {
 			RobotInfo rinfo = rc.senseRobotInfo(enemy);
 			int dist = rinfo.location.distanceSquaredTo(rc.getLocation());
 			if (rinfo.type == RobotType.SOLDIER) {
 				if (dist <= 2) {
 					count1++;
 				} else if (dist <=8) {
 					count2++;
 				} else if (dist > 8 && (dist <= 14 || dist == 18)) {
 					count3++;
 				}
 			} else {
 				if (dist <= 2) {
 					count1++;
 				} else if (dist <=8) {
 					count2 += 1;
 				} else if (dist > 8 && (dist <= 14 || dist == 18)) {
 					count3 += 1;
 				}
 			}
 
 		}
 
 		double[] output = {count1, count2, count3};
 		return output;
 	}
 
 	private double[] getEnemies2Or3StepsAwaySquare(MapLocation square, Team squareTeam) throws GameActionException {
 		double count1 = 0;
 		double count2 = 0;
 		double count3 = 0;
 		Robot[] enemiesInVision = rc.senseNearbyGameObjects(Robot.class, square, 18, squareTeam.opponent());
 		for (Robot enemy: enemiesInVision) {
 			RobotInfo rinfo = rc.senseRobotInfo(enemy);
 			int dist = rinfo.location.distanceSquaredTo(square);
 			if (rinfo.type == RobotType.SOLDIER) {
 				if (dist <= 2) {
 					count1++;
 				} else if (dist <=8) {
 					count2++;
 				} else if (dist <= 14 || dist == 18) {
 					count3++;
 				}
 			} else {
 				if (dist <= 2) {
 					count1++;
 				} else if (dist <=8) {
 					count2 += 1;
 				} else if (dist <= 14 || dist == 18) {
 					count3 += 1;
 				}
 			}
 		}
 
 		int selfDist = square.distanceSquaredTo(rc.getLocation());
 		
 		if (selfDist <= 2) {
 			count1++;
 		} else if (selfDist<=8) {
 			count2++;
 		} else if (selfDist <= 14 || selfDist == 18) {
 			count3++;
 		}
 
 		double[] output = {count1, count2, count3};
 		return output;
 	}
 
 	public int[] getClosestEnemy(Robot[] enemyRobots) throws GameActionException {
 		int closestDist = rc.getLocation().distanceSquaredTo(rc.senseEnemyHQLocation());
 		MapLocation closestEnemy=rc.senseEnemyHQLocation(); // default to HQ
 
 		int dist = 0;
 		for (int i=0;i<enemyRobots.length;i++){
 			RobotInfo arobotInfo = rc.senseRobotInfo(enemyRobots[i]);
 			dist = arobotInfo.location.distanceSquaredTo(rc.getLocation());
 			if (dist<closestDist){
 				closestDist = dist;
 				closestEnemy = arobotInfo.location;
 			}
 		}
 		int[] output = new int[4];
 		output[0] = closestDist;
 		output[1] = closestEnemy.x;
 		output[2] = closestEnemy.y;		
 		return output;
 	}
 	
 	private void pushCode() throws GameActionException {		
 		if (NavSystem.navMode != NavMode.SMART || NavSystem.destination != EnemyHQLocation) {
 			NavSystem.setupSmartNav(EnemyHQLocation);
 		} else {
 			NavSystem.followWaypoints(true, true);
 		}
 	}
 	/** code to be used by capturers
 	 * 
 	 * @throws GameActionException
 	 */
 	private void captureCode() throws GameActionException {
 		if (!unassigned) { // if assigned to something
 			EncampmentJobSystem.updateJobTaken();
 		}
 		if (rc.isActive()) {
 			if (rc.senseEncampmentSquare(currentLocation) && currentLocation.equals(EncampmentJobSystem.goalLoc)) {
 				if (rc.getTeamPower() > rc.senseCaptureCost()) {
 					rc.captureEncampment(EncampmentJobSystem.assignedRobotType);
 				}
 			} else {
 				if (rc.senseNearbyGameObjects(Robot.class, 14, rc.getTeam().opponent()).length > 0) {
 					unassigned = true;
 					soldierState = SoldierState.FIGHTING;
 				}
 				if (NavSystem.navMode == NavMode.BFSMODE) {
 					NavSystem.tryBFSNextTurn();
 				} else if (NavSystem.navMode == NavMode.GETCLOSER){
 					NavSystem.tryMoveCloser();
 				} else if (rc.getLocation().distanceSquaredTo(EncampmentJobSystem.goalLoc) <= 8) {
 					NavSystem.setupGetCloser(EncampmentJobSystem.goalLoc);
 					NavSystem.tryMoveCloser();
 				} else {
 					NavSystem.goToLocation(EncampmentJobSystem.goalLoc);
 //					if (NavSystem.navMode == NavMode.NEUTRAL){
 //						NavSystem.setupSmartNav(EncampmentJobSystem.goalLoc);
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
 	
 	private MapLocation findRallyPoint() {
 		MapLocation enemyLoc = EnemyHQLocation;
 		MapLocation ourLoc = HQLocation;
 		int x, y;
 		x = (enemyLoc.x+3*ourLoc.x)/4;
 		y = (enemyLoc.y+3*ourLoc.y)/4;
 		return new MapLocation(x,y);
 	}
 
 }
