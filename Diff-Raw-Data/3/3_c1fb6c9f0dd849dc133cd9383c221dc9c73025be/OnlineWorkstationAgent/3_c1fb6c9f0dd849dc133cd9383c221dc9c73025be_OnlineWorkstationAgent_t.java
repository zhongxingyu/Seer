 package engine.agent;
 
 import java.util.concurrent.Semaphore;
 
 import shared.Glass;
 import shared.enums.MachineType;
 import shared.interfaces.LineComponent;
 import shared.interfaces.NonnormBreakInteraction;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 public class OnlineWorkstationAgent extends Agent implements LineComponent, NonnormBreakInteraction {
 	private MachineType type;
 	private TChannel channel;
 	private Glass glass;
 
 	enum GlassState {pending, arrived, processing, processed, releasing, released};
 	enum GUIState {broken, working};
 
 	private GlassState state;
 	private GUIState aniState;
 	private LineComponent prev, next;
 	private Semaphore aniSem;
 	private boolean recPosFree;
 
 	public OnlineWorkstationAgent(String name, MachineType mt, Transducer t) {
 		super(name, t);
 		type = mt;
 		recPosFree = true;
 		channel = type.getChannel();
 		aniSem = new Semaphore(0);
 		aniState = GUIState.working;
 		transducer.register(this, channel);
 	}
 
 	// *** MESSAGES ***
 	
 	public void msgHereIsGlass(Glass g) {
 		print("received: " + g);
 		glass = g;
 		state = GlassState.pending;
 		stateChanged();
 	}
 
 	public void msgPositionFree() {
 		recPosFree = true;
 		stateChanged();
 	}
 
 	/* For the GUI break interaction to stop or start working again. */
 	public void msgGUIBreak(boolean stop) {
 		if (stop) {
 			aniState = GUIState.broken;
 		} else {
 			aniState = GUIState.working;
 		}
 		stateChanged();
 	}
 	
 	/* Transducer event. Always on the <type> TChannel. */
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		if (event == TEvent.WORKSTATION_LOAD_FINISHED) {
 			state = GlassState.arrived;
 			stateChanged();
 		} else if (event == TEvent.WORKSTATION_GUI_ACTION_FINISHED) {
 			aniSem.release();
 		} else if (event == TEvent.WORKSTATION_RELEASE_FINISHED) {
 			state = GlassState.released;
 			stateChanged();
 		}
 	}
 
 	// *** SCHEDULER ***
 	public boolean pickAndExecuteAnAction() {
 		if (aniState == GUIState.working) {
 			if (state == GlassState.arrived) {
 				processGlass();
 				return true;
 			}
 			if (state == GlassState.processed && recPosFree) {
 				releaseGlass();
 				return true;
 			}
 			if (state == GlassState.released) {
 				reset();
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// *** ACTIONS ***
 	
 	/* Process the glass if it needs to be processed. */
 	private void processGlass() {
 		if (glass.getNeedsProcessing(type)) {
 			doStartProcessing();
 			doWaitProcessing();
 		}
 		state = GlassState.processed;
 	}
 	
 	private void releaseGlass() {
 		transducer.fireEvent(channel, TEvent.WORKSTATION_RELEASE_GLASS, null);
 		state = GlassState.releasing;
		recPosFree = false;
 		
 		next.msgHereIsGlass(glass);
 	}
 	
 	private void reset() {
 		state = null;
 		glass = null;
 		prev.msgPositionFree();
 	}
 	
 	// *** ANIMATION ACTIONS ***
 	
 	private void doStartProcessing() {
 		transducer.fireEvent(channel, TEvent.WORKSTATION_DO_ACTION, null);
 		state = GlassState.processing;
 	}
 	
 	private void doWaitProcessing() {
 		try {
 			aniSem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	// *** ACCESSORS & MUTATORS ***
 	
 	public MachineType getType() {
 		return type;
 	}
 	
 	public TChannel getChannel() {
 		return channel;
 	}
 	
 	/* Setters */
 	public void setPrev(LineComponent lc) { prev = lc; }
 	public void setNext(LineComponent lc) { next = lc; }
 }
