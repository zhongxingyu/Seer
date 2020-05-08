 package antichess;
 
 import java.lang.Thread.State;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class AIPlayer extends Player {
 	private AIBoard currentBoard;
 	final int PAWN_VALUE = 1;
 	final int KNIGHT_VALUE = 3;
 	final int BISHOP_VALUE = 3;
 	final int ROOK_VALUE = 5;
 	final int QUEEN_VALUE = 9;
 	final int KING_VALUE = 2;
 	final int MIN = 0;
 	final int MAX = 1;
 
 	public AIPlayer(int colour) {
 		super(colour);
 		int[] evaluationValues = {PAWN_VALUE, KNIGHT_VALUE, BISHOP_VALUE, ROOK_VALUE, QUEEN_VALUE, KING_VALUE};
 		currentBoard = new AIBoard(colour, evaluationValues);
 	}
 
 	@Override
 	public Move getMove() {
 		int thinkingTime = 0;
 		if (playerColour == Definitions.WHITE) {
			thinkingTime = 1000;
 		} else {
			thinkingTime = 1000;
 		}
 		System.out.println("AI Thinking (" + thinkingTime/1000.0 + " seconds)");
 		long startTime = System.currentTimeMillis();
 		long finishTime = System.currentTimeMillis();
 
 		int depth = 1;
 		Move move = null;
 		Move[] moves = new Move[depth];
 		currentBoard.generateMoves(playerColour);
 
 		//Create the first thread and start it running.
 		System.out.println("Running depth 1 search");
 		Runnable search = new MiniMaxSearch(currentBoard, playerColour, otherPlayerColour, depth, moves);
 		Thread t = new Thread(search);
 		t.start();
 		//Ensure the first thread completes
 		try {
 			t.join();
 		} catch (InterruptedException ex) {
 			Logger.getLogger(AIPlayer.class.getName()).log(Level.SEVERE, null, ex);
 		}
 
 		while (System.currentTimeMillis() - startTime < thinkingTime) {
 			try {
 				//if the thread has finished start another one
 				if (t.getState() == State.TERMINATED) {
 					move = moves[0];
 					finishTime = System.currentTimeMillis();
 					//System.out.println("Current best move is " + move.oldX + move.oldY + move.newX + move.newY);
 					depth++;
 					System.out.println("Trying depth " + depth);
 					moves = new Move[depth];
 					currentBoard.generateMoves(playerColour);
 					search = new MiniMaxSearch(currentBoard, playerColour, otherPlayerColour, depth, moves);
 					t = new Thread(search);
 					t.start();
 				}
 				Thread.sleep(100);
 			} catch (InterruptedException ex) {
 				Logger.getLogger(AIPlayer.class.getName()).log(Level.SEVERE, null, ex);
 			}
 		}
 
 		t.interrupt();
 		try {
 			t.join();
 		} catch (InterruptedException ex) {
 			Logger.getLogger(AIPlayer.class.getName()).log(Level.SEVERE, null, ex);
 		}
 
 		System.out.println("Terminating depth " + depth + " search");
 		System.out.println("The move to be used is " + move.oldX + move.oldY + move.newX + move.newY);
 		System.out.println("The " + (depth - 1) + " level search took " + (finishTime - startTime) / 1000.0 + " seconds");
 
 
 		currentBoard.makeMove(move);
 
 		return move;
 
 	}
 
 	public void sendMove(Move move) {
 		currentBoard.makeMove(move);
 	}
 
 	private class MiniMaxSearch implements Runnable {
 		private AIBoard currentBoard;
 		private int playerColour;
 		private int otherPlayerColour;
 		private int maxDepth;
 		private Move[] moves;
 
 		public MiniMaxSearch(AIBoard currentBoard, int playerColour, int otherPlayerColour, int maxDepth, Move[] moves) {
 			this.currentBoard = currentBoard;
 			this.playerColour = playerColour;
 			this.otherPlayerColour = otherPlayerColour;
 			this.maxDepth = maxDepth;
 			this.moves = moves;
 		}
 
 		public void run() {
 			try {
 				miniMax(0, MAX, Integer.MAX_VALUE);
 			} catch (InterruptedException exception) {
 				//just exit if interrupted
 			}
 		}
 
 		public int miniMax(int currentDepth, int searchType, int parentNodeScore) throws InterruptedException {
 			try {
 				if (currentDepth == maxDepth) {
 					//If this is the bottom layer
 					return currentBoard.staticEval();
 				} else {
 					//Get the list of move (captures if one is possible, valid moves otherwise)
 					ArrayList<Move> moveList;
 					if (currentBoard.validCaptures.size() > 0) {
 						moveList = (ArrayList<Move>) currentBoard.validCaptures.clone();
 					} else {
 						moveList = (ArrayList<Move>) currentBoard.validMoves.clone();
 					}
 					//Certain actions should be taken if the move list has no moves in it
 					if (moveList.size() == 0) {
 						int finished = currentBoard.isFinished(playerColour);
 
 						//check for wins
 						if (finished == Definitions.WHITE_WINS || finished == Definitions.BLACK_WINS) {
 							return currentBoard.staticEval();
 						}
 
 						//check for stalemates
 						if (finished == Definitions.LOCKED_STALEMATE || finished == Definitions.DERIVED_STALEMATE) {
 							//THIS SHOULD REALLY BE DISCUSSED. I'm not sure 0 is the appropriate score for stalemate. MCS
 							return 0;
 						}
 
 						//if the player just has no moves add a null move
 						moveList.add(null);
 					}
 
 					//Iterate through the list, make each move, find the max of that position
 					//at the end return the minimum score and update some list of moves
 					//Might need something to check for situations where there are no possible moves
 					//(someone has won/some has lost/can hapen in stalemate)
 					int score = 0;
 					int testScore = 0;
 					//switch based on min or max search
 					switch (searchType) {
 						case MIN:
 							score = Integer.MAX_VALUE;
 							for (Move move : moveList) {
 								currentBoard.makeMove(move);
 								Thread.sleep(0);
 								currentBoard.generateMoves(playerColour);
 								testScore = miniMax(currentDepth + 1, MAX, score);
 								if (testScore < score) {
 									score = testScore;
 									moves[currentDepth] = move;
 								}
 								currentBoard.undoMove();
 								if (score <= parentNodeScore) {
 									break;
 								}
 								
 							}
 							break;
 						case MAX:
 							score = Integer.MIN_VALUE;
 							for (Move move : moveList) {
 								currentBoard.makeMove(move);
 								Thread.sleep(0);
 								currentBoard.generateMoves(otherPlayerColour);
 								testScore = miniMax(currentDepth + 1, MIN, score);
 								if (testScore > score) {
 									score = testScore;
 									moves[currentDepth] = move;
 								}
 								currentBoard.undoMove();
 								if (score >= parentNodeScore) {
 									break;
 								}
 								
 							}
 							break;
 					}
 					//end switch
 					return score;
 				}
 			}
 			catch (InterruptedException exception) {
 				currentBoard.undoMove();
 				throw exception;
 			}
 		}
 	}
 }
