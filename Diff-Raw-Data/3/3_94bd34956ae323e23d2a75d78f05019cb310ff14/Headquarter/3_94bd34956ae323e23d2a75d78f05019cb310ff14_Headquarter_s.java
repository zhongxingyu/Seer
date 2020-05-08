 package team216;
 
 import battlecode.common.*;
 
 /**
  *
  */
 public class Headquarter
 {
 
     private static int RESEARCH_WINDOW = 2;
 
     private static int nextSpawn = 0;
     private static int nextResearch = 0;
 
     /**
      *
      */
     private static boolean trySpawn(RobotController rc, int ord)
         throws GameActionException
     {
         Direction dir = Utils.dirByOrd[ord & Utils.dirOrdMask];
        if (!rc.canMove(dir) || rc.senseMine(rc.getLocation().add(dir)) != null) return false;
 
         rc.spawn(dir);
         return true;
     }
 
 
     /**
      *
      */
     private static void spawn(RobotController rc, MapLocation coord)
         throws GameActionException
     {
         Direction dir = coord.directionTo(Storage.ENEMY_HQ);
         int dirOrd = dir.ordinal();
         boolean spawned = false;
 
         for (int i = 0; i < 5; ++i) {
             spawned = trySpawn(rc, dirOrd + i);
             spawned = spawned || trySpawn(rc, dirOrd - i);
 
             if (spawned) break;
         }
 
         if (spawned) nextSpawn = -1;
     }
 
 
     /**
      *
      */
     public static boolean research(RobotController rc, double mapSize)
         throws GameActionException
     {
 
         if (mapSize < Weights.MAPSIZE_S) {
             if (!rc.hasUpgrade(Upgrade.VISION)) {
                 rc.researchUpgrade(Upgrade.VISION);
                 return true;
             }
             if (!rc.hasUpgrade(Upgrade.PICKAXE)) {
                 rc.researchUpgrade(Upgrade.PICKAXE);
                 return true;
             }
         }
 
         else if (mapSize < Weights.MAPSIZE_M) {
             if (!rc.hasUpgrade(Upgrade.VISION)) {
                 rc.researchUpgrade(Upgrade.VISION);
                 return true;
             }
             if (!rc.hasUpgrade(Upgrade.FUSION)) {
                 rc.researchUpgrade(Upgrade.FUSION);
                 return true;
             }
             if (!rc.hasUpgrade(Upgrade.DEFUSION)) {
                 rc.researchUpgrade(Upgrade.DEFUSION);
                 return true;
             }
             if (!rc.hasUpgrade(Upgrade.PICKAXE)) {
                 rc.researchUpgrade(Upgrade.PICKAXE);
                 return true;
             }
         }
 
         else {
             if (!rc.hasUpgrade(Upgrade.DEFUSION)) {
                 rc.researchUpgrade(Upgrade.DEFUSION);
                 return true;
             }
             if (!rc.hasUpgrade(Upgrade.FUSION)) {
                 rc.researchUpgrade(Upgrade.FUSION);
                 return true;
             }
             if (!rc.hasUpgrade(Upgrade.VISION)) {
                 rc.researchUpgrade(Upgrade.VISION);
                 return true;
             }
             if (!rc.hasUpgrade(Upgrade.PICKAXE)) {
                 rc.researchUpgrade(Upgrade.PICKAXE);
                 return true;
             }
         }
 
         return false;
     }
 
 
     public static void run(RobotController rc) throws GameActionException
     {
         MapLocation coord = rc.getLocation();
         double mapSize = Math.sqrt(
                 Storage.ENEMY_HQ.distanceSquaredTo(Storage.ENEMY_HQ));
 
         rc.setIndicatorString(0, "mapsize=" + mapSize);
 
         if (mapSize < Weights.MAPSIZE_S)
             RESEARCH_WINDOW = Weights.SHORT_WINDOW;
 
         else if (mapSize < Weights.MAPSIZE_M)
             RESEARCH_WINDOW = Weights.MEDIUM_WINDOW;
 
         else
             RESEARCH_WINDOW = Weights.LONG_WINDOW;
 
 
         while (true) {
             if (Clock.getRoundNum() > 198){
                 if (Storage.nukePanic())
                     rc.setIndicatorString(1, "PANIC!!!!");
             }
             if (!rc.isActive()) { rc.yield(); continue; }
 
             int round = Clock.getRoundNum();
 
             if (nextSpawn < 0) nextSpawn = round + RESEARCH_WINDOW;
 
             if (nextSpawn <= round || !research(rc, mapSize))
                 spawn(rc, coord);
 
 
             rc.yield();
         }
     }
 
 }
