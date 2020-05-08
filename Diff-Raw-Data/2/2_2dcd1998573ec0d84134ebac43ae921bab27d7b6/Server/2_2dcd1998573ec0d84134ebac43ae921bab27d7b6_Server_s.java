 package Networking;
 
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.LinkedHashMap;
 
 import DeviceGraphics.CameraGraphics;
 import DeviceGraphics.ConveyorGraphics;
 import DeviceGraphics.DeviceGraphics;
 import DeviceGraphics.FeederGraphics;
 import DeviceGraphics.KitRobotGraphics;
 import DeviceGraphics.LaneGraphics;
 import DeviceGraphics.NestGraphics;
 import DeviceGraphics.PartsRobotGraphics;
 import Utils.Constants;
 import agent.Agent;
 import agent.CameraAgent;
 import agent.ConveyorAgent;
 import agent.FeederAgent;
 import agent.KitRobotAgent;
 import agent.LaneAgent;
 import agent.NestAgent;
 import agent.PartsRobotAgent;
 import agent.StandAgent;
 
 /**
  * The Server is the "middleman" between Agents and the GUI clients. This is
  * where constructors of the different Agents will be called, as well as
  * establishing connections with the GUI clients.
  * @author Peter Zhang
  */
 public class Server {
 	private ServerSocket ss;
 	private Socket s;
 
 	// V0 Config
 	private ClientReader kitRobotMngrReader;
 	private StreamWriter kitRobotMngrWriter;
 
 	private ClientReader partsRobotMngrReader;
 	private StreamWriter partsRobotMngrWriter;
 
 	private ClientReader laneMngrReader;
 	private StreamWriter laneMngrWriter;
 
 	// See how many clients have connected
 	private int numClients = 0;
 
 	public volatile LinkedHashMap<String, DeviceGraphics> devices = new LinkedHashMap<String, DeviceGraphics>();
 	public volatile LinkedHashMap<String, Agent> agents = new LinkedHashMap<String, Agent>();
 
 	public Server() {
 		initAgents();
 		initDevices();
 		connectAgentsWithDevices();
 		connectAgentsWithEachOther();
 
 		initStreams();
 		// will never run anything after init Streams
 	}
 
 	private void initStreams() {
 		try {
 			ss = new ServerSocket(Constants.SERVER_PORT);
 		} catch (Exception e) {
 			System.out.println("Server: cannot init server socket");
 			e.printStackTrace();
 			System.exit(0);
 		}
 
 		while (true) {
 			try {
 				s = ss.accept();
 				identifyClient(s);
 				System.out.println("Server: accepted client");
 			} catch (Exception e) {
 				System.out.println("Server: got an exception" + e.getMessage());
 			}
 		}
 	}
 
 	private void initAgents() {
 		agents.put(Constants.FEEDER_TARGET, new FeederAgent(Constants.FEEDER_TARGET));
 		agents.put(Constants.LANE_TARGET+":"+0, new LaneAgent(Constants.LANE_TARGET+":"+0));
 		agents.put(Constants.LANE_TARGET+":"+1, new LaneAgent(Constants.LANE_TARGET+":"+1));
 		agents.put(Constants.CAMERA_TARGET, new CameraAgent(Constants.CAMERA_TARGET));
 		agents.put(Constants.KIT_ROBOT_TARGET, new KitRobotAgent(Constants.KIT_ROBOT_TARGET));
 		agents.put(Constants.CONVEYOR_TARGET, new ConveyorAgent(
 				Constants.CONVEYOR_TARGET));
 		 agents.put(Constants.PARTS_ROBOT_TARGET, new PartsRobotAgent(
 		 Constants.PARTS_ROBOT_TARGET));
 		agents.put(Constants.NEST_TARGET + ":" + 0, new NestAgent(
 				Constants.NEST_TARGET));
 		agents.put(Constants.NEST_TARGET + ":" + 1, new NestAgent(
 				Constants.NEST_TARGET));
 		agents.put("Stand", new StandAgent("Stand"));
 	}
 
 	private void initDevices() {
 		devices.put(Constants.LANE_TARGET + ":" + 0, new LaneGraphics(this, 0,
 				agents.get(Constants.LANE_TARGET + ":" + 0), agents.get(Constants.FEEDER_TARGET)));
 		devices.put(Constants.LANE_TARGET + ":" + 1, new LaneGraphics(this, 1,
 				agents.get(Constants.LANE_TARGET + ":" + 1), agents.get(Constants.FEEDER_TARGET)));
 		devices.put(
 				Constants.FEEDER_TARGET,
 				new FeederGraphics(0, this, agents.get(Constants.FEEDER_TARGET), agents.get(Constants.LANE_TARGET + ":" + 0), agents.get(Constants.LANE_TARGET + ":" + 1)));
 		devices.put(Constants.CONVEYOR_TARGET, new ConveyorGraphics(this, agents.get(Constants.CONVEYOR_TARGET)));
 		devices.put(Constants.KIT_ROBOT_TARGET, new KitRobotGraphics(this,
 				agents.get(Constants.KIT_ROBOT_TARGET)));
 		devices.put(Constants.PARTS_ROBOT_TARGET, new PartsRobotGraphics(this));
 		devices.put(Constants.CAMERA_TARGET,
 				new CameraGraphics(this, agents.get(Constants.CAMERA_TARGET)));
 		devices.put(Constants.NEST_TARGET + ":" + 0, new NestGraphics(this, 0,
 				agents.get(Constants.NEST_TARGET + ":" + 0)));
 		devices.put(Constants.NEST_TARGET + ":" + 1, new NestGraphics(this, 1,
 				agents.get(Constants.NEST_TARGET + ":" + 1)));
 	}
 
 	private void connectAgentsWithDevices() {
 
 		/* for(Entry<String, Agent> entry : agents.entrySet()) {
 			// entry.getValue().setGraphicRepresentation(devices.get(entry.getKey()));
 		} */
 		agents.get(Constants.FEEDER_TARGET).setGraphicalRepresentation(devices.get(Constants.FEEDER_TARGET));
 		agents.get(Constants.LANE_TARGET + ":" + 0).setGraphicalRepresentation(devices.get(Constants.LANE_TARGET + ":" + 0));
 		agents.get(Constants.LANE_TARGET + ":" + 1).setGraphicalRepresentation(devices.get(Constants.LANE_TARGET + ":" + 1));
 		agents.get(Constants.FEEDER_TARGET).startThread();
 		agents.get(Constants.LANE_TARGET + ":" + 0).startThread();
 		agents.get(Constants.LANE_TARGET + ":" + 1).startThread();
 		
 		agents.get(Constants.KIT_ROBOT_TARGET).setGraphicalRepresentation(devices.get(Constants.KIT_ROBOT_TARGET));
 		agents.get(Constants.CONVEYOR_TARGET).setGraphicalRepresentation(devices.get(Constants.CONVEYOR_TARGET));
 		agents.get(Constants.KIT_ROBOT_TARGET).startThread();
 		agents.get(Constants.NEST_TARGET + ":" + 0).setGraphicalRepresentation(
 				devices.get(Constants.NEST_TARGET + ":" + 0));
 		agents.get(Constants.NEST_TARGET + ":" + 1).setGraphicalRepresentation(
 				devices.get(Constants.NEST_TARGET + ":" + 1));
 		agents.get(Constants.CONVEYOR_TARGET).startThread();
 		agents.get(Constants.PARTS_ROBOT_TARGET).setGraphicalRepresentation(devices.get(Constants.PARTS_ROBOT_TARGET));
 		agents.get(Constants.PARTS_ROBOT_TARGET).startThread();
 		agents.get("Stand").startThread();
 	}
 
 	private void connectAgentsWithEachOther() {
 		KitRobotAgent kitrobot = (KitRobotAgent) agents
 				.get(Constants.KIT_ROBOT_TARGET);
 		StandAgent stand = (StandAgent) agents.get("Stand");
 		PartsRobotAgent partsrobot = (PartsRobotAgent) agents
 				.get(Constants.PARTS_ROBOT_TARGET);
 		CameraAgent camera = (CameraAgent) agents.get(Constants.CAMERA_TARGET);
 		ConveyorAgent conveyor = (ConveyorAgent) agents
 				.get(Constants.CONVEYOR_TARGET);
 		NestAgent nest0 = (NestAgent) agents.get(Constants.NEST_TARGET+":"+0);
 		NestAgent nest1 = (NestAgent) agents.get(Constants.NEST_TARGET+":"+1);
 		
 		stand.setKitrobot(kitrobot);
 		kitrobot.setCamera(camera);
 		kitrobot.setConveyor(conveyor);
 		kitrobot.setStand(stand);
 		conveyor.setKitrobot(kitrobot);
 		partsrobot.setStand(stand);
 		camera.setPartRobot(partsrobot);
 		camera.setKitRobot(kitrobot);
 		camera.setNest(nest0);
 		camera.setNest(nest1);
 
 		
 		
 	}
 
 	/**
 	 * Organize incoming streams according to the first message that we receive
 	 */
 	private void identifyClient(Socket s) {
 		try {
 			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
 			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
 
 			// initial identity read
 			Request req = (Request) ois.readObject();
 			System.out.println("Server: Received client");
 
 			if (req.getTarget().equals(Constants.SERVER_TARGET)
 					&& req.getCommand().equals(Constants.IDENTIFY_COMMAND)) {
 				String identity = (String) req.getData();
 				System.out.println("Server: Received identity: " + identity);
 
 				if (identity.equals(Constants.KIT_ROBOT_MNGR_CLIENT)) {
 					kitRobotMngrWriter = new StreamWriter(oos);
 					kitRobotMngrReader = new ClientReader(ois, this);
 					new Thread(kitRobotMngrReader).start();
 					numClients++;
 				} else if (identity.equals(Constants.PARTS_ROBOT_MNGR_CLIENT)) {
 					partsRobotMngrWriter = new StreamWriter(oos);
 					partsRobotMngrReader = new ClientReader(ois, this);
 					new Thread(partsRobotMngrReader).start();
 					numClients++;
 				} else if (identity.equals(Constants.LANE_MNGR_CLIENT)) {
 					laneMngrWriter = new StreamWriter(oos);
 					laneMngrReader = new ClientReader(ois, this);
 					new Thread(laneMngrReader).start();
 					numClients++;
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void receiveData(Request req) {
 		String target = req.getTarget();
 
 		if (target.equals(Constants.SERVER_TARGET)) {
 
 		} else {
 			devices.get(target).receiveData(req);
 		}
 	}
 
 	public void sendData(Request req) {
 		String target = req.getTarget();
 
 		if (target.contains(Constants.CONVEYOR_TARGET)) {
 			sendDataToConveyor(req);
 		} else if (target.contains(Constants.KIT_ROBOT_TARGET)) {
 			sendDataToKitRobot(req);
 		} else if (target.contains(Constants.PARTS_ROBOT_TARGET)) {
 			sendDataToPartsRobot(req);
 		} else if (target.contains(Constants.NEST_TARGET)) {
 			sendDataToNest(req);
 		} else if (target.contains(Constants.CAMERA_TARGET)) {
 			sendDataToCamera(req);
 		} else if (target.contains(Constants.LANE_TARGET)) {
 			sendDataToLane(req);
 		} else if (target.contains(Constants.FEEDER_TARGET)) {
 			sendDataToLane(req);
 		} else if (target.contains(Constants.KIT_TARGET)) {
 			sendDataToPartsRobot(req);
 		}
 	}
 
 	private void sendDataToConveyor(Request req) {
 		kitRobotMngrWriter.sendData(req);
 	}
 
 	private void sendDataToKitRobot(Request req) {
 		kitRobotMngrWriter.sendData(req);
 	}
 
 	private void sendDataToPartsRobot(Request req) {
 		partsRobotMngrWriter.sendData(req);
 	}
 
 	private void sendDataToNest(Request req) {
 		partsRobotMngrWriter.sendData(req);
 	}
 
 	private void sendDataToCamera(Request req) {
 		partsRobotMngrWriter.sendData(req);
 	}
 
 	private void sendDataToLane(Request req) {
 		laneMngrWriter.sendData(req);
 	}
 
 	public static void main(String[] args) {
 		Server server = new Server();
 	}
 }
