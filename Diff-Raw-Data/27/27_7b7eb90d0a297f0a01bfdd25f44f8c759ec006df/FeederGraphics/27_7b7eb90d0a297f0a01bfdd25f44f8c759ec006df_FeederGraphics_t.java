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
 
 	private Server server;
 	private Location location;
 	
 	private boolean haveBin;
 	
 	private int feederID;
 	private int partsFed;
 	private int partsRemaining;
 	
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
 		
 		// TODO edit location coordinates later
 		location = new Location(200, 100*feederID);
 		
 		// TODO send message to FeederGraphicsDisplay
 		server.sendData(new Request(Constants.FEEDER_INIT_GRAPHICS_COMMAND, Constants.FEEDER_TARGET, location));
 		
 	}
 	
 	/**
 	 * This function receives a bin.
 	 * @param bg BinGraphics passed in by the Agent
 	 */
	public void receiveBin(BinGraphics bg) {
 		partsRemaining = bg.getQuantity();
 		partGraphics = bg.getPart();
 		haveBin = true;
 	}
 	
 	/**
 	 * This function purges the bin.
 	 * @param bg
 	 */
	public void purgeBin(BinGraphics bg) {
 		partsFed = 0;
 		partsRemaining = 0;
 		haveBin = false;
 	}
 	
 	/**
 	 * This function determines if the part is low.
 	 * @return partsRemaining is less than PARTS_LOW_THRESHOLD
 	 */
 	boolean isPartLow() {
 		return (partsRemaining < PARTS_LOW_THRESHOLD);
 	}
 	
 	/**
 	 * This function moves a part to the lane.
 	 */
	public void movePartToLane(PartGraphics pg) {
 		
 	}
 	
 	/**
 	 * This function moves a part to the diverter.
 	 * @param pg
 	 */
	public void movePartToDiverter(PartGraphics pg) {
 		
 	}
 	
 	/**
 	 * This function flips the diverter.
 	 */
	public void flipDiverter() {
		// TODO do we need to animate this
 	}
 	
 	
 }
