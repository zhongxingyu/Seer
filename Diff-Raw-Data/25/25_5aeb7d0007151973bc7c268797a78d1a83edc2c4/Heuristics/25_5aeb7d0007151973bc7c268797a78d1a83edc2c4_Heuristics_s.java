 
 public class Heuristics {
 	
 	public static int calculateHeuristic(State state) {
 		int distance = 0;
 		
 		for(BoardPosition boxPos : state.boxPositions) {
			int minDist = 0;
 			
 			for(BoardPosition goalPos : Board.goalPositions) {
 				int currDist = 0;
 				currDist += Math.abs(boxPos.row-goalPos.row);
 				currDist += Math.abs(boxPos.col-goalPos.col);
 				
				if(currDist > minDist) {
 					minDist = currDist;
 				}
 			}
 			
 			distance += minDist;
 		}
 		
		return -distance;
 	}
 }
