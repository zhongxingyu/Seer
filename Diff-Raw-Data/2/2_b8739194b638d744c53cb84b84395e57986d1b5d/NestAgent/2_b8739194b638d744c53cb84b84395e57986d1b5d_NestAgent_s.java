 package agents;
 import agents.*;
 import agents.include.*;
 import agents.interfaces.*;
 import gui.GUI_Nest;
 
 import java.util.*;
 
 public class NestAgent extends Agent implements Nest {
 
     /*** Data Structures **/
 	
 	private String name;
     private GUI_Nest guiNest;
     private int requested;
     private int needed;
     private String partType;        
     private List<Part> parts; // parts in the Nest
     private List<Part> sendParts; // parts to send
     private PartRobot partRobot;
     private PartVision vision;
     private NestState nestState;
     //private Lane lane;
     enum NestState { none, inactive, requestReceived, requested, checking, checked }
     
     /*** Constructor **/
     
     // need Lane lane in constructor too for v1+
     public NestAgent(String name, String partType, PartRobot partRobot, GUI_Nest guiNest, PartVision vision) {
         this.name = name;
     	this.requested = 0;
         this.needed = 0;
         this.partType = partType;
         this.parts = new ArrayList<Part>();
         this.partRobot = partRobot;
         this.nestState = NestState.inactive;
         this.guiNest = guiNest;
         this.vision = vision;
         //this.lane = lane;
     }
 
     /*** Messages / Public API ***/
 
     /*  (PartRobot) Message to ask NestAgent to get parts! 
      *  -> assign part type and possibly flush parts too
      */
     public void msgRequestParts(String type, int count) {
     
     	// assign type and flush
         if(!partType.equals(type)) 
         { 
             DoFlushParts(type);
             parts = new ArrayList<Part>();
             partType = type;
             print("Assigned with type " + partType);
         }
         requested = count;  
         nestState = NestState.requestReceived;
 
         stateChanged();
     }
 
     /*  (PartRobot) Individual parts coming from lanes */
     public void msgHereIsPart(String p) {
         parts.add(new Part(p));
         DoPutPartArrivedAtNest(new Part(p)); 
         stateChanged();
     }
 
     
     public void msgHereIsPart(Part p) {
     	parts.add(p);
     	DoPutPartArrivedAtNest(p); 
     	stateChanged();
     }
     /*  (PartVisionAgent) Confirmation that parts are good */
     public void msgPartsAreGood(List<Part> visionParts)
     {
     	print("Received confirmation that parts are good");
         sendParts = visionParts;
         nestState = NestState.checked;
         stateChanged();
     }
     
     /* Getter for part type */
     public String getPartType()
     {
         return partType;
     }
     
     /* Getter for name */
     public String getName() {
         return name;
     }    
     
     /* Getter for GUI_Nest */
     public GUI_Nest guiNest() { return guiNest; }
 
     /*** Scheduler ***/
     
     public boolean pickAndExecuteAnAction() {
 
     	// request to lanes
         if(nestState == NestState.requestReceived)
         {
             int current = parts.size();
             needed = requested - current;
             requestParts(needed);
             needed = 0;
             nestState = NestState.requested;
             return true;
         }
 
         // check parts once we have requested
         if(nestState == NestState.requested)
         {
             if(parts.size() >= requested)
             {
                 checkParts();
                 nestState = NestState.checking;
             }
             return true;
         }
 
         // once checked, send and become inactive
         if(nestState == NestState.checked)
         {
             sendParts();
             nestState = NestState.inactive;
             return true;
         }
 
         return false;
     }
 
     // requests parts to the lane
     private void requestParts(int num)
     {
         if(num > 0)
         {
            //lane.msgRequestParts(partType,num); v1
         }
         stateChanged();
     }
 
     // check parts with the vision agent
     private void checkParts()
     {
         List<Part> visionParts = new ArrayList<Part>();
 
         for(int i=0; i<requested; i++)
         {
             visionParts.add(parts.get(i));
         }
         for(int i=0; i<requested; i++)
         {
             parts.remove(0);
         }
         requested = 0;
 
         print("Sending request to check parts");
         vision.msgVerifyParts(visionParts, this);
         stateChanged();
     }
 
     // send parts to partrobot to process
     private void sendParts()
     {
         partRobot.msgHereAreParts(sendParts, this);
         sendParts = new ArrayList<Part>();
     }
 
     private void DoFlushParts(String type)
     {
         // also case when nest isn't initialized yet
         // tell gui to flush parts and change GuiNest part type
         guiNest.setPartHeld(type);
         stateChanged();
     }
 
     private void DoPutPartArrivedAtNest(Part p)
     {
         // call gui to put parts on the nest
         guiNest.doPutPartArrivedAtNest(p);
         stateChanged();
     }
     
 }
