 package team211;
 
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
 
 /*
  * Goals
  * - capture camps
  * - research NUKE
  * 	- when is this appropriate?
  * - protect my camps
  * - prevent enemy from getting camps.
  * - attack enemy HQ
  * - heal damaged units
  * - destroy weak enemy units (isolated and/or damaged)
  * 
  * > Identify camps which are probably good to go after.
  * > Coordinate dispatch of units to camps.
  * > Build something on the camps.
  */
 
 /* Ideas:
  * - fuzzy search for "something" in a particular direction (cone-like expansion of search area).
  * - Use bytecode budget of things that are working (ie: HQ while spawning bots, bots while
  *       laying/defusing/capturing) to do higher level calculations & broadcast the information.
  * - Assign "ROLES" to subsets of RobotTypes (some will be "guards", "attack", "explore", "capture")
  * - In general, for rts games, positioning is very important. Specifically, you want to have
  * 		a concave shape for your army, surrounding the other guy's army. And, of course don't let
  * 		your scounts get caught. Micro-managing is important. Hurt guys run away, lose aggro, then come
  * 		back again.
  */
 
 
 /* Ideas:
  * - make should_clump() true every other hundred turns
  * - use number of allies adjacent to an enemy in do_battle()
  * - retask wanderer soilders after some number of turns
  * - make an alternate waypoint out of the way of attackers
  * - form 2 groups: 1 "wall" of defenders, and 1 of attackers which go around behind the enemy base.
  * 
  */
 
 public class RobotPlayer {
 	private static RobotController rc;
 	
 	private static MapLocation hq;
 	private static MapLocation enemy_hq;
 	
 	private static Team my_team;
 	
 	private final static int battle_len = 10;
 	private final static int battle_center = 7;
 	
 	final private static int [][] battle_allies  = new int[battle_len][battle_len];
 	final private static int [][] battle_enemies = new int[battle_len][battle_len];
 	final private static int [][] battle_good    = new int[battle_len][battle_len];
 	final private static int [][] battle_bad     = new int[battle_len][battle_len];
 	
 	private static String name = "Unnamed";
 	
 	private static void moveOrDefuse(Direction dir) throws GameActionException {
 		MapLocation ahead = rc.getLocation().add(dir);
 		Team t = rc.senseMine(ahead);
 		if(t != null && t != my_team) {
 			rc.defuseMine(ahead);
 		} else {
 			rc.setIndicatorString(1, "Last direction moved: " + dir);
 			rc.move(dir);			
 		}
 	}
 	
 	private static void careful_move(Direction dir) throws GameActionException {
 		if(rc.canMove(dir)) {
 			moveOrDefuse(dir);
 		}
 	}
 	
 	private static MapLocation find_closest_camp_2() throws GameActionException {
 		int r_sq = 50;
 		MapLocation [] locs = null;
 		while (locs == null || locs.length == 0) {
 			locs = rc.senseEncampmentSquares(rc.getLocation(), r_sq, Team.NEUTRAL);
 			r_sq = 4 * r_sq;
 		}
 		return locs[(int)(Math.random() * locs.length)];
 	}
 	
 	/* FIXME: expensive. */
 	private static MapLocation find_closest_neutral_camp() throws GameActionException {
 		MapLocation [] locs = rc.senseEncampmentSquares(rc.getLocation(), 1000000, Team.NEUTRAL);
 		MapLocation my_loc = rc.getLocation();
 		MapLocation closest = null;
 		int closest_d = 1000000;
 		for (MapLocation loc: locs) {
 			int d = loc.distanceSquaredTo(my_loc);
 			if (d < closest_d) {
 				closest_d = d;
 				closest = loc;
 			}
 		}
 		return closest;
 	}
 	
 	private static void random_careful_move()
 			throws GameActionException {
 		// Choose a random direction, and move that way if possible
 		Direction [] dirs = Direction.values();
 		int start = (int)(Math.random()*8);
 		int c = start;
 		Direction dir = dirs[c];
 		while (!rc.canMove(dir)) {
 			c = (c + 3) % 8; /* TODO: does this cover all values? */
 			if (c == start) {
 				System.out.println("FAILED TO MOVE");
 				return; /* can't move anywhere */
 			}
 			dir = dirs[c];
 		}
 		
 		moveOrDefuse(dir);
 	}
 	
 	private static void jamm_coms(RobotController rc, int ct) throws GameActionException {
 		return;
 		/*
 		while(ct > 0) {
 			rc.broadcast((int)(Math.random()*GameConstants.BROADCAST_MAX_CHANNELS), (int)(Math.random()*65535));
 			ct = ct - 1;
 		}
 		*/		
 	}
 	private static void battle_prep(Robot[] evil_robots){
 		Robot[] allies = rc.senseNearbyGameObjects(Robot.class, 1000000, rc.getTeam());
 		MapLocation me = rc.getLocation();
 		
 		for (int x = 0; x < battle_len; x++)
 			for (int y = 0; y < battle_len; y++) {
 				battle_allies[x][y] = 0;
 				battle_enemies[x][y] = 0;
 			}
 		
 		int c_x = me.x + battle_len / 2;
 		int c_y = me.y + battle_len / 2;
 		
 		/* encode allies & enemies into grid */
 		for (Robot ally: allies) {
 			try {
 				MapLocation it = rc.senseLocationOf(ally);
 				battle_allies[c_x - it.x][c_y - it.y] =  1;
 			} catch (Exception e) {}
 		}
 		
 		for (Robot r: evil_robots) {
 			try {
 				MapLocation it = rc.senseLocationOf(r);
 				battle_enemies[c_x - it.x][c_y - it.y] |=  1;
 			} catch (Exception e) {}
 		}
 		System.out.println(" OMG ENEMY " + evil_robots.length);
 	}
 	
 	private static void do_battle() throws GameActionException {
 		MapLocation me = rc.getLocation();
 		
 		/* Decide where to move */
 		for (int i = 0; i < battle_len; i++) {
 			for (int j = 0; j < battle_len; j++) {
 				int good = 0;
 				try { good += battle_allies[i-1][j  ]; } catch (Exception e) {}
 				try { good += battle_allies[i-1][j-1]; } catch (Exception e) {}
 				try { good += battle_allies[i-1][j+1]; } catch (Exception e) {}
 				try { good += battle_allies[i  ][j-1]; } catch (Exception e) {}
 				try { good += battle_allies[i  ][j+1]; } catch (Exception e) {}
 				try { good += battle_allies[i+1][j  ]; } catch (Exception e) {}
 				try { good += battle_allies[i+1][j-1]; } catch (Exception e) {}
 				try { good += battle_allies[i+1][j+1]; } catch (Exception e) {}
 				
 				battle_good[i][j] = good;
 				
 				int bad = 0;
 				try { bad += battle_enemies[i-1][j  ]; } catch (Exception e) {}
 				try { bad += battle_enemies[i-1][j-1]; } catch (Exception e) {}
 				try { bad += battle_enemies[i-1][j+1]; } catch (Exception e) {}
 				try { bad += battle_enemies[i  ][j-1]; } catch (Exception e) {}
 				try { bad += battle_enemies[i  ][j+1]; } catch (Exception e) {}
 				try { bad += battle_enemies[i+1][j  ]; } catch (Exception e) {}
 				try { bad += battle_enemies[i+1][j-1]; } catch (Exception e) {}
 				try { bad += battle_enemies[i+1][j+1]; } catch (Exception e) {}
 				
 				battle_bad[i][j] = bad;
 			}
 		}
 		
 		int best_good = battle_good[battle_center][battle_center];
 		int best_x = 0;
 		int best_y = 0;
 		
 		int retreat_good = battle_good[battle_center][battle_center];
 		int retreat_bad  = battle_bad[battle_center][battle_center];
 		int retreat_x = 0;
 		int retreat_y = 0;
 		for (int i = 0; i < battle_len; i++)
 			for (int j = 0; j < battle_len; j++) {
 				if (battle_allies[i][j] != 0 || battle_enemies[i][j] != 0)
 					continue;
 				int good = battle_good[i][j];
 				int bad = battle_bad[i][j];
 			
 				if (best_good < good) {
 					if (bad > 0) {
 						best_x = i;
 						best_y = j;
 						best_good = good;
 					}
 				}
 				
 				if (retreat_good < good) {
 					if (retreat_bad > bad) {
 						retreat_good = good;
 						retreat_bad = bad;
 						retreat_x = i;
 						retreat_y = j;
 					}
 				}
 			}
 		
 		if (best_good > retreat_good) {
 			//rc.move(me.directionTo(me.add(best_x - battle_center, best_y - battle_center)));
 			goToLocation(me.add(best_x - battle_center, best_y - battle_center));
 		} else {
 			//rc.move(me.directionTo(me.add(retreat_x - battle_center, retreat_y - battle_center)));
 			goToLocation(me.add(retreat_x - battle_center, retreat_y - battle_center));
 		}
 	}
 	
 	private static boolean handle_battle() throws GameActionException {
 		Robot[] en = rc.senseNearbyGameObjects(Robot.class, 14, rc.getTeam().opponent());
 		if (en.length != 0) {
 			battle_prep(en);
 			do_battle();
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	private static boolean build_encampment() {
 		boolean succ = true;
 		try {
 			double r = new java.util.Random(rc.getRobot().getID()).nextDouble();
 			if (r > 0.2) {
 				rc.captureEncampment(RobotType.SUPPLIER);
 			} else {
 				rc.captureEncampment(RobotType.GENERATOR);
 			}
 		} catch (GameActionException e) {
 			succ = false;
 		}
 
 		return succ;
 	}
 	
 	private static void r_soilder_capper() {
 		MapLocation camp_goal = null;
 		while(true) {
 			try {
 				if (rc.isActive()) {
 					if (handle_battle()) {
 						MapLocation my_loc = rc.getLocation();
 						
 						/* See if we've captured the encampment since the last turn */
 						try {
 							if (rc.senseObjectAtLocation(my_loc).getTeam() == my_team)
 								camp_goal = null;
 						} catch (GameActionException e) {}
 						
 						if (camp_goal == null) {
 							camp_goal = find_closest_neutral_camp();
 						}
 						
 						if (my_loc.equals(camp_goal)) {
 							if (build_encampment())
 								camp_goal = null;
						} else if (rc.senseObjectAtLocation(camp_goal).getTeam() == my_team) {
							r_soilder_assault(); /* The cappers spin in cicles when there's a guy already on the spot trying to cap it. This attempted to fix it and failed. Now no cappers spawn.*/
 						} else {	
 							goToLocation(camp_goal);
 						}
 					}
 				} else {
 					jamm_coms(rc, 5);
 				}
 			} catch (GameActionException e) {
 				e.printStackTrace();
 			}
 			rc.yield();
 		}
 	}
 
 	private static void r_soilder_random_layer() {
 		while(true) {
 			try {
 				if (rc.isActive()) {
 					if (handle_battle()) {
 						MapLocation my_loc = rc.getLocation();
 						double r = Math.random();
 						if (r < 0.5 && rc.senseEncampmentSquare(my_loc)) {
 							rc.captureEncampment(RobotType.SUPPLIER);
 						} else if (r<0.051 && rc.senseMine(my_loc) == null) {
 							rc.layMine();
 						} else { 		
 							random_careful_move();
 						}
 					}
 				} else {
 					jamm_coms(rc, 5);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			rc.yield();
 		}
 	}
 	
 	
 	private static boolean assaulting = false;
 	private static boolean should_clump() {
 		if (assaulting)
 			return false;
 		if (Clock.getRoundNum() > 200)
 			assaulting = ((Clock.getRoundNum() / 100) % 2) == 0;
 		return !assaulting;
 	}
 
 
 	private static void goToLocation(MapLocation whereToGo) throws GameActionException {
 		int dist = rc.getLocation().distanceSquaredTo(whereToGo);
 		if (dist>0&&rc.isActive()){
 			Direction dir = rc.getLocation().directionTo(whereToGo);
 			int[] directionOffsets = {0,1,-1,2,-2};
 			Direction lookingAtCurrently = null;
 			lookAround: for (int d:directionOffsets){
 				lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
 				if(rc.canMove(lookingAtCurrently)){
 					moveOrDefuse(lookingAtCurrently);
 					break lookAround;
 				}
 			}
 		}
 	}
 	
 	
 	/* FIXME: some assault units max out bytecode usage. */
 	private static void r_soilder_assault() {
 		MapLocation rally_point = new MapLocation((hq.x * 2 + enemy_hq.y)/3, (hq.y * 2+ enemy_hq.y)/3);
 		while(true) {
 			try {
 				if (rc.isActive()) {
 					if (handle_battle()) {
 						// CLUMP then ATTACK.
 						if (should_clump()) {
 							goToLocation(rally_point);
 						} else {
 							goToLocation(enemy_hq);
 						}
 					}
 				} else {
 					jamm_coms(rc, 5);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			rc.yield();
 		}
 	}
 	
 	//private static boolean try_in_the_general_direction(RobotController rc, Direction d, Functo)
 	
 	/* mill around the HQ. */
 	private static void r_soilder_guard() {
 		MapLocation goal = hq;
 		while(true) {
 			try {
 				if (rc.isActive()) {
 					MapLocation my_loc = rc.getLocation();
 					double dist = my_loc.distanceSquaredTo(goal);
 					if (dist > 15) {
 						Direction dir = my_loc.directionTo(goal);
 						if (rc.canMove(dir)) {
 							careful_move(dir);
 						} else {
 							random_careful_move();
 						}
 					} else if (dist <= 2) {
 						Direction dir = my_loc.directionTo(goal).opposite();
 						if (rc.canMove(dir)) {
 							careful_move(dir);
 						} else {
 							random_careful_move();
 						}
 					} else {
 						if (Math.random() > 0.5) {
 							rc.layMine();
 						} else {
 							random_careful_move();
 						}
 					}
 				} else {
 					jamm_coms(rc, 5);
 				}
 			} catch (GameActionException e) {
 				e.printStackTrace();
 			}
 			rc.yield();
 		}
 	}
 	
 	private static void r_hq() {
 		while(true) {
 			try {
 				if (rc.isActive()) {
 					// We probably just finished spawning a solder.
 					// Can we keep track of it?
 
 					Direction dir = rc.getLocation().directionTo(enemy_hq);
 					if (rc.canMove(dir)) {
 						rc.spawn(dir);
 					} else {
 						Direction dnextup   = dir;
 						Direction dnextdown = dir;
 						for(int rot_count = 0; rot_count <= 4; rot_count = rot_count+1) {
 							dnextup   = dnextup.rotateLeft();
 							dnextdown = dnextdown.rotateRight();
 							if (rc.canMove(dnextup)) {
 								rc.spawn(dnextup);
 								break;
 							} else if (rc.canMove(dnextdown)) {
 								rc.spawn(dnextdown);
 								break;
 							}
 						}
 					}
 				} else {
 					jamm_coms(rc, 5);
 				}
 			} catch (GameActionException e) {
 				e.printStackTrace();
 			}
 			rc.yield();
 		}
 	}
 	
 	/* Jammer */
 	private static void r_other(RobotController rc) {
 		System.out.println("r_other: robot type = " + rc.getType());
 		while(true) {
 			try {
 				jamm_coms(rc, 5);
 			} catch (GameActionException e) {
 				e.printStackTrace();
 			}
 			rc.yield();
 		}
 	}
 	
 	public static void run(RobotController rc_) {
 		rc = rc_;
 		hq = rc.senseHQLocation();
 		enemy_hq = rc.senseEnemyHQLocation();
 		my_team = rc.getTeam();
 		
 		RobotType rt = rc.getType();
 		if (rt == RobotType.HQ) {
 			name = "HQ";
 			r_hq();
 		} else if (rt == RobotType.SOLDIER) {
 			while(true) {
 				int i = rc.getRobot().getID() % 10;
 				if (i >= 4) {
 					name = "Assault";
 					rc.setIndicatorString(0, name);
 					r_soilder_assault();
 				} else if (i >= 2) {
 					name = "Capper";
 					rc.setIndicatorString(0, name);
 					r_soilder_capper();
 				} else {
 					name = "Wanderer";
 					rc.setIndicatorString(0, name);
 					r_soilder_random_layer();
 				}
 			}
 		} else {
 			r_other(rc);
 		}
 	}
 }
