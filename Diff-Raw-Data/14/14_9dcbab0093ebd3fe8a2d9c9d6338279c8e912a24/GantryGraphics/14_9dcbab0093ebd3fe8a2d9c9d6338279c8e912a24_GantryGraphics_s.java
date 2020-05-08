 package DeviceGraphics;
 
 import java.util.ArrayList;
 
 import GraphicsInterfaces.FeederGraphics;
 import Networking.Request;
 import Networking.Server;
 import Utils.BinData;
 import Utils.Constants;
 import Utils.Location;
 import agent.Agent;
 import agent.FeederAgent;
 import agent.GantryAgent;
 import agent.data.Bin;
 
 public class GantryGraphics implements DeviceGraphics, GraphicsInterfaces.GantryGraphics  {
 	
 	Bin heldBin; // Bin that gantry is carrying
 	private Server server;
 	ArrayList<BinGraphics> binList = new ArrayList<BinGraphics>();
 	
 	boolean removeState = false;
 	boolean removeState2 = false;
 	boolean receiveState = false;
 	boolean receiveState2 = false;
 	boolean dropState = false;
 	boolean newBinState = false;
 	
 	GantryAgent gantryAgent;
 	
 	Location newLocation;
 
 	public GantryGraphics(Server s, Agent ga) {
 		heldBin = null;
 		server = s;
 		gantryAgent = (GantryAgent) ga;
 	}
 	
 	// Move robot to bin location
 	public void receiveBin(Bin newBin, FeederAgent feeder) {
 		heldBin = newBin;
 		newLocation = newBin.binGraphics.getInitialLocation();
 		moveTo(newLocation);
 		newLocation = feeder.feederGUI.getLocation();
 		newLocation.incrementX(50);
 		newLocation.incrementY(35);
 
 		receiveState = true;
 	}
 	
 	// Move to bin location
 	public void removeBin(Bin newBin) {
 		heldBin = newBin;
 		moveTo (heldBin.binGraphics.getLocation());
 		removeState = true;
 	}
 	
 	// drop bin into feeder
 	public void dropBin (Bin newBin, FeederAgent feeder) {
 		server.sendData(new Request(Constants.GANTRY_ROBOT_DROP_BIN_COMMAND, Constants.GANTRY_ROBOT_TARGET, null));
 		gantryAgent.msgDropBinDone(newBin);
 		heldBin = null;
 	}
 		
 	
 	private void moveTo (Location newLocation) {
 		server.sendData(new Request(Constants.GANTRY_ROBOT_MOVE_TO_LOC_COMMAND, Constants.GANTRY_ROBOT_TARGET, newLocation));
 	}
 	
 	@Override
 	public void receiveData(Request req) {
 		if (req.getCommand().equals(Constants.GANTRY_ROBOT_DONE_MOVE)) {
 			// Robot is over bin, picks up bin and moves to feeder
 			if (receiveState) {
 				server.sendData(new Request(Constants.GANTRY_ROBOT_GET_BIN_COMMAND, Constants.GANTRY_ROBOT_TARGET, new BinData(heldBin.binGraphics.getLocation(), heldBin.part.type)));
 				moveTo(newLocation);
 				receiveState = false;
 				receiveState2 = true;
 			}
 			// Robot is at feeder, send done message to agent 
 			else if (receiveState2) {
 				gantryAgent.msgReceiveBinDone(heldBin);
 				receiveState2 = false;
 			}
 			
 			// Pick up bin, move it back to initial location
 			else if (removeState) {
 				server.sendData(new Request(Constants.GANTRY_ROBOT_GET_BIN_COMMAND, Constants.GANTRY_ROBOT_TARGET, new BinData(heldBin.binGraphics.getLocation(), heldBin.part.type)));
 				moveTo (heldBin.binGraphics.getInitialLocation());
 				removeState = false;
 				removeState2 = true;
 			}
 			// Robot is above initial bin location, removes bin
 			else if (removeState2) {
 				server.sendData(new Request(Constants.GANTRY_ROBOT_GET_BIN_COMMAND, Constants.GANTRY_ROBOT_TARGET, null));
 				gantryAgent.msgRemoveBinDone(heldBin);
 				heldBin = null;
 				removeState2 = false;
 			}
 		}
 	}
 
 	public void hereIsNewBin(Bin bin) {
 		binList.add(bin.binGraphics);
 		server.sendData(new Request(Constants.GANTRY_ROBOT_ADD_NEW_BIN, Constants.GANTRY_ROBOT_TARGET, new BinData(bin.binGraphics.getInitialLocation(), bin.part.type)));
 	}
 
 
 }
