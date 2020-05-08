 package com.github.UnluckyNinja.signplus;
 
 import com.github.UnluckyNinja.signplus.listeners.SignsListener;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SignPlus extends JavaPlugin {
     private Configuration config;
     private SignsManager SM = SignsManager.getInstance(this);
     int i;
     
     @Override
     public  void onEnable(){
         config = this.getConfig();
         config.options().copyDefaults(true);
         getServer().getPluginManager().registerEvents(new SignsListener(this), this);
        getLogger().info("[SignPlus] SignPlus has been enabled.");
     }
     @Override
     public void onDisable(){
        getLogger().info("[SignPlus] SignPlus has been disabled.");
     }
    public SignsManager getSignsManager(){
         return SM;
     }
 }
