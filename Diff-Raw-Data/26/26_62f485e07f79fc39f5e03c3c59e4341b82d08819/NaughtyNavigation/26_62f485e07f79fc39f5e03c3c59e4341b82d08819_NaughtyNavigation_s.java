 package team298;
 
 import battlecode.common.*;
 import static battlecode.common.GameConstants.*;
 import java.util.*;
 
 public class NaughtyNavigation extends Base {
 
     public MapStore map;
     public MexicanMessaging messaging;
     public SensationalSensing sensing;
     public NavigationGoal goal;
     public FollowArchonGoal followArchonGoal;
     public LinkedList<NavigationGoal> goalStack;
 
     public NaughtyNavigation(NovaPlayer player) {
         super(player);
         map = player.map;
         messaging = player.messaging;
         goal = null;
         followArchonGoal = null;
         goalStack = new LinkedList<NavigationGoal>();
     }
 
     public MapLocation findClosest(ArrayList<MapLocation> locations) {
         MapLocation closest = null, current = controller.getLocation();
         int min = Integer.MAX_VALUE, distance;
 
         for (MapLocation location : locations) {
             if (location == null) {
                 continue;
             }
             distance = current.distanceSquaredTo(location);
             if (distance < min) {
                 closest = location;
                 min = distance;
             }
         }
         return closest;
     }
         public MapLocation findFurthest(ArrayList<MapLocation> locations) {
             MapLocation furthest = null, current = controller.getLocation();
             int min = Integer.MIN_VALUE, distance;
 
             for (MapLocation location : locations) {
                 if (location == null) {
                     continue;
                 }
                 distance = current.distanceSquaredTo(location);
                 if (distance > min) {
                     furthest = location;
                     min = distance;
                 }
             }
 
         return furthest;
     }
 
     public MapLocation findClosest(MapLocation[] locations) {
         MapLocation closest = null, current = controller.getLocation();
         int min = Integer.MAX_VALUE, distance;
 
         for (MapLocation location : locations) {
             if (location == null) {
                 continue;
             }
             distance = current.distanceSquaredTo(location);
             if (distance < min) {
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
         if (dir == null) {
             return Status.fail;
         }
 
         if (controller.getDirection().equals(dir)) {
             return Status.success;
         }
 
         if (dir.equals(Direction.OMNI)) {
             return Status.success;
         }
 
         while (controller.hasActionSet() || controller.getRoundsUntilMovementIdle() != 0) {
             controller.yield();
         }
 
         try {
             controller.setDirection(dir);
             controller.yield();
             return Status.success;
         } catch (Exception e) {
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
         Direction newDir = controller.getLocation().directionTo(location);
         return faceDirection(newDir);
     }
 
     /**
      * Returns location of an Archon Leader who the unit should follow
      */
     public MapLocation findArchonLeader(int desiredArchonID) {
         MapLocation currentLocation = controller.getLocation();
         MapLocation[] archonLocations = sensing.senseArchonLocations();
         Robot possibleLeader = null;
 
         for (int i = 0; i < archonLocations.length; ++i) {
             try {
                 possibleLeader = controller.senseAirRobotAtLocation(archonLocations[i]);
                 if (possibleLeader.getID() == desiredArchonID) {
                     return archonLocations[i];
                 }
             } catch (Exception e) {
                 pa("----Caught exception in findArchonLeader()");
             }
         }
         return null;
     }
 
     /**
      * Returns the first direction that the robot can move in, starting with the given direction.
      */
     public Direction getMoveableDirection(Direction dir) {
         if (dir == null) {
             return null;
         }
         Direction leftDir = dir, rightDir = dir;
         if (controller.canMove(dir)) {
             return dir;
         } else {
             for (int d = 0; d < 3; d++) {
                 leftDir = leftDir.rotateLeft();
                 rightDir = rightDir.rotateRight();
 
                 if (controller.canMove(leftDir)) {
                     return leftDir;
                 }
                 if (controller.canMove(rightDir)) {
                     return rightDir;
                 }
             }
         }
         return null;
     }
 
     public Direction getMoveableArchonDirection(Direction dir) {
         //pa("getMoveableArchonDirection() called");
         if (dir == null) {
             return null;
         }
         Direction leftDir = dir, rightDir = dir;
         if (controller.canMove(dir) && map.onMap(controller.getLocation().add(dir))) {
             return dir;
         } else {
             for (int d = 0; d < 3; d++) {
                 leftDir = leftDir.rotateLeft();
                 rightDir = rightDir.rotateRight();
 
                 if (controller.canMove(leftDir) && map.onMap(controller.getLocation().add(leftDir))) {
                     return leftDir;
                 }
                 if (controller.canMove(rightDir) && map.onMap(controller.getLocation().add(rightDir))) {
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
 
     /**
      * Returns true if there is no Air or Ground unit at the given location.
      * If a robot is blind (channeler), this method should not be called.  It does
      * not check if the robot can sense at that location.
      */
     public boolean isLocationFree(MapLocation location, boolean isAirUnit) {
         try {
             if (!map.onMap(location)) {
                 return false;
             }
 
             if (!controller.canSenseSquare(location)) {
                 return true;
             }
 
             TerrainTile tile = map.get(location);
             if (tile != null && !tile.isTraversableAtHeight((isAirUnit ? RobotLevel.IN_AIR : RobotLevel.ON_GROUND))) {
                 return false;
             }
 
             if (isAirUnit) {
                 return controller.senseAirRobotAtLocation(location) == null;
             } else {
                 return controller.senseGroundRobotAtLocation(location) == null;
             }
         } catch (Exception e) {
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
         if (!block && (controller.hasActionSet() || controller.getRoundsUntilMovementIdle() > 0)) {
             return Status.turnsNotIdle;
         }
 
         if (goal == null) {
             return Status.noGoal;
         }
         if (goal.done()) {
             return Status.success;
         }
 
         Direction dir = goal.getDirection();
 
         if (faceDirection(dir) != Status.success) {
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
             if (controller.canMove(dir)) {
 
                 controller.moveForward();
                 controller.yield();
                 player.pathStepTakenCallback();
                 return Status.success;
             }
             return Status.cantMoveThere;
         } catch (Exception e) {
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
         while (controller.hasActionSet() || controller.getRoundsUntilMovementIdle() != 0) {
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
         if (goalStack.size() > 0) {
             goal = goalStack.removeFirst();
         }
     }
 
     /**
      * Saves the current goal onto the stack so it can be restored later.
      */
     public void pushGoal(boolean removePreviousGoals) {
         if (removePreviousGoals) {
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
         if (goal instanceof MoveableDirectionGoal) {
             return;
         }
         pushGoal(removePreviousGoals);
         goal = new MoveableDirectionGoal();
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
     public void changeToWoutCollectingFluxGoal(boolean removePreviousGoals) {
         pushGoal(removePreviousGoals);
         goal = new WoutCollectingFluxGoal();
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
         else goal = new LocationGoalWithBugPlanning(location);
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
         if(goal instanceof ArchonGoal || goal instanceof ArchonGoalWithBugPlanning) return;
         pushGoal(removePreviousGoals);
         if(player.isArchon) goal = new ArchonGoal();
         else goal = new ArchonGoalWithBugPlanning();
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
         if (goal instanceof ClosestTeleporterGoal) {
             return;
         }
         pushGoal(removePreviousGoals);
         goal = new ClosestTeleporterGoal();
     }
 
     public void changeToFollowingArchonGoal(int archonID, boolean removePreviousGoals) {
         if (goal instanceof FollowArchonGoal) {
             return;
         }
         pushGoal(removePreviousGoals);
         goal = new FollowArchonGoal();
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
 
     class MoveableDirectionGoal extends FollowArchonGoal {
 
         public Direction getDirection() {
             if(player.isArchon) {
                 ArrayList<MapLocation> enemies = sensing.senseEnemyRobotLocations();
                 if(enemies.size() > 0) {
                     MapLocation closest = findClosest(enemies);
                     int distance = closest.distanceSquaredTo(controller.getLocation());
                     if(distance >= 15 && distance <= 20) {
                         return null;
                     } else if(distance > 20) {
                         return getMoveableArchonDirection(controller.getLocation().directionTo(closest));
                     } else {
                         return getMoveableArchonDirection(closest.directionTo(controller.getLocation()));
                     }
                 } else {
                     // TODO: Change this to get if archon is leader
                     if (((ArchonPlayer)(player)).archonNumber == 1) {
                         return getMoveableArchonDirection(controller.getDirection());
                     } else {
                          return archonDirection;
                     }
                 }
             }
             else {
                 return getMoveableArchonDirection(controller.getDirection());
             }
         }
 
         public boolean done() {
             return false;
         }
     }
 
     class WoutCollectingFluxGoal extends NavigationGoal {
 
         public Direction getDirection() {
             if(controller.getRoundsUntilMovementIdle() > 1) return null;
 
             ArrayList<MapLocation> enemies = sensing.senseEnemyRobotLocations();
             if(enemies.size() > 0) {
                 //p("Enemy");
                 MapLocation closest = findClosest(enemies);
                 int distance = closest.distanceSquaredTo(controller.getLocation());
                 if(distance < 15) {
                     return getMoveableArchonDirection(closest.directionTo(controller.getLocation()));
                 } else {
                     return getMoveableArchonDirection(controller.getLocation().directionTo(closest));
                 }
             } else {
                 //try to move in the current direction first
                 Direction currentDirection = controller.getDirection();
                 int dx = currentDirection.dx*3, dy = currentDirection.dy*3;
                 MapLocation location = controller.getLocation();
                 MapLocation newLocation = new MapLocation(location.getX()+dx, location.getY()+dy);
                 try {
                     if(controller.senseFluxAtLocation(newLocation) > 1) {
                         //p("Going straight: "+newLocation);
                         return getMoveableDirection(currentDirection);
                     }
                 } catch (Exception e) {
                     pa("----Caught exception in WoutCollectingFluxGoal "+e.toString());
                 }
 
                 //alright, we cant go straight, lets see whats best
                 int[][] fluxDeltas = ((WoutPlayer)player).fluxDeltas;
                 int[] cur, min=null;
                 int minAmount = -1, curAmount;
                 for(int c = 0; c < fluxDeltas.length; c++) {
                     //p(fluxDeltas[c][0]+", "+fluxDeltas[c][1]+":  "+fluxDeltas[c][2]);
                     cur = fluxDeltas[c];
                     curAmount = cur[2];
                     if(curAmount > minAmount) {
                         minAmount = curAmount;
                         min = cur;
                     }
                 }
 
                 //p("MIN: "+min[0]+", "+min[1]+", "+min[2]);
 
                 if(minAmount > 1) {
                     location = new MapLocation(location.getX()+min[0], location.getY()+min[1]);
                     //p("Returning: "+getMoveableDirection(controller.getLocation().directionTo(location)));
                     return getMoveableDirection(controller.getLocation().directionTo(location));
                 } else {
                     //p("Returning2: "+getMoveableDirection(controller.getDirection()));
                     return getMoveableDirection(controller.getDirection());
                 }
             }
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
             completed = controller.getLocation().equals(location);
             return completed;
         }
     }
 
     class FollowArchonGoal extends NavigationGoal {
 
         public MapLocation archonLocation;
         public int archonID = -1;
         public Direction archonDirection;
 
         public FollowArchonGoal() {
             followArchonGoal = this;
         }
 
         public Direction getDirection() {
             //archonLocation = findArchonLeader(archonID
             //getMoveableDirection(dir);
             return archonDirection;
         }
 
         public boolean done() {
             return false;
         }
 
         public void updateArchonGoal(MapLocation location, int archonID) {
             if (this.archonID == -1) {
                 this.archonID = archonID;
             }
             if (archonID == this.archonID) {
                 if (archonLocation == null) {
                     archonLocation = location;
                     archonDirection = controller.getLocation().directionTo(location);
                 } else {
                     archonDirection = archonLocation.directionTo(location);
                     if (archonDirection == Direction.EAST) {
                         if (controller.getLocation().getX() >= location.getX()) {
                             archonDirection = null;
                         }
                     } else if (archonDirection == Direction.NORTH) {
                         if (controller.getLocation().getY() <= location.getY()) {
                             archonDirection = null;
                         }
                     } else if (archonDirection == Direction.NORTH_EAST) {
                         if (controller.getLocation().getY() <= location.getY() && controller.getLocation().getX() >= location.getX()) {
                             archonDirection = null;
                         }
                     } else if (archonDirection == Direction.NORTH_WEST) {
                         if (controller.getLocation().getY() <= location.getY() && controller.getLocation().getX() <= location.getX()) {
                             archonDirection = null;
                         }
                     } else if (archonDirection == Direction.OMNI) {
                         archonDirection = null;
                     } else if (archonDirection == Direction.SOUTH) {
                         if (controller.getLocation().getY() >= location.getY()) {
                             archonDirection = null;
                         }
                     } else if (archonDirection == Direction.SOUTH_EAST) {
                         if (controller.getLocation().getY() >= location.getY() && controller.getLocation().getX() >= location.getX()) {
                             archonDirection = null;
                         }
                     } else if (archonDirection == Direction.SOUTH_WEST) {
                         if (controller.getLocation().getY() >= location.getY() && controller.getLocation().getX() <= location.getX()) {
                             archonDirection = null;
                         }
                     } else if (archonDirection == Direction.WEST) {
                         if (controller.getLocation().getX() <= location.getX()) {
                             archonDirection = null;
                         }
                     } else {
                         // It was none?
                     }
                     archonLocation = location;
                     this.archonID = archonID;
                 }
             }
         }
     }
 
     
     class LocationGoalWithBugPlanning extends BendoverBugging {
 
         public MapLocation location;
 
         public LocationGoalWithBugPlanning(MapLocation location) {
             super(controller, map);
             this.location = location;
         }
 
         public MapLocation getGoal() {
             return location;
         }
 
         public boolean done() {
             completed = controller.getLocation().equals(location);
             return completed;
         }
     }
 
     class ArchonGoalWithBugPlanning extends BendoverBugging {
         public ArchonGoalWithBugPlanning() {
             super(controller, map);
         }
 
         public MapLocation getGoal() {
             return sensing.senseClosestArchon();
         }
 
         public boolean done() {
             completed = isAdjacent(controller.getLocation(), sensing.senseClosestArchon());
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
             completed = isAdjacent(controller.getLocation(), sensing.senseClosestArchon());
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
 
             if (loc.isEmpty()) {
                 return true;
             }
 
             //done can be called before getDirection, which means there is no cached tower
             if (tower == null) {
                 getDirection();
             }
 
             //shenanigans
             if (tower == null) {
                 return true;
             }
 
             // we are finished when we are in broadcast range
             if (controller.getLocation().distanceSquaredTo(tower) <= Math.pow(controller.getRobotType().broadcastRadius() - 1, 2)) {
                 return true;
             }
 
             return false;
         }
     }
 
     
 }
