 package com.minecarts.verrier.lockdown;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Type;
 
 import org.bukkit.util.config.Configuration;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.HashMap;
 
 import java.util.Date;
 
 import com.minecarts.verrier.lockdown.listener.*;
 
 public class Lockdown extends JavaPlugin {
     public final Logger log = Logger.getLogger("Minecraft.Lockdown");
 
     private PluginManager pluginManager;
 
     public Configuration config;
     private List<String> requiredPlugins;
     private boolean debug = false;
     
     private boolean locked = false;
     private HashMap<String, Boolean> disabledPlugins = new HashMap<String, Boolean>();
     private HashMap<String, Boolean> lockedPlugins = new HashMap<String, Boolean>();
     
     private HashMap<String, Date> msgThrottle = new HashMap<String,Date>();
     
     private EntityListener entityListener;
     private BlockListener blockListener;
     private PlayerListener playerListener;
     
     public void onEnable(){
         PluginDescriptionFile pdf = getDescription();
         log.info("[" + pdf.getName() + "] version " + pdf.getVersion() + " enabled.");
         
         pluginManager = getServer().getPluginManager();
 
         loadConfig();
         // Start the world in lockdown mode?
         if(config.getBoolean("locked", false)) {
             lock("Starting in lockdown mode.");
         }
         
         //Create our listeners
             entityListener = new EntityListener(this);
             blockListener = new BlockListener(this);
             playerListener = new PlayerListener(this);
             
         
         //Register our events
             //Player
                 pluginManager.registerEvent(Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
                 pluginManager.registerEvent(Type.ENTITY_DAMAGE, entityListener, Event.Priority.Normal, this);
                 //TODO: Add PLAYER_BUCKET_EMPTY, PLAYER_BUCKET_FULL?
             //Painting
                 //Temporarily removed until we update Craftbukkit
                 //pluginManager.registerEvent(Type.PAINTING_CREATE, entityListener, Event.Priority.Normal, this);
                 //pluginManager.registerEvent(Type.PAINTING_REMOVE, entityListener, Event.Priority.Normal, this);
             //Explosions
                 pluginManager.registerEvent(Type.EXPLOSION_PRIME, entityListener, Event.Priority.Normal, this);
                 pluginManager.registerEvent(Type.ENTITY_EXPLODE, entityListener, Event.Priority.Normal, this);
             //Blocks
                 pluginManager.registerEvent(Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
                 pluginManager.registerEvent(Type.BLOCK_PLACE, blockListener, Event.Priority.Normal, this);
                 pluginManager.registerEvent(Type.BLOCK_IGNITE, blockListener, Event.Priority.Normal, this);
                 pluginManager.registerEvent(Type.BLOCK_BURN, blockListener, Event.Priority.Normal, this);
                 pluginManager.registerEvent(Type.BLOCK_FROMTO, blockListener, Event.Priority.Normal, this);
         
         //Start the timer to monitor our required plugins
             Runnable checkLoadedPlugins = new checkLoadedPlugins();
             getServer().getScheduler().scheduleSyncRepeatingTask(this, checkLoadedPlugins, 20, 300); //Check after one second, then every 15 seconds (300 ticks)
             
             
     }
     
     public void onDisable(){
         
     }
     
     public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args){
         if(cmdLabel.equals("lockdown") && sender.isOp()){
             String msg = "Console command";
             if(sender instanceof Player) msg = "Command issued by " + ((Player) sender).getName();
             
             if(args.length > 0) {
                 if(args[0].equals("lock")){
                     lock(msg);
                     sender.sendMessage("LOCKDOWN ENFORCED!");
                     return true;
                 } else if (args[0].equals("unlock")){
                     unlock(msg);
                     sender.sendMessage("Lockdown lifted!");
                     return true;
                 } else if (args[0].equals("reload")) {
                     loadConfig();
                     
                     sender.sendMessage("Lockdown config reloaded.");
                     log("Config reloaded.", false);
                     
                     return true;
                 }
             } else {
                 msg = "Lockdown status: " + (isLocked() ? "LOCKED" : "UNLOCKED");
                 if(!lockedPlugins.isEmpty()) msg += "\n   Locked plugins: " + lockedPlugins.keySet();
                 if(!disabledPlugins.isEmpty()) msg += "\n   Disabled plugins: " + disabledPlugins.keySet();
                 
                 sender.sendMessage(msg);
                 
                 return true;
             }
         }
         return false;
     }
     
     private void loadConfig() {
         if(config == null) config = getConfiguration();
         else config.load();
         
         requiredPlugins = config.getStringList("required_plugins", new ArrayList<String>());
         debug = config.getBoolean("debug", false);
     }
     
     //Repeating plugin loaded checker 
     public class checkLoadedPlugins implements Runnable { 
         public void run() { 
             if(disabledPlugins.isEmpty()) { 
                 log("All required plugins enabled."); 
                 return;
             } 
             
             // clear disabled plugins list in case the required plugins list changes on a config reload
             disabledPlugins.clear();
             
             for(String p : Lockdown.this.requiredPlugins) { 
                 if(!Lockdown.this.pluginManager.isPluginEnabled(p)) { 
                     disabledPlugins.put(p, true);
                     log("Required plugin " + p + " is not loaded or disabled."); 
                 }
             } 
         } 
     }
     
     
     // Internal lock/unlock
     private void lock(String reason){
         locked = true;
         log("LOCKDOWN ENFORCED: " + reason, false);
     }
     private void unlock(String reason){
         locked = false;
         log("Lockdown lifted: " + reason, false);
     }
     
     //External API
     public boolean isLocked(){
         return locked || !disabledPlugins.isEmpty() || !lockedPlugins.isEmpty();
     }
     public boolean isLocked(Plugin p) {
         return isLocked(p.getDescription().getName());
     }
     public boolean isLocked(String pluginName) {
         return lockedPlugins.containsKey(pluginName);
     }
 
     public void lock(Plugin p, String reason) {
         lock(p.getDescription().getName(), reason);
     }
     public void lock(String pluginName, String reason) {
         lockedPlugins.put(pluginName, true);
         log(pluginName + " PLUGIN LOCK: " + reason, false);
     }
 
     public void unlock(Plugin p, String reason) {
         unlock(p.getDescription().getName(), reason);
     }
     public void unlock(String pluginName, String reason) {
         lockedPlugins.remove(pluginName);
         log(pluginName + " plugin lock lifted: " + reason, false);
     }
     
     //Internal logging and messaging
     public void log(String msg){
         log(msg, true);
     }
     public void log(String msg, boolean debug){
         if(debug && !this.debug) return;
         log.info("Lockdown> " + msg);
     }
     
     public void informPlayer(org.bukkit.entity.Player player){
         String playerName = player.getName();
         long time = new Date().getTime();
         if(!this.msgThrottle.containsKey(playerName)){
             this.msgThrottle.put(playerName, new Date());
         }
         if((time - (this.msgThrottle.get(player.getName()).getTime())) > 1000 * 3){ //every 3 seconds
            player.sendMessage(ChatColor.GRAY + "The world is in " + ChatColor.YELLOW + "temporary lockdown mode" + ChatColor.GRAY + " while all plugins are");
            player.sendMessage(ChatColor.GRAY + "   properly loaded. Please try again in a few seconds.");
             this.msgThrottle.put(player.getName(), new Date());
         }
             
     }
 }
