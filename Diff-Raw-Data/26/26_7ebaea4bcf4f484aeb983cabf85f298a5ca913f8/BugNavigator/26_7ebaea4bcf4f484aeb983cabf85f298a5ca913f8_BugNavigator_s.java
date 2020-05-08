 package newTeam.handler.navigation;
 
 import newTeam.common.QuantumConstants;
 import newTeam.common.util.Logger;
 import newTeam.common.Knowledge;
 import battlecode.common.Direction;
 import battlecode.common.MapLocation;
 import battlecode.common.MovementController;
 import battlecode.common.RobotController;
 
 public class BugNavigator implements Navigator {
     
     private final RobotController myRC;
     private final Knowledge myK;
     private final MovementController myMC;
     
     private final int MEMORY_LENGTH = QuantumConstants.BUG_MEMORY_LENGTH;
     
     private        MapLocation          goal;
     private        boolean              navigating = false;
     private        boolean              goingToAdjacent = false;
     private        Direction            movementDirection = Direction.NONE;
     
     /*** Bug Navigation variables ***/
     private        boolean              tracking;
     private        boolean              trackingCW;
     private        boolean              startTracking;
     private        Direction            trackingDirection;
     private        Direction            prevTrackingDirection;
     private        Direction            prevDirectionToGoal;
     private        MapLocation          prevLocation;
     private        int                  turningNumber;
     private        MapLocation          bugStart;
     private        MapLocation []       bugPrevLocations;  
     private        Direction []         bugPrevDirections; 
     private        boolean []           bugPrevCW;
     private        int                  bugStep;
     
     public BugNavigator(RobotController rc, Knowledge know, MovementController mc) {
         myRC = rc;
         myK = know;
         myMC = mc;
     }
 
     public MovementAction getNextAction() {
         // do nothing if the motor is active or you are not navigating
         if(!navigating || myMC.isActive()) {
             return MovementAction.NONE;
         }
 
         MovementAction action;
         
         if(goingToAdjacent) {
             action = navigateToAdjacent();
         } else if(reachedGoal()) {
             navigating = false;
             action = MovementAction.NONE;
         } else {
             action = navigateBug();
         }
         
         prevLocation = myK.myLocation;
         return action;
 
     }
 
     public Direction getMovementDirection() {
         return movementDirection;
 //        if(tracking) {
 //            return trackingDirection;
 //        } else {
 //            return myK.myLocation.directionTo(goal);
 //        }
     }
 
     public void setGoal(MapLocation g) {
         goal = g;
     }
 
     public boolean reachedGoal() {
         return goal.equals(myK.myLocation);
     }
     
     public int distanceSquaredToGoal(MapLocation fromHere) {
         return fromHere.distanceSquaredTo(goal);
     }
     
     public void setGoingToAdjacent(boolean b) {
         goingToAdjacent = b;
     }
     
     public void initiateNavigation(MapLocation newGoal) {
         goal = newGoal;
         navigating = true;
         tracking = false;
         startTracking = false;
         bugStart = myK.myLocation;
         prevDirectionToGoal = bugStart.directionTo(goal);
         bugPrevLocations = new MapLocation [MEMORY_LENGTH];
         bugPrevDirections = new Direction [MEMORY_LENGTH];
         bugPrevCW = new boolean [MEMORY_LENGTH];
         bugStep = 0;
     }
     
     public void initiateNavigation() {
         initiateNavigation(goal);
     }
     
     /******************* Private Methods ********************/
 
     private MovementAction navigateBug() {
         MapLocation location = myK.myLocation;
         Direction directionToGoal = location.directionTo(goal);
         int bugPos = bugStep % MEMORY_LENGTH;
         MovementAction action;
         
         // if you have moved between calls to navigateBug, set the previous tracking direction and 
         // the previous direction to goal
         if(!prevLocation.equals(location)) {
             prevTrackingDirection = prevLocation.directionTo(location);
             prevDirectionToGoal = prevLocation.directionTo(goal);
         }
         
         if(tracking) {
 
             if(directionToGoal != prevDirectionToGoal) {
                 // changing goal contributes negatively to the turning number.
                 turningNumber -= calculateTurningChange(prevDirectionToGoal, directionToGoal, trackingCW);
             }
             
             //set the initial direction to begin testing from.
             Direction startDirection = prevTrackingDirection.opposite();
             Direction testDirection = startDirection;
             
             // check directions beginning with the reference direction, in an order depending on 
             // if you are tracking clockwise or counterclockwise
             if(startTracking) {
                 startTracking = false;
             } else {
                 turningNumber -= 4;
             }
             boolean pathBlocked = true;
             while(pathBlocked) {
                 //increment direction
                 if(trackingCW) {
                     testDirection = testDirection.rotateLeft();
                 } else {
                     testDirection = testDirection.rotateRight();
                 }
                 
                 //update turning number
                 turningNumber++;
                 
                 if(myMC.canMove(testDirection)){
                     trackingDirection = testDirection;
                     pathBlocked = false;
                 } else if(testDirection == startDirection) {
                     turningNumber -= 4;
                     break;
                 }
             }
 
             //check if you are finished tracking
             if(turningNumber <= 0) {
                 tracking = false;
             }
             
             movementDirection = trackingDirection;
             action = getBugAction(trackingDirection);
             
         } else {
             //not tracking, so check if you can move towards the goal
             if(myMC.canMove(directionToGoal)) {
                 movementDirection = directionToGoal;
                 action = getBugAction(directionToGoal);
             } else {
                 
                 //the path is blocked, so begin tracking.
                 tracking = true;
                 startTracking = true;
                 bugPos = (bugPos + 1) % QuantumConstants.BUG_MEMORY_LENGTH;
                 bugPrevLocations[bugPos] = location;
             
                 //determine if you should track around the obstacle clockwise or counterclockwise
                 Direction ccwDir = directionToGoal;
                 Direction cwDir = directionToGoal;
                 boolean searching = true;
                 while(searching) {
                     ccwDir = ccwDir.rotateRight();
                     cwDir = cwDir.rotateLeft();
                     if(myMC.canMove(ccwDir)) {
                         bugPrevDirections[bugPos] = ccwDir;
                         trackingCW = false;
                         searching = false;
                         if(myMC.canMove(cwDir) && location.add(cwDir).distanceSquaredTo(goal) 
                                 < location.add(ccwDir).distanceSquaredTo(goal)) {
                             bugPrevDirections[bugPos] = cwDir;
                             trackingCW = true;
                         }
                     } else if(myMC.canMove(cwDir)) {
                         bugPrevDirections[bugPos] = cwDir;
                         trackingCW = true;
                         searching = false;
                     }
                         
                     //stop searching if you are surrounded
                     if(ccwDir == cwDir) {
                         bugPrevDirections[bugPos] = ccwDir;
                         break;
                     }
                 }
                 
                 bugPrevCW[bugPos] = trackingCW;
                 trackingDirection = bugPrevDirections[bugPos];
                 prevTrackingDirection = directionToGoal.opposite();
                 turningNumber = calculateTurningChange(directionToGoal, trackingDirection, trackingCW);
                 movementDirection = trackingDirection;
                 action = getBugAction(trackingDirection);
             }
         }
         
         return action;
     }
     
     private int calculateTurningChange(Direction oldDir, Direction newDir, boolean CW) {
         int turn = -4;
         Direction testDir = oldDir.opposite();
         while(testDir != newDir) {
             turn++;
             if(CW) {
                 testDir = testDir.rotateLeft();
             } else {
                 testDir = testDir.rotateRight();
             }
         }
         return turn;
     }
     
     private MovementAction getBugAction(Direction dir) {
         MovementAction action;
         if(myK.myDirection == dir)
             action = MovementAction.MOVE_FORWARD;
 //        else if(knowledge.myDirection == dir.opposite())
 //            action = MovementAction.MOVE_BACKWARD;
         else
             action = MovementAction.ROTATE;
         
         return action;
     }
     
     private MovementAction navigateToAdjacent() {
       
         MapLocation location = myK.myLocation;
         int distanceSquaredToGoal = location.distanceSquaredTo(goal);
         
         if(distanceSquaredToGoal == 0) {
             Direction moveDir = myK.myDirection;
             boolean searching = true;
             while(searching){
                 if(myMC.canMove(moveDir)) {
                     searching = false;
                 } else {
                     moveDir = moveDir.rotateRight();
                 }
                 
                 if(searching && moveDir == myK.myDirection) {
                     moveDir = Direction.NONE;
                     searching = false;
                 }
             }
             
             if(moveDir == myK.myDirection) {
                 movementDirection = myK.myDirection;
                 return MovementAction.MOVE_FORWARD;
             } else if(moveDir == myK.myDirection.opposite()) {
                 movementDirection = myK.myDirection.opposite();
                 return MovementAction.MOVE_BACKWARD;
             } else if(moveDir != Direction.NONE) {
                 movementDirection = moveDir;
                 return MovementAction.ROTATE;
             } else {
                 movementDirection = Direction.NONE;
                 return MovementAction.NONE;
             }
             
         } else if(distanceSquaredToGoal <= 2) {
             navigating = false;
             goingToAdjacent = false;
             
             Direction directionToGoal = location.directionTo(goal);
             movementDirection = directionToGoal;
             if(directionToGoal == myK.myDirection || directionToGoal == Direction.OMNI) {
                 return MovementAction.NONE;
             } else {
                 return MovementAction.ROTATE;
             }
         }    
             
         return navigateBug();
 
     }
 }
