 package heuristics;
 
 import edificios.Board;
 import edificios.BuildingState;
 import gps.api.GPSState;
 
 public class SimpleHeuristic extends Heuristic{
 	
 	@Override
 	public Integer getH(GPSState state) {
 		Board board = ((BuildingState) state).getCurrentBoard();
 		if (checkNoSolution(board)) {
 			return Integer.MAX_VALUE;
 		}
 		int n = board.getSize();
		return n * n - board.getBuildingsOnBoard();
 	}
 
 }
