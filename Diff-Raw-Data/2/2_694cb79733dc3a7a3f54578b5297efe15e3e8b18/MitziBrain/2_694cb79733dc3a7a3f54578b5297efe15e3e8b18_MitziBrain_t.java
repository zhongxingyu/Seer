 package mitzi;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import static mitzi.MateScores.*;
 import mitzi.UCIReporter.InfoType;
 
 public class MitziBrain implements IBrain {
 
 	private GameState game_state;
 
 	private long eval_counter;
 
 	private long table_counter = 0;
 
 	private IPositionAnalyzer board_analyzer = new BoardAnalyzer();
 
 	private long start_mtime = System.currentTimeMillis();
 
 	@Override
 	public void set(GameState game_state) {
 		this.game_state = game_state;
 		this.eval_counter = 0;
 	}
 
 	private long runTime() {
 		return System.currentTimeMillis() - start_mtime;
 	}
 
 	/**
 	 * Sends updates about evaluation status to UCI GUI.
 	 * 
 	 */
 	class UCIUpdater extends TimerTask {
 		private long old_mtime;
 		private long old_eval_counter;
 		private long old_eval_counter_seldepth;
 
 		@Override
 		public void run() {
 			long mtime = System.currentTimeMillis();
 
 			long eval_span_0 = eval_counter - old_eval_counter;
 			long eval_span_sel = +BoardAnalyzer.eval_counter_seldepth
 					- old_eval_counter_seldepth;
 			long eval_span = eval_span_0 + eval_span_sel;
 
 			if (old_mtime != 0) {
 				long time_span = mtime - old_mtime;
 				UCIReporter.sendInfoNum(InfoType.NPS, eval_span * 1000
 						/ time_span);
 
 				UCIReporter.sendInfoNum(InfoType.HASHFULL,
 						ResultCache.getHashfull());
 			}
 
 			old_mtime = mtime;
 			old_eval_counter += eval_span_0;
 			old_eval_counter_seldepth += eval_span_sel;
 
 		}
 	}
 
 	/**
 	 * NegaMax with Alpha Beta Pruning and Transposition Tables
 	 * 
 	 * @see <a
 	 *      href="http://en.wikipedia.org/wiki/Negamax#NegaMax_with_Alpha_Beta_Pruning_and_Transposition_Tables">NegaMax
 	 *      with Alpha Beta Pruning and Transposition Tables</a>
 	 * @param position
 	 *            the position to evaluate
 	 * @param total_depth
 	 *            the total depth to search
 	 * @param depth
 	 *            the remaining depth to search
 	 * @param alpha
 	 *            the alpha value
 	 * @param beta
 	 *            the beta value
 	 * @return returns the result of the evaluation
 	 */
 	private AnalysisResult evalBoard(IPosition position, int total_depth,
 			int depth, int alpha, int beta) {
 
 		// ---------------------------------------------------------------------------------------
 		int alpha_old = alpha;
 
 		// Cache lookup
 		AnalysisResult entry = ResultCache.getResult(position);
 		if (entry != null && entry.plys_to_eval0 >= depth) {
 			table_counter++;
 			if (entry.flag == Flag.EXACT)
 				return entry.tinyCopy();
 			else if (entry.flag == Flag.LOWERBOUND)
 				alpha = Math.max(alpha, entry.score);
 			else if (entry.flag == Flag.UPPERBOUND)
 				beta = Math.min(beta, entry.score);
 
 			if (alpha >= beta)
 				return entry.tinyCopy();
 		}
 
 		// ---------------------------------------------------------------------------------------
 		// base of complete tree search
 		if (depth == 0) {
 			// position is a leaf node
 			return board_analyzer.evalBoard(position, alpha, beta);
 		}
 
 		// ---------------------------------------------------------------------------------------
 		// whose move is it?
 		Side side = position.getActiveColor();
 		int side_sign = Side.getSideSign(side);
 
 		// ---------------------------------------------------------------------------------------
 		// generate moves
 		List<IMove> moves = position.getPossibleMoves();
 
 		// check for mate and stalemate
 		if (moves.isEmpty()) {
 			eval_counter++;
 			if (position.isCheckPosition()) {
 				return new AnalysisResult(NEG_INF * side_sign, false, false, 0,
 						0, Flag.EXACT);
 			} else {
 				return new AnalysisResult(0, true, false, 0, 0, Flag.EXACT);
 			}
 		}
 		// ---------------------------------------------------------------------------------------
 		// Sort the moves:
 		ArrayList<IMove> ordered_moves = new ArrayList<IMove>(40);
 		ArrayList<IMove> remaining_moves = new ArrayList<IMove>(40);
 		BasicMoveComparator move_comparator = new BasicMoveComparator(position);
 
 		// Get Killer Moves:
 		List<IMove> killer_moves = KillerMoves.getKillerMoves(total_depth
 				- depth);
 
 		// if possible use the moves from Position cache as the moves with
 		// highest priority
 		if (entry != null) {
 			ordered_moves.addAll(entry.best_moves);
 
 			for (IMove k_move : killer_moves)
 				if (position.isPossibleMove(k_move)
 						&& !ordered_moves.contains(k_move))
 					ordered_moves.add(k_move);
 
 		} else {
 			// Killer_moves have highest priority
 			for (IMove k_move : killer_moves)
 				if (position.isPossibleMove(k_move))
 					ordered_moves.add(k_move);
 		}
 		// add the remaining moves and sort them using a basic heuristic
 		for (IMove move : moves)
 			if (!ordered_moves.contains(move))
 				remaining_moves.add(move);
 
 		Collections.sort(remaining_moves,
 				Collections.reverseOrder(move_comparator));
 		ordered_moves.addAll(remaining_moves);
 		// ---------------------------------------------------------------------------------------
 
 		if (entry != null && entry.plys_to_eval0 < depth)
 			entry.best_moves.clear();
 
 		// create new AnalysisResult and parent
 		AnalysisResult new_entry = null, parent = null;
 		if (entry == null)
 			new_entry = new AnalysisResult(0, null, false, 0, 0, null);
 
 		int best_value = NEG_INF; // this starts always at negative!
 
 		int i = 0;
 		// alpha beta search
 		for (IMove move : ordered_moves) {
 
 			if (depth == total_depth && total_depth >= 6) {
 				// output currently searched move to UCI
 				UCIReporter.sendInfoCurrMove(move, i + 1);
 			}
 
 			IPosition child_pos = position.doMove(move).new_position;
 			AnalysisResult result = evalBoard(child_pos, total_depth,
 					depth - 1, -beta, -alpha);
 
 			int negaval = result.score * side_sign;
 
 			// better variation found
			if (negaval > best_value || parent == null) {
 
 				best_value = negaval;
 
 				// update cache entry
 				if (entry != null && entry.plys_to_eval0 < depth)
 					entry.best_moves.add(move);
 				if (entry == null)
 					new_entry.best_moves.add(move);
 
 				// update AnalysisResult
 				byte old_seldepth = (parent == null ? 0
 						: parent.plys_to_seldepth);
 				parent = result; // change reference
 				parent.best_move = move;
 				parent.plys_to_eval0 = (byte) depth;
 				if (best_value != POS_INF) {
 					parent.plys_to_seldepth = (byte) Math.max(old_seldepth,
 							parent.plys_to_seldepth);
 				}
 
 				// output to UCI
 				// boolean truly_better = negaval > best_value;
 				if (depth == total_depth) { // && truly_better) {
 					position.updateAnalysisResult(parent);
 					UCIReporter.sendInfoPV(game_state.getPosition(), runTime());
 				}
 			}
 
 			// alpha beta cutoff
 			alpha = Math.max(alpha, negaval);
 			if (alpha >= beta) {
 				// set also KillerMove:
 				if (!killer_moves.contains(move))
 					KillerMoves.addKillerMove(total_depth - depth, move,
 							killer_moves);
 				break;
 			}
 
 			i++;
 		}
 
 		// ---------------------------------------------------------------------------------------
 		// Transposition Table Store; game_state is the lookup key for parent
 		if (parent.score <= alpha_old)
 			parent.flag = Flag.UPPERBOUND;
 		else if (parent.score >= beta)
 			parent.flag = Flag.LOWERBOUND;
 		else
 			parent.flag = Flag.EXACT;
 
 		if (entry != null && entry.plys_to_eval0 < depth) {
 			entry.tinySet(parent);
 			Collections.reverse(entry.best_moves);
 		}
 
 		if (entry == null) {
 			new_entry.tinySet(parent);
 			Collections.reverse(new_entry.best_moves);
 			ResultCache.setResult(position, new_entry);
 		}
 
 		return parent;
 
 	}
 
 	@Override
 	public IMove search(int movetime, int maxMoveTime, int searchDepth,
 			boolean infinite, List<IMove> searchMoves) {
 
 		// first of all, ignoring the timings and restriction to certain
 		// moves...
 
 		IPosition position = game_state.getPosition();
 
 		Timer timer = new Timer();
 		timer.scheduleAtFixedRate(new UCIUpdater(), 1000, 5000);
 		start_mtime = System.currentTimeMillis();
 
 		// iterative deepening
 		AnalysisResult result = null;
 
 		// Parameters for aspiration windows
 		int alpha = NEG_INF; // initial value
 		int beta = POS_INF; // initial value
 		int asp_window = 25; // often 50 or 25 is used
 		int factor = 2; // factor for increasing if out of bounds
 
 		for (int current_depth = 1; current_depth <= searchDepth; current_depth++) {
 			table_counter = 0;
 			result = evalBoard(position, current_depth, current_depth, alpha,
 					beta);
 			position.updateAnalysisResult(result);
 
 			if (result.score == POS_INF || result.score == NEG_INF) {
 				break;
 			}
 
 			// If Value is out of bounds, redo search with larger bounds, but
 			// with the same variation tree
 			if (result.score <= alpha) {
 				alpha -= factor * asp_window;
 				current_depth--;
 				UCIReporter.sendInfoString("Boards found: " + table_counter);
 				continue;
 			} else if (result.score >= beta) {
 				beta += factor * asp_window;
 				current_depth--;
 				UCIReporter.sendInfoString("Boards found: " + table_counter);
 				continue;
 			}
 
 			alpha = result.score - asp_window;
 			beta = result.score + asp_window;
 
 			UCIReporter.sendInfoString("Boards found: " + table_counter);
 		}
 
 		timer.cancel();
 		UCIReporter.sendInfoPV(position, runTime());
 		KillerMoves.updateKillerMove();
 		return result.best_move;
 	}
 
 	@Override
 	public IMove stop() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
