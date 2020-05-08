 package com.vildaberper.DefaultCommands;
 
 import java.util.List;
 
 import org.bukkit.entity.Player;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class Perm{
 	public static PermissionHandler PermissionsHandler = null;
 
 	public static void setupPermissions(String world){
 		if(Perm.PermissionsHandler == null){
 			if(V.plugin.getServer().getPluginManager().getPlugin("Permissions") != null && ((Permissions) V.plugin.getServer().getPluginManager().getPlugin("Permissions")).getHandler() != null){
 				Perm.PermissionsHandler = ((Permissions)V.plugin.getServer().getPluginManager().getPlugin("Permissions")).getHandler();
 			}else{
 				Misc.setConfig(world, "op_permissions", true);
 			}
 		}
 	}
 
 	public static void clearPermissions(String world){
 		Misc.getConfig(world).getStringList("permissions").clear();
 	}
 
 	public static List<String> getPermissions(String world){
 		return Misc.getConfig(world).getStringList("permissions");
 	}
 
 	public static String getPermission(String world, int index){
 		return Misc.getConfig(world).getStringList("permissions").get(index);
 	}
 
 	public static void setPermission(String world, String permission){
 		Misc.getConfig(world).getStringList("permissions").add(permission);
 	}
 
 	public static int getSize(String world){
 		return Misc.getConfig(world).getStringList("permissions").size();
 	}
 
 	public static boolean hasPermission(Player player, String node){
 		if(Misc.getConfig(player.getWorld().getName()).getBoolean("op_permissions")){
 			if(player.isOp()){
 				return true;
 			}
 			for(int i = 0; i < getSize(player.getWorld().getName()); i++){
 				if(getPermission(player.getWorld().getName(), i).equalsIgnoreCase(node) || getPermission(player.getWorld().getName(), i).equalsIgnoreCase(node.substring(0, node.lastIndexOf(".")) + ".*")){
 					return true;
 				}
 			}
 			player.sendMessage(Misc.getColoredString("not_permission"));
 			return false;
 		}
 		if(!PermissionsHandler.has(player, node)){
 			player.sendMessage(Misc.getColoredString("not_permission"));
 			return false;
 		}
 		return true;
 	}
 
 	public static boolean hasPermissionSilent(Player player, String node){
 		if(Misc.getConfig(player.getWorld().getName()).getBoolean("op_permissions")){
 			if(player.isOp()){
 				return true;
 			}
 			for(int i = 0; i < getSize(player.getWorld().getName()); i++){
				if(getPermission(player.getWorld().getName(), i).equalsIgnoreCase(node)){
 					return true;
 				}
 			}
 			return false;
 		}
 		if(!PermissionsHandler.has(player, node)){
 			return false;
 		}
 		return true;
 	}
 }
