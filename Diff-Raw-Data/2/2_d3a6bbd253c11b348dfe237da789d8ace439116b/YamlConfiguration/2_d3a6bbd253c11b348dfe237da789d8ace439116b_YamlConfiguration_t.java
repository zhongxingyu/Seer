 package me.drakken.rankplate;
 
 import java.util.*;
 import org.bukkit.command.*;
 import org.bukkit.event.*;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.configuration.file.FileConfiguration;
  
  
public class YamlConfiguration
  
     @Override
     public void onEnable() {
         // Save a copy of the default config.yml if one is not there
         this.saveDefaultConfig();
  
         // Register a new listener
         getServer().getPluginManager().registerEvents(new Listener() {
  
             @EventHandler
             public playerJoin(PlayerJoinEvent event) {
                 // On player join send them the message from config.yml
                 event.getPlayer().sendMessage(YamlConfiguration.this.getConfig().getString("message"));
             }
         }, this);
  
         // Set the command executor for the rules command
         this.getCommand("rules").setExecutor(new CommandExecutor() {
  
             public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                 // On command send the rules from config.yml to the sender of the command
                 List<String> rules = YamlConfiguration.this.getConfig().getStringList("rules");
                 for (String s : rules)
                     sender.sendMessage(s);
                 }
                 return true;
             }
         });
     }
