 package engine.alex.agent;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.TReceiver;
 import transducer.Transducer;
 import engine.agent.Agent;
 import engine.interfaces.ConveyorFamily;
 import engine.sky.agent.SkyConveyorAgent;
 import engine.util.GlassType;
 
 public class AlexConveyorAgent extends Agent implements ConveyorFamily{
 
 	//Data
 	Integer[] conveyorNumber = new Integer[1];
 	enum SensorStates {pressed,doingNothing,released};//,waitToPass};
 	SensorStates startSensorStates=SensorStates.released;
 	SensorStates endSensorStates=SensorStates.released;
 
 
 	boolean allowPass;
 	boolean conveyorOn;
 	
 
 
 	ConveyorFamily preAgent,nextAgent;
 
 
 	private List<GlassType> glasses=Collections.synchronizedList(new ArrayList<GlassType>());
 
 	public AlexConveyorAgent(String name,Transducer t,int i){
 		super(name,t);
 		t.register(this, TChannel.SENSOR);
 		conveyorNumber[0]=i;
 		allowPass=false;
 		conveyorOn=false;
 		stateChanged();//to run the scheduler for the first time so it can send message to preCF
 	}
 
 	//message
 
 
 	public void msgStartSensorPressed() {
 		startSensorStates=SensorStates.pressed;
 
 		//		System.out.println("start sensor pressed");
 
 		stateChanged();
 	}
 
 	public void msgStartSensorReleased() { 
 		startSensorStates=SensorStates.released;
 
 		//		System.out.println("start sensor released");
 
 		stateChanged();
 
 	}
 
 	public void msgEndSensorPressed(){
 		endSensorStates=SensorStates.pressed;
 
 		//		System.out.println("end sensor pressed");
 
 		stateChanged();
 	}
 
 	public void msgEndSensorReleased(){
 		endSensorStates=SensorStates.released;
 
 		//		System.out.println("end sensor released");
 
 		stateChanged();
 
 	}
 
 	@Override
 	public void msgPassingGlass(GlassType gt) {
 		// TODO Auto-generated method stub
 		glasses.add(gt);	
 
 		//		System.out.println("adding a glasstype");
 
 		stateChanged();
 
 	}
 
 	@Override
 	public void msgIAmAvailable() {
 		// TODO Auto-generated method stub
 		allowPass=true;
 
 		//		System.out.println("glass is allowed to pass to next agent");
 
 		stateChanged();
 	}
 
 	public void msgIAmNotAvailable(){
 		allowPass=false;
 
 		//		System.out.println("glass is allowed to pass to next agent");
 
 		stateChanged();
 	}
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// TODO Auto-generated method stub
 		if(glasses.isEmpty()){
 			if(!conveyorOn){
 			TurnOnConveyor();
 			}
 		}
 		
 		
 		if (startSensorStates==SensorStates.released){
 			tellingPreCFImAvailable();
 			startSensorStates=SensorStates.doingNothing;
 			return true;
 		}
 
 
 
 		if (startSensorStates==SensorStates.pressed){
 			if (endSensorStates==SensorStates.released){
 				if(!conveyorOn){
 				TurnOnConveyor();}
 				startSensorStates=SensorStates.doingNothing;
 				return true;
 			}
 		}
 
 
 		if (endSensorStates==SensorStates.pressed){
 
 			if (allowPass==true){
 				if(!conveyorOn){
 				TurnOnConveyor();}
 				passingGlass();
 				endSensorStates=SensorStates.doingNothing;
 				return true;
 			}else 	{
 				if(conveyorOn){
 				TurnOffConveyor();}
 				return true;
 
 			}
 		}
 
 		if (endSensorStates==SensorStates.released){
 			if(!conveyorOn){
 			TurnOnConveyor();}
 			endSensorStates=SensorStates.doingNothing;
 			return true;
 		}
 
 		return false;
 	}
 
 
 	public void tellingPreCFImAvailable() {//step1 telling previous CF im available
 		preAgent.msgIAmAvailable();
 		System.out.println(this +": tells "+preAgent+" tellingPreCFImAvailable() ");
 
 		//		if(conveyorNumber[0]==0){
 		//			System.out.println("sending msg to binAgent saying I'm ready");
 		//		}else if(conveyorNumber[0]==1){
 		//			System.out.println("sending msg to cutterAgent saying I'm ready");
 		//		}else if(conveyorNumber[0]==2){
 		//			System.out.println("sending msg to conveyor1Agent saying I'm ready");
 		//		}else if(conveyorNumber[0]==3){
 		//			System.out.println("sending msg to breakoutAgent saying I'm ready");
 		//		}else if(conveyorNumber[0]==4){
 		//			System.out.println("sending msg to manualBreakoutAgent saying I'm ready");
 		/*		}/*else if(conveyorNumber[0]==5){
 		conveyor4Agent.msgIAmAvailable();	
 		System.out.println("sending msg to conveyor4Agent saying I'm ready");
 	}else if(conveyorNumber==6){
 		drillAgent.msgIAmAvailable();	
 		System.out.println("sending msg to drillAgent saying I'm ready");
 	}else if(conveyorNumber==7){
 		crossSeamerAgent.msgIAmAvailable();	
 		System.out.println("sending msg to crossSeamerAgent saying I'm ready");
 	}else if(conveyorNumber==8){
 		grinderAgent.msgIAmAvailable();	
 		System.out.println("sending msg to grinderAgent saying I'm ready");
 	}else if(conveyorNumber==9){
 		washerAgent.msgIAmAvailable();	
 		System.out.println("sending msg to washerAgent saying I'm ready");
 	}else if(conveyorNumber==10){
 		conveyor9Agent.msgIAmAvailable();	
 		System.out.println("sending msg to conveyor9Agent saying I'm ready");
 	}else if(conveyorNumber==11){
 		uvLampAgent.msgIAmAvailable();	
 		System.out.println("sending msg to uvLampAgent saying I'm ready");
 	}else if(conveyorNumber==12){
 		painterAgent.msgIAmAvailable();	
 		System.out.println("sending msg to painterAgent saying I'm ready");
 	}else if(conveyorNumber==13){
 		conveyor12Agent.msgIAmAvailable();	
 		System.out.println("sending msg to conveyor12Agent saying I'm ready");
 	}else if(conveyorNumber==14){
 		ovenAgent.msgIAmAvailable();	
 		System.out.println("sending msg to ovenAgent saying I'm ready");
 	}
 		 */
 	}
 
 
 	public void TurnOnConveyor(){
 		conveyorOn=true;
 		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, conveyorNumber);
 		System.out.println(this + ": TurnOnConveyor()");
 		//		System.out.println("Conveyor "+ conveyorNumber[0]+" is on");
 
 	}
 
 	public void TurnOffConveyor(){
 		conveyorOn=false;
 		
		//preAgent.msgIAmNotAvailable();
 		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, conveyorNumber);
 		
 		System.out.println(this + ": TurnOffConveyor()");
 
 
 		//		System.out.println("Conveyor "+ conveyorNumber[0]+" is off");
 
 	}
 
 	public void passingGlass(){
 		GlassType temp=glasses.remove(0);
 		nextAgent.msgPassingGlass(temp);
 		allowPass=false;
 		
 		System.out.println(this + ": passingGlass()");
 		//		if(conveyorNumber[0]==0){
 		//			System.out.println("passing glass " + temp.getGlassID() + " to cutter");
 		//		}else if(conveyorNumber[0]==1){
 		//			System.out.println("passing glass " + temp.getGlassID() + " to conveyor2");
 		//		}else if(conveyorNumber[0]==2){
 		//			System.out.println("passing glass " + temp.getGlassID()+ " to breakout");
 		//		}else if(conveyorNumber[0]==3){
 		//			System.out.println("passing glass " + temp.getGlassID()+ " to manualBreakout");
 		//		}else if(conveyorNumber[0]==4){
 		//			System.out.println("passing glass " + temp.getGlassID()+ " to conveyor5");
 		/*}/*else if(conveyorNumber==5){
 		drillAgent.msgPassingGlass(temp);	
 		System.out.println("passing glass " + temp.getGlassID()+ " to drill popup");
 	}else if(conveyorNumber==6){
 		crossSeamerAgent.msgPassingGlass(temp);
 		System.out.println("passing glass " + temp.getGlassID()+ " to crossSeamer popup");
 	}else if(conveyorNumber==7){
 		grinderAgent.msgPassingGlass(temp);
 		System.out.println("passing glass " + temp.getGlassID()+ " to grinder popup");
 	}else if(conveyorNumber==8){
 		washerAgent.msgPassingGlass(temp);
 		System.out.println("passing glass " + temp.getGlassID()+ " to washer");
 	}else if(conveyorNumber==9){
 		conveyor10Agent.msgPassingGlass(temp);	
 		System.out.println("passing glass " + temp.getGlassID()+ " to conveyor10");
 	}else if(conveyorNumber==10){
 		uvLampAgent.msgPassingGlass(temp);		
 		System.out.println("passing glass " + temp.getGlassID()+ " to uvlamp");
 	}else if(conveyorNumber==11){
 		painterAgent.msgPassingGlass(temp);
 		System.out.println("passing glass " + temp.getGlassID()+ " to painter");
 	}else if(conveyorNumber==12){
 		conveyor13Agent.msgPassingGlass(temp);
 		System.out.println("passing glass " + temp.getGlassID()+ " to conveyor13");
 	}else if(conveyorNumber==13){
 		ovenAgent.msgPassingGlass(temp);	
 		System.out.println("passing glass " + temp.getGlassID()+ " to oven");
 	}else if(conveyorNumber==14){
 		truckAgent.msgPassingGlass(temp);	
 		System.out.println("passing glass " + temp.getGlassID()+ " to truck");
 	}*/
 	}
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		// TODO Auto-generated method stub
 		if (event == TEvent.SENSOR_GUI_PRESSED){
 			Integer[] newArgs = new Integer[1];
 			newArgs[0] = (Integer)args[0] / 2;//turn on the matching conveyor
 			if (conveyorNumber[0]==(Integer)newArgs[0]){
 				if (((Integer)args[0] % 2) == 0 ){//when it's start sensor pressed
 					this.msgStartSensorPressed();
 				}else if (((Integer)args[0] % 2) == 1){//when it's end sensor pressed
 					this.msgEndSensorPressed();
 				}
 			}
 		}else if (event == TEvent.SENSOR_GUI_RELEASED){
 			Integer[] newArgs = new Integer[1];
 			newArgs[0] = (Integer)args[0] / 2;//decide which machine to send message to based on the number of the start sensor
 			if (conveyorNumber[0]==(Integer)newArgs[0]){
 				if (((Integer)args[0] % 2) == 0){//when it's start sensor released	
 					this.msgStartSensorReleased();
 				}else if (((Integer)args[0] % 2) == 1){//when it's end sensor released
 					this.msgEndSensorReleased();
 				}
 			}
 		}
 	}
 
 	public ConveyorFamily getPreAgent() {
 		return preAgent;
 	}
 
 	public void setPreAgent(ConveyorFamily preAgent) {
 		this.preAgent = preAgent;
 	}
 
 	public ConveyorFamily getNextAgent() {
 		return nextAgent;
 	}
 
 	public void setNextAgent(ConveyorFamily nextAgent) {
 		this.nextAgent = nextAgent;
 	}
 
 }
