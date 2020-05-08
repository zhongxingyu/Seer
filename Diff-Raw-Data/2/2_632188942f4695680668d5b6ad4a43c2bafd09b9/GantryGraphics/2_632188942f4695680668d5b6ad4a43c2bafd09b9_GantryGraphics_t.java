 package DeviceGraphics;
 
 import java.util.ArrayList;
 
 import Networking.Request;
 import Networking.Server;
 import Utils.BinData;
 import Utils.Constants;
 import Utils.Location;
 import agent.Agent;
 import agent.FeederAgent;
 import agent.GantryAgent;
 import agent.data.Bin;
 import factory.PartType;
 
 public class GantryGraphics implements DeviceGraphics, GraphicsInterfaces.GantryGraphics  {
 	
 	Bin heldBin; // Bin that gantry is carrying
 	private Server server;
 	ArrayList<BinGraphics> binList = new ArrayList<BinGraphics>();
 	
 	boolean removeState = false;
 	boolean removeState2 = false;
 	boolean receiveState = false;
 	boolean receiveState2 = false;
 	
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
 		newLocation = new Location (feeder.feederGUI.getLocation());
 		newLocation.incrementX(50);
 		newLocation.incrementY(35);
 
 		receiveState = true;
 	}
 	
 	// Move to bin location
 	public void removeBin(Bin newBin) {
 		heldBin = newBin;
 		newLocation = heldBin.binGraphics.getLocation();
 		moveTo (newLocation);
 		removeState = true;
 	}
 	
 	// drop bin into feeder
 	public void dropBin (Bin newBin, FeederAgent feeder) {
 		server.sendData(new Request(Constants.GANTRY_ROBOT_DROP_BIN_COMMAND, Constants.GANTRY_ROBOT_TARGET, null));
 		gantryAgent.msgDropBinDone(newBin);
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
 				heldBin.binGraphics.setLocation(newLocation);
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
 				newLocation = heldBin.binGraphics.getInitialLocation();
 				moveTo (newLocation);
 				removeState = false;
 				removeState2 = true;
 			}
 			// Robot is above initial bin location, removes bin
 			else if (removeState2) {
 				server.sendData(new Request(Constants.GANTRY_ROBOT_DROP_BIN_COMMAND, Constants.GANTRY_ROBOT_TARGET, null));
 				gantryAgent.msgRemoveBinDone(heldBin);
 				removeState2 = false;
 			}
 		}
 	}
 
 	public void hereIsNewBin(Bin bin) {
 		binList.add(bin.binGraphics);
 		server.sendData(new Request(Constants.GANTRY_ROBOT_ADD_NEW_BIN, Constants.GANTRY_ROBOT_TARGET, new BinData(bin.binGraphics.getInitialLocation(), bin.part.type)));
 	}
 	
 	public void editBin(PartType pt) {
 		for (int i = 0; i < binList.size(); i ++) {
			if (binList.get(i).getPart().getPartType().equals(pt)) {
 				binList.get(i).getBin().part.type = pt;
 			}
 		}
 	}
 
 }
