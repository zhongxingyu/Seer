 package engine.conveyorfamily_Poojan;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import shared.Glass;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.agent.Agent;
 import engine.conveyorfamily.Interfaces_Poojan.Conveyor_PJ;
 import engine.conveyorfamily.Interfaces_Poojan.ConveyorFamilyInterface;
 import engine.conveyorfamily.Interfaces_Poojan.InLineMachine_PJ;
 import engine.conveyorfamily.Interfaces_Poojan.Operator_PJ;
 import engine.conveyorfamily.Interfaces_Poojan.Popup_PJ;
 import engine.conveyorfamily.Interfaces_Poojan.TransducerInterface_PJ;
 import engine.conveyorfamily_Poojan.ConveyorAgent_PJ.MyCGlass;
 import engine.conveyorfamily_Poojan.ConveyorAgent_PJ.MyOperators;
 import engine.interfaces.ConveyorFamily;
 
 
 
 public class InLineMachineAgent_PJ extends Agent implements InLineMachine_PJ  {
 
 	private int number;
 	private String name;
 	private Transducer myTransducer;
 	public Conveyor_PJ myconveyor;
 	public HalfConveyorAgent halfconveyor;
 	public ConveyorFamily MyFamily;
 	private ConveyorFamily NEXTFamily;
 
 	private List<MyPGlass> glassoninline = Collections.synchronizedList(new ArrayList<MyPGlass>());
 	private List<MyPGlass> finishedglassonpopup = Collections.synchronizedList(new ArrayList<MyPGlass>());
 
 	private boolean inlinebroken;
 
 	private boolean secondconveyorfree;
 
 	private enum GlassStatusInline{NEW,CHECKING,NOPROCESSING,PROCESSING,BEINGPROCESSED, DONE,DONE2, PROCESSINGDONE};
 	public InLineMachineAgent_PJ(String string, int i, ConveyorFamily conveyorFamily,
 			Transducer transducer) {
 		// TODO Auto-generated constructor stub
 
 		this.name=string;
 		this.number=i;
 		secondconveyorfree=true;
 		this.MyFamily=conveyorFamily;
 
 		myTransducer = transducer;
 		myTransducer.register(this, TChannel.ALL_GUI);
 		myTransducer.register(this, TChannel.CUTTER);
 		myTransducer.register(this, TChannel.CONVEYOR);
 		myTransducer.register(this, TChannel.ALL_AGENTS);
 	}
 
 
 
 
 	public class MyPGlass
 	{
 		private Glass pcglass;
 		private GlassStatusInline status;
 		Boolean NeedsProcessing;
 
 		public MyPGlass(Glass g, boolean b)
 		{
 			this.pcglass=g;
 			this.status=GlassStatusInline.NEW;
 			this.NeedsProcessing=b;
 			print(""+b);
 		}
 	}
 
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 
 		if(inlinebroken)
 		{
 			return false;
 		}
 		
 	synchronized(glassoninline){
 
 			for(MyPGlass mg:glassoninline){
 
 			    if(mg.status == GlassStatusInline.CHECKING ){
 				checkingforglass(mg);
 				return true;
 			    }
 			}
 		    	};
 
 
 		    	synchronized(glassoninline){
 
 					for(MyPGlass mg:glassoninline){
 
 					    if(mg.status == GlassStatusInline.PROCESSING ){
 					    processtheglassaction(mg);
 						return true;
 					    }
 					}
 				    	};    	
 
 
 		synchronized(glassoninline){
 
 			for(MyPGlass mg:glassoninline){
 			    if(mg.status == GlassStatusInline.NOPROCESSING && secondconveyorfree){
 
 			    shiptheglasstonextconveyor(mg);
 				return true;
 			    }
 			}
 		    	};
 
 		    	synchronized(glassoninline){
 
 					for(MyPGlass mg:glassoninline){
 					    if(mg.status == GlassStatusInline.PROCESSINGDONE && secondconveyorfree ){
 
 						shiptheglasstonextconveyor(mg);
 						return true;
 					    }
 					}
 				    	}; 	
 
 
 
 				    	synchronized(glassoninline){
 
 							for(MyPGlass mg:glassoninline){
 
 							    if(mg.status == GlassStatusInline.DONE ){
 								glassoninline.remove(mg);
 								return true;
 							    }
 								}
 						    	}; 	
 
 
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	private void shiptheglasstonextconveyor(MyPGlass mg) {
 		// TODO Auto-generated method stub
 		print("RELEASING GLASS");
 		
 		mg.status=GlassStatusInline.DONE;
 		this.halfconveyor.msgHereIsGlass(mg.pcglass);
 		myTransducer.fireEvent(TChannel.CUTTER, TEvent.WORKSTATION_RELEASE_GLASS, null);
 		secondconveyorfree=false;
 		//myconveyor.msgIamFree();
 		stateChanged();
 
 
 	}
 
 
 	private void glassdone(MyPGlass mg)
 	{
 		mg.status=GlassStatusInline.DONE2;
 		stateChanged();
 	}
 
 
 
 	private void processtheglassaction(MyPGlass mg) {
 		// TODO Auto-generated method stub
 		myTransducer.fireEvent(TChannel.CUTTER, TEvent.WORKSTATION_DO_ACTION, null);
 		print("Workstation do action");
 		mg.status=GlassStatusInline.DONE2;
 		stateChanged();
 	}
 
 
 
 
 
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		// TODO Auto-generated method stub
 		if(channel == TChannel.CUTTER)
 		{
 			if(event == TEvent.WORKSTATION_LOAD_FINISHED)
 			{
 				synchronized(glassoninline){
 					for(MyPGlass mg:glassoninline){
 					    if(mg.status == GlassStatusInline.NEW ){
 					    	print("cutter called");
 						mg.status=GlassStatusInline.CHECKING;
 
 
 						stateChanged();
 						return;
 					    }	
 					}
 			}
 			}
 
 			if(event == TEvent.WORKSTATION_LOAD_FINISHED)
 			{
 
 				synchronized(glassoninline){
 					for(MyPGlass mg:glassoninline){
 						 if(mg.status == GlassStatusInline.DONE2 ){
 							 mg.status=GlassStatusInline.PROCESSINGDONE;
 							 print("Load fininshed");
 
 								stateChanged();
 								return;
 							    }	
 					}
 			}
 			}
 
 
 
 			if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
 			{
 
 				synchronized(glassoninline){
 					for(MyPGlass mg:glassoninline){
 					    if(mg.status == GlassStatusInline.DONE2 ){
 					    	mg.status=GlassStatusInline.PROCESSINGDONE;
 
 						stateChanged();
 						return;
 					    }	
 					}
 			}
 			}
 
 
 			}
 
 
 	}
 
 
 
 	public String getName(){
         return name;
     }
 
 	public int getNumber(){
         return number;
     }
 
 
 	public int getglassonpopupsize(){
         return  glassoninline.size();
     }
 
 
 	public int getfinishedglassonpopupsize(){
         return  finishedglassonpopup.size();
     }
 
 
 	public void setConveyor(Conveyor_PJ conveyor,HalfConveyorAgent halfconveyor) {
 		// TODO Auto-generated method stub
 		this.myconveyor=conveyor;
 		this.halfconveyor=halfconveyor;
 	}
 
 	// MESSAGES
 
 	@Override
 	public void msgGlassDoesNotNeedProcessing(Glass glass) {
 		print("Glass received. Glass does not need processing");
 		// TODO Auto-generated method stub
 		glassoninline.add(new MyPGlass(glass,false));
 		stateChanged();
 	}
 
 	public void msgGlassNeedsProcessing(Glass pcglass,Boolean choice) {
 		// TODO Auto-generated method stub
 		print("Glass received. Glass needs processing"+choice+pcglass.getNumber());
 		glassoninline.add(new MyPGlass(pcglass,choice));
 		stateChanged();
 	}
 
 
 
 
 	private void checkingforglass(MyPGlass mg)
 	{
 		if(mg.NeedsProcessing==true)
 		{
 			mg.status=GlassStatusInline.PROCESSING;
 		}
 		else
 		{
 			mg.status=GlassStatusInline.NOPROCESSING;
 			print("NO PRocessing");
 
 		}
 		stateChanged();
 	}
 	
 	public void setNextFamily(ConveyorFamily c0)
 	{
 		NEXTFamily=c0;
 	}
 
 
 
 
 	@Override
 	public void msgIamFreeForGlass() {
 		// TODO Auto-generated method stub
 		print("HALF CONVEYOR IS FREE FOR GLASS");
 		secondconveyorfree = true;
 		stateChanged();
 	}
 
 
 	public void setbroken(boolean s) {
 		inlinebroken=s;
 		stateChanged();
 	}
 
 
 
 
 
 
 }
