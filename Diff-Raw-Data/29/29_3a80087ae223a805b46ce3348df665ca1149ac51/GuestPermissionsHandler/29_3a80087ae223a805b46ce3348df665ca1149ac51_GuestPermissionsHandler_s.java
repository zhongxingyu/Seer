 package com.thevoxelbox.voxelguest.permissions;
 
 import com.thevoxelbox.voxelguest.VoxelGuest;
 import java.io.File;
 import java.util.List;
 import org.bukkit.Server;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class GuestPermissionsHandler extends PermissionsHandler {
 
     private final File file = new File("plugins/VoxelGuest/permissions.yml");
     private final YamlConfiguration yamlPerms = new YamlConfiguration();
     
     private final String header = "#########################################\n" +
             "#\n" +
             "# =============================\n" +
             "# VOXELGUEST 4 PERMISSIONS FILE\n" +
             "#\n" +
             "# Built by: psanker\n" +
             "# =============================\n" +
             "#\n" +
             "# This is a standard permissions system for VoxelGuest, which VoxelGuest handles after all\n" +
             "# other systems are disregarded OR the config explicitly says to use the VG perms system.\n" +
             "# This file accepts both UNIX LF line endings (\"\\n\") and Windows line endings (\"\\r\\n\").\n" +
             "#\n" +
             "# BE SURE TO USE SPACES AND NOT TABS. Use a good text editor to edit this file.\n" +
             "# Notepad++ is recommended for Windows, and TextMate is recommended for Mac.\n" +
             "#\n" +
             "# Comments start with a pound sign (like the beginning of this and the above lines).\n" +
             "# The YAML processor will ignore these lines. To verify your YAML syntax, please use\n" +
             "# http://yaml-online-parser.appspot.com/\n" +
             "#\n" +
             "# Please note: There is no multi-world and multi-group support\n" +
             "#\n" +
             "#########################################\n" +
             "\n";
     
     @Override
     public PermissionsHandler initialize(Server server) {
         return new GuestPermissionsHandler(server);
     }
     
     public GuestPermissionsHandler(Server server) {
         try {
             
             this.server = server;
         
             if (!file.exists()) {
                 file.getParentFile().mkdirs();
                 file.createNewFile();
             }
             
             yamlPerms.load(file);
             yamlPerms.options().header(header);
             
         } catch (Throwable t) {
             VoxelGuest.log("Error in loading permissions file", 2);
             t.printStackTrace();
             return;
         }
     }
 
     @Override
     public String getDetectionMessage() {
         return "Using built-in permissions system";
     }
 
     @Override
     public boolean hasPermission(String name, String permission) {
         List<String> list = yamlPerms.getStringList("user." + name + ".permissions");
         String group = yamlPerms.getString("user." + name + ".group");
         
        if (list != null && list.contains(permission))
             return true;
         
         if (group != null)
             return hasGroupPermissionRecursive(group, permission);
         
         return false;
     }
 
     @Override
     public boolean hasPermission(String world, String name, String permission) {
         return hasPermission(name, permission);
     }
 
     @Override
     public boolean inGroup(String name, String group) {
         String userGroup = yamlPerms.getString("user." + name + ".group");
         
         return userGroup.equalsIgnoreCase(group);
     }
 
     @Override
     public String[] getGroups(String name) {
         String[] out = new String[1];
         out[0] = yamlPerms.getString("user." + name + ".group");
         
         if (out[0] == null)
             return null;
         else
             return out;
     }
     
     private boolean hasGroupPermissionRecursive(String group, String permission) {
         List<String> list = yamlPerms.getStringList("group." + group + ".permissions");
         String parent = yamlPerms.getString("group." + group + ".inherits");
         
         if (list == null)
             return false;
        else if (list.contains(permission))
             return true;
         else if (parent != null)
             return hasGroupPermissionRecursive(parent, permission);
         
         return false;
     }
 }
