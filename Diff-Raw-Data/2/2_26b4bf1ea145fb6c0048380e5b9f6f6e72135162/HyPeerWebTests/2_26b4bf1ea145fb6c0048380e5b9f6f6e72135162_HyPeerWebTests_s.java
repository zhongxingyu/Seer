 package hypeerweb;
 
 import java.util.Random;
 
 import junit.framework.TestCase;
 import node.ExpectedResult;
 import node.Node;
 import node.NodeTests;
 import node.SimplifiedNodeDomain;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import database.HyPeerWebDatabase;
 
 /**
  * JUnit Test cases for the HyPeerWeb class
  * @author Jason Robertson
  */
 public class HyPeerWebTests extends TestCase{
 	
 	private HyPeerWeb web = HyPeerWeb.getSingleton();
 	private Node nodes[] = new Node[8];
 	
 	@Before
 	public void setUp() {
 		web = HyPeerWeb.getSingleton();
 		
 		nodes[0] = new Node(0,0);
 		nodes[1] = new Node(1,1);
 		nodes[2] = new Node(2,2);
 		nodes[3] = new Node(3,2);
 		nodes[4] = new Node(4,3);
 		nodes[5] = new Node(5,3);
 		nodes[6] = new Node(6,3);
 		nodes[7] = new Node(7,3);
 		
 		nodes[0].addNeighbor(nodes[1]);
 		nodes[0].addNeighbor(nodes[2]);
 		nodes[0].addNeighbor(nodes[4]);
 		
 		nodes[1].addNeighbor(nodes[0]);
 		nodes[1].addNeighbor(nodes[3]);
 		nodes[1].addNeighbor(nodes[5]);
 		
 		nodes[2].addNeighbor(nodes[0]);
 		nodes[2].addNeighbor(nodes[3]);
 		nodes[2].addNeighbor(nodes[6]);
 		
 		nodes[3].addNeighbor(nodes[1]);
 		nodes[3].addNeighbor(nodes[2]);
 		nodes[3].addNeighbor(nodes[7]);
 		
 		nodes[4].addNeighbor(nodes[0]);
 		nodes[4].addNeighbor(nodes[5]);
 		nodes[4].addNeighbor(nodes[6]);
 		
 		nodes[5].addNeighbor(nodes[1]);
 		nodes[5].addNeighbor(nodes[4]);
 		nodes[5].addNeighbor(nodes[7]);
 		
 		nodes[6].addNeighbor(nodes[4]);
 		nodes[6].addNeighbor(nodes[7]);
 		nodes[6].addNeighbor(nodes[2]);
 		
 		nodes[7].addNeighbor(nodes[3]);
 		nodes[7].addNeighbor(nodes[6]);
 		nodes[7].addNeighbor(nodes[5]);
 	}
 	
 	@After
 	public void tearDown() {
 		web.clear();
 	}
 	
 	@Test
 	public void testGetSingleton(){
 		assertNotNull(HyPeerWeb.getSingleton());
 	}
 	
 	@Test
 	public void testSize(){
 		assertEquals(0, web.size());
 	}
 	
 	@Test
 	public void testAddNode(){
 		web.addNode(nodes[0]);
 		assertEquals(1, web.size());
 	}
 	
 	@Test
 	public void testGetNode(){
 		
 		web.addNode(nodes[0]);
 		web.addNode(nodes[1]);
 		
 		assertTrue(nodes[0].equals(web.getNode(0)));
 		assertTrue(nodes[1].equals(web.getNode(1)));
 	}
 	
 	@Test
 	public void testClear(){
 		
 		assertEquals(0,web.size());
 		
 		web.addNode(nodes[0]);
 		assertEquals(1,web.size());
 		
 		web.addNode(nodes[1]);
 		assertEquals(2,web.size());
 
 		web.addNode(nodes[2]);
 		assertEquals(3,web.size());
 		
 		web.clear();
 		assertEquals(0,web.size());
 	}
 	
 	@Test
 	public void testContains(){
 
 		web.addNode(nodes[0]);
 		assertTrue(web.contains(nodes[0]));
 	}
 	
 	@Test
 	public void testGetHyPeerWebDatabase(){
 		assertNotNull(web.getHyPeerWebDatabase());
 	}
 	
 	@Test
 	public void testReload(){
 		
 		for(Node n : nodes) {
 			web.addNode(n);
 		}
 		
 		assertEquals(nodes.length,web.size());
 		
 		web.saveToDatabase();
 
 		web.clear();
 		assertEquals(0,web.size());
 		
 		web.reload();
 		assertEquals(nodes.length,web.size());
 	}
 	
 	@Test
 	public void testReloadWithName(){
 		
 		final String dbName = "testing.db";
 		HyPeerWebDatabase.initHyPeerWebDatabase(dbName);
 		
 		for(Node n : nodes) {
 			web.addNode(n);
 		}
 		
 		assertEquals(nodes.length,web.size());
 		
 		web.saveToDatabase();
 		
 		web.clear();
 		assertEquals(0,web.size());
 		
 		web.reload(dbName);
 		assertEquals(nodes.length,web.size());
 	}
 	
 	@Test
 	public void testRemoveNode(){
 		assertEquals(0,web.size());
 		
 		web.addNode(nodes[0]);
 		assertEquals(1,web.size());
 		
 		web.removeNode(nodes[0]);
 		assertEquals(0,web.size());
 	}
 	
 	@SuppressWarnings("static-access")
 	@Test
 	public void testSaveToDatabase(){
 		web.addNode(nodes[0]);
 		web.addNode(nodes[1]);
 		web.addNode(nodes[2]);
 		
 		web.saveToDatabase();
 		
 		assertEquals(3, web.getHyPeerWebDatabase().getSingleton().getAllWebIds().size());
 	}
 	
 	@Test
 	public void testAddToHyPeerWeb(){
 		HyPeerWeb web = HyPeerWeb.getSingleton();
 		web.clear();
 		
 		Random generator = new Random();
 		int numberOfNodes = generator.nextInt(300)+231;
 		
 		Node startNode = null;
 		Node newNode = null;
 		for(int i = 0; i < numberOfNodes; i++){
 			newNode = new Node(generator.nextInt(10000));
 			
 			if(i == 0){
 				startNode = Node.NULL_NODE;
 			}
 			else{
 				startNode = web.getNode(generator.nextInt(i));
 			}
 			
 			web.addToHyPeerWeb(newNode, startNode);
 			
 			assertTrue("\nExpected WebId: " + i + "\n"+
 						"Actual: " + web.getNode(i).getWebIdValue(), 
 						web.getNode(i).getWebIdValue() == i);
 			
 			for(int j = 0; j <= i; j++){
 				assertTrue(NodeTests.isNodeDomainCorrect(web.getNode(j), i+1));
 			}
 		}
 	}
 	
 	@Test
 	public void testRemoveFromHyPeerWeb(){
 		HyPeerWeb web = HyPeerWeb.getSingleton();
 		web.clear();
 		
 		final int TEST_SIZE = 32;
 		
 		web.addToHyPeerWeb(nodes[0], Node.NULL_NODE);
 		
 		for(int i = 1; i < TEST_SIZE; i++){
 			web.addToHyPeerWeb(new Node(i), nodes[0]);
 		}
 		
 		int max = TEST_SIZE - 1;
 		
 		while(max > 1){
 			for(int i = max; i > 0; i--){
 				SimplifiedNodeDomain expected = new ExpectedResult(max + 1, i);
 				assertEquals(expected, web.getNode(i).constructSimplifiedNodeDomain());
 			}
 		
			web.removeFromHyPeerWeb(new Node(max));
 			max--;
 		}
 	}
 	
 	@Test
 	public void testAddToHyPeerWebExp(){
 		HyPeerWeb web = HyPeerWeb.getSingleton();
         web.clear();
         
         final int HYPEERWEB_SIZE = 32;
         
         for (int size = 1; size <= HYPEERWEB_SIZE; size++) {
             web.clear();
             Node node0 = new Node(0);
             web.addToHyPeerWeb(node0, null);
             Node firstNode = web.getNode(0);
             SimplifiedNodeDomain simplifiedNodeDomain = firstNode.constructSimplifiedNodeDomain();
             ExpectedResult expectedResult = new ExpectedResult(1, 0);
 
             if (!simplifiedNodeDomain.equals(expectedResult)) {
                 System.out.println("Size: "+size+"\nActual Node: "+simplifiedNodeDomain.toString()+"\n"+
                 					"Expected Node: "+expectedResult.toString());
             }
             
             for (int startNodeId = 0; startNodeId < size - 1; startNodeId++) {
                 web.clear();
                 Node nodeZero = new Node(0);
                 web.addToHyPeerWeb(nodeZero, null);
 
                 for (int i = 1; i < size-1; i++) {
                     Node node = new Node(0);
                     web.addToHyPeerWeb(node, nodeZero);
                 }
                 
                 Node node = new Node(0);
                 Node startNode = web.getNode(startNodeId);
                 web.addToHyPeerWeb(node, startNode);
                 
                 for (int i = 0; i < size; i++) {
                 	
                     Node nodei = web.getNode(i);
                     simplifiedNodeDomain = nodei.constructSimplifiedNodeDomain();
                     expectedResult = new ExpectedResult(size, i);
 
                     if (!simplifiedNodeDomain.equals(expectedResult)) {
                     	System.out.println("Error");
                     }
                 }
             }
         }
 	}
 }
