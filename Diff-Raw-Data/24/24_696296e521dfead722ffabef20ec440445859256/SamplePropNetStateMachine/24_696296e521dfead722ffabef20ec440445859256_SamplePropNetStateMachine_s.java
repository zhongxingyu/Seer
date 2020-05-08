 package util.statemachine.implementation.propnet;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import util.gdl.grammar.Gdl;
 import util.gdl.grammar.GdlConstant;
 import util.gdl.grammar.GdlProposition;
 import util.gdl.grammar.GdlRelation;
 import util.gdl.grammar.GdlSentence;
 import util.gdl.grammar.GdlTerm;
 import util.propnet.architecture.Component;
 import util.propnet.architecture.PropNet;
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
     
     private MachineState initialState;
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
     }    
     
     private MachineState computeInitialState()
 	{
     	Proposition init = propNet.getInitProposition();
     	Set<Proposition> initProp = new HashSet<Proposition>();
     	initProp.add(init);
     	return updateStateMachine(initProp);
 	}
     
 	/**
 	 * Computes if the state is terminal. Should return the value
 	 * of the terminal proposition for the state.
 	 */
     @Override
     public boolean isTerminal(MachineState state) {
         Proposition terminal = propNet.getTerminalProposition();
         Map<GdlTerm, Proposition> props = propNet.getBasePropositions();
         for(GdlSentence s : state.getContents()){
             if(props.get(s.toTerm()) == terminal) return true;
         }
         return false;
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
         Set<Proposition> goals = propNet.getGoalPropositions().get(role);
         Map<GdlTerm, Proposition> props = propNet.getBasePropositions();
         Proposition goal = null;
         for(GdlSentence s : state.getContents()){
             if(goals.contains(props.get(s.toTerm()))){
                 if(goal != null) throw new GoalDefinitionException(state, role);
                 goal = props.get(s.toTerm());
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
		// TODO: Compute legal moves.
		return null;
 	}
 	
 	/**
 	 * Computes the next state given state and the list of moves.
 	 */
 	@Override
 	public MachineState getNextState(MachineState state, List<Move> moves)
 	throws TransitionDefinitionException {		
 		Map<GdlTerm, Proposition> termToProps = propNet.getInputPropositions();
 		List<GdlTerm> moveTerms = toDoes(moves);
 		
 		Set<Proposition> inputProps = new HashSet<Proposition>();
 		for (GdlTerm term : moveTerms) {
 			inputProps.add(termToProps.get(term));
 		}
 		
 		Map<GdlTerm, Proposition> baseMap = propNet.getBasePropositions();	
 		for (GdlSentence s : state.getContents()) {
 			inputProps.add(baseMap.get(s.toTerm()));
 		}
 		
 		return updateStateMachine(inputProps);
 	}
 	
     
     private MachineState updateStateMachine(Set<Proposition> inputProps) {
     	for(Proposition prop : ordering){
     		boolean isTrue = true;
     		for(Component input : prop.getInputs()){
     			for(Component inProp : input.getInputs()){
     				if(!inputProps.contains(inProp)){
     					isTrue = false;
     					break;
     				}
     			}
     			if(!isTrue)break;
     		}
     		if(isTrue) inputProps.add(prop);
     	}
     	return null;
     }
     
     private MachineState makeStateFromProps(Set<Proposition> stateProps){
     	Set<GdlSentence> contents = new HashSet<GdlSentence>();
     	for(Proposition prop : stateProps){
     		contents.add(prop.getName().toSentence());
     	}
     	return new MachineState(contents);
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
 	       List<Component> components = new ArrayList<Component>(propNet.getComponents());
 
 	       // All of the propositions in the PropNet.
 	       List<Proposition> propositions = new ArrayList<Proposition>(propNet.getPropositions());
 
 	       // TODO: Compute the topological ordering.
 	       Set<Component> solved = new HashSet<Component>();
 	       solved.addAll(propNet.getBasePropositions().values());
 	       solved.addAll(propNet.getInputPropositions().values());
 	       int initSize = solved.size();
 
 	       while(propositions.size() > 0){
 	           for(Component comp : components){
 	               boolean allSolved = true;
 	               for(Component c : comp.getInputs()){
 	                   if(!allSolved) break;
 	                   if(!solved.contains(c)) allSolved = false;
 	               }
 	               if(allSolved) solved.add(comp);
 	           }
 	           components.removeAll(solved);
 	           for(Proposition prop : propositions){
 	               boolean allSolved=true;
 	               for(Component comp : prop.getInputs()){
 	                   if(!allSolved) break;
 	                   if(!solved.contains(comp)) allSolved = false;
 	               }
 	               if(allSolved){
 	                   solved.add(prop);
 	                   order.add(prop);
 	               }
 	           }
 	           propositions.removeAll(solved);
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
 			{
 				contents.add(p.getName().toSentence());
 			}
 
 		}
 		return new MachineState(contents);
 	}
 }
