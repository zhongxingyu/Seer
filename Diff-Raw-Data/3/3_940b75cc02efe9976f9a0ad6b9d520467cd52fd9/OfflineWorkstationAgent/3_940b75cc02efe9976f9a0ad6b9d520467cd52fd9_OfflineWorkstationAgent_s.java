 package engine.agent;
 
 import java.util.concurrent.Semaphore;
 
 import shared.Glass;
 import shared.enums.MachineType;
 import shared.interfaces.NonnormBreakInteraction;
 import shared.interfaces.OfflineWorkstation;
 import shared.interfaces.PopupWorkstationInteraction;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 public class OfflineWorkstationAgent extends Agent implements OfflineWorkstation, NonnormBreakInteraction {
 	// *** DATA ***
 	
 	private MachineType mt;
 	private TChannel mtc;
 	private int index;
 	private Glass g;
 	enum GlassState {pending, arrived, processing, done};
 	private GlassState gs;
 	private PopupWorkstationInteraction p;
 	private Semaphore waitSem;
 	
 	enum GUIState {broken, working};
 	private GUIState aniState;
 	private boolean glassBroken;
 	
 	public OfflineWorkstationAgent(String name, MachineType mt, int index, Transducer trans) {
 		super(name, trans);
 		
 		this.mt = mt;
 		this.index = index;
 		
 		mtc = mt.getChannel();
 		g = null;
 		gs = null;
 		waitSem = new Semaphore(0);
 		aniState = GUIState.working;
 		glassBroken = false;
 		
 		transducer.register(this, mtc);
 	}
 	
 	// *** MESSAGES ***
 	
 	/* Set current piece of glass. */
 	public void msgHereIsGlass(Glass glass) {
 		print("Received msgHereIsGlass");
 		g = glass;
 		gs = GlassState.pending;
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
 	
 	/* Transducer event. All events are on this workstation's machine type TChannel. */
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		if ((Integer)args[0] == index) {
 			if (event == TEvent.WORKSTATION_LOAD_FINISHED) {
 				print("WORKSTATION_LOAD_FINISHED");
 				gs = GlassState.arrived;
 				stateChanged();
 			} else if (event == TEvent.WORKSTATION_GUI_ACTION_FINISHED) {
 				print("WORKSTATION_GUI_ACTION_FINISHED");
 				waitSem.release(); // don't need stateChanged because sem release wakes agent
 			} else if (event == TEvent.WORKSTATION_REMOVED_GLASS) {
 				glassBroken = true;
 				if (gs == GlassState.processing)
 					waitSem.release();
 				stateChanged();
 			}
 		}
 	}
 	
 	/* Scheduler.  Determine what action is called for, and do it. */
 	public boolean pickAndExecuteAnAction() {
 		if (glassBroken) {
 			removeBrokenGlassAndMessagePopup();
 			return true;
 		}
 		if (aniState == GUIState.working) {		
 			if (gs == GlassState.arrived) {
 				processGlass();
 				return true;
 			}
 		}
 		return false;
 	}
 
 	
 	// *** ACTIONS ***
 	
 	/* Remove a piece of broken glass and alert popup agent. */
 	private void removeBrokenGlassAndMessagePopup() {
 		g = null;
 		gs = GlassState.done;
 		p.msgGUIBreakRemovedGlassFromWorkstation(index);
 		glassBroken = false;
 	}
 
 	/* Tell animation to process glass, wait for it to finish, then send msgGlassDone. */
 	private void processGlass() {
 		print("processing glass");
 		doStartProcessing();
 		doWaitProcessing();
		p.msgGlassDone(g, index);
 		// Popup will take glass when it is ready, Workstation now waits for next glass to arrive
 	}
 
 	
 	// *** ANIMATION ACTIONS ***
 	
 	/* Tell animation to start processing glass. */
 	private void doStartProcessing() {
 		Integer args[] = {index};
 		transducer.fireEvent(mtc, TEvent.WORKSTATION_DO_ACTION, args); // originally WORKSTATION_DO_LOAD_GLASS, but this is incorrect?
 		gs = GlassState.processing;
 	}
 	
 	/* Wait for animation to finish processing glass. */
 	private void doWaitProcessing() {
 		try {
 			waitSem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		gs = GlassState.done;
 	}
 	
 	// *** EXTRA ***
 	
 	/* Getters/Setters */
 	public MachineType getType() {
 		return mt;
 	}
 	
 	public TChannel getChannel() {
 		return mtc;
 	}
 	
 	public int getIndex() {
 		return index;
 	}
 	
 	public void setPopupWorkstationInteraction(PopupWorkstationInteraction p) {
 		this.p = p;
 	}
 }
