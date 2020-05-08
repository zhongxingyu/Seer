 package com.platymuus.bukkit.permcompat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.Configuration;
 
 import de.hydrox.bukkit.DroxPerms.DroxPermsAPI;
 
 /**
  *
  * @author code
  */
 public class PermissionHandler extends com.nijiko.permissions.PermissionHandler {
     
     private Server server;
     private DroxPermsAPI API;
     
     public PermissionHandler(DroxPermsAPI droxPermsAPI) {
         server = Bukkit.getServer();
         API = droxPermsAPI;
     	if (API == null) {
     		throw new RuntimeException();
     	}
     }
     
     private boolean internalHasPermission(Player player, String permission) {
         if (player.hasPermission("superpermbridge.*")) {
             return true;
         }
 
         int index = permission.indexOf('.');
         if (index >= 0) {
             String pluginName = permission.substring(0, index);
             if (player.hasPermission("superpermbridge." + pluginName)) {
                 return true;
             }
         }
         
         while (index >= 0) {
             String subnodeName = permission.substring(0, index);
             if (player.hasPermission("superpermbridge." + subnodeName + ".*")) {
                 return true;
             }
             index = permission.indexOf('.', index + 1);
         }
         
         return player.hasPermission("superpermbridge." + permission) || player.hasPermission(permission);
     }
 
     @Override
     public boolean has(Player player, String permission) {
         return internalHasPermission(player, permission);
     }
 
     @Override
     public boolean has(String worldName, String playerName, String permission) {
         if (server.getPlayer(playerName) == null) return false;
         return internalHasPermission(server.getPlayer(playerName), permission);
     }
 
     @Override
     public boolean permission(Player player, String permission) {
         return internalHasPermission(player, permission);
     }
     
     public boolean permission(String worldName, Player player, String permission){
         return internalHasPermission(player, permission);
     }
 
     @Override
     public boolean permission(String worldName, String playerName, String permission) {
         if (server.getPlayer(playerName) == null) return false;
         return internalHasPermission(server.getPlayer(playerName), permission);
     }
 
     @Override
     public String getGroup(String world, String userName) {
         return API.getPlayerGroup(userName);
     }
 
     @Override
     public String[] getGroups(String world, String userName) {
     	ArrayList<String> tmp = API.getPlayerSubgroups(userName);
     	tmp.add(API.getPlayerGroup(userName));
         return tmp.toArray(new String[0]);
     }
 
     @Override
     public boolean inGroup(String world, String userName, String groupName) {
     	return inGroup(userName, groupName);
     }
 
     @Override
     public boolean inGroup(String name, String group) {
     	if (API.getPlayerGroup(name).equalsIgnoreCase(group)) {
     		return true;
     	}
     	ArrayList<String> tmp = API.getPlayerSubgroups(name);
     	for (String subgroup : tmp) {
     		if (subgroup.equalsIgnoreCase(group)) {
         		return true;
         	}
 		}
     	return false;
     	
     }
 
     @Override
     public boolean inSingleGroup(String world, String userName, String groupName) {
     	return API.getPlayerGroup(userName).equalsIgnoreCase(groupName);
     }
 
     @Override
     public String getGroupPrefix(String world, String groupName) {
        return API.getGroupInfo(groupName, "prefix");
     }
 
     @Override
     public String getGroupSuffix(String world, String groupName) {
        return API.getGroupInfo(groupName, "suffix");
     }
 
     @Override
     public boolean canGroupBuild(String world, String groupName) {
         return true;
     }
 
     @Override
     public String getGroupPermissionString(String world, String groupName, String permission) {
     	return API.getGroupInfo(groupName, permission);
     }
 
     @Override
     public int getGroupPermissionInteger(String world, String groupName, String permission) {
     	Logger.getLogger("Minecraft").warning("[DroxPermsBridge] getGroupPermissionInteger unsupported");
         throw new UnsupportedOperationException("Unsupported operation");
     }
 
     @Override
     public boolean getGroupPermissionBoolean(String world, String groupName, String permission) {
     	Logger.getLogger("Minecraft").warning("[DroxPermsBridge] getGroupPermissionBoolean unsupported");
         throw new UnsupportedOperationException("Unsupported operation");
     }
     
     @Override
     public double getGroupPermissionDouble(String world, String groupName, String permission) {
     	Logger.getLogger("Minecraft").warning("[DroxPermsBridge] getGroupPermissionDouble unsupported");
         throw new UnsupportedOperationException("Unsupported operation");
     }
 
     @Override
     public String getUserPermissionString(String world, String userName, String permission) {
    	return API.getPlayerInfo(userName, permission);
     }
 
     @Override
     public int getUserPermissionInteger(String world, String userName, String permission) {
     	Logger.getLogger("Minecraft").warning("[DroxPermsBridge] getUserPermissionInteger unsupported");
         throw new UnsupportedOperationException("Unsupported operation");
     }
 
     @Override
     public boolean getUserPermissionBoolean(String world, String userName, String permission) {
         return this.has(world, userName, permission);
     }
 
     @Override
     public double getUserPermissionDouble(String world, String userName, String permission) {
     	Logger.getLogger("Minecraft").warning("[DroxPermsBridge] getUserPermissionDouble unsupported");
         throw new UnsupportedOperationException("Unsupported operation");
     }
 
     @Override
     public String getPermissionString(String world, String userName, String permission) {
     	return API.getPlayerInfo(userName, permission);
     }
 
     @Override
     public int getPermissionInteger(String world, String userName, String permission) {
     	Logger.getLogger("Minecraft").warning("[DroxPermsBridge] getPermissionInteger unsupported");
         throw new UnsupportedOperationException("Unsupported operation");
     }
 
     @Override
     public boolean getPermissionBoolean(String world, String userName, String permission) {
         return this.has(world, userName, permission);
     }
     
     @Override
     public double getPermissionDouble(String world, String userName, String permission) {
     	Logger.getLogger("Minecraft").warning("[DroxPermsBridge] getPermissionDouble unsupported");
         throw new UnsupportedOperationException("Unsupported operation");
     }
 
     @Override
     public void addUserPermission(String world, String user, String node) {
     	API.addPlayerPermission(user, world, node);
     }
 
     @Override
     public void removeUserPermission(String world, String user, String node) {
     	API.removePlayerPermission(user, world, node);
     }
 
     /*
      * Here came unneccesary for implementation stuff
      */
     @Override
     public void addGroupInfo(String world, String group, String node, Object data) {
     	if (data instanceof String) {
     		API.setGroupInfo(group, node, (String)data);
     	} else {
         	Logger.getLogger("Minecraft").warning("[DroxPermsBridge] addGroupInfo only supports String data");
     	}
     }
 
     @Override
     public void removeGroupInfo(String world, String group, String node) {
     	Logger.getLogger("Minecraft").warning("[DroxPermsBridge] removeGroupInfo unsupported");
     	throw new UnsupportedOperationException("Unsupported operation");
     }
 
     @Override
     public void setDefaultWorld(String world) {
         throw new UnsupportedOperationException("Unsupported operation");
     }
 
     @Override
     public boolean loadWorld(String world) {
         throw new UnsupportedOperationException("Unsupported operation");
     }
 
     @Override
     public void forceLoadWorld(String world) {
         throw new UnsupportedOperationException("Unsupported operation");
     }
 
     @Override
     public boolean checkWorld(String world) {
         throw new UnsupportedOperationException("Unsupported operation");
     }
 
     @Override
     public void load() {
         throw new UnsupportedOperationException("Unsupported operation");
     }
 
     @Override
     public void load(String world, Configuration config) {
         throw new UnsupportedOperationException("Unsupported operation");
     }
 
     @Override
     public boolean reload(String world) {
         throw new UnsupportedOperationException("Unsupported operation");
     }
 
     // Cache
     @Override
     public void setCache(String world, Map<String, Boolean> Cache) {
         server.getLogger().warning("[DroxPermsBridge] setCache item are internal Permissions plugin stuff. Nag plugin author.");
     }
 
     @Override
     public void setCacheItem(String world, String player, String permission, boolean data) {
         server.getLogger().warning("[DroxPermsBridge] setCacheItem item are internal Permissions plugin stuff. Nag plugin author.");
     }
 
     @Override
     public Map<String, Boolean> getCache(String world) {
         server.getLogger().warning("[DroxPermsBridge] setCacheItem item are internal Permissions plugin stuff. Nag plugin author.");
         return new HashMap<String, Boolean>();
     }
 
     @Override
     public boolean getCacheItem(String world, String player, String permission) {
         server.getLogger().warning("[DroxPermsBridge] getCacheItem item are internal Permissions plugin stuff. Nag plugin author.");
         return false;
     }
 
     @Override
     public void removeCachedItem(String world, String player, String permission) {
         server.getLogger().warning("[DroxPermsBridge] removeCachedItem item are internal Permissions plugin stuff. Nag plugin author.");
     }
 
     @Override
     public void clearCache(String world) {
         server.getLogger().warning("[DroxPermsBridge] clearCache item are internal Permissions plugin stuff. Nag plugin author.");
     }
 
     @Override
     public void clearAllCache() {
         server.getLogger().warning("[DroxPermsBridge] clearAllCache item are internal Permissions plugin stuff. Nag plugin author.");
     }
 
     @Override
     public void save(String world) {
         throw new UnsupportedOperationException("Unsupported operation");
     }
 
     @Override
     public void saveAll() {
         throw new UnsupportedOperationException("Unsupported operation");
     }
 
     @Override
     public void reload() {
         throw new UnsupportedOperationException("Unsupported operation");
     }
     
 }
