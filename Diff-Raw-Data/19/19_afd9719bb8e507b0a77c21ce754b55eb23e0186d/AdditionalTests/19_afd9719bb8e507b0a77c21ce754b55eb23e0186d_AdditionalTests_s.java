 package uk.ed.inf.graph.compound.impl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import uk.ed.inf.graph.compound.base.BaseCompoundEdge;
 import uk.ed.inf.graph.compound.base.BaseCompoundNode;
 import uk.ed.inf.graph.state.IGraphState;
 
 public class AdditionalTests {
 	
 	private CompoundGraph testInstance ;
 	private CompoundGraph emptyTestInstance ;
 	private CompoundNode rootNode;
 	private CompoundNode node1;
 //	private CompoundNode node2;
 	private CompoundEdge edge1;
 	
 	private static final String EMPTY_NODE_BITSTRING = "{0}" ;
 	private static final String EMPTY_EDGE_BITSTRING = "{}" ;
 	
 	private static final String NOT_EMPTY_NODE_BITSTRING = "{0, 1, 2}" ;
 	private static final String NOT_EMPTY_EDGE_BITSTRING = "{0}" ;
 	
 	private static final String REMOVED_NODE_BITSTRING = "{0, 2}" ;
 	private static final String REMOVED_NODE_EDGES_BITSTRING = "{}" ;
 	
 	private static final int ONLY_ROOT_NODE = 1 ;
 	private static final int NO_EDGES = 0 ;
 	private static final int NUMERIC_VALUES[] = {0,1,2,3,4,5,6,7,8,9} ;
 	
 	
 	@Before
 	public void setUp() throws Exception {
 		emptyTestInstance = new CompoundGraph () ;
 		testInstance = new CompoundGraph () ;
 		this.rootNode = this.testInstance.getRootNode();
 		CompoundNodeFactory rootNodeFact = this.testInstance.getRootNode().getChildCompoundGraph().nodeFactory();
 		node1 = rootNodeFact.createNode();
//		node2 = rootNodeFact.createNode();
 		
 		
 		CompoundEdgeFactory edgeFact = this.testInstance.edgeFactory();
 		edgeFact.setPair(node1, node1);
 		edge1 = edgeFact.createEdge();
 	}
 	
 	@After
 	public void tearDown () throws Exception {
 		
 	}
 	
 	@Test
 	public final void testCannotCopyASingleEdge () throws Exception 
 	{
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		subGraphfactory.addEdge(edge1) ;
 		SubCompoundGraph  ordinarySubGraph = subGraphfactory.createSubgraph() ;
 		assertFalse ( "cannot copy" , rootNode.getChildCompoundGraph().canCopyHere(ordinarySubGraph) );
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public final void testTryToCopyASingleEdge () throws Exception 
 	{
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		subGraphfactory.addEdge(edge1) ;
 		SubCompoundGraph  ordinarySubGraph = subGraphfactory.createSubgraph() ;
 		rootNode.getChildCompoundGraph().copyHere(ordinarySubGraph) ;
 	}
 	
 	@Test
 	public final void testCannotMoveRootNode () throws Exception 
 	{
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		subGraphfactory.addNode(rootNode) ;
 		SubCompoundGraph  ordinarySubGraph = subGraphfactory.createSubgraph() ;
 		assertFalse("cannot move rootNode" , node1.getChildCompoundGraph().canMoveHere(ordinarySubGraph) );
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public final void testTryToMoveRootNode () throws Exception 
 	{
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		subGraphfactory.addNode(rootNode) ;
 		SubCompoundGraph  ordinarySubGraph = subGraphfactory.createSubgraph() ;
 		rootNode.getChildCompoundGraph().moveHere(ordinarySubGraph) ;
 		assertFalse("cannot move rootNode" , node1.getChildCompoundGraph().canMoveHere(ordinarySubGraph) );
 	}	
 	
 	@Test
 	public final void testCannotCopyRootNode () throws Exception 
 	{
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		subGraphfactory.addNode(rootNode) ;
 		SubCompoundGraph  ordinarySubGraph = subGraphfactory.createSubgraph() ;
 		assertFalse("cannot move rootNode" , node1.getChildCompoundGraph().canCopyHere(ordinarySubGraph) );
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public final void testTryToCopyRootNode () throws Exception 
 	{
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		subGraphfactory.addNode(rootNode) ;
 		SubCompoundGraph  ordinarySubGraph = subGraphfactory.createSubgraph() ;
 		rootNode.getChildCompoundGraph().copyHere(ordinarySubGraph) ;
 	}
 	
 	@Test(expected=IllegalStateException.class)
 	public final void testTryToDeleteRootNode () throws Exception 
 	{
 		SubCompoundGraphFactory subGraphfactory = testInstance.subgraphFactory() ; 
 		subGraphfactory.addNode(rootNode) ;
 		SubCompoundGraph  ordinarySubGraph = subGraphfactory.createSubgraph() ;
 		testInstance.removeSubgraph(ordinarySubGraph) ;
 	}
 	
 	@Test
 	public final void testSaveStateOfEmptyDiagram () throws Exception
 	{
 		IGraphState<BaseCompoundNode, BaseCompoundEdge> emptyGraphState = emptyTestInstance.getCurrentState() ;
 		
 		assertEquals ( "only one node" , EMPTY_NODE_BITSTRING , emptyGraphState.getNodeStates().toString()) ;
 		assertEquals ( "no egdes" , EMPTY_EDGE_BITSTRING , emptyGraphState.getEdgeStates().toString() );
 	}
 	
 	@Test
 	public final void testSaveStateOfPopulatedDiagram () throws Exception
 	{
 		IGraphState<BaseCompoundNode, BaseCompoundEdge> nonEmptyGraphState = testInstance.getCurrentState() ;
 		
 		assertEquals ( "two nodes" , NOT_EMPTY_NODE_BITSTRING , nonEmptyGraphState.getNodeStates().toString()) ;
 		assertEquals ( "one node" , NOT_EMPTY_EDGE_BITSTRING , nonEmptyGraphState.getEdgeStates().toString() );
 	}	
 	
 	@Test(expected=IllegalArgumentException.class)
 	public final void testRestoreStateFromAnotherGraph () throws Exception
 	{
 		IGraphState<BaseCompoundNode, BaseCompoundEdge> nonEmptyGraphState = testInstance.getCurrentState() ;
 		emptyTestInstance.restoreState(nonEmptyGraphState) ;
 		assertEquals ( "only root" , ONLY_ROOT_NODE , emptyTestInstance.getNumNodes()) ;
 		assertEquals ( "no edges" , NO_EDGES , emptyTestInstance.getNumEdges()) ;
 	}
 	
 	@Test
 	public final void testRestoreStateFromDeletedElementsGraph () throws Exception 
 	{
 		assertEquals ( "three Nodes" , NUMERIC_VALUES[3] , testInstance.getNumNodes() ) ;
 		assertEquals ( "one Edge" , NUMERIC_VALUES[1] , testInstance.getNumEdges() ) ;
 		IGraphState<BaseCompoundNode, BaseCompoundEdge> nonEmptyGraphState = testInstance.getCurrentState() ;
 		assertNotNull ( "state exists" , nonEmptyGraphState) ;
 		assertEquals ( "has Nodes" , NOT_EMPTY_NODE_BITSTRING , nonEmptyGraphState.getNodeStates().toString() ) ;
 		assertEquals ( "has Edge" , NOT_EMPTY_EDGE_BITSTRING , nonEmptyGraphState.getEdgeStates().toString() ) ;
 		SubCompoundGraphFactory subGraphFactory = testInstance.subgraphFactory() ;
 		subGraphFactory.addNode(node1) ;
 		SubCompoundGraph subGraph = subGraphFactory.createSubgraph() ;
 		testInstance.removeSubgraph(subGraph) ;
 		IGraphState<BaseCompoundNode, BaseCompoundEdge> removedNodeGraphState = testInstance.getCurrentState() ;
 		assertEquals ( "removed one node" , REMOVED_NODE_BITSTRING , removedNodeGraphState.getNodeStates().toString()) ;
 		assertEquals ( "removed one node" , REMOVED_NODE_EDGES_BITSTRING , removedNodeGraphState.getEdgeStates().toString()) ;
 		assertEquals ( "only two nodes" , NUMERIC_VALUES[2] , testInstance.getNumNodes() ) ;
 		assertEquals ( "no edges" , NUMERIC_VALUES[0] , testInstance.getNumEdges() ) ;
 		testInstance.restoreState(nonEmptyGraphState) ;
 		assertEquals ( "three Nodes" , nonEmptyGraphState.getNodeStates() , testInstance.getCurrentState().getNodeStates() ) ;
 		assertEquals ( "one Edge" , nonEmptyGraphState.getEdgeStates() , testInstance.getCurrentState().getEdgeStates() ) ;
 	}
 	
 	@Test
 	public final void testRestoreStateFromDeletedSingleEdge () throws Exception
 	{
 		assertEquals ( "three Nodes" , NUMERIC_VALUES[3] , testInstance.getNumNodes() ) ;
 		assertEquals ( "one Edge" , NUMERIC_VALUES[1] , testInstance.getNumEdges() ) ;
 		IGraphState<BaseCompoundNode, BaseCompoundEdge> nonEmptyGraphState = testInstance.getCurrentState() ;
 		assertNotNull ( "state exists" , nonEmptyGraphState) ;
 		assertEquals ( "has Nodes" , NOT_EMPTY_NODE_BITSTRING , nonEmptyGraphState.getNodeStates().toString() ) ;
 		assertEquals ( "has Edge" , NOT_EMPTY_EDGE_BITSTRING , nonEmptyGraphState.getEdgeStates().toString() ) ;
 		SubCompoundGraphFactory subGraphFactory = testInstance.subgraphFactory() ;
 		subGraphFactory.addEdge(edge1) ;
 		SubCompoundGraph subGraph = subGraphFactory.createSubgraph() ;
 		testInstance.removeSubgraph(subGraph) ;
 		assertEquals ( "three nodes" , NUMERIC_VALUES[3] , testInstance.getNumNodes() ) ;
 		assertEquals ( "no edges" , NUMERIC_VALUES[0] , testInstance.getNumEdges() ) ;
 		testInstance.restoreState(nonEmptyGraphState) ;
 		assertEquals ( "three Nodes" , NUMERIC_VALUES[3] , testInstance.getNumNodes() ) ;
 		assertEquals ( "one Edge" , NUMERIC_VALUES[1] , testInstance.getNumEdges() ) ;
 	}
 	
 	@Test
 	public final void testCanMoveBetweenDifferentGraphs () throws Exception
 	{
 		SubCompoundGraphFactory subGraphFactory = testInstance.subgraphFactory() ;
 		subGraphFactory.addNode(node1) ;
 		SubCompoundGraph subGraph = subGraphFactory.createSubgraph() ;
 		assertFalse ("cannot move to other diagram" , this.emptyTestInstance.getRootNode().getChildCompoundGraph().canMoveHere(subGraph)) ;
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public final void testTryToMoveBetweenDifferentGraphs () throws Exception
 	{
 		SubCompoundGraphFactory subGraphFactory = testInstance.subgraphFactory() ;
 		subGraphFactory.addNode(node1) ;
 		SubCompoundGraph subGraph = subGraphFactory.createSubgraph() ;
 		this.emptyTestInstance.getRootNode().getChildCompoundGraph().moveHere(subGraph) ;
 	}
 	
 	@Test
 	public final void testCanCopyBetweenDifferentGraphs () throws Exception
 	{
 		SubCompoundGraphFactory subGraphFactory = testInstance.subgraphFactory() ;
 		subGraphFactory.addNode(node1) ;
 		SubCompoundGraph subGraph = subGraphFactory.createSubgraph() ;
 		assertTrue ("cannot move to other diagram" , this.emptyTestInstance.getRootNode().getChildCompoundGraph().canCopyHere(subGraph)) ;
 	}
 	
 	@Test
 	public final void testTryToCopyBetweenDifferentGraphs () throws Exception
 	{
 		SubCompoundGraphFactory subGraphFactory = testInstance.subgraphFactory() ;
 		subGraphFactory.addNode(node1) ;
 		SubCompoundGraph subGraph = subGraphFactory.createSubgraph() ;
 		this.emptyTestInstance.getRootNode().getChildCompoundGraph().canCopyHere(subGraph) ;
 		assertEquals ( "emptyGraph has one node" , NUMERIC_VALUES[1] , this.emptyTestInstance.getNumNodes() ) ;
 	}
 	
 }
