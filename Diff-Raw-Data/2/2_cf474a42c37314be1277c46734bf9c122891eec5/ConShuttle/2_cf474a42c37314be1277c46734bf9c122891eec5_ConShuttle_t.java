 package engine.brandonCF.agents;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.agent.Agent;
 import engine.agent.shared.Glass;
 import engine.agent.shared.Interfaces.ConveyorFamily;
 import engine.agent.shared.Interfaces.Machine;
 
 public class ConShuttle extends Agent implements ConveyorFamily
 {
 	public enum Status {normal, waiting, send};
 	
 	private class GlassPacket
 	{
 		public Glass g;
 		public Status status;
 		
 		public GlassPacket(Glass g)
 		{
 			this.g = g;
 			status = Status.normal;
 		}
 	}
 
 	private List<GlassPacket> glass = Collections.synchronizedList(new ArrayList<GlassPacket>());
 	private Integer[] number;
 	ConveyorFamily con;
 	Machine mac;
 	boolean canSend, notified, conMoving, released, overRide;
 	
 	public ConShuttle(String name, Transducer t, int num)
 	{
 		super(name, t);
 		number = new Integer[1];
 		number[0] = num;
 		transducer.register(this, TChannel.CONVEYOR);
 		transducer.register(this, TChannel.SENSOR);
 		canSend = true;
 		notified = false;
 		conMoving = true;
 		released = false;
 		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, number);
 		overRide = false;
 	}
 	
 	@Override
 	public void msgSpaceAvailable() {
 		// TODO Auto-generated method stub
 		print("Next Open");
 		canSend = true;
 		stateChanged();
 	}
 
 	@Override
 	public void msgHereIsGlass(Glass g) {
 		this.glass.add(new GlassPacket(g));
 		print(name.toString() +" received glass");
 		notified = false;
 		stateChanged();
 	}
 	
 	public void msgOverRideStop()
 	{
 		overRide = true;
 		stateChanged();
 	}
 	
 	public void msgOverRideStart()
 	{
 		overRide = false;
 		stateChanged();
 	}
 //scheduler
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// TODO Auto-generated method stub
 		if(!overRide)
 		{
 			if(glass.size()>0)//if there are packets, try to do something
 			{
 				if(glass.get(0).status == Status.send)//if you can send
 				{
 					if(canSend){
 						sendGlass(glass.get(0));
 						//return true;
 					}
 					else//*/
 					{
 						stopConveyor();
 						return false;
 					}
 				}
 	
 				if(notified ==false & conMoving & released)//if i haven't notified & the conveyor is moving is not stopped
 				{
 					msgMac();
 					return true;
 				}
 			}
 			else
 			{
 				msgMac();
 			}
 		}
 		
 		return false;
 	}
 	//actions
 	
 	private void stopConveyor() {
 		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, number);
 		conMoving = false;
 	}
 
 	private void msgMac()
 	{
 		mac.msgSpaceAvailable();
 		print(name.toString() +" Space Open");
 		notified = true;
 		released = false;
 	}
 	
 	private void sendGlass(GlassPacket glassPacket) {
 			canSend = false;
 			con.msgHereIsGlass(glassPacket.g);//send the glass
 			glass.remove(glassPacket);//remove from list
 			//if(conMoving!= true)
 			{
 				transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, number);
 				conMoving = true;
 			}
 			print(name.toString() +" sent glass");
 			print(""+notified);
 			print(""+released);
 			print(""+ glass.size());
 			
 			stateChanged();
 	}
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		// TODO Auto-generated method stub
 		if(channel == TChannel.SENSOR & event == TEvent.SENSOR_GUI_RELEASED)
 		{
 			if(args[0].equals(number[0] *2))//if second sensor
 			{
 				//released = true;
 				//stateChanged();
 			}
 //			if(args[0].equals(number[0] *2+1))//if second sensor
 //			{
 //				glass.get(0).status = Status.send;
 //				stateChanged();
 //			}
 		}
 		
		if(channel == TChannel.SENSOR & event == TEvent.SENSOR_GUI_RELEASED) {
 			if(args[0].equals(number[0] *2+1))//if second sensor
 			{
 				released = true;
 				glass.get(0).status = Status.send;
 				stateChanged();
 			}
 		}
 		
 		/*if(channel == TChannel.CONVEYOR & event == TEvent.CONVEYOR_BREAK)
 		{
 			if(args[0].equals(number[0]))
 			{
 				msgOverRideStop();
 			}
 		}
 		if(channel == TChannel.CONVEYOR & event == TEvent.CONVEYOR_FIX)
 		{
 			if(args[0].equals(number[0]))
 			{
 				msgOverRideStart();
 			}
 		}*/
 	}
 	
 	public void setConveyor(ConveyorFamily c)
 	{
 		con = c;
 	}
 	
 	public void setMachine(Machine m)
 	{
 		mac = m;
 	}
 
 }
