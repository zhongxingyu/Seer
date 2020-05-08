 package org._127001.frymaster.gbp;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.PermissionAttachment;
 import org.bukkit.plugin.Plugin;
 
 /**
  *
  * @author frymaster
  */
 public class PermissionCoordinator {
 
     private FileConfiguration usersConfig = null;
     private FileConfiguration groupsConfig = null;
     private File usersFile = null;
     private File groupsFile = null;
     private Map<String, FryGroup> groups = new HashMap<String, FryGroup>();
     private Map<String, PermissionAttachment> players = new HashMap<String, PermissionAttachment>();
     private Plugin plugin;
 
     PermissionCoordinator(Plugin plugin) {
         this.plugin = plugin;
     }
 
     /**
      * Rebuild the list of groups as well as what permissions each group will
      * apply If the config files have already been parsed they are not reloaded
      * Does not cause the permissions on existing players to change
      */
     void calculateGroups() {
         groups.clear();
         FileConfiguration gc = this.getGroupsConfig();
         Set<String> groupNames = gc.getKeys(false);
         // Iterate through all the groupnames to cause their objects to be created
         for (String group : groupNames) {
             discoverGroup(group, null);
         }
     }
 
     /**
      * Returns the group object if it exists, or tries to create it if it does
      * not. When calling this, the breadcrumbs parameter should always be null
      * This is used internally in recursive calls to track possible inheritance
      * loops
      *
      * @param group Name of the group to return
      * @param breadcrumbs Should always be null
      * @return The group object, or null if the group is not specified in the
      * configuration files
      */
     private FryGroup discoverGroup(String group, List<String> breadcrumbs) {
 
         FryGroup fg = groups.get(group);
         if (fg != null) {
             return fg;
         }
 
         ConfigurationSection gc = this.getGroupsConfig().getConfigurationSection(group);
         if (gc == null) {
             return null;
         }
         int priority = gc.getInt("priority", 0);
         boolean isDefault = gc.getBoolean("default", false);
         List<String> inheritList = gc.getStringList("inherit");
         String file = gc.getString("file", null);
         List<FryGroup> inheritGroups = new ArrayList<FryGroup>();
 
         // From the list of groups to inherit from, get the actual group objects
         if (inheritList.size() > 0) {
             if (breadcrumbs == null) {
                 breadcrumbs = new ArrayList<String>();
             }
             breadcrumbs.add(group);
             for (Iterator<String> i = inheritList.iterator(); i.hasNext();) {
                 String inherit = i.next();
 
                 if (breadcrumbs.contains(inherit)) {
                     plugin.getLogger().log(Level.SEVERE, "Inheritance loop detected trying to make {0} inherit from {1}", new Object[]{group, inherit});
                     i.remove();
                 } else {
                     FryGroup parent = discoverGroup(inherit, breadcrumbs);
                     if (parent == null) {
                         plugin.getLogger().log(Level.SEVERE, "Group: {0} tried to set a non-existent parent {1} - ignoring inherit setting", new Object[]{group, inherit});
                         i.remove();
                     } else {
                         inheritGroups.add(parent);
                     }
                 }
             }
         }
 
         // Put the inherited groups in priority order
         Collections.sort(inheritGroups);
 
         // Create new group object
        fg = new FryGroup(plugin,group, priority, inheritList, file, isDefault);
         Map<String, Boolean> perms = fg.permissions();
 
         for (FryGroup parent : inheritGroups) {
             perms.putAll(parent.permissions());
         }
 
         // Add whatever permissions this group explicitly adds
         List<String> newPermissions = gc.getStringList("permissions");
         for (String permission : newPermissions) {
             if (permission.startsWith("-")) {
                 perms.put(permission.substring(1), false);
             } else {
                 perms.put(permission, true);
             }
         }
 
         groups.put(group, fg);
         return fg;
 
     }
 
     void addPermissions(Player player) {
         PermissionAttachment pa = players.remove(player.getName());
         if (pa != null) {
             try {
                 player.removeAttachment(pa);
             } catch (IllegalArgumentException e) {
                 // In all probability this was invalid
             }
         }
         boolean isInNonMetaGroups = false;
         ConfigurationSection pc = this.getUsersConfig().getConfigurationSection(player.getName());
         List<FryGroup> playerGroups = new ArrayList<FryGroup>();
         // Add all groups the player is a member of
         for (FryGroup group : this.groups.values()) {
             if (group.isMember(player) == true) {
                 if (group.isMetaGroup() == false) {
                     isInNonMetaGroups = true;
                 }
                 playerGroups.add(group);
             }
         }
         // If the player is not in any explicit groups, add default groups
         if (isInNonMetaGroups == false) {
             for (FryGroup group : this.groups.values()) {
                 if (group.isDefaultGroup()) {
                     playerGroups.add(group);
                 }
             }
         }
 
         // Add player-specific group overrides
         if (pc != null) {
             List<?> groupOverrides = pc.getList("groups");
             if (groupOverrides != null) {
                 for (Object o : groupOverrides) {
                     String groupName = o.toString();
                     if (groupName.startsWith("-")) {
                         groupName = groupName.substring(1);
                         // Use an iterator here because you can remove in an iterator
                         for (Iterator<FryGroup> i = playerGroups.iterator(); i.hasNext();) {
                             FryGroup fg = i.next();
                             if (fg.getName().equals(groupName)) {
                                 i.remove();
                                 // Don't break here, because between proper group membership and user overrides there may be two copies of a group
                                 // (Though if a user is both adding AND removing a group membership in user.yml things are already beyond hope)
                             }
                         }
                     } else {
                         FryGroup fg = groups.get(groupName);
                         if (fg != null) {
                             playerGroups.add(fg);
                         }
                     }
                 }
             }
 
         }
 
         // Put groups in priority order
         Collections.sort(playerGroups);
 
         // Get adjusted permissions 
         HashMap<String, Boolean> permissions = new HashMap<String, Boolean>();
         for (FryGroup group : playerGroups) {
             permissions.putAll(group.permissions());
         }
 
         // Add player-specific permission overrides
         if (pc != null) {
             List<?> permissionOverrides = pc.getList("permissions");
             if (permissionOverrides != null) {
                 for (Object o : permissionOverrides) {
                     String permission = o.toString();
                     if (permission.startsWith("-")) {
                         permissions.put(permission.substring(1), false);
                     } else {
                         permissions.put(permission, true);
                     }
                 }
             }
         }
 
 
         // Add permissions to player
         pa = player.addAttachment(plugin);
         for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
             pa.setPermission(entry.getKey(), entry.getValue());
         }
 
         players.put(player.getName(), pa);
 
 
 
     }
 
     void removePermissions(Player player) {
         PermissionAttachment pa = players.remove(player.getName());
         if (pa != null) {
             try {
                 player.removeAttachment(pa);
             } catch (IllegalArgumentException e) {
             }
         }
     }
    
    void removePermissions(String player) {
        Player p = plugin.getServer().getPlayer(player);
        if (p!=null) {
            removePermissions(p);
        }
    }
 
     /**
      * Gets the group with the specified name
      *
      * @param group Name of the group
      *
      * @return The group object, or null
      */
     /**
      * @return the usersConfig
      */
     private FileConfiguration getUsersConfig() {
         if (usersConfig == null) {
             reloadConfigFiles();
         }
         return usersConfig;
     }
 
     /**
      * @return the groupsConfig
      */
     private FileConfiguration getGroupsConfig() {
         if (groupsConfig == null) {
             reloadConfigFiles();
         }
         return groupsConfig;
     }
 
     /**
      * Reload the YML files
      */
     void reloadConfigFiles() {
         if (usersFile == null) {
             usersFile = new File(plugin.getDataFolder(), "users.yml");
         }
         if (!usersFile.exists()) {
             plugin.getLogger().info("users.yml file does not exist, creating default");
             plugin.saveResource("users.yml", false);
         }
         usersConfig = YamlConfiguration.loadConfiguration(usersFile);
         usersConfig.setDefaults(YamlConfiguration.loadConfiguration(plugin.getResource("users.yml")));
 
         if (groupsFile == null) {
             groupsFile = new File(plugin.getDataFolder(), "groups.yml");
         }
         if (!groupsFile.exists()) {
             plugin.getLogger().info("groups.yml file does not exist, creating default");
             plugin.saveResource("groups.yml", false);
         }
         groupsConfig = YamlConfiguration.loadConfiguration(groupsFile);
         groupsConfig.setDefaults(YamlConfiguration.loadConfiguration(plugin.getResource("groups.yml")));
     }
 
     void addAllPlayers() {
         for (Player p : plugin.getServer().getOnlinePlayers()) {
             addPermissions(p);
         }
     }
 
     void removeAllPlayers() {
        for (String player : players.keySet()) {
            removePermissions(player);
         }
         players.clear();
     }
    
     Plugin getPlugin() {
         return plugin;
     }
 
     void shutdown() {
         removeAllPlayers();
         groupsConfig = null;
         groupsFile = null;
         usersConfig = null;
         usersFile = null;
     }
 
     void startup() {
         calculateGroups();
         //If we've been reloaded, players may already be online
         addAllPlayers();
     }
 }
