 package org.ggp.base.player.gamer.statemachine.sample;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.ggp.base.util.statemachine.*;
 import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
 import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
 import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
 
 /**
  * Fabulous Monte-Carlo Tree Search Player
  * 
  * @author Irme, Nicolai
  *
  */
 final class FabulousMonteCarlo extends SampleGamer {
 	
	private static final int MAX_SCORE = 100;
 	
	private static final int MIN_SCORE = 0;
 	
 	private static final double C = 1.0;
 	
 	/**
 	 * Class representing a node of the MCTS tree
 	 */
 	protected abstract class Node{
 		
 	}
 	
 	/**
 	 * Non-terminal node in the MCTS tree
 	 */
 	private class NonTerminalNode extends Node{
 		
 		final MachineState state;
 		final List<List<Move>> legal;
 		Map<int[], Node> successors;
 		int n;
 		int [][] n_action;
 		double [][] q_action;
 		
 		/**
 		 * @param state Game state
 		 */
 		protected NonTerminalNode(MachineState state){
 			this.state = state;
 			List<List<Move>> temp;
 			try {
 				temp = theMachine.getLegalJointMoves(state);
 			} catch (MoveDefinitionException e) {
 				System.err.println("Couldn't compute legal moves.");
 				temp = null;
 			}
 			legal = temp;
 			n = 0;
 			n_action = new int[theMachine.getRoles().size()][];
 			q_action = new double[n_action.length][];
 			for(int i = 0; i < n_action.length; i++) {
 				n_action[i] = new int[legal.get(i).size()];	
 				q_action[i] = new double[n_action[i].length];
 				Arrays.fill(n_action[i], 0);
 				Arrays.fill(q_action[i], 0);
 			}
 			successors = new HashMap<int[], Node>();
 		}
 	}
 	
 	/**
 	 * Terminal node in the MCTS tree
 	 */
 	private class TerminalNode extends Node{
 		
 		final int[] goal;
 		
 		/**
 		 * @param state Game state
 		 */
 		protected TerminalNode(MachineState state){
 			List<Integer> temp;
 			 try {
 				temp = theMachine.getGoals(state);
 			} catch (GoalDefinitionException e) {
 				System.err.println("No goals");
 				temp = new ArrayList<Integer>();
 			}
 			 goal = new int[temp.size()];
 			 for(Integer i : temp){
 				 goal[i] = temp.get(i);
 			 }
 		}
 	}
 	
 	protected StateMachine theMachine;
 	
 	private NonTerminalNode root;
 	
 	private MachineState currentState;
 	
 	private int role;
 	
 	@Override
 	public void setState(MachineState state){
 		currentState = state;
 	}
 	
 	@Override
 	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
 		timeout -= 500;
 		Move m = mcts(timeout);
 		if(m != null){
 			return m;
 		}
 		System.out.println("Playing random move.");
 		return theMachine.getRandomMove(currentState, theMachine.getRoles().get(role));
 	}
 	
 	@Override
 	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
 		theMachine = getStateMachine();
 		root = new NonTerminalNode(currentState);
 		role = theMachine.getRoleIndices().get(getRole());
 		timeout -= 500;
 		mcts(timeout);
 	
 	}	
 	
 	/**
 	 * Performs Monte-Carlo tree search.
 	 * 
 	 * @param timeout Time limit
 	 * @return Best move in the root state
 	 */
 	private Move mcts(long timeout){
 		Move bestMove = null;
 		double bestScore = Double.NEGATIVE_INFINITY;
 		while(System.currentTimeMillis() < timeout){
 			int next = mctsStep(timeout);
 			if(next == -1){
 				continue;
 			}
 			double score = root.q_action[role][next] - uct(root.n, root.n_action[role][next]);
 			if(score > bestScore){
 				bestScore = score;
 				bestMove = root.legal.get(role).get(next);
 			}
 		}
 		return bestMove;
 	}
 	
 	/**
 	 * Performs one MCTS step.
 	 * 
 	 * @param timeout Time limit
 	 * @return Index of the move performed by the player in the root state
 	 * @throws TimeoutException Time limit reached
 	 */
 	private int mctsStep(long timeout){
 		List<NonTerminalNode> path = new ArrayList<NonTerminalNode>();
 		int[] scores;
 		Node current = root;
 		NonTerminalNode c = (NonTerminalNode)root;
 		int rootMove = -1;
 		
 		// Selection
 		List<int[]> select = new ArrayList<int[]>();
 		List<Move> selectM = new ArrayList<Move>();
 		while(current != null){
 			if(current instanceof TerminalNode){
 				scores = ((TerminalNode)current).goal;
 				break;
 			}
 			c = (NonTerminalNode)current;
 			path.add(c);
 			select.add(new int[theMachine.getRoles().size()]);
 			selectM = new ArrayList<Move>();
 			for(int p = 0; p < theMachine.getRoles().size(); p++){
 				List<Move> moves = c.legal.get(p);
 				double bestval = Double.NEGATIVE_INFINITY;
 				Move bestmove = null;
 				int bestindex = -1;
 				for(int i = 0; i < moves.size(); i++){
 					Move m = moves.get(i);
 					double value = c.q_action[p][i] + uct(c.n, c.n_action[p][i]);
 					if(value > bestval){
 						bestval = value;
 						bestmove = m;
 						bestindex = i;
 					}
 				}
 				select.get(select.size() - 1)[p] = bestindex;
 				selectM.add(bestmove);
 			}
 			if(current == root){
 				rootMove = select.get(select.size() - 1)[role];
 			}
 			current = c.successors.get(select);
 		}
 		
 		// Expansion & Playout
 		MachineState next;
 		try {
 			next = theMachine.getNextState(c.state, selectM);
 		} catch (TransitionDefinitionException e) {
 			System.err.println("Could not perform state update.");
 			return -1;
 		}
 		if(theMachine.isTerminal(next)){
 			current = new TerminalNode(next);
 		}
 		else{
 			current = new NonTerminalNode(next);
 		}
 		c.successors.put(select.get(select.size() - 1), current);
 		scores = playout(current, timeout);
 		if(scores == null){
 			return -1;
 		}
 		
 		// Backpropagation
 		for(int i = 0; i < path.size(); i++){
 			NonTerminalNode node = path.get(i);
 			for(int j = 0; j < theMachine.getRoles().size(); j++){
 				node.n_action[j][select.get(i)[j]]++;
 				node.q_action[j][select.get(i)[j]] = (node.q_action[j][select.get(i)[j]] * node.n_action[j][select.get(i)[j]] + scores[i])
 						/ (node.n_action[j][select.get(i)[j]] + 1);
 			}
 		}
 		
 		return rootMove;
 	}
 	
 	/**
 	 * Monte-Carlo simulation for playout.
 	 * 
 	 * @param node Node to start at
 	 * @param timeout Time limit
 	 * @return Scores for all players
 	 */
 	private int[] playout(Node node, long timeout){
 		if(node instanceof TerminalNode){
 			return ((TerminalNode)node).goal;
 		}
 		MachineState state = ((NonTerminalNode)node).state;
 		while(System.currentTimeMillis() < timeout){
 			if(theMachine.isTerminal(state)){
 				List<Integer> s;
 				try {
 					s = theMachine.getGoals(state);
 				} catch (GoalDefinitionException e) {
 					System.err.println("Could not compute goal values.");
 					return null;
 				}
 				int[] scores = new int[s.size()];
 				for(int i = 0; i < scores.length; i++){
 					scores[i] = s.get(i);
 				}
 				return scores;
 			}
 			try {
 				state = theMachine.getNextState(state, theMachine.getRandomJointMove(state));
 			} catch (TransitionDefinitionException e) {
 				System.err.println("Could not perform state update.");
 				return null;
 			} catch (MoveDefinitionException e) {
 				System.err.println("Could not compute legal moves.");
 				return null;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Calculates UCT for MCTS.
 	 * 
 	 * @param n Total number of simulations
 	 * @param na Number of simulations for this action
 	 * @return Confidence bound
 	 */
 	public static double uct(int n, int na){
 		return C * Math.sqrt(Math.log(n) / na);
 	}
 	
 }
