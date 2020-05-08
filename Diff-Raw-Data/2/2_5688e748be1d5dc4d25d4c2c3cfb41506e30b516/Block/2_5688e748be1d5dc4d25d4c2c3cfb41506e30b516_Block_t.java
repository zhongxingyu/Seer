 package TKM;
 import TNM.*;
 
 import java.util.ArrayList;
 
 public class Block extends TrackElement
 {
     public static final boolean DIRECTION_FWD = false;
     public static final boolean DIRECTION_REV = true; 
     //public static final boolean TRAINING_WHEELS = false; 
 
     /* For sane double comparisons */
     public static final double SMALL_DOUBLE = 0.000001;
 
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
     public boolean isCrossingOn;
     public boolean signalState;
     public String stationName;
     public String transponderMessage;
     public String transponderMessageFwd;
     public String transponderMessageRev;
     public TrackElement prev;
     public TrackElement next;
 
     /* Dynamic info*/
     public boolean occupied;
     public boolean brokenRailFailure;
     public boolean trackCircuitFailure;
     public boolean powerFailure;
     public boolean isClosed;
 
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
         transponderMessage = "";
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
 
     public double getFBSpeed() {
         if (powerFailure) {
             return Double.NaN;
         }
         return fbSpeed;
     }
 
     public double getFBAuthority() {
         if (powerFailure) {
             return Double.NaN;
         }
         return fbAuthority;
     }
 
     public void disconnect(TrackElement target) {
         if (prev == target) {
             prev = null;
         }
         if (next == target) {
             next = null;
         }
     }
 
     public String readTransponder(boolean travelDirection) {
         if (travelDirection == DIRECTION_FWD) {
             return transponderMessageFwd;
         } else {
             return transponderMessageRev;
         }
     }
 
     public void connect(TrackElement prev, TrackElement next){
         this.prev = prev;
         this.next = next;
     }
                         
 
     public boolean isOccupied() {
         /* TODO: Correctly evaluate failure mode */
         if (trackCircuitFailure && powerFailure) {
             return false;
         }
         if (brokenRailFailure) {
             return true;
         }
         return occupied;
     }
 
     /* Gets the block at a distance away from the current location. It always
      * follows the straight switch path */
     public ArrayList<Block> getBlocksOnPath(double distOnBlock, boolean direction, double distTotal) {
         ArrayList<Block> retList = new ArrayList<Block>();
 
         /* Take us to the end of the current block */
         if (direction == DIRECTION_FWD) {
             distTotal -= (this.length-distOnBlock);
         } else {
             distTotal -= distOnBlock;
         }
 
         Block curBlk = this;
         Block oldBlk = this;
         
         while (distTotal > 0.) {
             oldBlk = curBlk;
             curBlk = curBlk.getNext(direction);
             distTotal -= curBlk.length;
             retList.add(curBlk);
 
             /* Default direction may have changed */
             if (curBlk.getNext(direction, true) == oldBlk) {
                 /* Default direction is opposite current default */
                 direction = direction;
             }
         }
 
         /* FIXME require the path to be at least one block */
         if (retList.isEmpty()) {
             retList.add(curBlk.getNext(direction));
         }
 
         return retList;
     }
 
     //~ public double getDistToLocation(double distOnBlock, double direction, double stopBlock)
     //~ {
 //~ 
     //~ }
 
     private Block getSwitchDest(Switch sw, boolean dryRun) {
         return getSwitchDest(sw, dryRun, sw.state);
     }
 
     private Block getSwitchDest(Switch sw, boolean dryRun, boolean swState) {
         if (!dryRun) System.out.printf("Switch %d from block %d\n", sw.id, this.id);
         /* NYI: Derail if switch is not set properly */
         
         if (this == sw.blkMain) {
             /* Go in the direction according to the switch state */
             if (swState == Switch.STATE_STRAIGHT)
                 return sw.blkStraight;
             else if (swState == Switch.STATE_DIVERGENT)
                 return sw.blkDiverg;
         } else if (this == sw.blkStraight) {
             if (!dryRun && swState != Switch.STATE_STRAIGHT) {
                 System.out.printf("switch %d auto-flipped to STRAIGHT\n", sw.id);
                 sw.state = Switch.STATE_STRAIGHT;
             }
             return sw.blkMain;
         } else if (this == sw.blkDiverg) {
             if (!dryRun && swState != Switch.STATE_DIVERGENT) {
                 System.out.printf("switch %d auto-flipped to DIVERGENT\n", sw.id);
                 sw.state = Switch.STATE_DIVERGENT;
             }
             return sw.blkMain;
         }
         
         System.out.print("Switching error\n");
         return null;
     }
 
     
     public Block getNext(boolean direction) {
         return getNext(direction, true);
     }
     
     /* Returns the block that is next on the expected path */
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
         if ((-SMALL_DOUBLE < train.positionMeters) && (train.positionMeters < (train.positionBlock.length + SMALL_DOUBLE))) {
 
             /* Stay within current block */
             train.positionBlock.occupied = true;
 
             /* Have the train become fully inside this block? */
             if (
                 train.positionBlock != train.positionBlockTail // Not already contained within a block
                 && ((
                     train.positionDirection == DIRECTION_FWD &&
                     train.positionMeters > train.length
                 ) || (
                     train.positionDirection == DIRECTION_REV &&
                     (train.positionBlock.length - train.positionMeters) > train.length
                 ))) {
                     train.positionBlockTail.occupied = false;
                     train.positionBlockTail = train.positionBlock;
                 }
             return;
         } else {
             
             /* Move to next block */
             Block dest = train.positionBlock.getNext(train.positionDirection, false);
 
            if (dest == dest.line.yard) {
                 train.positionMeters = 0.;
             }
 
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
