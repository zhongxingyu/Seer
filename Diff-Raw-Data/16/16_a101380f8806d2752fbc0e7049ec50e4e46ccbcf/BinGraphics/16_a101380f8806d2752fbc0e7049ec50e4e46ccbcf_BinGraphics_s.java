 package DeviceGraphics;
 
 import java.awt.Toolkit;
 
 import Utils.Location;
 
 public class BinGraphics extends DeviceGraphics  {
 	
	PartGraphics partType; // Type of part found in bin
 	int partNumber; // Number of parts in bin
 	Location binLocation;
 	
 	// Constructor
 	public BinGraphics (PartGraphics parts, int partNum) {
		partType = parts;
 		partNumber = partNum;
 	}
 	
 	
 	/**
 	 * Used in order to receive parts from a feeder's purge
 	 * 
 	 * @param parts - Part type
 	 * @param partNum - Number of parts
 	 */
 	public void receiveParts(PartGraphics parts, int partNum) {
		partType = parts;
 		partNumber = partNum;
 	}
 	
 	
	public PartGraphics getPartType() {
		return partType;
 	}
 	
 	
 	public int getQuantity() {
 		return partNumber;
 	}
 	
 	/**
 	 * Empties out the bin during purge
 	 */
 	public void setEmpty() {
 		partNumber = 0;
		partType = null;
 	}
 	
 	
 	public void setLocation(Location newLocation) {
 		binLocation = newLocation;
 	}
 	
 	
 	public Location getLocation() {
 		return binLocation;
 	}
 }
