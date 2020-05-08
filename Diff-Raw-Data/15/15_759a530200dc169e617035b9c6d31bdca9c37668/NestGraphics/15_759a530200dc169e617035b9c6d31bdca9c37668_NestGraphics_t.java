 package DeviceGraphics;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import DeviceGraphicsDisplay.PartGraphicsDisplay;
 import Networking.Request;
 import Networking.Server;
 import Utils.Constants;
 import Utils.Location;
 import agent.Agent;
 import agent.NestAgent;
 import agent.data.Part;
 import agent.data.PartType;
 
 /**
  * This class represents the graphics logic for a nest.
  * 
  * @author Shalynn Ho, Aaron Harris, Harry Trieu
  *
  */
 public class NestGraphics implements GraphicsInterfaces.NestGraphics, DeviceGraphics {
 	// max number of parts this Nest holds
 	private static final int MAX_PARTS = 8;
 	
 	// instructions to display graphics will be sent through the server
 	private Server server;
 	// the ID of this Nest
 	private int nestID;
 	// the NestAgent
 	private NestAgent nestAgent;
 	
 	// Location of upper left corner of this nest
 	private Location location;
 	// dynamically stores the parts currently in the Nest
 	private ArrayList<PartGraphics> partsInNest;
 	
 	// true during Nest purge cycle, can't receive parts
 	private boolean isPurging;
 	// true if nest is full, can't receive parts
 	private boolean isFull;
 	// true if spot is filled, false if not
 	private ArrayList<Boolean> nestSpots;
 	// y-coordinate of the Nest
 	private static int NEST_Y;
 	// location of the nest
 	private Location nestLocation;
 	
 	
 	public NestGraphics(Server s, int nid, Agent agent) {
 		server = s;
 		nestID = nid;
 		nestAgent = (NestAgent) agent;
 		nestLocation = new Location(600, 100 + nestID * 75);
 		partsInNest = new ArrayList<PartGraphics>(MAX_PARTS);
 		nestSpots = new ArrayList<Boolean>(MAX_PARTS);
 		
 		if(nestID==0) {
 			NEST_Y=100;
 		} else {
 			NEST_Y=175;
 		}
 		
 		// Begin V0 requirements
 		isFull = true;
 		for (int i = 0; i < 8; i++) {
 			PartGraphics temp = new PartGraphics(PartType.A);
 			
 			if(i < 4) {
 				temp.setLocation(new Location((119+i*20),(NEST_Y+1)));
 			} else {
 				temp.setLocation(new Location((119+(i-4)*20),(NEST_Y+23))); 
 			}
 			partsInNest.add(temp);
 		}
 	}
 
 	/**
 	 * @param - 
 	 */
 	public void receivePart(PartGraphics pg) {
 		partsInNest.add(pg);
		addPartToCorrectLocation(pg, partsInNest.size()); // set part location to next empty spot
		PartType pt = pg.getPartType();
 		server.sendData(new Request(Constants.NEST_RECEIVE_PART_COMMAND, Constants.NEST_TARGET+":"+nestID, pt));
 	}
 	
 	/**
 	 * @param
 	 */
 	public void givePartToPartsRobot(PartGraphics pg) {
 		int i = partsInNest.indexOf(pg); // this might not work. depends on if part passed in matches what is already in nest
 										// otherwise, must find a way to figure out which part is being taken from which spot in the nest
 		partsInNest.remove(i);
 		server.sendData(new Request(Constants.NEST_GIVE_TO_PART_ROBOT_COMMAND, Constants.NEST_TARGET+":"+nestID, null));
 	}
 	
 	/**
 	 * 
 	 */
 	public void purge() {
//		purging = true;
 		partsInNest.clear();
 		server.sendData(new Request(Constants.NEST_PURGE_COMMAND, Constants.NEST_TARGET+":"+nestID, null));
 	}
 	
 	/**
 	 * Receives message data from the Server
 	 * @param r - the request to be parsed
 	 */
 	public void receiveData(Request req) {
 		if (req.getCommand().equals(Constants.NEST_RECEIVE_PART_COMMAND + Constants.DONE_SUFFIX)) {
 			nestAgent.msgReceivePartDone();
 		} else if (req.getCommand().equals(Constants.NEST_GIVE_TO_PART_ROBOT_COMMAND + Constants.DONE_SUFFIX)) {
 			nestAgent.msgGivePartToPartsRobotDone();
 		} else if (req.getCommand().equals(Constants.NEST_PURGE_COMMAND + Constants.DONE_SUFFIX)) {
 			nestAgent.msgPurgingDone();
 		}
 		
 	}
 	
 	public void addPartToCorrectLocation(PartGraphics temp, int i){
 		if(i < 4) {
 			temp.setLocation(new Location((nestLocation.getX()+i*20),(nestLocation.getY()+1)));
 		} else {
 			temp.setLocation(new Location((nestLocation.getX()+(i-4)*20),(nestLocation.getY()+23))); 
 		}
 	}
 	
 	/**
 	 * update location of the parts
 	 * @param x
 	 */
 	public void updateLocationOfParts(ArrayList<PartGraphics> x){
 		for(int i=0; i<x.size(); i++){
 			addPartToCorrectLocation(x.get(i),i);
 		}
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isFull() {
 		return partsInNest.size() == MAX_PARTS;
 	}
 	
 	/**
 	 * V2 ONLY
 	 * @return
 	 */
 	public boolean allPartsUnanalyzed() {
 		// TODO: IMPLEMENT THIS METHOD FOR V2
 		return false;
 	}
 	
 	/**
 	 * V2 ONLY
 	 * @return
 	 */
 	public boolean allPartsGood() {
 		// TODO: IMPLEMENT THIS METHOD FOR V2
 		return false;
 	}
 	
 	/**
 	 * V2 ONLY
 	 * @return
 	 */
 	public boolean allPartsBad() {
 		// TODO: IMPLEMENT THIS METHOD FOR V2
 		return false;
 	}
 	
 	/**
 	 * @return location of this nest
 	 */
 	public Location getLocation() {
 		return location;
 	}
 
 	/**
 	 * 
 	 * @returns an array list of the parts in nest
 	 */
 	public ArrayList<PartGraphics> getPartsInNest() {
 		return partsInNest;
 	}
 	
 	/**
 	 * V2 ONLY, no bad parts in V1.
 	 * @return
 	 */
 	public Map<PartGraphics,Boolean> getQualityOfParts() {
 		// TODO: IMPLEMENT THIS METHOD FOR V2
 		return null;
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 	}
 
 	
//	@Override
//	 V0 ONLY
 //	public void givePartToPartsRobot(PartGraphics part) {
//		 TODO Auto-generated method stub	}
 	
 }
