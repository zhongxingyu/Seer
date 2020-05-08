 package engine.ryanCF.agent;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.agent.Agent;
 import engine.agent.shared.ConveyorFamilyOnlineMachine;
 import engine.agent.shared.Glass;
 import engine.ryanCF.interfaces.*;
 
 public class ConveyorAgent extends Agent implements Conveyor {
 
 	TruckAgent truck;
 	FrontSensorAgent front;
 	EndSensorAgent end;
 	
 	List<MyGlass> glassOnConveyor = Collections
 			.synchronizedList(new ArrayList<MyGlass>());
 
 	Transducer t;
 
 	int index;
 
 	boolean truckAvailable = false;
 	
 	enum GlassState {
 		SENSOR1, CONVEYOR, SENSOR2
 	};
 
 	class MyGlass {
 		Glass g;
 		GlassState state;
 	}
 
 	public ConveyorAgent(int index, String name, Transducer t) {
 		this.index = index;
 		this.name = name;
 		this.t = t;
 		
 		front = new FrontSensorAgent(index*2, "Front Sensor " + index*2, t);
 		end = new EndSensorAgent(index*2+1, "End Sensor " + index*2+1, t);
 		end.setConveyor(this);
 	}
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// TODO Auto-generated method stub
 		synchronized(glassOnConveyor) {
 			if(!glassOnConveyor.isEmpty()) {
 				for(MyGlass g : glassOnConveyor) {
 					//if there exists glass on conveyor such that it's on the first sensor, then start conveyor
					if(g.state == GlassState.SENSOR1){
 						startConveyor(g);
 						return true;
 					}
 					else if(g.state == GlassState.SENSOR2){
 						if(truckAvailable) {
 							sendGlassToTruck(g);
 							return true;
 						}
 						else {
 							System.out.println(name + " : Truck not available");
 							Integer[] arg1 = new Integer[1];
 							arg1[0] = index;
 							t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, arg1);
 						}
						return true;
 					}
 				}
 			}
 		}
 		if(truckAvailable) {
 			truckAvailable = false;
 			turnOnConveyor();
 			return true;
 		}
 		return false;
 	}
 
 	private void turnOnConveyor() {
 		// TODO Auto-generated method stub
 		Integer[] arg1 = new Integer[1];
 		arg1[0] = index;
 		t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, arg1);
 	}
 
 	private void startConveyor(MyGlass g) {
 		// TODO Auto-generated method stub
 		g.state = GlassState.CONVEYOR;
 		Integer[] argument = new Integer[1];
 		argument[0] = index;
 		t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, argument);
 	}
 
 	private void sendGlassToTruck(MyGlass g) {
 		truck.msgHereIsGlass(g.g);
 		front.msgSpaceAvailable();
 	}
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		// TODO Auto-generated method stub
 		if(channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_PRESSED) {
			if(args[0].equals(index)) {
 				print("second hit");
				glassOnConveyor.get(0).state = GlassState.SENSOR2;
 				
 			}
 		}
 	}
 
 	@Override
 	public void msgHereIsGlass(Glass g) {
 		// TODO Auto-generated method stub
 		print("Received msgHereIsGlass");
 		MyGlass myGlass = new MyGlass();
 		myGlass.g = g;
 		myGlass.state = GlassState.SENSOR1;
 		synchronized (glassOnConveyor) {
 			glassOnConveyor.add(myGlass);
 		}
 		stateChanged();
 	}
 
 	@Override
 	public void msgSpaceAvailable() {
 		// TODO Auto-generated method stub
 		print("Received msgSpaceAvailable");
 		truckAvailable = true;
 		stateChanged();
 	}
 
 	public void setTruck(TruckAgent truck) {
 		// TODO Auto-generated method stub
 		this.truck = truck;
 	}
 
 	public FrontSensorAgent getFrontSensor() {
 		return this.front;
 	}
 	/*public void setPreviousConveyor(ConveyorFamilyOnlineMachine prevCF) {
 		// TODO Auto-generated method stub
 		sensor1.setPreviousConveyor(prevCF);
 	}*/
 
 	@Override
 	public void msgEndSensorPressed() {
 		// TODO Auto-generated method stub
 		print("Must stop");
 		stateChanged();
 	}
 
 }
