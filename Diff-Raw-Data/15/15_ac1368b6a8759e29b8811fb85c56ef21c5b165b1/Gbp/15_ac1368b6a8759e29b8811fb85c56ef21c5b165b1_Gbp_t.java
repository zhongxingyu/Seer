 package org._127001.frymaster.gbp;
 
 import java.io.File;
 import java.util.AbstractMap;
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
 import org.bukkit.plugin.java.JavaPlugin;
 
 // TODO: Allow inheritance from multiple groups
 // TODO: Implement commands to allow in-game altering of permissions instead of editing config file and using /reload
 // TODO: per-world permissions
 // TODO: Allow looking up group membership using SQL queries
 // TODO: Vault integration
 public final class Gbp extends JavaPlugin {
     
     private FileConfiguration usersConfig = null;
     private FileConfiguration groupsConfig = null;
     private File usersFile = null;
     private File groupsFile = null;
     private List<FryGroup> groups = new ArrayList<FryGroup>();
     private AbstractMap<String, PermissionAttachment> players = new HashMap<String, PermissionAttachment>();
     static Plugin plugin;
     
     @Override
     public void onEnable() {
         Gbp.plugin = this;
         recalculateGroups();
         getServer().getPluginManager().registerEvents(new EventProcessor(this), this);
 
         //If we've been reloaded, players may already be online
         for (Player p : getServer().getOnlinePlayers()) {
             addPermissions(p);
         }
     }
     
     @Override
     public void onDisable() {
         for (Map.Entry<String, PermissionAttachment> entry : players.entrySet()) {
             Player p = getServer().getPlayer(entry.getKey());
             if (p != null) {
                 p.removeAttachment(entry.getValue());
             }
         }
         players.clear();
     }
 
     /**
      * Gets the group with the specified name
      *
      * @param group Name of the group
      *
      * @return The group object, or null
      */
     public FryGroup getGroup(String group) {
         int i = groups.indexOf(group);
         if (i > -1) {
             return groups.get(i);
         } else {
             return null;
         }
         
     }
 
     /**
      * @return the usersConfig
      */
     public FileConfiguration getUsersConfig() {
         if (usersConfig == null) {
             reloadConfigFiles();
         }
         return usersConfig;
     }
 
     /**
      * @return the groupsConfig
      */
     public FileConfiguration getGroupsConfig() {
         if (groupsConfig == null) {
             reloadConfigFiles();
         }
         return groupsConfig;
     }
 
     /**
      * Reload the YML files
      */
     public void reloadConfigFiles() {
         if (usersFile == null) {
             usersFile = new File(getDataFolder(), "users.yml");
         }
         if (!usersFile.exists()) {
             this.saveResource("users.yml", false);
         }
         usersConfig = YamlConfiguration.loadConfiguration(usersFile);
         usersConfig.setDefaults(YamlConfiguration.loadConfiguration(this.getResource("users.yml")));
         
         if (groupsFile == null) {
             groupsFile = new File(getDataFolder(), "groups.yml");
         }
         if (!groupsFile.exists()) {
             this.saveResource("groups.yml", false);
         }
         groupsConfig = YamlConfiguration.loadConfiguration(groupsFile);
         groupsConfig.setDefaults(YamlConfiguration.loadConfiguration(this.getResource("groups.yml")));
     }
 
     /**
      * Rebuild the list of groups as well as what permissions each group will
      * apply If the config files have already been parsed they are not reloaded
      * Does not cause the permissions on existing players to change
      */
     public void recalculateGroups() {
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
         
         int i = groups.indexOf(group);
         if (i > -1) {
             return groups.get(i);
         }
         
         ConfigurationSection gc = this.getGroupsConfig().getConfigurationSection(group);
         if (gc == null) {
             return null;
         }
         Integer priority = gc.getInt("priority", 0);
         boolean isDefault = gc.getBoolean("default", false);
         String inherit = gc.getString("inherit", null);
         String file = gc.getString("file", null);
         FryGroup fg = new FryGroup(group, priority, inherit, file, isDefault);
         
         AbstractMap<String, Boolean> perms = fg.getPermissions();
         if (inherit != null) {
             if (breadcrumbs == null) {
                 breadcrumbs = new ArrayList<String>();
             }
             breadcrumbs.add(group);
             if (breadcrumbs.contains(inherit)) {
                 getLogger().log(Level.SEVERE, "Inheritance loop detected trying to make {0} inherit from {1}", new Object[]{group, inherit});
                 fg.setInherit(null);
             } else {
                 FryGroup parent = discoverGroup(inherit, breadcrumbs);
                 if (parent == null) {
                    getLogger().log(Level.SEVERE, "Group: {0} tried to set a non-existent parent {1} - ignoring inherit setting", new Object[]{group, inherit});
                    fg.setInherit(null);
                } else {
                    perms.putAll(parent.getPermissions());
                 }
             }
         }
         
         List<?> newPermissions = gc.getList("permissions");
         if (newPermissions != null) {
             for (Object o : newPermissions) {
                 String permission = o.toString();
                 if (permission.startsWith("-")) {
                     perms.put(permission.substring(1), false);
                 } else {
                     perms.put(permission, true);
                 }
             }
         }
         
         groups.add(fg);
         return fg;
         
     }
     
     public void addPermissions(Player player) {
         boolean isInNonMetaGroups = false;
         ConfigurationSection pc = this.getUsersConfig().getConfigurationSection(player.getName());
         List<FryGroup> playerGroups = new ArrayList<FryGroup>();
         // Add all groups the player is a member of
         for (FryGroup group : this.groups) {
             if (group.isMember(player)== true) {
                 if (group.isMetaGroup() == false) {
                     isInNonMetaGroups = true;
                 }
                 playerGroups.add(group);
             }
         }
         // If the player is not in any explicit groups, add default groups
         if (isInNonMetaGroups == false) {
             for (FryGroup group : this.groups) {
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
                         for (Iterator<FryGroup> i = playerGroups.iterator(); i.hasNext();) {
                             FryGroup fg = i.next();
                             if (fg.getName().equals(groupName)) {
                                 i.remove();
                                // Don't break here, because between proper group membership and user overrides there may be two copies of a group
                                // (Though if a user is both adding AND removing a group membership in user.yml things are already beyond hope)
                             }
                         }
                     } else {
                         for (FryGroup fg : this.groups) {
                             if (fg.getName().equals(groupName)) {
                                // Could end up with more than one copy of the group... not actually a problem since they'll have identical effect
                                 playerGroups.add(fg);
                                break;
                             }
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
             permissions.putAll(group.getPermissions());
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
         PermissionAttachment pa = player.addAttachment(this);
         for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
             pa.setPermission(entry.getKey(), entry.getValue());            
         }
         
         players.put(player.getName(), pa);
         
         
         
     }
     
     public void removePermissions(Player player) {
         PermissionAttachment pa = players.get(player.getName());
         if (pa != null) {
             player.removeAttachment(pa);
             players.remove(player.getName());
         }
     }
 }
