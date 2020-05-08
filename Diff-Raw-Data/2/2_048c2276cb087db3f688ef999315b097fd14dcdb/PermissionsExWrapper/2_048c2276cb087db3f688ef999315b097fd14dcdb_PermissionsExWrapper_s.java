 package de.cubenation.plugins.utils.wrapperapi;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 public class PermissionsExWrapper {
     private static ru.tehkode.permissions.bukkit.PermissionsEx permissionsEx;
     private static Logger log;
 
     public static void setLogger(Logger log) {
         PermissionsExWrapper.log = log;
     }
 
     public static void loadPlugin() {
         if (permissionsEx == null) {
            ru.tehkode.permissions.bukkit.PermissionsEx permissionsEx = (ru.tehkode.permissions.bukkit.PermissionsEx) Bukkit.getServer().getPluginManager()
                     .getPlugin(WrapperManager.PLUGIN_NAME_PERMISSIONS_EX);
             if (permissionsEx == null) {
                 log.info(WrapperManager.PLUGIN_NAME_MULTIVERSE_CORE + " not found");
             }
         }
     }
 
     public static PermissionManager getPermissionManager() {
         if (permissionsEx == null) {
             loadPlugin();
         }
 
         if (permissionsEx != null) {
             return new PermissionManager(ru.tehkode.permissions.bukkit.PermissionsEx.getPermissionManager());
         }
         return null;
     }
 
     public static class PermissionManager {
         private ru.tehkode.permissions.PermissionManager permissionManager;
 
         public PermissionManager(ru.tehkode.permissions.PermissionManager permissionManager) {
             this.permissionManager = permissionManager;
         }
 
         public boolean has(Player player, String rightName) {
             return permissionManager.has(player, rightName);
         }
 
         public PermissionUser getUser(String username) {
             return new PermissionUser(permissionManager.getUser(username));
         }
 
         public PermissionGroup getGroup(String groupname) {
             return new PermissionGroup(permissionManager.getGroup(groupname));
         }
 
         // TODO
     }
 
     public static class PermissionUser {
         private ru.tehkode.permissions.PermissionUser permissionUser;
 
         public PermissionUser(ru.tehkode.permissions.PermissionUser permissionUser) {
             this.permissionUser = permissionUser;
         }
 
         public void setGroups(PermissionGroup[] groups, String arg1) {
             ru.tehkode.permissions.PermissionGroup[] list = null;
 
             if (groups != null) {
                 ArrayList<ru.tehkode.permissions.PermissionGroup> arrayList = new ArrayList<ru.tehkode.permissions.PermissionGroup>();
 
                 for (PermissionGroup group : groups) {
                     arrayList.add(group.permissionGroup);
                 }
 
                 list = arrayList.toArray(new ru.tehkode.permissions.PermissionGroup[] {});
             }
 
             permissionUser.setGroups(list, arg1);
         }
 
         // TODO
     }
 
     public static class PermissionGroup {
         private ru.tehkode.permissions.PermissionGroup permissionGroup;
 
         public PermissionGroup(ru.tehkode.permissions.PermissionGroup permissionGroup) {
             this.permissionGroup = permissionGroup;
         }
 
         // TODO
     }
 }
