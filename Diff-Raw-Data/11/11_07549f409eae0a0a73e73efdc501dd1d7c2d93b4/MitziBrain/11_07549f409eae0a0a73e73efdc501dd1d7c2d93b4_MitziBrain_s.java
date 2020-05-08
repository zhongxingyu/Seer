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
 	 * NegaMax with Alpha Beta Pruning. Furthermore the function sets the
 	 * variable next_move.
 	 * 
 	 * @see <a href="https://en.wikipedia.org/wiki/Negamax">Negamax</a>
 	 * 
 	 * @param board
 	 *            the current board
 	 * @param depth
 	 *            the search depth
 	 * @param alpha
 	 * @param beta
 	 * @param side_sign
 	 *            white's turn +1, black's turn -1
 	 * @return sets the best move and returns the value
 	 */
 	private double evalBoard(IBoard board, int depth, double alpha,
 			double beta, int side_sign) {
 		// TODO: there is still a problem, that the function returns a NULL -
 		// move although some are possible... especially for check positions
 
 		// if the base case is reached
 		if (depth == 0 || board.isStaleMatePosition() || board.isMatePosition()) {
 			return evalBoard0(board) * side_sign;
 
 		}
 
 		double best_value = -10 ^ 6;
 		double val = 0;
 		Set<IMove> moves = board.getPossibleMoves();
 		// TODO: order moves for best alpha-beta effect
 		for (IMove move : moves) {
 			val = -evalBoard(board.doMove(move), depth - 1, -beta, -alpha,
 					-side_sign);
 			if (val >= best_value) {
 				best_value = val;
				next_move = move;
 			}
 			alpha = Math.max(alpha, val);
 			if (alpha >= beta) {
 				break;
 			}
 		}
 
 		return best_value;
 
 	}
 
 	/**
 	 * returns the value of a board.
 	 * 
 	 * @param board
 	 *            the board to be analyzed
 	 * @return the value of a board
 	 */
 	private double evalBoard0(IBoard board) {
 
 		// check for checkmate
 		if (board.isMatePosition()) {
 			if (board.getActiveColor() == PieceHelper.WHITE)
 				return -10 ^ 6;
 			else
 				return 10 ^ 6;
 		}
 
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
 
 		return value;
 	}
 
 	@Override
 	public IMove search(int movetime, int maxMoveTime, int searchDepth,
 			boolean infinite, Set<IMove> searchMoves) {
 
 		// first of all, ignoring the timings and restriction to certain
 		// moves...
 
 		int side_color = -1;
 		if (board.getActiveColor() == PieceHelper.WHITE) {
 			side_color = 1;
 		}
 		@SuppressWarnings("unused")
 		double value = evalBoard(board, searchDepth, -10 ^ 6, 10 ^ 6,
 				side_color); // value might be interesting for debugging
 
 		return next_move;
 	}
 
 	@Override
 	public IMove stop() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
