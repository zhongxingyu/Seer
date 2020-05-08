 package com.codemelon.graph.util;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.codemelon.graph.DiGraph;
 import com.codemelon.graph.common.Color;
 import com.codemelon.graph.vertex.Vertex;
 import com.codemelon.graph.vertex.VertexConstants;
 /**
  * @author Marshall Farrier
  * @version Nov 25, 2012
  *
  */
 public class TransposerTest {
 	private static final int VERTICES_IN_TEST_GRAPH = 1000;
 	private static final Color SPECIAL_COLOR = Color.GRAY;
 	private static final int SPECIAL_DISTANCE = 4;
 	private static final int SPECIAL_DISCOVERY_TIME = 5;
 	private static final int SPECIAL_FINISH_TIME = 6;
 	private static final int SPECIAL_SEARCH_ORDER = 7;
 	private static final int SPECIAL_TREE_NUMBER = 8;
 	
 	private DiGraph graph;
 	private ArrayList<Vertex> vertices;
 
 	@Before
 	public void setUp() {
 		vertices = new ArrayList<Vertex>(VERTICES_IN_TEST_GRAPH);
 		for (int i = 0; i < VERTICES_IN_TEST_GRAPH; i++) {
 			vertices.add(new Vertex(i));
 		}
 		graph = new DiGraph(vertices);
 	}
 
 	@After
 	public void tearDown() {
 		graph = null;
 		vertices = null;
 	}
 	/**
 	 * Test method for {@link com.codemelon.graph.util.core.Transposer#transpose()}.
 	 */
 
 	@Test
 	public void testTranspose() {
 		graph.addEdge(vertices.get(0), vertices.get(2));
 		graph.addEdge(vertices.get(3), vertices.get(2));
 		vertices.get(2).color = SPECIAL_COLOR;
 		vertices.get(2).parent = vertices.get(1);
 		vertices.get(2).discoveryTime = SPECIAL_DISCOVERY_TIME;
 		vertices.get(2).distance = SPECIAL_DISTANCE;
 		vertices.get(2).finishTime = SPECIAL_FINISH_TIME;
 		vertices.get(2).searchOrder = SPECIAL_SEARCH_ORDER;
 		vertices.get(2).treeNumber = SPECIAL_TREE_NUMBER;
		DiGraph transposedGraph = new Transposer(graph).transpose();
 		assertEquals("Correct number of edges in transposed graph", 
 				2, transposedGraph.edgeCount());
 		ArrayList<Vertex> transposedVertices = transposedGraph.getVertices();
 		// transposedGraph has proper structure
 		for (Vertex v : transposedVertices) {
 			if (v.label == 2) {
 				assertEquals("Vertex 2 is source for 2 edges", 2, v.adjacencyCount());
 				assertTrue("Vertex 2 contains adjacency 0", v.containsAdjacency(0));
 				assertTrue("Vertex 2 contains adjacency 3", v.containsAdjacency(3));
 				assertNull("Vertex 2 has correct parent", v.parent);
 				assertEquals("Vertex 2 has correct color", SPECIAL_COLOR, 
 						v.color);
 				assertEquals("Vertex 2 has correct discovery time", SPECIAL_DISCOVERY_TIME, 
 						v.discoveryTime);
 				assertEquals("Vertex 2 has correct distance", SPECIAL_DISTANCE, 
 						v.distance);
 				assertEquals("Vertex 2 has correct finish time", SPECIAL_FINISH_TIME, 
 						v.finishTime);
 				assertEquals("Vertex 2 has correct search order", SPECIAL_SEARCH_ORDER, 
 						v.searchOrder);
 				assertEquals("Vertex 2 has correct tree number", SPECIAL_TREE_NUMBER, 
 						v.treeNumber);
 			}
 			else {
 				assertFalse("Only vertex 2 has adjacencies", v.hasAdjacencies());
 				assertNull("Vertex has correct parent", v.parent);
 				assertEquals("Vertex has correct color", VertexConstants.INITIAL_COLOR, 
 						v.color);
 				assertEquals("Vertex has correct discovery time", VertexConstants.INITIAL_DISCOVERY_TIME, 
 						v.discoveryTime);
 				assertEquals("Vertex has correct distance", VertexConstants.INITIAL_DISTANCE, 
 						v.distance);
 				assertEquals("Vertex has correct finish time", VertexConstants.INITIAL_FINISH_TIME, 
 						v.finishTime);
 				assertEquals("Vertex has correct search order", VertexConstants.DEFAULT_SEARCH_ORDER_VALUE, 
 						v.searchOrder);
 				assertEquals("Vertex has correct tree number", VertexConstants.INITIAL_TREE_NUMBER, 
 						v.treeNumber);
 			}
 		}
 	}
 
 }
