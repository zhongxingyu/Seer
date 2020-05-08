 // An iterative deepening minimax algorithm that uses an MCBoard
import java.util.HashMap;
 
 public class MinimaxThread implements Runnable {
 	public MCBoard board;
 	public int depth;
 	public HashMap<Tuple, Integer> moveEvals;
 	boolean cont = true;
 	boolean verbose = false;
 
 	public MinimaxThread(MCBoard board) {
 		this.board = board;
 		depth = 1;
 		moveEvals = new HashMap<Tuple, Integer>();
 		for (Tuple t : board.legalMoves) {
 			moveEvals.put(t, new Integer(0));
 		}
 	}
 
 	public MinimaxThread(MCBoard board, int initialDepth) {
 		this.board = board;
 		depth = initialDepth;
 		moveEvals = new HashMap<Tuple, Integer>();
 		for (Tuple t : board.legalMoves) {
 			moveEvals.put(t, new Integer(0));
 		}
 	}
 
 	public MinimaxThread(MCBoard board, int initialDepth, boolean verbose) {
 		this.board = board;
 		depth = initialDepth;
 		moveEvals = new HashMap<Tuple, Integer>();
 		for (Tuple t : board.legalMoves) {
 			moveEvals.put(t, new Integer(0));
 		}
 		this.verbose = verbose;
 	}
 
 	public int minimax(int currentDepth) {
 	    if (currentDepth == depth || board.legalMoves.isEmpty()) {
 	    	int eval = board.eval();
 	    	board.takeBack(1);
 	    	return eval;
 	    }
 	    int x = Integer.MIN_VALUE;
 	    for (int i=0;i<board.legalMoves.size();i++) {
 	        board.move(board.legalMoves.get(i));
	        x = max(x, -1*minimax(currentDepth+1));
 	        board.takeBack(1);
 	    }
	    return x;
 	}
 
 	public void run() {
 		while(cont) {
 			for (Tuple t : moveEvals.keySet()) {
				moveEvals.put(t, minimax(0));
 			}
 			if (verbose)
 				System.out.println(toString());
 			depth += 1;
 		}
 	}
 
 	public int max(int x, int y) {
 		if (x > y)
 			return x;
 		return y;
 	}
 	public String toString(){
 		String out = "Move evaluations at depth " + depth + ":\n";
 		for (Tuple t : moveEvals.keySet()) {
 			out += t.toString() + "\t" + moveEvals.get(t) + "\n";
 		}
 		return out;
 	}
 }
 
 
