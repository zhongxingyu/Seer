 package factory.test.mock;
 
 import factory.Part;
 import factory.interfaces.Feeder;
 import factory.interfaces.Gantry;
 import factory.interfaces.Lane;
 import factory.interfaces.Nest;
 
 public class MockFeeder extends MockAgent implements Feeder {
 	
 	public MockFeeder(String name) {
 		super(name);
 		// TODO Auto-generated constructor stub
 	}
 
 	public EventLog log = new EventLog();
 
 	@Override
 	public void msgHereAreParts(Part pt) {
 		// TODO Auto-generated method stub
 		log.add(new LoggedEvent(
 				"Received message msgHereAreParts from the Gantry with part " + pt.name));
 	}
 
 	@Override
 	public void msgEmptyNest(Nest n) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void msgNestWasDumped(Lane la) {
 		log.add(new LoggedEvent("msgNestWasDumped()"));
 	}
 
 	@Override
 	public void msgLaneNeedsPart(Part part, Lane lane) {
 		log.add(new LoggedEvent("msgLaneNeedsPart()"));
 	}
 
 	@Override
 	public void msgBadNest(Nest n) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setGantry(Gantry g) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean getFeederHasABinUnderneath() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public int getFeederNumber() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void msgNestHasStabilized(Lane lane) {
 		log.add(new LoggedEvent("msgNestHasStabilized()"));
 	}
 
 	@Override
 	public void msgNestHasDeStabilized(Lane lane) {
 		log.add(new LoggedEvent("msgNestHasDestabilized()"));
 	}
 
	@Override
 	public void msgPartRobotHasRemovedPartsFromLane(int numberOfParts, Lane lane) {
 		// TODO Auto-generated method stub
 		
 	}
 
	@Override
 	public void msgLaneIsOutOfParts(Lane lane) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
