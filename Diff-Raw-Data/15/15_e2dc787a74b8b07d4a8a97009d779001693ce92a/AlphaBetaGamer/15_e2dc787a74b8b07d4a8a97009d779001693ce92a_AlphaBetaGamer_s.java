 package player.gamer.statemachine.alphaBeta;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Stack;
 import java.util.concurrent.ConcurrentHashMap;
 import java.math.*;
 
 import player.gamer.statemachine.StateMachineGamer;
 import util.statemachine.MachineState;
 import util.statemachine.Move;
 import util.statemachine.Role;
 import util.statemachine.StateMachine;
 import util.statemachine.exceptions.GoalDefinitionException;
 import util.statemachine.exceptions.MoveDefinitionException;
 import util.statemachine.exceptions.TransitionDefinitionException;
 import util.statemachine.implementation.prover.ProverStateMachine;
 
 public class AlphaBetaGamer extends StateMachineGamer {
 
 	@Override
 	public StateMachine getInitialStateMachine() {
 		return new ProverStateMachine();
 	}
 	private int search_depth=1;
 	@Override
 	public void stateMachineMetaGame(long timeout)
 			throws TransitionDefinitionException, MoveDefinitionException,
 			GoalDefinitionException {
 		search_depth = 4;//estimateDepth(50,4);
 		values = new ConcurrentHashMap<MachineState,Float>();
 		depths = new ConcurrentHashMap<MachineState,Integer>();
 		moves = new ConcurrentHashMap<MachineState,Move>();
 
 	}
 	
 	private int estimateDepth(int trials, int max_depth) throws TransitionDefinitionException, MoveDefinitionException{
 		int min_depth = max_depth;
 		trials = trials > 0 ? trials : 0;
 		for(int i = 0; i < trials; i++) {
 			// depth charge destroys the current state so we first take a single step
 			// which clones the state for us
 			MachineState state = getStateMachine().getRandomNextState(this.getCurrentState());
 			int[] depth = new int[1];
 			getStateMachine().performDepthCharge(state, depth);
 			// add one to account for our first step
 			min_depth = Math.min(min_depth, depth[0] + 1);
 		}
 		return min_depth;
 	}
 	private ConcurrentHashMap<MachineState,Float> values;
 	private ConcurrentHashMap<MachineState,Move> moves;
 	private ConcurrentHashMap<MachineState,Integer> depths;
 	
 	public void findMinMax(int depth) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
 		// Asuming 0-100 range
 		iterMinMax(depth, this.getCurrentState(), -1, 101);		
 	}
 	// we prefer incomplete to getting nothing but prefer getting something to it
 	final static float INCOMPLETE_SEARCH_VALUE = 0.5f;
 	private void iterMinMax(int depth, MachineState s, float alpha, float beta) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
 		StateMachine sm = this.getStateMachine();
 		// have we done this before?
 		if(!depths.containsKey(s) || depths.get(s) < depth){
 			// are we at the base-case?
 			if(sm.isTerminal(s)){
 				int score = getGoal(s);
 				values.put(s, (float) score);
 				depths.put(s, depth);
 			} else if(depth == 0){
 				values.put(s, INCOMPLETE_SEARCH_VALUE);
 				depths.put(s, depth);			
 			} else {
 				List<Move> our_moves = sm.getLegalMoves(s, this.getRole());
 				List<Role> opposing_roles = new ArrayList<Role>(sm.getRoles());
 				opposing_roles.remove(this.getRole());				
				Move best_move = null;
				float max_min_val = 0;			
 				// Assume we go first. Opponent has full info for optimal counter.
 				float a = alpha;
 				for(Move our_move : our_moves) {
 					float b = beta;
 					for(List<Move> joint_move : sm.getLegalJointMoves(s, this.getRole(), our_move)) {
 						MachineState next_state = sm.getNextState(s, joint_move);
 						iterMinMax(depth - 1, next_state, a, b);
 						float state_val = values.get(next_state);
 						b = Math.min(b, state_val);
						if (b <= alpha)
 							break;
 					}
 					// a = Math.max(a, beta);
 					if(beta >= a){
 						a = beta;
 						best_move = our_move;
 					}
 					if ( beta <= a)
 						break;
 				}
				values.put(s, max_min_val);
 				depths.put(s, depth);
				moves.put(s, best_move);	
 			}
 		}
 	}
 	
 	private int getGoal(MachineState s) throws GoalDefinitionException{
 		if(this.getStateMachine().isTerminal(s)){
 			return this.getStateMachine().getGoal(s, this.getRole());
 		} else {
 			return 0;
 		}
 	}
 	
 	@Override
 	public Move stateMachineSelectMove(long timeout)
 			throws TransitionDefinitionException, MoveDefinitionException,
 			GoalDefinitionException {
 		MinimaxThread mmt = new MinimaxThread(search_depth);
 	    Thread t = new Thread(mmt);
 	    t.start();
 	    try {
 			// Thread.sleep(timeout - System.currentTimeMillis());
 	    	Thread.sleep(10*1000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	    mmt.stop();
 		Move final_move = moves.get(this.getCurrentState());
 		float final_value = values.get(this.getCurrentState());
 		System.out.println("Final value: " + final_value);
 		if(final_move == null) {
 			System.err.println("Minimax: Failed to get valid move in time. Random play.");			
 			final_move = this.getStateMachine().getRandomMove(this.getCurrentState(), this.getRole());
 		}
 		return final_move;
 	}
     class MinimaxThread implements Runnable {
     	private int depth;
 		private boolean stop = false;
     	MinimaxThread(int depth){
     		this.depth = depth;
     	}
 		@Override
 		public void run() {		
 			stop = false;
 			int cur_depth = depth;
 			try {
 				while(!stop){
					System.out.println("looking: " + cur_depth);
 					findMinMax(cur_depth);
 					cur_depth+=1;
 				}
 			} catch (GoalDefinitionException e) {
 				e.printStackTrace();
 			} catch (MoveDefinitionException e) {
 				e.printStackTrace();
 			} catch (TransitionDefinitionException e) {
 				e.printStackTrace();
 			}
 		}
 		public void stop(){
 			stop = true;
 		}
 
     }
 	@Override
 	public void stateMachineStop() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void stateMachineAbort() {
 		// TODO Auto-generated method stub
 
 	}
 	@Override
 	public String getName() {
 		return "AlphaBeta";
 	}
 }
