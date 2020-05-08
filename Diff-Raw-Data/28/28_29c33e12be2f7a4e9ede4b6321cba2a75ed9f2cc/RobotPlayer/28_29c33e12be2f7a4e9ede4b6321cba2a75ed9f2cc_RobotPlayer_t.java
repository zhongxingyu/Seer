 package confusedteam;
 
 import battlecode.common.Direction;
 import battlecode.common.GameConstants;
 import battlecode.common.RobotController;
 import battlecode.common.RobotType;
 import battlecode.common.*;
 import java.util.*;
 
 /** The example funcs player is a player meant to demonstrate basic usage of the most common commands.
  * Robots will move around randomly, occasionally mining and writing useless messages.
  * The HQ will spawn soldiers continuously. 
  */
 public class RobotPlayer {
 	public static int arrIndexOf(Object[] objs, Object o){
 		for(int i = 0; i < objs.length; i++){
 			if(objs[i].equals(o))
 				return i;
 		}
 		return -1;
 	}
 	
 	public static void run(RobotController rc) {
 		while (true) {
 			try {
 				if (rc.getType() == RobotType.HQ) {
 					if (rc.isActive()) {
 						if(rc.senseEnemyNukeHalfDone()){
 							rc.broadcast(1354, 1000);
 						}
 						else{
 							rc.broadcast(1354, 100);
 						}
 						if((int)rc.getEnergon() < rc.readBroadcast(1257)){
 							rc.broadcast(1489, 1000);
 						}
 						else{
 							rc.broadcast(1489, 100);
 						}
 						rc.broadcast(1257, (int)rc.getEnergon());
 //						Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, 100, rc.getTeam().opponent());
 //						System.out.println(enemies.length + " enemies found");
 //						if(enemies.length > 0){
 //							MapLocation loc = rc.senseRobotInfo(enemies[0]).location;
 //							System.out.println("x: " + loc.x + " y: " + loc.y);
 //						}
 						/*if(rc.checkResearchProgress(Upgrade.PICKAXE) < Upgrade.PICKAXE.numRounds && Math.random() > 0.25){
 							rc.researchUpgrade(Upgrade.PICKAXE);
 						}
 						else*/ if(rc.getTeamPower() > 25 && Math.random() < 0.4){
 							// Spawn a soldier
 							Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
 							if(rc.senseMineLocations(rc.getLocation(), 2, rc.getTeam()).length < 8){
 								for(Direction d : Direction.values()){
 									if(rc.senseMine(rc.getLocation().add(d))==null){
 										dir = d;
 										break;
 									}
 								}
 							}
							int i = 0;
							while(i < 10 && (!rc.canMove(dir) || (dir.equals(Direction.NONE) || dir.equals(Direction.OMNI)))){
 								dir = Direction.values()[(int)(Math.random()*8)];
								i++;
 							}
 							if (rc.canMove(dir)){
								System.out.println("spawning soldier");
 								rc.spawn(dir);
 							}
							else{
								System.out.println("researching nuke cuz cant spawn");
 								rc.researchUpgrade(Upgrade.NUKE);
							}
 						}
 						else{
							System.out.println("researching nuke");
 							rc.researchUpgrade(Upgrade.NUKE);
 						}
 					}
 				} else if (rc.getType() == RobotType.SOLDIER) {
 					if (rc.isActive()) {
 						if(rc.senseEncampmentSquare(rc.getLocation()) && rc.getTeamPower() > rc.senseCaptureCost()){
 							RobotType[] far = {RobotType.GENERATOR, RobotType.SUPPLIER, RobotType.SUPPLIER, RobotType.ARTILLERY};
 							RobotType[] close = {RobotType.ARTILLERY};
 							RobotType[] mid = {RobotType.ARTILLERY};
 //							RobotType[] mid = Arrays.copyOf(close, close.length);
 							MapLocation loc = rc.getLocation();
 							int hqDist = loc.distanceSquaredTo(rc.senseEnemyHQLocation());
 							int totalDist = rc.senseHQLocation().distanceSquaredTo(rc.senseEnemyHQLocation());
 							if(hqDist < totalDist/3 && rc.getTeamPower() > rc.senseCaptureCost()){
 								rc.captureEncampment(close[(int)(Math.random()*close.length)]);
 							}
 							else if(hqDist < totalDist*2/3 && rc.getTeamPower() > rc.senseCaptureCost()){
 								rc.captureEncampment(mid[(int)(Math.random()*mid.length)]);
 							}
 							else if(rc.getTeamPower() > rc.senseCaptureCost()){
 								rc.captureEncampment(far[(int)(Math.random()*far.length)]);
 							}
 						}
 						else {
 							// Choose a random direction, and move that way if possible
 							MapLocation loc = rc.getLocation();
 //							int hqDist = rc.senseEnemyHQLocation().distanceSquaredTo(loc);
 							MapLocation target;
 							Direction dir;
 							Robot me = (Robot)rc.senseObjectAtLocation(loc);
							if(me.getID()%3==0 || (rc.getTeamPower() > GameConstants.BROADCAST_READ_COST && rc.readBroadcast(1354) > 500)){
								target = rc.senseEnemyHQLocation();
								dir = loc.directionTo(target);
							}
							else if(me.getID()%3 == 2 || (rc.getTeamPower() > GameConstants.BROADCAST_READ_COST && rc.readBroadcast(1257) > 500)){
 								target = rc.senseHQLocation().add(rc.senseHQLocation().directionTo(rc.senseEnemyHQLocation()));
 								dir = loc.directionTo(target);
 								if (rc.senseMine(rc.getLocation()) == null && Math.random()<0.5) {
 									// Lay a mine 
 									//							if(rc.senseMine(rc.getLocation())==null)
 									rc.layMine();
 									continue;
 								}
 								else if(rc.senseMineLocations(rc.senseHQLocation(), 2, rc.getTeam()).length < 8){
 									for(Direction d : Direction.values()){
 										if(rc.senseMine(rc.senseHQLocation().add(d))==null){
 											System.out.println("mine found missing in direction " + dir);
 											dir = loc.directionTo(rc.senseHQLocation().add(d));
 											break;
 										}
 									}
 								}
 							}
 							else{
 								target = rc.senseEnemyHQLocation();
 								dir = loc.directionTo(target);
 								for(MapLocation camp : rc.senseEncampmentSquares(loc, 14, Team.NEUTRAL)){
 									if(camp.distanceSquaredTo(loc) < loc.distanceSquaredTo(target)){
 										dir = loc.directionTo(camp);
 										target = camp;
 									}
 								}
 							}
 							
 							target = rc.getLocation().add(dir);
 							if(!dir.equals(Direction.NONE) && !dir.equals(Direction.OMNI) && rc.canMove(dir)){
 								if(rc.senseMine(target) == null || rc.senseMine(target).equals(rc.getTeam())) {
 									rc.move(dir);
 									rc.setIndicatorString(0, "Last direction moved: "+dir.toString());
 								}
 								else if(!rc.senseMine(target).equals(rc.getTeam())){
 									rc.defuseMine(target);
 								}
 							}
 							else{
 								do{
 									dir = Direction.values()[(int)(Math.random()*8)];
 								}
 								while(!rc.canMove(dir) || (dir.equals(Direction.NONE) || dir.equals(Direction.OMNI)));
 
 								target = rc.getLocation().add(dir);
 								if(rc.canMove(dir) && (rc.senseMine(target) == null || rc.senseMine(target).equals(rc.getTeam()))) {
 									rc.move(dir);
 									rc.setIndicatorString(0, "Last direction moved: "+dir.toString());
 								}
 								else if(!rc.senseMine(target).equals(rc.getTeam())){
 									rc.defuseMine(target);
 								}
 							}
 						}
 					}
 
 //					if (Math.random()<0.01 && rc.getTeamPower()>5) {
 //						// Write the number 5 to a position on the message board corresponding to the robot's ID
 //						rc.broadcast(rc.getRobot().getID()%GameConstants.BROADCAST_MAX_CHANNELS, 5);
 //					}
 				}
 				else if(rc.getType() == RobotType.ARTILLERY){
 					if(rc.isActive()){
 						MapLocation target = rc.getLocation();
 						MapLocation loc = rc.getLocation();
 						Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, 63, rc.getTeam().opponent());
 //						Robot[] friends = rc.senseNearbyGameObjects(Robot.class, 63, rc.getTeam());
 						MapLocation[] enemyLocs = new MapLocation[enemies.length];
 //						MapLocation[] friendLocs = new MapLocation[friends.length];
 						int avg = 0;
 						for(int i = 0; i < enemies.length; i++){
 							enemyLocs[i] = rc.senseRobotInfo(enemies[i]).location;
 							avg += enemyLocs[i].distanceSquaredTo(loc);
 						}
 						if(enemies.length > 0){
 							avg /= enemies.length;
 							target = enemyLocs[0];
 						}
 						if(avg > 0)
 							for(MapLocation aim : enemyLocs){
 								if(Math.abs(avg - aim.distanceSquaredTo(loc)) < 100){
 									target = aim;
 								}
 							}
 						MapLocation[] enemyCamps = rc.senseEncampmentSquares(loc, 63, Team.NEUTRAL);
 						for(MapLocation l : enemyCamps){
 							if(rc.canSenseSquare(l)){
 								Robot camp = (Robot) rc.senseObjectAtLocation(l);
 								if(camp != null && !camp.getTeam().equals(rc.getTeam()) && arrIndexOf(enemyLocs, l) >= 0){
 									target = l;
 								}
 							}
 						}
 						if(arrIndexOf(enemyLocs, rc.senseEnemyHQLocation()) >= 0){
 							target = rc.senseEnemyHQLocation();
 						}
 //						Direction dir = target.directionTo(rc.senseEnemyHQLocation());
 //						for(int i = 0; i < 5; i++){
 //							target = target.add(dir);
 //							dir = target.directionTo(rc.senseEnemyHQLocation());
 //						}
 						System.out.println("artillery");
 						if(rc.canAttackSquare(target) && avg > 0)
 							rc.attackSquare(target);
 					}
 				}
 
 				// End turn
 				rc.yield();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
