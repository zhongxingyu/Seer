 package engine.agent.Luis;
 
 import java.util.*;
 import java.util.concurrent.Semaphore;
 
 import shared.Glass;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 import engine.agent.Agent;
 import engine.agent.Alex.Operator;
 import engine.interfaces.ConveyorFamily;
 
 public class Truck_Agent_LV extends Agent implements ConveyorFamily{
 	
 	String name;
 	ConveyorFamily previousFamily;
 	Transducer t;
 	List<Glass> truckGlass;
 	enum TruckState{PARKED,COMMUTING,ARRIVED,LOADING};
 	Semaphore drivingSemaphore = new Semaphore(0,true);
 	TruckState state;
 	
 	public Truck_Agent_LV(String n)
 	{
 		name = n;
 		truckGlass = new ArrayList<Glass>();
 		state = TruckState.ARRIVED;
 	}
 
 	//Messages
 	@Override
 	public void msgHereIsGlass(Glass glass)
 	{
 		truckGlass.add(glass);
 		stateChanged();
 	}
 	
 	//Scheduler
 	public boolean pickAndExecuteAnAction() {
 
 		if(state == TruckState.ARRIVED)
 		{
 			tellConveyorIAmFree();
 			return true;
 		}
 		
 		if(state == TruckState.LOADING)
 		{
 			for(Glass g : truckGlass)
 			{
 				moveGlass(g);
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 
 		if(channel == TChannel.TRUCK)
 		{
 			if(event == TEvent.TRUCK_GUI_LOAD_FINISHED)
 			{
 				state = TruckState.LOADING;
 			}
 			else if(event == TEvent.TRUCK_GUI_EMPTY_FINISHED)
 			{
 				state = TruckState.ARRIVED;
 			}
 		}
 		
 		stateChanged();
 		
 	}
 	
 	//Actions
 	public void tellConveyorIAmFree()
 	{
 		print("Letting conveyor know I can load glass");
 		if(truckGlass.size()!=0)
 			truckGlass.remove(0);
 		previousFamily.msgIAmFree();
 		state = TruckState.PARKED;
 	}
 	
 	public void moveGlass(Glass g)
 	{
 		print("Delivering Glass");
		Integer[] args = new Integer[1];
		args[0] = 0;
		t.fireEvent(TChannel.TRUCK, TEvent.TRUCK_DO_EMPTY,args);
 		state = TruckState.COMMUTING;	
 	}
 
 	@Override
 	public void msgHereIsFinishedGlass(Operator operator, Glass glass) {
 		// Nothing
 	}
 
 	@Override
 	public void msgIHaveGlassFinished(Operator operator) {
 		// Nothing	
 	}
 
 	@Override
 	public void msgIAmFree() {
 		// Nothing	
 	}
 
 	@Override
 	public void setNextConveyorFamily(ConveyorFamily c3) {
 		// Nothing
 	}
 
 	@Override
 	public void setPreviousConveyorFamily(ConveyorFamily cf) {
 		previousFamily = cf;		
 	}
 	
 	public void setTransducer(Transducer trans){
 		t = trans;
 		t.register(this, TChannel.TRUCK);
 	}
 
 	@Override
 	public void startThreads() {
 		// Nothing
 	}
 	
 	@Override
 	public String getName(){
 		return name;
 	}
 }
