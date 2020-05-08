 package team211;
 
 import com.sun.org.apache.bcel.internal.generic.BALOAD;
 
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
  */
 public class RobotPlayer {
 	final static int battle_len = 13;
 	final static int battle_center = 6;
 	
 	private static int [][] battle_allies = new int[battle_len][battle_len];
 	private static int [][] battle_enemies = new int[battle_len][battle_len];
 	private static int [][] battle_good = new int [battle_len][battle_len];
 	private static int [][] battle_bad = new int [battle_len][battle_len];
 	
 	private static void careful_move(RobotController rc, Direction dir, MapLocation my_loc, Team my_team) throws GameActionException {
 		if(rc.canMove(dir)) {
 			MapLocation new_loc = my_loc.add(dir);
 			Team maybe_mine = rc.senseMine(new_loc);
 			if (maybe_mine != null && maybe_mine != my_team) {
 				rc.defuseMine(new_loc);
 			} else {
 				rc.move(dir);
 				rc.setIndicatorString(0, "Last direction moved: " + dir);
 			}
 		}
 	}
 	
 	private static MapLocation find_closest_camp(RobotController rc) throws GameActionException {
 		int r_sq = 50;
 		MapLocation [] locs = null;
 		while (locs == null || locs.length == 0) {
 			locs = rc.senseEncampmentSquares(rc.getLocation(), r_sq, Team.NEUTRAL);
 			r_sq = 4 * r_sq;
 		}
 		return locs[(int)(Math.random() * locs.length)];
 	}
 	
 	private static void random_careful_move(RobotController rc, MapLocation my_loc, Team my_team)
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
 		
 		careful_move(rc, dir, my_loc, my_team);
 	}
 	
 	private static void careful_move_toward(RobotController rc, MapLocation goal, MapLocation my_loc, Team my_team)
 			throws GameActionException {
 		Direction dir = my_loc.directionTo(goal);
 		careful_move(rc, dir, my_loc, my_team);
 	}
 	
 	private static void jamm_coms(RobotController rc, int ct) throws GameActionException {
 		while(ct > 0) {
 			rc.broadcast((int)(Math.random()*GameConstants.BROADCAST_MAX_CHANNELS), (int)(Math.random()*65535));
 			ct = ct - 1;
 		}		
 	}
 	
 	private static void do_battle(RobotController rc, Robot[] evil_robots) throws GameActionException {
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
 		
 		/* Decide where to move */
 		System.out.println(" OMG ENEMY " + evil_robots.length);
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
 		for (int i = battle_center - 1; i <= battle_center + 1; i++)
 			for (int j = battle_center - 1; j <= battle_center + 1; j++) {
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
 			rc.move(me.directionTo(me.add(best_x, best_y)));
 		} else {
 			rc.move(me.directionTo(me.add(retreat_x, retreat_y)));
 		}
 	}
 	
 	private static boolean handle_battle(RobotController rc) throws GameActionException {
 		Robot[] en = rc.senseNearbyGameObjects(Robot.class, 1000000, rc.getTeam().opponent());
 		if (en.length != 0) {
 			do_battle(rc, en);
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	private static void r_soilder_capper(RobotController rc) {
 		Team my_team = rc.getTeam();
 		MapLocation camp_goal = null;
 		while(true) {
 			try {
 				if (rc.isActive()) {
 					if (handle_battle(rc)) {
 						MapLocation my_loc = rc.getLocation();
 						if (camp_goal == null) {
 							camp_goal = find_closest_camp(rc);
 						}
 						
 						if (my_loc.equals(camp_goal)) {
 							if (Math.random() > 0.5) {
 								rc.captureEncampment(RobotType.SUPPLIER);
 							} else {
 								rc.captureEncampment(RobotType.GENERATOR);
 							}
 							camp_goal = null;
 						} else {
 							/* look for closest unclaimed, unoccupied camp. */
 							Direction dir = my_loc.directionTo(camp_goal);
 							if (rc.canMove(dir)) {
 								careful_move(rc, dir, my_loc, my_team);
 							} else {
 								random_careful_move(rc, my_loc, my_team);
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
 
 	private static void r_soilder_random_layer(RobotController rc) {
 		Team my_team = rc.getTeam();
 		while(true) {
 			try {
 				if (rc.isActive()) {
 					if (handle_battle(rc)) {
 						MapLocation my_loc = rc.getLocation();
 						//boolean on_bad_mine = my_team != rc.senseMine(my_loc);
 						
 						if (Math.random()<0.005 && rc.senseMine(my_loc) == null) {
 							if(rc.getTeamPower() > 10)
 								rc.layMine();
 						} else { 		
 							random_careful_move(rc, my_loc, my_team);
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
 	
 	private static boolean should_clump(RobotController rc) {
 		return Clock.getRoundNum() < 300;
 	}
 	
 	private static MapLocation center_of_map(RobotController rc) {
 		return new MapLocation(rc.getMapHeight() / 2, rc.getMapWidth());
 	}
 	
 	private static MapLocation get_clump(RobotController rc) {
 		return center_of_map(rc);
 	}
 	
 	private static void r_soilder_assault(RobotController rc) {
 		Team my_team = rc.getTeam();
 		MapLocation enemy_hq = rc.senseEnemyHQLocation();
 		while(true) {
 			try {
 				if (rc.isActive()) {
 					if (handle_battle(rc)) {
 						// CLUMP then ATTACK.
 						MapLocation my_loc = rc.getLocation();
 						if (should_clump(rc)) {
 							MapLocation c = get_clump(rc);
 							careful_move_toward(rc, c, my_loc, my_team);
 						} else {
 							careful_move_toward(rc, enemy_hq, my_loc, my_team);
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
 	private static void r_soilder_guard(RobotController rc) {
 		Team my_team = rc.getTeam();
 		MapLocation goal = rc.senseHQLocation();
 		while(true) {
 			try {
 				if (rc.isActive()) {
 					MapLocation my_loc = rc.getLocation();
 					double dist = my_loc.distanceSquaredTo(goal);
 					if (dist > 15) {
 						Direction dir = my_loc.directionTo(goal);
 						if (rc.canMove(dir)) {
 							careful_move(rc, dir, my_loc, my_team);
 						} else {
 							random_careful_move(rc, my_loc, my_team);
 						}
 					} else if (dist <= 2) {
 						Direction dir = my_loc.directionTo(goal).opposite();
 						if (rc.canMove(dir)) {
 							careful_move(rc, dir, my_loc, my_team);
 						} else {
 							random_careful_move(rc, my_loc, my_team);
 						}
 					} else {
 						if (Math.random() > 0.5) {
 							rc.layMine();
 						} else {
 							random_careful_move(rc, my_loc, my_team);
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
 	
 	private static void r_hq(RobotController rc) {
 		while(true) {
 			try {
 				if (rc.isActive()) {
 					// We probably just finished spawning a solder.
 					// Can we keep track of it?
 					// Spawn a soldier
					if (rc.getTeamPower() > 10) {
 						Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
 						if (rc.canMove(dir)) {
 							rc.spawn(dir);
 						} else { 
 							Direction dnextup   = dir;
 							Direction dnextdown = dir;
							for(int rot_count=0; rot_count <= 4; rot_count = rot_count+1)
								Direction dnextup   = dnextup.rotateLeft();
								Direction dnextdown = dnextdown.rotateRight();
 									if (rc.canMove(dnextup)) {
 										rc.spawn(dnextup);
 									}
 									else if (rc.canMove(dnextdown)) {
 										rc.spawn(dnextdown);
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
 	
 	public static void run(RobotController rc) {
 		RobotType rt = rc.getType();
 		if (rt == RobotType.HQ) {
 			r_hq(rc);
 		} else if (rt == RobotType.SOLDIER) {
 			while(true) {
 				int i = rc.getRobot().getID() % 10;
 				if (i >= 8) {
 					r_soilder_assault(rc);
 				} else if (i >= 3) {
 					r_soilder_capper(rc);
 				} else {
 					r_soilder_guard(rc);
 				}
 			}
 		} else {
 			r_other(rc);
 		}
 	}
 }
