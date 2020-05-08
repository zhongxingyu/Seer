 package player.gamer.statemachine.eggplant;
 
 import java.util.HashMap;
 import java.util.List;
 
 import player.gamer.statemachine.StateMachineGamer;
 import player.gamer.statemachine.eggplant.expansion.DepthLimitedExpansionEvaluator;
 import player.gamer.statemachine.eggplant.expansion.ExpansionEvaluator;
 import player.gamer.statemachine.eggplant.heuristic.Heuristic;
 import player.gamer.statemachine.eggplant.heuristic.MobilityHeuristic;
 import player.gamer.statemachine.eggplant.heuristic.MobilityType;
 import player.gamer.statemachine.eggplant.misc.CacheValue;
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
 import util.statemachine.implementation.prover.cache.CachedProverStateMachine;
 import apps.player.config.ConfigPanel;
 import apps.player.detail.DetailPanel;
 
 public class EggplantPrimaryGamer extends StateMachineGamer {
 	protected int statesSearched;
 	protected int leafNodesSearched;
 	protected int cacheHits, cacheMisses;
 	protected EggplantConfigPanel config = new EggplantConfigPanel();
 	protected ExpansionEvaluator expansionEvaluator;
 	protected Heuristic heuristic;
 	protected int maxSearchDepth;
 	protected int numPlayers;
 	protected int rootDepth;
 	protected ValuedMove bestWorkingMove;
 	protected int nextStartDepth;
 	protected HashMap<MachineState, CacheValue> principalMovesCache;
 
 	private final long GRACE_PERIOD = 200;
 
 	// TODO: Hashcode is NOT overridden by GDLSentence - this will only check if
 	// the sentences are actually the same objects in memory
 
 	@Override
 	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
 		// initialize cache, evaluators
 		rootDepth = 0;
 		numPlayers = getStateMachine().getRoles().size();
 		expansionEvaluator = new DepthLimitedExpansionEvaluator(10);
 		principalMovesCache = new HashMap<MachineState, CacheValue>();
 
         StateMachine machine = getStateMachine();
         MachineState state = getCurrentState();
         Role role = getRole();
 		iterativeDeepening(machine, state, role, 0, 100, true, timeout);
 	}
 
 	@Override
 	  public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
 	    long start = System.currentTimeMillis();
 	
 	    leafNodesSearched = statesSearched = 0;
 	    cacheHits = cacheMisses = 0;
 	    
 	
 	    StateMachine machine = getStateMachine();
 	    MachineState state = getCurrentState();
 	    Role role = getRole();
 	    bestWorkingMove = new ValuedMove(-1, machine.getRandomMove(state, role));
 	    
 	    iterativeDeepening(machine, state, role, 0, 100, machine.getLegalMoves(state, role).size() == 1, timeout - GRACE_PERIOD);
         rootDepth++;
         
 	    long stop = System.currentTimeMillis();
 	    notifyObservers(new EggplantMoveSelectionEvent(bestWorkingMove.move, bestWorkingMove.value, stop - start, statesSearched, leafNodesSearched, cacheHits,
 	        cacheMisses));
 	    return bestWorkingMove.move;
 	  }
 
 	protected void iterativeDeepening(StateMachine machine, MachineState state, Role role, int alpha, int beta, boolean preemptiveSearch, long endTime)
 	throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
 	  int depth = nextStartDepth;
 	  maxSearchDepth = depth;
 	  System.out.println("Turn " + rootDepth + ", starting search at " + depth);
 	  try {
 	    while (depth <= maxSearchDepth) {
 	      expansionEvaluator = new DepthLimitedExpansionEvaluator(depth);
 	      heuristic = new MobilityHeuristic(MobilityType.ONE_STEP, numPlayers);
 	      int alreadySearched = statesSearched;
 	      HashMap<MachineState, CacheValue> currentCache = new HashMap<MachineState, CacheValue>();
 	      ValuedMove move = memoizedAlphaBeta(machine, state, role, alpha, beta, 0, currentCache, principalMovesCache, endTime, false);
 	      if (!preemptiveSearch) {
 	        bestWorkingMove = move;
 	      }
 	      principalMovesCache = currentCache;
 	      System.out.println("Turn " + rootDepth + ", after depth " + depth + "; best = " + bestWorkingMove + " searched " + (statesSearched - alreadySearched) + " new states");
 	      depth++;
 	    }
 	  }
 	  catch (TimeUpException ex) {
 	    if (preemptiveSearch) {
 	      bestWorkingMove = new ValuedMove(-2, machine.getRandomMove(state, role));
	      nextStartDepth = depth - 1;
 	    }
 	    else {
 	      nextStartDepth = 1;
 	    }
 	  }
 	}
 
 	protected ValuedMove memoizedAlphaBeta(StateMachine machine, MachineState state, Role role, int alpha, int beta, int depth,
 			HashMap<MachineState, CacheValue> cache, HashMap<MachineState, CacheValue> principalMoves, long endTime, boolean debug) throws MoveDefinitionException, TransitionDefinitionException,
 			GoalDefinitionException, TimeUpException {
 		if (System.currentTimeMillis() > endTime)
 			throw new TimeUpException();
 		if (cache != null) {
 			if (cache.containsKey(state)) {
 				CacheValue cached = cache.get(state);
 				if (alpha >= cached.alpha && beta <= cached.beta) {
 					if (debug)
 						System.out.println("Cache hit: " + cached);
 					cacheHits++;
 					return cached.valuedMove;
 				} else {
 					// Alpha-beta bounds are incompatible
 					// System.out.println("Alpha: " + alpha + "\tBeta: " + beta
 					// + "\tCached Alpha: " + cached.alpha + "\tCached Beta: " +
 					// cached.beta);
 				}
 			}
 			cacheMisses++;
 			ValuedMove result = alphaBeta(machine, state, role, alpha, beta, depth, cache, principalMoves, endTime, debug);
 			if (debug) {
 				System.out.println("AlphaBeta returned with " + result + " " + state + " " + cache);
 			}
 			if (result.move != null)
 				cache.put(state, new CacheValue(result, alpha, beta));
 			return result;
 		} else {
 			return alphaBeta(machine, state, role, alpha, beta, depth, cache, principalMoves, endTime, debug);
 		}
 	}
 
 
 
 	private ValuedMove alphaBeta(StateMachine machine, MachineState state, Role role, int alpha, int beta, int depth,
 			HashMap<MachineState, CacheValue> cache, HashMap<MachineState, CacheValue> principalMovesCache, long endTime, boolean debug) throws MoveDefinitionException, TransitionDefinitionException,
 			GoalDefinitionException, TimeUpException {
 		statesSearched++;
 		if (debug)
 			System.out.println("At depth " + depth + "; searched " + statesSearched + "; searching " + state);
 		if (machine.isTerminal(state)) {
 			leafNodesSearched++;
 			return new ValuedMove(machine.getGoal(state, role), null);
 		}
 
 		if (depth > maxSearchDepth) {
 			maxSearchDepth = depth;
 		}
 
 		if (!expansionEvaluator.eval(machine, state, role, alpha, beta, depth)) { // expansion should stop
 			if (debug)
 				System.out.println("Stopping expanding at depth " + depth);
 			return new ValuedMove(heuristic.eval(machine, state, role, alpha, beta, depth, rootDepth), null);
 		}
 		ValuedMove maxMove = new ValuedMove(-1, null);
 		List<Move> possibleMoves = machine.getLegalMoves(state, role);
 		// Collections.shuffle(possibleMoves); // TODO: Remove this line
 		heuristic.update(machine, state, role, alpha, beta, depth, rootDepth);
 		if (debug)
 			System.out.println("At depth " + depth + "; searched " + statesSearched + "; moves: " + possibleMoves);
 
 		// search best move first
 		CacheValue principalMove = principalMovesCache.get(state);
 		if (principalMove != null) {
 			if (possibleMoves.remove(principalMove.valuedMove.move)) {
 				possibleMoves.add(0, principalMove.valuedMove.move);
 			}
 		}
 
 		for (Move move : possibleMoves) {
 			List<List<Move>> jointMoves = machine.getLegalJointMoves(state, role, move);
 			int min = 100;
 			int newBeta = beta;
 			for (List<Move> jointMove : jointMoves) {
 				MachineState nextState = machine.getNextState(state, jointMove);
 				int value = memoizedAlphaBeta(machine, nextState, role, alpha, newBeta, depth + 1, cache, principalMovesCache, endTime, debug).value;
 				if (value < min) {
 					min = value;
 					if (min <= alpha)
 						break;
 					if (min < newBeta)
 						newBeta = min;
 				}
 			}
 			if (min > maxMove.value) {
 				maxMove.value = min;
 				maxMove.move = move;
 				if (maxMove.value >= beta)
 					break;
 				if (maxMove.value > alpha)
 					alpha = maxMove.value;
 			}
 		}
 		return maxMove;
 	}
 
 	@Override
 	  public String getName() {
 	    return "EGGPLANT";
 	  }
 
 	@Override
 	public StateMachine getInitialStateMachine() {
 		return new CachedProverStateMachine();
 	}
 
 	@Override
 	public DetailPanel getDetailPanel() {
 		return new EggplantDetailPanel();
 	}
 
 	@Override
 	public ConfigPanel getConfigPanel() {
 		return config;
 	}
 
 }
