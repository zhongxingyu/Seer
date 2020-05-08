 package util.statemachine.implementation.propnet;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.sun.org.apache.xpath.internal.operations.And;
 
 import util.gdl.grammar.Gdl;
 import util.gdl.grammar.GdlConstant;
 import util.gdl.grammar.GdlProposition;
 import util.gdl.grammar.GdlRelation;
 import util.gdl.grammar.GdlSentence;
 import util.gdl.grammar.GdlTerm;
 import util.propnet.architecture.Component;
 import util.propnet.architecture.PropNet;
 import util.propnet.architecture.components.Or;
 import util.propnet.architecture.components.Proposition;
 import util.propnet.factory.OptimizingPropNetFactory;
 import util.statemachine.MachineState;
 import util.statemachine.Move;
 import util.statemachine.Role;
 import util.statemachine.StateMachine;
 import util.statemachine.exceptions.GoalDefinitionException;
 import util.statemachine.exceptions.MoveDefinitionException;
 import util.statemachine.exceptions.TransitionDefinitionException;
 import util.statemachine.implementation.prover.query.ProverQueryBuilder;
 import util.statemachine.implementation.prover.result.ProverResultParser;
 
 @SuppressWarnings("unused")
 public class SamplePropNetStateMachine extends StateMachine {
     /** The underlying proposition network  */
     private PropNet propNet;
     /** The topological ordering of the propositions */
     private List<Proposition> ordering;
     /** The player roles */
     private List<Role> roles;
     /**The factors of this game, with their goal states as keys*/
     Map<Component, Set<Component>> factors;
     Component selectedGoal;
     Set<Proposition> selectedLegals;
     
     private MachineState initialState;
     private MachineState currentState;
     /**
      * Initializes the PropNetStateMachine. You should compute the topological
      * ordering here. Additionally you may compute the initial state here, at
      * your discretion.
      */
     @Override
     public void initialize(List<Gdl> description) {
         try {
 			propNet = OptimizingPropNetFactory.create(description);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
         roles = propNet.getRoles();
         ordering = getOrdering();
         initialState = computeInitialState();
        propNet.renderToFile("graph.dot");
         System.out.println("order: "+ordering.toString());
         System.out.println("initState: "+initialState.toString());
     }    
     
     private MachineState computeInitialState()
 	{
     	clearPropNet();
     	propNet.getInitProposition().setValue(true);//should be the only true proposition at start
     	return updateStateMachine(new MachineState(new HashSet<GdlSentence>()));//empty base
 	}
     
 	/**
 	 * Computes if the state is terminal. Should return the value
 	 * of the terminal proposition for the state.
 	 */
     @Override
     public boolean isTerminal(MachineState state) {
 		if(!state.equals(currentState)){
 			clearPropNet();
 			updateStateMachine(state);
 		}
 		//once the state machine is on the right state, it's easy to read get terminal
         return propNet.getTerminalProposition().getValue();
     }
 	
 	/**
 	 * Computes the goal for a role in the current state.
 	 * Should return the value of the goal proposition that
 	 * is true for that role. If there is not exactly one goal
 	 * proposition true for that role, then you should throw a
 	 * GoalDefinitionException because the goal is ill-defined. 
 	 */
     @Override
     public int getGoal(MachineState state, Role role)
     throws GoalDefinitionException {
 		if(!state.equals(currentState)){
 			clearPropNet();
 			updateStateMachine(state);
 		}
     	
         Set<Proposition> goals = propNet.getGoalPropositions().get(role);
         Proposition goal = null;
         //loop over goals and make sure only one is true in this state
         for(Proposition g : goals){
             if(g.getValue()){
                 if(goal != null) throw new GoalDefinitionException(state, role);
                 goal = g;
             }
         }
         if(goal == null) throw new GoalDefinitionException(state, role);
         return getGoalValue(goal);
     }
 	
 	/**
 	 * Returns the initial state. The initial state can be computed
 	 * by only setting the truth value of the INIT proposition to true,
 	 * and then computing the resulting state.
 	 */
 	@Override
 	public MachineState getInitialState() {
 		return initialState;
 	}
 	
 	/**
 	 * Computes the legal moves for role in state.
 	 */
 	@Override
 	public List<Move> getLegalMoves(MachineState state, Role role)
 	throws MoveDefinitionException {
 		if(!state.equals(currentState)){
 			clearPropNet();
 			updateStateMachine(state);
 		}
 		//just check propositions corresponding to all possible moves for role
 		//move is legal if the proposition is true
 		Set<Proposition> legalProp = propNet.getLegalPropositions().get(role);
 		List<Move> moves = new ArrayList<Move>();
 		for(Proposition prop : legalProp){			
 			if(prop.getValue()){
 				if(selectedGoal == null)
 					moves.add(getMoveFromProposition(prop));
 				else if(selectedLegals.contains(prop))
 					moves.add(getMoveFromProposition(prop));
 			}
 		}
 		
 		return moves;
 	}
 	
 	/**
 	 * Computes the next state given state and the list of moves.
 	 */
 	@Override
 	public MachineState getNextState(MachineState state, List<Move> moves)
 	throws TransitionDefinitionException {
 		clearPropNet();
 		
     	//Use the moves to define inputs for the next state
 		Map<GdlTerm, Proposition> termToProps = propNet.getInputPropositions();
 		List<GdlTerm> moveTerms = toDoes(moves);
 		for (GdlTerm m : moveTerms) {
 			termToProps.get(m).setValue(true);
 		}
 
 		return updateStateMachine(state);
 	}
 	
 	private void clearPropNet(){
 		Set<Proposition> props = propNet.getPropositions();
 		for(Proposition p : props){
 			p.setValue(false);
 		}
 	}
     
     private MachineState updateStateMachine(MachineState state) {
 		//map the state to base propositions
 		Map<GdlTerm, Proposition> baseMap = propNet.getBasePropositions();
 		for (GdlSentence s : state.getContents()) {
 			baseMap.get(s.toTerm()).setValue(true);
 		}
     	
     	//update the props in order
     	for(Proposition prop : ordering)
     		prop.setValue(prop.getSingleInput().getValue());
     	
     	currentState = state;
     	return getStateFromBase();
     }    
 	
 	/**
 	 * This should compute the topological ordering of propositions.
 	 * Each component is either a proposition, logical gate, or transition.
 	 * Logical gates and transitions only have propositions as inputs.
 	 * 
 	 * The base propositions and input propositions should always be exempt
 	 * from this ordering.
 	 * 
 	 * The base propositions values are set from the MachineState that
 	 * operations are performed on and the input propositions are set from
 	 * the Moves that operations are performed on as well (if any).
 	 * 
 	 * @return The order in which the truth values of propositions need to be set.
 	 */
 	public List<Proposition> getOrdering()
 	{
 		  // List to contain the topological ordering.
 	       List<Proposition> order = new LinkedList<Proposition>();
 
 	       // All of the components in the PropNet
 	       Set<Component> components = new HashSet<Component>(propNet.getComponents());
 	       
 	       Set<Component> solved = new HashSet<Component>();
 	       solved.add(propNet.getInitProposition());
 	       solved.addAll(propNet.getBasePropositions().values());
 	       solved.addAll(propNet.getInputPropositions().values());
 	       
 	       int numToSolve = propNet.getPropositions().size() - solved.size();
 
 	       while(order.size() < numToSolve){
 	    	   Set<Component> nowSolved = new HashSet<Component>();
 	           for(Component comp : propNet.getComponents()){	   
 	        	   if(!solved.contains(comp)){
 	            	   boolean allSolved = true;
 	                   for(Component in : comp.getInputs()){
 	                	   if(!solved.contains(in)){
 	                		   allSolved = false;
 	                		   break;
 	                	   }
 	                   }
 	                   if(allSolved){
 	                	   nowSolved.add(comp);
 		            	   if(comp instanceof Proposition) order.add((Proposition)comp);
 	                   }
 	               }
 	           }
 	           solved.addAll(nowSolved);
 	       }
 	       return order;
 	}
 	
 	/* Already implemented for you */
 	@Override
 	public List<Role> getRoles() {
 		return roles;
 	}
 
 	/* Helper methods */
 		
 	/**
 	 * The Input propositions are indexed by (does ?player ?action).
 	 * 
 	 * This translates a list of Moves (backed by a sentence that is simply ?action)
 	 * into GdlTerms that can be used to get Propositions from inputPropositions.
 	 * and accordingly set their values etc.  This is a naive implementation when coupled with 
 	 * setting input values, feel free to change this for a more efficient implementation.
 	 * 
 	 * @param moves
 	 * @return
 	 */
 	private List<GdlTerm> toDoes(List<Move> moves)
 	{
 		List<GdlTerm> doeses = new ArrayList<GdlTerm>(moves.size());
 		Map<Role, Integer> roleIndices = getRoleIndices();
 		
 		for (int i = 0; i < roles.size(); i++)
 		{
 			int index = roleIndices.get(roles.get(i));
 			doeses.add(ProverQueryBuilder.toDoes(roles.get(i), moves.get(index)).toTerm());
 		}
 		return doeses;
 	}
 	
 	/**
 	 * Takes in a Legal Proposition and returns the appropriate corresponding Move
 	 * @param p
 	 * @return a PropNetMove
 	 */
 	public static Move getMoveFromProposition(Proposition p)
 	{
 		return new Move(p.getName().toSentence().get(1).toSentence());
 	}
 	
 	/**
 	 * Helper method for parsing the value of a goal proposition
 	 * @param goalProposition
 	 * @return the integer value of the goal proposition
 	 */	
     private int getGoalValue(Proposition goalProposition)
 	{
 		GdlRelation relation = (GdlRelation) goalProposition.getName().toSentence();
 		GdlConstant constant = (GdlConstant) relation.get(1);
 		return Integer.parseInt(constant.toString());
 	}
 	
 	/**
 	 * A Naive implementation that computes a PropNetMachineState
 	 * from the true BasePropositions.  This is correct but slower than more advanced implementations
 	 * You need not use this method!
 	 * @return PropNetMachineState
 	 */	
 	public MachineState getStateFromBase()
 	{
 		Set<GdlSentence> contents = new HashSet<GdlSentence>();
 		for (Proposition p : propNet.getBasePropositions().values())
 		{
 			p.setValue(p.getSingleInput().getValue());
 			if (p.getValue())
 				contents.add(p.getName().toSentence());
 		}
 		return new MachineState(contents);
 	}
 	
 	/*
 	 * Checks to see if this game is disjunctively factorable.
 	 * returns the number of factored games 
 	 */
 	public int factorDisjunctiveGoalStates(Role role) {
 		selectedGoal = null;
 		Set<Proposition> goalProps = propNet.getGoalPropositions().get(role);
 		Proposition bestGoal = null;
 		int bestVal = 0;
 		//any goal proposition with an or gate could lead to disjunction
 		//only focus on the goal which gets us the best reward
 		for (Proposition goalProp : goalProps){
 			if (goalProp.getSingleInput() instanceof Or) {
 				System.out.println("Here's a potential disjunction"+goalProp.toString());
 				System.out.println(goalProp.getSingleInput().getInputs().size());
 				//goalProp.getSingleInput().getInputs().size();
 				if(bestVal < getGoalValue(goalProp)){
 					bestGoal = goalProp;
 					bestVal = getGoalValue(goalProp);
 				}
 			} else {
 				System.out.println("regular goal"+goalProp.toString());
 			}
 		}
 		
 		//there are up to n factors, where n is the number of inputs to the goal prop
 		factors = new HashMap<Component, Set<Component>>();
 		if(bestGoal == null) return 0;
 		
 		//make a set of all propositions affecting each input
 		for(Component factorGoal : bestGoal.getSingleInput().getInputs()){
 			Set<Component> f = new HashSet<Component>();
 			Set<Component> fringe = new HashSet<Component>();
 			f.add(factorGoal);
 			fringe.add(factorGoal);
 			//iterate until all connected factors are in the set
 			//VERY INEFFICIENT!!!
 			while(true){
 				Set<Component> newFringe = new HashSet<Component>();
 				for(Component comp : fringe){
 					for(Component in : comp.getInputs()){
 						//we must cut out the init proposition
 						if(!in.equals(propNet.getInitProposition()) && !f.contains(in)){
 							f.add(in);
 							newFringe.add(in);
 						}
 					}
 					for(Component out : comp.getOutputs()){
 						//adding the goal will have all kinds of negative implications
 						if(!out.equals(factorGoal) && !f.contains(out)){
 							f.add(out);
 							newFringe.add(out);
 						}
 					}
 				}
 				fringe = newFringe;
 				//if we didn't add anything this round, we're done
 				if(fringe.size() == 0) break;
 			}	
 			factors.put(factorGoal, f);
 		}
 		//check for overlap of the generated sets
 		List<Set<Component>> sets = new ArrayList<Set<Component>>(factors.values());
 		for(int i = 0; i < sets.size(); i++){
 			for(int j = i+1; j < sets.size(); j++){
 				Set<Component> copy = new HashSet<Component>(sets.get(i));
 				copy.retainAll(sets.get(j));
 				if(copy.size() > 0){
 					factors.remove(sets.get(i));
 					break;
 				}
 			}
 		}
 		int smallest = -1;
 		for(Component key: factors.keySet()){
 			if(smallest == -1 || factors.get(key).size()<smallest){
 				smallest = factors.get(key).size();
 				selectedGoal = key;
 			}
 		}
 		selectedLegals = new HashSet<Proposition>();
 		for(Component comp : factors.get(selectedGoal)){
 			if(propNet.getLegalInputMap().containsKey(comp)){
 				selectedLegals.add(propNet.getLegalInputMap().get(comp));
 			}
 		}
 		//System.out.println(propNet.getLegalInputMap().keySet());
 		//System.out.println(propNet.getLegalInputMap().);
 		//System.out.println(factors.get(selectedGoal));
 		return factors.size();		
 	}
 }
