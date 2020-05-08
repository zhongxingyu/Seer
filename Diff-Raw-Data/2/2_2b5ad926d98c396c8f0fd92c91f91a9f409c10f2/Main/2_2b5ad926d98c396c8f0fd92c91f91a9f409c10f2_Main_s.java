 package com.pvpkillz.plugins.Abilities;
 
 import java.util.logging.Logger;
 
 import utilities.BGChat;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin{
 
 	Logger log = Logger.getLogger("Minecraft");
 	
 	public ABListener list;
 	public ConfigManager config;
 	
 	public void onEnable() {
 			
 			config = new ConfigManager(this);
 			abdesc();
 			list = new ABListener(this);
 			log.info("[Extra-Abilities] Made by Undeadkillz, For PvPKillz");
 			log.info("[Extra-Abilities] If you purchased this plugin, then please contact Undeadkillz on Bukkit.org");
 			log.info("[Extra-Abilities] This plugin requires The BukkitGames to function correctly.");
 	}
 	
 	public void onDisable() {
 		
 		log.info("[Extra-Abilities] Plugin disabled");
 	}
 	
 	public void abdesc() {
 		
 		BGChat.setAbilityDesc(100, config.readString("Abilities.100.Desc"));
 		BGChat.setAbilityDesc(101, config.readString("Abilities.101.Desc"));
		BGChat.setAbilityDesc(101, config.readString("Abilities.102.Desc"));
 		
 	}
 }
