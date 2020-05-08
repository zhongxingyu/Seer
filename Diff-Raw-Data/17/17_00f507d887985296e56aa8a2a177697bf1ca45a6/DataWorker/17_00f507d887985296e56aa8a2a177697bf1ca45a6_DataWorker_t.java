 package squirtlesquad;
 import battlecode.common.*;
 class DataWorker {
     public RobotController rc;
     public MapLocation[] bm;
    public boolean[][] mines;
    public int x;
    public int y;
     public DataWorker (RobotController rc) {
         this.rc = rc;
        mines = new boolean[rc.getMapWidth][rc.getMapHeight];
     }
     public void takeBadMines (MapLocation[] in) {
         bm = in;
        x=0;
        y=0;
    }
    public boolean prepare() {
        while (clock.getByteCodesLeft > 500) {
            for (
        }

     }
     public MapLocation[] pathFind (MapLocation from, MapLocation to) {
        return null;
     }
 
 }
