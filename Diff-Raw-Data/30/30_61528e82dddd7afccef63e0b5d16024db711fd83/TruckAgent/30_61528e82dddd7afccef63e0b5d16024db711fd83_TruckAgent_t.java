 package engine.agent;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.interfaces.ConveyorFamily;
 import engine.util.GlassType;
 
 public class TruckAgent extends Agent implements ConveyorFamily {
 
 	ConveyorFamily previousComponent;
 	
 	// The state of the truck
 	TruckState state;
 	
 	// the size of the truck
 	int glassSize = 3;
 
 	enum TruckState{LOADING,LEAVING,DOINGNOTHING};
 	
 	List <GlassType> glasses = Collections.synchronizedList(new ArrayList<GlassType>());
 	List <GlassType> currentGlass = Collections.synchronizedList(new ArrayList<GlassType>());
 	String name;
 
 	public TruckAgent(Transducer t, String name){
 		super(name, t);
 		previousComponent = null;
 		state = TruckState.LOADING;
 		stateChanged();
 	}
 
 
 
	
	//Messages
	
 	@Override
 	public void msgPassingGlass(GlassType gt) {
 		currentGlass.add(gt);
 		state = TruckState.LOADING;
 		stateChanged();
 	}
 
	
 	@Override
 	public void msgIAmAvailable() {
 		// Empty method
 	}
 	
 	
 	public void msgGlassLoadedToTruck(){
 		state = TruckState.LOADING;
 		stateChanged();
 	}
 	
 	public void msgTruckIsBack(){
 		state = TruckState.LOADING;
 		stateChanged();
 	}
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		
 		if(glasses.size() == glassSize && state == TruckState.LOADING){
 			transducer.fireEvent(TChannel.TRUCK, TEvent.TRUCK_DO_EMPTY, null);
 			state = TruckState.LEAVING;
 			return true;
 		}
 		
 		if(glasses.size() < glassSize && state == TruckState.LOADING){
 			tellIAmAvailable();
 			state = TruckState.DOINGNOTHING;
 			return true;
 		}
 		
 //		if(state == TruckState.RETURNING){
 //			state = TruckState.LOADING;
 //			return true;
 //		}
 		
 		
 		return false;
 	}
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		// TODO Auto-generated method stub
 		if(event == TEvent.TRUCK_GUI_EMPTY_FINISHED){
 			//state = TruckState.RETURNING;
 			this.msgTruckIsBack();
 		}
 		if(event == TEvent.TRUCK_GUI_LOAD_FINISHED){
 			if(currentGlass.size() > 0){
 				glasses.add(currentGlass.remove(0));
 				this.msgGlassLoadedToTruck();
 			}else
 				System.out.println("Loading error");
 		}
 
 	}
 
 	// ACTION
 	public void tellIAmAvailable(){	
 		previousComponent.msgIAmAvailable();
 		//state = TruckState.LOADING;
 	}
 
 	public void setPreviousComponent(ConveyorFamily previousC){
 		previousComponent = previousC;
 	}
 }
