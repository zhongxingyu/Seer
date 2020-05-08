 package team063;
 
 import battlecode.common.GameActionException;
 import battlecode.common.RobotController;
 
 //singleton
 public class HQUnit extends BaseUnit {
 	public int curSoldiers=0;
 	
 	public HQUnit(RobotController rc) {
 		super(rc);
 	}
 
 	@Override
 	public void run() throws GameActionException {
 		if (this.rc.isActive()) {
			if (this.rc.canMove(this.enemyBaseDir) && curSoldiers <=5) {
 				this.rc.spawn(this.enemyBaseDir);
 				curSoldiers+=1;
 			}
 		}
 	}
 
 	@Override
 	public void decodeMsg(int encodedMsg) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
