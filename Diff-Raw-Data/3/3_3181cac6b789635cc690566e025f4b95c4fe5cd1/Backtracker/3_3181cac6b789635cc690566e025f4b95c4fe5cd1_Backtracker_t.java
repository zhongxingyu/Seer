 package mirroruniverse.g2;
 
 import java.util.List;
 
 import mirroruniverse.g2.astar.BacktrackingAStar;
 import mirroruniverse.g2.astar.State;
 
 public class Backtracker {
 	private BacktrackingAStar Astar;
 	Map leftMap;
 	Map rightMap;
 	private List<State> path;
 	
 	public Backtracker(Map leftMap, Map rightMap, Position leftTarget, Position rightTarget) {
 		this.leftMap = leftMap;
 		this.rightMap = rightMap;
 		this.Astar = new BacktrackingAStar(leftMap, rightMap, leftTarget, rightTarget);
 		path = this.Astar.compute(new State(leftMap.playerPos, rightMap.playerPos));
		if (path != null)
			path.remove(0);
 	}
 	
 	public boolean pathFound() {
 		return path != null && path.size() != 0;
 	}
 	
 	public int getMove() {
 		if (path.size() == 0) {
 			System.out.println("Why?!");
 		}
 		
 		State from = new State(leftMap.playerPos, rightMap.playerPos);
 		State to = path.remove(0);
 		return RouteFinder.computeMove(from, to);
 	}
 }
