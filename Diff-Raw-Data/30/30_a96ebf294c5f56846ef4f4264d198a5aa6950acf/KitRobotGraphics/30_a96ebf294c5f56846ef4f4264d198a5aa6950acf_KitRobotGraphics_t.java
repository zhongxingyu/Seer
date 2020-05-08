 package DeviceGraphics;
 
 import java.util.TreeMap;
 
 import Networking.Request;
 import Networking.Server;
 import Utils.Constants;
 import Utils.Location;
 import agent.Agent;
 import agent.KitRobotAgent;
 import agent.StandAgent;
 import factory.KitConfig;
 import factory.PartType;
 
 public class KitRobotGraphics implements GraphicsInterfaces.KitRobotGraphics,
 		DeviceGraphics {
 
 	private final Server server;
 
 	TreeMap<String, KitGraphics> kitPositions; // keeps track of where the kits
 												// are
 
 	KitRobotAgent kitRobotAgent;
 	StandAgent standAgent; // for testing
 
 	KitGraphics testKit1; // for testing
 	KitGraphics testKit2; // for testing
 	KitGraphics currentKit;
 
 	Location inspectionLocation;
 	Location location1;
 	Location location2;
 
 	public KitRobotGraphics(Server s, Agent kra, Agent sta) {
 		kitPositions = new TreeMap<String, KitGraphics>();
 		initKitPositions();
 		server = s;
 		kitRobotAgent = (KitRobotAgent) kra;
 		standAgent = (StandAgent) sta;
 		testKit1 = new KitGraphics(server);
 		testKit2 = new KitGraphics(server);
 		inspectionLocation = new Location(240, 100);
 		location1 = new Location(280, 200);
 		location2 = new Location(240, 300);
 	}
 
 	/*
 	 * initializes the Treemap of kits.
 	 */
 	public void initKitPositions() {
 
 		KitGraphics tempKitGraphics = null;
 		kitPositions.put(Constants.KIT_INITIAL, tempKitGraphics);
 		tempKitGraphics = null;
 		kitPositions.put(Constants.KIT_INSPECTION_AREA, tempKitGraphics);
 		tempKitGraphics = null;
 		kitPositions.put(Constants.KIT_LOCATION1, tempKitGraphics);
 		tempKitGraphics = null;
 		kitPositions.put(Constants.KIT_LOCATION2, tempKitGraphics);
 
 	}
 
 	/*
 	 * sends message to KitRobotGraphicsDisplay to move a kit to inspection area
 	 * (non-Javadoc)
 	 * 
 	 * @see GraphicsInterfaces.KitRobotGraphics#msgPlaceKitInInspectionArea(
 	 * DeviceGraphics.KitGraphics) passes the kit that will move to the
 	 * inspection area
 	 */
 	@Override
 	public void msgPlaceKitInInspectionArea(KitGraphics kit) {
 		for (String kitGraphicsKey : kitPositions.keySet()) {
 			KitGraphics tempKitGraphics = kitPositions.get(kitGraphicsKey);
 			if (tempKitGraphics == null) {
 				System.out.println(this.toString() + " tempKitGraphics null");
 				System.out.println(this.toString() + kitGraphicsKey);
 			}
 			if (tempKitGraphics == kit) {
 
 				kitPositions.put(kitGraphicsKey, null);
 				if (kitGraphicsKey.equals(Constants.KIT_LOCATION1)) {
 					tempKitGraphics.setPosition(0);
 					kitPositions.put(Constants.KIT_INSPECTION_AREA,
 							tempKitGraphics);
 					server.sendData(new Request(
 							Constants.STAND_GIVE_KIT_COMMAND,
 							Constants.STAND_TARGET + 1, null));
 				} else {
 					tempKitGraphics.setPosition(0);
 					kitPositions.put(Constants.KIT_INSPECTION_AREA,
 							tempKitGraphics);
 					server.sendData(new Request(
 							Constants.STAND_GIVE_KIT_COMMAND,
 							Constants.STAND_TARGET + 2, null));
 				}
 			}
 
 		}
 
 		kitPositions.put(Constants.KIT_INSPECTION_AREA, kit);
 
 	}
 
 	public void msgPlaceKitOnStand1(KitGraphics kit) {
 		kit.setLocation(location1);
 		kit.setPosition(1);
 		kitPositions.put(Constants.KIT_LOCATION1, kit);
 		server.sendData(new Request(
 				Constants.KIT_ROBOT_DISPLAY_PICKS_CONVEYOR_TO_LOCATION1,
 				Constants.KIT_ROBOT_TARGET, null));
 
 	}
 
 	public void msgPlaceKitOnStand2(KitGraphics kit) {
 		kit.setLocation(location2);
 		kit.setPosition(2);
 		kitPositions.put(Constants.KIT_LOCATION2, kit);
 		server.sendData(new Request(
 				Constants.KIT_ROBOT_DISPLAY_PICKS_CONVEYOR_TO_LOCATION2,
 				Constants.KIT_ROBOT_TARGET, null));
 	}
 
 	/*
 	 * messages KitRobotGraphicsDisplay to move a kit to the GoodConveyor
 	 * (non-Javadoc)
 	 * 
 	 * @see GraphicsInterfaces.KitRobotGraphics#msgPlaceKitOnConveyor()
 	 */
 	@Override
 	public void msgPlaceKitOnConveyor() {
 		kitPositions.get(Constants.KIT_INSPECTION_AREA).setPosition(3);
 		kitPositions.put(Constants.KIT_INSPECTION_AREA, null);
 		server.sendData(new Request(Constants.STAND_GIVE_KIT_COMMAND,
 				Constants.STAND_TARGET + 0, null));
 	}
 
 	/*
 	 * Moves the kit to a stand location (non-Javadoc)
 	 * 
 	 * @see
 	 * GraphicsInterfaces.KitRobotGraphics#msgPlaceKitOnStand(DeviceGraphics
 	 * .KitGraphics, int)
 	 */
 	@Override
 	public void msgPlaceKitOnStand(KitGraphics kit, int location) {
 		Boolean placeFromInspection = false;
 		if(kitPositions.get(Constants.KIT_INSPECTION_AREA)==kit)
 		{
 			placeFromInspection=true;	
 		}
 		
 		System.out.println("dfjklqhwetjhqweiponqwerihadfjkghuiafghuioqhejtoiqwewqehtuijklllllllllllllllllllllllllllllllllllllllllqwetiohqweuithqweuiothj");
 		if ( !placeFromInspection ){
 			if ( location == 1) {
 				msgPlaceKitOnStand1(kit);
 			} else if ( location == 2) {
 				msgPlaceKitOnStand2(kit);
 			}
 		} else {
 			if ( location == 1 ){
 				kitPositions.put(Constants.KIT_LOCATION1, kit);
 			} else if( location == 2){
 				kitPositions.put(Constants.KIT_LOCATION2, kit);
 			}
 			
 			server.sendData(new Request(Constants.STAND_GIVES_BACK_TO_ANOTHER_STAND, Constants.STAND_TARGET + 0 , location) );	
 			kitPositions.put(Constants.KIT_INSPECTION_AREA, null);
 		}
 		
 
 	}
 
 	/*
 	 * receives request that will send commands based on the request
 	 * 
 	 * @see DeviceGraphics.DeviceGraphics#receiveData(Networking.Request)
 	 * request is a class that has a command target and data
 	 */
 	@Override
 	public void receiveData(Request req) {
 		String target = req.getTarget();
 		String command = req.getCommand();
 		Object object = req.getData();
 
 		if (command
 				.equals(Constants.KIT_ROBOT_LOGIC_PICKS_CONVEYOR_TO_LOCATION1)) {
 
 			msgPlaceKitOnStand(testKit1, 1);
 		} else if (command
 				.equals(Constants.KIT_ROBOT_LOGIC_PICKS_CONVEYOR_TO_LOCATION2)) {
 
 			msgPlaceKitOnStand(testKit2, 2);
 		} else if (command
 				.equals(Constants.KIT_ROBOT_LOGIC_PICKS_LOCATION1_TO_INSPECTION)) {
 			msgPlaceKitInInspectionArea(testKit1);
 
 		} else if (command
 				.equals(Constants.KIT_ROBOT_LOGIC_PICKS_LOCATION2_TO_INSPECTION)) {
 			msgPlaceKitInInspectionArea(testKit2);
 		} else if (command
 				.equals(Constants.KIT_ROBOT_LOGIC_PICKS_INSPECTION_TO_GOOD_CONVEYOR)) {
 			msgPlaceKitOnConveyor();		
		} else if(command.equals(Constants.KIT_ROBOT_LOGIC_PICKS_INSPECTION_TO_LOCATION1 + Constants.DONE_SUFFIX)){
 			server.sendData(new Request(Constants.KIT_ROBOT_DISPLAY_PICKS_INSPECTION_TO_LOCATION1, Constants.KIT_ROBOT_TARGET, object));
		} else if(command.equals(Constants.KIT_ROBOT_LOGIC_PICKS_INSPECTION_TO_LOCATION2 + Constants.DONE_SUFFIX)){
 			server.sendData(new Request(Constants.KIT_ROBOT_DISPLAY_PICKS_INSPECTION_TO_LOCATION2, Constants.KIT_ROBOT_TARGET, object));
 		} else if (command
 				.equals(Constants.CONVEYOR_GIVE_KIT_TO_KIT_ROBOT_COMMAND)) {
 			server.sendData(new Request(Constants.CONVEYOR_RECEIVE_KIT_COMMAND,
 					Constants.CONVEYOR_TARGET, null));
 		} else if (command.equals(Constants.KIT_ROBOT_AGENT_RECEIVES_KIT1_DONE)) {
 		} else if (command.equals(Constants.KIT_ROBOT_AGENT_RECEIVES_KIT2_DONE)) {
 		} else if (command
 				.equals(Constants.KIT_ROBOT_AGENT_RECEIVES_KIT_INSPECTED)) {
 			// Hack for KitRobotManager
 			kitRobotAgent.msgKitPassedInspection();
 		} else if (command.equals(Constants.KIT_ROBOT_ON_STAND1_DONE)) {
 
 			kitRobotAgent.msgPlaceKitOnStandDone();
 			server.sendData(new Request(Constants.STAND_RECEIVE_KIT_COMMAND,
 					Constants.STAND_TARGET + 1, object));
 		} else if (command.equals(Constants.KIT_ROBOT_ON_STAND2_DONE)) {
 			kitRobotAgent.msgPlaceKitOnStandDone();
 			server.sendData(new Request(Constants.STAND_RECEIVE_KIT_COMMAND,
 					Constants.STAND_TARGET + 2, object));
 		} else if (command.equals(Constants.KIT_ROBOT_ON_CONVEYOR_DONE)) {
 			server.sendData(new Request(Constants.KIT_ROBOT_PASSES_KIT_COMMAND,
 					Constants.CONVEYOR_TARGET, object));
 			kitRobotAgent.msgPlaceKitOnConveyorDone();
 		} else if (command.equals(Constants.KIT_ROBOT_ON_INSPECTION_DONE)) {
 			kitRobotAgent.msgPlaceKitInInspectionAreaDone();
 			server.sendData(new Request(Constants.STAND_RECEIVE_KIT_COMMAND,
 					Constants.STAND_TARGET + 0, object));
 		} else if (command.equals(Constants.KIT_RECEIVES_PART)) {
 
 			// PartGraphics testPart = new
 			// PartGraphics(Constants.DEFAULT_PARTTYPES.get(1));
 			// kitPositions.get(Constants.KIT_LOCATION1).receivePart(testPart);
 			PartType testPartType = Constants.DEFAULT_PARTTYPES.get(2);
 			server.sendData(new Request(Constants.STAND_RECEIVE_PART_COMMAND,
 					Constants.STAND_TARGET + 1, testPartType));
 		} else if (command
 				.equals(Constants.KIT_ROBOT_DISPLAY_STAND_NOW_MOVES_FROM +Constants.DONE_SUFFIX)) {
 			KitConfig kitConfig = (KitConfig)object;
 			if (kitConfig.getStandId()== 0 )
 			{
 				server.sendData(new Request(
 					Constants.KIT_ROBOT_DISPLAY_PICKS_INSPECTION_TO_GOOD_CONVEYOR,
 					Constants.KIT_ROBOT_TARGET, object));
 			}
 			else if (kitConfig.getStandId() == 1 )
 			{
 				server.sendData(new Request(
 					Constants.KIT_ROBOT_DISPLAY_PICKS_LOCATION1_TO_INSPECTION,
 					Constants.KIT_ROBOT_TARGET, object));
 			}
 			else if (kitConfig.getStandId() ==2 )
 			{
 				server.sendData(new Request(
 					Constants.KIT_ROBOT_DISPLAY_PICKS_LOCATION2_TO_INSPECTION,
 					Constants.KIT_ROBOT_TARGET, object));
 			}
 		} 
 	}
 
 }
