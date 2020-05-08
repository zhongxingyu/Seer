 package physical.navigation;
 
 import lejos.robotics.Pose;
 import physical.navigation.commands.BlockingCallback;
 import physical.navigation.commands.CommandPriority;
 import physical.navigation.commands.NavigatorCommand;
 import physical.navigation.commands.nav.CmdPose;
 
 public class NavControl
 {
 	private Thread controlThread;
 	private Thread readerThread;
 	
 	private BetterNavigator nav;
 	private CommandQueue<NavigatorCommand> commands = new CommandQueue<NavigatorCommand>();
 	private CommandQueue<NavigatorCommand> reads = new CommandQueue<NavigatorCommand>();
 	private NavigatorCommand currentCmd = null;
 	private NavigatorCommand currentRead = null;
 	private boolean active = true;
 	private boolean readPause = false;
 	
 	public NavControl(BetterNavigator nav)
 	{
 		this.nav = nav;
 		controlThread = new Thread(new Control());
 		controlThread.start();
 		
 		readerThread = new Thread(new Reader());
 		readerThread.start();
 	}
 	
 	public void BExecute(NavigatorCommand cmd)
 	{
 		BlockingCallback bc = new BlockingCallback();
 		cmd.setCaller(bc);
 		
 		Execute(cmd);
 		
 		while (!bc.isExecuted())
 		{
 			Thread.yield();
 		}
 	}
 	
 	public void Execute(NavigatorCommand cmd)
 	{
 		if (cmd.getPriority() == CommandPriority.READ)
 			reads.enqueue(cmd);
 		else
 			commands.enqueue(cmd);
 	}
 	
 	public synchronized void stop()
 	{
 		currentCmd.halt();
 		reads.clear();
 		commands.clear();
 	}
 	
 	public void shutdown()
 	{
 		this.active = false;
 	}
 	
 	public Pose getPose()
 	{
 		CmdPose cPose = new CmdPose();
 		BExecute(cPose);
 		
		return nav.getPose();
 	}
 	
 	private class Control implements Runnable
 	{
 		@Override
 		public void run()
 		{
 			while (active)
 			{
 				Thread.yield();
 				
 				currentCmd = commands.nextCommand();
 				if (currentCmd == null) continue;
 				
 				if (!currentCmd.isInterruptibile())
 				{
 					readPause = true;
 					while (currentRead != null) Thread.yield();
 				}
 
 				System.out.println("PROCESSING COMMAND " + currentCmd);
 				currentCmd.setNavigator(nav);
 				
 				try
 				{
 					currentCmd.execute();
 				}
 				catch (InterruptedException ex)
 				{
 					currentCmd.halt();
 				}
 				finally
 				{
 					currentCmd.finish();
 					readPause = false;
 				}
 			}
 		}
 	}
 	
 	private class Reader implements Runnable
 	{
 		@Override
 		public void run()
 		{
 			while (active)
 			{
 				Thread.yield();
 				while (readPause) Thread.yield();
 				
 				currentRead = reads.nextCommand();
 				if (currentRead == null) continue;
 
 				System.out.println("PROCESSING READ " + currentRead);
 				currentRead.setNavigator(nav);
 				
 				try
 				{
 					currentRead.execute();
 				}
 				catch (InterruptedException ex)
 				{
 					currentRead.halt();
 				}
 				finally
 				{
 					currentRead.finish();
 					currentRead = null;
 				}
 			}
 		}
 	}
 }
