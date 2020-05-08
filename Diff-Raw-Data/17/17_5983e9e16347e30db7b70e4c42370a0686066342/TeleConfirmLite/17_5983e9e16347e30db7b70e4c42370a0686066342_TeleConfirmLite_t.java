 package com.ementalo.tcl;
 
 import com.ementalo.tcl.Commands.ITclCommand;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 public class TeleConfirmLite extends JavaPlugin {
     public static int REQUEST_TIMEOUT = 30;
     private TeleConfirmLitePlayerListener playerListener = null;
     public TeleConfirmLiteUserHandler tclUserHandler = null;
     public Config config = null;
     static final Logger log = Logger.getLogger("Minecraft");
     public static Economy econ = null;
     public static Permission permsBase = null;
 
     public void _expire() {
         final ArrayList<TpAction> removal = new ArrayList<TpAction>();
         for (final TpAction req : tclUserHandler.pendingRequests) {
             if (System.currentTimeMillis() - req.getCreationTime() > REQUEST_TIMEOUT * 1000) {
                 removal.add(req); // queue for removal, it's expired
             }
         }
         for (final TpAction rem : removal) {
             tclUserHandler.pendingRequests.remove(rem);
         }
     }
 
     public void onEnable() {
         tclUserHandler = new TeleConfirmLiteUserHandler();
         config = new Config(this);
         playerListener = new TeleConfirmLitePlayerListener(this);
         this.getServer().getPluginManager().registerEvents(playerListener, this);
 
         try {
             config.loadSettings();
         } catch (Exception ex) {
             log.log(Level.SEVERE, "[TeleConfimLite] Could not load the config file", ex);
         }
         config.AssignSettings();
         log.log(Level.INFO, "[" + this.getDescription().getName() + "] [v" + this.getDescription().getVersion() + "]" + " loaded");
         if (!setupPermissions()) {
            log.log(Level.WARNING, "[" + this.getDescription().getName() + "] Vault not found. Commands will be available to all");
         }
 
     }
 
     private boolean setupPermissions() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
         RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
         permsBase = rsp.getProvider();
         return permsBase != null;
     }
 
     public boolean executeCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args, final ClassLoader classLoader, final String commandPath) {
 
         ITclCommand cmd;
         try {
             cmd = (ITclCommand) classLoader.loadClass(commandPath + command.getName()).newInstance();
         } catch (Exception ex) {
             return false;
         }
 
         if (sender instanceof Player) {
             if (permsBase != null) {
                 if (!permsBase.has((Player) sender, "tcl." + commandLabel)) {
                     sender.sendMessage(Config.permissionDenied);
                     return true;
                 }
             }
 
             cmd.execute((Player) sender, command, commandLabel, args, this);
         } else {
             cmd.execute(sender, command, commandLabel, args, this);
         }
         return true;
     }
 
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
         this._expire();
         return executeCommand(sender, command, commandLabel, args, this.getClass().getClassLoader(), "com.ementalo.tcl.Commands.Command");
     }
 }
 
     
 
 
     
