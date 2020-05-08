 package engine.conveyorfamily_Poojan;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 
 
 import shared.Glass;
 import shared.enums.SharedData.WorkType;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.agent.Agent;
 
 import engine.conveyorfamily.Interfaces_Poojan.Conveyor_PJ;
 import engine.conveyorfamily.Interfaces_Poojan.InLineMachine_PJ;
 import engine.conveyorfamily.Interfaces_Poojan.Operator_PJ;
 import engine.conveyorfamily.Interfaces_Poojan.Popup_PJ;
 import engine.conveyorfamily.Interfaces_Poojan.TransducerInterface_PJ;
 
 import engine.conveyorfamily_Poojan.PopupAgent_PJ.GlassStatusPopup;
 import engine.interfaces.ConveyorFamily;
 
 /**
  * 
  * HalfConveyor : Conveyor for mediating.
  * @author POOJAN JHAVERI
  *
  */
 
 public class HalfConveyorAgent extends Agent implements Conveyor_PJ {
 
 	private String name;
 	private int number;
 	private Transducer myTransducer;
 	private ConveyorFamily MyFamily;
 	public ConveyorFamily NEXTFamily;
 	private ConveyorFamily PREVIOUSFamily;
 
 	private InLineMachine_PJ myinline;
 	private enum GlassStatusConveyor{NEW,DONE,ONENTRYSENSOR,CHECKED, ONEXITSENSOR, NEEDSMACHINEPROCESSING, NOMACHINEPROCESSING, CHECKINGPROCESSING, FIRSTDONE, INLINEBUSY, ONLASTSENSOR, ONLASTSENSORSTOP, ONTHIRDSENSOR, THIRDSENSORDONE, CHECKEDDONE, ONENTRYSENSORSTOP};
 
 	private Boolean isPopUpBusy;
 
 	private boolean isNextConveyorFamilyBusy;
 	private ConveyorAgent_PJ previousconveyor;
 	private enum ConveyorState{Running,Stopped,Jammed,Need_Fix, Need_Run, Need_Break};
 	private enum SensorState{NOTHING, PRESSED, RELEASED};
 	private ConveyorState conveyor1;
 	private SensorState sensor1;
 	private SensorState sensor2;
 
 
 	private List<MyCGlass> glassonconveyor = Collections.synchronizedList(new ArrayList<MyCGlass>());
 
 
 /**
  *Agent for mediating conveyor ie. without any connecton to Inline Agent
  * 
  * 
  * @param string : Conveyor name
  * @param number : Conveyor number
  * @param myfamily	: Conveyor Family instace, recognize my own conveyor family
  * @param transducer : Transducer
  * @param popup : Popup Agnet
  * @param inline : Inline Agent
  * @param previous - Previous Conveyor Family
  */
 	public HalfConveyorAgent(String string,int number, ConveyorFamily myfamily, Transducer transducer,Popup_PJ popup,InLineMachine_PJ inline,ConveyorFamily previous) {
 
 	this.name=string;
 	this.number=number;
 
 	this.MyFamily=myfamily;
 	this.myinline=inline;
 	this.PREVIOUSFamily=previous;
 
 	this.isPopUpBusy=false;
 	myTransducer = transducer;
 
 	sensor1 = SensorState.NOTHING;
 	sensor2 = SensorState.NOTHING;
 	conveyor1 = ConveyorState.Stopped;
 
 	myTransducer.register(this, TChannel.ALL_GUI);
 	myTransducer.register(this, TChannel.SENSOR);
 	myTransducer.register(this, TChannel.ALL_AGENTS);
 	myTransducer.register(this, TChannel.CONVEYOR);
 	isNextConveyorFamilyBusy=false;
 	}
 
 
 	public class MyCGlass
 	{
 		private Glass pcglass;
 		private GlassStatusConveyor status;
 		private Boolean NeedsProcessing;
 
 		public MyCGlass(Glass g)
 		{
 			this.pcglass=g;
 			this.status=GlassStatusConveyor.NEW;
 		}
 
 	}
 
 	public class MyOperators
 	{
 		private Operator_PJ op;
 		private boolean occupied;
 
 
 		public MyOperators(Operator_PJ o)
 		{
 			this.op=o;
 			this.occupied=false;
 		}
 
 	}
 
 
 	// MESSAGES
 
 		/**
 		 * msgHereIsGlass recieved from the previous conveyor Family or from Previous Component
 		 * 
 		 */
 		public void msgHereIsGlass(Glass g1) {
 			print("Glass Recieved."+this.number);
 			MyCGlass mcg = new MyCGlass(g1);
 			glassonconveyor.add(mcg);
 			stateChanged();
 		}
 
 		/**
 		 * msgIamFree received from the my own conveyor family which is in turn recieved by the next conveyor family
 		 */
 		public void msgIamFree() {			
 			isNextConveyorFamilyBusy=false;
 			print("NEXT CONVEYOR FAMILY -> FREE "+ isNextConveyorFamilyBusy);
 			stateChanged();	
 		}
 
 
 		@Override
 		public void msgOperatorIsfree(Operator_PJ operatorAgent) {
 
 		}
 
 	// SCHEDULER	
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 
 		while(conveyor1==ConveyorState.Jammed)
 		{
 			return false;
 		}
 
 		while(conveyor1==ConveyorState.Need_Run)
 		{
 			print("Starting the conveyor");
 			startconveyor1();
 		}
 
 		if( (isNextConveyorFamilyBusy) && (sensor2 == SensorState.PRESSED) && (conveyor1==ConveyorState.Running) ) {
 			stopConveyor();
 			return true;
 		}
 
 		if( (!isNextConveyorFamilyBusy) && (conveyor1==ConveyorState.Stopped) ) {
 			startconveyor1();
 			return true;
 		}
 
 		if( (sensor2 == SensorState.RELEASED) ) {
 
 			return true;
 		}
 
 		if( sensor1 == SensorState.RELEASED ) {
 
 			return true;
		}
 
 
 
 		return false;
 	}
 
 
 
 
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 
 
 
 
 		if(channel == TChannel.SENSOR)
 		{
 
 			if(event == TEvent.SENSOR_GUI_PRESSED)
 			{
 
 				if((Integer)args[0]==2)
 				{
 					this.previousconveyor.msgIamFree();
 					sensor1 = SensorState.PRESSED;
 			    };    	
 			}
 
 			if(event == TEvent.SENSOR_GUI_PRESSED)
 			{
 				if((Integer)args[0]==3)
 				{
 					sensor2 = SensorState.PRESSED;
 			    };    	
 			}
 
 
 			if(event == TEvent.SENSOR_GUI_RELEASED)
 			{
 				if((Integer)args[0]==2)
 				{
 					informthepreviousfamily();
 					sensor1 = SensorState.RELEASED;
 				}
 
 			}
 
 			if(event == TEvent.SENSOR_GUI_RELEASED)
 			{
 				if((Integer)args[0]==3)
 				{
 					shipttheglass();
 					sensor2 = SensorState.RELEASED;
 				}
 
 			}
 			stateChanged();	
 			return;
 		}
 
 
 		if( (channel == TChannel.CONVEYOR) && ( (Integer) (args[0]) == this.number) ) {
 			if(event == TEvent.CONVEYOR_BROKEN) {
 
 				conveyor1 = ConveyorState.Jammed;
 				stateChanged();		
 				return;
 
 			} else if (event == TEvent.CONVEYOR_FIXED) {
 				conveyor1 = ConveyorState.Need_Run;
 
 				stateChanged();		
 				return;
 			}
 		}
 
 	}
 
 
 	private void shipttheglass() {
 
 		this.NEXTFamily.msgHereIsGlass(glassonconveyor.get(0).pcglass);
 		sensor2 = SensorState.NOTHING;
 		isNextConveyorFamilyBusy=true;
 		glassonconveyor.remove(0);
 	}
 
 
 	private void startconveyor1()
 	{
 		Object [] no={this.getNumber()};
     	myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,no);
     	conveyor1=ConveyorState.Running;
 	}
 
 
 
 	private void stopConveyor() {
 
 		Object[] ar={this.number};
 		myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, ar);
 		conveyor1 = ConveyorState.Stopped;
 	}
 
 
 	private void informthepreviousfamily() {
 		this.myinline.msgIamFreeForGlass();
 		sensor1 = SensorState.NOTHING;
 	}
 
 	public String getName(){
         return name;
     }
 
 	public int getNumber(){
         return number;
     }
 
 	public int getglassonconveyorsize(){
         return glassonconveyor.size();
     }
 
 	public void getoperatorlist(){
 
     }
 
 	public Boolean getisPopUpBusy(){
         return this.isPopUpBusy;
     }
 
 
 	public Boolean getisINLINEBusy()
 	{
 		return isNextConveyorFamilyBusy;
 		//return this.isINLINEBusy;
 	}
 
 
 
 	@Override
 	public void setOperator(Operator_PJ o1) {
 		//
 
 	}
 
 
 	@Override
 	public void setisINLINEBusy(Boolean s) {
 		// 
 	//	isINLINEBusy=s;
 		stateChanged();
 	}
 
 
 	public void setConveyor(ConveyorAgent_PJ conveyor) {
 		// TODO Auto-generated method stub
 		this.previousconveyor=conveyor;
 	}
 
 
 
 /*
 	private void breakConveyor(int i) {
 
 		Object [] no={i};
 			myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_STOP,no);
 			conveyor1=ConveyorState.Jammed;
     	stateChanged();
     	print("Conveyor broken");
     	
 	}
 
 
 	private void unbreakConveyor(int i) {
 		Object [] cno={i};
 		if(i==1)
 		{
 			if( ( frontsensor || !glassonconveyor.isEmpty() ) && !nextsensor && finished ){
 				myTransducer.fireEvent( TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, cno );
 				conveyor1=ConveyorState.Running;
 			}
 			else{
 				myTransducer.fireEvent( TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, cno );
 				conveyor1=ConveyorState.Stopped;
 			}
 		}
 
 		
 	}
 */
 
 
 
 
 
 
 
 	public void setbrokenstatus(boolean s,int i) {
 		// 
 		if(s)
 		{
 			conveyor1=ConveyorState.Jammed;
 		}
 		else
 		{
 				conveyor1=ConveyorState.Need_Run;
 		}
     	stateChanged();
 
 	}
 
 
 
 
 
 
 
 
 
 
 
 }
