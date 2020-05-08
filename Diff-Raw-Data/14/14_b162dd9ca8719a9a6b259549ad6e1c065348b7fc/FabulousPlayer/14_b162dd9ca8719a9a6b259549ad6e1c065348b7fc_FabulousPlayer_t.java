 package org.ggp.base.player.gamer.statemachine.sample;
 
 import org.ggp.base.player.gamer.exception.MetaGamingException;
 import org.ggp.base.util.statemachine.Move;
 import org.ggp.base.util.statemachine.StateMachine;
 import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
 import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
 import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
 
 /**
  * The fabulous player.
  * 
 * @author Nera, Nicolai, Irme
  *
  */
 public final class FabulousPlayer extends SampleGamer {
 	
 	private final SampleGamer singlePlayer = new FabulousSinglePlayer2();
 	
 	private final SampleGamer multiPlayer = new FabulousMultiPlayer2();
	
 	private final SampleGamer montecarlo = new FabulousMonteCarlo();
 	
 	private SampleGamer currentPlayer;
 	
 	@Override
 	public void stateMachineMetaGame(long timeout){
 		long total = System.currentTimeMillis();
 		System.out.println();
 		StateMachine m = getStateMachine();
 		int roles = m.getRoles().size();
 		if(roles == 1){
 			currentPlayer = montecarlo;
 		}
 		else{
 			currentPlayer = montecarlo;
 		}
 		currentPlayer.setMatch(this.getMatch());
 		currentPlayer.setRoleName(this.getRoleName());
 		currentPlayer.setState(m.getInitialState());
 		try {
 			currentPlayer.metaGame(timeout);
 		} catch (MetaGamingException e) {
 			System.err.println("Metagaming failed!");
 		}
 		total = System.currentTimeMillis() - total;
 		System.out.println("Completed metagaming in " + total + "ms.");
 	}
 	
 	@Override
 	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
 		long total = System.currentTimeMillis();
 		currentPlayer.setState(getCurrentState());
 		Move move = currentPlayer.stateMachineSelectMove(timeout);
 		total = System.currentTimeMillis() - total;
 		System.out.println("Selected move in " + total + "ms.");
 		return move;
 	}
 	
 }
