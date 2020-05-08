 package eighteen;
 
 import eighteen.Board.BadMoveException;
 import eighteen.Board.GameOverException;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 public class AI {
 	// Will need to implement Thread at some point in the future
 	
 	private Tree minMaxTree;
 	private Color myColor;
 	private int levels;
 	private long maxTime;
 	public ArrayList<Move> bestMove = new ArrayList<Move>();
 	
 	public AI(Color color, long time) {
 		minMaxTree = new Tree();
 		myColor = color;
 		levels = 1;
 		maxTime = time;
 	}
 	
 	// Adds a new level to the MiniMax tree
 	public void getNewLevel() throws BadMoveException {
 		addLevel(minMaxTree.getRoot());
 		levels++;
 	}
 	
 	public Color getColor() {
 		return myColor;
 	}
 	
 	public void addLevel(TreeNode node) throws BadMoveException {
 		// If the node doesn't have children, we've reached the bottom of the tree
 		if(!node.hasChildren()) {
 			Color color = Board.oppositeColor(node.board.chainColor);
 			ArrayList<Move> validMoves = node.board.getValidMoves(color);
 			for(Move move: validMoves) {
 				// Must create a duplicate board to not alter the original one
 				Board newBoard = new Board(node.board); 
 				try {
 					newBoard.move(move);
 				} catch (GameOverException e) {
					System.out.println("Game over");
 				}
 				TreeNode newChild = new TreeNode(newBoard);
 				node.addChild(newChild);
 				// Checks for chain possibilities
 				if(move.getState() == AttackState.ADVANCING || move.getState() == AttackState.WITHDRAWING)
 					chainCheck(newChild);
 			}
 		}
 		// Haven't reached the deepest level, keep iterating
 		else
 			for(TreeNode child: node.getChildren())
 				addLevel(child);
 		displayChoices();
 	}
 	
 	void displayChoices() {
 		for(TreeNode child: minMaxTree.getRoot().getChildren()) {
 		}
 	}
 	
 	// Checks for chain possibilities of a created child node
 	public void chainCheck(TreeNode child) throws BadMoveException {
 		Piece.adjLoc previousSpot = child.board.previousSpot;
 		// Ensures that no incorrect chain assumptions are made
 		if(child.board.turn == myColor) {
 			ArrayList<Move> chainMoves = child.board.getValidChainMoves(previousSpot);
 			System.out.println("Found " + chainMoves.size() + " chain possiblities.");
 			for(Move move: chainMoves) {
 				Board newBoard = new Board(child.board);
 				try {
 					newBoard.move(move);
 				} catch (GameOverException e) {
 					e.printStackTrace();
 				}
 				// It's a sibling NOT a child
 				TreeNode newSibling = new TreeNode(newBoard);
 				child.getParent().addChild(newSibling);
 				chainCheck(newSibling);
 			}
 		}
 	}
 	
 	// Finds opponent's move and updates the board accordingly
 	public void opponentMove(Board newBoard) {
 		TreeNode root = minMaxTree.getRoot();
 		boolean found = false;
 		// Somehow tree has no children, make a new root node
 		if(!root.hasChildren()) {
 			System.out.println("AI had no children in tree.");
 			TreeNode newRoot = new TreeNode(newBoard);
 			minMaxTree.setRoot(newRoot);
 		}
 		// Iterate through children and find the board
 		else {
 			System.out.println("AI had children in tree");
 			for(TreeNode child: root.getChildren()) {
 				if(child.board.equals(newBoard)) {
 					found = true;
 					System.out.println("AI found the move made.");
 					minMaxTree.setRoot(child);
 					break;
 				}
 			}
 		}
 		// Unknown move, create a new node and start a new tree
 		if(!found)
 			minMaxTree.setRoot(new TreeNode(newBoard));
 	}
 	
 	static ArrayList<Move> ret = new ArrayList<Move>();
 	static long startTime;
 	
 	// Searches for the best board state and returns the corresponding moves
 	public ArrayList<Move> alphaBetaSearch() throws BadMoveException {
 		ExecutorService service = Executors.newSingleThreadExecutor();
 		try {
 			startTime = System.currentTimeMillis();
 		    Runnable runnable = new Runnable() {
 		        @Override
 		        public void run() {
 		        	//while(true) {
 			        	TreeNode root = minMaxTree.getRoot();
 			    		try {
 							getNewLevel();
 						} catch (BadMoveException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 			    		double value = 0;
 			    		// How we start iterating depends on my color and the board's turn
 			    		if (root.board.chainColor != myColor)
 			    			if(myColor == Color.WHITE)
 			    				value = maxValue(root, -999999, 999999);
 			    			else
 			    				value = minValue(root, -999999, 999999);
 			    		else
 			    			if(myColor == Color.WHITE)
 			    				value = minValue(root, -999999, 999999);
 			    			else
 			    				value = maxValue(root, -999999, 999999);
 			    		// Finds the node with the highest value
 			    		for(TreeNode child: root.getChildren()) {
 			    			if(child.traversalValue == value) {
 			    				minMaxTree.setRoot(child);
 			    				ret = new ArrayList<Move>(child.getMoves());
 			    			}
 			    		}
 			    		if(System.currentTimeMillis() - startTime > maxTime) {
 			    			//break;
 			    		}
 		        	//}
 		        }
 		    };
 
 		    Future<?> future = service.submit(runnable);
 
 		    future.get(maxTime, TimeUnit.MILLISECONDS);     // attempt the task for two minutes
 		}
 		catch (final InterruptedException e) {
 		}
 		catch (final TimeoutException e) {
 		}
 		catch (final ExecutionException e) {
 		}
 		finally {
 		    service.shutdown();
 		}
 		// Fall-back for if no moves are found
 		System.out.println("No move found");
 		return ret;
 	}
 	
 	public double maxValue(TreeNode state, double alpha, double beta) {
 		if(!state.hasChildren())
 			return state.value;
 		
 		for(TreeNode child: state.getChildren()) {
 			alpha = Math.max(alpha,  minValue(child, alpha, beta));
 			child.traversalValue = alpha;
 			if(beta <= alpha) {
 				// Beta cut-off
 				break;
 			}
 		}
 		return alpha;
 	}
 	
 	public double minValue(TreeNode state, double alpha, double beta) {
 		if(!state.hasChildren())
 			return state.value;
 		
 		for(TreeNode child: state.getChildren()) {
 			beta = Math.min(beta, maxValue(child, alpha, beta));
 			child.traversalValue = beta;
 			if(beta <= alpha) {
 				// Alpha cut-off
 				break;
 			}
 		}
 		return beta;
 	}
 }
