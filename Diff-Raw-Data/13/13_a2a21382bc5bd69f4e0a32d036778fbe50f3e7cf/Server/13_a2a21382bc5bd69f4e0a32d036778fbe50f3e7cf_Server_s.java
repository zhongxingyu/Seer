 package Networking;
 
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 
 import DeviceGraphics.ConveyorGraphics;
 import DeviceGraphics.DeviceGraphics;
 import DeviceGraphics.FeederGraphics;
 import Utils.Constants;
 
 /**
  * The Server is the "middleman" between Agents and the GUI clients. 
  * This is where constructors of the different Agents will be called, 
  * as well as establishing connections with the GUI clients.
  * 
  * @author Peter Zhang
  *
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
 	
	private HashMap<String, DeviceGraphics> devices = new HashMap<String, DeviceGraphics>();
 	
 	public Server() {
 		initStreams();
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
 	
 	private void initDevices() {
 		devices.put(Constants.FEEDER_TARGET, new FeederGraphics(0, this));
 		devices.put(Constants.CONVEYOR_TARGET, new ConveyorGraphics(this));
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
 			
 			if (req.getTarget().equals(Constants.SERVER_TARGET) && req.getCommand().equals(Constants.IDENTIFY_COMMAND)) {
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
 					new Thread(partsRobotMngrReader). start();
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
 		
 		if(target.equals(Constants.SERVER_TARGET)) {
 			
 		} else {
			devices.get(req.getTarget()).receiveData(req);
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
