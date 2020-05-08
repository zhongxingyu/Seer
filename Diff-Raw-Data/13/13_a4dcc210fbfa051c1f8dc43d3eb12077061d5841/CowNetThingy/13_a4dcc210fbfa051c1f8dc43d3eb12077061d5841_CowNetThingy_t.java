 package us.fitzpatricksr.cownet.utils;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.logging.Logger;
 
 public class CowNetThingy implements CommandExecutor {
     private Logger logger = Logger.getLogger("Minecraft");
     private JavaPlugin plugin;
     private String trigger;
     private String permissionNode;
     private boolean isEnabled;
 
     public CowNetThingy(JavaPlugin plugin, String permissionRoot, String trigger) {
         this.plugin = plugin;
         this.trigger = trigger;
         this.permissionNode = permissionRoot + "." + trigger;
         this.isEnabled = getConfigBoolean("enable", false);
         if (!this.isEnabled()) {
             //allow this common alias
             this.isEnabled = getConfigBoolean("enabled", true);
         }
         if (isEnabled) {
             plugin.getCommand(trigger).setExecutor(this);
             logInfo(trigger + " enabled");
         } else {
             logInfo(trigger + " disabled");
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
             if (args.length == 1) {
                 if ("help".equalsIgnoreCase(args[0])) {
                     player.sendMessage(getHelpString(player));
                     return true;
                 } else if ("reload".equalsIgnoreCase(args[0])) {
                     reload();
                     player.sendMessage("Reloaded...");
                     return true;
                 }
             }
             if (!hasPermissions(player)) {
                 player.sendMessage("Sorry, you don't have permission");
                 return false;
             }
             return handleCommand(sender, cmd, args);
         } else if (sender.getClass().getName().contains("Console")) {
             // commands from the console
             return handleCommand(sender, cmd, args);
         } else {
             return false;
         }
     }
 
     public final String getTrigger() {
         return trigger;
     }
 
     public final ConfigurationSection getConfigSection() {
         return (ConfigurationSection) plugin.getConfig().get(trigger);
     }
 
     public final int getConfigInt(String key, int def) {
         return plugin.getConfig().getInt(trigger + "." + key, def);
     }
 
     public final boolean getConfigBoolean(String key, boolean def) {
         return plugin.getConfig().getBoolean(trigger + "." + key, def);
     }
 
     public final String getConfigString(String key, String def) {
         return plugin.getConfig().getString(trigger + "." + key, def);
     }
 
     public final void saveConfiguration() {
         plugin.saveConfig();
     }
 
     public final boolean hasPermissions(Player player) {
         if (player.isOp() || player.hasPermission(permissionNode)) {
             return true;
         } else {
             logInfo(player.getName() + " does not have " + permissionNode);
             return false;
         }
     }
 
     public final boolean hasPermissions(Player player, String perm) {
        return hasPermissions(player, perm, false) || player.hasPermission("*");
     }
 
     public final boolean hasPermissions(Player player, String perm, boolean allowOps) {
         if ((allowOps && player.isOp()) || player.hasPermission(permissionNode + "." + perm)) {
             return true;
         } else {
             logInfo(player.getName() + " does not have explicit " + permissionNode + "." + perm);
             return false;
         }
     }
 
     public final JavaPlugin getPlugin() {
         return plugin;
     }
 
     public final void logInfo(String msg) {
         logger.info(permissionNode + ": " + msg);
     }
 
     public final boolean isEnabled() {
         return isEnabled;
     }
 
     // disable the plugin if there was some critical startup error.
     protected void disable() {
         isEnabled = false;
         logInfo("Plugin disabled.");
     }
 
     protected void reload() {
     }
 
     protected String getHelpString(Player player) {
         return "There isn't any help for you...";
     }
 
     protected boolean handleCommand(Player sender, Command cmd, String[] args) {
         return false;
     }
 
     protected boolean handleCommand(CommandSender sender, Command cmd, String[] args) {
         if (sender instanceof Player) {
             return handleCommand((Player) sender, cmd, args);
         } else {
             return false;
         }
     }
 }
