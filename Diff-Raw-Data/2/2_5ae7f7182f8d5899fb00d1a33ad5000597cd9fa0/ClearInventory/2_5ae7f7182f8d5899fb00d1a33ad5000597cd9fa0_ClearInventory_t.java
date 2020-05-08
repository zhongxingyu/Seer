 package no.runsafe.runsafeinventories.commands;
 
 import no.runsafe.framework.command.ExecutableCommand;
 import no.runsafe.framework.server.ICommandExecutor;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 
 import java.util.HashMap;
 
 public class ClearInventory extends ExecutableCommand
 {
 	public ClearInventory()
 	{
 		super("clearinventory", "Clears a players inventory", "runsafe.inventories.clear");
 	}
 
 	@Override
 	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters)
 	{
 		return null;
 	}
 
 	@Override
 	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters, String[] arguments)
 	{
 		if (arguments.length > 0)
 		{
 			RunsafePlayer player = RunsafeServer.Instance.getPlayer(arguments[0]);
 			if (player != null)
 			{
 				if (player instanceof RunsafeAmbiguousPlayer)
 					return player.toString();
 
 				if (player.isOnline())
 				{
 					player.getInventory().clear();
 					player.updateInventory();
					return "&2Inventory for " + player.getPrettyName() + " &2cleared.";
 				}
 				return "&cThat player is offline.";
 			}
 			return "&cThat player does not exist.";
 		}
 		else
 		{
 			if (executor instanceof RunsafePlayer)
 			{
 				RunsafePlayer player = (RunsafePlayer) executor;
 				player.getInventory().clear();
 				player.updateInventory();
 				return "&2Your inventory has been cleared.";
 			}
 			return "&cPlease specify the player you wish to clear.";
 		}
 	}
 }
