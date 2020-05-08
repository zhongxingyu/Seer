 package agents;
 import agents.include.*;
 import agents.interfaces.*;
 import gui.GUI_Nest;
 import java.util.*;
 import java.util.concurrent.Semaphore;
 import state.FactoryState;
 
 public class NestAgent extends Agent implements Nest, PartsTaker {
 
     /*** Data Structures **/
 	
     private NestAgent itself = this;
 	private String name;
 	private int num;
     private GUI_Nest guiNest;
     private int requested;
     private int needed;
     public String partType;        
     public List<Part> parts; // parts in the Nest
     private List<Part> sendParts; // parts to send
     private PartRobot partRobot;
     private PartVision vision;
     public NestState nestState;
     private int capacity = 10;
     public Lane lane;
     public enum NestState { none, inactive, requestReceived, requested, checking, checked, partsBad, goodbye }  
     
     /* Non-Normative State Variables */
     private int partsMissingCount; 
     private boolean partsMissing;
     private boolean executePartsMissing;
     public boolean partsClumped;
     public boolean partsBad;
     public boolean partsUnstable;
     private boolean photobomb;
     private boolean cameraError;
     private java.util.Timer timer = new java.util.Timer();
 
     /*** Getters ***/
 
     public String getName() { return name; }    
     public int getNum() { return num; }
     public GUI_Nest guiNest() { return guiNest; }
     public String getPartType() { return partType; }
 
     /*** Constructor **/
     
     // need Lane lane in constructor too for v1+
     public NestAgent(String name, int num, String partType, PartRobot partRobot, GUI_Nest guiNest, PartVision vision, Lane lane) {
         this.name = name;
         this.num = num;
     	this.requested = 0;
         this.needed = 0;
         this.partType = partType;
         this.parts = new ArrayList<Part>();
         this.partRobot = partRobot;
         this.nestState = NestState.inactive;
         this.guiNest = guiNest;
         this.vision = vision;
         this.lane = lane;
         this.partsMissing = false;
         this.executePartsMissing = false;
         this.partsMissingCount = 0;
         this.partsClumped = false;
         this.partsBad = false;
         this.partsUnstable = false;
         this.photobomb = false;
         this.cameraError = false;
     }
 
     /*** Messages / Public API ***/
     
     /* (PartRobot) Message to ask NestAgent to get parts! 
      * -> assign part type and possibly flush parts too
      */
     public void msgRequestParts(String type, int count) {
     
     	// assign type and flush
         if(!partType.equals(type)) 
         { 
         	// don't flush gui...if unit testing
             if(guiNest !=null) DoFlushParts(type);
             parts = new ArrayList<Part>();
             partType = type;
             print("Assigned with type " + partType);
         }
         requested += count;  
         nestState = NestState.requestReceived;
         if(count == 0)
         {
             nestState = NestState.goodbye;
             System.out.println("Error: can't request nothing");
         }
         stateChanged();
     }
     
     /* (Lane) Individual parts coming from lanes */
     public void msgHereIsPart(String p) {
     	partsMissing = false;
     	executePartsMissing = false;
     	partsMissingCount = 0;
     	Part part = new Part(p);
         parts.add(part);
         if(guiNest != null) DoPutPartArrivedAtNest(part); 
         stateChanged();
     }
     
     /* (PartVision) Confirmation that parts are good */
     public void msgPartsAreGood(List<Part> visionParts)
     {
     	print("Received confirmation that parts are good");
     	FactoryState.out.println("Received confirmation that parts are good");
 
         sendParts = visionParts;
         nestState = NestState.checked;
         stateChanged();
     }
     
     /* (PartVision) Message that parts are bad and need to be flushed */
     public void msgPartsAreBad()
     {
        	print("Received confirmation that parts are bad");
     	FactoryState.out.println("Received confirmation that parts are bad");
 
         sendParts = new ArrayList<Part>();
         nestState = NestState.partsBad;
         stateChanged();	
     }
     
     /* (PartVision) Message that camera is bad  */
     public void msgPartsAreCameraError()
     {
        	print("Camera error");
     	FactoryState.out.println("Camera error");
 
         sendParts = new ArrayList<Part>();
     	nestState = NestState.none;	
 
     	timer.schedule(new TimerTask(){
     		public void run() {		
 
     			print("Camera fixed");
                 cameraError = false;
                 nestState = NestState.requested;
 	    		stateChanged();
     		}
     	}, (int)(3000));   
     }
     
     /* (PartVision) Message that parts are unstable and need to be wait */
     public void msgPartsAreUnstable()
     {
        	print("Received confirmation that parts are unstable");
     	FactoryState.out.println("Received confirmation that parts are unstable");
 
         sendParts = new ArrayList<Part>();
         nestState = NestState.none;
         
         if(guiNest != null)
         {
         	timer.schedule(new TimerTask(){
         		public void run() {		
 
         	    	FactoryState.out.println("Nest " + num + " now stable");
         	        guiNest.doSetUnstable(false);
 
         		}
         	}, (int)(2000));    
             
             
         	timer.schedule(new TimerTask(){
         		public void run() {		
 
         			partsUnstable = false;
                     nestState = NestState.requested;
     	    		stateChanged();
         		}
         	}, (int)(2000));  
         }
         else
         {
  			partsUnstable = false;
             nestState = NestState.requested;
     		stateChanged();
         }
    
     }
     
     /*** Semaphores for Flushing Parts Multi-Action ***/
     
     private Semaphore stopLane = new Semaphore(0);
     private Semaphore flushParts = new Semaphore(0);
     public void msgDoStoppedLane() { stopLane.release(); }
     public void msgDoneFlushParts() { flushParts.release(); }
     
     /*** Non-Normative Driver Messages ***/
     
     /* (PartVision) Drives the Parts Missing Non-Normative
      * -> after taking first bad picture */
     public void msgPartsMissing()
     {
     	partsMissing = true;
     	executePartsMissing = true;	
     	partsMissingCount = 0;
     	stateChanged();
     }
     
     /* (GUI NN Panel) Drives the Camera Error Non-Normative
      * -> after taking first bad picture */
     public void msgCameraError()
     {
     	print("Received camera error non-normative");
     	FactoryState.out.println("Received camera error non-normative");
     	cameraError = true;
     	stateChanged();
     }
     
     /* (GUI NN Panel) Drives the Parts Clumped Non-Normative */ 
     public void msgPartsClumped()
     {
     	print("Received parts clumped non-normative");
     	FactoryState.out.println("Received parts clumped non-normative");
     	partsClumped = true;
     	if(guiNest != null) guiNest.doSetClumped(true);
       	stateChanged();
     }
     
     /* (GUI NN Panel) Drives the Parts Bad Non-Normative */ 
     public void msgPartsBad()
     {
     	print("Received parts bad non-normative");
     	FactoryState.out.println("Received parts bad non-normative");
         partsBad = true;
         if(guiNest != null) guiNest.doMakeBadParts();
       	stateChanged();
     }   
     
     /* (GUI NN Panel) Drives the Parts Unstable Non-Normative */ 
     public void msgPartsUnstable()
     {
     	print("Received parts unstable non-normative");
     	FactoryState.out.println("Received parts unstable non-normative");
         partsUnstable = true;        
         if(guiNest!=null) guiNest.doSetUnstable(true);
       	stateChanged();
     }   
     
     /* (GUI NN Panel) Drives the PartsRobot Photobombing Non-Normative */ 
     public void msgPartRobotPhotobomb()
     {
     	print("Received part robot photobombing non-normative");
     	FactoryState.out.println("Received part robot photobombing non-normative");
         photobomb = true;
         stateChanged();
     }   
     
     /*** Scheduler ***/
     
     public boolean pickAndExecuteAnAction() {
 
     	// parts missing non-normative
     	if(partsMissing && executePartsMissing) missingParts();
     	
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
             if(parts.size() >= requested || (parts.size() == 10 && requested > 10))
             {
                 checkParts();
             }
             return true;
         }
 
         // once checked, send and become inactive
         if(nestState == NestState.checked)
         {
             sendParts();
             if(requested == 0) nestState = NestState.inactive;
             else nestState = NestState.requested;
             return true;
         }
         
         // reset nest and purge if bad parts
         if(nestState == NestState.partsBad)
         {
         	resetPartRequest();
         	return true;
         }
 
         return false;
     }
     
     // parts missing non-normative: set timer for picture or send msg to lane
     private void missingParts()
     {
     	if(partsMissing)
     	{
     		if(partsMissingCount > 3)
     		{
     			// non-normatives to lane, feeder, gantry
         		lane.msgWhereIsPart();
     			partsMissing = false;
     		}
     		else
     		{
     			executePartsMissing = false;
     	    	timer.schedule(new TimerTask(){
     	    		public void run() {		
     	    			if(partsMissing)
     	    			{
         	        		partsMissingCount++;
         	                print("Sending request to check parts");
         	                List<Part> visionParts = new ArrayList<Part>();
         	                if(vision !=null) vision.msgVerifyPartsMissing(visionParts, itself);
         	        
         	    			executePartsMissing = true;
         	    			stateChanged();
     	    			}
     	    		}
     	    	}, (int)(5000));
     		}
     	}    	
     }
     
     // requests parts to the lane
     private void requestParts(int num)
     {
         if(num > 0)
         {
         	print("Requested " + num + " of " + partType + " with " + (capacity-parts.size()) + " empty spaces.");
         	if(lane !=null) lane.msgRequestParts(partType,num,capacity-parts.size(), this);
         }
         stateChanged();
     }
 
     /*** Semaphores for Flushing Parts Multi-Action ***/
     
     public void msgNowPhotobombing() { nowPhotobombing.release(); }
     public Semaphore nowPhotobombing = new Semaphore(0);
     
     public void msgPartsArePhotobombing() { partsArePhotobombing.release(); }
     public Semaphore partsArePhotobombing = new Semaphore(0);
     
     public void msgDonePhotobombing() { donePhotobombing.release(); }
     public Semaphore donePhotobombing = new Semaphore(0);
 
     // check parts with the vision agent
     private void checkParts()
     {
         List<Part> visionParts = new ArrayList<Part>();
    
         if(partsBad || partsClumped)
         {
             print("Sending request to check parts");
             if(vision !=null) vision.msgVerifyPartsBad(visionParts, this);	
             nestState = NestState.checking;
 
         }
         else if(partsUnstable)
         {
             print("Sending request to check parts");
             if(vision !=null) vision.msgVerifyPartsUnstable(visionParts, this);	 
             nestState = NestState.checking;
 
         }
         else if(cameraError)
         {
             print("Sending request to check parts");
             if(vision !=null) vision.msgVerifyPartsCameraError(visionParts, this);	 
             nestState = NestState.checking;
         }
         else if(photobomb)
         {
         	partRobot.msgPhotoBomb(this);
 
         	try {
             	nowPhotobombing.acquire();
         	}
         	catch(Exception ex) { }
 
         	FactoryState.out.println("PartsRobot photobombing");
             if(vision !=null) vision.msgVerifyPartsPhotobombing(visionParts, this);	       	
 
         	try {
             	partsArePhotobombing.acquire();
         	}
         	catch(Exception ex) { }
         	
            	FactoryState.out.println("Received confirmation that partsrobot is in the way");
         	
             partRobot.msgTakenPicture();
 
         	try {
             	donePhotobombing.acquire();
         	}
         	catch(Exception ex) { }
         	
         	FactoryState.out.println("PartsRobot no longer photobombing");
         	
         	photobomb = false;
         	nestState = NestState.requested; 	
         }
     	else // normative (everything else non-normative)
     	{
     		// making sure to handle when we have less than requested (i.e. full nest)
     		
             for(int i=0; i<requested; i++)
             {
             	if(i<10) visionParts.add(parts.get(i));
             }
             for(int i=0; i<requested; i++)
             {
                 if(i<10) parts.remove(0);
             }
 
             print("Sending request to check parts");
             if(visionParts.size() == requested) requested = 0;
             else
             {
             	requested -= visionParts.size();
             }
 
             if(vision !=null) vision.msgVerifyParts(visionParts, this);
             nestState = NestState.checking;
     	}
         stateChanged();
     }
 
     // send parts to partrobot to process
     private void sendParts()
     {
         partRobot.msgHereAreParts(sendParts, this);
         sendParts = new ArrayList<Part>();
         stateChanged();
     }
     
     // reset order request and flush the parts
     private void resetPartRequest()
     {
         if(guiNest !=null) DoFlushParts(partType);
         parts = new ArrayList<Part>();
         partsClumped = false;
         partsBad = false;
     	    	
         int current = parts.size();
         needed = requested - current;
         requestParts(needed);
         needed = 0;
         nestState = NestState.requested;
         
         stateChanged();
     }
 
     private void DoFlushParts(String type)
     {
         // also case when nest isn't initialized yet
         // tell gui to flush parts and change GuiNest part type
         guiNest.setPartHeld(type);
         if(!partType.equals("")) 
         {
         	lane.msgStopLane(); // stop the lane
         	try {
         		stopLane.acquire();
         	}
         	catch(Exception ex) { }
         	
         	FactoryState.out.println("Flushing nest " + num);
         	guiNest.DoPurgeGuiNest();
        	/*
             try {
                 flushParts.acquire();
             }
            catch(Exception ex) { }*/
         	
             lane.msgStartLaneAgain();
 
         }
 
         stateChanged();
     }
 
     private void DoPutPartArrivedAtNest(Part p)
     {
         // call gui to put parts on the nest
         guiNest.doPutPartArrivedAtNest(p); 
         stateChanged();
     } 
 }
