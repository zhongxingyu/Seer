 package org.ggp.base.player.gamer.statemachine.sample;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.collections4.map.AbstractReferenceMap;
 import org.apache.commons.collections4.map.ReferenceMap;
 import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
 import org.ggp.base.util.statemachine.MachineState;
 import org.ggp.base.util.statemachine.Move;
 import org.ggp.base.util.statemachine.Role;
 import org.ggp.base.util.statemachine.StateMachine;
 import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
 import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
 import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
 
 /**
  * Fabulous Multiplayer
  * 
  * @author Nera, Nicolai
  *
  */
 final class FabulousMultiPlayer2 extends SampleGamer {
 
 	private static final int MAX_SCORE = 100;
 
 	private static final int MIN_SCORE = 0;
 
 	private static final int TIME_MULT = 1;
 
 	private static final int TIME_DIV = 10;
 
 	private static final ReferenceStrength SOFT = AbstractReferenceMap.ReferenceStrength.SOFT;
 
 	/**
 	 * Minimax internal node return value.
 	 * Holds a score and information about completeness of exploration.
 	 */
 	private class Tuple {
 		protected final int score;
 		protected final boolean complete;
 		protected final boolean pruned;
 		protected final int alpha;
 		protected final int beta;
 		protected final Move move;
 		protected final List <Move> moves;
 
 		protected Tuple (int score, boolean complete, boolean pruned, int alpha, int beta, Move move, List <Move> moves){
 			this.complete = complete;
 			this.score = score;
 			this.pruned = pruned;
 			this.alpha = alpha;
 			this.beta = beta;
 			this.move = move;
 			this.moves = moves;
 		}
 	}
 
 	/**
 	 * Thrown if a timeout occurs during search.
 	 */
 	private class TimeoutException extends Throwable {
 		private static final long serialVersionUID = 7485356568086889532L;
 
 		public TimeoutException(){
 			super();
 		}
 	}
 
 	private final TimeoutException timeoutException = new TimeoutException();
 
 	private ReferenceMap<MachineState, Tuple> transposition;
 
 	private  ReferenceMap <MachineState, Map <Move, Tuple>> transpositionMin;
 
 	private Role role;
 
 	private StateMachine theMachine;
 
 	private MachineState currentState;
 
 	private long timeout;
 
 	private long turnpoint;
 
 	private boolean prune;
 
 	@Override
 	public void setState(MachineState state){
 		currentState = state;
 	}
 
 	@Override
 	public void stateMachineMetaGame(long timeout){
 		timeout -= 500;
 		this.timeout = timeout;
 		this.turnpoint = ((TIME_DIV - TIME_MULT) * System.currentTimeMillis() + TIME_MULT * timeout) / TIME_DIV;
 		theMachine = getStateMachine();
 		role = getRole();
 		transposition = new ReferenceMap<MachineState, Tuple>(SOFT, SOFT);
 		transpositionMin = new ReferenceMap <MachineState, Map <Move, Tuple>> (SOFT,SOFT);
 		prune = false;
 		minimax(currentState);
 	}
 
 	@Override
 	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
 		timeout -= 500;
 		this.timeout = timeout;
 		this.turnpoint = 0;
 		prune = false;
 		Move move = minimax(currentState);
 		if(move != null){
 			return move;
 		}
 		System.out.println("Playing random move.");
 		return theMachine.getRandomMove(currentState, role);
 	}
 
 	/**
 	 * Performs exhaustive minimax search in a state.
 	 * 
 	 * @param state Game state
 	 * @param timeout Time limit
 	 * @return Ideal move
 	 */
 	private Move minimax(MachineState state){
 		if(transposition.containsKey(state) && transposition.get(state).complete){
 			Tuple lookup = transposition.get(state);
 			if(!lookup.pruned || (lookup.alpha == MIN_SCORE - 1 && lookup.beta == MAX_SCORE + 1)){
 				return lookup.move;
 			}
 		}
 		List<Move> moves;
 		try {
 			moves = theMachine.getLegalMoves(state, role);
 		} catch (MoveDefinitionException e) {
 			System.err.println("No legal moves!");
 			return null;
 		}
 		boolean notDone = true;
 		boolean pruned = false;
 		int depth = 1;	//Change to something high for testing with output!
 		int bestScore = MIN_SCORE - 1;
 		Move bestMove =  null;
 		Search:
 			while(notDone){
 				if(System.currentTimeMillis() > timeout){
 					System.out.println("Ran out of time!");
 					break;
 				}
 				depth++;
 				if(System.currentTimeMillis() > turnpoint){
 					prune = true;
 				}
 				int alpha = MIN_SCORE - 1;
 				int beta = MAX_SCORE + 1;
 				notDone = false;
 				pruned = false;
 				for (Move move: moves){
 					Tuple tempScore = new Tuple(bestScore, false, false, alpha, beta, move, null);
 					try {
 						tempScore = minPlayer (state, move, depth, alpha, beta);
 					} catch (TimeoutException e){
 						System.out.println("Ran out of time!");
 						notDone = true;
 						break Search;
 					}
 					if(!tempScore.complete){
 						notDone = true;
 					}
 					else{
 						if(tempScore.score > bestScore){
 							bestScore = tempScore.score;
 							bestMove = move;
 						}
 						if(tempScore.score > alpha){
 							alpha = tempScore.score;
 						}
 					}
 					if(tempScore.pruned){
 						pruned = true;
 					}
 				}
 			}
 		transposition.put(state, new Tuple(bestScore, ! notDone, pruned, MIN_SCORE - 1, MAX_SCORE + 1, bestMove, null));
 		//System.out.println("Done. Depth: " + depth);
 		return bestMove;
 	}
 
 	/**
 	 * Recursively performs minimax search (max-player move)
 	 * 
 	 * @param state Game state
 	 * @param depth Depth limit
 	 * @param timeout Time limit
 	 * @param alpha Alpha value
 	 * @param beta Beta value
 	 * @return Best score
 	 * @throws TimeoutException Time limit exceeded
 	 */
 	private Tuple maxPlayer(MachineState state, int depth, int alpha, int beta) throws TimeoutException{
 		if(System.currentTimeMillis() > timeout){
 			throw timeoutException;
 		}
 		if(theMachine.isTerminal(state)){
 			Tuple ret;
 			try {
 				ret = new Tuple(theMachine.getGoal(state, role), true, false, alpha, beta, null, null);
 			} catch (GoalDefinitionException e) {
 				System.err.println("Bad goal description!");
 				ret = new Tuple(Integer.MIN_VALUE, false, false, alpha, beta, null, null);
 			}
 			//System.out.println("Found a goal of value " + ret.score);
 			transposition.put(state, ret);
 			return ret;
 		}
 		if(transposition.containsKey(state) && transposition.get(state).complete){
 			Tuple lookup = transposition.get(state);
 			/*if( lookup.pruned && (lookup.score  >= beta)){
 				return lookup;
 			}*/
 
 			if (lookup.alpha <= alpha && lookup.beta >= beta){
 				return lookup;
 			}
 		}
 		if(depth == 0){
 			return new Tuple(Integer.MIN_VALUE, false, false, alpha, beta, null, null);
 		}
 
 		int bestScore = MIN_SCORE - 1;
 		//Move bestMove = null;
 		//boolean pruned = false;
 		boolean complete = true;
 		boolean foundOne = false;
 		boolean pruned = false;
 		Move bestMove = null;
 		int alpha0 = alpha;
 
 		Move firstTry = null;
 		if(transposition.containsKey(state) && transposition.get(state).move != null){
 			firstTry = transposition.get(state).move;
 			Tuple s = minPlayer(state, firstTry, depth, alpha, beta);
 			if(!s.complete){
 				complete = false;
 				//return new Tuple (Integer.MIN_VALUE, false);
 			}	
 			if(s.pruned){
 				pruned = true;
 			}
 			if(s.score != Integer.MIN_VALUE){
 				foundOne = true;
 				/*
 				if(s.score > bestScore){
 					bestScore = s.score;
 					//bestMove = move;
 				}
 				 */
 				if(s.complete){
 					if(s.score > bestScore){
 						bestScore = s.score;
 						bestMove = firstTry;
 					}
 					if(bestScore > alpha){
 						alpha = bestScore;
 					}
 					if(prune && alpha >= beta){
 						pruned = true;
 					}
 				}
 			}
 		}
 
 		List<Move> moves;
 		try {
 			moves = theMachine.getLegalMoves(state, role);
 		} catch (MoveDefinitionException e) {
 			System.err.println("No legal moves!");
 			return new Tuple(Integer.MIN_VALUE, false, false, alpha0, beta, null, null);
 		}
 		for(Move move : moves){
 			if(move.equals(firstTry)){
 				continue;
 			}
 			Tuple s = minPlayer(state, move, depth, alpha, beta);
 			if(!s.complete){
 				complete = false;
 				//return new Tuple (Integer.MIN_VALUE, false);
 			}	
 			if(s.pruned){
 				pruned = true;
 			}
 			if(s.score != Integer.MIN_VALUE){
 				foundOne = true;
 				/*
 				if(s.score > bestScore){
 					bestScore = s.score;
 					//bestMove = move;
 				}
 				 */
 				//if(s.complete){
 				if(s.score > bestScore){
 					bestScore = s.score;
 					bestMove = move;
 				}
 				if(bestScore > alpha){
 					alpha = bestScore;
 				}
 				if(prune && alpha >= beta){
 					pruned = true;
 					break;
 				}
 				//}
 			}
 		}
 
 		if(! foundOne){
 			bestScore = Integer.MIN_VALUE;
 		}
 		Tuple ret = new Tuple(bestScore, complete, pruned, alpha0, beta, bestMove, null);
 		
 			transposition.put(state, ret);
 		
 		
 
 		return ret;
 	}
 
 	/**
 	 * Recursively performs minimax search (min-player move)
 	 * 
 	 * @param state Game state
 	 * @param move Max-player's move
 	 * @param depth Depth limit
 	 * @param timeout Time limit
 	 * @param alpha Alpha value
 	 * @param beta Beta value
 	 * @return Worst score
 	 * @throws TimeoutException Time limit exceeded
 	 */
 	private Tuple minPlayer(MachineState state, Move move, int depth, int alpha, int beta) throws TimeoutException{
 		if( System.currentTimeMillis() > timeout){
 			throw timeoutException;
 		}
 
 		if(transpositionMin.containsKey(state) && transpositionMin.get(state).containsKey(move) && transpositionMin.get(state).get(move).complete){
 			Tuple lookup = transpositionMin.get(state).get(move);
 			/*if( lookup.pruned && (alpha >=lookup.score )){
 				return lookup;
 			}	*/			
 			if (lookup.alpha <= alpha && lookup.beta >= beta){
 				return lookup;				
 			}
 		}
 
 		List<List<Move>> options = new ArrayList<List<Move>>();
 		List<Role> roles = theMachine.getRoles();
 		int fabulous = 0;
 		for(int i = 0; i < roles.size(); i++){
 			Role player = roles.get(i);
 			if(player.equals(role)){
 				fabulous = i;
 				continue;
 			}
 			List<Move> moves;
 			try {
 				moves = theMachine.getLegalMoves(state, player);
 			} catch (MoveDefinitionException e) {
 				System.err.println("No legal moves!");
 				moves = new ArrayList<Move>();
 			}
 			options.add(moves);
 		}
 		Set<List<Move>> next = combinations(options);
 
 		MachineState nextState;
 		int worstScore = MAX_SCORE + 1;
 		boolean complete = true;
 		boolean foundOne = false;
 		boolean pruned = false;
 		int beta0 = beta;
 		List <Move> bestMoves = null;
 		for(List<Move> moves : next){
 			//System.out.println("Expanding " + moves.get(0).toString());
 			moves.add(fabulous, move);
 			try {
 				nextState = theMachine.getNextState(state, moves);
 			} catch (TransitionDefinitionException e) {
 				System.err.println("Attempted bad moves!");
 				complete = false;
 				continue;
 			}
 			Tuple s = maxPlayer(nextState, depth - 1, alpha, beta);
 			if(!s.complete){
 				complete = false;
 				//return new Tuple (Integer.MIN_VALUE, false);
 			}
 			if(s.pruned){
 				pruned = true;
 			}
 			if(s.score != Integer.MIN_VALUE){
 				foundOne = true;
 				/*
 				if(s.score < worstScore){
 					worstScore = s.score;
 				}
 				 */
 				//if(s.complete){
 					if(s.score < worstScore){
 						worstScore = s.score;
 
 					}
 					if(worstScore < beta){
 						beta = worstScore;
 						bestMoves = moves;
 					}
 					if(prune && alpha >= beta){
 						pruned = true;
 						break;
 					}
 				//}
 			}
 		}
 
 		if(!foundOne){
 			worstScore = Integer.MIN_VALUE;
 		}
 		
 		Tuple ret = new Tuple(worstScore, complete, pruned, alpha, beta0, null, bestMoves);
 		if (! transpositionMin.containsKey(state)){
 			
 			transpositionMin.put(state, new ReferenceMap<Move, FabulousMultiPlayer2.Tuple>());
 			
 						
 		}
 		
 		transpositionMin.get(state).put(move, ret);
 
 		/*
 		else {
 			if (transpositionMin.get(state).containsKey(move)){
 				if (transpositionMin.get(state).get(move).beta > beta0){
 					
 
 				}
 			}
 			else{
 				transpositionMin.get(state).put(move, ret);
 			}
 
 		}*/
		transpositionMin.get(state).put(move, ret);
 		return ret;
 	}
 
 	/**
 	 * Creates all combinations of moves for opposing players.
 	 * 
 	 * @param moves List of all possible moves for all opposing player (in order)
 	 * @return Set of combinations of one move per opposing player (maintains order)
 	 */
 	private Set<List<Move>> combinations(List<List<Move>> moves){
 		Set<List<Move>> ret = new HashSet<List<Move>>();
 		if(moves.size() == 0){
 			return ret;
 		}
 		int num = 1;
 		for(List<Move> l : moves){
 			num *= l.size();
 		}
 		for(int i = 0; i < num; i++){
 			int tmp = i;
 			List<Move> combination = new ArrayList<Move>();
 			for(int r = 0; r < moves.size(); r++){
 				List<Move> l = moves.get(r);
 				if(l.size() == 0){
 					combination.add(null);
 					continue;
 				}
 				combination.add(l.get(tmp % l.size()));
 				tmp /= l.size();
 			}
 			ret.add(combination);
 		}
 		return ret;
 	}
 
 }
