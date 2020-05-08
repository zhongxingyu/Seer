 package com.lamtrung.friendlytp;
 
import org.bukkit.Color;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class FriendlyTP extends JavaPlugin implements Listener{
 
 	@Override
 	public void onEnable() {
 		super.onEnable();
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 	
 	@Override
 	public void onDisable() {
 		// TODO Auto-generated method stub
 		super.onDisable();
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String Label, String[] args) {
 		
 		if (cmd.getName().equalsIgnoreCase("tele")) {
 			if (args.length == 1) {
 				Player players[] = sender.getServer().getOnlinePlayers();
 				Player player = (Player)sender;
 				for (Player p : players) {
 					if (args[0].equalsIgnoreCase(p.getName())) {
 						 boolean value = this.getConfig().getBoolean("players." + p.getName());
 						if (value) {
 							player.teleport(p.getLocation());
							player.sendMessage(Color.GREEN + "You teleported to" + p.getName());
							p.sendMessage(Color.GREEN + player.getName() + " teleported to you");
 						}
 						else
							player.sendMessage(Color.GREEN + "Target player does not allow others to teleport or invalid player name");
 						break;
 					}
 				}
 			}
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("settrue")) {
 			Player player = (Player)sender;
 			this.getConfig().set("players." + player.getName(), true);
 			this.saveConfig();
			player.sendMessage(Color.GREEN + "Players may teleport to you");
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("setfalse")) {
 			Player player = (Player)sender;
 			this.getConfig().set("players." + player.getName(), false);
 			this.saveConfig();
			player.sendMessage(Color.GREEN + "Players may not teleport to you");
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("check")) {
 			Player player = (Player)sender;
 			boolean value = this.getConfig().getBoolean("players." + player.getName());
 			if (value)
				player.sendMessage(Color.GREEN + "Players may teleport to you");
 			else
				player.sendMessage(Color.GREEN + "Players may not teleport to you");
 		}
 		return false;
 	}
 	
 	@EventHandler
 	public void playerEnter(PlayerJoinEvent event) {
 		Player player = event.getPlayer();
		player.sendMessage(Color.PURPLE + "/tele [playername] : teleports you to the location of the player if the player allows it");
		player.sendMessage(Color.PURPLE + "/settrue : sets you to allow players to teleport to you");
		player.sendMessage(Color.PURPLE + "/setfalse : sets you to not allow players to teleport to you");
		player.sendMessage(Color.PURPLE + "/check : checks yours current settings");
 		boolean value = this.getConfig().getBoolean("players." + player.getName());
 		this.getConfig().set("players." + player.getName(), value);
 
 		this.saveConfig();
 		
 	}
 
 }
