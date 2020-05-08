 package net.new_liberty.commandtimer;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import static net.new_liberty.commandtimer.CommandTimer.log;
 import net.new_liberty.commandtimer.set.CommandSet;
 import org.bukkit.configuration.ConfigurationSection;
 import net.new_liberty.commandtimer.set.CommandSetGroup;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.permissions.Permission;
 
 /**
  * Loads the configuration.
  */
 public final class ConfigLoader {
     /**
      * C'tor
      */
     private ConfigLoader() {
     }
 
     public static Map[] loadConfig(Configuration config) {
         Map msgs = loadMessages(config);
         Map[] sets = loadSets(config);
         Map groups = loadSetGroups(config, sets[0]);
         return new Map[]{msgs, sets[0], sets[1], groups};
     }
 
     /**
      * Loads messages from the config.
      *
      * @param config
      * @return
      */
     public static Map<String, String> loadMessages(Configuration config) {
         Map<String, String> messages = new HashMap<String, String>();
         ConfigurationSection messagesSection = config.getConfigurationSection("messages");
         if (messagesSection != null) {
             for (Map.Entry<String, Object> msg : messagesSection.getValues(false).entrySet()) {
                 messages.put(msg.getKey(), msg.getValue().toString());
             }
         }
         return messages;
     }
 
     /**
      * Loads a CommandSet.
      *
      * @param id
      * @param section
      * @param commandMappings
      * @return
      */
     public static CommandSet loadSet(String id, ConfigurationSection section, Map<String, CommandSet> commandMappings) {
         if (section == null) {
             return null;
         }
 
         // Set messages
         Map<String, String> messages = new HashMap<String, String>();
         ConfigurationSection messagesSection = section.getConfigurationSection("messages");
         if (messagesSection != null) {
             for (Map.Entry<String, Object> message : messagesSection.getValues(false).entrySet()) {
                 messages.put(message.getKey(), message.getValue().toString());
             }
         }
 
         // Set commands
         Set<String> cmdSet = new HashSet<String>();
         List<String> cmds = section.getStringList("commands");
 
         cmd:
         for (String cmd : cmds) {
             // Lowercase the commands to make sure
             cmd = cmd.toLowerCase();
 
             // Check if the command has already been added in a different form to prevent conflicts
 
             // In this command set
             for (String setCmd : cmdSet) {
                if (setCmd.startsWith(cmd + " ") || cmd.startsWith(setCmd + " ")) {
                     log(Level.WARNING, "The command '" + cmd + "' from set '" + id + "' conflicts with the command '" + setCmd + "' from the same set.");
                     continue cmd;
                 }
             }
 
             // In previous command sets
             for (CommandSet set : commandMappings.values()) {
                 String otherCmd = set.getCommand(cmd);
                 if (otherCmd != null) {
                     log(Level.WARNING, "The command '" + cmd + "' from set '" + id + "' conflicts with the command '" + otherCmd + "' from set '" + set.getId() + "'.");
                     continue cmd;
                 }
             }
             cmdSet.add(cmd);
         }
 
         return new CommandSet(id, messages, cmdSet);
     }
 
     /**
      * Loads the CommandSets.
      *
      * @param config
      * @return
      */
     public static Map<String, CommandSet>[] loadSets(Configuration config) {
         Map<String, CommandSet> sets = new HashMap<String, CommandSet>();
         Map<String, CommandSet> commands = new HashMap<String, CommandSet>();
         ConfigurationSection setsSection = config.getConfigurationSection("sets");
         if (setsSection != null) {
             for (String key : setsSection.getKeys(false)) {
                 // Get the set section
                 ConfigurationSection setSection = setsSection.getConfigurationSection(key);
                 CommandSet set = loadSet(key, setSection, commands);
                 if (set == null) {
                     log(Level.WARNING, "Invalid set configuration for set '" + key + "'. Skipping.");
                     continue;
                 }
                 sets.put(key, set);
 
                 // Add the commands to our mapping
                 for (String cmd : set.getCommands()) {
                     commands.put(cmd, set);
                 }
             }
         }
 
         return new Map[]{sets, commands};
     }
 
     /**
      * Loads the ComandSetGroup
      *
      * @param key
      * @param section
      * @param sets A map containing all the sets
      * @return
      */
     public static CommandSetGroup loadSetGroup(String key, ConfigurationSection section, Map<String, CommandSet> sets) {
         if (section == null) {
             return null;
         }
 
         Map<CommandSet, Integer> warmups = new HashMap<CommandSet, Integer>();
         Map<CommandSet, Integer> cooldowns = new HashMap<CommandSet, Integer>();
 
         // Get the group's command set configurations
         for (String set : section.getKeys(false)) {
             // Verify if this is an actual CommandSet
             CommandSet cs = sets.get(set);
             if (cs == null) {
                 // Skip if not a section
                 log(Level.WARNING, "The set '" + set + "' does not exist for group '" + key + "' to use. Skipping.");
                 continue;
             }
 
             ConfigurationSection setSection = section.getConfigurationSection(set);
             if (setSection == null) {
                 log(Level.WARNING, "Invalid group set configuration for group '" + key + "' and set '" + set + "'. Skipping.");
                 continue;
             }
 
             int warmup = setSection.getInt("warmup", 0);
             warmups.put(cs, warmup);
 
             int cooldown = setSection.getInt("cooldown", 0);
             cooldowns.put(cs, cooldown);
         }
 
         return new CommandSetGroup(key, warmups, cooldowns);
     }
 
     /**
      * Loads all CommandSetGroups.
      *
      * @param config
      * @param sets
      * @return
      */
     public static Map<String, CommandSetGroup> loadSetGroups(Configuration config, Map<String, CommandSet> sets) {
         Map<String, CommandSetGroup> groups = new HashMap<String, CommandSetGroup>();
         ConfigurationSection groupsSection = config.getConfigurationSection("groups");
         if (groupsSection != null) {
             for (String key : groupsSection.getKeys(false)) {
                 // Get the group section
                 ConfigurationSection groupSection = groupsSection.getConfigurationSection(key);
 
                 CommandSetGroup group = loadSetGroup(key, groupSection, sets);
                 if (group == null) {
                     log(Level.WARNING, "Invalid group configuration for group '" + key + "'. Skipping.");
                     continue;
                 }
 
                 groups.put(key, group);
             }
         }
 
         if (!groups.containsKey("default")) {
             log(Level.INFO, "There isn't a default group; creating one with no settings.");
             groups.put("default", new CommandSetGroup("default", new HashMap<CommandSet, Integer>(), new HashMap<CommandSet, Integer>()));
         }
 
         return groups;
     }
 }
