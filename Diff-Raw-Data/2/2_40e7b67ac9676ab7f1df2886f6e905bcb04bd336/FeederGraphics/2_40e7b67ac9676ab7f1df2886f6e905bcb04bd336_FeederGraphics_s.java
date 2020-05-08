 package DeviceGraphics;
 
 import Networking.Request;
 import Networking.Server;
 import Utils.Constants;
 import Utils.Location;
 
 /**
  * This class handles the logic for the feeder animation.
  * @author Harry Trieu
  *
  */
 
 public class FeederGraphics extends DeviceGraphics implements GraphicsInterfaces.FeederGraphics {
 	// TODO ask 201 team what should be the threshold
 	private static final int PARTS_LOW_THRESHOLD = 2;
 	
 	// a reference to the server
 	private Server server;
 	
 	// private Location location;
 		
 	// true if the diverter is pointing to the top lane
 	private boolean diverterTop;
 	
 	// the feeder's unique ID
 	private int feederID;
 	
 	// number of parts fed
 	private int partsFed;
 	
 	// number of parts left to be fed
 	private int partsRemaining;
 	
 	// a part
 	private PartGraphics partGraphics;
 	
 	/**
 	 * This is the constructor.
 	 * @param id the unique ID of the feeder (there will be 4 feeders so we need to uniquely identify them)
 	 * @param myServer a reference to the Server
 	 */
 	public FeederGraphics(int id, Server myServer) {
 		id = feederID;
 		server = myServer;
 		partsFed = 0;
 		partsRemaining = 0;
 		
 		// TODO diverter starts on the top lane
 		diverterTop = true; // this means it is currently pointing at the top lane
 		
 		// TODO edit location coordinates later
 		// location = new Location(200, 100*feederID);
 		
 		// TODO send message to FeederGraphicsDisplay
 		// server.sendData(new Request(Constants.FEEDER_INIT_GRAPHICS_COMMAND, Constants.FEEDER_TARGET, location));
 	}
 	
 	/**
 	 * This function receives a bin.
 	 * @param bg BinGraphics passed in by the Agent
 	 */
 	public void receiveBin(BinGraphics bg) {
 		partsRemaining = bg.getQuantity();
 		partGraphics = bg.getPart();
 				
 		// TODO someone else draw bin on top of feeder
 	}
 	
 	
 	/**
 	 * This function purges the bin.
 	 * @param bg
 	 */
 	public void purgeBin(BinGraphics bg) {
 		partsFed = 0;
 		partsRemaining = 0;
 	
 		// TODO animate?
 	}
 	
 	/**
 	 * This function determines if the part is low.
 	 * @return partsRemaining is less than PARTS_LOW_THRESHOLD
 	 */
 	public boolean isPartLow() {
 		return (partsRemaining < PARTS_LOW_THRESHOLD);
 	}
 	
 	/**
 	 * This function moves a part to the diverter.
 	 * @param pg
 	 */
 	public void movePartToDiverter(PartGraphics pg) {
 		if (diverterTop) { // if diverter is pointing to the top lane
 			// TODO who draws the part moving? I change its coordinates
 		}
 	}
 	
 	/**
 	 * This function moves a part to the lane.
 	 */
 	public void movePartToLane(PartGraphics pg) {
 		pg.getLocation().incrementX();
 		
 		partsRemaining--;
 		partsFed++;
 		
 		// TODO who draws the part moving?  I change its coordinates
 		
 		// server.sendData(new Request(Constants.FEEDER_MOVE_TO_LANE, Constants.PART_TARGET + ":" + feederID, pg.getLocation()));
 	}
 	
 	/**
 	 * This function flips the diverter.
 	 */
 	public void flipDiverter() {
 		diverterTop = !diverterTop; 
 		
 		// TODO do we need to animate this
		server.sendData(new Request(Constants.FEEDER_FLIP_DIVERTER_COMMAND, Constants.FEEDER_TARGET + ":" + feederID, null));
 	}
 
 	@Override
 	public void receiveData(Request req) {
 		if (req.getCommand().equals("Testing")) {
 			flipDiverter();
 		}
 		
 	}
 }
