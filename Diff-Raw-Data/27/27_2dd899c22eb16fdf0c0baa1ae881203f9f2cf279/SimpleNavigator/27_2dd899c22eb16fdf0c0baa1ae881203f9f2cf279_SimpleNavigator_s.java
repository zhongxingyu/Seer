 package current;
 
 import battlecode.common.*;
 
 public class SimpleNavigator {
	
 	private BaseRobot r;
 
 	private int closestAttainedDist;
 	private int closestAttainedDistRoundNum;
 	private int mineCost = 6;
 	private MapLocation curLoc;
 	private Direction curHeadingDir;
 	
 	SimpleNavigator(BaseRobot robot){
 		this.r = robot;
 		closestAttainedDist = 99999999;
 	}
 	
	public boolean moveTo(MapLocation goal){
 		curLoc = r.rc.getLocation();
 		if (curLoc.equals(goal))
 		{
 			closestAttainedDist = 99999999;
			return Direction.OMNI;
 		}
 		
 		Direction optimalDir = curLoc.directionTo(goal);
		int distToGoal = Utilities.getDistanceBetween(curLoc, goal);
 		if (distToGoal < closestAttainedDist)
 		{
 			closestAttainedDist = distToGoal;
 			closestAttainedDistRoundNum = Clock.getRoundNum();
 		}
 		else {
 			// Check if we've not made any progress towards the goal for a while.
			if (distToGoal > closestAttainedDist && (Clock.getRounNum() - closestAttainedDistNum > mineCost)){
 				// We are getting frustrated. Will move in optimalDir no matter what.
 				curHeadingDir = optimalDir;
 				if (forceStep(optimalDir))
 					return true;
 				else
 					return false;	// move impossible.
 			}
 		}
 		
 		// If we have gotten here, then we are not desperate to move in dirs[0]
 		// Let's take the most relevant step possible towards the goal.
 		return takeSafeRelevantStep(optimalDir, curHeadingDir);
 	}
 	
	public boolean takeSafeRelevantStep(Direction optimalDir, Direction avoidDir)
 	{		
 		Direction dir1 = optimalDir;
 		Direction dir2 = optimalDir.rotateRight();
 		
 		for (int i = 0; i < 3; i++) {
 			if (dir1 != avoidDir){
 				if (takeSafeStep(dir1) == 0)
 				{
 					curHeadingDir = dir1;
 					return true;
 				}
 			}
 			if (dir2 != avoidDir){
 				if (takeSafeStep(dir2) == 0)
 				{
 					curHeadingDir = dir2;
 					return true;
 				}
 			}
 			dir1 = dir1.rotateLeft();
 			dir2 = dir2.rotateRight();
 		}
 		return false;
 	}
 	
 	public boolean forceStep(Direction dir) throws GameActionException {
 		int safeStepRes = takeSafeStep(dir);
 		if (safeStepRes == 0)
 			return true;
 		else if (safeStepRes == 1){
 			if (forceStepOntoMine(dir, true))
 				return true;
 		}
 		return false;
 	}
 	
 	public int takeSafeStep(Direction dir) throws GameActionException {
 		MapLocation dest = curLoc.add(dir);
 		Team mine = r.rc.senseMine(dest);
 		if (r.rc.canMove(dir) && (mine == r.rc.getTeam() || mine == null))
 		{
 			r.rc.move(dir);
 			return 0;
 		}
 		else if (mine == r.rc.getTeam().opponent() || mine == Team.NEUTRAL)
 		{
 			return 1;	// there is a mine (status code 0x01)
 		}
 		else
 		{
 			return 2;	// there is an obstacle (enemy/friendly robot)
 		}
 	}
 	
 	public boolean forceStepOntoMine(Direction dir, boolean doDefuse) throws GameActionException {
 		if (r.rc.canMove(dir)){
 			if (doDefuse)
 				r.rc.defuseMine(curLoc.add(dir));
 			r.rc.move(dir);
 			return true;
 		} else
 			return false;
 	}
 }
