 package basePlusSwarm;
 
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
 				
 				rc.setIndicatorString(0, soldierState.toString());
 				
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
 						if (NavSystem.navMode != NavMode.SMART || NavSystem.destination != EnemyHQLocation) {
 							NavSystem.setupSmartNav(EnemyHQLocation);
 						}
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
 	
 	private double[] getEnemies2Or3StepsAway() throws GameActionException {
 		double count2 = 0;
 		double count3 = 0;
 		Robot[] enemiesInVision = rc.senseNearbyGameObjects(Robot.class, 18, rc.getTeam().opponent());
 		for (Robot enemy: enemiesInVision) {
 			RobotInfo rinfo = rc.senseRobotInfo(enemy);
 			int dist = rinfo.location.distanceSquaredTo(currentLocation);
 			if (rinfo.type == RobotType.SOLDIER) {
 				if (dist <=8) {
 					count2++;
 				} else if (dist > 8 && (dist <= 14 || dist == 18)) {
 					count3++;
 				}
 			} else {
 				if (dist <=8) {
 					count2 += 0.5;
 				} else if (dist > 8 && (dist <= 14 || dist == 18)) {
 					count3 += 0.5;
 				}
 			}
 			
 		}
 		
 		double[] output = {count2, count3};
 		return output;
 	}
 	
 	private double[] getEnemies2Or3StepsAwaySquare(MapLocation square, Team squareTeam) throws GameActionException {
 		double count2 = 0;
 		double count3 = 0;
 		Robot[] enemiesInVision = rc.senseNearbyGameObjects(Robot.class, 18, squareTeam.opponent());
 		for (Robot enemy: enemiesInVision) {
 			RobotInfo rinfo = rc.senseRobotInfo(enemy);
 			int dist = rinfo.location.distanceSquaredTo(currentLocation);
 			if (rinfo.type == RobotType.SOLDIER) {
 				if (dist <=8) {
 					count2++;
 				} else if (dist > 8 && (dist <= 14 || dist == 18)) {
 					count3++;
 				}
 			} else {
 				if (dist <=8) {
 					count2 += 0.5;
 				} else if (dist > 8 && (dist <= 14 || dist == 18)) {
 					count3 += 0.5;
 				}
 			}
 		}
 		
 		double[] output = {count2, count3};
 		return output;
 	}
 	
 	public int[] getClosestEnemy(Robot[] enemyRobots) throws GameActionException {
 		int closestDist = currentLocation.distanceSquaredTo(EnemyHQLocation);
 		MapLocation closestEnemy=EnemyHQLocation; // default to HQ
 
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
 
 	
     private void microCode() throws GameActionException {
         Robot[] enemiesList = rc.senseNearbyGameObjects(Robot.class, 100000, rc.getTeam().opponent());
         int[] closestEnemyInfo = getClosestEnemy(enemiesList);
         MapLocation closestEnemyLocation = new MapLocation(closestEnemyInfo[1], closestEnemyInfo[2]);
         if (rc.senseNearbyGameObjects(Robot.class, 14, rc.getTeam().opponent()).length > 0) {
                 double[] our23 = getEnemies2Or3StepsAway();
                 double[] enemy23 = getEnemies2Or3StepsAwaySquare(closestEnemyLocation, rc.getTeam().opponent());
                 Direction dir = currentLocation.directionTo(closestEnemyLocation);
 //              int numAlliesNext = getNumAlliedNeighborsSquare(currentLocation.add(dir));
 //              int numAllies = getNumAlliedNeighbors();
                 if (our23[0] + our23[1] < enemy23[0] + enemy23[1]) {
                         NavSystem.goToLocation(closestEnemyLocation);
                 } else if (our23[0] + our23[1] > enemy23[0] + enemy23[1]){
                         NavSystem.goAwayFromLocation(closestEnemyLocation);
                 }
         } else {
                 NavSystem.goToLocation(closestEnemyLocation);
         }
 }
 //	private void microCode() throws GameActionException {
 //		if (rc.isActive()) {
 //			Robot[] enemiesList = rc.senseNearbyGameObjects(Robot.class, 100000, rc.getTeam().opponent());
 //			int[] closestEnemyInfo = getClosestEnemy(enemiesList);
 //			MapLocation closestEnemyLocation = new MapLocation(closestEnemyInfo[1], closestEnemyInfo[2]);
 //			NavSystem.setupSmartNav(closestEnemyLocation);
 ////			if (DataCache.numNearbyAlliedSoldiers > 3 * DataCache.numTotalEnemyRobots) {
 //////				NavSystem.goToLocation(closestEnemyLocation);
 ////				double random = Util.Random();
 ////				System.out.println("random: " + random);
 ////				if (random < 0.05) {
 ////					NavSystem.followWaypoints(true);
 ////				} else {
 ////					NavSystem.followWaypoints(false);
 ////				}
 //				
 ////			} else {
 //				if (DataCache.numNearbyEnemyRobots > 0) {
 //					double[] our23 = getEnemies2Or3StepsAway();
 //					double[] enemy23 = getEnemies2Or3StepsAwaySquare(closestEnemyLocation, rc.getTeam().opponent());
 //					Direction dir = currentLocation.directionTo(closestEnemyLocation);
 //					//					int numAlliesNext = getNumAlliedNeighborsSquare(currentLocation.add(dir));
 //					//					int numAllies = getNumAlliedNeighbors();
 //					if (our23[0] + our23[1] < enemy23[0] + enemy23[1]) {
 //						NavSystem.goToLocationAvoidMines(closestEnemyLocation);
 //					} else if (our23[0] + our23[1] > enemy23[0] + enemy23[1]){
 //						NavSystem.goAwayFromLocationAvoidMines(closestEnemyLocation);
 //					}
 //					
 //					
 //
 //				} else {
 //					NavSystem.followWaypoints(true);
 //				}
 //		}
 //	}
 
 	
 	private void pushCode() throws GameActionException {
 		
 		// Swarming
 		double dxToEnemyHQ = DataCache.enemyHQLocation.x - rc.getLocation().x;
 		double dyToEnemyHQ = DataCache.enemyHQLocation.y - rc.getLocation().y;
 		double distanceToEnemyHQ = Math.sqrt(rc.getLocation().distanceSquaredTo(EnemyHQLocation));
 		
 		Robot[] nearbyAlliedRobots = rc.senseNearbyGameObjects(Robot.class, 14, rc.getTeam());
 		int totalDx = 0;
 		int totalDy = 0;
 		for (Robot alliedRobot : nearbyAlliedRobots) {
 			RobotInfo robotInfo = rc.senseRobotInfo(alliedRobot);
 			if (robotInfo.type == RobotType.SOLDIER) {
 				MapLocation iterLocation = robotInfo.location;
 				totalDx += (iterLocation.x - rc.getLocation().x);
 				totalDy += (iterLocation.y - rc.getLocation().y);
 			}
 		}
 		
 		double c = 3;
 		
 		double denom = Math.sqrt(totalDx*totalDx + totalDy*totalDy);
 		double addX, addY;
 		if (Math.abs(totalDx) < 0.01) {
 			addX = 0;
 		} else {
 			addX = c * totalDx / denom;
 		}
 		if (Math.abs(totalDy) < 0.01) {
 			addY = 0;
 		} else {
 			addY = c * totalDy / denom;
 		}
 		double finalDx = dxToEnemyHQ / distanceToEnemyHQ + addX;
 		double finalDy = dyToEnemyHQ / distanceToEnemyHQ + addY;
 		
 		rc.setIndicatorString(0, "totalDx: " + Integer.toString(totalDx) + ", finalDx: " + Double.toString(finalDx));
 		rc.setIndicatorString(1, "totalDy: " + Integer.toString(totalDy) + ", finalDy: " + Double.toString(finalDy));
 		
 		int dirOffset;
 		double ratioCutoff = 2.5;
 		
 		double ratio = Math.abs(finalDx / finalDy);
 		
 		if (ratio > ratioCutoff) {
 			// go along x-axis
 			if (finalDx > 0) {
 				dirOffset = 2;
 			} else {
 				dirOffset = 6;
 			}
 		} else if (ratio < 1 / ratioCutoff) {
 			// go along y-axis
 			if (finalDy > 0) {
 				dirOffset = 4;
 			} else {
 				dirOffset = 0;
 			}
 		} else {
 			if (finalDx > 0) {
 				if (finalDy >= 0) {
 					dirOffset = 3;
 				} else {
 					dirOffset = 1;
 				}
 			} else {
 				if (finalDy > 0) {
 					dirOffset = 5;
 				} else {
 					dirOffset = 7;
 				}
 			}
 		}
 		
 		Direction dirToMoveIn = Direction.values()[dirOffset];
 		NavSystem.goDirectionAndDefuse(dirToMoveIn);
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
