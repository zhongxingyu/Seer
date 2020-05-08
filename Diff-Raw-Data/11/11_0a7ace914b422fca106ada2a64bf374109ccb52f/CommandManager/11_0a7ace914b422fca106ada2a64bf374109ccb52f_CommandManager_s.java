 /*******************************************************************************
  * Copyright (c) 2011 James Richardson.
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
 
 package name.richardson.james.bukkit.util.command;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 public class CommandManager implements CommandExecutor {
 
   private final HashMap<String, Command> commands = new LinkedHashMap<String, Command>();
   private final String plugin_full_name;
   private final String description;
 
   public CommandManager(final PluginDescriptionFile description) {
     this.plugin_full_name = description.getFullName();
     this.description = description.getDescription();
   }
 
   public Map<String, Command> getCommands() {
     return Collections.unmodifiableMap(this.commands);
   }
 
   @Override
   public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command, final String label, final String[] args) {
     if (args.length != 0) {
       if (this.commands.containsKey(args[0])) {
         this.commands.get(args[0]).onCommand(sender, command, label, args);
       } else if (args[0].equalsIgnoreCase("help")) {
         if (args.length != 2) {
           sender.sendMessage(ChatColor.RED + "/" + command.getName() + " help <command>");
           sender.sendMessage(ChatColor.YELLOW + "You must specify a command.");
         } else if (this.commands.containsKey(args[1])) {
           final Command c = this.commands.get(args[1]);
           sender.sendMessage(ChatColor.LIGHT_PURPLE + c.getDescription());
           sender.sendMessage(ChatColor.YELLOW + "/" + command.getName() + " " + c.getName() + " " + c.getUsage());
           
         } else {
           sender.sendMessage(ChatColor.RED + "/" + command.getName() + " help <command>");
           sender.sendMessage(ChatColor.YELLOW + "You must specify a valid command.");
         }
       } else {
         sender.sendMessage(ChatColor.RED + "Invalid command!");
         sender.sendMessage(ChatColor.YELLOW + "Type /" + command.getName() + " for a list of commands.");
       }
     } else {
       sender.sendMessage(ChatColor.LIGHT_PURPLE + this.plugin_full_name);
       sender.sendMessage(ChatColor.AQUA + this.description);
       sender.sendMessage(ChatColor.GREEN + "Type /" + command.getName() + " help <command> for details on a command.");
       for (final Entry<String, Command> c : this.commands.entrySet()) {
         // only display usage information for commands that the player is allowed to use.
         if (sender.hasPermission(c.getValue().getPermission())) {
          sender.sendMessage(ChatColor.YELLOW + "- /" + command.getName() + " " + c.getValue().getName() + " " + c.getValue().getUsage());
         }
       }
     }
     return true;
   }
 
   /**
    * Register a sub command underneath the root command defined the executor was
    * created.
    * 
    * @param commandName
    * The string to associated this command with (without the prefix).
    * @param command
    * An instance of the command that should be registered.
    */
   public void registerCommand(final String command, final Command executor) {
     this.commands.put(command, executor);
   }
 
 }
