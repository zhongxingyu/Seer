 package de.JeterLP.MakeYourOwnCommands;
 
 import de.JeterLP.MakeYourOwnCommands.Events.CommandListener;
 import de.JeterLP.MakeYourOwnCommands.Events.PlayerJoinListener;
 import de.JeterLP.MakeYourOwnCommands.commands.myoc;
 import de.JeterLP.MakeYourOwnCommands.utils.*;
 import java.net.MalformedURLException;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin {
 
        private ConfigFile loader = null;
        private MetricsChecker mchecker = null;
         public static Permission perms = null;
         public boolean useVault = false;
         private AdvancedUpdater updater = null;
         public static Main instance;
         private static CommandUtils utils;
 
        @Override
         public void onEnable() {
                 getLogger().info("(by JeterLP Version: " + getDescription().getVersion() + ") loading...");
                Main.instance = this;
                utils = new CommandUtils();
                loader = new ConfigFile();
                mchecker = new MetricsChecker();
                 this.loader.loadConfig();
                 try {
                         this.updater = new AdvancedUpdater(this, 54353, "http://dev.bukkit.org/bukkit-plugins/simple-info2/", "SearchForUpdates");
                 } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                 }
                 if (updater != null) updater.search();
                 if (checkVault() && setupPermissions()) {
                         this.useVault = true;
                         getLogger().info("Vault was found! Successfully hooked into: " + perms.getName());
                 } else {
                         getLogger().info("Vault was not found! Vault-support is disabled...");
                 }
                 this.mchecker.checkMetrics();
                 getCommand("myoc").setExecutor(new myoc());
                 getServer().getPluginManager().registerEvents(new CommandListener(), this);
                 getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
                 getLogger().info("(by JeterLP Version: " + getDescription().getVersion() + ") is now enabled.");
         }
 
         @Override
         public void onDisable() {
                 getLogger().info("(by JeterLP Version: " + getDescription().getVersion() + ") is now disabled.");
         }
 
         private boolean setupPermissions() {
                 RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
                 perms = (Permission) rsp.getProvider();
                 return perms != null;
         }
 
         private boolean checkVault() {
                 Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Vault");
                 return plugin != null;
         }
 
         public AdvancedUpdater getUpdater() {
                 return this.updater;
         }

         public static CommandUtils getUtils() {
                 return utils;
         }

 }
