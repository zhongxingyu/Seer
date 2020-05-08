 package engine.conveyorfamily_Poojan;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import mocks_PJ.MockOperator;
 
 
 import shared.Glass;
 import shared.enums.SharedData.WorkType;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.agent.Agent;
 import engine.agent.Alex.BinAgent;
 import engine.conveyorfamily.Interfaces_Poojan.Conveyor_PJ;
 import engine.conveyorfamily.Interfaces_Poojan.ConveyorFamilyInterface;
 import engine.conveyorfamily.Interfaces_Poojan.InLineMachine_PJ;
 import engine.conveyorfamily.Interfaces_Poojan.Operator_PJ;
 import engine.conveyorfamily.Interfaces_Poojan.Popup_PJ;
 import engine.conveyorfamily.Interfaces_Poojan.TransducerInterface_PJ;
 import engine.conveyorfamily_Poojan.PopupAgent_PJ.GlassStatusPopup;
 import engine.interfaces.ConveyorFamily;
 
 
 public class ConveyorAgent_PJ extends Agent implements Conveyor_PJ {
 
 
 	private String name;
 	private int number;
 	private Transducer myTransducer;
 	public ConveyorFamily MyFamily;
 	public ConveyorFamily NEXTFamily;
 	public ConveyorFamily PREVIOUSFamily;
 
 	private InLineMachine_PJ myinline;
 
 	public enum GlassStatusConveyor{NEW,DONE,ONENTRYSENSOR,CHECKED, ONEXITSENSOR, NEEDSMACHINEPROCESSING, NOMACHINEPROCESSING, CHECKINGPROCESSING, FIRSTDONE, INLINEBUSY, ONLASTSENSOR, ONLASTSENSORSTOP, ONTHIRDSENSOR, THIRDSENSORDONE};
 
 	private Boolean isPopUpBusy;
 	private Boolean isINLINEBusy;
 	private Boolean isConveyorRunning;
 	public boolean isNextConveyorFamilyBusy;
 
 
 	private enum ConveyorState{Running,Stopped,Jammed,Need_Fix, Need_Run, Need_Break};
 	private enum SensorState{Pressed, Released,None};
 
 	ConveyorState conveyor0;
 	ConveyorState conveyor1;
 
 	SensorState sensor0;
 	SensorState sensor1;
 	SensorState sensor2;
 	SensorState sensor3;
 
 	private Boolean secondconveyorfree;
 
 	private List<MyCGlass> glassonconveyor = Collections.synchronizedList(new ArrayList<MyCGlass>());
 
 
 
 	public ConveyorAgent_PJ(String string,int number, ConveyorFamily c1, Transducer transducer,Popup_PJ p1,InLineMachine_PJ p2,ConveyorFamily cp) {
 	this.name=string;
 	this.number=number;
 	this.MyFamily=c1;
 	this.myinline=p2;
 	this.PREVIOUSFamily=cp;
 
 	this.isPopUpBusy=false;
 	this.isINLINEBusy=false;
 	myTransducer = transducer;
 
 	conveyor0=ConveyorState.Need_Run;
 
 	sensor0=SensorState.None;
 	sensor1=SensorState.None;
 	sensor2=SensorState.None;
 	sensor3=SensorState.None;
 
 
 	myTransducer.register(this, TChannel.CUTTER);
 	myTransducer.register(this, TChannel.SENSOR);
 	myTransducer.register(this, TChannel.ALL_AGENTS);
 
 	Object[] conveyornumber={this.number};
 	isConveyorRunning=true;
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
 
 
 
 
 
 
 
 
 
 
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 
 		while(conveyor1==ConveyorState.Need_Break)
 		{
 			breakConveyor(1);
 		}
 
 		while(conveyor0==ConveyorState.Need_Break)
 		{
 			breakConveyor(0);
 		}
 
 
 
 		while(conveyor1==ConveyorState.Need_Fix)
 		{
 			unbreakConveyor(1);
 		}
 
 		while(conveyor0==ConveyorState.Need_Fix)
 		{
 			unbreakConveyor(0);
 		}
 
 
 
 
 
 		while(conveyor0==ConveyorState.Need_Run)
 		{
 			startconveyor0();
 		}
 
 
 		synchronized(glassonconveyor){
 			for(MyCGlass mg:glassonconveyor){
 
 				if(mg.status == GlassStatusConveyor.ONENTRYSENSOR ){
 				checktheglass(mg);
 				return true;
 			    }
 			}
 		};
 
 		synchronized(glassonconveyor){
 
 			 for(MyCGlass mg:glassonconveyor){
 
 				 if(mg.status == GlassStatusConveyor.ONEXITSENSOR && isINLINEBusy){
 					 inlinebusy(mg);
 					 return true;
 				 }
 			 }
 		 }; 
 
 
 		 synchronized(glassonconveyor){
 
 			 for(MyCGlass mg:glassonconveyor){
 
 				 if(mg.status == GlassStatusConveyor.INLINEBUSY && !isINLINEBusy && conveyor0!=ConveyorState.Jammed){
 					 {starttheconveyor(mg);
 					 return true;
 					 }			
 				 }
 			 }
 		 };
 
 		 synchronized(glassonconveyor){
 
 			 for(MyCGlass mg:glassonconveyor){
 
 				 if(mg.status == GlassStatusConveyor.ONEXITSENSOR && !isINLINEBusy){
 					 PassingGlassToInLineMachine(mg);
 					 return true;
 				 }
 			 }
 		 };  
 
 
 		 synchronized(glassonconveyor){
 
 			 for(MyCGlass mg:glassonconveyor){
 
 				 if(mg.status == GlassStatusConveyor.ONTHIRDSENSOR && !isNextConveyorFamilyBusy &&conveyor1!=ConveyorState.Jammed ){
 					 {
						print("STATUS : ON THE THIRD SENSOR");
 						mediatingconveyorstart(mg);
 					 return true;
 					 }			
 				 }
 			 }
 		 };
 
 
		
		 
		 
		 
 
 		 synchronized(glassonconveyor){
 
 			 for(MyCGlass mg:glassonconveyor){
 
 				 if(mg.status == GlassStatusConveyor.ONLASTSENSOR && conveyor1!=ConveyorState.Jammed ){
 					 {
 						 print("STATUS : ON THE LAST SENSOR");
 						 stopconveyor1(mg);
 					 return true;
 					 }			
 				 }
 			 }
 		 };
 
 
 
 
 
 		 synchronized(glassonconveyor){
 
 			 for(MyCGlass mg:glassonconveyor){
 
 				 if(mg.status == GlassStatusConveyor.ONLASTSENSORSTOP && !isNextConveyorFamilyBusy && conveyor1!=ConveyorState.Jammed){
 					 {
 						 print("STATUS : ON THE LAST SENSORSTOP");
 						 shiptheglasstonexyfamily(mg);
 					 return true;
 					 }			
 				 }
 			 }
 		 };
 
		 
	
 
 
 		 synchronized(glassonconveyor){
 
 			 for(MyCGlass mg:glassonconveyor){
 
 				 if(mg.status == GlassStatusConveyor.DONE){
 					 glassonconveyor.remove(mg);
 					 return true;
 				 }
 			 }
 		 }
 
 		return false;
 	}
 
 
 
 	private void unbreakConveyor(int i) {
 		Object [] no={i};
 		if(i==0)
 		{
 			myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,no);
 			conveyor0=ConveyorState.Running;
 		}
 		else
 		{
 			myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,no);
 			conveyor1=ConveyorState.Running;
 		}
     	stateChanged();
 
 	}
 
 
 
 	private void stopconveyor1(MyCGlass mg) {
 		Object[] cno ={this.number+1};
 		myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_STOP,cno);
 		mg.status=GlassStatusConveyor.ONLASTSENSORSTOP;
 		conveyor1=ConveyorState.Stopped;
 	}
 
 
 
 	private void inlinebusy(MyCGlass mg) {
 
 		Object[] cno={this.number};
 		 myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_STOP,cno);
 		 mg.status=GlassStatusConveyor.INLINEBUSY;
 		 conveyor0=ConveyorState.Stopped;
 		 stateChanged();
 	}
 
 
 
 
 
 
 	private void breakConveyor(int i) {
 
 		Object [] no={i};
 		if(i==0)
 		{
 			myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_STOP,no);
 			conveyor0=ConveyorState.Jammed;
 		}
 		else
 		{
 			myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_STOP,no);
 			conveyor1=ConveyorState.Jammed;
 		}
     	stateChanged();
     	
 	}
 
 
 
 
 
 
 	private void starttheconveyor(MyCGlass mg) {
 		print("start conveyor 0");
 		Object [] no={this.getNumber()};
     	myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,no);
     	mg.status=GlassStatusConveyor.ONEXITSENSOR;
     	conveyor0=ConveyorState.Running;
     	stateChanged();
 	}
	
	
	private void starttheconveyor1() {
		print("start conveyor 1");
		Object [] no={this.getNumber()+1};
    	myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,no);
    	conveyor1=ConveyorState.Running;
    	stateChanged();
	}
 
 
 	private void startconveyor0() {
 		Object [] no={this.getNumber()};
     	myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,no);
     	conveyor0=ConveyorState.Running;
     	stateChanged();
 	}
 
 
 
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 
 
 		if(channel == TChannel.SENSOR)
 		{
 			if(event == TEvent.SENSOR_GUI_PRESSED)
 			{
 
 				if((Integer)args[0]==0)
 				{
 					onentrysensor((Integer)args[1]);
 			    };    	
 			}
 
 
 			if(event == TEvent.SENSOR_GUI_RELEASED)
 			{
 				if((Integer)args[0]==0)
 				{
 					this.PREVIOUSFamily.msgIAmFree();
 				}    	
 			}
 
 			if(event == TEvent.SENSOR_GUI_PRESSED)
 			{
 				if((Integer)args[0]==1)
 				{
 					onexitsensor((Integer)args[1]);
 			    };    	
 			}
 
 			if(event == TEvent.SENSOR_GUI_PRESSED)
 			{
 				if((Integer)args[0]==2)
 				{	
 					//mediatingconveyorstart();
 					OnThirdSensor((Integer)args[1]);
 				}
 			}
 
 
 			if(event == TEvent.SENSOR_GUI_RELEASED)
 			{
 				if((Integer)args[0]==2)
 				{
 					isINLINEBusy=false;
 					this.myinline.msgIamFreeForGlass();
 
 				}
 
 			}
 
 
 
 
 			if(event == TEvent.SENSOR_GUI_PRESSED)
 			{
 				if((Integer)args[0]==3)
 				{	
 					onthelastsensor((Integer)args[1]); 
 
 				}
 			}
 
 
 		}
 
 	}
 
 
 
 
 
 
 	private void OnThirdSensor(Integer glassno) {
 		// 
 		synchronized(glassonconveyor){
 			for(MyCGlass mg:glassonconveyor){
 
 				if(mg.pcglass.getNumber() == glassno){
 					{
 						mg.status=GlassStatusConveyor.ONTHIRDSENSOR;
 						stateChanged();
 						return;
 					}
 				}
 			}
 			}
 
 
 
 	}
 
 
 
 	private void onthelastsensor(Integer args) {
 		// 
 		synchronized(glassonconveyor){
 			for(MyCGlass mg:glassonconveyor){
 
 				if(mg.pcglass.getNumber() == args){
 					{
 						mg.status=GlassStatusConveyor.ONLASTSENSOR;
 						stateChanged();
 						return;
 					}
 				}
 			}
 			}
 
 
 
 	}
 
 
 
 	private void shiptheglasstonexyfamily(MyCGlass mg) {
 
 						  Object[] cno ={this.number+1};
 							isNextConveyorFamilyBusy=true;
 
 							this.NEXTFamily.msgHereIsGlass(mg.pcglass);
 							print("RELEASE THE GLASS. PROCESSING DONE");
 							Object[] args1 = {this.number+1};
 							myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,cno);
 							conveyor1=ConveyorState.Running;
 							mg.status=GlassStatusConveyor.DONE;
 							stateChanged();
 
 			}
 
 
 
 
 	// MESSAGES
 
 
 
 
 	public void msgHereIsGlass(Glass g1) {
 		print("Glass Recieved. Conveyor Start"+this.number);
 		MyCGlass mcg = new MyCGlass(g1);
 		mcg.NeedsProcessing = mcg.pcglass.getRecipe(TChannel.CUTTER);
 		glassonconveyor.add(mcg);
 		print("glass number "+g1.getNumber());
 		stateChanged();
 	}
 
 	public void msgIamFree() {
 
 		isPopUpBusy=false;
 		isINLINEBusy=false;
 		stateChanged();
 	}
 
 
 
 	// ACTIONS
 
 	private void checktheglass(MyCGlass mg) {
 
 		print("Checking the functionalities of glass"+mg.pcglass.getRecipe(TChannel.CUTTER));
 		mg.NeedsProcessing=mg.pcglass.getRecipe(TChannel.CUTTER);
 		mg.status=GlassStatusConveyor.CHECKED;
 		stateChanged();
 	}
 
 	private void onentrysensor(Integer args) {
 
 		synchronized(glassonconveyor){
 			for(MyCGlass mg:glassonconveyor){
 
 				if(mg.pcglass.getNumber() == args){
 					{
 						mg.status=GlassStatusConveyor.ONENTRYSENSOR;
 						stateChanged();
 						return;
 					}
 				}
 			}
 			}
 
 	}
 
 
 	private void onexitsensor(Integer args) {
 
 		synchronized(glassonconveyor){
 			for(MyCGlass mg:glassonconveyor){
 
 				if(mg.pcglass.getNumber() == args){
 					{
 						isConveyorRunning=true;
 						mg.status=GlassStatusConveyor.ONEXITSENSOR;
 						stateChanged();
 						return;
 					}
 				}
 			}
 			}
 
 	}
 
 
 
 	private void PassingGlassToInLineMachine(MyCGlass mg) {
 
 			this.myinline.msgGlassNeedsProcessing(mg.pcglass,mg.NeedsProcessing);
 			print("Glass passed to inline machine");
 			mg.status=GlassStatusConveyor.FIRSTDONE;	
 			isINLINEBusy=true;
 			stateChanged();
 	}
 
 
 
 	private void mediatingconveyorstart(MyCGlass mg) {
 
 
 		print("NEXTCONVEYORFAMILY STATUS"+isNextConveyorFamilyBusy);
 
 		Object[] cno ={1};
 		myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,cno);
 		conveyor1=ConveyorState.Running;
 		mg.status=GlassStatusConveyor.THIRDSENSORDONE;
		print("THIRD SENSOR DONE");
 		 stateChanged();
 	}
 
 
 	/*
 private void mediatingconveyorstart(MyCGlass mg) {
 		
 		
 		print("NEXTCONVEYORFAMILY STATUS"+isNextConveyorFamilyBusy);
 		
 		if(!isNextConveyorFamilyBusy && conveyor1!=ConveyorState.Jammed)
 		{
 		Object[] cno ={this.number+1};
 		myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,cno);
 		conveyor1=ConveyorState.Running;
 		}   
 		else if(conveyor1==ConveyorState.Jammed)
 		{
 			conveyor1=ConveyorState.Jammed;
 		}
 		else
 		{
 			conveyor1=ConveyorState.Stopped;
 		}
 		 stateChanged();
 	}
 */
 
 
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
 		return this.isINLINEBusy;
 	}
 
 
 
 	@Override
 	public void setOperator(Operator_PJ o1) {
 		//
 
 	}
 
 
 	@Override
 	public void setisINLINEBusy(Boolean s) {
 		// 
 		isINLINEBusy=s;
 		stateChanged();
 	}
 
 
 
 	@Override
 	public void msgOperatorIsfree(Operator_PJ operatorAgent) {
 		// 
 
 	}
 
 
 
 
 
 
 	public void setbrokenstatus(boolean s,int i) {
 		// 
 		if(s)
 		{
 		Object [] no={i};
 		if(i==0)
 		{
 			conveyor0=ConveyorState.Need_Break;
 		}
 		else
 		{
 			conveyor1=ConveyorState.Need_Break;
 		}
 		}
 		else
 		{
 			Object [] no={i};
 			if(i==0)
 			{
 				conveyor0=ConveyorState.Need_Fix;
 			}
 			else
 			{
 				conveyor1=ConveyorState.Need_Fix;
 			}
 		}
 
     	stateChanged();
 
 	}
 
 
 
 	public void msgIsNextConveyorFamilyBusy() {
 		// TODO Auto-generated method stub
		
 		isNextConveyorFamilyBusy=false;
 		stateChanged();
 	}
 
 
 
 
 
 
 
 
 
 
 
 }
