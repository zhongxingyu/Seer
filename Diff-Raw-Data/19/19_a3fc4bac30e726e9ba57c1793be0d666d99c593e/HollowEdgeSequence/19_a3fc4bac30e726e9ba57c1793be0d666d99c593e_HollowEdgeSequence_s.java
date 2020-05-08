 package com.tinkerpop.blueprints.pgm.impls.hollow.util;
 
 import com.tinkerpop.blueprints.pgm.Edge;
 import com.tinkerpop.blueprints.pgm.impls.hollow.HollowGraph;
 import com.tinkerpop.blueprints.pgm.impls.hollow.HollowEdge;
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  * @author Elaine Angelino (http://www.eecs.harvard.edu/~elaine)
  */
 public class HollowEdgeSequence implements Iterable<Edge>, Iterator<Edge> {
 
     private HollowGraph graph = null;
     private int i = 0;
     
     public HollowEdgeSequence(final HollowGraph graph) {
         this.graph = graph;
     }
     
     public HollowEdgeSequence(final HollowGraph graph, final long vid, final boolean getOut) {
         this.graph = graph;
     }
     
     public HollowEdgeSequence(final HollowGraph graph, final long vid, final boolean getOut, final String[] labels) {
         this.graph = graph;
     }
 	
 	public Edge next() {
		if (i++ < this.graph.edgeCount / this.graph.vertexCount ) {
 			return new HollowEdge(this.graph, 0, 0, 0, "");
 		} else {
 			throw new NoSuchElementException();
 		}
 	}
 
 	public boolean hasNext() {
    	return i < this.graph.edgeCount / this.graph.vertexCount;
     }
 	
 	public void close() {
		i = this.graph.edgeCount / this.graph.vertexCount;
 	}
 
     public void remove() { 
         throw new UnsupportedOperationException(); 
     } 
 
     public Iterator<Edge> iterator() {
         return this;
     }
 }
