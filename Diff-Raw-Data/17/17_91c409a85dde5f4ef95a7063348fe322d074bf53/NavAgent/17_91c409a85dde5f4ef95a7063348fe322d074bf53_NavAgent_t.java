 package jadex.agent;
 
 import core.Logger;
 import jadex.bridge.*;
 import jadex.commons.ChangeEvent;
 import jadex.commons.IChangeListener;
 import jadex.micro.MicroAgent;
 import jadex.micro.MicroAgentMetaInfo;
 import jadex.service.HelloService;
 import jadex.service.ReceiveNewGoalService;
 import jadex.service.SendPositionService;
 import data.Position;
 import device.DeviceNode;
 import robot.NavRobot;
 
 public class NavAgent extends MicroAgent
 {
 
 	/** Services */
 	HelloService hs = null;
 	SendPositionService ps = null;
 	ReceiveNewGoalService gs = null;
 	
 	DeviceNode deviceNode = null;
 	NavRobot robot = null;
 	Position curPos = null;
	Position lastPos = null;
 	
 	
 	@Override
 	public void agentCreated()
 	{
 		hs = new HelloService(getExternalAccess());
 		ps = new SendPositionService(getExternalAccess());
 		gs = new ReceiveNewGoalService(getExternalAccess());
 
 		addDirectService(hs);
 		addDirectService(ps);
 		addDirectService(gs);
 
 		Integer port = (Integer)getArgument("port");
 		// Get the device node
 		deviceNode = new DeviceNode(new Object[] {"localhost",port, "localhost",port+1});
 		deviceNode.runThreaded();
 
 		robot = new NavRobot(deviceNode);
 		robot.setRobotId((String)getArgument("robot name"));
 //		robot.runThreaded();
 		robot.setPosition(new Position((Double)getArgument("X"), (Double)getArgument("Y"), (Double)getArgument("Yaw")));
 		
 		hs.send(getComponentIdentifier().toString(), "Hello");
 		Logger.logActivity(false, "Hello", getComponentIdentifier().toString(), -1, null);
 	}
 
 	@Override
 	public void executeBody()
 	{
 		if (robot == null || deviceNode == null) {
 			killAgent();
 		}
 
 		scheduleStep(new IComponentStep()
 		{
 			public Object execute(IInternalAccess ia)
 			{
 				getReceiveNewGoalService().addChangeListener(new IChangeListener()
 				{
 					public void changeOccurred(ChangeEvent event)
 					{
 						Object[] content = (Object[])event.getValue();
 						StringBuffer buf = new StringBuffer();
 						buf.append("[").append(content[0].toString()).append("]: ").append(content[1].toString());
 						
 						Logger.logActivity(false, "Receiving "+buf.toString(), getComponentIdentifier().toString(), -1, null);
 					}
 				});
 				return null;
 			}
 		});
 		final IComponentStep step = new IComponentStep()
 		{			
 			public Object execute(IInternalAccess args)
 			{
 				curPos = robot.getPosition();
				if(curPos.equals(lastPos) == false) {
					ps.send(getComponentIdentifier().toString(), robot.getRobotId(), curPos);
//					Logger.logActivity(false, "Send position", getComponentIdentifier().toString(), -1, null);
					lastPos = curPos;
				}
 
 				waitFor(1000, this);
 				return null;
 			}
 		};
 		waitForTick(step);
 	}
 	@Override
 	public void agentKilled() {
 		
 		robot.shutdown();
 		deviceNode.shutdown();
 		
 		hs.send(getComponentIdentifier().toString(), "Bye");
 
 	}
 	
 	public HelloService getHelloService() { return hs; }
 	public SendPositionService getSendPositionService() { return ps; }
 	public ReceiveNewGoalService getReceiveNewGoalService() { return gs; }
 
 	public static MicroAgentMetaInfo getMetaInfo()
 	{
 		IArgument[] args = {
 //				new Argument("requires player", "dummy", "Boolean", new Boolean(false)),
 //				new Argument("player path", "dummy", "String", ""),
 				new Argument("port", "dummy", "Integer", new Integer(6665)),
 				new Argument("robot name", "dummy", "String", "r0"),
 				new Argument("X", "dummy", "Double", new Double(-6)),
 				new Argument("Y", "dummy", "Double", new Double(-5)),
 				new Argument("Yaw", "dummy", "Double", new Double(Math.toRadians(90)))
 //				new Argument("player config", "dummy", "String", "/Users/sebastian/robotcolla/SimpleAgent/player/planner2.cfg")
 		};
 		
 		return new MicroAgentMetaInfo("This agent starts up a navigation agent.", null, args, null);
 	}
 }
