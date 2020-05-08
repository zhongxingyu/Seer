 package com.github.eramsey.SignHelper;
 
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SignHelper extends JavaPlugin {
 	protected static FileConfiguration config;
 	static Logger log;
     public enum Options {
     	LIST,
     	SHOW
     }
     
 	public void onEnable(){
		try{
			log = this.getLogger();
			config = getConfig();
			config.options().copyDefaults(true);
			this.saveConfig();
			log.info("SignHelper Enabled!");
			getServer().getPluginManager().registerEvents(new SignHelperListener(), this);
		}catch(Exception e1){
		e1.printStackTrace();
		}
		
 	}
 	 
 	public void onDisable(){
 		log.info("SignHelper Disabled.");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		if(cmd.getName().equalsIgnoreCase("sh")){
 			try {
 				Options option = Options.valueOf(args[0].toUpperCase());
 				this.reloadConfig();
 				switch(option) {
 					case LIST:
 						Set<String> storedSigns = config.getConfigurationSection("").getKeys(false);
 						for(String storedSign: storedSigns) {
 							sender.sendMessage(storedSign + ": " + config.getString(storedSign + ".desc"));
 						}
 						return true;
 					case SHOW:
 						if (args.length == 1) { return false; }
 						String[] lines = config.getString(args[1] + ".text").split("\\\\n");
 						sender.sendMessage(args[1]+ ": " + config.getString(args[1] + ".desc"));
 						for(String line: lines) {
 							sender.sendMessage("  " + line);
 						}
 						return true;
 				}
 			} catch (Exception e) {
 				return false;
 			}
 			return true;
 		} 
 		return false; 
 	}
 }
