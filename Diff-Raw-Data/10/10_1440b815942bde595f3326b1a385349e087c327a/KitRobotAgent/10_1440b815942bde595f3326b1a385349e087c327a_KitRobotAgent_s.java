 package agents;
 import agents.*;
 import agents.include.*;
 import agents.interfaces.*;
 import java.util.*;
 import java.util.concurrent.Semaphore; 
 import state.*;
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
     Factory_PartA factory;
 
     public List<Kit> kits;
 
     KitRobotState state;
     enum KitRobotState { none, waitingRequests, receivedEmpty, kitting }
 
     public int current = 2;
     public int numRequests = 0;
     
     String name;
     
     private Semaphore kitRobotInformPartRobotAccess = new Semaphore(0);
 
     /*** Constructor **/
     
     public KitRobotAgent(String name, Conveyor c, KitVision v, KitStand k, PartRobot p, Factory_PartA factory) {
         this.name = name;
     	this.conveyor = c;
         this.vision = v;
         this.kitStand = k;
         this.partRobot = p;
         this.factory = factory;
         this.kits = new ArrayList<Kit>();
         this.state = KitRobotState.waitingRequests;
         guiKitStand = factory.getGUIKitStand();
        guiKitRobot = new GUI_KitRobot(c.getGUIConveyor(), guiKitStand);
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
         print("received list of kits from conveyor");
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
             state = KitRobotState.none;
             processRequests();
             return true;
         }
 
         if(state == KitRobotState.receivedEmpty)
         {
             state = KitRobotState.none;
             putKitsOnStand();
             return true;
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
 
     private void processRequests()
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
             DoPlaceOnKitStand(kit);
             kitStand.insertEmptyKit(kit);
             conveyor.provideNextKit();
             print("guiRobot placed kit on stand.");
         }
         /*
         try {
 			kitRobotInformPartRobotAccess.acquire();
 			//removeing line below only for v0, place back for v1
 	        //partRobot.msgMakeKits();
 	        kits = new ArrayList<Kit>();
 	        stateChanged();
         } catch (InterruptedException e) {
         	System.out.println("kit robot sema4 messed up");
         }*/
         kits = new ArrayList<Kit>();
         stateChanged();
     }
 
     public Semaphore visionLock = new Semaphore(0);
     public Semaphore visionLock() { return visionLock; }
 
     private void inspectionProcess()
     {
         Kit kit = kitStand.removeCompleteKit();
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
         DoMoveFromInspectionToConveyor(kit);
         conveyor.msgHereIsCompleteKit(kit);
     
         stateChanged();
     }
     
     //extra
     public String getName(){ return name; }
 
     //GUI
     private void DoPlaceOnKitStand(Kit kit) { 
     	//guiKitRobot.DoPlaceOnKitStand(kit);
     	//guiKitStand.DoAddKit(kit);
     }
 
     private void DoMoveFromKitStandToInspection(Kit kit) { 
     	//guiKitStand.DoRemoveKit(kit);
     	//guiKitRobot.DoMoveKitToInspect(kit);
     	//guiKitStand.DoAddKitToInspection(kit);
     }
 
     private void DoMoveFromInspectionToConveyor(Kit kit) {
     	//guiKitStand.DoRemoveKit(kit);
     	//guiKitRobot.DoPlaceKitOnConveyor(kit);
     	print("gui placed complete and inspected kit on conveyor");
     }
 
 }
