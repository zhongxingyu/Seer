 package com.mrz.dyndns.server.warpsuite.commands.publicWarps;
 
 import static com.mrz.dyndns.server.warpsuite.util.Coloring.*;
 
 import java.util.List;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 
 import com.mrz.dyndns.server.warpsuite.WarpSuite;
 import com.mrz.dyndns.server.warpsuite.commands.WarpSuiteCommand;
 import com.mrz.dyndns.server.warpsuite.managers.PublicWarpManager;
 import com.mrz.dyndns.server.warpsuite.permissions.Permissions;
 import com.mrz.dyndns.server.warpsuite.players.WarpSuitePlayer;
 
 public class PublicRemoveWarp extends WarpSuiteCommand
 {
 
 	public PublicRemoveWarp(WarpSuite plugin)
 	{
 		super(plugin);
 	}
 
 	@Override
 	public boolean warpPlayerExecute(WarpSuitePlayer player, List<String> args, List<String> variables)
 	{
 		if(Permissions.PUBLIC_REMOVE.check(player, true) == false)
 		{
 			return true;
 		}
 		
 		return execute(player.getPlayer(), args);
 	}
 	
 	@Override
 	public boolean consoleExecute(ConsoleCommandSender sender, List<String> args, List<String> variables)
 	{
 		return execute(sender, args);
 	}
 	
 	private boolean execute(CommandSender sender, List<String> args)
 	{
 		if(args.size() == 0)
 		{
 			return false;
 		}
 		
 		String warpName = args.get(0);
 		PublicWarpManager manager = plugin.getPublicWarpManager();
 		if(manager.warpIsSet(warpName))
 		{
 			manager.removeWarp(warpName);
 			sender.sendMessage(POSITIVE_PRIMARY + "Public warp \'" + POSITIVE_SECONDARY + warpName + POSITIVE_PRIMARY + "\' has been removed");
 			return true;
 		}
 		else
 		{
 			sender.sendMessage(NEGATIVE_PRIMARY + "There is no public warp called \'" + NEGATIVE_SECONDARY + warpName + NEGATIVE_PRIMARY + "\' set!");
 			return true;
 		}
 	}
 
 	@Override
 	public String getUsage()
 	{
		return "warp public" + USAGE_ARGUMENT + "[playerName] " + USAGE + "delete|del|remove|clear " + USAGE_ARGUMENT + "[warpName]";
 	}
 
 }
