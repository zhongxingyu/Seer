 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package hypeerweb;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 import validator.Validator;
 
 /**
  * HyPeerWeb testing
  */
 public class HyPeerWebTest {
 	//Validation variables
 	private final int MAX_TESTS = 114;//use <=100 if testing database
 	private final int TEST_EVERY = 1;
 	private final int GRAPH_LEVELS = 3;
 	private final boolean TEST_DATABASE = false;
 	private final boolean USE_TRACE_LOG = false;
 	private final boolean DRAW_GRAPH = false;
 	private HyPeerWeb web;
 	private DrawingThread draw;
 	
 	public HyPeerWebTest() throws Exception{
 		web = HyPeerWeb.getInstance();
 		if (!TEST_DATABASE)
 			web.disableDatabase();
 		if (USE_TRACE_LOG){
 			//*
 			if (!web.loadTrace()){
 				System.out.println("Could not load insertion trace from log file!!!");
 				System.out.println("Try again or disable USE_TRACE_LOG");
 				fail();
 			}
 			//*/
 		}
 		else web.startTrace();
 		//Drawing a HyPeerWeb Graph
 		if(DRAW_GRAPH)
 		    draw = new DrawingThread(this);
 	}
 	
 	/**
 	 * Test of addNode method, of class HyPeerWeb.
 	 */
 	@Test
 	public void testAddNode() throws Exception {
 		int t = 0, i = 1;
 		try{
 			if (TEST_DATABASE){
 				//I put the testHyPeerWeb code here because it was always running after testAddNode and so wasn't testing anything.
 				System.out.println("Testing restore");
 				assertTrue((new Validator(web)).validate());//comment out this line to get new HyPeerWeb
 				System.out.println("Done testing restore");
 			}
 
 			//Add a bunch of nodes; if it validates afterwards, addNode should be working
 			//We cannot do simulated tests, since addNode inserts at arbitrary places
 			web.removeAllNodes();
 			boolean valid;
 			int old_size = 0;
 			Node temp;
 			for (; t<2; t++){
 				System.out.println("BEGIN "+(t == 0 ? "ADDING" : "DELETING")+" NODES");
 				for (i=1; i<=MAX_TESTS; i++){
 					//Add nodes first time around
 					if (t == 0){
 						if ((temp = web.addNode()) == null)
 							throw new Exception("Added node should not be null!");
 						if (web.getSize() != ++old_size)
 							throw new Exception("HyPeerWeb is not the correct size");
 						if (temp.getWebId() == 254)
 							drawGraph(temp);
 						System.out.println("ADDED = "+temp.getWebId());
 					}
 					//Then delete all nodes
 					else{
 						if ((temp = web.removeNode(web.getFirstNode())) == null)
 							throw new Exception("Removed node should not be null!");
 						if (web.getSize() != --old_size)
 							throw new Exception("HyPeerWeb is not the correct size");
 					}
 					if (i % TEST_EVERY == 0){
 						valid = (new Validator(web)).validate();
 						assertTrue(valid);
 					}
 				}
 				//After insertion graph
 				System.out.println("DONE "+(t == 0 ? "ADDING" : "DELETING")+" NODES");
 				if(DRAW_GRAPH)
				    drawGraph(web.getFirstNode());
 			}
 		} catch (Exception e){
 			System.out.println("Fatal Error from HyPeerWeb:");
 			System.out.println(e);
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 			fail();
 		} finally{
 			if (!web.endTrace())
 				System.out.println("WARNING!!! Could not save the insertion trace to log file");
 			System.out.println("ADDED "+(t > 0 ? MAX_TESTS : i)+" NODES");
 			System.out.println("DELETED "+(t == 1 ? i : t == 2 ? MAX_TESTS : 0)+" NODES");
 		}
 	}
 	
 	public void drawGraph(Node n) throws Exception{
 		if (n == null) return;
 		draw.start(n, GRAPH_LEVELS);
 		synchronized (this){
 			this.wait();
 		}
 	}
 }
