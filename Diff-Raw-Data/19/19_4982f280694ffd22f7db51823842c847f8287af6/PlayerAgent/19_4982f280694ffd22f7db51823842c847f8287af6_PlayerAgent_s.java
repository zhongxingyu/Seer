 package jadex;
 
 import core.Logger;
 import core.OSCommand;
 import jadex.bridge.*;
 import jadex.micro.*;
 
 public class PlayerAgent extends MicroAgent {
 	
 	protected static String[] playerCmd={"/usr/local/bin/player","/Users/sebastian/robotcolla/SimpleAgent/player/uhh1.cfg"};
 	protected OSCommand startPlayer = null;
 	protected static int port = 6665;
 
 	public void agentCreated()
 	{
 		Logger.logActivity(false, "running", this.toString(), port, Thread.currentThread().getName());
 
 		agentStarted();
 
 		if ((Boolean)getArgument("requires player") == true) {
 			// Get the Gui argument, if any
 			String[] command = {
 					((String)getArgument("player path")),
					new String("-p ").concat( String.valueOf(((Integer)getArgument("player port")).intValue()) ),
 					((String)getArgument("player config"))
 			};
 
 			startPlayer = new OSCommand(command);
 		}
 	}
 
 	protected void agentStarted () {};
 
 	public void executeBody()
 	{	
 		// TODO no blocking
 		agentBody();
 //		if (startPlayer != null) {
 //			startPlayer.waitFor();
 //		}
 	}
 	protected void agentBody () {};
 	
 	public void agentKilled()
 	{
 		agentTerminated();
 		if (startPlayer != null) {
 			startPlayer.terminate();
 		}
 		Logger.logActivity(false, "Termination", this.toString(), port, Thread.currentThread().getName());
 	}
 	protected void agentTerminated () {};
 	
 	public static MicroAgentMetaInfo getMetaInfo()
 	{
 		Argument[] args = {
 			new Argument("requires player", "dummy", "Boolean", new Boolean(false)),
 			new Argument("player path", "dummy", "String", playerCmd[0]),
 			new Argument("player port", "dummy", "Integer", new Integer(port)),	
 			new Argument("player config", "dummy", "String", playerCmd[1])
 		};
 		
 		return new MicroAgentMetaInfo("This agent starts up a Player agent.", null, args, null);
 	}
 }
