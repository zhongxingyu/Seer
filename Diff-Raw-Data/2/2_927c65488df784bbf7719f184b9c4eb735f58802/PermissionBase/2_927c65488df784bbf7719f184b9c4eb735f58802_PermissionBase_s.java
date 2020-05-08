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
 package net.ae97.totalpermissions.permission;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import net.ae97.totalpermissions.TotalPermissions;
 import net.ae97.totalpermissions.permission.util.PermissionUtility;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionAttachment;
 
 /**
  * Loads all permissions for every type of setup. This may load and try to find
  * excess information, such as inheritance in users.
  *
  * @since 0.1
  * @author Lord_Ralex
  */
 public abstract class PermissionBase {
 
     protected final String name;
     protected final Map<String, Object> options = new HashMap<String, Object>();
     protected final ConfigurationSection section;
     protected final Map<String, Permission> perms = new HashMap<String, Permission>();
     protected final List<String> inherited = new ArrayList<String>();
     protected final PermissionType permType;
     protected final Permission permission;
 
     public PermissionBase(PermissionType type, String aName) {
         TotalPermissions plugin = TotalPermissions.getPlugin();
         plugin.debugLog("Creating new Base: " + type + " " + aName);
         name = aName;
         if (type == null) {
             throw new IllegalArgumentException();
         }
         permType = type;
         if (!plugin.getPermFile().contains(permType + "." + name)) {
             plugin.debugLog("Section " + permType + "." + name + " does not exist, creating");
             plugin.getPermFile().createSection(permType + "." + name);
         }
         permission = new Permission("totalpermissions.baseItem." + permType + "." + name);
         plugin.debugLog("Created permission: " + permission.getName());
         section = plugin.getPermFile().getConfigurationSection(permType + "." + name);
         load();
     }
 
     protected final void load() {
         TotalPermissions plugin = TotalPermissions.getPlugin();
        plugin.debugLog("Loading base:" + permType + " " + name);
         options.clear();
         plugin.debugLog("Clearing out old permissions");
         for (String key : perms.keySet()) {
             Permission p = perms.get(key);
             if (Bukkit.getPluginManager().getPermission(p.getName()) != null) {
                 Bukkit.getPluginManager().removePermission(p.getName());
             }
         }
         perms.clear();
         inherited.clear();
         Map<String, Boolean> permMap = new HashMap<String, Boolean>();
         if (section != null) {
             if (section.isList("permissions")) {
                 List<String> permList = section.getStringList("permissions");
                 if (permList != null) {
                     for (String perm : permList) {
                         plugin.debugLog("Permission: " + perm);
                         String p = perm;
                         boolean allow = true;
                         if (perm.startsWith("-") || perm.startsWith("^")) {
                             p = perm.substring(1);
                             allow = false;
                         } else if (perm.endsWith(": true") || perm.endsWith(": false")) {
                             if (perm.endsWith(": true")) {
                                 perm = perm.substring(0, perm.length() - ": true".length());
                                 allow = true;
                             } else if (perm.endsWith(": false")) {
                                 perm = perm.substring(0, perm.length() - ": false".length());
                                 allow = false;
                             }
                         }
                         if ((!TotalPermissions.getPlugin().getConfiguration().getBoolean("reflection.starperm"))
                                 && (p.equalsIgnoreCase("*") || p.equalsIgnoreCase("**"))) {
                             List<String> allPerms = PermissionUtility.handleWildcard(p.equalsIgnoreCase("**"));
                             for (String perm_ : allPerms) {
                                 if (!permMap.containsKey(perm_)) {
                                     permMap.put(perm_, allow);
                                 }
                             }
                         } else if (!permMap.containsKey(p)) {
                             plugin.debugLog("  Added to map with " + allow);
                             permMap.put(p, allow);
                         }
                     }
                 }
             } else if (section.isConfigurationSection("permissions")) {
                 Set<String> keys = section.getConfigurationSection("permissions").getKeys(false);
                 for (String key : keys) {
                     plugin.debugLog("Adding permission: " + key);
                     permMap.put(key, section.getConfigurationSection("permissions").getBoolean(key, true));
                 }
             }
             List<String> inherList = section.getStringList("inheritance");
             if (inherList != null) {
                 for (String in : inherList) {
                     plugin.debugLog("Adding to inheritence: " + in);
                     inherited.add(in);
                 }
             }
             List<String> groupList = section.getStringList("groups");
             if (groupList != null) {
                 for (String in : groupList) {
                     plugin.debugLog("Adding to groups: " + in);
                     inherited.add(in);
                 }
             }
             List<String> groupList2 = section.getStringList("group");
             if (groupList2 != null) {
                 for (String in : groupList2) {
                     plugin.debugLog("Adding to groups: " + in);
                     inherited.add(in);
                 }
             }
             ConfigurationSection optionSec = section.getConfigurationSection("options");
             if (optionSec != null) {
                 Set<String> optionsList = optionSec.getKeys(true);
                 for (String option : optionsList) {
                     plugin.debugLog("Adding to options: " + option + " " + optionSec.get(option));
                     options.put(option, optionSec.get(option));
                 }
             }
             ConfigurationSection worldSec = section.getConfigurationSection("worlds");
             if (worldSec != null) {
                 Set<String> worldList = worldSec.getKeys(false);
                 for (String world : worldList) {
                     plugin.debugLog("Adding in world perms for world: " + world);
                     ConfigurationSection tempSection = worldSec.getConfigurationSection(world);
                     List<String> tempWorldPerms = tempSection.getStringList("permissions");
                     for (String perm : tempWorldPerms) {
                         plugin.debugLog("Adding: " + perm);
                         addPermission(perm, world);
                     }
                 }
             }
             List<String> commandList = section.getStringList("commands");
             if (commandList != null) {
                 for (String command : commandList) {
                     plugin.debugLog("Adding command: " + command);
                     boolean allow = true;
                     if (command.startsWith("-")) {
                         command = command.substring(1).trim();
                         allow = false;
                     }
                     PluginCommand cmd = Bukkit.getPluginCommand(command);
                     if (cmd == null) {
                         //removes a trailing / if possible
                         if (command.startsWith("/")) {
                             command = command.substring(1);
                             cmd = Bukkit.getPluginCommand(command);
                             if (cmd == null) {
                                 continue;
                             }
                         } else {
                             continue;
                         }
                     }
                     String p = cmd.getPermission();
                     if (!permMap.containsKey(p)) {
                         permMap.put(p, allow);
                     }
                 }
             }
         }
         permission.getChildren().clear();
         permission.getChildren().putAll(permMap);
         if (Bukkit.getPluginManager().getPermission(permission.getName()) != null) {
             Bukkit.getPluginManager().removePermission(permission.getName());
         }
         Bukkit.getPluginManager().addPermission(permission);
         plugin.getManager().addPermissionToMap(permType.toString(), name, permission);
         perms.put(null, permission);
     }
 
     /**
      * Gets a list of the permissions for this group that are global. This
      * includes inherited perms. Negative perms start with a '-'.
      *
      * @return List of permissions with - in front of negative nodes
      *
      * @since 0.1
      */
     public synchronized Map<String, Boolean> getPerms() {
         return getPerms(null);
     }
 
     /**
      * Gets a list of the permissions for this group in the given world. This
      * includes inherited perms. Negative perms start with a '-'.
      *
      * @param world World to get perms
      *
      * @return List of permissions with - in front of negative nodes
      *
      * @since 0.1
      */
     public synchronized Map<String, Boolean> getPerms(String world) {
         Map<String, Boolean> permList = new HashMap<String, Boolean>();
         Permission global = perms.get(null);
         if (global != null) {
             if (global.getChildren() != null) {
                 permList.putAll(global.getChildren());
             }
         }
         if (world != null) {
             Permission children = perms.get(world);
             if (children != null) {
                 if (children.getChildren() != null) {
                     permList.putAll(children.getChildren());
                 }
             }
         }
         return permList;
     }
 
     /**
      * Gets an option for the group. This is what is stored in the options:
      * section of the permissions in the groups
      *
      * @param key Path to option
      * @return Value of that option, or null if no option
      *
      * @since 0.1
      */
     public Object getOption(String key) {
         return options.get(key);
     }
 
     /**
      * Returns the options for this PermissionHolder.
      *
      * @return Map of the options for this holder
      * @since 0.2
      */
     public Map<String, Object> getOptions() {
         return options;
     }
 
     /**
      * Get the name of this permission holder.
      *
      * @return Name of permission holder
      *
      * @since 0.1
      */
     public String getName() {
         return name;
     }
 
     @Override
     public boolean equals(Object val) {
         if (val instanceof PermissionBase) {
             if (((PermissionBase) val).getName().equalsIgnoreCase(name)) {
                 return true;
             } else {
                 return false;
             }
         } else {
             return super.equals(val);
         }
     }
 
     /**
      * Add a permission node to the group. This will apply for adding negative
      * nodes too.
      *
      * @param perm Perm to add to this group
      *
      * @since 0.1
      */
     protected final synchronized void addPermission(String perm) {
         addPermission(perm, null);
     }
 
     /**
      * Add a permission node to the group to this world. This will apply for
      * adding negative nodes too.
      *
      * @param perm Perm to add to this group
      * @param world World to have this perm affect
      *
      * @since 0.1
      */
     protected final synchronized void addPermission(String perm, String world) {
         String p = perm;
         boolean allow = true;
         if (p.startsWith("-")) {
             p = p.substring(0);
             allow = false;
         }
         addPermission(p, world, allow);
     }
 
     /**
      * Adds a permission node to the permission holder with this world. This
      * will accept null for the world
      *
      * @param perm Permission to add
      * @param world World to add to, or null for global
      * @param allow Whether to allow or deny the permission
      *
      * @since 0.1
      */
     protected final synchronized void addPermission(String perm, String world, boolean allow) {
         Permission pr = perms.get(world);
         if (pr == null) {
             if (world != null) {
                 pr = new Permission("totalpermissions.baseItem." + permType + "." + name + ".worlds.permissions." + world);
             } else {
                 pr = new Permission("totalpermissions.baseItem." + permType + "." + name);
             }
         }
         Map<String, Boolean> permList = pr.getChildren();
         if (permList == null) {
             permList = new HashMap<String, Boolean>();
         }
         if (!TotalPermissions.getPlugin().getConfiguration().getBoolean("reflection.starperm")) {
             if (perm.equals("**")) {
                 List<String> allPerms = PermissionUtility.handleWildcard(true);
                 for (String perm_ : allPerms) {
                     if (!permList.containsKey(perm_)) {
                         permList.put(perm_, Boolean.TRUE);
                     }
                 }
             } else if (perm.equals("*")) {
                 List<String> allPerms = PermissionUtility.handleWildcard(false);
                 for (String perm_ : allPerms) {
                     if (!permList.containsKey(perm_)) {
                         permList.put(perm_, Boolean.TRUE);
                     }
                 }
             }
         }
         permList.put(perm, allow);
         pr.getChildren().clear();
         pr.getChildren().putAll(permList);
         perms.put(world, pr);
     }
 
     /**
      * Checks to see if permission is given. This only checks the plugin-given
      * permissions
      *
      * @param perm Permission to check for
      * @return True if user/group has permission based on plugin
      *
      * @since 0.1
      */
     public synchronized boolean has(String perm) {
         return has(perm, null);
     }
 
     /**
      * Checks to see if permission is given. This only checks the plugin-given
      * permissions
      *
      * @param perm Permission to check for
      * @param world The world to check in
      * @return True if user/group has permission based on plugin
      *
      * @since 0.1
      */
     public synchronized boolean has(String perm, String world) {
         Permission permList = perms.get(world);
         if (permList == null) {
             return false;
         }
         Map<String, Boolean> result = permList.getChildren();
         if (result == null) {
             return false;
         }
         if (result.get(perm) == null) {
             return false;
         }
         Boolean res = result.get(perm);
         if (res == null && world != null) {
             return has(perm, null);
         } else {
             return res;
         }
     }
 
     /**
      * Adds the permissions for this PermissionBase to the given CommandSender.
      * If the CommandSender is a Player, then the Player's world they are in is
      * also added
      *
      * @param cs CommandSender to add the permissions to
      * @param att An existing PermissionAttachment to remove
      * @param worldName World name to apply perms for
      * @return The resulting PermissionAttachment
      *
      * @since 0.1
      */
     public PermissionAttachment setPerms(CommandSender cs, PermissionAttachment att, String worldName) {
         if (att != null) {
             try {
                 cs.removeAttachment(att);
             } catch (Exception e) {
             }
         }
         PermissionAttachment attachment = cs.addAttachment(TotalPermissions.getPlugin());
         Permission mainPerm = perms.get(null);
         attachment.setPermission(mainPerm, true);
         if (cs instanceof Player) {
             Player player = (Player) cs;
             if (player.getWorld() != null) {
                 Permission worldperm = perms.get(player.getWorld().getName());
                 if (worldperm != null) {
                     attachment.setPermission(worldperm, true);
                 }
             }
         }
         Set<String> inher = getInheritances(null);
         for (String in : inher) {
             PermissionGroup group = TotalPermissions.getPlugin().getManager().getGroup(in);
             attachment.setPermission(group.perms.get(null), true);
             if (cs instanceof Player) {
                 Player player = (Player) cs;
                 if (player.getWorld() != null) {
                     Permission worldperm = group.perms.get(player.getWorld().getName());
                     if (worldperm != null) {
                         attachment.setPermission(worldperm, true);
                     }
                 }
             }
         }
         return attachment;
     }
 
     /**
      * Adds the permissions for this PermissionBase to the given CommandSender.
      * This assumes no existing PermissionAttachment exists and for the main
      * world.
      *
      * @param cs CommandSender to add the permissions to
      * @return The resulting PermissionAttachment
      *
      * @since 0.1
      */
     public PermissionAttachment setPerms(CommandSender cs) {
         return setPerms(cs, null, null);
     }
 
     /**
      * Returns a Map of all perms registered for this permission holder.
      *
      * @return Map of all perms, key being world
      *
      * @since 0.1
      */
     public Map<String, Map<String, Boolean>> getAllPerms() {
         Map<String, Map<String, Boolean>> permMap = new HashMap<String, Map<String, Boolean>>();
         synchronized (perms) {
             Set<String> keys = perms.keySet();
             for (String key : keys) {
                 permMap.put(key, getPerms(key));
             }
         }
         return permMap;
     }
 
     /**
      * Adds a group to the inheritence list for this permission holder. Note:
      * This only works with global, world is currently ignored in this version
      *
      * @param group Group to add to the inheritence
      * @param world World to apply this to, or null for global
      * @throws IOException If an error occurs on saving the file
      *
      * @since 0.2
      */
     public void addInheritance(String group, String world) throws IOException {
         List<String> existing = section.getStringList("inheritence");
         if (existing == null) {
             existing = new ArrayList<String>();
         }
         existing.add(group);
         section.set("inheritence", existing);
         TotalPermissions.getPlugin().getManager().save(this);
         load();
     }
 
     /**
      * Adds a command to the inheritence list for this permission holder. Note:
      * This only works with global, world is currently ignored in this version
      *
      * @param item Command to add to the permission holder
      * @param world World to apply this to, or null for global
      * @throws IOException If an error occurs on saving the file
      *
      * @since 0.2
      */
     public void addCommand(String item, String world) throws IOException {
         List<String> existing = section.getStringList("commands");
         if (existing == null) {
             existing = new ArrayList<String>();
         }
         existing.add(item);
         section.set("commands", existing);
         TotalPermissions.getPlugin().getManager().save(this);
         load();
     }
 
     /**
      * Adds a group to the group list for this permission holder. Note: This
      * only works with global, world is currently ignored in this version
      *
      * @param item Group to add to the group list
      * @param world World to apply this to, or null for global
      * @throws IOException If an error occurs on saving the file
      *
      * @since 0.2
      */
     public void addGroup(String item, String world) throws IOException {
         List<String> existing = section.getStringList("groups");
         if (existing == null) {
             existing = new ArrayList<String>();
         }
         existing.add(item);
         section.set("groups", existing);
         TotalPermissions.getPlugin().getManager().save(this);
         load();
     }
 
     /**
      * Adds a permission to this permission holder.
      *
      * @param item Permission to add
      * @param world World to apply this to, or null for global
      * @throws IOException If an error occurs on saving the file
      *
      * @since 0.2
      */
     public void addPerm(String item, String world) throws IOException {
         List<String> existing;
         if (world == null) {
             existing = section.getStringList("permissions");
         } else {
             existing = section.getStringList(world + ".permissions");
         }
         if (existing == null) {
             existing = new ArrayList<String>();
         }
         if (existing.contains(item)) {
         }
         if (existing.contains("-" + item)) {
             existing.remove("-" + item);
         }
         if (item.startsWith("-")) {
             String temp = item.substring(1);
             existing.remove(temp);
         }
         existing.add(item);
         if (world == null) {
             section.set("permissions", existing);
         } else {
             section.set(world + ".permissions", existing);
         }
         TotalPermissions.getPlugin().getManager().save(this);
         load();
     }
 
     /**
      * Checks to see if the given parameter in the world is a parent. Note: This
      * does not check multiworld in this version.
      *
      * @param item Item to check for
      * @param world World to check in
      * @return True if the holder inherits from this item, otherwise false
      *
      * @since 0.2
      */
     public boolean hasInheritance(String item, String world) {
         for (String listItem : inherited) {
             if (item.equalsIgnoreCase(listItem)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Checks to see if the given command in the world is a parent. Note: This
      * does not check multiworld in this version.
      *
      * @param item Item to check for
      * @param world World to check in
      * @return True if the holder has access to this command, otherwise false
      *
      * @since 0.2
      */
     public boolean hasCommand(String item, String world) {
         List<String> list = section.getStringList("commands");
         for (String listItem : list) {
             if (item.equalsIgnoreCase(listItem)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns a list of commands explicitedly given to this holder. This does
      * not include commands given by permission, only the commands listed in the
      * "commands" section of the file. Note: This does not check multiworld in
      * this version.
      *
      * @param world World to get commands from
      * @return Collection of commands specified
      *
      * @since 0.2
      */
     public Set<String> getCommands(String world) {
         List<String> list = section.getStringList("commands");
         Set<String> returned = new HashSet<String>();
         for (String item : list) {
             returned.add(item);
         }
         return returned;
     }
 
     public Set<String> getInheritances(String world) {
         Set<String> returned = new HashSet<String>();
         for (String item : inherited) {
             returned.add(item);
         }
         return returned;
     }
 
     public void remPerm(String item, String world) throws IOException {
         List<String> existing;
         if (world == null) {
             existing = section.getStringList("permissions");
         } else {
             existing = section.getStringList(world + ".permissions");
         }
         if (existing == null) {
             existing = new ArrayList<String>();
         }
         existing.remove(item);
         existing.add("-" + item);
         if (world == null) {
             section.set("permissions", existing);
         } else {
             section.set(world + ".permissions", existing);
         }
         TotalPermissions.getPlugin().getManager().save(this);
         load();
     }
 
     public void remInheritance(String item, String world) throws IOException {
         List<String> existing = section.getStringList("inheritence");
         if (existing == null) {
             existing = new ArrayList<String>();
         }
         existing.remove(item);
         section.set("inheritence", existing);
         TotalPermissions.getPlugin().getManager().save(this);
         load();
     }
 
     public void remCommand(String item, String world) throws IOException {
         List<String> existing = section.getStringList("groups");
         if (existing == null) {
             existing = new ArrayList<String>();
         }
         existing.remove(item);
         section.set("groups", existing);
         TotalPermissions.getPlugin().getManager().save(this);
         load();
     }
 
     public void remOption(String option, String world) throws IOException {
         TotalPermissions.getPlugin().getManager().save(this);
     }
 
     public void remGroup(String item, String world) throws IOException {
         List<String> existing = section.getStringList("groups");
         if (existing == null) {
             existing = new ArrayList<String>();
         }
         existing.remove(item);
         section.set("groups", existing);
         TotalPermissions.getPlugin().getManager().save(this);
         load();
     }
 
     public void setOption(String option, String item, String world) throws IOException {
         section.set("options." + option, item);
         TotalPermissions.getPlugin().getManager().save(this);
         load();
     }
 
     public PermissionType getType() {
         return permType;
     }
 
     public ConfigurationSection getConfigSection() {
         return section;
     }
 }
