 import java.util.Arrays;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Callable;
 
 public class MultithreadedAI extends AIEngine {
 	public Move[] rankBestMove (Move[] moves, Game g, Player p, int recursionDepth) {
 		//creates array to hold values of boards
 		double[] boardValues = new double[moves.length];
 
 		//if recursionDepth is one, calculate direct values of moves
 		if (recursionDepth==1) {
 			//iterates over all moves and calculates values to put in boardValues
 			for (int i = 0; i<moves.length; i++) {
 				boardValues[i] = (new Board(g.getGameBoard(), moves[i])).calculateValue(p);
 			}
 
 		//recursionDepth must be greater than one, so get values of the best opponent moves for each possible move
 		} else {
 			ExecutorService service = Executors.newFixedThreadPool(8);
 			
 			@SuppressWarnings({"unchecked"})
 			Future<Double>[] valueFutures = new Future[moves.length];
 			
 			for (int i = 0; i<moves.length; i++) {
 				valueFutures[i] = service.submit(new ValueCalculator(new Game(g, moves[i]), recursionDepth-1, true, p));
 			}
 			
 			//iterates over all moves and calculates value based on best opponent move
 			for (int i = 0; i<moves.length; i++) {
 				try {
 					boardValues[i] = valueFutures[i].get();
 				} catch (InterruptedException ex) {
 					ex.printStackTrace();
 				} catch (ExecutionException ex) {
 					ex.printStackTrace();
 				}
 			}
 			
 			service.shutdownNow();
 		}
 
 		//creates array to hold sorted values from lowest to highest
 		double[] boardValuesSorted = Arrays.copyOf(boardValues, boardValues.length);
 		Arrays.sort(boardValuesSorted);
 
 		//creates array to hold final list of moves
 		Move[] sortedMoves = new Move[moves.length];
 		//creates variable to hold index of move in original list
 		int index = 0;
 		//iterates over all values of boardValuesSorted
 		for (int i = 0; i<boardValuesSorted.length; i++) {
 			//finds the index of the current values in the original move list
 			index = ArraysHelper.find(boardValues, boardValuesSorted[i]);
 			//sets the value at index to very large so that the same move is not used again, even if multiple moves have equal values
 			boardValues[index] = Board.maxBoardValue*1000;
 
 			//puts the correct move in the correct position in the final array
 			sortedMoves[moves.length-i-1] = moves[index];
 		}
 
 		//logs values for debugging
 		// g.getGameBoard().printBoard();
 		// for (Move m : sortedMoves) {
 		// 	System.out.println(Arrays.deepToString(m.getWaypoints()));
 		// }
 		// System.out.print("" + recursionDepth + " ");
 		// System.out.println(Arrays.toString(boardValuesSorted));
 
 		return sortedMoves;
 	}
 
 	private static class ValueCalculator implements Callable<Double> {
 		private Game origGame;
 		private int startRecursionDepth;
 		private boolean startTestOpponentMoves;
 		private Player player;
		private double startMax;
		private double startMin;
 		
 		public ValueCalculator (Game g, int recursionDepth, boolean testOpponentMoves, Player p) {
 			this.origGame = g;
 			this.startRecursionDepth = recursionDepth;
 			this.startTestOpponentMoves = testOpponentMoves;
 			this.player = p;
 			if (this.startTestOpponentMoves) {
 				this.startAB = -(recursionDepth+1)*Board.maxBoardValue;
 			} else {
 				this.startAB = (recursionDepth+1)*Board.maxBoardValue;
 			}
 		}
 		
 		public Double call() {
 			return this.valueOfMoves(this.origGame, this.startRecursionDepth, this.startTestOpponentMoves, this.startAB);
 		}
 		
		private double valueOfMoves(Game g, Player p, int recursionDepth, boolean testOpponentMoves, double ab) {
 			if (g.isDraw()) {
 				// System.out.println(Arrays.deepToString(g.getLastFewMoves()));
 				// System.out.println("Detected possible draw");
 				return 0;
 			}
 
 			//creates array to hold all possible moves
 			Move[] moves;
 			if (testOpponentMoves) {
 				//gets possible moves for other player
 				moves = g.getOtherPlayer(p).getAllMoves(g.getGameBoard());
 			} else {
 				//gets possible moves for self
 				moves = p.getAllMoves(g.getGameBoard());
 			}
 
 			//tests if no moves are available, signaling a loss for one of the players
 			if (moves.length==0) {
 				if (testOpponentMoves) {
 					//result is an opponent loss, or a positive value
 					return (recursionDepth+1)*Board.maxBoardValue;
 				} else {
 					//result is a loss, or a negative value
 					return -(recursionDepth+1)*Board.maxBoardValue;
 				}
 			}
 
 			//tests if only one move is available, meaning that the return value will be the value of the one move
 			if (moves.length==1) {
 				//if only thinking one move ahead, calculate the simple value
 				if (recursionDepth==1) {
 					return (new Board(g.getGameBoard(), moves[0])).calculateValue(p);
 				//if thinking multiple moves ahead, calculate the recursive value
 				} else {
 					if (!testOpponentMoves) {
 						return this.valueOfMoves(new Game(g, moves[0]), p, recursionDepth-1, true, -(recursionDepth+1)*Board.maxBoardValue);
 					} else {
 						return this.valueOfMoves(new Game(g, moves[0]), p, recursionDepth-1, false, (recursionDepth+1)*Board.maxBoardValue);
 					}
 				}
 			}
 
 			//creates array to hold values of boards
 			double[] boardValues = new double[moves.length];
 
 			//if recursionDepth is one, calculate direct values of moves
 			if (recursionDepth==1) {
 				//iterates over all moves and calculates values to put in boardValues
 				for (int i = 0; i<moves.length; i++) {
 					boardValues[i] = (new Board(g.getGameBoard(), moves[i])).calculateValue(p);
 					//returns if this portion of the tree can be eliminated by alpha-beta pruning
 					if ((testOpponentMoves && boardValues[i]<=ab) || (!testOpponentMoves && boardValues[i]>=ab)) {
 						return boardValues[i];
 					}
 				}
 			
 			//recursionDepth must be greater than one, so get values of the best opponent moves for each possible move
 			} else {
 				//sets new alpha-beta value based on testOpponentMoves
 				double newAB;
 				if (!testOpponentMoves) {
 					newAB = -(recursionDepth+1)*Board.maxBoardValue;
 				} else {
 					newAB = (recursionDepth+1)*Board.maxBoardValue;
 				}
 				//iterates over all moves and calculates value based on best opponent move
 				for (int i = 0; i<moves.length; i++) {
 					boardValues[i] = this.valueOfMoves(new Game(g, moves[i]), p, recursionDepth-1, !testOpponentMoves, newAB);
 					//returns if this portion of the tree can be eliminated by alpha-beta pruning
 					if ((testOpponentMoves && boardValues[i]<=ab) || (!testOpponentMoves && boardValues[i]>=ab)) {
 						return boardValues[i];
 					}
 
 					//sets the new board value to newAB if it is better for the current player
 					if ((!testOpponentMoves && boardValues[i]>newAB) || (testOpponentMoves && boardValues[i]<newAB)) {
 						newAB = boardValues[i];
 					}
 				}
 			}
 
 			//creates variable to hold result value
 			double result = 100000000;
 			if (!testOpponentMoves) {
 				result = -100000000;
 			}
 			//creates variable to count occurrences of best move
 			// double count = 0;
 			//iterates over all board values
 			for (double testValue : boardValues) {
 				//if the value is better, change the result value and reset the counter
 				if ((testOpponentMoves && testValue<result) || (!testOpponentMoves && testValue>result)) {
 					result = testValue;
 					// count = 1;
 				//if the value is the same as the current result, increment the counter
 				// } else if (testValue==result) {
 				// 	count++;
 				}
 			}
 
 			// if (testOpponentMoves) {
 			// 	result -= count*0.0001;
 			// }
 
 			//logs the values for debugging
 			// g.getGameBoard().printBoard();
 			// Move[] sortedMoves = new Move[moves.length];
 			// int index = 0;
 			// for (int i = 0; i<boardValuesSorted.length; i++) {
 			// 	index = ArraysHelper.find(boardValues, boardValuesSorted[i]);
 			// 	boardValues[index] = Board.maxBoardValue*1000;
 			// 	sortedMoves[moves.length-i-1] = moves[index];
 			// }
 			// for (Move m : sortedMoves) {
 			// 	System.out.println(Arrays.deepToString(m.getWaypoints()));
 			// }
 			// System.out.print("" + recursionDepth + " ");
 			// System.out.println(Arrays.toString(boardValuesSorted));
 			// System.out.println(result);
 
 			return result;
 		}
 	}
 }
