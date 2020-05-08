 package apps.team;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import player.gamer.statemachine.StateMachineGamer;
 import util.statemachine.MachineState;
 import util.statemachine.Move;
 import util.statemachine.StateMachine;
 import util.statemachine.implementation.propnet.PropNetStateMachine;
 
 public class Engine extends StateMachineGamer {
 	// How much free time do we want to leave ourselves to do cleanup
 	// and return the move.
 	private static final long TIMEOUT_BUFFER = 500;
 	// If we spend X time expanding out the GameNode tree, then spend
 	// MONTE_CARLO_RATIO*X time doing monte carlo afterwards before we move
 	// on to the next state
 	private static final double MONTE_CARLO_RATIO = 1.0;
 
 	private MachineStateCache stateCache;
 	private Random random = new Random();
 
 	// A node in the search tree. Note that this is *not* isomorphic to a
 	// MachineState. Multiple GameNodes can have the same MachineState. This
 	// is because arriving in a MachineState through different paths may let
 	// heuristics evaluate the situation differently.
 
 	// Note: We never store a MachineState in this class; this lets us move all
 	// the caching decisions elsewhere.
 	private class GameNode
 	{
 		// Which GameNode led to us. Null if we are the root of the search tree
 		// (i.e. the current game state).
 		private GameNode parent;
 		// Which move number in the parent's MachineState led to us (null if we're
 		// the search tree root.
 		private Move move;
 		// Which submove number in the parent's MachineState led to us
 		// (i.e., which move did the opponents select, after our move was
 		// determined. This is because
 		private int subMoveNum;
 		// The children of this GameNode. Null if this node hasn't been
 		// expanded yet.
 		private List<GameNode> children;
 		// The children of this GameNode, ordered by the move that leads to them
 		private Map<Move, List<GameNode>> childrenByMove;
 		// Is this a terminal node?
 		private boolean terminal;
 		// If this is a terminal node, what is its value?
 		private int value;
 		// The sum of all depth charges performed starting at this node
 		private int monteCarloSum;
 
 		GameNode(GameNode parent, Move move, int subMoveNum)
 		{
 			this.parent = parent;
 			this.move = move;
 			this.subMoveNum = subMoveNum;
 			this.value = 0;
 			this.monteCarloSum = 0;
 			this.terminal = false;
 			
 			MachineState myState = getMachineState();
 			this.terminal = Engine.this.stateCache.isTerminalState(myState);
 			if (terminal) {
 				this.value = Engine.this.stateCache.terminalValue(myState);
 			}
 		}
 
 		public GameNode getParent() {
 			return parent;
 		}
 
 		public Move getMove() {
 			return move;
 		}
 
 		public int getSubMoveNum() {
 			return subMoveNum;
 		}
 		
 		public boolean isTerminal() {
 			return terminal;
 		}
 
 		public List<GameNode> getChildren() {
 			if (children != null) {
 				return children;
 			}
 			children = new ArrayList<GameNode>();
 
 			MachineState state = getMachineState();
 
 			if (isTerminal()) {
 				return children;
 			}
 
 			Map<Move, List<MachineState>> transitions = stateCache.getTransitions(state);
 			for (Map.Entry<Move, List<MachineState>> entry : transitions.entrySet()) {
 				Move myMove = entry.getKey();
 				for(int i = 0; i < entry.getValue().size(); ++i) {
 					children.add(new GameNode(this, myMove, i));
 				}
 			}
 			return children;
 		}
 
 		public Map<Move, List<GameNode>> getChildrenByMove() {
 			if (childrenByMove != null) {
 				return childrenByMove;
 			}
 			childrenByMove = new HashMap<Move, List<GameNode>>();
 			List<GameNode> children = getChildren();
 			for (GameNode child : children) {
 				List<GameNode> siblings = childrenByMove.get(child.move);
 				if (siblings == null) {
 					siblings = new ArrayList<GameNode>();
 					childrenByMove.put(child.move, siblings);
 				}
 				siblings.add(child);
 			}
 			return childrenByMove;
 		}
 
 		public MachineState getMachineState() {
 			if (parent == null) {
 				return Engine.this.getCurrentState();
 			}
 			MachineState parentState = parent.getMachineState();
 			return stateCache.getTransitionResult(parentState, move, subMoveNum);
 		}
 	}
 
 	@Override
 	public String getName() {
 		return "PGGGPPG engine";
 	}
 
 	@Override
 	public StateMachine getInitialStateMachine() {
 		return new PropNetStateMachine();
 	}
 
 	Move getRandomMove(MachineState state) {
 		Map<Move, List<MachineState>> transitions = stateCache.getTransitions(state);
 		List<Move> moves = new ArrayList<Move>(transitions.keySet());
 		return moves.get(random.nextInt(moves.size()));
 	}
 
 	// Fill in all the information that heuristics might want to use when
 	// performing minimax.
 	void fillHeuristicState(GameNode node) {		
 
 	}
 
 	private Integer getScoreEstimate(GameNode searchTreeRoot, int numDepthCharges) {
 		// just use monte carlo for now
 		if (searchTreeRoot.isTerminal()) {
 			return searchTreeRoot.value;
 		}
 		return 50;
		/*
		if (numDepthCharges == 0) {
			return 1;
		}
		return (int)(((double)searchTreeRoot.monteCarloSum)/numDepthCharges)-1;
		*/
 	}
 
 	// Performs depth charges until we time out.
 	// Note: we don't return TIMEOUT_BUFFER millis before the timeout; we
 	// only stop doing depth charges once we've *actually* hit the timeout
 	int performMonteCarlo(List<GameNode> fringe, long timeout)
 	{
 		int numCharges;
 		for (numCharges = 0; System.currentTimeMillis() < timeout; ++numCharges) {
 			GameNode startPoint = fringe.get(random.nextInt(fringe.size()));
 			MachineState state = startPoint.getMachineState();
 			if (stateCache.isTerminalState(state)) {
 				continue;
 			}
 			while (!stateCache.isTerminalState(state)) {
 				// Replace state with a random successor, if time is left
 				if (System.currentTimeMillis() >= timeout) {
 					break;
 				}
 				Map<Move, List<MachineState>> transitions = stateCache.getTransitions(state);
 				List<List<MachineState>> successors = new ArrayList<List<MachineState>>(transitions.values());
 				List<MachineState> successorsAfterMove = successors.get(random.nextInt(successors.size()));
 				state = successorsAfterMove.get(random.nextInt(successorsAfterMove.size()));
 			}
 			if (stateCache.isTerminalState(state)) {
 				// State is terminal; incremenet GameNode's counter.
 				startPoint.monteCarloSum += stateCache.terminalValue(state);
 			}
 		}
 		return numCharges;
 	}
 
 	// Returns the best move at the given GameNode
 	// Returns null if we couldn't finish the minimax by timeout - TIMEOUT_BUFFER
 	Pair<Move, Integer> performMinimax(GameNode root, int depth, int numDepthCharges, long timeout) {
 		if (System.currentTimeMillis() + TIMEOUT_BUFFER >= timeout) {
 			return null;
 		}
 
 		if (depth == 0 || root.isTerminal()) {
 			return new Pair<Move, Integer>(null, getScoreEstimate(root, numDepthCharges));
 		}
 
 		// Not at the tree bottom
 		int bestScoreSoFar = -1;
 		Move bestMoveSoFar = null;
 		for (Map.Entry<Move, List<GameNode>> entry : root.getChildrenByMove().entrySet()) {
 			Move move = entry.getKey();
 			List<GameNode> children = entry.getValue();
 			int worstScoreForMoveSoFar = 101;
 			for (GameNode child : children) {
 				Pair<Move, Integer> childMinMaxScore = performMinimax(child, depth - 1, numDepthCharges, timeout);
 				if (childMinMaxScore == null) {
 					return null;
 				}
 				if (childMinMaxScore.snd < worstScoreForMoveSoFar) {
 					worstScoreForMoveSoFar = childMinMaxScore.snd;
 				}
 			}
 			if (worstScoreForMoveSoFar > bestScoreSoFar) {
 				bestScoreSoFar = worstScoreForMoveSoFar;
 				bestMoveSoFar = move;
 			}
 		}
 		return new Pair<Move, Integer>(bestMoveSoFar, bestScoreSoFar);
 	}
 
 	// Fills in the heuristic state at each gamenode up to the depth indicated.
 	// Returns whether or not we should stop searching because of timeouts.
 	// I.e., returns true if we should stop.
 	// Additionally, adds every node on the fringe to the "fringeNodes" list
 	private boolean expandNodes(GameNode node, int depth, long timeout, List<GameNode> fringeNodes) {
 		if (System.currentTimeMillis() + TIMEOUT_BUFFER >= timeout) {
 			return true;
 		}
 
 		if (node.isTerminal()) {
 			fringeNodes.add(node);
 			return false;
 		}
 
 		if (depth == 0) {
 			fillHeuristicState(node);
 			fringeNodes.add(node);
 		} else {
 			for (GameNode childNode : node.getChildren()) {
 				boolean timedOut = expandNodes(childNode, depth - 1, timeout, fringeNodes);
 				if (timedOut) {
 					return timedOut;
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public Move stateMachineSelectMove(long timeout) {
 		try {
 			if (stateCache == null) {
 				stateCache = new MachineStateCache(getStateMachine(), getRole());
 			}
 
 			// Start off with a random move so that we can at least do *something*
 			// if we run out of time.
 			Move bestSoFar = getRandomMove(getCurrentState());
 			// A place to start the search tree. It has no parents, hence the nulls.
 			GameNode searchTreeRoot = new GameNode(null, null, 0);
 			// Try to find the best move up to depth deep, until we run out of
 			// time.
			for (int depth = 0; true; ++depth) {
 				System.out.println(">> Exploring to depth " + depth);
 				List<GameNode> fringeNodes = new ArrayList<GameNode>();
 				long expandStartTime = System.currentTimeMillis();
 				boolean timedOut = expandNodes(searchTreeRoot, depth, timeout, fringeNodes);
 				if (timedOut) {
 					break;
 				}
 				long expandEndTime = System.currentTimeMillis();
 				long monteCarloTime = (long)(MONTE_CARLO_RATIO*(expandEndTime - expandStartTime));
 				long monteCarloTimeout = Math.min(timeout - TIMEOUT_BUFFER, expandEndTime + monteCarloTime);
 				int numDepthCharges = performMonteCarlo(fringeNodes, monteCarloTimeout);
 				Pair<Move, Integer> newBest = performMinimax(searchTreeRoot, depth, numDepthCharges, timeout);
				if (newBest != null) {
					bestSoFar = newBest.fst;
 				}
 			}
 			return bestSoFar;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	@Override
 	public void stateMachineMetaGame(long timeout) {

 	}
 
 	@Override
 	public void stateMachineStop() {
 		// Do nothing.
 	}
 
 	@Override
 	public void stateMachineAbort() {
 		// Do nothing
 	}
 }
