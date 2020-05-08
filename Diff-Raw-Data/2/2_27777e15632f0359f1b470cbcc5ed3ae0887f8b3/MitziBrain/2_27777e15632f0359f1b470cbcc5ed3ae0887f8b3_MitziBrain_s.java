 package mitzi;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import static mitzi.MateScores.*;
 import mitzi.UCIReporter.InfoType;
 
 /**
  * This class implements the AI of Mitzi. The best move is found using the
  * negamax algorithms with Transposition tables. The class regularly sends
  * information about the current search, including nodes per second ("nps"), the
  * filling of the Transposition Table ("hashfull") and the current searched move
  * on top-level. The board evaluation is moved to a separate class
  * BoardAnalyzer.
  * 
  */
 public class MitziBrain implements IBrain {
 
 	/**
 	 * maximal number of threads
 	 */
 	private static final int THREAD_POOL_SIZE = 1;
 
 	/**
 	 * unit for time management
 	 */
 	private static final TimeUnit THREAD_TIMEOUT_UNIT = TimeUnit.MILLISECONDS;
 
 	/**
 	 * timeout for thread shutdown
 	 */
 	private static final int THREAD_TIMEOUT = 1000;
 
 	/**
 	 * upper limit for evaluation time
 	 */
 	private int maxEvalTime;
 
 	/**
 	 * the currently best result
 	 */
 	private AnalysisResult result;
 
 	/**
 	 * the executor for the tasks
 	 */
 	private ExecutorService exe;
 
 	/**
 	 * the current game state
 	 */
 	private GameState game_state;
 
 	private class PositionEvaluator implements Runnable {
 
 		private final IPosition position;
 		private final int searchDepth;
 
 		public PositionEvaluator(final IPosition position, final int depth) {
 			this.position = position;
 			this.searchDepth = depth;
 		}
 
 		@Override
 		public void run() {
 
 			try {
 				// Parameters for aspiration windows
 				int alpha = NEG_INF; // initial value
 				int beta = POS_INF; // initial value
 				int asp_window = 25; // often 50 or 25 is used
 				int factor = 2; // factor for increasing if out of bounds
 
 				// iterative deepening
 				for (int current_depth = 1; current_depth <= searchDepth; current_depth++) {
 					table_counter = 0;
 					BoardAnalyzer.table_counter = 0;
 
 					result = negaMax(position, current_depth, current_depth,
 							alpha, beta);
 					position.updateAnalysisResult(result);
 
 					if (result.score == POS_INF || result.score == NEG_INF) {
 						break;
 					}
 
 					// If Value is out of bounds, redo search with larger
 					// bounds, but with the same variation tree
 					if (result.score <= alpha) {
 						alpha -= factor * asp_window;
 						current_depth--;
 						UCIReporter
 								.sendInfoString("Boards found: "
 										+ (table_counter + BoardAnalyzer.table_counter));
 						continue;
 					} else if (result.score >= beta) {
 						beta += factor * asp_window;
 						current_depth--;
 						UCIReporter
 								.sendInfoString("Boards found: "
 										+ (table_counter + BoardAnalyzer.table_counter));
 						continue;
 					}
 
 					alpha = result.score - asp_window;
 					beta = result.score + asp_window;
 
 					UCIReporter.sendInfoString("Boards found: "
 							+ (table_counter + BoardAnalyzer.table_counter));
 				}
 			} catch (InterruptedException e) {
 			}
 
 		}
 
 		@Override
 		public String toString() {
 			return position.toString();
 		}
 
 	}
 
 	/**
 	 * counts the number of evaluated board
 	 */
 	private long eval_counter;
 
 	/**
 	 * counts the number of found boards in the transposition table.
 	 */
 	private long table_counter;
 
 	/**
 	 * the board analyzer for board evaluation
 	 */
 	private IPositionAnalyzer board_analyzer = new BoardAnalyzer();
 
 	/**
 	 * the current time.
 	 */
 	private long start_mtime = System.currentTimeMillis();
 
 	private Timer timer;
 
 	@Override
 	public void set(GameState game_state) {
 		this.game_state = game_state;
 		this.eval_counter = 0;
 		this.table_counter = 0;
 	}
 
 	/**
 	 * @return the time, which passes since start_mtime
 	 */
 	private long runTime() {
 		return System.currentTimeMillis() - start_mtime;
 	}
 
 	/**
 	 * Sends updates about evaluation status to UCI GUI, namely the number of
 	 * searched board per second and the size of the Transposition Table in
 	 * permill of the maximal size.
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
 			long eval_span_sel = BoardAnalyzer.eval_counter_seldepth
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
 	 * @return returns the result of the evaluation, stored in the class
 	 *         AnalysisResult
 	 * 
 	 * @throws InterruptedException
 	 */
 	private AnalysisResult negaMax(IPosition position, int total_depth,
 			int depth, int alpha, int beta) throws InterruptedException {
 
 		if (Thread.interrupted()) {
 			throw new InterruptedException();
 		}
 		// ---------------------------------------------------------------------------------------
 		// whose move is it?
 		Side side = position.getActiveColor();
 		int side_sign = Side.getSideSign(side);
 
 		// ---------------------------------------------------------------------------------------
 		int alpha_old = alpha;
 
 		// Cache lookup (Transposition Table)
 		AnalysisResult entry = ResultCache.getResult(position);
 		if (entry != null && entry.plys_to_eval0 >= depth) {
 			table_counter++;
 			if (entry.flag == Flag.EXACT)
 				return entry.tinyCopy();
 			else if (entry.flag == Flag.LOWERBOUND)
 				alpha = Math.max(alpha, entry.score * side_sign);
 			else if (entry.flag == Flag.UPPERBOUND)
 				beta = Math.min(beta, entry.score * side_sign);
 
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
 		// generate moves
 		List<IMove> moves = position.getPossibleMoves(true);
 
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
 		int number_legal_movs_TT =0;
 		if (entry != null) {
 			ordered_moves.addAll(entry.best_moves);
 			number_legal_movs_TT = ordered_moves.size();
 
 			for (IMove k_move : killer_moves)
 				if (moves.contains(k_move)
 						&& !ordered_moves.contains(k_move))
 					ordered_moves.add(k_move);
 
 		} else {
 			// Killer_moves have highest priority
 			for (IMove k_move : killer_moves)
 				if (moves.contains(k_move))
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
 		int illegal_move_counter =0;
 		// alpha beta search
 		for (IMove move : ordered_moves) {
 
			if(i<number_legal_movs_TT && position.isCheckAfterMove(move)){
 				illegal_move_counter++;
 				continue;
 			}
 			
 			// output currently searched move to UCI
 			if (depth == total_depth && total_depth >= 6)
 				UCIReporter.sendInfoCurrMove(move, i + 1);
 
 			position.doMove(move);
 			AnalysisResult result = negaMax(position, total_depth, depth - 1,
 					-beta, -alpha);
 			position.undoMove(move);
 
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
 				if (depth == total_depth) {
 					position.updateAnalysisResult(parent);
 					game_state.getPosition().updateAnalysisResult(parent);
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
 		// check for mate and stalemate
 		if (illegal_move_counter == ordered_moves.size()) {
 			eval_counter++;
 			if (position.isCheckPosition()) {
 				return new AnalysisResult(NEG_INF * side_sign, false, false, 0,
 						0, Flag.EXACT);
 			} else {
 				return new AnalysisResult(0, true, false, 0, 0, Flag.EXACT);
 			}
 		}
 
 		// ---------------------------------------------------------------------------------------
 		// Transposition Table Store;
 		if (best_value <= alpha_old)
 			parent.flag = Flag.UPPERBOUND;
 		else if (best_value >= beta)
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
 
 		// note, the variable seachMoves is currently unused, this feature is
 		// not yet implemented!
 
 		// set up threading
 		timer = new Timer();
 		exe = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
 
 		// make a copy of the actual position
 		IPosition position = game_state.getPosition().returnCopy();
 
 		int max_depth;
 
 		// set parameters for searchtime and searchdepth
 		if (movetime == 0 && maxMoveTime == 0) {
 			maxEvalTime = 60 * 60 * 1000; // 1h
 			max_depth = searchDepth;
 		} else if (movetime == 0 && infinite == false) {
 			maxEvalTime = maxMoveTime;
 			max_depth = searchDepth;
 		} else if (movetime == 0 && infinite == true) {
 			maxEvalTime = maxMoveTime;
 			max_depth = 200;
 		} else if (maxMoveTime == 0) {
 			maxEvalTime = movetime;
 			max_depth = 200; // this can never be reached :)
 		} else if (infinite == true) {
 			maxEvalTime = maxMoveTime;
 			max_depth = 200; // this can never be reached :)
 		} else {
 			maxEvalTime = Math.min(movetime, maxMoveTime);
 			max_depth = searchDepth;
 		}
 
 		timer.scheduleAtFixedRate(new UCIUpdater(), 1000, 5000);
 		start_mtime = System.currentTimeMillis();
 
 		// reset the result
 		result = null;
 
 		// create a new task
 		PositionEvaluator evaluator = new PositionEvaluator(position, max_depth);
 
 		// execute the task
 		exe.execute(evaluator);
 
 		return wait_until();
 	}
 
 	/**
 	 * stops all active threads if mitzi is running out of time
 	 * 
 	 * @return the best move
 	 */
 	public IMove wait_until() {
 
 		exe.shutdown();
 
 		// wait for termination of execution
 		try {
 			if (exe.awaitTermination(maxEvalTime, THREAD_TIMEOUT_UNIT)) {
 				UCIReporter.sendInfoString("task completed");
 			} else {
 				UCIReporter.sendInfoString("forcing task shutdown");
 				exe.shutdownNow();
 				exe.awaitTermination(THREAD_TIMEOUT, TimeUnit.SECONDS);
 			}
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// shut down timers and update killer moves
 		timer.cancel();
 		UCIReporter.sendInfoPV(game_state.getPosition(), runTime());
 		KillerMoves.updateKillerMove();
 
 		// if no best_move has been found yet, choose any
 		if (result == null) {
 			List<IMove> possibleMoves = game_state.getPosition()
 					.getPossibleMoves();
 			int randy = new Random().nextInt(possibleMoves.size());
 			return possibleMoves.get(randy);
 		}
 
 		// return the best move of the last completely searched tree
 		return result.best_move;
 	}
 
 	@Override
 	public IMove stop() {
 		// shut down immediately
 		exe.shutdownNow();
 
 		// shut down timers and update killer moves
 		timer.cancel();
 		UCIReporter.sendInfoPV(game_state.getPosition(), runTime());
 		KillerMoves.updateKillerMove();
 
 		// return the best move of the last completely searched tree
 		if (result == null)
 			return null; // this should never happen
 
 		return result.best_move;
 	}
 }
