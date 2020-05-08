 package engine.agent.tim.agents;
 
 import java.util.*;
 import java.util.concurrent.Semaphore;
 
 import engine.agent.Agent;
 import engine.agent.OfflineWorkstationAgent;
 import engine.agent.tim.interfaces.Machine;
 import engine.agent.tim.interfaces.PopUp;
 import engine.agent.tim.misc.ConveyorFamilyImp;
 import engine.agent.tim.misc.MachineCom;
 import engine.agent.tim.misc.MyGlassPopUp;
 import engine.agent.tim.misc.MyGlassPopUp.processState;
 import shared.Glass;
 import shared.enums.MachineType;
 import shared.interfaces.OfflineConveyorFamily;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 public class PopUpAgent extends Agent implements PopUp {
 
 	// Name: PopUpAgent
 
 	// Description:  Will act as a mediator between the conveyor agent and the robot agents for getting glass to the processing machines, if necessary.
 	// Of course, this agent may not be needed because there is NO ROBOT in the animation. but I will leave it in for now.
 
 	// Data:	
 	private List<MyGlassPopUp> glassToBeProcessed; // This name will be abbreviated as glassToBeProcessed in many functions to save on space and complexity
 	private List<MachineCom> machineComs; // Channels for communicating with the machines, since there will most likely be two per offline process
 
 	// Positional variable for whether the Pop-Up in the GUI is up or down, and it will be changed through the transducer and checked within one of the scheduler rules
 	private boolean popUpDown; // Is this value is true, then the associated popUp is down (will be changed through the appropriate transducer eventFired(args[]) function.
 	
 	private ConveyorFamilyImp cf; // Reference to the current conveyor family
 	
 	private MachineType processType; // Will hold what the concurrent workstation agents can process for any given popUp  it is safe to assume that the workstations process the same thing
 	
 	private boolean passNextCF; // Is it possible to pass to the next conveyor family yet?
 
 	int guiIndex; // Needed to communicate with the transducer
 	
 	// Add semaphores to delay the popUp agent accordingly between GUI transitions
 	private List<Semaphore> animationSemaphores;
 	
 	// Add list for tickets that allow the glass to move on to the next conveyor family
 	private List<Boolean> tickets;
 	
 	// Constructors:
 	public PopUpAgent(String name, Transducer transducer, List<OfflineWorkstationAgent> machines, int guiIndex) {  
 		// Set the passed in values first
 		super(name, transducer);
 		
 		// Then set the values that need to be initialized within this class, specifically
 		glassToBeProcessed = Collections.synchronizedList(new ArrayList<MyGlassPopUp>());
 		machineComs = Collections.synchronizedList(new ArrayList<MachineCom>());
 		animationSemaphores = Collections.synchronizedList(new ArrayList<Semaphore>());
 		tickets = Collections.synchronizedList(new ArrayList<Boolean>());
 		tickets.add(new Boolean(true)); // Make sure to have an initial ticket, or else the glass will never go through
 		
 		// This loop will go for the number of machines that are in the machines argument
 		int i = 0; // Machine indexes related to the GUI machinea
 		for (OfflineWorkstationAgent m: machines) {			
 			machineComs.add(new MachineCom(m, i));
 			i++;
 		}
 		
 		processType = machineComs.get(0).machine.getType(); // Set the correct process type
 		
 		popUpDown = true; // The popUp has to be down when the system starts...
 		passNextCF = true; // The next conveyor will always be available when the system starts
 		
 		this.guiIndex = guiIndex;
 		
 		// Initialize the semaphores as binary semaphores with value 0
 		for (int j = 0; j < 5; j++) {
 			animationSemaphores.add(new Semaphore(0));
 		}
 		
 		initializeTransducerChannels();		
 	}
 	
 	// alternate constructor that accepts a machine array versus an arrayList:
 	public PopUpAgent(String name, Transducer transducer, OfflineWorkstationAgent[] machines, int guiIndex) {  
 		// Set the passed in values first
 		super(name, transducer);
 		
 		// Then set the values that need to be initialized within this class, specifically
 		glassToBeProcessed = Collections.synchronizedList(new ArrayList<MyGlassPopUp>());
 		machineComs = Collections.synchronizedList(new ArrayList<MachineCom>());
 		animationSemaphores = Collections.synchronizedList(new ArrayList<Semaphore>());
 		tickets = Collections.synchronizedList(new ArrayList<Boolean>());
 		tickets.add(new Boolean(true)); // Make sure to have an initial ticket, or else the glass will never go through
 		
 		// This loop will go for the number of machines that are in the machines argument
 		int i = 0; // Machine indexes related to the GUI machinea
 		for (OfflineWorkstationAgent m: machines) {			
 			machineComs.add(new MachineCom(m, i));
 			i++;
 		}
 		
 		processType = machineComs.get(0).machine.getType(); // Set the correct process type
 		
 		popUpDown = true; // The popUp has to be down when the system starts...
 		passNextCF = true; // The next conveyor will always be available when the system starts
 		
 		this.guiIndex = guiIndex;
 		
 		// Initialize the semaphores as binary semaphores with value 0
 		for (int j = 0; j < 5; j++) {
 			animationSemaphores.add(new Semaphore(0));
 		}
 		
 		initializeTransducerChannels();		
 	}
 	
 	private void initializeTransducerChannels() { // Initialize the transducer channels and everything else related to it
 		// Register any appropriate channels
 		transducer.register(this, TChannel.POPUP); // Set this agent to listen to the POPUP channel of the transducer
 		transducer.register(this, processType.getChannel()); // Set this agent to listen to the processType channel of the transducer
 	}
 
 
 	//Messages:
 	public void msgGiveGlassToPopUp(Glass g) { // Get Glass from conveyor to PopUp
 		glassToBeProcessed.add(new MyGlassPopUp(g, processState.awaitingArrival));
 		print("Glass with ID (" + g.getID() + ") added");
 		stateChanged();
 	}
 
 	public void msgGlassDone(Glass g, int index) { // Adds glass back from a machine and then resets the machine channel to be free
 		synchronized (glassToBeProcessed) {
 			for (MyGlassPopUp glass: glassToBeProcessed) {
 				if (glass.glass.getID() == g.getID()) {
 					glass.processState = processState.doneProcessing;
 					stateChanged();
 					break;
 				}
 			}
 			// Should never get here
 		}
 	}
 	
 	public void msgPositionFree() {
 		passNextCF = true;
 		tickets.add(new Boolean(true));
 		print("Got msgPositionFree() " + tickets.size());
 		stateChanged();
 	}
 
 	//Scheduler:
 	public boolean pickAndExecuteAnAction() {
 		// Use null variables for determining is value is found from synchronized loop
 		MyGlassPopUp glass = null;
 		MachineCom machCom = null;
 		
 		synchronized(glassToBeProcessed) {
 			for (MyGlassPopUp g: glassToBeProcessed) {
 				if (g.processState == processState.awaitingRemoval) { // If glass needs to be sent out to next conveyor and a position is available
					if (!tickets.isEmpty()) { // If there is a ticket for the glass to go to the next conveyor family
 						glass = g;
 						break;
 					}
 					else {
 						print("Glass needs to be removed, but no position free");
 						return false; // Do not want another piece of glass to collide, so shut the agent down until positionFree() is called
 					}
 				}				
 			}
 		}
 		if (glass != null) {
 			actPassGlassToNextCF(glass); return true;
 		}
 		
 		synchronized(glassToBeProcessed) {
 			for (MyGlassPopUp g: glassToBeProcessed) {
 				if (g.processState == processState.unprocessed) { // If glass needs to be sent out to a machine and a position is available
 					synchronized(machineComs) {
 						for (MachineCom com: machineComs) {
 							if ((com.inUse == false && popUpDown == true)) { // If there is an available machine and the popUp is down
 								glass = g;
 								machCom = com;
 								break;
 							}
 						}
 					}
 				}				
 			}
 		}
 		if (glass != null && machCom != null) {
 			actPassGlassToMachine(glass, machCom); return true;
 		}
 		
 		synchronized(glassToBeProcessed) {
 			for (MyGlassPopUp g: glassToBeProcessed) {
 				if (g.processState == processState.doneProcessing) { // If glass needs to be sent out to next conveyor and a position is available
 					glass = g;
 					break;
 				}				
 			}
 		}
 		if (glass != null) {
 			actRemoveGlassFromMachine(glass); return true;
 		}
 		
 		synchronized(glassToBeProcessed) {
 			for (MyGlassPopUp g: glassToBeProcessed) {
 				if (g.processState == processState.awaitingArrival) { // If glass needs to be sent out to next conveyor and a position is available
 					synchronized(machineComs) {
 						for (MachineCom com: machineComs) {
 							if ((com.inUse == false) || !g.glass.getNeedsProcessing(processType)) { // If there is an available machine and the popUp is down
 								glass = g;
 								machCom = com;
 								break;
 							}
 						}
 					}
 				}				
 			}
 		}
 		if (glass != null && machCom != null) {
 			actSendForGlass(glass); return true;
 		}		
 		
 		return false;
 	}
 	
 	//Actions:
 	private void actSendForGlass(MyGlassPopUp glass) {
 		// Fire transducer event to move the popUp down here index  make sure to stall the agent until the right time to prevent any weird synchronization issues
 		doMovePopUpDown();
 		cf.getConveyor().msgPositionFree(); // Tell conveyor to send down the glass
 		// Wait until the glass is loaded to continue further action
 		doDelayForAnimation(0); 
 		if (glass.glass.getNeedsProcessing(processType))
 			glass.processState = processState.unprocessed;
 		else 
 			glass.processState = processState.awaitingRemoval;
 		print("Glass " + glass.glass.getID() + " added to queue for processing.  Glass state: " + glass.processState);
 	}
 	
 	private void actPassGlassToNextCF(MyGlassPopUp glass) {
 		cf.getNextCF().msgHereIsGlass(glass.glass);
 		tickets.remove(0); // Make sure to remove the ticket, as it has already been used 
 		// Fire the transducer to turn off this CF's conveyor if there is no glass on it
 		if (cf.getConveyor().getGlassSheets().size() == 0) { // Turn off the conveyor, there is no glass on it
 			Integer[] args = {cf.getConveyor().getGUIIndex()};
 			transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);
 		}				
 		// Fire transducer event to release glass index  make sure to stall the agent until the glass arrives to prevent any weird synchronization issues
 		doReleaseGlassPopUp();
 		passNextCF = false;
 		glassToBeProcessed.remove(glass);	
 		print("Glass " + glass.glass.getID() + " passed to nextCF");
 
 	}
 	
 	private void actRemoveGlassFromMachine(MyGlassPopUp glass) {
 		// Make sure to call Transducer events: 
 		// Move PopUp up, 
 		doMovePopUpUp();
 		// Machine Release Glass,
 		doReleaseGlassWorkstation(glass.machineIndex);
 		// Move PopUp Down 
 		doMovePopUpDown();
 		// all with the correct timing so nothing is funky
 		glass.processState = processState.awaitingRemoval;
 		print("Glass " + glass.glass.getID() + " removed from machine");
 
 	}
 	
 	private void actPassGlassToMachine(MyGlassPopUp glass, MachineCom com) {
 		glass.processState = processState.processing;
 		glass.machineIndex = com.machineIndex;
 		// Fire the transducer to turn off this CF's conveyor if there is no glass on it
 		if (cf.getConveyor().getGlassSheets().size() == 0) { // Turn off the conveyor, there is no glass on it
 			Integer[] args = {cf.getConveyor().getGUIIndex()};
 			transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);
 		}				
 		// Fire the PopUp up transducer event index  make sure to stall the agent until the glass arrives to prevent any weird synchronization issues
 		doMovePopUpUp();
 		com.machine.msgHereIsGlass(glass.glass);
 		// Machine Load glass transducer events w/right index (can be attained from the machineCom machineIndex)  make sure to stall the agent until the glass arrives to prevent any weird synchronization issues
 		doLoadGlassWorkStation(com.machineIndex);
 		print("Glass " + glass.glass.getID() + " passed to machine " + com.machine.getName());
 	}	
 
 	//Other Methods:
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		// Catch all of the animation events and open up the correct semaphores to continue the processing of the glass on the PopUp or workstation
 		if ((channel == TChannel.POPUP && (Integer) args[0] == guiIndex ) || channel == processType.getChannel()) {
 			if (event == TEvent.POPUP_GUI_LOAD_FINISHED) {
 				animationSemaphores.get(0).release();
 				print("Animation semaphore 0 released");
 			}
 			else if (event == TEvent.POPUP_GUI_MOVED_DOWN) {
 				animationSemaphores.get(1).release();		
 				print("Animation semaphore 1 released");
 			}
 			else if (event == TEvent.POPUP_GUI_MOVED_UP) {
 				animationSemaphores.get(2).release();
 				print("Animation semaphore 2 released");
 			}
 			else if (event == TEvent.POPUP_GUI_RELEASE_FINISHED) {
 				animationSemaphores.get(3).release();
 				print("Animation semaphore 3 released");
 			}
 			else if (event == TEvent.WORKSTATION_LOAD_FINISHED) {
 				animationSemaphores.get(4).release();
 				print("Animation semaphore 4 released");
 			}
 		}		
 	}
 	
 	// Special Animation Methods Below ("do" methods):
 	private void doMovePopUpUp() { // Make the GUI PopUp move up
 		if (popUpDown == true) { // Only do this action if the popUp is down
 			Integer args[] = {guiIndex};
 			transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_UP, args);
 			doDelayForAnimation(2); // Wait for the popUp to move up
 			popUpDown = false;
 		}
 	}
 	
 	private void doMovePopUpDown() { // Make the GUI PopUp move down
 		if (popUpDown == false) { // Only do this action if the popUp is up
 			Integer args[] = {guiIndex};
 			transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
 			doDelayForAnimation(1); // Wait for the popUp to move down
 			popUpDown = true;
 		}		
 	}
 	
 	private void doReleaseGlassPopUp() { // Make the GUI PopUp release it's glass
 		Integer args[] = {guiIndex};
 		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, args);
 		doDelayForAnimation(3); // Wait for the popUp to release the glass
 	}
 	
 	private void doLoadGlassWorkStation(int index) { // Make the GUI Workstation (index) next to the popUp load glass
 		Integer args[] = {index};
 		transducer.fireEvent(processType.getChannel(), TEvent.WORKSTATION_DO_LOAD_GLASS, args);
 		doDelayForAnimation(4); // wait for popup load to finish
 		machineComs.get(index).inUse = true; // Make sure to set the machineCom to isUse, or else other glasses will be able to access machine
 	}
 	
 	private void doReleaseGlassWorkstation(int index) { // Make the GUI Workstation (index) next to the popUp release its glass
 		Integer args[] = {index};
 		transducer.fireEvent(processType.getChannel(), TEvent.WORKSTATION_RELEASE_GLASS, args);
 		doDelayForAnimation(0); // wait for popup load to finish
 		machineComs.get(index).inUse = false; // Make sure to make the machineCom available again
 	}
 	
 	private void doDelayForAnimation(int index) { // Depending on what index is passed in, a certain animation semaphore will block until the animation is done
 		try {
 			animationSemaphores.get(index).acquire();
 		}
 		catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}	
 	
 	// Getters and Setters	
 	public int getFreeChannels() {
 		int freeChannels = 0;
 		synchronized(machineComs) {	
 			for (MachineCom com: machineComs) {
 				if (com.inUse == false)					
 					freeChannels++;
 			}
 		}
 		
 		// Make sure to augment the free channels number by the amount of glasses that are currently within the popUp, so that two glasses do not come up when there shoulkd only be one
 		
 		freeChannels -= glassToBeProcessed.size();
 		
 		return freeChannels;
 	}
 
 	/**
 	 * @return the glassToBeProcessed
 	 */
 	public List<MyGlassPopUp> getGlassToBeProcessed() {
 		return glassToBeProcessed;
 	}
 
 	/**
 	 * @return the popUpDown
 	 */
 	public boolean isPopUpDown() {
 		return popUpDown;
 	}
 
 	@Override
 	public void setCF(OfflineConveyorFamily conveyorFamilyImp) {
 		cf = (ConveyorFamilyImp) conveyorFamilyImp;		
 	}
 
 	@Override
 	public void runScheduler() {
 		pickAndExecuteAnAction();		
 	}
 
 	@Override
 	public boolean doesGlassNeedProcessing(Glass glass) { // Method invoked by the conveyor for a special case of sending glass down the popUp in the line
 		if (glass.getNeedsProcessing(processType)) { // Both machines on every offline process do the same process
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 	/**
 	 * @return the passNextCF
 	 */
 	public boolean isPassNextCF() {
 		return passNextCF;
 	}
 	
 	public List<Semaphore> getAnimationSemaphores() {
 		return animationSemaphores;
 	}
 
 	/**
 	 * @return the machineComs
 	 */
 	public List<MachineCom> getMachineComs() {
 		return machineComs;
 	}
 }
