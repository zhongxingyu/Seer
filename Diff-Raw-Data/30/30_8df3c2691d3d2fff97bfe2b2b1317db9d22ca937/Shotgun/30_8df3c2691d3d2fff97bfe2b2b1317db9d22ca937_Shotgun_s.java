 package com.thebkkingdom.Shotgun;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Shotgun extends JavaPlugin {
 
 	public void onEnable() {
 
 		this.registerEvents();
 		System.out.println("[Shotgun enabled]");
 
 	}
 
 	public void onDisable() {
 
 	}
 
 	private void registerEvents() {
 
 		getServer().getPluginManager().registerEvents(new PlayerListener(),
 				this);
 	}
 
 	public boolean onCommand(CommandSender sender, Command command,
 			String cmdLabel, String[] args) {
 
 		Player player = (Player) sender;
 		Block target = player.getTargetBlock(null, 200);
 		Location targetLocation = target.getLocation();
 
		if (command.getName().equalsIgnoreCase("airstrike") && args.length == 1) {
 			if (player.hasPermission("shotgun.airstrike")) {
 				target.getWorld().strikeLightning(targetLocation);
 				target.getWorld().createExplosion(targetLocation, 5);
 				sender.sendMessage(ChatColor.BLUE
 						+ "[Shotgun] Airstrike called at your crosshairs");
 			}
 		}
 		return false;
 
 	}
 }
