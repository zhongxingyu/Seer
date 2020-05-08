 package net.robbytu.banjoserver.bungee;
 
 import net.craftminecraft.bungee.bungeeyaml.bukkitapi.InvalidConfigurationException;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.plugin.Plugin;
 import net.robbytu.banjoserver.bungee.bans.BanCommand;
 import net.robbytu.banjoserver.bungee.bans.BanLoginListener;
 import net.robbytu.banjoserver.bungee.bans.TempBanCommand;
 import net.robbytu.banjoserver.bungee.kicks.KickCommand;
 import net.robbytu.banjoserver.bungee.warns.WarnsCommand;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 
 public class Main extends Plugin {
     public static final String PLUGIN_NAME = "bs-bungee";
     public static Main instance;
     public static PluginConfig config;
     public static Connection conn;
 
     public void onLoad() {
         instance = this;
         config = new PluginConfig(this);
     }
 
     public void onEnable() {
         // Set up a connection
         try{
             conn = DriverManager.getConnection("jdbc:mysql://" + config.db_host + ":" + config.db_port + "/" + config.db_database + "?autoReconnect=true", config.db_username, config.db_password);
         }
         catch (Exception e) {
             getLogger().warning("bs-bungee has not been enabled. Please check your configuration.");
             e.printStackTrace();
         }
 
         getLogger().info("Registering commands...");
         this.registerCommands();
 
         getLogger().info("Registering listeners...");
         this.registerListeners();
     }
 
     private void registerCommands() {
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new WarnsCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new BanCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new TempBanCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new KickCommand());
         ProxyServer.getInstance().getPluginManager().registerCommand(this, new IPBanCommand());
     }
 
     private void registerListeners() {
         ProxyServer.getInstance().getPluginManager().registerListener(this, new BanLoginListener());
     }
 
     public void onDisable() {
         try {
             config.save();
             getLogger().info("Saved configuration.");
         } catch (InvalidConfigurationException e) {
             getLogger().warning("Could not save configuration file to disk.");
             e.printStackTrace();
         }
     }
 }
