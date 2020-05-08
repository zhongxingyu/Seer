 package shef.strategies.uct;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 
 import player.gamer.statemachine.reflex.event.ReflexMoveSelectionEvent;
 
 import shef.strategies.BaseGamer;
 import shef.strategies.uct.tree.StateActionPair;
 import shef.strategies.uct.tree.StateModel;
 import shef.strategies.uct.tree.UCTTree;
 import util.statemachine.MachineState;
 import util.statemachine.Move;
 import util.statemachine.exceptions.GoalDefinitionException;
 import util.statemachine.exceptions.MoveDefinitionException;
 import util.statemachine.exceptions.TransitionDefinitionException;
 
 /**
  * Specific Base Gamer which uses a UCT search
  * @author jonathan
  */
 public abstract class BaseGamerUCT extends BaseGamer implements IUCTStrategy{
 	
 	/** UCT tree */
 	protected UCTTree tree;
	private final int rollNum = 500;
 	
 	@Override
 	public void stateMachineMetaGame(final long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
 		final long finishBy = timeout - 1000;
 		
 		// initial setup of player names etc.
 		super.stateMachineMetaGame(timeout);
 		
 		// create an initial UCT tree
 		try {
 			tree = new UCTTree(getCurrentState(), this, roleCount);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// implementation specific setup
 		strategyMetaSetup(finishBy);
 		
 		// begin rollouts with time left
 		final StateModel currentSM = tree.getStateLists().get(moveCount).states.get(getStateMachine().getInitialState());
 		
 		int rollCount = 0;
 		System.out.println("beginning rollouts");
 //		while (System.currentTimeMillis() < finishBy) {
 		while (rollCount < rollNum) {
 			inTreeRollout(currentSM);
 			rollCount++;
 		}
 
 		System.out.println(rollCount + " initial");
 	}
 	
 	/**
 	 * Perform any extra setup needed in the time remaining
 	 * @param timeout
 	 */
 	public abstract void strategyMetaSetup(final long timeout);
 	
 	/**
 	 * As many times as possible in the time available perform rollouts from the
 	 * current state
 	 * 
 	 * @param timeout
 	 *            when in ms this move selection should be completed by
 	 * @return the move attributed to the most promising {@link StateActionPair}
 	 */
 	@Override
 	public Move stateMachineSelectMove(final long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
 		System.out.println("GO");
 		final long start = System.currentTimeMillis();
 		final long finishBy = timeout - 1000;
 		final MachineState cState = getCurrentState();
 		final StateModel currentSM = tree.getStateLists().get(moveCount).states.get(cState);
 		final List<Move> moves = theMachine.getLegalMoves(cState, myRole);
 		
 		Move selection = moves.get(0);
 		int rollCount = 0;
 
 		while (true) {
 //			if (System.currentTimeMillis() > finishBy ) {
 			if (rollCount >= rollNum) {
 				// select best move!
 				double maxVal = Float.NEGATIVE_INFINITY;
 				List<Move> maxMove = null;
 				HashMap<List<Move>, StateActionPair> saps = currentSM.actionsPairs;
 				for (Entry<List<Move>, StateActionPair> sap : saps.entrySet()) {
 					System.out.println("Move " + sap.getKey() + " explored " + sap.getValue().timesExplored + " " + Arrays.toString(sap.getValue().VALUE));
 					double v = sap.getValue().VALUE[myRoleID];
 					if (v > maxVal || maxMove == null) {
 						maxMove = sap.getKey();
 						maxVal = v;
 					}
 				}
 				selection = maxMove.get(myRoleID);
 				break;
 			}
 
 			inTreeRollout(currentSM);
 			rollCount++;
 
 		}
 		final long stop = System.currentTimeMillis();
 		moveCount++;
 //		 StringBuilder sb = new StringBuilder();
 //		 tree.print(sb);
 //		 System.out.println(sb.toString());
 		notifyObservers(new ReflexMoveSelectionEvent(moves, selection, stop - start));
 		System.out.println(rollCount + " " + selection);
 		return selection;
 	}
 	
 	
 	public void inTreeRollout(final StateModel rolloutRootSM) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
 		StateModel traverser = rolloutRootSM;
 		
 		// get all of the actions which can be performed from this move 
 		ArrayList<StateActionPair> actions = new ArrayList<StateActionPair>(traverser.actionsPairs.values());
 		ArrayList<StateActionPair> backupSAPs = new ArrayList<StateActionPair>();
 		ArrayList<StateModel> backupStates = new ArrayList<StateModel>();
 
 		boolean expandLeaf = true;
 
 		while (!actions.isEmpty()) {
 			List<Move> toPlay = new ArrayList<Move>();
 			
 			// for each player discover the best move
 			for (int p = 0; p < roleCount; p++) {
 				expandLeaf = true;
 				int i = 0;
 				float[] v = new float[actions.size()];
 				for (StateActionPair sap : actions) {
 					if (sap.timesExplored == 0) {
 						v[i] = Float.POSITIVE_INFINITY;
 						expandLeaf = false;
 					} else {
 						float uctBonus = (float) Math.sqrt(Math.log(traverser.timesExplored) / (float) sap.timesExplored);
 						v[i] = (float) (sap.VALUE[p] + UCT_NOVELTY_C * uctBonus);
 					}
 					i++;
 				}
 
 				// index of highest valued node
 				int index = 0;
 				float lowest = Integer.MIN_VALUE;
 				for (int j = 0; j < v.length; j++) {
 					if (v[j] > lowest) {
 						index = j;
 						lowest = v[j];
 					}
 				}
 				toPlay.add(actions.get(index).ACTION.get(p));
 			}
 			backupStates.add(traverser);
 
 			StateActionPair chosenSAP = traverser.actionsPairs.get(toPlay);
 			backupSAPs.add(chosenSAP);
 			traverser = chosenSAP.RESULT;
 			actions = new ArrayList<StateActionPair>(traverser.actionsPairs.values());
 
 		}
 		
 		// include the last state visited
 		backupStates.add(traverser);
 		
 		if (expandLeaf && !theMachine.isTerminal(traverser.state)) {
 			List<List<Move>> newStateActionPairs = tree.expandNode(traverser);
 			List<Move> nextState = horizonStatePair(newStateActionPairs, traverser.state);
 			// add the horizon chosen action
 			StateActionPair nextAction = traverser.actionsPairs.get(nextState);
 			traverser = nextAction.RESULT;
 			backupStates.add(traverser);
 			backupSAPs.add(nextAction);
 		}
 		
 		MachineState terminal;
 		if (!theMachine.isTerminal(traverser.state)) {
 			// complete the rollouts past this UCT horizon
 			terminal = outOfTreeRollout(traverser.state);
 		} else {
 			terminal = traverser.state;
 		}
 		
 		List<Double> outcome =  theMachine.getDoubleGoals(terminal);
 		
 		// distribute goal to each player
 		backpropogate(backupSAPs, backupStates, outcome);
 	}
 	
 	/**
 	 * Discount factor applied to each backup of the reward. The reward should
 	 * have a great effect on the states close to it and less to those further
 	 * away.
 	 */
 	private static final double discountFactor = 0.95;
 
 	/**
 	 * Update every state visited in this path and update its average. Applying
 	 * a discount factor to the result at every stage.
 	 * 
 	 * @param backupStatesPairs the state pairs visited
 	 * @param backupStates the states visited
 	 * @param outcome the resulting reqard from the terminal state reach on rollout
 	 */
 	public void backpropogate(final List<StateActionPair> backupStatesPairs, final List<StateModel> backupStates, List<Double> outcome) {
 		for (StateModel m : backupStates) {
 			m.timesExplored++;
 		}
 		
 		// the last entry recieves highest weight of credit
 		Collections.reverse(backupStatesPairs);
 		for(StateActionPair sap : backupStatesPairs){
 			sap.updateAverage(outcome);
 			
 			// degrade reward to prefer earlier wins
 			for (int i = 0; i < roleCount; i++) {
 				outcome.set(i, outcome.get(i) * discountFactor);
 			}
 		}
 	}
 }
