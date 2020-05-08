 package agents.test;
 
 import agents.ConveyorAgent;
 import agents.Kit;
 import agents.test.mock.*;
 
 import org.junit.Test;
 import java.util.*;
 import junit.framework.TestCase;
 
 public class ConveyorAgentTest_1Kit extends TestCase {
 	
 	public ConveyorAgent conveyor;
 
 	/**
 	 * This is a test to receive 1 kit and request the kitrobot to grab it"
 	 */
 	
 	@Test
 	public void testMsgHeresKit(){
 		
 		//create components involved in test
 		MockFCS fcs = new MockFCS("FCS");
 		MockKitRobot kitRobot = new MockKitRobot("KitRobot");
 		conveyor = new ConveyorAgent("Conveyor", kitRobot, fcs);
 		
 		Map<String,Integer> list = new HashMap<String,Integer>();
 		
 		list.put("BlueFuckingWhale",1);
 		list.put("FuckingShamu", 2);
 		list.put("MobyDick", 1);
 		list.put("Shrimp", 50);
 		
 		//creates 1 kit
 		Kit emptyKit = new Kit(list, 1);
 		
 		conveyor.msgHeresKit(emptyKit);
 		
 		//checks not messaging the kitrobot in conveyor's message reception
 		assertEquals(
 				"Mock KitRobot should have an empty event log before the " +
 				"conveyor's scheduler is called.  Instead, the mock KitRobot's event log " +
 				"reads: " + kitRobot.log.toString(), 0, kitRobot.log.size());
 		
 		//call scheduler
 		conveyor.pickAndExecuteAnAction();
 		
 		assertTrue(
 				"Mock KitRobot should have received msg request to move parts. "
 				+ kitRobot.log.toString(), kitRobot.log.containsString(
 						"Received msgRequestMoveKit"));
 		
 		assertEquals(
 				"Only 1 message should have been sent to the KitRobot. Event log: "
 				+ kitRobot.log.toString(), 1, kitRobot.log.size());
 		
 		/*
 		assertTrue("Message should be sent from " + conveyor.toString(),
 				kitRobot.log.getLastLoggedEvent().getMessage().contains(
 						conveyor.toString()));
 		*/
 		
 	}
 	
 	/** 
 	 * This tests that the conveyor provide kits to the kitRobot when
 	 * the kitRobot requests it.
 	 */
 	
 	@Test
 	public void testMsgGiveMeKits(){
 		
 		//create components involved in test
 		MockFCS fcs = new MockFCS("FCS");
 		MockKitRobot kitRobot = new MockKitRobot("KitRobot");
 		conveyor = new ConveyorAgent("Conveyor", kitRobot, fcs);
 		
 		Map<String,Integer> list = new HashMap<String,Integer>();
 		
 		list.put("BlueFuckingWhale",1);
 		list.put("FuckingShamu", 2);
 		list.put("MobyDick", 1);
 		list.put("Shrimp", 50);
 		
 		//creates 1 kit
 		Kit emptyKit = new Kit(list, 1);
 		
 		//kitRobot requests 1 kit
 		conveyor.msgGiveMeKits(1);
 		
 		assertEquals(
 				"Mock KitRobot should have an empty event log before the " +
 				"conveyor's scheduler is called.  Instead, the mock KitRobot's event log " +
 				"reads: " + kitRobot.log.toString(), 0, kitRobot.log.size());
 		
 		conveyor.pickAndExecuteAnAction();
 		
 		assertTrue(
 				"Mock KitRobot should have received msg providing parts. "
 				+ kitRobot.log.toString(), kitRobot.log.containsString(
 						"Received msgHeresKits"));
 		
 		assertEquals(
 				"Only 1 message should have been sent to the KitRobot. Event log: "
 				+ kitRobot.log.toString(), 1, kitRobot.log.size());
 		
 	}
 
 	/** 
 	 * This tests that the conveyor receives a complete kit from the kitRobot
 	 */
 	
 	@Test
 	public void testMsgHereIsCompleteKit(){
 		
 		//create components involved in test
 		MockFCS fcs = new MockFCS("FCS");
 		MockKitRobot kitRobot = new MockKitRobot("KitRobot");
 		conveyor = new ConveyorAgent("Conveyor", kitRobot, fcs);
 		
		assertEquals(
 				"Mock Conveyor completedKits list should be empty.  " +
 				"Instead, the size is: ", 0,
				conveyor.completedKits.size());
 		
 		//create kit to give conveyor
 		Map<String,Integer> list = new HashMap<String,Integer>();
 		
 		list.put("BlueFuckingWhale",1);
 		list.put("FuckingShamu", 2);
 		list.put("MobyDick", 1);
 		list.put("Shrimp", 50);
 		
 		Kit completeKit = new Kit(list, 1);
 		//end create kit
 		
 		conveyor.msgHereIsCompleteKit(completeKit);
 		
		assertEquals(
 				"Mock Conveyor should contain 1 complete kit in completedKits list.  " +
 				"Instead, the size is: ", 1,
				conveyor.completedKits.size());
 		
 	}
 }
