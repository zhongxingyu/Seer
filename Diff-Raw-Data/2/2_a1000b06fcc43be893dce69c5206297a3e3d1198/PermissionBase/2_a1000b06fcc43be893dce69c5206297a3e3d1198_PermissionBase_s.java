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
 import java.util.Map.Entry;
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
  * @version 0.1
  */
 public abstract class PermissionBase {
 
     protected final String name;
     protected final Map<String, Object> options = new HashMap<String, Object>();
     protected final ConfigurationSection section;
     protected final Map<String, Permission> perms = new HashMap<String, Permission>();
     protected final PermissionType permType;
 
     public PermissionBase(PermissionType type, String aName) {
         name = aName;
         if (type == null) {
             throw new IllegalArgumentException();
         }
         permType = type;
         if (TotalPermissions.isDebugMode()) {
             TotalPermissions.getPlugin().getLogger().info("Adding perms for " + permType + "." + name);
         }
         section = TotalPermissions.getPlugin().getPermFile().getConfigurationSection(permType + "." + name);
         load();
     }
 
     protected final void load() {
         Map<String, Boolean> permMap = new HashMap<String, Boolean>();
         if (section != null) {
             if (section.isList("permissions")) {
                 List<String> permList = section.getStringList("permissions");
                 if (permList != null) {
                     for (String perm : permList) {
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
 
                         if (TotalPermissions.isDebugMode()) {
                             TotalPermissions.getPlugin().getLogger().info("Adding perm: " + p);
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
                             permMap.put(p, allow);
                         }
                     }
                 }
             } else {
                 Set<String> keys = section.getConfigurationSection("permissions").getKeys(false);
                 for (String key : keys) {
                     permMap.put(key, section.getConfigurationSection("permissions").getBoolean(key, true));
                 }
             }
             List<String> inherList = section.getStringList("inheritance");
             if (inherList != null) {
                 for (String tempName : inherList) {
                     Permission permtoAdd = Bukkit.getPluginManager().getPermission("totalpermissions.baseitem.groups." + tempName);
                     if (permtoAdd != null) {
                         Map<String, Boolean> children = permtoAdd.getChildren();
                         if (children != null) {
                             for (Entry<String, Boolean> p : children.entrySet()) {
                                 if (!permMap.containsKey(p.getKey())) {
                                     permMap.put(p.getKey(), p.getValue());
                                 }
                             }
                         }
                     }
                 }
             }
             List<String> groupList = section.getStringList("groups");
             if (groupList != null) {
                 for (String tempName : groupList) {
                     Permission permtoAdd = Bukkit.getPluginManager().getPermission("totalpermissions.baseitem.groups." + tempName);
                     if (permtoAdd != null) {
                         Map<String, Boolean> children = permtoAdd.getChildren();
                         if (children != null) {
                             for (Entry<String, Boolean> p : children.entrySet()) {
                                 if (!permMap.containsKey(p.getKey())) {
                                     permMap.put(p.getKey(), p.getValue());
                                 }
                             }
                         }
                     }
                 }
             }
             List<String> groupList2 = section.getStringList("group");
             if (groupList2 != null) {
                 for (String tempName : groupList2) {
                     Permission permtoAdd = Bukkit.getPluginManager().getPermission("totalpermissions.baseitem.groups." + tempName);
                     if (permtoAdd != null) {
                         Map<String, Boolean> children = permtoAdd.getChildren();
                         if (children != null) {
                             for (Entry<String, Boolean> p : children.entrySet()) {
                                 if (!permMap.containsKey(p.getKey())) {
                                     permMap.put(p.getKey(), p.getValue());
                                 }
                             }
                         }
                     }
                 }
             }
             ConfigurationSection optionSec = section.getConfigurationSection("options");
             if (optionSec != null) {
                 Set<String> optionsList = optionSec.getKeys(true);
                 for (String option : optionsList) {
                     options.put(option, optionSec.get(option));
                 }
             }
             ConfigurationSection worldSec = section.getConfigurationSection("worlds");
             if (worldSec != null) {
                 Set<String> worldList = worldSec.getKeys(false);
                 for (String world : worldList) {
                     ConfigurationSection tempSection = worldSec.getConfigurationSection(world);
                     List<String> tempWorldPerms = tempSection.getStringList("permissions");
                     for (String perm : tempWorldPerms) {
                         addPermission(perm, world);
                     }
                 }
             }
             List<String> commandList = section.getStringList("commands");
             if (commandList != null) {
                 for (String command : commandList) {
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
         Permission permission = new Permission("totalpermissions.baseItem." + permType + "." + name, permMap);
         if (Bukkit.getPluginManager().getPermission(permission.getName()) != null) {
             Bukkit.getPluginManager().removePermission(permission.getName());
         }
         Bukkit.getPluginManager().addPermission(permission);
         TotalPermissions.getPlugin().getManager().addPermissionToMap(permType.toString(), name, permission);
         perms.put(null, permission);
     }
 
     /**
      * Gets a list of the permissions for this group that are global. This
      * includes inherited perms. Negative perms start with a '-'.
      *
      * @return List of permissions with - in front of negative nodes
      *
      * @since 1.0
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
      * @since 1.0
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
      * @since 1.0
      */
     public Object getOption(String key) {
         return options.get(key);
     }
 
     /**
      * Returns the options for this PermissionHolder.
      *
      * @return Map of the options for this holder
      * @since 0.2
      *
      */
     public Map<String, Object> getOptions() {
         return options;
     }
 
     /**
      * Get the name of this permission holder.
      *
      * @return Name of permission holder
      *
      * @since 1.0
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
 
     protected final synchronized void addPermission(String perm, String world, boolean allow) {
         Permission permission = perms.get(world);
         if (permission != null) {
             Map<String, Boolean> permList = permission.getChildren();
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
             permission.getChildren().clear();
             permission.getChildren().putAll(permList);
             permission.recalculatePermissibles();
             perms.put(world, permission);
         }
     }
 
     /**
      * Checks to see if permission is given. This only checks the plugin-given
      * permissions
      *
      * @param perm Permission to check for
      * @return True if user/group has permission based on plugin
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
        if (res == null) {
             return has(perm, null);
         } else {
             return res;
         }
     }
 
     /**
      * Adds the permissions for this PermissionBase to the given CommandSender.
      *
      * @param cs CommandSender to add the permissions to
      *
      * @since 1.0
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
         return attachment;
     }
 
     public PermissionAttachment setPerms(CommandSender cs) {
         return setPerms(cs, null, null);
     }
 
     public Map<String, Map<String, Boolean>> getAllPerms() {
         return null;
     }
 
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
         existing.add(item);
         if (world == null) {
             section.set("permissions", existing);
         } else {
             section.set(world + ".permissions", existing);
         }
         TotalPermissions.getPlugin().getManager().save(this);
         load();
     }
 
     public boolean hasInheritance(String item, String world) {
         List<String> list = section.getStringList("inheritence");
         for (String listItem : list) {
             if (item.equalsIgnoreCase(listItem)) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean hasCommand(String item, String world) {
         List<String> list = section.getStringList("commands");
         for (String listItem : list) {
             if (item.equalsIgnoreCase(listItem)) {
                 return true;
             }
         }
         return false;
     }
 
     public Set<String> getCommands(String world) {
         List<String> list = section.getStringList("commands");
         Set<String> returned = new HashSet<String>();
         for (String item : list) {
             returned.add(item);
         }
         return returned;
     }
 
     public Set<String> getInheritances(String world) {
         List<String> list = section.getStringList("inheritence");
         Set<String> returned = new HashSet<String>();
         for (String item : list) {
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
