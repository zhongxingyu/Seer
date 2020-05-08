 package team161;
 
 import battlecode.common.Clock;
 import battlecode.common.Direction;
 import battlecode.common.GameActionException;
 import battlecode.common.GameConstants;
 import battlecode.common.MapLocation;
 import battlecode.common.Robot;
 import battlecode.common.RobotController;
 import battlecode.common.RobotInfo;
 import battlecode.common.RobotType;
 import battlecode.common.Team;
 
 public class Bug {
     public MapLocation target;
     public static RobotController rc;
     public Direction prev;
     public static int distTravelled;
     private static int turnDir;
     private static int depth = 1;
     private static Direction dir2target;
     private static MapLocation stuck;
     private static boolean burrow;
     private static int dumb = 0;
     private static int dist_btw_HQs;
     private static int initTurn;
 	
     public Bug(MapLocation target_, RobotController rc_) {
         target = target_;
         rc = rc_;
         prev = rc.getLocation().directionTo(target);
         distTravelled = 0;
         dir2target = rc.getLocation().directionTo(target);
         stuck = null;
         burrow = false;
         dist_btw_HQs = (int) Math.sqrt(rc.getLocation().distanceSquaredTo(rc.senseEnemyHQLocation()));
         if (Clock.getRoundNum()/10 % 7 < 4) initTurn = 7;
         else initTurn = 1;
         turnDir = initTurn;
     }
     private static boolean hasMine(MapLocation loc) {
         Team t = rc.senseMine(loc);
         if (t == rc.getTeam().opponent() || t == Team.NEUTRAL) return true;
         return false;
     }
     public void retreat(Robot r) throws GameActionException {
         RobotInfo info = rc.senseRobotInfo(r);
         MapLocation loc = info.location;
         if (info.roundsUntilMovementIdle != 0 && info.type == RobotType.SOLDIER) {
             Direction dir = rc.getLocation().directionTo(loc);
             if (rc.canMove(dir) && !hasMine(rc.getLocation().add(dir))) rc.move(dir);
             return;
         }
         if (info.robot.getID() > rc.getRobot().getID()) return;
         Direction dir = rc.getLocation().directionTo(loc).opposite();
         if (rc.canMove(dir) && !hasMine(rc.getLocation().add(dir))) rc.move(dir);
         else if (rc.canMove(dir.rotateLeft()) && !hasMine(rc.getLocation().add(dir.rotateLeft()))) rc.move(dir.rotateLeft());
         else if (rc.canMove(dir.rotateRight()) && !hasMine(rc.getLocation().add(dir.rotateRight()))) rc.move(dir.rotateRight());
         else if (rc.canMove(dir) && hasMine(rc.getLocation().add(dir))) defuse(rc, dir);
         else if (rc.canMove(dir.rotateLeft()) && hasMine(rc.getLocation().add(dir.rotateLeft()))) defuse(rc, dir.rotateLeft());
         else if (rc.canMove(dir.rotateRight()) && hasMine(rc.getLocation().add(dir.rotateRight()))) defuse(rc, dir.rotateRight());		
     }
     private static void defuse(RobotController rc, Direction dir) throws GameActionException {
         rc.defuseMine(rc.getLocation().add(dir));
         rc.yield();rc.yield();rc.yield(); rc.yield();rc.yield();rc.yield();rc.yield();rc.yield();rc.yield();rc.yield();rc.yield();rc.yield();
     }
 	
     public void shieldGo() throws GameActionException {
         if (rc.getLocation().distanceSquaredTo(target) > 49) go();
         else if (rc.getLocation().equals(target)) return;
         else {
             dir2target = rc.getLocation().directionTo(target);
             if (rc.canMove(dir2target)) rc.move(dir2target);
             else if (rc.canMove(dir2target.rotateLeft())) rc.move(dir2target.rotateLeft());
             else if (rc.canMove(dir2target.rotateRight())) rc.move(dir2target.rotateRight());
         }
     }
     public void go() throws GameActionException {
         if (rc.getLocation().distanceSquaredTo(target) == 0) return;
         if (rc.getLocation().distanceSquaredTo(target) <= 2 && rc.senseEnemyHQLocation().equals(target)) return;
         if (rc.getLocation().distanceSquaredTo(target) > 81) {
             if (Clock.getRoundNum() > 400) {
                 Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, 18, rc.getTeam().opponent());
                 Robot[] friends = rc.senseNearbyGameObjects(Robot.class, 8, rc.getTeam());
                 if (enemies.length > 0 && friends.length < 3) {
                     retreat(enemies[0]); //WANT TO RETREAT ONLY IF OUR ROBOT ID IS LESS THAN ENEMIES. else they do damage to us and we don't do any damage to them.
                     return;
                 }
             }
         }
         rc.setIndicatorString(1, "turnDir" + turnDir);
         dir2target = rc.getLocation().directionTo(target);
         if (burrow) {
             MapLocation newloc = rc.getLocation();//.add(dir2target);
             if (!hasMine(newloc.add(dir2target)) ||
                 !hasMine(newloc.add(dir2target.rotateLeft())) ||
                 !hasMine(newloc.add(dir2target.rotateRight()))) {
                 burrow = false;
                 return;
             }
             rc.setIndicatorString(0, "burrowing");
             defuse(rc, dir2target);
             if (rc.canMove(dir2target)) {
                 rc.move(dir2target);
                 return;
             }
         }
         if (rc.getLocation().equals(stuck)) {
             burrow = true;
         }
         MapLocation loc = rc.getLocation();
         Direction dir = loc.directionTo(target);
         dir = turn(dir, 8);
         int back = prev.opposite().ordinal();
         if (dir.ordinal() == back ||
             dir.rotateLeft().ordinal() == back ||
             dir.rotateRight().ordinal() == back) {
             rc.setIndicatorString(0, "toggled dir. prev = " + prev + " dir = " + dir);
             toggleDirection();
             if (turnDir == initTurn) stuck = rc.getLocation(); //BROADCAST STUCK SPOTS!!!
             dir = turn(dir2target, 8);
         }
         if (dir.opposite().rotateLeft() == dir2target || dir.opposite().rotateRight() == dir2target) dumb ++;
         if (dir.opposite() == dir2target ||
             (( dir.opposite().rotateLeft() == dir2target || dir.opposite().rotateRight() == dir2target) && dumb > dist_btw_HQs/10)) {
             depth = 3;
             toggleDirection();
             if (turnDir == initTurn) {
                 stuck = rc.getLocation();
             }
         }
         dir = turn(dir2target, 8);
         if (rc.canMove(dir)) {
             rc.move(dir);
             prev = dir;
             distTravelled ++;	//if dist travelled > mineRatio * timeToDefuseMine * distBtwHeadquarters, broadcast shit
             rc.setIndicatorString(0, "depth = " + depth);
         }
     }
     private static void toggleDirection() {
         if (turnDir == 1) turnDir = 7;
         else turnDir = 1;
     }
     private static Direction turn(Direction dir, int iterations) throws GameActionException {
         if (rc.canMove(dir)) {
             MapLocation newloc = rc.getLocation().add(dir);
             if (hasMine(newloc)) {
                 if (thinEnough(dir, newloc) || iterations == 0) {
                     defuse(rc, dir);
                     if (rc.getLocation().equals(stuck)) burrow = true;
                     toggleDirection();
                     return dir;
                 } else {
                     depth = 1;
                     return turn(Direction.values()[(dir.ordinal()+turnDir) % 8], iterations-1);
                 }
             } else {
                 depth = 1;
                 return dir;
             }
         } else {
             depth = 1;
             return turn(Direction.values()[(dir.ordinal()+turnDir) % 8], iterations-1);
         }
     }
     private static boolean thinEnough(Direction dir, MapLocation loc) {
         for(int i = 1; i < depth; i++) if (!hasMine(loc.add(dir, i))) return true;
         return false;
     }
 	
 }
