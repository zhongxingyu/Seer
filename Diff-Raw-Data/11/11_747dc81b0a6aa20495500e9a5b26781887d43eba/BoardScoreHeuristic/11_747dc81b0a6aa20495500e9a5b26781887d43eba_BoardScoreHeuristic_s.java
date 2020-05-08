 package de.innoq.blockdrop.algo.heuristics;
 
 import static de.innoq.blockdrop.algo.Util.dropBlockOnOffset;
 import static de.innoq.blockdrop.algo.Util.maxCoords;
 import static de.innoq.blockdrop.algo.Util.minCoords;
 import static de.innoq.blockdrop.algo.Util.pointsToGrid;
 import static de.innoq.blockdrop.algo.Util.testFits;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import de.innoq.blockdrop.algo.CombinedScoreFunction;
 import de.innoq.blockdrop.algo.Operation;
 import de.innoq.blockdrop.algo.ScoreFunction;
 import de.innoq.blockdrop.algo.Util;
 import de.innoq.blockdrop.model.Point;
 
 /**
  * A Heuristic that tries each possible Position and Orientation for a tile and
  * scores the Result using a Score Function. the best Result is taken.
  * 
  */
 public class BoardScoreHeuristic implements Heuristic {
 
 	private ScoreFunction scoreFunction = new CombinedScoreFunction();
 
 	@Override
 	public List<Operation> calculateMoves(Point[] currentBlock, Point[] fixed) {
 		// Wir verzichten zuncsht auf rotieren und probieren nur jede Mgliche
 		// Situation aus.
 
 		Point minCoords = minCoords(currentBlock);
 		Point maxCoods = maxCoords(currentBlock);
 
 		int bestScore = Integer.MAX_VALUE;
 		int bestX = -1; int bestY = -1; int bestRot = -1;
 		
 		byte[][][] board = pointsToGrid(fixed);
 		
 		
 		Point[][] rotated = Util.rotations(currentBlock);
 		for (int rot = 0; rot < 4; rot++) {
 			currentBlock = rotated[rot];
 			for (int x = 0; x < 5; x++) {
 				for (int y = 0; y < 5; y++) {
 					// Move Block to x,y
 					int xOffset = x - minCoords.x;
 					int yOffset = y - minCoords.y;
 
 					// Past der Block berhaupt an der aktuellen Position?
 					if (testFits(board, currentBlock, xOffset, yOffset, 0)) {
 						// Falls Ja : Drop:
 						byte[][][] newBoard = dropBlockOnOffset(board,
 								currentBlock, xOffset, yOffset);
 
 						int newScore = scoreFunction.score(newBoard);
						System.out.println("Score for " + x + "/" + y  +" rot "+ bestRot+" is "
 								+ newScore);
						System.out.println("Board for " + x + "/" + y +" rot "+ bestRot+ " is "
 								+ Util.toString(newBoard));
 						if (newScore < bestScore) {
 							bestScore = newScore;
 							bestX = x;
 							bestY = y;
 							bestRot = rot;
 						}
 					}
 				}
 
 			}
 		}
 
 		if (bestX == -1 || bestY == -1) {
 			System.out.println ("Ups. NoMove  found?");
 			bestX = 4; bestY = 4; // Move into center...
 			
 		}
 		System.out.println ("Best Moves seems to be to "+bestX + "/"+ bestY+ "rotz: "+bestRot);
 		// translate into Moves.
 		List<Operation> result = new LinkedList<Operation>();
 		for (int i = 0; i < minCoords.x - bestX; i++) {
 			result.add(Operation.minusX);
 		}
 		for (int i = 0; i < bestX- minCoords.x; i++ ) {
 			result.add(Operation.plusX);
 		}
 		for (int i = 0; i < bestY- minCoords.y; i++ ) {
 			result.add(Operation.plusY);
 		}
 		for (int i = 0; i <  minCoords.y-bestY; i++ ) {
 			result.add(Operation.minusY);
 		}
 		
 		// Am Ziel sollte die Rotation mglich sein..
 		for (int i = 0; i < bestRot; i++) {
 			result.add (Operation.plusRotZ);
 		}
 		
 		result.add (Operation.drop);
 		return result;
 	}
 
 
 
 	
 
 }
