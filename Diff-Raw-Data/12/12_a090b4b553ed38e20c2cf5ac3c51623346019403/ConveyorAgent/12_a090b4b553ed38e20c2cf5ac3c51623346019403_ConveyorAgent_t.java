 package engine.agent.david.agents;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import shared.Glass;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.agent.Agent;
 import engine.agent.david.interfaces.Conveyor;
 import engine.agent.david.misc.ConveyorFamilyEntity;
 import engine.agent.david.misc.ConveyorFamilyEntity.GlassState;
 import engine.agent.david.misc.ConveyorFamilyEntity.MyGlass;
 import engine.agent.david.misc.ConveyorFamilyEntity.RunningState;
 
 public class ConveyorAgent extends Agent implements Conveyor {
 	// *** Constructor(s) ***
 	public ConveyorAgent(ConveyorFamilyEntity f, Transducer transducer) {
 		super(f.type+" conveyor", transducer);
 		family = f;
 		transducer.register(this, TChannel.SENSOR);
 	}
 	
 	// *** DATA ***
 	private ConveyorFamilyEntity family;
 	public enum ConveyorState { GLASS_JUST_ARRIVED, WAITING_FOR_GLASS_TO_REACH_ENDING_SENSOR, SHOULD_NOTIFY_POSITION_FREE, NOTHING_TO_DO }
 	private ConveyorState state = ConveyorState.NOTHING_TO_DO;
 	private List<Glass> glasses = Collections.synchronizedList(new ArrayList<Glass>());
 	
 	// Testing variables
 //	private Timer timer = new Timer();
 //	private final int REST_INTERVAL = 1000*4;
 
 	// *** MESSAGES ***
 	@Override
 	public void msgHereIsGlass(Glass g) {
 		print("Received msgHereIsGlass");
 		state = ConveyorState.GLASS_JUST_ARRIVED; // previous sensor should have already started the conveyor
 		// at this point, this should be true: family.runningState == RunningState.ON_BC_SENSOR_TO_CONVEYOR
 		glasses.add(g);
 		stateChanged();
 	}
 
 	@Override
 	public void msgTakingGlass() {
 		print("Received msgTakingGlass");
 		state = ConveyorState.SHOULD_NOTIFY_POSITION_FREE;
 		glasses.remove(0);
 		stateChanged();
 	}
 	
 	@Override
 	public void msgGUIBreak(boolean stop) {
 		print("Received msgGUIBreak: "+stop);
 		if (stop) { // break
 			family.acquireSem(family.brokenStopSem); // TODONOW
 			
 			family.prevRState = family.runningState;
//			System.err.println("state saved: "+family.runningState);
 			family.runningState = RunningState.OFF_BC_BROKEN;
 			family.doStopConveyor();
 		} else { // unbreak
 			family.runningState = family.prevRState;
 			family.prevRState = null;
//			System.err.println("state brought back: "+family.runningState);
 			
 			if (family.isRunning()) // if runningState is one of the ON_ ones, make sure conveyor is running b/c that's what it was doing before
 				family.doStartConveyor();
 			
 			family.brokenStopSem.release();
 		}
 		stateChanged();
 	}
 	
 	// *** SCHEDULER ***
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		if (family.runningState != RunningState.OFF_BC_BROKEN) {
 			if (state == ConveyorState.GLASS_JUST_ARRIVED) {
 				// !glasses.isEmpty() should be true
 				state = ConveyorState.WAITING_FOR_GLASS_TO_REACH_ENDING_SENSOR;
 				actTellPopupGlassOnConveyor(glasses.get(0));
 				return true;
 			} else if (state == ConveyorState.WAITING_FOR_GLASS_TO_REACH_ENDING_SENSOR) { // technically could be merged into NOTHING_TO_DO
 				// Do nothing. Next thing that happens is conveyor auto-stops via eventFired, popup agent realizes sensorOccupied = true, 
 				// does actLoadGlassOntoPopup which *then tells this conveyor agent msgTakingGlass()*
 	//			return false;
 			} else if (state == ConveyorState.SHOULD_NOTIFY_POSITION_FREE) {
 				state = ConveyorState.NOTHING_TO_DO;
 				actTellSensorPositionFree();
 	//			return false;
 			} else { // NOTHING_TO_DO
 	//			return false;
 			}
 		}
 		
 		// Testing purposes
 //		// Schedule to wake us up every now and then
 //		timer.schedule(new TimerTask() { // "anonymous inner class"
 //			public void run() {
 //				System.err.println(family.runningState);
 //				stateChanged();
 //			}
 //		}, (int) (REST_INTERVAL));
 		
 		return false;
 	}
 	
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		if (state == ConveyorState.WAITING_FOR_GLASS_TO_REACH_ENDING_SENSOR) {
 			if (channel == TChannel.SENSOR) {
 				// When the sensor right after the conveyor has been pressed, stop the conveyor
 				if (event == TEvent.SENSOR_GUI_PRESSED) {
 					if (family.thisSensor(args)) {
 						family.doStopConveyor();
 						family.runningState = RunningState.OFF_BC_WAITING_AT_SENSOR;
 					}
 				}
 			}
 		}
 	}
 	
 	// *** ACTIONS ***
 	public void actTellPopupGlassOnConveyor(Glass g) {
 		print("Doing actTellPopupGlassOnConveyor");
 		GlassState glassState = family.decideIfGlassNeedsProcessing(g); // conveyor decides this since it has time
 		MyGlass myGlass = family.new MyGlass(g, glassState);
 
 		family.runningState = RunningState.ON_BC_CONVEYOR_TO_SENSOR;
 		family.popup.msgGlassComing(myGlass);
 
 		// Trust that conveyor knows to stop glass the moment the right sensor fires. See eventFired.
 	}
 
 	public void actTellSensorPositionFree() {
 		print("Doing actTellSensorPositionFree");
 		family.sensor.msgPositionFree();
 	}
 	
 	// *** EXTRA ***
 	public ConveyorState getState() {
 		return state;
 	}
 	public List<Glass> getGlasses() {
 		return glasses;
 	}
 }
