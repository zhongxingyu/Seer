 package environment;
 
 import actor.Predator;
 import actor.Prey;
 import action.StateAction;
 import policy.EpsilonGreedyPolicy;
 import policy.LearnedPolicy;
 import policy.Policy;
 import policy.SoftmaxPolicy;
 import state.State;
 import statespace.StateSpace;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 
 public class Algorithms {	
 	private StateSpace stateSpace;
 	private Prey prey;
 	private Predator predator;
 
 	private final int MAX_ITERATIONS = 1000;
 	private final double THETA = 0.00001; // threshold for the loop end condition
 	private final double GAMMA = 0.8;
 
     public enum action {
 		NORTH("N","^"),
 		SOUTH("S","v"),
 		EAST("E",">"),
 		WEST("W","<"),
 		WAIT("X","-");
 		
 		private String shortName;
 		private String arrow;
 
 		public String getShortName() { return this.shortName; }
 		
 		public String getArrow() { return this.arrow; }
 		
 		public action getOpposite() {
 			switch(this) {
 			case WAIT:	return action.WAIT;
 			case EAST:	return action.WEST;
 			case WEST:	return action.EAST;
 			case NORTH:	return action.SOUTH;
 			case SOUTH: return action.NORTH;
 			} return action.WAIT;
 		}
 		
 		private action(String shortName, String arrow){
 			this.shortName = shortName;
 			this.arrow = arrow;
 		}
 	};
 	
 	public Algorithms(StateSpace givenStateSpace) {
 		this.stateSpace = givenStateSpace;
 		this.predator = new Predator(new Coordinates(0,0), stateSpace);
 		this.prey = new Prey(new Coordinates(5,5), stateSpace);
 	}
 
     public StateSpace getStateSpace() { return this.stateSpace; }
     public Predator getPredator() { return predator; }
     /**
      * Runs many simulations  and outputs the number of rounds it takes for the predator to catch the prey on each
      * simulation (less is better), in a .csv file. It is used for evaluating learning algorithms.
      * - New round when both prey and predator move.
      * @param episodeCount The number of episodes to be run.
      * @param filePrefix The prefix of the .csv file to be created.
      */
 	public void simulate(double episodeCount, String filePrefix) {
 		double episodes = episodeCount - 1;
         double allRounds = 0;
         do {
             // initialize Episode
             this.predator.setCoordinates(new Coordinates(0, 0));
             this.prey.setCoordinates(new Coordinates(5, 5));
             this.prey.setAlive(true);
             State initialState = this.stateSpace.getState(this.prey.getCoordinates(), this.predator.getCoordinates());
 
             int rounds = 0;
             while (!isEpisodeOver()) { // run episode
                 initialState = this.nextRound(initialState);
                 rounds++; allRounds++;
             }
             System.out.println((episodeCount-episodes) + ", " + rounds);
             episodes--;
         } while (episodes > -1);
         System.out.println("Rounds on average: " + (allRounds / episodeCount));
     }
 
 	public boolean isEpisodeOver() { return !this.prey.getAlive(); }
 	
 	public State nextRound(State s) {
 		// ask predator for action
 		action a = this.predator.getPolicy().getAction(s);
 		
 		// get next state.
 		s = this.stateSpace.produceStochasticTransition(s, a);
 		
 		// collision detection?
 		if(s.getPreyCoordinates().equals(new Coordinates(0,0))){ 
 			prey.setAlive(false);
 		}
 		return s;
 	}
 	
 	private void collisionDetection() {
 		if (predator.getCoordinates().equals(prey.getCoordinates())) { prey.setAlive(false); }
 	}
 
 	/**
 	 * Task 1.2
 	 * For extensive explanation see : http://webdocs.cs.ualberta.ca/~sutton/book/ebook/node41.html
      */
 	public void policyEvaluation(/*Policy policy, */) {
 		double delta; // defines the maximum difference observed in the stateValue of all states
 		int sweeps = 0;
 		
 		Policy policy = this.predator.getPolicy();
 		
 		do { // Repeat
 			delta = 0.0;
 			Iterator<State> stateSpaceIt = this.stateSpace.iterator();
 			while(stateSpaceIt.hasNext()) {
 				State currState = stateSpace.next(); // for s in S+
 				double V_s = 0.0;
 				double v = currState.getStateValue();
 
 				for (action ac : Algorithms.action.values()) { // summation over a
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
 			for (action a : Algorithms.action.values()) {
 				double sum = 0.0;
 				for (State s_prime : this.stateSpace.getNeighbors(s)) {
 					double p;
 					if (this.stateSpace.getTransitionAction(s, s_prime) == a) { p = 1.0; }
 					else { p = 0.0; }
 					// P^(pi(s))_ss' has only two possible values: 1 if the action will lead to s', 0 otherwise
 					// ac: the action that would be required to move to state st
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
 
 		this.stateSpace.initializeStateValues(0.0); // initialization
 		int numIterations = 0;
 
 		do { // calculation of the V(s)
 			numIterations++;
 			delta = 0.0;
 			
 			// for each s in S
 			Iterator<State> stateSpaceIt = this.stateSpace.iterator();
 			while(stateSpaceIt.hasNext()) {
                 State s = stateSpaceIt.next();
 				double v = s.getStateValue();
 				double max = 0.0;
 				for (action a: Algorithms.action.values()) { // max over a
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
 			for (action a: Algorithms.action.values()) { // argmax over a
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
 
     /************************************************* Assignment2 *************************************************/
 
     /**
      * initializes all state-action pair value (Q) from a single value
      * @param value
      * @return Q q
      */
     public Q initializeQ(double value) {
         Q q = new Q();
         for(State s : this.stateSpace){
             for(action a : Algorithms.action.values()){
                 q.set(s, a, value);
             }
         }
         return q;
     }
 
     /**
      * Runs many simulations and returns the number of rounds it takes for the predator to catch the prey on average
      * for all simulations (less is better). It is used for evaluating learning algorithms.
      * - New round when both prey and predator move.
      * @param episodeCount The number of episodes to be run.
      */
     public double getSimulationAverageRounds(double episodeCount) {
         double episodes = episodeCount - 1;
         double allRounds = 0;
         do {
             // initialize Episode
             this.predator.setCoordinates(new Coordinates(0, 0));
             this.prey.setCoordinates(new Coordinates(5, 5));
             this.prey.setAlive(true);
             State initialState = this.stateSpace.getState(this.prey.getCoordinates(), this.predator.getCoordinates());
 
             int i = 0; // sometimes a terminal state is never reached.
            while (!isEpisodeOver() && i<10000) { // run episode
                 initialState = this.nextRound(initialState);
                allRounds++;
                 i++;
             }
             episodes--;
         } while (episodes > -1);
         return allRounds / episodeCount;
     }
 
     /**
      * Implementation of the Q-Learning algorithm.
      * @param pi The epsilon-greedy policy that will be gradually updated within the algorithm according to the Q values
      * @param initialQ Initial value for Q.
      * @param alpha Learning rate.
      * @param gamma Decay factor.
      * @return
      */
     public Q Q_Learning(EpsilonGreedyPolicy pi, double initialQ, double alpha, double gamma, int episodeCount) {
     	Q q = this.initializeQ(initialQ); // initialize Q(s,a) arbitrarily
     	pi.setQ(q); // I know it's not the best thing, but for now, it works.
         for (int i = 0; i < episodeCount; i++) {  // repeat for each episode
             State s = this.stateSpace.getRandomState(); // initialize s randomly
             State s_prime;
             do { // repeat for each step of episode
             	action a =  pi.getAction(s);    // Choose a from s using policy derived from Q (e-greedy)
 
                 // Take action a. observe r, s'
                 s_prime = this.stateSpace.produceStochasticTransition(s,a);
 
                 double q_sa = q.get(s, a);
                 double max_a_q = q.getMax(s_prime);
                 
                 // the reward r that is consequence of action a, in our case (prey/predator system) is always set 
                 // as the reward associated with the destination state
                 double r = s_prime.getStateReward();
                 
                 double discounted_max_a_q = gamma * max_a_q;
                 double newQ_sa = q_sa + alpha * (r + discounted_max_a_q - q_sa);
 
                 q.set(s, a, newQ_sa); // Q(s,a) = Q(s,a) + α[r + γmax_aQ() - Q(s,a)]
 
                 s = s_prime;
                 //System.out.println("a:"+a+" s':"+s_prime); //FIXME DEBUG
             } while (!s.isTerminal()); // repeat until s is terminal
         }
         return q;
     }
 
     /**
      * Implementation of the Q-Learning algorithm.
      * @param pi The epsilon-greedy policy that will be gradually updated within the algorithm according to the Q values
      * @param initialQ Initial value for Q.
      * @param alpha Learning rate.
      * @param gamma Decay factor.
      * @return
      * TODO: Gotta hate duplicate code.
      */
     public Q Q_Learning(SoftmaxPolicy pi, double initialQ, double alpha, double gamma) {
         Q q = this.initializeQ(initialQ); // initialize Q(s,a) arbitrarily
         pi.setQ(q); // I know it's not the best thing, but for now, it works.
         for (int i = 0; i < Util.EPISODE_COUNT; i++) {  // repeat for each episode
             State s = this.stateSpace.getRandomState(); // initialize s randomly
             State s_prime;
             do { // repeat for each step of episode
                 action a =  pi.getAction(s);    // Choose a from s using policy derived from Q (e-greedy)
 
                 // Take action a. observe r, s'
                 s_prime = this.stateSpace.produceStochasticTransition(s,a);
 
                 double q_sa = q.get(s, a);
                 double max_a_q = q.getMax(s_prime);
 
                 // the reward r that is consequence of action a, in our case (prey/predator system) is always set
                 // as the reward associated with the destination state
                 double r = s_prime.getStateReward();
 
                 double discounted_max_a_q = gamma * max_a_q;
                 double newQ_sa = q_sa + alpha * (r + discounted_max_a_q - q_sa);
 
                 q.set(s, a, newQ_sa); // Q(s,a) = Q(s,a) + α[r + γmax_aQ() - Q(s,a)]
 
                 s = s_prime;
                 //System.out.println("a:"+a+" s':"+s_prime); //FIXME DEBUG
             } while (!s.isTerminal()); // repeat until s is terminal
         }
         return q;
     }
 
     /**
      * @param pi
      * @param initialQ
      * @param alpha
      * @param gamma
      * @return
      */
     public Q sarsa(EpsilonGreedyPolicy pi, double initialQ, double alpha, double gamma) {
         Q q = this.initializeQ(initialQ); // initialize Q(s,a) arbitrarily
         pi.setQ(q); // I know it's not the best thing, but for now, it works.
 
         for (int i = 0; i < Util.EPISODE_COUNT; i++) { // repeat for each episode
             State s = this.stateSpace.getRandomState();// initialize s randomly
             State s_prime;
 
             action a =  pi.getAction(s);    // Choose a from s using policy derived from Q (e-greedy)
             do { // repeat for each step of episode
                 // Take action a. observe r, s'
                 s_prime = this.stateSpace.produceStochasticTransition(s, a);
                 double r = s_prime.getStateReward();
 
                 action a_prime =  pi.getAction(s_prime);    // Choose a from s using policy derived from Q (e-greedy)
 
                 double q_sa = q.get(s, a);
                 double q_sprime_aprime = q.get(s_prime, a_prime);
                 double newQ_sa = q_sa + alpha * (r + gamma * q_sprime_aprime - q_sa);
 
                 q.set(s, a, newQ_sa);
 
                 s = s_prime;
                 a = a_prime;
             } while (!s.isTerminal()); // repeat until s is terminal
         } return q;
     }
 
     /**
      * Loops over all actions and creates a list with all possible StateAction instances, which it returns in a List.
      * @param s
      * @return
      */
     public List<StateAction> getAllStateActions(State s) {
         ArrayList<StateAction> al = new ArrayList<>();
         for (action a : action.values()) {
             StateAction sa = new StateAction(s, a);
             al.add(sa);
         } return al;
     }
 
     public Q monteCarloOffPolicy(EpsilonGreedyPolicy pi, double initialQ, double gamma, int episodeCount) {
         Q q = this.initializeQ(initialQ);               // for all s∈S: Q(s,a) = arbitrary
         pi.initializeActionsArbitrarily(action.SOUTH);  // for all s∈S: π(s) = arbitrary
         pi.setQ(q);
 
         HashMap<StateAction, List<Double>> stateReturns = new HashMap<>();
         for (State s : this.stateSpace) {               // for all s∈S: Returns(s,a) = empty list
             for (StateAction sa : this.getAllStateActions(s)) {
                 stateReturns.put(sa, new ArrayList<Double>());
             }
         }
 
         for (int i = 0; i < episodeCount; i++) {        // Repeat forever:
 
             // (a) generate an episode using exploring starts and π.
             State initialState = this.stateSpace.getRandomState();
             State s = initialState;
 
             List<State> episode = new ArrayList<>();
             episode.add(s);
             while (!s.isTerminal()) {                   // (b) for each pair s,a in the episode.
                 action a =  pi.getAction(s);            // Derive a π.
 
                 double r = s.getStateReward();
 
                 List<Double> returns = stateReturns.get(new StateAction(s, a));
                 returns.add(r);
                 stateReturns.put(new StateAction(s, a), returns);
 
                 q.set(s, a, this.averageReturns(returns));
 
                 // Take action a. observe r, s'
                 s = this.stateSpace.getNextState(s, a);
                 episode.add(s);
             }
 
             for (State state : episode) { // (c) for each s in the episode
                 pi.setUniqueAction(state, q.getArgmaxA(s));
             }
         } return q;
     }
 
     /**
      * Used by Monte Carlo Off policy.
      * @param l List of Doubles to find double of.
      * @return double average of contents of list.
      */
     private double averageReturns(List<Double> l) {
         double ret = 0.0;
         if (l.size() == 0) { return ret; }
         for (double d : l) { ret += d; }
         return ret / l.size();
     }
 }
