 package engine.alex.agent;
 
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.TReceiver;
 import transducer.Transducer;
 import engine.agent.Agent;
 import engine.interfaces.ConveyorFamily;
 import engine.util.GlassType;
 
 public class MachineAgent extends Agent implements ConveyorFamily{
 
 	//data
 
 	enum States {noparts,doingnothing,partsloaded,readytopass};
 
 	ConveyorFamily preConveyor,nextConveyor;
 
 	boolean allowPass;
 
 	int machineNumber;
 	GlassType glass;
 	States myState;
 
 
 	public MachineAgent(String name,Transducer t,int i){
 		super(name,t);
 		machineNumber=i;
 		allowPass=false;
 		myState=States.noparts;
 		if(machineNumber==0){
 			t.register(this, TChannel.CUTTER);
 		}else if(machineNumber==1){
 			t.register(this, TChannel.BREAKOUT);
 		}else if(machineNumber==2){
 			t.register(this, TChannel.MANUAL_BREAKOUT);
 		}
 		stateChanged();//to run the scheduler for the first time so it can send message to preCF
 
 	}
 
 
 
 	//message
 	@Override
 	public void msgPassingGlass(GlassType gt) {
 		// TODO Auto-generated method stub
 		glass=gt;
 		System.out.println("adding a glasstype");
 		stateChanged();
 	}
 
 	@Override
 	public void msgIAmAvailable() {
 		// TODO Auto-generated method stub
 		allowPass=true;
 		System.out.println("glass is allowed to pass to next conveyor");
 		stateChanged();
 	}
 
 	public void msgWorkStationGuiActionFinished(){
 		myState=States.readytopass;
 		stateChanged();
 	}
 
 	public void msgWorkStationLoadFinished(){
 		myState=States.partsloaded;
 		stateChanged();
 	}
 
 	public void msgWorkStationReleaseFinished(){
 		myState=States.noparts;
 		stateChanged();
 	}
 
 	///schedule
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// TODO Auto-generated method stub
 
 		if (myState==States.noparts){
 			tellingPreCFImAvailable();
 			myState=States.doingnothing;
 			return true;
 
 		}
 
 
 		if (myState==States.partsloaded){
 			workingStationDoAction();
 			myState=States.doingnothing;
 			return true;
 		}
 
 		if (myState==States.readytopass){
 			if (allowPass==true){
 				passingGlass();
 				myState=States.doingnothing;
 				return true;
 			}
 
 		}
 
 		return false;
 	}
 
 	//action
 	public void tellingPreCFImAvailable() {
 		preConveyor.msgIAmAvailable();	
 		if(machineNumber==0){
 			System.out.println("sending msg to Conveyor0 saying I'm ready");
 		}else if(machineNumber==1){
 			System.out.println("sending msg to Conveyor2 saying I'm ready");
 		}else if(machineNumber==2){
 			System.out.println("sending msg to Conveyor3 saying I'm ready");
 		}
 
 	}
 
 	public void workingStationDoAction(){
 		if(machineNumber==0){
 			transducer.fireEvent(TChannel.CUTTER, TEvent.WORKSTATION_DO_ACTION, null );
 		}else if(machineNumber==1){
 			transducer.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_DO_ACTION, null);
 		}else if(machineNumber==2){
 			transducer.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_DO_ACTION, null);
 		}
 		System.out.println("doing workstation animation ");
 	}
 
 	public void passingGlass(){
 		nextConveyor.msgPassingGlass(glass);
		allowPass=false;
 		if(machineNumber==0){
 			transducer.fireEvent(TChannel.CUTTER, TEvent.WORKSTATION_RELEASE_GLASS, null );
 		}else if(machineNumber==1){
 			transducer.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_RELEASE_GLASS, null);
 		}else if(machineNumber==2){
 			transducer.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_RELEASE_GLASS, null);
 		}
 	}
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		// TODO Auto-generated method stub
 		if (event==TEvent.WORKSTATION_GUI_ACTION_FINISHED){
 
 			this.msgWorkStationGuiActionFinished();
 
 		}else if(event==TEvent.WORKSTATION_LOAD_FINISHED){
 
 			this.msgWorkStationLoadFinished();
 
 		}else if(event==TEvent.WORKSTATION_RELEASE_FINISHED){
 
 			this.msgWorkStationReleaseFinished();
 
 		}
 
 
 
 	}
 
 
 
 	public ConveyorFamily getPreConveyor() {
 		return preConveyor;
 	}
 
 
 
 	public void setPreConveyor(ConveyorFamily preConveyor) {
 		this.preConveyor = preConveyor;
 	}
 
 
 
 	public ConveyorFamily getNextConveyor() {
 		return nextConveyor;
 	}
 
 
 
 	public void setNextConveyor(ConveyorFamily nextConveyor) {
 		this.nextConveyor = nextConveyor;
 	}
 
 
 
 
 }
