 package engine.agent.Yinong;
 
 import shared.Glass;
 import transducer.TChannel;
 import transducer.Transducer;
 import engine.agent.Yinong.ConveyorAgent.Mode;
 import engine.interfaces.*;
 import engine.agent.Alex.*;
 
 //This is not truly an agent.
 public class ConveyorFamilyAgents implements ConveyorFamily {
 	
 	//data
 	int index;
 	String function;
 	ConveyorAgent conveyor;
 	PopupAgent popup;
 	InlineAgent inline;
 	boolean isOffline;
 	
 	private ConveyorFamilyAgents() {}
 	public ConveyorFamilyAgents(int i, String f, boolean offline) {
 		index = i;
 		function = f;
 		isOffline = offline;
 		
 		if(isOffline) {			//Conveyor Family 1-3
			conveyor = new ConveyorAgent("Conveyor"+(index+1), index, Mode.OFFLINE);
			popup = new PopupAgent("Popup"+(index+1), index);
 			conveyor.setPopup(popup);
 			popup.setConveyor(conveyor);
 			inline = null;
 		} else {				//Conveyor Family 4-7
			conveyor = new ConveyorAgent("Conveyor"+(index+1), index, Mode.ONLINE);
 			popup = null;
			inline = new InlineAgent(index, "Inline"+(index+1), "nothing");
 			conveyor.setInline(inline);
 			inline.setConveyor(conveyor);
 		}
 	}
 
 	@Override
 	public void msgHereIsGlass(Glass glass) {
 		System.out.println("msg Glass recieved 2nd family");
 		conveyor.msgHereIsGlass(glass);
 	}
 
 	@Override
 	public void msgHereIsFinishedGlass(Operator operator, Glass glass) {
 		popup.msgHereIsFinishedGlass(glass);
 	}
 	@Override
 	public void msgIAmFree() {
 		if(isOffline)
 			popup.msgIAmFree();
 		else
 			inline.msgIAmFree();
 	}
 
 	public void setOperator(int index, Operator o, TChannel c, int operatorIndex) {
 		popup.setOperator(index, o, c, operatorIndex);
 	}
 	@Override
 	public void msgIHaveGlassFinished(Operator operator) {
 		popup.msgIHaveGlassFinished(operator);
 	}
 	@Override
 	public String getName() {
 		
 		return null;
 	}
 	@Override
 	public void setNextConveyorFamily(ConveyorFamily c3) {
 		if(isOffline) {
 			popup.setNextConveyorFamily(c3);
 		} else {
 			inline.setNextConveyorFamily(c3);
 		}
 	}
 	@Override
 	public void startThreads() {
 		conveyor.startThread();
 		if(popup != null) {
 			popup.startThread();
 		}
 		if(inline != null) {
 			inline.startThread();
 		}
 	}
 	@Override
 	public void setPreviousConveyorFamily(ConveyorFamily c2) {
 		conveyor.setPreviousConveyorFamily(c2);
 	}
 	
 	public void setTransducer(Transducer t) {
 		conveyor.setTransducer(t);
 		if(popup != null)
 			popup.setTransducer(t);
 		if(inline != null)
 			inline.setTransducer(t);
 	}
 	
 	public void setChannel(TChannel c) {
 		if(popup != null)
 			popup.setTChannel(c);
 		if(inline != null)
 			inline.setChannel(c);
 	}
 }
