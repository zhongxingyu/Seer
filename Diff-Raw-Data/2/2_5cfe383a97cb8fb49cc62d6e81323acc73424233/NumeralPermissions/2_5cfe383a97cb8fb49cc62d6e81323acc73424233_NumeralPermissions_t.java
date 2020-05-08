 package com.mrz.dyndns.server.warpsuite.permissions;
 
 import org.bukkit.permissions.PermissionAttachmentInfo;
 
 import com.mrz.dyndns.server.warpsuite.players.WarpSuitePlayer;
 import com.mrz.dyndns.server.warpsuite.util.Config;
 
 public enum NumeralPermissions
 {
 	COUNT("warpsuite.count.");
 	
 	private NumeralPermissions(String node)
 	{
 		this.node = node;
 	}
 	
 	private final String node;
 	
 	public int getAmount(WarpSuitePlayer p) throws NumberFormatException
 	{
 		if(Permissions.COUNT_INFINITE.check(p, false))
 		{
 			return -1;
 		}
 		
 		for(PermissionAttachmentInfo perm : p.getPlayer().getEffectivePermissions())
 		{
 			if(perm.getPermission().startsWith(node))
 			{
 				String[] permParts = perm.getPermission().split("\\.");
 				String amount = permParts[permParts.length - 1];
				return Integer.parseInt(amount.substring(0, amount.length()));
 			}
 		}
 		
 		return Config.defaultMaxWarps;
 	}
 }
