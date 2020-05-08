 package team298;
 
 import battlecode.common.*;
 import static battlecode.common.GameConstants.*;
 import java.util.*;
 
 public class ChainerPlayer extends AttackPlayer {
 
     public ChainerPlayer(RobotController controller) {
         super(controller);
 
     }
 
     public void step() {
         MapLocation location = sensing.senseClosestArchon();
         int distance = location.distanceSquaredTo(controller.getLocation());
 
         if(energon.isEnergonLow() || energon.isFluxFull() || distance > 34) {
             navigation.changeToArchonGoal(true);
             if(distance < 3) {
                 energon.requestEnergonTransfer();
                 controller.yield();
             } else {
                 navigation.moveOnce(false);
             }
             return;
         }
 
         processEnemies();
         sortEnemies();
         EnemyInfo enemy = mode.getEnemyToAttack();
         if(enemy != null) {
             // attack
             if(!controller.canAttackSquare(enemy.location)) {
                 navigation.faceLocation(enemy.location);
                 processEnemies();
             }
             executeAttack(enemy.location, enemy.type.isAirborne() ? RobotLevel.IN_AIR : RobotLevel.ON_GROUND);
             processEnemies();
             attackLocation = enemy.location;
         } else {
             if(outOfRangeEnemies.size() > 0) {
                 // only move if we can do it in 1 turn or less
                 if(controller.getRoundsUntilMovementIdle() < 2) {
                     moveToAttack();
                 }
             } else {
                 navigation.changeToMoveableDirectionGoal(true);
                 navigation.moveOnce(true);
             }
         }
     }
 }
