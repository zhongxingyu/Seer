 package me.limebyte.battlenight.core.commands;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.util.Message;
 import me.limebyte.battlenight.api.util.Messenger;
 import me.limebyte.battlenight.core.tosort.ConfigManager;
 import me.limebyte.battlenight.core.tosort.ConfigManager.Config;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
import com.google.common.collect.ImmutableList;

 /**
  * Represents a BattleNight sub-command, which executes various tasks upon user
  * input
  */
 public abstract class BattleNightCommand {
     private final String name;
     private String label;
     private List<String> aliases;
     private List<String> primaryChoices;
     protected String description = "";
     protected String usageMessage;
     private CommandPermission permission;
     public BattleNightAPI api;
 
     public BattleNightCommand(String name) {
         this.name = name;
         label = name.toLowerCase();
         aliases = new ArrayList<String>();
        primaryChoices = ImmutableList.of();
         description = "";
         usageMessage = "/bn " + label;
     }
 
     public boolean aliasMatches(String input) {
         return aliases.contains(input.toLowerCase());
     }
 
     /**
      * Returns a list of active aliases of this command
      * 
      * @return List of aliases
      */
     public List<String> getAliases() {
         return aliases;
     }

     public List<String> getPrimaryChoices() {
         return primaryChoices;
     }

     public void setPrimaryChoices(List<String> primaryChoices) {
         this.primaryChoices = primaryChoices;
     }
 
     /**
      * Gets a brief description of this command
      * 
      * @return Description of this command
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * Returns the current label for this command
      * 
      * @return Label of this command or null if not registered
      */
     public String getLabel() {
         return label;
     }
 
     /**
      * Returns the name of this command
      * 
      * @return Name of this command
      */
     public String getName() {
         return name;
     }
 
     /**
      * Gets the permission required by users to be able to perform this command
      * 
      * @return Permission name, or null if none
      */
     public CommandPermission getPermission() {
         return permission;
     }
 
     /**
      * Gets an example usage of this command
      * 
      * @return One or more example usages
      */
     public String getUsage() {
         return usageMessage;
     }
 
     public boolean labelMatches(String input) {
         return input.equalsIgnoreCase(getLabel());
     }
 
     public boolean matches(String input) {
         return input.equalsIgnoreCase(getName());
     }
 
     /**
      * Executes the command, returning its success
      * 
      * @param sender Source object which is executing this command
      * @param commandLabel The alias of the command used
      * @param args All arguments passed to the command, split via ' '
      * @return true if the command was successful, otherwise false
      */
     public boolean perform(CommandSender sender, String[] args) {
         if (!testPermission(sender)) return false;
         return onPerformed(sender, Arrays.copyOfRange(args, 1, args.length));
     }
 
     /**
      * Sets the list of aliases to request on registration for this command
      * 
      * @param aliases Aliases to register to this command
      * @return This command object, for linking
      */
     public BattleNightCommand setAliases(List<String> aliases) {
         List<String> newAliases = new ArrayList<String>();
 
         for (String alias : aliases) {
             newAliases.add(alias.toLowerCase());
         }
 
         this.aliases = newAliases;
         return this;
     }
 
     /**
      * Sets a brief description of this command
      * 
      * @param description New command description
      * @return This command object, for linking
      */
     public BattleNightCommand setDescription(String description) {
         this.description = description;
         return this;
     }
 
     /**
      * Sets the label of this command If the command is currently registered the
      * label change will only take effect after its been re-registered e.g.
      * after a /bn reload
      * 
      * @param name The command's name
      * @return returns true if the name change happened instantly or false if it
      *         was scheduled for re-registration
      */
     public boolean setLabel(String name) {
         label = name;
         return true;
     }
 
     /**
      * Sets the permission required by users to be able to perform this command
      * 
      * @param permission Permission name or null
      */
     public void setPermission(CommandPermission permission) {
         this.permission = permission;
     }
 
     /**
      * Sets the example usage of this command
      * 
      * @param usage New example usage
      * @return This command object, for linking
      */
     public BattleNightCommand setUsage(String usage) {
         usageMessage = usage;
         return this;
     }
 
     /**
      * Tests the given {@link CommandSender} to see if they can perform this
      * command. If they do not have permission, they will be informed that they
      * cannot do this.
      * 
      * @param target User to test
      * @return true if they can use it, otherwise false
      */
     public boolean testPermission(CommandSender sender) {
         Messenger messenger = api.getMessenger();
         if (getPermission() == null) return true;
 
         if (getPermission().getBukkitPerm() == null || getPermission().getBukkitPerm().length() == 0) return true;
 
         if (ConfigManager.get(Config.MAIN).getBoolean("UsePermissions", false) && sender instanceof Player) {
             String permission = getPermission().getBukkitPerm();
 
            if (sender.hasPermission(permission)) return true;
             else {
                 messenger.tell(sender, Message.NO_PERMISSION_COMMAND);
                 return false;
             }
         } else {
             if (getPermission().isOpPerm()) {
                if (sender.isOp()) return true;
                 else {
                     messenger.tell(sender, Message.NO_PERMISSION_COMMAND);
                     return false;
                 }
             } else return true;
         }
     }
 
     protected String createString(String[] args, int start) {
         StringBuilder string = new StringBuilder();
 
         for (int x = start; x < args.length; x++) {
             string.append(args[x]);
             if (x != args.length - 1) {
                 string.append(" ");
             }
         }
 
         return string.toString();
     }
 
     protected int getInteger(String value, int min) {
         return getInteger(value, min, Integer.MAX_VALUE);
     }
 
     protected int getInteger(String value, int min, int max) {
         int i = min;
 
         try {
             i = Integer.valueOf(value);
         } catch (NumberFormatException ex) {
         }
 
         if (i < min) {
             i = min;
         } else if (i > max) {
             i = max;
         }
 
         return i;
     }
 
     protected abstract boolean onPerformed(CommandSender sender, String[] args);
 
 }
