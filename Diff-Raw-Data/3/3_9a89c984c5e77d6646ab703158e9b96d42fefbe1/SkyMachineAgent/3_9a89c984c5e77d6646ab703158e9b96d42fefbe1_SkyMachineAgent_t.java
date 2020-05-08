 package engine.sky.agent;
 
 import engine.agent.Agent;
 import engine.interfaces.ConveyorFamily;
 import engine.interfaces.SkyMachine;
 import engine.util.GlassType;
 import shared.enums.MachineType;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 public class SkyMachineAgent extends Agent implements ConveyorFamily, SkyMachine {
 
 	/* (non-Javadoc)
 	 * @see engine.agent.Machine#msgPassingGlass(engine.agent.GlassType)
 	 */
 
 	private int myGuiIndex;
 	private ConveyorFamily pairedPopUp;
 	private GlassType myGlass;
 	boolean readyToPass;
 	private enum MachineState {Idle, Loading, Loaded, Finished,DoingNothing}
 	private MachineState state;
 	private MachineType type;
 	public SkyMachineAgent (ConveyorFamily popUp, int in, String n, Transducer tr) {
 		super(n,tr);
 		pairedPopUp = popUp;
 		myGuiIndex = in;
 		readyToPass = false;
 		state = MachineState.Idle;
 
 		if (n.equals("Drill0") || n.equals("Drill1")) {
 			type = MachineType.DRILL;
 			transducer.register(this, TChannel.DRILL);
 		}
 
 		if (n.equals("Cross_Seamer0") || n.equals("Cross_Seamer1")) {
 			type = MachineType.CROSS_SEAMER;
 			transducer.register(this, TChannel.CROSS_SEAMER);
 		}
 
 		if (n.equals("Grinder0") || n.equals("Grinder1")) {
 			type = MachineType.GRINDER;
 			transducer.register(this, TChannel.GRINDER);
 		}
 	}
 
 	public SkyMachineAgent (int index, String n, Transducer tr) {
 		super(n,tr);
 		myGuiIndex = index;
 		readyToPass = false;
 		state = MachineState.Idle;
 
 		if (n.equals("Drill0") || n.equals("Drill1")) {
 			type = MachineType.DRILL;
 			transducer.register(this, TChannel.DRILL);
 		}
 
 		if (n.equals("Cross_Seamer0") || n.equals("Cross_Seamer1")) {
 			type = MachineType.CROSS_SEAMER;
 			transducer.register(this, TChannel.CROSS_SEAMER);
 
 		}
 
 		if (n.equals("Grinder0") || n.equals("Grinder1")) {
 			type = MachineType.GRINDER;
 			transducer.register(this, TChannel.GRINDER);
 		}
 
 	}
 
 	/** Messages **/
 	@Override
 
 	public void msgPassingGlass(GlassType gt) {
 		myGlass = gt;
 		state = MachineState.Loading;
 		stateChanged();
 	}
 
 
 	@Override
 	public void msgIAmAvailable() {
 
 	}
 
 	public void msgChangeProcessingTime(int i){//to change the timer
 		Object[] args = new Object[2];
 		args[0] = new Integer(myGuiIndex);
 		args[1] = i;
 		if (type==MachineType.DRILL){
 			transducer.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_DO_CHANGE_ANIMATION_TIME, args);
 		}
 		else if (type == MachineType.CROSS_SEAMER) {
 			transducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.WORKSTATION_DO_CHANGE_ANIMATION_TIME, args);
 		}
 		else if (type == MachineType.GRINDER) {
 			transducer.fireEvent(TChannel.GRINDER, TEvent.WORKSTATION_DO_CHANGE_ANIMATION_TIME, args);
 		}
 		
 	}
 	
 	public void msgRemoveGlass(){
 		
 		Object[] args = new Object[1];
 		args[0] = new Integer(myGuiIndex);
 		if (type==MachineType.DRILL){
 			transducer.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_DO_REMOVE_GLASS, args);
 		}
 		else if (type == MachineType.CROSS_SEAMER) {
 			transducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.WORKSTATION_DO_REMOVE_GLASS, args);
 		}
 		else if (type == MachineType.GRINDER) {
 			transducer.fireEvent(TChannel.GRINDER, TEvent.WORKSTATION_DO_REMOVE_GLASS, args);
 		}
 		//TO DO 
 		//NEED TO NOTICE POPUP I AM READY
 		((SkyPopUpAgent) pairedPopUp).msgGlassRemoved(this);
		if(myGlass!=null){
			myGlass=null;
		}
 		
 	}
 
 	@Override
 	public void msgIAmReady() {
 		readyToPass = true;
 		stateChanged();
 	}
 
 	public void msgLoadFinished() {
 		state = MachineState.Loaded;
 		stateChanged();
 	}
 
 	public void msgActionFinished() {
 		state = MachineState.Finished;
 		stateChanged();
 	}
 
 
 
 	public void msgReleaseFinished() {
 		state = MachineState.Idle;
 		stateChanged();
 	}
 
 	/** Scheduler **/
 	@Override
 	public boolean pickAndExecuteAnAction() {
 //		if (state == MachineState.Idle) {
 ////			informAvailability();
 //			return true;
 //		}
 
 		if (state == MachineState.Loading) {
 			loadGlass();
 			return true;
 		}
 
 		if (state == MachineState.Loaded) {
 			processGlass();
 			return true;
 		}
 
 		if (readyToPass && state == MachineState.Finished) {
 			passGlass(myGlass);
 			return true;
 		}
 		return false;
 	}
 
 
 	/** Action **/
 	public void informAvailability() {
 		System.out.println(this + " : action: informAvailability");
 		pairedPopUp.msgIAmAvailable();
 		state = MachineState.DoingNothing;
 	}
 
 	public void loadGlass() {
 		System.out.println(this + " : action : loadGlass");
 		Object[] args = new Object[1];
 		args[0] = new Integer(myGuiIndex);
 		if (type==MachineType.DRILL){
 			transducer.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_DO_LOAD_GLASS, args);
 		}
 		else if (type == MachineType.CROSS_SEAMER) {
 			transducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.WORKSTATION_DO_LOAD_GLASS, args);
 		}
 		else if (type == MachineType.GRINDER) {
 			transducer.fireEvent(TChannel.GRINDER, TEvent.WORKSTATION_DO_LOAD_GLASS, args);
 		}
 		state = MachineState.DoingNothing;
 
 	}
 
 	public void processGlass() {
 		System.out.println(this + " : action : processGlass");
 		Object[] args = new Object[1];
 		args[0] = new Integer(myGuiIndex);
 		if (type==MachineType.DRILL){
 			transducer.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_DO_ACTION, args);
 		}
 		else if (type == MachineType.CROSS_SEAMER) {
 			transducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.WORKSTATION_DO_ACTION, args);
 		}
 		else if (type == MachineType.GRINDER) {
 			transducer.fireEvent(TChannel.GRINDER, TEvent.WORKSTATION_DO_ACTION, args);
 		}
 		state = MachineState.DoingNothing;
 	}
 
 	public void passGlass(GlassType gt) {
 		System.out.println(this + " : action : passGlass");
 		((SkyPopUpAgent)pairedPopUp).msgReturningGlass(this, myGlass);
 		readyToPass = false;
 		Object[] args = new Object[1];
 		args[0] = new Integer(myGuiIndex);
 		if (type == MachineType.DRILL) {
 			transducer.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_RELEASE_GLASS, args);
 		}
 		else if (type == MachineType.CROSS_SEAMER) {
 			transducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.WORKSTATION_RELEASE_GLASS, args);
 		}
 		else if (type == MachineType.GRINDER) {
 			transducer.fireEvent(TChannel.GRINDER, TEvent.WORKSTATION_RELEASE_GLASS, args);
 		}
 		state = MachineState.DoingNothing;
 
 	}
 
 	/** Utilities **/
 
 	public void connectAgents(ConveyorFamily cf) {
 		pairedPopUp = cf;
 	}
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		if ((Integer)args[0] == myGuiIndex) {
 			if (event == TEvent.WORKSTATION_GUI_ACTION_FINISHED) {
 				this.msgActionFinished();
 				((SkyPopUpAgent) pairedPopUp).msgGlassDone(this,myGlass);
 
 			}
 			if (event == TEvent.WORKSTATION_LOAD_FINISHED ) {
 				this.msgLoadFinished();
 				((SkyPopUpAgent) pairedPopUp).msgLoadFinished();
 
 			}
 			if (event == TEvent.WORKSTATION_RELEASE_FINISHED) {
 				this.msgReleaseFinished();
 
 			}
 		}
 	}
 
 	@Override
 	public void msgIAmNotAvailable() {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 }
