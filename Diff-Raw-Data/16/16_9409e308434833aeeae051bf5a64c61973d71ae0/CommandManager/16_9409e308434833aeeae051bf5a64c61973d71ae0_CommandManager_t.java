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
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.TabCompleter;
 
 import name.richardson.james.bukkit.utilities.localisation.Localisation;
 import name.richardson.james.bukkit.utilities.logging.Logger;
 import name.richardson.james.bukkit.utilities.plugin.Plugin;
 
 public final class CommandManager implements CommandExecutor, TabCompleter {
 
   /** The colour given to optional arguments */
   public static final ChatColor OPTIONAL_ARGUMENT_COLOUR = ChatColor.GREEN;
 
   /** The colour given to required arguments */
   public static final ChatColor REQUIRED_ARGUMENT_COLOUR = ChatColor.YELLOW;
 
   /** A collection of all the commands registered to this manager */
   private final Map<String, Command> commands = new LinkedHashMap<String, Command>();
 
   /** the localised name of the help command */
   private final String helpCommand;
 
   private final Localisation localisation;
 
   private final Logger logger;
 
   /** The localised description of the plugin */
   private final String pluginDescription;
 
   /** The full name of the plugin including version */
   private final String pluginName;
 
   public CommandManager(final Plugin plugin) {
     this.localisation = plugin.getLocalisation();
     this.logger = plugin.getCustomLogger();
     this.pluginName = plugin.getDescription().getFullName();
     this.pluginDescription = this.localisation.getMessage(plugin, "description");
     this.helpCommand = this.localisation.getMessage(this, "help-command");
   }
 
   public void addCommand(final Command command) {
     this.addCommand(command, command.getName());
   }
 
   public void addCommand(final Command command, final String name) {
     this.logger.debug(String.format("Adding command: %s", command.getClass().getSimpleName()));
     this.commands.put(name, command);
   }
 
   public Map<String, Command> getCommands() {
     return Collections.unmodifiableMap(this.commands);
   }
 
   public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command cmd, final String label, final String[] args) {
 
     if (args.length == 0) {
       // display command listing and help
       sender.sendMessage(ChatColor.LIGHT_PURPLE + this.pluginName);
       sender.sendMessage(ChatColor.AQUA + this.pluginDescription);
       sender.sendMessage(this.localisation.getMessage(this, "help-usage", cmd.getName(), this.helpCommand));
       for (final Command command : this.commands.values()) {
         if (command.testPermission(sender)) {
           sender.sendMessage(this.getCommandHelpEntry(label, command));
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
         sender.sendMessage(this.getCommandHelpEntry(label, command));
       } else {
         sender.sendMessage(this.localisation.getMessage(this, "invalid-command"));
         sender.sendMessage(this.localisation.getMessage(this, "list-commands", cmd.getName()));
       }
       return true;
     } else {
       sender.sendMessage(this.localisation.getMessage(this, "invalid-command"));
       sender.sendMessage(this.localisation.getMessage(this, "list-commands", cmd.getName()));
       return true;
     }
 
   }
 
   private String getCommandHelpEntry(final String label, final Command command) {
     String usage = command.getUsage();
     usage = usage.replaceAll("\\<", REQUIRED_ARGUMENT_COLOUR + "<");
     usage = usage.replaceAll("\\[", OPTIONAL_ARGUMENT_COLOUR + "[");
     return this.localisation.getMessage(this, "help-entry", label, REQUIRED_ARGUMENT_COLOUR + command.getName(), usage);
   }
 
   private String[] prepareArguments(final String[] args, final String name) {
     if (args[0].equalsIgnoreCase(name)) {
       final String[] arguments = new String[args.length - 1];
       System.arraycopy(args, 1, arguments, 0, args.length - 1);
       return arguments;
     }
     return args;
   }
 
   public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
     this.logger.debug(String.format("%s supplied %s arguments for completion", sender.getName(), String.valueOf(args.length)));
     List<String> list = new ArrayList<String>();
    if (args.length == 1 ) {
      if ("help".startsWith(args[0])) {
        list.add("help");
      }
       for (Command command : this.commands.values()) {
        if (command.testPermission(sender) && command.getName().startsWith(args[0])) {
          list.add(command.getName());
         }
       }     
       return list;
     } else {
       if (this.commands.containsKey(args[0])) {
         Command command = commands.get(args[0]);
         this.logger.debug(String.format("Passing tab completion to %s", command.getName()));
         return command.onTabComplete(sender, cmd, label, prepareArguments(args, args[0]));
       } else if (args[0].equalsIgnoreCase("help")) {
         for (Command command : this.commands.values()) {
           if (command.testPermission(sender)) {
            if (command.getName().startsWith(args[1])) {
               list.add(command.getName());
             }
           }
         }    
         return list;
       } else {
         return list;
       }
     }
   }
 
 }
