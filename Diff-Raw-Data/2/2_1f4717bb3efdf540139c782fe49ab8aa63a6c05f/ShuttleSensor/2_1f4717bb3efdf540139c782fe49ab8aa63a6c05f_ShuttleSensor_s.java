 package engine.brandonCF.agents;
 
 import java.util.concurrent.Semaphore;
 
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.agent.Agent;
 import engine.agent.shared.Glass;
 import engine.agent.shared.MachineAgent;
 import engine.agent.shared.Interfaces.ConveyorFamily;
 import engine.brandonCF.interfaces.ConveyorFamilyInterface;
 import engine.brandonCF.interfaces.ConveyorInterface;
 import engine.brandonCF.interfaces.PopUpInterface;
 import engine.brandonCF.interfaces.SensorInterface;
 
 public class ShuttleSensor extends Agent implements SensorInterface
 {
 	//Data:
 	Glass glass;
 	private ConveyorFamily next;
 	private ConveyorInterface conveyor;
 	private enum Status {open, filled, finished, waiting};
 	private Status status;
 	private Semaphore canSend;
 	private Transducer trans;
 	private Integer[] number;
 	private MachineAgent nextMac;
 	
 	//Methods:
 	public ShuttleSensor(String name, Transducer t, int num)
 	{
 		super(name, t);
 		t.register(this, TChannel.SENSOR);
 		trans =t;
 		status = Status.open;
 		canSend = new Semaphore(0, true);
 		number = new Integer[1];
 		number[0] = num;
 	}
 	
 	//Msgs:
 	@Override
 	public void msgHereisGlass(Glass g)//This is where glass is sent in
 	{
 		this.glass = g;
 		status = Status.filled;
 		print("Received glass");
 		trans.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_STOP, number);
 		stateChanged();
 	}
 	
 	@Override
 	public void msgSpaceOpen()//Here the agent is "told" it can send glass when ready
 	{
 		canSend.release();
 		print("Notified that space is open");
 		stateChanged();
 	}
 	
 	//Scheduler
 	@Override
 	public boolean pickAndExecuteAnAction()
 	{
 		if(status == Status.finished){
 			sendFinishedGlass();
 			return true;
 		}
 		if(status == Status.filled)
 		{
 			sensorWait();
 			return true;
 		}
 		if(status == Status.open)
 		{
 			askForGlass();
 			return true;
 		}
 		return false;
 	}
 	
 	//Actions
 	private void sendFinishedGlass()
 	{
 		status = Status.open;
 		try{
 			canSend.acquire();
 			next.msgHereIsGlass(glass);
 			glass = null;
 			print("Sent glass");
 			stateChanged();
 		} catch(Exception e){}
 	}
 	
 	private void sensorWait()
 	{
 		status = Status.waiting;
 		stateChanged();
 	}
 	
 	private void askForGlass()
 	{
 		conveyor.msgSpaceOpen();
 		status = Status.waiting;
 		print("Asked for Glass");
 		stateChanged();
 	}
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		if(channel == TChannel.SENSOR & event == TEvent.SENSOR_GUI_RELEASED)
 		{
			if((int) args[0] == number[0] *2+1)
 			{
 				status = Status.finished;
 				print("Finished:)");
 				stateChanged();
 			}
 		}
 	}
 	
 	@Override
 	public void setPreviousFamily(ConveyorFamilyInterface previous)//for the shuttle sensor, this is actually the next conveyor family
 	{
 		//this.next = previous;
 	}
 	
 	@Override
 	public void setConveyor(ConveyorInterface conAgent)
 	{
 		this.conveyor = conAgent;
 	}
 
 	@Override
 	public void setPopUp(PopUpInterface popUp) {
 		//Does nothing here
 		
 	}
 	
 	public void setNumber(int num)
 	{
 		number[0] = num;
 	}
 
 	public void setNextConveyor(ConveyorFamily con) {
 		
 		next = con;
 	}
 }
