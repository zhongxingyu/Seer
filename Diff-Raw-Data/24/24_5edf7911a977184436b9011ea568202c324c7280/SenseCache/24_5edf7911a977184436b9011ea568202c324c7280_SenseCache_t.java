 package bobot;
 
 import battlecode.common.*;
 
 /** Note that while this started as a caching mechanism for the sense call it
     doesn't do much of that right now. Right now it provides a nice and clean
     interface to the RobotController's sense interface and hides away some of
     the uglyness.
 
     While it can still do caching just fine, make sure its really worth it
     before adding it. Cache invalidation isn't free and if it's never actually
     reused then it's wasted. I recommend only doing caching when it was tested
     (use the ByteCode class) and confirmed that there's a saving to be had.
 
     Feel free to abstract any other ugly interfacs using this class.
  */
 public class SenseCache
 {
     public static final int SHIELDS_CHANNEL = 16661;
     public static final int MIL_CHANNEL = 16761;
     public static final int MIL_CAMP_NUM = 9871;
 
     public static final int NUM_SHIELDS = 55987;
     public static final int NUM_MIL = 48265;
 
     RobotController rc;
     double est_rush_time;
     int rush_calcs;
 
     MapLocation[] allEncampments = null;
     MapLocation[] alliedEncampments = null;
     MapLocation[] localEncampments = null;
     MapLocation[] neutralEncampments = null;
 
     double SLOPE;
     MapLocation MY_HQ;
     MapLocation ENEMY_HQ;
     double DISTANCE_BETWEEN_HQS;
 
 
     SenseCache(RobotController rc)
     {
         this.rc = rc;
         est_rush_time = 0;
         MY_HQ = rc.senseHQLocation();
         ENEMY_HQ = rc.senseEnemyHQLocation();
         SLOPE = (double)(MY_HQ.y - ENEMY_HQ.y) / (MY_HQ.x - ENEMY_HQ.x);
         DISTANCE_BETWEEN_HQS = Utils.distTwoPoints(MY_HQ, ENEMY_HQ);
     }
 
     public MapLocation rallyBroadcast() throws GameActionException
     {
         int signal = Communication.readBroadcast(SHIELDS_CHANNEL, true);
         if (signal != -1) {
            int y = signal % 100;
            return new MapLocation((signal-y)/100, y);
         }
         signal = Communication.readBroadcast(MIL_CHANNEL, true);
         if (signal != -1) {
            int y = signal % 100;
            return new MapLocation((signal-y)/100, y);
         }
         return null;
     }
 
     public void updateRushTime()
     {
         double x_dif = ENEMY_HQ.x - MY_HQ.x;
         double y_dif = ENEMY_HQ.y - MY_HQ.y;
         double x, y, offset;
         double time = 10.0;
 
         for (int i = 5; --i > 0;) {
             offset = 6 * Math.random() - 3;
             x = Math.random() * x_dif + MY_HQ.x;
             y = SLOPE * x + ENEMY_HQ.y;
 
             Team mine = rc.senseMine(new MapLocation((int)x, (int)y));
             if (mine == Team.NEUTRAL) time += 12;
         }
 
         time *= (DISTANCE_BETWEEN_HQS/10.0);
         time += DISTANCE_BETWEEN_HQS;
 
         if (est_rush_time == 0) {
             est_rush_time = time;
             rush_calcs = 1;
         }
         else {
             est_rush_time = ((est_rush_time * rush_calcs) + time) / (rush_calcs+1);
             ++rush_calcs;
         }
     }
 
     double sight()
     {
         return sightRadius;
     }
 
     boolean nonAlliedMine(MapLocation loc)
         throws GameActionException
     {
         Team mine = rc.senseMine(loc);
         return mine == Team.NEUTRAL || mine == rc.getTeam().opponent();
     }
 
     MapLocation[] adjacentNonAlliedMines(MapLocation loc)
         throws GameActionException
     {
         return rc.senseNonAlliedMineLocations(loc, 2);
     }
 
     boolean busy(RobotInfo info)
     {
         return info.roundsUntilMovementIdle > 0;
     }
 
     boolean robotBusy(MapLocation loc, Team team)
         throws GameActionException
     {
         RobotInfo info = robotInfo(loc, team);
         return info != null ? busy(info) : false;
     }
 
     boolean battleBot(RobotInfo info)
     {
         return
             info.type == RobotType.SOLDIER ||
             info.type == RobotType.ARTILLERY ||
             info.type == RobotType.MEDBAY;
     }
 
     RobotInfo robotInfo(MapLocation loc, Team team)
         throws GameActionException
     {
         Robot r = (Robot) rc.senseObjectAtLocation(loc);
         if (r == null) return null;
 
         RobotInfo info = rc.senseRobotInfo(r);
         return info.team == team ? info : null;
     }
 
     // Includes the diagonals!
     public Robot[] adjacentRobots(MapLocation loc, Team team)
         throws GameActionException
     {
         return rc.senseNearbyGameObjects(Robot.class, loc, 2, team);
     }
 
     // Robots of the team x that are within striking distance.
     public Robot[] strikeRobots(MapLocation loc, Team team)
         throws GameActionException
     {
         return rc.senseNearbyGameObjects(Robot.class, loc, 8, team);
     }
 
 
     public RobotInfo[] allEnemies()
         throws GameActionException
     {
         final int SAMPLE_SIZE = 10;
 
         Robot[] enemies = rc.senseNearbyGameObjects(
                 Robot.class, Integer.MAX_VALUE, rc.getTeam().opponent());
 
         int steps = Utils.ceilDiv(enemies.length, SAMPLE_SIZE);
         int length = enemies.length / steps;
 
         RobotInfo[] info = new RobotInfo[length];
         for (int i = info.length; --i >= 0;)
             info[i] = rc.senseRobotInfo(enemies[i*steps]);
 
         return info;
     }
 
     private RobotInfo[] nearbyEnemiesCache = null;
     public RobotInfo[] nearbyEnemies()
         throws GameActionException
     {
         if (nearbyEnemiesCache != null) return nearbyEnemiesCache;
 
         Robot[] enemies = rc.senseNearbyGameObjects(
                 Robot.class, sightRadius, rc.getTeam().opponent());
 
         nearbyEnemiesCache = new RobotInfo[enemies.length];
         for (int i = enemies.length; --i >= 0;)
             nearbyEnemiesCache[i] = rc.senseRobotInfo(enemies[i]);
 
         return nearbyEnemiesCache;
     }
 
 
     private RobotInfo[] nearbyAlliesCache = null;
     public RobotInfo[] nearbyAllies()
         throws GameActionException
     {
         if (nearbyAlliesCache != null) return nearbyAlliesCache;
 
         Robot[] allies = rc.senseNearbyGameObjects(
                 Robot.class, sightRadius, rc.getTeam());
 
         nearbyAlliesCache = new RobotInfo[Math.min(20, allies.length)];
         for (int i = nearbyAlliesCache.length; --i >= 0;)
             nearbyAlliesCache[i] = rc.senseRobotInfo(allies[i]);
 
         return nearbyAlliesCache;
     }
 
     private int ts;
     private boolean sightAdjusted = false;
     private int sightRadius = RobotType.SOLDIER.sensorRadiusSquared;
 
     public void reset()
     {
         ts = Clock.getRoundNum();
         nearbyAlliesCache = null;
         nearbyEnemiesCache = null;
 
         allEncampments = null;
         alliedEncampments = null;
         localEncampments = null;
         neutralEncampments = null;
 
         updateRushTime();
 
         if (!sightAdjusted && rc.hasUpgrade(Upgrade.VISION)) {
             sightAdjusted = true;
             sightRadius += GameConstants.VISION_UPGRADE_BONUS;
         }
     }
 
     public MapLocation[] neutralEncampments()
     {
         if (allEncampments == null)
             allEncampments = rc.senseAllEncampmentSquares();
         return allEncampments;
     }
 
     public MapLocation[] allEncampments()
     {
         if (allEncampments == null)
             allEncampments = rc.senseAllEncampmentSquares();
         return allEncampments;
     }
 
     public MapLocation[] alliedEncampments()
     {
         if (alliedEncampments == null)
             alliedEncampments = rc.senseAlliedEncampmentSquares();
         return alliedEncampments;
     }
 
     public MapLocation[] localEncampments(Team team)
         throws GameActionException
     {
         if (localEncampments != null) return localEncampments;
 
         localEncampments = rc.senseEncampmentSquares(
                 rc.getLocation(), 63, team);
 
         int campRadius = 63;
         while (localEncampments.length < 1 && campRadius < DISTANCE_BETWEEN_HQS)
         {
             campRadius *= 2;
             localEncampments = rc.senseEncampmentSquares(
                     rc.getLocation(), campRadius, team);
         }
 
         return localEncampments;
     }
 
     public double strategicRelevance(MapLocation p)
     {
         double factor = DISTANCE_BETWEEN_HQS / Weights.STRAT_RATIO;
         return (factor - Utils.distToLineBetween(p, MY_HQ, ENEMY_HQ))
                 / factor;
     }
 
     public double defensiveRelevance(MapLocation p)
     {
         double factor = DISTANCE_BETWEEN_HQS / Weights.DEF_RATIO;
         return
             ( 0.9 * (factor - Utils.distTwoPoints(p, MY_HQ)) +
             0.1 * (factor - Utils.distToLineBetween(p, MY_HQ, ENEMY_HQ)) )
                 / factor;
     }
 
     public int militaryEncampments() throws GameActionException {
         // TODO : something weird happening with broadcasts:
      // [java] [B:SOLDIER#1911@401] java.lang.ArrayIndexOutOfBoundsException: -35018
      // [java] [B:SOLDIER#1911@401]    at bobot.SoldierMacro.supplierValue(SoldierMacro.java:299)
      // [java] [B:SOLDIER#1911@401]    at bobot.SoldierMacro.capture(SoldierMacro.java:227)
      // [java] [B:SOLDIER#1911@401]    at bobot.SoldierMacro.formup(SoldierMacro.java:40)
      // [java] [B:SOLDIER#1911@401]    at bobot.Soldier.run(Soldier.java:31)
      // [java] [B:SOLDIER#1911@401]    at bobot.RobotPlayer.run(RobotPlayer.java:15)
 
         // int n = Communication.readBroadcast(NUM_MIL, true);
         // if (n == -1)
         //     n = 0;
         // return n;
         return 0;
     }
 
     public boolean haveShields() throws GameActionException {
         int n = Communication.readBroadcast(NUM_SHIELDS, true);
         return n > 0;
     }
 
     public static final int[] roundsBySuppliers = {
         10, 9, 8, 8, 7, 7, 6, 6, 6, 5, 5, 5, 5,
         4,  4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3,
         3,  3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2,
         2,  2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
         2,  2, 2, 2, 2, 1
     };
 
     public static final int[] suppliersUntilJump = {
         1, 1, 2, 1, 2, 1, 3, 2, 1, 4, 3, 2, 1,
         6, 5, 4, 3, 2, 1, 12, 11, 10, 9, 8, 7, 6,
         5, 4, 3, 2, 1, 26, 25, 24, 23, 22, 21, 20, 19,
         18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6,
         5, 4, 3, 2, 1, 1
     };
 }
