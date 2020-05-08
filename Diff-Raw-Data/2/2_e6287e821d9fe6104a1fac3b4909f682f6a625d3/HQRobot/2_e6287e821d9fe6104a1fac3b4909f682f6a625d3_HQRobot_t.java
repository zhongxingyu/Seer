 package turtlenuke;
 
 import battlecode.common.*;
 
 public class HQRobot extends BaseRobot {
 
   
   //@@ TurtleNuke strategy vars
   private static final int FORTIFY_RADIUS = 2; // May want something more sophisticated, e.g. layers
   private int stage = 0; // Strategy stage; have this?
 
   HQRobot(RobotController rc){
     super(rc);
   }
 
   private int turnsToPickaxe = Upgrade.PICKAXE.numRounds;
   public void run() throws GameActionException{
     if (rc.isActive()){
       if(stage == 0){
     	  rc.researchUpgrade(Upgrade.PICKAXE);
    	  if(--turnsToPickaxe == 0) stage = 1;
       } else if(stage == 1){
     	  Robot[] defenders = rc.senseNearbyGameObjects(Robot.class, 8, myTeam); //5x5; ## depends on FORTIFY_RADIUS
     	  if(defenders.length >= 16){
     		  rc.researchUpgrade(Upgrade.NUKE);
     	  }
     	  else {
     		  spawn(); // ## in actual strategy, will need to instruct new robot to defend
     	  }
       }
     }
   }
   
   private void spawn() throws GameActionException{
     // Spawn a soldier
     Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
 
     // Spawns soldier if possible to move
     if (rc.canMove(dir)) {
       rc.spawn(dir);
     }
     // Try to move around to spawn in different direction
     else {
       Direction dirLeft = dir;
       Direction dirRight = dir;
 
       while (dirRight != dirLeft) {
         dirLeft = dirLeft.rotateLeft();
         if (rc.canMove(dirLeft)) {
           rc.spawn(dirLeft);
           break;
         }
         else {
           dirRight = dirRight.rotateRight();
           if (rc.canMove(dirRight)) {
             rc.spawn(dirRight);
             break;
           }
         }
       }
     }
   }
 
 }
