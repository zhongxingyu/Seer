 /*******************************************************************************
  * Copyright (c) 2012 James Richardson.
  * 
  * TeleportCommand.java is part of Hearthstone.
  * 
  * Hearthstone is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * Hearthstone is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Hearthstone. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package name.richardson.james.hearthstone.general;
 
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 
 import name.richardson.james.bukkit.utilities.command.CommandArgumentException;
 import name.richardson.james.bukkit.utilities.command.CommandPermissionException;
 import name.richardson.james.bukkit.utilities.command.CommandUsageException;
 import name.richardson.james.bukkit.utilities.command.PluginCommand;
 import name.richardson.james.bukkit.utilities.formatters.TimeFormatter;
 import name.richardson.james.hearthstone.DatabaseHandler;
 import name.richardson.james.hearthstone.Hearthstone;
 import name.richardson.james.hearthstone.HomeRecord;
 
 public class TeleportCommand extends PluginCommand {
 
   private final Server server;
   private final DatabaseHandler database;
   private final Map<String, Long> cooldownTracker;
   
   //* The name of the player we are teleporting to *//
   private String playerName;
   
   //* The UID of the world we are teleporting to *//
   private UUID worldUUID;
   
   //* The player who is teleporting *//
   private Player player;
   
   //* The cooldown to apply to the teleporting player *//
   private long cooldown;
 
   public TeleportCommand(Hearthstone plugin) {
     super(plugin);
     this.server = plugin.getServer();
     this.database = plugin.getDatabaseHandler();
     this.cooldownTracker = plugin.getCooldownTracker();
     this.cooldown = plugin.getHearthstoneConfiguration().getCooldown();
     this.registerPermissions();
   }
 
   public void execute(CommandSender sender) throws CommandArgumentException, CommandPermissionException, CommandUsageException {
     final String senderName = sender.getName().toLowerCase();
 
     if (!isPlayerCooldownExpired() && !player.hasPermission(this.getPermission(2))) {
       throw new CommandUsageException(plugin.getSimpleFormattedMessage("cooldown-not-expired", TimeFormatter.millisToLongDHMS(cooldownTracker.get(senderName))));
     }
     
     if (sender.hasPermission(this.getPermission(1)) && senderName.equalsIgnoreCase(playerName)) {
       teleportPlayer();
       return;
     } else if (sender.hasPermission(this.getPermission(3)) && !senderName.equalsIgnoreCase(playerName)) {
       teleportPlayer();
       sender.sendMessage(String.format(ChatColor.GREEN + this.plugin.getSimpleFormattedMessage("teleported-home", playerName)));
     } else {
       throw new CommandPermissionException(plugin.getMessage("can-only-teleport-to-own-home"), this.getPermission(3));
     }
 
   }
 
   private boolean isPlayerCooldownExpired() {
     
     if (!cooldownTracker.containsKey(playerName)) return true;
     
     final String playerName = player.getName().toLowerCase();
     final long cooldown = cooldownTracker.get(playerName);
     
     if ((System.currentTimeMillis() - cooldown) > 0) return false;
     
     cooldownTracker.remove(playerName);
     return true;
       
   }
 
   private void teleportPlayer() throws CommandUsageException {
     List<HomeRecord> homes = database.findHomeRecordsByOwnerAndWorld(playerName, worldUUID);
     if (!homes.isEmpty()) {
      cooldownTracker.put(playerName, cooldown);
       player.teleport(homes.get(0).getLocation(server));
     } else {
       throw new CommandUsageException(plugin.getSimpleFormattedMessage("no-home-set", playerName));
     }
   }
 
   private String matchPlayerName(String playerName) {
     List<Player> matches = this.server.matchPlayer(playerName);
     if (matches.isEmpty()) {
       return playerName;
     } else {
       return matches.get(0).getName();
     }
   }
 
   private void registerPermissions() {
     final String prefix = plugin.getDescription().getName().toLowerCase() + ".";
     final String wildcardDescription = String.format(plugin.getMessage("wildcard-permission-description"), this.getName());
     // create the wildcard permission
     Permission wildcard = new Permission(prefix + this.getName() + ".*", wildcardDescription, PermissionDefault.OP);
     wildcard.addParent(plugin.getRootPermission(), true);
     this.addPermission(wildcard);
     // create the base permission
     Permission base = new Permission(prefix + this.getName(), plugin.getMessage("teleportcommand-permission-description"), PermissionDefault.TRUE);
     base.addParent(wildcard, true);
     this.addPermission(base);
     // create a permission to allow players to ignore the cooldown
     Permission cooldown = new Permission(prefix + this.getName() + "." + plugin.getMessage("teleportcommand-ignore-cooldown-permission-name"), plugin.getMessage("teleportcommand-ignore-cooldown-permission-description"), PermissionDefault.OP);
     cooldown.addParent(wildcard, true);
     this.addPermission(cooldown);
     // create permission to enable players to teleport to other others
     Permission others = new Permission(prefix + this.getName() + "." + plugin.getMessage("teleportcommand-others-permission-name"), plugin.getMessage("teleportcommand-others-permission-description"), PermissionDefault.OP);
     others.addParent(wildcard, true);
     this.addPermission(others);
   }
   
   private UUID getWorldUUID(String worldName) throws CommandArgumentException {
     World world = server.getWorld(worldName);
     if (world != null) {
       return world.getUID();
     } else {
       throw new CommandArgumentException(this.getMessage("invalid-world"), this.getMessage("world-must-be-loaded"));
     }
   }
   
   public void parseArguments(String[] arguments, CommandSender sender) throws CommandArgumentException {
     this.player = (Player) sender;
     
     if (arguments.length == 0) {
       this.playerName = player.getName();
       this.worldUUID = player.getLocation().getWorld().getUID();
     } else if (arguments.length == 2) {
       String playerName = matchPlayerName(arguments[0]);
       this.player = server.getPlayerExact(playerName);
       this.worldUUID = getWorldUUID(arguments[1]);
       if (this.worldUUID == null) throw new CommandArgumentException(this.plugin.getMessage("invalid-world"), this.plugin.getMessage("invalid-world-hint"));
     }
     
   }
 
 }
