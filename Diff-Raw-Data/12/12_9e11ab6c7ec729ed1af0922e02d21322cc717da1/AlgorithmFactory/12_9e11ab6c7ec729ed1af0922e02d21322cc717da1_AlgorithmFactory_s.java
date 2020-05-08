 /**
  * $$\\ToureNPlaner\\$$
  */
 package de.tourenplaner.algorithms;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 
  * This factory class is used to create Algorithm instances and to provide
  * information on the created Algorithm used by clients to adapt to the server
  */
 public abstract class AlgorithmFactory {
 	/**
 	 * Creates a new instance of the Algorithm class(s) associated with this
 	 * factory
 	 * 
 	 * @return a new Algorithm instance
 	 */
 	public abstract Algorithm createAlgorithm();
 
 	/**
 	 * Used to get the URLSuffix for the constructed Algorithms e.. "sp" will
 	 * make the Algorithm available under /algsp
 	 * 
 	 * @return
 	 */
 	public abstract String getURLSuffix();
 
 	/**
 	 * Returns the human readable name of the constructed Algorithms e.g.
 	 * "Shortest Path"
 	 * 
 	 * @return
 	 */
 	public abstract String getAlgName();
 
 	/**
 	 * Returns the version of the constructed Algorithms
 	 * 
 	 * @return
 	 */
 	public abstract int getVersion();
 
 	/**
 	 * Returns if it is an isHidden algorithm
 	 * 
 	 */
 	public abstract boolean isHidden();
 
     /**
      * Gets the Constraints not bound to any Point
      *
     * @return
      */
     public abstract List<Map<String, Object>> getConstraints();
 
     /**
      * Gets Details for the the algorithm
      * this includes whether the algorithm has sourceIsTarget
      * and the minimal number of points
      *
     * @return
      */
     public abstract Map<String, Object> getDetails();
 }
