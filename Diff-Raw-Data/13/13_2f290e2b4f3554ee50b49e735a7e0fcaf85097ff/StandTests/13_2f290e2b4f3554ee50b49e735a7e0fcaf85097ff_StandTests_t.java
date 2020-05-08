 package factory.test;
 /*
 import factory.StandAgent;

public class StandTests extends TestCase {
 
 	StandAgent stand;
 	MockVisionAgent visionMock;
 	MockKitRobotAgent kitRobotMock;
 	MockPartsRobotAgent partsRobotMock;
 	MockConveyor conveyorMock;
 	
 	@Before
 	public void setUp() throws Exception {
 		stand = new StandAgent(conveyorMock, visionMock, kitRobotMock, partsRobotMock);
 	}
 
 	@Test
 	public void testStandAgent() {
 		// Test that the conveyor was saved in MyConveyor
 		assertEquals(stand.conveyor.conveyor,conveyorMock);
 		// Test that the conveyor state is empty
 		assertEquals(stand.conveyor.state,MyConveyorState.EMPTY);
 		
 		// Test that vision was saved
 		assertEquals(stand.vision,visionMock);
 		// Test that kitRobot was saved
 		assertEquals(stand.kitRobot,kitRobotMock);
 		// Test that partsRobot was saved
 		assertEquals(stand.partsRobot,partsRobotMock);
 		
 
 		// Test that the partsRobot does not want to deliver parts
 		assertEquals(stand.partsRobotWantsToDeliverParts,false);
 		
 
 		// Test that the state is FREE
 		assertEquals(stand.state, StandAgentState.FREE);
 	
 
 		// Test that the topSlot was saved correctly with an empty kit and state to empty
 		assertEquals(stand.topSlot.name, "topSlot");
 		assertEquals(stand.topSlot.kit, null);
 		assertEquals(stand.topSlot.state,MySlotState.EMPTY);
 
 		// Test that the bottomSlot was saved correctly with an empty kit and state to empty
 		assertEquals(stand.bottomSlot.name, "bottomSlot");
 		assertEquals(stand.bottomSlot.kit, null);
 		assertEquals(stand.bottomSlot.state,MySlotState.EMPTY);
 
 		// Test that the inspectionSlot was saved correctly with an empty kit and state to empty
 		assertEquals(stand.inspectionSlot.name, "inspectionSlot");
 		assertEquals(stand.inspectionSlot.kit, null);
 		assertEquals(stand.inspectionSlot.state,MySlotState.EMPTY);
 		
 		// Test that nothing gets executed if pickAndExecuteAnAction gets called
 		assertEquals(stand.pickAndExecuteAnAction() ,false);
 	}
 
 	@Test
 	public void testMsgEmptyKitIsHereWithTopSlotFree() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testMsgEmptyKitIsHereWithBottomSlotFree() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testMsgEmptyKitIsHereWithNoSlotFree() {
 		fail("Not yet implemented");
 	}
 	
 
 	@Test
 	public void testMsgHereIsAnEmptyKit() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testMsgKitRobotNoLongerUsingStand() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testMsgPartRobotWantsToPlaceParts() {
 		fail("Not yet implemented");
 	}
 	
 	@Test
 	public void testMsgPartRobotWantsToPlacePartsWithEmptySlots() {
 
 		// Test that nothing gets executed if the Parts Robot wants to delivery parts but the slots are empty
 		stand.msgPartRobotWantsToPlaceParts();
 		assertEquals(stand.pickAndExecuteAnAction() , false);
 	}
 
 	@Test
 	public void testMsgPartsRobotNoLongerUsingStand() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testMsgResultsOfKitAtInspection() {
 		fail("Not yet implemented");
 	}
 
 }*/
