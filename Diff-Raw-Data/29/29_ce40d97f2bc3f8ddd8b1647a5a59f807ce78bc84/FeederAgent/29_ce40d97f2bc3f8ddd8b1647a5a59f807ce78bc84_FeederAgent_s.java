 package agents;
 
 import agents.*;
 import agents.include.*;
 import agents.interfaces.*;
 import gui.GUI_Feeder;
 import gui.GUI_Part;
 
 import java.util.*;
 
 /*** DO METHODS ***
  *  doPurge()
  *  doHandPart() 
  * 
  **/
 
 
 
 
 public class FeederAgent extends PartsMoverAgent implements Feeder{
 	
 	//Constant defining the amount that the feeder can hold
 	private static final int FEEDER_SIZE = 200;
 	
     /*** Data Structures **/
 	
 	GUI_Feeder guiFeeder;
 	PartsTaker top;
 	PartsTaker bottom;
 
     /*** Constructor **/
 	
     public FeederAgent(String name, PartsTaker top, PartsTaker bottom) {
     	super(name, FEEDER_SIZE);
     	this.top = top;
     	this.bottom = bottom;
     }
     
 	/*** Messages ***/
 	
 	@Override
 	public void msgHereIsPart(String partType) {
 		super.msgHereIsPart(partType);
     	guiFeeder.setPart(new GUI_Part(new Part(partType)));
 	}
	    
     /*** Actions ***/
     
     @Override
     protected void doPurge() {
     	super.doPurge();
    	guiFeeder.setPart(new GUI_Part(null));
     }
     
     @Override
     protected void doHandPart() {
     	super.doHandPart();
    	if (holding == 0)
        	guiFeeder.setPart(new GUI_Part(null));
     }
 
 	@Override
 	public void partRemoved() {
 		guiFeeder.removePart();
 	}
 
 	@Override
 	public void partAdded() {
 		guiFeeder.setPart(new GUI_Part(new Part(currentPart)));
 		guiFeeder.setDiverter(receiver == top);
 		guiFeeder.addPart();
 	}
 
 	@Override
 	public void partsCleared() {
 		guiFeeder.purge(true);
 		guiFeeder.purge(false);
 	}
 	
 	public void setGuiFeeder(GUI_Feeder guiFeeder)
 	{
 		this.guiFeeder = guiFeeder; 
 	}
 }
 	
 	
