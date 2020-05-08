 /*******************************************************************************
  * Copyright (c) 2012 James Richardson.
  * 
  * SetCommand.java is part of Hearthstone.
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
 
 import com.sk89q.worldguard.protection.GlobalRegionManager;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 
 import name.richardson.james.bukkit.utilities.command.CommandArgumentException;
 import name.richardson.james.bukkit.utilities.command.CommandPermissionException;
 import name.richardson.james.bukkit.utilities.command.CommandUsageException;
 import name.richardson.james.bukkit.utilities.command.PluginCommand;
 import name.richardson.james.hearthstone.DatabaseHandler;
 import name.richardson.james.hearthstone.Hearthstone;
 import name.richardson.james.hearthstone.HomeRecord;
 
 public class SetCommand extends PluginCommand {
 
   private final Server server;
   private final DatabaseHandler database;
   
   /** The name of the players home we are setting */
   private String playerName;
 
   /** The location of the home */
   private Location location;
   
   private final GlobalRegionManager manager;
   
   /** The player who is setting the home */
   private Player player;
   
   public SetCommand(Hearthstone plugin) {
     super(plugin);
     this.manager = plugin.getGlobalRegionManager();
     this.server = plugin.getServer();
     this.database = plugin.getDatabaseHandler();
     this.registerPermissions();
   }
 
   private void createHome() throws CommandUsageException {
     // check if location is obstructed
     if (isLocationObstructed()) throw new CommandUsageException(this.plugin.getMessage("location-is-obstructed"));
     // check if the location is buildable
     if (!isPlayerAllowedToBuild()) throw new CommandUsageException(this.plugin.getMessage("not-allowed-to-build-here"));
     // delete any existing homes
     database.deleteHomes(playerName, location.getWorld().getUID());
     // create the home
     final HomeRecord record = new HomeRecord();
     record.setCreatedAt(System.currentTimeMillis());
     record.setCreatedBy(playerName);
     record.setX(location.getX());
     record.setY(location.getY());
     record.setZ(location.getZ());
     record.setYaw(location.getYaw());
     record.setPitch(location.getPitch());
     record.setWorldUUID(location.getWorld().getUID());
     database.save(record);
   }
 
   public void execute(CommandSender sender) throws CommandArgumentException, CommandUsageException, CommandPermissionException {
     this.location = ((Player) sender).getLocation(); 
     
     if (sender.hasPermission(this.getPermission(1)) && this.playerName.equalsIgnoreCase(sender.getName())) {
       this.createHome();
       sender.sendMessage(ChatColor.GREEN + this.plugin.getMessage("home-set"));
       return;
     } else if (this.playerName.equalsIgnoreCase(sender.getName())) {
       throw new CommandPermissionException(null, this.getPermission(1));
     }
     
     if (sender.hasPermission(this.getPermission(2)) && !this.playerName.equalsIgnoreCase(sender.getName())) {
       this.createHome();
       sender.sendMessage(ChatColor.GREEN + this.plugin.getSimpleFormattedMessage("home-set-others", this.playerName));
     } else if (!this.playerName.equalsIgnoreCase(sender.getName())) {
       throw new CommandPermissionException(null, this.getPermission(2));
     }
 
   }
 
   private boolean isLocationObstructed() {
    Location location = this.location.clone();
    // check the block that the player's legs occupy.
    if (!location.getBlock().isEmpty()) return true;
    // check the block that the player's head occupies.
    if (!location.add(0, 1, 0).getBlock().isEmpty()) return true;
     return false;
   }
 
   private boolean isPlayerAllowedToBuild() {
     if (this.manager != null) {
       return manager.canBuild(this.player, this.location);
     } else {
       return true;
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
     Permission base = new Permission(prefix + this.getName(), plugin.getMessage("setcommand-permission-description"), PermissionDefault.TRUE);
     base.addParent(wildcard, true);
     this.addPermission(base);
     // add ability to set other user's homes
     Permission others = new Permission(prefix + this.getName() + "." + plugin.getMessage("setcommand-others-permission-name"), plugin.getMessage("setcommand-others-permission-description"), PermissionDefault.OP);
     others.addParent(wildcard, true);
     this.addPermission(others);
   }
 
   
   public void parseArguments(String[] arguments, CommandSender sender) throws CommandArgumentException {
     this.player = (Player) sender;
     
     if (arguments.length == 0) {
       this.playerName = sender.getName(); 
     } else {
       this.playerName = matchPlayerName(arguments[0]);
     }
   }
 
   
 
 
 }
