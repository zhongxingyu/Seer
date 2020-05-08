 import java.io.*;
 import java.util.ArrayList;
 
 /** class constructs basic functionality of feeder */
 public class Feeder implements Serializable {
 	/** milliseconds between feeding each part */
 	public static final long FEED_INTERVAL = 500;
 	private final int LOW = 10;
 
 	/** -1 if parts go to top lane, 1 if parts go to bottom lane */
 	private int diverter;
 	/** true if parts are low */
 	private boolean partsLow;
 	/** true if gate is lowered */
 	private boolean gateRaised;
 	/** true if parts are being fed */
 	private boolean feeding;
 	/** true if feeder is on */
 	private boolean imOn;
 	/** arraylist of parts that are loaded into feeder */
 	private ArrayList<Part> parts;
 	/** counts number of parts fed */
 	private int fedCount;
 	/** time that last part was fed */
 	private long feedTime;
 	/** Bin placed behind the feeder for purging */
 	private Bin purgeBin;
 
 	/** Initialize variables */
 	public Feeder(){
 		diverter = -1;
 		partsLow = true;
 		gateRaised = true;
 		feeding = false;
 		imOn = true;
 		parts = new ArrayList<Part>();
 		fedCount = 0;
 		feedTime = 0;
 		purgeBin = new Bin( new Part(), 0 );
 	}
 
 	/** returns whether parts are low */
 	public boolean checkIfLow(){
 		return partsLow;
 	}
 
 	/** flip boolean diverter */
 	public void changeLane(){
 		diverter *= -1;
 	}
 	
 	/** getter for diverter */
 	public int getDiverter() {
 		return diverter;
 	}
 	
 	/** change lane that parts are fed to */
 	public void setDiverter( int newDiverter ) {
 		diverter = newDiverter;
 	}
 	
 	public void setPurgeBin(Bin purgeBin)
 	{
 		this.purgeBin = purgeBin;
 	}
 	
 	public Bin getPurgeBin(){
 		return purgeBin;
 	}
 
 	/** load parts into feeder */
 	public void loadParts( ArrayList<Part> load ){
 		parts.addAll(load);
 		if (!gateRaised) purge(purgeBin);
 		if( parts.size() > LOW ){
 			partsLow = false;
 		}
 	}
 
 	/** load bin into feeder */
 	public void loadBin(Bin load) {
 		// Purge the current parts if they're a different type
 		if (parts.size() > 0 && !load.part.equals(parts.get(0)))
 		{
 			// Purge to the purgeBin if there is one, otherwise, dump the parts into the abyss
 			if (purgeBin != null)
 				purge(purgeBin);
 			else
 				parts.clear(); // No purgeBin set - bye bye, parts
 		}
 
 		for (int i = 0; i < load.getNumParts(); i++) {
 			parts.add(load.part);
 		}
 				
 		if (!gateRaised) purge(purgeBin);
 		if( parts.size() > LOW ){
 			partsLow = false;
 		}
 	}
 	
 	/** empties the feeder into purge bin */
 	public void purge( Bin purgeBin ){
		if (parts.isEmpty()) return;
 		purgeBin.fillBin( parts.get(0), parts.size() );
 		parts.clear();
 	}
 
 	/** return part and increments fedCount*/
 	public Part feedPart(long currentTime){
 		if( parts.size() > LOW ){
 			partsLow = false;
 		} else {
 			partsLow = true;
 		}
 		if( parts.size() > 0 ){
 			fedCount++;
 			feedTime = currentTime;
 			return parts.remove( 0 );
 		} else {
 			return null;
 		}
 	}
 
 	/** returns whether it is time to feed the next part */
 	public boolean shouldFeed(long currentTime) {
 		return imOn && feeding && parts.size() > 0 && currentTime >= feedTime + FEED_INTERVAL;
 	}
 	
 	/** raise the gate, sets gateRaised to false */
 	public void raiseGate(){
 		gateRaised = true;
 	}
 	
 	/** lower the gate, sets gateRaised to true */
 	public void lowerGate(){
 		gateRaised = false;
 		purge(purgeBin);
 	}
 	
 	/** returns if the gate is lowered */
 	public boolean isGateRaised(){
 		return gateRaised;
 	}
 	
 	/** start feeding parts */
 	public void startFeeding(){
 		feeding = true;
 	}
 	
 	/** stop feeding parts */
 	public void stopFeeding(){
 		feeding = false;
 	}
 	
 	/** returns if the feeder is feeding parts */
 	public boolean isFeeding(){
 		return feeding;
 	}
 	
 	/** turn on feeder */
 	public void turnOn(){
 		imOn = true;
 	}
 	
 	/** turn off feeder */
 	public void turnOff(){
 		imOn = false;
 	}
 	
 	/** returns if the feeder is on */
 	public boolean isOn(){
 		return imOn;
 	}
 	
 	/** returns number of parts fed */
 	public int partsFed(){
 		return fedCount;
 	}
 	
 	/** resets fedCount to 0 */
 	public void resetCount(){
 		fedCount = 0;
 	}
 	
 	public ArrayList<Part> getParts() {
 		return parts;
 	}
 }
