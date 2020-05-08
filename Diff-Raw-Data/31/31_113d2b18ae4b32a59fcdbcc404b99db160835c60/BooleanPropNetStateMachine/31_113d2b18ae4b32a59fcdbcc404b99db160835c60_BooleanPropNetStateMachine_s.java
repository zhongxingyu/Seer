 package util.statemachine.implementation.propnet;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import player.gamer.statemachine.eggplant.misc.Log;
 import util.gdl.grammar.Gdl;
 import util.gdl.grammar.GdlConstant;
 import util.gdl.grammar.GdlProposition;
 import util.gdl.grammar.GdlRelation;
 import util.gdl.grammar.GdlSentence;
 import util.gdl.grammar.GdlTerm;
 import util.propnet.architecture.BooleanPropNet;
 import util.propnet.architecture.Component;
 import util.propnet.architecture.components.And;
 import util.propnet.architecture.components.Or;
 import util.propnet.architecture.components.Proposition;
 import util.propnet.architecture.components.Transition;
 import util.propnet.factory.CachedPropNetFactory;
 import util.statemachine.BooleanMachineState;
 import util.statemachine.MachineState;
 import util.statemachine.Move;
 import util.statemachine.Role;
 import util.statemachine.StateMachine;
 import util.statemachine.exceptions.GoalDefinitionException;
 import util.statemachine.exceptions.MoveDefinitionException;
 import util.statemachine.exceptions.TransitionDefinitionException;
 import util.statemachine.implementation.prover.query.ProverQueryBuilder;
 
 public class BooleanPropNetStateMachine extends StateMachine {
 
 	private static final String PNET_FOLDER = "pnet";
 	private static final String ORIGINAL_PNET_PATH = "pnet" + File.separator + "orig.dot";
 	
 	/** The original description */ 
 	private List<Gdl> description;
 	
 	/** The underlying proposition network */
 	private BooleanPropNet pnet;
 	
 	/** References to every Proposition in the PropNet. */
 	private Proposition[] propIndex;
 	
 	/** References to every Proposition in the PropNet. */
 	private Map<Proposition, Integer> propMap;
 	
 	/** References to every BaseProposition in the PropNet, indexed by name. */
 	private Map<GdlTerm, Integer> basePropMap;
 	
 	/** References to every InputProposition in the PropNet, indexed by name. */
 	private Map<GdlTerm, Integer> inputPropMap;
 	
 	/**
 	 * References to every LegalProposition in the PropNet, indexed by player
 	 * name.
 	 */
 	private int[][] legalPropMap;
 	
 	/**
 	 * A map between legal and input propositions. The map contains mappings in
 	 * both directions
 	 */
 	private int[] legalInputMap;
 	
 	/**
 	 * References to every GoalProposition in the PropNet, indexed by player
 	 * name.
 	 */
 	private int[][][] goalPropMap;
 	
 	/** A the index of the single, unique InitProposition. */
 	private int initIndex;
 	
 	/** A the index of the first BaseProposition. */
 	private int basePropStart;
 	
 	/** A the index of the first InputProposition. */
 	private int inputPropStart;
 	
 	/** A the index of the first InternalProposition. */
 	private int internalPropStart;
 	
 	/** A the index of the single, unique TerminalProposition. */
 	private int terminalIndex;
 	
 	
 	/** The topological ordering of the propositions */
 	private List<Proposition> defaultOrdering;
 	
 	/** The roles of different players in the game */
 	private List<Role> rolesList;
 	
 	private Role[] roleIndex;
 	private Map<Role, Integer> roleMap;
 	
 	private Move[] moveIndex;
 	
 	private Operator operator;
 	
 	private static Random random = new Random();
 
 	/**
 	 * Initializes the PropNetStateMachine. You should compute the topological
 	 * ordering here. Additionally you can compute the initial state here, if
 	 * you want.
 	 * 
 	 * We should do this and then throw away the init node + it's connections
 	 */
 	@Override
 	public void initialize(List<Gdl> description) {
 		this.description = description;
 		this.pnet = (BooleanPropNet) CachedPropNetFactory.create(description);
 		this.rolesList = computeRoles(description);
 		this.pnet.renderToFile(ORIGINAL_PNET_PATH);
 		initializeFromPropNet(this.pnet);
 	}
 	
 	public void initialize(Set<Component> components, List<Role> roles) {
 		this.description = null;
 		this.pnet = new BooleanPropNet(roles, components);
 		this.rolesList = roles;
 		initializeFromPropNet(this.pnet);
 	}
 
 	private void initializeFromPropNet(BooleanPropNet pnet) {
 		propIndex = pnet.getPropIndex();
 		propMap = pnet.getPropMap();
 		basePropMap = pnet.getBasePropMap();
 		inputPropMap = pnet.getInputPropMap();
 		legalInputMap = pnet.getLegalInputMap();
 		initIndex = pnet.getInitIndex();
 		basePropStart = pnet.getBasePropStart();
 		inputPropStart = pnet.getInputPropStart();
 		internalPropStart = pnet.getInternalPropStart();
 		terminalIndex = pnet.getTerminalIndex();
 		
 		computeRoleIndices(rolesList);
 		
 		Map<Role, int[]> rolesLegalPropMap = pnet.getLegalPropMap();
 		Map<Role, int[][]> rolesGoalPropMap = pnet.getGoalPropMap();
 		legalPropMap = new int[roleIndex.length][];
 		goalPropMap = new int[roleIndex.length][][];
 		for (int role = 0; role < roleIndex.length; role++) {
 			legalPropMap[role] = rolesLegalPropMap.get(roleIndex[role]);
 			goalPropMap[role] = rolesGoalPropMap.get(roleIndex[role]);
 		}
 		
 		moveIndex = new Move[propIndex.length]; 
 		for (int i = inputPropStart; i < internalPropStart; i++) {
 			moveIndex[i] = getMoveFromProposition(propIndex[i]);
 		}
 		
 		defaultOrdering = getOrdering(null);
 
 		
 		initOperator();
 	}
 
 	/**
 	 * Computes if the state is terminal. Should return the value of the
 	 * terminal proposition for the state.
 	 */
 	@Override
 	public boolean isTerminal(MachineState state) {
 		try {
 			boolean[] props = initBasePropositionsFromState(state);
 			operator.propagateTerminalOnly(props);
 			return props[terminalIndex];
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return false;
 	}
 	
 	/** 
 	 * Retrieves the goal values in ascending numerical order.
 	 */
 	public int[] getGoalValues(Role role) {
 		int roleNum = roleMap.get(role);
 		int[][] goals = goalPropMap[roleNum];
 		int[] values = new int[goals.length];
 		for (int i = 0; i < goals.length; i++) {
 			values[i] = goals[i][1];
 		}
 		Arrays.sort(values);
 		return values;
 	}
 
 	/**
 	 * Computes the goal for a role in the current state. Should return the
 	 * value of the goal proposition that is true for role. If the number of
 	 * goals that are true for role != 1, then you should throw a
 	 * GoalDefinitionException
 	 */
 	@Override
 	public int getGoal(MachineState state, Role role) throws GoalDefinitionException {
 		try {
 			boolean[] props = initBasePropositionsFromState(state);
 			int roleIndex = roleMap.get(role);
 			int[][] goals = goalPropMap[roleIndex];
 			operator.propagateGoalOnly(props, roleIndex);
 			boolean goalFound = false;
 			int goalValue = -1;
 			for (int i = 0; i < goals.length; i++) {
 				if (props[goals[i][0]]) {
 					if (goalFound) {
 						throw new GoalDefinitionException(state, role);
 					} else {
 						goalValue = goals[i][1];
 						goalFound = true;
 					}
 				}
 			}
 
 			return goalValue;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return -1;
 
 	}
 
 	/**
 	 * Returns the initial state. The initial state can be computed by only
 	 * setting the truth value of the init proposition to true, and then
 	 * computing the resulting state.
 	 */
 	@Override
 	public BooleanMachineState getInitialState() {
 		boolean[] props = new boolean[propIndex.length];
 		props[initIndex] = true;
 		operator.propagate(props);
 		return new BooleanMachineState(Arrays.copyOfRange(props, basePropStart, inputPropStart), propIndex);
 	}
 
 	/**
 	 * Computes the legal moves for role in state
 	 */
 	@Override
 	public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException {
 		try {
 			List<Move> legalMoves = new LinkedList<Move>();
 
 			int roleIndex = roleMap.get(role);
 			
 			int[] legals = legalPropMap[roleIndex];
 
 			boolean[] props = initBasePropositionsFromState(state);
 			
 			// Clear initial moves
 			for (int i = 0; i < legals.length; i++) {
 				props[legals[i]] = false;
 			}
 			for (int i = 0; i < legals.length; i++) {
 				int inputIndex = legalInputMap[legals[i]];
 				props[inputIndex] = true;
 				operator.propagateLegalOnly(props, roleIndex);
 				if (props[legals[i]]) {
 					legalMoves.add(moveIndex[inputIndex]);
 				}
 				props[inputIndex] = false;
 			}
 			return legalMoves;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Computes the next state given state and the list of moves.
 	 */
 	@Override
 	public MachineState getNextState(MachineState state, List<Move> moves)
 			throws TransitionDefinitionException {
 		try {
 			// Set up the base propositions
 			boolean[] props = initBasePropositionsFromState(state);
 
 			// Set up the input propositions
 			List<GdlTerm> doeses = toDoes(moves);
 			
 			// All input props start as false
 
 			for (GdlTerm does : doeses) {
 				Log.println('c', "Marking move with " + does);
 				props[inputPropMap.get(does)] = true;
 			}
 
 			Log.println('c', "Before propagate: " + Arrays.toString(props));
 			operator.propagate(props);
 			Log.println('c', "After propagate: " + Arrays.toString(props));
 			return new BooleanMachineState(Arrays.copyOfRange(props, basePropStart, inputPropStart), propIndex);
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	private boolean[] initBasePropositionsFromState(MachineState state) {
 		if (state instanceof BooleanMachineState) {
 			boolean[] props = new boolean[propIndex.length];
 			boolean[] baseProps = ((BooleanMachineState) state).getBooleanContents();
 			System.arraycopy(baseProps, 0, props, 1, baseProps.length);
 			return props;
 		} else {
 			Set<GdlSentence> initialTrueSentences = state.getContents();
 			for (GdlTerm propName : basePropMap.keySet()) {
 				propIndex[basePropMap.get(propName)].setValue(
 						initialTrueSentences.contains(propName.toSentence()));
 			}
 			return generatePropArray(basePropStart, inputPropStart);
 		}
 	}
 	
 	/**
 	 * This should compute the topological ordering of propositions. Each
 	 * component is either a proposition, logical gate, or transition. Logical
 	 * gates and transitions only have propositions as inputs. The base
 	 * propositions and input propositions should always be exempt from this
 	 * ordering The base propositions values are set from the MachineState that
 	 * operations are performed on and the input propositions are set from the
 	 * Moves that operations are performed on as well (if any).
 	 * 
 	 * @return The order in which the truth values of propositions need to be
 	 *         set.
 	 */
 	public List<Proposition> getOrdering(int[] preferredOrdering) {
 		LinkedList<Proposition> order = new LinkedList<Proposition>();
 
 		// All of the components in the PropNet
 		HashSet<Component> components = new HashSet<Component>(pnet.getComponents());
 		// Log.println('p', "Components: " + components);
 		
 		// All of the internal propositions in the prop net
 		HashSet<Proposition> propositions = new HashSet<Proposition>();
 		for (int i = internalPropStart; i < propIndex.length; i++) {
 			propositions.add(propIndex[i]);
 		}
 		
 		// Remove all base propositions from components 
 		for (int i = initIndex; i < inputPropStart; i++) {
 			components.remove(propIndex[i]);
 		}
 		
 		// TODO: Optimize this. Not efficient at all
 		Iterator<Component> componentIterator = components.iterator();
 		while (componentIterator.hasNext()) {
 			Component component = componentIterator.next();
 			if ((component instanceof Proposition) && component.getInputs().size() < 1) {
 				componentIterator.remove();
 			}
 		}
 		
 		// Use DFS to compute topological sort of propositions
 		if (preferredOrdering == null) { // sort all
 			while (!propositions.isEmpty()) {
 				Proposition currComponent = propositions.iterator().next();
 				topologicalSort(currComponent, order, propositions, components);
 			}
 		}
 		else {
 			for (int i = 0; i < preferredOrdering.length; i++) {
 				Proposition currComponent = propIndex[preferredOrdering[i]];
 				if (propositions.contains(currComponent)) {
 					topologicalSort(currComponent, order, propositions, components);
 				}
 			}
 		}
 		
 		Log.println('p', "Order: " + order);
 		return order;
 	}
 
 	private void topologicalSort(Component currComponent, LinkedList<Proposition> order,
 			HashSet<Proposition> propositions, HashSet<Component> components) {
 		for (Component input : currComponent.getInputs()) {
 			if (components.contains(input)) { // not yet visited
 				topologicalSort(input, order, propositions, components);
 			}
 		}
 		if (currComponent instanceof Proposition) {
 			order.add((Proposition) currComponent);
 			propositions.remove(currComponent);
 		}
 		components.remove(currComponent);
 	}
 
 	/** Already implemented for you */
 	@Override
 	public Move getMoveFromSentence(GdlSentence sentence) {
 		return new PropNetMove(sentence);
 	}
 
 	/** Already implemented for you */
 	@Override
 	public MachineState getMachineStateFromSentenceList(Set<GdlSentence> sentenceList) {
 		return new PropNetMachineState(sentenceList);
 	}
 
 	/** Already implemented for you */
 	@Override
 	public Role getRoleFromProp(GdlProposition proposition) {
 		return new PropNetRole(proposition);
 	}
 
 	/** Already implemented for you */
 	@Override
 	public List<Role> getRoles() {
 		return rolesList;
 	}
 
 	/* Helper methods */
 	/**
 	 * The Input propositions are indexed by (does ?player ?action) This
 	 * translates a List of Moves (backed by a sentence that is simply ?action)
 	 * to GdlTerms that can be used to get Propositions from inputPropositions
 	 * and accordingly set their values etc. This is a naive implementation when
 	 * coupled with setting input values, feel free to change this for a more
 	 * efficient implementation.
 	 * 
 	 * @param moves
 	 * @return
 	 */
 	private List<GdlTerm> toDoes(List<Move> moves) {
 		List<GdlTerm> doeses = new ArrayList<GdlTerm>(moves.size());
 		Map<Role, Integer> roleIndices = getRoleIndices();
 
 		for (int i = 0; i < rolesList.size(); i++) {
 			int index = roleIndices.get(rolesList.get(i));
 			doeses.add(ProverQueryBuilder.toDoes(rolesList.get(i), moves.get(index)).toTerm());
 		}
 		return doeses;
 	}
 
 	/**
 	 * Takes in a Legal Proposition and returns the appropriate corresponding
 	 * Move
 	 * 
 	 * @param p
 	 * @return a PropNetMove
 	 */
 
 	public static Move getMoveFromProposition(Proposition p) {
 		return new PropNetMove(p.getName().toSentence().get(1).toSentence());
 	}
 
 	/**
 	 * Helper method for parsing the value of a goal proposition
 	 * 
 	 * @param goalProposition
 	 * @return the integer value of the goal proposition
 	 */
 
 	private int getGoalValue(Proposition goalProposition) {
 		GdlRelation relation = (GdlRelation) goalProposition.getName().toSentence();
 		GdlConstant constant = (GdlConstant) relation.get(1);
 		return Integer.parseInt(constant.toString());
 	}
 
 	/**
 	 * Helper method, used to get compute roles. You should only be using this
 	 * for Role indexing (because of compatibility with the GameServer state
 	 * machine's roles)
 	 * 
 	 * @param description
 	 * @return the list of roles for the current game. Compatible with the
 	 *         GameServer's state machine.
 	 */
 	private List<Role> computeRoles(List<Gdl> description) {
 		List<Role> roles = new ArrayList<Role>();
 		for (Gdl gdl : description) {
 			if (gdl instanceof GdlRelation) {
 				GdlRelation relation = (GdlRelation) gdl;
 				if (relation.getName().getValue().equals("role")) {
 					roles.add(new PropNetRole((GdlProposition) relation.get(0).toSentence()));
 				}
 			}
 		}
 		return roles;
 	}
 	
 	private void computeRoleIndices(List<Role> roles) {
 		roleIndex = new Role[roles.size()];
 		roleMap = new HashMap<Role, Integer>();
 		for (int i = 0; i < roles.size(); i++) {
 			roleIndex[i] = roles.get(i);
 			roleMap.put(roleIndex[i], i);
 		}
 	}
 
 	private void initOperator() {
 		List<Proposition> transitionOrdering = new ArrayList<Proposition>();
 		for (int i = basePropStart; i < inputPropStart; i++) {
 			transitionOrdering.add(propIndex[i]);
 		}
 		
 		List<Proposition> terminalOrdering = getOrdering(new int[]{terminalIndex});
 		
 		List<List<Proposition>> legalOrderings = new LinkedList<List<Proposition>>();
 		for (int i = 0; i < roleIndex.length; i++) {
 			legalOrderings.add(getOrdering(legalPropMap[i]));
 		}
 		
 		List<List<Proposition>> goalOrderings = new LinkedList<List<Proposition>>();
 		for (int role = 0; role < roleIndex.length; role++) {
 			int[][] goalPropsAndValues = goalPropMap[role];
 			int[] goalProps = new int[goalPropsAndValues.length];
 			for (int j = 0; j < goalProps.length; j++) {
 				goalProps[j] = goalPropsAndValues[j][0];
 			}
 			goalOrderings.add(getOrdering(goalProps));
 		}
 		
//		operator = OperatorFactory.buildOperator(propMap, transitionOrdering, defaultOrdering, terminalOrdering, legalOrderings, goalOrderings);
		operator = NativeOperatorFactory.buildOperator(propMap, transitionOrdering, defaultOrdering, terminalOrdering, legalOrderings,
				goalOrderings, legalPropMap, legalInputMap, inputPropStart, inputPropMap.size(), terminalIndex);
 //		operator = new CheckedOperator(propMap, transitionOrdering, defaultOrdering, terminalOrdering, legalOrderings, goalOrderings);
 	}
 	
 	
 
 	private boolean[] generatePropArray(int start, int end) {
 		boolean[] props = new boolean[propIndex.length];
 		for (int i = start; i < end; i++) {
 			props[i] = propIndex[i].getValue();
 		}
 
 		return props;
 	}
 
 	private String booleanArrayToString(boolean[] array) {
 		String str = "[";
 		for (int i = 0; i < array.length; i++) {
 			str += array[i] ? "1" : "0";
 		}
 		str += "]";
 		return str;
 	}
 	
 	public void speedTest() {
 		BooleanMachineState state = getInitialState();
 		
 		long start = System.currentTimeMillis();
 		for (long i = 0; i < 1000; i++) {
             MachineState newState = monteCarlo(state);
 //            System.out.println("Terminal? " + isTerminal(newState));
 		}
 		long end = System.currentTimeMillis();
 		System.out.println((end-start) + " ms");
 	}
 	
 
 	
 	public BooleanMachineState monteCarlo(MachineState state) {
 		if (operator instanceof NativeOperator) {
 			boolean[] props = initBasePropositionsFromState(state);
 			((NativeOperator)operator).monteCarlo(props);
 			return new BooleanMachineState(Arrays.copyOfRange(props, basePropStart, inputPropStart), propIndex);
 		} else {
 			boolean[] props = initBasePropositionsFromState(state);
 			int[] input = new int[legalPropMap.length];
 			while (true) {
 				boolean legal = false;
 				while (!legal) {
 					Arrays.fill(props, inputPropStart, inputPropStart + inputPropMap.size(), false); // set all input props to false
 					for (int role = 0; role < legalPropMap.length; role++) { // set one random input prop to true for each role
 						int index = random.nextInt(legalPropMap[0].length);
 						int inputIndex = legalInputMap[legalPropMap[role][index]];
 						props[inputIndex] = true;
 						input[role] = inputIndex;
 					}
 					operator.propagateInternal(props);
 
 					if (props[terminalIndex]) {
 						return new BooleanMachineState(Arrays.copyOfRange(props, basePropStart, inputPropStart), propIndex);
 					}
 
 					legal = true;
 					for (int role = 0; role < legalPropMap.length; role++) {
 						legal = legal && props[legalInputMap[input[role]]];
 					}
 				}
 				operator.transition(props);
 			}
 		}
 	}
 	
 	@Override
 	public String toString() {
 		int numGoals = 0;
 		for(int[][] role : goalPropMap) {
 			numGoals += role.length;
 		}
 		return "BPNSM with " + (basePropStart - initIndex) + " init, " + (inputPropStart - basePropStart) + " base, " + (internalPropStart - inputPropStart) + " input, " + (propIndex.length - internalPropStart) + " internal " + numGoals + " goals, " + terminalIndex + " term, "; 
 	}
 	
 	/** Factoring logic */
 	
 	public BooleanPropNetStateMachine[] factor() {
 		
 		Log.println('g', "Starting factoring with " + this.toString());
 		
 		BooleanPropNetStateMachine referenceMachine = new BooleanPropNetStateMachine();
 		referenceMachine.initialize(description);
 				
 		Map<Proposition, List<Proposition>> lowestLevel = new HashMap<Proposition, List<Proposition>>();
 		findLowestLevel(referenceMachine.propIndex[referenceMachine.terminalIndex], lowestLevel, new LinkedList<Proposition>());
 		
 		List<Factor> factors = new ArrayList<Factor>();
 		for (Proposition prop : lowestLevel.keySet()) { // tentative root 
 			Factor factor = new Factor(prop);
 			factors.add(factor);
 			
 			// Reverse DFS
 			reverseDFS(prop, factor, referenceMachine, 0);
 		}
 		
 		// Merge if necessary
 		for (int i = 0; i < factors.size(); i++) {
 			for (int j = i + 1; j < factors.size(); j++) {
 				HashSet<Proposition> copy = new HashSet<Proposition>(factors.get(i).internalProps);
 				copy.retainAll(factors.get(j).internalProps);
 				if (copy.size() > 0) { // Should merge
 										
 					// Find most recent ancestor
 					List<Proposition> trail1 = lowestLevel.get(factors.get(i).terminalProp);
 					List<Proposition> trail2 = lowestLevel.get(factors.get(j).terminalProp);
 					while (!trail2.contains(trail1.get(0))) {
 						trail1.remove(0);
 					}
 					Proposition ancestor = trail1.get(0);
 					Factor newFactor = new Factor(ancestor);
 					
 					// Find all factors that have this as an ancestor
 					for (int k = 0; k < factors.size(); k++) {
 						if (lowestLevel.get(factors.get(k).terminalProp).contains(ancestor)) {
 							lowestLevel.remove(factors.get(k).terminalProp);
 							factors.remove(k);
 							k--;
 						}
 					}
 					factors.add(newFactor);
 					reverseDFS(ancestor, newFactor, referenceMachine, 0);
 					Log.println('g', "Merged into " + newFactor);
 					// lowestLevel.put(ancestor, trail1); // Throw this back into the pool of factors
 					
 					// Hacky way to reset loop
 					i = -1;
 					break;
 				}
 			}
 		}
 		
 		// Sanity check: assert that all base and internal propositions in each factor are exclusive
 		for (int i = 0; i < factors.size(); i++) {
 			for (int j = i + 1; j < factors.size(); j++) {
 				HashSet<Proposition> copy = new HashSet<Proposition>(factors.get(i).internalProps);
 				copy.retainAll(factors.get(j).internalProps);
 				assert(copy.size() == 0);
 				copy = new HashSet<Proposition>(factors.get(i).baseProps);
 				copy.retainAll(factors.get(j).baseProps);
 				assert(copy.size() == 0);
 			}
 		}
 		
 		// Set name of each factor's root to terminal 
 		for (Factor factor : factors) {
 			factor.terminalProp.setName(referenceMachine.propIndex[referenceMachine.terminalIndex].getName());
 		}
 		
 		// TODO By assumption, goals are disjunctively factorable and descendants of terminal
 		
 		// Add goals
 		for (int role = 0; role < referenceMachine.goalPropMap.length; role++) {
 			for (int[] goalProp : referenceMachine.goalPropMap[role]) {
 				int[] numFactorsFound = new int[]{0};
 				addGoals(referenceMachine.propIndex[goalProp[0]], referenceMachine.propIndex[goalProp[0]], factors, referenceMachine.roleIndex[role], new LinkedList<Component>(), numFactorsFound);
 			}
 		}
 		
 		// Ensure all inputs are present first
 		for (Factor factor : factors) {
 			for (int role = 0; role < referenceMachine.legalPropMap.length; role++) {
 				int[] legals = referenceMachine.legalPropMap[role];
 				for (int legal = 0; legal < legals.length; legal++) {
 					if (!factor.inputProps.contains(referenceMachine.propIndex[referenceMachine.legalInputMap[legals[legal]]])) {
 						factor.inputProps.add(referenceMachine.propIndex[referenceMachine.legalInputMap[legals[legal]]]);
 						factor.components.add(referenceMachine.propIndex[referenceMachine.legalInputMap[legals[legal]]]);
 						factor.components.add(referenceMachine.propIndex[legals[legal]]);
 					}
 				}
 			}
 		}
 		
 		// Input consolidation
 		
 		Map<Proposition, List<Proposition>> highestLevel = new HashMap<Proposition, List<Proposition>>();
 		for (int inputProp = inputPropStart; inputProp < internalPropStart; inputProp++) {
 			findHighestLevel(referenceMachine.propIndex[inputProp], highestLevel, referenceMachine.propIndex[inputProp]);
 		}
 		
 		String temp = "";
 		for (Proposition reachableProp : highestLevel.keySet()) {
 			if (highestLevel.get(reachableProp).size() > 1) {
 				temp += reachableProp.getName() + " in factor ";
 				for (int j = 0; j < factors.size(); j++) {
 					if (factors.get(j).internalProps.contains(reachableProp)) {
 						temp += j;
 					}
 				}
 				temp += " " + highestLevel.get(reachableProp).size() + ";";
 			}
 		}
 		Log.println('g', "Input lists " + temp);
 		
 
 		for (Proposition reachableProp : highestLevel.keySet()) {
 			if (highestLevel.get(reachableProp).size() > 1) {
 				for (Factor factor : factors) {
 					if (factor.internalProps.contains(reachableProp)) {
 						// Keep only one path
 						Component connector = reachableProp.getSingleInput();
 						Component randomInputPath = connector.getInputs().iterator().next();
 						for (Component input : connector.getInputs()) {
 							//Log.println('g', "Removing " + input + " " + randomInputPath + " " + input.hashCode() + " in factor " + k);
 							if (input == randomInputPath) {
 								continue;
 							}
 							Component currentToHide = input;
 							factor.components.remove(currentToHide);
 							while (true) {
 								currentToHide = currentToHide.getSingleInput();
 								factor.components.remove(currentToHide);
 								if (currentToHide.getInputs().size() == 0) { // Reached an input prop
 									factor.inputProps.remove(currentToHide);
 									break;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		// Add legals
 		for (Factor factor : factors) {
 			for (Proposition inputProp : factor.inputProps) {
 				// Find legal corresponding to each goal
 				Proposition legalProp = referenceMachine.propIndex[referenceMachine.legalInputMap[referenceMachine.propMap.get(inputProp)]];
 				reverseDFS(legalProp, factor, referenceMachine, 0);
 				factor.legalInputMap.put(legalProp, inputProp);
 				factor.legalInputMap.put(inputProp, legalProp);
 			}
 		}
 		/*
 		for (int i = 0; i < factors.size(); i++) {
 			Log.println('g', "Factor " + i + " has " + factors.get(i).inputProps.size() + " inputs");
 			factors.get(i).legalInputMap = referenceMachine.legalInputMap;
 		}
 		*/
 		
 		// TODO Fix Init hack around
 		
 		Set<Component> alreadyVisited = new HashSet<Component>();
 		List<Component> toVisit = new LinkedList<Component>();
 		// BFS
 		int depth = 0;
 		toVisit.add(referenceMachine.propIndex[referenceMachine.initIndex]);
 		while (true) {
 			List<Component> willVisit = new LinkedList<Component>();
 			while (!toVisit.isEmpty()) {
 				Component curr = toVisit.remove(0);	
 				int factor = -1;
 				for (int i = 0; i < factors.size(); i++) {
 					if (factors.get(i).components.contains(curr)) {
 						factor = i;
 						break;
 					}
 				}
 
 				if (!alreadyVisited.contains(curr)) {
 					Log.println('g', String.format("%" + (depth + 1) + "s :%2d:" + (curr instanceof Proposition ? ((Proposition) curr).getName() : curr.getClass().getName()) + curr.hashCode() + " with " + curr.getInputs().size() + " inputs, " + curr.getOutputs().size() + " outputs", " ", factor));
 				
 					alreadyVisited.add(curr);
 					for (Component next : curr.getOutputs()) {
 						willVisit.add(next);
 					}
 				}
 				else {
 					Log.println('g', String.format("%" + (depth + 1) + "s :%2d: visited " + (curr instanceof Proposition ? ((Proposition) curr).getName() : curr.getClass().getName()) + curr.hashCode() + " with " + curr.getInputs().size() + " inputs, " + curr.getOutputs().size() + " outputs", " ", factor));
 				}
 			}
 			if (willVisit.isEmpty()) {
 				break;
 			}
 			toVisit = willVisit;
 			depth++;
 		}
 		
 		// Add entire init subtree to all factors
 		for (Factor factor : factors) {
 			factor.components.addAll(alreadyVisited);
 			for (Component comp : alreadyVisited) {
 				if (comp instanceof Proposition) {
 					factor.internalProps.add((Proposition) comp);
 				}
 			}
 		}
 		
 		alreadyVisited = new HashSet<Component>();
 		toVisit = new LinkedList<Component>();
 		// BFS
 		depth = 0;
 		toVisit.add(referenceMachine.propIndex[referenceMachine.terminalIndex]);
 		while (true) {
 			List<Component> willVisit = new LinkedList<Component>();
 			while (!toVisit.isEmpty()) {
 				Component curr = toVisit.remove(0);	
 				
 				int factor = -1;
 				for (int i = 0; i < factors.size(); i++) {
 					if (factors.get(i).components.contains(curr)) {
 						factor = i;
 						break;
 					}
 				}
 				
 				Log.println('g', String.format("%" + (depth + 1) + "s :%2d:" + (curr instanceof Proposition ? ((Proposition) curr).getName() : curr.getClass().getName()) + " with " + curr.getInputs().size() + " inputs, " + curr.getOutputs().size() + " outputs", " ", factor));
 				
 				alreadyVisited.add(curr);
 				for (Component next : curr.getInputs()) {
 					if (!alreadyVisited.contains(next)) {
 						willVisit.add(next);
 					}
 				}
 			}
 			if (willVisit.isEmpty()) {
 				break;
 			}
 			toVisit = willVisit;
 			depth++;
 		}
 		// Generate the new statemachines
 		BooleanPropNetStateMachine[] minions = new BooleanPropNetStateMachine[factors.size()];
 		for (int i = 0; i < factors.size(); i++) {
 			minions[i] = new BooleanPropNetStateMachine();
 			minions[i].initialize(factors.get(i).components, rolesList);
 			minions[i].pnet.renderToFile(PNET_FOLDER + File.separator + "factor" + i + ".dot");			
 			Log.println('f', "Factor " + i + " : " + minions[i].toString());
 			for (int role = 0; role < minions[i].legalPropMap.length; role++) {
 				for (int legal = 0; legal < minions[i].legalPropMap[role].length; legal++) {
 					Log.println('f', "Factor " + i + " legal " + legal + " for " + role + " : " + minions[i].propIndex[minions[i].legalPropMap[role][legal]] + " " + minions[i].legalInputMap[minions[i].legalPropMap[role][legal]]);
 				}
 			}
 			MachineState initialState = minions[i].getInitialState();
 		}
 		
 		return minions;
 	}
 	
 	// Has the potential to search the entire supertree of goalProp
 	private void addGoals(Proposition prop, Proposition goalProp, List<Factor> factors, Role role, List<Component> trail, int[] numFactorsFound) {
 		if (numFactorsFound[0] == factors.size()) {
 			return;
 		}
 		Log.println('g', "exploring " + prop.getName() + " with goal " + goalProp.getName());
 		for (Factor factor : factors) {
 			if (factor.internalProps.contains(prop)) { // Proposition intersects factored tree; should only happen once
 				Log.println('g', "Found goal " + prop.getName());
 				if (!factor.goalProps.containsKey(role)) {
 					factor.goalProps.put(role, new HashSet<Proposition>());
 				}
 				factor.goalProps.get(role).add(goalProp);
 				for (Component c : trail) {
 					if (c instanceof Proposition) {
 						factor.internalProps.add((Proposition) c);
 					}
 					factor.components.add(c);
 				}
 				numFactorsFound[0]++;
 				return; // Only one factor will contain prop
 			}
 		}
 		if (prop.getInputs().size() == 0) {
 			return;
 		}
 		Component comp = prop.getSingleInput();
 		for (Component higherProp : comp.getInputs()) {
 			List<Component> trailCopy = new LinkedList<Component>(trail);
 			trailCopy.add(prop);
 			trailCopy.add(comp);
 			addGoals((Proposition) higherProp, goalProp, factors, role, trailCopy, numFactorsFound);
 		}
 	}
 	
 	private void reverseDFS(Proposition prop, Factor factor, BooleanPropNetStateMachine referenceMachine, int depth) {
 		int index = referenceMachine.propMap.get(prop);
 		if (index >= inputPropStart && index < internalPropStart) {
 			Log.println('g', String.format("%" + (depth + 1) + "s%s", " ", "Reached " + prop.getName() + prop.hashCode() + " " + prop.getInputs().size() + " inputs, " + prop.getOutputs().size() + " outputs, " + " input"));
 			factor.inputProps.add(prop);
 			factor.components.add(prop);
 			return;
 		}	
 		boolean continueSearching = prop.getInputs().size() == 1;
 		if (index >= basePropStart && index < inputPropStart) {
 			if (factor.baseProps.add(prop)) { 
 				factor.components.add(prop);
 			}
 			else {
 				continueSearching = false;
 			}
 		}
 		else if(!factor.internalProps.contains(prop)) {
 			int numMatches = 0;
 			for (Proposition p : factor.internalProps) {
 				if (p.hashCode() == prop.hashCode()) {
 					numMatches++;
 				}
 			}
 			if (numMatches != 0) {
 				System.err.println("Problem ");
 			}
 			factor.internalProps.add(prop);
 			factor.components.add(prop);
 		}
 		else {
 			continueSearching = false;
 		}
 		Log.println('g', String.format("%" + (depth + 1) + "s%s", " ", "Reached " + prop.getName() + prop.hashCode() + " " + prop.getInputs().size() + " inputs, " + prop.getOutputs().size() + " outputs, " + (continueSearching ? "" : " stopping")));
 		if (continueSearching) { // Not a spurious proposition
 			Component comp = prop.getSingleInput();
 			factor.components.add(comp);
 			Log.println('g', String.format("%" + (depth + 1) + "s%s", " ", "Expanding " + comp.getClass().getName() + comp.hashCode() + " " + comp.getInputs().size() + " inputs"));
 			for (Component input : comp.getInputs()) {
 				reverseDFS((Proposition)input, factor, referenceMachine, depth + 1);
 			}
 		}
 
 	}
 	
 	// TODO Such a hack! Works only for special versions of lights on
 	
 	private boolean findHighestLevel(Proposition prop, Map<Proposition, List<Proposition>> level, Proposition inputProp) {
 		/*
 		Log.println('g', "At prop " + prop.getName());
 		if (!level.containsKey(prop)) {
 			level.put(prop, new LinkedList<Proposition>());
 		}
 		level.get(prop).add(inputProp);
 		Component comp = prop.getSingleOutput();
 		if (comp instanceof And || comp instanceof Transition) {
 			if (comp.getOutputs().size() == 1) {
 				findHighestLevel((Proposition)comp.getSingleOutput(), level, inputProp);
 			} 
 		} else if (comp instanceof Or) {
 			for (Component input : comp.getOutputs()) {
 				findHighestLevel((Proposition)input, level, inputProp);
 			}
 		}
 		*/
 		//Log.println('g', "At prop " + prop.getName());
 		if (prop.getOutputs().size() == 0) {
 			return false;
 		}
 		Component comp = prop.getSingleOutput();
 		if (comp.getInputs().size() > 1) {
 			Proposition disjunctiveNode = (Proposition) comp.getSingleOutput();
 			if (!level.containsKey(disjunctiveNode)) {
 				level.put(disjunctiveNode, new LinkedList<Proposition>());
 			}
 			level.get(disjunctiveNode).add(inputProp);
 			return true;
 		}
 		if (comp instanceof And || comp instanceof Transition) {
 			if (comp.getOutputs().size() == 1) {
 				if (findHighestLevel((Proposition)comp.getSingleOutput(), level, inputProp))
 					return true;
 			} 
 		} else if (comp instanceof Or) {
 			for (Component output : comp.getOutputs()) {
 				if (findHighestLevel((Proposition)output, level, inputProp))
 					return true;
 			}
 		}
 		return false;
 		
 	}
 	
 	private void findLowestLevel(Proposition prop, Map<Proposition, List<Proposition>> level, LinkedList<Proposition> trail) {
 		Component comp = prop.getSingleInput();
 		if (comp instanceof And) {
 			if (comp.getInputs().size() == 1) {
 				trail.add(prop);
 				findLowestLevel((Proposition)comp.getSingleInput(), level, trail);
 				trail.remove(prop);
 			} else {
 				level.put(prop, new LinkedList<Proposition>(trail));
 			}
 		} else if (comp instanceof Or) {
 			for (Component input : comp.getInputs()) {
 				trail.add(prop);
 				findLowestLevel((Proposition)input, level, trail);
 				trail.remove(prop);
 			}
 		} else {
 			level.put(prop, new LinkedList<Proposition>(trail));
 			
 		}
 	}
 	
 	
 	private class Factor {
 		public Proposition terminalProp;
 		public Map<Role, Set<Proposition>> goalProps;
 		public Map<Proposition, Proposition> legalInputMap;
 		public Set<Proposition> baseProps;
 		public Set<Proposition> inputProps;
 		public Set<Component> components;
 		//assert(everything will be OK) /*of course, the assert fails*/
 		public Set<Proposition> internalProps;
 		public Factor(Proposition terminalProp) {
 			this.terminalProp = terminalProp;
 			this.components = new HashSet<Component>();
 			this.goalProps = new HashMap<Role, Set<Proposition>>();
 			this.internalProps = new HashSet<Proposition>();
 			this.baseProps = new HashSet<Proposition>();
 			this.inputProps = new HashSet<Proposition>();
 			this.legalInputMap = new HashMap<Proposition, Proposition>();
 		}
 		
 		@Override
 		public String toString() {
 			int numGoals = 0;
 			for(Role r : goalProps.keySet()) {
 				numGoals += goalProps.get(r).size();
 			}
 			return "Factor rooted at " + terminalProp.getName() + " with " + components.size() + " components, " + internalProps.size() + " internal props, " + baseProps.size() + " base props, " + inputProps.size() + " input props, " + numGoals + " goal props";
 		
 		}
 	}
 }
