 package team298;
 
 import battlecode.common.*;
 import static battlecode.common.GameConstants.*;
 import java.util.*;
 
 public abstract class BendoverBugging extends NavigationGoal {
 
     public boolean[][] terrain;
     public MapLocation goal, current;
     public Direction currentDirection, originalDirection;
     public int size, currentX, currentY, maxPath, index, currentPathIndex;
     public boolean tracing, tracingLeft, pathCalculated, bugging, isAirRobot;
     public MapLocation[] path;
     public RobotController robotController;
     public boolean debug = false;
 
     public abstract MapLocation getGoal();
 
     public BendoverBugging(RobotController controller, MapStore map) {
         this.robotController = controller;
         isAirRobot = controller.getRobotType().isAirborne();
         terrain = map.boolMap;
         size = terrain.length;
 
         maxPath = 25;
         bugging = pathCalculated = tracing = tracingLeft = false;
         path = new MapLocation[maxPath];
     }
 
     public Direction getDirection() {
         MapLocation newGoal = getGoal();
         if(newGoal == null) {
             return null;
         }
 
         boolean sameGoal = goal == null || goal.equals(newGoal);
         goal = newGoal;
 
         //if we havent resorted to bugging yet, lets at least try to go straight to the goal first
         if(!bugging) {
             if(debug) System.out.println("not bugging");
             Direction dir = robotController.getLocation().directionTo(newGoal);
             if(robotController.canMove(dir)) {
                 if(debug) System.out.println("greedy in "+dir);
                 return dir;
             }
         }
 
         bugging = true;
         if(pathCalculated) {
             //if the goal has changed, but the new goal is next to the old goal, then we really dont need to recalculate the path
             if(!sameGoal && canGo(goal, newGoal)) {
                 path[index - 1] = newGoal;
                 return getNextPathDirection(false);
             } else {
                 planPath();
                 return getNextPathDirection(false);
             }
         } else {
             planPath();
             return getNextPathDirection(false);
         }
     }
 
     /**
      * This method has to figure out where in the path we are and figure out the next waypoint.
      * It has to be careful about waypoints that were optimized out.
      *
      * TODO: What if we had bad sensor data and the new waypoint is VOID or OFF_MAP, or we just cant get there.
      */
     public Direction getNextPathDirection(boolean recurse) {
         if(debug) System.out.println("getNextPathDirection");
         MapLocation goal = null, start = robotController.getLocation();
 
         //if we are not out of bounds, if the current path index has been optimized out, or the current path index is the start location, then we can advanced the waypoint
         while(currentPathIndex < index && (path[currentPathIndex] == null || path[currentPathIndex].equals(start))) currentPathIndex++;
         if(currentPathIndex >= index) {
             if(debug) System.out.println(currentPathIndex+" "+index);
             if(recurse) return null;
             //we have run out of path
             planPath();
             //we should really watch out for infinite recursion here
             return getNextPathDirection(true);
         }
         if(debug) System.out.println(path[currentPathIndex] == null);
         if((goal = path[currentPathIndex]) == null) return null;
         if(debug) System.out.println(start+" "+goal+" "+getMoveableDirection(start.directionTo(goal)));
         return getMoveableDirection(start.directionTo(goal));
     }
 
     /**
      * Returns the first direction that the robot can move in, starting with the given direction.
      */
     public Direction getMoveableDirection(Direction dir) {
         if(dir == null) {
             return null;
         }
         Direction leftDir = dir, rightDir = dir;
         if(robotController.canMove(dir)) {
             return dir;
         } else {
             for(int d = 0; d < 3; d++) {
                 leftDir = leftDir.rotateLeft();
                 rightDir = rightDir.rotateRight();
 
                 if(robotController.canMove(leftDir)) {
                     return leftDir;
                 }
                 if(robotController.canMove(rightDir)) {
                     return rightDir;
                 }
             }
         }
         return null;
     }
 
     /**
      * This method is used for path planning, when we cant sense far enough away.
      */
     public boolean canMove(int x, int y) {
         if(terrain[x % size][y % size]) return false;
        /*try {
             MapLocation location = new MapLocation(x, y);
             if(robotController.canSenseSquare(location)) {
                 if(isAirRobot) {
                     return robotController.senseAirRobotAtLocation(location) != null;
                 } else {
                    return robotController.senseGroundRobotAtLocation(goal) != null;
                 }
             }
         } catch (Exception e) {
             System.out.println("----Caught exception in canMove "+e.toString());
        }*/
         return true;
     }
 
     /**
      * This method computes the path that a bugging algorithm would normally take.
      */
     public void planPath() {
         goal = getGoal();
         currentDirection = robotController.getDirection();
         pathCalculated = true;
         if(debug) System.out.println("Plan path to "+goal);
 
         Direction dir;
         tracing = false;
         index = 1;
         currentPathIndex = 0;
         int pathLength = 1;
 
         path[0] = current = robotController.getLocation();
         currentX = current.getX();
         currentY = current.getY();
 
         while(true) {
             if(current.equals(goal)) {
                 //System.out.println("Arrived");
                 break;
             }
 
             if(pathLength >= maxPath-1) {
                 //System.out.println("Path to big");
                 break;
             }
 
             dir = getNextDirection();
 
             current = current.add(dir);
 
             //we only need to store waypoints, which is when the robot changes direction
             if(dir != currentDirection) {
                 path[index] = current;
                 index++;
             }
             currentX += dir.dx;
             currentY += dir.dy;
             currentDirection = dir;
 
             pathLength++;
         }
         path[index] = goal;
         index++;
 
         optimizePath();
 
         if(debug) {
             for(int c = 0; c < index; c++) {
                 if(path[c] != null) {
                     System.out.println(path[c]);
                 }
             }
         }
     }
 
     /**
      * This method removes unnecessary waypoints.
      * It looks at each set of three consecutive waypoints and sees if it can get to the waypoint
      * using a greedy algorithm.
      * If it can, then that waypoint is set to null.
      */
     public int optimizePath() {
         MapLocation start, goal;
 
         int osize = index;
         int waypointIndex = 1;
         start = path[0];
 
         for(int c = 0; c < index - 2; c++) {
             //the goal will always be the MapLocation immediately after the waypoint
             goal = path[waypointIndex + 1];
 
             //try to go straight from start to goal
             if(canGo(start, goal)) {
                 path[waypointIndex] = null;
                 osize--;
             } else {
                 //since we cant go directly from start to goal, we cannot get rid of the waypoint
                 //so, lets make this waypoint the new start and continue
                 start = path[waypointIndex];
             }
 
             //always advance to the next waypoint
             waypointIndex++;
         }
         return osize;
     }
 
     /**
      * Uses a greedy algorithm to determine if we can get from start to goal.
      * It just continually uses location.directionTo.
      */
     public boolean canGo(MapLocation start, MapLocation goal) {
         while(!start.equals(goal)) {
             start = start.add(start.directionTo(goal));
             if(!canMove(start.getX(), start.getY())) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Returns the next step that the bugging algorithm would take.
      * It tries to move directly to the goal.
      * If that is not available then it begins to trace.
      * Tracing means that it will try to turn all the way in one direction, say right, until it can no longer move forward.
      * Then, each step it will try to undo the trace by moving left as much as it can.
      * Tracing continues until the robot has returned to its original direction.
      * TODO: Fix this for robots that get stuck in an infinite loop.
      */
     public Direction getNextDirection() {
         Direction dir = current.directionTo(goal);
         int x, y;
         if(tracing) {
             dir = tryToUndoTrace(currentDirection);
             x = currentX + dir.dx;
             y = currentY + dir.dy;
             if(!canMove(x, y)) {
                 dir = getInitialTracingDirection(dir);
             }
             return dir;
         } else {
             x = currentX + dir.dx;
             y = currentY + dir.dy;
             if(canMove(x, y)) {
                 return dir;
             } else {
                 tracing = true;
                 originalDirection = dir;
                 tracingLeft = !(dir == Direction.NORTH || dir == Direction.NORTH_EAST || dir == Direction.EAST || dir == Direction.SOUTH_WEST);
                 dir = getInitialTracingDirection(dir);
                 return dir;
             }
         }
     }
 
     /**
      * We can 'simulate' turning as far right as possible, but just turning left until we hit the first obstacle.
      * That is what this method does.
      * It returns the direction in which bug-tracing should start.
      */
     public Direction getInitialTracingDirection(Direction dir) {
         int x, y;
         for(int c = 0; c < 8; c++) {
             x = currentX + dir.dx;
             y = currentY + dir.dy;
             if(!canMove(x, y)) {
                 dir = tracingLeft ? dir.rotateLeft() : dir.rotateRight();
             } else {
                 return dir;
             }
         }
 
         return null;
     }
 
     /**
      * Returns the direction in which the robot should move, trying to rotate the robot in the opposite direction from the initial trace.
      */
     public Direction tryToUndoTrace(Direction dir) {
         Direction tmp;
         int x, y;
 
         for(int c = 0; c < 8; c++) {
             tmp = tracingLeft ? dir.rotateRight() : dir.rotateLeft();
             x = currentX + tmp.dx;
             y = currentY + tmp.dy;
 
             if(canMove(x, y)) {
                 if(tmp == originalDirection) {
                     tracing = false;
                     return tmp;
                 }
                 dir = tmp;
             } else {
                 return dir;
             }
         }
 
         return dir;
     }
 }
