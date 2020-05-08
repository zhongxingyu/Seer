 import java.io.*;
 import java.util.ArrayList;
 
 /** class constructs basic functionality of feeder */
 public class Feeder implements Serializable {
 	/** true if parts go to top lane, false otherwise */
 	private boolean diverter;
 	/** true if parts are low */
 	private boolean partsLow;
 	/** true if gate is lowered */
 	private boolean gateRaised;
 	/** true if parts are being fed */
 	private boolean feedParts;
 	/** true if feeder is on */
 	private boolean imOn;
	/** arraylist of parts that are loaded into feeder */
 	private ArrayList<Part> parts;
 	/** counts number of parts fed */
 	private int fedCount;
 
 	/** Initialize variables */
 	public Feeder(){
 		diverter = false;
 		partsLow = true;
 		gateRaised = true;
 		feedParts = false;
 		imOn = false;
 		parts = new ArrayList<Part>();
 		fedCount = 0;
 	}
 	/** set partsLow true */
 	public void setPartsLow(){
 		partsLow = true;
 	}
 
 	/** set partsLow false */
 	public void setPartsUnlow(){
 		partsLow = false;
 	}
 
 	/** returns whether parts are low */
 	public boolean checkIfLow(){
 		return partsLow;
 	}
 
 	/** flip boolean diverter */
 	public void changeLane(){
 		diverter = !diverter;
 	}
 	
 	/** getter for diverter */
 	public boolean getDiverterTop() {
 		return diverter;
 	}
 	
 	/** change lane that parts are fed to */
 	public void setDiverterTop( boolean topLane ) {
 		if ( !diverter && topLane || diverter && !topLane )
 			changeLane();
 	}
 
 	/** returns lane number that parts are fed to (1 = bottom, 2= top) */
 	public int getLane(){
 		if( diverter ) {
 			return 2;
 		} else {
 			return 1;
 		}
 	}
 
 	/** load parts into feeder */
 	public void loadFeeder( ArrayList<Part> load ){
 		parts = load;
 	}
 	
	/** empties the feeder into purge bin */
	public void purgeFeeder( Bin purged ){
		purged.fillBin( parts.get(0), parts.size() - 1 );
 		parts = new ArrayList<Part>();
 	}
 
 	/** return part and increments fedCount*/
 	public Part getPart(){
 		if( parts.size() > 0 ){
 			fedCount++;
 			return parts.remove( parts.size() - 1 );
 		} else {
 			return null;
 		}
 	}
 	
 	/** raise the gate, sets gateRaised to false */
 	public void raiseGate(){
 		gateRaised = true;
 	}
 	
 	/** lower the gate, sets gateRaised to true */
 	public void lowerGate(){
 		gateRaised = false;
 	}
 	
 	/** returns if the gate is lowered */
 	public boolean isGateRaised(){
 		return gateRaised;
 	}
 	
 	/** start feeding parts, sets feedParts to true */
 	public void startFeeding(){
 		feedParts = true;
 	}
 	
 	/** stop feeding parts, set feedParts to false */
 	public void stopFeeding(){
 		feedParts = false;
 	}
 	
 	/** returns if the feeder is feeding parts */
 	public boolean isFeeding(){
 		return feedParts;
 	}
 	
 	/** turn on feeder */
 	public void turnOn(){
 		imOn = true;
 		System.out.println( "" + imOn );
 	}
 	
 	/** turn off feeder */
 	public void turnOff(){
 		imOn = false;
 		System.out.println( "" + imOn );
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
 }
