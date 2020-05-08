 package com.notoriousdev.irmc;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import com.notoriousdev.irmc.irc.Bot;
 
 public class IRMC extends JavaPlugin {
 
     private Bot bot;
 
     @Override
     public void onDisable() {
         getLogger().info("IRMC bot is disconnecting...");
         bot.disconnect();
         getLogger().info("IRMC Successfully disabled");
     }
 
     @Override
     public void onEnable() {
         saveDefaultConfig();
         getLogger().info("IRMC bot is connecting...");
         bot.connect();
         getLogger().info("IRMC Successfully enabled");
     }
 }
