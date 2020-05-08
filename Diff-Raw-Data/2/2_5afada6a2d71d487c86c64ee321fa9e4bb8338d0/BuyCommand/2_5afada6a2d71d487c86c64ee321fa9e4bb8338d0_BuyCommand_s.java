 package net.mayateck.BuyCommand;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BuyCommand extends JavaPlugin implements Listener{
 	public static String head = "8[3BuyCommand8]r ";
 	public static String ver = "";
 	public static PluginDescriptionFile pluginFile = null;
 	public static Permission permission = null;
 	public static Economy economy = null;
 	
 	@Override
 	public void onEnable(){
 		pluginFile = this.getDescription();
 		ver = pluginFile.getVersion();
 		getLogger().info("#===# BuyCommand v"+ver+" by Wehttam664 #===#");
 		getLogger().info("Checking for updates...");
 			// TODO: Check for updates.
 		getLogger().info("Setting up...");
 			this.saveDefaultConfig();
			new BuyCommandHandler(this);
 			new EconomyHandler(this);
 			setupPermissions();
 			setupEconomy();
 			new NodeSetup(this);
 			// No need for event handlers.
 		getLogger().info("Ready.");
 		getLogger().info("#================================================#");
 	}
 	
 	@Override
 	public void onDisable(){
 		getLogger().info("#===# BuyCommand v"+ver+" by Wehttam664 #===#");
 		getLogger().info("Wrapping up...");
 			// Reset these in-case the thread lingers.
 			pluginFile = null;
 			ver = "";
 		getLogger().info("Shut down. Goodbye.");
 		getLogger().info("#================================================#");
 	}
 	
 	private boolean setupPermissions(){
         RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
         if (permissionProvider != null) {
             permission = permissionProvider.getProvider();
         }
         return (permission != null);
     }
 	private boolean setupEconomy(){
         RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
         if (economyProvider != null) {
             economy = economyProvider.getProvider();
         }
         return (economy != null);
     }
 }
