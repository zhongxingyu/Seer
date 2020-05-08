 package com.acyrid.SunSteel;
 
 import com.acyrid.SunSteel.listeners.*;
import com.acyrid.SunSteel.utils.SSConfig;
 import com.acyrid.SunSteel.utils.SSMechanics;
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import java.io.File;
 import java.util.logging.Level;
 
import static java.nio.file.Files.delete;
import static java.nio.file.Files.deleteIfExists;

 public class SunSteel extends JavaPlugin{
 
     private SSPlayerListener playerListener = new SSPlayerListener(this);
     private SSDamageListener damageListener = new SSDamageListener(this);
     private SSBlockListener blockListener = new SSBlockListener(this);
     private SSEntityListener entityListener = new SSEntityListener(this);
 
     public void onDisable(){
         this.getLogger().log(Level.INFO, "is Disabled!");
     }                                                    
     public void onEnable(){
        load();
         getConfig();
         this.registerEvents();
         SSMechanics.init(this);
         this.getLogger().log(Level.INFO, "is now enabled with more solar flares then ever!");
         if(!SSMechanics.getNoCheatInstalled()){
         this.getLogger().log(Level.INFO, "Based on the config you do not have Essentials Anti-Cheat, or NoCheat installed.");
         }else{this.getLogger().log(Level.INFO, "Based on the config you have Essentials Anti-Cheat, or NoCheat installed. Play-Nice mode ENFORCED.");}
         getConfig().getKeys(true);
     }
    private void load(){
        if(!new File(this.getDataFolder(), "config.yml").exists()){
            saveDefaultConfig();
        }else if(this.getDescription().getVersion()!=this.getConfig().getString(SSConfig.configCheck)){
             saveDefaultConfig();
        }
    }
     private void registerEvents() {
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(this.playerListener, this);
         pm.registerEvents(this.blockListener, this);
         pm.registerEvents(this.damageListener, this);
         pm.registerEvents(this.entityListener, this);
 
     }
 
 
 }
