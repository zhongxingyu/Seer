 /**
  * $$\\ToureNPlaner\\$$
  */
 package de.tourenplaner.algorithms;
 
 import de.tourenplaner.graphrep.GraphRep;
 
 /**
  *  @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
  *
 * Used to create instances of shared algorithms
  */
 public abstract class SharingAlgorithmFactory extends GraphAlgorithmFactory {
 
 	public SharingAlgorithmFactory(GraphRep graph) {
 		super(graph);
 	}
 
 	public abstract Algorithm createAlgorithm(DijkstraStructs rs);
 
 }
