 package no.runsafe.UniqueInventories;
 
 import no.runsafe.framework.command.RunsafeCommand;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.player.RunsafePlayer;
 
 import java.util.logging.Level;
 
 public class DebugCommand extends RunsafeCommand
 {
	public DebugCommand()
 	{
 		super("debug", null);
 	}
 
 	@Override
 	public String requiredPermission()
 	{
 		return "uniqueinventories.debug";
 	}
 
 	@Override
 	public boolean Execute(RunsafePlayer player, String[] args)
 	{
 		return Execute(args);
 	}
 
 	@Override
 	public boolean Execute(String[] args)
 	{
 		if (args != null && args.length > 0)
 		{
 			setDebug(args[0]);
 			return true;
 		}
 		return false;
 	}
 
 	private void setDebug(String level)
 	{
 		try
 		{
 			output.setDebugLevel(Level.parse(level));
 		}
 		catch (IllegalArgumentException e)
 		{
 			output.setDebugLevel(Level.OFF);
 		}
 	}
 
 	private IOutput output;
 }
