 package engine.agent.tim.test.Mock;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import shared.Glass;
 import shared.enums.MachineType;
 import shared.interfaces.ConveyorFamily;
import shared.interfaces.PopupWorkstationInterface;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.agent.tim.agents.MachineAgent;
 import engine.agent.tim.interfaces.Machine;
 import engine.agent.tim.interfaces.PopUp;
 import engine.agent.tim.misc.ConveyorFamilyImp;
 import engine.agent.tim.misc.MyGlassPopUp;
 import engine.agent.tim.misc.MyGlassPopUp.processState;
 
public class MockPopUp extends MockAgent implements PopUp, PopupWorkstationInterface {
 	
 	// Data:	
 
 
 	public List<MyGlassPopUp> glassToBeProcessed; // This name will be abbreviated as glassToBeProcessed in many functions to save on space and complexity
 	public List<MyGlassPopUp> glassProcessing; // Hack list to bring glass out of the above list when glass is "processing" so the conveyor agent will work 
 
 	// Positional variable for whether the Pop-Up in the GUI is up or down, and it will be changed through the transducer and checked within one of the scheduler rules
 	public boolean popUpDown; // Is this value is true, then the associated popUp is down (will be changed through the appropriate transducer eventFired(args[]) function.
 	
 	public ConveyorFamilyImp cf;
 	
 	// Constructors:
 	public MockPopUp(String name, Transducer transducer) {  
 		// Set the passed in values first
 		super(name, transducer);
 		
 		this.name = name;
 		
 		// Then set the values that need to be initialized within this class, specifically
 		glassToBeProcessed = Collections.synchronizedList(new ArrayList<MyGlassPopUp>());
 		glassProcessing = Collections.synchronizedList(new ArrayList<MyGlassPopUp>());
 
 		popUpDown = true; // The popUp has to be down when the system starts...
 		initializeTransducerChannels();		
 	}
 	
 	public void initializeTransducerChannels() { // Initialize the transducer channels and everything else related to it
 		// Register any appropriate channels
 		transducer.register(this, TChannel.POPUP); // Set this agent to listen to the POPUP channel of the transducer
 	}
 
 
 	//Messages:
 	public void msgGiveGlassToPopUp(Glass g) { // Get Glass from conveyor to PopUp
 		glassToBeProcessed.add(new MyGlassPopUp(g, processState.unprocessed));
 		print("Glass with ID (" + g.getID() + ") added");
 	}
 	
 	public void processGlass() { // Hack method to fake processing the glass
 		if (!glassToBeProcessed.isEmpty()) {
 			print("Glass with ID (" + glassToBeProcessed.get(0).glass.getID() + ") removed");
 			glassProcessing.add(glassToBeProcessed.remove(0));
 		}
 	}
 
 	//Other Methods:
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		// Move the PopUp up or down, depending on what the protocol is -- This is an update from the animation, Mock or not
 		if (event == TEvent.POPUP_DO_MOVE_DOWN) { // There will only be one boolean argument as of now, and that tells whether the popUp is UP or DOWN
 			popUpDown = true;
 		}
 		else if (event == TEvent.POPUP_DO_MOVE_UP) { // There will only be one boolean argument as of now, and that tells whether the popUp is UP or DOWN
 			popUpDown = false;
 		}
 		
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
 	
 	// Hack method for the mock PopUp
 	public void sendBackProcessedGlass(Glass glass) { // Will "return" the glass from processing
 		cf.getConveyor().msgUpdateGlass(glass);
 		for (MyGlassPopUp g: glassToBeProcessed) {
 			if (g.glass.getID() == glass.getID()) {
 				print("Glass with ID (" + g.glass.getID() + ") removed");
 				glassToBeProcessed.remove(g);
 				return;
 			}
 		}
 		for (MyGlassPopUp g: glassProcessing) {
 			if (g.glass.getID() == glass.getID()) {
 				print("Glass with ID (" + g.glass.getID() + ") removed");
 				glassProcessing.remove(g);
 				return;
 			}
 		}
 	}
 
 	@Override
 	public int getFreeChannels() {
 		// TODO Auto-generated method stub
 		return 2 - glassToBeProcessed.size();
 	}
 
 	@Override
 	public void setCF(ConveyorFamily conveyorFamilyImp) {
 		cf = (ConveyorFamilyImp) conveyorFamilyImp;		
 	}
 
 	@Override
 	public void runScheduler() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean doesGlassNeedProcessing(Glass glass) {
 		if (getFreeChannels() > 0) {
 			return true;
 		}
 		else {
 			return false;
 		}
	}

	@Override
	public void msgGlassDone(Glass g, int index) {
		// TODO Auto-generated method stub
		
 	}	
 }
