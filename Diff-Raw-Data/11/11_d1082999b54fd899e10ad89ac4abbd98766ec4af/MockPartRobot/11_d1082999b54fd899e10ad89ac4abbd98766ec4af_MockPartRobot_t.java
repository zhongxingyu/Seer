 package agents.test.mock;
 import java.util.List;
 import java.util.Map;
 
 import agents.NestAgent;
 import agents.Part;
 import agents.interfaces.*;
 
 public class MockPartRobot extends MockAgent implements PartRobot{
 
 	public EventLog log = new EventLog();
 	
 	public MockPartRobot(String name) {
 		super(name);
 	}
 
 	public void msgMakeKits() {
 		log.add(new LoggedEvent( "Received msgMakeKits"));
 	}
 
 
 	public void msgHereAreParts(List<Part> givenParts, Nest n) {
 		log.add(new LoggedEvent("Received msgHereAreParts"));
 	}
 	
     public void DoPickedUpParts() { }
     public String getName() { return null; }
 
 	@Override
 	public void msgFixKit(int num, Map<String, Integer> missingParts) {
 		log.add(new LoggedEvent( "Received msgFixKit"));		
 	}
 
 	@Override
 	public void msgPhotoBomb(NestAgent n) {
 		log.add(new LoggedEvent("Received msgPhotoBomb"));
 		
 	}
 
    public void msgTakenPicture() { }

	
 }
