 package com.github.haw.ai.gkap.graph.test;
 
 import static org.junit.Assert.*;
 
 import java.util.HashSet;
 import java.util.Set;
 import org.junit.Before;
 import org.junit.Test;
 import com.github.haw.ai.gkap.graph.Edge;
 import com.github.haw.ai.gkap.graph.Graph;
 import com.github.haw.ai.gkap.graph.GraphImpl;
 import com.github.haw.ai.gkap.graph.Vertex;
 import static com.github.haw.ai.gkap.graph.Graphs.*;
 
 public class GraphTest {
 	private Graph<String, String> g1;
 	private Graph<String, String> g2;
 	private Vertex<String> v1;
 	private Vertex<String> v2;
 	private Vertex<String> v3;
 	private Vertex<String> v4;
 	private Vertex<String> v5;
 	private Edge<String, String> e1;
 	private Edge<String, String> e2;
 	private Edge<String, String> e3;
 	private Edge<String, String> e4;
 	private Edge<String, String> e5;
 	
 	@Before
 	public void setup() {
 		Set<Edge<String, String>> edges1 = new HashSet<Edge<String, String>>();
 		Set<Vertex<String>> vertices1 = new HashSet<Vertex<String>>();
 		
 		Set<Edge<String, String>> edges2 = new HashSet<Edge<String, String>>();
 		Set<Vertex<String>> vertices2 = new HashSet<Vertex<String>>();
 		
 		this.v1 = vertex("V1");
 		this.v2 = vertex("V2");
 		this.v3 = vertex("V3");
 		this.v4 = vertex("V4");
 		this.v5 = vertex("V5");
 		this.e1 = directedEdge(this.v1, this.v1, "E1");
 		this.e2 = directedEdge(this.v1, this.v2, "E2");
 		this.e3 = directedEdge(this.v2, this.v3, "E3");
 		this.e4 = directedEdge(this.v5, this.v3, "E4");
 		this.e5 = directedEdge(this.v1, this.v5, "E5");
 		
 		edges1.add(e1);
 		vertices1.add(v1);
 		this.g1 = GraphImpl.valueOf(edges1, vertices1);
 		
 		vertices2.add(v1);
 		vertices2.add(v2);
 		vertices2.add(v3);
 		vertices2.add(v4);
 		vertices2.add(v5);
 		edges2.add(e1);
 		edges2.add(e2);
 		edges2.add(e3);
 		edges2.add(e4);
 		edges2.add(e5);
 		this.g2 = GraphImpl.valueOf(edges2, vertices2);
 	}
 	
 	@Test
 	public void testDegree() {
 		// public int degree(Vertex<V> vertex);
 		assertEquals(1, this.g1.degree(this.v1));
 		
 		assertEquals(3, this.g2.degree(this.v1));
 		assertEquals(2, this.g2.degree(this.v2));
 		assertEquals(2, this.g2.degree(this.v3));
 	}
 	
 	@Test
 	public void testIsAdjacent() {
 		// public boolean isAdjacent(Vertex<V> vertex, Vertex<V> otherVertex);
 		assertTrue(this.g1.isAdjacent(v1, v1));
 		assertFalse(this.g1.isAdjacent(v1, v2));
 		
 		assertTrue(this.g2.isAdjacent(v1, v1));
 		assertTrue(this.g2.isAdjacent(v1, v2));
 		assertTrue(this.g2.isAdjacent(v2, v1));
 		assertTrue(this.g2.isAdjacent(v2, v3));
 		assertTrue(this.g2.isAdjacent(v3, v2));
 		assertTrue(this.g2.isAdjacent(v3, v5));
 		assertTrue(this.g2.isAdjacent(v5, v3));
 		assertTrue(this.g2.isAdjacent(v1, v5));
 		assertTrue(this.g2.isAdjacent(v5, v1));
 		assertFalse(this.g2.isAdjacent(v2, v5));
 		assertFalse(this.g2.isAdjacent(v5, v2));
 	}
 	
 	@Test
 	public void testAdjacent() {
 		// public Set<Vertex<V>> adjacent(Vertex<V> vertex);
 		Set<Vertex<String>> a1 = new HashSet<Vertex<String>>();
 		a1.add(v1);
 		
 		Set<Vertex<String>> a2 = new HashSet<Vertex<String>>();
 		a2.add(v1); a2.add(v2); a2.add(v5);
 		
 		Set<Vertex<String>> a3 = new HashSet<Vertex<String>>();
		a3.add(v1); a3.add(v3);
 		
 		Set<Vertex<String>> a4 = new HashSet<Vertex<String>>();
		a4.add(v1); a4.add(v3);
 		
 		assertEquals(a1, this.g1.adjacent(v1));
 		assertEquals(a2, this.g2.adjacent(v1));
 		assertEquals(a3, this.g2.adjacent(v2));
 		assertEquals(a4, this.g2.adjacent(v5));
 	}
 	
 	@Test
 	public void testIsIncident() {
 		// public boolean isIncident(Vertex<V> vertex, Edge<E,V> edge);
 		assertTrue(g1.isIncident(v1, e1));
 		assertTrue(g2.isIncident(v1, e2));
 		assertTrue(g2.isIncident(v1, e5));
 	}
 	
 	@Test
 	public void testIncident() {
 		// public Set<Edge<E,V>> incident(Vertex<V> vertex);
 		Set<Edge<String,String>> i1 = new HashSet<Edge<String,String>>();
 		i1.add(e1);
 		
 		Set<Edge<String,String>> i2 = new HashSet<Edge<String,String>>();
 		i2.add(e2); i2.add(e3);
 		
 		assertEquals(i1, g1.incident(v1));
 		
 		assertEquals(i2, g2.incident(v2));
 	}
 }
