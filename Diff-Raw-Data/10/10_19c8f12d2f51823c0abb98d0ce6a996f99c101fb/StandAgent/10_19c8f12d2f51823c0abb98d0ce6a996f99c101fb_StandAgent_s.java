 package factory;
 
 import factory.interfaces.*;
 import agent.Agent;
 import factory.Kit;
 
 public class StandAgent extends Agent implements Stand {
 	
 	/** DATA **/
	public enum StandAgentState { FREE, KIT_ROBOT, PARTS_ROBOT }
	public enum MyConveyorState { EMPTY, HAS_EMPTY_KIT, FETCHING_EMPTY_KIT }
	public enum MySlotState { EMPTY, EMPTY_KIT_JUST_PLACED, BUILDING_KIT, MOVING_KIT_TO_INSPECTION, KIT_JUST_PLACED_AT_INSPECTION, ANALYZING_KIT, KIT_ANALYZED, PROCESSING_ANALYZED_KIT}
 
 	public StandAgentState state;
 	
 	public MyConveyor conveyor;
 	public PartsRobot partsRobot;
 	public Boolean partsRobotWantsToDeliverParts = false;
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
 	
 	public class MyConveyor {
 		public Conveyor conveyor;
 		public MyConveyorState state;
 
 		public MyConveyor(Conveyor conveyor){
 			this.state = MyConveyorState.EMPTY;
 			this.conveyor = conveyor;
 		}
 	}
 
 	
 	/**
 	 * Constructor
 	 * @param conveyor
 	 * @param vision
 	 * @param kitRobot
 	 * @param partsRobot
 	 */
 	public StandAgent(Conveyor conveyor, Vision vision, KitRobot kitRobot, PartsRobot partsRobot){
 
 		this.conveyor = new MyConveyor(conveyor);
 		this.vision = vision;
 		this.partsRobot = partsRobot;
 		this.kitRobot = kitRobot;
 		
 		partsRobotWantsToDeliverParts = false;
 		this.state = StandAgentState.FREE;
 		
 		topSlot = new MySlot("topSlot");
 		bottomSlot = new MySlot("bottomSlot");
 		inspectionSlot = new MySlot("inspectionSlot");
 	}
 	
 	/** MESSAGES **/
 	
 	/**
 	 * Message that is Received from the conveyor when it brought an empty kit
 	 */
 	public void msgEmptyKitIsHere() {
 	   conveyor.state = MyConveyorState.HAS_EMPTY_KIT;
 	   stateChanged();
 	}
 	
 	/**
 	 * Message that is received from the KitRobot when it has moved out of the way
 	 */
 	public void msgKitRobotNoLongerUsingStand() {
 	   if(state == StandAgentState.KIT_ROBOT){
 	      state = StandAgentState.FREE;
 	      stateChanged();
 	   }
 	   else {
 	      // Throw Exception
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
 	      // Throw Exception
 	   }
 	}
 	
 	/**
 	 * Message that is received when the Vision has analyzed the results
 	 */
 	public void msgResultsOfKitAtInspection(KitState results) {   
 	   inspectionSlot.kit.state = results;
 	   inspectionSlot.state = MySlotState.KIT_ANALYZED;
 	   stateChanged();
 	}
 	
 	/** SCHEDULER **/
 	public boolean pickAndExecuteAnAction() {
 		
 		synchronized(state) {
 
 			/**
 			 * If there is a Kit in the Inspection Slot that hasn't been analyzed, then ask Vision to do so 
 			 */
 			if (inspectionSlot.state == MySlotState.KIT_ANALYZED) {
 			   DoProcessAnalyzedKit();
 			   return true;
 			}
 			/**
 			 * If there is a Kit in the Inspection Slot that has been analyzed, then ask KitRobot to process it 
 			 */
 			if (inspectionSlot.state == MySlotState.KIT_JUST_PLACED_AT_INSPECTION) {
 			   DoAskVisionToInspectKit();
 			   return true;
 			}
 			/**
 			 * If there an empty kit was just placed in a slot, tell the partsRobot to build it
 			 */
 			if(topSlot.state  == MySlotState.EMPTY_KIT_JUST_PLACED || bottomSlot.state  == MySlotState.EMPTY_KIT_JUST_PLACED) {
 				DoProcessEmptyBinFromConveyor();
 			}
 			/**
 			 * If there is a completed kit and the stand is not being used
 			 */
 			if (state == StandAgentState.FREE && topSlot.kit != null && topSlot.kit.state == KitState.COMPLETE && topSlot.state == MySlotState.BUILDING_KIT) {
 			   DoTellKitRobotToMoveKitToInspectionSlot(topSlot);
 			   return true;
 			}
 			if (state == StandAgentState.FREE && bottomSlot.kit != null && bottomSlot.kit.state == KitState.COMPLETE && bottomSlot.state == MySlotState.BUILDING_KIT) {
 			   DoTellKitRobotToMoveKitToInspectionSlot(bottomSlot);
 			   return true;
 			}
 			/**
 			 * If there is an empty kit at the conveyor and there is a place to put it, ask the Kit Robot to fetch it
 			 * as long as the stand is not being used.
 			 */
 			if (state == StandAgentState.FREE && conveyor.state == MyConveyorState.HAS_EMPTY_KIT 
 					&& (topSlot.state == MySlotState.EMPTY || bottomSlot.state == MySlotState.EMPTY)) {
 			   DoFetchEmptyKitFromConveyor();
 			   return true;
 			}       
 			/**
 			 * If the stand is free and the partRobot wants to deliver parts
 			 */
 			if (state == StandAgentState.FREE && partsRobotWantsToDeliverParts == true && (topSlot.kit != null || bottomSlot.kit != null)) {
 			   DoTellPartsRobotToDeliverParts();
 			   return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/** ACTIONS **/
 	
 	/**
 	 * Method that tells the KitRobot to fetch the empty Kit
 	 */
 	private void DoFetchEmptyKitFromConveyor() {
 		if (topSlot.state == MySlotState.EMPTY) {
 			kitRobot.msgGrabAndBringEmptyKitFromConveyorToSlot(topSlot.name);
 		}
 		else if (bottomSlot.state == MySlotState.EMPTY) {
 			kitRobot.msgGrabAndBringEmptyKitFromConveyorToSlot(bottomSlot.name);
 		}
 		else {
 			// Throw exception
 		}
 		state = StandAgentState.KIT_ROBOT;
 		conveyor.state = MyConveyorState.FETCHING_EMPTY_KIT;
 	}   
 	/**
 	 * Method that places the empty bin in the right slot
 	 */
 	private void DoProcessEmptyBinFromConveyor() {
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
 	   kitRobot.msgComeMoveKitToInspectionSlot(slot.name);
 	   state = StandAgentState.KIT_ROBOT;
 	   slot.state = MySlotState.MOVING_KIT_TO_INSPECTION;                   
 	}
 	/**
 	 * Method that tells the Vision to take picture of the kit
 	 */
 	private void DoAskVisionToInspectKit() {
 	   vision.msgAnalyzeKitAtInspection(inspectionSlot.kit);
 	   inspectionSlot.state = MySlotState.ANALYZING_KIT;                   
 	}   
 	/**
 	 * Method that tells the KitRobot to process the Kit
 	 */
 	private void DoProcessAnalyzedKit() {
 	   kitRobot.msgComeProcessAnalyzedKitAtInspectionSlot();
 	   state = StandAgentState.KIT_ROBOT;
 	   inspectionSlot.state = MySlotState.PROCESSING_ANALYZED_KIT;                    
 	}    
 
 }
 
 
