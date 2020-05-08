 /*******************************************************************************
  * Copyright (c) 2011 James Richardson.
  * 
  * StartCommand.java is part of TimedMessages.
  * 
  * TimedMessages is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * TimedMessages is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * TimedMessages. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package name.richardson.james.bukkit.timedmessages.management;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 
 import name.richardson.james.bukkit.timedmessages.TimedMessages;
 import name.richardson.james.bukkit.utilities.command.CommandArgumentException;
 import name.richardson.james.bukkit.utilities.command.ConsoleCommand;
 import name.richardson.james.bukkit.utilities.command.PluginCommand;
 import name.richardson.james.bukkit.utilities.formatters.TimeFormatter;
 
 @ConsoleCommand
 public class StartCommand extends PluginCommand {
 
   private final TimedMessages plugin;
   
   /** The delay (in seconds) before starting the messages */
   private long delay;
 
   public StartCommand(TimedMessages plugin) {
     super(plugin);
     this.plugin = plugin;
     this.registerPermissions();
   }
 
   public void execute(CommandSender sender) throws name.richardson.james.bukkit.utilities.command.CommandArgumentException, name.richardson.james.bukkit.utilities.command.CommandPermissionException, name.richardson.james.bukkit.utilities.command.CommandUsageException {
     // stop the timers if necessary
     if (plugin.isTimersStarted()) plugin.stopTimers();
     
     plugin.startTimers(this.delay);
     sender.sendMessage(ChatColor.GREEN + this.getFormattedTimerStartMessage());  
   }
 
   public String getFormattedTimerStartMessage() {
     Object[] arguments = {this.plugin.getTimerCount(), this.delay};
     double[] limits = {0, 1, 2};
     String[] formats = {this.getMessage("no-timers"), this.getMessage("one-timer"), this.getMessage("many-timers")};
     return this.getChoiceFormattedMessage("timers-started", arguments, formats, limits);
   }
   
   private void registerPermissions() {
     final String prefix = this.plugin.getDescription().getName().toLowerCase() + ".";
     // create the base permission
     final Permission base = new Permission(prefix + this.getName(), this.plugin.getMessage("startcommand-permission-description"), PermissionDefault.OP);
     base.addParent(this.plugin.getRootPermission(), true);
     this.addPermission(base);
   }
   
   public void parseArguments(String[] arguments, CommandSender sender) throws name.richardson.james.bukkit.utilities.command.CommandArgumentException {
     this.delay = TimedMessages.START_DELAY;
   
     if (arguments.length >= 1) {
       try {
         this.delay = TimeFormatter.parseTime(arguments[0]);
       } catch (NumberFormatException exception) {
         throw new CommandArgumentException(this.getMessage("invalid-time"), this.getMessage("time-format-help"));
       }
    } else if (this.delay == 0) {
      throw new CommandArgumentException(this.getMessage("invalid-time"), this.getMessage("time-format-help"));
     }
     
   }
 
 }
