 package engine.agent.Yinong;
 
 import shared.*;
 import transducer.*;
 import engine.agent.Agent;
 import engine.interfaces.*;
 import engine.interfaces.Yinong.Conveyor;
 import engine.interfaces.Yinong.Inline;
 
 import java.util.concurrent.*;
 
 public class InlineAgent extends Agent implements Inline {
 	
 	//data
 	int index;
 	String function;
 	Transducer transducer;
 	TChannel channel;
 	
 	Glass glassOnSpot;
 	ConveyorFamily next;
 	boolean nextFree;
 	
 	Conveyor conveyor;
 	Semaphore machineSemaphore = new Semaphore(0, true);
 	
 	//Constructor
 	private InlineAgent () {}
 	
 	public InlineAgent(int i, String n, String func) {
 		index = i;
 		name = n;
 		function = func;
 		
 		glassOnSpot = null;
 		nextFree = true;
 	}
 
 	//Messages and Eventfires
 	@Override
 	public void eventFired(TChannel c, TEvent event, Object[] args) {
 		if(channel == c) {
 			if(event == TEvent.WORKSTATION_LOAD_FINISHED) {
 				machineSemaphore.release();
 				Do("Glass loading finished.");
 			}
 			if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED) {
 				machineSemaphore.release();
 				Do("Glass processing finished.");
 			}
 			if(event == TEvent.WORKSTATION_RELEASE_FINISHED) {
 				machineSemaphore.release();
 				Do("Glass unloading finished.");
 			}
 		}
 	}
 
 	public void msgHereIsGlass(Glass glass) {
 		if(glassOnSpot != null) {
 			try {
 				throw new Exception ("Anyone shouldn't send in a glass when I already have a glass.");
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		glassOnSpot = glass;
 		Do("Received a glass.");
 		stateChanged();
 	}
 
 	public void msgIAmFree() {
		Do("Received msgIAmFree from the next conveyor (family).");
 		nextFree = true;
 		stateChanged();
 	}
 	
 	//Scheduler
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		if(glassOnSpot != null) {
 			processGlass();
 			return true;
 		}
 		return false;
 	}
 
 	//Actions
 	private void processGlass() {
 		Do("Waiting glass to be fully loaded into machine.");
 		//STEP 1: Wait until GUI finishes loading the glass.
 		try {
 			machineSemaphore.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		//STEP 2: GUI finishes loading; make GUI process the glass and wait until it's done
 		Do("Asking GUI machine to process the glass. I'll wait until it's finished.");
 		if(glassOnSpot.getRecipe(channel) ) {
 			transducer.fireEvent(channel, TEvent.WORKSTATION_DO_ACTION, null);
 			try {
 				machineSemaphore.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		//STEP 3: Code will get stuck here unless next thing is free
 		while(! nextFree) {}
 		//Do("LOOK! I'm here");
 		//STEP 4: Release glass to the next thing
 		Do("Asking GUI machine to release the glass. I'll wait until it's fully released.");
 		next.msgHereIsGlass(glassOnSpot);
 		transducer.fireEvent(channel, TEvent.WORKSTATION_RELEASE_GLASS, null);
 		try {
 			machineSemaphore.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		//STEP 5: Unloading finished. Notify the conveyor that I'm free.
 		Do("Notifying conveyor that I'm free.");
 		glassOnSpot = null;
 		conveyor.msgInlineFree();
 		//Do("LOOK! I'm here");
 	}
 
 	//Getters, Setters and Hacks
 	public String getName() {
 		return name;
 	}
 	
 	public void setTransducer(Transducer t) {
 		transducer = t;
 		t.register(this, channel);
 	}
 	
 	//Do this first.
 	public void setChannel(TChannel c) {
 		channel = c;
 	}
 	
 
 	public void setNextConveyorFamily(ConveyorFamily cf) {
 		next = cf;
 	}
 	
 	public void setConveyor(Conveyor c) {
 		conveyor = c;
 	}
 	
 	//Test
 	public Glass getInlineGlass() {
 		return glassOnSpot;
 	}
 	
 	public void setNextFree(boolean nf) {
 		nextFree = nf;
 	}
 	
 }
