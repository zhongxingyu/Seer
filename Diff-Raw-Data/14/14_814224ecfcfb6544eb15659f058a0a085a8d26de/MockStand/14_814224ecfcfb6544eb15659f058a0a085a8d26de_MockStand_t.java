 package factory.test.mock;
 
 import factory.Kit;
 import factory.Kit.KitState;
 import factory.StandAgent.MySlotState;
 import factory.interfaces.Stand;
 
 public class MockStand extends MockAgent implements Stand {
 
 	public EventLog log = new EventLog();
 
 	public MockStand(String name) {
 		super(name);
 	}
 	
 	@Override
 	public void msgResultsOfKitAtInspection(KitState results) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void msgEmptyKitIsHereAndWantToDeliver() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void msgKitRobotNoLongerUsingStand() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void msgPartRobotWantsToPlaceParts() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void msgKitRobotWantsToExportKit() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean setSlotKit(String slot, Kit k) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public Kit getSlotKit(String slot) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean setSlotState(String slot, MySlotState state) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public MySlotState getSlotState(String slot) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void msgClearStand() {
 		log.add(new LoggedEvent("msgClearStand() received from the PartsRobot")); 
 	}
 
	@Override
	public void msgPartsRobotNoLongerUsingStand() {
		// TODO Auto-generated method stub
		
	}

 }
