 package engine.agent.Alex;
 import java.util.ArrayList;
 
 import shared.Glass;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.agent.Agent;
 
 import engine.interfaces.*;
 
 public class AlexsConveyorFamily extends Agent implements ConveyorFamily {
 	public ConveyorFamily previousCF;
 	public ConveyorFamily nextCF;
 	public EntryAgent entryAgent;
 	public ConveyorAgent conveyorAgent;
 
 	static int cfIndex;
 	
 	public ArrayList<MyGlass> glassOnCF;
 	public Transducer transducer;
 	public enum ConveyorFamilyEvent {pushGlass};
 	
 	public ArrayList<ConveyorFamilyEvent> events;
 	//public EventLog log = new EventLog();
 
 	private TChannel channel;
 	
 	public class MyGlass{
 		MyGlass(Glass g){
 			glass = g;
 			hasBeenProcessed = false;
 			needsProcessing = false;
 			//needsProcessing = glass.getRecipe(channel);
 		}
 		public Glass glass;
 		public boolean hasBeenProcessed;
 		public boolean needsProcessing;
 	}
 
 	
 	public AlexsConveyorFamily(String n, Transducer t, int index, TChannel c) {
 		super(n);
 		events = new ArrayList<ConveyorFamilyEvent>();
 		channel = c;
 		
 		entryAgent = new EntryAgent("entry agent", this);
		conveyorAgent = new ConveyorAgent("conveyor agent", this, index);
 			
 		entryAgent.setConveyorAgent(conveyorAgent);
 		conveyorAgent.setEntryAgent(entryAgent);
 		
 
 		 glassOnCF = new ArrayList<MyGlass>();
 		// TODO Auto-generated constructor stub
 		t.register(this, TChannel.SENSOR);
 		cfIndex = index;
 		transducer = t;
 		conveyorAgent.setTransducer(transducer);
 	}
 	
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// TODO Auto-generated method stub\
 
 		return false;
 	}
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		// TODO Auto-generated method stub
 		if (channel == TChannel.SENSOR){
 			//if one of the proper sensors
 			if ((Integer)args[0] == cfIndex * 2){
 						
 				
 				if (event == TEvent.SENSOR_GUI_PRESSED){
 					entryAgent.msgSensorPressed();
 				}
 				else if (event == TEvent.SENSOR_GUI_RELEASED){
 					entryAgent.msgSensorReleased();
 				}
 			}
 			if ((Integer)args[0] == cfIndex * 2 + 1){
 				if (event == TEvent.SENSOR_GUI_PRESSED){
 					conveyorAgent.msgSensorPressed();
 				}
 				else if (event == TEvent.SENSOR_GUI_RELEASED){
 					conveyorAgent.msgSensorReleased();
 				}
 			}
 			
 		}
 		
 		
 		
 	}
 	public void msgHereIsGlass(Glass g){
 	//from previous CFring m
 		print("Message, here is glass");
 		MyGlass mg = new MyGlass(g);
 		
 		glassOnCF.add(mg);
 		
 	}
 	public void msgIHaveGlassFinshed(Operator o){
 		int a  = 6/0; //throw if you get here, this has no popup
 		
 	}
 	
 	public void msgHereIsFinishedGlass(Operator o, Glass g){
 		int a  = 6/0; //throw if you get here, this has no popup
 		
 		
 	}
 	public void msgSendImFreeMsgToCF() {
 		// TODO Auto-generated method stub
 		previousCF.msgIAmFree();
 	}
 
 
 
 	public void stopConveyor() {
 		Integer [] args= new Integer[1];
 		args[0] = cfIndex;
 		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);
 	}
 
 	public void startConveyor() {
 
 		Integer [] args= new Integer[1];
 		args[0] = cfIndex;
 		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
 	}
  
 	public void movePopupDown(){
 		int a = 6 / 0;
 		System.out.println("CF moving popup down!");
 		Integer [] args= new Integer[1];
 		args[0] = cfIndex;
 		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
 	}
 	
 	public void movePopupUp(){
 		
 		int a = 6 /0;
 		System.out.println("telling transducer to move up");
 		Integer [] args= new Integer[1];
 		args[0] = cfIndex;
 		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_UP, args);
 	}
 	
 	
 	
 
 	
 	public void pushGlass(){
 		Integer [] args= new Integer[1];
 		args[0] = cfIndex;
 		nextCF.msgHereIsGlass(glassOnCF.remove(glassOnCF.size()-1).glass);
 		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, args);
 	}
 	public boolean doesLastGlassOnCFNeedProcessing(){
 		if (!glassOnCF.isEmpty())
 			return glassOnCF.get(glassOnCF.size()-1).needsProcessing;
 		else 
 			
 			return false;
 	}
 
 	public int numGlassOnLine() {
 		// TODO Auto-generated method stub
 		return glassOnCF.size();
 	}
 
 	public boolean lastItemBeenProcessed() {
 		// TODO Auto-generated method stub
 		if (!glassOnCF.isEmpty())
 			return glassOnCF.get(glassOnCF.size() - 1).hasBeenProcessed;
 		return true; //if nothing on line
 	}
 
 	public boolean lineEmpty() {
 		// TODO Auto-generated method stub
 		return glassOnCF.isEmpty();
 	}
 
 	public void giveGlassToOperator(Operator operator) {
 		
 		if (!glassOnCF.isEmpty())
 			operator.msgHereIsGlass( glassOnCF.remove(glassOnCF.size() - 1).glass);
 	}
 
 	@Override
 	public void msgIHaveGlassFinished(Operator operator) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void msgIAmFree() {
 		// TODO Auto-generated method stub
 		conveyorAgent.msgImFree();
 	}
 
 	@Override
 	public void setNextConveyorFamily(ConveyorFamily c3) {
 		nextCF = c3;
 	}
 
 	@Override
 	public void setPreviousConveyorFamily(ConveyorFamily c2) {
 		previousCF = c2;
 	}
 
 	@Override
 	public void startThreads() {
 		entryAgent.startThread();
 		conveyorAgent.startThread();
 		this.startThread();
 	}
 
 
 	@Override
 	public void setConveyorBroken(boolean s, int conveyorno) {
 		print("******");
 
 		conveyorAgent.setConveyorBroken(s);
 	}
 
 	public void notifyPreviousCFFree() {
 		previousCF.msgIAmFree();
 	}
 
 	@Override
 	public void setInlineBroken(boolean s, TChannel channel) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 
 }
