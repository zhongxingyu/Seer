 /*
 * Copyright (C) 2011-2012 Keyle
  */
 
 package me.xXLupoXx.NoSpawn.Util;
 import me.xXLupoXx.NoSpawn.NoSpawn;
 
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import java.util.logging.Logger;
 
 public class NoSpawnPermissions
 {
     private static Object Permissions;
 
     private static Logger Log = Logger.getLogger("Minecraft");
 
     public enum PermissionsType
     {
         NONE, Vault, BukkitPermissions,
     }
 
     private static PermissionsType PermissionsMode = PermissionsType.NONE;
 
 
     public static boolean has(Player player, String node)
     {
         if (player.isOp())
         {
             return true;
         }
         else if (PermissionsMode == PermissionsType.NONE)
         {
             return false;
         }
         else if(PermissionsMode == PermissionsType.Vault)
         {
            return ((Permission)Permissions).has(player,node);
         }
         else if (PermissionsMode == PermissionsType.BukkitPermissions)
         {
            return player.hasPermission(node);
         }
         return false;
 
     }
 
     @SuppressWarnings("unused")
     public static void setup(PermissionsType pt)
     {
         PermissionsMode = pt;
     }
 
     public static void setup()
     {
         Plugin p;
 
         p = NoSpawn.getPlugin().getServer().getPluginManager().getPlugin("Vault");
         if (p != null && PermissionsMode == PermissionsType.NONE)
         {
             PermissionsMode = PermissionsType.Vault;
             Permissions = null;
 
             RegisteredServiceProvider<Permission> permissionProvider = NoSpawn.getPlugin().getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
             if (permissionProvider != null)
             {
                 Permissions = permissionProvider.getProvider();
             }
             if(Permissions != null)
             {
                 Log.info("[NoSpawn] Vault integration enabled!");
                 return;
             }
             PermissionsMode = PermissionsType.NONE;
         }
 
         if (PermissionsMode == PermissionsType.NONE && ConfigBuffer.BukkitPerm)
         {
             PermissionsMode = PermissionsType.BukkitPermissions;
             Log.info("[NoSpawn] BukkitPermissions enabled!");
             return;
         }
 
         Log.info("[NoSpawn] No permissions system fund!");
     }
 }
