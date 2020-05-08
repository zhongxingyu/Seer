 /**
  * This plugin is used to extend the functionality of SimpleClans
  * to make is easier to use in a PvP setting.  
  * 
  * Written by:
  * Ryan Mendivil <http://nullreff.net>
  * 
  * Tower Management By:
  * Dmitri Amariei <https://github.com/damariei>
  * 
  * Some code and ideas borrowed from:
  * 	Scyntrus <http://www.minecraftforum.net/user/474851-scyntrus/>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License.  This program 
  * is distributed in the hope that it will be useful,  but WITHOUT ANY 
  * WARRANTY.  See the GNU General Public License for more details.
  */
 
 package com.barroncraft.sce;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import net.sacredlabyrinth.phaed.simpleclans.*;
 import net.sacredlabyrinth.phaed.simpleclans.managers.*;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 
 public class SimpleClansExtensions extends JavaPlugin
 {
     public ClanManager clanManager;
     public WorldGuardPlugin guardManager;
     public RegionManager regionManager;
     private ExtensionsCommand commandManager;
 
     public Map<String, ClanTeam> clanTeams;
     public int maxDifference;
     public long maxTimeEmpty;
     public Logger log;
 
 
     public void onEnable()
     {
         log = this.getLogger();
         clanTeams = new HashMap<String, ClanTeam>();
 
         PluginManager manager = getServer().getPluginManager();
 
         Plugin clansPlugin = manager.getPlugin("SimpleClans");
         if (clansPlugin == null)
         {
             log.severe("SimpleClans plugin not found.  SimpleClansExtenisons was not enabled.");
             return;
         }
 
         Plugin guardPlugin = manager.getPlugin("WorldGuard");
         if (guardPlugin == null)
         {
             log.severe("WorldGuard plugin not found.  SimpleClansExtenisons was not enabled.");
             return;
         }
 
         clanManager = ((SimpleClans)clansPlugin).getClanManager();
         guardManager = (WorldGuardPlugin)guardPlugin;
         commandManager = new ExtensionsCommand(this);
 
         log.info("Loading Config File...");
         FileConfiguration config = this.getConfig();
         config.options().copyDefaults(true);
 
         maxDifference = config.getInt("joinDifference");
         log.info("joinDifference: " + maxDifference);
 
         maxTimeEmpty = config.getLong("maxTimeEmpty") * 1000;
         log.info("maxTimeEmpty: " + maxTimeEmpty);
 
         World world = this.getServer().getWorld(config.getString("world"));
         log.info("world: " + world.getName());
 
         Set<String> clans = config.getConfigurationSection("clans").getKeys(false);
         log.info("Clans (" + clans.size() + "):");
         for (String clan : clans)
         {
             log.info("  " + clan);
             clanTeams.put(clan, new ClanTeam(
                         clan, 
                         ChatColor.valueOf(clan.toUpperCase()),
                         new Location(world, 
                             config.getInt("clans." + clan + ".spawn.x"),
                             config.getInt("clans." + clan + ".spawn.y"),
                             config.getInt("clans." + clan + ".spawn.z")
                             ),
                         config.getString("clans." + clan + ".baseRegion"),
                         config.getString("clans." + clan + ".spawnRegion")
                         ));
         }
 
         new ExtensionsListener(this, world);
 
         log.info("SimpleClanExtensions has been enabled");
     }
 
     public void onDisable()
     {
         log.info("SimpleClanExtensions has been disabled");
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
         if (!(sender instanceof Player))
             return false;
 
         Player player = (Player)sender;
         String commandName = cmd.getName();
         if (commandName.equalsIgnoreCase("sce"))
         {
             if (clanManager == null)
             {
                 player.sendMessage(ChatColor.RED + "SimpleClans plugin not found...");
                 return true;
             }
             else if (args.length == 2 && args[0].equalsIgnoreCase("join"))
             {
                 /*if (!player.hasPermission("sce.join"))
                   player.sendMessage(ChatColor.RED + "You don't have permission to join teams...");
 
                   else*/ if (!clanTeams.containsKey(args[1]))
                 player.sendMessage(ChatColor.RED + "The clan " + args[1] + " doesn't exist.");
                 else
                     commandManager.CommandJoin(player, args[1]);
                 return true;
             }
             else if (args.length == 1 && args[0].equalsIgnoreCase("surrender"))
             {
                 commandManager.CommandSurrender(player);
                 return true;
             }
         }
 
         return false;
     }
 
 
 }
