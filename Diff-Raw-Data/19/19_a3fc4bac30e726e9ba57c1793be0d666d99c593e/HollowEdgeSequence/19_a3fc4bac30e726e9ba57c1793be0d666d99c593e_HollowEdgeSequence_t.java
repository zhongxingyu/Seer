 package com.tinkerpop.blueprints.pgm.impls.hollow.util;
 
 import com.tinkerpop.blueprints.pgm.Edge;
 import com.tinkerpop.blueprints.pgm.impls.hollow.HollowGraph;
 import com.tinkerpop.blueprints.pgm.impls.hollow.HollowEdge;
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
import java.util.Random;
 
 /**
  * @author Elaine Angelino (http://www.eecs.harvard.edu/~elaine)
  */
 public class HollowEdgeSequence implements Iterable<Edge>, Iterator<Edge> {
	
	private static Random rand = new Random();
 
     private HollowGraph graph = null;
     private int i = 0;
    private int degree = 0;
     
     public HollowEdgeSequence(final HollowGraph graph) {
         this.graph = graph;
        
        int avgDegree = (graph.edgeCount + graph.vertexCount - 1) / graph.vertexCount;
        this.degree = rand.nextInt(2 * avgDegree + 1);
     }
     
     public HollowEdgeSequence(final HollowGraph graph, final long vid, final boolean getOut) {
         this.graph = graph;
     }
     
     public HollowEdgeSequence(final HollowGraph graph, final long vid, final boolean getOut, final String[] labels) {
         this.graph = graph;
     }
 	
 	public Edge next() {
		if (i++ < degree ) {
 			return new HollowEdge(this.graph, 0, 0, 0, "");
 		} else {
 			throw new NoSuchElementException();
 		}
 	}
 
 	public boolean hasNext() {
    	return i < degree;
     }
 	
 	public void close() {
		i = degree;
 	}
 
     public void remove() { 
         throw new UnsupportedOperationException(); 
     } 
 
     public Iterator<Edge> iterator() {
         return this;
     }
 }
