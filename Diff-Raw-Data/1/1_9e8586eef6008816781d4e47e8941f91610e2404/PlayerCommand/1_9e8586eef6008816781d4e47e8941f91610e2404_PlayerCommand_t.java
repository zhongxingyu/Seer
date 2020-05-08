 /*******************************************************************************
  * Copyright (c) 2011 James Richardson.
  * 
  * PlayerCommand.java is part of BukkitUtilities.
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
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.permissions.Permission;
 
 import name.richardson.james.bukkit.util.Logger;
 import name.richardson.james.bukkit.util.Plugin;
 
 public abstract class PlayerCommand implements Command {
 
   protected final Logger logger = new Logger(this.getClass());
   protected Map<String, Object> arguments = new HashMap<String, Object>();
   
   private String description;
   private String name;
   private Permission permission;
   private String permissionDescription;
   private String usage;
   
   public PlayerCommand(Plugin plugin, String name, String description, String usage, String permissionDescription, Permission permission) {
     this.name = name;
     this.description = description;
     this.permissionDescription = permissionDescription;
     this.usage = usage;
     this.permission = permission;
     plugin.addPermission(this.permission, true);
   }
 
   @Override
   public abstract void execute(CommandSender sender, Map<String, Object> arguments) throws CommandPermissionException, CommandUsageException;
 
   @Override
   public Map<String, Object> getArguments() {
     return Collections.unmodifiableMap(this.arguments);
   }
 
 
   /**
    * Check to see if a player has permission to use this command.
    * 
    * A console user is permitted to use all commands by default.
    * 
    * @param sender
    * The player/console that is attempting to use the command
    * @return true if the player has permission; false otherwise.
    */
   public boolean isSenderAuthorised(final CommandSender sender) {
     return sender.hasPermission(this.getPermission());
   }
 
   @Override
   public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command, final String label, final String[] args) {
     try {
       final LinkedList<String> arguments = new LinkedList<String>();
       arguments.addAll(Arrays.asList(args));
       arguments.remove(0);
       final Map<String, Object> parsedArguments = this.parseArguments(arguments);
       this.execute(sender, parsedArguments);
     } catch (final CommandPermissionException exception) {
       sender.sendMessage(ChatColor.RED + "You do not have permission to do this.");
     } catch (final CommandUsageException exception) {
       sender.sendMessage(ChatColor.RED + exception.getMessage());
     } catch (final CommandArgumentException exception) {
       sender.sendMessage(ChatColor.RED + exception.getMessage());
       sender.sendMessage(ChatColor.YELLOW + exception.getHelp());
     }
     return true;
   }
 
   @Override
   public Map<String, Object> parseArguments(final List<String> arguments) throws CommandArgumentException {
     return new HashMap<String, Object>();
   }
 
   @Override
   public void setArguments(final Map<String, Object> arguments) {
     this.arguments = arguments;
   }
   
   @Override
   public String getDescription() {
     return this.description;
   }
 
   @Override
   public String getName() {
     return this.name;
   }
 
   @Override
   public Permission getPermission() {
     return this.permission;
   }
 
   @Override
   public String getPermissionDescription() {
     return this.permissionDescription;
   }
 
   @Override
   public String getUsage() {
     return this.usage;
   }
 
 }
