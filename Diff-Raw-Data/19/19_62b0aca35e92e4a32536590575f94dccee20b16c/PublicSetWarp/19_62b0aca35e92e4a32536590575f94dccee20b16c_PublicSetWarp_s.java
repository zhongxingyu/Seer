 package com.mrz.dyndns.server.warpsuite.commands.publicWarps;
 
 import static com.mrz.dyndns.server.warpsuite.util.Coloring.*;
 
 import java.util.List;
 
 import com.mrz.dyndns.server.warpsuite.WarpSuite;
 import com.mrz.dyndns.server.warpsuite.commands.WarpSuiteCommand;
 import com.mrz.dyndns.server.warpsuite.managers.PublicWarpManager;
 import com.mrz.dyndns.server.warpsuite.permissions.Permissions;
 import com.mrz.dyndns.server.warpsuite.players.WarpSuitePlayer;
 import com.mrz.dyndns.server.warpsuite.util.SimpleLocation;
 import com.mrz.dyndns.server.warpsuite.util.Util;
 
 public class PublicSetWarp extends WarpSuiteCommand
 {
 
 	public PublicSetWarp(WarpSuite plugin)
 	{
 		super(plugin);
 	}
 
 	@Override
 	public boolean warpPlayerExecute(WarpSuitePlayer player, List<String> args, List<String> variables)
 	{
 		if(Permissions.PUBLIC_SET.check(player, true) == false)
 		{
 			return true;
 		}
 		
 		if(args.size() == 0)
 		{
 			return false;
 		}
 		
 		String warpName = args.get(0);
 		if(Util.isValidWarpName(warpName) == false)
 		{
 			player.sendMessage(NEGATIVE_PRIMARY + "\'" + NEGATIVE_SECONDARY + warpName + NEGATIVE_PRIMARY + "\' is an invalid warp name!");
 			return true;
 		}
 		PublicWarpManager manager = plugin.getPublicWarpManager();
 		boolean overwritten = manager.warpIsSet(warpName);
 		SimpleLocation sLoc = new SimpleLocation(player.getPlayer().getLocation());
 		manager.setWarp(warpName, sLoc);
 		player.sendMessage(POSITIVE_PRIMARY + "Public warp \'" + POSITIVE_SECONDARY + warpName + POSITIVE_PRIMARY + "\' has been " + (overwritten ? "overwritten" : "set"));
 		
 		return true;
 	}
 
 	@Override
 	public String getUsage()
 	{
		return "warp public" + USAGE_ARGUMENT + "[playerName] " + USAGE + "set|add " + USAGE_ARGUMENT + "[warpName]";
 	}
 }
