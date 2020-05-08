 package edu.itba.skbsolver;
 
 import java.util.List;
 import java.util.PriorityQueue;
 
 public class BFSRunner {
 	public static Solution run(Level level, boolean tree){
 		PriorityQueue<State> queue = new PriorityQueue<State>();
 		PositionsTable posTable = new PositionsTable();
 		StateSpawner stateSpawner = new StateSpawner(posTable, level);
 
 		State winner = null;
 		queue.add(level.getInitialState());
 		
 		while (!queue.isEmpty()){
 			State s = queue.remove();
 			
			s.map.logger.info(s.toString());
 			
 			List<State> newStates = stateSpawner.childs(s);
 			
 			// TODO: reorder states with a Heuristic
 			
 			for(State n : newStates){
 				if (level.playerWin(n)){
 					winner = n;
 					queue.clear();
 					break;
 				}
 				queue.add(n);
 			}
 		}
 		
 		return new Solution(winner);
 	}
 
 }
