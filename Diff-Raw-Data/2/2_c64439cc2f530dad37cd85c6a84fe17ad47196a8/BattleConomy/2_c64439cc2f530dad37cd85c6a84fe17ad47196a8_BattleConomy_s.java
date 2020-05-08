 package net.battlenexus.bukkit.economy;
 
 import java.io.File;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.battlenexus.bukkit.economy.api.Api;
 import net.battlenexus.bukkit.economy.api.Vault_BattleConomy;
 import net.battlenexus.bukkit.economy.listeners.BattleCommands;
 import net.battlenexus.bukkit.economy.listeners.BattleConomyListen;
 import net.battlenexus.bukkit.economy.sql.SqlClass;
 import net.milkbowl.vault.economy.Economy;
 
 import org.apache.commons.lang.WordUtils;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.ServicePriority;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BattleConomy extends JavaPlugin {
     FileConfiguration config;
     SqlClass sql;
     boolean connected = false;
     public static BattleConomy INSTANCE;
 
     @Override
     public void onEnable() {
         INSTANCE = this;
         File config = new File(this.getDataFolder(), "config.yml");
         if (!config.exists()) {
             saveDefaultConfig();
             getLogger()
                     .info("Configuration file created, please edit it before attempting to load this plugin");
             getServer().getPluginManager().disablePlugin(this);
         }
         getConfig();
 
         try {
             setupSQL();
         } catch (Exception e) {
             e.printStackTrace();
             getServer().getPluginManager().disablePlugin(this);
         }
 
         if (sql.connect(getConfig().getString("sql.host"), getConfig()
                 .getString("sql.port"), getConfig().getString("sql.database"),
                 getConfig().getString("sql.username"),
                 getConfig().getString("sql.password"))) {
             connected = true;
             sql.prefix = getConfig().getString("sql.prefix");
             Api.sql = sql;
             if (getConfig().getBoolean("sql.auto-create")) {
                 setupMysql();
                 getConfig().set("sql.auto-create", false);
                 saveConfig();
             }
             getLogger().info("Connected to sql server!");
         } else {
             getLogger().info("Couldn't connect to mysql");
             getLogger().info("Plugin not loaded");
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
         Api.config = getConfig();
         Api.prefix = getConfig().getString("currency.prefix");
         Api.singular = getConfig().getString("currency.singular");
         Api.plural = getConfig().getString("currency.plural");
 
         for (String econKey : getConfig().getConfigurationSection("economies")
                 .getKeys(false)) {
             List<String> worlds = new ArrayList<String>();
             for (String world : getConfig().getStringList(
                     "economies." + econKey + ".worlds")) {
                 worlds.add(world);
             }
             Api.economies.put(econKey, worlds);
         }
 
         setupVault();
         new BattleConomyListen(this);
         new BattleCommands(sql);
         getCommand("bc").setExecutor(BattleCommands.instance);
         getCommand("balance").setExecutor(BattleCommands.instance);
         getCommand("addmoney").setExecutor(BattleCommands.instance);
         getCommand("setmoney").setExecutor(BattleCommands.instance);
         getCommand("takemoney").setExecutor(BattleCommands.instance);
         getCommand("balancetop").setExecutor(BattleCommands.instance);
 
         
         getLogger().info("BattleConomy loaded successfully");
     }
 
     private void setupSQL() throws ClassNotFoundException,
             NoSuchMethodException, SecurityException, InstantiationException,
             IllegalAccessException, IllegalArgumentException,
             InvocationTargetException {
        Class<?> class_ = Class.forName("net.battlenexus.bukkit.economy.sql."
                 + WordUtils.capitalizeFully(getConfig().getString("sql.driver",
                         "Sqlite")));
         Class<? extends SqlClass> runClass = class_.asSubclass(SqlClass.class);
         Constructor<? extends SqlClass> constructor = runClass.getConstructor();
         sql = constructor.newInstance();
     }
 
     private void setupMysql() {
         getLogger().info("Creating mysql tables...");
         sql.build("CREATE TABLE IF NOT EXISTS " + sql.prefix + "balances ("
                 + "  economy_key varchar(20) NOT NULL,"
                 + "  user_id int(11) NOT NULL,"
                 + "  balance decimal(19,2) NOT NULL,"
                 + "  PRIMARY KEY (economy_key,user_id)"
                 + ") ENGINE=MyISAM DEFAULT CHARSET=latin1;");
         sql.executeUpdate();
         sql.build("CREATE TABLE IF NOT EXISTS " + sql.prefix + "players ("
                 + "  id int(11) NOT NULL AUTO_INCREMENT,"
                 + "  username varchar(20) NOT NULL," + "  PRIMARY KEY (id),"
                 + "  UNIQUE KEY username (username)"
                 + ") ENGINE=MyISAM  DEFAULT CHARSET=latin1;");
         sql.executeUpdate();
         getLogger().info("Mysql tables created successfully...");
     }
 
     private void setupVault() {
         Plugin vault = getServer().getPluginManager().getPlugin("Vault");
 
         if (vault == null) {
             return;
         }
 
         RegisteredServiceProvider<Economy> economyProvider = getServer()
                 .getServicesManager().getRegistration(Economy.class);
 
         if (economyProvider != null) {
             getServer().getServicesManager().unregister(
                     economyProvider.getProvider());
         }
 
         getServer().getServicesManager().register(Economy.class,
                 new Vault_BattleConomy(this), this, ServicePriority.Highest);
     }
 
     @Override
     public void onDisable() {
         if (connected) {
             sql.disconnect();
             if (!sql.isConnected())
                 getLogger().info("Disconnected from mysql");
             else
                 getLogger().info(
                         "There were errors trying to disconnect from mysql");
         }
         Api.economies.clear();
         getLogger().info("BattleConomy disabled successfully");
         INSTANCE = null;
     }
     
 }
