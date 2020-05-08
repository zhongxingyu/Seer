 package bobot;
 
 import battlecode.common.*;
 
 // TODO: add an ability to self-destruct if another base type is needed badly
 public class Bases
 {
 
     static final int MAX_ROBOTS = 30;
     static final double ARTY_THRESHOLD = 10.0;
 
     private static void artillery(RobotController rc) throws GameActionException
     {
         Robot enemies[] = rc.senseNearbyGameObjects(
                 Robot.class, RobotType.ARTILLERY.attackRadiusMaxSquared, 
                 rc.getTeam().opponent());
 
         // Set an initial target at the enemy HQ
         // The score is 1.5 the amount of a single enemy bot
         MapLocation target = rc.senseEnemyHQLocation();
         double targetScore = Weights.ENEMY_HQ_TARGET_VALUE;
 
         int steps = Math.max(1, enemies.length / MAX_ROBOTS);
 
         for (int i = 0; i < enemies.length; i += steps) {
             MapLocation pos = rc.senseRobotInfo(enemies[i]).location;
 
             Robot robots[] = rc.senseNearbyGameObjects(
                     Robot.class, pos, 1, null);
 
             double score = 0;
             for (int j = 0; j < robots.length; ++j)
                 score += rc.senseRobotInfo(robots[j]).energon;
 
             if (score > targetScore) {
                 targetScore = score;
                 target = pos;
             }
         }
 
         if (target != null) {
             String str = "target=" + target.toString() + ", score=" + targetScore;
             rc.setIndicatorString(0, str);
 
             if (targetScore >= ARTY_THRESHOLD) {
                 if (rc.canAttackSquare(target)) rc.attackSquare(target);
             }
         }
     }
 
     private static void shields(RobotController rc) 
             throws GameActionException {
        int cast = rc.getLocation().x * 100 + rc.getLocation().y;
         Communication.broadcast(SenseCache.SHIELDS_CHANNEL, cast);
         Communication.broadcast(SenseCache.NUM_SHIELDS, 1);
     }
 
     private static void milBroadcast(RobotController rc) 
             throws GameActionException {
         int others = Communication.readBroadcast(SenseCache.MIL_CHANNEL, false);
         if (others == -1 || 
 
            Utils.distTwoPoints( (others - others % 100)/100, 
            others % 100, 
             rc.senseEnemyHQLocation().x,
             rc.senseEnemyHQLocation().y ) 
             <
             Utils.distTwoPoints(rc.getLocation(), rc.senseHQLocation()))
         {
            int cast = rc.getLocation().x + rc.getLocation().y * 100;
             Communication.broadcast(SenseCache.MIL_CHANNEL, cast);
         }
 
         int n = Communication.readBroadcast(SenseCache.NUM_MIL, false);
         n++;
         if (n == 0)
             n = 1;
         Communication.broadcast(SenseCache.NUM_MIL, n);
     }
 
     public static void run(RobotController rc) throws GameActionException
     {
         boolean isArty = rc.getType() == RobotType.ARTILLERY;
         boolean isShields = rc.getType() == RobotType.SHIELDS;
         boolean isMedbay = rc.getType() == RobotType.MEDBAY;
 
         while (true) {
 
             Math.random();
 
             if (!rc.isActive()) { rc.yield(); continue; }
 
             if (isArty) artillery(rc);
 
             if (isShields) shields(rc);
 
             if (isMedbay || isArty) milBroadcast(rc);
 
             rc.yield();
         }
     }
 }
