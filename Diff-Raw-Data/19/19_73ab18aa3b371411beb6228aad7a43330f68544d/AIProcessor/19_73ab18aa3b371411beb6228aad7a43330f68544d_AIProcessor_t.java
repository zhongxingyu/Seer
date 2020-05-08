 package chessAI;
 
 import java.util.Vector;
 
 import chessBackend.Board;
 import chessBackend.GameStatus;
 import chessBackend.BoardHashEntry;
 import chessBackend.Move;
 import chessBackend.MoveNote;
 import chessPieces.Values;
 
 public class AIProcessor extends Thread {
 	private DecisionNode rootNode;
 	private Board board;
 
 	private int searchDepth;
 	private int decisionNodeDepth;
 	private boolean twigGrowthEnabled;
 	private final boolean iterativeDeepening = false;
 	private final boolean useHashTable = false;
 
 	private int maxInCheckFrontierLevel = 2;
 	private int maxPieceTakenFrontierLevel = 2;
 
 	private final boolean pruningEnabled = true;
 	private int aspirationWindowSize;
 
 	private boolean threadActive;
 
 	private AI ai;
 
 	private BoardHashEntry[] hashTable;
 
 	public AIProcessor(AI ai, int maxTreeLevel) {
 		this.ai = ai;
 		this.searchDepth = maxTreeLevel;
 		twigGrowthEnabled = false;
 		decisionNodeDepth = searchDepth;
 		aspirationWindowSize = 0;
 
 		threadActive = true;
 
 		hashTable = ai.getHashTable();
 
 	}
 
 	@Override
 	public void run() {
 
 		synchronized (this) {
 			while (threadActive) {
 
 				try {
 					this.wait();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 				if (threadActive) {
 					executeTask();
 
 					// System.out.println("HashTable size = " +
 					// hashTable.size());
 					// hashTable.clear();
 				}
 
 			}
 
 		}
 
 	}
 
 	public synchronized void terminate() {
 		threadActive = false;
 		this.notifyAll();
 	}
 
 	public synchronized void setNewTask() {
 		this.notifyAll();
 	}
 
 	public synchronized void setBoard(Board board) {
 		this.board = board;
 	}
 
 	public Board getBoard() {
 		return board;
 	}
 
 	public synchronized void setRootNode(DecisionNode rootNode) {
 
 		if (rootNode.getMove() != null) {
 			board.makeMove(rootNode.getMove());
 		}
 
 		this.rootNode = rootNode;
 
 		// if game isnt over make sure tree root shows what next valid moves are
 		if (!this.rootNode.hasChildren()) {
 			board.makeNullMove();
 			Vector<Move> moves = board.generateValidMoves();
 			for (int m = 0; m < moves.size(); m++) {
 				this.rootNode.addChild(new DecisionNode(moves.elementAt(m)));
 			}
 		}
 
 	}
 
 	private void executeTask() {
 		DecisionNode task;
 		int alpha = Integer.MIN_VALUE + 100;
 		BoardHashEntry hashOut;
 		boolean hashHit = false;
 
 		while ((task = ai.getNextTask()) != null) {
 
 			if (rootNode.getHeadChild() != null) {
 				alpha = rootNode.getHeadChild().getChosenPathValue();
 			}
 
 			board.makeMove(task.getMove());
 
 			// task.setChosenPathValue(-growDecisionTreeLite(alpha,
 			// Integer.MAX_VALUE-100, searchDepth, task.getMove()));
 
 			growDecisionTree(task, alpha, Integer.MAX_VALUE - 100, searchDepth, 0);
 
 			board.undoMove();
 
 			rootNode.addChild(task);
 
 			ai.taskDone();
 
 		}
 
 	}
 
 	private boolean hashTableUpdate(BoardHashEntry present, int cLevel, int cMoveNum) {
 		if (present.getMoveNum() <= cMoveNum - 2) {
 			return true;
 		} else {
 			if (cLevel > present.getLevel()) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param branch
 	 *            Branch to grow
 	 * @param level
 	 *            Level from the rootNode that the branch is at
 	 * @return
 	 */
 	private void growDecisionTree(DecisionNode branch, int alpha, int beta, int level, int bonusLevel) {
 
 		boolean pruned = false;
 
 		if (!branch.hasChildren()) {
 
 			board.makeNullMove();
 
 			if ((board.getBoardStatus() == GameStatus.CHECK) && (level > -maxInCheckFrontierLevel)) {
 				bonusLevel = Math.min(bonusLevel, level - 2);
 			}
 
 			if ((branch.hasPieceTaken()) && (level > -maxPieceTakenFrontierLevel)) {
 				bonusLevel = Math.min(bonusLevel, level - 1);
 			}
 
 			if (level > bonusLevel) {
 				// check all moves of all pieces
 				Vector<Move> moves = board.generateValidMoves();
 
 				if (!board.isGameOver()) {
 
 					Move move;
 					DecisionNode newNode = null;
 					DecisionNode tailNode = null;
 					for (int m = 0; m < moves.size(); m++) {
 
 						move = moves.elementAt(m);
 
 						newNode = new DecisionNode(move);
 
 						if (!pruned) {
 
 							board.makeMove(move);
 
 							if (level > 0) {
 
 								growDecisionTree(newNode, -beta, -alpha, level - 1, bonusLevel);
 
 							} else {
 								// bonus depth
 
 								if (twigGrowthEnabled) {
									newNode.setChosenPathValue(-growDecisionTreeLite(-beta, -alpha, level, move, bonusLevel));
 								} else {
 									growDecisionTree(newNode, -beta, -alpha, level - 1, bonusLevel);
 								}
 
 							}
 
 							board.undoMove();
 
 							branch.addChild(newNode);
 
 							// alpha beta pruning
 							if (pruningEnabled) {
 
 								if (newNode.getChosenPathValue() > alpha) {
 									alpha = newNode.getChosenPathValue();
 								}
 
 								if (alpha >= beta) {
 									tailNode = newNode.getLastSibling();
 									pruned = true;
 								}
 							}
 
 						} else {
 							newNode.setAB(alpha, beta);
 							tailNode.setNextSibling(newNode);
 							tailNode = newNode;
 						}
 
 						branch.setChosenPathValue(-branch.getHeadChild().getChosenPathValue());
 						branch.setAB(alpha, beta);
 
 					}
 				} else {
 					if (board.isInStaleMate() || board.isDraw()) {
 						branch.setChosenPathValue(-board.staticScore());
 					} else {
 						branch.setChosenPathValue(Values.CHECKMATE_MOVE);
 					}
 				}
 			} else {
 				branch.setChosenPathValue(-board.staticScore());
 				branch.setAB(alpha, beta);
 			}
 
 		} else {
 
 			// Node has already been created and has children
 			// int childrenSize = branch.getChildrenSize();
 			DecisionNode nextChild;
 			DecisionNode currentChild = branch.getHeadChild();
 			branch.removeAllChildren();
 			while (currentChild != null) {
 
 				// System.out.println(" next child" + i);
 				nextChild = currentChild.getNextSibling();
 
 				// if (currentChild.getChosenPathValue() !=
 				// Values.CHECKMATE_MOVE) {
 
 				board.makeMove(currentChild.getMove());
 
 				growDecisionTree(currentChild, -beta, -alpha, level - 1, bonusLevel);
 
 				board.undoMove();
 
 				branch.addChild(currentChild);
 
 				// alpha beta pruning
 				if (pruningEnabled) {
 
 					if (currentChild.getChosenPathValue() > alpha) {
 						alpha = currentChild.getChosenPathValue();
 					}
 
 					if (alpha >= beta) {
 						currentChild.getLastSibling().setNextSibling(nextChild);
 						break;
 					}
 
 				}
 
 				// }
 
 				currentChild = nextChild;
 			}
 
 			branch.setChosenPathValue(-branch.getHeadChild().getChosenPathValue());
 			branch.setAB(alpha, beta);
 
 		} // end of node already has children code
 
 	}
 
 	/**
 	 * This method has similar functionality as 'growDecisionTree()' but it
 	 * doesn't add on to the decision tree data structure. Adding on to the data
 	 * structure at some tree depths can cause the program to overflow the heap.
 	 * This method is used as a quick alternative to 'growDecisionTree()' at
 	 * large tree depths.
 	 * 
 	 * @param board
 	 *            The board that needs all possible moves evaluated.
 	 * @param player
 	 *            The player whose turn it is.
 	 * @param level
 	 *            The stack distance from the initial call on
 	 *            'growDecisionTreeLite()'
 	 * @return The value of the best move for 'player' on the 'board'
 	 */
 	private int growDecisionTreeLite(int alpha, int beta, int level, Move moveMade, int bonusLevel) {
 		int suggestedPathValue;
 		int bestPathValue = Integer.MIN_VALUE;
 
 		board.makeNullMove();
 
		if ((board.getBoardStatus() == GameStatus.CHECK) && (level > -maxInCheckFrontierLevel)) {
			bonusLevel = Math.min(bonusLevel, level - 2);
 		}
 
		if ((moveMade.hasPieceTaken()) && (level > -maxPieceTakenFrontierLevel)) {
			bonusLevel = Math.min(bonusLevel, level - 1);
 		}
 
		if (level > bonusLevel) {
 
 			GameStatus tempBoardState;
 			Move move;
 
 			Vector<Move> moves = board.generateValidMoves();
 
 			if (!board.isGameOver()) {
 
 				for (int m = 0; m < moves.size(); m++) {
 					move = moves.elementAt(m);
 
 					tempBoardState = board.getBoardStatus();
 
 					board.makeMove(move);
 
 					suggestedPathValue = -growDecisionTreeLite(-beta, -alpha, level - 1, move, bonusLevel);
 
 					board.undoMove();
 
 					board.setBoardStatus(tempBoardState);
 
 					if (suggestedPathValue > bestPathValue) {
 						bestPathValue = suggestedPathValue;
 					}
 
 					if (bestPathValue > alpha) {
 						alpha = bestPathValue;
 					}
 
 					if (alpha >= beta) {
 						break;
 					}
 
 				}
 
 			}
 		} else {
 			bestPathValue = board.staticScore();
 		}
 
 		if (board.isInCheckMate()) {
 			bestPathValue = -(Values.CHECKMATE_MOVE - (searchDepth - level));
 		} else {
 			if (board.isInStaleMate() || board.isDraw()) {
 				bestPathValue = -((board.staticScore()) / Values.DRAW_DIVISOR);
 			}
 		}
 
 		return bestPathValue;
 	}
 
 	public synchronized void setMaxTreeLevel(int maxTreeLevel) {
 		this.searchDepth = maxTreeLevel;
 	}
 
 }
