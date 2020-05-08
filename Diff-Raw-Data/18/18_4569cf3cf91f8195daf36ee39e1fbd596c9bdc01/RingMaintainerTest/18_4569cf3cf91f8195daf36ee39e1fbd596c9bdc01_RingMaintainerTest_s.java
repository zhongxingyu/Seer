 package decentchat.internal.nodes;
 
 import java.rmi.RemoteException;
 import java.util.List;
 
 import decentchat.internal.NodeKey;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 
 public class RingMaintainerTest {
 	
 	private NodeImpl[] nodes;
 	private RingMaintainer[] maintainers;
 	
 	private NodeImpl setupNode(int key, NodeImpl seed) throws RemoteException {
 		return new NodeImpl(NodeKey.MIN_KEY.inc(key), seed);
 	}
 	
 	private NodeImpl setupNode(int key) throws RemoteException {
 		return new NodeImpl(NodeKey.MIN_KEY.inc(key));
 	}
 	
 	@Before
 	public void setup() throws RemoteException {
 		int[] node_ids = {1, 8, 14, 21, 32, 38, 42, 48, 51, 56};
 		nodes = new NodeImpl[node_ids.length];
 		nodes[0] = setupNode(node_ids[0]);
 		for (int i = 1; i < node_ids.length; ++i) {
 			nodes[i] = setupNode(node_ids[i], nodes[0]);
 		}
 		maintainers = new RingMaintainer[nodes.length];
 		for (int i = 0; i < nodes.length; ++i) {
 			maintainers[i] = new RingMaintainer(nodes[i]);
 		}
 	}
 	
 	@Test
 	public void testStabilize() {
 		stabilizeNodes();		
 		for (int i = 1; i < nodes.length; ++i) {
 			assertEquals("Successor of " + nodes[i-1] + "is wrong", nodes[i], nodes[i-1].getSuccessor());
 		}
 	}
 
 	private void stabilizeNodes() {
 		for (int i = 0; i < 10; ++i) {
 			for (int k = 0; k < nodes.length; ++k) {
 				maintainers[k].stabilize();
 			}
 		}
 	}
 	
 	@Test
 	public void testFixFingers() throws RemoteException {
 		stabilizeNodes();
 		for (int i = 0; i < 6; ++i) {
 			maintainers[1].fixFingers();
 		}
 		byte[] expected_fingers = {14,14,14,21,32,42};
 		List<Node> fingers = nodes[1].getFingers();
 		assertEquals(6, fingers.size());
 		for (int i = 0; i < 6; ++i) {
 			assertEquals(expected_fingers[i], fingers.get(i).getKey().getHash()[0]);
 		}
 	}
 
 }
