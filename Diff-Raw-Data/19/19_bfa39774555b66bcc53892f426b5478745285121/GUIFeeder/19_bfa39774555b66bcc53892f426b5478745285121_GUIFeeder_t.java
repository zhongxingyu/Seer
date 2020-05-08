 import java.awt.*;
 import java.awt.geom.Point2D;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 @SuppressWarnings("serial")
 /** Contains data and methods for drawing and animating a feeder */
 public class GUIFeeder implements GUIItem, Serializable {
 	/** used to access feeder data */
 	public Feeder feeder;
 	/** used to access movement data */
 	public Movement movement;
 	/** Initialization */
 	public GUIFeeder(Feeder feeder, double x, double y)
 	{
 		this.feeder = feeder;
 		movement = new Movement(new Point2D.Double(x,y), 0);
 	}
 	/** draws the feeder */
 	public void draw( Graphics2D g, long currentTime ){
 		Painter.draw(g, Painter.ImageEnum.FEEDER, currentTime, movement, true);
 	}
 
 	/** setter for movement */
 	public void setMove(Movement movement)
 	{
 		this.movement = movement;
 	}
 
 	/** getter for movement */
 	public Movement getMove() {
 		return movement;
 	}
 	
 	/** turns feeder on if x is true else it turns it off */
 	public void setFeederOn( boolean x ){
 		if( x ){
 			feeder.turnOn();
 		} else {
 			feeder.turnOff();
 		}
 	}
 	
 	/** raises gate if x is true else it lowers the gate */
 	public void setGateRaised( boolean x ){
 		if( x ){
 			feeder.raiseGate();
 		} else {
 			feeder.lowerGate();
 		}
 	}
 	
 	/** starts feeding if x is true else it stops feeding */
 	public void setFeeding( boolean x ){
 		if( x ){
 			feeder.startFeeding();
 		} else {
 			feeder.stopFeeding();
 		}
 	}
 	
 	/** changes diverter to alternate position */
 	public void setDiverterTop( boolean topLane ) {
 		feeder.setDiverterTop( topLane );
 	}
 
 	/** returns lane number that parts are fed to (1 = bottom, 2= top)*/
 	public boolean getDiverterTop(){
 		return feeder.getDiverterTop();
 	}
 
 	/** load parts into feeder */
 	public void loadFeeder( ArrayList<Part> load ){
 		feeder.loadFeeder(load);
 	}
 	
	/** empties the feeder into purge bin */
	public void purgeFeeder( Bin purged ){
		feeder.purgeFeeder( purged );
 	}
 
 	/** return part */
 	public Part getPart(){
 		return feeder.getPart();
 	}
 	
 	/** turn on feeder */
 	public void turnOn(){
 		feeder.turnOn();
 	}
 	
 	/** turn off feeder */
 	public void turnOff(){
 		feeder.turnOff();
 	}
 	
 	/** returns if the feeder is on */
 	public boolean isOn(){
 		return feeder.isOn();
 	}
 	
 	/** return if the feeder is feeding */
 	public boolean isFeeding(){
 		return feeder.isFeeding();
 	}
 	
 	/** return if feeder gate is lowered */
 	public boolean isGateRaised(){
 		return feeder.isGateRaised();
 	}
 	
 	/** returns number of parts fed */
 	public int partsFed(){
 		return feeder.partsFed();
 	}
 	
 	/** resets fedCount to 0 */
 	public void resetCount(){
 		feeder.resetCount();
 	}
 	
 	public boolean getPartsLow() {
 		return feeder.checkIfLow();
 	}
 }
