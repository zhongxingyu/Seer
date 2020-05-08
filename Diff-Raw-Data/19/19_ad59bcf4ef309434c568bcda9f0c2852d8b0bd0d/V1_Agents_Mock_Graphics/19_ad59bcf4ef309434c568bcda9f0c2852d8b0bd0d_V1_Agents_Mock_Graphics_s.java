 package agent.test;
 
 import java.util.ArrayList;
 
 import agent.CameraAgent;
 import agent.ConveyorAgent;
 import agent.FCSAgent;
 import agent.FeederAgent;
 import agent.GantryAgent;
 import agent.KitRobotAgent;
 import agent.LaneAgent;
 import agent.NestAgent;
 import agent.PartsRobotAgent;
 import agent.StandAgent;
 import agent.test.mock.MockFeederGraphics;
 import agent.test.mock.MockGraphics;
 import agent.test.mock.MockLaneGraphics;
 import agent.test.mock.MockNestGraphics;
 import factory.KitConfig;
 import factory.Order;
 import factory.PartType;
 
 public class V1_Agents_Mock_Graphics {
 
 	public static void main(String[] args) {
 		GantryAgent gantry = new GantryAgent("Gantry Agent");
 		ArrayList<FeederAgent> feeders = new ArrayList<FeederAgent>();
 		for (int i = 0; i < 4; i++) {
 			feeders.add(new FeederAgent("Feeder Agent " + i));
 		}
 		ArrayList<LaneAgent> lanes = new ArrayList<LaneAgent>();
 		for (int i = 0; i < 8; i++) {
 			lanes.add(new LaneAgent("Lane Agent " + i));
 		}
 		ArrayList<NestAgent> nests = new ArrayList<NestAgent>();
 		for (int i = 0; i < 8; i++) {
 			nests.add(new NestAgent("Nest Agent " + i));
 		}
 		PartsRobotAgent partsRobot = new PartsRobotAgent("Parts Robot Agent");
 		CameraAgent camera = new CameraAgent("Camera Agent");
 		StandAgent stand = new StandAgent("Stand Agent");
 		KitRobotAgent kitRobot = new KitRobotAgent("Kit Robot Agent");
 		ConveyorAgent conveyor = new ConveyorAgent("Conveyor Agent");
 		FCSAgent fcs = new FCSAgent("FCS Agent");
 
 		for (int i = 0; i < 8; i++) {
 			feeders.get(i / 2).setGantry(gantry);
 			feeders.get(i / 2).setLane(lanes.get(i));
 			lanes.get(i).setFeeder(feeders.get(i / 2));
 			lanes.get(i).setNest(nests.get(i));
 			nests.get(i).setLane(lanes.get(i));
 			nests.get(i).setCamera(camera);
 			camera.setNest(nests.get(i));
 			fcs.setNest(nests.get(i));
 		}
		conveyor.setFcs(fcs);
 		conveyor.setKitrobot(kitRobot);
		if(conveyor.getFcs()==null){
 			System.out.println("Conveyor fcs null");
 		}
		if(conveyor.getKitrobot()==null){
 			System.out.println("Conveyor kitrobot null");
 		} else {
 			System.out.println("Conveyor kitrobot not null");
 		}
 		camera.setKitRobot(kitRobot);
 		camera.setPartsRobot(partsRobot);
 		partsRobot.setStand(stand);
 		stand.setFCS(fcs);
 		stand.setKitrobot(kitRobot);
 		stand.setPartsRobot(partsRobot);
 		kitRobot.setCamera(camera);
 		kitRobot.setStand(stand);
 		kitRobot.setConveyor(conveyor);
 		fcs.setConveyor(conveyor);
 		fcs.setGantry(gantry);
 		fcs.setPartsRobot(partsRobot);
 		fcs.setStand(stand);
 
 		MockGraphics mg = new MockGraphics("Mock Graphics");
 		ArrayList<MockFeederGraphics> mockFeeders = new ArrayList<MockFeederGraphics>();
 		for (int i = 0; i < 4; i++) {
 			mockFeeders.add(new MockFeederGraphics());
 		}
 		ArrayList<MockLaneGraphics> mockLanes = new ArrayList<MockLaneGraphics>();
 		for (int i = 0; i < 8; i++) {
 			mockLanes.add(new MockLaneGraphics());
 		}
 		ArrayList<MockNestGraphics> mockNests = new ArrayList<MockNestGraphics>();
 		for (int i = 0; i < 8; i++) {
 			mockNests.add(new MockNestGraphics());
 		}
		
 		mg.setCamera(camera);
 		mg.setConveyor(conveyor);
		//mg.setFeeder(feeder);
 		mg.setGantry(gantry);
 		mg.setKitrobot(kitRobot);
		//mg.setLane(lane);
		//mg.setNest(nest);
 		mg.setPartsrobot(partsRobot);
 
 		gantry.setGraphicalRepresentation(mg);
 		for (int i = 0; i < 4; i++) {
 			feeders.get(i).setGraphicalRepresentation(mockFeeders.get(i));
 			mockFeeders.get(i).setFeederAgent(feeders.get(i));
 		}
 		for (int i = 0; i < 8; i++) {
 			lanes.get(i).setGraphicalRepresentation(mockLanes.get(i));
 			mockLanes.get(i).setLaneAgent(lanes.get(i));
 		}
 		for (int i = 0; i < 8; i++) {
 			nests.get(i).setGraphicalRepresentation(mockNests.get(i));
 			mockNests.get(i).setNestAgent(nests.get(i));
 		}
 		partsRobot.setGraphicalRepresentation(mg);
 		camera.setGraphicalRepresentation(mg);
 		stand.setGraphicalRepresentation(mg);
 		kitRobot.setGraphicalRepresentation(mg);
 		conveyor.setGraphicalRepresentation(mg);
 		fcs.setGraphicalRepresentation(mg);
 
 		KitConfig kg = new KitConfig("Kit config");
 		kg.addItem(new PartType("1"), 1);
 		kg.addItem(new PartType("2"), 1);
 		kg.addItem(new PartType("3"), 1);
 		kg.addItem(new PartType("4"), 1);
 		kg.addItem(new PartType("5"), 1);
 		kg.addItem(new PartType("6"), 1);
 		kg.addItem(new PartType("7"), 1);
 		kg.addItem(new PartType("8"), 1);
 
 		gantry.startThread();
 		for (int i = 0; i < 4; i++) {
 			feeders.get(i).startThread();
 			mockFeeders.get(i).startThread();
 		}
 		for (int i = 0; i < 8; i++) {
 			lanes.get(i).startThread();
 			mockLanes.get(i).startThread();
 		}
 		for (int i = 0; i < 8; i++) {
 			nests.get(i).startThread();
 			mockNests.get(i).startThread();
 		}
 		mg.startThread();
 		partsRobot.startThread();
 		camera.startThread();
 		stand.startThread();
 		kitRobot.startThread();
 		conveyor.startThread();
 		fcs.startThread();
 
 		fcs.msgStartProduction();
 		fcs.msgAddKitsToQueue(new Order(kg, 1));
 	}
 
 }
