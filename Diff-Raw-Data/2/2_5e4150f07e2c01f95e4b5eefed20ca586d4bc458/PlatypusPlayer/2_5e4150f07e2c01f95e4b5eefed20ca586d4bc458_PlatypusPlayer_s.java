 package org.ggp.base.player.gamer.statemachine.PlatypusPlayer;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.ggp.base.apps.player.detail.DetailPanel;
 import org.ggp.base.apps.player.detail.SimpleDetailPanel;
 import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
 import org.ggp.base.player.gamer.exception.GameAnalysisException;
 import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
 import org.ggp.base.util.game.Game;
 import org.ggp.base.util.statemachine.MachineState;
 import org.ggp.base.util.statemachine.Move;
 import org.ggp.base.util.statemachine.Role;
 import org.ggp.base.util.statemachine.StateMachine;
 import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
 import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
 import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
 import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
 
 import players.PlayerResult;
 import players.SingleSearchPlayer;
 
 
 public class PlatypusPlayer extends StateMachineGamer{
 
	private static final String PLAYER_NAME = "First Player";
 
 	private List<Move> optimalSequence = null;
 
 	@Override
 	public StateMachine getInitialStateMachine() {
 		// TODO Auto-generated method stub
 		return new ProverStateMachine();
 	}
 
 	@Override
 	public void stateMachineMetaGame(long timeout)
 			throws TransitionDefinitionException, MoveDefinitionException,
 			GoalDefinitionException {
 		if(getStateMachine().getRoles().size()==1){
 			/* Single-player game, so try to brute force as much as possible */
 			optimalSequence = solveSinglePlayerGame(getStateMachine(),getCurrentState(), 0);
 		}
 	}
 
 	public List<Move> solveSinglePlayerGame(StateMachine theMachine, MachineState start, int depth) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException{
 		if(theMachine.isTerminal(start)) {
 			if(theMachine.getGoal(start,getRole())==100){
 				System.out.println("Solved!");
 				return new ArrayList<Move>();
 			} else{
 				/* No optimal state found */
 				return null;
 			}
 		}
 		List<Move> moves = theMachine.getLegalMoves(start, getRole());
 		List<Move> bestMoves = null;
 		for(Move moveUnderConsideration: moves){
 			List<Move> partialBest = solveSinglePlayerGame(theMachine, theMachine.getRandomNextState(start, getRole(), moveUnderConsideration), depth+1);
 			if(partialBest!=null){
 				partialBest.add(moveUnderConsideration);
 				bestMoves = partialBest;
 				break;
 			}
 		}
 		return bestMoves;
 	}
 
 
 	@Override
 	public Move stateMachineSelectMove(long timeout)
 			throws TransitionDefinitionException, MoveDefinitionException,
 			GoalDefinitionException {
 		long start = System.currentTimeMillis();
 		List<Move> moves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
 		if(getStateMachine().getRoles().size()==1){
 			/* Single-player game */
 			if(optimalSequence!=null){
 				/* Best move is the first move in the sequence */
 				Move bestMove = optimalSequence.remove(optimalSequence.size()-1);
 				long stop = System.currentTimeMillis();
 				notifyObservers(new GamerSelectedMoveEvent(moves, bestMove, stop - start));
 				return bestMove;
 			}
 
 		}
 
 		
 		PlayerResult singleSearchPlayerResult = new PlayerResult();
 		Thread singleSearchPlayer = new Thread(new SingleSearchPlayer(getStateMachine(), getRole(), singleSearchPlayerResult,getCurrentState()));
 
 		singleSearchPlayer.start();
 		try {
 			/* Sleep for 2 seconds less than the maximum time allowed */
 			Thread.sleep(timeout-start-2000);
 		} catch (InterruptedException e) {
 			//e.printStackTrace();
 		}
 		/* Tell the thread searching for the best move it is done so it can exit */
 		singleSearchPlayer.interrupt();
 		Move bestMove = singleSearchPlayerResult.getBestMoveSoFar();
 		long stop = System.currentTimeMillis();
 
 		notifyObservers(new GamerSelectedMoveEvent(moves, bestMove, stop - start));
 		return bestMove;
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
 	public void analyze(Game g, long timeout) throws GameAnalysisException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public String getName() {
 		// TODO Auto-generated method stub
 		return PLAYER_NAME;
 	}
 
 	@Override
 	public DetailPanel getDetailPanel(){
 		return new SimpleDetailPanel();
 	}
 
 }
