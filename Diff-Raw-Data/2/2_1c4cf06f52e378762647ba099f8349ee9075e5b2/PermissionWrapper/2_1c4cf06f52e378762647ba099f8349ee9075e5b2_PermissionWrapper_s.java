 package spia1001.InvFall;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class PermissionWrapper 
 {
 	public static String NODE_INVFALL = "invfall.invfall";
	public static String NODE_INVFALL_ARG = "invfall.invfall";
 	public static String NODE_BLOCKFALL = "invfall.blockfall";
 	public static String NODE_TOOLFALL = "invfall.toolfall";
 	private static PermissionHandler permissionHandler;
 	private boolean PermissionsEnabled = true;
 	public PermissionWrapper(InvFall plugin)
 	{
 	    if (permissionHandler != null) {
 	        return;
 	    }
 	    
 	    Plugin permissionsPlugin = plugin.getServer().getPluginManager().getPlugin("Permissions");
 	    
 	    if (permissionsPlugin == null) {
 	    	System.out.println("Permission system not detected, defaulting to all players.");
 	    	PermissionsEnabled = false;
 	    	return;
 	    }
 	    
 	    permissionHandler = ((Permissions) permissionsPlugin).getHandler();
 	    System.out.println("Found and will use plugin "+((Permissions)permissionsPlugin).getDescription().getFullName());
 	}
 	public boolean hasPermission(Player player,String node)
 	{
 		if(!PermissionsEnabled)
 			return true;
 		if(permissionHandler.has(player, "invfall.*"))
 			return true;
 		return permissionHandler.has(player, node);
 	}
 }
