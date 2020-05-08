 package uk.ed.inf.graph.compound.impl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import uk.ed.inf.graph.compound.base.BaseCompoundEdge;
 import uk.ed.inf.graph.compound.base.BaseCompoundNode;
 import uk.ed.inf.graph.state.IGraphState;
 
 public class CompoundGraphIntegrationTest {
 	private static final int EXPECTED_INITIAL_EDGES = 0;
 	private static final int EXPECTED_INITIAL_NODES = 1;
 	private static final int EXPECTED_NUM_EDGES = 9;
 	private static final int EXPECTED_NUM_NODES = 9;
 	private static final int EXPECTED_ROOT_NODE_IDX = 0;
 	private static final int UNKNOWN_EDGE_HIGH_IDX = 99;
 	private static final int UNKNOWN_EDGE_LOW_IDX = -1;
 	private static final int UNKNOWN_NODE_HIGH_IDX = 99;
 	private static final int UNKNOWN_NODE_LOW_IDX = -1;
 //	private static final int EXPECTED_LAST_NODE = 8;
 	private static final int NUM_ROOT_NODE_EDGES = 4;
 	private static final int NUM_NODE1_EDGES = 2;
 	private static final int NUM_NODE2_EDGES = 1;
 	private static final int NUM_NODE3_EDGES = 0;
 	private static final int NUM_NODE4_EDGES = 0;
 	private static final int NUM_NODE5_EDGES = 0;
 	private static final int NUM_NODE6_EDGES = 2;
 	private static final int NUM_NODE7_EDGES = 0;
 	private static final int NUM_NODE8_EDGES = 0;
 	private static final int EXPECTED_NODES_AFTER_NODE2_DELETED = 4;
 	private static final int EXPECTED_EDGES_AFTER_NODE2_DELETED = 2;
 	private static final int EXPECTED_EDGES_AFTER_EDGE2_DELETED = 8;
 	private static final int EXPECTED_NODES_AFTER_NODE5_DELETED = 8;
 	private static final int EXPECTED_EDGES_AFTER_EDGE2_NODE5_DELETED = 8;
 	private static final int EXPECTED_NODES_AFTER_NODES_1_AND_2_DELETED = 1;
 	private static final int EXPECTED_EDGES_AFTER_NODES_1_AND_2_DELETED = 0;
 	private static final int [] NUMERIC_VALUE = { 0,1,2,3,4,5,6,7,8,9,10 } ;
 	
 	private static final String SAVED_NODES = "{0, 1, 2, 3, 4, 5, 6, 7, 8}" ;
 	private static final String SAVED_EDGES = "{0, 1, 2, 3, 4, 5, 6, 7, 8}" ;
 	private static final String NEW_STATE_SAVED_NODES = "{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}" ;
 	private static final String NEW_STATE_SAVED_EDGES = "{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}" ;
 	
 
 	
 	private CompoundGraph testInstance;
 	private CompoundNode rootNode;
 	private CompoundNode node1;
 	private CompoundNode node2;
 	private CompoundNode node3;
 	private CompoundNode node4;
 	private CompoundNode node5;
 	private CompoundNode node6;
 	private CompoundNode node7;
 	private CompoundNode node8;
 	private CompoundEdge edge1;
 	private CompoundEdge edge2;
 	private CompoundEdge edge3;
 	private CompoundEdge edge4;
 	private CompoundEdge edge5;
 	private CompoundEdge edge6;
 	private CompoundEdge edge7;
 	private CompoundEdge edge8;
 	private CompoundEdge edge9;
 
 	@Before
 	public void setUp() throws Exception {
 		this.testInstance = new CompoundGraph();
 		this.rootNode = this.testInstance.getRootNode();
 		CompoundNodeFactory rootNodeFact = this.testInstance.getRootNode().getChildCompoundGraph().nodeFactory();
 		node1 = rootNodeFact.createNode();
 		node2 = rootNodeFact.createNode();
 		CompoundNodeFactory node1Fact = node1.getChildCompoundGraph().nodeFactory();
 		node3 = node1Fact.createNode();
 		node4 = node1Fact.createNode();
 		CompoundNodeFactory node2Fact = node2.getChildCompoundGraph().nodeFactory();
 		node5 = node2Fact.createNode();
 		node6 = node2Fact.createNode();
 		CompoundNodeFactory node6Fact = node6.getChildCompoundGraph().nodeFactory();
 		node7 = node6Fact.createNode();
 		CompoundNodeFactory node7Fact = node7.getChildCompoundGraph().nodeFactory();
 		node8 = node7Fact.createNode();
 		
 		CompoundEdgeFactory edgeFact = this.testInstance.edgeFactory();
 		edgeFact.setPair(node1, node2);
 		edge1 = edgeFact.createEdge();
 		edgeFact.setPair(node2, node1);
 		edge2 = edgeFact.createEdge();
 		edgeFact.setPair(node2, node6);
 		edge3 = edgeFact.createEdge();
 		edgeFact.setPair(node2, node4);
 		edge4 = edgeFact.createEdge();
 		edgeFact.setPair(node7, node6);
 		edge5 = edgeFact.createEdge();
 		edgeFact.setPair(node6, node7);
 		edge6 = edgeFact.createEdge();
 		edgeFact.setPair(node7, node3);
 		edge7 = edgeFact.createEdge();
 		edgeFact.setPair(node1, node1);
 		edge8 = edgeFact.createEdge();
 		edge9 = edgeFact.createEdge();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		this.testInstance =  null;
 		this.rootNode= null;
 		this.node1 = null;
 		this.node2 = null;
 		this.node3 = null;
 		this.node4 = null;
 		this.node5 = null;
 		this.node6 = null;
 		this.node7 = null;
 		this.node8 = null;
 		this.edge1 = null;
 		this.edge2 = null;
 		this.edge3 = null;
 		this.edge4 = null;
 		this.edge5 = null;
 		this.edge6 = null;
 		this.edge7 = null;
 		this.edge8 = null;
 		this.edge9 = null;
 	}
 
 	@Test
 	public final void testCompoundGraph() {
 		CompoundGraph testEmptyInstance = new CompoundGraph();
 		assertEquals("empty edges", EXPECTED_INITIAL_EDGES, testEmptyInstance.getNumEdges());
 		assertEquals("empty nodes", EXPECTED_INITIAL_NODES, testEmptyInstance.getNumNodes());
 		assertEquals("root node is as expected", EXPECTED_ROOT_NODE_IDX, testEmptyInstance.getRootNode().getIndex());
 		assertTrue("expected empty edge iterator", testEmptyInstance.edgeIterator().hasNext() == false);
 		assertTrue("obtain edge factory", testEmptyInstance.edgeFactory() != null);
 		assertTrue("obtain subgraph factory", testEmptyInstance.subgraphFactory() != null);
 		assertTrue("obtain node factory", testEmptyInstance.nodeFactory() != null);
 		Iterator<BaseCompoundNode> iter = testEmptyInstance.nodeIterator();
 		assertTrue("expected root node iterator", iter.hasNext());
 		iter.next();
 		assertTrue("expected root node iterator", iter.hasNext() == false);
 	}
 
 	@Ignore @Test
 	public final void testCopyConstructor(){
 		CompoundGraph testGraphFromCopyConstructor = new CompoundGraph ( testInstance) ;
 		
 		assertTrue ( "not the same instances" , testGraphFromCopyConstructor != testInstance ) ;
 		assertEquals ( "same number of Nodes" , testInstance.getNumNodes() , testGraphFromCopyConstructor.getNumNodes()) ;
 		assertEquals ( "same number of Edges" , testInstance.getNumEdges() , testGraphFromCopyConstructor.getNumEdges()) ;
 		assertTrue ("has root node", testGraphFromCopyConstructor.getRootNode() != null);
 		assertTrue ("root nodes not the same" , testInstance.getRootNode() != testGraphFromCopyConstructor.getRootNode()) ;
 		assertTrue ( "node factories not the same" , testInstance.nodeFactory() != testGraphFromCopyConstructor.nodeFactory()) ;
 		assertTrue ( "edge factories not the same" , testInstance.edgeFactory() != testGraphFromCopyConstructor.edgeFactory()) ;
 		assertTrue ( "subgraph factories not the same" , testInstance.subgraphFactory() != testGraphFromCopyConstructor.subgraphFactory()) ;
 		assertTrue ( "node iterator has elements" , testGraphFromCopyConstructor.nodeIterator().hasNext()) ;
 		assertTrue ( "edge iterator has elements" , testGraphFromCopyConstructor.edgeIterator().hasNext()) ;
 	}
 	
 	@Test
 	public final void testDegreeAndInAndOutDegreeForNodes () throws Exception
 	{
 		assertEquals ( "node1" , NUMERIC_VALUE[6] , node1.getDegree() ) ;
 		assertEquals ( "node1 in edges" , NUMERIC_VALUE[3] , node1.getInDegree() ) ;
 		assertEquals ( "node1 out edges" , NUMERIC_VALUE[3] , node1.getOutDegree() ) ;
 		
 		assertEquals ( "node2" , NUMERIC_VALUE[4] , node2.getDegree() ) ;
 		assertEquals ( "node2 in edges" , NUMERIC_VALUE[1] , node2.getInDegree() ) ;
 		assertEquals ( "node2 out edges" , NUMERIC_VALUE[3] , node2.getOutDegree() ) ;
 		
 		assertEquals ( "node3" , NUMERIC_VALUE[1] , node3.getDegree() ) ;
 		assertEquals ( "node3 in edges" , NUMERIC_VALUE[1] , node3.getInDegree() ) ;
 		assertEquals ( "node3 out edges" , NUMERIC_VALUE[0] , node3.getOutDegree() ) ;
 		
 		assertEquals ( "node4" , NUMERIC_VALUE[1] , node4.getDegree() ) ;
 		assertEquals ( "node4 in edges" , NUMERIC_VALUE[1] , node4.getInDegree() ) ;
 		assertEquals ( "node4 out edges" , NUMERIC_VALUE[0] , node4.getOutDegree() ) ;
 		
 		assertEquals ( "node5" , NUMERIC_VALUE[0] , node5.getDegree() ) ;
 		assertEquals ( "node5 in edges" , NUMERIC_VALUE[0] , node5.getInDegree() ) ;
 		assertEquals ( "node5 out edges" , NUMERIC_VALUE[0] , node5.getOutDegree() ) ;
 		
 		assertEquals ( "node6" , NUMERIC_VALUE[3] , node6.getDegree() ) ;
 		assertEquals ( "node6 in edges" , NUMERIC_VALUE[2] , node6.getInDegree() ) ;
 		assertEquals ( "node6 out edges" , NUMERIC_VALUE[1] , node6.getOutDegree() ) ;
 		
 		assertEquals ( "node7" , NUMERIC_VALUE[3] , node7.getDegree() ) ;
 		assertEquals ( "node7 in edges" , NUMERIC_VALUE[1] , node7.getInDegree() ) ;
 		assertEquals ( "node7 out edges" , NUMERIC_VALUE[2] , node7.getOutDegree() ) ;
 		
 		assertEquals ( "node8" , NUMERIC_VALUE[0] , node8.getDegree() ) ;
 		assertEquals ( "node8 in edges" , NUMERIC_VALUE[0] , node8.getInDegree() ) ;
 		assertEquals ( "node8 out edges" , NUMERIC_VALUE[0] , node8.getOutDegree() ) ;
 	}
 	
 	@Test
 	public final void testNodesAndEdgesBelongToGraph () throws Exception
 	{
 		Iterator<BaseCompoundNode> nodesIterator = testInstance.nodeIterator() ;
 		
 		while ( nodesIterator.hasNext())
 		{
 			assertEquals ( "graph is parent" , testInstance , nodesIterator.next().getGraph()) ;
 		}
 		
 		Iterator <BaseCompoundEdge> edgeIterator = testInstance.edgeIterator() ;
 		while ( edgeIterator.hasNext())
 		{
 			assertEquals ( "graph is parent" , testInstance , edgeIterator.next().getGraph()) ;
 		}
 	}
 	
 	@Test
 	public final void testContainsDirectedEdge() {
 		assertTrue("expected edge 1", this.testInstance.containsDirectedEdge(this.node1, this.node2));
 		assertTrue("expected edge 2", this.testInstance.containsDirectedEdge(this.node2, this.node1));
 		assertTrue("expected edge 3", this.testInstance.containsDirectedEdge(this.node2, this.node6));
 		assertTrue("expected edge 4", this.testInstance.containsDirectedEdge(this.node2, this.node4));
 		assertTrue("expected edge 5", this.testInstance.containsDirectedEdge(this.node7, this.node6));
 		assertTrue("expected edge 6", this.testInstance.containsDirectedEdge(this.node6, this.node7));
 		assertTrue("expected edge 7", this.testInstance.containsDirectedEdge(this.node7, this.node3));
 		assertTrue("expected edge 8/9", this.testInstance.containsDirectedEdge(this.node1, this.node1));
 		assertFalse("expected no edge", this.testInstance.containsDirectedEdge(null, this.node7));
 		assertFalse("expected no edge", this.testInstance.containsDirectedEdge(this.node8, null));
 		assertFalse("expected no edge", this.testInstance.containsDirectedEdge(null, null));
 
 		assertFalse("expected no edge", this.testInstance.containsDirectedEdge(this.node2, this.node3));
 		assertFalse("expected no edge", this.testInstance.containsDirectedEdge(this.node3, this.node7));
 		assertFalse("expected no edge", this.testInstance.containsDirectedEdge(this.node8, this.node7));
 	}
 
 	@Test
 	public final void testContainsEdgeINodeINode() {
 		assertTrue("expected edge 1", this.testInstance.containsConnection(this.node1, this.node2));
 		assertTrue("expected edge 2", this.testInstance.containsConnection(this.node2, this.node1));
 		assertTrue("expected edge 3", this.testInstance.containsConnection(this.node2, this.node6));
 		assertTrue("expected edge 4", this.testInstance.containsConnection(this.node2, this.node4));
 		assertTrue("expected edge 5", this.testInstance.containsConnection(this.node7, this.node6));
 		assertTrue("expected edge 6", this.testInstance.containsConnection(this.node6, this.node7));
 		assertTrue("expected edge 7", this.testInstance.containsConnection(this.node7, this.node3));
 		assertTrue("expected edge 8/9", this.testInstance.containsConnection(this.node1, this.node1));
 
 		assertFalse("expected no edge", this.testInstance.containsConnection(this.node2, this.node3));
 		assertTrue("expected no edge", this.testInstance.containsConnection(this.node3, this.node7));
 		assertFalse("expected no edge", this.testInstance.containsConnection(this.node8, this.node7));
 		assertFalse("expected no edge", this.testInstance.containsConnection(null, this.node7));
 		assertFalse("expected no edge", this.testInstance.containsConnection(this.node8, null));
 		assertFalse("expected no edge", this.testInstance.containsConnection(null, null));
 	}
 
 	@Test
 	public final void testContainsEdgeIEdge() {
 		assertTrue("expected edge 1", this.testInstance.containsEdge(this.edge1));
 		assertTrue("expected edge 2", this.testInstance.containsEdge(this.edge2));
 		assertTrue("expected edge 3", this.testInstance.containsEdge(this.edge3));
 		assertTrue("expected edge 4", this.testInstance.containsEdge(this.edge4));
 		assertTrue("expected edge 5", this.testInstance.containsEdge(this.edge5));
 		assertTrue("expected edge 6", this.testInstance.containsEdge(this.edge6));
 		assertTrue("expected edge 7", this.testInstance.containsEdge(this.edge7));
 		assertTrue("expected edge 8", this.testInstance.containsEdge(this.edge8));
 		assertTrue("expected edge 8", this.testInstance.containsEdge(this.edge9));
 		assertFalse("expected no edge", this.testInstance.containsConnection(null));
 	}
 
 	@Test
 	public final void testContainsEdgeInt() {
 		assertTrue("expected edge 1", this.testInstance.containsEdge(this.edge1.getIndex()));
 		assertTrue("expected edge 2", this.testInstance.containsEdge(this.edge2.getIndex()));
 		assertTrue("expected edge 3", this.testInstance.containsEdge(this.edge3.getIndex()));
 		assertTrue("expected edge 4", this.testInstance.containsEdge(this.edge4.getIndex()));
 		assertTrue("expected edge 5", this.testInstance.containsEdge(this.edge5.getIndex()));
 		assertTrue("expected edge 6", this.testInstance.containsEdge(this.edge6.getIndex()));
 		assertTrue("expected edge 7", this.testInstance.containsEdge(this.edge7.getIndex()));
 		assertTrue("expected edge 8", this.testInstance.containsEdge(this.edge8.getIndex()));
 		assertTrue("expected edge 9", this.testInstance.containsEdge(this.edge9.getIndex()));
 		assertFalse("expected no edge", this.testInstance.containsEdge(UNKNOWN_EDGE_HIGH_IDX));
 		assertFalse("expected no edge", this.testInstance.containsEdge(UNKNOWN_EDGE_LOW_IDX));
 	}
 
 	@Test
 	public final void testContainsNodeInt() {
 		assertTrue("expected node 1", this.testInstance.containsNode(this.node1.getIndex()));
 		assertTrue("expected node 2", this.testInstance.containsNode(this.node2.getIndex()));
 		assertTrue("expected node 3", this.testInstance.containsNode(this.node3.getIndex()));
 		assertTrue("expected node 4", this.testInstance.containsNode(this.node4.getIndex()));
 		assertTrue("expected node 5", this.testInstance.containsNode(this.node5.getIndex()));
 		assertTrue("expected node 6", this.testInstance.containsNode(this.node6.getIndex()));
 		assertTrue("expected node 7", this.testInstance.containsNode(this.node7.getIndex()));
 		assertTrue("expected node 8", this.testInstance.containsNode(this.node8.getIndex()));
 		assertFalse("expected no node", this.testInstance.containsNode(UNKNOWN_NODE_HIGH_IDX));
 		assertFalse("expected no node", this.testInstance.containsNode(UNKNOWN_NODE_LOW_IDX));
 	}
 
 	@Test
 	public final void testContainsNodeINode() {
 		assertTrue("expected node 1", this.testInstance.containsNode(this.node1));
 		assertTrue("expected node 2", this.testInstance.containsNode(this.node2));
 		assertTrue("expected node 3", this.testInstance.containsNode(this.node3));
 		assertTrue("expected node 4", this.testInstance.containsNode(this.node4));
 		assertTrue("expected node 5", this.testInstance.containsNode(this.node5));
 		assertTrue("expected node 6", this.testInstance.containsNode(this.node6));
 		assertTrue("expected node 7", this.testInstance.containsNode(this.node7));
 		assertTrue("expected node 8", this.testInstance.containsNode(this.node8));
 		assertFalse("expected no node", this.testInstance.containsNode(null));
 	}
 
 	@Test
 	public final void testGetEdge() {
 		assertEquals("expected edge 1", edge1, this.testInstance.getEdge(this.edge1.getIndex()));
 		assertEquals("expected edge 2", edge2, this.testInstance.getEdge(this.edge2.getIndex()));
 		assertEquals("expected edge 3", edge3, this.testInstance.getEdge(this.edge3.getIndex()));
 		assertEquals("expected edge 4", edge4, this.testInstance.getEdge(this.edge4.getIndex()));
 		assertEquals("expected edge 5", edge5, this.testInstance.getEdge(this.edge5.getIndex()));
 		assertEquals("expected edge 6", edge6, this.testInstance.getEdge(this.edge6.getIndex()));
 		assertEquals("expected edge 7", edge7, this.testInstance.getEdge(this.edge7.getIndex()));
 		assertEquals("expected edge 8", edge8, this.testInstance.getEdge(this.edge8.getIndex()));
 		assertEquals("expected edge 9", edge9, this.testInstance.getEdge(this.edge9.getIndex()));
 		assertTrue("expected no edge", this.testInstance.getEdge(UNKNOWN_EDGE_HIGH_IDX) == null);
 		assertTrue("expected no edge", this.testInstance.getEdge(UNKNOWN_EDGE_LOW_IDX) == null);
 	}
 	
 	@Test
 	public final void testGetEdgeIterator() {
 		Iterator<BaseCompoundEdge> iter = this.testInstance.edgeIterator();
 		CompoundEdge expectedIterationOrder[] = { edge2, edge8, edge9, edge1, edge3, edge4, edge7, edge5, edge6 };
 		List<CompoundEdge> expectedEdgeList = Arrays.asList(expectedIterationOrder);
 		for(CompoundEdge expectedEdge : expectedEdgeList){
 			assertTrue("edge available", iter.hasNext());
 			BaseCompoundEdge actualEdge = iter.next();
 			assertEquals("next edge idx", expectedEdge.getIndex(), actualEdge.getIndex());
 			assertEquals("next edge", expectedEdge, actualEdge);
 //			System.out.print("Edge ID = "); System.out.println(actualEdge.getIndex());
 		}
 		assertFalse("iterator is exhausted as expected", iter.hasNext());
 		try{
 			iter.next();
 			fail("Expected a NoSuchElementException");
 		}
 		catch(NoSuchElementException e){
 			// exception is expected here
 		}
 	}
 
 	@Test
 	public final void testGetNode() {
 		assertEquals("expected node 1", node1, this.testInstance.getNode(this.node1.getIndex()));
 		assertEquals("expected node 2", node2, this.testInstance.getNode(this.node2.getIndex()));
 		assertEquals("expected node 3", node3, this.testInstance.getNode(this.node3.getIndex()));
 		assertEquals("expected node 4", node4, this.testInstance.getNode(this.node4.getIndex()));
 		assertEquals("expected node 5", node5, this.testInstance.getNode(this.node5.getIndex()));
 		assertEquals("expected node 6", node6, this.testInstance.getNode(this.node6.getIndex()));
 		assertEquals("expected node 7", node7, this.testInstance.getNode(this.node7.getIndex()));
 		assertEquals("expected node 8", node8, this.testInstance.getNode(this.node8.getIndex()));
 		assertTrue("expected no node", this.testInstance.getNode(UNKNOWN_NODE_HIGH_IDX) == null);
 		assertTrue("expected no node", this.testInstance.getNode(UNKNOWN_NODE_LOW_IDX) == null);
 	}
 
 	@Test
 	public final void testGraphStructure(){
 		assertEquals("root subgraph edges", NUM_ROOT_NODE_EDGES, rootNode.getChildCompoundGraph().getNumEdges());
 		assertEquals("node1 subgraph edges", NUM_NODE1_EDGES, node1.getChildCompoundGraph().getNumEdges());
 		assertEquals("node2 subgraph edges", NUM_NODE2_EDGES, node2.getChildCompoundGraph().getNumEdges());
 		assertEquals("node3 subgraph edges", NUM_NODE3_EDGES, node3.getChildCompoundGraph().getNumEdges());
 		assertEquals("node4 subgraph edges", NUM_NODE4_EDGES, node4.getChildCompoundGraph().getNumEdges());
 		assertEquals("node5 subgraph edges", NUM_NODE5_EDGES, node5.getChildCompoundGraph().getNumEdges());
 		assertEquals("node6 subgraph edges", NUM_NODE6_EDGES, node6.getChildCompoundGraph().getNumEdges());
 		assertEquals("node7 subgraph edges", NUM_NODE7_EDGES, node7.getChildCompoundGraph().getNumEdges());
 		assertEquals("node8 subgraph edges", NUM_NODE8_EDGES, node8.getChildCompoundGraph().getNumEdges());
 	}
 	
 	@Test
 	public final void testCreateSubGraphWithSelfEdges () throws Exception
 	{
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		subGraphfactory.addNode(node1) ;
 		SubCompoundGraph  nonInducedSubGraph = subGraphfactory.createSubgraph() ;
 		assertEquals ( "from original Graph" , testInstance , nonInducedSubGraph.getSuperGraph()) ;
 		assertEquals ( "3 nodes" , NUMERIC_VALUE[3] , nonInducedSubGraph.getNumNodes()) ;
 		assertEquals ( "2 edges" , NUMERIC_VALUE[2] , nonInducedSubGraph.getNumEdges()) ;
 	}
 	
 	@Test
 	public final void testCreateSubGraphWithInternalEdges () throws Exception
 	{
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		subGraphfactory.addNode(node2) ;
 		SubCompoundGraph  nonInducedSubGraph = subGraphfactory.createSubgraph() ;
 		assertEquals ( "from original Graph" , testInstance , nonInducedSubGraph.getSuperGraph()) ;
 		assertEquals ( "5 nodes" , NUMERIC_VALUE[5] , nonInducedSubGraph.getNumNodes()) ;
 		assertEquals ( "3 edges" , NUMERIC_VALUE[3] , nonInducedSubGraph.getNumEdges()) ;
 	}
 	
 	@Test
 	public final void testCreateInducedSubGraph () throws Exception
 	{
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		subGraphfactory.addNode(node1) ;
 		subGraphfactory.addNode(node2) ;
 		SubCompoundGraph  inducedSubGraph = subGraphfactory.createInducedSubgraph() ;
 		assertEquals ( "from original Graph" , testInstance , inducedSubGraph.getSuperGraph()) ;
 		assertEquals ( "8 nodes" , NUMERIC_VALUE[8] , inducedSubGraph.getNumNodes()) ;
 		assertEquals ( "9 edges" , NUMERIC_VALUE[9] , inducedSubGraph.getNumEdges()) ;
 	}
 	
 	@Test
 	public final void testGetNodeIterator() {
 		Iterator<BaseCompoundNode> iter = this.testInstance.nodeIterator();
 		CompoundNode expectedIterationOrder[] = { rootNode, node1, node2, node3,
 				node4, node5, node6, node7, node8 };
 		List<CompoundNode> expectedNodeList = Arrays.asList(expectedIterationOrder);
 		for(CompoundNode expectedNode : expectedNodeList){
 			assertTrue("node available", iter.hasNext());
 			assertEquals("expected next node", expectedNode, iter.next());
 		}
 		assertFalse("iterator is exhausted as expected", iter.hasNext());
 		try{
 			iter.next(); 	
 			fail("Expected a NoSuchElementException");
 		}
 		catch(NoSuchElementException e){
 			// exception is expected here
 		}
 	}
 
 	@Test
 	public final void testGetNumEdges() {
 		assertEquals("expected num edges", EXPECTED_NUM_EDGES, this.testInstance.getNumEdges());
 	}
 
 	@Test
 	public final void testGetNumNodes() {
 		assertEquals("expected num nodes", EXPECTED_NUM_NODES, this.testInstance.getNumNodes());
 	}
 
 	@Test(expected=IllegalStateException.class)
 	public final void testRemoveSubgraphRemoveRootNotAllowed() {
 		SubCompoundGraphFactory subgraphFact = this.testInstance.subgraphFactory();
 		subgraphFact.addNode(this.rootNode);
 		SubCompoundGraph subGraph = subgraphFact.createSubgraph();
 		this.testInstance.removeSubgraph(subGraph);
 	}
 
 	@Test
 	public final void testRemoveSubgraphWithSingleNode() {
 		SubCompoundGraphFactory subgraphFact = this.testInstance.subgraphFactory();
 		subgraphFact.addNode(this.node2);
 		SubCompoundGraph subGraph = subgraphFact.createInducedSubgraph();
 		this.testInstance.removeSubgraph(subGraph);
 		assertEquals("Expected num nodes", EXPECTED_NODES_AFTER_NODE2_DELETED, this.testInstance.getNumNodes());
 		assertEquals("Expected num edges", EXPECTED_EDGES_AFTER_NODE2_DELETED, this.testInstance.getNumEdges());
 		assertTrue("node 2 removed", this.testInstance.containsNode(this.node2)== false);
 		assertTrue("edge 2 removed", this.testInstance.containsEdge(this.edge2)== false);
 		assertTrue("edge 1 removed", this.testInstance.containsEdge(this.edge1)== false);
 		assertTrue("edge 3 removed", this.testInstance.containsEdge(this.edge2)== false);
 		assertTrue("edge 4 removed", this.testInstance.containsEdge(this.edge4)== false);
 		assertTrue("node 2 marked removed", this.node2.isRemoved());
 		assertTrue("edge 2 marked removed", this.edge2.isRemoved());
 		assertTrue("edge 1 marked removed", this.edge1.isRemoved());
 		assertTrue("edge 3 marked removed", this.edge2.isRemoved());
 		assertTrue("edge 4 marked removed", this.edge4.isRemoved());
 	}
 
 	@Test
 	public final void testRemoveSubgraphWithSingleEdge() {
 		SubCompoundGraphFactory subgraphFact = this.testInstance.subgraphFactory();
 		subgraphFact.addEdge(this.edge2);
 		SubCompoundGraph subGraph = subgraphFact.createInducedSubgraph();
 		this.testInstance.removeSubgraph(subGraph);
 		assertEquals("Expected num nodes", EXPECTED_NUM_NODES, this.testInstance.getNumNodes());
 		assertEquals("Expected num edges", EXPECTED_EDGES_AFTER_EDGE2_DELETED, this.testInstance.getNumEdges());
 		assertTrue("edge 2 removed", this.testInstance.containsDirectedEdge(this.edge2.getConnectedNodes())== false);
 		assertTrue("edge 2 removed", this.testInstance.containsEdge(this.edge2)== false);
 		assertTrue("edge 2 marked removed", this.edge2.isRemoved());
 	}
 
 	@Test
 	public final void testRemoveInducedSubgraphWithSingleEdge() {
 		SubCompoundGraphFactory subgraphFact = this.testInstance.subgraphFactory();
 		subgraphFact.addNode(this.node5);
 		subgraphFact.addEdge(this.edge2);
 		SubCompoundGraph subGraph = subgraphFact.createInducedSubgraph();
 		this.testInstance.removeSubgraph(subGraph);
 		assertEquals("Expected num nodes", EXPECTED_NODES_AFTER_NODE5_DELETED, this.testInstance.getNumNodes());
 		assertEquals("Expected num edges", EXPECTED_EDGES_AFTER_EDGE2_NODE5_DELETED, this.testInstance.getNumEdges());
 		assertTrue("node 5 removed", this.testInstance.containsNode(this.node5)== false);
 		assertTrue("edge 2 removed", this.testInstance.containsEdge(this.edge2)== false);
 		assertTrue("edge 2 removed", this.testInstance.containsDirectedEdge(this.edge2.getConnectedNodes())== false);
 		assertTrue("edge 1 present", this.testInstance.containsEdge(this.edge1));
 		assertTrue("node 5 marked removed", this.node5.isRemoved());
 		assertTrue("edge 2 marked removed", this.edge2.isRemoved());
 	}
 
 	@Test
 	public final void testRemoveSubgraphRemoveInducedGraph() {
 		SubCompoundGraphFactory subgraphFact = this.testInstance.subgraphFactory();
 		subgraphFact.addNode(this.node1);
 		subgraphFact.addNode(this.node2);
 		SubCompoundGraph subGraph = subgraphFact.createInducedSubgraph();
 		this.testInstance.removeSubgraph(subGraph);
 		assertEquals("Expected num nodes", EXPECTED_NODES_AFTER_NODES_1_AND_2_DELETED, this.testInstance.getNumNodes());
 		assertEquals("Expected num edges", EXPECTED_EDGES_AFTER_NODES_1_AND_2_DELETED, this.testInstance.getNumEdges());
 		assertTrue("node 1 removed", this.testInstance.containsNode(this.node1)== false);
 		assertTrue("node 2 removed", this.testInstance.containsNode(this.node2)== false);
 		assertTrue("edge 2 removed", this.testInstance.containsEdge(this.edge2)== false);
 		assertTrue("edge 2 removed", this.testInstance.containsDirectedEdge(this.edge2.getConnectedNodes())== false);
 		assertTrue("edge 1 removed", this.testInstance.containsEdge(this.edge1) == false);
 		assertTrue("node 1 marked removed", this.node1.isRemoved());
 		assertTrue("node 2 marked removed", this.node2.isRemoved());
 	}
 
 	@Test
 	public final void testRemoveSubgraphRemoveNonInducedGraph() {
 		SubCompoundGraphFactory subgraphFact = this.testInstance.subgraphFactory();
 		subgraphFact.addNode(this.node1);
 		subgraphFact.addNode(this.node2);
 		SubCompoundGraph subGraph = subgraphFact.createSubgraph();
 		this.testInstance.removeSubgraph(subGraph);
 		assertEquals("Expected num nodes", EXPECTED_NODES_AFTER_NODES_1_AND_2_DELETED, this.testInstance.getNumNodes());
 		assertEquals("Expected num edges", EXPECTED_EDGES_AFTER_NODES_1_AND_2_DELETED, this.testInstance.getNumEdges());
 		assertTrue("node 1 removed", this.testInstance.containsNode(this.node1)== false);
 		assertTrue("node 2 removed", this.testInstance.containsNode(this.node2)== false);
 		assertTrue("edge 2 removed", this.testInstance.containsEdge(this.edge2)== false);
 		assertTrue("edge 2 removed", this.testInstance.containsDirectedEdge(this.edge2.getConnectedNodes())== false);
 		assertTrue("edge 1 removed", this.testInstance.containsEdge(this.edge1) == false);
 		assertTrue("node 1 marked removed", this.node1.isRemoved());
 		assertTrue("node 2 marked removed", this.node2.isRemoved());
 	}
 
 //	@Test
 //	public final void testGetLcaNode() {
 //		assertEquals("expected lcm node1", node1, this.testInstance.getLcaNode(node3, node4));
 //		assertEquals("expected lcm root node", rootNode, this.testInstance.getLcaNode(node3, node8));
 //		assertEquals("expected lcm root node", rootNode, this.testInstance.getLcaNode(node8, node4));
 //		assertEquals("expected lcm node2", node2, this.testInstance.getLcaNode(node5, node8));
 //		assertEquals("expected lcm node1", node1, this.testInstance.getLcaNode(node1, node1));
 //		assertEquals("expected lcm node7", node7, this.testInstance.getLcaNode(node8, node7));
 //		assertEquals("expected lcm node6", node6, this.testInstance.getLcaNode(node6, node8));
 //		assertEquals("expected lcm root node", rootNode, this.testInstance.getLcaNode(node8, rootNode));
 //		assertEquals("expected lcm root node", rootNode, this.testInstance.getLcaNode(rootNode, node5));
 //		try{
 //			this.testInstance.getLcaNode(null, node6);
 //			fail("expected exception to be thrown");
 //		}
 //		catch(IllegalArgumentException e){
 //			// success! expected exception to be thrown
 //		}
 //		try{
 //			this.testInstance.getLcaNode(null, null);
 //			fail("expected exception to be thrown");
 //		}
 //		catch(IllegalArgumentException e){
 //			// success! expected exception to be thrown
 //		}
 //		try{
 //			this.testInstance.getLcaNode(node7, null);
 //			fail("expected exception to be thrown");
 //		}
 //		catch(IllegalArgumentException e){
 //			// success! expected exception to be thrown
 //		}
 //	}
 	
 	@Test//(expected=IllegalArgumentException.class) == ?? 
 	public final void testCopyNonInducedSubGraph(){
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		subGraphfactory.addNode(node1) ;
 		SubCompoundGraph  nonInducedSubGraph = subGraphfactory.createSubgraph() ;
 		this.testInstance.getRootNode().getChildCompoundGraph().copyHere(nonInducedSubGraph) ;
 		assertEquals ( "copied nodes" , EXPECTED_NUM_NODES + 3 , testInstance.getNumNodes() ) ;
 		assertEquals ( "copied edges" , EXPECTED_NUM_EDGES + 2, testInstance.getNumEdges() ) ;
 	}
 	
 	@Test
 	public final void testCopyInducedSubGraph(){
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		subGraphfactory.addNode(node1) ;
 		subGraphfactory.addNode(node2) ;
 		SubCompoundGraph  inducedSubGraph = subGraphfactory.createInducedSubgraph() ;
 		assertEquals ( "rootNode has 2 Nodes" , NUMERIC_VALUE[2] , rootNode.getChildCompoundGraph().getNumNodes()) ;
 		this.testInstance.getRootNode().getChildCompoundGraph().copyHere(inducedSubGraph) ;
 		assertEquals ( "copied nodes" , EXPECTED_NUM_NODES + 8 , testInstance.getNumNodes() ) ;
 		assertEquals ( "Copied edges" , EXPECTED_NUM_EDGES + 9, testInstance.getNumEdges() ) ;
 		assertEquals ( "rootNode has 4 Nodes" , NUMERIC_VALUE[4] , rootNode.getChildCompoundGraph().getNumNodes()) ;
 	}
 	
 	@Test//(expected=IllegalArgumentException.class) == ?? 
 	public final void testCopyEmptySubGraph(){
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		SubCompoundGraph  inducedSubGraph = subGraphfactory.createInducedSubgraph() ;
 		this.testInstance.getRootNode().getChildCompoundGraph().copyHere(inducedSubGraph) ;
 		assertEquals ( "copied Edges nodes" , EXPECTED_NUM_NODES , testInstance.getNumNodes() ) ;
 		assertEquals ( "CopiedEdges edges" , EXPECTED_NUM_EDGES, testInstance.getNumEdges() ) ;
 	}
 	
 	@Test
 	public final void testSaveAndRestoreState(){
 		IGraphState<BaseCompoundNode, BaseCompoundEdge> originalState = testInstance.getCurrentState() ;
 		assertTrue ( "state saved" , originalState != null) ;
 		assertEquals ( "state belongs to Graph" , testInstance , originalState.getGraph() ) ;
 		assertEquals ("check saved nodes" , SAVED_NODES , originalState.getNodeStates().toString()) ;
 		assertEquals ("check saved edges" , SAVED_EDGES , originalState.getEdgeStates().toString()) ;
		testInstance.nodeFactory().createNode() ;
 		assertEquals ( "one more node" , EXPECTED_NUM_NODES + 1 , testInstance.getNumNodes() ) ;
 		CompoundEdgeFactory edgeFactory = testInstance.edgeFactory() ;
 		edgeFactory.setPair(node8, node5) ; 
		edgeFactory.createEdge() ;
 		assertEquals ( "one more edge" , EXPECTED_NUM_EDGES + 1 , testInstance.getNumEdges() ) ;
 		IGraphState<BaseCompoundNode, BaseCompoundEdge> newState = testInstance.getCurrentState() ;
 		assertEquals ("check saved nodes" , NEW_STATE_SAVED_NODES , newState.getNodeStates().toString()) ;
 		assertEquals ("check saved edges" , NEW_STATE_SAVED_EDGES , newState.getEdgeStates().toString()) ;
 		testInstance.restoreState(originalState) ;
 		assertEquals ( "original nodes" , EXPECTED_NUM_NODES , testInstance.getNumNodes() ) ;
 		assertEquals ( "original edges" , EXPECTED_NUM_EDGES , testInstance.getNumEdges() ) ;
 	}
 	
 	@Test
 	public final void testDeleteOrdinarySubGraph () throws Exception 
 	{
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		subGraphfactory.addNode(node1) ;
 		SubCompoundGraph  ordinarySubGraph = subGraphfactory.createSubgraph() ;
 		testInstance.removeSubgraph(ordinarySubGraph) ;
 		assertEquals ( "3 less Nodes" , EXPECTED_NUM_NODES - 3 , testInstance.getNumNodes()) ;
 		assertEquals ( "6 less Edges" , EXPECTED_NUM_EDGES - 6 , testInstance.getNumEdges()) ;
 		assertEquals ("no nodes on subgraph" , NUMERIC_VALUE[0] , ordinarySubGraph.getNumNodes()) ;
 		assertEquals ("no edges on subgraph" , NUMERIC_VALUE[0] , ordinarySubGraph.getNumEdges()) ;
 	}
 	
 	@Test
 	public final void testDeleteInducedSubGraph () throws Exception 
 	{
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		subGraphfactory.addNode(node1) ;
 		subGraphfactory.addNode(node2) ;
 		SubCompoundGraph  inducedSubGraph = subGraphfactory.createInducedSubgraph() ;
 		testInstance.removeSubgraph(inducedSubGraph) ;
 		assertEquals ( "3 less Nodes" , EXPECTED_NUM_NODES - 8 , testInstance.getNumNodes()) ;
 		assertEquals ( "6 less Edges" , EXPECTED_NUM_EDGES - 9 , testInstance.getNumEdges()) ;
 		assertEquals ("empty nodes on subgraph" , NUMERIC_VALUE[0] , inducedSubGraph.getNumNodes()) ;
 		assertEquals ("empty edges on subgraph" , NUMERIC_VALUE[0] , inducedSubGraph.getNumEdges()) ;
 	}
 	
 	@Test
 	public final void testMoveOrdinarySubGraph () throws Exception 
 	{
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		subGraphfactory.addNode(node6) ;
 		SubCompoundGraph  ordinarySubGraph = subGraphfactory.createSubgraph() ;
 		assertEquals ( "node1 has 2 nodes" , NUMERIC_VALUE[2] , node1.getChildCompoundGraph().getNumNodes()) ;
 		assertEquals ( "node2 has 2 nodes" , NUMERIC_VALUE[2] , node1.getChildCompoundGraph().getNumNodes()) ;
 		rootNode.getChildCompoundGraph().moveHere(ordinarySubGraph) ;		
 		assertEquals ( "same Nodes" , EXPECTED_NUM_NODES , testInstance.getNumNodes()) ;
 		assertEquals ( "2 less Edges" , EXPECTED_NUM_EDGES - 2 , testInstance.getNumEdges()) ;
 		assertEquals ( "rootNode has 3 nodes" , NUMERIC_VALUE[3] , rootNode.getChildCompoundGraph().getNumNodes()) ;
 		assertEquals ( "node2 has 1 node" , NUMERIC_VALUE[1] , node2.getChildCompoundGraph().getNumNodes()) ;
 		
 	}
 	
 	@Test
 	public final void testMoveInducedSubGraph () throws Exception 
 	{
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		subGraphfactory.addNode(node6) ;
 		SubCompoundGraph  inducedSubGraph = subGraphfactory.createInducedSubgraph() ;
 		rootNode.getChildCompoundGraph().moveHere(inducedSubGraph) ;
 
 		assertEquals ( "same Nodes" , EXPECTED_NUM_NODES , testInstance.getNumNodes()) ;
 		assertEquals ( "2 less Edges" , EXPECTED_NUM_EDGES - 2 , testInstance.getNumEdges()) ;
 		
 		assertEquals ( "rootNode has 3 nodes" , NUMERIC_VALUE[3] , rootNode.getChildCompoundGraph().getNumNodes()) ;
 		assertEquals ( "node2 has 1 node" , NUMERIC_VALUE[1] , node2.getChildCompoundGraph().getNumNodes()) ;
 	}
 	
 	@Test
 	public final void testIsSubGraphConsistent () throws Exception 
 	{
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		subGraphfactory.addNode(node1) ;
 		SubCompoundGraph  node1SubGraph = subGraphfactory.createSubgraph() ;
 		assertFalse("node 4 removed", node4.isRemoved());
 		assertTrue ( "is Consistent with diagram" , node1SubGraph.isConsistentSnapShot()) ;
 		SubCompoundGraphFactory subGraphfactory2 = testInstance.subgraphFactory() ; 
 		subGraphfactory2.addNode(node4) ;
 		SubCompoundGraph  node4SubGraph = subGraphfactory2.createSubgraph() ;
 		testInstance.removeSubgraph(node4SubGraph) ;
 		assertTrue("node 4 removed", node4.isRemoved());
 		assertEquals ( "one less Node" , EXPECTED_NUM_NODES - 1 , testInstance.getNumNodes()) ;
 		assertFalse ( "is not Consistent with diagram" , node1SubGraph.isConsistentSnapShot()) ;
 	}
 }
