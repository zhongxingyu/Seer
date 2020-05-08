 package net.new_liberty.commandtimer;
 
 import net.new_liberty.commandtimer.listeners.CommandListener;
 import net.new_liberty.commandtimer.timer.TimerManager;
 import com.google.common.collect.ImmutableMap;
 import java.io.IOException;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.new_liberty.commandtimer.listeners.WarmupCancelListener;
 import net.new_liberty.commandtimer.set.CommandSet;
 import net.new_liberty.commandtimer.set.CommandSetManager;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Command Timer.
  */
 public class CommandTimer extends JavaPlugin {
     private static CommandTimer instance;
 
     private static final Map<String, String> DEFAULT_MESSAGES;
 
     static {
         ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder();
 
         builder.put("warmup", "&6Command will run in &c%time% seconds. Don't move.");
         builder.put("warmup-in-progress", "&cThis command is warming up. Don't move.");
         builder.put("warmup-no-damage", "&cPending command request cancelled.");
         builder.put("warmup-no-interact", "&cPending command request cancelled.");
         builder.put("warmup-no-move", "&cPending command request cancelled.");
         builder.put("cooldown", "&cError: &6You must wait &c%time% seconds to use this command again.");
 
         DEFAULT_MESSAGES = builder.build();
     }
     /**
      * Manages our command sets
      */
     private CommandSetManager commandSets;
 
     /**
      * Manages our timers (warmups and cooldowns)
      */
     private TimerManager timers;
 
     /**
      * Stores the global messages.
      */
     private Map<String, String> messages;
 
     @Override
     public void onEnable() {
         instance = this;
 
         saveDefaultConfig();
         reloadConfig();
         loadConfig();
 
         Bukkit.getPluginManager().registerEvents(new CommandListener(this), this);
         Bukkit.getPluginManager().registerEvents(new WarmupCancelListener(this), this);
 
         timers = new TimerManager(this);
         timers.initialize();
     }
 
     @Override
     public void onDisable() {
         try {
             timers.save();
         } catch (IOException ex) {
             getLogger().log(Level.SEVERE, "Error when saving timers!", ex);
         }
     }
 
     /**
      * Loads the configuration.
      */
     private void loadConfig() {
         Map[] c = ConfigLoader.loadConfig(getConfig());
         messages = c[0];
         commandSets = new CommandSetManager(c[1], c[2], c[3]);
         commandSets.setupPermissions();
     }
 
     /**
      * Gets the CommandSetManager instance.
      *
      * @return
      */
     public CommandSetManager getCommandSets() {
         return commandSets;
     }
 
     /**
      * Gets the TimerManager instance.
      *
      * @return
      */
     public TimerManager getTimers() {
         return timers;
     }
 
     /**
      * Gets a CTPlayer.
      *
      * @param name
      * @return
      */
     public CTPlayer getPlayer(String name) {
         return new CTPlayer(this, name);
     }
 
     /**
      * Gets the default message.
      *
      * @param key
      * @return
      */
     public String getMessage(String key) {
         String msg = messages.get(key);
         if (msg == null) {
             msg = DEFAULT_MESSAGES.get(key);
         }
        return msg;
     }
 
     /**
      * Gets this CommandTimer instance.
      *
      * @return
      */
     public static CommandTimer getInstance() {
         return instance;
     }
 
     /**
      * Logs a message.
      *
      * @param level
      * @param msg
      */
     public static void log(Level level, String msg) {
         instance.getLogger().log(level, msg);
     }
 }
