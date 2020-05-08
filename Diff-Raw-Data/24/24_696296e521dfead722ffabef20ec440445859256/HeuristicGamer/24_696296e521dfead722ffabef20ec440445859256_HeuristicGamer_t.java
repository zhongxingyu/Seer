 package player.gamer.statemachine;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import util.statemachine.MachineState;
 import util.statemachine.Move;
 import util.statemachine.Role;
 import util.statemachine.StateMachine;
 import util.statemachine.exceptions.GoalDefinitionException;
 import util.statemachine.exceptions.MoveDefinitionException;
 import util.statemachine.exceptions.TransitionDefinitionException;
 import util.statemachine.implementation.propnet.SamplePropNetStateMachine;
 import util.statemachine.implementation.prover.ProverStateMachine;
 
 public abstract class HeuristicGamer extends StateMachineGamer {
 	//defines the heuristic used to value a state
 	public abstract double getHeuristic(MachineState state, long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException;
 	public abstract List<Double> getHeuristicPOST(MachineState state, long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException;
     
 	//names the player
 	public abstract String getName();
     
 	private static final int MAX_SCORE = 100;
 	private static final int STATES_TO_EXPAND = 64;
 	private static final int CALCULATION_BUFFER = 1000;
     
 	protected long _stopTime;
 	private int _numStatesExpanded;
 	protected int _levelsToExpand;
     
 	public class Move_Node {
 		public double value;
 		public Move move;
 		public Move_Node(double value, Move move){
 			this.value = value;
 			this.move = move;
 		}
 	}
     
 	@Override
 	public StateMachine getInitialStateMachine() {	
		//return new ProverStateMachine();
		return new SamplePropNetStateMachine();
 				
 	}
     
 	@Override
 	public void stateMachineAbort() {}
     
 	@Override
 	public void stateMachineMetaGame(long timeout)
     throws TransitionDefinitionException, MoveDefinitionException,
     GoalDefinitionException {
 		/* ***************NOT DOING METAGAMING YET************************/
 		
 		
 		StateMachine pnet = getStateMachine();
 		//propNet.initialize(description)
 		int a = 4;
 		int b = a+1;
 		
 		for(int i = 0; i<a; i++){
 			b+=b;
 		}
 		
 		
 	}
     
 	@Override
 	public Move stateMachineSelectMove(long timeout)
     throws TransitionDefinitionException, MoveDefinitionException,
     GoalDefinitionException {
         
 		
 		
 		
 		_stopTime = timeout - CALCULATION_BUFFER;
 		_numStatesExpanded = 0;
 
 		long currTime = System.currentTimeMillis();
 		long timeAllocatedPRE = (timeout-currTime)/4; //DETERMINES THE TIME ALLOCATED FOR PRE STUFF
 		//System.out.println("Time ALlocated:" + timeAllocatedPRE);
 
 		List<Move> legalMoves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
 		_levelsToExpand = 2;
 		List<Double> moveScores = getMove(getCurrentState(), currTime + timeAllocatedPRE, legalMoves);
 		//System.out.println("Pre->Post");
 		_levelsToExpand = 0;
 		List<Double> moveScoresPOST = getHeuristicPOST(getCurrentState(), timeout); 
 				
 				//getMovePOST(getCurrentState(), timeout, legalMoves);
         
 		if(moveScores.isEmpty() && moveScoresPOST.isEmpty()){
             //	System.out.println("FAILED TO GET MOVE");
 			return getStateMachine().getRandomMove(getCurrentState(), getRole());
 		}
         
 		int indexOfBestScore = 0;
 		double bestscore = 0;
 		for(int i = 0; i<legalMoves.size(); i++) {
 			double score = 0;
 			if (moveScores.size() > i) {
 				score += (moveScores.get(i) * 0)/5;    //RANDOM RATIO FOR HEURISTICS VS MONTE
 			}
 			if (moveScoresPOST.size() > i) {
 				score += ((moveScoresPOST.get(i)) * 5)/5;  //THIS IS THE MONTE RATIO PART
 			}			
             //			if (moveScores.size() >= i-1) {
             //				score += (moveScores.get(i) * 2)/5;    //RANDOM RATIO FOR HEURISTICS VS MONTE
             //			}
             //			if (moveScoresPOST.size() >= i-1) {
             //				score += ((moveScoresPOST.get(i)+100) * 3)/5;  //THIS IS THE MONTE RATIO PART
             //			}
 			System.out.println("pre "+i+" : " + moveScores.get(i));
 			System.out.println("post "+i+" : " + moveScoresPOST.get(i));
 			System.out.println("score "+i+" : " + score);
 			if (score > bestscore) {
 				indexOfBestScore = i;
 				bestscore = score;
 			}
 		}
 
 
         
 		return legalMoves.get(indexOfBestScore);
 	}	
     
 	/*
 	 * Selects the move with the highest minimax value from the available moves
 	 * for this state.  This is essentially a copy of maxScore which tracks moves.
 	 */
 	private List<Double> getMove(MachineState currState, long timeout, List<Move> legalMoves) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
 		if(getStateMachine().isTerminal(currState)){
 			return new ArrayList<Double>();
 		}
 		List<Double> ScoreList = new ArrayList<Double>();
 		long curr_time = System.currentTimeMillis();
 		long time_step = (timeout-curr_time)/legalMoves.size();
         
 		//Put minScore for each legal move into array and return array
 		for(int i = 0; i<legalMoves.size(); i++){  //PREPROCESSING (IE NON-MONTE CARLO HEURISTICS)
 			Move move = legalMoves.get(i);
 			ScoreList.add(minScore(move, currState, 0, curr_time + (i+1)*time_step));
 		}
 		return ScoreList;
 	}
     
 	/*
 	 * Function to control the depth of expansion.  
 	 * Currently not very interesting.
 	 */
 	private boolean stopExpanding(MachineState state, int level, long timeout){
 		return (System.currentTimeMillis() >= timeout-CALCULATION_BUFFER 
 				|| _numStatesExpanded >= STATES_TO_EXPAND)
         || level > _levelsToExpand;
 	}
     
 	/*
 	 * Gets the max possible score for a given role at this state
 	 * Level is the number of times to allow recursion, at this level
 	 * we use the heuristic value of the state.
 	 */
 	private double maxScore(MachineState state, int level, long timeout) throws GoalDefinitionException, TransitionDefinitionException, MoveDefinitionException{
 		if(getStateMachine().isTerminal(state)){ //base case 
 			//System.out.println("terminal");
 			return getStateMachine().getGoal(state, getRole());
 		}
 		if(stopExpanding(state, level, timeout)) {
             //	System.out.println("heuristic");
 			return getHeuristic(state, timeout); 
 		}
 
 		List<Move> legalMoves = getStateMachine().getLegalMoves(state, getRole());
 		long curr_time = System.currentTimeMillis();
 		long time_step = (timeout-curr_time)/legalMoves.size();
         //	System.out.println("timestep_inmax: "+time_step);
         
 		double maxVal = 0;
 		//choose the move with the max value against a rational player
 		for(int i = 0; i<legalMoves.size(); i++){
 			Move move = legalMoves.get(i);
 			double currVal = minScore(move, state, level, curr_time + (i+1)*time_step);
 			if(System.currentTimeMillis() >= _stopTime) return getHeuristic(state, timeout);
 			if(currVal > maxVal) maxVal = currVal;			
 		}
 		return maxVal;
 	}
 
 	/*
 	 * Gets the minimum score a rational opponent would allow to be the value at
 	 * this state
 	 */
 	private double minScore(Move move, MachineState state, int level, long timeout) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
 		double minVal = MAX_SCORE;
 		//iterate over all possible joint moves, chose lowest scoring
 		List<List<Move>> jointMoves = getStateMachine().getLegalJointMoves(state, getRole(), move);
 		long curr_time = System.currentTimeMillis();
 		long time_step = (timeout-curr_time)/jointMoves.size();
 		//System.out.println("timestep_inmin: "+time_step);
         
 		for(int i = 0; i<jointMoves.size(); i++){
 			List<Move> jointMove = jointMoves.get(i);
 			_numStatesExpanded++;
 			double currVal = maxScore(getStateMachine().getNextState(state, jointMove), level+1, curr_time + (i+1)*time_step);
 			if(currVal < minVal) minVal = currVal;
 		}
 		return minVal;
 	}
     
 	/*
 	 * Selects the move with the highest minimax value from the available moves
 	 * for this state.  This is essentially a copy of maxScore which tracks moves.
 	 */
 	protected List<Double> getMovePOST(MachineState currState, long timeout, List<Move> legalMoves) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
 		if(getStateMachine().isTerminal(currState)){
 			return new ArrayList<Double>();
 		}
 		List<Double> ScoreList = new ArrayList<Double>();
 		long curr_time = System.currentTimeMillis();
 		long time_step = (timeout-curr_time)/legalMoves.size();
         
 		//Put minScore for each legal move into array and return array
 		for(int i = 0; i<legalMoves.size(); i++){  //POSTPROCESSING (IE MONTE CARLO HEURISTICS)
 			Move move = legalMoves.get(i);
 			ScoreList.add(minScorePOST(move, currState, 0, curr_time + (i+1)*time_step));
 		}
 		return ScoreList;
 	}
 
 	/*
 	 * Gets the max possible score for a given role at this state
 	 * Level is the number of times to allow recursion, at this level
 	 * we use the heuristic value of the state.
 	 */
 	private double maxScorePOST(MachineState state, int level, long timeout) throws GoalDefinitionException, TransitionDefinitionException, MoveDefinitionException{
 		if(getStateMachine().isTerminal(state)){ //base case 
             	//System.out.println("terminal");
 			double relGoal = getRelGoal(state);
 			if (relGoal<0) return -101;
 			if(relGoal>0) return 101;
 			return 0;
 					//getStateMachine().getGoal(state, getRole());
 		}
 		//if(stopExpanding(state, level, timeout)) {
 		if(level > _levelsToExpand || System.currentTimeMillis() > timeout){
 			//System.out.println("heuristic");
 			//return getHeuristicPOST(state, timeout); 
 			return 0;
 		}
         
 		List<Move> legalMoves = getStateMachine().getLegalMoves(state, getRole());
 		long curr_time = System.currentTimeMillis();
 		long time_step = (timeout-curr_time)/legalMoves.size();
 		//System.out.println("timestep_inmax: "+time_step);
 
 		boolean validscore = false;
 		double maxVal = -1000;
 		//choose the move with the max value against a rational player
 		for(int i = 0; i<legalMoves.size(); i++){
 			Move move = legalMoves.get(i);
 			double currVal = minScorePOST(move, state, level, curr_time + (i+1)*time_step);
             
 			if(System.currentTimeMillis() >= _stopTime) return 0; //TESSST TEST return getHeuristicPOST(state, timeout);
 
             
 			if(currVal > maxVal) {
 				maxVal = currVal;
 				validscore = true;
 			}
 			if(currVal == 101) return 101;
 		//	if(currVal==-2) continue;
 		}
 		if(validscore) return maxVal;
 		return 0;
 		//return maxVal;
 	}
 
 	/*
 	 * Gets the minimum score a rational opponent would allow to be the value at
 	 * this state
 	 */
 	private double minScorePOST(Move move, MachineState state, int level, long timeout) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
 		double minVal = MAX_SCORE+100;
 		boolean validscore = false;
 		//iterate over all possible joint moves, chose lowest scoring
 		List<List<Move>> jointMoves = getStateMachine().getLegalJointMoves(state, getRole(), move);
 		long curr_time = System.currentTimeMillis();
 		long time_step = (timeout-curr_time)/jointMoves.size();
 		//System.out.println("timestep_inmin: "+time_step);
         
 		for(int i = 0; i<jointMoves.size(); i++){
 			List<Move> jointMove = jointMoves.get(i);
 			_numStatesExpanded++;
 			double currVal = maxScorePOST(getStateMachine().getNextState(state, jointMove), level+1, curr_time + (i+1)*time_step);
 			//if(currVal==-2)continue;
 			if(currVal==-101)return -101;
 			if(currVal <= minVal) {
 				validscore = true;
 				minVal = currVal;
 			}
 		}
 		if(validscore) return minVal;
 		return 0;
 		//return minVal;
 	}
     
 	@Override
 	public void stateMachineStop() {
 	}
 	
 	
 	
 
 	public double getRelGoal(MachineState state) throws GoalDefinitionException{
 		double mygoal = getStateMachine().getGoal(state, getRole());
 		//return mygoal;
 
 		
 		List<Integer> allgoals = getStateMachine().getGoals(state);
 		double average=0;
 		for(int i=0; i<allgoals.size(); i++){
 			average+=allgoals.get(i);
 		}
 		average /= allgoals.size();
 		return mygoal-average;
 
 //		return _cacheHit;
 		 
 	}
 }
