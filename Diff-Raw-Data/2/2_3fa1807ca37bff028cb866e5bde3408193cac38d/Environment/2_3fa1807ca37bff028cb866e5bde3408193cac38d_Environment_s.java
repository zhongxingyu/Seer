 package environment;
 
 import actor.Predator;
 import actor.Prey;
 import policy.EpsilonGreedyPolicy;
 import policy.LearnedPolicy;
 import policy.Policy;
 import state.State;
 import statespace.StateSpace;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Set;
 
 public class Environment {	
 	private StateSpace stateSpace;
 	private Prey prey;
 	private Predator predator;
 
 	private static final int MAX_ITERATIONS = 1000;
 	private final double THETA = 0.00001; // threshold for the loop end condition
 	private final double GAMMA = 0.8;
 	
 	public enum action { NORTH, SOUTH, EAST, WEST, WAIT;
 		
 		public action getOpposite() {
 			switch(this) {
 			case WAIT:
 				return action.WAIT;
 			case EAST:
 				return action.WEST;
 			case WEST:
 				return action.EAST;
 			case NORTH:
 				return action.SOUTH;
 			case SOUTH:
 				return action.NORTH;
 			}
 			return action.WAIT;
 		}
 	};
 	
 	public Environment(StateSpace givenStateSpace) {
 		this.stateSpace = givenStateSpace;
 		this.predator = new Predator(new Coordinates(0,0), stateSpace);
 		this.prey = new Prey(new Coordinates(5,5), stateSpace);
 	}
 	
 	public void simulate(int episodeCount) {
 		int episodes = episodeCount;
 		do {
 			// initialize Episode
 			this.predator.setCoordinates(new Coordinates(0, 0));
 			this.prey.setCoordinates(new Coordinates(5, 5));
 			this.prey.setAlive(true);
 			State initialState = this.stateSpace.getState(this.prey.getCoordinates(), this.predator.getCoordinates());
 			
 			int rounds = 0;
 			while (!isEpisodeOver()) { // run episode
 				initialState = this.nextRound(initialState);
 				rounds++;
 			}
 			//REPORT
 			System.out.println("[simulate()] rounds: " + rounds);
 			episodes--;
 		} while (episodes > 0);
 	}
 
 	public boolean isEpisodeOver() { return !this.prey.getAlive(); }
 	
 	public State nextRound(State s) {
 		State currentState = s;
 		this.predator.move(currentState);
 		
 		// update currentState.
 		currentState = this.stateSpace.getState(this.prey.getCoordinates(), this.predator.getCoordinates());
 		this.collisionDetection();
 		
 		this.prey.move(currentState);
 		this.collisionDetection();
 		return this.stateSpace.getState(this.prey.getCoordinates(), this.predator.getCoordinates());
 	}
 	
 	private void collisionDetection() {
 		if (predator.getCoordinates().equals(prey.getCoordinates())) { prey.setAlive(false); }
 	}
 
 	/**
 	 * Task 1.2
 	 * For extensive explanation see : http://webdocs.cs.ualberta.ca/~sutton/book/ebook/node41.html
      */
 	public void policyEvaluation(/*Policy policy, */) {
 		double delta = 0.0; // defines the maximum difference observed in the stateValue of all states
 		int sweeps = 0;
 		
 		Policy policy = this.predator.getPolicy();
 		
 		do { // Repeat
 			delta = 0.0;
 			Iterator<State> stateSpaceIt = this.stateSpace.iterator();
 			while(stateSpaceIt.hasNext()) {
 				State currState = stateSpace.next(); // for s in S+
 				double V_s = 0.0;
 				double v = currState.getStateValue();
 
 				for (action ac : Environment.action.values()) { // summation over a
 					double pi = policy.getActionProbability(currState, ac);
 					ProbableTransitions probableTransitions = stateSpace.getProbableTransitions(currState, ac);
 					Set<State> neighbours = probableTransitions.getStates();
 					double sum = 0.0;
 					for(State s_prime : neighbours){ // summation over s'
 						double p = probableTransitions.getProbability(s_prime);
 						sum += p * (s_prime.getStateReward() + GAMMA * s_prime.getStateValue());
 					}
 					V_s += pi * sum;
 				}
 				currState.setStateValue(V_s);
 				delta = Math.max(Math.abs(V_s - v), delta);
 			}
 			sweeps++;
 		} while (delta > THETA);
 
 		// REPORT
 		Coordinates preyCoordinates = new Coordinates(0,0);
 		Coordinates predatorCoordinates = new Coordinates(0,0);
 		
 		System.out.println(this.stateSpace.getState(preyCoordinates, predatorCoordinates));
 		System.out.println("[policyEvaluation()] Sweeps: " + sweeps);
 		// /REPORT
 	}
 
     /**
      * Task 1.3
      */
 	public boolean policyImprovement(/*Policy policy*/) {
 		boolean policyStable = true;
 		Policy policy = this.predator.getPolicy();
 		for (State s : this.stateSpace) {
 			action b = policy.getAction(s);
 			
 			double max = 0.0;
 			action argmax_a = null;
 			for (action a : Environment.action.values()) {
 				double sum = 0.0;
 				for (State s_prime : this.stateSpace.getNeighbors(s)) {
 					double p;
 					if (this.stateSpace.getTransitionAction(s, s_prime) == a) { p = 1.0; }
 					else { p = 0.0; }
 					// P^(pi(s))_ss' has only two possible values: 1 if the action will lead to s', 0 otherwise
 //					// ac: the action that would be required to move to state st
 					double r = stateSpace.getActionReward(s, a);
 					sum += p * (r + GAMMA * s_prime.getStateValue());
 				}
 				if(sum > max) {
 					argmax_a = a;
 					max = sum;
 				}
 			}
 			policy.setUniqueAction(s, argmax_a);			
 			if(argmax_a != b) { policyStable = false; }
 		}
 		return policyStable;
 	}
 
     /**
      * Task 1.3
      */
     public void policyIteration(/*Policy policy*/) {
         int debugIterations = 0;
         Policy policy = this.predator.getPolicy();
         this.stateSpace.initializeStateValues(0.0);
 
         do {
             this.policyEvaluation();
             this.stateSpace.printActions(policy);
             debugIterations++;
         } while (!this.policyImprovement());
         System.out.println("[policyIteration()] Number of Iterations : " + debugIterations);
     }
 
 	public ValueIterationResult valueIteration(double local_gamma) {
 		double delta;
 		
 		// initialization
 		this.stateSpace.initializeStateValues(0.0);
 		int numIterations = 0;
 		
 		// calculation of the V(s)
 		do {
 			numIterations++;
 			delta = 0.0;
 			
 			// for each s in S
 			Iterator<State> stateSpaceIt = this.stateSpace.iterator();
 			while(stateSpaceIt.hasNext()) {
                 State s = stateSpaceIt.next();
 				double v = s.getStateValue();
 				double max = 0.0;
 				for (action a: Environment.action.values()) { // max over a
 					ProbableTransitions probableTransitions = stateSpace.getProbableTransitions(s, a);
 					Set<State> neighbours = probableTransitions.getStates();
 					double sum = 0.0;
 					for(State s_prime : neighbours){ // summation over s'
 						double p = probableTransitions.getProbability(s_prime);
 						sum += p * (s_prime.getStateReward() + local_gamma * s_prime.getStateValue());
 					}
 					max = Math.max(max, sum);
 				}
 				double V_s = max;
 				s.setStateValue(V_s);
 				delta = Math.max(delta, Math.abs(s.getStateValue() - v));
 			}
 			if(numIterations >= this.MAX_ITERATIONS) { break; }
 		} while(delta > this.THETA);
 
 		// production of the policy
 		LearnedPolicy pi = new LearnedPolicy();
 		Iterator<State> stateSpaceIt = this.stateSpace.iterator();
 		while(stateSpaceIt.hasNext()) {
             State s = stateSpaceIt.next();
 			action argmax_a = action.WAIT;
 			double max = 0.0;
 			for (action a: Environment.action.values()) { // argmax over a
 				ProbableTransitions probableTransitions = stateSpace.getProbableTransitions(s, a);
 				Set<State> neighbours = probableTransitions.getStates();
 				double sum = 0.0;
 				for(State s_prime : neighbours){ // summation over s'
 					double p = probableTransitions.getProbability(s_prime);
 					sum += p * (s_prime.getStateReward() + local_gamma * s_prime.getStateValue());
 				}
 				if(sum > max) {
 					max = sum;
 					argmax_a = a;
 				}
 			}
 			pi.setUniqueAction(s, argmax_a);
 		}
 		// TODO: output State values somehow.
 		return new ValueIterationResult(numIterations,pi);
 	}
 	
 	/**
 	 * increases gamma with step 0.001. Outputs to results.csv. Used for plotting.
 	 */
 	public void valueIterationGammas() {
 		double gamma = 0.0;
 		int size = 1000;
 		ValueIterationResult[] iterations = new ValueIterationResult[size];
 		BufferedWriter br;
 		try {
 			br = new BufferedWriter(new FileWriter("results.csv"));
 			for(int i = 0; i < size-1; i++) {
 				gamma += 0.001;
 				System.out.println("gamma:"+gamma);
 				iterations[i] = this.valueIteration(gamma);
 				System.out.println("num iterations:"+iterations[i].getNumIterations());
 				String str = i+","+gamma+","+iterations[i].getNumIterations()+"\n";
 				System.out.println(str);
 				br.write(str);
 			} br.close();
 		} catch (IOException e) { e.printStackTrace(); }
 	}
 
 	/**
 	 * initializes all state-action pair value (Q) from a single value  
 	 * @param value
 	 * @return HashMap<StateAction,Double> q
 	 */
 	public Q initializeQ(double value) {
 		Q q = new Q();
 		for(State s : this.stateSpace){
 			for(action a : Environment.action.values()){
 				q.set(s, a, value);
 			}
 		}
 		return q;
 	}
 	
 
     /***********************************************************************************/
     public void Q_Learning(EpsilonGreedyPolicy pi) {
         // initialize Q(s,a) arbitrarily
     	Q q = this.initializeQ(15.0);
 
         for(State starting_s : this.stateSpace) { // repeat for each episode // initialize s
         	State s = starting_s;
             do { // repeat for each step of episode
                 this.predator.setCoordinates(s.getPredatorCoordinates());
                 this.prey.setCoordinates(s.getPreyCoordinates());
                 // Choose a from s using policy derived from Q (e-greedy)
             	action a =  pi.getAction(s);
 
                 s = this.stateSpace.getState(this.prey.getCoordinates(), this.predator.getCoordinates());
 
 
                 // Take action a. observe r, s'
             	this.predator.move(a);
                 this.prey.move(s);
                 
                 // update currState.
                 State s_prime = this.stateSpace.getState(this.prey.getCoordinates(), this.predator.getCoordinates());
 
                double q_sa = s.getStateReward();
                 double max_a_q = q.getMax(s_prime);
                 double r = s_prime.getStateReward();
                 double newQ_sa = q_sa + Util.alpha * (r + Util.gamma * max_a_q - q_sa);
 
                 q.set(s, a, newQ_sa);
             } while(!s.isTerminal()); // repeat until s is terminal
         }
     }
 }
