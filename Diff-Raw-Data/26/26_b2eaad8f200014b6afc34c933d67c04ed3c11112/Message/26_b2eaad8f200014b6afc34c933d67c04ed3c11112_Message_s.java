 /*******************************************************************************
  * Copyright (c) 2011 James Richardson.
  * 
  * Message.java is part of TimedMessages.
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
 
 package name.richardson.james.bukkit.timedmessages;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import name.richardson.james.bukkit.utilities.formatters.ColourFormatter;
 import name.richardson.james.bukkit.utilities.permissions.PermissionManager;
 
 public abstract class Message implements Runnable {
   
   protected final List<String> messages;
 
   private final Long ticks;
   private final String permission;
   private final Server server;
 
   private final PermissionManager permissionManager;
 
   private final Set<String> worlds = new HashSet<String>();
   private final Set<String> regions = new HashSet<String>();
 
   public Message(final TimedMessages plugin, final Server server, final Long milliseconds, final List<String> messages, final String permission, final List<String> worlds, List<String> regions) {
     final long seconds = milliseconds / 1000;
     this.ticks = seconds * 20;
     this.messages = messages;
     this.permission = permission;
     this.permissionManager = plugin.getPermissionManager();
     this.server = server;
     this.worlds.addAll(worlds);
     this.regions.addAll(regions);
   }
 
   public List<String> getMessages() {
     return this.messages;
   }
 
   public String getPermission() {
     return this.permission;
   }
 
   public Long getTicks() {
     return this.ticks;
   }
 
   public void run() {
     String message = this.getNextMessage();
     message = ColourFormatter.replace("&", message);
     final String[] parts = message.split("/n");
     final List<Player> players = new LinkedList<Player>();
     final List<String> worldNames = this.getLoadedWorldNames();
     
     for (final Player player : this.server.getOnlinePlayers()) {
       // ignore the player if they are not in the world required
       if (!worldNames.contains(player.getWorld().getName())) continue;    
       // ignore the player if they do not have the correct permission
       if (!this.permissionManager.hasPlayerPermission(player, this.permission)) continue;
       players.add(player);
     }
   
     if (players.isEmpty()) return;
   
     for (final String part : parts) {
       for (final Player player : players) {
         player.sendMessage(part);
       }
     }
   }
 
   private List<String> getLoadedWorldNames() {
     List<String> worldNames = new LinkedList<String>();
     for (World world : this.server.getWorlds()) {
       worldNames.add(world.getName());
     }
     return worldNames;
   }
 
 
 
   protected abstract String getNextMessage();
 
 }
