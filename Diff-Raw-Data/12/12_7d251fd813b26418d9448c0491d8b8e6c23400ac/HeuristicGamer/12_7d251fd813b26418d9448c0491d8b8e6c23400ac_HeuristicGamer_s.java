 package apps.team;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import apps.pgggppg.compilation.NativePropNetStateMachine;
 
 import player.gamer.statemachine.StateMachineGamer;
 import util.gdl.grammar.Gdl;
 import util.propnet.architecture.PropNet;
 import util.propnet.factory.OptimizingPropNetFactory;
 import util.statemachine.MachineState;
 import util.statemachine.Move;
 import util.statemachine.StateMachine;
 import util.statemachine.exceptions.GoalDefinitionException;
 import util.statemachine.exceptions.MoveDefinitionException;
 import util.statemachine.exceptions.TransitionDefinitionException;
 import util.statemachine.implementation.propnet.PropNetStateMachine;
 
 public abstract class HeuristicGamer extends StateMachineGamer {
 	
 	private static final int GOAL_MIN = 0;
 	private static final int GOAL_MAX = 100;
 	
 	// configurable
 	private long timeoutBuffer = 1000;
 	
 	// cache
 	private Map<MachineState, Pair<Integer, Move>> stateCache;
 	private Map<MachineState, Integer> heuristicCache;
 	private Map<MachineState, Map<Move, List<MachineState>>> transitionCache;
 	
 	// in-game temporary state
 	private long timeout;
 	private boolean depthLimited;
 	
 	public void reset() {
 		stateCache = new HashMap<MachineState, Pair<Integer, Move>>();
 		heuristicCache = new HashMap<MachineState, Integer>();
 		transitionCache = new HashMap<MachineState, Map<Move,List<MachineState>>>();
 	}
 	
 	public abstract int getHeuristicValue(MachineState state) throws Exception;
 
 	@Override
 	public StateMachine getInitialStateMachine() {
 		// initialize
 		reset();

		return new NativePropNetStateMachine();
 		//return new PropNetStateMachine();
 	}
 	
 	public int getTerminalValue(MachineState state) throws GoalDefinitionException {
 		if (!stateCache.containsKey(state)) {
 			stateCache.put(state, new Pair<Integer, Move>(getStateMachine().getGoal(state, getRole()), null));
 		}
 		return stateCache.get(state).fst;
 	}
 	
 	public Map<Move, List<MachineState>> getTransitions(MachineState state, boolean cache) throws Exception {
 		if (transitionCache.containsKey(state)) {
 			return transitionCache.get(state);
 		}
 
 		Map<Move, List<MachineState>> transitions = getStateMachine().getNextStates(state, getRole());
 		if (cache) {
 			transitionCache.put(state, transitions);
 		}
 		return transitions;
 	}
 	
 	public boolean isTerminal(MachineState state) {
 		return getStateMachine().isTerminal(state);
 	}
 	
 	public int randomInt(int n) {
 		return (int) (Math.random() * n);
 	}
 	
 	public MachineState getRandomSuccessor(MachineState state) throws Exception {
 		Map<Move, List<MachineState>> transitions = getTransitions(state, false);
 		List<Move> moves = new ArrayList<Move>(transitions.keySet());
 		Move randMove = moves.get(randomInt(moves.size()));
 		List<MachineState> successors = transitions.get(randMove);
 		return successors.get(randomInt(successors.size()));
 	}
 	
 	public MachineState depthCharge(MachineState state) throws Exception {
 		int depth = 0;
 		while(!isTerminal(state)) {
         	// maybe only do this every n-th iteration?
         	if (System.currentTimeMillis() + timeoutBuffer >= timeout) {
         		throw new RuntimeException("search timeout");
         	}
             state = getRandomSuccessor(state);
             depth++;
         }
         return state;
 	}
 	
 	public List<MachineState> depthChargeFull(MachineState state) throws Exception {
 		int depth = 0;
 		List<MachineState> chain = new ArrayList<MachineState>();
 		chain.add(state);
 		
 		while(!isTerminal(state)) {
         	// maybe only do this every n-th iteration?
         	if (System.currentTimeMillis() + timeoutBuffer >= timeout) {
         		throw new RuntimeException("search timeout");
         	}
             state = getRandomSuccessor(state);
             chain.add(state);
             depth++;
         }
         return chain;
 	}
 	
 	private Score dlsMin(List<MachineState> states, int depth, Score alpha, Score beta) throws Exception {
 		for (MachineState state: states) {
 			Score s = dls(state, depth, alpha, beta).fst;
 			if (s.compareTo(beta) < 0) {
 				beta = s;
 			}
 			if (beta.compareTo(alpha) <= 0) {
 				break;
 			}
 		}
 		return beta;
 	}
 	
 	private int stateCount = 0;
 	
 	private Pair<Score, Move> dls(MachineState state, int depth, Score alpha, Score beta) throws Exception {
 		// check periodically for a timeout
 		if (System.currentTimeMillis() + timeoutBuffer >= timeout) {
 			throw new RuntimeException("search timeout");
 		}
 		
 		stateCount++;
 		
 		StateMachine sm = getStateMachine();
 		
 		// exact state, cached
 		if (stateCache.containsKey(state)) {
 			//return stateCache.get(state);
 		}
 		
 		// terminal state, uncached
 		if (sm.isTerminal(state)) {
 			int value = sm.getGoal(state, getRole());
 			stateCache.put(state, new Pair<Integer, Move>(value, null));
 			return new Pair<Score, Move>(new Score(value, -depth), null);
 		}
 		
 		// depth limit reached
 		if (depth == 0) {
 			depthLimited = true;
 			if (!heuristicCache.containsKey(state)) {
 				heuristicCache.put(state, getHeuristicValue(state));
 			}
 			int value = heuristicCache.get(state);
 			return new Pair<Score, Move>(new Score(value, 1), null);
 		}
 	
 		// recurse: minimax
 		Map<Move, List<MachineState>> legalMoves = getTransitions(state, true);
 		Move optMove = null;
 		
 		for (Move move: legalMoves.keySet()) {
 			Score cur = dlsMin(legalMoves.get(move), depth - 1, alpha, beta);
 			if (cur.compareTo(alpha) > 0) {
 				alpha = cur;
 				optMove = move;
 			}
 			if (beta.compareTo(alpha) <= 0) {
 				break;
 			}
 		}
 		return new Pair<Score, Move>(alpha, optMove);
 	}
 	
 	private Move searchGameTree() throws MoveDefinitionException {
 		Pair<Score, Move> opt = null;
 		try {
 			int depth = 0;
 			while (true) {
 				depthLimited = false;
 				depth++;
 				System.out.println(">> exploring to depth " + depth);
 				Score alpha = new Score(GOAL_MIN - 1, 1);
 				Score beta = new Score(GOAL_MAX + 1, -1000);
 				opt = dls(getCurrentState(), depth, alpha, beta);
 				System.out.println(opt);
 				if (!depthLimited) {
 					System.out.println(">> explored full depth");
 					break;
 				}
 				if (opt.fst.getScore() == GOAL_MAX) {
 					System.out.println(">> found forced win");
 					break;
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(">> stopping: " + e.getMessage());
 		}
 		
 		if (opt != null && opt.snd != null) {
 			return opt.snd;
 		}
 		
 		// didn't evaluate any moves, shouldn't happen in practice
 		System.out.println(">> returning random move");
 		return getStateMachine().getRandomMove(getCurrentState(), getRole());
 	}
 	
 	/*
 	private int minSearch(List<MachineState> states, int depth) throws Exception {
 		int min = GOAL_MAX + 1;
 		for (MachineState state: states) {
 			int val = maxSearch(state, depth).fst;
 			if (val < min) {
 				min = val;
 			}
 		}
 		return min;
 	}
 	
 	
 	private Pair<Integer, Move> maxSearch(MachineState state, int depth) throws Exception {
 		// check periodically for a timeout
 		if (System.currentTimeMillis() + timeoutBuffer >= timeout) {
 			System.out.println("timing out: " + System.currentTimeMillis() + " getting close to " + timeout);
 			throw new RuntimeException("search timeout");
 		}
 		
 		StateMachine sm = getStateMachine();
 		
 		// exact state, cached
 		if (stateCache.containsKey(state)) {
 			return stateCache.get(state);
 		}
 		
 		// terminal state, uncached
 		if (sm.isTerminal(state)) {
 			int value = sm.getGoal(state, getRole());
 			stateCache.put(state, new Pair<Integer, Move>(value, null));
 			return stateCache.get(state);
 		}
 		
 		// depth limit reached
 		if (depth == 0) {
 			depthLimited = true;
 			if (!heuristicCache.containsKey(state)) {
 				heuristicCache.put(state, getHeuristicValue(state));
 			}
 			return new Pair<Integer, Move>(heuristicCache.get(state), null);
 		}
 	
 		// recurse: minimax
 		Map<Move, List<MachineState>> legalMoves = getTransitions(state, true);
 		Move optMove = null;
 		int optVal = -1;
 		
 		for (Move move: legalMoves.keySet()) {
 			int cur = minSearch(legalMoves.get(move), depth - 1);
 			if (cur > optVal) {
 				optVal = cur;
 				optMove = move;
 			}
 		}
 		stateCache.put(state, new Pair<Integer, Move>(optVal, optMove));
 		return stateCache.get(state);
 	}
 	
 
 	
 	private void createEndGameBook() {
 		try {
 			int depth = 1;
 			while (true) {
 				System.out.println("end game book: depth " + depth);
 				for (int i = 0; i < 10; i++) {
 					List<MachineState> chain = depthChargeFull(getCurrentState());
 					int startIdx = Math.max(0, chain.size() - depth - 1);
 					MachineState start = chain.get(startIdx);
 					maxSearch(start, 10000);			
 				}
 				if (stateCache.containsKey(getCurrentState())) {
 					break;
 				}
 				depth++;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private MachineState getRandomState() throws Exception {
 		List<MachineState> list = depthChargeFull(getCurrentState());
 		int randIdx = (int) (Math.random() * list.size());
 		return list.get(randIdx);
 	}
 	
 	private double sigmoid(double t) {
 		return 1 / (1 + Math.exp(-t));
 	}
 	*/
 	
 	@Override
 	public void stateMachineMetaGame(long timeout)
 			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
 		System.out.println(">> metagaming");
 		
 		long curTime = System.currentTimeMillis();
 		long duration = timeout - curTime;
 		this.timeout = curTime + duration;
 		
 		searchGameTree();
 		
 		long endTime = System.currentTimeMillis();
 		System.out.println("elapsed time: " + (endTime - curTime));
 		System.out.println("states/sec: " + (stateCount * 1000.0 / (endTime - curTime)));
 		
 		System.out.println(">> done metagaming");
 	}
 
 	@Override
 	public Move stateMachineSelectMove(long timeout)
 			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
 		this.timeout = timeout;
 		return searchGameTree();
 	}
 
 	@Override
 	public void stateMachineStop() {}
 
 	@Override
 	public void stateMachineAbort() {}
 
 }
