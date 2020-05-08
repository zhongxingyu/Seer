 package net.new_liberty.commandtimer;
 
 import net.new_liberty.commandtimer.models.CommandSet;
 import net.new_liberty.commandtimer.models.CommandSetGroup;
 import net.new_liberty.commandtimer.timer.TimerManager;
 import com.google.common.collect.ImmutableMap;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permission;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Command Timer.
  */
 public class CommandTimer extends JavaPlugin {
     private static final Map<String, String> DEFAULT_MESSAGES;
 
     static {
         ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder();
 
         builder.put("warmup", "&6Command will run in &c%time% seconds. Don't move.");
         builder.put("warmup-cancelled", "&cPending command request cancelled.");
         builder.put("warmup-in-progress", "&cThis command is warming up. Don't move.");
         builder.put("cooldown", "&cError: &6You must wait &c%time% seconds to use this command again.");
        builder.put("interact-blocked", "&cError: &6You can''t do this while the command is warming up!");
 
         DEFAULT_MESSAGES = builder.build();
     }
     /**
      * Manages our timers (warmups and cooldowns)
      */
     private TimerManager timers;
 
     /**
      * Stores the global messages.
      */
     private Map<String, String> messages;
 
     /**
      * Stores sets.
      */
     private Map<String, CommandSet> sets;
 
     /**
      * Stores commands mapped to their corresponding sets.
      */
     private Map<String, CommandSet> commands;
 
     /**
      * Stores the command groups.
      */
     private Map<String, CommandSetGroup> groups;
 
     @Override
     public void onEnable() {
         saveDefaultConfig();
         loadConfig();
 
         Bukkit.getPluginManager().registerEvents(new CTListener(this), this);
 
         timers = new TimerManager(this);
         timers.startTasks();
     }
 
     /**
      * Loads the configuration.
      */
     private void loadConfig() {
         FileConfiguration config = getConfig();
 
         // Load messages
         messages = new HashMap<String, String>();
         ConfigurationSection messagesSection = config.getConfigurationSection("messages");
         if (messagesSection != null) {
             for (Entry<String, Object> msg : messagesSection.getValues(false).entrySet()) {
                 messages.put(msg.getKey(), msg.getValue().toString());
             }
         }
 
         // Load sets
         sets = new HashMap<String, CommandSet>();
         commands = new HashMap<String, CommandSet>();
         ConfigurationSection setsSection = config.getConfigurationSection("sets");
         if (setsSection != null) {
             for (String key : setsSection.getKeys(false)) {
                 // Get the set section
                 ConfigurationSection setSection = setsSection.getConfigurationSection(key);
                 if (setSection == null) {
                     // Skip if not a section
                     getLogger().log(Level.WARNING, "Invalid set configuration for set ''{0}''. Skipping.", key);
                     continue;
                 }
 
                 // Set messages
                 Map<String, String> setMessages = new HashMap<String, String>();
                 ConfigurationSection setMessagesSection = setSection.getConfigurationSection("messages");
                 if (setMessagesSection != null) {
                     for (Entry<String, Object> setMessage : setMessagesSection.getValues(false).entrySet()) {
                         setMessages.put(setMessage.getKey(), setMessage.getValue().toString());
                     }
                 }
 
                 // Set commands
                 Set<String> setCommands = new HashSet<String>();
                 List<String> setCmdConfig = setSection.getStringList("commands");
 
                 cmd:
                 for (String setCmd : setCmdConfig) {
                     // Lowercase the commands to make sure
                     setCmd = setCmd.toLowerCase();
 
                     // Check if the command has already been added in a different form to prevent conflicts
 
                     // In this command set
                     for (String cmd : setCommands) {
                         if (cmd.startsWith(setCmd) || setCmd.startsWith(cmd)) {
                             getLogger().log(Level.WARNING, "The command ''{0}'' from set ''{1}'' conflicts with the command ''{2}'' from the same set.", new Object[]{setCmd, key, cmd});
                             continue cmd;
                         }
                     }
 
                     // In previous command sets
                     for (String cmd : commands.keySet()) {
                         if (cmd.startsWith(setCmd) || setCmd.startsWith(cmd)) {
                             getLogger().log(Level.WARNING, "The command ''{0}'' from set ''{1}'' conflicts with the command ''{2}'' from set ''{3}''.", new Object[]{setCmd, key, cmd, commands.get(cmd).getId()});
                             continue cmd;
                         }
                     }
 
                     setCommands.add(setCmd);
                 }
 
                 // Add the set to memory
                 CommandSet set = new CommandSet(this, key, setMessages, setCommands);
                 sets.put(key, set);
 
                 // Add the commands to our mapping
                 for (String cmd : set.getCommands()) {
                     commands.put(cmd, set);
                 }
             }
         }
 
         // Load groups
         groups = new HashMap<String, CommandSetGroup>();
         ConfigurationSection groupsSection = config.getConfigurationSection("groups");
         if (groupsSection != null) {
             for (String key : groupsSection.getKeys(false)) {
                 // Get the group section
                 ConfigurationSection groupSection = groupsSection.getConfigurationSection(key);
                 if (groupSection == null) {
                     // Skip if not a section
                     getLogger().log(Level.WARNING, "Invalid group configuration for group ''{0}''. Skipping.", key);
                     continue;
                 }
 
                 Map<CommandSet, Integer> warmups = new HashMap<CommandSet, Integer>();
                 Map<CommandSet, Integer> cooldowns = new HashMap<CommandSet, Integer>();
 
                 // Get the group's command set configurations
                 for (String set : groupSection.getKeys(false)) {
                     // Verify if this is an actual CommandSet
                     CommandSet cs = sets.get(set);
                     if (cs == null) {
                         // Skip if not a section
                         getLogger().log(Level.WARNING, "The set ''{0}'' does not exist for group ''{1}'' to use. Skipping.", new Object[]{set, key});
                         continue;
                     }
 
                     ConfigurationSection setSection = groupSection.getConfigurationSection(set);
                     if (setSection == null) {
                         // Skip if not a section
                         getLogger().log(Level.WARNING, "Invalid group set configuration for group ''{0}'' and set ''{1}''. Skipping.", new Object[]{key, set});
                         continue;
                     }
 
                     int warmup = setSection.getInt("warmup", 0);
                     warmups.put(cs, warmup);
 
                     int cooldown = setSection.getInt("cooldown", 0);
                     cooldowns.put(cs, cooldown);
                 }
 
                 CommandSetGroup group = new CommandSetGroup(this, key, warmups, cooldowns);
 
                 // Create and add a permission
                 Permission perm = new Permission(group.getPermission());
                 Bukkit.getPluginManager().addPermission(perm);
 
                 groups.put(key, group);
             }
         }
     }
 
     /**
      * Gets the CommandSetGroup of a given player.
      *
      * @param p
      * @return
      */
     public CommandSetGroup getGroup(Player p) {
         if (p == null) {
             return null; // Player is offline, silently fail
         }
 
         for (CommandSetGroup g : groups.values()) {
             if (p.hasPermission(g.getPermission())) {
                 return g;
             }
         }
         return null;
     }
 
     /**
      * Gets a CommandSet from the corresponding command.
      *
      * @param command
      * @return
      */
     public CommandSet getCommandSet(String command) {
         for (Entry<String, CommandSet> e : commands.entrySet()) {
             if (command.startsWith(e.getKey())) {
                 return e.getValue();
             }
         }
         return null;
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
      * Gets the TimerManager instance.
      *
      * @return
      */
     public TimerManager getTimers() {
         return timers;
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
 }
