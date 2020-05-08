 /*
  * Copyright (C) 2012 mewin <mewin001@hotmail.de>
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
 package com.mewin.WGRegionPermissions;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.permissions.PermissionAttachment;
 
 /**
  *
  * @author mewin <mewin001@hotmail.de>
  */
 public class WGRegionPermissionsListener implements Listener {
     private Map<Player, PermissionAttachment> attachments;
     private WGRegionPermissionsPlugin plugin;
     private WorldGuardPlugin wgPlugin;
     private Map<Player, Set<String>> addedPermissions, removedPermissions;
     
     public WGRegionPermissionsListener(WGRegionPermissionsPlugin plugin, WorldGuardPlugin wgPlugin)
     {
         attachments = new HashMap<>();
         addedPermissions = new HashMap<>();
         removedPermissions = new HashMap<>();
         this.plugin = plugin;
         this.wgPlugin = wgPlugin;
     }
     
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent e)
     {
         attachments.put(e.getPlayer(), e.getPlayer().addAttachment(plugin));
         addedPermissions.put(e.getPlayer(), new HashSet<String>());
         removedPermissions.put(e.getPlayer(), new HashSet<String>());
     }

     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent e)
     {
        this.onPlayerLeave(e);
     }

     @EventHandler
     public void onPlayerKick(PlayerKickEvent e)
     {
        this.onPlayerLeave(e);
    }

    private void onPlayerLeave(PlayerEvent e)
    {
        PermissionAttachment attachment = attachments.remove(e.getPlayer());
        if (attachment != null)
        {
            e.getPlayer().removeAttachment(attachment);
        }
         addedPermissions.remove(e.getPlayer());
         removedPermissions.remove(e.getPlayer());
     }

     @EventHandler
     public void onPlayerMove(PlayerMoveEvent e)
     {
         PermissionAttachment attachment = attachments.get(e.getPlayer());
         
         Map<String, Boolean> perms = attachment.getPermissions();
         
         Utils.setPermissionsForPlayer(wgPlugin, perms, e.getPlayer(), addedPermissions.get(e.getPlayer()), removedPermissions.get(e.getPlayer()));
         
         setPrivateValue(attachment, "permissions", perms);
         
         e.getPlayer().recalculatePermissions();
     }
     
     public void addAttachments()
     {
         for(Player player : Bukkit.getOnlinePlayers())
         {
             player.addAttachment(plugin);
         }
     }
     
     public void clearAttachments()
     {
         for (Entry<Player, PermissionAttachment> entry : this.attachments.entrySet())
         {
             entry.getValue().remove();
         }
         
         attachments.clear();
     }
     
     private void setPrivateValue(Object obj, String name, Object value)
     {
         try {
             Field f = obj.getClass().getDeclaredField(name);
             f.setAccessible(true);
             f.set(obj, value);
         } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
             
         }
     }
 }
