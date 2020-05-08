 package me.Kruithne.UniqueInventories;
 
 import java.util.List;
 import java.util.logging.Level;
 
import no.runsafe.framework.command.ICommand;
 import no.runsafe.framework.command.RunsafeCommandHandler;
 
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.bukkit.command.ConsoleCommandSender;
 
 public class DebugCommand extends RunsafeCommandHandler 
 {
    public DebugCommand(ICommand command)
    {
        super(command);
    }
 
 	@Override
 	public String getName() 
 	{
 		return "uniqueinv";
 	}
 
 	protected boolean playerExecute(RunsafePlayer player, List<String> args)
 	{
 		if(args.get(0).equalsIgnoreCase("debug"))
 		{
 			setDebug(args.get(1));
 			return true;
 		}
 		return false;
 	}
 	
 	protected boolean consoleExecute(ConsoleCommandSender console, List<String> args)
 	{
 		if(args.get(0).equalsIgnoreCase("debug"))
 		{
 			setDebug(args.get(1));
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
 		catch(IllegalArgumentException e)
 		{
 			output.setDebugLevel(Level.OFF);
 		}
 	}
 	
 	private IOutput output;
 }
