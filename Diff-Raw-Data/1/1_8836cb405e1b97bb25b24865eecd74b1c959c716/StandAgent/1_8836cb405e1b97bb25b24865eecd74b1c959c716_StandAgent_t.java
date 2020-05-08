 package factory;
 
 import factory.interfaces.*;
 import factory.masterControl.MasterControl;
 import agent.Agent;
 import factory.Kit;
 import factory.Kit.KitState;
 
 public class StandAgent extends Agent implements Stand {
 	
 	/** DATA **/
 
 	public enum StandAgentState { FREE, KIT_ROBOT, PARTS_ROBOT }
 	public enum MySlotState { EMPTY, EMPTY_KIT_REQUESTED, EMPTY_KIT_JUST_PLACED, BUILDING_KIT, MOVING_KIT_TO_INSPECTION, KIT_JUST_PLACED_AT_INSPECTION, ANALYZING_KIT, KIT_ANALYZED, PROCESSING_ANALYZED_KIT}
 
 	public StandAgentState state;
 	
 	public PartsRobot partsRobot;
 	public Boolean partsRobotWantsToDeliverParts = false;
 	public Boolean kitRobotWantsToDeliverEmptyKit = false;
 	public Boolean needToClearStand = false;
 	
 	public boolean kitRobotWantsToExportKit = false;
 	
 	public KitRobot kitRobot;
 	public Vision vision;
 	
 	public MySlot topSlot;
 	public MySlot bottomSlot;
 	public MySlot inspectionSlot;
 
 	public class MySlot {
 		public String name;
 		public Kit kit;
 		public MySlotState state;
 	
 		public MySlot(String name){
 			this.state = MySlotState.EMPTY;
 			this.name = name;
 			this.kit = null;
 		}
 	}
 
 	
 	/**
 	 * Constructor
 	 * @param vision
 	 * @param kitRobot
 	 * @param partsRobot
 	 */	
 	public StandAgent(Vision vision, KitRobot kitRobot, PartsRobot partsRobot, MasterControl mc){
 		super(mc);
 		this.vision = vision;
 		this.partsRobot = partsRobot;
 		this.kitRobot = kitRobot;
 
 		partsRobotWantsToDeliverParts = false;
 		kitRobotWantsToDeliverEmptyKit = false;
 		this.state = StandAgentState.FREE;
 		
 		topSlot = new MySlot("topSlot");
 		bottomSlot = new MySlot("bottomSlot");
 		inspectionSlot = new MySlot("inspectionSlot");
 	}
 	
 	public StandAgent(MasterControl mc, KitRobot kitRobot) {
 		super(mc);
 		this.kitRobot = kitRobot;
 		
 		partsRobotWantsToDeliverParts = false;
 		kitRobotWantsToDeliverEmptyKit = false;
 		this.state = StandAgentState.FREE;
 		
 		topSlot = new MySlot("topSlot");
 		bottomSlot = new MySlot("bottomSlot");
 		inspectionSlot = new MySlot("inspectionSlot");
 	}
 	
 	//UnitTesting Constructor
 	public StandAgent() {
 		super(null);
 		partsRobotWantsToDeliverParts = false;
 		kitRobotWantsToDeliverEmptyKit = false;
 		this.state = StandAgentState.FREE;
 		
 		topSlot = new MySlot("topSlot");
 		bottomSlot = new MySlot("bottomSlot");
 		inspectionSlot = new MySlot("inspectionSlot");
 	}
 	
 	/** MESSAGES **/
 	
 	/**
 	 * Message that is Received from the conveyor when it brought an empty kit
 	 */
 	public void msgEmptyKitIsHereAndWantToDeliver() {
 		debug("Received msgEmptyKitIsHereAndWantToDeliver() from the kitRobot.");
 		kitRobotWantsToDeliverEmptyKit = true;
 		stateChanged();
 	}
 	
 	public void msgKitRobotWantsToExportKit() {
 		debug("Received msgKitRobotWantsToExportKit() from the kitRobot");
 		kitRobotWantsToExportKit = true;
 		stateChanged();
 	}
 	
 	/**
 	 * Message that is received from the KitRobot when it has moved out of the way
 	 */
 	public void msgKitRobotNoLongerUsingStand() {
 		if(state == StandAgentState.KIT_ROBOT){
 			debug("Received msgKitRobotNoLongerUsingStand() from the kit robot.");
 			state = StandAgentState.FREE;
 			stateChanged();
 		}
 		else {
 			//debug("msgPartsRobotNoLongerUsingStand() called when it wasn't using it!!");
 			//System.exit(1);
 	   }
 	}
 	
 	/**
 	 * Message received when the PartsRobot wants to deliver a part(s)
 	 */
 	public void msgPartRobotWantsToPlaceParts() {
 	   partsRobotWantsToDeliverParts = true;
 	   stateChanged();
 	}
 	
 	/**
 	 * Message that is received from the PartsRobot when it has moved out of the way  
 	 */
 	public void msgPartsRobotNoLongerUsingStand() {   
 	   if(state == StandAgentState.PARTS_ROBOT){
 	      state = StandAgentState.FREE;
 	      stateChanged();
 	   }
 	   else {
 		   debug("msgPartsRobotNoLongerUsingStand() called when it wasn't using it!!");
 		   System.exit(1);
 	   }
 	}
 	
 	/**
 	 * Message that is received when the Vision has analyzed the results
 	 */
 	public void msgResultsOfKitAtInspection(KitState results) {   
 		debug("Received msgResultsOfKitAtInspection()"); 
 		
 		inspectionSlot.kit.state = results;
 		inspectionSlot.state = MySlotState.KIT_ANALYZED;
 		stateChanged();
 	}
 	
 	/** SCHEDULER **/
 	public boolean pickAndExecuteAnAction() {
 		
 		synchronized(state) {
 			/**
 			 * If the stand is free and the kitRobot wants to deliver empty kit
 			 */
 			if (state == StandAgentState.FREE && this.needToClearStand) {
 			   DoTellKitRobotToClearStand();
 			   return true;
 			}
 			
 			/**
 			 * If there is a Kit in the Inspection Slot that hasn't been analyzed, then ask Vision to do so 
 			 */
 			if (inspectionSlot.state == MySlotState.KIT_JUST_PLACED_AT_INSPECTION) {
 				DoAskVisionToInspectKit();
 				return true;
 			}
 			
 			/**
 			 * If there is a Kit in the Inspection Slot that has been analyzed, then ask KitRobot to process it 
 			 */
 			if (state == StandAgentState.FREE && inspectionSlot.state == MySlotState.KIT_ANALYZED) {
 				DoProcessAnalyzedKit();
 				return true;
 			}
 			
 			if (state == StandAgentState.FREE && kitRobotWantsToExportKit) {
 				DoTellKitRobotToExport();
 				return true;
 			}
 			
 			/**
 			 * If there an empty kit was just placed in a slot, tell the partsRobot to build it
 			 */
 			if(topSlot.state  == MySlotState.EMPTY_KIT_JUST_PLACED || bottomSlot.state  == MySlotState.EMPTY_KIT_JUST_PLACED) {
 				DoProcessEmptyBinFromConveyor();
 				return true;
 			}
 			/**
 			 * If there is a completed kit and the stand is not being used
 			 */
 			if (state == StandAgentState.FREE && topSlot.kit != null && topSlot.kit.state == KitState.COMPLETE 
 					&& topSlot.state == MySlotState.BUILDING_KIT && inspectionSlot.state == MySlotState.EMPTY) {
 			   DoTellKitRobotToMoveKitToInspectionSlot(topSlot);
 			   return true;
 			}
 			if (state == StandAgentState.FREE && bottomSlot.kit != null && bottomSlot.kit.state == KitState.COMPLETE 
 					&& bottomSlot.state == MySlotState.BUILDING_KIT && inspectionSlot.state == MySlotState.EMPTY) {
 			   DoTellKitRobotToMoveKitToInspectionSlot(bottomSlot);
 			   return true;
 			}
 		
 
 			/**
 			 * If the stand is free and the kitRobot wants to deliver empty kit
 			 */
 			if (state == StandAgentState.FREE && kitRobotWantsToDeliverEmptyKit == true
 					&& (topSlot.state == MySlotState.EMPTY_KIT_REQUESTED || bottomSlot.state == MySlotState.EMPTY_KIT_REQUESTED)) {
 			   DoTellKitRobotToDeliverEmptyKit();
 			   return true;
 			}
 			
 
 			/**
 			 * If the stand is free and the partRobot wants to deliver parts
 			 */
 			if (state == StandAgentState.FREE && partsRobotWantsToDeliverParts == true && (topSlot.kit != null || bottomSlot.kit != null)) {
 			   DoTellPartsRobotToDeliverParts();
 			   return true;
 			}
 
 			/**
 			 * If there is an empty kit at the conveyor and there is a place to put it, ask the Kit Robot to fetch it
 			 * as long as the stand is not being used.
 			 */
 			if (state == StandAgentState.FREE && (topSlot.state == MySlotState.EMPTY || bottomSlot.state == MySlotState.EMPTY)
 					&& (inspectionSlot.state ==  MySlotState.EMPTY)) {
 			   DoAskKitRobotToGetEmptyKit();
 			   return true;
 			}       
 			
 		}
 		
 		return false;
 	}
 	
 	/** ACTIONS **/
 	
 	private void DoTellKitRobotToExport() {
 		debug("Executing DoTellKitRobotToExport()");
 		kitRobot.msgStandClear();
 		state = StandAgentState.KIT_ROBOT;
 		kitRobotWantsToExportKit = false;
 	}
 	
 	/**
 	 * Method that tells the KitRobot to fetch the empty Kit
 	 */
 	private void DoAskKitRobotToGetEmptyKit() {
 		debug("Executing DoAskKitRobotToGetEmptyKit()");
 
 		if (topSlot.state == MySlotState.EMPTY) {
 			debug("Asking kit robot to get empty bin for slot " + topSlot.name);
 			kitRobot.msgNeedEmptyKitAtSlot(topSlot.name);
 			topSlot.state = MySlotState.EMPTY_KIT_REQUESTED;
 		}
 		else if (bottomSlot.state == MySlotState.EMPTY) {
 			debug("Asking kit robot to get empty bin for slot " + bottomSlot.name);
 			kitRobot.msgNeedEmptyKitAtSlot(bottomSlot.name);
 			bottomSlot.state = MySlotState.EMPTY_KIT_REQUESTED;
 		}
 		else {
 			// Throw exception
 		}
 	}  
 	
 	/**
 	 * Method that tells the KitRobot to deliver the empty kit
 	 */
 	private void DoTellKitRobotToDeliverEmptyKit(){
 		debug("Executing DoTellKitRobotToDeliverEmptyKit()");
 		kitRobot.msgStandClear();
 		state = StandAgentState.KIT_ROBOT;
 		kitRobotWantsToDeliverEmptyKit = false;
 	}
 	
 	/**
 	 * Method that places the empty bin in the right slot
 	 */
 	private void DoProcessEmptyBinFromConveyor() {
 		debug("Executing DoProcessEmptyBinFromConveyor()");
 
 		if(topSlot.state  == MySlotState.EMPTY_KIT_JUST_PLACED) {
 			DoTellPartsRobotToBuildKitAtSlot(topSlot);
 		}
 		else if(bottomSlot.state  == MySlotState.EMPTY_KIT_JUST_PLACED) {
 			DoTellPartsRobotToBuildKitAtSlot(bottomSlot);
 		}
 		else {
 			// Throw Exception
 		}
 	   
 	}
 	
 	/**
 	 * Method that tells the PartsRobot to build Kit
 	 */
 	private void DoTellPartsRobotToBuildKitAtSlot(MySlot slot) {
 		debug("Executing DoTellPartsRobotToBuildKitAtSlot("+slot.name+")");
 		partsRobot.msgBuildKitAtSlot(slot.name);
 		slot.state = MySlotState.BUILDING_KIT;
 	} 
 	
 	/**
 	 * Method that tells the PartsRobot to deliver parts
 	 */
 	private void DoTellPartsRobotToDeliverParts() {
 	   partsRobot.msgDeliverKitParts();   
 	   state = StandAgentState.PARTS_ROBOT;                      
 	}   
 	/**
 	 * Method that tells the KitRobot to move a Kit to the inspection slot
 	 */
 	private void DoTellKitRobotToMoveKitToInspectionSlot(MySlot slot) {
 		debug("Executing DoTellKitRobotToMoveKitToInspectionSlot("+slot.name+")");
 		kitRobot.msgComeMoveKitToInspectionSlot(slot.name);
 		state = StandAgentState.KIT_ROBOT;
 		slot.state = MySlotState.MOVING_KIT_TO_INSPECTION;                   
 	}
 	/**
 	 * Method that tells the Vision to take picture of the kit
 	 */
 	private void DoAskVisionToInspectKit() {
 		debug("Executing DoAskVisionToInspectKit()");
 		vision.msgAnalyzeKitAtInspection(inspectionSlot.kit);
 	   	inspectionSlot.state = MySlotState.ANALYZING_KIT; 
 	   	stateChanged();
 	}   
 	/**
 	 * 
 	 * Method that tells the KitRobot to process the Kit
 	 */
 	private void DoProcessAnalyzedKit() {
 		debug("Executing DoProcessAnalyzedKit()");
 		kitRobot.msgComeProcessAnalyzedKitAtInspectionSlot();
 		state = StandAgentState.KIT_ROBOT;
 		inspectionSlot.state = MySlotState.PROCESSING_ANALYZED_KIT;                    
 	}
 	
 	public void DoTellKitRobotToClearStand(){
 		debug("Executing DoTellKitRobotToClearStand()");
 		kitRobot.msgClearTheStandOff();
 		state = StandAgentState.KIT_ROBOT;
 		this.needToClearStand = false;
 	}
 	/**
 	 * Hacks and Misc
 	 */
 	public void setVision(Vision v) {
 		this.vision = v;
 	}
 	
 	public void setKitRobot(KitRobot kr) {
 		this.kitRobot = kr;
 	}
 	
 	public void setPartsRobot(PartsRobot pr) {
 		this.partsRobot = pr;
 	}
 	
 	public boolean setSlotKit(String slot, Kit k) {
 		if (slot.equals("topSlot")) {
 			topSlot.kit = k;
 			return true;
 		} else if (slot.equals("bottomSlot")) {
 			bottomSlot.kit = k;
 			return true;
 		} else if (slot.equals("inspectionSlot")) {
 			inspectionSlot.kit = k;
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public Kit getSlotKit(String slot) {
 		if (slot.equals("topSlot")) {
 			return topSlot.kit;
 		} else if (slot.equals("bottomSlot")) {
 			return bottomSlot.kit;
 		} else {
 			//assume inspectionSlot 
 			return inspectionSlot.kit;
 		}
 	}
 	
 	public boolean setSlotState(String slot, MySlotState state) {
 		if (slot.equals("topSlot")) {
 			topSlot.state = state;
 			return true;
 		} else if (slot.equals("bottomSlot")) {
 			bottomSlot.state = state;
 			return true;
 		} else if (slot.equals("inspectionSlot")) {
 			inspectionSlot.state = state;
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public MySlotState getSlotState(String slot) {
 		if (slot.equals("topSlot")) {
 			return topSlot.state;
 		} else if (slot.equals("bottomSlot")) {
 			return bottomSlot.state;
 		} else {
 			//assume inspectionSlot
 			return inspectionSlot.state;
 		}
 	}
 
 	@Override
 	public void msgClearStand() {
 		needToClearStand = true;
		this.stateChanged();
 	}
 
 
 }
