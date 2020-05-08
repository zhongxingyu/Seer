 package othello.ai.heuristic;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import othello.model.Board;
 
 /*
  * Mobility refers to the number of valid moves you can make given your current state.
  * 
  * According to Othello strategy, it is generally profitable to limit your opponent's mobility. Thus,
  * this heuristic checks every valid move you can make, and for every one of those moves, determines 
  * your opponent's mobility had you made that move.
  * 
  * The algorithm adds the board that returned the lowest opponent mobilityScore to the list of bestBoards.
  */
 public class Mobility implements Heuristic {
 
 	private int size;
 
 	public Mobility(int size) {
 		this.size = size;
 	}
 
 	@Override
 	public Map<Board, Double> getUtility(List<Board> boardStates) {
 		Map<Board, Double> utilities = new HashMap<Board, Double>();
 		List<Board> bestBoards = new ArrayList<Board>();
 		
 		int maxScore = Integer.MAX_VALUE;
 		Map<Board, Double> mobilityBoards = new HashMap<Board, Double>();
 		Board temp;
 		for (Board b : boardStates) {
 			
 			int mobilityScore = 0;
 			Board prev = new Board(b);
 			
 			for (int y = 0; y < size; y++) {
 				for (int x = 0; x < size; x++) {
 					if(prev.move(x,y)) { // valid move
 						temp = new Board(prev);
 						for(int c = 0; c < size; c++) {
 							for(int d = 0; d < size; d++) {
 								if(temp.move(c,d)) // valid move
 									mobilityScore++;
 								temp = new Board(prev);
 							}
 						}
 						mobilityBoards.put(temp, new Double(mobilityScore));
 						prev = new Board(b);
 						mobilityScore = 0;
 					}
 				}
 			}
 			
 			utilities.put(b, 0.0);
			
			int lowestMobilityScore = Collections.min(mobilityBoards.values()).intValue();
 			
 			if(lowestMobilityScore < maxScore) {
 				maxScore = lowestMobilityScore;
 				bestBoards.clear();
 				bestBoards.add(b);
 			} else if(lowestMobilityScore == maxScore) {
 				bestBoards.add(b);
 			}
 			
 			mobilityBoards = new HashMap<Board, Double>();		
 		}
 		
 		for(Board b : bestBoards) {
 			utilities.put(b, 1.0);
 		}
 		
 		return utilities;
 	}
 
 	@Override
 	public double getMax() {
 		return 1;
 	}
 
 	@Override
 	public double getMin() {
 		return 0;
 	}
 }
