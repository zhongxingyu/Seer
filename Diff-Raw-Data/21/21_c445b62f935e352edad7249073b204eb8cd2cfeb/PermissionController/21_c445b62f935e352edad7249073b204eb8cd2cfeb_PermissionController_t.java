 package com.alk.battleShops.controllers;
 
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import com.alk.battleShops.BattleShops;
 import com.alk.battleShops.Defaults;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 /**
  * 
  * @author alkarin
  *
  */
 public class PermissionController {
 	//    private static final Configuration config = new Configuration(new File(Defaults.PERMISSION_FILE));
 
 	public static WorldGuardPlugin worldGuard;
 
 	public PermissionController(){}
 
 	public void loadPermissions(){
 		loadWorldGuardPlugin();
 		//    	config.load();
 	}
 
 
 	private void loadWorldGuardPlugin() {
 		if (worldGuard != null) {
 			return;
 		}
 
 		Plugin wgPlugin = BattleShops.getBukkitServer().getPluginManager().getPlugin("WorldGuard");
 		if (wgPlugin == null) {
 			System.out.println("WorldGuard not detected, defaulting to OP");
 			return;
 		}
 
 		worldGuard = ((WorldGuardPlugin) wgPlugin);
 		System.out.println("Found and will use plugin "+((WorldGuardPlugin)wgPlugin).getDescription().getFullName());
 	}
 
 	public static boolean hasPermissions(Player player, Block block) {
 		if (worldGuard != null) 
 			return worldGuard.canBuild(player, block) || PermissionController.isAdmin(player);
 		if (player.hasPermission("shop.create"))
 			return true;
 		return PermissionController.isAdmin(player);		
 	}
 
 	public static boolean isAdmin(String name) {
 		if (name.equals(Defaults.ADMIN_NAME)) return true;
 		Player p = BattleShops.getBukkitServer().getPlayer(name);
 		if (p == null)
 			return false;
 		return PermissionController.isAdmin(p);
 	}
 	public static boolean isAdmin(CommandSender p) {
 		return p.hasPermission("shop.admin") || p.isOp();
 	}
 
 }
