 /*
  * Copyright (C) 2013 AE97
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.ae97.totalpermissions;
 
 import java.io.File;
 import java.io.IOException;
 import net.ae97.totalpermissions.permission.PermissionConsole;
 import net.ae97.totalpermissions.permission.PermissionGroup;
 import net.ae97.totalpermissions.permission.PermissionRcon;
 import net.ae97.totalpermissions.permission.PermissionUser;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import net.ae97.totalpermissions.permission.PermissionBase;
 import net.ae97.totalpermissions.permission.PermissionOp;
 import net.ae97.totalpermissions.permission.PermissionType;
 import org.bukkit.Bukkit;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionAttachment;
 
 /**
  * @version 0.2
  * @author Lord_Ralex
  * @since 0.1
  */
 public final class PermissionManager {
 
     protected final Map<String, PermissionGroup> groups = new ConcurrentHashMap<String, PermissionGroup>();
     protected final Map<String, PermissionUser> users = new ConcurrentHashMap<String, PermissionUser>();
     protected final Map<String, PermissionAttachment> permAttMap = new ConcurrentHashMap<String, PermissionAttachment>();
     protected final Map<String, Map<String, Permission>> permissions = new ConcurrentHashMap<String, Map<String, Permission>>();
     protected String defaultGroup;
     protected PermissionConsole console;
     protected PermissionRcon remote;
     protected final TotalPermissions plugin;
     protected PermissionOp op;
 
     public PermissionManager(TotalPermissions p) {
         plugin = p;
     }
 
     /**
      * Loads the permissions from the permissions file
      *
      * @throws InvalidConfigurationException If the file is not in a valid
      * configuration layout
      *
      * @since 0.1
      */
     public void load() throws InvalidConfigurationException {
         FileConfiguration perms = plugin.getPermFile();
         ConfigurationSection filegroups = perms.getConfigurationSection("groups");
         if (groups == null) {
             throw new InvalidConfigurationException(plugin.getLangFile().getString("manager.null-group"));
         }
         Set<String> allGroups = filegroups.getKeys(false);
         for (String group : allGroups) {
             plugin.debugLog("Adding group: " + group);
             PermissionGroup temp = new PermissionGroup(group);
             groups.put(group, temp);
             if (temp.isDefault()) {
                 plugin.debugLog("Setting group to be default: " + temp.getName());
                 defaultGroup = temp.getName();
             }
         }
         if (defaultGroup == null) {
             throw new InvalidConfigurationException(plugin.getLangFile().getString("manager.null-default"));
         }
         plugin.debugLog("Adding special users");
         console = new PermissionConsole();
         remote = new PermissionRcon();
         op = new PermissionOp();
         permAttMap.put("console", console.setPerms(Bukkit.getConsoleSender(), null, null));
     }
 
     /**
      * Unloads the permissions.
      *
      * @since 0.1
      */
     public synchronized void unload() {
         plugin.debugLog("Unloading permission manager");
         users.clear();
         groups.clear();
         clearPerms();
         clearRegisteredPerms();
     }
 
     /**
      * Get the name of the default group.
      *
      * @return Gets the name of the default group
      */
     public synchronized String getDefaultGroup() {
         return defaultGroup;
     }
 
     /**
      * Gets the {@link PermissionConsole}. This is the console's perms.
      *
      * @return The {@link PermissionConsole} for the console
      *
      * @since 0.1
      */
     public synchronized PermissionConsole getConsole() {
         return console;
     }
 
     /**
      * Gets the {@link PermissionRcon}. This is the rcon's perms.
      *
      * @return The {@link PermissionRcon} for the console
      *
      * @since 0.1
      */
     public synchronized PermissionRcon getRcon() {
         return remote;
     }
 
     /**
      * Gets the {@link PermissionOp}. This has the Op's perms
      *
      * @return The {@link PermissionOp} loaded
      *
      * @since 0.2
      */
     public synchronized PermissionOp getOP() {
         return op;
     }
 
     /**
      * Loads a player's {@link PermissionUser} object. This can return the one
      * that is stored in the cache if the player is online or has joined and
      * went offline.
      *
      * @param player The player's name to load
      * @return The {@link PermissionUser} for that player
      *
      * @since 0.1
      */
     public PermissionUser getUser(String player) {
         PermissionUser user;
         synchronized (users) {
             user = users.get(player.toLowerCase());
             if (user == null) {
                 user = new PermissionUser(player.toLowerCase());
             }
             users.put(player.toLowerCase(), user);
         }
         return user;
     }
 
     /**
      * Loads a player's {@link PermissionUser} object. This can return the one
      * that is stored in the cache if the player is online or has joined and
      * went offline.
      *
      * @param player The player's name to load
      * @return The {@link PermissionUser} for that player
      *
      * @since 0.1
      */
     public PermissionUser getUser(Player player) {
         return getUser(player.getName());
     }
 
     /**
      * Loads a group's {@link PermissionGroup} object. This can return the one
      * that is stored in the cache if there is one there.
      *
      * @param name The player's name to load
      * @return The {@link PermissionGroup} for that group
      *
      * @since 0.1
      */
     public PermissionGroup getGroup(String name) {
         PermissionGroup group;
         synchronized (groups) {
             group = groups.get(name.toLowerCase());
             if (group == null) {
                 group = new PermissionGroup(name.toLowerCase());
             }
             groups.put(name.toLowerCase(), group);
         }
         return group;
     }
 
     /**
      * Returns a raw String array of all groups by name. If null, returns
      * "default" as a group. Capable of grabbing cached groups.
      *
      * @return A String array of all groups
      *
      * @since 0.1
      */
     public synchronized String[] getGroups() {
         String[] group;
         synchronized (groups) {
             group = groups.keySet().toArray(new String[groups.size()]);
             if (group == null) {
                 group = new String[]{"default"};
             }
         }
         return group;
     }
 
     /**
      * Returns a sorted form of getGroups()
      *
      * @return A sorted String array of all groups
      *
      * @since 0.1
      */
     public synchronized String[] getSortedGroups() {
         String[] temp = getGroups();
         Arrays.sort(temp);
         return temp;
     }
 
     /**
      * Checks to see if the given CommandSender has the specified perm
      *
      * @param player The CommandSender to check perms for
      * @param perm The permission to check for
      * @return True if the sender has been given this perm, false otherwise
      *
      * @since 0.1
      */
     public synchronized boolean has(CommandSender player, String perm) {
         return has(player.getName(), perm);
     }
 
     /**
      * Checks to see if the given name has the specified perm
      *
      * @param player The name to check perms for
      * @param perm The permission to check for
      * @return True if they been given this perm, false otherwise
      *
      * @since 0.1
      */
     public synchronized boolean has(String player, String perm) {
         PermissionUser user = getUser(player);
         return user.has(perm);
     }
 
     /**
      * Checks to see if a given group has a certain permission.
      *
      * @param groupName Name of group
      * @param perm Permission to check for
      * @return True if group has permission, false otherwise
      *
      * @since 0.2
      */
     public synchronized boolean groupHas(String groupName, String perm) {
         PermissionGroup group = getGroup(groupName);
         return group.getPerms().containsKey(perm);
     }
 
     /**
      * Adds a permission to a player.
      *
      * @param player Player to give permission to
      * @param world World to give it in, null is for global
      * @param perm Permission to give
      * @param allowance Whether to allow or deny this permission
      * @throws IOException If an error occurs on saving
      *
      * @since 0.2
      */
     public synchronized void addPerm(Player player, String world, String perm, boolean allowance) throws IOException {
         PermissionUser user = getUser(player);
         if (!allowance) {
             perm = "-" + perm;
         }
         if (allowance) {
             user.addPerm(world, perm);
         } else {
             user.remPerm(world, perm);
         }
     }
 
     /**
      * Adds a permission to an offline player.
      *
      * @param player OfflinePlayer to give permission to
      * @param world World to give it in, null is for global
      * @param perm Permission to give
      * @param allowance Whether to allow or deny this permission
      * @throws IOException If an error occurs on saving
      *
      * @since 0.2
      */
     public synchronized void addPerm(OfflinePlayer player, String world, String perm, boolean allowance) throws IOException {
         PermissionUser user = getUser(player.getName());
         if (allowance) {
             user.addPerm(world, perm);
         } else {
             user.remPerm(world, perm);
 
         }
     }
 
     /**
      * Adds a permission to a group.
      *
      * @param group Player to give permission to
      * @param world World to give it in, null is for global
      * @param perm Permission to give
      * @param allowance Whether to allow or deny this permission
      * @throws IOException If an error occurs on saving
      *
      * @since 0.2
      */
     public synchronized void addPermToGroup(String group, String world, String perm, boolean allowance) throws IOException {
         PermissionGroup gr = getGroup(group);
         if (allowance) {
             gr.addPerm(world, perm);
         } else {
             gr.remPerm(world, perm);
 
         }
     }
 
     /**
      * Adds a permission to a user.
      *
      * @param user User to give permission to
      * @param world World to give it in, null is for global
      * @param perm Permission to give
      * @param allowance Whether to allow or deny this permission
      * @throws IOException If an error occurs on saving
      *
      * @since 0.2
      */
     public synchronized void addPermToUser(String user, String world, String perm, boolean allowance) throws IOException {
         PermissionUser gr = getUser(user);
         if (allowance) {
             gr.addPerm(perm, world);
         } else {
             gr.remPerm(perm, world);
 
         }
     }
 
     public void handleLoginEvent(PlayerLoginEvent event) {
         plugin.debugLog("Manager handling PlayerLoginEvent " + event.getPlayer().getName());
         PermissionUser user = getUser(event.getPlayer());
         PermissionAttachment att = user.setPerms(event.getPlayer(), getAttachment(event.getPlayer()), null);
         permAttMap.put(event.getPlayer().getName(), att);
     }
 
     public void handleLogoutEvent(PlayerQuitEvent event) {
         plugin.debugLog("Manager handling PlayerQuitEvent " + event.getPlayer().getName());
         PermissionAttachment att = permAttMap.remove(event.getPlayer().getName());
         if (att != null) {
             try {
                 event.getPlayer().removeAttachment(att);
             } catch (IllegalArgumentException e) {
             }
         }
     }
 
     public synchronized PermissionAttachment getAttachment(String player) {
         return permAttMap.get(player);
     }
 
     public synchronized PermissionAttachment getAttachment(Player player) {
         return getAttachment(player.getName());
     }
 
     /**
      * Clears the permissions out.
      */
     public synchronized void clearPerms() {
         plugin.debugLog("Clearing perm attachments from manager");
         for (String name : permAttMap.keySet()) {
             Player player = Bukkit.getPlayerExact(name);
             if (player == null) {
                 continue;
             }
             try {
                 player.removeAttachment(permAttMap.get(name));
             } catch (IllegalArgumentException e) {
             }
         }
         permAttMap.clear();
     }
 
     /**
      * Clears the permissions for the player given. The name must match exactly.
      *
      * @param name Name of player to clear perms from
      *
      * @since 0.2
      */
     public synchronized void clearPerms(String name) {
         plugin.debugLog("Clearing perms for name: " + name);
         clearPerms(Bukkit.getPlayerExact(name));
     }
 
     /**
      * Clears the permissions for the player given.
      *
      * @param player Player to clear perms from
      *
      * @since 0.2
      */
     public synchronized void clearPerms(Player player) {
         plugin.debugLog("Clearing perm attachments for player: " + player.getName());
         try {
             player.removeAttachment(permAttMap.remove(player.getName()));
         } catch (IllegalArgumentException e) {
         } catch (NullPointerException e) {
         }
     }
 
     /**
      * Recalculates permissions for all players on the server.
      */
     public synchronized void recalculatePermissions() {
         plugin.debugLog("Recalculating permissions for all players");
         clearPerms();
         for (Player player : Bukkit.getOnlinePlayers()) {
            handleLogoutEvent(new PlayerQuitEvent(player, null));
             handleLoginEvent(new PlayerLoginEvent(player, null, null, null, null));
         }
     }
 
     /**
      * Recalculates the permissions for the given player
      *
      * @param user Player to recalculate perms for
      *
      * @since 0.1
      */
     public synchronized void recalculatePermissions(Player user) {
         plugin.debugLog("Recalculating permissions for player " + user.getName());
         clearPerms(user);
         handleLoginEvent(new PlayerLoginEvent(user, null, null, null, null));
     }
 
     public synchronized void addPermissionToMap(String org, String key, Permission perm) {
         plugin.debugLog("Adding " + org + " with key " + key + " with permission " + perm.getName() + " to the map");
         Map<String, Permission> map = permissions.get(org);
         if (map == null) {
             map = new HashMap<String, Permission>();
         }
         map.put(key, perm);
         permissions.put(org, map);
     }
 
     /**
      * Removes the permissions this object has made
      *
      * @since 0.2
      */
     public synchronized void clearRegisteredPerms() {
         plugin.debugLog("Removing registered perms completely");
         for (String org : permissions.keySet()) {
             Map<String, Permission> map = permissions.get(org);
             if (map != null) {
                 for (String name : map.keySet().toArray(new String[0])) {
                     Permission perm = map.get(name);
                     if (perm != null) {
                         Bukkit.getPluginManager().removePermission(perm);
                     }
                     map.remove(name);
                 }
             }
             permissions.remove(org);
         }
     }
 
     /**
      * Saves this PermissionBase to the permissions file and writes changes
      *
      * @param base Base to change changes for
      * @throws IOException If an error occurs on save
      *
      * @since 0.2
      */
     public synchronized void save(PermissionBase base) throws IOException {
         plugin.debugLog("Saving PermissionBase " + base.getName() + " to the perm file");
         FileConfiguration file = plugin.getPermFile();
         PermissionType type = base.getType();
         file.set(type + "." + base.getName(), base.getConfigSection());
         save();
     }
 
     /**
      * Saves the entire permission file
      *
      * @throws IOException If an error occurs on save
      *
      * @since 0.2
      */
     private synchronized void save() throws IOException {
         plugin.debugLog("Saving permissions file");
         plugin.getPermFile().save(new File(plugin.getDataFolder(), "permissions.yml"));
     }
 
     /**
      * Changes the default group to another group. This will not error if the
      * new group does not exist.
      *
      * @param newDefault New group to change to
      * @throws IOException If an error occurs on save
      *
      * @since 0.2
      */
     public synchronized void changeDefaultGroup(String newDefault) throws IOException {
         plugin.debugLog("Changing default group to " + newDefault);
         PermissionGroup old = getGroup(defaultGroup);
         old.getConfigSection().set("default", false);
         this.defaultGroup = newDefault;
         PermissionGroup newDef = getGroup(defaultGroup);
         newDef.getConfigSection().set("default", true);
         save(old);
         save(newDef);
     }
 }
