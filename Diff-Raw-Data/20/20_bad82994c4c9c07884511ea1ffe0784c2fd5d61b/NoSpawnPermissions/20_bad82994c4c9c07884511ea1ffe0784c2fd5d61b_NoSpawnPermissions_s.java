 /*
 * Copyright (c) <2012> <xXLupoXx>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
            ((Permission)Permissions).has(player,node);
         }
         else if (PermissionsMode == PermissionsType.BukkitPermissions)
         {
            player.hasPermission(node);
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
