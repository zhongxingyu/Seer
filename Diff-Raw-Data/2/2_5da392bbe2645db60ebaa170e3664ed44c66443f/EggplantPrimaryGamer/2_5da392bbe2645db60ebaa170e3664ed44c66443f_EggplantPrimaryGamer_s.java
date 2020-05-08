 package player.gamer.statemachine.eggplant;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import player.gamer.statemachine.StateMachineGamer;
 import player.gamer.statemachine.eggplant.expansion.DepthLimitedExpansionEvaluator;
 import player.gamer.statemachine.eggplant.expansion.ExpansionEvaluator;
 import player.gamer.statemachine.eggplant.heuristic.Heuristic;
 import player.gamer.statemachine.eggplant.heuristic.LogisticClassifier;
 import player.gamer.statemachine.eggplant.heuristic.LatchHeuristic;
 import player.gamer.statemachine.eggplant.heuristic.NullHeuristic;
 import player.gamer.statemachine.eggplant.metagaming.EndgameBook;
 import player.gamer.statemachine.eggplant.metagaming.OpeningBook;
 import player.gamer.statemachine.eggplant.misc.CacheValue;
 import player.gamer.statemachine.eggplant.misc.Log;
 import player.gamer.statemachine.eggplant.misc.StateMachineFactory;
 import player.gamer.statemachine.eggplant.misc.TimeUpException;
 import player.gamer.statemachine.eggplant.misc.UpdateMachineException;
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
 import util.statemachine.implementation.propnet.BooleanPropNetStateMachine;
 import util.statemachine.implementation.propnet.cache.CachedBooleanPropNetStateMachine;
 import util.statemachine.implementation.prover.cache.CachedProverStateMachine;
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
 	protected boolean updateStateMachine;
 	protected Object updateStateMachineLock;
 	
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
 		updateStateMachine = false;
 		updateStateMachineLock = new Object();
 		Log.println('y', "Before thread init");
 		(new Thread() {
 			public void run() {
 				generateBooleanPropNetStateMachine();
 			}
 		}).start();
 		Log.println('y', "After thread init");
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
 
 		// minions = new StateMachine[] { machine };// ((BooleanPropNetStateMachine)
 		// machine).factor();
 		// long start = System.currentTimeMillis();
 
 		/*
 		 * openingBook = new OpeningBook(machine, state, role);
 		 * openingBook.expandBook(time + (timeout - time) / 2);
 		 */
 
 		// ((BooleanPropNetStateMachine) machine).speedTest();
 		// minions = new StateMachine[]{machine};
 		/*
 		if (minions != null && minions.length > 1) {
 			Log.println('h', "Switching to factor 0");
 			switchStateMachine(minions[0]);
 			state = minions[0].getInitialState();
 		}
 		*/
 		
 		//((BooleanPropNetStateMachine)machine).speedTest();
 
 		endBook = new EndgameBook(numPlayers);
 		// endBook.buildEndgameBook(machine, state, role, 6, 4, 8, start +
 		// (timeout - start) / 2);
 
 		bestWorkingMove = new ValuedMove(-2, machine.getRandomMove(state, role));
 		Log.println('y', "Beginning metagame evaluation with machine " + machine);
 		while (true) {
 			try {
 				try {
 					iterativeDeepening(machine, state, role, minGoal - 1, maxGoal + 1,
 							true, timeout - GRACE_PERIOD);
 					break;
 				} catch (Exception ex) {
 					if (ex instanceof UpdateMachineException) {
 						throw (UpdateMachineException)ex;
 					}
 					else {
 						ex.printStackTrace();
 						StateMachineFactory.popMachine();
 						throw new UpdateMachineException(false);
 					}
 				}
 			} catch(UpdateMachineException ex) {
 				synchronized (updateStateMachineLock) {
 					updateStateMachine = false;
 				}
 				StateMachine newMachine = StateMachineFactory.getCurrentMachine();
 				Log.println('y', "Switching to " + newMachine);
 				switchStateMachine(newMachine);
 				machine = getStateMachine();
 				state = getCurrentState();
 				role = getRole();
 				findGoalBounds(machine, role);
 			}
 		}
 		if (KEEP_TIME) {
 			en = System.currentTimeMillis();
 			timeLog.add("Metagaming took " + (en - st) + " ms");
 		}
 	}
 
 	private void findGoalBounds(StateMachine machine, Role role) {
 		int[] values;
 		if (machine instanceof BooleanPropNetStateMachine) { 
 			values = ((BooleanPropNetStateMachine)machine).getGoalValues(role);
 		}
 		else {
 			values = new int[]{0, 100};
 		}
 		minGoal = values[0];
 		maxGoal = values[values.length - 1];
 		int total = 0;
 		for (int i = 0; i < values.length; i++)
 			total += values[i];
 		avgGoal = total / (double) values.length;
 		Log.println('i', "Min: " + minGoal + ", max: " + maxGoal + ", avg: "
 				+ avgGoal);
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
 		Role role = getRole();
 		bestWorkingMove = new ValuedMove(-2, machine.getRandomMove(state, role));
 
 		try {
 			while (true) {
 				try {
 					if (machine instanceof BooleanPropNetStateMachine) {
 						((BooleanPropNetStateMachine) machine).updateSatisfiedLatches(state);
 					}
 
 					Log.println('i', "State on turn " + rootDepth + " : " + state.getContents());
 					try {
 						iterativeDeepening(machine, state, role, minGoal - 1, maxGoal + 1,
 								machine.getLegalMoves(state, role).size() == 1, timeout
 										- GRACE_PERIOD);
 					} catch (Exception ex) {
 						if (ex instanceof UpdateMachineException) {
 							throw (UpdateMachineException)ex;
 						}
 						else {
 							ex.printStackTrace();
 							StateMachineFactory.popMachine();
 							throw new UpdateMachineException(false);
 						}
 					}
 					break;
 				} catch (UpdateMachineException ex) {
 					synchronized (updateStateMachineLock) {
 						updateStateMachine = false;
 						StateMachine newMachine = StateMachineFactory.getCurrentMachine();
 						Log.println('y', "Switching to " + newMachine);
 						switchStateMachine(newMachine);
 						machine = getStateMachine();
 						state = getCurrentState();
 						role = getRole();
 						findGoalBounds(machine, role);
 					}
 				}
 			}
 			
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
 
 	private Heuristic getHeuristic(StateMachine machine, Role role) {
 		/*
 		MobilityHeuristic h1 = new MobilityHeuristic(MobilityType.ONE_STEP, numPlayers),
 			h2 = new OpponentFocusHeuristic(MobilityType.ONE_STEP, numPlayers);
 		h1.setAvgGoal((int) avgGoal, minGoal, maxGoal);
 		h2.setAvgGoal((int) avgGoal, minGoal, maxGoal);
 		return new WeightedHeuristic(new Heuristic[] {
 				h1, h2,
 				//new MonteCarloHeuristic(3, avgGoal)
 				}, new double[] { 0.15, 0.85 });
 		 */
 		if (machine instanceof BooleanPropNetStateMachine) {
 			BooleanPropNetStateMachine bpnsm = (BooleanPropNetStateMachine) machine;
 			int roleIndex = bpnsm.getRoleIndices().get(role);
 			//return new GoalHeuristic(bpnsm, roleIndex);
 			return new LatchHeuristic(bpnsm, roleIndex);
 		}
 		else {
 			return new NullHeuristic((int) avgGoal);
 		}
 		// return new MonteCarloHeuristic(4, 5, avgGoal);
 		//return new NullHeuristic((int) avgGoal);
 	}
 
 	protected void iterativeDeepening(StateMachine machine, MachineState state,
 			Role role, int alpha, int beta, boolean preemptiveSearch,
 			long endTime) throws MoveDefinitionException,
 			TransitionDefinitionException, GoalDefinitionException, UpdateMachineException {
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
 		boolean hasLost = false, hasWon = bestWorkingMove.value == maxGoal;
 		int alreadySearched, alreadyPVSearched;
 		alreadySearched = alreadyPVSearched = 0;
 		long searchStartTime = System.currentTimeMillis();
 		long searchEndTime;
 		try {
 			if (!hasWon) {
 				heuristic = getHeuristic(machine, role);
 				Log.println('x', "Current state evaluates " + heuristic.eval(machine, state, role, alpha, beta, depth, rootDepth, 0));
 				while (depth <= maxSearchDepth) {		
 					// Check for update to statemachine
 					if (updateStateMachine) {
 						throw new UpdateMachineException(true);
 					}
 
 					heuristicUpdateCounter = heuristicUpdateInterval = rootDepth + depth;
 					expansionEvaluator = new DepthLimitedExpansionEvaluator(depth);
 					alreadySearched = statesSearched;
 					alreadyPVSearched = pvStatesSearched;
 					HashMap<MachineState, CacheValue> currentCache = new HashMap<MachineState, CacheValue>();
 					searchStartTime = System.currentTimeMillis();
 					ValuedMove move = memoizedAlphaBeta(machine, state, role,
 							alpha, beta, 0, DEPTH_INITIAL_OFFSET, currentCache,
 							principalMovesCache, endTime, false);
 					if (!preemptiveSearch) {
 						bestWorkingMove = move;
 					}
 					searchEndTime = System.currentTimeMillis();
 					Log.println('i', "Turn " + rootDepth + ", depth " + depth
 							+ " (max " + maxSearchActualDepth + "; abs "
 							+ (rootDepth + depth) + "); working = " + move
 							+ " searched " + (statesSearched - alreadySearched - (pvStatesSearched - alreadyPVSearched))
 							+ " new states, "
 							+ (pvStatesSearched - alreadyPVSearched)
 							+ " additional PV states; "
 							+ (int) (1000.0 * (statesSearched - alreadySearched) / (searchEndTime - searchStartTime))
 							+ " states / s");
 					if (move.value == minGoal) {
 						hasLost = true;
 						break;
 					}
 
 					principalMovesCache = currentCache;
 
 					if (move.value == maxGoal) {
 						hasWon = true;
 						break;
 					}
 
 					depth++;
 				}
 			}
 			// Try to make opponents' life hard / force them to respond
 			// Iterative blunder approach: give opponent more and more ways to
 			// blunder
 			if (hasLost && !preemptiveSearch) {
 				Log.println('i', "Trying desperate measures...");
 				if (principalMovesCache.containsKey(state))
 					bestWorkingMove = principalMovesCache.get(state).valuedMove;
 			} else if (hasWon) {
 				Log.println('i', "Found a win at depth " + (rootDepth + depth)
 						+ ". Move towards win: " + bestWorkingMove);
 				Log.println('i', "Cache (size " + principalMovesCache.size() + "): " + principalMovesCache);
 				if (depth == 1) {
 					printTimeLog();
 				}
 			}
 			throw new TimeUpException();
 		} catch (TimeUpException ex) {
 			if (preemptiveSearch) {
 				bestWorkingMove = new ValuedMove(-2, machine.getRandomMove(
 						state, role));
 			}
 			searchEndTime = System.currentTimeMillis();
 			Log.println('i', "Turn " + rootDepth + ", interrupted at depth " + depth
 					+ " (max " + maxSearchActualDepth + "; abs "
 					+ (rootDepth + depth) + "); best = " + bestWorkingMove
 					+ " searched " + (statesSearched - alreadySearched - (pvStatesSearched - alreadyPVSearched))
 					+ " new states, "
 					+ (pvStatesSearched - alreadyPVSearched)
 					+ " additional PV states, "
 					+ (int) (1000.0 * (statesSearched - alreadySearched) / (searchEndTime - searchStartTime))
 					+ " states / s");
 			nextStartDepth = depth - 2;
 			if (nextStartDepth < 1)
 				nextStartDepth = 1;
 			if (hasLost)
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
 					beta, actualDepth, rootDepth, endTime), null, rootDepth + actualDepth,
 					false);
 		}
 
 		if (actualDepth > maxSearchActualDepth) {
 			maxSearchActualDepth = actualDepth;
 		}
 		if (!expansionEvaluator.eval(machine, state, role, alpha, beta,
 				actualDepth)) {
 			pvStatesSearched++;
 			// Clear cache for pv search
 			cache = null;
 		}
 		if (depth > actualDepth) {
 			System.err.println("ERROR: " + depth + " " + actualDepth + " "
 					+ pvDepthOffset);
 		}
 
 		List<Move> possibleMoves = machine.getLegalMoves(state, role);
 		if (heuristicUpdateCounter == heuristicUpdateInterval) {
 			heuristic.update(machine, state, role, alpha, beta, actualDepth, rootDepth);
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
 	
 	public void generateBooleanPropNetStateMachine() {
 		Log.println('y', "Threaded BPNSM compute started " + System.currentTimeMillis());
 		CachedBooleanPropNetStateMachine bpnet = new CachedBooleanPropNetStateMachine(getRoleName());
 		bpnet.initialize(getMatch().getDescription());
 		Log.println('y', "Threaded BPNSM compute ended " + System.currentTimeMillis());
 	}
 	
 	public void signalUpdateMachine() {
 		synchronized (updateStateMachineLock) {
 			updateStateMachine = true;
 		}
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
 		StateMachineFactory.reset();
 		StateMachineFactory.setDelegate(this);
 		return StateMachineFactory.getCurrentMachine();
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
