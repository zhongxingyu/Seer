 /*******************************************************************************
  * Copyright (c) 2012 James Richardson.
  * 
  * CommandManager.java is part of BukkitUtilities.
  * 
  * BukkitUtilities is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * BukkitUtilities is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * BukkitUtilities. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package name.richardson.james.bukkit.utilities.command;
 
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import name.richardson.james.bukkit.utilities.plugin.Localisable;
 import name.richardson.james.bukkit.utilities.plugin.SkeletonPlugin;
 
 public final class CommandManager implements CommandExecutor, Localisable {
 
   public static final ChatColor REQUIRED_ARGUMENT_COLOUR = ChatColor.RED;
   public static final ChatColor OPTIONAL_ARGUMENT_COLOUR = ChatColor.GREEN;
 
   private final SkeletonPlugin plugin;
 
   private final Map<String, Command> commands = new LinkedHashMap<String, Command>();
 
   /** The full name of the plugin including version */
   private final String pluginName;
 
   /** The localised description of the plugin */
   private final String pluginDescription;
 
   /** the localised name of the help command */
   private final String helpCommand;
 
   public CommandManager(final SkeletonPlugin plugin) {
     this.plugin = plugin;
     this.pluginName = plugin.getDescription().getFullName();
     this.pluginDescription = plugin.getMessage("plugin-description");
     this.helpCommand = this.getMessage("help-command");
   }
 
   public void addCommand(final PluginCommand command) {
     this.commands.put(command.getName(), command);
   }
 
   public void addCommand(final PluginCommand command, final String name) {
     this.commands.put(name, command);
   }
 
   public String getChoiceFormattedMessage(String key, final Object[] arguments, final String[] formats, final double[] limits) {
     key = this.getClass().getSimpleName().toLowerCase() + "." + key;
     return this.plugin.getChoiceFormattedMessage(key, arguments, formats, limits);
   }
 
   public Map<String, Command> getCommands() {
     return Collections.unmodifiableMap(this.commands);
   }
 
   public Locale getLocale() {
     return this.plugin.getLocale();
   }
 
   public String getMessage(String key) {
     key = this.getClass().getSimpleName().toLowerCase() + "." + key;
     return this.plugin.getMessage(key);
   }
 
  public String getSimpleFormattedMessage(final String key, final Object argument) {
     final Object[] arguments = { argument };
     return this.plugin.getSimpleFormattedMessage(key, arguments);
   }
 
   public String getSimpleFormattedMessage(String key, final Object[] arguments) {
     key = this.getClass().getSimpleName().toLowerCase() + "." + key;
     return this.plugin.getSimpleFormattedMessage(key, arguments);
   }
 
   public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command cmd, final String label, final String[] args) {
 
     if (args.length == 0) {
       // display command listing and help
       sender.sendMessage(ChatColor.LIGHT_PURPLE + this.pluginName);
       sender.sendMessage(ChatColor.AQUA + this.pluginDescription);
       final String[] messages = { cmd.getName(), this.helpCommand };
       sender.sendMessage(ChatColor.GREEN + this.getSimpleFormattedMessage("help-usage", messages));
       for (final Command command : this.commands.values()) {
         if (command.testPermission(sender)) {
           sender.sendMessage(ChatColor.YELLOW + this.getCommandHelpEntry(label, command));
         }
       }
       return true;
     }
 
     if ((args.length != 0) && this.commands.containsKey(args[0].toLowerCase())) {
       // execute the command
       final Command command = this.commands.get(args[0]);
       final String[] arguments = this.prepareArguments(args, args[0]);
       command.onCommand(sender, cmd, null, arguments);
       return true;
     } else if ((args.length == 2) && args[0].equalsIgnoreCase(this.helpCommand)) {
       if (this.commands.containsKey(args[1]) && this.commands.get(args[1]).testPermission(sender)) {
         final Command command = this.commands.get(args[1]);
         sender.sendMessage(ChatColor.LIGHT_PURPLE + command.getDescription());
         sender.sendMessage(ChatColor.YELLOW + this.getCommandHelpEntry(label, command));
       } else {
         sender.sendMessage(ChatColor.RED + this.getMessage("invalid-command"));
         sender.sendMessage(ChatColor.YELLOW + this.getSimpleFormattedMessage("list-commands-hint", cmd.getName()));
       }
       return true;
     } else {
       sender.sendMessage(ChatColor.RED + this.getMessage("invalid-command"));
       sender.sendMessage(ChatColor.YELLOW + this.getSimpleFormattedMessage("list-commands-hint", cmd.getName()));
       return true;
     }
 
   }
 
   private String getCommandHelpEntry(final String label, final Command command) {
     String usage = command.getUsage();
     usage = usage.replaceAll("\\<", REQUIRED_ARGUMENT_COLOUR + "<");
     usage = usage.replaceAll("\\[", OPTIONAL_ARGUMENT_COLOUR + "[");
     final String[] arguments = { label, REQUIRED_ARGUMENT_COLOUR + command.getName(), usage };
     return this.getSimpleFormattedMessage("help-entry", arguments);
   }
 
   private String[] prepareArguments(final String[] args, final String name) {
     if (args[0].equalsIgnoreCase(name)) {
       final String[] arguments = new String[args.length - 1];
       System.arraycopy(args, 1, arguments, 0, args.length - 1);
       return arguments;
     }
     return args;
   }
 
 }
