 package com.mrz.dyndns.server.warpsuite.commands.admin;
 
 import static com.mrz.dyndns.server.warpsuite.util.Coloring.*;
 
 import java.util.List;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 
 import com.mrz.dyndns.server.warpsuite.WarpSuite;
 import com.mrz.dyndns.server.warpsuite.commands.WarpSuiteCommand;
 import com.mrz.dyndns.server.warpsuite.permissions.Permissions;
 import com.mrz.dyndns.server.warpsuite.players.WarpSuitePlayer;
 import com.mrz.dyndns.server.warpsuite.util.SimpleLocation;
 import com.mrz.dyndns.server.warpsuite.util.Util;
 
 public class WarpPlayerToTheirWarp extends WarpSuiteCommand
 {
 
 	public WarpPlayerToTheirWarp(WarpSuite plugin)
 	{
 		super(plugin);
 	}
 
 	@Override
 	public boolean warpPlayerExecute(WarpSuitePlayer player, List<String> args, List<String> variables)
 	{
 		if(Permissions.ADMIN_SENDTO.check(player) == false)
 		{
 			return Util.invalidPermissions(player);
 		}
 		
 		return(execute(player.getPlayer(), args, variables));
 	}
 	
 	@Override
 	public boolean consoleExecute(ConsoleCommandSender sender, List<String> args, List<String> variables)
 	{
 		return(execute(sender, args, variables));
 	}
 	
 	private boolean execute(CommandSender sender, List<String> args, List<String> variables)
 	{
 		String playerName = variables.get(0);
 		WarpSuitePlayer target = plugin.getPlayerManager().getWarpPlayer(playerName);
 		if(target == null)
 		{
 			sender.sendMessage(NEGATIVE_PRIMARY + "Player \'" + NEGATIVE_SECONDARY + playerName + NEGATIVE_PRIMARY + "\' is not online!");
 			return true;
 		}
 		else
 		{
 			if(args.size() == 0)
 			{
 				return false;
 			}
 			else
 			{
 				String warpName = args.get(0);
 				if(target.getWarpManager().warpIsSet(warpName) == false)
 				{
 					sender.sendMessage(NEGATIVE_PRIMARY + "Player \'" + NEGATIVE_SECONDARY + playerName + NEGATIVE_PRIMARY + "\' does not have a warp called \'" +
 							NEGATIVE_SECONDARY + warpName + NEGATIVE_PRIMARY + "\' set!");
 					return true;
 				}
 				else
 				{
 					SimpleLocation sLoc = target.getWarpManager().loadWarp(warpName);
 					target.warpTo(sLoc, true);
 					sender.sendMessage(POSITIVE_PRIMARY + "\'" + POSITIVE_SECONDARY + playerName + POSITIVE_PRIMARY + "\' has been warped to warp \'" 
 							+ POSITIVE_SECONDARY + sLoc.getListingName() + POSITIVE_PRIMARY + "\'");
 					return true;
 				}
 			}
 		}
 	}
 
 	@Override
 	public String getUsage()
 	{
		return "warp sendto|to their|his|her " + USAGE_ARGUMENT + "[warpName]";
 	}
 
 }
