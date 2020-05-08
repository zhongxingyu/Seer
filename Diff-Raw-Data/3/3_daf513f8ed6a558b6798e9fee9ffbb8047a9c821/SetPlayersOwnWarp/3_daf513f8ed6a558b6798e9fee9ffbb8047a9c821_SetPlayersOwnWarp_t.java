 package com.mrz.dyndns.server.warpsuite.commands;
 
 import java.util.List;
 
 import com.mrz.dyndns.server.warpsuite.WarpSuite;
 import com.mrz.dyndns.server.warpsuite.WarpSuitePlayer;
 import com.mrz.dyndns.server.warpsuite.permissions.NumeralPermissions;
 import com.mrz.dyndns.server.warpsuite.permissions.Permissions;
 import com.mrz.dyndns.server.warpsuite.util.SimpleLocation;
 import com.mrz.dyndns.server.warpsuite.util.Util;
 
 import static com.mrz.dyndns.server.warpsuite.util.Coloring.*;
 
 public class SetPlayersOwnWarp extends WarpSuiteCommand
 {
 	//warp set|add [warpname]
 	public SetPlayersOwnWarp(WarpSuite plugin)
 	{
 		super(plugin);
 	}
 
 	@Override
 	public boolean warpPlayerExecute(WarpSuitePlayer player, List<String> args, List<String> variables)
 	{
 		if(args.size() == 0)
 		{
 			return false;
 		}
 		
 		if(!Permissions.WARP_SET.check(player))
 		{
 			return Util.invalidPermissions(player);
 		}
 		
 		int allowedWarps = NumeralPermissions.COUNT.getAmount(player);
 		int warpsOwned = player.getWarpManager().getAmountOfSetWarps();
 		
 		if(allowedWarps != -1 && warpsOwned >= allowedWarps)
 		{
 			player.sendMessage(NEGATIVE_PRIMARY + "You already have the maximum warps you're allowed to set! (" + NEGATIVE_SECONDARY + allowedWarps + NEGATIVE_PRIMARY + ")");
 			return true;
 		}
 		
 		String warpName = args.get(0);
 		if(!Util.isValidWarpName(warpName))
 		{
 			player.sendMessage(NEGATIVE_PRIMARY + "\'" + NEGATIVE_SECONDARY + warpName + NEGATIVE_PRIMARY + "\' is an invalid warp name!");
 			return true;
 		}
 		boolean overwritten = player.getWarpManager().warpIsSet(warpName);
		player.getWarpManager().setWarp(warpName, new SimpleLocation(player.getPlayer().getLocation()));
 		player.sendMessage(POSITIVE_PRIMARY + "Warp \'" + POSITIVE_SECONDARY + warpName + POSITIVE_PRIMARY + "\' has been " + (overwritten ? "overwritten" : "set") + ".");
 		
 		return true;
 	}
 
 	@Override
 	public String getUsage()
 	{
 		return "warp set|add " + USAGE_ARGUMENT + "[warpName]";
 	}
 }
