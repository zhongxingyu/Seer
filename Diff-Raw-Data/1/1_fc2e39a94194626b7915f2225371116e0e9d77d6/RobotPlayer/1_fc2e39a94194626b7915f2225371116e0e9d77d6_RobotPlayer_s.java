 package oc002;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import battlecode.common.Clock;
 import battlecode.common.Direction;
 import battlecode.common.GameActionException;
 import battlecode.common.GameConstants;
 import battlecode.common.GameObject;
 import battlecode.common.MapLocation;
 import battlecode.common.Robot;
 import battlecode.common.RobotController;
 import battlecode.common.RobotType;
 import battlecode.common.Team;
 import battlecode.common.Upgrade;
 
 public class RobotPlayer {    	
 	private final static double MIN_TEAM_POWER = 50.0;
 
 	private final static int ROUND_NUM_SELF_DESTRUCT = 2000;
 	
 	private final static int BIG_NUM_FRIENDLIES_ALIVE_ATTACK_THRESHOLD = 40;
 	
 	private final static int SMALL_RADIUS = 3;
 	private final static int HUGE_RADIUS = 10000;
 	private final static int DEFAULT_SENSE_ENEMY_RADIUS = 150;
 	private final static int DEFAULT_SENSE_FRIENDLY_RADIUS = 1;	
 	
 	// TODO: calculate these dynamically maybe based on the X and Y starting positions? map width and height?
 	private final static int NUM_ENEMIES_CHANNEL_1 = 12098;
 	private final static int NUM_ENEMIES_CHANNEL_2 = 21987;
 	private final static int NUM_ENEMIES_CHANNEL_3 = 31045;
 
 	private final static int NUM_ENEMIES_SEEN_RECENTLY_ATTACK_HQ_THRESHOLD = 20;
 	private final static int MIN_NUM_FRIENDLIES_ALIVE_ATTACK_THRESHOLD = 20;
 	private final static int MAGIC_CONSTANT = 900;
 	
 	private final static double MIN_POWER_TO_READ_BROADCAST = GameConstants.BROADCAST_READ_COST * 2;
 	private final static double MIN_POWER_TO_SEND_BROADCAST = GameConstants.BROADCAST_SEND_COST * 2;
 	
 	public static void run(RobotController rc) {
 		while(true) {
 			try {
 				playOneTurn(rc);
 				rc.yield();		
 			} catch (Exception e) {
 				debug_printf(e);
 			}
 		}
 	}
 
 	// System.setProperty("debugMethodsEnabled", "true");
 	// use bc.conf to turn debug mode on	
 	private static void debug_printf(String format, Object ... objects) {
 		System.out.printf(format, objects);
 		System.out.printf("\n");
 	}	
 
 	private static void debug_printf(Exception e) {
 		System.out.printf("%s: %s", e.getStackTrace()[0].getMethodName(), e.getMessage());
 		System.out.printf("\n");
 	}	
 
 	private static void playOneTurn(RobotController rc) {
 		if (rc.isActive()) {
 			switch(rc.getType()) {
 				case HQ:
 					playOneTurn_hq(rc);
 					break;		
 				case SOLDIER:
 					playOneTurn_soldier(rc);
 					break;			
 				case SUPPLIER:
 					playOneTurn_supplier(rc);
 					break;
 				case ARTILLERY:
 					playOneTurn_artillery(rc);
 					break;
 				case GENERATOR:
 					playOneTurn_generator(rc);
 					break;
 				case MEDBAY:
 					playOneTurn_medbay(rc);
 					break;
 				case SHIELDS:
 					playOneTurn_shields(rc);
 					break;
 			}
 		}
 	}
 
 	private static void playOneTurn_hq(RobotController rc) {
 		double power = rc.getTeamPower();
 
 		// try to spawn a soldier
 
 		if (power >= MIN_TEAM_POWER) {
 			Direction spawnDirection = findBestSpawnDirection(rc);
 			if (spawnDirection != Direction.NONE && couldSpawnSoldier(rc, spawnDirection)) {
 				return;
 			}
 		}
 
 		// try to do research
 
 		Upgrade bestUpgrade = findBestUpgrade(rc);
 		try {
 			rc.researchUpgrade(bestUpgrade);
 		} catch (GameActionException e) {
 			debug_printf(e);
 		}
 	}	
 
 	private static Upgrade findBestUpgrade(RobotController rc) {
 		Upgrade bestUpgrade = Upgrade.NUKE;
 
 		Upgrade upgradePriorities[] = new Upgrade[]{
 				Upgrade.FUSION, // assume FUSION is the best upgrade?
 				Upgrade.VISION,
 				Upgrade.PICKAXE,
 				Upgrade.DEFUSION,
 				Upgrade.NUKE
 		};
 
 		for (Upgrade upgrade: upgradePriorities) {
 			if (!rc.hasUpgrade(upgrade)) {
 				bestUpgrade = upgrade;
 				break;
 			}
 		}
 		return bestUpgrade;
 	}
 
 	private static Direction findBestSpawnDirection(RobotController rc) {
 		Direction bestDirection = Direction.NONE;
 		MapLocation currentLocation = rc.getLocation();
 
 		for (Direction direction : Direction.values()) {
 			MapLocation newLocation = currentLocation.add(direction);
 			GameObject gameObject = null;
 			try {
 				gameObject = rc.senseObjectAtLocation(newLocation);
 			} catch (GameActionException e) {
 				debug_printf(e);
 			}
 			if (gameObject == null) {
 				bestDirection = direction;
 				break;
 			}
 		}
 		return bestDirection;
 	}
 
 	private static boolean couldSpawnSoldier(RobotController rc, Direction direction) {
 		boolean spawned = false;
 		try {
 			rc.spawn(direction);
 			spawned = true;
 		} catch (GameActionException e) {
 			debug_printf(e);
 			spawned = false;
 		} 	
 		return spawned;
 	}	
 
 	private static void playOneTurn_soldier(RobotController rc) {
 		MapLocation myLocation = rc.getLocation();		
 		Team myTeam = rc.getTeam();
 	
 		Robot nearbyEnemyRobots[] = getNearbyEnemies(rc);
 		
 		// this wastes power, so don't do it all the time
 		if (Clock.getRoundNum() % 5 == 0) {
 			if (nearbyEnemyRobots.length > 0) {
 				increaseNumEnemiesSeenRecently(rc, nearbyEnemyRobots.length * MAGIC_CONSTANT);
 			}
 			// decay
 			decreaseNumEnemiesSeenRecently(rc, 1);
 		}
 		
 		Robot nearbyFriendlyRobots[] = getNearbyFriendlies(rc);
 		Robot allFriendlyRobots[] = getAllFriendlies(rc);
 		
 		int numFriendlyRobots = allFriendlyRobots.length;
 		int numEnemiesSeenRecently = getNumEnemiesSeenRecently(rc);
 		
 		boolean shouldCounterAttack = numEnemiesSeenRecently >= NUM_ENEMIES_SEEN_RECENTLY_ATTACK_HQ_THRESHOLD &&
 									  numFriendlyRobots > MIN_NUM_FRIENDLIES_ALIVE_ATTACK_THRESHOLD;
 									  
 		debug_printf("round %d: %s, %d enemies, %d friendlies\n", Clock.getRoundNum(), shouldCounterAttack,  numEnemiesSeenRecently, numFriendlyRobots);
 		
 		boolean shouldKamikaze = allFriendlyRobots.length > BIG_NUM_FRIENDLIES_ALIVE_ATTACK_THRESHOLD ||
 				 				   Clock.getRoundNum() >= ROUND_NUM_SELF_DESTRUCT;
 		
 		boolean shouldAttackHQ = shouldCounterAttack || shouldKamikaze;
 				 				   
 		boolean shouldExplore = Math.random() < 0.05;
 				
 		boolean shouldMove = nearbyEnemyRobots.length > 1 || 
 							 nearbyFriendlyRobots.length > 1 ||
 							 shouldAttackHQ ||
 							 shouldExplore;	
 
 		// try to defuse enemy mine
 							 
 		MapLocation mineLocations[] = rc.senseNonAlliedMineLocations(myLocation, SMALL_RADIUS);
 		for (MapLocation mineLocation : mineLocations) {
 			if (couldDefuseMine(rc, mineLocation)) {
 				return;
 			}				
 		}
 							 
 		// try to move						 
 							 
 		if (shouldMove) {
 			
 			Direction direction = findBestDirectionToMove(rc, myLocation, myTeam, nearbyEnemyRobots, shouldAttackHQ);
 
 			if (direction != null) {
 				if (couldMove(rc, direction)) {
 					return;
 				}
 			}			
 		}
 		
 
 		// try to build encampments
 
 		if (rc.senseEncampmentSquare(myLocation)) {
 			RobotType encampmentTypes[] = new RobotType[]{
 					// prioritised best to worst
 					RobotType.SUPPLIER,
 					RobotType.GENERATOR,										
 					RobotType.MEDBAY, 
 					//RobotType.MEDBAY, 
 					RobotType.SHIELDS,
 					RobotType.ARTILLERY,
 			};
 			for (RobotType encampmentType: encampmentTypes) {
 				if (shouldBuildEncampment(rc, encampmentType, myLocation, myTeam) && couldBuildEncampment(rc, encampmentType)) {
 					return;
 				}				
 			}
 		}	
 
 		// mining
 
 		Team mineStatus = rc.senseMine(myLocation);
 		
 		if (mineStatus == null) {			
 			if (couldLayMine(rc)) {
 				return;
 			}
 		} 	
 
 	}	
 
 	private static boolean couldDefuseMine(RobotController rc, MapLocation location) {
 		boolean defused = false;
 		try {
 			rc.defuseMine(location);
 			defused = true;
 		} catch (GameActionException e) {
 			debug_printf(e);
 			defused = false;
 		}
 		return defused;
 	}
 
 	private static boolean shouldBuildEncampment(RobotController rc, RobotType encampmentType, MapLocation location, Team team) {		
 		boolean shouldBuild = false;
 
 		MapLocation encampmentSquares[] = {};
 		try {
 			encampmentSquares = rc.senseEncampmentSquares(location, SMALL_RADIUS, team);
 		} catch (GameActionException e) {
 			debug_printf(e);
 		}
 
 		if (encampmentSquares.length == 0) {
 			shouldBuild = true;
 		}
 
 		return shouldBuild;
 	}
 
 	private static boolean couldBuildEncampment(RobotController rc, RobotType encampmentType) {
 		boolean builtEncampment = false;
 		try {
 			rc.captureEncampment(encampmentType);
 			builtEncampment = true;
 		} catch (GameActionException e) {
 			debug_printf(e);
 			builtEncampment = false;
 		}
 		return builtEncampment;
 	}
 
 	private static boolean couldLayMine(RobotController rc) {
 		boolean mined = false;
 		try {
 			rc.layMine();
 			mined = true;
 		} catch (GameActionException e) {
 			debug_printf(e);
 			mined = false;
 		}
 		return mined;
 	}
 
 	private static boolean couldMove(RobotController rc, Direction direction) {
 		boolean moved = false;
 		try {
 			rc.move(direction);
 			moved = true;
 		} catch (GameActionException e) {
 			debug_printf(e);
 			moved = false;
 		}
 		return moved;
 	}
 
 	private static Direction findBestDirectionToMove(RobotController rc, MapLocation myLocation, Team myTeam, Robot nearbyEnemyRobots[], boolean shouldAttackHQ) {
 		Direction bestDirection = null;
 
 		List<Direction> prioritisedDirections = getPrioritisedDirections(rc, myLocation, myTeam, shouldAttackHQ);
 
 		for (Direction direction: prioritisedDirections) {
 			// causes exception
 			if (direction != Direction.OMNI && direction != Direction.NONE) {
 				MapLocation newLocation = myLocation.add(direction);
 				Team mineTeam = rc.senseMine(newLocation);
 				boolean safeDirection = (mineTeam == null || mineTeam == myTeam);
 				if (safeDirection && rc.canMove(direction)) {
 					bestDirection = direction;
 					break;
 				}
 			}
 		}	
 		
 		return bestDirection;
 	}
 
 	private static int getNumEnemiesSeenRecently(RobotController rc) {	
 		int numEnemies = 0;
 		
 		if (rc.getTeamPower() > (3 * MIN_POWER_TO_READ_BROADCAST)) {
 			int message1 = tryGetMessage(rc, NUM_ENEMIES_CHANNEL_1);
 			int message2 = tryGetMessage(rc, NUM_ENEMIES_CHANNEL_2);
 			int message3 = tryGetMessage(rc, NUM_ENEMIES_CHANNEL_3);
 			
 			if (message1 == message2 && message2 == message3) {
 				numEnemies = message1;
 			}
 		}
 		
 		return numEnemies;
 	}
 	
 	private static void increaseNumEnemiesSeenRecently(RobotController rc, int amount) {
 		if (rc.getTeamPower() > (3 * MIN_POWER_TO_SEND_BROADCAST) + (3 * MIN_POWER_TO_READ_BROADCAST)) {
 			
 			int numEnemies = getNumEnemiesSeenRecently(rc);
 			numEnemies += amount;
 			
 			trySendMessage(rc, NUM_ENEMIES_CHANNEL_1, numEnemies);
 			trySendMessage(rc, NUM_ENEMIES_CHANNEL_2, numEnemies);
 			trySendMessage(rc, NUM_ENEMIES_CHANNEL_3, numEnemies);
 		}
 	}
 	
 	private static void decreaseNumEnemiesSeenRecently(RobotController rc, int amount) {
 		if (rc.getTeamPower() > (3 * MIN_POWER_TO_SEND_BROADCAST) + (3 * MIN_POWER_TO_READ_BROADCAST)) {
 			int numEnemies = getNumEnemiesSeenRecently(rc);
 			numEnemies -= amount;
 			
 			if (numEnemies < 0) {
 				numEnemies = 0;
 			}
 			
 			trySendMessage(rc, NUM_ENEMIES_CHANNEL_1, numEnemies);
 			trySendMessage(rc, NUM_ENEMIES_CHANNEL_2, numEnemies);
 			trySendMessage(rc, NUM_ENEMIES_CHANNEL_3, numEnemies);
 		}
 	}		
 	
 	private static void trySendMessage(RobotController rc, int channel, int message) {
 		try {
 			rc.broadcast(channel,  message);
 		} catch (GameActionException e) {
 			debug_printf(e);
 		}
 	}
 
 	private static int tryGetMessage(RobotController rc, int channel) {
 		int message = 0;
 		try {
 			message = rc.readBroadcast(channel);
 		} catch (GameActionException e) {
 			debug_printf(e);
 		}
 		return message;
 	}
 
 	private static List<Direction> getPrioritisedDirections(final RobotController rc, final MapLocation myLocation, final Team myTeam, boolean shouldAttackHQ) {
 
 		List<Direction> directions = Arrays.asList(Direction.values());
 		final Robot nearbyEnemies[] = getNearbyEnemies(rc);
 		final MapLocation enemyHQLocation = rc.senseEnemyHQLocation();
 	
 		if (nearbyEnemies.length > 0) {	
 
 			// if there are nearby enemy, go towards them
 			
 			// sort based on distance to enemy
 			// TODO: more efficient sort
 			Collections.sort(directions, new Comparator<Direction>() {
 				@Override
 				public int compare(Direction o1, Direction o2) {
 					Double distance1 = distanceToNearbyEnemy(o1);
 					Double distance2 = distanceToNearbyEnemy(o2);
 					return distance1.compareTo(distance2);
 				}
 				
 				private double distanceToNearbyEnemy(Direction direction) {
 					MapLocation newLocation = myLocation.add(direction);
 					double shortestDistance = -1;
 
 					for (Robot robot: nearbyEnemies) {
 						double distance = -1;
 						try {
 							distance = newLocation.distanceSquaredTo(rc.senseLocationOf(robot));
 						} catch (GameActionException e) {
 							debug_printf(e);
 						}
 						if (shortestDistance == -1 || distance < shortestDistance) {
 							shortestDistance = distance;
 						}
 					}
 
 					return shortestDistance;
 				}
 			});
 			
 		} else if (shouldAttackHQ) {
 			
 			// head to enemy HQ
 			// TODO: more efficient sort
 			Collections.sort(directions, new Comparator<Direction>() {
 				@Override
 				public int compare(Direction o1, Direction o2) {
 					Double distance1 = distanceToEnemyHQ(o1);
 					Double distance2 = distanceToEnemyHQ(o2);
 					return distance1.compareTo(distance2);
 				}
 				
 				private double distanceToEnemyHQ(Direction direction) {
 					MapLocation newLocation = myLocation.add(direction);
 					double distance = newLocation.distanceSquaredTo(enemyHQLocation);
 					return distance;
 				}
 			});
 			
 		} else {			
 			// otherwise move randomly
 			Collections.shuffle(directions);
 		}
 
 		return directions;
 	}
 
 	private static Robot[] getAllEnemies(RobotController rc) {
 		return getNearbyEnemies(rc, HUGE_RADIUS);
 	}	
 	
 	private static Robot[] getNearbyEnemies(RobotController rc) {
 		return getNearbyEnemies(rc, DEFAULT_SENSE_ENEMY_RADIUS);
 	}
 	
 	private static Robot[] getNearbyEnemies(RobotController rc, int radius) {
 		Team otherTeam = rc.getTeam().opponent();
 		return rc.senseNearbyGameObjects(Robot.class, radius, otherTeam);
 	}
 	
 	private static Robot[] getAllFriendlies(RobotController rc) {
 		return getNearbyFriendlies(rc, HUGE_RADIUS);
 	}
 	
 	private static Robot[] getNearbyFriendlies(RobotController rc) {
 		return getNearbyFriendlies(rc, DEFAULT_SENSE_FRIENDLY_RADIUS);
 	}	
 	
 	private static Robot[] getNearbyFriendlies(RobotController rc, int radius) {
 		Team myTeam = rc.getTeam();
 		return rc.senseNearbyGameObjects(Robot.class, radius, myTeam);
 	}	
 
 	private static void playOneTurn_artillery(RobotController rc) {
 		throw new RuntimeException("TODO: implement artillery logic");		
 	}	
 	
 	private static void playOneTurn_shields(RobotController rc) {
 		// no action possible	
 	}
 
 	private static void playOneTurn_medbay(RobotController rc) {
 		// no action possible
 	}
 
 	private static void playOneTurn_generator(RobotController rc) {
 		// no action possible
 	}
 
 	private static void playOneTurn_supplier(RobotController rc) {
 		// no action possible
 	}	
 }
 
