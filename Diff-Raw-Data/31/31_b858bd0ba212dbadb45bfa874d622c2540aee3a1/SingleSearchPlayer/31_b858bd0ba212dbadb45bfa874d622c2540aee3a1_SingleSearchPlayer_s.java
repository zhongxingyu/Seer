 package players;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.ggp.base.util.statemachine.MachineState;
 import org.ggp.base.util.statemachine.Move;
 import org.ggp.base.util.statemachine.Role;
 import org.ggp.base.util.statemachine.StateMachine;
 import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
 import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
 import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
 
 public class SingleSearchPlayer extends Subplayer{
 	public SingleSearchPlayer(StateMachine stateMachine, Role role,
 			PlayerResult playerResult, MachineState currentState) {
 		super(stateMachine, role, playerResult, currentState);
		// TODO Auto-generated constructor stub
 	}
 
	/* The maximum number of levels to which this player will descend looking for moves */
	private int max_depth;
	private PlayerResult playerResult;

 	@Override
 	public void run() {
 		List<Move> moves;
 		List<List<Move>> turnsToProcess = new ArrayList<List<Move>>();
 		try {
 			moves = stateMachine.getLegalMoves(currentState, role);
 			/* Default Random Move in case of time-out, etc */
			playerResult.setBestMoveSoFar(moves.get(new Random().nextInt(moves.size())));
 			MachineState defaultNextState = stateMachine.getRandomNextState(currentState, role, playerResult.getBestMoveSoFar());
 			if(stateMachine.isTerminal(defaultNextState)){
 				Integer defaultScore = stateMachine.getGoal(defaultNextState,role);
 				playerResult.setBestMoveScore(defaultScore);
 			} else{
 				playerResult.setBestMoveScore(0);
 			}
 
 			/* Adds the first turn's worth of moves to consider */
 			turnsToProcess.add(moves);
 
 			/* Searches to see if it can win and if so wins */
 			while(true){
 				if(turnsToProcess.isEmpty()) break;
 				if(Thread.currentThread().isInterrupted()) break;
 				List<Move> currentMoves = turnsToProcess.remove(0);
 				for(Move moveUnderConsideration: currentMoves){
 					MachineState nextState = stateMachine.getRandomNextState(currentState,role,moveUnderConsideration);
 					/* If the move allows this player to win, take it */
 					if(stateMachine.isTerminal(nextState)){
 						Integer myScore = stateMachine.getGoal(nextState,role);
 						if(myScore>playerResult.getBestMoveScore()){
 							playerResult.setBestMoveSoFar(moveUnderConsideration);
 							playerResult.setBestMoveScore(myScore);
 						}
 					}
 				}
 			}
 		} catch (MoveDefinitionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransitionDefinitionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (GoalDefinitionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}		
 }
