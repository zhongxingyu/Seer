 package player.gamer.statemachine.eggplant;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import player.gamer.statemachine.StateMachineGamer;
 import player.gamer.statemachine.eggplant.expansion.DepthLimitedExpansionEvaluator;
 import player.gamer.statemachine.eggplant.expansion.ExpansionEvaluator;
 import player.gamer.statemachine.eggplant.heuristic.Heuristic;
 import player.gamer.statemachine.eggplant.heuristic.MobilityHeuristic;
 import player.gamer.statemachine.eggplant.heuristic.MobilityType;
 import player.gamer.statemachine.eggplant.heuristic.MonteCarloHeuristic;
 import player.gamer.statemachine.eggplant.heuristic.OpponentFocusHeuristic;
 import player.gamer.statemachine.eggplant.heuristic.WeightedHeuristic;
 import player.gamer.statemachine.eggplant.metagaming.EndgameBook;
 import player.gamer.statemachine.eggplant.metagaming.OpeningBook;
 import player.gamer.statemachine.eggplant.misc.CacheValue;
 import player.gamer.statemachine.eggplant.misc.Log;
 import player.gamer.statemachine.eggplant.misc.TimeUpException;
 import player.gamer.statemachine.eggplant.misc.ValuedMove;
 import player.gamer.statemachine.eggplant.ui.EggplantConfigPanel;
 import player.gamer.statemachine.eggplant.ui.EggplantDetailPanel;
 import player.gamer.statemachine.eggplant.ui.EggplantMoveSelectionEvent;
 import util.statemachine.MachineState;
 import util.statemachine.Move;
 import util.statemachine.Role;
 import util.statemachine.StateMachine;
 import util.statemachine.exceptions.GoalDefinitionException;
 import util.statemachine.exceptions.MoveDefinitionException;
 import util.statemachine.exceptions.TransitionDefinitionException;
 import util.statemachine.implementation.propnet.cache.CachedBooleanPropNetStateMachine;
 import apps.player.config.ConfigPanel;
 import apps.player.detail.DetailPanel;
 
 public class EggplantPrimaryGamer extends StateMachineGamer {
 
 	protected int statesSearched;
 	protected int pvStatesSearched;
 	protected int leafNodesSearched;
 	protected int cacheHits, cacheMisses;
 	protected EggplantConfigPanel config = new EggplantConfigPanel();
 	protected ExpansionEvaluator expansionEvaluator;
 	protected Heuristic heuristic;
 	protected OpeningBook openingBook;
 	protected EndgameBook endBook;
 	protected int maxSearchDepth;
 	protected int maxSearchActualDepth;
 	protected int numPlayers;
 	protected int rootDepth;
 	protected ValuedMove bestWorkingMove;
 	protected int nextStartDepth;
 	protected int minGoal;
 	protected int maxGoal;
 	protected double avgGoal;
 	protected int heuristicUpdateCounter;
 	protected HashMap<MachineState, CacheValue> principalMovesCache;
 
 	private final boolean KEEP_TIME = true;
 	private final long GRACE_PERIOD = 200;
 	private final float PRINCIPAL_MOVE_DEPTH_FACTOR = 0.1f;
 	private final float DEPTH_INITIAL_OFFSET = 0.5f;
 	private int heuristicUpdateInterval = 0;
 	private List<String> timeLog = new ArrayList<String>();
 	private final String testers = "mop";
 	/*
 	 * Heuristic testing codes m - Monte Carlo o - Opponent mobility p - Player
 	 * mobility
 	 */
 
 	// TODO: Hashcode is NOT overridden by GDLSentence - this will only check if
 	// the sentences are actually the same objects in memory
 
 	private StateMachine[] minions;
 
 	@Override
 	public void stateMachineMetaGame(long timeout)
 			throws TransitionDefinitionException, MoveDefinitionException,
 			GoalDefinitionException {
 		Log.println('i', "Starting metagame");
 		// initialize cache, evaluators
 		long st, en;
 		if (KEEP_TIME) {
 			st = System.currentTimeMillis();
 		}
 		rootDepth = 0;
 		nextStartDepth = 1;
 		numPlayers = getStateMachine().getRoles().size();
 		expansionEvaluator = new DepthLimitedExpansionEvaluator(10);
 		principalMovesCache = new HashMap<MachineState, CacheValue>();
 		heuristicUpdateCounter = 0;
 
 		StateMachine machine = getStateMachine();
 		MachineState state = getCurrentState();
 		Role role = getRole();
 		findGoalBounds(machine, role);
 
 		minions = new StateMachine[] { machine };// ((BooleanPropNetStateMachine)
 		// machine).factor();
 		// long start = System.currentTimeMillis();
 
 		/*
 		 * openingBook = new OpeningBook(machine, state, role);
 		 * openingBook.expandBook(time + (timeout - time) / 2);
 		 */
 
 		// ((BooleanPropNetStateMachine) machine).speedTest();
 		// minions = new StateMachine[]{machine};
 		if (minions != null && minions.length > 1) {
 			Log.println('h', "Switching to factor 0");
 			switchStateMachine(minions[0]);
 			state = minions[0].getInitialState();
 		}
 
 		endBook = new EndgameBook(numPlayers);
 		// endBook.buildEndgameBook(machine, state, role, 6, 4, 8, start +
 		// (timeout - start) / 2);
 
 		bestWorkingMove = new ValuedMove(-2, machine.getRandomMove(state, role));
 		iterativeDeepening(minions[0], state, role, minGoal - 1, maxGoal + 1,
 				true, timeout - GRACE_PERIOD);
 		if (KEEP_TIME) {
 			en = System.currentTimeMillis();
 			timeLog.add("Metagaming took " + (en - st) + " ms");
 		}
 	}
 
 	private void findGoalBounds(StateMachine m, Role role) {
 		int[] values = { 0, 100 }; // ((BooleanPropNetStateMachine)m).getGoalValues(role);
 		minGoal = values[0];
 		maxGoal = values[values.length - 1];
 		int total = 0;
 		for (int i = 0; i < values.length; i++)
 			total += values[i];
 		avgGoal = total / (double) values.length;
 		Log.println('i', "Min: " + minGoal + ", max: " + maxGoal + ", avg: "
 				+ avgGoal);
 	}
 
 	// Want to know:
 	// Mean and variance of each heuristic
 	// Correllation of each heuristic with good score
 	private void boof() {
 	}
 
 	@Override
 	public Move stateMachineSelectMove(long timeout)
 			throws TransitionDefinitionException, MoveDefinitionException,
 			GoalDefinitionException {
 		long start = System.currentTimeMillis();
 		leafNodesSearched = statesSearched = pvStatesSearched = 0;
 		cacheHits = cacheMisses = 0;
 
 		StateMachine machine = getStateMachine();
 		MachineState state = getCurrentState();
 		Log.println('i', "State on turn " + rootDepth + " : " + state.getContents());
 		Role role = getRole();
 		bestWorkingMove = new ValuedMove(-2, machine.getRandomMove(state, role));
 
 		try {
 			iterativeDeepening(machine, state, role, minGoal - 1, maxGoal + 1,
 					machine.getLegalMoves(state, role).size() == 1, timeout
 							- GRACE_PERIOD);
 			rootDepth++;
 
 			long stop = System.currentTimeMillis();
 			if (KEEP_TIME) {
 				timeLog.add("Selecting move at depth " + rootDepth + " took "
 						+ (stop - start) + " ms");
 			}
 			notifyObservers(new EggplantMoveSelectionEvent(
 					bestWorkingMove.move, bestWorkingMove.value, stop - start,
 					statesSearched, leafNodesSearched, cacheHits, cacheMisses));
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		if (bestWorkingMove.move != null)
 			return bestWorkingMove.move;
 		return new ValuedMove(-2, machine.getRandomMove(state, role)).move;
 	}
 
 	private Heuristic getHeuristic() {
 		return new WeightedHeuristic(new Heuristic[] {
 				new MobilityHeuristic(MobilityType.ONE_STEP, numPlayers),
 				new OpponentFocusHeuristic(MobilityType.ONE_STEP, numPlayers),
 				new MonteCarloHeuristic(3, avgGoal)
				}, new double[] { 0.2, 0.4, 0.4 });
 		// return new MonteCarloHeuristic(4, 5, avgGoal);
 		// return new NullHeuristic((int) avgGoal);
 	}
 
 	protected void iterativeDeepening(StateMachine machine, MachineState state,
 			Role role, int alpha, int beta, boolean preemptiveSearch,
 			long endTime) throws MoveDefinitionException,
 			TransitionDefinitionException, GoalDefinitionException {
 		int depth;
 		/*
 		 * int bd = openingBook.bookDepth(); if (rootDepth < bd) { ValuedMove vm
 		 * = openingBook.cachedValuedMove(machine, state, role); if (vm != null)
 		 * { bestWorkingMove = vm; depth = maxSearchDepth = bd - rootDepth + 1;
 		 * } else { depth = maxSearchDepth = 1; // problem if this happens //
 		 * System.out.println("openingBook returned null move"); } } else
 		 */
 		if (principalMovesCache.containsKey(state)) {
 			CacheValue cached = principalMovesCache.get(state);
 			bestWorkingMove = cached.valuedMove;
 			depth = maxSearchDepth = maxSearchActualDepth = nextStartDepth;
 		} else { // this state was not previously explored due to alpha-beta
 			// pruning; to ensure non-random moves, start at root
 			depth = maxSearchDepth = 1;
 		}
 		Log.println('i', "Turn " + rootDepth + ", starting search at " + depth
 				+ " with best = " + bestWorkingMove + "; end book size = "
 				+ endBook.book.size());
 		boolean hasLost = false, hasWon = false;
 		int alreadySearched, alreadyPVSearched;
 		alreadySearched = alreadyPVSearched = 0;
 		try {
 			while (depth <= maxSearchDepth) {
 				heuristicUpdateCounter = heuristicUpdateInterval = rootDepth + depth;
 				
 				expansionEvaluator = new DepthLimitedExpansionEvaluator(depth);
 				heuristic = getHeuristic();
 				alreadySearched = statesSearched;
 				alreadyPVSearched = pvStatesSearched;
 				HashMap<MachineState, CacheValue> currentCache = new HashMap<MachineState, CacheValue>();
 				ValuedMove move = memoizedAlphaBeta(machine, state, role,
 						alpha, beta, 0, DEPTH_INITIAL_OFFSET, currentCache,
 						principalMovesCache, endTime, false);
 				if (!preemptiveSearch) {
 					bestWorkingMove = move;
 				}
 				Log.println('i', "Turn " + rootDepth + ", after depth " + depth
 						+ " (max " + maxSearchActualDepth + "; abs "
 						+ (rootDepth + depth) + "); working = " + move
 						+ " searched " + (statesSearched - alreadySearched - (pvStatesSearched - alreadyPVSearched))
 						+ " new states, "
 						+ (pvStatesSearched - alreadyPVSearched)
 						+ " additional PV states");
 				if (move.value == minGoal) {
 					hasLost = true;
 					break;
 				}
 				if (move.value == maxGoal) {
 					hasWon = true;
 					break;
 				}
 				principalMovesCache = currentCache;
 				depth++;
 			}
 			// Try to make opponents' life hard / force them to respond
 			// Iterative blunder approach: give opponent more and more ways to
 			// blunder
 			if (hasLost && !preemptiveSearch) {
 				Log.println('i', "Trying desperate measures...");
 				if (principalMovesCache.containsKey(state))
 					bestWorkingMove = principalMovesCache.get(state).valuedMove;
 				throw new TimeUpException();
 			} else if (hasWon && !preemptiveSearch) {
 				Log.println('i', "Found a win at depth " + (rootDepth + depth)
 						+ ". Move towards win: " + bestWorkingMove);
 				if (depth == 1) {
 					printTimeLog();
 				}
 				throw new TimeUpException();
 			}
 		} catch (TimeUpException ex) {
 			if (preemptiveSearch) {
 				bestWorkingMove = new ValuedMove(-2, machine.getRandomMove(
 						state, role));
 			}
 			Log.println('i', "Turn " + rootDepth + ", interrupted at depth " + depth
 					+ " (max " + maxSearchActualDepth + "; abs "
 					+ (rootDepth + depth) + "); best = " + bestWorkingMove
 					+ " searched " + (statesSearched - alreadySearched - (pvStatesSearched - alreadyPVSearched))
 					+ " new states, "
 					+ (pvStatesSearched - alreadyPVSearched)
 					+ " additional PV states");
 			nextStartDepth = depth - 2;
 			if (nextStartDepth < 1)
 				nextStartDepth = 1;
 			if (hasWon || hasLost)
 				nextStartDepth = 1;
 		} catch (RuntimeException e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected ValuedMove memoizedAlphaBeta(StateMachine machine,
 			MachineState state, Role role, int alpha, int beta,
 			int actualDepth, float pvDepthOffset,
 			HashMap<MachineState, CacheValue> cache,
 			HashMap<MachineState, CacheValue> principalMoves, long endTime,
 			boolean debug) throws MoveDefinitionException,
 			TransitionDefinitionException, GoalDefinitionException,
 			TimeUpException {
 		if (System.currentTimeMillis() > endTime)
 			throw new TimeUpException();
 		if (cache != null) {
 			if (cache.containsKey(state)) {
 				CacheValue cached = cache.get(state);
 				if (alpha >= cached.alpha && beta <= cached.beta) {
 					Log.println('a', "Cache hit: " + cached);
 					cacheHits++;
 					return cached.valuedMove;
 				}
 			}
 			cacheMisses++;
 			ValuedMove result = alphaBeta(machine, state, role, alpha, beta,
 					actualDepth, pvDepthOffset, cache, principalMoves, endTime,
 					debug);
 			if (debug) {
 				Log.println('a', "AlphaBeta returned with " + result + " "
 						+ state + " " + cache);
 			}
 			if (result.move != null) {
 				cache.put(state, new CacheValue(result, alpha, beta));
 			}
 			if (result.value == minGoal && !endBook.book.containsKey(state)) {
 				// sure loss
 				endBook.book.put(state, new CacheValue(result, alpha, beta));
 			} else if (result.value == maxGoal
 					&& !endBook.book.containsKey(state)) {
 				// sure win - possibly unsafe
 				// endBook.book.put(state, new CacheValue(result, alpha, beta));
 			}
 			return result;
 		} else {
 			return alphaBeta(machine, state, role, alpha, beta, actualDepth,
 					pvDepthOffset, cache, principalMoves, endTime, debug);
 		}
 	}
 
 	private ValuedMove alphaBeta(StateMachine machine, MachineState state,
 			Role role, int alpha, int beta, int actualDepth,
 			float pvDepthOffset, HashMap<MachineState, CacheValue> cache,
 			HashMap<MachineState, CacheValue> principalMovesCache,
 			long endTime, boolean debug) throws MoveDefinitionException,
 			TransitionDefinitionException, GoalDefinitionException,
 			TimeUpException {
 		statesSearched++;
 
 		int depth = (int) (actualDepth + pvDepthOffset);
 
 		ValuedMove endLookup = endBook.endgameValue(state);
 		if (endLookup != null) {
 			Log.println('a', "At depth " + depth + "; searched "
 					+ statesSearched + "; found in EndgameBook");
 			return endLookup;
 		}
 
 		if (machine.isTerminal(state)) {
 			Log.println('a', "At depth " + depth + "; searched "
 					+ statesSearched + "; terminal");
 			leafNodesSearched++;
 			return new ValuedMove(machine.getGoal(state, role), null, rootDepth
 					+ depth, true);
 		}
 
 		if (depth > maxSearchDepth) {
 			maxSearchDepth = depth;
 		}
 
 		if (!expansionEvaluator.eval(machine, state, role, alpha, beta, depth)) { // expansion
 			// should
 			// stop
 			Log.println('a', "Heuristic; stopping expanding at depth " + depth);
 			return new ValuedMove(heuristic.eval(machine, state, role, alpha,
 					beta, depth, rootDepth, endTime), null, rootDepth + depth,
 					false);
 		}
 
 		if (actualDepth > maxSearchActualDepth) {
 			maxSearchActualDepth = actualDepth;
 		}
 		if (!expansionEvaluator.eval(machine, state, role, alpha, beta,
 				actualDepth)) {
 			pvStatesSearched++;
 		}
 		if (depth > actualDepth) {
 			System.err.println("ERROR: " + depth + " " + actualDepth + " "
 					+ pvDepthOffset);
 		}
 
 		List<Move> possibleMoves = machine.getLegalMoves(state, role);
 		// Collections.shuffle(possibleMoves); // TODO: Remove this line
 		if (heuristicUpdateCounter == heuristicUpdateInterval) {
 			heuristic.update(machine, state, role, alpha, beta, depth, rootDepth);
 			heuristicUpdateCounter = 0;
 		}
 		else {
 			heuristicUpdateCounter++;
 		}
 		Log.println('a', "At depth " + depth + "; searched " + statesSearched
 				+ "; searching " + state + " ; moves: " + possibleMoves);
 
 		// search best move first
 		boolean principalMoveFound = false;
 		float principalMoveSignificance = 0;
 		CacheValue principalMove = principalMovesCache.get(state);
 		if (principalMove != null) {
 			if (possibleMoves.remove(principalMove.valuedMove.move)) {
 				principalMoveFound = true;
 				int cachedValue = principalMove.valuedMove.value;
 				int cachedAlpha = principalMove.alpha;
 				int cachedBeta = principalMove.beta;
 				principalMoveSignificance = cachedValue / (float) (avgGoal);
 				/*
 				if (cachedValue <= cachedAlpha) {
 					principalMoveSignificance = 0;
 				} else if (cachedValue >= cachedBeta) {
 					principalMoveSignificance = 1;
 				} else {
 					principalMoveSignificance = ((float) (cachedValue - cachedAlpha)) / (cachedBeta - cachedAlpha);
 				}
 				*/
 				possibleMoves.add(0, principalMove.valuedMove.move);
 				Log.println('a', "At depth " + depth + "; searched "
 						+ statesSearched + " principal move = "
 						+ principalMove.valuedMove.move);
 			}
 		}
 
 		ValuedMove maxMove = new ValuedMove(-3, null);
 		for (Move move : possibleMoves) {
 			Log.println('a', "Considering move " + move + " at depth " + depth);
 			List<List<Move>> jointMoves = machine.getLegalJointMoves(state,
 					role, move);
 			int minValue = maxGoal + 1;
 			int minDepth = rootDepth + depth;
 			int newBeta = beta;
 			for (List<Move> jointMove : jointMoves) {
 				MachineState nextState = machine.getNextState(state, jointMove);
 				Log.println('a', "Considering joint move " + jointMove
 						+ " with state = " + nextState);
 				ValuedMove bestMove;
 				if (principalMoveFound) {
 					Log.println('d', "NUIDS : At offset " + pvDepthOffset
 							+ "; " + state.getContents());
 					bestMove = memoizedAlphaBeta(machine, nextState, role,
 							alpha, newBeta, actualDepth + 1, pvDepthOffset
 									- principalMoveSignificance
 									* PRINCIPAL_MOVE_DEPTH_FACTOR, cache,
 							principalMovesCache, endTime, debug);
 				} else {
 					bestMove = memoizedAlphaBeta(machine, nextState, role,
 							alpha, newBeta, actualDepth + 1, pvDepthOffset,
 							cache, principalMovesCache, endTime, debug);
 				}
 				int bestMoveValue = bestMove.value;
 				int bestMoveDepth = bestMove.depth;
 				if (bestMoveValue < minValue
 						|| (bestMoveValue == minValue && (bestMoveValue >= (int) avgGoal
 								&& bestMoveDepth > minDepth || bestMoveValue <= (int) avgGoal
 								&& bestMoveDepth < minDepth))) { // heuristic to
 					// break
 					// ties
 					if (bestMoveValue == minValue) {
 						Log.println('a', "Tie broken inside: curr depth "
 								+ minDepth + "; best = " + bestMove);
 					}
 					Log.println('a', "Inside min update: best move = "
 							+ bestMove + "; previous min value = " + minValue);
 					minValue = bestMoveValue;
 					minDepth = bestMoveDepth;
 					if (minValue <= alpha)
 						break;
 					if (minValue < newBeta)
 						newBeta = minValue;
 				}
 			}
 			if (principalMoveFound) {
 				principalMoveFound = false;
 			}
 			if (maxMove.value < minValue
 					|| (maxMove.value == minValue && (maxMove.value >= (int) avgGoal
 							&& minDepth < maxMove.depth || maxMove.value <= (int) avgGoal
 							&& minDepth > maxMove.depth))) { // heuristic to
 				// break ties
 				if (maxMove.value == minValue) {
 					Log.println('a', "Tie broken outside: curr depth "
 							+ minDepth + "; best = " + maxMove);
 				}
 				Log.println('a', "Outside max update: new best move = "
 						+ new ValuedMove(minValue, move, minDepth)
 						+ "; previous max move = " + maxMove);
 
 				maxMove.value = minValue;
 				maxMove.depth = minDepth;
 				maxMove.move = move;
 				if (maxMove.value >= beta)
 					break;
 				if (maxMove.value > alpha)
 					alpha = maxMove.value;
 			}
 		}
 		return maxMove;
 	}
 
 	public void printTimeLog() {
 		if (!KEEP_TIME) {
 			Log.println('i', "No timing information kept (turn on KEEP_TIME)");
 			return;
 		}
 		Log.println('i', "\nTiming info:\n---");
 		for (String tim : timeLog) {
 			Log.println('i', "   " + tim);
 		}
 		Log.println('i', "---\n");
 	}
 
 	@Override
 	public String getName() {
 		return "EGGPLANT";
 	}
 
 	@Override
 	public StateMachine getInitialStateMachine() {
 		return new CachedBooleanPropNetStateMachine();
 	}
 
 	@Override
 	public DetailPanel getDetailPanel() {
 		return new EggplantDetailPanel();
 	}
 
 	@Override
 	public ConfigPanel getConfigPanel() {
 		return config;
 	}
 
 	class HeuristicStats {
 		private List<Integer> evals;
 		private List<Double> winFractions;
 		private boolean cachedMean, cachedDev, cachedCor;
 		private double mean, stDev, correlation;
 
 		public HeuristicStats() {
 			evals = new ArrayList<Integer>();
 			cachedMean = cachedDev = cachedCor = false;
 		}
 
 		public void update(int eval) {
 			update(eval, .5);
 		}
 
 		public void update(int eval, double winFraction) {
 			evals.add(eval);
 			winFractions.add(winFraction);
 			cachedMean = cachedDev = cachedCor = false;
 		}
 
 		public double mean() {
 			if (!cachedMean)
 				computeMean();
 			return mean;
 		}
 
 		public double standardDeviation() {
 			if (!cachedDev)
 				computeStDev();
 			return stDev;
 		}
 
 		public double correlation() {
 			if (!cachedCor)
 				computeCor();
 			return 0.0;
 		}
 
 		private void computeMean() {
 			int total = 0;
 			for (Integer i : evals)
 				total += i;
 			mean = total / (double) evals.size();
 		}
 
 		private void computeStDev() {
 			if (!cachedMean)
 				computeMean();
 			double total = 0;
 			for (Integer i : evals) {
 				double diff = i - mean;
 				total += diff * diff;
 			}
 			stDev = Math.sqrt(total / evals.size());
 		}
 
 		private void computeCor() {
 			correlation = 0;
 		}
 	}
 }
