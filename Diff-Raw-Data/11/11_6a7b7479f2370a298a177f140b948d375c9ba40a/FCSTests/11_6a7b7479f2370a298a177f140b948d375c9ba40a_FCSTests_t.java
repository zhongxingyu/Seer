 package factory.test;
 
 import static org.junit.Assert.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 
 
 import org.junit.Test;
 
 import factory.Bin;
 import factory.FCSAgent;
 import factory.KitConfig;
 import factory.Part;
 import factory.interfaces.PartsRobot;
 import factory.test.mock.*;
 import factory.BinConfig;
 import factory.FCSAgent.KitProductionState;;
 
 
 
 public class FCSTests extends TestCase{
 
 //	MockGantry mockGantry;
 //	MockPartsRobotAgent partsRobot;
 //	FCSAgent FCSagent;
 //	BinConfig binConfig;
 //	boolean passBinConfigurationToGantry;
 	
 	/**tests the msgInitialize().  The initialize msg should send the bin config information to a gantry agent **/
 	public void testMsgInitialize(){
 		
 		HashMap<Bin, Integer> binList = new HashMap<Bin, Integer>();
 		BinConfig binConfig = new BinConfig(binList);
 		MockGantry gantry = new MockGantry("gantry");
 		MockPartsRobot partsRobot = new MockPartsRobot("parts robot");
 		FCSAgent fcs = new FCSAgent(gantry, partsRobot, null);
 		
 		//create message for FCS.  Should eventually call gantry.msgChangeGantryBinConfig(this.binConfig);
 		fcs.msgInitialize(binConfig);
 		
 		//passBinConfigurationToGantry should be true
 		assertTrue("passBinConfigurationToGantry should be true after calling msgInitialize.  Instead, it is: "
 				+ fcs.passBinConfigurationToGantry, fcs.passBinConfigurationToGantry);
 		
 		//test to see that the gantry robot has not been given a message before the scheduler is called.
 		//Its log should be empty.
 		assertEquals(
 				"Mock Gantry should have an empty event log before the FCSAgent scheduler is called. Instead, the mock gantry event log reads: "
 						+ gantry.log.toString(), 0, gantry.log.size());
 		
 		//call the fcs scheduler
 		fcs.pickAndExecuteAnAction();
 		
 		//tests to see if the gantry log got the message
 		assertTrue("Gantry should have gotten a message to change bin config.", gantry.log
 				.containsString("msgChangeGantryBinConfig"));
 		
 		//passBinConfigurationToGantry should be false after calling the scheduler. 
 		assertFalse("passBinConfigurationToGantry should be false after calling the scheduler.  Instead, it is: "
 				+ fcs.passBinConfigurationToGantry, fcs.passBinConfigurationToGantry);
 		
 		//only one message should be sent to the gantry
 		assertEquals(
 				"Only 1 message should have been sent to the gantry. Event log: "
 						+ gantry.log.toString(), 1, gantry.log.size());
 		
 	}
 	
 	public void testMsgProduceKit()
 	{
 		
 		KitConfig kitConfig = new KitConfig();
 		
 		MockGantry gantry = new MockGantry("gantry");
 		MockPartsRobot partsRobot = new MockPartsRobot("parts robot");
 		FCSAgent fcs = new FCSAgent(gantry, partsRobot, null);
 		fcs.kitRecipes.put("typeA", kitConfig);
 //		fcs.partsList = createPartsList();
 		
 		//create message for FCS.  Should eventually call this.partsRobot.msgMakeKit(mkc.kitConfig);
 		fcs.msgProduceKit(5, "typeA");
 		
 		//test to see that the parts robot has not been given a message before the scheduler is called.
 		//Its log should be empty.
 		assertEquals(
 				"Mock parts robot should have an empty event log before the FCSAgent scheduler is called. Instead, the mock parts robot event log reads: "
 						+ partsRobot.log.toString(), 0, partsRobot.log.size());
 		
 		//test to see if parts robot added a kitConfig to its list.  The list shouldn't be empty after msgProduceKit()
 		assertFalse("Parts robot should have a kitConfig to its list of kit configs, but it is empty", 
 					fcs.orders.isEmpty());
 		
 		//call the fcs scheduler
 		fcs.pickAndExecuteAnAction();
 		
 		//tests to see if the parts robot log got the message from the FCS
 		assertTrue("Parts Robot should have gotten a message to make a kit from the FCS.", partsRobot.log
 				.containsString("msgMakeKit"));
 		
 		//tests to see if the kit config state changed from PENDING to PRODUCING
 //		assertTrue("Parts Robot should have gotten a message to make a kit.", );
 	}
 	
 	public void testImports(){
 		FCSAgent fcs = new FCSAgent(null);
 	}
 	
 	public void testAddingAndFinishingKitConfig(){
 		
 		//this test tests the fcs robot handling incoming orders.  It takes in 2 initial orders, and simulates
 		//the factory producing a kit.
 		KitProductionState stateFinished = KitProductionState.FINISHED;
 		KitProductionState statePending = KitProductionState.PENDING;
 		KitProductionState stateProducing = KitProductionState.PRODUCING;
 		KitConfig kit1 = new KitConfig();
 		KitConfig kit2 = new KitConfig();
 		FCSAgent fcs = new FCSAgent(null);
 		
 		fcs.kitRecipes.clear();
 		
 		fcs.kitRecipes.put("typeA", kit1);
 		fcs.kitRecipes.put("typeB", kit2);
		fcs.partsRobot = new MockPartsRobot("name");
 		
 		//test to see if fcs has 2 recipe types.
 		assertEquals("FCS should have 2 recipes of kits, but instead has: " + fcs.kitRecipes.size()
 				, 2, fcs.kitRecipes.size());
 		
 		fcs.msgProduceKit(2, "typeA");
 		fcs.msgProduceKit(2, "typeB");
 		
 		
 		//does the FCS has 2 orders pending?
 		assertEquals("FCS should have 2 orders in its list, but instead has: " + fcs.orders.size()
 				, 2, fcs.orders.size());
 		
 		//is the number of kits exported 0?
 		assertEquals("The kits exported should be 0, but instead is: " + fcs.kitsExportedCount
 				, 0, fcs.kitsExportedCount);
 		
 		
 		//the state of the FCS should be PENDING
 		assertEquals("The FCS state should be PENDING, but instead is: " + fcs.state
 				, statePending, fcs.state);
 		
 		//the first order in the list should be for typeA
 		assertEquals("The first order in the order list should be for kitConfig typeA, but instead is: " 
 				+ fcs.orders.peek().kitName, "typeA", fcs.orders.peek().kitName);
 		
 		//tell fcs to change states
 		fcs.pickAndExecuteAnAction();
 		
 		//export a kit
 		fcs.msgKitIsExported(null);
 		
 		//the state of the FCS should be PROUDUCING 
 		assertEquals("The FCS state should be PRODUCING, but instead is: " + fcs.state
 				, stateProducing, fcs.state);
 		
 		//is the number of kits exported 1?
 		assertEquals("The kits exported should be 1, but instead is: " + fcs.kitsExportedCount
 				, 1, fcs.kitsExportedCount);
 		
 		//the first order in the list should still be for typeA
 		assertEquals("The first order in the order list should be for kitConfig typeA, but instead is: " 
 				+ fcs.orders.peek().kitName, "typeA", fcs.orders.peek().kitName);
 		
 		//export a kit
 		fcs.msgKitIsExported(null);
 		
 		//is the number of kits exported 2?
 		assertEquals("The kits exported should be 2, but instead is: " + fcs.kitsExportedCount
 				, 2, fcs.kitsExportedCount);
 		
 		assertEquals("The FCS state should be FINISHED, but instead is: " + fcs.state
 				, stateFinished, fcs.state);
 		
 		//the first order in the list should still be for typeA
 		assertEquals("The first order in the order list should be for kitConfig typeA, but instead is: " 
 				+ fcs.orders.peek().kitName, "typeA", fcs.orders.peek().kitName);
 		
 		fcs.pickAndExecuteAnAction();
 		
 		//check the state of the fcs
 		assertEquals("The FCS state should be PENDING, but instead is: " + fcs.state
 				, statePending, fcs.state);
 		
 		//the first order in the list should now be for typeB
 		assertEquals("The first order in the order list should be for kitConfig typeB, but instead is: " 
 				+ fcs.orders.peek().kitName, "typeB", fcs.orders.peek().kitName);
 		
 		//FCS should have removed an order
 		assertEquals("The FCS orders list should have 1 order, but instead has: " + fcs.orders.size()
 				, 1, fcs.orders.size());
 		
 		fcs.pickAndExecuteAnAction();
 		
 		//check the FCS state (should be PRODUCING)
 		assertEquals("The FCS state should be PRODUCING, but instead is: " + fcs.state
 				, stateProducing, fcs.state);
 		
 		//is the number of kits exported 0?
 		assertEquals("The kits exported should be 0, but instead is: " + fcs.kitsExportedCount
 				, 0, fcs.kitsExportedCount);
 		
 		//the first order in the list should still be for typeB
 		assertEquals("The first order in the order list should be for kitConfig typeB, but instead is: " 
 				+ fcs.orders.peek().kitName, "typeB", fcs.orders.peek().kitName);
 		
 		fcs.msgKitIsExported(null);
 		
 		//check the FCS state (should be PRODUCING)
 		assertEquals("The FCS state should be PRODUCING, but instead is: " + fcs.state
 				, stateProducing, fcs.state);
 		
 		//is the number of kits exported 1?
 		assertEquals("The kits exported should be 1, but instead is: " + fcs.kitsExportedCount
 				, 1, fcs.kitsExportedCount);
 		
 		//the first order in the list should still be for typeB
 		assertEquals("The first order in the order list should be for kitConfig typeB, but instead is: " 
 				+ fcs.orders.peek().kitName, "typeB", fcs.orders.peek().kitName);
 		
 		fcs.msgKitIsExported(null);
 		
 		//is the number of kits exported 2?
 		assertEquals("The kits exported should be 2, but instead is: " + fcs.kitsExportedCount
 				, 2, fcs.kitsExportedCount);
 		
 		//the first order in the list should still be for typeB
 		assertEquals("The first order in the order list should be for kitConfig typeB, but instead is: " 
 				+ fcs.orders.peek().kitName, "typeB", fcs.orders.peek().kitName);
 		
 		//check the FCS state (should be PRODUCING)
 		assertEquals("The FCS state should be FINISHED, but instead is: " + fcs.state
 				, stateFinished, fcs.state);
 		
 		fcs.pickAndExecuteAnAction();
 		
 		//check the FCS state (should be PENDING)
 		assertEquals("The FCS state should be PENDING, but instead is: " + fcs.state
 				, statePending, fcs.state);
 		
 		//the FCS should have removed its last order because it finished it
 		assertEquals("The orders list should have no orders in it, but instead, has: " + fcs.orders.size()
 				, 0, fcs.orders.size());
 		
 		fcs.pickAndExecuteAnAction();
 		
 		//check the FCS state (should be PENDING)
 		assertEquals("The FCS state should be PENDING because there are no orders left, but instead is: " + fcs.state
 				, statePending, fcs.state);
 	}
 	
 	public Map<String, Part> createPartsList(){
 		 Map<String, Part> partsList = new HashMap<String, Part>();
 		 Part p1 = new Part("nose", 1, "nose of potato", "jpg", 5);
 		 Part p2 = new Part("ears", 1, "ears of potato", "jpg", 5);
 		 Part p3 = new Part("arm", 1, "arm of potato", "jpg", 5);
 		 Part p4 = new Part("hat", 1, "hat of potato", "jpg", 5);
 		 Part p5 = new Part("leg", 1, "leg of potato", "jpg", 5);
 		 Part p6 = new Part("mouth", 1, "mouth of potato", "jpg", 5);
 		 partsList.put(p1.name, p1);
 		 partsList.put(p2.name, p2);
 		 partsList.put(p3.name, p3);
 		 partsList.put(p4.name, p4);
 		 partsList.put(p5.name, p5);
 		 partsList.put(p6.name, p6);
 		 
 		 return partsList;
 		
 	}
 
 	
 	
 	
 	
 }
