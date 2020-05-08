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
 	
 
 	private Popup_PJ mypopup;
 	private InLineMachine_PJ myinline;
 	
 	public enum GlassStatusConveyor{NEW,DONE,ONENTRYSENSOR,CHECKED, ONEXITSENSOR, NEEDSMACHINEPROCESSING, NOMACHINEPROCESSING, CHECKINGPROCESSING, FIRSTDONE, INLINEBUSY};
 	
 	private Boolean isPopUpBusy;
 	private Boolean isINLINEBusy;
 	private Boolean isConveyorRunning;
 	public boolean isNextConveyorFamilyBusy;
 	
 	
 	private Boolean secondconveyorfree;
 	
 	private List<MyCGlass> glassonconveyor = Collections.synchronizedList(new ArrayList<MyCGlass>());
 	private List<MyOperators> operatorlist = Collections.synchronizedList(new ArrayList<MyOperators>());
 	
 	
 	public ConveyorAgent_PJ(String string,int number, ConveyorFamily c1, Transducer transducer,Popup_PJ p1,InLineMachine_PJ p2,ConveyorFamily cp) {
 		// TODO Auto-generated constructor stub
 	this.name=string;
 	this.number=number;
 	this.MyFamily=c1;
 	this.mypopup=p1;
 	this.myinline=p2;
 	this.PREVIOUSFamily=cp;
 	
	
 	this.isPopUpBusy=false;
 	this.isINLINEBusy=false;
 	myTransducer = transducer;
 	
 	myTransducer.register(this, TChannel.CUTTER);
 	myTransducer.register(this, TChannel.SENSOR);
 	myTransducer.register(this, TChannel.ALL_AGENTS);
 	
 	Object[] args={number};
 	isConveyorRunning=true;
 	isNextConveyorFamilyBusy=false;
 	myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,args);
 	
 	}
 	
 	public class MyCGlass
 	{
 		private Glass pcglass;
 		private GlassStatusConveyor status;
 		private Boolean NeedsProcessing;
 		private MyOperators myoperator;
 		
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
 
 		synchronized(glassonconveyor){
 	    	
 			for(MyCGlass mg:glassonconveyor){
 		
 			    if(mg.status == GlassStatusConveyor.ONENTRYSENSOR ){
 			    	print("CHecking the glass");
 				checktheglass(mg);
 				return true;
 			    }
 			}
 		    	};
 		    	
 		    	synchronized(glassonconveyor){
 			    	
 					for(MyCGlass mg:glassonconveyor){
 				
 					    if(mg.status == GlassStatusConveyor.ONEXITSENSOR ){
 					    	{PassingGlassToInLineMachine(mg);
 					    	return true;
 					    	}
 						
 					    }
 					}
 				    	};  
 				    	
 				    	synchronized(glassonconveyor){
 					    	
 							for(MyCGlass mg:glassonconveyor){
 						
 							    if(mg.status == GlassStatusConveyor.INLINEBUSY && secondconveyorfree ){
 							    	{starttheconveyor(mg);
 							    	return true;
 							    	}
 								
 							    }
 							}
 						    	};  
 						    	
 				   
 				    	
 				    	
 				    	
 				    	
 				    	synchronized(glassonconveyor){
 					    	
 							for(MyCGlass mg:glassonconveyor){
 						
 							    if(mg.status == GlassStatusConveyor.NOMACHINEPROCESSING && isPopUpBusy==false ){
 								PassingGlassToPopup(mg);
 								return true;
 							    }
 							}
 						    	};   	
 				    	   	
 						    	
 						    	synchronized(glassonconveyor){
 							    	
 									for(MyCGlass mg:glassonconveyor){
 								
 									    if(mg.status == GlassStatusConveyor.NEEDSMACHINEPROCESSING){
 									//	PassingGlassToPopupToProcess(mg);
 									    	PassingGlassToInLineMachine(mg);
 										return true;
 									    }
 									}
 								    	};   	
 				    	
 			
 								    	synchronized(glassonconveyor){
 									    	
 											for(MyCGlass mg:glassonconveyor){
 										
 											    if(mg.status == GlassStatusConveyor.DONE){
 											//	PassingGlassToPopupToProcess(mg);
 										    	glassonconveyor.remove(mg);
 										    	
 										    	
 												return true;
 											    }
 											}
 								    	}
 		    	
 		
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 
 	
 
 
 	private void starttheconveyor(MyCGlass mg) {
 		// TODO Auto-generated method stub
 		print("start conveyor 0");
 		Object [] no={this.getNumber()};
     	myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,no);
     	mg.status=GlassStatusConveyor.ONEXITSENSOR;
     	stateChanged();
 	}
 
 
 
 
 
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		
 		
 		
 		// TODO Auto-generated method stub
 		if(channel == TChannel.SENSOR)
 		{
 			if(event == TEvent.SENSOR_GUI_PRESSED)
 			{
 				
 				if((Integer)args[0]==0)
 				{
 					synchronized(glassonconveyor){
 					for(MyCGlass mg:glassonconveyor){
 					
 						if(mg.pcglass.getNumber() == (Integer)args[1]){
 							{
 								Object [] no={this.getNumber()};
 								if(isConveyorRunning)
 								{
 						    	myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,no);
 						    	isConveyorRunning=true;
 								}
 								mg.status=GlassStatusConveyor.ONENTRYSENSOR;
 								print("SENSOR PRESSED");
 								print("Conveyor started"+this.getNumber());
 								stateChanged();
 								return;
 							}
 						}
 					}
 					}
 			    };    	
 			}
 			
 			
 			if(event == TEvent.SENSOR_GUI_RELEASED)
 			{
 				if((Integer)args[0]==0)
 				{
 								print("Sensor 0 Released");
 								this.PREVIOUSFamily.msgIAmFree();
 					}    	
 			}
 			
 			if(event == TEvent.SENSOR_GUI_PRESSED)
 			{
 				if((Integer)args[0]==1)
 				{
 					synchronized(glassonconveyor){
 					for(MyCGlass mg:glassonconveyor){
 						if(mg.pcglass.getNumber() == (Integer)args[1]){
 							{
 								print("2nd sensor");
 								Object [] no={this.getNumber()};
 								isConveyorRunning=true;
 								mg.status=GlassStatusConveyor.ONEXITSENSOR;
 								stateChanged();
 								return;
 							}
 						}
 					}
 					}
 			    };    	
 			}
 			
 			if(event == TEvent.SENSOR_GUI_PRESSED)
 			{
 				if((Integer)args[0]==2)
 				{	
 					if(isNextConveyorFamilyBusy)
 					{
 						
 					}
 					else
 					{
 					Object[] cno ={1};
 					print("NCCUTTER : 3rd sensor");
 					myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,cno);
 					}   
 					 secondconveyorfree=false;
 				}
 			}
 			
 			
 			if(event == TEvent.SENSOR_GUI_RELEASED)
 			{
 				if((Integer)args[0]==2)
 				{
 					isINLINEBusy=false;
 					secondconveyorfree=true;
 				}
 							
 			}
 			
 			
 			
 			
 			if(event == TEvent.SENSOR_GUI_PRESSED)
 			{
 				if((Integer)args[0]==3)
 				{	
 					synchronized(glassonconveyor)
 					{
 					for(MyCGlass mg:glassonconveyor){
 				//		if(mg.pcglass.getNumber() == (Integer)args[1]){
 							{
 								
 								Object[] cno ={1};
 								myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_STOP,cno);
 								  if(!(isNextConveyorFamilyBusy))
 									{
 									  print("4th sensor");
 										Object[] cno1 ={1};
 										isNextConveyorFamilyBusy=true;
 								//		myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_STOP,cno1);
 										this.NEXTFamily.msgHereIsGlass(mg.pcglass);
 										print("RELEASE THE GLASS. PROCESSING DONE");
 										Object[] args1 = {1};
 										myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,cno);
 										
 										mg.status=GlassStatusConveyor.DONE;
 										stateChanged();
 										return;
 									}
 						//	}
 							}
 						}
  
 					} 
 
 				}
 			}
 			
 			
 			
 			
 			if(event == TEvent.SENSOR_GUI_RELEASED)
 			{
 				if((Integer)args[0]==3)
 				{	
 				
 				}    	
 			}
 			
 			
 			
 		}
 		
 	}
 	
 	
 	// MESSAGES
 
 	public void msgHereIsGlass(Glass g1) {
 		// TODO Auto-generated method stub
 		print("Glass Recieved. Conveyor Start"+this.number);
 		MyCGlass mcg = new MyCGlass(g1);
 		mcg.NeedsProcessing = mcg.pcglass.getRecipe(TChannel.CUTTER);
 		glassonconveyor.add(mcg);
 		print("glass number "+g1.getNumber());
 		stateChanged();
 	}
 	
 	public void msgIamFree() {
 		// TODO Auto-generated method stub
 		isPopUpBusy=false;
 	}
 	
 	@Override
 	public void msgOperatorIsfree(Operator_PJ operatorAgent) {
 		// TODO Auto-generated method stub
 		for(MyOperators mg:operatorlist){
 			
 		    if(mg.op == operatorAgent ){
 			mg.occupied=false;
 		    }
 		}
 	}
 	
 	
 	
 	// ACTIONS
 	
 	private void checktheglass(MyCGlass mg) {
 		// TODO Auto-generated method stub
 		print("Checking the functionalities of glass"+mg.pcglass.getRecipe(TChannel.CUTTER));
 		mg.NeedsProcessing=mg.pcglass.getRecipe(TChannel.CUTTER);
 		mg.status=GlassStatusConveyor.CHECKED;
 		stateChanged();
 	}
 	
 	
 	private void PassingGlassToPopup(MyCGlass mg) {
 		// TODO Auto-generated method stub
 		print("Glass passed to the popup. Conveyor starts again");
 		this.mypopup.msgGlassDoesNotNeedProcessing(mg.pcglass);
 		Object[] args1={this.number};
 		myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,args1);
 		isConveyorRunning=true;
 		mg.status=GlassStatusConveyor.DONE;
 		stateChanged();
 	}
 	
 	
 	
 	private void PassingGlassToInLineMachine(MyCGlass mg) {
 		// TODO Auto-generated method stub
 		if(isINLINEBusy)
 		{
 		Object[] cno={0};
 		 myTransducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_STOP,cno);
 		 print("Conveyor Stop because Inline Is BUSY");
 		 mg.status=GlassStatusConveyor.INLINEBUSY;
 		}
 		else
 		{
 			this.myinline.msgGlassNeedsProcessing(mg.pcglass,mg.NeedsProcessing);
 			print("Glass passed to inline machine");
 			mg.status=GlassStatusConveyor.FIRSTDONE;	
 			isINLINEBusy=true;
 		}
 		stateChanged();
 		
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
 	
 	public int getoperatorlist(){
         return operatorlist.size();
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
 		// TODO Auto-generated method stub
 		
 	}
 	
 
 
 	
 
 
 
 
 
 
 
 }
