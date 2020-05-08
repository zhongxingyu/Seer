 /*******************************************************************************
  * Copyright (c) 2011 James Richardson.
  * 
  * ReloadCommand.java is part of TimedMessages.
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
 
 import java.io.IOException;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 
 import name.richardson.james.bukkit.timedmessages.TimedMessages;
 import name.richardson.james.bukkit.utilities.command.CommandArgumentException;
 import name.richardson.james.bukkit.utilities.command.CommandPermissionException;
 import name.richardson.james.bukkit.utilities.command.CommandUsageException;
 import name.richardson.james.bukkit.utilities.command.ConsoleCommand;
 import name.richardson.james.bukkit.utilities.command.PluginCommand;
 
 @ConsoleCommand
 public class ReloadCommand extends PluginCommand {
 
   private final TimedMessages plugin;
 
   public ReloadCommand(final TimedMessages plugin) {
     super(plugin);
     this.plugin = plugin;
     this.registerPermissions();
   }
 
   public void execute(final CommandSender sender) throws CommandArgumentException, CommandPermissionException, CommandUsageException {
 
     try {
       this.plugin.loadMessagesConfiguration();
     } catch (final IOException e) {
       throw new CommandUsageException(this.plugin.getMessage("panic"));
     }
 
     sender.sendMessage(this.getSimpleFormattedMessage("plugin-reloaded", this.plugin.getDescription().getName()));
 
   }
 
   public void parseArguments(final String[] arguments, final CommandSender sender) throws CommandArgumentException {
     return;
   }
 
   private void registerPermissions() {
     final String prefix = this.plugin.getDescription().getName().toLowerCase() + ".";
     // create the base permission
    final Permission base = new Permission(prefix + this.getName(), this.getMessage("reloadcommand-permission-description"), PermissionDefault.OP);
     base.addParent(this.plugin.getRootPermission(), true);
     this.addPermission(base);
   }
 
 }
