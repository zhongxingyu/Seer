 package com.runetooncraft.plugins.EasyMobArmory;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.runetooncraft.plugins.EasyMobArmory.SpawnerHandler.SpawnerHandler;
 import com.runetooncraft.plugins.EasyMobArmory.core.Messenger;
 
 
 public class Commandlistener implements CommandExecutor {
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player p = (Player) sender;
 		if(commandLabel.equalsIgnoreCase("ema") || commandLabel.equalsIgnoreCase("easymobarmory")) {
 			if(args.length == 0) {
 				Usage(p);
 			}else if(args.length == 1) {
 				if(p.hasPermission("ema.use")) {
 					if(args[0].equalsIgnoreCase("enable")) {EMAListener.Armoryenabled.put(p, true); Messenger.playermessage("EasyMobArmory enabled", p);}
 					else if(args[0].equalsIgnoreCase("disable")) {EMAListener.Armoryenabled.put(p, false); Messenger.playermessage("EasyMobArmory disabled", p);}
 					else{Usage(p);}
				}else if(p.hasPermission("ema.spawners")) {
 					if(args[0].equalsIgnoreCase("stopspawner")) {SpawnerHandler.CancelSpawnTimer(p);}
 					else if(args[0].equalsIgnoreCase("startspawner")) {SpawnerHandler.StartSpawnTimer(p);}
 					else{Usage(p);}
 				}else{
 					Messenger.NoPermissionCommand(p);
 				}
 			}else if(args.length == 2) {
 				if(p.hasPermission("ema.spawners")) {
 					if(args[0].equalsIgnoreCase("setspawntick")) {SpawnerHandler.SetSpawnTick(p,args[1]);}
 					else if(args[0].equalsIgnoreCase("setdetectionradius")) {SpawnerHandler.setDetectionRadius(p,args[1]);}
 					else if(args[0].equalsIgnoreCase("setradius")) {SpawnerHandler.setMonsterSpawnRadius(p,args[1]);}
 					else{Usage(p);}
 				}else{
 					Messenger.NoPermissionCommand(p);
 				}
 			}else{
 				Usage(p);
 			}
 		}
 		return false;
 	}
 	private void Usage(Player p) {
 		Messenger.playermessage("Usage: /EMA Enable, /EMA Disable, /EMA SetSpawnTick <Seconds>, /EMA SetDetectionRadius <PlayerDetectionRadius>, /EMA SetRadius <SpawnRadius>", p);
 	}
 }
