 /*
  * Fireworks display on levelling up.
  * Copyright (C) 2013 Andrew Stevanus (Hoot215) <hoot893@gmail.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package me.Hoot215.LevelFlare;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import me.Hoot215.LevelFlare.api.Leveller;
 import me.Hoot215.LevelFlare.api.LevellerManager;
 import me.Hoot215.LevelFlare.metrics.BukkitMetrics;
 import me.Hoot215.updater.AutoUpdater;
 import me.Hoot215.updater.AutoUpdaterQueue;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Color;
 import org.bukkit.FireworkEffect;
 import org.bukkit.FireworkEffect.Type;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Firework;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.meta.FireworkMeta;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class LevelFlare extends JavaPlugin
   {
     private LevelFlarePlayerListener playerListener;
     private LevellerManager levellerManager;
     private AutoUpdaterQueue autoUpdaterQueue;
     private static final Color[] COLOURS = {Color.AQUA, Color.BLACK,
         Color.BLUE, Color.FUCHSIA, Color.GRAY, Color.GREEN, Color.LIME,
         Color.MAROON, Color.NAVY, Color.OLIVE, Color.ORANGE, Color.PURPLE,
         Color.RED, Color.SILVER, Color.TEAL, Color.WHITE, Color.YELLOW};
     
     public LevellerManager getLevellerManager ()
       {
         return levellerManager;
       }
     
     public void launchFireworks (Player player)
       {
         FileConfiguration config = this.getConfig();
         
         for (String s : config.getStringList("fireworks"))
           {
             boolean flicker;
             boolean trail;
             Type type;
             List<Color> colours = new ArrayList<Color>();
             List<Color> fades = new ArrayList<Color>();
             if (s.equals("RANDOM"))
               {
                 flicker = new Random().nextBoolean();
                 trail = new Random().nextBoolean();
                 type =
                     Type.values()[new Random().nextInt(Type.values().length)];
                 for (int i = 0; i < 3; i++)
                   {
                     colours.add(COLOURS[new Random().nextInt(COLOURS.length)]);
                     fades.add(COLOURS[new Random().nextInt(COLOURS.length)]);
                   }
               }
             else
               {
                 if (config.getConfigurationSection(s) == null)
                   {
                     this.getLogger().warning(
                         "Firework '" + s + "' does not exist in the config!");
                     continue;
                   }
                 flicker = config.getBoolean(s + ".flicker");
                 trail = config.getBoolean(s + ".trail");
                 String typeString = config.getString(s + ".type");
                 try
                   {
                     type = Type.valueOf(typeString);
                   }
                 catch (IllegalArgumentException e)
                   {
                     this.getLogger().warning(
                         "Invalid type specified for firework '" + s + "'!");
                     this.getLogger().warning("Using BALL instead");
                     type = Type.BALL;
                   }
                 catch (NullPointerException e)
                   {
                     this.getLogger().warning(
                         "Invalid type specified for firework '" + s + "'!");
                     this.getLogger().warning("Using BALL instead");
                     type = Type.BALL;
                   }
                 for (String t : config.getStringList(s + ".colours"))
                   {
                     try
                       {
                         colours.add((Color) Color.class.getField(t).get(null));
                       }
                     catch (IllegalArgumentException e)
                       {
                         e.printStackTrace();
                       }
                     catch (IllegalAccessException e)
                       {
                         e.printStackTrace();
                       }
                     catch (NoSuchFieldException e)
                       {
                         this.getLogger().warning(
                             "Invalid colour '" + t
                                 + "' specified for firework '" + s + "'!");
                         this.getLogger().warning("Using WHITE instead");
                         colours.add(Color.WHITE);
                       }
                     catch (SecurityException e)
                       {
                         e.printStackTrace();
                       }
                   }
                 for (String t : config.getStringList(s + ".fades"))
                   {
                     try
                       {
                         fades.add((Color) Color.class.getField(t).get(null));
                       }
                     catch (IllegalArgumentException e)
                       {
                         e.printStackTrace();
                       }
                     catch (IllegalAccessException e)
                       {
                         e.printStackTrace();
                       }
                     catch (NoSuchFieldException e)
                       {
                         this.getLogger().warning(
                             "Invalid fade '" + t + "' specified for firework '"
                                 + s + "'!");
                         this.getLogger().warning("Using WHITE instead");
                         fades.add(Color.WHITE);
                       }
                     catch (SecurityException e)
                       {
                         e.printStackTrace();
                       }
                   }
               }
             Firework firework =
                 (Firework) player.getWorld().spawnEntity(player.getLocation(),
                     EntityType.FIREWORK);
             FireworkMeta meta = firework.getFireworkMeta();
             FireworkEffect effect =
                 FireworkEffect.builder().flicker(flicker).trail(trail)
                     .with(type).withColor(colours).withFade(fades).build();
             meta.addEffect(effect);
             firework.setFireworkMeta(meta);
           }
       }
     
     public boolean onCommand (CommandSender sender, Command cmd,
       String commandLabel, String[] args)
       {
         if (cmd.getName().equals("levelflare"))
           {
             if (sender.hasPermission(cmd.getPermission()))
               {
                 if (args.length == 0)
                   {
                     sender.sendMessage(ChatColor.GREEN + "/" + commandLabel
                         + ChatColor.WHITE + " - Displays LevelFlare help");
                     sender.sendMessage(ChatColor.GREEN + "/" + commandLabel
                         + " reload" + ChatColor.WHITE
                         + " - Reloads config file");
                     sender.sendMessage(ChatColor.GREEN + "/" + commandLabel
                         + " update" + ChatColor.WHITE
                         + " - Checks for a newer version of LevelFlare");
                     sender.sendMessage(ChatColor.GREEN + "/" + commandLabel
                         + " levellers" + ChatColor.WHITE
                         + " - Lists loaded levellers");
                     sender.sendMessage(ChatColor.GREEN + "/" + commandLabel
                         + " load <name>" + ChatColor.WHITE
                         + " - Loads leveller <name>.jar");
                     sender.sendMessage(ChatColor.GREEN + "/" + commandLabel
                         + " unload <name>" + ChatColor.WHITE
                         + " - Unloads leveller <name>.jar");
                     return true;
                   }
                 if (args.length == 1)
                   {
                     if (args[0].equalsIgnoreCase("reload"))
                       {
                         if (sender.hasPermission("levelflare.reload"))
                           {
                             this.reloadConfig();
                             sender.sendMessage(ChatColor.GREEN
                                 + "Config reloaded!");
                             return true;
                           }
                         sender.sendMessage(cmd.getPermissionMessage());
                         return true;
                       }
                     if (args[0].equalsIgnoreCase("update"))
                       {
                         if (sender.hasPermission("levelflare.update"))
                           {
                             sender.sendMessage(ChatColor.BLUE
                                 + "Checking for a newer version "
                                 + "of LevelFlare...");
                             autoUpdaterQueue.add(sender);
                             return true;
                           }
                         sender.sendMessage(cmd.getPermissionMessage());
                         return true;
                       }
                     if (args[0].equalsIgnoreCase("levellers"))
                       {
                         if (sender.hasPermission("levelflare.levellers"))
                           {
                             sender.sendMessage(ChatColor.GREEN
                                 + "Currently loaded levellers:");
                             for (String s : this.getLevellerManager()
                                 .getLevellerNames())
                               {
                                 if (s == null)
                                   break;
                                 sender.sendMessage(ChatColor.DARK_GREEN + s);
                               }
                             return true;
                           }
                         sender.sendMessage(cmd.getPermissionMessage());
                         return true;
                       }
                     else
                       return false;
                   }
                 if (args.length == 2)
                   {
                     if (args[0].equalsIgnoreCase("load"))
                       {
                         if (sender.hasPermission("levelflare.load"))
                           {
                             String levellerName = args[1];
                             Leveller leveller =
                                 this.getLevellerManager().loadLeveller(
                                     levellerName);
                             if (leveller != null)
                               {
                                 sender.sendMessage(ChatColor.GREEN
                                     + "Leveller '" + levellerName
                                     + "' loaded successfully");
                               }
                             else
                               {
                                 sender.sendMessage(ChatColor.RED + "Leveller '"
                                     + levellerName
                                     + "' doesn't exist or is already loaded");
                               }
                             return true;
                           }
                         sender.sendMessage(cmd.getPermissionMessage());
                         return true;
                       }
                     if (args[0].equalsIgnoreCase("unload"))
                       {
                         if (sender.hasPermission("levelflare.unload"))
                           {
                             String levellerName = args[1];
                             if (this.getLevellerManager().unloadLeveller(
                                 levellerName))
                               {
                                 sender.sendMessage(ChatColor.GREEN
                                     + "Leveller '" + levellerName
                                     + "' unloaded successfully");
                               }
                             else
                               {
                                 sender.sendMessage(ChatColor.RED + "Leveller '"
                                     + levellerName + "' isn't loaded");
                               }
                             return true;
                           }
                         sender.sendMessage(cmd.getPermissionMessage());
                         return true;
                       }
                     else
                       return false;
                   }
                 return false;
               }
             sender.sendMessage(cmd.getPermissionMessage());
             return true;
           }
         return false;
       }
     
     @Override
     public void onDisable ()
       {
         // Levellers
         this.getLogger().info("Unloading levellers...");
         this.getLevellerManager().unloadLevellers();
         this.getLogger().info("Levellers unloaded");
         
        // Auto updater
        autoUpdaterQueue.add(null);
        
         this.getLogger().info("Is now disabled");
       }
     
     @Override
     public void onEnable ()
       {
         this.saveDefaultConfig();
         this.getConfig().options().copyDefaults(true);
         this.saveConfig();
         
         String version = this.getServer().getVersion();
         int start = version.lastIndexOf("-b") + 2;
         int stop = version.lastIndexOf("jnks");
         int build = 0;
         try
           {
             build = Integer.valueOf(version.substring(start, stop));
             if (build < 2565)
               {
                 if (this.getConfig().getBoolean("allow-old-build"))
                   {
                     this.getLogger().warning(
                         "You are running an old version of Craftbukkit "
                             + "but have forced the plugin to load");
                     this.getLogger().warning(
                         "The plugin may not work correctly");
                   }
                 else
                   {
                     this.getLogger()
                         .severe(
                             "You seem to be running an old "
                                 + "version of Craftbukkit! Build 2565 or higher is "
                                 + "required to use this plugin!");
                     this.getLogger()
                         .severe(
                             "Disabling plugin! You can force "
                                 + "the plugin to load by setting allow-old-build to "
                                 + "true in the config file");
                     this.getServer().getPluginManager().disablePlugin(this);
                     return;
                   }
               }
           }
         catch (NumberFormatException e)
           {
             this.getLogger()
                 .warning(
                     "You seem to be running an unusual version "
                         + "of Craftbukkit");
             this.getLogger().warning(
                 "Please make sure that it is compatible with the changes "
                     + "from build 2565+!");
           }
         catch (IndexOutOfBoundsException e)
           {
             this.getLogger()
                 .warning(
                     "You seem to be running an unusual version "
                         + "of Craftbukkit");
             this.getLogger().warning(
                 "Please make sure that it is compatible with the changes "
                     + "from build 2565+!");
           }
         if (build != 0)
           {
             this.getLogger().info("Detected build " + String.valueOf(build));
           }
         
         playerListener = new LevelFlarePlayerListener(this);
         this.getServer().getPluginManager()
             .registerEvents(playerListener, this);
         
         try
           {
             BukkitMetrics metrics = new BukkitMetrics(this);
             metrics.start();
           }
         catch (IOException e)
           {
             e.printStackTrace();
           }
         
         // Levellers
         levellerManager = new LevellerManager(this);
         this.getLogger().info("Loading levellers...");
         levellerManager.loadLevellers();
         this.getLogger().info("Levellers loaded");
         
         // Auto updater
         AutoUpdater autoUpdater = new AutoUpdater(this);
         autoUpdater.start();
         autoUpdaterQueue = new AutoUpdaterQueue(autoUpdater, this.getName());
         autoUpdaterQueue.start();
         
         this.getLogger().info("Is now enabled");
       }
   }
