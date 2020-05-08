 package engine.agent.david.misc;
 
 import java.util.concurrent.Semaphore;
 
 import shared.Glass;
 import shared.enums.MachineType;
 import shared.interfaces.LineComponent;
 import shared.interfaces.OfflineConveyorFamily;
 import shared.interfaces.OfflineWorkstation;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.agent.david.agents.ConveyorAgent;
 import engine.agent.david.agents.PopupAgent;
 import engine.agent.david.agents.SensorAgent;
 import engine.agent.david.interfaces.Conveyor;
 import engine.agent.david.interfaces.Popup;
 import engine.agent.david.interfaces.Sensor;
 import engine.agent.david.test.mock.MockConveyor;
 import engine.agent.david.test.mock.MockConveyorFamily;
 import engine.agent.david.test.mock.MockPopup;
 import engine.agent.david.test.mock.MockSensor;
 
 /**
  * Key class that represents my version of the ConveyorFamily design (hence its implementation of ConveyorFamily). Contains agents to represent the ConveyorFamily, whose interface is broadly used by
  * other team members. Follows the "sensor-conveyor-popup" pattern, so the sensor b/w the conveyor and popup is absorbed.
  * 
  * @author David Zhang
  */
 public class ConveyorFamilyEntity implements OfflineConveyorFamily {
 	// *** Constructor(s) ***
 	public ConveyorFamilyEntity(Transducer transducer, int convIndex, int popupIndex, OfflineWorkstation workstation1, OfflineWorkstation workstation2) {
 		this.t = transducer;
 		this.type = workstation1.getType(); // workstations should have same type
 		this.conveyorIndex = convIndex;
 		this.popupIndex = popupIndex;
 		this.workstationChannel = workstation1.getChannel();
 
 		sensor = new SensorAgent(this, transducer);
 		conv = new ConveyorAgent(this, transducer);
 		popup = new PopupAgent(this, transducer, workstation1, workstation2);
 	}
 	
 	// secondary, not really used
 	public ConveyorFamilyEntity(Transducer transducer, OfflineWorkstation workstation1, OfflineWorkstation workstation2) {
 		this.t = transducer;
 		this.type = workstation1.getType(); // workstations should have same type
 		this.conveyorIndex = 0; // default
 		this.workstationChannel = workstation1.getChannel();
 
 		sensor = new SensorAgent(this, transducer);
 		conv = new ConveyorAgent(this, transducer);
 		popup = new PopupAgent(this, transducer, workstation1, workstation2);
 	}
 
 	// *** DATA - mostly accessible by contained agents ***
 	// acquired in sensor, released in popup agent
 	public Semaphore stopSem = new Semaphore(1); // check if conveyor is off because quiet (when popup officially taken a glass); when release, conveyor can move again
 	
 	// acquired & released in sensor and conveyor agents
 	public Semaphore brokenStopSem = new Semaphore(1); // check if broken conveyor
 	
 	private Transducer t;
 	public MachineType type;
 	private int conveyorIndex, popupIndex;
 	public TChannel workstationChannel; // should be same for both workstations
 
 	public Sensor sensor;
 	public Conveyor conv;
 	public Popup popup;
 
 	public LineComponent next;
 	public LineComponent prev;
 
 	public enum GlassState {
 		NEEDS_PROCESSING, DOES_NOT_NEED_PROCESSING
 	}
 
 	// State of conveyor family so we know if the conveyor is on or off because (BC) of whatever reasons; mainly used for testing/validation
 	public RunningState runningState = RunningState.OFF_BC_QUIET;
 	public RunningState prevRState = null; // only to save prev state if broken
 	public enum RunningState {
 		// On states are listed in order of how they would appear. Off states come in between.
 		ON_BC_SENSOR_TO_CONVEYOR, ON_BC_CONVEYOR_TO_SENSOR, ON_BC_SENSOR_TO_POPUP, OFF_BC_QUIET, OFF_BC_WAITING_AT_SENSOR, OFF_BC_BROKEN
 	}
 
 	public class MyGlass {
 		public MyGlass(Glass g, GlassState s) {
 			this.glass = g;
 			this.state = s;
 		}
 
 		private Glass glass;
 		private GlassState state;
 
 		public boolean needsProcessing() {
 			return state == GlassState.NEEDS_PROCESSING;
 		}
 
 		public void setState(GlassState s) {
 			state = s;
 		}
 
 		public GlassState getState() {
 			return state;
 		}
 
 		public Glass getGlass() {
 			return glass;
 		}
 	}
 
 	// *** MESSAGES - just passes on immediately to appropriate agent ***
 	public void msgHereIsGlass(Glass g) {
 		sensor.msgHereIsGlass(g);
 	}
 
 	public void msgPositionFree() {
 		popup.msgPositionFree();
 	}
 
 	public void msgGlassDone(Glass g, int index) {
 		popup.msgGlassDone(g, index); // pass to popup
 	}
 	
 	// Side-effect of interface extension - these 3 are ignored. These are never sent to the family.
 	@Override
 	public void msgGUIBreakWorkstation(boolean stop, int index) {
 	}
 
 	@Override
 	public void msgGUIBreakRemovedGlassFromWorkstation(int index) {
		popup.msgGUIBreakRemovedGlassFromWorkstation(index); // just forward because of workstation change
 	}
 
 	@Override
 	public void msgGUIBreak(boolean stop) {
 	}
 
 	// *** TRANSDUCER / ANIMATION CALLS ***
 	public void doStartConveyor() {
 //		System.err.println("index of conveyor that is starting: "+conveyorIndex);
 		t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, new Integer[] { conveyorIndex });
 	}
 
 	public void doStopConveyor() {
 //		System.err.println("conveyor STOPPED index "+conveyorIndex);
 		t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, new Integer[] { conveyorIndex });
 	}
 
 	public void doMovePopupUp() {
 //		System.err.println("doing move popup up for popup index "+popupIndex);
 		t.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_UP, new Integer[] { popupIndex });
 		popup.setIsUp(true);
 	}
 
 	public void doMovePopupDown() {
 		t.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, new Integer[] { popupIndex });
 		popup.setIsUp(false);
 	}
 
 	public void doReleaseGlassFromPopup() {
 		t.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, new Integer[] { popupIndex });
 	}
 
 	public void doLoadGlassOntoWorkstation(int workstationIndex) {
 		t.fireEvent(workstationChannel, TEvent.WORKSTATION_DO_LOAD_GLASS, new Integer[] { workstationIndex });
 	}
 
 	// *** EXTRA ***
 
 	public void startThreads() {
 		((SensorAgent) sensor).startThread();
 		((ConveyorAgent) conv).startThread();
 		((PopupAgent) popup).startThread();
 	}
 	
 	// Quick helpers for parsing args in eventFired
 	public boolean thisSensor(Object args[]) {
 		return (Integer) args[0] == getSensorIndex();
 	}
 
 	public boolean thisPopup(Object args[]) {
 		return (Integer) args[0] == getPopupIndex();
 	}
 
 	public boolean thisConveyor(Object args[]) {
 		return (Integer) args[0] == getConveyorIndex();
 	}
 
 	public void setNextLineComponent(LineComponent l) {
 		next = l;
 	}
 
 	public void setPreviousLineComponent(LineComponent l) {
 		prev = l;
 	}
 
 	public GlassState decideIfGlassNeedsProcessing(Glass g) {
 		GlassState gs = null;
 		if (g.getNeedsProcessing(this.type))
 			gs = GlassState.NEEDS_PROCESSING;
 		else
 			gs = GlassState.DOES_NOT_NEED_PROCESSING;
 		return gs;
 	}
 
 	public int getSensorIndex() { // based on conveyor index
 		return conveyorIndex * 2 + 1; // returns 2nd sensor
 	}
 
 	public int getConveyorIndex() {
 		return conveyorIndex;
 	}
 
 	public void setConveyorIndex(int i) {
 		conveyorIndex = i;
 	}
 
 	public int getPopupIndex() {
 		return popupIndex;
 	}
 
 	public void setPopupIndex(int i) {
 		popupIndex = i;
 	}
 
 	public Conveyor getConveyor() {
 		return conv;
 	}
 
 	public Popup getPopup() {
 		return popup;
 	}
 
 	/**
 	 * Quick helper to call acquire on s - so we don't need try catch all the time
 	 * @param s
 	 */
 	public void acquireSem(Semaphore s) {
 		try {
 			s.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean isRunning() {
 		return runningState == RunningState.ON_BC_CONVEYOR_TO_SENSOR || runningState == RunningState.ON_BC_SENSOR_TO_CONVEYOR || runningState == RunningState.ON_BC_SENSOR_TO_POPUP; 
 	}
 	
 	/* Testing helpers */
 	public void setConveyor(Conveyor c) {
 		conv = c;
 	}
 
 	public void setSensor(Sensor s) {
 		sensor = s;
 	}
 
 	public void setPopup(Popup p) {
 		popup = p;
 	}
 
 	public MockSensor getMockSensor() {
 		return (MockSensor) sensor;
 	}
 
 	public MockPopup getMockPopup() {
 		return (MockPopup) popup;
 	}
 
 	public MockConveyor getMockConveyor() {
 		return (MockConveyor) conv;
 	}
 
 	public MockConveyorFamily getMockPrevConveyorFamily() {
 		return (MockConveyorFamily) prev;
 	}
 
 	public MockConveyorFamily getMockNextConveyorFamily() {
 		return (MockConveyorFamily) next;
 	}
 }
