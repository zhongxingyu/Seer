 package TKM;
 import TNM.*;
 
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
 
     //public double mbSpeed;
     //public double mbAuthority;
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
                     boolean isStation, boolean isCrossing, String stationName,
                     boolean brokenRailFailure, boolean trackCircuitFailure, boolean powerFailure) {
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
         this.brokenRailFailure = brokenRailFailure;
         this.trackCircuitFailure = trackCircuitFailure;
         this.powerFailure = powerFailure;
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
 
 
     private Block getSwitchDest(Switch sw, boolean dryRun) {
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
 
     public static void advanceTrain(Train train, double distance) {
         /* TODO: Implement negative distance */
        if (distance < -SMALL_DOUBLE) {
             distance = 0.0;
             System.out.printf("Train traveling backwards!\n");
         }
         
         /* Ensure we can legally travel in the requested direction */
         if (train.positionDirection == DIRECTION_FWD) {
             train.positionMeters += distance;
         } else {
             if (!train.positionBlock.isBidir && !train.positionBlock.isYard) {
                 System.out.printf("Unauthorized travel direction!\n");
                 //resp.failed = true;
                 //return;
             }
             train.positionMeters -= distance;
         }
 
         /* Determine the new location of the front of the train */
         if (0. < train.positionMeters && train.positionMeters < train.positionBlock.length) {
 
             /* Stay within current block */
             train.positionBlock.occupied = true;
 
             /* Have we become fully inside this block? */
             if ((
                     train.positionDirection == DIRECTION_FWD &&
                     train.positionMeters > train.length
                 ) || (
                     train.positionDirection == DIRECTION_REV &&
                     (train.positionBlock.length - train.positionMeters) > train.length
                 )) {
                     train.positionBlockTail.occupied = false;
                     train.positionBlockTail = train.positionBlock;
                 }
             return;
         } else {
             
             /* Move to next block */
             Block dest = train.positionBlock.getNext(train.positionDirection, false);
 
             /* Default direction may have changed */
             if (dest.getNext(train.positionDirection, true) == train.positionBlock) {
                 /* Default direction is opposite current default */
                 train.positionDirection = !train.positionDirection;
                 if (train.positionMeters < SMALL_DOUBLE) train.positionMeters = -train.positionMeters;
                 else train.positionMeters = dest.length - (train.positionMeters - train.positionBlock.length);
             } else { /* default direction doesn't change */
                 if (train.positionMeters < SMALL_DOUBLE) train.positionMeters = train.positionMeters + dest.length;
                 else train.positionMeters = train.positionMeters - train.positionBlock.length;
             }
             
             /* Determine the new occupancy states of all blocks involved */
             /* TODO: Make this work correctly */
             dest.occupied = true;
 
             train.positionBlock = dest;
 
             /* TODO: Make this more realistic */
             train.routeIndex++;
         }
     }
 
     public String toString()
     {
         return ("Block " + Integer.toString(id));
     }
 }
