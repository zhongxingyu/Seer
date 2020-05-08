 package org.jpc.engine.pdtconnector;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NoSuchElementException;
 
 import org.cs3.prolog.cterm.CTerm;
 import org.cs3.prolog.pif.PrologInterface;
 import org.cs3.prolog.pif.PrologInterfaceException;
 import org.cs3.prolog.session.PrologSession;
 import org.jpc.Jpc;
 import org.jpc.query.DeterministicPrologQuery;
 import org.jpc.query.QuerySolution;
 import org.jpc.term.Term;
 
 public class PdtConnectorQuery extends DeterministicPrologQuery {
 
 	private PrologInterface wrappedPdtEngine;
 	private String queryString;
 	
	public PdtConnectorQuery(PdtConnectorEngine prologEngine, Term goal, boolean errorHandledQuery, Jpc context) {
		super(prologEngine, goal, errorHandledQuery, context);
 		wrappedPdtEngine = prologEngine.getWrappedEngine();
 		queryString = getInstrumentedGoal().toEscapedString();
 	}
 
 	@Override
 	protected QuerySolution basicOneSolutionOrThrow() {
 		Map<String, Object> pdtBindings;
 		PrologSession session = null;
 		try {
 			session = wrappedPdtEngine.getSession(PrologInterface.CTERMS);
 			pdtBindings = session.queryOnce(queryString);
 			if(pdtBindings == null)
 				throw new NoSuchElementException();
 		} catch (PrologInterfaceException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if(session != null)
 				session.dispose();
 		}
 		Map<String, Term> oneSolution = new HashMap<>();
 		for(Entry<String, Object> pdtBinding : pdtBindings.entrySet()) {
 			CTerm pdtTerm = (CTerm) pdtBinding.getValue();
 			Term term = PdtConnectorBridge.fromPdtConnectorToJpc(pdtTerm);
 			oneSolution.put(pdtBinding.getKey(), term);
 		}
 		return new QuerySolution(oneSolution, getPrologEngine(), getJpcContext());
 	}
 	
 	@Override
 	public boolean isAbortable() {
 		return false;
 	}
 
 	@Override
 	protected void basicAbort() {
 		throw new UnsupportedOperationException();
 	}
 
 }
