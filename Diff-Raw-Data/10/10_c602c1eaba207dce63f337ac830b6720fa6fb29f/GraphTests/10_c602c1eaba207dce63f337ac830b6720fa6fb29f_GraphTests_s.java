 import org.junit.Before;
 import org.junit.Test;
 import org.operationsproject.operations.graph.CycleException;
 import org.operationsproject.operations.graph.Graph;
 import org.operationsproject.operations.graph.Node;
 
 import java.util.Set;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.operationsproject.operations.graph.Graph.EdgeDirection.LINKED_TO_BY;
 import static org.operationsproject.operations.graph.Graph.EdgeDirection.LINKS_TO;
 
 /**
  * @author Matthew Johnstone
  *         Date: 31/05/13
  *         Time: 20:36
  */
 public class GraphTests {
 
     private Graph graph;
 
     @Before
     public void setUp() {
         graph = new Graph();
     }
 
     /**
      * Add a single node, and check that it is returned.
      */
     @Test
     public void should_return_sole_added_node() {
 
         Node node = createNode();
         graph.addNode(node);
 
         Set<Node> allNodes = graph.getAllNodes();
 
         assertEquals(1, allNodes.size());
         assertEquals(node, allNodes.iterator().next());
     }
 
     /**
      * Add two nodes, unconnected, and check that they are both returned.
      */
     @Test
     public void should_return_both_added_nodes() {
 
         Node node1 = createNode();
         Node node2 = createNode();
 
         graph.addNode(node1);
         graph.addNode(node2);
         Set<Node> allNodes = graph.getAllNodes();
 
         assertEquals(2, allNodes.size());
         assertTrue(allNodes.contains(node1));
         assertTrue(allNodes.contains(node2));
     }
 
     /**
      * Add two nodes, connect them, and check that the first refers to the second, but not the other way round
      */
     @Test
    public void should_be_able_to_navigate_between_nodes() throws CycleException {
 
         Node node1 = createNode();
         Node node2 = createNode();
 
         graph.addNodes(node1, node2);
         graph.linkNodes(node1, node2);
 
         Set<Node> nodesLinkingToNode1 = graph.getLinkedNodes(LINKS_TO, node1);
         assertEquals(0, nodesLinkingToNode1.size());
 
         Set<Node> nodesLinkedToByNode1 = graph.getLinkedNodes(LINKED_TO_BY, node1);
         assertEquals(1, nodesLinkedToByNode1.size());
         assertEquals(node2, nodesLinkedToByNode1.iterator().next());
 
         Set<Node> nodesLinkingToNode2 = graph.getLinkedNodes(LINKS_TO, node2);
         assertEquals(1, nodesLinkingToNode2.size());
         assertEquals(node1, nodesLinkingToNode2.iterator().next());
 
         Set<Node> nodesLinkedToByNode2 = graph.getLinkedNodes(LINKED_TO_BY, node2);
         assertEquals(0, nodesLinkedToByNode2.size());
     }
 
     /**
      * Add two nodes to a graph, and attempt to link them together, forming a cycle
      */
     @Test(expected = CycleException.class)
    public void should_prevent_cycle_between_two_nodes_from_being_allowed() throws CycleException {
         Node node1 = createNode();
         Node node2 = createNode();
 
         graph.addNodes(node1, node2);
 
         graph.linkNodes(node1, node2);
         graph.linkNodes(node2, node1);
     }
 
     /**
      * Add two nodes to a graph, and link them together twice, asserting that this is indicated by linkNodes and that
      * the link does not count twice.
      */
     @Test
    public void should_indicate_that_link_already_existed() throws CycleException {
         Node node1 = createNode();
         Node node2 = createNode();
 
         graph.addNodes(node1, node2);
 
         assertFalse(graph.linkNodes(node1, node2));
         assertTrue(graph.linkNodes(node1, node2));
 
         Set<Node> linkedNodes = graph.getLinkedNodes(LINKED_TO_BY, node1);
         assertEquals(1, linkedNodes.size());
     }
 
     // TODO Tests for lots more complicated cycle detection
 
     private Node createNode() {
         return new Node();
     }
 
 }
