 /*
  * Copyright (C) 2013 Dabo Ross <http://www.daboross.net/>
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
 package net.daboross.bukkitdev.purewarp;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.logging.Level;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.scheduler.BukkitTask;
 import org.mcstats.MetricsLite;
 
 /**
  *
  * @author daboross
  */
 public class PureWarpPlugin extends JavaPlugin implements Listener {
 
     private PureWarpDatabase database;
     private final Map<String, BukkitTask> tasks = new HashMap<String, BukkitTask>();
 
     @Override
     public void onEnable() {
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(this, this);
         database = new PureWarpDatabase(this);
         database.load();
         MetricsLite metrics = null;
         try {
             metrics = new MetricsLite(this);
         } catch (IOException ex) {
            getLogger().log(Level.WARNING, "Unable to create Metrics", ex);
         }
         if (metrics != null) {
             metrics.start();
         }
     }
 
     @Override
     public void onDisable() {
         database.save();
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (cmd.getName().equalsIgnoreCase("pwarp")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage(ChatColor.DARK_RED + "You aren't a player.");
                 return true;
             }
             if (args.length < 1) {
                 sender.sendMessage(ChatColor.DARK_RED + "Please specify a warp name.");
                 return false;
             }
             final Player p = (Player) sender;
             final String name = toString(args);
             final PureWarp warp = database.getWarp(name);
             if (warp != null) {
                 BukkitTask old = tasks.get(p.getName().toLowerCase());
                 if (old != null) {
                     old.cancel();
                     sender.sendMessage(ChatColor.GRAY + "Last teleportation request canceled.");
                 }
                 sender.sendMessage(ChatColor.GRAY + "Please wait 3 seconds before warping.");
                 tasks.put(p.getName().toLowerCase(), new BukkitRunnable() {
                     @Override
                     public void run() {
                         p.sendMessage(ChatColor.GRAY + "Warping to " + ChatColor.AQUA + name + ChatColor.GRAY + ".");
                         PureWarp player = new PureWarp(p);
                         database.setWarp("player_back_" + p.getName().toLowerCase(), player);
                         p.teleport(warp.toLocation());
                     }
                 }.runTaskLater(this, 60L));
             } else {
                 sender.sendMessage(ChatColor.DARK_RED + "Warp " + ChatColor.RED + name + ChatColor.DARK_RED + " does not exist.");
             }
         } else if (cmd.getName().equalsIgnoreCase("psetwarp")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage(ChatColor.DARK_RED + "You aren't a player.");
             }
             if (args.length < 1) {
                 sender.sendMessage(ChatColor.DARK_RED + "Please specify a warp name.");
                 return false;
             }
             Player p = (Player) sender;
             String name = toString(args);
             sender.sendMessage(ChatColor.GRAY + "Setting warp " + ChatColor.AQUA + name + ChatColor.GRAY + " at your current location.");
             PureWarp warp = new PureWarp(p);
             database.setWarp(name, warp);
         } else if (cmd.getName().equalsIgnoreCase("punsetwarp")) {
             if (args.length < 1) {
                 sender.sendMessage(ChatColor.DARK_RED + "Please specify a warp name.");
                 return false;
             }
             String name = toString(args);
             if (database.removeWarp(name)) {
                 sender.sendMessage(ChatColor.GRAY + "Warp " + ChatColor.AQUA + name + ChatColor.GRAY + " removed.");
             } else {
                 sender.sendMessage(ChatColor.DARK_RED + "Warp " + ChatColor.RED + name + ChatColor.DARK_RED + " did not exist.");
             }
         } else if (cmd.getName().equalsIgnoreCase("plistwarps")) {
             if (args.length != 0) {
                 sender.sendMessage(ChatColor.DARK_RED + "Too many arguments.");
                 return false;
             }
             Iterator<String> i = database.getWarpList().iterator();
             if (i.hasNext()) {
                 StringBuilder warps = new StringBuilder(ChatColor.AQUA.toString()).append(i.next());
                 while (i.hasNext()) {
                     warps.append(ChatColor.GRAY).append(", ").append(ChatColor.AQUA).append(i.next());
                 }
                 sender.sendMessage(ChatColor.GRAY + "Warps: " + warps);
             } else {
                 sender.sendMessage(ChatColor.GRAY + "No warps defined.");
             }
         } else if (cmd.getName().equalsIgnoreCase("pback")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage(ChatColor.DARK_RED + "You aren't a player.");
             }
             if (args.length != 0) {
                 sender.sendMessage(ChatColor.DARK_RED + "Too many arguments.");
                 return false;
             }
             final Player p = (Player) sender;
             final PureWarp back = database.getWarp("player_back_" + p.getName().toLowerCase());
             if (back == null) {
                 sender.sendMessage(ChatColor.DARK_RED + "No back location.");
             } else {
                 BukkitTask old = tasks.get(p.getName().toLowerCase());
                 if (old != null) {
                     old.cancel();
                     sender.sendMessage(ChatColor.GRAY + "Last teleportation request canceled.");
                 }
                 sender.sendMessage(ChatColor.GRAY + "Please wait 3 seconds before warping.");
                 tasks.put(p.getName().toLowerCase(), new BukkitRunnable() {
                     @Override
                     public void run() {
                         p.sendMessage(ChatColor.GRAY + "Warping back");
                         PureWarp player = new PureWarp(p);
                         database.setWarp("player_back_" + p.getName().toLowerCase(), player);
                         p.teleport(back.toLocation());
                     }
                 }.runTaskLater(this, 60L));
             }
         } else {
             sender.sendMessage("PureWarp doesn't know about the command /" + cmd.getName());
         }
         return true;
     }
 
     private String toString(String[] args) {
         StringBuilder result = new StringBuilder(args[0]);
         for (int i = 1; i < args.length; i++) {
             result.append(" ").append(args[i]);
         }
         return result.toString();
     }
 
     @EventHandler
     public void onQuit(PlayerQuitEvent evt) {
         BukkitTask task = tasks.get(evt.getPlayer().getName().toLowerCase());
         if (task != null) {
             task.cancel();
         }
     }
 }
