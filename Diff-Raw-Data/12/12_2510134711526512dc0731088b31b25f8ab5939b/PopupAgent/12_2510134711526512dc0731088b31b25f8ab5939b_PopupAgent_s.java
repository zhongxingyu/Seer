 package engine.heidiCF.agent;
 
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 import engine.agent.shared.Glass;
 
 import engine.agent.Agent;
 import engine.heidiCF.interfaces.*;
 import engine.agent.shared.Interfaces.*;
 
 
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 public class PopupAgent extends Agent implements Popup{
 
 	public boolean nextFamilyAvailable;
 	public List<MyGlass> glasses;
 	Semaphore animationSem = new Semaphore(0,true);
 	class MyGlass{
 		Glass glass; 
 		GlassStatus  status;
 		Integer robotIndex;
 		public MyGlass(Glass g)
 		{
 			glass = g;
 		}
 		public int getRobotIndex()
 		{
 			return robotIndex;
 		}
 	};
 	
 	enum GlassStatus {Pending, Delivering, Handling, Ready,WaitingForPass};
 	public enum PopupStatus {Falling, Up, Down, Rising};
 	enum RobotStatus {Working,Empty};
 	
 	PopupStatus status = PopupStatus.Down;
 	TChannel myChannel;
 	class MyRobot{
 		//Robot robot;
 		RobotStatus status;
 		public MyRobot()
 		{
 			//this.robot  = r;
 			status = RobotStatus.Empty;
 		}
 
 	}
 	List<MyRobot> robots = Collections.synchronizedList(new ArrayList<MyRobot>());
 	ConveyorFamily nextCF;
 	Conveyor conveyor;
 	Integer myIndex;
 	public PopupAgent(Integer index, Transducer t,TChannel channelType)//ArrayList<Robot> inputRobots
 	{		
 		super("popupAgent");
 		myIndex = index;
 		glasses = Collections.synchronizedList(new ArrayList<MyGlass>());
 		nextFamilyAvailable =true;
 		transducer = t;
 		myChannel = channelType;
 //		for(Robot r: inputRobots)
 //		{
 //			robots.add(new MyRobot(r));
 //		}
 		robots.add(new MyRobot());
 		robots.add(new MyRobot());
 		t.register(this, TChannel.CONVEYOR);
 		t.register(this, TChannel.SENSOR);
 		t.register(this, TChannel.POPUP);
 		//need to listen to another work station
 		t.register(this, myChannel);
 	}
 
 	public void msgHereIsGlass(Glass g) //8 from conveyor
 	{
 		MyGlass newGlass = new MyGlass(g);
 		newGlass.status = GlassStatus.Pending;
 		glasses.add(newGlass);
 		stateChanged();
 	}
 
 	public void msgGlassReady(Integer thisIndex)// 10a from machine, moveup to take the glass if 10b is received labter
 	{
 		for(MyGlass myGlass: glasses)
 		{
 			if(myGlass.status==GlassStatus.Delivering&&myGlass.robotIndex.equals(thisIndex))
 			{	
 
 				myGlass.status = GlassStatus.Ready;
 				break;
 			}
 		}	
 		stateChanged();
 	}
 
 	public void msgNewSpaceAvailable()//10b from next family
 	{	
 		nextFamilyAvailable =true;
 		stateChanged();
 	}
 
 	public void msgComeDownAndLetGlassPass(Glass g)
 	{
 		MyGlass newGlass = new MyGlass(g);
 		newGlass.status = GlassStatus.WaitingForPass;
 		glasses.add(newGlass);
 		stateChanged();
 	}
 	public void setConveyor(Conveyor conveyor)
 	{
 		this.conveyor = conveyor;
 	}
 
 	public boolean pickAndExecuteAnAction() {
 	
 		MyGlass tempG = null;
 		MyGlass tempG1 =null;
 
 		for(int i=0;i<robots.size();i++)
 		{
 			if(robots.get(i).status == RobotStatus.Empty)
 			{
 				synchronized(glasses)
 				{	for (MyGlass g:glasses)//there exists a MyGlass g in glasses such that g.status = GlassStatus.Pending
 					{	
 						if(g.status ==GlassStatus.Pending)
 						{	
 							tempG =g;
 							break;
 						}
 					}
 				}
 				if(tempG!=null)
 				{
 					tempG.robotIndex=i;
 					robots.get(i).status = RobotStatus.Working;
 					deliverGlass(tempG);
 					return true;
 				}
 			}
 		}
 		if (nextFamilyAvailable)
 		{
 	
 			synchronized(glasses)
 			{	
 				for(MyGlass g :glasses)
 				{
 					if(g.status == GlassStatus.WaitingForPass)
 					{
 						tempG = g;
 						break;
 					}
 					if(g.status == GlassStatus.Ready)
 					{
 						tempG1 = g;
 						break;
 					}
 				}
 
 			}
 			if(tempG!=null)
 			{
 				passGlass(tempG);
 				return true;
 			}
 			else if(tempG1 !=null)
 			{
 				getGlassFromMachine(tempG1);
 				return true;
 			}
 		}
 		
 
 		return false;
 	}
 	public void deliverGlass(MyGlass g)
 	{
 		System.out.println("deliver the glass is called");
 		g.status=GlassStatus.Delivering;
 		Integer[] popupArgs = new Integer[1];
 		popupArgs[0] = myIndex;
 		Integer[] conveyorArgs = new Integer[1];
 		conveyorArgs[0] = conveyor.getMyIndex();
 		Integer[] robotArgs = new Integer[1];
 		robotArgs[0] = g.robotIndex;
 		//move down, take glass, take it to the next available machine
 		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN,popupArgs);
 		try {
 			animationSem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} // released by POPUP_GUI_MOVED_DOWN
 		print("popup"+myIndex+" moved down the pop up");
 		transducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,conveyorArgs);
 		try {
 			animationSem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} // released by POPUP_GUI_LOAD_FINISHED
 		print("popup"+myIndex+"popup finished loading");
 		transducer.fireEvent(TChannel.POPUP,TEvent.POPUP_DO_MOVE_UP,popupArgs);
 		try {
 			animationSem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} // released by POPUP_GUI_MOVED_UP
 		print("popup"+myIndex+"popup moved up");
 		transducer.fireEvent(myChannel, TEvent.WORKSTATION_DO_LOAD_GLASS,robotArgs);
 		try {
 			animationSem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} // released by WORKSTATION_LOAD_FINISHED
 		//TODO
 		print("workStation"+robotArgs[0]+"load glass finished");
 		transducer.fireEvent(myChannel, TEvent.WORKSTATION_DO_ACTION,robotArgs);
 
 		//robots.get(g.robotIndex).robot.msgHereIsGlass(g.glass);
 
 		stateChanged();
 	}
 	
 	public void getGlassFromMachine(MyGlass g)
 	{
 		System.out.println("get glass from machine");
 		Integer[] popupArgs = new Integer[1];
 		popupArgs[0] = myIndex;
 		Integer[] robotArgs = new Integer[1];
 		robotArgs[0] = g.robotIndex;
 		nextFamilyAvailable=false;
 		transducer.fireEvent(TChannel.POPUP,TEvent.POPUP_DO_MOVE_UP,popupArgs);
 		try {
 			animationSem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}  // released by POPUP_GUI_MOVED_UP;
 		print("popup"+myIndex+"popup moved up to get glass form station");
 		transducer.fireEvent(myChannel, TEvent.WORKSTATION_RELEASE_GLASS,robotArgs);
 		try {
 			animationSem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} // released by TEvent.WORKSTATION_RELEASE_FINISHED
 		print("workstation"+robotArgs[0]+"workstation released glass");
 		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN,popupArgs);
 		try {
 			animationSem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}  // released by POPUP_GUI_MOVED_DOWN
 		print("popup"+myIndex+"popup moved down to release glass");
 		transducer.fireEvent(TChannel.POPUP,TEvent.POPUP_RELEASE_GLASS,popupArgs);
 		try {
 			animationSem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} // released by POPUP_GUI_RELEASE_FINISHED
 		robots.get(g.robotIndex).status = RobotStatus.Empty;
 		print("popup"+myIndex+"popup released the glass");
 		glasses.remove(g);
 		nextCF.msgHereIsGlass(g.glass);
 		conveyor.msgPopupAvailable();
 
 		stateChanged();
 		//move up, go to machine, take the glass and move down and release glass
 	}
 
 	public void passGlass(MyGlass g)
 	{
 		Integer[] popupArgs = new Integer[1];
 		popupArgs[0] = myIndex;
 		Integer[] conveyorArgs = new Integer[1];
 		conveyorArgs[0] = conveyor.getMyIndex();
 		nextFamilyAvailable=false;
 		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN,popupArgs);
 		try {
 			animationSem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} // released by POPUP_GUI_MOVED_DOWN
 		transducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,conveyorArgs);
 		try {
 			animationSem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} // released by POPUP_GUI_LOAD_FINISHED
 		transducer.fireEvent(TChannel.POPUP,TEvent.POPUP_RELEASE_GLASS,popupArgs);
 		try {
 			animationSem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} // released by POPUP_GUI_RELEASE_FINISHED
 		nextCF.msgHereIsGlass(g.glass);
 		glasses.remove(g);
 		stateChanged();
 		//move down, take the glass and pass it to the next family when the family is ready
 	}
 	
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		
 		if(channel == TChannel.POPUP)
 		{
 			if(args[0]==myIndex)
 			{
 				if(event == TEvent.POPUP_GUI_MOVED_DOWN)
 				{
 					animationSem.release();
 					print("POPUP_GUI_MOVED_DOWN is released");
 				}
 				else if (event ==  TEvent.POPUP_GUI_LOAD_FINISHED)
 				{
 					animationSem.release();
 					print("POPUP_GUI_LOAD_FINISHED is released");
 
 				}
 				else if (event == TEvent.POPUP_GUI_MOVED_UP)
 				{
 					animationSem.release();
 					print("POPUP_GUI_MOVED_UP is released");
 
 				}
 				else if (event == TEvent.POPUP_GUI_RELEASE_FINISHED)
 				{
 					animationSem.release();
 					print("POPUP_GUI_RELEASE_FINISHED is released");
 				}
 			}
 		}
 		else if (channel == myChannel)
 		{
 			if (event == TEvent.WORKSTATION_LOAD_FINISHED)
 			{
 				animationSem.release();
 				System.out.println();
 				print("WORKSTATION_LOAD_FINISHED is released");
 
 			}
			else if(event == TEvent.WORKSTATION_RELEASE_FINISHED)
			{
				animationSem.release();
				print("WORKSTATION_LOAD_FINISHED is released");

			}
 			else if (event ==TEvent.WORKSTATION_GUI_ACTION_FINISHED){	// we do not have machine agent so we use this eventFired
 				Integer tempIndex= (Integer)args[0];
 				msgGlassReady(tempIndex);
 			}
 		}
 	}
 	//methods for unit testing
 	public int availableMachine()
 	{
 		int count=0;
 		for(MyRobot r:robots)
 		{
 			if(r.status ==RobotStatus.Empty)
 				count++;
 		}
 		return count;
 	}
 	public void makeOneMachineUnavailable() // only for unit test
 	{
 		robots.get(0).status=RobotStatus.Working;
 	}
 	public void makeBothMachineUnavailable() // only for unit test
 	{
 		robots.get(0).status=RobotStatus.Working;
 		robots.get(1).status=RobotStatus.Working;
 	}
 	public void setNextCF(ConveyorFamily nextCF)
 	{
 		this.nextCF= nextCF;
 	}
 
 }
