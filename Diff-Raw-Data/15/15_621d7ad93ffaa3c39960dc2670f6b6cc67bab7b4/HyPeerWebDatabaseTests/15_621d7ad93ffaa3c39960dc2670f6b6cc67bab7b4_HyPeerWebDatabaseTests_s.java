 package database;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Iterator;
import java.util.Random;
 
import node.ExpectedResult;
 import node.Node;
 import node.NodeState;
 import node.SimplifiedNodeDomain;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class HyPeerWebDatabaseTests {
 	
 	private static Node node;
 	private static Node neighbor, up, down;
 	private static SimplifiedNodeDomain other;
 	private static HyPeerWebDatabase db;
 	
 	private static final int TEST_HEIGHT = 1, TEST_ID = 1;
 	private static final int NEIGHBOR_HEIGHT = 2, NEIGHBOR_ID = 2;
 	
 	@BeforeClass
 	public static void setupClass(){
 		node = new Node(TEST_ID, TEST_HEIGHT);
 		neighbor = new Node(NEIGHBOR_ID, NEIGHBOR_HEIGHT);
 		down = new Node(NEIGHBOR_ID + 1, NEIGHBOR_HEIGHT + 1);
 		up = new Node(NEIGHBOR_ID + 2, NEIGHBOR_HEIGHT + 2);
 		
 		node.addNeighbor(neighbor);
 		node.addUpPointer(up);
 		node.addDownPointer(down);
 
 		HyPeerWebDatabase.initHyPeerWebDatabase("test_db2.sqlite");
 		db = HyPeerWebDatabase.getSingleton();
 	}
 
 	/**
 	 * Simple case that inserts a node into the database, retrieves it, and checks
 	 * that the retrieved values match what should have been put in.
 	 */
 	@Test
 	public void testAddRetrieve(){
 		db.storeNode(node);
 		other = db.getNode(TEST_ID);
 
 		Iterator<Integer> iter = other.getNeighbors().iterator();
 		assertEquals(neighbor.getWebId().getValue(), iter.next().intValue());
 
 		iter = other.getUpPointers().iterator();
 		assertEquals(up.getWebId().getValue(), iter.next().intValue());
 
 		iter = other.getDownPointers().iterator();
 		assertEquals(down.getWebId().getValue(), iter.next().intValue());
 	}
 
 	@Test
 	public void testStoreAndGet(){
 
 		// Build a node with random data
 		Node node1 = new Node(10);
 
 		node1.setFold(new Node(47));
 		node1.setSurrogateFold(new Node(25));
 		node1.setInverseSurrogateFold(new Node(89));
 		
 		node1.addNeighbor(new Node(747));
 		node1.addNeighbor(new Node(343));
 		
 		node1.addUpPointer(Node.NULL_NODE);
 		node1.addDownPointer(new Node(17));
 
 		// Store the node
 		db.storeNode(node1);
 
 		// Retrieve the node
 		SimplifiedNodeDomain result = db.getNode(node1.getWebIdValue());
 
 		// Expect to get this node back
 		SimplifiedNodeDomain confirm = node1.constructSimplifiedNodeDomain();
 
 //		System.out.println("from db: "+ result);
 //		System.out.println("from node: "+ confirm);
 
 		assertTrue(result.equals(confirm));
 
 		// Change some information
 		node1.setFold(new Node(3090));
 		node1.setSurrogateFold(Node.NULL_NODE);
 		node1.setState(NodeState.CAP);
 
 		// Store the node again...it should update the node already in the database
 		db.storeNode(node1);
 
 		// Confirm changes took place
 		result = db.getNode(node1.getWebIdValue());
 		confirm = node1.constructSimplifiedNodeDomain();
 		assertTrue(result.equals(confirm));
 
 	}
 }
