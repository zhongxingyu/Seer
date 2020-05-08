 package agents;
 
 import agents.*;
 import agents.include.*;
 import agents.interfaces.*;
 import gui.GUI_GantryRobot;
 
 import java.util.*;
 import java.util.concurrent.Semaphore; 
 
 /*** DO METHODS ***
  * 
  *  doMakeRequest(int count) 
  *  doFill(Request r)
  * 
  **/
 
 
 public class GantryAgent extends PartsMoverAgent implements Gantry {
 
     /*** Data Structures **/
 	
 	GUI_GantryRobot guiGantry;
 	
     /*** Constructor **/
 	
     public GantryAgent(String name) {
     	super(name, 40);
     }
     
     /*** Messages ***/
     
     // Handled by superclass
 
     /*** Scheduler ***/
     
     // Handled by superclass
     
     /*** Actions ***/
     
     @Override
     protected void doMakeRequest(int count) {
     	//Automatically fill with parts for now until integration with controller and bins
     	
    	holding = count+(16-count%16);
     	guiGantry.goToBins();
     }
 
     @Override
     protected void doFill(Request r) {
     	super.doFill(r);
     	print("FILLING");
     	try{
     	FeederAgent f = (FeederAgent)receiver; 
     	guiGantry.setFeeder( f.guiFeeder );}
     	catch (Exception e) {}
     }
     
 	@Override
 	public void partRemoved() {
 		//We started dumping into feeder, finish that by continuing to be ready to move until all parts are dumped
 		if (holding > 0) msgReadyToMove();
 		//Then tell the gui component to display no bin
 		else guiGantry.dumped();
 	}
 
 	@Override
 	public void partAdded() {
 		//Never called because requests are not filled externally
 	}
 
 	@Override
 	public void partsCleared() {
 		guiGantry.dumped();
 	}
     
 	public void setGuiGantry(GUI_GantryRobot guiGantry)
 	{
 		this.guiGantry = guiGantry; 
 	}
 	
 	
 }
