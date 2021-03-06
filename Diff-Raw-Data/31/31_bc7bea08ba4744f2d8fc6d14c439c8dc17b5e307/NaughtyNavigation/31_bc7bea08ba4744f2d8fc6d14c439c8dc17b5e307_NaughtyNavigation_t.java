 package team298;
 
 import battlecode.common.*;
 import static battlecode.common.GameConstants.*;
 import java.util.*;
 
 public class NaughtyNavigation extends Base {
 
     public MapStore map;
     public MexicanMessaging messaging;
     public SensationalSensing sensing;
     public NavigationGoal goal;
     public LinkedList<NavigationGoal> goalStack;
 
     public NaughtyNavigation(NovaPlayer player) {
         super(player);
         map = player.map;
         messaging = player.messaging;
         goal = null;
         goalStack = new LinkedList<NavigationGoal>();
     }
 
     public MapLocation findClosest(ArrayList<MapLocation> locations) {
         MapLocation closest = null, current = controller.getLocation();
         int min = Integer.MAX_VALUE, distance;
 
         for(MapLocation location : locations) {
             if(location == null) continue;
             distance = current.distanceSquaredTo(location);
             if(distance < min) {
                 closest = location;
                 min = distance;
             }
         }
 
         return closest;
     }
 
     public MapLocation findClosest(MapLocation[] locations) {
         MapLocation closest = null, current = controller.getLocation();
         int min = Integer.MAX_VALUE, distance;
 
         for(MapLocation location : locations) {
             if(location == null) continue;
             distance = current.distanceSquaredTo(location);
             if(distance < min) {
                 closest = location;
                 min = distance;
             }
         }
 
         return closest;
     }
 
     /**
      * Causes the robot to the face the specified direction if necessary.
      */
     public int faceDirection(Direction dir) {
         if(dir == null) {
             return Status.fail;
         }
 
         if(controller.getDirection().equals(dir)) {
             return Status.success;
         }
 
         if(dir.equals(Direction.OMNI)) {
             return Status.success;
         }
 
         while(controller.hasActionSet() || controller.getRoundsUntilMovementIdle() != 0) {
             controller.yield();
         }
 
         try {
             controller.setDirection(dir);
             controller.yield();
             return Status.success;
         } catch(Exception e) {
             System.out.println("----Caught Exception in faceDirection with dir: " + dir.toString() + " Exception: " + e.toString());
         }
 
         return Status.fail;
     }
 
     /**
      * Calculates the direction needed to turn and face the given location, such as
      * for when an Archon needs to turn to face an empty space to spawn a unit.
      * The robot will then wait until it's movement is idle and proceed to turn to that direction.
      */
     public int faceLocation(MapLocation location) {
         Direction newDir = getDirection(controller.getLocation(), location);
         return faceDirection(newDir);
     }
 
     /**
      * Returns the direction object needed to move a robot from the start square to the end square.
      */
     public Direction getDirection(MapLocation start, MapLocation end) {
         int x = end.getX() - start.getX();
         int y = end.getY() - start.getY();
         return getDirection(x, y);
     }
 
     public Direction getDirection(int x, int y) {
         if(y < 0) {
             if(x > 0) {
                 return Direction.NORTH_EAST;
             }
             if(x == 0) {
                 return Direction.NORTH;
             }
             if(x < 0) {
                 return Direction.NORTH_WEST;
             }
         } else if(y == 0) {
             if(x > 0) {
                 return Direction.EAST;
             }
             if(x < 0) {
                 return Direction.WEST;
             }
         } else if(y > 0) {
             if(x > 0) {
                 return Direction.SOUTH_EAST;
             }
             if(x == 0) {
                 return Direction.SOUTH;
             }
             if(x < 0) {
                 return Direction.SOUTH_WEST;
             }
         }
         return null;
     }
 
     /**
      * Returns the change to the location of an object if it moves one tile in the specified direction.
      */
     public int[] getDirectionDelta(Direction direction) {
         if(direction == Direction.NORTH_WEST) {
             return new int[] {-1, -1};
         }
         if(direction == Direction.NORTH) {
             return new int[] {0, -1};
         }
         if(direction == Direction.NORTH_EAST) {
             return new int[] {1, -1};
         }
 
         if(direction == Direction.EAST) {
             return new int[] {1, 0};
         }
         if(direction == Direction.WEST) {
             return new int[] {-1, 0};
         }
 
         if(direction == Direction.SOUTH_WEST) {
             return new int[] {-1, 1};
         }
         if(direction == Direction.SOUTH) {
             return new int[] {0, 1};
         }
         if(direction == Direction.SOUTH_EAST) {
             return new int[] {1, 1};
         }
 
         return new int[] {0, 0};
     }
 
     /**
      * Returns the Manhattan Distance
      */
     public int getDistanceTo(MapLocation location) {
         int x = location.getX() - controller.getLocation().getX();
         int y = location.getY() - controller.getLocation().getY();
         return Math.abs(x) + Math.abs(y);
     }
 
     /**
      * Returns the Manhattan Distance to the nearest archon
      */
     public int getDistanceToNearestArchon() {
         MapLocation location = sensing.senseClosestArchon();
         int x = location.getX() - controller.getLocation().getX();
         int y = location.getY() - controller.getLocation().getY();
         return Math.abs(x) + Math.abs(y);
     }
 
     /**
      * Returns the first direction that the robot can move in, starting with the given direction.
      */
     public Direction getMoveableDirection(Direction dir) {
         if(dir == null) {
             return null;
         }
         Direction leftDir = dir, rightDir = dir;
         if(controller.canMove(dir)) {
             return dir;
         } else {
             for(int d = 0; d < 3; d++) {
                 leftDir = leftDir.rotateLeft();
                 rightDir = rightDir.rotateRight();
 
                 if(controller.canMove(leftDir)) {
                     return leftDir;
                 }
                 if(controller.canMove(rightDir)) {
                     return rightDir;
                 }
             }
         }
         return null;
     }
 
     /**
      * Returns an array of the 8 map locations around a robot.  These are sorted so
      * that the first location is the one the robot is facing, and then the 2 next to
      * that location, the 2 next to that, and so on.  The last location is the tile directly
      * behind the robot.
      */
     public MapLocation[] getOrderedMapLocations() {
         Direction cur = controller.getDirection(), left, right;
         MapLocation start = controller.getLocation();
 
 
         MapLocation[] ret = new MapLocation[8];
         ret[0] = start.add(cur);
         ret[7] = start.subtract(cur);
 
         left = cur.rotateLeft();
         right = cur.rotateRight();
         ret[1] = start.add(right);
         ret[2] = start.add(left);
 
         left = cur.rotateLeft();
         right = cur.rotateRight();
         ret[3] = start.add(right);
         ret[4] = start.add(left);
 
         left = cur.rotateLeft();
         right = cur.rotateRight();
         ret[5] = start.add(right);
         ret[6] = start.add(left);
 
         return ret;
     }
 
     public void checkBlockedUnitsAndWait(MapLocation location) {
         boolean messageSent = false;
         int pauseCount = 5;
         do {
             try {
                 if(controller.canSenseSquare(location) && controller.senseGroundRobotAtLocation(location) != null) {
                     if(!messageSent) {
                         messaging.sendMove(location);
                         messageSent = true;
                     }
                     controller.yield();
                 } else {
                     break;
                 }
             } catch(Exception e) {
             }
             pauseCount--;
         } while(pauseCount >= 0);
     }
 
     /**
      * Returns true if there is no Air or Ground unit at the given location.
      * If a robot is blind (channeler), this method should not be called.  It does
      * not check if the robot can sense at that location.
      */
     public boolean isLocationFree(MapLocation location, boolean isAirUnit) {
         try {
             if(!map.onMap(location)) {
                 return false;
             }
 
             if(!controller.canSenseSquare(location)) {
                 return true;
             }
 
             TerrainTile tile = map.get(location);
             if(tile != null && !tile.isTraversableAtHeight((isAirUnit ? RobotLevel.IN_AIR : RobotLevel.ON_GROUND))) {
                 return false;
             }
 
             if(isAirUnit) {
                 return controller.senseAirRobotAtLocation(location) == null;
             } else {
                 return controller.senseGroundRobotAtLocation(location) == null;
             }
         } catch(Exception e) {
             pa("----Caught Exception in isLocationFree location: " + location.toString() + " isAirUnit: " +
                     isAirUnit + " Exception: " + e.toString());
             return false;
         }
     }
 
     /**
      * Makes the robot face a certain direction and then moves forawrd.
      * If block is false, then the robot will not call yield until it is able to move, it will return immediately instead.
      */
     public int moveOnce(boolean block) {
         if(!block && (controller.hasActionSet() || controller.getRoundsUntilMovementIdle() > 0)) {
             return Status.turnsNotIdle;
         }
 
         if(goal == null) return Status.noGoal;
         if(goal.done()) return Status.success;
 
         Direction dir = goal.getDirection();
 
         if(faceDirection(dir) != Status.success) {
             return Status.fail;
         }
 
         return moveOnce();
     }
 
     /*
      * Moves the robot one step forward if possible.
      * If block is false, then the robot will not call yield until it is able to move, it will return immediately instead.
      */
     private int moveOnce() {
         Direction dir = controller.getDirection();
         yieldMoving();
         try {
             if(controller.canMove(dir)) {
                 controller.moveForward();
                 controller.yield();
                 player.pathStepTakenCallback();
                 return Status.success;
             }
             return Status.cantMoveThere;
         } catch(Exception e) {
             System.out.println("----Caught Exception in moveOnce dir: " + dir.toString() + " Exception: " + e.toString());
         }
         return Status.fail;
     }
 
     /**
      * Returns true if the two squares are next to each other or are equal.
      */
     public boolean isAdjacent(MapLocation start, MapLocation end) {
         return start.distanceSquaredTo(end) < 3;
     }
 
     /**
      * Calls controller.yield until the robot is able to move again.
      */
     public void yieldMoving() {
         String cur = Goal.toString(player.currentGoal);
         controller.setIndicatorString(1, "yielding");
         while(controller.hasActionSet() || controller.getRoundsUntilMovementIdle() != 0) {
             controller.yield();
         }
         controller.setIndicatorString(1, cur);
     }
 
     /**
      * Removes any navigation goal.
      */
     public void clearGoal() {
         goal = null;
     }
 
     /**
      * Restores the previous goal.
      */
     public void popGoal() {
         if(goalStack.size() > 0) {
             goal = goalStack.removeFirst();
         }
     }
 
     /**
      * Saves the current goal onto the stack so it can be restored later.
      */
     public void pushGoal(boolean removePreviousGoals) {
         if(removePreviousGoals) {
             goalStack.clear();
         } else {
             goalStack.addFirst(goal);
         }
     }
 
     /**
      * Causes moveOnce to always move in direction.
      *
      * If removePreviousGoals is false, then the previous goal will be pushed onto a stack.
      * This enables temporary goals, like requestEnergonTransfer to work, without affecting high level goals.
      * If removePreviousGoals is true, then the stack is cleared.  This is useful if the goal needs to be changed from the main method.
      */
     public void changeToDirectionGoal(Direction direction, boolean removePreviousGoals) {
         pushGoal(removePreviousGoals);
         goal = new DirectionGoal(direction);
     }
 
     /**
      * Causes moveOnce to first try to move straight.  If it can't, the robot will rotate right until it can.
      *
      * This method will not overwrite the current goal if it is already a MoveableDirectionGoal.
      * To force it to overwrite, call clearGoal first.
      *
      * If removePreviousGoals is false, then the previous goal will be pushed onto a stack.
      * This enables temporary goals, like requestEnergonTransfer to work, without affecting high level goals.
      * If removePreviousGoals is true, then the stack is cleared.  This is useful if the goal needs to be changed from the main method.
      */
     public void changeToMoveableDirectionGoal(boolean removePreviousGoals) {
         pushGoal(removePreviousGoals);
         goal = new MoveableDirectionGoal();
     }
 
     /**
      * Causes moveOnce to always move closer to location.
      *
      * If removePreviousGoals is false, then the previous goal will be pushed onto a stack.
      * This enables temporary goals, like requestEnergonTransfer to work, without affecting high level goals.
      * If removePreviousGoals is true, then the stack is cleared.  This is useful if the goal needs to be changed from the main method.
      */
     public void changeToLocationGoal(MapLocation location, boolean removePreviousGoals) {
         pushGoal(removePreviousGoals);
         if(player.isArchon) goal = new LocationGoal(location);
         else goal = new LocationGoalWithBugging(location);
     }
 
     /**
      * Changes the navigation goal to move closer to the nearest archon.
      *
      * This method will not overwrite the current goal if it is already an ArchonGoal.
      * To force it to overwrite, call clearGoal first.
      * 
      * If removePreviousGoals is false, then the previous goal will be pushed onto a stack.
      * This enables temporary goals, like requestEnergonTransfer to work, without affecting high level goals.
      * If removePreviousGoals is true, then the stack is cleared.  This is useful if the goal needs to be changed from the main method.
      */
     public void changeToArchonGoal(boolean removePreviousGoals) {
         if(goal instanceof ArchonGoal || goal instanceof ArchonGoalWithBugging) return;
         pushGoal(removePreviousGoals);
         pr("Changing to Archon Goal");
         if(player.isArchon) goal = new ArchonGoal();
         else goal = new ArchonGoalWithBugging();
     }
 
     /**
      * Changes the navigation goal to move closer to the nearest tower.
      * 
      * This method will not overwrite the current goal if it is already a TowerGoal.
      * To force it to overwrite, call clearGoal first.
      * 
      * If removePreviousGoals is false, then the previous goal will be pushed onto a stack.
      * This enables temporary goals, like requestEnergonTransfer to work, without affecting high level goals.
      * If removePreviousGoals is true, then the stack is cleared.  This is useful if the goal needs to be changed from the main method.
      */
     public void changeToClosestTeleporterGoal(boolean removePreviousGoals) {
         if(goal instanceof ClosestTeleporterGoal) return;
         pushGoal(removePreviousGoals);
         goal = new ClosestTeleporterGoal();
     }
 
     /**
      * The purpose of this class is to enable flexible route planning that allows for movement one step at a time.
      */
     abstract class NavigationGoal {
         public boolean completed = false;
 
         /**
          * This method is called every time moveOnce is called.  It should return the direction in which the robot should move next.
          */
         public abstract Direction getDirection();
 
         /**
          * This method should return true when the robot is at the goal.
          */
         public abstract boolean done();
     }
 
     class DirectionGoal extends NavigationGoal {
 
         public Direction direction;
 
         public DirectionGoal(Direction direction) {
             this.direction = direction;
         }
 
         public Direction getDirection() {
             return direction;
         }
 
         public boolean done() {
             return false;
         }
     }
 
     class MoveableDirectionGoal extends NavigationGoal {
 
         public Direction getDirection() {
             return getMoveableDirection(controller.getDirection());
         }
 
         public boolean done() {
             return false;
         }
     }
 
     class LocationGoal extends NavigationGoal {
 
         public MapLocation location;
 
         public LocationGoal(MapLocation location) {
             this.location = location;
         }
 
         public Direction getDirection() {
             Direction dir = controller.getLocation().directionTo(location);
             return getMoveableDirection(dir);
         }
 
         public boolean done() {
             completed = completed || controller.getLocation().equals(location);
             return completed;
         }
     }
 
     abstract class BuggingGoal extends NavigationGoal {
         public boolean tracing, tracingLeft;
         public Direction originalDirection;
         public Hashtable<String, String> visitedStates;
 
         public abstract Direction getDirectionToGoal();
 
         public BuggingGoal() {
             tracing = false;
             tracingLeft = false;
             originalDirection = null;
             visitedStates = new Hashtable<String, String>();
         }
 
         public Direction getInitialTracingDirection(Direction dir) {
             for(int c = 0; c < 8; c++) {
                 if(!controller.canMove(dir)) {
                     if(tracingLeft)
                         dir = dir.rotateLeft();
                     else
                         dir = dir.rotateRight();
                 } else {
                     return dir;
                 }
             }
 
             return null;
         }
 
         public Direction tryToUndoTrace(Direction dir) {
             Direction tmp;
             for(int c = 0; c < 8; c++) {
                 if(tracingLeft)
                     tmp = dir.rotateRight();
                 else
                     tmp = dir.rotateLeft();
                 
                 if(controller.canMove(tmp)) {
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
         
         public Direction getDirection() {
             Direction dir = getDirectionToGoal();
             if(tracing) {
                 dir = controller.getDirection();
                 dir = tryToUndoTrace(dir);
                 if(!controller.canMove(dir)) {
                     dir = getInitialTracingDirection(dir);
                 }
                 return dir;
             } else {
                 if(controller.canMove(dir)) {
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
     }
 
     class LocationGoalWithBugging extends BuggingGoal {
 
         public MapLocation location;
         
         public LocationGoalWithBugging(MapLocation location) {
             super();
             this.location = location;
         }
 
         public Direction getDirectionToGoal() {
             return controller.getLocation().directionTo(location);
         }
 
         public boolean done() {
             completed = completed || controller.getLocation().equals(location);
             return completed;
         }
     }
 
     class ArchonGoal extends NavigationGoal {
 
         public Direction getDirection() {
             MapLocation location = sensing.senseClosestArchon();
             Direction dir = controller.getLocation().directionTo(location);
             return getMoveableDirection(dir);
         }
 
         public boolean done() {
             completed = completed || isAdjacent(controller.getLocation(), sensing.senseClosestArchon());
             return completed;
         }
     }
 
     class ArchonGoalWithBugging extends BuggingGoal {
         public ArchonGoalWithBugging() {
             super();
         }
 
         public Direction getDirectionToGoal() {
             MapLocation location = sensing.senseClosestArchon();
             new BugPlanner(location);
             pr(location.toString());
             pr(controller.getLocation().directionTo(location).toString());
             return controller.getLocation().directionTo(location);
         }
 
         public boolean done() {
             completed = completed || isAdjacent(controller.getLocation(), sensing.senseClosestArchon());
             return completed;
         }
     }
 
     class ClosestTeleporterGoal extends NavigationGoal {
         public MapLocation tower = null;
         
         public Direction getDirection() {
             ArrayList<MapLocation> locations = sensing.senseAlliedTeleporters();
             tower = findClosest(locations);
             Direction dir = controller.getLocation().directionTo(tower);
             return getMoveableDirection(dir);
         }
 
         public boolean done() {
             ArrayList<MapLocation> loc = sensing.senseAlliedTeleporters();
             
             if(loc.isEmpty()) return true;
 
             //done can be called before getDirection, which means there is no cached tower
             if(tower == null) getDirection();
 
             //shenanigans
             if(tower == null) return true;
 
             // we are finished when we are in broadcast range
             if(controller.getLocation().distanceSquaredTo(tower) <= Math.pow(controller.getRobotType().broadcastRadius()-1, 2))
                 return true;
 
             return false;
         }
     }
 
     class BugPlanner {
         public boolean[][] terrain;
         public MapLocation goal, start, current;
         public Direction currentDirection, originalDirection;
         public int width, height, currentX, currentY;
         public boolean tracing, tracingLeft;
 
         public BugPlanner(MapLocation goal) {
             this.goal = goal;
             start = controller.getLocation();
             currentDirection = controller.getDirection();
             terrain = map.boolMap;
             
             height = terrain.length;
             width = terrain[0].length;
 
             int turn = Clock.getRoundNum();
             LinkedList<BuggingState> path = planPath();
            int size = path.size();
            int doneTurn = Clock.getRoundNum();
             int turns = (Clock.getRoundNum() - turn);
            optimizePath(path);
            int oturns = Clock.getRoundNum() - doneTurn;
            System.out.println("Path took: "+turns+".  Optimization took: "+oturns+"  OriginalSize:  "+size+"  OptimizedSize:  "+path.size());
         }
 
         public boolean canMove(int x, int y) {
             return !terrain[x%width][y%height];
         }
 
         public LinkedList<BuggingState> planPath() {
             LinkedList<BuggingState> path = new LinkedList<BuggingState>();
             Direction dir;
             BuggingState currentState;
             int pathLength = 0;
             currentX = start.getX();
             currentY = start.getY();
             tracing = false;
 
             current = start;
             currentState = new BuggingState(currentDirection, current);
             path.add(currentState);
 
             while(true) {
                 if(pathLength > 300) {
                     System.out.println("Path to big");
                     break;
                 }
 
                 if(current.equals(goal)) {
                     //System.out.println("Arrived");
                     break;
                 }
 
                 dir = getNextDirection();
 
                 if(dir != currentDirection) {
                     currentState = new BuggingState(dir, current);
                     path.add(currentState);
                 }
                 currentState.count += 1;
                 currentState.end = current = current.add(dir);
                 currentX += dir.dx;
                 currentY += dir.dy;
                 currentDirection = dir;
 
                 pathLength++;
             }
 
             return path;
         }
 
        public void optimizePath(LinkedList<BuggingState> path) {
            BuggingState start, goal;
            for(int c = 0; c < path.size()-2; c++) {
                start = path.get(c);
                goal = path.get(c+2);

                //try to go straight from start to goal
                if(canGo(start.start, goal.start)) {
                    path.remove(c+1);
                    c--;
                }
            }
        }

        public boolean canGo(MapLocation start, MapLocation goal) {
            while(!start.equals(goal)) {
                start = start.add(start.directionTo(goal));
                if(!canMove(start.getX(), start.getY())) return false;
            }
            return true;
        }

         public Direction getDirectionToGoal() {
             return current.directionTo(goal);
         }
 
         public Direction getNextDirection() {
             Direction dir = getDirectionToGoal();
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
 
     class BuggingState {
         public MapLocation start, end;
         public Direction dir;
         public int count;
 
         public BuggingState (Direction dir, MapLocation start) {
             count = 0;
             this.start = start;
             this.end = start;
             this.dir = dir;
         }
 
         public String toString() {
             return "\n"+start+" for "+count+" turns in direction "+dir;
         }
     }
 }
