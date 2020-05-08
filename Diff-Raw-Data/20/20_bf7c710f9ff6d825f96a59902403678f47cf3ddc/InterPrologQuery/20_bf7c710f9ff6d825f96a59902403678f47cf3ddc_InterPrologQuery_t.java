 package org.jpc.engine.interprolog;
 
 import static java.util.Arrays.asList;
import static org.jpc.JpcPreferences.JPC_ANON_VAR_PREFIX;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NoSuchElementException;
 
 import org.jpc.Jpc;
 import org.jpc.query.DeterministicPrologQuery;
 import org.jpc.query.QuerySolution;
 import org.jpc.term.Compound;
 import org.jpc.term.Term;
 import org.jpc.term.Variable;
 import org.jpc.term.interprolog.InterPrologTermWrapper;
 import org.jpc.util.PrologUtil;
 
 import com.declarativa.interprolog.TermModel;
 import com.declarativa.interprolog.util.VariableNode;
 
 /**
  * This class models an InterProlog query.
  * In InterProlog it does not seem to exist a straightforward way to ask for the bindings of all variables present in the query (given that such bindings are not known in advance).
  * Therefore, a query goal should be instrumented in order to generate such dictionary of bindings indexed by variable names.
  * This instrumentation may depend on the underlying Prolog engine.
  * @author sergioc
  *
  */
 public abstract class InterPrologQuery extends DeterministicPrologQuery {
 
 	private com.declarativa.interprolog.PrologEngine wrappedInterPrologEngine;
 
	public static final String ALL_VARIABLES = JPC_ANON_VAR_PREFIX + "ALL_VARIABLES";
 	public static final String ALL_VARIABLES_TERM_MODEL = ALL_VARIABLES + "_TERM_MODEL";
	public static final String INSTRUMENTED_GOAL = JPC_ANON_VAR_PREFIX + "INSTRUMENTED_GOAL";
 	
 	public InterPrologQuery(InterPrologEngine prologEngine, Term goal, boolean errorHandledQuery, Jpc context) {
 		super(prologEngine, goal, errorHandledQuery, context);
 		wrappedInterPrologEngine = prologEngine.getWrappedEngine();
 	}
 
 	/**
 	 * This method executes an instrumented goal in a Prolog engine.
 	 * The executed goal contains a dictionary of variable names to terms bound to a variable identified by the value of ALL_VARIABLES_TERM_MODEL.
 	 * The structure of the dictionary is implementation dependent. 
 	 * The method getRawInterPrologVarDictionary() provides a common implementation for converting a dictionary term into a Java map. This method should be overridden if a term dictionary with another structure is used.
 	 */
 	@Override
 	protected QuerySolution basicOneSolutionOrThrow() {
 		//InterPrologTermWrapper termWrapper = InterPrologBridge.fromJpcToInterProlog(getInstrumentedGoal());
 		//TermModel interPrologQuery = termWrapper.getTermModel();
 		Object[] results = wrappedInterPrologEngine.deterministicGoal(getInstrumentedGoal().toString(), "["+ALL_VARIABLES_TERM_MODEL+"]");
 		if(results == null)
 			throw new NoSuchElementException();
 		TermModel engineDependentVarDictionary = (TermModel) results[0];
 		Map<Integer, String> variablesNames = new HashMap<>();
 		Map<String, InterPrologTermWrapper> interPrologBindings = new HashMap<>(); //a solution to the query mapping variable names to InterPrologWrapper terms
 		for(Entry<String, TermModel> entry : getRawInterPrologVarDictionary(engineDependentVarDictionary).entrySet()) {
 			String name = entry.getKey();
 			TermModel interPrologTerm = entry.getValue();
 			if(interPrologTerm.node instanceof VariableNode) {
 				VariableNode variable = (VariableNode)interPrologTerm.node;
 				variablesNames.put(InterPrologTermWrapper.getVariableCode(variable), name);
 			}
 			interPrologBindings.put(name, new InterPrologTermWrapper(interPrologTerm, variablesNames));
 		}
 		Map<String, Term> oneSolution = new HashMap<>();
 		for(Entry<String, InterPrologTermWrapper> interPrologBinding : interPrologBindings.entrySet()) {
 			Term term = InterPrologBridge.fromInterPrologToJpc(interPrologBinding.getValue());
 			oneSolution.put(interPrologBinding.getKey(), term);
 		}
 		return new QuerySolution(oneSolution, getPrologEngine(), getJpcContext());
 	}
 	
 	/**
 	 * This method knows how to extract from an InterProlog term a dictionary binding variable names to terms.
 	 * By default, it is assumed that the dictionary term has a structure like: [f(Name,Var), ...]
 	 * Where f is an arbitrary functor name, Name is the name of a variable and Var the variable itself.
 	 * @param varDictionaryTerm an instrumented query goal.
 	 * @return a dictionary binding variable names to terms.
 	 */
 	protected Map<String, TermModel> getRawInterPrologVarDictionary(TermModel varDictionaryTerm) {
 		TermModel[] unifications = varDictionaryTerm.flatList();
 		Map<String, TermModel> dict = new HashMap<>();
 		for(TermModel unification : unifications) {
 			String name = (String)((TermModel)unification.getChild(0)).node;
 			TermModel interPrologTerm = (TermModel)((TermModel)unification.getChild(1));
 			dict.put(name, interPrologTerm);
 		}
 		return dict;
 	}
 	
 //	@Override
 //	protected Term instrumentGoal(Term goal) {
 //		return instrumentParsedGoal(goal);
 //	}
 	
 	/**
 	 * This method provides a straightforward but inefficient goal instrumentation for InterProlog.
 	 * It requires the query goal to be a term object (this may imply a connection to the Prolog engine to parse a text goal).
 	 * Once the term object representing the goal has been generated, a dictionary of variable names to unbound variables is appended at the beginning of the query.
 	 * This method is deprecated and may be removed in a future version.
 	 * @deprecated
 	 * @param goal
 	 * @return
 	 */
 	protected Term instrumentParsedGoal(Term goal) {
 		goal = super.instrumentGoal(goal);
 		Term mapVarsNamesTerm = PrologUtil.varDictionaryTerm(goal);
 		Compound allVarsUnification = new Compound("=", asList(new Variable(ALL_VARIABLES), mapVarsNamesTerm));
 		Term instrumentedGoal = new Compound(",", asList(allVarsUnification, goal));
 		Term buildTermModel = new Compound("buildTermModel", asList(new Variable(ALL_VARIABLES), new Variable(ALL_VARIABLES_TERM_MODEL)));
 		return new Compound(",", asList(instrumentedGoal, buildTermModel));
 	}
 	
 	@Override
 	public boolean isAbortable() {
 		return true;
 	}
 	
 	@Override
 	protected void basicAbort() {
 		((InterPrologEngine)getPrologEngine()).getWrappedEngine().interrupt();
 		super.basicAbort();
 	}
 	
 }
