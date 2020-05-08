 package org.jpc.engine.jpl;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NoSuchElementException;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.jpc.Jpc;
 import org.jpc.query.PrologQuery;
 import org.jpc.query.QuerySolution;
 import org.jpc.term.Term;
 
 /**
  * An adapter to a JPL query
  * @author sergioc
  *
  */
 public class JplQuery extends PrologQuery {
 
 	private jpl.Query jplQuery;
 	/**
 	 * At the moment, JPL requires that calls to hasMoreSolutions() and nextSolution() occur in the same thread.
 	 * Also, a JPL query should not be opened in the same thread where another JPL query is already open.
 	 * However, the contract of JPC queries does not specify such constraints.
 	 * The executor below allows to provide a JPC query (encapsulating a JPL query) that does not have the constraint limiting certain cursor operations to happen in the same thread (as in JPL).
 	 * This is done executing relevant JPL operations in the context of a single threaded executor (an executor backed up with only one thread).
 	 * Although we gain on simplifying the usage contract of JPL Query objects (specially in heavily multi-threaded scenarios), 
 	 * a disadvantage of this approach is that it implies to maintain one separate thread per each JPC query wrapping a JPL query.
 	 * Although the thread is destroyed when the query is closed, this could imply a certain additional demand of resources (threads) that does not occur in plain JPL.
 	 * This implementation should be modified (i.e., the executor removed) if JPL eventually removes existing constraints regarding cursor operations occurring in the same/different thread.
 	 */
 	private ExecutorService executor;
 	
	public JplQuery(JplEngine prologEngine, Term goal, boolean errorHandledQuery, Jpc context) {
		super(prologEngine, goal, errorHandledQuery, context);
 		jpl.Term jplGoal = JplBridge.fromJpcToJpl(getInstrumentedGoal());
 		jplQuery = new jpl.Query(jplGoal);
 	}
 
 	private ExecutorService getExecutor() {
 		if(executor == null)
 			executor = Executors.newSingleThreadExecutor();
 		return executor;
 	}
 	
 	@Override
 	public boolean isAbortable() {
 		return false;
 	}
 	
 	@Override
 	protected void basicAbort() {
 		throw new UnsupportedOperationException();
 		//jplQuery.abort(); //this method exists but does not work
 	}
 
 	@Override
 	protected void basicClose() {
 		executor = getExecutor();
 		try {
 			executor.submit(new Callable<Object>() {
 				@Override
 				public Object call() throws Exception {
 					jplQuery.close();
 					return null;
 				}
 			}).get();
 			executor.shutdownNow();
 			executor = null;
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public QuerySolution basicNext() {
 		executor = getExecutor();
 		QuerySolution querySolution = null;
 		try {
 			querySolution = executor.submit(new Callable<QuerySolution>() {
 				@Override
 				public QuerySolution call() throws Exception {
 					if(jplQuery.hasMoreSolutions()) {
 						Map<String, Term> nextSolution = new HashMap<>();
 						Map<String, jpl.Term> jplSolution = jplQuery.nextSolution();
 						for(Entry<String, jpl.Term> jplEntry : jplSolution.entrySet()) {
 							String varName = jplEntry.getKey();
 							Term term = JplBridge.fromJplToJpc(jplEntry.getValue());
 							nextSolution.put(varName, term);
 						}
 						return new QuerySolution(nextSolution, getPrologEngine(), getJpcContext());
 					} else {
 						return null;
 					}
 				}
 			}).get();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		if(querySolution!=null)
 			return querySolution;
 		else
 			throw new NoSuchElementException();
 	}
 
 }
