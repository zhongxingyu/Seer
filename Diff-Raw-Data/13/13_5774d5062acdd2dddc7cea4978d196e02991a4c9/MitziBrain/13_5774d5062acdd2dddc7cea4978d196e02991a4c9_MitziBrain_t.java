 package mitzi;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 //import mitzi.UCIReporter.InfoType;
 
 public class MitziBrain implements IBrain {
 
 	private IBoard board;
 
 	private Map<Integer, IMove> principal_variation;
 
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
 	private double evalBoard(IBoard board, int total_depth, int depth,
 			double alpha, double beta, int side_sign) {
 
 		// generate moves
 		Set<IMove> moves = board.getPossibleMoves();
 
 		// check for mate and stalemate
 		if (moves.isEmpty()) {
 			if (board.isCheckPosition()) {
 				// remove rest of pv if favorable mate reached
 				if (board.getActiveColor() == Side.WHITE && side_sign == 1
 						|| board.getActiveColor() == Side.BLACK
 						&& side_sign == -1) {
 					for (int i = total_depth - depth; i < total_depth; i++) {
 						principal_variation.remove(i);
 					}
 				}
 				// return infty
 				if (board.getActiveColor() == Side.WHITE) {
 					return Double.NEGATIVE_INFINITY * side_sign;
 				} else {
 					return Double.POSITIVE_INFINITY * side_sign;
 				}
 			} else {
 				// TODO: truncate pv when forcing stalemate
 				return 0;
 			}
 		}
 
 		// base case
 		if (depth == 0) {
 			return side_sign * evalBoard0(board);
 		}
 
 		double best_value = Double.NEGATIVE_INFINITY;
 		// TODO: order moves for better alpha beta effect
 
 		// alpha beta search
 		for (IMove move : moves) {
 			double val = -evalBoard(board.doMove(move), total_depth, depth - 1,
 					-beta, -alpha, -side_sign);
			/*for (int i = total_depth - depth; i > 0; i--) {
				System.out.print("  ");
			}
			UCIReporter.sendInfoString("move: " + move + " value: " + side_sign
					* val + " board: " + board.doMove(move));*/
 			if (val >= best_value) {
 				best_value = val;
				// FIXME: this doesn't work for deeper moves
 				principal_variation.put(total_depth - depth, move);
 			}
			// alpha = Math.max(alpha, val);
 			if (alpha >= beta)
 				break;
 		}
 
 		return best_value;
 
 	}
 
 	/**
 	 * returns the value of a board. Does NOT recognize mate and stalemate!
 	 * 
 	 * @param board
 	 *            the board to be analyzed
 	 * @return the value of a board
 	 */
 	private double evalBoard0(IBoard board) {
 
 		// A very very simple implementation
 		double value = 0;
 
 		// One way to prevent copy and paste
 		double[] fig_value = { 1, 5, 3.3, 3.3, 9 };
 
 		// Maybe not the most efficient way (several runs over the board)
 		for (Side c : Side.values()) {
 			for (Piece fig : Piece.values()) {
 				if (fig != Piece.KING) {
 					if (c == Side.WHITE)
 						value += board.getNumberOfPiecesByColorAndType(c, fig)
 								* fig_value[fig.ordinal()];
 					else if (c == Side.BLACK)
 						value -= board.getNumberOfPiecesByColorAndType(c, fig)
 								* fig_value[fig.ordinal()];
 				}
 			}
 
 		}
 
 		return value;
 	}
 
 	@Override
 	public IMove search(int movetime, int maxMoveTime, int searchDepth,
 			boolean infinite, Set<IMove> searchMoves) {
 
 		// first of all, ignoring the timings and restriction to certain
 		// moves...
 
 		int side_sign = -1;
 		if (board.getActiveColor() == Side.WHITE) {
 			side_sign = 1;
 		}
 
 		principal_variation = new HashMap<Integer, IMove>();
 
 		double value = side_sign
 				* evalBoard(board, searchDepth, searchDepth,
 						Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
 						side_sign);
		//UCIReporter.sendInfoPV(principal_variation, searchDepth, value);
 		return principal_variation.get(0);
 	}
 
 	@Override
 	public IMove stop() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
