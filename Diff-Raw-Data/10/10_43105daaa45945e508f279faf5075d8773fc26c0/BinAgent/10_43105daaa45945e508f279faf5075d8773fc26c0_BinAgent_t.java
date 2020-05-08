 package engine.ryanCF.agent;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.agent.Agent;
 import engine.ryanCF.interfaces.Bin;
 import engine.agent.shared.*;
 
 public class BinAgent extends Agent implements Bin{
 
 	List<Glass> glassInBin = new ArrayList<Glass>();
 	
 	ConveyorFamilyOnlineMachine cfom;
 	Transducer t;
 	//ConveyorFamily0 
 	public BinAgent(Transducer t) {
		this.name = "Bin Agent";
 		this.t = t;
 	}
 	public void msgProcessGlassOrder(List<Glass> glassList) {
 		glassInBin.addAll(glassList);
 		stateChanged();
 	}
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// TODO Auto-generated method stub
 		if(!glassInBin.isEmpty()) {
 			sendGlassToCF(glassInBin.remove(0));
 			return true;
 		}
 		return false;
 	}
 
 	public void sendGlassToCF(Glass g) {
 		// TODO Auto-generated method stub
 		cfom.msgHereIsGlass(g);
 		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
 	}
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		// TODO Auto-generated method stub
 		
 	}
 	public void setConveyorFamilyOnlineMachine(ConveyorFamilyOnlineMachine cfom) {
 		this.cfom = cfom;
 	}
 }
