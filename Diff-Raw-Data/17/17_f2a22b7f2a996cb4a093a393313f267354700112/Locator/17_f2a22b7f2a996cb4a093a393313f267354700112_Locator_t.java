 package com.wwsean08.locator;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Locator extends JavaPlugin{
 	Server server;
 	@Override
 	public void onDisable() {
 	}
 
 	@Override
 	public void onEnable() {
 		server = Bukkit.getServer();
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(args.length == 1){
 			if(sender instanceof Player){
 				Player player = (Player) sender;
 				if(player.hasPermission("Geo.geoLocate"))
					geoLocate(args[0], sender);
 			}else if(sender instanceof ConsoleCommandSender)
				geoLocate(args[0], sender);
 		}
 		return true;
 	}
 	/**
 	 * this does the actual geolocation of the player
 	 * @param playerName the player who we are geolocating
 	 * @param sender the person who sent the command
 	 */
 	private void geoLocate(String playerName, final CommandSender sender){
 		LocatorAsyncCall runnable;
 		if(sender == null){
			runnable = new LocatorAsyncCall(this, playerName, sender);
 		}else{
			runnable = new LocatorAsyncCall(this, playerName, sender);
 		}
 		server.getScheduler().scheduleAsyncDelayedTask(this, runnable);
 	}
 }
