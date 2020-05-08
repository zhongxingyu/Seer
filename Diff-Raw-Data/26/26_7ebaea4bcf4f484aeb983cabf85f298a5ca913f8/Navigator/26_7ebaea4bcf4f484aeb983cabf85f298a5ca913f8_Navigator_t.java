 package newTeam.handler.navigation;
 
 import battlecode.common.Direction;
 import battlecode.common.MapLocation;
 
 public interface Navigator {
 
     public MovementAction getNextAction();
     
     public Direction getMovementDirection();
     
     public void setGoal(MapLocation g);
     
     public boolean reachedGoal();
     
     public int distanceSquaredToGoal(MapLocation fromHere);
     
     public void setGoingToAdjacent(boolean b);
     
     public void initiateNavigation(MapLocation newGoal);
     
     public void initiateNavigation();
     
    public void pauseNavigation();
    
 }
