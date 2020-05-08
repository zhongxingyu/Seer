 package factory;
 
 import factory.interfaces.*;
 import agent.Agent;
 
 public class ConveyorAgent extends Agent implements Conveyor {
 	////Data
 	enum KitRobot_State { NO_ACTION, TAKING_KIT, WANTS_EMPTY_KIT, WANTS_EXPORT };
 	
 	KitRobot kit_robot;
 	ConveyorController conveyor_controller;
 	
 	Kit on_conveyor;  //Supposed to represent what is on the ConveyorAgent
 	KitRobot_State kr_state = KitRobot_State.NO_ACTION;
 	
 	/** Public Constructor **/
 	public ConveyorAgent() {
		super();
 	}
 	
 	////Messages
 	public void msgHeresEmptyKit(Kit k) {
 	   on_conveyor = k;
 	   stateChanged();
 	}
 
 	public void msgTakingKit() {
 	   kr_state = KitRobot_State.TAKING_KIT;
 	   stateChanged();
 	}
 
 	public void msgPuttingKit(KitRobot kr, Kit k) {
 	   on_conveyor = k;
 	   stateChanged();
 	}
 
 	public void msgNeedEmptyKit(KitRobot kr) {
 	   kr_state = KitRobot_State.WANTS_EMPTY_KIT;
 	   stateChanged();
 	}
 
 	public void msgExportKit(KitRobot kr) {
 	   kr_state = KitRobot_State.WANTS_EXPORT;
 	   stateChanged();
 	}
 	
 	////Scheduler
 	protected boolean pickAndExecuteAnAction() {
 		if (kr_state.equals(KitRobot_State.WANTS_EXPORT)) { exportKit(); return true; }
 
 		if (kr_state.equals(KitRobot_State.WANTS_EMPTY_KIT) && on_conveyor != null) { tellKitRobotAboutEmptyKit(); return true; }
 
 		if (kr_state.equals(KitRobot_State.TAKING_KIT)) { giveKit(); return true; }
 
 		if (kr_state.equals(KitRobot_State.WANTS_EMPTY_KIT) && on_conveyor == null) { requestEmptyKit(); return true; }
 		
 		return false;
 	}
 	
 	////Actions
 	private void tellKitRobotAboutEmptyKit() {
 		   kit_robot.msgEmptyKitIsHere();
 		   kr_state = KitRobot_State.NO_ACTION;
 		}
 
 		private void requestEmptyKit() {
 		   conveyor_controller.msgConveyorWantsEmptyKit(this);
 		   kr_state = KitRobot_State.NO_ACTION;
 		}
 
 		private void exportKit() {
 		   //should call the kit exporting animation, afterwards COULD call msgKitExported if you want to keep a list of all the finished kits
 			conveyor_controller.msgKitExported(this, on_conveyor);
 		}
 
 		private void giveKit() {
 		   kit_robot.msgYouTook(on_conveyor);
 		   on_conveyor = null;
 		   kr_state = KitRobot_State.NO_ACTION;
 		}
 
 	////Misc
 	
 }
 
