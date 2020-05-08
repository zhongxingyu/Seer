 package com.cyberkitsune.prefixchat;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.Configuration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class KitsuneChat extends JavaPlugin{
 	
 	public Logger mcLog = Logger.getLogger("Minecraft");
 	public ChatChannels chans = new ChatChannels();
 	public KitsuneChatCommand exec = new KitsuneChatCommand(this);
 	
 	
 	@Override
 	public void onEnable() {
 		mcLog.info("[KitsuneChat] Enabling KitsuneChat version "+this.getDescription().getVersion());
 		if(!(new File(getDataFolder()+"/config.yml").exists())) {
 			mcLog.info("[KitsuneChat] KitsuneChat config does not exist! Creating default config...");
 			if(!(new File(getDataFolder().toString()).isDirectory())) {
 				new File(getDataFolder().toString()).mkdir();
 			}
 			try {
 				new File(getDataFolder()+"/config.yml").createNewFile();
 			} catch (Exception e) {
 				mcLog.severe("[KitsuneChat] KitsuneChat could NOT create config file!!");
 				e.printStackTrace();
 			}
 			loadConfig();
 			Configuration config = this.getConfig();
 			config.set("global.prefix", "!");
 			config.set("world.prefix", "#");
 			config.set("admin.prefix", "@");
 			config.set("party.prefix", "$");
 			config.set("local.prefix", "%");
 			config.set("party.cost", 0);
 			config.set("local.radius", 200);
 			
 			config.set("global.sayformat", "[{world}] {sender}: {message}");
 			config.set("admin.sayformat", "[{prefix}] {sender}: {message}");
 			config.set("world.sayformat", "[{prefix}] {sender}: {message}");
 			config.set("party.sayformat", "[{party}] {sender}: {message}");
 			config.set("local.sayformat", "{sender}: {message}");
 			
 			config.set("version", 1);
 			this.saveConfig();	
 		}
 		loadConfig();
 		mcLog.info("[KitsuneChat] KitsuneChat config loaded!");
 		this.getServer().getPluginManager().registerEvents(new ChatListener(this), this);
 		getCommand("kc").setExecutor(exec);
 	}
 	
 	@Override
 	public void onDisable() {
 		// TODO Disable stuff?
 		mcLog.info("[KitsuneChat] KitsuneChat disabled! You made CK cry ;~;");
 	}
 	
 	public void loadConfig() {
 		try {
			this.getConfig().load(getDataFolder()+"/config.yml");
 		} catch (Exception e) {
 			mcLog.severe("KitsuneChat could not load the config file!!");
 			e.printStackTrace();
 		} 
 	}
 
 }
