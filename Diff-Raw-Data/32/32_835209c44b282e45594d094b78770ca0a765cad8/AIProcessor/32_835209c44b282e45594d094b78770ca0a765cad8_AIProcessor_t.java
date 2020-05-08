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
 
 	private int maxTreeLevel;
 	private int maxTwigLevel;
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
 
 	public AIProcessor(AI ai, int maxTreeLevel, int maxTwigLevel) {
 		this.ai = ai;
 		this.maxTreeLevel = maxTreeLevel;
 		this.maxTwigLevel = maxTwigLevel;
 		twigGrowthEnabled = false;
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
 			Vector<Move> moves = board.generateValidMoves();
 			for (int m = 0; m < moves.size(); m++) {
 				this.rootNode.addChild(new DecisionNode(moves.elementAt(m)));
 			}
 		}
 
 	}
 
 	private void executeTask() {
 		DecisionNode task;
 		int ab = Integer.MIN_VALUE;
 		BoardHashEntry hashOut;
 		boolean hashHit = false;
 
 		while ((task = ai.getNextTask()) != null) {
 
 			if (rootNode.getHeadChild() != null) {
 				ab = rootNode.getHeadChild().getChosenPathValue(0, 0);
 			}
 
 			board.makeMove(task.getMove());
 
 			// if (maxTreeLevel >= 1) {
 			// for (int level = 1; level <= maxTreeLevel; level++) {
 			// growDecisionTree(task, level, ab);
 			// System.out.println("level " + level);
 			// }
 			// } else {
 			// growDecisionTree(task, 0, ab);
 			// }
 
 			hashOut = hashTable[board.getHashIndex()];
 
 			if (hashOut != null) {
 				if (hashOut.getHashCode() == board.getHashCode() && hashOut.getLevel() >= maxTreeLevel + maxTwigLevel + 2) {
 					task.setChosenPathValue(hashOut.getScore());
 					hashHit = true;
 				}
 			}
 
 			if (!hashHit) {
 				task.setChosenPathValue(null);
 
 				if (iterativeDeepening) {
 					twigGrowthEnabled = false;
 					for (int i = 1; i <= maxTreeLevel; i++) {
 						growDecisionTree(task, i, ab);
 					}
 
 					twigGrowthEnabled = true;
 					growDecisionTree(task, maxTreeLevel, ab);
 
 				} else {
 					growDecisionTree(task, maxTreeLevel, ab);
 				}
 
 				if (useHashTable) {
 					if (hashOut == null) {
 						hashTable[board.getHashIndex()] = new BoardHashEntry(board.getHashCode(), maxTreeLevel + maxTwigLevel + 2,
 								task.getChosenPathValue(0, 0), ai.getMoveNum());
 					} else {
 						if (hashTableUpdate(hashOut, maxTreeLevel + maxTwigLevel + 2, ai.getMoveNum())) {
 							hashTable[board.getHashIndex()].setAll(board.getHashCode(), maxTreeLevel + maxTwigLevel + 2,
 									task.getChosenPathValue(0, 0), ai.getMoveNum());
 						}
 					}
 				}
 			}
 
 			board.undoMove();
 
			if (rootNode.getHeadChild() != null) {
				if (task.getChosenPathValue(0, 0) > rootNode.getHeadChild().getChosenPathValue(0, 0)) {
					rootNode.removeAllChildren();
					rootNode.addChild(task);
				}
			} else {
				rootNode.addChild(task);
			}
 
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
 	private void growDecisionTree(DecisionNode branch, int level, int alphaBeta) {
 
 		boolean pruned = false;
 		int newAlphaBeta = Integer.MIN_VALUE;
 		BoardHashEntry hashOut;
 		boolean hashHit = false;
 
 		if (!branch.hasChildren()) {
 
 			// check all moves of all pieces
 			Vector<Move> moves = board.generateValidMoves();
 
 			if (board.isGameOver()) {
 				if (board.isInStaleMate() || board.isDraw()) {
 					branch.setChosenPathValue((board.winningBy(board.getTurn())) / Values.DRAW_DIVISOR);
 				} else {
 					branch.setChosenPathValue(Values.CHECKMATE_MOVE);
 				}
 
 				return;
 			}
 
 			boolean branchInCheckSearch = (board.getBoardStatus() == GameStatus.CHECK) && (level > -maxInCheckFrontierLevel);
 			boolean pieceTakenBonusSearch;
 			Move move;
 			DecisionNode newNode = null;
 			DecisionNode tailNode = null;
 			Boolean bonusSearch;
 			for (int m = 0; m < moves.size(); m++) {
 
 				move = moves.elementAt(m);
 
 				newNode = new DecisionNode(move);
 
 				if (!pruned) {

 					pieceTakenBonusSearch = newNode.hasPieceTaken() && (level > -maxPieceTakenFrontierLevel);
 
 					bonusSearch = pieceTakenBonusSearch || branchInCheckSearch || (move.getNote() == MoveNote.NEW_QUEEN);
 
 					if (level > 0 || bonusSearch) {
 
 						board.makeMove(move);
 
 						// use hashtable to see if hashcode has been seen before
 						hashOut = hashTable[board.getHashIndex()];
 
 						hashHit = false;
 
 						if (hashOut != null) {
 							// check to see if depth is sufficient
 							if (hashOut.getHashCode() == board.getHashCode() && hashOut.getLevel() >= (level + maxTwigLevel + 1)) {
 								newNode.setChosenPathValue(hashOut.getScore());
 								hashHit = true;
 							}
 						}
 
 						if (!hashHit) {
 							if (branch.getHeadChild() != null) {
 								newAlphaBeta = branch.getHeadChild().getChosenPathValue(0, 0);
 							}
 
 							if (level > 0) {
 
 								growDecisionTree(newNode, level - 1, newAlphaBeta);
 
 							} else {
 								// bonus depth
 
 								if (twigGrowthEnabled) {
 									growFrontierTwig(newNode, newAlphaBeta);
 								} else {
 									growDecisionTree(newNode, level - 1, newAlphaBeta);
 								}
 
 							}
 
 							if (useHashTable) {
 								// add or update new entry into hash table
 								if (hashOut == null) {
 									hashTable[board.getHashIndex()] = new BoardHashEntry(board.getHashCode(), level + maxTwigLevel + 1,
 											newNode.getChosenPathValue(0, 0), ai.getMoveNum());
 								} else {
 									if (hashTableUpdate(hashOut, level + maxTwigLevel + 1, ai.getMoveNum())) {
 										hashTable[board.getHashIndex()].setAll(board.getHashCode(), level + maxTwigLevel + 1,
 												newNode.getChosenPathValue(0, 0), ai.getMoveNum());
 									}
 								}
 
 							}
 						}
 
 						board.undoMove();
 
 						branch.addChild(newNode);
 
 						// alpha beta pruning
 						if (pruningEnabled) {
 							if (branch.getMoveValue() - newNode.getChosenPathValue(0, 0) <= alphaBeta + aspirationWindowSize) {
 								// branch.setChosenPathValue(branch.getMoveValue()
 								// -
 								// newNode.getChosenPathValue(0));
 								// branch.removeAllChildren();
 								// return;
 								tailNode = newNode.getLastSibling();
 								pruned = true;
 							}
 						}
 
 					} else {
 						branch.addChild(newNode);
 					}
 
 				} else {
 					tailNode.setNextSibling(newNode);
 					tailNode = newNode;
 				}
 
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
 
 				if (level > 0) {
 
 					if (branch.getHeadChild() != null) {
 						newAlphaBeta = branch.getHeadChild().getChosenPathValue(0, 0);
 					}
 
 					board.makeMove(currentChild.getMove());
 
 					// explore down tree
 					hashOut = hashTable[board.getHashIndex()];
 
 					hashHit = false;
 
 					if (hashOut != null) {
 						if (hashOut.getLevel() >= level + maxTwigLevel + 1) {
 							currentChild.setChosenPathValue(hashOut.getScore());
 							hashHit = true;
 						}
 					}
 
 					if (!hashHit) {
 						currentChild.setChosenPathValue(null);
 						growDecisionTree(currentChild, level - 1, newAlphaBeta);
 
 						if (useHashTable) {
 
 							if (hashOut == null) {
 								hashTable[board.getHashIndex()] = new BoardHashEntry(board.getHashCode(), level + maxTwigLevel + 1,
 										currentChild.getChosenPathValue(0, 0), ai.getMoveNum());
 							} else {
 								if (hashTableUpdate(hashOut, level + maxTwigLevel + 1, ai.getMoveNum())) {
 									hashTable[board.getHashIndex()].setAll(board.getHashCode(), level + maxTwigLevel + 1,
 											currentChild.getChosenPathValue(0, 0), ai.getMoveNum());
 								}
 							}
 
 						}
 					}
 
 					board.undoMove();
 
 				}
 
 				// alpha beta pruning
 				if (pruningEnabled) {
 
 					if (branch.getMoveValue() - currentChild.getChosenPathValue(0, 0) <= alphaBeta + aspirationWindowSize) {
 						// branch.setChosenPathValue(branch.getMoveValue() -
 						// currentChild.getChosenPathValue(0));
 						// branch.removeAllChildren();
 						// return;
 
 						branch.addChild(currentChild);
 						currentChild.getLastSibling().setNextSibling(nextChild);
 						return;
 					}
 
 				}
 
 				branch.addChild(currentChild);
 
 				currentChild = nextChild;
 			}
 
 		} // end of node already has children code
 
 	}
 
 	/**
 	 * This function uses the lite version of growDecisionTree() to get a quick
 	 * idea of the move value of a twig. A twig is the bottom of the tree, which
 	 * is a node that has no children. These twigs are investigated to make sure
 	 * there isn't some significant event just beyond the knowledge of the
 	 * decision tree. The growDecisionLite() method is used instead of the
 	 * growDecisionTree() method because the massive number of twigs can be
 	 * computationally problematic.
 	 * 
 	 * @param twig
 	 *            A node at the bottom of the tree, which has no children.
 	 * @return
 	 */
 	private void growFrontierTwig(DecisionNode twig, int alphaBeta) {
 
 		int twigsBestPathValue = growDecisionTreeLite(board, twig.getMove(), alphaBeta, maxTwigLevel);
 
 		// twig.setStatus(board.getBoardStatus());
 
 		twig.setChosenPathValue(twigsBestPathValue);
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
 	private int growDecisionTreeLite(Board board, Move parentMove, int alphaBeta, int level) {
 		Vector<Move> moves;
 		Move move;
 		int suggestedPathValue;
 		GameStatus tempBoardState;
 		boolean hashHit = false;
 
 		BoardHashEntry hashOut;
 
 		int childsBestPathValue = Integer.MIN_VALUE;
 
 		moves = board.generateValidMoves();
 
 		if (!board.isGameOver()) {
 
 			for (int m = 0; m < moves.size(); m++) {
 				move = moves.elementAt(m);
 
 				if ((level > 0 || board.isInCheck() || move.hasPieceTaken()) && (level > -maxInCheckFrontierLevel)) {
 
 					hashHit = false;
 					tempBoardState = board.getBoardStatus();
 					board.makeMove(move);
 
 					hashOut = hashTable[board.getHashIndex()];
 
 					if (hashOut != null) {
 						if (hashOut.getLevel() >= level) {
 							// use hashed value
 							hashHit = true;
 						}
 					}
 
 					if (!hashHit) {
 						suggestedPathValue = growDecisionTreeLite(board, move, childsBestPathValue, level - 1);
 					} else {
 						suggestedPathValue = hashOut.getScore();
 					}
 
 					board.undoMove();
 
 					board.setBoardStatus(tempBoardState);
 
 					// hashTable.put(board.getHashCode(), new
 					// BoardHashEntry(level, suggestedPathValue));
 				} else {
 					suggestedPathValue = move.getValue();
 					// hashTable.put(board.getHashCode(), new
 					// BoardHashEntry(0, suggestedPathValue));
 				}
 
 				if (suggestedPathValue > childsBestPathValue) {
 					childsBestPathValue = suggestedPathValue;
 				}
 
 				if (parentMove.getValue() - childsBestPathValue <= alphaBeta + aspirationWindowSize) {
 					break;
 				}
 
 			}
 
 		}
 
 		int parentsBestPathValue;
 
 		if (board.isInCheckMate()) {
 			parentsBestPathValue = Values.CHECKMATE_MOVE - (maxTwigLevel - level + maxTreeLevel + 1);
 		} else {
 			if (board.isInStaleMate() || board.isDraw()) {
 				parentsBestPathValue = (board.winningBy(board.getTurn()) + parentMove.getValue()) / Values.DRAW_DIVISOR;
 			} else {
 				parentsBestPathValue = parentMove.getValue() - childsBestPathValue;
 			}
 		}
 
 		return parentsBestPathValue;
 	}
 
 	public synchronized void setMaxTreeLevel(int maxTreeLevel) {
 		this.maxTreeLevel = maxTreeLevel;
 	}
 
 	public synchronized void setMaxTwigLevel(int maxTwigLevel) {
 		this.maxTwigLevel = maxTwigLevel;
 	}
 
 }
