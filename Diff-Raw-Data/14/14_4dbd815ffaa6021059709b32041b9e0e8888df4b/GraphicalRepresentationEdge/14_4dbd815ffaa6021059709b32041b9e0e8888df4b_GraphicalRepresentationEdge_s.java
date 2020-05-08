 package de.unisiegen.informatik.bs.alvis.graph.datatypes;
 
 import de.unisiegen.informatik.bs.alvis.primitives.*;
 
 /**
  * GraphicalRepresentation of Edge, should be implemented by every Edge
  * 
  * @author Dominik Dingel
  * 
  */
 
 public interface GraphicalRepresentationEdge extends GraphicalRepresentation {
 	/**
 	 * Edge connects two Nodes, getSource returns first of them
 	 * 
 	 * @return source node
 	 */
	GraphicalRepresentationVertex getSource();
 
 	/**
 	 * Edge connects two Nodes, getDestination returnst second of them
 	 * 
 	 * @return destination node
 	 */
	GraphicalRepresentationVertex getDestination();
 
 }
