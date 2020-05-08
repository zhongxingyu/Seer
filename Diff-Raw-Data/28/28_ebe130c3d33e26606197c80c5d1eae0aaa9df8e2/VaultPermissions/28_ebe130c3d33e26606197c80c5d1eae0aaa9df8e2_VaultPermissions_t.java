 package com.bukkit.gemo.utils.Permissions;
 
 import com.bukkit.gemo.utils.Permissions.SuperPermsPermissions;

import java.util.ArrayList;
 import java.util.List;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 public class VaultPermissions extends SuperPermsPermissions {
 
    private Permission perms = null;
 
 
    public VaultPermissions() {
       Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Vault");
       if(plugin != null) {
          RegisteredServiceProvider rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
          this.perms = (Permission)rsp.getProvider();
       }
    }
 
    public boolean isActive() {
       return this.perms != null;
    }
 
    public List getGroups(Player player) {
      List groups = new ArrayList();
       if(this.perms == null) {
          return groups;
       } else {
          String[] playerGroups = this.perms.getPlayerGroups(player);
          if(playerGroups != null) {
             String[] var7 = playerGroups;
             int var6 = playerGroups.length;
 
             for(int var5 = 0; var5 < var6; ++var5) {
                String group = var7[var5];
                groups.add(group);
             }
          }
 
          return groups;
       }
    }
 
    public List getGroups(String playerName, String worldName) {
      List groups = new ArrayList();
       if(this.perms == null) {
          return groups;
       } else {
          String[] playerGroups = this.perms.getPlayerGroups(worldName, playerName);
          if(playerGroups != null) {
             String[] var8 = playerGroups;
             int var7 = playerGroups.length;
 
             for(int var6 = 0; var6 < var7; ++var6) {
                String group = var8[var6];
                groups.add(group);
             }
          }
 
          return groups;
       }
    }
 
    public boolean permission(Player player, String node) {
       return this.perms.playerHas(player, node);
    }
 }
