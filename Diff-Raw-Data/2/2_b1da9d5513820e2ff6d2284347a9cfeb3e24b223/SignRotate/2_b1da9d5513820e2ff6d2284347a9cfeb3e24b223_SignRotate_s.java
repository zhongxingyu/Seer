 /**
  * Programmer: Jacob Scott
  * Program Name: SignRotate
  * Description:
  * Date: Apr 8, 2011
  */
 package com.jascotty2.signrotate;
 
 import com.jascotty2.CheckInput;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijikokun.bukkit.Permissions.Permissions;
 import org.bukkit.plugin.Plugin;
 
 /**
  * @author jacob
  */
 public class SignRotate extends JavaPlugin {
 
     protected final static Logger logger = Logger.getLogger("Minecraft");
     public static final String name = "SignRotate";
     public SRConfig config = null;
     protected static Permissions permissions = null;
     Rotater rot = new Rotater(this);
 
     public void onEnable() {
 
         PluginManager pm = getServer().getPluginManager();
         Plugin test = pm.getPlugin("Permissions");
         if (test != null) {//this.getServer().getPluginManager().isPluginEnabled("Permissions")) {
             permissions = (Permissions) test;//this.getServer().getPluginManager().getPlugin("Permissions");
             Log("Attached to Permissions.");
         }
 
         pm.registerEvent(Event.Type.PLAYER_INTERACT, rot.signAdder, Priority.Normal, this);
         config = new SRConfig(this.getConfiguration());

         rot.start();
         Log(this.getDescription().getVersion() + " enabled");
     }
 
     public void onDisable() {
         if (rot.delaySave != null) {
             rot.save();
         }
         rot.stop();
         Log("disabled");
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command,
             String commandLabel, String[] args) {
         String commandName = command.getName().toLowerCase();
         if (commandName.equalsIgnoreCase("signrotate")) {
             if (args.length == 0) {
                 if (sender instanceof Player) {
                     if (permissions == null || Permissions.Security == null
                             || Permissions.Security.has((Player) sender, "signrotate.create")) {
                         if (rot.signAdder.toggleWait((Player) sender)) {
                             sender.sendMessage("click a sign");
                         } else {
                             sender.sendMessage("click listener removed");
                         }
                     } else {
                         sender.sendMessage("only an authorized player can do that!");
                     }
                 } else {
                     sender.sendMessage("only a player can do that!");
                 }
                 return true;
             } else if (args[0].equalsIgnoreCase("clockwise") || args[0].equalsIgnoreCase("cw")) {
                 if (permissions == null || Permissions.Security == null
                         || Permissions.Security.has((Player) sender, "signrotate.admin")) {
                     config.isClockwise = true;
                     config.save();
                     sender.sendMessage("set clockwise");
                 } else {
                     sender.sendMessage("only an authorized player can do that!");
                 }
                 return true;
             } else if (args[0].equalsIgnoreCase("counterclockwise") || args[0].equalsIgnoreCase("ccw")) {
                 if (permissions == null || Permissions.Security == null
                         || Permissions.Security.has((Player) sender, "signrotate.admin")) {
                     config.isClockwise = false;
                     config.save();
                     sender.sendMessage("set counterclockwise");
                 } else {
                     sender.sendMessage("only an authorized player can do that!");
                 }
                 return true;
             } else if (args[0].equalsIgnoreCase("delay")) {
                 if (permissions == null || Permissions.Security == null
                         || Permissions.Security.has((Player) sender, "signrotate.admin")) {
                     if (args.length == 2) {
                         double t = CheckInput.GetDouble(args[1], -1);
                         if (t > 0) {
                             rot.setWait((long) (t * 1000));
                             config.save();
                             sender.sendMessage(String.format("rotate delay set to %.1fs", (double) rot.getWait() / 1000));
                             rot.start();
                         } else {
                             sender.sendMessage(args[1] + " is not a positive number");
                         }
                         return true;
                     } else {
                         sender.sendMessage("delay time required");
                     }
                 } else {
                     sender.sendMessage("only an authorized player can do that!");
                     return true;
                 }
             }
         }
         return false;
     }
 
     public static void Log(String txt) {
         logger.log(Level.INFO, String.format("[%s] %s", name, txt));
     }
 
     public static void Log(Level loglevel, String txt) {
         Log(loglevel, txt, true);
     }
 
     public static void Log(Level loglevel, String txt, boolean sendReport) {
         logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt));
     }
 
     public static void Log(Level loglevel, String txt, Exception params) {
         if (txt == null) {
             Log(loglevel, params);
         } else {
             logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt), (Exception) params);
         }
     }
 
     public static void Log(Level loglevel, Exception err) {
         logger.log(loglevel, String.format("[%s] %s", name, err == null ? "? unknown exception ?" : err.getMessage()), err);
     }
 } // end class SignRotate
 
