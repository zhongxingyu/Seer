 package factory;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import factory.Kit.KitState;
 import factory.StandAgent.MySlotState;
 import factory.interfaces.*;
 import factory.masterControl.MasterControl;
 import agent.*;
 
 public class KitRobotAgent extends Agent implements KitRobot {
 	////Data
 	public Stand stand;
 	public Conveyor conveyor;
 
 	public String name;
 	
 	public Kit holding = null;
 	Semaphore standApproval = new Semaphore(0); //Semaphore for waiting for stand
 	int inspectionAreaClear = 1; //0 = not clear, 1 = clear, -1 = need to find out	starting at empty
 	public ConveyorStatus conveyor_state = ConveyorStatus.EMPTY;
 	public List<StandInfo> actions = Collections.synchronizedList(new ArrayList<StandInfo>());
 	public KitAtInspection atInspection = KitAtInspection.EMPTY;
 	
 	boolean isUnitTesting = false; 
 	public enum KitAtInspection { TOP, BOTTOM, EMPTY };
 	public enum ConveyorStatus {EMPTY, GETTING_KIT, EMPTY_KIT, COMPLETED_KIT};
 	public enum StandInfo { NEED_EMPTY_TOP, NEED_EMPTY_BOTTOM, NEED_INSPECTION_TOP, NEED_INSPECTION_BOTTOM, INSPECTION_SLOT_DONE, KIT_GOOD, KIT_BAD, CLEAR_OFF_UNFINISHED_KITS };
 	
 	//Constructor used for UnitTesting only
 	public KitRobotAgent() {
 		super(null);
 		isUnitTesting = true;
 	}
 	
 	public KitRobotAgent(MasterControl mc, Conveyor c) {
 		super(mc);
 		this.conveyor = c;
 	}
 	
 	////Messages
 	public void msgStandClear() {
 		debug("Received msgStandClear() from Stand");
 		standApproval.release();
 	}
 	
 	public void msgClearTheStandOff() {
 		debug("Receieved msgClearTheStandOff(), now knows to clear off the stand");
 		if (!actions.contains(StandInfo.CLEAR_OFF_UNFINISHED_KITS)) {
 			actions.add(StandInfo.CLEAR_OFF_UNFINISHED_KITS);
 		}
 		stateChanged();
 	}
 	
 	public void msgNeedEmptyKitAtSlot(String pos) {
 		debug("Received msgNeedEmptyKitAtSlot() from the Stand for "+ pos);
 		if (pos.equals("topSlot")) {
 			if (!actions.contains(StandInfo.NEED_EMPTY_TOP)) {
 				actions.add(StandInfo.NEED_EMPTY_TOP);
 			}
 		} else if (pos.equals("bottomSlot")) {
 			if (!actions.contains(StandInfo.NEED_EMPTY_BOTTOM)) {
 				actions.add(StandInfo.NEED_EMPTY_BOTTOM);
 			}
 		}
 		stateChanged();
 	}
 	
 	public void msgComeMoveKitToInspectionSlot(String pos) {
 		debug("Received msgComeMoveKitToInspectionSlot() From Stand for " + pos);
 		if (pos.equals("topSlot")) {
 			if (!actions.contains(StandInfo.NEED_INSPECTION_TOP)) {
 				actions.add(StandInfo.NEED_INSPECTION_TOP);
 			}
 		} else if (pos.equals("bottomSlot")) {
 			if (!actions.contains(StandInfo.NEED_INSPECTION_BOTTOM)) {
 				actions.add(StandInfo.NEED_INSPECTION_BOTTOM);
 			}
 		}
 		stateChanged();
 	}
 		
 	public void msgEmptyKitOnConveyor() {
 		debug("Received msgEmptyKitOnConveyor() from the Conveyor");
 		conveyor_state = ConveyorStatus.EMPTY_KIT;
 		stateChanged();
 	}
 	
 	public void msgKitExported() {
 		debug("Received msgKitExported() From the Conveyor");
 		conveyor_state = ConveyorStatus.EMPTY;
 		stateChanged();
 	}
 	
 	/*
 	public void msgInspectionAreaStatus(int status) {
 		debug("Received msgInspectionAreaStatus() from the Stand with a status of "+status);
 		if (status < 2 && status >= 0) {
 			inspectionAreaClear = status;
 			stateChanged();
 		}
 	}
 	*/
 	
 	public void msgComeProcessAnalyzedKitAtInspectionSlot() {
 		debug("Received msgComeProcessAnalayzedKitAtInspectionSlot from Stand");
 		if (!actions.contains(StandInfo.INSPECTION_SLOT_DONE)) {
 			actions.add(StandInfo.INSPECTION_SLOT_DONE);
 		}
 		stateChanged();
 	}
 	
 	
 	////Scheduler
 	public boolean pickAndExecuteAnAction() {	
 
 		synchronized(actions){
 			if (actions.contains(StandInfo.KIT_BAD)) {
 				putKitBackToFix();
 				return true;
 			}
 			
 			if (actions.contains(StandInfo.CLEAR_OFF_UNFINISHED_KITS)) {
 				clearOffStand();
 				return true;
 			}
 			
 			if (actions.contains(StandInfo.KIT_GOOD) && conveyor_state.equals(ConveyorStatus.EMPTY)) {
 				exportKit();
 				return true;
 			}
 			
 			if (actions.contains(StandInfo.NEED_EMPTY_TOP) && conveyor_state.equals(ConveyorStatus.EMPTY_KIT)) {
 				putEmptyKitOnStand("topSlot");
 				return true;
 			}
 			
 			if (actions.contains(StandInfo.NEED_EMPTY_BOTTOM) && conveyor_state.equals(ConveyorStatus.EMPTY_KIT)) {
 				actions.remove(StandInfo.NEED_EMPTY_BOTTOM);
 				putEmptyKitOnStand("bottomSlot");
 				return true;
 			}
 			
 			if (actions.contains(StandInfo.NEED_INSPECTION_TOP) && (inspectionAreaClear == 1)) {
 				moveToInspectionSpot("topSlot");
 				return true;
 			}
 			
 			if (actions.contains(StandInfo.NEED_INSPECTION_BOTTOM) && (inspectionAreaClear == 1)) {
 				moveToInspectionSpot("bottomSlot");
 				return true;
 			}
 			
 			if (actions.contains(StandInfo.INSPECTION_SLOT_DONE)) {
 				processKitAtInspection(); 
 				return true;
 			}
 			
 			
 			if ((actions.contains(StandInfo.NEED_EMPTY_TOP) || actions.contains(StandInfo.NEED_EMPTY_BOTTOM)) && conveyor_state.equals(ConveyorStatus.EMPTY)) {
 				requestEmptyKit();
 				return true;
 			}
 			
 		}
 		
 		return false;
 	}
 	
 	//Actions
 	public void processKitAtInspection() {
 		//action for checking the inspected kit and then doing the correct action.
 		debug("Executing processKitAtInspection()");
 		
 		stand.msgKitRobotWantsStandAccess();
 		
 		if (!isUnitTesting) {
 			try {
 				debug("Waiting for the stand to tell KitRobot it is clear to go");
 				standApproval.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		if (stand.getSlotKit("inspectionSlot").state.equals(KitState.PASSED_INSPECTION)) {
 			//kit passed inspection
 			debug("KitRobot sees that the kit passed inspection");
 			if (!actions.contains(StandInfo.KIT_GOOD) && !actions.contains(StandInfo.KIT_BAD)) {
 				actions.add(StandInfo.KIT_GOOD);
 				actions.remove(StandInfo.INSPECTION_SLOT_DONE);
 				stand.msgKitRobotNoLongerUsingStand();
 			}
 		}
 		//TODO this is where kit inspection fails.  figure out why it removes all parts rather than just bad parts
 		else {
 			//kit failed inspection
 			debug("KitRobot sees that the kit failed inspection");
 			if (!actions.contains(StandInfo.KIT_GOOD) && !actions.contains(StandInfo.KIT_BAD)) {
 				actions.add(StandInfo.KIT_BAD);
 				actions.remove(StandInfo.INSPECTION_SLOT_DONE);
 			}
 		}
 	}
 	/*
 	public void dumpKit() {
 		//action for dumping a bad kit
 		debug("KitRobot dumping bad kit");
 		
 		if (!isUnitTesting) {
 			DoDumpKitAtSlot("inspectionSlot");
 		}
 		debug("Dumping animation complete");
 		
 		holding = null;
 		stand.setSlotKit("inspectionSlot", null);
 		stand.setSlotState("inspectionSlot", MySlotState.EMPTY);
 		actions.remove(StandInfo.KIT_BAD);
 		stand.msgKitRobotNoLongerUsingStand();
 	}
 	*/
 	
 	public void putKitBackToFix() {
 		//action for moving a kit back to the slot it came from to add in missing parts
 		debug("KitRobot is putting back the kit that failed inspection.");
 		print("KitRobot is putting back the kit that failed inspection.");
 		if (atInspection.equals(KitAtInspection.TOP) || atInspection.equals(KitAtInspection.BOTTOM)) {
 			String pos;
 			if (atInspection.equals(KitAtInspection.TOP)) { pos = "topSlot"; } else { pos = "bottomSlot"; }
 			if (!isUnitTesting) {
 				DoPutKitBack(pos);
 			}
 			
 			Kit temp = stand.getSlotKit("inspectionSlot");
 			temp.state = KitState.INCOMPLETE;
			
 			stand.setSlotKit(pos, temp);
 			stand.setSlotState(pos, MySlotState.NEEDS_FIXING);
 			stand.setSlotKit("inspectionSlot", null);
 			stand.setSlotState("inspectionSlot", MySlotState.EMPTY);
 			actions.remove(StandInfo.KIT_BAD);
 			atInspection = KitAtInspection.EMPTY;
 			stand.msgKitRobotNoLongerUsingStand();
 		} else {
 			//shouldn't have been called
 			debug("putKitBackToFix() was called when atInspection = EMPTY");
 			stand.msgKitRobotNoLongerUsingStand();
 		}
 	}
 	
 	public void exportKit() {
 		//action for exporting a good kit
 		debug("KitRobot picking up kit from inspection to export");
 		
 		stand.msgKitRobotWantsStandAccess();
 		if (!isUnitTesting) {
 			try {
 				debug("Waiting for the stand to tell KitRobot it is clear to go");
 				standApproval.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			
 			DoMoveInspectedKitToConveyor();
 		}
 		
 		//Animation is now done, kit is on the conveyor
 		holding = stand.getSlotKit("inspectionSlot");
 		conveyor.msgExportKit(holding);
 
 		stand.setSlotKit("inspectionSlot", null);
 		stand.setSlotState("inspectionSlot", MySlotState.EMPTY);
 		conveyor_state = ConveyorStatus.COMPLETED_KIT;
 		actions.remove(StandInfo.KIT_GOOD);
 		
 		atInspection = KitAtInspection.EMPTY;
 		
 		debug("Kit exported, telling the Stand it is done");
 		stand.msgKitRobotNoLongerUsingStand();
 	}
 	
 	public void putEmptyKitOnStand(String pos) {
 		//method for putting an empty kit on the stand
 		
 		debug("Executing putEmptyKitOnStand "+ pos);
 		
 		
 		stand.msgKitRobotWantsStandAccess();
 		
 		if (!isUnitTesting) {
 			try {
 				debug("Waiting for the stand to tell KitRobot it is clear to go");
 				standApproval.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		debug("Got the go from the Stand");
 		//Can assume that the kit robot has exclusive access to the stand here
 		holding = new Kit();
 		conveyor_state = ConveyorStatus.EMPTY;
 		conveyor.setAtConveyor(null);
 		
 		if (!isUnitTesting) {
 			DoPutEmptyKitAt(pos);
 		}
 		
 		stand.setSlotKit(pos, this.holding);
 		stand.setSlotState(pos, MySlotState.EMPTY_KIT_JUST_PLACED);
 		holding = null;
 		
 		debug("Done putting empty kit in "+pos);
 		
 		if (pos.equals("topSlot")) {
 			actions.remove(StandInfo.NEED_EMPTY_TOP);
 		} else if (pos.equals("bottomSlot")) {
 			actions.remove(StandInfo.NEED_EMPTY_BOTTOM);
 		}
 		
 		stand.msgKitRobotNoLongerUsingStand();
 		stateChanged();
 	}
 	
 	public void clearOffStand() {
 		/**
 		 * KitRobot will dump any Kits that are not yet completed.
 		 * the kits that are are already in need of inspection or in the inspectionSlot will not be disturbed
 		 */
 		
 		stand.msgKitRobotWantsStandAccess();
 		
 		if (!isUnitTesting) {
 			try {
 				debug("Waiting for the stand to tell KitRobot it is clear to go");
 				standApproval.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		if (stand.getSlotKit("topSlot") != null) {
 			if (stand.getSlotKit("topSlot").parts.size() != 0) {
 				//do animation of dumping topSlot
 				if (!isUnitTesting) {
 					DoDumpKitAtSlot("topSlot");
 				}
 				stand.setSlotKit("topSlot", null);
 				stand.setSlotState("topSlot", MySlotState.EMPTY);
 			}
 		}
 		if (stand.getSlotKit("bottomSlot") != null) {
 			if (stand.getSlotKit("bottomSlot").parts.size() != 0) {
 				//do animation of dumping bottomSlot
 				if (!isUnitTesting) {
 					DoDumpKitAtSlot("bottomSlot");
 				}
 				stand.setSlotKit("bottomSlot", null);
 				stand.setSlotState("bottomSlot", MySlotState.EMPTY);
 				if (actions.contains(StandInfo.NEED_INSPECTION_BOTTOM)) {
 					actions.remove(StandInfo.NEED_INSPECTION_BOTTOM);
 				}
 			}
 		}
 		
 		if (stand.getSlotKit("inspectionSlot") != null) {
 			if (!isUnitTesting) {
 				DoDumpKitAtSlot("inspectionSlot");
 				stand.setSlotKit("inspectionSlot", null);
 				stand.setSlotState("inspectionSlot", MySlotState.EMPTY);
 				if (actions.contains(StandInfo.NEED_INSPECTION_TOP)) {
 					actions.remove(StandInfo.NEED_INSPECTION_TOP);
 				}
 			}
 		}
 		
 		actions.remove(StandInfo.CLEAR_OFF_UNFINISHED_KITS);
 		stand.msgKitRobotNoLongerUsingStand();
 
 		stateChanged();
 	}
 	
 	public void requestEmptyKit() {
 		//method for asking for a new kit from the conveyor
 		debug("Requesting an Empty Kit from the Conveyor");
 		conveyor.msgNeedEmptyKit();
 		conveyor_state = ConveyorStatus.GETTING_KIT;
 		
 		stateChanged();
 	}
 	
 	//TODO check to see if there is a way to prevent an automatic kit to be put in empty slot
 	public void moveToInspectionSpot(String pos) {
 		//method for KitRobot moving a kit to the inspection slot
 		//Can assume that has exclusive access to the Stand during this
 		stand.msgKitRobotWantsStandAccess();
 		
 		if (!isUnitTesting) {
 			try {
 				debug("Waiting for the stand to tell KitRobot it is clear to go");
 				standApproval.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		debug("Executing moveToInspectionSpot for the" + pos);
 		
 		if (stand.getSlotKit(pos) != null) {
 		
 			if (!isUnitTesting) {
 				DoMoveKitToInspection(pos);
 			}
 			
 			debug("Animation moveKitFromSlotToInspection() was completed");
 			
 			stand.setSlotKit("inspectionSlot", stand.getSlotKit(pos));
 			stand.setSlotKit(pos, null); //TODO why is this null?
 			stand.setSlotState("inspectionSlot", MySlotState.KIT_JUST_PLACED_AT_INSPECTION);
 			stand.setSlotState(pos, MySlotState.EMPTY);	//TODO change MySlotState.EMPTY to MySlotState.PENDING?
 		}
 		if (pos.equals("bottomSlot")) {
 			actions.remove(StandInfo.NEED_INSPECTION_BOTTOM);
 			atInspection = KitAtInspection.BOTTOM;
 		} else if (pos.equals("topSlot")) {
 			actions.remove(StandInfo.NEED_INSPECTION_TOP);
 			atInspection = KitAtInspection.TOP;
 		}
 		
 		stand.msgKitRobotNoLongerUsingStand();
 		
 		stateChanged();
 	}
 	
 	////Animations
 	
 	private void waitForAnimation() {
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void DoMoveInspectedKitToConveyor() {
 		debug("doing moveInspectedKitToConveyor");
 		server.command(this,"kra kam cmd putinspectionkitonconveyor");
 		waitForAnimation();
 	}
 	
 	private void DoPutEmptyKitAt(String pos) {
 		debug("doing PutEmptyKitAt Animation for the "+ pos);
 		server.command(this,"kra kam cmd putemptykitatslot "+pos);
 		waitForAnimation();
 	}
 	
 	private void DoMoveKitToInspection(String pos) {
 		debug("doing MoveKitToInspection Animation.  moving the kit at the "+pos+" to the inspectionSlot");
 		server.command(this,"kra kam cmd movekittoinspectionslot " + pos);
 		waitForAnimation();
 	}
 	
 	private void DoDumpKitAtSlot(String pos) {
 		debug("doing DoDumpKitAtSlot Animation.  dumping the kit at "+pos);
 		server.command(this,"kra kam cmd dumpkitatslot "+pos);
 		waitForAnimation();
 	}
 	
 	private void DoPutKitBack(String pos) {
 		debug("doing DoPutKitBack Animation. putting the kit back at "+pos);
 		server.command(this,"kra kam cmd movekitback "+pos);
 		waitForAnimation();
 		
 	}
 	////Hacks / MISC
 	public void setStand(Stand s) {
 		this.stand = s;
 	}
 	
 	public void setConveyor(Conveyor c) {
 		this.conveyor = c;
 	}
 	
 	public KitAtInspection getAtInspection(){
 		return this.atInspection;
 	}
 	
 }
