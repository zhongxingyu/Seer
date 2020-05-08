 package graphimplementations;
 
 import static org.junit.Assert.*;
 
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
 import org.junit.experimental.theories.DataPoints;
 import org.junit.experimental.theories.Theory;

import com.google.common.collect.Sets;
//import org.junit.experimental.theories.DataPoints;
//import org.junit.experimental.theories.Theories;
//import org.junit.experimental.theories.Theory;
//import org.junit.runner.RunWith;
 
 import graphimplementations.Edge;
import graphimplementations.EdgesGraph;
 import graphimplementations.Vertex;
 
 public class EdgeTest {
 	@DataPoints
 	public static Vertex[] VERTICES = { new Vertex(), new Vertex(), new Vertex() };
 	
 	@Theory
 	public void undirectedInverse_equal(Vertex v, Vertex w) {
 		Edge oneWay = Edge.between(v).and(w);
 		Edge otherWay = Edge.between(w).and(v);
 		
 		assertTrue(oneWay.equals(otherWay));
 	}
 	
 	@Theory
 	public void directedInverse_notEqual(Vertex v, Vertex w) {
 		Edge oneWay = Edge.from(v).to(w);
 		Edge otherWay = Edge.from(w).to(v);
		
		assertFalse(oneWay.equals(otherWay));
 	}
 	
 	@Theory
 	public void directedUndirected_notEqual(Vertex v, Vertex w) {
 		Edge dir = Edge.from(v).to(w);
 		Edge undir = Edge.between(v).and(w);
 		
 		assertFalse(dir.equals(undir));
 	}
 	
 	@Theory
 	public void undirectedLoop_true(Vertex v) {
 		Edge loop = Edge.between(v).and(v);
 		
 		assertTrue(loop.isLoop());
 	}
 	
 	@Theory
 	public void directedLoop_true(Vertex v) {
 		Edge loop = Edge.from(v).to(v);
 		
 		assertTrue(loop.isLoop());
 	}
 }
