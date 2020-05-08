 package net.swagserv.andrew2060.swagservbounties;
 //Class Imports
 import java.sql.SQLException;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy; //Vault Economy Support
 import net.milkbowl.vault.permission.Permission; //Vault Permissions Support
 import net.milkbowl.vault.chat.Chat; //Vault Chat Support
 
 //Begin Bukkit Class Imports
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 //End Bukkit Class Imports
 
 
 @SuppressWarnings("unused")
 public class Bounties extends JavaPlugin {
 	Logger log;
 	private CommandHandler commandHandler;
 	public Economy economy;
 	public Permission permission;
 	public Chat chat;
     public boolean factionisEnabled = false;
 	public boolean MySQL;
 	String dbHost;
 	int dbPort;
 	String dbUser;
 	String dbPass;
	String dbDatabase; 
 	SQLHandler sqlHandler;
     //Begin External Plugin Detection Setup
 	private void setupFactions()
 	{
 		Plugin factions = getServer().getPluginManager().getPlugin("Factions");
         if (factions != null) {             
         	this.factionisEnabled = true;
         	log.info("Successfully Hooked Into Factions");
         }
 	}
 	private boolean setupEconomy()
     {
         RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
         if (economyProvider != null) {
             economy = economyProvider.getProvider();
             log.info("Economy Plugin Hooked Through Vault");
         }
 
         return (economy != null);
     }	
 	private Boolean setupPermissions()
     {
         RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
         if (permissionProvider != null) {
             permission = permissionProvider.getProvider();
             log.info("Permissions Plugin Hooked Through Vault");
         }
         return (permission != null);
     }
     private boolean setupChat()
     {
         RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
         if (chatProvider != null) {
             chat = chatProvider.getProvider();
             log.info("Chatting Plugin Hooked Through Vault");
         }
 
         return (chat != null);
     }
     private void loadConfig() {
 		getConfig().options().copyDefaults(true);
 		this.MySQL = getConfig().getBoolean("MySQL");
 		this.dbHost = getConfig().getString("MySQLhost", "");
 		this.dbPort = getConfig().getInt("MySQLport");
 		this.dbUser = getConfig().getString("MySQLuser", "");
 		this.dbPass = getConfig().getString("MySQLpass", "");
 		this.dbDatabase = getConfig().getString("MySQLdb", "");
 		saveConfig();
 		
     }
     private void loadMySQL() {
     	if (this.MySQL) {
  			// Declare MySQL Handler
 			try {
 				sqlHandler = new SQLHandler(dbHost, dbPort, dbDatabase, dbUser,
 						dbPass);
 			} catch (InstantiationException e1) {
 				e1.printStackTrace();
 			} catch (IllegalAccessException e1) {
 				e1.printStackTrace();
 			} catch (ClassNotFoundException e1) {
 				e1.printStackTrace();
 			}
  			// Initialize MySQL Handler
  				if (sqlHandler.connect(true)) {
  					log.info("[SwagServ-Bounties] MySQL connection successful");
  	 				// Check if the tables exist, if not, create them
  					if (!sqlHandler.tableExists(dbDatabase,"bountiesplayer")) {
  						log.info("[Swagserv-Bounties] Creating player MySQL table");
  						String query = "CREATE TABLE `bountiesplayer` ( `id` int(5) NOT NULL AUTO_INCREMENT, `target` varchar(32) NOT NULL, `amount` double(64,2), PRIMARY KEY (`id`) ) AUTO_INCREMENT=1 ;";
  						try {
  							sqlHandler.executeQuery(query, true);
  						} catch (SQLException e) {
  							e.printStackTrace();
  						}
  					}
  				} else {
  					log.severe("[SwagServ-Bounties] MySQL is necessary");
  					this.MySQL = false;
  		 			Bukkit.getServer().getPluginManager().disablePlugin(this);
  				}
  		} else {
  			Bukkit.getServer().getPluginManager().disablePlugin(this);
 		}
     }
 	public void onEnable() {
 		log = this.getLogger();
 		//Enable Status Logging
 		log.info("Initializing...");
 		//Initialize Vault Hooks
 		setupEconomy();
 		setupPermissions();
 		setupChat();
 		//Initialize Factions
 		setupFactions();
 		//Load Config.yml
 		loadConfig();
 		loadMySQL();
 		commandHandler = new CommandHandler(this);
 		getCommand("bounty").setExecutor(commandHandler);
 		Bukkit.getPluginManager().registerEvents(new PlayerDeathHandler(this), this);
 		log.info("Plugin Hooks Successful");
 		//-----------------//
 	}
 }
