 package org.jpc.engine.jpl;
 
 import static org.jpc.engine.prolog.ThreadModel.SINGLE_THREADED;
 
 import org.jpc.Jpc;
 import org.jpc.engine.prolog.AbstractPrologEngine;
 import org.jpc.engine.prolog.ThreadModel;
 import org.jpc.error.PrologParsingException;
 import org.jpc.error.SyntaxError;
 import org.jpc.query.Query;
 import org.jpc.term.Term;
 import org.jpc.util.JpcPreferences;
 import org.jpc.util.engine.PrologResourceLoader;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JplEngine extends AbstractPrologEngine {
 
 	private static final Logger logger = LoggerFactory.getLogger(JplEngine.class);
 	
 	static JplEngine prologEngine; //the default Jpl Prolog engine. This variable is static since only one JPL Prolog engine can be created per JVM.
 	
 	public static final String JPL_PROLOG_RESOURCES = JpcPreferences.JPC_PROLOG_RESOURCES + "jpl/";
 	public static final String JPL_LOGTALK_LOADER_FILE = JPL_PROLOG_RESOURCES + "load_all.lgt";
 	
 	public static JplEngine getPrologEngine() {
 		return prologEngine;
 	}
 	
 	private final ThreadModel threadModel; 
 	
 	JplEngine(ThreadModel threadModel) {
 		this.threadModel = threadModel;
 	}
 	
 	@Override
 	public void close() {
 		throw new UnsupportedOperationException();
 //		logger.info("Shutting down the JPL prolog engine ...");
 //		boolean result = query(new Atom("halt")).hasSolution(); //WARNING: the Java process would also dye. Commented out until finding another way to halt the JPL logic engine.
 //		if(result)
 //			logger.info("A Jpl prolog engine has been shut down.");
 //		else
 //			logger.warn("Impossible to shut down the prolog engine.");
 //		return result;
 	}
 	
 	@Override
 	public boolean isCloseable() {
 		return false;
 	}
 	
 	@Override
 	public ThreadModel threadModel() {
 		return threadModel;
 	}
 	
 	@Override
 	public Term asTerm(String termString, Jpc context) {
 		jpl.Term jplTerm = null;
 		try {
 			jplTerm = jpl.Util.textToTerm(termString);
 		} catch(jpl.PrologException jplException) {
 			RuntimeException problem = context.fromTerm(JplBridge.fromJplToJpc(jplException.term()));
 			if(problem instanceof SyntaxError)
 				problem = new PrologParsingException(termString, problem);
 			throw problem;
 		}
 		return JplBridge.fromJplToJpc(jplTerm);
 	}
 	
 	@Override
 	public Query basicQuery(Term goal, boolean errorHandledQuery, Jpc context) {
 		if(threadModel.equals(SINGLE_THREADED))
 			return new SingleThreadedJplQuery(this, goal, errorHandledQuery, context);
 		else
 			return new MultiThreadedJplQuery(this, goal, errorHandledQuery, context);
 	}
 
 	@Override
	protected void loadLogtalkFiles() {
		super.loadLogtalkFiles(); //load default JPC Logtalk files.
 		PrologResourceLoader resourceLoader = new PrologResourceLoader(this);
 		resourceLoader.logtalkLoad(JPL_LOGTALK_LOADER_FILE); //load Logtalk JPL specific Logtalk files.
 	}
 	
 
 
 }
