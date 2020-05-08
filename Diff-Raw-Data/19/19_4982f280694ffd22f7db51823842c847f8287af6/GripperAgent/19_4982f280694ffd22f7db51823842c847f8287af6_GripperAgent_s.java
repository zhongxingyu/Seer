 package jadex;
 
 import jadex.bridge.*;
 import data.Position;
 import robot.PioneerRG;
 
 public class GripperAgent extends PlayerAgent
 {
 	protected final static String[] playerCmd={"/usr/local/bin/player","/Users/sebastian/robotcolla/SimpleAgent/player/planner2.cfg"};
 
 	PioneerRG pion = null;
 	Position curPos = null;
	
 	@Override
 	protected void agentStarted () {
 		try {
 			pion = new PioneerRG("localhost", port, 1);
 			pion.runThreaded();
 			pion.setPosition(new Position(-28, 3, 90));
 		} catch (Exception e) {
 //			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	protected void agentBody () {
 		if (pion == null) {
 			killAgent();
 		}
 
 		final IComponentStep step = new IComponentStep()
 		{			
 			public Object execute(IInternalAccess args)
 			{
 				curPos = pion.getPosition();
 
 				waitFor(1000, this);
 				return null;
 			}
 		};
 		waitForTick(step);
 	}
 }
