 package org.jpc.engine.interprolog;
 
 import static org.jpc.JpcPreferences.JPC_VAR_PREFIX;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jpc.Jpc;
 import org.jpc.engine.prolog.AbstractPrologEngine;
 import org.jpc.query.Query;
 import org.jpc.term.Atom;
 import org.jpc.term.Term;
 import org.jpc.term.interprolog.InterPrologTermWrapper;
 
 import com.declarativa.interprolog.PrologEngine;
 import com.declarativa.interprolog.TermModel;
 import com.declarativa.interprolog.util.VariableNode;
 
 public class InterPrologEngine extends AbstractPrologEngine {
 	
 	public static final String READ_ATOM_TO_TERM_TERM = JPC_VAR_PREFIX + "READ_ATOM_TO_TERM_TERM"; //the variable name used by the second argument of read_atom_to_term
 	public static final String READ_ATOM_TO_TERM_VARS = JPC_VAR_PREFIX + "READ_ATOM_TO_TERM_VARS"; //the variable name used by the third argument of read_atom_to_term
 	
 	
 	private PrologEngine wrappedEngine;
 	
 	public InterPrologEngine(PrologEngine wrappedEngine) {
 		this.wrappedEngine = wrappedEngine;
 	}
 
 	public PrologEngine getWrappedEngine() {
 		return wrappedEngine;
 	}
 
 	@Override
 	public void close() {
 		wrappedEngine.shutdown();
 	}
 
 	@Override
 	public boolean isCloseable() {
 		return true;
 	}
 	
 	@Override
 	public boolean isMultiThreaded() {
 		return false;
 	}
 	
 	@Override
 	public Term asTerm(String termString) {
 		String escapedTermString = new Atom(termString).toEscapedString();
 		String query = "read_atom_to_term(" + escapedTermString + ", " + READ_ATOM_TO_TERM_TERM  + ", "+ READ_ATOM_TO_TERM_VARS + ")";
 		Object bindings[] = wrappedEngine.deterministicGoal(query, null);
 		TermModel queryTermModel = (TermModel) bindings[0];
 		TermModel queryAsTerm = (TermModel) queryTermModel.getChild(1);
 		TermModel variablesTuplesTerm = (TermModel) queryTermModel.getChild(2);
 		
 		Map<Integer, String> variablesNames = new HashMap<>();
 		while(! (variablesTuplesTerm.node instanceof VariableNode) ) {
 			TermModel bindingTerm = (TermModel) variablesTuplesTerm.getChild(0);
 			String name = (String)((TermModel)bindingTerm.getChild(0)).node;
 			VariableNode variable = (VariableNode)((TermModel)bindingTerm.getChild(1)).node;
 			variablesNames.put(InterPrologTermWrapper.getVariableCode(variable), name);
 			variablesTuplesTerm = (TermModel) variablesTuplesTerm.getChild(1);
 		}
 		InterPrologTermWrapper interPrologTermWrapper = new InterPrologTermWrapper(queryAsTerm, variablesNames);
 		return InterPrologBridge.fromInterPrologToJpc(interPrologTermWrapper);
 	}
 	
 	@Override
	public Query basicQuery(Term goal, boolean errorHandledQuery, Jpc context) {
		return new InterPrologQuery(this, goal, errorHandledQuery, context);
 	}
 
 }
