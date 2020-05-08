 package TKM;
 
 public class Block extends TrackElement
 {
     public static final boolean DIRECTION_FWD = false;
     public static final boolean DIRECTION_REV = true; 
 
     /* For sane double comparisons */
     public static final double SMALL_DOUBLE = 0.00001;
 
     /* TODO: clean this up */
 
 
     /* Static info */
     public double length;
     public double grade;
     public double speedLimit;
     public boolean isBidir;
     public boolean isUground;
     public boolean isYard;
     public boolean isStation;
     public boolean isCrossing;
     public String stationName;
     public TrackElement prev;
     public TrackElement next;
 
     /* Dynamic info*/
     public boolean occupied;
     public boolean brokenRailFailure;
     public boolean trackCircuitFailure;
     public boolean powerFailure;
 
     public double mbSpeed;
     public double mbAuthority;
     public double fbSpeed;
     public double fbAuthority;
 
     /* etc */
     public int fwdId;
     public int revId;
     public int mapX2;
     public int mapY2;
 
 
     public Block() {
         prev = null;
         next = null;
         id = -1;
     }
 
     public Block(int id, String lineId, String sectionId, double length, double grade,
                     double speedLimit, boolean isBidir, boolean isUground, boolean isYard,
                    boolean isStation, boolean isCrossing, String stationName) {
         this.id = id;
         this.lineId = lineId;
         this.sectionId = sectionId;
         this.length = length;
         this.grade = grade;
         this.speedLimit = speedLimit;
         this.isBidir = isBidir;
         this.isUground = isUground;
         this.isYard = isYard;
         this.isStation = isStation;
         this.isCrossing = isCrossing;
         this.stationName = stationName;
     }
 
 
     public void connect(TrackElement prev, TrackElement next)
     {
         this.prev = prev;
         this.next = next;
     }
                         
 
     public boolean isOccupied() {
         /* TODO: Correctly evaluate failure mode */
         return occupied;
     }
 
 
     private Block getSwitchDest(Switch sw, boolean dryRun)
     {
         if (!dryRun) System.out.printf("Switch %d from block %d\n", sw.id, this.id);
         /* NYI: Derail if switch is not set properly */
         
         if (this == sw.blkMain) {
             /* Go in the direction according to the switch state */
             if (sw.state == Switch.STATE_STRAIGHT)
                 return sw.blkStraight;
             else if (sw.state == Switch.STATE_DIVERGENT)
                 return sw.blkDiverg;
         } else if (this == sw.blkStraight) {
             if (!dryRun && sw.state != Switch.STATE_STRAIGHT) {
                 System.out.printf("Warning: switch %d auto-flipped to STRAIGHT\n", sw.id);
                 sw.state = Switch.STATE_STRAIGHT;
             }
             return sw.blkMain;
         } else if (this == sw.blkDiverg) {
             if (!dryRun && sw.state != Switch.STATE_DIVERGENT) {
                 System.out.printf("Warning: switch %d auto-flipped to DIVERGENT\n", sw.id);
                 sw.state = Switch.STATE_DIVERGENT;
             }
             return sw.blkMain;
         }
         
         System.out.print("Switching error\n");
         return null;
     }
 
     public Block getNext(boolean direction, boolean dryRun) {
 
         Block dest = null;
 
         if (direction == DIRECTION_FWD) { /* direction is toward next */
             if (next != null && next instanceof Switch) {
                 dest =  getSwitchDest((Switch) next, dryRun);
             } else {
                 dest = (Block) next;
             }
         } else { /* direction is toward prev */
             if (prev != null && prev instanceof Switch) {
                 dest = getSwitchDest((Switch) prev, dryRun);
             } else {
                 /* prev is a block */
                 dest = (Block) prev;
             }
         }
 
         return dest;
     }
 
     public static void advanceTrain(TrainLocation resp, double distance) {
         /* Ensure we can legally travel in the requested direction */
         if (resp.direction == DIRECTION_FWD) {
             resp.distance += distance;
         } else {
             if (!resp.block.isBidir) {
                 System.out.printf("Unauthorized travel direction!\n");
                 resp.failed = true;
                 return;
             }
             resp.distance -= distance;
         }
 
         /* Determine the new location of the front of the train */
         if (0. < resp.distance && resp.distance < resp.block.length) {
             /* Stay within current block */
             return;
         } else {
             /* Move to next block */
             Block dest = resp.block.getNext(resp.direction, false);
 
             /* Default direction may have changed */
             if (dest.getNext(resp.direction, true) == resp.block) {
                 /* Default direction is opposite current default */
                 resp.direction = !resp.direction;
                 if (resp.distance < SMALL_DOUBLE) resp.distance = -resp.distance;
                 else resp.distance = dest.length - (resp.distance - resp.block.length);
             } else { /* default direction doesn't change */
                 if (resp.distance < SMALL_DOUBLE) resp.distance = resp.distance + dest.length;
                 else resp.distance = resp.distance - resp.block.length;
             }
             
             resp.block = dest;
         }
 
         /* Determine the new occupancy states of all blocks involved */
         resp.block.occupied = true;
         
     }
 
     public String toString()
     {
         return ("Block " + Integer.toString(id));
     }
 }
