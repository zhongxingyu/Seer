 package team197.modules;
 
 import battlecode.common.Direction;
 import battlecode.common.GameActionException;
 import battlecode.common.MapLocation;
 import battlecode.common.Robot;
 import battlecode.common.RobotController;
 
 import java.util.ArrayList;
 
 
 /** Module for attacking targets.
  * This module has methods to move a robot about while
  *  attacking, which operate by sensing enemy and friendly
  *  robots and returning directions to position oneself
  *  accordingly.
  *
  * Direction.NONE indicates that the robot shouldn't move,
  *  while returning NULL indicates that the attack algorithm
  *  doesn't have anything to fight.
  */
 public class FightModule {
     private static final double STRAFE_ADJ_COST = 25;
     private static final double STRAFE_ALLY_BASE = 36;
     private static final double STRAFE_ENEMY_BASE = 36;
 
     private MapLocation cur;
 
 
     /** Creates a list of directions that move the robot can move
      *   in order to be adjacent to one of the targets.
      * Returns an empty array if it is impossible to approach any
      *  target with a single move action.
      */
     private Direction[] getApproaches(RobotController rc, MapLocation[] targets) {
         ArrayList<Direction> dirs;
         Direction[] values;
 
         // Create an array list to hold valid directions
         dirs = new ArrayList<Direction>();
 
         // Step through each possible direction (skipping OMNI),
         //  and test if it is a valid direction.
         values = Direction.values();
         for(int i = 0; i < values.length; i++) {
             // Skip this value if the direction is OMNI
             if(values[i] == Direction.OMNI) continue;
 
             // Test if the new direction can be moved to
             if(values[i] != Direction.NONE && !rc.canMove(values[i])) continue;
 
             // Test if the new location is adjacent to
             // any of the targets
             MapLocation m = cur.add(values[i]);
             int j;
             for(j = 0; j < targets.length; j++)
                 if(m.isAdjacentTo(targets[j])) break;
             if(j == targets.length) continue;
 
             // This is a valid direction
             dirs.add(values[i]);
         }
 
         // Finally, return the array
         return dirs.toArray(new Direction[0]);
     }
 
     /** Finds an optimum direction to move for fighting.
      * Returns a direction from a list that optimizes the following:
      *  - least number of adjacent enemies
      *  - most distance from nearby allies
      *  - most distance from nearby enemies
      *
      * If all directions place the robot adjacent to an enemy, then
      *  this function will pick the safest place to be.
      */
     private Direction strafe(RobotController rc, Direction[] dirs, MapLocation[] enemies, MapLocation[] allies) {
         MapLocation tmp;
         int besti;
         double bestval, val;
 
         // Loop through each direction and compute a score based
         // on the following:
         //  - Subtract STRAFE_ADJ_COST for each adjacent enemy
         //  - Add 36/(distance squared) for each nearby enemy
         //  - Add 36/(distance squared) to nearby allies
         // The lowest total score is the best location.
         besti = 0;
         bestval = 10000;
         for(int i = 0; i < dirs.length; i++) {
             val = 0;
             tmp = cur.add(dirs[i]);
 
             // First step through allies and add distance
             for(int j = 0; j < allies.length; j++)
                 val += STRAFE_ALLY_BASE/tmp.distanceSquaredTo(allies[j]);
 
             // Next step through enemies.  Adjacent enemies subtract
             //  the adjacent cost (because they can attack), while
             //  non-adjacent enemies add their distance.
             for(int j = 0; j < enemies.length; j++)
                 if(tmp.isAdjacentTo(enemies[j]))
                     val += STRAFE_ADJ_COST;
                 else
                     val += STRAFE_ENEMY_BASE/tmp.distanceSquaredTo(enemies[j]);
 
             // If this score is better than the old best, record
             //  the score and the index.
             if(val < bestval) {
                 besti = i;
                 bestval = val;
             }
         }
 
         // Finally, return the computed best direction
         return dirs[besti];
     }
 
 
     /** Moves towards the nearest enemy robot in sight.
      * Simply chases down the closest enemy and attacks it.  If the
      *  robot can move adjacent to one or more enemies, it chooses to
      *  move next to fewer enemies.
      */
     public Direction fightClosestRobot(RobotController rc) throws GameActionException {
         MapLocation[] enemies,
                       allies;
         Direction[] dirs;
         Robot[] robots;
 
         // Store current location
         cur = rc.getLocation();
 
         // Get a list of nearby enemies
         robots = rc.senseNearbyGameObjects(Robot.class, 33, rc.getRobot().getTeam().opponent());
 
         // If there are no enemies, there is nothing to fight
         if(robots.length == 0) return null;
 
         // Record enemy locations
         enemies = new MapLocation[robots.length];
         for(int i = 0; i < enemies.length; i++)
             enemies[i] = rc.senseLocationOf(robots[i]);
 
         // If we can move adjacent to someone, find the best way
         //  to do so.  Otherwise, find the closest enemy and move
         //  towards it.
         dirs = getApproaches(rc, enemies);
         if(dirs.length == 0) {
             // Find the closest enemy and move towards it
             int mindist = 100;
             int mini = 0;
             int dist;
             for(int i = 0; i < enemies.length; i++) {
                 if((dist = cur.distanceSquaredTo(enemies[i])) < mindist) {
                     mindist = dist;
                     mini = i;
                 }
             }
 
            // Return the direction to the closest enemy
            return cur.directionTo(enemies[mini]);
         } else if(dirs.length == 1) {
             // Only one direction to move, ATTACK!
             return dirs[0];
         } else {
             // Get a list of nearby allies
             robots = rc.senseNearbyGameObjects(Robot.class, 33, rc.getRobot().getTeam());
             allies = new MapLocation[robots.length];
             for(int i = 0; i < allies.length; i++)
                 allies[i] = rc.senseLocationOf(robots[i]);
 
             // Now return the best direction to move
             return strafe(rc, dirs, enemies, allies);
         }
     }
 }
