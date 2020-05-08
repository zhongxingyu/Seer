 package DeviceGraphics;
 
 import Networking.*;
 import Utils.*;
 import factory.data.*;
 import factory.*;
 import agent.Agent;
 
 import java.util.ArrayList;
 
 /**
  * This class contains the graphics logic for a lane.
  * 
  * @author Shalynn Ho
  * 
  */
 public class LaneGraphics extends DeviceGraphics implements
 		GraphicsInterfaces.LaneGraphics {
 	// max number of parts that can be on a Lane
 	private static final int MAX_PARTS = 8;
 
 	// start location of the part
 	private Location startLoc;
 
 	// instructions to display graphics will be sent through the server
 	private Server server;
 	// the ID of this Lane
 	private int laneID;
 	// the lane agent
 	private LaneAgent laneAgent;
 	// the Nest associated with this LaneGraphics object
 //	private NestGraphics nest;
 
 	// dynamically stores Parts currently on Lane
 	private ArrayList<PartGraphics> partsOnLane;
 	// storing for the 201 agents
 	private ArrayList<Part> agentPartsOnLane;
 
 	// vibration setting; how quickly parts vibrate down Lane
 	private int amplitude;
 
 	// true if Lane is on
 	private boolean laneOn;
 
 	/**
 	 * 
 	 * @param s - the Server
 	 * @param id - ID of this lane
 	 * @param la - the LaneAgent
 	 */
 	public LaneGraphics(Server s, int id, Agent la) {
 		server = s;
 		laneID = id;
 		laneAgent = (LaneAgent) la;
 //		nest = n;
 
 		partsOnLane = new ArrayList<PartGraphics>();
 		agentPartsOnLane = new ArrayList<Part>();
 		amplitude = 5;
 		laneOn = true;
 	}
 
 	/**
 	 * 
 	 * @return true if this lane is full
 	 */
 	public boolean isFull() {
 		return partsOnLane.size() >= MAX_PARTS;
 	}
 
 	/**
 	 * Called when part needs to be given to the nest associated with this lane.
 	 * Basically, this lane doesn't care about that part anymore.
 	 * 
 	 * @param pg
 	 *            - the part passed to the nest associated with this lane
 	 */
 	public void givePartToNest(Part p) {
 		 PartGraphics pg = p.part;
 		/*
 		 * at the end of the Lane, gives the Part to the Nest - receive message
 		 * from LGD that Part is at end of Lane and Nest not full - tell
 		 * NestGraphicsLogic that we are passing Part - remove from Lane parts
 		 * queue
 		 */
 		// do i need to check if nest is full first? (or do agents do this?)
 		// just to double check, i don't call nest.receivePart(part) right?
 
 		partsOnLane.remove(0); // this is kind of dangerous. check that correct
 								// part is removed.
 		server.sendData(new Request(Constants.LANE_GIVE_PART_TO_NEST, Constants.LANE_TARGET +":"+ laneID, null));
 	}
 
 	/**
 	 * 
 	 */
 	public void purge() {
 		partsOnLane.clear();
 		agentPartsOnLane.clear();
 		// TODO: set location of parts to fall off lane
 		server.sendData(new Request(Constants.LANE_PURGE_COMMAND,
 				Constants.LANE_TARGET  +":"+  laneID, null));
 	}
 
 	/**
 	 * Called when part is delivered to this lane
 	 * 
 	 * @param pg
 	 *            - the part passed to this lane
 	 */
 	public void receivePart(Part p) {
 		agentPartsOnLane.add(p);
 		PartGraphics pg = p.part;
 		partsOnLane.add(pg);
 		pg.setLocation(startLoc);
 		PartType pt = p.type;
 		
 		server.sendData(new Request(Constants.LANE_RECEIVE_PART_COMMAND, Constants.LANE_TARGET+laneID, pt));
 		
 		// later pass if good/bad part also
 	}
 	
 	/**
 	 * TODO: OVERLOADED TEST METHOD FOR V0, REMOVE LATER
 	 * 
 	 * @param pg
 	 *            - the part passed to this lane
 	 */
 	public void receivePart(PartGraphics pg) {
 		partsOnLane.add(pg);
 		pg.setLocation(startLoc);
 		PartType pt = pg.getPartType();
 		
 		server.sendData(new Request(Constants.LANE_RECEIVE_PART_COMMAND, Constants.LANE_TARGET +":"+ laneID, pt));
 		
 		// later pass if good/bad part also
 	}
 
 	/**
 	 * 
 	 * @param r
 	 */
 	public void receiveData(Request r) {
 		String cmd = r.getCommand();
 	
 		// must parse data request here
 	
 		// TODO: We want confirmation from Display each time an animation is
 		// completed.
 		
 		if (cmd.equals(Constants.LANE_RECEIVE_PART_COMMAND)) {	// testing purposes only, remove later
 			receivePart(new PartGraphics(PartType.A));			
 		} else if (cmd.equals(Constants.LANE_RECEIVE_PART_COMMAND+Constants.DONE_SUFFIX)) {
			// add these back later
			//laneAgent.msgReceivePartDone(agentPartsOnLane.get(agentPartsOnLane.size()-1));
 		} else if (cmd.equals(Constants.LANE_GIVE_PART_TO_NEST + Constants.DONE_SUFFIX)) {
			// add these back later
			//laneAgent.msgGivePartToNestDone(agentPartsOnLane.get(0));
			//agentPartsOnLane.remove(0);
 		}
 	
 	}
 
 	/**
 	 * Sets the vibration amplitude of this lane (how quickly parts vibrate down
 	 * lane)
 	 * 
 	 * @param amp
 	 *            - the amplitude
 	 */
 	public void setAmplitude(int amp) {
 		amplitude = amp;
 		server.sendData(new Request(Constants.LANE_SET_AMPLITUDE_COMMAND,
 				Constants.LANE_TARGET +":"+ laneID, amp));
 	}
 
 	/**
 	 * 
 	 * @param on
 	 *            - on/off switch for this lane
 	 */
 	public void toggleSwitch(boolean on) {
 		laneOn = on;
 		server.sendData(new Request(Constants.LANE_TOGGLE_COMMAND,
 				Constants.LANE_TARGET +":"+  laneID, laneOn));
 	}
 
 	/**
 	 * Sends an instance of Animation through the server. Tells the display
 	 * class end Location of animation and duration allotted.
 	 */
 	private void sendAnimation(Animation ani) {
 		server.sendData(new Request(Constants.LANE_SEND_ANIMATION_COMMAND,
 				Constants.LANE_TARGET  +":"+  laneID, ani));
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
