 package com.mrz.dyndns.server.warpsuite.commands.invites;
 
 import static com.mrz.dyndns.server.warpsuite.util.Coloring.*;
 
 import java.util.List;
 
 import com.mrz.dyndns.server.warpsuite.WarpSuite;
 import com.mrz.dyndns.server.warpsuite.commands.WarpSuiteCommand;
 import com.mrz.dyndns.server.warpsuite.permissions.Permissions;
 import com.mrz.dyndns.server.warpsuite.players.WarpSuitePlayer;
 import com.mrz.dyndns.server.warpsuite.util.SimpleLocation;
 
 public class SendInvite extends WarpSuiteCommand
 {
 	public SendInvite(WarpSuite plugin)
 	{
 		super(plugin);
 	}
 
 	@Override
 	public boolean warpPlayerExecute(WarpSuitePlayer player, List<String> args, List<String> variables)
 	{
 		if(Permissions.INVITE.check(player, true) == false)
 		{
 			return true;
 		}
 		
 		if(args.size() == 0)
 		{
 			return false;
 		}
 		
 		String targetName = variables.get(0);
 		WarpSuitePlayer target = plugin.getPlayerManager().getWarpPlayer(targetName);
 		if(target == null)
 		{
 			player.sendMessage(NEGATIVE_PRIMARY + "\'" + NEGATIVE_SECONDARY + targetName + NEGATIVE_PRIMARY + "\' is not online!");
 			return true;
 		}
 		
 		String warpName = args.get(0);
 		if(player.getWarpManager().warpIsSet(warpName) == false)
 		{
 			player.sendMessage(NEGATIVE_PRIMARY + "You do not have a warp called \'" + NEGATIVE_SECONDARY + warpName + NEGATIVE_PRIMARY + "\' set!");
 			return true;
 		}
 		
 		SimpleLocation sLoc = player.getWarpManager().loadWarp(warpName);
 		boolean result = target.sendRequest(sLoc);
 		if(result)
 		{
 			target.sendMessage(POSITIVE_PRIMARY + "Player \'" + POSITIVE_SECONDARY + player.getName() + POSITIVE_PRIMARY + "\' has invited you to one of their warps! Use " 
					+ USAGE + "/warp accept" + POSITIVE_PRIMARY + " to go to their warp, or " + USAGE + "/warp deny" + POSITIVE_PRIMARY + "to deny it.");
 			
 			player.sendMessage(POSITIVE_PRIMARY + "Request to go to warp \'" + POSITIVE_SECONDARY + warpName 
 					+ POSITIVE_PRIMARY + "\' sent to player \'" + POSITIVE_SECONDARY + targetName + POSITIVE_PRIMARY + "\'");
 			
 			plugin.getLogger().info(player.getName() + " has sent a warp invite to " + targetName + " at location x=" + sLoc.getX() + ", y=" + sLoc.getY() + ", z=" + sLoc.getZ());
 		}
 		else
 		{
 			target.sendMessage(NEGATIVE_PRIMARY + "Player \'" + NEGATIVE_SECONDARY + target.getName() + NEGATIVE_PRIMARY + "\' already has a pending warp request!");
 		}
 		return true;
 	}
 
 	@Override
 	public String getUsage()
 	{
 		return "warp invite" + USAGE_ARGUMENT + "[playerName] " + USAGE + "to " + USAGE_ARGUMENT + "[warpName]";
 	}
 }
