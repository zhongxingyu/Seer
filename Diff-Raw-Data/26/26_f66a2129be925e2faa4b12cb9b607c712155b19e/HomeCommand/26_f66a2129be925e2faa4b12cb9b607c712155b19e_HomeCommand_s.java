 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
  */
 package com.github.lankylord.SimpleHomes.commands;
 
 import com.github.lankylord.SimpleHomes.SimpleHomes;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author cedeel
  */
 public class HomeCommand implements CommandExecutor {
 
     private SimpleHomes instance;
 
     public HomeCommand(SimpleHomes instance) {
         this.instance = instance;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
 
             if (instance.getHomes().contains(player.getName())) {
                 ConfigurationSection home = instance.getHomes().getConfigurationSection(player.getName());
 
                 if (args.length == 0) {
                     if (sender.hasPermission("simplehomes.homes")) {
                         String w = home.getString("world");
                         int x = home.getInt("x"),
                                 y = home.getInt("y"),
                                 z = home.getInt("z");
                         player.teleport(new Location(Bukkit.getWorld(w), x, y, z));
                         sender.sendMessage(ChatColor.YELLOW + "Teleported.");
                         return true;
                     }
                 } else if (args.length == 1) {
                     if (sender.hasPermission("simplehomes.multihomes")) {
                         String w = home.getString(args[0] + ".world");
                         int x = home.getInt(args[0] + ".x"),
                                 y = home.getInt(args[0] + ".y"),
                                 z = home.getInt(args[0] + ".z");
                         player.teleport(new Location(Bukkit.getWorld(w), x, y, z));
                         sender.sendMessage(ChatColor.YELLOW + "Teleported.");
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 }
