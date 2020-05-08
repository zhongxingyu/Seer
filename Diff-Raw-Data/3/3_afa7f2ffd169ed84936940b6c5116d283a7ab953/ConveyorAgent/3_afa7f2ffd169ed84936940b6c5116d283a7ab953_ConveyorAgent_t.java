 package engine.JaniceCF.agent;
 
 import java.util.*;
 import java.util.concurrent.Semaphore;
 
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.JaniceCF.interfaces.*;
 import engine.agent.Agent;
 import engine.agent.shared.*;
 import engine.agent.shared.Interfaces.*;
 import engine.ryanCF.interfaces.Bin;
 
 public class ConveyorAgent extends Agent implements Conveyor {
 
 	enum ConveyorStatus {Nothing, GlassAtEnd}
 		
 	TChannel channel;
 	
 	ConveyorStatus status;
 	
 	int conveyorIndex;
 
 	Machine machine;
 	Bin bin;
 	ConveyorFamily previousCF;
 	Machine previousMachine;
 
 	Boolean nextFree;
 	Boolean loading = false;
 	boolean started =false;
 
 	List<Glass> glassList = Collections.synchronizedList(new ArrayList<Glass>());
 
 	public ConveyorAgent(String name, Transducer transducer, int index, TChannel channel) {
 		super(name, transducer);
 
 		conveyorIndex = index;
 
 		machine = null;
 		bin = null;
 		previousCF = null;
 		previousMachine = null;
 
 		nextFree = true;
 		
 		this.channel = channel;
 
 		transducer.register(this, TChannel.SENSOR);
 		transducer.register(this, TChannel.CONVEYOR);
 		transducer.register(this, channel);
 	}
 
 	//Messages
 	@Override
 	public void msgSpaceAvailable() {
 		if (nextFree == true) {
 			System.err.println(name + ": nextFree should not be true.");
 		}
 		nextFree = true;
 		stateChanged();
 	}
 
 	@Override
 	public void msgHereIsGlass(Glass g) {
 		synchronized (glassList) {
 			glassList.add(g);
 		} 
 		stateChanged();
 	}
 
 
 	//Scheduler
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// TODO Auto-generated method stub
 		if(loading==false&&nextFree==true&&status==ConveyorStatus.Nothing&&!started)
 			startConveyor();
 			
 		if (status == ConveyorStatus.GlassAtEnd)  {
 			if (nextFree == true) {
 				passToMachine();
 				return true;
 			}
 		}
 		
 		
 		return false;
 	}
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 
 		//Sensors
 		if (channel == TChannel.SENSOR) {
 			if (event == TEvent.SENSOR_GUI_PRESSED) {
 				if (args[0].equals(conveyorIndex*2)) {	//Front Sensor
 					if (status != ConveyorStatus.GlassAtEnd) {
 						Integer[] newArgs = new Integer[1];
 						newArgs[0] = (Integer) conveyorIndex;
 						started=true;
 						transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
 						stateChanged();
 						return;
 					}
 				} else if (args[0].equals((conveyorIndex * 2) + 1)) {	//End Sensor
 					status = ConveyorStatus.GlassAtEnd;					
 					Integer[] newArgs = new Integer[1];
 					newArgs[0] = (Integer) conveyorIndex;
 					if(!loading)
 					{
 						transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, newArgs);
 						started=false;
 					}
 					stateChanged();
 					return;
 				}
 			} else if (event == TEvent.SENSOR_GUI_RELEASED) {
 				if (args[0].equals(conveyorIndex*2)) {		//Front sensor
 					if (previousCF != null) {
 						previousCF.msgSpaceAvailable();
 						stateChanged();
 						return;
 					} else if (bin != null) {
 						bin.msgSpaceAvailable();
 						stateChanged();
 						return;
 					} else if (previousMachine != null) {
 						previousMachine.msgSpaceAvailable();
 						stateChanged();
 						return;	
 					}
 				} else if (args[0].equals((conveyorIndex*2) + 1)) {		//end sensor
 					status = ConveyorStatus.Nothing;
 					stateChanged();
 					return;
 				}
 			}
 		}
 
 
 		//Workstation
 		if (channel == this.channel) {
 			
 			if (event == TEvent.WORKSTATION_LOAD_FINISHED) {
 				Integer[] newArgs = new Integer[1];
 				newArgs[0] = (Integer) conveyorIndex;
 				loading = false;
				if(status==ConveyorStatus.GlassAtEnd)
					transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, newArgs );
 				started=false;
 				return;
 			}
 		}
 		
 		
 	}
 
 
 	//Actions
 	private void passToMachine() {
 		nextFree = false;
 		Glass g = glassList.get(0);
 		synchronized (glassList) {
 			glassList.remove(0);
 		}
 		machine.msgHereIsGlass(g);
 
 		Integer[] newArgs = new Integer[1];
 		newArgs[0] = (Integer)conveyorIndex;
 		started=true;
 		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
 		loading =true;
 		//transducer.fireEvent(this.channel, TEvent.WORKSTATION_DO_LOAD_GLASS, null);
 		//TODO pass to machine.  
 	}
 
 	private void startConveyor()
 	{
 		Integer[] newArgs = new Integer[1];
 		newArgs[0] = (Integer) conveyorIndex;
 		started=true;
 		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs );
 		stateChanged();
 	}
 	//Helper Methods. 
 
 	@Override
 	public void setPreviousCF(ConveyorFamily cf) {
 		this.previousCF = cf;
 	}
 
 	@Override
 	public void setMachine(Machine m) {
 		this.machine = m;
 	}
 
 	@Override
 	public void setBin(Bin b) {
 		this.bin = b;
 	}
 	
 	@Override
 	public void setPreviousMachine(Machine m) {
 		this.previousMachine = m;
 	}
 
 }
