 package com.wolvencraft.promote;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.wolvencraft.promote.ranks.PromotionLadder;
 
 public class Promote extends JavaPlugin {
 	
 	private static Promote instance;
 	
 	private static FileConfiguration languageData = null;
 	private static File languageDataFile = null;
 	private static Language language;
 	
 	private static Economy economy;
 	private static Permission permissions;
 	
 	private static List<PromotionLadder> ladders;
 	
 	@Override
 	public void onEnable() {
 		instance = this;
 		
 		if (getServer().getPluginManager().getPlugin("Vault") == null) {
             Message.log(Level.SEVERE, "Vault dependency not found!");
 			this.setEnabled(false);
 			return;
         }
         
 		try {
 	        economy = ((RegisteredServiceProvider<Economy>)(getServer().getServicesManager().getRegistration(Economy.class))).getProvider();
 	        permissions = ((RegisteredServiceProvider<Permission>)(getServer().getServicesManager().getRegistration(Permission.class))).getProvider();
 		} catch (NullPointerException npe) {
 			Message.log(Level.SEVERE, "An error occurred while setting up Vault dependency");
 			this.setEnabled(false);
 			return;
 		}
 		
 		if(!new File(getDataFolder(), "config.yml").exists()) {
 			getConfig().options().copyDefaults(true);
 			saveConfig();
 		}
 		
 		reloadSettings();
 	}
 
 	@Override
 	public void onDisable() {
 		
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		CommandManager.setSender(sender);
 		
 		for(CommandManager cmd : CommandManager.values()) {
 			if(cmd.isCommand(command)) {
 				boolean result = cmd.run(args);
 				CommandManager.resetSender();
 				return result;
 			}
 		}
 		
 		Message.sendError(getLanguage().ERROR_COMMAND);
 		CommandManager.resetSender();
 		return false;
 	}
 	
 	private static void reloadLanguageData() {		
 		if (languageDataFile == null) languageDataFile = new File(instance.getDataFolder(), "english.yml");
 		languageData = YamlConfiguration.loadConfiguration(languageDataFile);
 		
 		InputStream defConfigStream = instance.getResource("english.yml");
 		if (defConfigStream != null) {
 			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 			languageData.setDefaults(defConfig);
 		}
 	}
 	
	static FileConfiguration getLanguageData() {
 		if (languageData == null) reloadLanguageData();
 		return languageData;
 	}
 
 	private static void saveLanguageData() {
 		if (languageData == null || languageDataFile == null) return;
 		try { languageData.save(languageDataFile); }
 		catch (IOException ex) { Message.log(Level.SEVERE, "Could not save config to " + languageDataFile); }
 	}
 	
 	public static Promote getInstance() 					{ return instance; }
 	public static Language getLanguage()				{ return language; }
 	public static List<PromotionLadder> getLadders()	{ return ladders; }
 	public static Economy getEconomy()					{ return economy; }
 	public static Permission getPermissions()			{ return permissions; }
 	
 	public static void reloadSettings() {
 		ladders = null;
 		ladders = new ArrayList<PromotionLadder>();
 		for(String ladder : instance.getConfig().getStringList("ladders")) ladders.add(new PromotionLadder(ladder));
 		
 		getLanguageData().options().copyDefaults(true);
 		saveLanguageData();
 		language = null;
 		language = new Language(instance);
 	}
 }
