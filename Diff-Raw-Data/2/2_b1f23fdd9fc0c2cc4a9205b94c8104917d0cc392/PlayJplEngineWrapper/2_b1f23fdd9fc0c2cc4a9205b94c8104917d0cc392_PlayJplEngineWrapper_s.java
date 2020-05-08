 package eu.play_project.dcep.distributedetalis;
 
 import java.util.Hashtable;
 
 import jpl.Atom;
 import jpl.PrologException;
 import jpl.Query;
 import jpl.Term;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.jtalis.core.event.EtalisEventListener;
 import com.jtalis.core.plengine.EngineOutputListener;
 import com.jtalis.core.plengine.JPLEngineWrapper;
 import com.jtalis.core.plengine.PrologEngineWrapper;
 
 import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
 import eu.play_project.dcep.distributedetalis.api.PrologEngineWrapperPlayExtensions;
 
 /**
  * To synchronize acces to JPL TODO synchronize it.
  * www.swi-prolog.org/packages/jpl/java_api/high-level_interface.html
  * 
  * Important: To execute goals related to SemWebLib use only
  * PrologEngineWrapperPlayExtensions.execute(String command). This method
  * guarantees exclusive access to db. (Only from Java side).
  * 
  * @author sobermeier
  * 
  */
 public class PlayJplEngineWrapper implements PrologEngineWrapper<Object>, PrologEngineWrapperPlayExtensions {
 
 	private final JPLEngineWrapper engine;
 	private static PlayJplEngineWrapper localEngine = new PlayJplEngineWrapper();
 	private final Logger logger = LoggerFactory.getLogger(PlayJplEngineWrapper.class);
 
 	private PlayJplEngineWrapper(){
 		engine = new JPLEngineWrapper();
 	}
 	
 	public static PlayJplEngineWrapper getPlayJplEngineWrapper(){
 		return localEngine;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public synchronized Hashtable<String, Object>[] execute(String command) throws DistributedEtalisException {
 		try {
 			// Get data from triplestore
 			Query q = new Query(command);
 			return q.allSolutions();
 		} catch (PrologException e) {
			logger.error("Error shutting down Etalis. {}", e.getMessage());
 			return new Hashtable[0];
 		}
 	}
 
 	@Override
 	public synchronized boolean execute(com.jtalis.core.plengine.logic.Term term) {
 		try {
 			return engine.execute(term);
 		} catch(PrologException e){
 			logger.error("Error executing Prolog goal. {}", e.getMessage());
 			return false;
 		}
 	}
 
 	@Override
 	public boolean executeGoal(String goal) {
 		try {
 			Query q = new Query(goal);
 			return q.hasSolution();
 		} catch (PrologException e) {
 			logger.error("Error executing Prolog goal. {}", e.getMessage());
 			return false;
 		}
 	}
 
 	@Override
 	public synchronized Object registerPushNotification(EtalisEventListener listener) {
 		return engine.registerPushNotification(listener);
 	}
 
 	@Override
 	public synchronized void unregisterPushNotification(EtalisEventListener listener) {
 		engine.unregisterPushNotification(listener);
 	}
 
 
 	@Override
 	public synchronized void shutdown() {
 		try {
 			engine.shutdown();
 			//It is not possible to shutdown completly. We will clean up the database.
 			//this.executeGoal("retractall(_)");
 			this.executeGoal("rdf_retractall(_S,_P,_O,_DB)");
 			this.executeGoal("reset_etalis");
 		} catch (PrologException e) {
 			logger.error("Error shutting down Etalis. {}", e.getMessage());
 		}
 	}
 
 	@Override
 	public synchronized void addOutputListener(EngineOutputListener listener) {
 		engine.addOutputListener(listener);
 	}
 
 	@Override
 	public synchronized String getName() {
 		return engine.getName();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Hashtable<String, Object>[] getTriplestoreData(String triplestoreID) {
 		
 		Hashtable<String, Object>[] result;
 		try {
 			//Get data from triplestore
 			result = this.execute("rdfTest(S,P,O, " + triplestoreID + ")");
 		} catch (PrologException e) {
 			logger.error("Error getting data from Prolog. {}", e.getMessage());
 			result = new Hashtable[0];
 		} catch (DistributedEtalisException e) {
 			logger.error("Error getting data from Prolog. {}", e.getMessage());
 			result = new Hashtable[0];
 		} finally {
 			try {
 				// Free space
 				this.executeGoal("retractall(rdfTest(_S,_P,_O, " + triplestoreID + "))");
 				
 			} catch (PrologException e) {}
 		}
 		return result;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public Hashtable<String, Object>[] getVariableValues(String queryId){
 		StringBuffer comand = new StringBuffer();
 		
 		comand.append("variableValues('");
 		comand.append(queryId);
 		comand.append("', _, Value)");
 		
 		try {
 			// Get Variables and values
 			return this.execute((comand.toString()));
 		} catch (DistributedEtalisException e) {
 			logger.error("Error getting values from Prolog. {}", e.getMessage());
 			return new Hashtable[0];
 		} catch (PrologException e) {
 			logger.error("Error getting values from Prolog. {}", e.getMessage());
 			return new Hashtable[0];
 		}
 	}
 	
 	@Override
 	public synchronized boolean consult(String file) {
 		try {
 			Query consult_query = new Query("consult",
 					new Term[] { new Atom(file) });
 			return consult_query.hasSolution();
 		} catch (PrologException e) {
 			logger.error("Error consulting Prolog file. {}", e.getMessage());
 			return false;
 		}
 	}
 
 	@Override
 	public boolean assertFromFile(String file) {
 		return false;
 	}
 }
 
 	
