 package util.statemachine.implementation.propnet;
 
 import java.awt.print.Printable;
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
 
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtMethod;
 import javassist.CtNewMethod;
 import javassist.NotFoundException;
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
 import util.propnet.architecture.components.Constant;
 import util.propnet.architecture.components.Not;
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
 	private int[] legalInputMap; //this should probably be two hashmaps >.<
 	
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
 		pnet = CachedPropNetFactory.create(description);
 		rolesList = computeRoles(description);
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
 		pnet.renderToFile("D:\\Code\\Stanford\\cs227b_svn\\logs\\test.out");
 
 		initOperator();
 		
 		/*
 		for (int i = 0; i < propIndex.length; i++) {
 			Log.println('c', "Prop " + i + ": " + propIndex[i].getName());
 		}
 		*/
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
 		return new BooleanMachineState(Arrays.copyOfRange(props, basePropStart, inputPropStart),
 				propIndex);
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
 
 	// private void propagateInternalOnly() {
 	//		
 	// // All the input propositions are set, update all propositions in order
 	// for (Proposition prop : ordering) {
 	// prop.setValue(prop.getSingleInput().getValue());
 	// }
 	// }
 	//
 	// private void propagate() {
 	// boolean[] props = operator.propagate(generatePropArray());
 	// propagateInternalOnly();
 	//
 	// // All the internal propositions are updated, update all base
 	// // propositions
 	// for (Proposition baseProposition : basePropositions.values()) {
 	// baseProposition.setValue(baseProposition.getSingleInput().getValue());
 	// }
 	//		
 	// for (int i = 0; i < booleanOrdering.length; i++) {
 	// if (booleanOrdering[i].getValue() != props[i]) {
 	// System.err.println("INCORRECT!");
 	// }
 	// }
 	// }
 
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
 		
 		operator = OperatorFactory.buildOperator(propMap, transitionOrdering, defaultOrdering, terminalOrdering, legalOrderings, goalOrderings);
 //		operator = NativeOperatorFactory.buildOperator(propMap, transitionOrdering, defaultOrdering, terminalOrdering, legalOrderings, goalOrderings);
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
 //		BooleanMachineState state = getInitialState();
 //		boolean[] props = initBasePropositionsFromState(state);
 //		
 //		long start = System.currentTimeMillis();
 //		for (long i = 0; i < 1000000; i++) {
 //            operator.propagate(props);
 //		}
 //		long end = System.currentTimeMillis();
 //		System.out.println((end-start) + " ms");
 //		System.exit(0);
 	}
 	
 
 	
 	public BooleanMachineState monteCarlo(MachineState state) {
 		boolean[] props = initBasePropositionsFromState(state);
 		int[] input = new int[legalPropMap.length];
 		while (true) {
 			boolean legal = false;
 			while (!legal) {
 				Arrays.fill(props, inputPropStart, inputPropStart + inputPropMap.size(), false); // set all input props to false
 				for (int role = 0; role < legalPropMap.length; role++) {  // set one random input prop to true for each role
 					int index = random.nextInt(legalPropMap[0].length);
 					int inputIndex = legalInputMap[ legalPropMap[role][index] ];
 					props[inputIndex] = true;
 					input[role] = inputIndex;
 				}
 				operator.propagateInternal(props);
 				
 				if (props[terminalIndex])
 					return new BooleanMachineState(Arrays.copyOfRange(props, basePropStart, inputPropStart), propIndex);
 				
 				legal = true;
 				for (int role = 0; role < legalPropMap.length; role++) {
 					legal = legal && props[ legalInputMap[ input[role] ] ];
 				}
 			}
 			operator.transition(props);
 		}
 	}
 }
