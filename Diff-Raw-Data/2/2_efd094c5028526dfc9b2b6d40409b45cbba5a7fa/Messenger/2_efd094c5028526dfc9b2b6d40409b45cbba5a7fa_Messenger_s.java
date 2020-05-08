 package com.runetooncraft.plugins.EasyMobArmory.core;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 
 public class Messenger {
 
 	private static final Logger log = Logger.getLogger("Minecraft");
	private static final String prefix = "[EMA]";
 	private static String colorprefix;
 	private Config config;
     public Messenger(Config config) {
     	this.config = config;
     	if(config.load()) {
     	colorprefix = config.parsestringcolors(config.getstring("EMA.prefix")) + " ";
     	}
     }
 	
 	public static void severe(String msg) {
 		log.severe(prefix + msg);
 	}
 	public static void info(String msg) {
 		log.info(prefix + msg);
 	}
 	public static void broadcast(String msg) {
 		Bukkit.broadcastMessage(colorprefix + msg);
 	}
 	public static void playermessage(String msg, Player p) {
 		p.sendMessage(colorprefix + msg);
 	}
 	
 }
