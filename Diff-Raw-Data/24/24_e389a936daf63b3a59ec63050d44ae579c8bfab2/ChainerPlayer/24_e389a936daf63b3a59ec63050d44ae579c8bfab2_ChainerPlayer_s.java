 package team298;
 
 import battlecode.common.*;
 import static battlecode.common.GameConstants.*;
 import java.util.*;
 
 public class ChainerPlayer extends AttackPlayer {
 
     public NavigationGoal prevGoal;
     
     public ChainerPlayer(RobotController controller) {
         super(controller);
     }
 
     public void step() {
         MapLocation location = sensing.senseClosestArchon();
         int distance = location.distanceSquaredTo(controller.getLocation());
 
         //if the robot goes to get energon, then we need to save the followArchon goal for later
         if(prevGoal != null) prevGoal = navigation.goal;
 
 
         //always check if we got enough juice to go another round, if u know what i mean
         if(energon.isEnergonLow() || distance > 34) {
             navigation.changeToArchonGoal(true);
             ignoreFollowRequest = true;
             if(distance < 3) {
                 energon.requestEnergonTransfer();
                 controller.yield();
             } else {
                 navigation.moveOnce(false);
             }
             return;
         }
 
         //restore the follow request goal
         if(prevGoal != null) navigation.goal = prevGoal;
         prevGoal = null;
         ignoreFollowRequest = false;
 
         //find any enemey to attack.  mode.getEnemeyToAttack could return an out of range enemy too
         processEnemies();
         sortEnemies();
         EnemyInfo enemy = mode.getEnemyToAttack();
 
         if(enemy != null) {
             navigation.faceLocation(enemy.location);
             
             if(!controller.canAttackSquare(enemy.location)) {
                 navigation.changeToLocationGoal(enemy.location, false);
                 navigation.moveOnce(true);
                 navigation.popGoal();
                 processEnemies();
             }
             int status = executeAttack(enemy.location, enemy.type.isAirborne() ? RobotLevel.IN_AIR : RobotLevel.ON_GROUND);
            if(status == Status.success) p("take that bitch");
             processEnemies();
             attackLocation = enemy.location;
         } else {
             //navigation.changeToMoveableDirectionGoal(true);
             navigation.moveOnce(true);
         }
     }
 }
