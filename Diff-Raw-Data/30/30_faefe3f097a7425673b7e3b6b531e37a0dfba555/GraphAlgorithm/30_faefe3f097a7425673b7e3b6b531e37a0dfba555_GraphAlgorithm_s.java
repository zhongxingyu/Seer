 /**
  * $$\\ToureNPlaner\\$$
  */
 package de.tourenplaner.algorithms;
 
 import de.tourenplaner.graphrep.GraphRep;
 /**
  * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
  *
  * This abstract class is the basis for Algorithms using a Graphrepresentation
  * and will be constructed with a reference to the
 * GraphRep. used by the de.tourenplaner.server
  *
  */
 public abstract class GraphAlgorithm implements Algorithm {
 	final GraphRep graph;
 
 	public GraphAlgorithm(GraphRep graph) {
 		this.graph = graph;
 	}
 
 }
