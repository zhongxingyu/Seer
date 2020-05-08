 package com.mrz.dyndns.server.warpsuite.commands.user;
 
 import static com.mrz.dyndns.server.warpsuite.util.Coloring.*;
 
 import java.util.List;
 
 import com.mrz.dyndns.server.warpsuite.WarpSuite;
 import com.mrz.dyndns.server.warpsuite.WarpSuitePlayer;
 import com.mrz.dyndns.server.warpsuite.commands.WarpSuiteCommand;
 import com.mrz.dyndns.server.warpsuite.permissions.Permissions;
 import com.mrz.dyndns.server.warpsuite.util.Config;
 import com.mrz.dyndns.server.warpsuite.util.SimpleLocation;
 import com.mrz.dyndns.server.warpsuite.util.Util;
 
 public class GoPlayersWarp extends WarpSuiteCommand
 {
 
 	public GoPlayersWarp(WarpSuite plugin)
 	{
 		super(plugin);
 	}
 
 	@Override
 	public boolean warpPlayerExecute(final WarpSuitePlayer player, List<String> args, List<String> variables)
 	{
 		if(!Permissions.WARP.check(player) && !Permissions.HELP.check(player) && !Permissions.ADMIN_WARP.check(player))
 		{
 			return Util.invalidPermissions(player);
 		}
 		
 		if(args.size() == 0)
 		{
 			if(Permissions.WARP.check(player))
 			{
				player.sendMessage(NEGATIVE_PRIMARY + "Invalid usage!" + POSITIVE_PRIMARY + " Correct usage: " + USAGE + "/warp " + USAGE_ARGUMENT + " [warpName]");
 			}
 			if(Permissions.ADMIN_WARP.check(player))
 			{
 				player.sendMessage(NEGATIVE_PRIMARY + "Invalid usage!" + POSITIVE_PRIMARY + " Correct usage: " + USAGE + "/warp " + USAGE_ARGUMENT + "[playerName] [warpName]");
 			}
 			if(Permissions.HELP.check(player))
 			{
 				player.sendMessage(POSITIVE_PRIMARY + "If you want to view all of the warp commands, issue " + USAGE + "/warp help");
 			}
 			return true;
 		}
 		
 		if(args.size() == 2)
 		{
 			//this is an admin command
 			if(Permissions.ADMIN_WARP.check(player) == false)
 			{
 				return Util.invalidPermissions(player);
 			}
 			
 			String targetPlayer = args.get(0);
 			WarpSuitePlayer target = plugin.getPlayerManager().getWarpPlayer(targetPlayer);
 			if(target == null)
 			{
 				player.sendMessage(NEGATIVE_PRIMARY + "Player \'" + NEGATIVE_SECONDARY + targetPlayer + NEGATIVE_PRIMARY + "\' is not online!");
 				return true;
 			}
 			else
 			{
 				String warpName = args.get(1);
 				if(target.getWarpManager().warpIsSet(warpName))
 				{
 					SimpleLocation sLoc = target.getWarpManager().loadWarp(warpName);
 					return warpPlayer(player, sLoc);
 				}
 				else
 				{
 					player.sendMessage(NEGATIVE_PRIMARY + "Player \'" + NEGATIVE_SECONDARY + targetPlayer + NEGATIVE_PRIMARY + "\' does not have a warp called \'" +
 							NEGATIVE_SECONDARY + warpName + NEGATIVE_PRIMARY + "\' set!");
 					return true;
 				}
 			}
 		}
 		
 		String warpName = args.get(0);
 		if(player.getWarpManager().warpIsSet(warpName))
 		{
 			final SimpleLocation sLoc = player.getWarpManager().loadWarp(warpName);
 			return warpPlayer(player, sLoc);
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
 	
 	private boolean warpPlayer(final WarpSuitePlayer player, final SimpleLocation sLoc)
 	{
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
 				int id = plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
 					@Override
 					public void run()
 					{
 						if(plugin.getPendingWarpManager().isWaitingToTeleport(player.getName()))
 						{
 							plugin.getPendingWarpManager().removePlayer(player.getName());
 							player.teleport(plugin, sLoc);
 						}
 					}
 				}, Config.timer * 20L).getTaskId();
 
 				plugin.getPendingWarpManager().addPlayer(player.getName(), id);
 				
 				return true;
 			}
 		}
 		else
 		{
 			player.sendMessage(NEGATIVE_PRIMARY + "The world warp \'" + NEGATIVE_SECONDARY + "\' is located in either no longer exists, or isn't loaded");
 			return true;
 		}
 	}
 
 }
