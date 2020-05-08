 package org.melonbrew.fee;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.melonbrew.fee.commands.YesCommand;
 
 public class Fee extends JavaPlugin {
 	private Logger log;
 	
 	private Economy economy;
 	
 	private Map<Player, String> commands;
 	
 	public void onEnable(){
 		log = getServer().getLogger();
 		
 		commands = new HashMap<Player, String>();
 		
 		Phrase.init(this);
 		
 		if (!setupEconomy()){
 			log(Phrase.VAULT_HOOK_FAILED);
 			
 			getServer().getPluginManager().disablePlugin(this);
 			
 			return;
 		}
 		
 		getConfig().options().copyDefaults(true);
 		
 		getConfig().options().header("Fee Config - melonbrew.org\n" +
 				"# serveraccount - An account for fees to go too. (Blank for none)\n" +
 				"# closespeed - How many milliseconds (1000 milliseconds is 1 second) before doors, trapdoors and gates auto close.\n" +
 				"# globalcommands - A command followed by it's cost. For all players.\n" +
 				"# groupcommands - Per group commands.);\n");
 		
 		saveConfig();
 		
 		getCommand("yes").setExecutor(new YesCommand(this));
 	}
 	
 	public String getKey(String message){
 		message = message.toLowerCase();
 		
 		//Group stuff here
 		
 		ConfigurationSection globalCommands = getConfig().getConfigurationSection("globalcommands");
 		
 		Set<String> keys = globalCommands.getKeys(false);
 		
 		for (String key : keys){
 			if (message.startsWith(key.toLowerCase())){
 				return key;
 			}
 		}
 		
 		return null;
 	}
 	
 	public double getKeyMoney(String key){
 		return getConfig().getDouble("globalcommands." + key);
 	}
 	
 	public String getCommand(Player player){
 		return commands.get(player);
 	}
 	
 	public void addCommand(Player player, String command){
 		commands.put(player, command);
 	}
 	
 	public void removeCommand(Player player){
 		commands.remove(player);
 	}
 	
 	public boolean containsPlayer(Player player){
 		return commands.containsKey(player);
 	}
 	
 	public Map<Player, String> getCommands(){
 		return commands;
 	}
 	
 	public void log(String message){
 		log.info("[Fee] " + message);
 	}
 	
 	public void log(Phrase phrase, String... args){
 		log(phrase.parse(args));
 	}
 
 	private boolean setupEconomy(){
 		economy = null;
 		
 		if (getServer().getPluginManager().getPlugin("Vault") != null){
 			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
 
 			if (economyProvider != null){
 				economy = economyProvider.getProvider();
 			}
 		}
 
 		return economy != null;
 	}
 	
 	public Economy getEconomy(){
 		return economy;
 	}
 	
 	public String getMessagePrefix(){
		return ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Fee" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
 	}
 }
