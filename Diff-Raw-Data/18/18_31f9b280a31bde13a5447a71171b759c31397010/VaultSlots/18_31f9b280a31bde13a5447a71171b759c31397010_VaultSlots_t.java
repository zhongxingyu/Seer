 package me.sirtyler.VaultSlots;
 
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class VaultSlots extends JavaPlugin{
 	public static VaultSlots plugin;
 	public final Logger logger = Logger.getLogger("Minecraft");
 	private FileConfiguration config;
 	private final SignChange signchanger = new SignChange(this);
 	private final PlayerUse interact = new PlayerUse(this);
 	public CommandEx myExecutor;
 	public Deck deck;
 	public Log log;
 	public Permission permission = null;
     public Economy economy = null;
     public boolean inDebug = false;
 	public VaultSlots() {
 		super();
 	}
 	
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		if(log.sendInfo("[" + pdfFile.getName() + "]" + " version-" + pdfFile.getVersion() + " is now Disabled.")) return;
 		this.logger.info("[" + pdfFile.getName() + "]" + " version-" + pdfFile.getVersion() + " is now Disabled.");
 	}
 
	@SuppressWarnings("deprecation")
 	@Override
 	public void onEnable() {
 		deck = new Deck(this);
 		log = new Log(this);
 		myExecutor = new CommandEx(this);
 		PluginManager pm = getServer().getPluginManager();
 		PluginDescriptionFile pdfFile = this.getDescription();
 		setupPermissions();
 		setupEconomy();
 		pm.registerEvent(Event.Type.SIGN_CHANGE, this.signchanger, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.interact, Event.Priority.Highest, this);
 		checkConfig();
 		checkDebug();
 		getCommand("slots").setExecutor(myExecutor);
 		getCommand("vs").setExecutor(myExecutor);
 		if(log.sendInfo("version-" + pdfFile.getVersion() + " is now Enabled.")) return;
 		this.logger.info("[" + pdfFile.getName() + "]" + " version-" + pdfFile.getVersion() + " is now Enabled.");
 	}
 	private void checkConfig() {
 		try{
 			config = this.getConfig();
 			config.options().copyDefaults(true);
 			this.saveConfig();
 		}catch(Exception e){
 			if(log.sendExceptionInfo(e)) return;
 			e.printStackTrace();
 		}
 	}
 	private void checkDebug() {
 		try {
			config.getBoolean("debug");
 			inDebug = true;
 		} catch(Exception e){
 			if(log.sendExceptionInfo(e)) return;
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
