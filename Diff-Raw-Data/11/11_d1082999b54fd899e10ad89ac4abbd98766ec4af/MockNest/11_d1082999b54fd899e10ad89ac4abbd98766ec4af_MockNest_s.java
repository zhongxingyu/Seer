 package agents.test.mock;
 import gui.GUI_Nest;
 
 import java.util.*;
 
 import agents.Part;
 import agents.interfaces.*;
 import agents.*;
 
 public class MockNest extends MockAgent implements Nest {
 
 	public PartRobot partRobot;
 	public EventLog log = new EventLog();
 	private String name;
 	public MockNest(String name, PartRobot partRobot) {
 		super(name);
 		this.name = name;
 		this.partRobot = partRobot;
 
 	}
 
 	public String partType = "";
 	public void msgRequestParts(String type, int count) {
 		
 		partType = type;
     	log.add(new LoggedEvent("Received request of " + count + " of " + type));
     	
     	List<Part> sendParts = new ArrayList<Part>();
     	for(int i=0; i< count; i++)
     	{
     		sendParts.add(new Part(type));
     	}
     	partRobot.msgHereAreParts(sendParts, this);
     	
 	}
 
 	
 	public void msgHereIsPart(Part p) {
 		
 	}
 
 	public void msgPartsAreGood(List<Part> visionParts) {
 		
 	}
 
 	public String getPartType() {
 		return partType;
 	}
 
 	public String getName() { return name; }
     public GUI_Nest guiNest() { return null; }
 
 
 	@Override
 	public void msgHereIsPart(String currentPart) {
 		log.add(new LoggedEvent("Received msgHereIsPart for part type " + currentPart));
 	}
 }
