 package net.betterverse.bettercapes;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class OKmain extends JavaPlugin
 {
   public static String name;
   public static String version;
   public static ArrayList<String> authors;
   public OKCommandManager commandManager = new OKCommandManager(this);
   public static HashMap<String, String> nodenames = new HashMap();
   public static HashMap<String, String> nameurls = new HashMap();
   public static String nocape;
 
 	@Override
   public void onEnable()
   {
     name = getDescription().getName();
     version = getDescription().getVersion();
     authors = getDescription().getAuthors();
     OKLogger.initialize(Logger.getLogger("Minecraft"));
     OKLogger.info("Attempting to enable " + name + " v" + version + " by " + (String)authors.get(0) + "...");
     PluginManager pm = getServer().getPluginManager();
     Plugin sp = pm.getPlugin("Spout");
     if (sp == null) {
       OKLogger.info("Spout was not found, disabling...");
       pm.disablePlugin(this);
     } else {
       if (!sp.isEnabled()) {
         pm.enablePlugin(sp);
       }
       OKConfig config = new OKConfig(this);
       config.configCheck();
       new OKFunctions(this);
       OKDB.initialize(this);
 			pm.registerEvents(new OKPlayerListener(this), sp);
       setupCommands();
       OKLogger.info(name + " v" + version + " enabled successfully.");
     }
   }
 
   public void onDisable() {
     OKLogger.info("Attempting to disable " + name + "...");
     OKLogger.info("Terminating worker threads...");
     getServer().getScheduler().cancelTasks(this);
     OKDB.disable();
     OKLogger.info(name + " disabled successfully.");
   }
 
   public static boolean CheckPermission(Player player, String string) {
     return player.hasPermission(string);
   }
 
   private void setupCommands()
   {
     addCommand("capes", new OKCmd(this));
   }
 
   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
     return this.commandManager.dispatch(sender, cmd, label, args);
   }
 
   private void addCommand(String command, CommandExecutor executor) {
     getCommand(command).setExecutor(executor);
     this.commandManager.addCommand(command, executor);
   }
 }
