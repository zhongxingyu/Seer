 package player.gamer.statemachine;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import util.gdl.grammar.GdlSentence;
 import util.statemachine.MachineState;
 import util.statemachine.Move;
 import util.statemachine.Role;
 import util.statemachine.exceptions.GoalDefinitionException;
 import util.statemachine.exceptions.MoveDefinitionException;
 import util.statemachine.exceptions.TransitionDefinitionException;
 
 public class TheDominator extends HeuristicGamer {
 	private static int LOOK_AHEAD = 1;
 	private static int NUM_LEAVES_GOAL = 10;
 	private static int META_MC_FRACTION = 3;
 	private static double alpha = .3; //learning rate
 	private static double gamma = .9; //discount rate
 	
 	private HashMap< MachineState, List<Double> > _featureCache;	
 	private List<Double > weights;
 	private HashMap< MachineState, Double> _leaves;
 	
 	@Override
 	public void heuristicMetagame(long timeout) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
 		//build game tree
 		//stores the tree
 		HashMap< MachineState,HashMap< List<Move>, MachineState> > stateCache = new HashMap<MachineState, HashMap< List<Move>, MachineState> >();
 		_leaves = new HashMap< MachineState, Double>();
 		List<MachineState > fringe = new ArrayList<MachineState >();
 		fringe.add(getStateMachine().getInitialState());
 		//goes until the size of fringe + found terminals exceeds our goal
 		for(;(fringe.size() + _leaves.size() < NUM_LEAVES_GOAL)||fringe.size() == 0;){//this for loop needs work
 			List<MachineState> oldFringe = new ArrayList<MachineState>(fringe);
 			fringe.clear();
 			for(MachineState currState : oldFringe){	
 				HashMap<List<Move>, MachineState> tempMap = new HashMap<List<Move>, MachineState>();
 				List<List<Move>> jointMoves = getStateMachine().getLegalJointMoves(currState);
 				for(List<Move > jm : jointMoves){
 					MachineState nextState = getStateMachine().getNextState(currState, jm);
 					tempMap.put(jm, nextState);
 					//all terminals are leaves
 					if(getStateMachine().isTerminal(nextState)){
 						_leaves.put(nextState, (double) getStateMachine().getGoal(nextState, getRole()));
 					}
 					//not a terminal, so it may not be a leaf
 					else fringe.add(nextState);				
 				}
 				stateCache.put(currState, tempMap);
 			}
 		}
 		//get monte carlo for fringe, then add to leaves
 		long leafTime = (timeout - System.currentTimeMillis()) / META_MC_FRACTION / fringe.size();
 		for(MachineState state : fringe){
 			System.out.println(state);
 			_leaves.put(state, MonteCarlo(state, System.currentTimeMillis() + leafTime));			
 			System.out.println(_leaves.get(state));
 		}
 		//train weights
 		_featureCache = new HashMap< MachineState, List<Double>>();
 		weights = new ArrayList<Double >(Arrays.asList(1.0, 1.0));
 		while(System.currentTimeMillis() < timeout){
 			learnWeights(getStateMachine().getInitialState(), 0);			
 		}		
 		System.out.println(weights);
 		return;
 	}
 	
 	/*
 	featureVector = self.getFeatures(gameState, action)
 	correction = (reward + self.gamma * self.getValue(nextGameState)) - self.getQValue(gameState, action)
 	       
 	for feat in featureVector:
 	   self.weights[feat] += self.alpha * (correction) * featureVector[feat]
 	*/
 	private double learnWeights(MachineState state, double expectedVal) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
 		//saved for later
 		List<Double > features = getFeatures(state);
 		MachineState bestState = null;
 		boolean first = true;
 		double bestVal = 0;
 		for(Move move : getStateMachine().getLegalMoves(state, getRole())){
 			//playing against random opponent
 			MachineState nextState = getStateMachine().getNextState(state, getStateMachine().getRandomJointMove(state, getRole(), move));
 			//evaluate the next state
 			double nextVal = getStateValue(weights, getFeatures(nextState));
 			if(nextVal >= bestVal){
 				bestVal = nextVal;
 				bestState = nextState;
 			}
 			if(first){
 				bestVal = nextVal;
 				bestState = nextState;
 				first = false;
 			}
 		}
 		//learning time!
 		double reward;
 		double correction;
 		if(_leaves.containsKey(bestState)){
 			return _leaves.get(bestState);
 			/*
 			correction = (reward + gamma * bestVal) - expectedVal;
 			for(int i = 0; i < weights.size(); i++){
 				weights.set(i, weights.get(i) + alpha * correction * features.get(i)); 
 			}
 			*/
 		}
 		else{
 			reward = learnWeights(bestState, bestVal);
 			correction = (reward + gamma * bestVal) - expectedVal;
 			for(int i = 0; i < weights.size(); i++){
 				weights.set(i, weights.get(i) + alpha * correction * features.get(i)); 
 			}
 			
 			return 0;			
 		}
 	}
 	//just dots the weights with the features
 	private double getStateValue(List<Double> weights, List<Double> features){
 		double value = 0;
 		for(int i=0; i < weights.size();i++){
 			value += weights.get(i) * features.get(i);
 		}
 		return value;
 	}
 	//fetches features from cache if possible, if not adds to the cache
 	private List<Double> getFeatures(MachineState state) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
 		List<Double > features;
 		if(_featureCache.containsKey(state)) features = _featureCache.get(state);
 		else{
 			features = mobilityHeuristics(state);
 			_featureCache.put(state, features);
 		}
 		return features;
 	}
 	
 	@Override
 	public double getHeuristic(MachineState state, long timeout)
 			throws TransitionDefinitionException, MoveDefinitionException,
 			GoalDefinitionException {
 		List<Double > features = getFeatures(state);
 		//if(features.get(2) == 0) return 0;
 		//if(features.get(2) == 100) return 1000000;
 		double value = 0;
 		for(int i = 0; i < weights.size(); i++){
 			value += weights.get(i) * features.get(i);
 		}		
 		return value;
 	}
 	/*
 	 * returns a list in this order:
 	 * array[0] = playermobility
 	 * array[1] = playerfocus
 	 * array[2] = oppmobility
 	 * array[3] = oppfocus
 	 */
 	private List<Double > mobilityHeuristics(MachineState state) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
 		
 		HashSet<MachineState > reachable = new HashSet<MachineState >();
 		//first, add this state
 		reachable.add(state);
 		//next, expand it, to see how many states are reachable after that
 		for(int i = 0; i < LOOK_AHEAD; i++){
 			if(System.currentTimeMillis() > _stopTime) break;
 			HashSet<MachineState > oldReachable = new HashSet<MachineState>(reachable);
 			reachable = new HashSet<MachineState >();
 			//expand each state in oldReachable
 			for(MachineState currState : oldReachable){
 				if(!getStateMachine().isTerminal(currState))
 					reachable.addAll(getStateMachine().getNextStates(currState));
 			}
 		}
 		double numOppMoves = 0;
 		double numPlayerMoves = 0;
 		double goalPoints = -1;
 		for(MachineState currState : reachable){
 			if(!getStateMachine().isTerminal(currState)){
 				numPlayerMoves += getStateMachine().getLegalMoves(currState, getRole()).size();
 				List<Role > roles = getStateMachine().getRoles();
 				for(Role role : roles){
 					if(role != getRole())
 						numOppMoves += getStateMachine().getLegalMoves(currState, role).size();
 				}
 			}
 			else goalPoints = getStateMachine().getGoal(currState, getRole());
 			//else goalPoints = 	getRelGoal(state);
 		}
 		double stateSize = state.getContents().size();
 		double trueCount = 0;
 		double intCount = 0;
 		for(GdlSentence s : state.getContents()){
 			if(s.toString().contains("true")) trueCount+=1;	
 		}
 		List<Double > heurs = new ArrayList<Double >();
 		heurs.addAll(Arrays.asList(numPlayerMoves/numOppMoves, goalPoints / 100 / numPlayerMoves));//, stateSize, trueCount));
 		/*
 		//normalize the heuristics
 		double heursMax = 0;
 		for(double val : heurs){
 			if(val > heursMax) heursMax = val;
 		}
 		for(int i = 0; i < heurs.size(); i++){
 			heurs.set(i, heurs.get(i)/heursMax);
 		}
 		*/
 		return heurs;
 	}	
 	
 	private int _count = 0;
 
 	@Override
 	public double getHeuristicPOST(MachineState state, long timeout)
 			throws TransitionDefinitionException, MoveDefinitionException,
 			GoalDefinitionException {
 		double MC_value = MonteCarlo(state,timeout); 
 		return MC_value;
 		//return 0;
 	}
 
 	private double MonteCarlo(MachineState state, long timeout) throws GoalDefinitionException, TransitionDefinitionException, MoveDefinitionException {
 		List<Double> scores = new ArrayList<Double>();
 		while(true){
 			MachineState tempState = state;
 
 
 			while(true){				
 				if(getStateMachine().isTerminal(tempState)){
 					int tempscore = getStateMachine().getGoal(tempState, getRole());
 					/*
 					double tempscore =  getRelGoal(tempState);
 					scores.add(tempscore);
 					*/
 					break;
 				}
 				tempState = getStateMachine().getNextStateDestructively(tempState, getStateMachine().getRandomJointMove(tempState));
 				//tempState = getStateMachine().getRandomNextState(tempState);
 			}
 			if(System.currentTimeMillis() >= timeout) return average_score(scores);
 
 		}
 	}
 
 	private Double average_score(List<Double> score) {
 		if(score.isEmpty()) {
 			System.out.println("no time!!");
 			return 0.0;
 		}
 		double sum = 0;
 		for(int i = 0; i<score.size(); i++){
 			sum+= score.get(i);
 		}
 		_count++;
 		System.out.print("count: "+_count);
 		System.out.println(" scorsize: " + score.size());
 		return sum/score.size();
 	}
 	
 	@Override
 	public String getName() {
 		return "Wacky: The Dominator Combinator";
 	}
 }
