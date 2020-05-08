 package com.github.haw.ai.gkap.graph.test;
 
 import static org.junit.Assert.*;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.haw.ai.gkap.graph.DijkstraAlgorithm;
 import com.github.haw.ai.gkap.graph.Edge;
 import com.github.haw.ai.gkap.graph.FloydWarshallAlgorithm;
 import com.github.haw.ai.gkap.graph.Graph;
 import com.github.haw.ai.gkap.graph.GraphImpl;
 import com.github.haw.ai.gkap.graph.Graphs;
 import com.github.haw.ai.gkap.graph.Vertex;
 
 public class FloydWarshallAlgorithmTest<E, V> {
 	
 	Vertex<String> v1 = Graphs.vertex("v1");
 	Vertex<String> v2 = Graphs.vertex("v2");
 	Vertex<String> v3 = Graphs.vertex("v3");
 	Vertex<String> v4 = Graphs.vertex("v4");
 	Vertex<String> v5 = Graphs.vertex("v5");
 	Vertex<String> v6 = Graphs.vertex("v6");
 	
 	Edge<Double, String> e1 = Graphs.undirectedEdge(v1, v2, 1.0);
 	Edge<Double, String> e2 = Graphs.undirectedEdge(v1, v6, 3.0);
 	Edge<Double, String> e3 = Graphs.undirectedEdge(v2, v6, 2.0);
 	Edge<Double, String> e4 = Graphs.undirectedEdge(v2, v3, 5.0);
 	Edge<Double, String> e5 = Graphs.undirectedEdge(v2, v5, 3.0);
 	Edge<Double, String> e6 = Graphs.undirectedEdge(v6, v3, 2.0);
 	Edge<Double, String> e7 = Graphs.undirectedEdge(v3, v5, 2.0);
 	Edge<Double, String> e8 = Graphs.undirectedEdge(v6, v5, 1.0);
 	Edge<Double, String> e9 = Graphs.undirectedEdge(v3, v4, 1.0);
 	Edge<Double, String> e10 = Graphs.undirectedEdge(v5, v4, 3.0);
 	Set<Edge<E, V>> edges = new HashSet<Edge<E, V>>();
 	Set<Vertex<V>> vertices = new HashSet<Vertex<V>>();
 	Graph graph = null;
 
 	@Before
 	public void setUp() throws Exception {
 		edges = new HashSet<Edge<E, V>>();
 		edges.add((Edge<E, V>) e1);
 		edges.add((Edge<E, V>) e2);
 		edges.add((Edge<E, V>) e3);
 		edges.add((Edge<E, V>) e4);
 		edges.add((Edge<E, V>) e5);
 		edges.add((Edge<E, V>) e6);
 		edges.add((Edge<E, V>) e7);
 		edges.add((Edge<E, V>) e8);
 		edges.add((Edge<E, V>) e9);
 		edges.add((Edge<E, V>) e10);
 		vertices = new HashSet<Vertex<V>>();
 		vertices.add((Vertex<V>) v1);
 		vertices.add((Vertex<V>) v2);
 		vertices.add((Vertex<V>) v3);
 		vertices.add((Vertex<V>) v4);
 		vertices.add((Vertex<V>) v5);
 		vertices.add((Vertex<V>) v6);
 		graph = GraphImpl.valueOf(edges, vertices);
 	}
 
 	@Test
 	public void runAlgorithm() {
 		FloydWarshallAlgorithm result = FloydWarshallAlgorithm.valueOf(graph);
 		result.runAlgorithm();
 		List expectedResult = new LinkedList();
 		expectedResult.add(v1);
 		expectedResult.add(v6);
 		expectedResult.add(v3);
 		expectedResult.add(v4);
 		
		assertEquals(expectedResult,result.shortestPath(v1,v2));
 	}
 
 }
