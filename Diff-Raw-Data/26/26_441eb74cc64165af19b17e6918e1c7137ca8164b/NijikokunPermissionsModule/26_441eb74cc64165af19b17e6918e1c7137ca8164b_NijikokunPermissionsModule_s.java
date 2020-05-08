 package net.eonz.bukkit.psduo.permissions;
 
 import org.bukkit.entity.Player;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class NijikokunPermissionsModule implements PermissionsInterface {
 
 	private final PermissionHandler handler;
 	private final String pluginName;
 	public NijikokunPermissionsModule(Permissions permissions, String pluginName) {
 		handler = permissions.getHandler();
 		this.pluginName = pluginName;
 	}
 	
 	@Override
 	public boolean has(Player player, String permission, String world) {
		String fullPermission = pluginName + '.' + permission;
		return handler.has(world, player.getName(), fullPermission);
 	}
 	
 	@Override
 	public boolean inGroup(Player player, String group, String world) {
 		return handler.inGroup(world, player.getName(), group);
 	}
 	
 }
