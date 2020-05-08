 package mitzi;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import static mitzi.MateScores.*;
 import mitzi.UCIReporter.InfoType;
 
 public class MitziBrain implements IBrain {
 
 	private IBoard board;
 
 	private Variation principal_variation;
 
 	private long eval_counter;
	
 	private IBoardAnalyzer board_analyzer = new PrimitiveBoardAnalyzer();
 
 	@Override
 	public void set(IBoard board) {
 		this.board = board;
 		this.eval_counter = 0;
 		this.principal_variation = null;
 	}
 
 	/**
 	 * Sends updates about evaluation status to UCI GUI.
 	 * 
 	 */
 	class UCIUpdater extends TimerTask {
 
 		private long old_mtime;
 		private long old_eval_counter;
 
 		@Override
 		public void run() {
 			long mtime = System.currentTimeMillis();
 			long eval_span = eval_counter - old_eval_counter;
 
 			if (old_mtime != 0) {
 				long time_span = mtime - old_mtime;
 				UCIReporter.sendInfoNum(InfoType.NPS, eval_span * 1000
 						/ time_span);
 			}
 
 			old_mtime = mtime;
 			old_eval_counter += eval_span;
 
 		}
 	}
 
 	/**
 	 * 
 	 * This Comparator class implements the comparing of two moves. The moves
 	 * are saved intern as an ArrayList for a simpler search.
 	 * 
 	 */
 	class MoveComperator implements Comparator<IMove> {
 
 		private double[] board_values;
 		private ArrayList<IMove> moves;
 
 		public void compute_values(Set<IMove> moves, IBoard board, int side_sign) {
 			this.moves = new ArrayList<IMove>(moves);
 			board_values = new double[moves.size()];
 			int i = 0;
 			for (IMove move : this.moves) {
				board_values[i] = side_sign * board_analyzer.eval0(board.doMove(move)).getScore();
 				i++;
 			}
 		}
 
 		public int compare(IMove m1, IMove m2) {
 
 			int i1 = moves.indexOf(m1);
 			int i2 = moves.indexOf(m2);
 
 			// "-" to receive an increasing ordering
 			return -Double.compare(board_values[i1], board_values[i2]);
 
 		}
 	}
 
 	/**
 	 * Sorts the moves w.r.t. the value of the next board (base case). If the
 	 * depth is 1 (so after one move the base case is reached) no sorting is
 	 * done. (preventing double evaluation of board)
 	 * 
 	 * @param moves
 	 *            the set of moves to be sorted
 	 * @param board
 	 *            the actual board
 	 * @param side_sign
 	 *            the actual side
 	 * @param depth
 	 *            the current search depth
 	 * @return the sorted set of moves in a TreeSet
 	 */
 	private ArrayList<IMove> sortMoves(Set<IMove> moves, IBoard board,
 			int side_sign, int depth) {
 
 		ArrayList<IMove> ordered_moves;
 		ordered_moves = new ArrayList<IMove>(moves);
 		if (depth != 1) {
 			MoveComperator my_comp = new MoveComperator();
 			my_comp.compute_values(moves, board, side_sign);
 
 			Collections.sort(ordered_moves, my_comp);
 		}
 
 		return ordered_moves;
 	}
 
 	/**
 	 * NegaMax with Alpha Beta Pruning
 	 * 
 	 * @see <a
 	 *      href="https://en.wikipedia.org/wiki/Negamax#NegaMax_with_Alpha_Beta_Pruning">NegaMax
 	 *      with Alpha Beta Pruning</a>
 	 * @param board
 	 *            the current board
 	 * @param total_depth
 	 *            the total depth to search
 	 * @param depth
 	 *            the remaining depth to search
 	 * @param alpha
 	 * @param beta
 	 * @return returns a Variation tree
 	 */
 	private Variation evalBoard(IBoard board, int total_depth, int depth,
 			int alpha, int beta, Variation old_tree) {
 
 		// whose move is it?
 		Side side = board.getActiveColor();
 		int side_sign = Side.getSideSign(side);
 
 		// generate moves
 		Set<IMove> moves = board.getPossibleMoves();
 
 		// check for mate and stalemate (the side should alternate)
 		if (moves.isEmpty()) {
 			Variation base_variation;
 			if (board.isCheckPosition()) {
 				base_variation = new Variation(null, NEG_INF * side_sign,
 						Side.getOppositeSide(side));
 			} else {
 				base_variation = new Variation(null, 0,
 						Side.getOppositeSide(side));
 			}
 			return base_variation;
 		}
 
 		// base case (the side should alternate)
 		if (depth == 0) {
 			AnalysisResult result = board_analyzer.eval0(board);
 			Variation base_variation = new Variation(null, result.getScore(),
 					Side.getOppositeSide(side));
 			return base_variation;
 		}
 
 		int best_value = NEG_INF; // this starts always at negative!
 
 		// Sort the moves:
 		ArrayList<IMove> ordered_moves;
 		ArrayList<Variation> ordered_variations = null;
 		if (old_tree == null || old_tree.getSubVariations().isEmpty()) {
 			// no previous computation given
 			ordered_moves = sortMoves(moves, board, side_sign, depth);
 		} else {
 			// use old Variation tree for ordering
 			Set<Variation> children = old_tree.getSubVariations();
 			ordered_variations = new ArrayList<Variation>(children);
 			Collections.sort(ordered_variations);
 			if (side == Side.WHITE)
 				Collections.reverse(ordered_variations);
 			ordered_moves = new ArrayList<IMove>();
 			for (Variation var : ordered_variations) {
 				ordered_moves.add(var.getMove());
 			}
 			// add remaining moves
 			for (IMove move : moves) {
 				if (!ordered_moves.contains(move))
 					ordered_moves.add(move);
 			}
 		}
 
 		// create new parent Variation
 		Variation parent = new Variation(null, NEG_INF,
 				Side.getOppositeSide(side));
 
 		int i = 0;
 		// alpha beta search
 		for (IMove move : ordered_moves) {
 
 			if (depth == total_depth && total_depth >= 6) {
 				// output currently searched move to UCI
 				UCIReporter.sendInfoCurrMove(move, i + 1);
 			}
 
 			Variation variation;
 			if (ordered_variations != null && i < ordered_variations.size()) {
 				variation = evalBoard(board.doMove(move), total_depth,
 						depth - 1, -beta, -alpha, ordered_variations.get(i));
 			} else {
 				variation = evalBoard(board.doMove(move), total_depth,
 						depth - 1, -beta, -alpha);
 			}
 			int negaval = variation.getValue() * side_sign;
 
 			// better variation found
 			if (negaval >= best_value) {
 				boolean truly_better = negaval > best_value;
 				best_value = negaval;
 
 				// update variation tree
 				parent.update(null, variation.getValue());
 
 				// update the missing move for the child
 				variation.update(move, variation.getValue());
 
 				parent.addSubVariation(variation);
 
 				// output to UCI
 				if (depth == total_depth && truly_better) {
 
 					principal_variation = parent.getPrincipalVariation();
 					UCIReporter.sendInfoPV(principal_variation, total_depth,
 							variation.getValue(), board.getActiveColor());
 
 				}
 			}
 
 			// alpha beta cutoff
 			alpha = Math.max(alpha, negaval);
 			if (alpha >= beta)
 				break;
 
 			i++; // keep ordered_moves and ordered_variations in sync
 		}
 
 		return parent;
 
 	}
 
 	private Variation evalBoard(IBoard board, int total_depth, int depth,
 			int alpha, int beta) {
 		return evalBoard(board, total_depth, depth, alpha, beta, null);
 	}
 
 	@Override
 	public IMove search(int movetime, int maxMoveTime, int searchDepth,
 			boolean infinite, Set<IMove> searchMoves) {
 
 		// first of all, ignoring the timings and restriction to certain
 		// moves...
 
 		Timer timer = new Timer();
 		timer.scheduleAtFixedRate(new UCIUpdater(), 1000, 5000);
 
 		// iterative deepening
 		Variation var_tree = null; // TODO: use previous searches as starting
 									// point
 		Variation var_tree_temp;
 
 		// Parameters for aspiration windows
 		int alpha = NEG_INF; // initial value
 		int beta = POS_INF; // initial value
 		int asp_window = 200; // often 50 or 25 is used
 		int factor = 3; // factor for increasing if out of bounds
 
 		for (int current_depth = 1; current_depth < searchDepth; current_depth++) {
 			this.principal_variation = null;
 			var_tree_temp = evalBoard(board, current_depth, current_depth,
 					alpha, beta, var_tree);
 			// mate found
 			if (principal_variation != null
 					&& principal_variation.getValue() == POS_INF
 					&& board.getActiveColor() == Side.WHITE
 					|| principal_variation.getValue() == NEG_INF
 					&& board.getActiveColor() == Side.BLACK) {
 				timer.cancel();
 
 				return principal_variation.getMove();
 			}
 			// If Value is out of bounds, redo search with larger bounds, but
 			// with the same variation tree
 			if (var_tree_temp.getValue() <= alpha) {
 				alpha -= factor * asp_window;
 				current_depth--;
 				continue;
 			} else if (var_tree_temp.getValue() >= beta) {
 				beta += factor * asp_window;
 				current_depth--;
 				continue;
 			}
 
 			alpha = var_tree_temp.getValue() - asp_window;
 			beta = var_tree_temp.getValue() + asp_window;
 
 			var_tree = var_tree_temp;
 		}
 
 		// repeat until a value inside the alpha-beta bound is found.
 		while (true) {
 			this.principal_variation = null;
 			var_tree_temp = evalBoard(board, searchDepth, searchDepth, alpha,
 					beta, var_tree);
 			if (var_tree_temp.getValue() <= alpha) {
 				alpha -= factor * asp_window;
 			} else if (var_tree_temp.getValue() >= beta) {
 				beta += factor * asp_window;
 			} else {
 				var_tree = var_tree_temp;
 				break;
 			}
 		}
		
 		timer.cancel();
 
 		if (principal_variation != null) {
 			return principal_variation.getMove();
 		} else {
 			// mitzi cannot avoid mate :(
 			return var_tree.getBestMove();
 		}
 	}
 
 	@Override
 	public IMove stop() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
