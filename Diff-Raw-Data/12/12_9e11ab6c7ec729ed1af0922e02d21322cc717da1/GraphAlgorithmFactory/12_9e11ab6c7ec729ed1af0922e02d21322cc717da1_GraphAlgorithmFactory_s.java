 /**
  * $$\\ToureNPlaner\\$$
  */
 package de.tourenplaner.algorithms;
 
 import de.tourenplaner.graphrep.GraphRep;
 
 import java.util.List;
 import java.util.Map;
 /**
  *  @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
  *
  * This class is used to construct Graphalgorithms
  * and provides additional information only used by Graphalgorithms
  *
  *
  */
 public abstract class GraphAlgorithmFactory extends AlgorithmFactory {
 	
 	protected GraphRep graph;
 	
 	
 	public GraphAlgorithmFactory(GraphRep graph){
 		this.graph = graph;
 	}
 	
 	/**
 	 * Gets the List of PointConstraints in a jsonserializable Map format
 	 * 
	 * @return
 	 */
 	public abstract List<Map<String, Object>> getPointConstraints();
 }
