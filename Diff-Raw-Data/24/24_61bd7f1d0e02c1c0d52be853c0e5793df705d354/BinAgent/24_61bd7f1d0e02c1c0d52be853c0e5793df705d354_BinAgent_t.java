 package engine.agent.Alex;
 
 import java.util.ArrayList;
 import java.util.Queue;
 
 import engine.agent.Agent;
 import engine.conveyorfamily.Interfaces_Poojan.ConveyorFamilyInterface;
 import engine.interfaces.ConveyorFamily;
 import gui.panels.subcontrolpanels.GlassSelectPanel;
 import shared.Barcode;
 import shared.Glass;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 public class BinAgent extends Agent implements ConveyorFamily{
 	/*
 	 * Data
 	 */
 	GlassSelectPanel gui;
 	
 	
 	int numGlassCreated = 0;
 	ArrayList<TransducerEvent> events;
 	ArrayList<GlassRequest> requests;
 	
 	
 	enum TransducerEvent {glassCreated}
 	Transducer transducer;
 	MyConveyorFamily ncCutter;
 	boolean currentlyCreatingGlass = false;
 	boolean nextCFFree = true;
 	
 	
 	
 	private class GlassRequest{
 		public Glass glass;
 		public boolean dealtWith;
 		public GlassRequest(Glass g){
 			glass = g;
 			dealtWith = false;
 			
 		}
 	}
 	private class MyConveyorFamily {
 		public ConveyorFamily conveyor;
 		public MyConveyorFamily(ConveyorFamily cf){
 			conveyor = cf;
 		}
 	}
 	
 	
 	public BinAgent(String n, Transducer t, GlassSelectPanel gui){
 		super (n);
 		this.gui = gui;
 		events = new ArrayList<TransducerEvent>();
 		requests = new ArrayList<GlassRequest>();
 		
 		transducer = t;
 		transducer.register(this, TChannel.BIN);
 		
 	}
 
 
 	/*
 	 * Messages:
 	 */
 	
 	//from transducer
 	private void msgGlassCreated(){
 		System.out.println(name + ": Glass has been created msg recieved from transducer");
 		events.add(TransducerEvent.glassCreated);
 		stateChanged();
 	}
 
 	public void msgIAmFree(){
 		print("NCCUTTER IS FREE");
 		nextCFFree = true;
 		stateChanged();
 	}
 
 	public void msgCreateGlass(Barcode bc){
 		System.out.println(name + ": Message to create glass recieved");
 		boolean[] recipe = bc.translateToRecipe();
 		
 		
 		GlassRequest glassReq = 
 		new GlassRequest(
 		new Glass(numGlassCreated++, recipe[0], recipe[1], 
 				recipe[2], recipe[3],
 				recipe[4], recipe[5], 
 				recipe[6], recipe[7], 
 				recipe[8], recipe[9]));
 		requests.add(glassReq);
 		stateChanged();
 	}
 
 
 
 
 
 	
 
 
 	/*
 	 * Scheduler
 	 * 
 	 */
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// TODO Auto-generated method stub
 		for (TransducerEvent event: events){
 			if (event == TransducerEvent.glassCreated)
 			{
 				events.remove(event);
				giveGlassToNCCutter();
 				return true;
 			}
 		}
 		
 		for (GlassRequest gr: requests){
 			if (!gr.dealtWith && nextCFFree && !currentlyCreatingGlass){
 				createGlassGUI(gr);
 				return true;
 			}
 		}
 		for (GlassRequest gr: requests){
 			if (!gr.dealtWith && !nextCFFree ){
 				warnInGUIWaitingForCFFree(gr);
 				return true;
 			}
 		}
 		
 		for (GlassRequest gr: requests){
 			if (!gr.dealtWith && currentlyCreatingGlass ){
 				warnCreatingGlass(gr);
 				return true;
 			}
 		}
 
 
 
 		return false;
 	}
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		// TODO Auto-generated method stub
 		if (channel == TChannel.BIN && event == TEvent.BIN_PART_CREATED){
 			this.msgGlassCreated();
 		}
 	}
 		/*
 		 *Actions 
 		 */
 	
 
 
 	private void createGlassGUI(GlassRequest gr){
 		gr.dealtWith = true;
 		currentlyCreatingGlass = true;
 		Integer [] args = new Integer[1];
 		transducer.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, args);
 	}
 
	private void giveGlassToNCCutter(){
 		System.out.println(name + ": Giving glass to nc Cutter");
 		nextCFFree = false;
 		//get first glass in requests
		ncCutter.conveyor.msgHereIsGlass(requests.remove(0).glass);
 		currentlyCreatingGlass = false;
 	}
 	private void warnInGUIWaitingForCFFree(GlassRequest gr) {
 		gr.dealtWith = true;
 		gui.warnWaitingForCFFree();
 		
 
 	}
 	private void warnCreatingGlass(GlassRequest gr){
 		gr.dealtWith = true;
 		gui.warnCreatingGlass();
 	}
 
 
 	@Override
 	public void msgHereIsGlass(Glass glass) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 	public void msgHereIsFinishedGlass(Operator operator, Glass glass) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 	public void msgIHaveGlassFinished(Operator operator) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 	public void setNextConveyorFamily(ConveyorFamily c3) {
 		// TODO Auto-generated method stub
 		ncCutter = new MyConveyorFamily(c3);
 	}
 
 
 	@Override
 	public void setPreviousConveyorFamily(ConveyorFamily c2) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void startThreads() {
 		// TODO Auto-generated method stub
 		this.startThread();
 	}
 
 
 	
 
 
 	@Override
 	public void setConveyorBroken(boolean s, int conveyorno) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void setInlineBroken(boolean s, TChannel channel) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 
 
}
