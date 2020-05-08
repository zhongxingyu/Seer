 package current;
 
 /*
 A collection of useful algorithms!
 - int genRandFromClosedInterval(min, max)
 - int getDistanceBetween(MapLocation1, MapLocation2)
 - swap(List, Idx1, Idx2)
 - getNearestNRobots( ... )
 
 */
 
 public class Utilities {
 	
 	private BaseRobot r;
 	
 	Utilities(BaseRobot robot){
 		this.r = robot;
 	}
 	
 	public int genRandFromClosedInterval(int min, int max){
 		return (int)(min + Math.random() * (max - min));
 	}
 	
 	public int getDistanceBetween(MapLocation loc1, MapLocation loc2){
 		return Math.max(Math.abs(loc1.x - loc2.x), Math.abs(loc1.y - loc2.y));
 	}
 	
	public void swap(<T>[] lst, int idx1, int idx2)
 	{
		<T> tmp = lst[idx1];
 		lst[idx1] = lst[idx2];
 		lst[idx2] = tmp;
 	}
 	
 	public class RobotIdDistPair {
 		int id;
 		int dist;
 	}
 	
	public RobotIdDistPair[] getNearestNRobots(int N, MapLocation c, int r2, Team t){
 		Robot[] robots = r.rc.senseNearbyGameObjects(Robot.class, c, r2, t);
 		int totalRobotNum = robots.length;
 		RobotIdDistPair[] rdPairs = new RobotIdDistPair[totalRobotNum];
 		for (int i = 0; i < totalRobotNum; i++)
 		{
 			rdPairs[i].id = robots[i].getID();
 			rdPairs[i].dist = getDistanceBetween(r.rc.senseLocationOf(robots[i]), c);
 		}
 		_qsNearestNRobots(rdPairs, N, 0, totalRobotNum - 1);
 		return rdPairs;
 	}
 	
 	private void _qsNearestNRobots(RobotIdDistPair[] rdPairs, int N, int idxLeft, int idxRight){
 		if (idxRight < idxLeft)
 			return;
 		int idxPivot = genRandFromClosedInterval(idxLeft, idxRight);
 		
 		// Partition around idxPivot
 		RobotIdDistPair pivotVal = rdPairs[idxPivot];
		swap(rdPairs[idxPivot], rdPairs[idxRight]);
 		int idxCur = idxLeft;
 		for (int i = idxLeft; i < idxRight; i++){
 			if (rdPairs[i].dist < pivotVal.dist)
 			{
				swap(rdPairs[i], rdPairs[idxCur]);
 				idxCur++;
 			}
 		}
		swap(rdPairs[idxCur], rdPairs[idxRight]);
 		
 		// Recurse on relevant sublist
 		_qsNearestNRobots(rdPairs, N, idxLeft, idxCur - 1);
 		if (idxCur < idxLeft + N)
 			_qsNearestNRobots(rdPairs, N - (idxCur - idxLeft) - 1, idxCur + 1, idxRight);
 	}
 }
