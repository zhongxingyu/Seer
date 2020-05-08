 package engine.agent.david.agents;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import shared.Glass;
 import shared.interfaces.OfflineWorkstation;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.agent.Agent;
 import engine.agent.david.interfaces.Popup;
 import engine.agent.david.misc.ConveyorFamilyEntity;
 import engine.agent.david.misc.ConveyorFamilyEntity.GlassState;
 import engine.agent.david.misc.ConveyorFamilyEntity.MyGlass;
 import engine.agent.david.misc.ConveyorFamilyEntity.RunningState;
 
 public class PopupAgent extends Agent implements Popup {
 	// *** Constructor(s) ***
 	public PopupAgent(ConveyorFamilyEntity f, Transducer transducer, OfflineWorkstation workstation1, OfflineWorkstation workstation2) {
 		super(f.type+" popup", transducer);
 		family = f;
 		this.workstation1 = workstation1;
 		this.workstation2 = workstation2;
 
 		transducer.register(this, TChannel.SENSOR);
 		transducer.register(this, TChannel.POPUP);
 		transducer.register(this, family.workstationChannel);
 	}
 
 	// *** DATA ***
 	private ConveyorFamilyEntity family;
 	private OfflineWorkstation workstation1; // top workstation, higher priority, one with lower index
 	private OfflineWorkstation workstation2; // bottom workstation
 	private List<MyGlass> glasses = Collections.synchronizedList(new ArrayList<MyGlass>()); // uses MyGlass instead of just Glass so it contains GlassState
 	// A glass is removed from glasses when it is messaged to a workstation. Then, workstation eventually sends glass back,
 	// and the glass is added to finishedGlasses.
 	private List<Glass> finishedGlasses = Collections.synchronizedList(new ArrayList<Glass>());
 	private boolean nextPosFree = true;
 	private boolean sensorOccupied = false; // roughly equivalent to family.runningState.OFF_BC_WAITING_AT_SENSOR, but needed for popup to internally decide to move to ON_BC_SENSOR_TO_POPUP
 	private boolean isUp = false; // up or down; starts out down
 
 	// Mainly used to differentiate between waiting for a transducer event to fire (WAIT_FOR) and when popup should actually check scheduler events (ACTIVE)
 	// if (ACTIVE) is used in scheduler, if (WAIT_FOR_SOMETHING) is used in eventFired to signal the popup is DOING_NOTHING for some animation to finish
 	public enum PopupState {
 		ACTIVE, WAITING_FOR_LOW_POPUP_BEFORE_LOADING_TO_WORKSTATION, WAITING_FOR_HIGH_POPUP_BEFORE_LOADING_TO_WORKSTATION, WAITING_FOR_HIGH_POPUP_BEFORE_RELEASING_FROM_WORKSTATION, WAITING_FOR_LOW_POPUP_WITH_GLASS_FROM_WORKSTATION, WAITING_FOR_LOW_POPUP_BEFORE_RELEASE, WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_RELEASING, WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_LOADING_TO_WORKSTATION, WAITING_FOR_WORKSTATION_GLASS_RELEASE, DOING_NOTHING
 	} // DOING_NOTHING is the default, doing nothing state - neither checking scheduler nor waiting for an animation to finish
 
 	public enum WorkstationState {
 		FREE, BUSY, DONE_BUT_STILL_HAS_GLASS
 	}
 
 	PopupState state = PopupState.DOING_NOTHING;
 	WorkstationState wsState1 = WorkstationState.FREE;
 	WorkstationState wsState2 = WorkstationState.FREE;
 
 	// *** MESSAGES ***
 	@Override
 	public void msgGlassComing(MyGlass myGlass) {
 		print("Received msgGlassComing");
 		glasses.add(myGlass);
 		if (state == PopupState.DOING_NOTHING) {
 			setState(PopupState.ACTIVE);
 			stateChanged(); // only check scheduler if doing nothing
 		}
 	}
 
 	@Override
 	public void msgPositionFree() {
 		print("Received msgPositionFree"); 
 		nextPosFree = true;
 		if (state == PopupState.DOING_NOTHING) {
 			setState(PopupState.ACTIVE);
 			stateChanged(); // only check scheduler if doing nothing
 		}
 	}
 
 	@Override
 	public void msgGlassDone(Glass g, int machineIndex) {
 		print("Received msgGlassDone");
 		updateWorkstationState(machineIndex, WorkstationState.DONE_BUT_STILL_HAS_GLASS);
 		finishedGlasses.add(g);
 
 		if (state == PopupState.DOING_NOTHING) {
 			setState(PopupState.ACTIVE);
 			stateChanged(); // only check scheduler if doing nothing
 		}
 		// otherwise, popup is busy WAITING_FOR something else to happen, or is already ACTIVE doing something perhaps for the other workstation
 	}
 
 	// *** SCHEDULER ***
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// ACTIVE is set by transducer and incoming messages. We only take action if we are 'active'.
 		if (state == PopupState.ACTIVE) {
 //			System.err.println(name+ "'s in scheduler! ");
 			// Case 1 (easy): Just deal with the workstation's finished glass by passing it on. No complications with sensor.
 			if (!sensorOccupied) {
 				// If next position is free and there exists a glass in glasses list that is finished by a workstation
 				if (nextPosFree && atLeastOneWorkstationIsDoneButStillHasGlass()) {
 					// Keep state as ACTIVE. This is implied.
 					print("case 0");
 					actReleaseGlassFromWorkstation();
 					return false;
 				}
 			}
 			// Case 2-x deal with when sensor is occupied, which adds complications.
 			else {
 				// TODONOW: SOMETIMES NULL
 				MyGlass g = getNextUnhandledGlass(); // the *unhandled* glass - we make the glass at the sensor more important than any glass at a workstation
 				if (g != null) { // should be present since sensorOccupied = true
 					print("in popup sched 2");
 					print("nextposfree: "+nextPosFree+"; needs proc: "+g.needsProcessing());
 					
 					// Case 2: Regardless of workstation, just load sensor's glass and pass it on - no workstation interaction
 					if (nextPosFree && !g.needsProcessing()) {
 						print("case 2");
 						actLoadSensorsGlassOntoPopupAndRelease();
 						return false;
 					}
 					// Case 3: Release workstation's finished glass to next family
 					else if (g.needsProcessing() && bothWorkstationsOccupiedButAtLeastOneIsDone() && nextPosFree) {
 						print("case 3");
 						actReleaseGlassFromWorkstation();
 						return false;
 					}
 					// Case 4: Load sensor's glass onto workstation. Must happen after case 3 if case 3 happens.
 					else if (g.needsProcessing() && aWorkstationIsFree()) {
 						print("case 4");
 						actLoadSensorsGlassOntoWorkstation();
 						return false;
 					}
 				} else {
 					System.err.println(" Null unhandled glass! ");
 					
 				}
 			}
 		} // returning true above is actually meaningless since all act methods lead to WAIT state, so we just reach false anyway.
 		setState(PopupState.DOING_NOTHING); // this could interfere with other wait states if you returned true above
 		return false;
 	}
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		// Most checks here involve seeing if state is a form of WAITING_FOR, which happen from scheduler actions.
 
 //		System.out.println(channel + " | " + event);
 
 		// Exception: we must update sensor status regardless of the state.
 		if (!sensorOccupied) { // should only bother to check if sensor is not occupied - here, the popup only cares about listening to see if a glass has arrived at the preceding sensor
 			if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_PRESSED) {
 				// When the sensor right before the popup has been pressed, allow loading of glass onto popup
 				if (family.thisSensor(args)) {
					setState(PopupState.ACTIVE);
 					sensorOccupied = true;
					stateChanged();
//					System.err.println(name+ "'s popup state should do scheduler now ");
 				}
 			}
 		} 
 		
 		// TEMP
 //		if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_PRESSED) {
 //			if (family.thisSensor(args)) {
 //				System.err.println(name+ "'s popup state: "+state);
 //			}
 //		}
 		
 		if (sensorOccupied) {
 			if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_RELEASED) {
 				if (family.thisSensor(args)) {
 					// no state changed because not needed here 
 					sensorOccupied = false;
 				}
 			}
 		}
 			
 		// From actLoadSensorsGlassOntoWorkstation, step 2 (sometimes)
 		if (state == PopupState.WAITING_FOR_LOW_POPUP_BEFORE_LOADING_TO_WORKSTATION) {
 			if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_DOWN) {
 				if (family.thisPopup(args)) {
 					setState(PopupState.WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_LOADING_TO_WORKSTATION);
 					family.doStartConveyor();
 					family.runningState = RunningState.ON_BC_SENSOR_TO_POPUP;
 					family.conv.msgTakingGlass();
 				}
 			}
 		}
 		// From actLoadSensorsGlassOntoWorkstation, step 3
 		else if (state == PopupState.WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_LOADING_TO_WORKSTATION) {
 			if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_LOAD_FINISHED) {
 				if (family.thisPopup(args)) {
 					setState(PopupState.WAITING_FOR_HIGH_POPUP_BEFORE_LOADING_TO_WORKSTATION);
 //					sensorOccupied = false; // done above now
 					family.runningState = RunningState.OFF_BC_QUIET;
 					family.stopSem.release(); // release so conveyor can be caused to move sensor again
 //					System.err.println("RELEASED");
 					family.doStopConveyor();
 					family.doMovePopupUp();
 				}
 			}
 		}
 		// From actLoadSensorsGlassOntoWorkstation, step 4 (final)
 		else if (state == PopupState.WAITING_FOR_HIGH_POPUP_BEFORE_LOADING_TO_WORKSTATION) {
 			if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_UP) {
 				if (family.thisPopup(args)) {
 					setState(PopupState.ACTIVE);
 					MyGlass g = glasses.remove(0); // first glass should be the one
 	
 					OfflineWorkstation w = getWorkstationWithState(WorkstationState.FREE);
 					updateWorkstationState(w, WorkstationState.BUSY);
 					w.msgHereIsGlass(g.getGlass());
 	
 					family.doLoadGlassOntoWorkstation(w.getIndex());
 					stateChanged();
 				}
 			}
 		}
 
 		// From actReleaseGlassFromWorkstation step 2 (sometimes)
 		else if (state == PopupState.WAITING_FOR_HIGH_POPUP_BEFORE_RELEASING_FROM_WORKSTATION) {
 			if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_UP) {
 				if (family.thisPopup(args)) {
 					setState(PopupState.WAITING_FOR_WORKSTATION_GLASS_RELEASE);
 					doReleaseGlassFromProperWorkstation();
					print("JUST RELEASED GLASS FROM PROPER WORKSTATION");
 				}
 			}
 		}
 		// From actReleaseGlassFromWorkstation step 3
 		else if (state == PopupState.WAITING_FOR_WORKSTATION_GLASS_RELEASE) {
 			if (channel == family.workstationChannel && event == TEvent.WORKSTATION_RELEASE_FINISHED) {
 				// Need to check proper workstation? No, because doReleaseGlassFromProperWorkstation() chooses the one that is ready to release.
 				// The scheduler can pick up next time and release the other workstation's glass if it's also done.
 				
 				setState(PopupState.WAITING_FOR_LOW_POPUP_WITH_GLASS_FROM_WORKSTATION);
 				family.doMovePopupDown();
 			}
 		}
 		// From actReleaseGlassFromWorkstation step 4 (final)
 		else if (state == PopupState.WAITING_FOR_LOW_POPUP_WITH_GLASS_FROM_WORKSTATION) {
 			if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_DOWN) {
 				if (family.thisPopup(args)) { // JUSTFIXED
 					setState(PopupState.ACTIVE);
 
 					// Here we can send the next family the message. No need to check POPUP_GUI_RELEASE_FINISHED b/c that is detected _after_ the next family's sensor already gets the glass.
 					Glass glass = finishedGlasses.remove(0); // remove & return first element
 					family.next.msgHereIsGlass(glass);
 					nextPosFree = false;
 
 					family.doReleaseGlassFromPopup();
 
 					stateChanged();
 				}
 			}
 		}
 
 		// From actLoadSensorsGlassOntoPopupAndRelease, step 2 (sometimes)
 		else if (state == PopupState.WAITING_FOR_LOW_POPUP_BEFORE_RELEASE) {
 			if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_DOWN) {
 				if (family.thisPopup(args)) {
 					setState(PopupState.WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_RELEASING);
 					family.doStartConveyor();
 					family.runningState = RunningState.ON_BC_SENSOR_TO_POPUP;
 					family.conv.msgTakingGlass();
 				}
 			}
 		}
 		// From actLoadSensorsGlassOntoPopupAndRelease, step 3 (final)
 		else if (state == PopupState.WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_RELEASING) {
 			if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_LOAD_FINISHED) {
 				if (family.thisPopup(args)) {
 					setState(PopupState.ACTIVE);
 //					sensorOccupied = false; // done above now
 	
 					// Here we can send the next family the message. No need to check POPUP_GUI_RELEASE_FINISHED b/c that is detected _after_ the next family's sensor already gets the glass.
 					MyGlass mg = glasses.remove(0); // should be first glass
 					family.next.msgHereIsGlass(mg.getGlass());
 					nextPosFree = false;
 	
 					family.runningState = RunningState.OFF_BC_QUIET;
 					family.stopSem.release(); // release so conveyor can be caused to move sensor again
 //					System.err.println("RELEASED");
 					family.doStopConveyor();
 					family.doReleaseGlassFromPopup();
 	
 					stateChanged();
 				}
 			}
 		}
 	}
 
 	// *** ACTIONS ***
 	// Note that each act method here sets the popup state to some form of DOING_NOTHING in order to work with animation calls.
 	/**
 	 * Loads glass from sensor onto popup and then releases to next conveyor family Multi-step with eventFired
 	 */
 	public void actLoadSensorsGlassOntoPopupAndRelease() {
 		print("Doing actLoadSensorsGlassOntoPopupAndRelease");
 		if (isUp) {
 			setState(PopupState.WAITING_FOR_LOW_POPUP_BEFORE_RELEASE);
 			family.doMovePopupDown();
 		} else { // popup already down
 			setState(PopupState.WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_RELEASING);
 			family.runningState = RunningState.ON_BC_SENSOR_TO_POPUP;
 			family.doStartConveyor();
 			family.conv.msgTakingGlass();
 		}
 	}
 
 	// Multi-step with eventFired
 	public void actLoadSensorsGlassOntoWorkstation() {
 		print("Doing actLoadSensorsGlassOntoWorkstation");
 		if (isUp) {
 			setState(PopupState.WAITING_FOR_LOW_POPUP_BEFORE_LOADING_TO_WORKSTATION);
 			family.doMovePopupDown();
 		} else { // popup already down
 			setState(PopupState.WAITING_FOR_GLASS_TO_COME_FROM_SENSOR_BEFORE_LOADING_TO_WORKSTATION);
 			family.runningState = RunningState.ON_BC_SENSOR_TO_POPUP;
 			family.doStartConveyor();
 			family.conv.msgTakingGlass();
 		}
 	}
 
 	/**
 	 * Releases glass from workstation to next conveyor family
 	 */
 	public void actReleaseGlassFromWorkstation() {
 		print("Doing actReleaseGlassFromWorkstation");
 		// Note: popup must be up -> WORKSTATION_RELEASE_FINISHED -> POPUP_GUI_LOAD_FINISHED (implied) ->
 		// POPUP_GUI_MOVED_DOWN -> automatically moves on
 
 		// Make sure gui is up first
 		if (!isUp) {
 			setState(PopupState.WAITING_FOR_HIGH_POPUP_BEFORE_RELEASING_FROM_WORKSTATION);
 			family.doMovePopupUp();
 			// System.err.println("here xxx"); 
 		} else { // popup already up
 			setState(PopupState.WAITING_FOR_WORKSTATION_GLASS_RELEASE);
 			doReleaseGlassFromProperWorkstation();
 		}
 	}
 
 	// Choose appropriate workstation and fires WORKSTATION_RELEASE_GLASS. Lower index has higher priority.
 	public void doReleaseGlassFromProperWorkstation() {
 		print("doReleaseGlassFromProperWorkstation");
 		if (wsState1 == WorkstationState.DONE_BUT_STILL_HAS_GLASS) {
 			transducer.fireEvent(workstation1.getChannel(), TEvent.WORKSTATION_RELEASE_GLASS, new Object[] { workstation1.getIndex() });
 			// Important: update workstation to be free again
 			updateWorkstationState(workstation1.getIndex(), WorkstationState.FREE); // index 0
 		} else if (wsState2 == WorkstationState.DONE_BUT_STILL_HAS_GLASS) {
 			transducer.fireEvent(workstation2.getChannel(), TEvent.WORKSTATION_RELEASE_GLASS, new Object[] { workstation2.getIndex() });
 			// Important: update workstation to be free again
 			updateWorkstationState(workstation2.getIndex(), WorkstationState.FREE); // index 1
 		}
 		
 		// No need to move popup down here because this happens in eventFired next
 		// transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, new Object[] { family.getPopupIndex() });
 	}
 
 	// *** EXTRA ***
 	/**
 	 * Returns next MyGlass object from glasses list that either needs processing or doesn't
 	 */
 	private MyGlass getNextUnhandledGlass() {
 		MyGlass g = null;
 		for (MyGlass mg : glasses) {
 			if (mg.getState() == GlassState.NEEDS_PROCESSING || mg.getState() == GlassState.DOES_NOT_NEED_PROCESSING)
 				return mg;
 		}
 		System.err.print("No glass to handle!");
 		return null;
 	}
 
 	// /**
 	// * @return MyGlass object in glasses list with same Glass pointer
 	// */
 	// private MyGlass findGlass(Glass g) { // no need for machine index since glass pointer preserved?
 	// for (MyGlass mg : glasses) {
 	// if (mg.getGlass() == g) {
 	// return mg;
 	// }
 	// }
 	// System.err.print("Could not find glass!");
 	// return null;
 	// }
 
 	// /**
 	// * A safety method for ensuring we choose the correct MyGlass in glasses list based on state
 	// * @return first MyGlass object in glasses list with the given state
 	// */
 	// private MyGlass findGlassWithState(GlassState state) {
 	// for (MyGlass mg : glasses) {
 	// if (mg.getState() == state) {
 	// return mg;
 	// }
 	// }
 	// System.err.print("Could not find glass with state "+state);
 	// return null;
 	// }
 
 	private boolean aWorkstationHasState(WorkstationState state) {
 		return wsState1 == state || wsState2 == state;
 	}
 
 	private void updateWorkstationState(OfflineWorkstation w, WorkstationState state) {
 		if (w.getIndex() % 2 == 0) // 0 or even index means top machine (machine 1)
 			wsState1 = state;
 		else
 			wsState2 = state;
 	}
 
 	private void updateWorkstationState(int machineIndex, WorkstationState state) {
 		if (machineIndex % 2 == 0) // 0 or even index means top machine (machine 1)
 			wsState1 = state;
 		else
 			wsState2 = state;
 	}
 
 	// Returns true if there is a glass that is done processing on a workstation
 	private boolean atLeastOneWorkstationIsDoneButStillHasGlass() {
 		return aWorkstationHasState(WorkstationState.DONE_BUT_STILL_HAS_GLASS);
 	}
 
 	private boolean aWorkstationIsFree() {
 		return aWorkstationHasState(WorkstationState.FREE);
 	}
 
 	private boolean bothWorkstationsOccupiedButAtLeastOneIsDone() {
 		return wsState1 != WorkstationState.FREE && wsState2 != WorkstationState.FREE && atLeastOneWorkstationIsDoneButStillHasGlass();
 	}
 
 	private OfflineWorkstation getWorkstationWithState(WorkstationState s) {
 		if (wsState1 == s) {
 			return workstation1;
 		} else if (wsState2 == s) {
 			return workstation2;
 		} else {
 			return null;
 		}
 	}
 
 	public void setState(PopupState s) {
//		 System.out.println("changing state from " + state + " to " + s);
 		 state = s;
 	}
 	
 	// Testing helpers
 	public void setWorkstationState(int i, WorkstationState s) {
 		if (i == 1) {
 			wsState1 = s;
 		} else {
 			wsState2 = s;
 		}
 	}
 	// public void setNextPosFree(boolean b) {
 	// nextPosFree = b;
 	// }
 	@Override
 	public void setIsUp(boolean b) {
 		isUp = b;
 	}
 
 	@Override
 	public List<Glass> getFinishedGlasses() {
 		return finishedGlasses;
 	}
 
 	@Override
 	public List<MyGlass> getGlasses() {
 		return glasses;
 	}
 
 	@Override
 	public PopupState getState() {
 		return state;
 	}
 
 	@Override	
 	public boolean getNextPosFree() {
 		return nextPosFree;
 	}
 
 	@Override
 	public void seedFinishedGlasses() {
 		Glass g = new Glass();
 		Glass g2 = new Glass();
 
 		finishedGlasses.add(g);
 		finishedGlasses.add(g2);
 	}
 }
