 package factory;
 
 import java.util.*;
 import factory.interfaces.*;
 import agent.Agent;
 
 public class ConveyorControllerAgent extends Agent implements ConveyorController {
 	////Data	
 	Conveyor conveyor;
 	List<Kit> exported_kits = new ArrayList<Kit>();
 	enum Conveyor_State { WANTS_EMPTY_KIT, EMPTY_KIT_SENDING, NO_ACTION };
 	Conveyor_State conveyor_state = Conveyor_State.NO_ACTION;
 	
 	Timer timer = new Timer();
 	
 	public ConveyorControllerAgent() {
		super();
 	}
 	////Messages
 	public void msgConveyorWantsEmptyKit(Conveyor c) {
 		if (!conveyor_state.equals(Conveyor_State.WANTS_EMPTY_KIT) && !conveyor_state.equals(Conveyor_State.EMPTY_KIT_SENDING)) {
 			conveyor_state = Conveyor_State.WANTS_EMPTY_KIT;
 			stateChanged();
 		}
 	}
 	
 	public void msgKitExported(Conveyor c, Kit k) {
 		exported_kits.add(k);
 		stateChanged();
 	}
 	
 	
 	////Scheduler
 	protected boolean pickAndExecuteAnAction() {
 		if (conveyor_state.equals(Conveyor_State.WANTS_EMPTY_KIT)) {
 			conveyor_state = Conveyor_State.EMPTY_KIT_SENDING;
 			sendEmptyKit();
 		}
 		return false;
 	}
 	
 	////Actions
 	private void sendEmptyKit() {
 		//Creates a random time that it will take for the empty kit to make it to the kitting cell
 		
 		int delivery_time = (int) (1000 + (Math.random()*9000)); //Random time it will take for the empty kit to make it to the cell
 		
 		timer.schedule(new TimerTask(){
 		    public void run(){
		    	//After this timer, the graphics needs to play the new kit animation and then after tell the ConveyorAgent about the new empty kit
		    	//The message to tell the Conveyor about the new kit is Conveyor.msgHeresEmptyKit(new Kit());
 		    	conveyor_state = Conveyor_State.NO_ACTION;
 		    }
 		}, delivery_time);
 	}
 
 }
 
