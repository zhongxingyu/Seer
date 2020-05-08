 /**
  * $$\\ToureNPlaner\\$$
  */
 package computecore;
 
import java.util.Collection;
 import java.util.Enumeration;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import algorithms.AlgorithmFactory;
 
 /**
  * Singelton Pattern used
  * 
  *
  * @author Niklas Schnelle, Peter Vollmer
  *
  */
 public class AlgorithmRegistry {
 
 	
 	private ConcurrentHashMap<String, AlgorithmFactory> registry;
 	
 	public AlgorithmRegistry(){
 		registry = new ConcurrentHashMap<String, AlgorithmFactory>();
 	};
 	
 	
 	public void registerAlgorithm(AlgorithmFactory algFac){
 		registry.putIfAbsent(algFac.getURLSuffix(), algFac);
 	}
 	
 
 	/**
 	 * Creates a new AlgorithmManager that has an instance for every registered Algorithms
 	 * 
 	 * @return
 	 */
 	public AlgorithmManager getAlgorithmManager(){
 		AlgorithmManager m = new AlgorithmManager();
 		
 		for(Map.Entry<String, AlgorithmFactory> registered : registry.entrySet()){
 			m.addAlgorithm(registered.getKey(), registered.getValue().createAlgorithm());
 		}
 		
 		return m;
 	}
 	
 	
	public Collection<AlgorithmFactory> getAlgorithms(){
		return registry.values();
 	}
 }
