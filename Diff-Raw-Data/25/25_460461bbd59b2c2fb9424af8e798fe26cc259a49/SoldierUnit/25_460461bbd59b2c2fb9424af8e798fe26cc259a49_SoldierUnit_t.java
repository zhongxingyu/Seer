 package team063;
 
 import team063.message.Message;
 import battlecode.common.Clock;
 import battlecode.common.Direction;
 import battlecode.common.GameActionException;
 import battlecode.common.MapLocation;
 import battlecode.common.Robot;
 import battlecode.common.RobotController;
import battlecode.engine.instrumenter.lang.System;
 
 public class SoldierUnit extends BaseUnit {
 	private SoldierState state;
 	private MapLocation targetLoc = myBaseLoc;
 	private int squadId;
 	private MapLocation curLoc;
	private int starting;
	private int finish;
 	
 	public SoldierUnit(RobotController rc) {
 		super(rc);
 		state = SoldierState.DEFEND_POSITION;
 	}
 
 	@Override
 	public void run() throws GameActionException {
 		/**
 		 * 1. read broadcast
 		 * 2. switch state of necessary
 		 * 3. act upon state
 		 */
 		//readbroadcast(channelNum)
 		int unitMsg = rc.readBroadcast(getUnitChannelNum());
 		int squadMsg = rc.readBroadcast(getSquadChannelNum());
 		int allUnitMsg = rc.readBroadcast(getAllUnitChannelNum());
 		this.curLoc = rc.getLocation();
 		
 		switch(state) {
 		case BRUTE_MOVE:
 			this.goToLocationBrute(this.enemyBaseLoc);
 			break;
 		case SMART_MOVE:
 			break;
 		case ATTACK_MOVE:
 			break;
 		case PATROL:
 			break;
 		case SCOUT:
 			break;
 		case CAPTURE_MOVE:
 			break;
 		case DEFEND_POSITION:
			this.starting=Clock.getBytecodeNum();
 			defendPosition(targetLoc);
			this.finish=Clock.getBytecodeNum();
 			break;
 		case BATTLE:
 			break;
 		case CHASE_AND_DESTROY:
 			break;
 		case SEEK_AND_DESTROY_GUERILLA:
 			break;
 		case SECURE_ENCAMPMENT:
 			break;
 		default:
 			//do nothing if no instructions from hq
 			break;
 		}
 	}
 
 	@Override
 	public void decodeMsg(int encodedMsg) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	protected void defendPosition(MapLocation defendPoint) throws GameActionException{
 		Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 25, otherTeam);
 		if (nearbyEnemies.length >= 1){
 			if (rc.senseNearbyGameObjects(Robot.class,4,myTeam).length <2){
				rc.setIndicatorString(0,"not enough neraby allies to fight!");
 				this.goToLocationBrute(defendPoint);
 			}
 			else if (curLoc.distanceSquaredTo(defendPoint)<=49) {
				rc.setIndicatorString(0,"attacking nearby enemy!");
				this.goToLocationBrute(rc.senseRobotInfo(nearbyEnemies[0]).location);
 			}
 			else {
				rc.setIndicatorString(0,"enemy is too far away to chase!");
 				this.goToLocationBrute(defendPoint);
 			}
 		} else {
 			MapLocation nearbyMine= this.senseAdjacentMine();
 			if (!(rc.senseMine(nearbyMine) == myTeam || rc.senseMine(nearbyMine)==null)){
 				rc.setIndicatorString(0,"mine detected at " + nearbyMine.x +" "+ nearbyMine.y);
 				rc.defuseMine(nearbyMine);
 				rc.yield();
 			} else if (rc.senseMine(curLoc)==null && (curLoc.x*2 + curLoc.y)%5 ==1){
 				rc.setIndicatorString(0,"laying mine");
 				rc.layMine();
 				rc.yield();
 			} else if (curLoc.distanceSquaredTo(defendPoint)<=25){
 				rc.setIndicatorString(0,"moving randomly");
 				double randomIndex=Math.random()*8;
 				rc.move(Direction.values()[(int) randomIndex]);
 				rc.yield();
 			} else {
 				rc.setIndicatorString(0,"returning to defend point");
 				this.goToLocationBrute(defendPoint);
 			}
 		}
 	}
 	
 }
