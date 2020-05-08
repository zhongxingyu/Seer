 package engine.agent.tim.agents;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import shared.Glass;
 import shared.interfaces.OfflineConveyorFamily;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.agent.Agent;
 import engine.agent.tim.interfaces.Conveyor;
 import engine.agent.tim.misc.ConveyorEvent;
 import engine.agent.tim.misc.ConveyorFamilyImp;
 import engine.agent.tim.misc.MyGlassConveyor;
 import engine.agent.tim.misc.MyGlassConveyor.conveyorState;
 
 public class ConveyorAgent extends Agent implements Conveyor {
 	//Name: ConveyorAgent
 
 	//Description:  Will hold the glass until it needs to go into the next conveyor for a different set of processes, or to leave the factory entirely.
 
 	//Data:
 	private List<MyGlassConveyor> glassSheets; // List to hold all of the glass sheets
 	private volatile boolean conveyorOn; // Is the Gui conveyor on?
 	private ConveyorFamilyImp cf; // Reference to the current conveyor family
 	
 	private List<ConveyorEvent> events; // Used to hold all of the sensor events
 	
 	int guiIndex; // Needed to communicate with the transducer conveyor
 	
 	boolean positionFreePopUp;
 	
 	private enum GUIBreakState {stop, stopped, restart, running};
 	GUIBreakState guiBreakState = GUIBreakState.running; // Value that determines whether the GUI conveyor is broken or not	
 	
 	// Constructors:
 	public ConveyorAgent(String name, Transducer transducer, int guiIndex) {
 		// Set the passed in values first
 		super(name, transducer);
 		
 		// Then set the values that need to be initialized within this class, specifically
 		this.glassSheets = Collections.synchronizedList(new ArrayList<MyGlassConveyor>());
 		this.conveyorOn = false; // The conveyor is off when this simulation starts
 		
 		this.events = Collections.synchronizedList(new ArrayList<ConveyorEvent>());
 		
 		this.guiIndex = guiIndex;
 		
 		this.positionFreePopUp = true;
 		
 		initializeTransducerChannels();
 	}
 	
 	private void initializeTransducerChannels() { // Initialize the transducer channels and everything else related to it
 		// Register any appropriate channels
 		transducer.register(this, TChannel.CONVEYOR); // Set this agent to listen to the CONVEYOR channel of the transducer
 		transducer.register(this, TChannel.POPUP); // Set this agent to listen to the CONVEYOR channel of the transducer
 	}
 
 	//Messages:
 	public void msgGiveGlassToConveyor(Glass g) { // Add glass to the conveyor
 		glassSheets.add(new MyGlassConveyor(g, conveyorState.beforeEntrySensor)); // conveyorState will always initializes to onConveyor
 		print("Glass with ID (" + g.getID() + ") added to conveyor");
 		stateChanged();
 	}
 	
 	public void msgPositionFree() { // Allow this conveyor to pass a piece of glass to the next conveyor family
 		events.add(ConveyorEvent.popUpFree);
 		positionFreePopUp = true;
 		print("Event added: PopUpFree.");
 		stateChanged();
 	}
 
 	public void msgUpdateGlass(ConveyorEvent e) { // This message is akin to a stub, but I wanted to match up to my current interaction diagram  I could just call msgGiveGlassToConveyor directly, but the semantics do not look as good that way
 		events.add(e);
 		print("Event added: " + e.toString());
 		stateChanged();
 	}
 
 	/* This message is from the GUI to stop or restart. */
 	public void msgGUIBreak(boolean stop) {
 		if (stop && guiBreakState == GUIBreakState.running) {
 			guiBreakState = GUIBreakState.stop;
 			stateChanged();
 		} 
 		else if (!stop && guiBreakState == GUIBreakState.stopped) {
 			guiBreakState = GUIBreakState.restart;
 			stateChanged();
 		}
 	}
 	
 	//Scheduler:
 	public boolean pickAndExecuteAnAction() {
 		// Check to see if a GUI break message came in
 		if (guiBreakState == GUIBreakState.stop) {
 			actBreakConveyorOff();
 			return false; // Make sure the method does not call again
 		}
 		
 		else if (guiBreakState == GUIBreakState.restart) {
 			actBreakConveyorOn();
 			return true; 		
 		}
 		
 		else if (guiBreakState == GUIBreakState.stopped) {
 			return false; // C'mon the Conveyor is broken!  It shouldn't run until this state is changed
 		}
 		
 		if (events.isEmpty()) { return false; }
 		
 		ConveyorEvent e = events.remove(events.size() - 1);
 		
 		MyGlassConveyor glass = null; // Use null variable for determining is value is found from synchronized loop
 		
 		if (e == ConveyorEvent.onEntrySensor) {
 			synchronized(glassSheets) {
 				for (MyGlassConveyor g: glassSheets) {
 					if (g.conveyorState == conveyorState.beforeEntrySensor) {
 						glass = g;
 						break;
 					}
 				}
 			}
 		}
 		if (glass != null) {
 			actSetGlassOnEntrySensor(glass); return true;
 		}
 		
 		if (e == ConveyorEvent.offEntrySensor) {
 			synchronized(glassSheets) {
 				for (MyGlassConveyor g: glassSheets) {
 					if (g.conveyorState == conveyorState.onEntrySensor) {
 						glass = g;
 						break;
 					}
 				}
 			}		
 		}
 		if (glass != null) {
 			actSetGlassOffEntrySensor(glass); return true;
 		}
 		
 		if (e == ConveyorEvent.onPopUpSensor) {
 			synchronized(glassSheets) {
 				for (MyGlassConveyor g: glassSheets) {
 					if (g.conveyorState == conveyorState.beforePopUpSensor) {
 						glass = g;
 						break;
 					}
 				}
 			}			
 		}
 		if (glass != null) {
 			actSetGlassOnPopUpSensor(glass); return true;
 		}
 		
 		if ((e == ConveyorEvent.popUpFree || positionFreePopUp)) {
 			synchronized(glassSheets) {
 				for (MyGlassConveyor g: glassSheets) {
 					if (g.conveyorState == conveyorState.onPopUpSensor) {
 						glass = g;
 						break;
 					}
 				}
 			}		
 		}
 		if (glass != null) {
 			actTurnOnConveyorAndSendGlass(glass); return true;
 		}
 		
 		if (e == ConveyorEvent.offPopUpSensor) {
 			synchronized(glassSheets) {
 				for (MyGlassConveyor g: glassSheets) {
 					if (g.conveyorState == conveyorState.onPopUpSensor) {
 						glass = g;
 						break;
 					}
 				}
 			}		
 		}
 		if (glass != null) {
 			actSetGlassOffPopUpSensor(glass); return true;
 		}
 		
 		if (e == ConveyorEvent.onPopUp) {
 			synchronized(glassSheets) {
 				for (MyGlassConveyor g: glassSheets) {
 					if (g.conveyorState == conveyorState.beforePopUp) {
 						glass = g;
 						break;
 					}
 				}
 			}		
 		}
 		
 		if (glass != null) {
 			actSetGlassOffConveyor(glass); return true;
 		}
 		
 		print("An event did not match a piece of glass's state, that's a problem");
 		
 		return false;		
 	}
 	
 	//Actions:
 	
 	private void actSetGlassOnEntrySensor(MyGlassConveyor g) {
 		g.conveyorState = conveyorState.onEntrySensor;
 		print("MyGlass " + g.glass.getID() + " at conveyorState: " + g.conveyorState.toString());
 		if (positionFreePopUp) {
 			turnOnConveyorGUI();
 		}
 	}
 
 	private void actSetGlassOffEntrySensor(MyGlassConveyor g) {
 		g.conveyorState = conveyorState.beforePopUpSensor;
 		print("MyGlass " + g.glass.getID() + " at conveyorState: " + g.conveyorState.toString());
 //		if (cf.getPrevCF() != null) {
 //			cf.getPrevCF().msgPositionFree();
 //			print("msgPositionFree() sent to previous LineComponent");
 //		}
 	}
 
 	private void actSetGlassOnPopUpSensor(MyGlassConveyor g) {
 		g.conveyorState = conveyorState.onPopUpSensor;
 		print("MyGlass " + g.glass.getID() + " at conveyorState: " + g.conveyorState.toString());
 		turnOffConveyorGUI(); 
 		cf.getPopUp().msgGiveGlassToPopUp(g.glass);
 		// Temp measure to see if weird glass overlapping issue stops when sending the message from OffEntrySensor when just breaking the conveyor
 		if (cf.getPrevCF() != null) {
 			cf.getPrevCF().msgPositionFree();
 			print("msgPositionFree() sent to previous LineComponent");
 		}
 		positionFreePopUp = false; // Wait for the popUp to send the msgPositionFree message to allow the conveyor to turn back on
 	}
 
 	private void actTurnOnConveyorAndSendGlass(MyGlassConveyor g) {
 		turnOnConveyorGUI(); 
 		g.conveyorState = conveyorState.referenceJustsentToPopUp;
 		
 	}
 
 	private void actSetGlassOffPopUpSensor(MyGlassConveyor g) {
 		g.conveyorState = conveyorState.beforePopUp;
 		print("MyGlass " + g.glass.getID() + " at conveyorState: " + g.conveyorState.toString());
 	}
 	
 	private void actSetGlassOffConveyor(MyGlassConveyor g) {
 		glassSheets.remove(g);
 		print("MyGlass " + g.glass.getID() + " removed from conveyor");
 		if (glassSheets.size() == 0) {
 			turnOffConveyorGUI();
 		}
 	}
 	
 	// New Non-norm GUI actions
 	private void actBreakConveyorOff() {
 		turnOffConveyorGUI();
 		guiBreakState = GUIBreakState.stopped; 
 	}
 	
 	private void actBreakConveyorOn() {
 		// First Check -- Is there no glass on popUp, is there a free machineCom, and it glass currently on a workstation
		if (!cf.getPopUp().isGlassOnPopUp() && cf.getPopUp().getFreeChannels() > 0 && !cf.getPopUp().isGlassOnWorkstation()) {
 			turnOnConveyorGUI();
 		}		
 		// Second Check -- there is nothing currently on the popUp Sensor
 		boolean glassOnPopUpSensor = false;
 		synchronized(glassSheets) {			
 			for (MyGlassConveyor g: glassSheets) {
 				if (g.conveyorState == conveyorState.onPopUpSensor || g.conveyorState == conveyorState.beforePopUp) {
 					glassOnPopUpSensor = true;
 					break;
 				}
 			}
 		}		
 		if (!glassOnPopUpSensor) { // Turn on the conveyor, so the glass can make it to the popUp sensor
 			turnOnConveyorGUI();
 		}
 		guiBreakState = GUIBreakState.running;
 	}
 
 	//Other Methods:
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		// N/A
 	}
 	
 	// Methods that turn the GUI conveyor on or off	
 	private void turnOnConveyorGUI() {
 		if (!conveyorOn) {
 			Integer[] args = {guiIndex};
 			transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
 			print("Turned on conveyor " + guiIndex);
 			conveyorOn = true;
 		}
 	}
 	
 	private void turnOffConveyorGUI() {
 		if (conveyorOn) {
 			Integer[] args = {guiIndex};
 			transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);
 			print("Turned off conveyor " + guiIndex);
 			conveyorOn = false;
 		}
 	}
 
 	// Getters and setters
 	
 	/**
 	 * @return the conveyorOn
 	 */
 	public boolean isConveyorOn() {
 		return conveyorOn;
 	}
 
 	/**
 	 * @return the glassSheets
 	 */
 	public List<MyGlassConveyor> getGlassSheets() {
 		return glassSheets;
 	}
 
 	@Override
 	public void setCF(OfflineConveyorFamily conveyorFamilyImp) {
 		cf = (ConveyorFamilyImp) conveyorFamilyImp;		
 	}
 
 	/**
 	 * @return the events
 	 */
 	public List<ConveyorEvent> getEvents() {
 		return events;
 	}
 
 	@Override
 	public Integer getGUIIndex() {
 		return guiIndex;
 	}
 }
