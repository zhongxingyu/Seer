 package projectrts.model.pathfinding;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import org.junit.Test;
 
 import projectrts.model.world.INode;
 import projectrts.model.world.Node;
 import projectrts.model.world.Position;
 
 public class NodeTest {
 
 	@Test
 	public void testNodePosition() {
 		double arg1 = 3.1;
 		double arg2 = 4.15;
 		Position p = new Position(arg1, arg2);
 		Node node = new Node(p);
 		
 		assertTrue(node.getPosition().equals(p));
 	}
 
 	@Test
 	public void testNodeDoubleDouble() {
 		double arg1 = 3.14;
 		double arg2 = 1.59;
 		Position p = new Position(arg1, arg2);
 		Node node = new Node(arg1, arg2);
 		
 		assertTrue(node.getPosition().equals(p));
 	}
 
 	@Test
 	public void testOccupied() {
 		Node node = new Node(1, 5);
 		node.setOccupied(1);
 		assertTrue(node.isOccupied(2));
 		node.setOccupied(0);
 		assertTrue(!node.isOccupied(2));
 	}
 
 	@Test
 	public void testNeighbours() {
 		Node node1 = new Node(2, 5);
 		Node node2 = new Node(1, 8);
 		Node node3 = new Node(3, 5);
 		List<INode> nodes = node1.getNeighbours();
 		assertTrue(nodes.size() == 0);
 		
 		node1.addNeighbour(node2);
 		node1.addNeighbour(node3);
 		nodes = node1.getNeighbours();
 		assertTrue(nodes.size() == 2);
 		assertTrue(nodes.contains(node2));
 		assertTrue(nodes.contains(node3));
 	}
 
 	@Test
 	public void testEqualsObject() {
 		Node node1 = new Node(2, 5);
 		Node node2 = new Node(1, 8);
 		Node node3 = new Node(2, 5);
 		
 		assertTrue(!node1.equals(node2));
 		assertTrue(!node2.equals(node1));
 		
 		assertTrue(node1.equals(node3));
 		assertTrue(node3.equals(node1));
 		
 		assertTrue(!node2.equals(node3));
 		assertTrue(!node3.equals(node2));
 	}
 
 	@Test
 	public void testCost() {
 		Node node = new Node(1, 5);
 		assertTrue(node.getCost() == 1);
 		node.setCost(3);
 		assertTrue(node.getCost() == 3);
 		node.setCost(0.5f);
 		assertTrue(node.getCost() == 0.5f);
 	}
 	
 	@Test
 	public void testIsAnyNodeOccupied()
 	{
		// TODO Plankton: !!IMPLEMENT testIsAnyNodeOccupied()!
 	}
 }
