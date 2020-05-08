 package team216;
 
 import battlecode.common.*;
 
 public class Storage {
 
     // Constants
 
     public static Team MY_TEAM;
     public static Team ENEMY_TEAM;
     public static MapLocation MY_HQ;
     public static MapLocation ENEMY_HQ;
     public static MapLocation CENTER; //center between HQs, not true center
     public static double DISTANCE_BETWEEN_HQ;
     public static double SLOPE;
     public static int MAP_HEIGHT;
     public static int MAP_WIDTH;
     public static int MAP_SIZE;
     public static RobotInfo MY_INFO;
     public static Robot ME;
     public static RobotController RC;
     public static double EST_RUSH_TIME;
 
     private static final int LC_RADIUS = 63; // FIXME
 
     private static double defensive_relevance; 
     private static double strategic_relevance;
     private static Direction direction_to_enemy_hq;
     private static double distance_to_enemy_hq;
 
     private static Robot[][] nearby_enemies = new Robot[2][];
     private static Integer[] nearby_enemies_last_updated = new Integer[2];
 
     private static int number_of_nearby_enemies;
 
     private static Robot[][] nearby_allies = new Robot[3][];
    private static Integer[] nearby_allies_last_updated = new Integer[3];
 
     private static MapLocation[] nearby_friendly_mines;
     private static MapLocation[][] nearby_nonallied_mines = new MapLocation[2][];
 
     public static void calculateValues(RobotController rc) {
         try {
             RC = rc;
             MY_TEAM = rc.getTeam();
             ENEMY_TEAM = MY_TEAM.opponent();
             MY_HQ = rc.senseHQLocation();
             ENEMY_HQ = rc.senseEnemyHQLocation();
             ME = rc.getRobot();
             MY_INFO = rc.senseRobotInfo(ME);
             MAP_HEIGHT = rc.getMapHeight();
             MAP_WIDTH = rc.getMapWidth();
             MAP_SIZE = MAP_HEIGHT * MAP_WIDTH;
         }
         catch(Exception e) { e.printStackTrace(); }
     }
 
     // Variables
 
     public static double getRushTime(RobotController rc) {
         //calculate estimated turns for rush
         if (EST_RUSH_TIME == 0.0) {
             double x_dif = MY_HQ.x - ENEMY_HQ.x;
             double y_dif = MY_HQ.y - ENEMY_HQ.y;
             double x;
             double y;
             double offset;
             double time = 0.0;
             String s = "";
             //rc.setIndicatorString(0, "c=" + CENTER + ", m=" + MY_HQ + ", e=" + ENEMY_HQ + ", xdif=" + x_dif + ", ydif" + y_dif + ", slope=" + SLOPE);
             for (int i=0; i<20; i++) {
                 offset = 6 * Math.random() - 3;
                 x = Math.random() * x_dif;
                 y = SLOPE * x + ENEMY_HQ.y;
                 s += " (" + (int)x + ", " + (int)y + ")";
                 if (Team.NEUTRAL.equals(rc.senseMine(new MapLocation((int)x, (int)y)))){
                     time += 12;
                     s += "!";
                 }
             }
             rc.setIndicatorString(2, s);
             time *= (distanceBetweenHQs()/20.0);
             EST_RUSH_TIME = time;
             return time;
         } else
             return EST_RUSH_TIME;
     }
 
     public static double distanceBetweenHQs() {
         return DISTANCE_BETWEEN_HQ == 0.0 ? DISTANCE_BETWEEN_HQ = Utils.distTwoPoints(MY_HQ, ENEMY_HQ) : DISTANCE_BETWEEN_HQ;
     }
 
     public static double slopeBetweenHQs() {
         return SLOPE == 0.0 ? SLOPE = (double)(MY_HQ.y - ENEMY_HQ.y) / (MY_HQ.x - ENEMY_HQ.x) : SLOPE;
     }
 
     public static MapLocation centerBetweenHQs() {
         return CENTER == null ? CENTER = new MapLocation((MY_HQ.x + ENEMY_HQ.x)/2,(MY_HQ.y + ENEMY_HQ.y)/2) : CENTER;
     }
 
     public static MapLocation myLocation() {
         // No need to cache this one, it's free
         return RC.getLocation();
     }
 
     public static Direction directionToEnemyHQ() {
         if (direction_to_enemy_hq == null || Clock.getRoundNum() % 6 == 0)
             direction_to_enemy_hq = RC.getLocation().directionTo(ENEMY_HQ);
         return direction_to_enemy_hq;
     }
 
     public static double distanceToEnemyHQ() {
         if (distance_to_enemy_hq == 0.0 || Clock.getRoundNum() % 3 == 1)
             distance_to_enemy_hq = RC.getLocation().distanceSquaredTo(ENEMY_HQ);
         return distance_to_enemy_hq;
     }
 
     public static double defensiveRelevance() {
         if (defensive_relevance == 0.0 || Clock.getRoundNum() % 3 == 2)
             defensive_relevance = Utils.defensiveRelevance(RC.getLocation(), MY_HQ, ENEMY_HQ, distanceBetweenHQs(), Weights.DEF_RATIO);
         return defensive_relevance;
     }
 
     public static double strategicRelevance() {
         if (strategic_relevance == 0.0 || Clock.getRoundNum() % 3 == 2)
             strategic_relevance = Utils.strategicRelevance(RC.getLocation(), MY_HQ, ENEMY_HQ, distanceBetweenHQs(), Weights.STRAT_RATIO);
         return strategic_relevance;
     }
 
     public static RobotInfo robotInfo(Robot r) throws GameActionException {
         // TODO: No caching here yet
         return RC.senseRobotInfo(r);
     }
 
     // This method doesn't cache, it simply returns the store result if we call it more than once in a turn
     public static Robot[] nearbyEnemies(int radiusSquared) {
         // Since we only have shitty fixed-sized arrays, we implement this only for common inputs:
         int i;
         switch (radiusSquared) {
             case LC_RADIUS:
                     i = 0;
                     break;
             case Soldier.GL_RADIUS:
                     i = 1;
                     break;
             // We don't cache that radius
             default: return RC.senseNearbyGameObjects(Robot.class, radiusSquared, ENEMY_TEAM);
         }
 
         if (nearby_enemies[i] == null || nearby_enemies_last_updated[i] < Clock.getRoundNum()) {
             nearby_enemies[i] = RC.senseNearbyGameObjects(Robot.class, radiusSquared, ENEMY_TEAM);
             nearby_enemies_last_updated[i] = Clock.getRoundNum();
         }
 
         return nearby_enemies[i];
     }
 
     public static int numberOfNearbyEnemies() {
         // Radius = 1
         if (number_of_nearby_enemies == 0 || Clock.getRoundNum() % 3 == 0)
             number_of_nearby_enemies = RC.senseNearbyGameObjects(Robot.class, 1, ENEMY_TEAM).length;
         return number_of_nearby_enemies;
     }
 
     public static Robot[] nearbyAllies(int radiusSquared) {
         // Since we only have shitty fixed-sized arrays, we implement caching for common inputs:
         int i;
         switch (radiusSquared) {
             case 3: 
                     i = 0;
                     break;
             case LC_RADIUS:
                     i = 1;
                     break;
             case Soldier.GL_RADIUS:
                     i = 2;
                     break;
             // We don't cache that radius
             default: return RC.senseNearbyGameObjects(Robot.class, radiusSquared, MY_TEAM);
         }
 
         if (nearby_allies[i] == null || nearby_allies_last_updated[i] < Clock.getRoundNum()) {
             nearby_allies[i] = RC.senseNearbyGameObjects(Robot.class, radiusSquared, MY_TEAM);
             nearby_allies_last_updated[i] = Clock.getRoundNum();
         }
 
         return nearby_allies[i];
     }
 
     public static MapLocation[] nearbyNonAlliedMines(int radiusSquared) {
         int i;
         switch (radiusSquared) {
             case LC_RADIUS:
                     i = 0;
                     break;
             case Soldier.GL_RADIUS:
                     i = 1;
                     break;
             default: return RC.senseNonAlliedMineLocations(RC.getLocation(), radiusSquared);
         }
 
         if(nearby_nonallied_mines[i] == null || Clock.getRoundNum() % 3 == 0)
             nearby_nonallied_mines[i] = RC.senseNonAlliedMineLocations(RC.getLocation(), radiusSquared);
 
         return nearby_nonallied_mines[i];
     }
 
     public static MapLocation[] nearbyFriendlyMines() {
         if (nearby_friendly_mines == null || Clock.getRoundNum() % 3 == 0)
             nearby_friendly_mines = RC.senseMineLocations(RC.getLocation(), LC_RADIUS, MY_TEAM);
         return nearby_friendly_mines;
     }
 }
