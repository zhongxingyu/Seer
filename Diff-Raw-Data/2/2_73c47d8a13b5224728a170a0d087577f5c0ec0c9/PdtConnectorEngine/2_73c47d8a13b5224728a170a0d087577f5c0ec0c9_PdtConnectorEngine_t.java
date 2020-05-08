 package org.jpc.engine.pdtconnector;
 
 import org.cs3.prolog.cterm.CTerm;
 import org.cs3.prolog.cterm.CTermUtil;
 import org.cs3.prolog.pif.PrologInterface;
 import org.cs3.prolog.pif.PrologInterfaceException;
 import org.jpc.Jpc;
 import org.jpc.engine.prolog.AbstractPrologEngine;
 import org.jpc.error.PrologParsingException;
 import org.jpc.query.Query;
 import org.jpc.term.Term;
 
 public class PdtConnectorEngine extends AbstractPrologEngine {
 
 	private PrologInterface wrappedEngine; 
 	
 	public PdtConnectorEngine(PrologInterface wrappedEngine) {
 		this.wrappedEngine = wrappedEngine;
 	}
 	
 	public PrologInterface getWrappedEngine() {
 		return wrappedEngine;
 	}
 	
 	@Override
 	public void close() {
 		try {
 			wrappedEngine.stop();
 		} catch (PrologInterfaceException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public boolean isCloseable() {
 		return true;
 	}
 	
 	@Override
 	public boolean isMultiThreaded() {
 		return true;
 	}
 
 	@Override
 	public Term asTerm(String termString, Jpc context) { //TODO delete context here
 		try {
			CTerm pdtTerm = CTermUtil.parseNonCanonicalTerm(termString, wrappedEngine);
 			return PdtConnectorBridge.fromPdtConnectorToJpc(pdtTerm);
 		} catch(Exception e) {
 			throw new PrologParsingException(termString, e);
 		}
 	}
 
 	@Override
 	protected Query basicQuery(Term goal, Jpc context) {
 		return new PdtConnectorQuery(this, goal, context);
 	}
 
 }
