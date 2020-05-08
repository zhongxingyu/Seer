 package org.javaan.graph;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.javaan.graph.CondensedEdge;
 import org.javaan.graph.CondensedGraphBuilder;
 import org.jgrapht.DirectedGraph;
 import org.jgrapht.EdgeFactory;
 import org.jgrapht.graph.DirectedMultigraph;
 import org.junit.Test;
 
 public class TestCondensedGraphBuilder {
 	
 	private static final String A = "A";
 
 	private static final String B = "B";
 
 	private static final String C = "C";
 	
 	private static final String EDGE_AB_1 = "ab1";
 	
 	private static final String EDGE_AB_2 = "ab2";
 	
 	private static final String EDGE_AC_1 = "ac1";
 	
 	private static final String EDGE_AC_2 = "ac2";
 	
 	private static final String EDGE_CA_1 = "ca1";
 	
 	private void assertEdgeAB(CondensedEdge<String, String> edgeAB) {
 		assertEquals(A, edgeAB.getSource());
 		assertEquals(B, edgeAB.getTarget());
		assertNotNull(edgeAB.getEdges());
		assertEquals(2, edgeAB.getEdges().size());
		assertTrue(edgeAB.getEdges().contains(EDGE_AB_1));
		assertTrue(edgeAB.getEdges().contains(EDGE_AB_2));
 	}
 	
 	private void assertEdgeAC(CondensedEdge<String, String> edgeAC) {
 		assertEquals(A, edgeAC.getSource());
 		assertEquals(C, edgeAC.getTarget());
		assertNotNull(edgeAC.getEdges());
		assertEquals(2, edgeAC.getEdges().size());
		assertTrue(edgeAC.getEdges().contains(EDGE_AC_1));
		assertTrue(edgeAC.getEdges().contains(EDGE_AC_2));		
 	}
 	
 	@Test
 	public void testCreateCondensedGraph() {
 		DirectedGraph<String, String> graph = new DirectedMultigraph<String, String>(new EdgeFactory<String, String>() {
 			public String createEdge(String sourceVertex, String targetVertex) {
 				return sourceVertex + targetVertex;
 			};
 		});
 		graph.addVertex(A);
 		graph.addVertex(B);
 		graph.addVertex(C);
 		assertTrue(graph.addEdge(A, B, EDGE_AB_1));
 		assertTrue(graph.addEdge(A, B, EDGE_AB_2));
 		assertTrue(graph.addEdge(A, C, EDGE_AC_1));
 		assertTrue(graph.addEdge(A, C, EDGE_AC_2));
 		assertTrue(graph.addEdge(C, A, EDGE_CA_1));
 		
 		assertEquals(4, graph.outDegreeOf(A));
 		assertEquals(1, graph.outDegreeOf(C));
 		
 		DirectedGraph<String, CondensedEdge<String, String>> condensedGraph = new CondensedGraphBuilder<>(graph).createCondensedGraph();
 		assertEquals(2, condensedGraph.outDegreeOf(A));
 		List<CondensedEdge<String, String>> condensedEdges = new ArrayList<>(condensedGraph.outgoingEdgesOf(A));
 		CondensedEdge<String, String> firstEdge = condensedEdges.get(0);
 		if (firstEdge.getTarget().equals(B)) assertEdgeAB(firstEdge);
 		else assertEdgeAC(firstEdge);
 		CondensedEdge<String, String> secondEdge = condensedEdges.get(1);
 		if (secondEdge.getTarget().equals(B)) assertEdgeAB(secondEdge);
 		else assertEdgeAC(secondEdge);
 	}
 }
