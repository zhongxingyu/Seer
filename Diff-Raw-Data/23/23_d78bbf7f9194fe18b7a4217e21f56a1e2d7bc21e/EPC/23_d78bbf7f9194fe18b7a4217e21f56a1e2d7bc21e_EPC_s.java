 package net.sqdmc.epc;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.plugin.PluginManager;
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
 
     /** Show cooldown messages. */
     boolean showMessage = true;
 
     /** Message to show on insufficient funds to cast enderpearl. */
     String messageMoney = "§cNot enough money to throw pearl. Need at least {price}.";
     /** Message to show on cooldown. */
     String messageCooldown = "§cEnderpearl cooldown remaining: {seconds} seconds.";
     /** Message to send if player is not allowed to use ender pearls. */
     String messageNotAllowed = "§cYou may not use ender pearls.";
 
     /** Vault hook. */
     Economy economy = null;
 
 
     public void onEnable() {
         log = getLogger();
         EPC = this;
 
         saveDefaultConfig();
 
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(new EPCListener(), this);
 
         getCommand("epc").setExecutor(this);
 
         setupEconomy(); 
         reloadConfig();
 
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
             economy = getServer().getServicesManager().load(c);
 
         } catch (ClassNotFoundException e) {
             // loading vault failed
         }
 
         if (economy != null)
             log.info("Vault hooked. Enderpearl throwing price enabled.");
         else {
             log.info("Unable to load Vault. Enderpearl throwing price is disabled.");
             price = 0;
         }
 
         return (economy != null);
     }
 
     @Override public void reloadConfig() {
         super.reloadConfig();
         Configuration config = getConfig();
         cooldown = config.getLong("cooldown");
         price = config.getDouble("price");
 
         ConfigurationSection msgs = config.getConfigurationSection("messages");
         messageCooldown = msgs.getString("cooldown", messageCooldown);
         messageMoney = msgs.getString("money", messageMoney);
         messageNotAllowed = msgs.getString("notallowed", messageNotAllowed);
     }
 
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
         if (args.length >=1 && "reload".equalsIgnoreCase(args[0])) {
             reloadConfig();
             sender.sendMessage("[EPC] Reloaded configuration!");
             return true;
         }
 
         return false;
     }
 
 }
