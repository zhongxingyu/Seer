 package com.mrz.dyndns.server.warpsuite.commands;
 
 import static com.mrz.dyndns.server.warpsuite.util.Coloring.*;
 
 import java.util.List;
 
 import com.mrz.dyndns.server.warpsuite.WarpSuite;
 import com.mrz.dyndns.server.warpsuite.WarpSuitePlayer;
 import com.mrz.dyndns.server.warpsuite.permissions.Permissions;
 import com.mrz.dyndns.server.warpsuite.util.Config;
 import com.mrz.dyndns.server.warpsuite.util.SimpleLocation;
 import com.mrz.dyndns.server.warpsuite.util.Util;
 
 public class GoPlayersOwnWarp extends WarpSuiteCommand
 {
 
 	public GoPlayersOwnWarp(WarpSuite plugin)
 	{
 		super(plugin);
 	}
 
 	@Override
 	public boolean warpPlayerExecute(final WarpSuitePlayer player, List<String> args, List<String> variables)
 	{
		if(!Permissions.WARP.check(player) && !Permissions.HELP.check(player))
 		{
 			return Util.invalidPermissions(player);
 		}
 		
 		if(args.size() == 0)
 		{
 			player.sendMessage(NEGATIVE_PRIMARY + "Invalid usage!" + POSITIVE_PRIMARY + " Correct usage: " + USAGE + "/warp " + USAGE_ARGUMENT + " [warpName]");
 			if(Permissions.HELP.check(player))
 			{
 				player.sendMessage(POSITIVE_PRIMARY + "Or, if you want to view all of the warp help, issue " + USAGE + "/warp help");
 			}
 			return true;
 		}
 		
 		String warpName = args.get(0);
 		if(player.getWarpManager().warpIsSet(warpName))
 		{
 			final SimpleLocation sLoc = player.getWarpManager().loadWarp(warpName);
 			boolean canGoToWorld = sLoc.tryLoad(plugin);
 			if(canGoToWorld)
 			{
 				//it is time to teleport!
 				if(Permissions.DELAY_BYPASS.check(player) || !Util.areTherePlayersInRadius(player))
 				{
 					player.teleport(plugin, sLoc);
 					return true;
 				}
 				else
 				{
 					Util.sendYouWillBeWarpedMessage(player);
 					plugin.getPendingWarpManager().addPlayer(player.getName());
 					plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
 						@Override
 						public void run()
 						{
 							if(plugin.getPendingWarpManager().isWaitingToTeleport(player.getName()))
 							{
 								plugin.getPendingWarpManager().removePlayer(player.getName());
 								player.teleport(plugin, sLoc);
 							}
 						}
 					}, Config.timer * 20L);
 					return true;
 				}
 			}
 			else
 			{
 				player.sendMessage(NEGATIVE_PRIMARY + "The world warp \'" + NEGATIVE_SECONDARY + "\' is located in either no longer exists, or isn't loaded");
 				return true;
 			}
 		}
 		else
 		{
 			player.sendMessage(NEGATIVE_PRIMARY + "Warp \'" + NEGATIVE_SECONDARY + warpName + NEGATIVE_PRIMARY + "\' is not set!");
 			return true;
 		}
 	}
 
 	@Override
 	public String getUsage()
 	{
 		//I'll do this myself above (see line (around) 25)
 		return null;
 	}
 
 }
