 package agents;
 
 import agents.include.*;
 import agents.interfaces.*;
 import java.util.*;
 import java.util.concurrent.Semaphore; 
 import state.*;
 import gui.GUI_Conveyor;
 import gui.GUI_KitRobot;
 import gui.GUI_KitStand;
 
 public class KitRobotAgent extends Agent implements KitRobot {
 
     /*** Data Structures **/
 
     Conveyor conveyor;
     KitVision vision;
     KitStand kitStand;
     PartRobot partRobot;
     public GUI_KitRobot guiKitRobot;
     public GUI_KitStand guiKitStand;
     FactoryState factory;
     
     boolean testing;
 
     public List<Kit> kits;
 
     KitRobotState state;
     enum KitRobotState { none, waitingRequests, receivedEmpty, kitting }
 
     public int current = 2;
     public int numRequests = 0;
     
     String name;
     
     //private Semaphore kitRobotInformPartRobotAccess = new Semaphore(0);
 
     /*** Constructor **/
     
     public KitRobotAgent(String name, Conveyor c, KitVision v, KitStand k, PartRobot p, FactoryState factoryState) {
         this.name = name;
     	this.conveyor = c;
         this.vision = v;
         this.kitStand = k;
         this.partRobot = p;
         this.factory = factoryState;
         this.kits = new ArrayList<Kit>();
         this.state = KitRobotState.waitingRequests;
         //guiKitStand = factoryState.guiKitStand;
         //guiKitRobot = factoryState.guiKitRobot;
         testing = false;
     }
     
     //constructor2 for PartA unit testing
     public KitRobotAgent(String name, Conveyor c, KitVision v, KitStand k, PartRobot p) {
         this.name = name;
     	this.conveyor = c;
         this.vision = v;
         this.kitStand = k;
         this.partRobot = p;
         this.kits = new ArrayList<Kit>();
         this.state = KitRobotState.waitingRequests;
         //guiKitStand = new GUI_KitStand(125, 100);
         //guiKitRobot = new GUI_KitRobot(new GUI_Conveyor(), guiKitStand);
         testing = true;
     }
 
     /*** Messages **/
 
     /*  Message to request to deliver empty kit
      *  Source: Conveyor
      */ 
     public void msgRequestMoveKit() {
         numRequests++;
         print("received request from conveyor to grab kits");
         stateChanged();
     }
 
     /*  Message to deliver empty kit
      *  Source: Conveyor
      */ 
     public void msgHeresKits(List<Kit> k) {
         kits.addAll(k);
         state = KitRobotState.receivedEmpty;
         current = k.size();
         //kitRobotInformPartRobotAccess.release();
         print("received list of kit(s) from conveyor holding " + current + " kits.");
         stateChanged();
     }
 
     /*  Message that kits have been made
      *  Source: PartRobot
      */ 
     public void msgKitIsDone() {
         state = KitRobotState.kitting;
         print("PartRobot filled kits on stand");
         stateChanged();
     }
 
     /*** Scheduler ***/
 
     public boolean pickAndExecuteAnAction() {
 
         if(state == KitRobotState.waitingRequests && numRequests > 0)
         {
         	//print("race condition testing:  about to request empty kit from conveyor");
             state = KitRobotState.none;
             processRequests();
             return true;
         }
 
         if(state == KitRobotState.receivedEmpty)
         {
             state = KitRobotState.none;
             putKitsOnStand();
             print("waiting for parts robot to fill kits");
             return true;
             
 	            /*  Factory v0 only
 	    		try {
 	  			  Thread.sleep(5000);    // 5 seconds
 	  			}
 	  			catch (Exception e) {}
 	            
 	            //hack to make Factory_PartA receive message from PartsRobot.
 	            //integration for 4.1 will not require this.
 	    		msgKitIsDone();
 	             */         
         }
         if(state == KitRobotState.kitting)
         {       
             inspectionProcess();
             current--;
             if(current == 0) state = KitRobotState.waitingRequests;
             else state = KitRobotState.none;
             return true;
         }
 
         return false;
     }
     /*** Actions ***/
 
     synchronized private void processRequests()
     {
         // processRequests
         if(numRequests == 1) 
         {
             current = 1;
             numRequests = 0;
             conveyor.msgGiveMeKits(1);
         }
         else // numRequests >=2 
         {
             current = 2;
             numRequests -= 2;
             conveyor.msgGiveMeKits(2);
         }
         stateChanged();
     }
 
     private void putKitsOnStand()
     {
         for(Kit kit : kits)
         {
         	if(!testing)
             DoPlaceOnKitStand(kit);
             kitStand.insertEmptyKit(kit);
             conveyor.provideNextKit();
             print("guiRobot placed kit on stand.");
             //if(testing)
             	partRobot.msgMakeKits();
         }
 
         kits = new ArrayList<Kit>();
         stateChanged();
     }
 
     public Semaphore visionLock = new Semaphore(0);
     public Semaphore visionLock() { return visionLock; }
 
     private void inspectionProcess()
     {
     	Kit kit;
     	if(testing)
     		kit = kitStand.removeCompleteKit();
     	else
 	    	//hack to bypass partsRobot
 	    	kit = guiKitStand.getTempKit();
     	if(!testing)
    	DoMoveFromKitStandToInspection(kit);	
         kitStand.placeInspection(kit);
         print("gui placed kit from filling to inspection");
 
         vision.msgKitReadyForInspection();
         try {
             visionLock.acquire();
         }
         catch(Exception ex) { print("fml"); }
 
         // after release
         kitStand.removeInspection(kit);
         if(!testing)
         	DoMoveFromInspectionToConveyor(kit);
         conveyor.msgHereIsCompleteKit(kit);
     
         stateChanged();
     }
     
     //setters
     public void setGUIKitRobot(GUI_KitRobot r){
     	guiKitRobot = r;
     }
     public void setGUIKitStand(GUI_KitStand stand){
     	guiKitStand = stand;
     }
     
     //extra
     public String getName(){ return name; }
 
     public Semaphore conveyor2standAnimmLock = new Semaphore(0);
     public void conveyor2StandLockRelease(){ conveyor2standAnimmLock.release(); }
     public Semaphore displayKitOnStandLock = new Semaphore(0);
     public void kitOnStandRelease() { displayKitOnStandLock.release(); }
     
     //GUI
     private void DoPlaceOnKitStand(Kit kit) { 
     	
     	guiKitRobot.DoPlaceOnKitStand(kit, this);
     	try {
 			conveyor2standAnimmLock.acquire();
 		} catch (InterruptedException e) {
 			print("fack");
 		}
     	guiKitStand.DoAddKit(kit, this);
     	try {
     		displayKitOnStandLock.acquire();
     	} catch (InterruptedException e) {
 			print("fack2");
 		}
     }
 
     public Semaphore inspectLock = new Semaphore(0);
     public void inspectLockRelease(){ inspectLock.release(); }
     
     private void DoMoveFromKitStandToInspection(Kit kit) { 
     	guiKitRobot.DoMoveFromKitStandToInspection(kit, this);
     	guiKitStand.DoRemoveKit(kit);
     	try {
 			inspectLock.acquire();
 		} catch (InterruptedException e) {
 			print("Shiet!");
 		}
     	guiKitStand.DoAddKitToInspection(kit);
     }
 
     private void DoMoveFromInspectionToConveyor(Kit kit) {
     	guiKitStand.DoRemoveKit(kit);
     	guiKitRobot.DoMoveFromInspectionToConveyor(kit);
     }
     public GUI_KitStand getGUIKitStand(){ return guiKitStand; }
 
 }
