 package squirtlesquad;
 import battlecode.common.*;
 class DataWorker {
     public RobotController rc;
     public MapLocation[] bm;
     public DataWorker (RobotController rc) {
         this.rc = rc;
     }
     public void takeBadMines (MapLocation[] in) {
         bm = in;
     }
     public MapLocation[] pathFind (MapLocation from, MapLocation to) {
        return [];
     }
 
 }
