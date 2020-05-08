 package DeviceGraphicsDisplay;
 
 import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
 
 import javax.swing.JComponent;
 
import agent.data.PartType;


 import Networking.Request;
 import Utils.Constants;
 import Utils.Location;
 
 /**
  * This class handles drawing of the bin.
  * @author Chris Gebert and Harry Trieu
  */
 public class BinGraphicsDisplay extends DeviceGraphicsDisplay {
 	// current location of the bin
 	Location binLocation;
 	// type of part in the bin
 	PartType partType;
 	// full status of the bin
 	boolean isFull;
 	
 	/**
 	 * Constructor
 	 * @param loc location of bin
 	 * @param pt type of part in the bin
 	 */
 	public BinGraphicsDisplay (Location loc, PartType pt) {
 		binLocation = loc;
 		partType = pt;
 	}
 	
 	/**
 	 * Sets the location of the bin
 	 * @param newLoc the new location of the bin
 	 */
 	public void setLocation (Location newLoc) {
 		binLocation = newLoc;
 	}
 	
 	/**
 	 * @returns the location of the bin
 	 */
 	public Location getLocation () {
 		return binLocation;
 	}
 	
 	/**
 	 * @returns the type of part in the bin
 	 */
 	public PartType getPartType() {
 		return partType;
 	}
 	
 	/**
 	 * Handles drawing of the bin components
 	 */
 	public void draw (JComponent c, Graphics2D g) {
 		if (isFull)
 			g.drawImage(Constants.BIN_FULL_IMAGE, binLocation.getX(), binLocation.getY(), c);
 		else
 			g.drawImage(Constants.BIN_EMPTY_IMAGE, binLocation.getX(), binLocation.getY(), c);
 	}
 	
 	/**
 	 * @param f new full status of the bin
 	 */
 	public void setFull (boolean f) {
 		isFull = f;
 	}
 
 	/**
 	 * Inherited method
 	 */
 	public void receiveData(Request req) {
 		// unused for bin
 	}
 }
