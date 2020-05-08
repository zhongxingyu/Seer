 package mitzi;
 
 import java.util.Set;
 
 public class MitziBrain implements IBrain {
 
 	private IBoard board;
 
 	private IMove next_move;
 
 	@Override
 	public void set(IBoard board) {
 		this.board = board;
 	}
 
 	/**
 	 * Basic minimax Algorithm, like in the lecture notes. Furthermore the
 	 * function sets the variable next_move.
 	 * 
 	 * @param board
 	 *            the current board
 	 * @param depth
 	 *            the search depth
 	 * @return searches the best move and returns the value
 	 */
 	private double evalBoard(IBoard board, int depth) {
 
 		// if the base case is reached
		if (depth == 0 || board.isStaleMatePosition() || board.isMatePosition()) {
 			return evalBoard0(board);
 
 		}// if the best move has to be found
 		else if (board.getActiveColor() == PieceHelper.WHITE) {
 			double max = -10 ^ 6; // some large negative number....
 			double val;
 			IMove best_move = null;
 
 			Set<IMove> moves = board.getPossibleMoves();
 			for (IMove move : moves) {
 				val = evalBoard(board.doMove(move), depth - 1);
 				if (max < val) {
 					max = val;
 					best_move = move;
 				}
 			}
 			next_move = best_move;
 			return max;
 
 		} // if the worst move has to be found
 		else if (board.getActiveColor() == PieceHelper.BLACK) {
 			double min = 10 ^ 6; // some large positive number....
 			double val;
 			IMove worst_move = null;
 
 			Set<IMove> moves = board.getPossibleMoves();
 			for (IMove move : moves) {
 				val = evalBoard(board.doMove(move), depth - 1);
 				if (min > val) {
 					min = val;
 					worst_move = move;
 				}
 			}
 			next_move = worst_move;
 			return min;
 		}
 
 		return 0; // cannot happen anyway.
 	}
 
 	/**
 	 * returns the value of a board.
 	 * 
 	 * @param board
 	 *            the board to be analyzed
 	 * @return the value of a board
 	 */
 	private double evalBoard0(IBoard board) {
 
 		// A very very simple implementation
 		double value = 0;
 
 		// One way to prevent copy and paste
 		double[] fig_value = { 1, 3.3, 3.3, 5, 9 };
 		int[] colors = { PieceHelper.WHITE, PieceHelper.BLACK };
 		int[] figure = { PieceHelper.PAWN, PieceHelper.BISHOP,
 				PieceHelper.KNIGHT, PieceHelper.ROOK, PieceHelper.QUEEN };
 
 		// Maybe not the most efficient way (several runs over the board)
 		for (int c = 0; c < 2; c++) {
 			for (int fig = 0; fig < 5; fig++) {
 				if (c == 0)
 					value += board.getNumberOfPiecesByColorAndType(colors[c],
 							figure[fig]) * fig_value[fig];
 				else
 					value -= board.getNumberOfPiecesByColorAndType(colors[c],
 							figure[fig]) * fig_value[fig];
 			}
 
 		}
 
 		if (board.getActiveColor() == PieceHelper.WHITE
 				&& board.isMatePosition())
 			value -= 10 ^ 6; // subtract a large number, bad for me
 		else if (board.getActiveColor() == PieceHelper.WHITE
 				&& board.isMatePosition())
 			value += 10 ^ 6; // add a large number, good for me
 		return value;
 	}
 
 	@Override
 	public IMove search(int movetime, int maxMoveTime, int searchDepth,
 			boolean infinite, Set<IMove> searchMoves) {
 
 		// first of all, ignoring the timings and restriction to certain
 		// moves...
 
 		@SuppressWarnings("unused")
 		double value = evalBoard(board, searchDepth); // value might be
 														// interesting for
 														// debugging
 
 		return next_move;
 	}
 
 	@Override
 	public IMove stop() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
