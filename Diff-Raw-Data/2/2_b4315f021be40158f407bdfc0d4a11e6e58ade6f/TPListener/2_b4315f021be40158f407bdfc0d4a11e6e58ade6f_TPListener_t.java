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
 package net.ae97.totalpermissions.listeners;
 
 import net.ae97.totalpermissions.TotalPermissions;
 import net.ae97.totalpermissions.permission.PermissionUser;
 import net.ae97.totalpermissions.reflection.TPPermissibleBase;
 import java.lang.reflect.Field;
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.server.RemoteServerCommandEvent;
 
 /**
  * @version 0.1
  * @author Lord_Ralex
  * @since 0.1
  */
 public class TPListener implements Listener {
 
     protected final TotalPermissions plugin;
 
     public TPListener(TotalPermissions p) {
         plugin = p;
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerLogin(PlayerLoginEvent event) {
         PermissionUser user = plugin.getManager().getUser(event.getPlayer());
         if (plugin.getConfiguration().getBoolean("reflection.starperm")
                 || (user.getDebugState() && plugin.getConfiguration().getBoolean("reflection.debug"))) {
             //forgive me for saying I did not want to do this
             //or as squid says
             //"Forgive me for my sins"
             Player player = event.getPlayer();
             try {
                 Class cl = Class.forName("org.bukkit.craftbukkit." + plugin.getBukkitVersion() + ".entity.CraftPlayer");
                 Field field = cl.getField("perm");
                 field.setAccessible(true);
                 TPPermissibleBase base = new TPPermissibleBase(event.getPlayer(), user.getDebugState());
                 field.set(player, base);
                 plugin.getLogger().info(plugin.getLangFile().getString("listener.tplistener.login.hooked", event.getPlayer().getName()));
             } catch (NoSuchFieldException ex) {
                 plugin.getLogger().log(Level.SEVERE, plugin.getLangFile().getString("listener.tplistener.login.error"), ex);
             } catch (SecurityException ex) {
                 plugin.getLogger().log(Level.SEVERE, plugin.getLangFile().getString("listener.tplistener.login.error"), ex);
             } catch (IllegalArgumentException ex) {
                 plugin.getLogger().log(Level.SEVERE, plugin.getLangFile().getString("listener.tplistener.login.error"), ex);
             } catch (IllegalAccessException ex) {
                 plugin.getLogger().log(Level.SEVERE, plugin.getLangFile().getString("listener.tplistener.login.error"), ex);
             } catch (ClassNotFoundException ex) {
                 plugin.getLogger().log(Level.SEVERE, plugin.getLangFile().getString("listener.tplistener.login.error"), ex);
             }
         }
         plugin.getManager().handleLoginEvent(event);
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerJoinEvent(PlayerJoinEvent event) {
         Player player = event.getPlayer();
         PermissionUser user = plugin.getManager().getUser(player);
         user.changeWorld(player, player.getWorld().getName(), plugin.getManager().getAttachment(player));
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerQuit(PlayerQuitEvent event) {
         plugin.getManager().handleLogoutEvent(event);
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
         Player player = event.getPlayer();
         PermissionUser user = plugin.getManager().getUser(player);
         user.changeWorld(player, player.getWorld().getName(), plugin.getManager().getAttachment(player));
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerCommandPreprocessDebugCheck(PlayerCommandPreprocessEvent event) {
         PermissionUser user = plugin.getManager().getUser(event.getPlayer());
         if (!user.getDebugState() || plugin.getConfiguration().getBoolean("reflection.debug")) {
             return;
         }
        plugin.getLogger().info(plugin.getLangFile().getString("listener.tplistener.preprocess.activate", event.getPlayer().getName(), event.getMessage()));
         try {
             String command = event.getMessage().split(" ", 2)[0].substring(1);
             Command cmd = Bukkit.getPluginCommand(command);
             if (cmd.testPermissionSilent(event.getPlayer())) {
                 plugin.getLogger().info(plugin.getLangFile().getString("listener.tplistener.preprocess.allow", event.getPlayer().getName(), cmd.getPermission()));
             } else {
                 plugin.getLogger().info(plugin.getLangFile().getString("listener.tplistener.preprocess.deny", event.getPlayer().getName(), cmd.getPermission()));
             }
         } catch (NullPointerException e) {
             plugin.getLogger().log(Level.SEVERE, plugin.getLangFile().getString("listener.tplistener.preprocess.invalid", event.getMessage()));
         } catch (IndexOutOfBoundsException e) {
             plugin.getLogger().log(Level.SEVERE, plugin.getLangFile().getString("listener.tplistener.preprocess.index", event.getMessage()));
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onRemoteConsoleEvent(RemoteServerCommandEvent event) {
         CommandSender sender = event.getSender();
         plugin.getManager().getRcon().setPerms(sender, plugin.getManager().getAttachment(sender.getName()), null);
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerCommandPreprocessPermCheck(PlayerCommandPreprocessEvent event) {
         String cmd = event.getMessage().split(" ")[0].substring(1);
         Command command = Bukkit.getPluginCommand(cmd);
         if (command == null) {
             return;
         }
         if (!command.testPermissionSilent(event.getPlayer())) {
             command.setPermissionMessage(cmd);
         }
     }
 }
