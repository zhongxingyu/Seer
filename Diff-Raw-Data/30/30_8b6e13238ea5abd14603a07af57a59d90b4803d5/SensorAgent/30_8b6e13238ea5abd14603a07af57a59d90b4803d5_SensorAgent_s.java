 package engine.JaniceCF.agent;
 
 import transducer.*;
 import engine.JaniceCF.interfaces.*;
 import engine.agent.Agent;
 import engine.agent.shared.Interfaces.ConveyorFamily;
 import engine.agent.shared.Interfaces.Machine;
 import engine.ryanCF.interfaces.Bin;
 
 public class SensorAgent extends Agent implements Sensor {
 	
 	ConveyorFamily previousCF;
 	Bin bin;
 	Machine machine;
 	
 	Conveyor conveyor;
 	
 	int sensorIndex;
 	
 	public SensorAgent(String name, Transducer transducer, Conveyor c, int index) {
 		super(name, transducer);
 		
 		transducer.register(this, TChannel.SENSOR);
 		
 		conveyor = c;
 		
 		sensorIndex = index;
 		
 		previousCF = null;
 		bin = null;
 		machine = null;
 	}
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// TODO Auto-generated method stub
 		//Technically empty? cause all msgs sent through eventFired?
 		return false;
 	}
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		if (channel == TChannel.SENSOR) {
 			if (event == TEvent.SENSOR_GUI_PRESSED) {
 				//transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, null);
 
 				Integer[] newArgs = new Integer[1];
 				if ((sensorIndex % 2) == 0)
 				{
 					if (args[0].equals(sensorIndex)) {
 						print("Pressed. Starting Conveyor. ");
 						newArgs[0] = (Integer)args[0] / 2;
 //						newArgs[0] = (Integer) sensorIndex;
						transducer.fireEvent(TChannel.CONVEYOR,
								TEvent.CONVEYOR_DO_START, newArgs);
 					}
 				} else {
 					if (args[0].equals(sensorIndex)) {
 						print("Pressed. Stoping Conveyor. ");
 						newArgs[0] = (Integer)args[0] / 2;
 //						newArgs[0] = (Integer) sensorIndex;
 						transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, newArgs);
 						conveyor.msgGlassAtEnd();
 					}
 				}
 			} else if (event == TEvent.SENSOR_GUI_RELEASED) {
 				
 				Integer[] newArgs = new Integer[1];
 				if ((sensorIndex % 2) == 0)
 				{
 					if (args[0].equals(sensorIndex)) {
 						
 						if (previousCF != null) {
 							print("Released. Sending msgSpaceAvailable to previousCF. ");
 							previousCF.msgSpaceAvailable();
 						}
 					
 					
 						if (bin != null) {
 							print("Released. Sending msgSpaceAvailable to bin. ");
 							bin.msgSpaceAvailable();
 						}
 						if (machine != null) {
 							print("Released. Sending msgSpaceAvailable to machine. ");
 							machine.msgSpaceAvailable();
 						}
 					}
 				} else {
 //					if (args[0].equals(sensorIndex)) {
 //						print("Released. Starting Conveyor. ");
 //						newArgs[0] = (Integer)args[0] / 2;
 ////						newArgs[0] = (Integer) sensorIndex;
 //						//					conveyor.msgGlassAtEnd();
 //						transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
 //					}
 				}
 			}
 		} 
 	}
 	
 	
 	
 	public void setPreviousCF(ConveyorFamily cf) {
 		previousCF = cf;
 	}
 	
 	public void setMachine(Machine m) {
 		machine = m;
 	}
 	
 	public void setBin(Bin b) {
 		bin = b;
 	}
 
 }
