 package glassLine.agents;
 
 import java.util.*;
 import java.util.concurrent.Semaphore;
 
 import javax.swing.JTextArea;
 
 
 import glassLine.Glass;
 import glassLine.interfaces.Machine;
 import gui.drivers.FactoryDriver;
 import gui.panels.ControlPanel;
 import gui.panels.subcontrolpanels.TracePanel;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 public class OnlineWorkStationAgent extends Agent implements Machine{
 
 	/**
 	 * This class represents an online machine.
 	 * Type : BREAKOUT, MANUAL_BREAKOUT, CUTTER,
 	 * WASHER, UV_LAMP, OVEN, PAINTER
 	 */
 
 	private String type;
 
 	private int guiIndex;  // for communication with GUI through transducer
 	private int capacity;
 	private List<MyGlass> glassList; 
 
 	private enum PrecedingAgentState {none, requestingToSend, sending};
 	private enum FollowingAgentState {none, requestSent, readyToReceive, receiving};
 	private enum GlassState {none, needsProcessing, doneProcessing, broken}
 	private PrecedingAgentState precedingAgentState;
 	private FollowingAgentState followingAgentState;
 	private ConveyorAgent precedingConveyorAgent;
 	private ConveyorAgent followingConveyorAgent;
 	private enum AgentState {functional, broken};
 	private AgentState state;
 	private boolean processing;
 	private boolean breakingGlass;
 	private Semaphore waitForLoadAnimation = new Semaphore(0,true);
 	private Semaphore waitForProcessAnimation = new Semaphore(0,true);
 	private Semaphore waitForReleaseAnimation = new Semaphore(0,true);
 
 
 
 	private class MyGlass {
 		private Glass g;
 		private GlassState state;
 
 		public MyGlass(Glass g){
 			this.g = g;
 			this.state = GlassState.none;
 		}
 	}
 
 	public OnlineWorkStationAgent(String type, int guiIndex, int capacity, Transducer transducer, TracePanel tracePanel){
 		super(type);
 		this.type = type;
 
 
 		this.capacity = capacity;
 		this.transducer = transducer;
 		//this.state = AgentState.notProcessing;
 		this.tracePanel = tracePanel;
 		this.processing = false;
 		this.state = AgentState.functional;
 		this.breakingGlass = false;
 
 
 		// Registering to the appropriate transducer channel
 		try{
 			if(type.equals("BREAKOUT"))
 				this.transducer.register(this, TChannel.BREAKOUT);
 			else if (type.equals("MANUAL_BREAKOUT"))
 				this.transducer.register(this, TChannel.MANUAL_BREAKOUT);
 			else if (type.equals("CUTTER"))
 				this.transducer.register(this, TChannel.CUTTER);
 			else if (type.equals("WASHER"))
 				this.transducer.register(this, TChannel.WASHER);
 			else if (type.equals("UV_LAMP"))
 				this.transducer.register(this, TChannel.UV_LAMP);
 			else if (type.equals("OVEN"))
 				this.transducer.register(this, TChannel.OVEN);
 			else if (type.equals("PAINTER"))
 				this.transducer.register(this, TChannel.PAINTER);
 			else
 				throw new Exception("Invalid Machine Type");
 		}catch(Exception e){
 			System.out.println(e.getMessage());
 		}
 		this.precedingAgentState = PrecedingAgentState.none;
 		this.followingAgentState = FollowingAgentState.none;
 		this.glassList = new ArrayList<MyGlass>();
 
 	}
 
 	/** MESSAGES **/
 
 	/** This message is sent by the preceding ConveyorAgent or by a RobotAgent transferring a piece of glass. 
 	 * @params : Glass g (instance of glass)
 	 **/
 
 	public void msgHereIsGlass(Glass g) {
 		print(this.type + " : Receiving new piece of glass from Conveyor " + precedingConveyorAgent.getConveyorIndex() + ".\n" );
 		glassList.add(new MyGlass(g));
 		if(this.processing)
 			glassList.get(0).state = GlassState.needsProcessing;
 		else 
 			glassList.get(0).state = GlassState.doneProcessing;
 		this.precedingAgentState = PrecedingAgentState.none;
 
 		stateChanged();
 	}
 
 	/** This message is sent by the preceding ConveyorAgent or by a RobotAgent requesting to transfer a piece of glass that needs processing. 
 	 *
 	 **/
 
 	public void msgGlassIsReady(){
 
 		print(this.type + " : Received a glass transfer request from Conveyor " + precedingConveyorAgent.getConveyorIndex() + ".\n");
 
 		this.processing = true;
 		this.precedingAgentState = PrecedingAgentState.requestingToSend;
 
 		stateChanged();
 	}
 
 
 	/** This message is sent by the preceding ConveyorAgent or by a RobotAgent requesting to transfer a piece of glass that doesn't need processing. 
 	 * 
 	 **/
 
 
 	@Override
 	public void msgGlassNeedsThrough() {
 		print(this.type + " : Received a glass transfer request from Conveyor " + precedingConveyorAgent.getConveyorIndex() + ".\n");
 
 		this.processing = false;
 		this.precedingAgentState = PrecedingAgentState.requestingToSend;
 
 
 		stateChanged();
 	}
 
 	/** This message is sent by the following ConveyorAgent or by a RobotAgent requesting to transfer a piece of glass. 
 	 * @params : Glass g (instance of glass)
 	 **/
 
 	public void msgReadyToTakeGlass(){
 		print(this.type + " : Received a confirmation that recipient is ready for glass transfer from Conveyor " + followingConveyorAgent.getConveyorIndex() + ".\n");
 		System.out.println("Received a confirmation that recipient is ready for glass transfer.");
 		this.followingAgentState = FollowingAgentState.readyToReceive;
 
 		stateChanged();
 	}
 
 
 	/** This message is sent when the processing animation is done.
 	 *
 	 **/
 	public void msgGlassDoneProcessing(){
 		print(this.type + " : Received a confirmation that glass is done processing.\n");
 		this.waitForProcessAnimation.release();
 		this.glassList.get(0).state = GlassState.doneProcessing;
 		if(breakingGlass){
 			this.glassList.get(0).state = GlassState.broken;
//			System.out.println("Glass: " + (glassList.get(0) == null) + " " + (glassList.get(0).g == null) + " " + (glassList.get(0).g.myGui == null));
 			this.glassList.get(0).g.myGui.msgPartBroken();
 			this.glassList.get(0).g.broken = true;
 		}
 		stateChanged();
 	}
 
 
 
 	/** This message is sent when the transfer animation is done.
 	 *
 	 **/
 	public void msgGlassRemoved(){
 		print(this.type + " : Glass has been removed.");
 		if(!this.glassList.get(0).g.broken)
 			followingConveyorAgent.msgHereIsGlass(this.glassList.get(0).g);
 		this.glassList.remove(0);
 		this.followingAgentState = FollowingAgentState.none;
 		this.waitForReleaseAnimation.release();
 
 	}
 
 
 	/** This message is sent by the GUI to break a machine.
 	 *
 	 **/
 	public void msgBreakMachine(){
 		print(this.type + " : Machine has broken down.");
 
 		this.state = AgentState.broken;
 		stateChanged();
 	}
 
 	/** This message is sent by the GUI to fix a machine.
 	 * 
 	 */
 	public void msgFixMachine(){
 		print(this.type + " : Machine has been fixed.");
 
 		this.state = AgentState.functional;
 		stateChanged();
 	}
 
 	public void msgBreakGlass(){
 		if(!this.breakingGlass){
 			print(this.type + " : Machine is breaking glass.");
 			this.breakingGlass = true;
 			
 		}else{
 			print(this.type + " : Machine is processing normally.");
 			this.breakingGlass = false;
 			
 		}
 		stateChanged();
 	}
 
 
 
 
 	/** SCHEDULER **/
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 
 		/* If the preceding conveyor agent is requesting to send a piece of glass, 
     check if ready.  */
 
 		/* If a piece of glass needs to be processed or transferred.  */
 		if(!glassList.isEmpty()){
 			if(glassList.get(0).state == GlassState.needsProcessing){
 				if(this.state == AgentState.functional){
 					processGlass();
 					return true;
 				}
 			}else if (glassList.get(0).state == GlassState.doneProcessing ){
 				if(followingAgentState == FollowingAgentState.none){
 					requestToTransferGlass();
 					return true;
 				}else if (followingAgentState == FollowingAgentState.readyToReceive){
 					transferGlass();
 					return true;
 				}
 
 			}else if (glassList.get(0).state == GlassState.broken){
 				removeBrokenGlassFromLine();
 				return true;
 			}
 		}
 
 		if(precedingAgentState == PrecedingAgentState.requestingToSend){
 			if(glassList.size() < capacity){
 				sayReadyToReceive();
 				return true;
 			}
 
 		}
 
 		return false;
 	}
 
 
 	/** ACTIONS **/
 
 	/**This action checks if the machine is ready to receive a piece of glass. 
 	 * 
 	 **/
 	public void sayReadyToReceive(){
 		print(this.type + " : Ready to Receive");
 
 		precedingConveyorAgent.msgReadyToTakeGlass();
 		this.precedingAgentState = PrecedingAgentState.sending;
 		try {
 			this.waitForLoadAnimation.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	
 	}
 
 	/**This action fires an event on the transducer to perform the animation.
 	 * 
 	 **/
 	public void processGlass(){
 		print(this.type + " : Processing Glass");
 
 		Object args[] = new Object[1];
 		args[0] = this.guiIndex;
 		if(type.equals("BREAKOUT"))
 			this.transducer.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_DO_ACTION, args); 
 		else if (type.equals("MANUAL_BREAKOUT"))
 			this.transducer.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_DO_ACTION, args); 
 		else if (type.equals("CUTTER"))
 			this.transducer.fireEvent(TChannel.CUTTER, TEvent.WORKSTATION_DO_ACTION, args); 
 		else if (type.equals("WASHER"))
 			this.transducer.fireEvent(TChannel.WASHER, TEvent.WORKSTATION_DO_ACTION, args); 
 		else if (type.equals("UV_LAMP"))
 			this.transducer.fireEvent(TChannel.UV_LAMP, TEvent.WORKSTATION_DO_ACTION, args); 
 		else if (type.equals("OVEN"))
 			this.transducer.fireEvent(TChannel.OVEN, TEvent.WORKSTATION_DO_ACTION, args); 
 		else if (type.equals("PAINTER"))
 			this.transducer.fireEvent(TChannel.PAINTER, TEvent.WORKSTATION_DO_ACTION, args); 
 		try {
 			this.waitForProcessAnimation.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 	/**This action sends a message to the following CovneyorAgent requesting to transfer a piece of glass.
 	 **/
 	public void requestToTransferGlass(){
 
 
 		print(this.type + " : Request To Transfer");
 
 		followingConveyorAgent.msgGlassIsReady();
 		this.followingAgentState = FollowingAgentState.requestSent;
 
 		//stateChanged();
 	}
 
 	/**This action sends a message to the following CovneyorAgent transferring a piece of glass.
 	 **/
 	public void transferGlass(){
 
 
 		print(this.type + " : Transfering glass");
 
 
 		this.glassList.get(0).state = GlassState.none;
 		this.followingAgentState = FollowingAgentState.receiving;
 		//		followingConveyorAgent.msgHereIsGlass(this.glassList.get(0).g);
 		Object args[] = new Object[1];
 		args[0] = this.guiIndex;
 		if(type.equals("BREAKOUT"))
 			this.transducer.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("MANUAL_BREAKOUT"))
 			this.transducer.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("CUTTER"))
 			this.transducer.fireEvent(TChannel.CUTTER, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("WASHER"))
 			this.transducer.fireEvent(TChannel.WASHER, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("UV_LAMP"))
 			this.transducer.fireEvent(TChannel.UV_LAMP, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("OVEN"))
 			this.transducer.fireEvent(TChannel.OVEN, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("PAINTER"))
 			this.transducer.fireEvent(TChannel.PAINTER, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		try {
 			this.waitForReleaseAnimation.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 	/**This method will simulate removal of broken glass
 	 * 
 	 */
 	public void removeBrokenGlassFromLine(){
 		/*this.transducer.fireEvent(TChannel.
 		 * 
 		 
 		Object args[] = new Object[1];
 		args[0] = this.guiIndex;
 		if(type.equals("BREAKOUT"))
 			this.transducer.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_REMOVE_BROKEN_GLASS, args); 
 		else if (type.equals("MANUAL_BREAKOUT"))
 			this.transducer.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_REMOVE_BROKEN_GLASS, args); 
 		else if (type.equals("CUTTER"))
 			this.transducer.fireEvent(TChannel.CUTTER, TEvent.WORKSTATION_REMOVE_BROKEN_GLASS, args); 
 		else if (type.equals("WASHER"))
 			this.transducer.fireEvent(TChannel.WASHER, TEvent.WORKSTATION_REMOVE_BROKEN_GLASS, args); 
 		else if (type.equals("UV_LAMP"))
 			this.transducer.fireEvent(TChannel.UV_LAMP, TEvent.WORKSTATION_REMOVE_BROKEN_GLASS, args); 
 		else if (type.equals("OVEN"))
 			this.transducer.fireEvent(TChannel.OVEN, TEvent.WORKSTATION_REMOVE_BROKEN_GLASS, args); 
 		else if (type.equals("PAINTER"))
 			this.transducer.fireEvent(TChannel.PAINTER, TEvent.WORKSTATION_REMOVE_BROKEN_GLASS, args); 
 		
 		*/
 		
 		print(this.type + " :  Removing broken glass");
 
 
 		this.glassList.get(0).state = GlassState.none;
 		this.followingAgentState = FollowingAgentState.receiving;
 		//		followingConveyorAgent.msgHereIsGlass(this.glassList.get(0).g);
 		Object args[] = new Object[1];
 		args[0] = this.guiIndex;
 		if(type.equals("BREAKOUT"))
 			this.transducer.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("MANUAL_BREAKOUT"))
 			this.transducer.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("CUTTER"))
 			this.transducer.fireEvent(TChannel.CUTTER, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("WASHER"))
 			this.transducer.fireEvent(TChannel.WASHER, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("UV_LAMP"))
 			this.transducer.fireEvent(TChannel.UV_LAMP, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("OVEN"))
 			this.transducer.fireEvent(TChannel.OVEN, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		else if (type.equals("PAINTER"))
 			this.transducer.fireEvent(TChannel.PAINTER, TEvent.WORKSTATION_RELEASE_GLASS, args); 
 		try {
 			this.waitForReleaseAnimation.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		
 	}
 
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		if(type.equals("BREAKOUT")){
 			if(channel == TChannel.BREAKOUT){
 				if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
 					this.msgGlassDoneProcessing();
 				else if (event == TEvent.WORKSTATION_RELEASE_FINISHED){
 					msgGlassRemoved();
 				}else if (event == TEvent.WORKSTATION_LOAD_FINISHED){
 					waitForLoadAnimation.release();
 				}
 			}
 		}else if (type.equals("MANUAL_BREAKOUT")){
 			if(channel == TChannel.MANUAL_BREAKOUT){
 				if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
 					this.msgGlassDoneProcessing();
 				else if (event == TEvent.WORKSTATION_RELEASE_FINISHED){
 					msgGlassRemoved();
 				}else if (event == TEvent.WORKSTATION_LOAD_FINISHED){
 					waitForLoadAnimation.release();
 				}
 			}
 		}else if (type.equals("CUTTER")){
 			if(channel == TChannel.CUTTER){
 				if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
 					this.msgGlassDoneProcessing();
 				else if (event == TEvent.WORKSTATION_RELEASE_FINISHED){
 					msgGlassRemoved();
 				}else if (event == TEvent.WORKSTATION_LOAD_FINISHED){
 					waitForLoadAnimation.release();
 				}
 			}
 		}else if (type.equals("WASHER")){
 			if(channel == TChannel.WASHER){
 				if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
 					this.msgGlassDoneProcessing();
 				else if (event == TEvent.WORKSTATION_RELEASE_FINISHED){
 					msgGlassRemoved();
 				}else if (event == TEvent.WORKSTATION_LOAD_FINISHED){
 					waitForLoadAnimation.release();
 				}
 			}
 		}else if (type.equals("UV_LAMP")){
 			if(channel == TChannel.UV_LAMP){
 				if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
 					this.msgGlassDoneProcessing();
 				else if (event == TEvent.WORKSTATION_RELEASE_FINISHED){
 					msgGlassRemoved();
 				}else if (event == TEvent.WORKSTATION_LOAD_FINISHED){
 					waitForLoadAnimation.release();
 				}
 			}
 		}else if (type.equals("OVEN")){
 			if(channel == TChannel.OVEN){
 				if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
 					this.msgGlassDoneProcessing();
 				else if (event == TEvent.WORKSTATION_RELEASE_FINISHED){
 					msgGlassRemoved();
 				}else if (event == TEvent.WORKSTATION_LOAD_FINISHED){
 					waitForLoadAnimation.release();
 				}
 			}
 		}else if (type.equals("PAINTER")){
 			if(channel == TChannel.PAINTER){
 				if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
 					this.msgGlassDoneProcessing();
 				else if (event == TEvent.WORKSTATION_RELEASE_FINISHED){
 					msgGlassRemoved();
 				}else if (event == TEvent.WORKSTATION_LOAD_FINISHED){
 					waitForLoadAnimation.release();
 				}
 			}
 
 		}
 	}
 
 
 	public void setConveyors(ConveyorAgent preceding, ConveyorAgent following){
 		this.precedingConveyorAgent = preceding;
 		this.followingConveyorAgent = following;
 	}
 
 	public String getType(){
 		return type;
 	}
 
 
 
 
 }
