 package sequentialsolver;
 
 import java.util.Collections;
 import java.util.List;
 
 import props.DiscreteDistribution;
 import props.Joint;
 
 import normalformgame.NormalFormGame;
 import normalformsolver.GameSolution;
 import normalformsolver.NormalFormSolver;
 import sequentialgame.AbstractAction;
 import sequentialgame.SequentialGame;
 import sequentialgame.AbstractState;
 
 /**
  * A class that generates multi-agent policies for sequential games
  * through value iteration. Value iteration runs for a specified
  * number of iterations with a specified discount factor (for discounting
  * future rewards). A key component of a multi-agent value iteration solver is 
  * the method for solving the one-step problem: given a vector (one per player)
  * of expected values for reaching each next state, what is the vector 
  * (one per player) of actions that should be taken? This one-step problem
  * is solved by a normal form game solver (passed as input), 
  * which can use any solution concept for solving such normal form games.
  * 
  * @author sodomka
  *
  * @param <S>
  * @param <A>
  */
 public class MultiAgentValueIteration<S extends AbstractState, A extends AbstractAction> {
 	
 	/**
 	 * The number of times each state has its values updated.
 	 */
 	private int numIterations;
 
 	/**
 	 * The game solver for determining player actions at a given state,
 	 * given the current value function.
 	 */
 	private NormalFormSolver<A> normalFormSolver;
 
 	/**
 	 * The exponential discounting of future values,
 	 * assumed to be the same for each agent.
 	 */
 	private double discountFactor;
 
 	/**
 	 * Keeps track of the optimal value function.
 	 * For each state, a list of values (one per player) specifying that
 	 * player's value for reaching that state.
 	 */
 	private JointValueFunction<S> jointValueFunction;
 
 	/**
 	 * Keeps track of the optimal policy.
 	 * For each state, a probability distribution over joint actions, specifying the
 	 * probability of certain actions being taken when a state is reached.
 	 */
 	private JointPolicy<S,A> jointPolicy;
 	
 	
 	
 	public MultiAgentValueIteration(int numIterations, NormalFormSolver<A> normalFormSolver, double gamma) {
 		this.numIterations = numIterations;
 		this.normalFormSolver = normalFormSolver;
 		this.discountFactor = gamma;
 		
 		this.jointValueFunction = new JointValueFunction<S>();		
 		this.jointPolicy = new JointPolicy<S,A>();
 	}
 	
 	public JointPolicy<S,A> generatePolicy(SequentialGame<S,A> sequentialGame) {
 		
 		// Initialize value function.
 		int numPlayers = sequentialGame.getNumPlayers();
 		Joint<Double> zeros = new Joint<Double>();
 		for (int playerIdx=0; playerIdx<numPlayers; playerIdx++) {
 			zeros.add(0.0);
 		}
 		for (S state : sequentialGame.getPossibleStates()) {
 			jointValueFunction.put(state, zeros);
 		}
 		
 		
 		for (int iteration=0; iteration<numIterations; iteration++) {
 			// Create a new value function so that old value function data can be used for each state.
 			JointValueFunction<S> updatedJointValueFunction = new JointValueFunction<S>();
 			for (S state : sequentialGame.getPossibleStates()) {
 
 				// Only update the value for non-terminal states.
 				// TODO: More elegant would be to have any terminal state contain only a null action
 				// and always transition to itself. That way, the accuracy of an algorithm doesn't
 				// depend on not updating terminal states.
 				if (sequentialGame.isTerminalState(state)) {
					updatedJointValueFunction.put(state, zeros);
 					continue;
 				}
 				
 				// Construct a normal form game where payoffs for a given 
 				// action profile are based on the expected immediate rewards for 
 				// the (state, actionProfile, nextState) tuple plus the expected
 				// discounted future reward over next states (based on the 
 				// current value function).
 				NormalFormGame<A> normalFormGame = createNormalFormGame(sequentialGame, state, jointValueFunction, discountFactor);
 				
 				// Compute solution to normal form game  
 				GameSolution<A> gameSolution = normalFormSolver.solve(normalFormGame);
 				
 				// Update values at this state. We don't put the updates into the main value function yet
 				// since we want other states to use the old value function.
 				Joint<Double> expectedPayoffs = gameSolution.getExpectedPayoffs();
 				updatedJointValueFunction.put(state, expectedPayoffs);
 				
 				// Update policy at this state.
 				DiscreteDistribution<Joint<A>> jointActionDistribution = gameSolution.getJointActionDistribution();
 				jointPolicy.put(state, jointActionDistribution);
 			}
 			// Now that all states have been considered, update the value function.
 			jointValueFunction = updatedJointValueFunction;
 		}
 		return jointPolicy;
 	}
 
 	private NormalFormGame<A> createNormalFormGame(
 			SequentialGame<S,A> g, S currentState, JointValueFunction<S> v, double gamma) {
 		// Create list of actions
 		Joint<List<A>> actionsPerPlayer = new Joint<List<A>>();
 		for (int playerIdx=0; playerIdx<g.getNumPlayers(); playerIdx++) {
 			List<A> playerActions = g.getPossibleActionsForPlayer(playerIdx); // add something to game to get this.
 			actionsPerPlayer.add(playerActions);
 		}
 
 		// Create normal form game with these actions
 		NormalFormGame<A> normalFormGame = new NormalFormGame<A>(g.getNumPlayers(), actionsPerPlayer, g.getPossibleJointActions());
 		
 		// Create expected payoffs
 		for (Joint<A> jointAction : g.getPossibleJointActions()) {
 			// Get expected payoffs for each player for this action profile occurring.
 			// (i.e., compute expected payoffs for this cell of the normal form game.)
 			Joint<Double> payoffs = createInitialPayoffs(g.getNumPlayers());
 			DiscreteDistribution<S> nextStateDistribution = g.getTransitionProbabilities(currentState, jointAction);
 			//System.out.println("state=" + currentState + ", action=" + jointAction + ", next state distribution: " + nextStateDistribution);
 			for (S nextState : nextStateDistribution.keySet()) {
 				//System.out.println("  nextState=" + nextState);
 				double nextStateProb = nextStateDistribution.get(nextState);
 				Joint<Double> immediateRewards = g.getImmediateRewards(currentState, jointAction, nextState);
 				Joint<Double> nextStateValues = v.getJointValuesForState(nextState);
 				addToPayoffs(payoffs, nextStateProb, immediateRewards, nextStateValues, gamma);
 			}
 			normalFormGame.addPayoffsForJointAction(jointAction, payoffs);
 		}
 		return normalFormGame;
 	}
 	
 	
 
 	/**
 	 * Creates an initial payoff vector.
 	 * @param numPlayers
 	 * @return
 	 */
 	private static Joint<Double> createInitialPayoffs(int numPlayers) {
 		Joint<Double> payoffs = new Joint<Double>(Collections.nCopies(numPlayers, 0.0));
 		return payoffs;
 	}
 
 	/**
 	 * Updates payoffs vector to account for additional reward.
 	 * @param payoffs
 	 * @param nextStateProb
 	 * @param immediateRewards
 	 * @param nextStateValues
 	 * @param gamma
 	 */
 	private static void addToPayoffs(List<Double> payoffs,
 			double nextStateProb, List<Double> immediateRewards,
 			List<Double> nextStateValues, double gamma) {
 		int numPlayers = payoffs.size();
 		//System.out.println("   payoffs=" + payoffs + ", reward=" + immediateRewards + ", nextVal=" + nextStateValues);
 		for (int playerIdx=0; playerIdx<numPlayers; playerIdx++) {
 			double oldPayoff = payoffs.get(playerIdx);
 			double additionalPayoff = nextStateProb * 
 					( immediateRewards.get(playerIdx) + gamma * nextStateValues.get(playerIdx) );
 			payoffs.set(playerIdx, oldPayoff + additionalPayoff);
 		}
 		return;
 	}
 	
 
 	
 	
 }
