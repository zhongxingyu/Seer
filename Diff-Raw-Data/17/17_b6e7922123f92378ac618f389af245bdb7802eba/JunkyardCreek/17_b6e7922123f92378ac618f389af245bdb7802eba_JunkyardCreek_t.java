 package me.sirtyler.JunkyardCreek;
 
 import java.io.File;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class JunkyardCreek extends JavaPlugin{
 	public static JunkyardCreek plugin;
 	public FileConfiguration config;
 	public CommandEx myExecutor;
 	public Permission permission = null;
 	public Economy economy = null;
 	
 	public JunkyardCreek() {
 		super();
 	}
 
 	@Override
 	public void onDisable() {
 		//Disabled
 	}
 
 	@Override
 	public void onEnable() {
 		myExecutor = new CommandEx(this);
 		setupPermissions();
 		this.getLogger().info("=====Permission Plugged into " + permission.getName() + "======");
 		setupEconomy();
 		this.getLogger().info("=====Economy Plugged into " + economy.getName() + "======");
 		getCommand("junk").setExecutor(myExecutor);
 		getCommand("jc").setExecutor(myExecutor);
 		checkConfig();
 		this.getLogger().info("================Config Loaded================");
 		Bukkit.getPluginManager().registerEvents(new FishListener(this), this);
 	}
 	
 	private void checkConfig() {
 		String mainDirectory = ("plugins/" + this.getDescription().getName());
 		File file = new File(mainDirectory + File.separator + "config.yml");
 		try{
 			config = this.getConfig();
 			if(!file.exists()) { 
 				config.options().copyDefaults(true);
 				this.saveConfig();
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
     private Boolean setupPermissions(){
         RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
         if (permissionProvider != null) {
             permission = permissionProvider.getProvider();
         }
         return (permission != null);
     }
     
     private Boolean setupEconomy(){
         RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
         if (economyProvider != null) {
             economy = economyProvider.getProvider();
         }
         return (economy != null);
     }
 
 }
