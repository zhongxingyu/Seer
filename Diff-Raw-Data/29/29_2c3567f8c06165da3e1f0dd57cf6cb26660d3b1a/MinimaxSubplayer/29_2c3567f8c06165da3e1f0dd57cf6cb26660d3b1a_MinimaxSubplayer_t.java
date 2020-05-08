 package players;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.ggp.base.util.statemachine.MachineState;
 import org.ggp.base.util.statemachine.Move;
 import org.ggp.base.util.statemachine.Role;
 import org.ggp.base.util.statemachine.StateMachine;
 import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
 import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
 import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
 
 public class MinimaxSubplayer extends Subplayer {
 
 	public MinimaxSubplayer(StateMachine stateMachine, Role role,
 			PlayerResult playerResult, MachineState currentState) {
 		super(stateMachine, role, playerResult, currentState);
 		// TODO Auto-generated constructor stub
 	}
 	
 	@Override
 	public void run() {
 		try {
 			List<Move> moves = stateMachine.getLegalMoves(currentState, role);
			double score = Integer.MIN_VALUE;
 			Move bestMoveSoFar = null;
 			for (Move move : moves) {
 				if (Thread.currentThread().isInterrupted()) return;
				double result = minscore(move, currentState);
 				System.out.println("MOVE: " + move + ", result: " + result);
 				if (result > score) {
 					score = result;
 					bestMoveSoFar = move;
 					playerResult.setBestMoveSoFar(bestMoveSoFar);
 					playerResult.setBestMoveScore(score);
 				}
 				//parentThread.interrupt();
 			}
 
 		} catch (MoveDefinitionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (GoalDefinitionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransitionDefinitionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	
 
	private double minscore(Move move, MachineState state) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException {
 		if(Thread.currentThread().isInterrupted()) return Integer.MAX_VALUE;
		double score = Double.MAX_VALUE;
 		List<List<Move>> jointMoves = stateMachine.getLegalJointMoves(state, role, move);
 		Collections.shuffle(jointMoves);
 		for (int i = 0; i < jointMoves.size(); i++) {
 			List<Move> jointMove = jointMoves.get(i);
 			MachineState newState = stateMachine.getNextState(state, jointMove);
			double result = maxscore(newState);
 			if (result < score) {
 				score = result;
 			}
 		}
 		//memoizedStatesMinValues.put(state,score);
 		return score;
 	}
 
 	//consider adding a depth parameter (int depth) to only search tree to a certain depth
 	// and add condition to base case if (depth == maxDepth)
	private double maxscore(MachineState state) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException {
 		if(Thread.currentThread().isInterrupted()) return Integer.MAX_VALUE;
 		if (stateMachine.isTerminal(state)) {
 			int goal = stateMachine.getGoal(state,role);
			playerResult.putMemoizedState(state, new Double(goal));
 			return goal;
 		}
 		if(playerResult.containsMemoizedState(state)){
 			//System.out.println("Memoized value!");
 			return playerResult.getMemoizedState(state);
 		}
 		List<Move> moves = stateMachine.getLegalMoves(state, role);
		double score = Integer.MIN_VALUE;
 		Collections.shuffle(moves);
 		for (Move move : moves) {
			double result = minscore(move, state);
 			if (result > score) {
 				score = result;
 			}
 		}
 		if(!Thread.currentThread().isInterrupted()){
			playerResult.putMemoizedState(state,new Double(score));
 		}
 		return score;
 	}
 
 }
