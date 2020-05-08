 package net.sqdmc.epc;
  
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.MetricsLite;
  
 public class EPC extends JavaPlugin {
 	
 	public static EPC EPC;
 	
 	private Logger log;
 	
 	// configuration section
 	
 	/** Cooldown of pearls in ms. */
 	long cooldown = 3000;
 	
 	/** Price of a pearl throw. */
 	double price = 0;
 	
 	/** Show cooldown message. */
 	boolean showMessage = true;
 	
 	/** Vault hook. */
 	Economy economy = null;
 	
 
     public void onEnable() {
     	log = getLogger();
     	EPC = this;
     	
     	saveDefaultConfig();
     	
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new EPCListener(), this);
 		
         getCommand("epc").setExecutor(new Commands());
         
         setupEconomy();
         loadConfig();
         
         try {
             MetricsLite metrics = new MetricsLite(this);
             metrics.start();
         } catch (IOException e) {
        	log.info("Failed to submit PluginMetrics stats");
         }
     }
     
     private boolean setupEconomy()
     {
    	try {
	        @SuppressWarnings("unchecked")
            Class<Economy> c = (Class<Economy>) Class.forName("net.milkbowl.vault.economy.Economy");
	        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(c);
	        if (economyProvider != null) 
	            economy = economyProvider.getProvider();
	        
        } catch (ClassNotFoundException e) {
        	log.info("Unable to load Vault. Enderpearl throwing price is disabled.");
	        price = 0;
         }
 
         return (economy != null);
     }
     
     private void loadConfig() {
     	reloadConfig();
 		Configuration config = getConfig();
 		cooldown = config.getLong("cooldown");
 		price = config.getDouble("price");
     }
     
     
     private class Commands implements CommandExecutor {
 
 		@Override
     	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 			
 			if (args.length >=1 && "reload".equalsIgnoreCase(args[0])) {
 				loadConfig();
 				sender.sendMessage("[EPC] Reloaded configuration!");
 				return true;
 			}
 			
 			return false;
 		}
     	
     }
 
 }
