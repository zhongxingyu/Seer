 package engine.minh.agent;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.agent.Agent;
 import engine.interfaces.ConveyorFamily;
 import engine.util.GlassType;
 
 enum ConveyorState {
 	NOTHING,
 	READY,
 	WAITING
 }
 public class BinAgent extends Agent implements ConveyorFamily {
 
 	public class NextComponent{
 		ConveyorFamily nextComponent;
 		ConveyorState state;		
 		public NextComponent(ConveyorFamily p, ConveyorState s){
 			this.nextComponent = p;
 			this.state = s;
 		}
 	}
 
 	NextComponent nextComponent;
 	List <GlassType> glasses = Collections.synchronizedList(new ArrayList<GlassType>());
 	String name;
 
 	public BinAgent(Transducer t, String name){
 		super(name, t);
 		nextComponent = null;
 	}
 	
 
 	
 
 	@Override
 	public void msgPassingGlass(GlassType gt) {
 		// EMPTY METHOD
 	}
 
 	@Override
 	public void msgIAmAvailable() {
 		nextComponent.state = ConveyorState.WAITING;
 		stateChanged();
 	}
 
 	@Override
 	public void msgIAmNotAvailable() {
		// TODO Auto-generated method stub
		nextComponent.state = ConveyorState.NOTHING;
		stateChanged();
 	}
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// the popup is waiting for glass.
 		if(nextComponent.state == ConveyorState.WAITING && glasses.size() > 0){
 			sendingGlass();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 	// ACTION
 	// Send the glass to the first conveyer. Also sends a fireEvent to create
 	// the GUI Glass
 	public void sendingGlass(){
 		nextComponent.nextComponent.msgPassingGlass(glasses.remove(0));
 		if(transducer != null){
 //			new Timer().schedule(new TimerTask(){
 //			    public void run(){//this routine is like a message reception    
 			    	transducer.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
 
 			
 		}else{
 			System.out.println("transducer is null");
 		}
 		//glasses.remove(0);
 		nextComponent.state = ConveyorState.NOTHING;
 		//stateChanged();
 	}
 
 	//Create GlassType out of Configs
 //	public void hereIsConfig(ArrayList<Config> configs){
 //		for(int i = 0; i < configs.size();i++){
 //			glasses.add(new GlassType(configs.get(0).getConfig(0),configs.get(0).getConfig(1),
 //					configs.get(0).getConfig(2), configs.get(0).getConfigID()));
 //		}
 //		System.out.println("hereIsConfig" + glasses.size() + nextComponent.state);
 //		stateChanged();
 //	}
 
 	//alex: change the type reference received
 	public void hereIsConfig(GlassType configs){
 			glasses.add(configs);
 		//System.out.println("hereIsConfig" + glasses.size() + nextComponent.state);
 		stateChanged();
 	}
 	
 	
 	
 	public void setNextComponent(ConveyorFamily nextC){
 		nextComponent = new NextComponent(nextC, ConveyorState.NOTHING);
 	}
 
 
 
 
 
 }
