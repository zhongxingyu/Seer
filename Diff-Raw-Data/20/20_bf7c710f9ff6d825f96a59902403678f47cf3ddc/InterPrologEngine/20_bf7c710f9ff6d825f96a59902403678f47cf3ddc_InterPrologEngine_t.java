 package org.jpc.engine.interprolog;
 
import static org.jpc.JpcPreferences.JPC_ANON_VAR_PREFIX;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jpc.Jpc;
 import org.jpc.engine.prolog.AbstractPrologEngine;
 import org.jpc.error.PrologParsingException;
 import org.jpc.term.Atom;
 import org.jpc.term.Term;
 import org.jpc.term.interprolog.InterPrologTermWrapper;
 
 import com.declarativa.interprolog.PrologEngine;
 import com.declarativa.interprolog.TermModel;
 import com.declarativa.interprolog.util.VariableNode;
 
 public abstract class InterPrologEngine extends AbstractPrologEngine {
 	
	public static final String READ_ATOM_TO_TERM_TERM = JPC_ANON_VAR_PREFIX + "READ_ATOM_TO_TERM_TERM"; //the variable name used by the second argument of read_atom_to_term
	public static final String READ_ATOM_TO_TERM_VARS = JPC_ANON_VAR_PREFIX + "READ_ATOM_TO_TERM_VARS"; //the variable name used by the third argument of read_atom_to_term
 	
 	
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
 	public Term asTerm(String termString, Jpc context) {
 		String escapedTermString = new Atom(termString).toEscapedString();
 		String query = "read_atom_to_term(" + escapedTermString + ", " + READ_ATOM_TO_TERM_TERM  + ", "+ READ_ATOM_TO_TERM_VARS + ")";
 		Object bindings[] = wrappedEngine.deterministicGoal(query, null);
 		if(bindings == null)
 			throw new PrologParsingException(termString);
 		TermModel queryTermModel = (TermModel) bindings[0];
 		TermModel queryAsTerm = (TermModel) queryTermModel.getChild(1);
 		TermModel varDictionaryTerm = (TermModel) queryTermModel.getChild(2);
 		Map<Integer, String> variablesNames = getVariablesNames(varDictionaryTerm);
 		InterPrologTermWrapper interPrologTermWrapper = new InterPrologTermWrapper(queryAsTerm, variablesNames);
 		return InterPrologBridge.fromInterPrologToJpc(interPrologTermWrapper);
 	}
 	
 	public static Map<Integer, String> getVariablesNames(TermModel varDictionaryTerm) {
 		Map<Integer, String> variablesNames = new HashMap<>();
 		while(! (varDictionaryTerm.node instanceof VariableNode) ) {
 			TermModel bindingTerm = (TermModel) varDictionaryTerm.getChild(0);
 			String name = (String)((TermModel)bindingTerm.getChild(0)).node;
 			VariableNode variable = (VariableNode)((TermModel)bindingTerm.getChild(1)).node;
 			variablesNames.put(InterPrologTermWrapper.getVariableCode(variable), name);
 			varDictionaryTerm = (TermModel) varDictionaryTerm.getChild(1);
 		}
 		return variablesNames;
 	}
 
 }
