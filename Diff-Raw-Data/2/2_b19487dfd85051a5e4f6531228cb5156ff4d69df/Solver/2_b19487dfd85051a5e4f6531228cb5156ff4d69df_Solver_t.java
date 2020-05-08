 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.PriorityQueue;
 
 public class Solver {
 	
 	public static String solve(ArrayList<String> lines) {
 		Board board = new Board(lines);
 		System.out.println(board.toString());
 		
 		State solvedState = aStar();
 		String revSoloution = solvedState.backtrackSolution();
 		new Guireplay(solvedState);
 		
 		return  new StringBuffer(revSoloution).reverse().toString();
 	}
 
 	private static State aStar() {
 		HashSet<State> visited = new HashSet<State>();
 		PriorityQueue<State> nodesLeft = new PriorityQueue<State>();
 		List<State> childStates = new LinkedList<State>();
 		
 		nodesLeft.add(Board.initialState);
 		
 		while(!nodesLeft.isEmpty()) {
 			State parent = nodesLeft.poll();
 			visited.add(parent);
 			
 			
 			parent.getPushStates(childStates);
 			
 			for(State child : childStates) {
 				
 				if(visited.contains(child)) {
 					continue;
 				}
 				
 				if(!nodesLeft.contains(child)) {
 					nodesLeft.add(child);
 				}
 				
 				if(child.isSolved()) {
 				    System.out.println("Solved in "+child.nPushes+" pushes.");
				    return child;
 				}
 			}
 		}
 		
 		return null;
 	}
 }
