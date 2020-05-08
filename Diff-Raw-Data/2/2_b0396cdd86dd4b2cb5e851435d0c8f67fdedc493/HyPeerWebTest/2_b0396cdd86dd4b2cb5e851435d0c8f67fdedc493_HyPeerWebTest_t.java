 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package hypeerweb;
 
 //blah
 import hypeerweb.visitors.SendVisitor;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 import org.junit.After;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import hypeerweb.validator.NodeInterface;
 import hypeerweb.validator.Validator;
 
 /**
  * HyPeerWeb testing
  */
 public class HyPeerWebTest {
 	public static boolean STOP_TESTS = false;
 	//Testing variables
 	private static final int
 		MAX_SIZE = 600,					//Maximum HyPeerWeb size for tests
 		TEST_EVERY = 1,					//How often to validate the HyPeerWeb for add/delete
 		SEND_TESTS = 2000,				//How many times to test send operation
 		BROADCAST_TESTS = 120,			//How many times to test broadcast operation
 		RAND_SEED = 5;					//Seed for getting random nodes (use -1 for a random seed)
 	private static final String DB_NAME = null;
 	private static Segment web;
 	private static String curTest;	
 	
 	public HyPeerWebTest() throws Exception{
 		System.out.println("WARNING!!! These tests are synchronous; do not use them to test Proxy stuff");
 		web = new Segment(DB_NAME, RAND_SEED);
 	}
 	
 	/**
 	 * Populates the HyPeerWeb with some nodes
 	 */
 	public void populate() throws Exception{
 		//Populate the DB with nodes, if needed
 		//Add a bunch of nodes if it validates afterwards, methods should be working
 		if (web.isEmpty()){
 			System.out.println("Populating...");
 			web.removeAllNodes(new SyncListener(){
 				@Override
 				public void callback(Node n){
 					NodeCache temp;
 					int old_size = 0;
 					boolean halt;
 					for (int i=1; i<=MAX_SIZE; i++){
 						final int final_i = i, final_size = ++old_size;
 						web.addNode(new Node(0, 0), new SyncListener(){
 							@Override
 							public void callback(Node n){
 								int size = web.getSegmentSize();
 								if (size != final_size){
 									System.err.println("HyPeerWeb is not the correct size; should be "+final_size+" but found "+size);
 									STOP_TESTS = true;
 								}
 								if (final_i % TEST_EVERY == 0){
									SegmentCache cache = web.getCache();
 									Validator x = new Validator(cache);
 									try{
 										if (!x.validate()){
 											System.out.println("VALIDATION FAILED:");
 											System.out.println(cache);
 											STOP_TESTS = true;
 										}
 									}catch (Exception e){
 										System.out.println("VALIDATION FAILED:");
 										System.out.println(cache);
 										e.printStackTrace();
 										STOP_TESTS = true;
 									}
 								}
 							}
 						});						
 						if (STOP_TESTS) fail();
 					}
 				}
 			});
 			if (STOP_TESTS) fail();
 		}
 	}
 	public void begin(String type) throws Exception{
 		curTest = type;
 		populate();
 		System.out.println("BEGIN:\t"+type);
 	}
 	@After
 	public void end(){
 		if (curTest != null){
 			System.out.println("END:\t"+curTest);
 			curTest = null;
 		}
 	}
 	
 	/**
 	 * Test of addNode method
 	 */
 	@Test
 	public void testAdd() throws Exception {
 		//This is a dummy method for populate()
 		//Don't remove this method
 		begin("ADDING");
 	}
 	
 	/**
 	 * Test of removeNode method (from zero, every time)
 	 */
 	/*
 	@Test
 	public void testRemoveZero() throws Exception {
 		begin("REMOVING ZERO");
 		Node temp;
 		int old_size = getSize();
 		assert(old_size == MAX_SIZE);
 		for (int i=1; i<=MAX_SIZE; i++){
 			web.removeNode(0, RemoveListener);
 			if (getSize() != --old_size)
 				throw new Exception("HyPeerWeb is not the correct size");
 			if (i % TEST_EVERY == 0)
 				assertTrue((new Validator(web.getSegmentNodeCache(0))).validate());
 		}
 	}
 	
 	@Test
 	public void testRemoveRandom() throws Exception {
 		begin("REMOVING RANDOM");
 		SegmentCache.Node temp, rand;
 		int old_size = getSize();
 		for (int i=1; i<=MAX_SIZE; i++){
 			rand = getRandom();
 			web.removeNode(rand.getWebId(), RemoveListener);
 			if (getSize() != --old_size)
 				throw new Exception("HyPeerWeb is not the correct size");
 			if (i % TEST_EVERY == 0)
 				assertTrue((new Validator(web.getSegmentNodeCache(0))).validate());
 		}
 	}
 	
 	@Test
 	public void testSendValid() throws Exception {
 		//Test send node
 		begin("SENDING VALID");
 		SegmentCache.Node f1, f2, found;
 		for (int j=0; j<SEND_TESTS; j++){
 			f1 = getRandom();
 			do{
 				f2 = getRandom();
 			} while (f2 == f1);
 			SendVisitor x = new SendVisitor(f1.getWebId(), SendVisitorListener);
 			//TODO: Figure out how to get a node
 			x.visit(f2);
 			found = x.getFinalNode();
 			if (found == null){
 				System.out.println("f1 = " + f1);
 				System.out.println("f2 = " + f2);
 			}
 			assertNotNull(found);
 			assert(found.getWebId() == f1.getWebId());
 		}
 	}
 	
 	@Test
 	public void testSendInvalid() throws Exception {
 		begin("SENDING INVALID");
 		Random r = new Random();
 		for (int i=0; i<SEND_TESTS; i++){
 			int bad_id = r.nextInt();
 			while (web.getSegmentNodeCache(0).getNode(bad_id) != null)
 				bad_id *= 3;
 			SendVisitor x = new SendVisitor(bad_id, SendVisitorListener);
 			//TODO: Figure out how to get a node
 			x.visit(web.getFirstNode());
 			assertNull(x.getFinalNode());
 		}
 	}
 	
 	@Test
 	public void testBroadcast() throws Exception {
 		begin("TESTING BROADCAST");
 		for (int i=0; i<BROADCAST_TESTS; i++){
 			SegmentCache.Node origin = getRandom();
 			//System.out.println("Starting with:"+origin);
 			ListNodesVisitor x = new ListNodesVisitor(ListNodesVisitorListener);
 			//TODO: Figure out how to get a node
 			x.visit(web.getNode(origin.getWebId(), GetListener));
 			if(x.getNodeList().size() < getSize()) {
 				for(Node n : web.getAllSegmentNodes()) {
 					if(!x.getNodeList().contains(n)){
 						System.out.println("Missing: " + n);
 					}
 				}
 			}
 			assertTrue(x.getNodeList().size() == getSize());
 			for(SegmentCache.Node n : x.getNodeList()) {
 				//TODO: Figure out how to get a node
 				assertTrue(web.getNode(n.getWebId()) != null);
 			}
 			Set<NodeCache.Node> set = new HashSet<>(x.getNodeList());
 			assertTrue(set.size() == x.getNodeList().size());
 		}
 	}
 	*/
 }
