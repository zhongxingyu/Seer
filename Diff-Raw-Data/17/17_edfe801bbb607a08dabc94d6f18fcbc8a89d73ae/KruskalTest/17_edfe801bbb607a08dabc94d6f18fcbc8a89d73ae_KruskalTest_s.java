 package com.codemelon.graph.spanningtree;
 
 import static org.junit.Assert.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.codemelon.graph.Graph;
 import com.codemelon.graph.edge.EdgeConstants;
 import com.codemelon.graph.vertex.Vertex;
 
 /**
  * @author Marshall Farrier
  * @version Dec 4, 2012
  */
 public class KruskalTest {
 	private Graph graph;
 	private Kruskal kruskal;
 	private HashMap<Character, Vertex> vertices;
 
 	/**
 	 * Set up graph from CLRS, p. 632
 	 */
 	@Before
 	public void setUp() {
 		setUpCLRSGraph();
 		kruskal = new Kruskal(graph);
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() {
 		graph = null;
 		kruskal = null;
 		vertices = null;
 	}
 
 	/**
 	 * Test method for {@link com.codemelon.graph.spanningtree.Kruskal#markEdges()}.
 	 */
 	@Test
 	public void testMarkEdges() {
 		kruskal.markEdges();
 		assertEquals("Edge a-b marked", Kruskal.MARKER_COLOR, graph.getEdgeColor(vertices.get('a'),
 				vertices.get('b')));
 		assertEquals("Edge h-g marked", Kruskal.MARKER_COLOR, graph.getEdgeColor(vertices.get('h'),
 				vertices.get('g')));
 		assertEquals("Edge c-i marked", Kruskal.MARKER_COLOR, graph.getEdgeColor(vertices.get('c'),
 				vertices.get('i')));
 		assertEquals("Edge f-g marked", Kruskal.MARKER_COLOR, graph.getEdgeColor(vertices.get('f'),
 				vertices.get('g')));
 		assertEquals("Edge c-f marked", Kruskal.MARKER_COLOR, graph.getEdgeColor(vertices.get('c'),
 				vertices.get('f')));
 		assertEquals("Edge c-d marked", Kruskal.MARKER_COLOR, graph.getEdgeColor(vertices.get('c'),
 				vertices.get('d')));
 		assertEquals("Edge d-e marked", Kruskal.MARKER_COLOR, graph.getEdgeColor(vertices.get('d'),
 				vertices.get('e')));
 		assertEquals("Edge b-h unmarked", EdgeConstants.DEFAULT_COLOR, graph
 				.getEdgeColor(vertices.get('b'), vertices.get('h')));
 		assertEquals("Edge i-h unmarked", EdgeConstants.DEFAULT_COLOR, graph
 				.getEdgeColor(vertices.get('i'), vertices.get('h')));
 		assertEquals("Edge g-i unmarked", EdgeConstants.DEFAULT_COLOR, graph
 				.getEdgeColor(vertices.get('g'), vertices.get('i')));
 		assertEquals("Edge d-f unmarked", EdgeConstants.DEFAULT_COLOR, graph
 				.getEdgeColor(vertices.get('d'), vertices.get('f')));
 		assertEquals("Edge e-f unmarked", EdgeConstants.DEFAULT_COLOR, graph
 				.getEdgeColor(vertices.get('e'), vertices.get('f')));
 	}
 
 	/**
 	 * Test method for {@link com.codemelon.graph.spanningtree.Kruskal#generateTree()}.
 	 */
 	@Test
 	public void testGenerateTree() {
 		Graph spanningTree = kruskal.generateTree();
 		Map<Vertex, Vertex> vertexMap = kruskal.getVertexMap();
 		assertTrue("Edge a-b in graph", spanningTree.containsEdge(vertexMap.get(vertices.get('a')),
 				vertexMap.get(vertices.get('b'))));
 		assertTrue("a-b copy has correct weight", spanningTree.areEqualWeights(4.0, spanningTree
 				.getEdgeWeight(vertexMap.get(vertices.get('a')), vertexMap.get(vertices.get('b')))));
 		assertTrue("Edge h-g in graph", spanningTree.containsEdge(vertexMap.get(vertices.get('h')),
 				vertexMap.get(vertices.get('g'))));
 		assertTrue("h-g copy has correct weight", spanningTree.areEqualWeights(1.0, spanningTree
 				.getEdgeWeight(vertexMap.get(vertices.get('h')), vertexMap.get(vertices.get('g')))));
 		assertTrue("Edge c-i in graph", spanningTree.containsEdge(vertexMap.get(vertices.get('c')),
 				vertexMap.get(vertices.get('i'))));
		/*
 		assertTrue("c-i copy has correct weight", spanningTree.areEqualWeights(2.0, spanningTree
				.getEdgeWeight(vertexMap.get(vertices.get('c')), vertexMap.get(vertices.get('i')))));
		*/
 		assertTrue("Edge f-g in graph", spanningTree.containsEdge(vertexMap.get(vertices.get('f')),
 				vertexMap.get(vertices.get('g'))));
		/*
 		assertTrue("f-g copy has correct weight", spanningTree.areEqualWeights(2.0, spanningTree
 				.getEdgeWeight(vertexMap.get(vertices.get('f')), vertexMap.get(vertices.get('g')))));
		*/
 		assertTrue("Edge c-f in graph", spanningTree.containsEdge(vertexMap.get(vertices.get('c')),
 				vertexMap.get(vertices.get('f'))));
 		assertTrue("c-f copy has correct weight", spanningTree.areEqualWeights(4.0, spanningTree
 				.getEdgeWeight(vertexMap.get(vertices.get('c')), vertexMap.get(vertices.get('f')))));
 		assertTrue("Edge c-d in graph", spanningTree.containsEdge(vertexMap.get(vertices.get('c')),
 				vertexMap.get(vertices.get('d'))));
 		assertTrue("c-d copy has correct weight", spanningTree.areEqualWeights(7.0, spanningTree
 				.getEdgeWeight(vertexMap.get(vertices.get('c')), vertexMap.get(vertices.get('d')))));
 		assertTrue("Edge d-e in graph", spanningTree.containsEdge(vertexMap.get(vertices.get('d')),
 				vertexMap.get(vertices.get('e'))));
 		assertTrue("d-e copy has correct weight", spanningTree.areEqualWeights(9.0, spanningTree
 				.getEdgeWeight(vertexMap.get(vertices.get('d')), vertexMap.get(vertices.get('e')))));
 		assertFalse("Edge b-h not in graph", spanningTree.containsEdge(vertexMap.get(vertices.get('b')),
 				vertexMap.get(vertices.get('h'))));
 		assertFalse("Edge i-h not in graph", spanningTree.containsEdge(vertexMap.get(vertices.get('i')),
 				vertexMap.get(vertices.get('h'))));
 		assertFalse("Edge g-i not in graph", spanningTree.containsEdge(vertexMap.get(vertices.get('g')),
 				vertexMap.get(vertices.get('i'))));
 		assertFalse("Edge d-f not in graph", spanningTree.containsEdge(vertexMap.get(vertices.get('d')),
 				vertexMap.get(vertices.get('f'))));
 		assertFalse("Edge e-f not in graph", spanningTree.containsEdge(vertexMap.get(vertices.get('e')),
 				vertexMap.get(vertices.get('f'))));
 	}
 
 	/**
 	 * Test getting vertex map prior to running generateTree()
 	 * Test method for {@link com.codemelon.graph.spanningtree.Kruskal#getVertexMap()}.
 	 */
 	@Test(expected=IllegalStateException.class)
 	public void testGetVertexMap() {
 		Map<Vertex, Vertex> vertexMap = kruskal.getVertexMap();
 		vertexMap.get(vertices.get('a'));
 	}
 	/**
 	 * Graph from CLRS, p. 596
 	 */
 	private void setUpCLRSGraph() {
 		vertices = new HashMap<Character, Vertex>();
 		for (char i = 'a'; i <= 'i'; i++) {
 			vertices.put(i, new Vertex(i));
 		}
 		graph = new Graph(vertices.values());
 		graph.addEdge(vertices.get('a'), vertices.get('b'));
 		graph.setEdgeWeight(vertices.get('a'), vertices.get('b'), 4.0);
 		graph.addEdge(vertices.get('a'), vertices.get('h'));
 		graph.setEdgeWeight(vertices.get('a'), vertices.get('h'), 8.0);
 		graph.addEdge(vertices.get('b'), vertices.get('h'));
 		graph.setEdgeWeight(vertices.get('h'), vertices.get('b'), 11.0);
 		graph.addEdge(vertices.get('b'), vertices.get('c'));
 		graph.setEdgeWeight(vertices.get('c'), vertices.get('b'), 8.0);
 		graph.addEdge(vertices.get('c'), vertices.get('d'));
 		graph.setEdgeWeight(vertices.get('c'), vertices.get('d'), 7.0);
 		graph.addEdge(vertices.get('c'), vertices.get('f'));
 		graph.setEdgeWeight(vertices.get('c'), vertices.get('f'), 4.0);
 		graph.addEdge(vertices.get('c'), vertices.get('i'));
 		graph.setEdgeWeight(vertices.get('c'), vertices.get('i'), 2.0);
 		graph.addEdge(vertices.get('d'), vertices.get('e'));
 		graph.setEdgeWeight(vertices.get('d'), vertices.get('e'), 9.0);
 		graph.addEdge(vertices.get('d'), vertices.get('f'));
 		graph.setEdgeWeight(vertices.get('d'), vertices.get('f'), 14.0);
 		graph.addEdge(vertices.get('e'), vertices.get('f'));
 		graph.setEdgeWeight(vertices.get('e'), vertices.get('f'), 10.0);
 		graph.addEdge(vertices.get('f'), vertices.get('g'));
 		graph.setEdgeWeight(vertices.get('g'), vertices.get('f'), 2.0);
 		graph.addEdge(vertices.get('g'), vertices.get('h'));
 		graph.setEdgeWeight(vertices.get('g'), vertices.get('h'), 1.0);
 		graph.addEdge(vertices.get('g'), vertices.get('i'));
 		graph.setEdgeWeight(vertices.get('g'), vertices.get('i'), 6.0);
 		graph.addEdge(vertices.get('h'), vertices.get('i'));
 		graph.setEdgeWeight(vertices.get('h'), vertices.get('i'), 7.0);
 	}
 }
