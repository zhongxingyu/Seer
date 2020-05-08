 package com.matejdro.bukkit.monsterhunt;
 
 import java.util.logging.Logger;
 
 import javax.swing.Timer;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.matejdro.bukkit.monsterhunt.commands.HuntCommand;
 import com.matejdro.bukkit.monsterhunt.commands.HuntReloadCommand;
 import com.matejdro.bukkit.monsterhunt.commands.HuntScoreCommand;
 import com.matejdro.bukkit.monsterhunt.commands.HuntStartCommand;
 import com.matejdro.bukkit.monsterhunt.commands.HuntStatusCommand;
 import com.matejdro.bukkit.monsterhunt.commands.HuntStopCommand;
 import com.matejdro.bukkit.monsterhunt.commands.HuntTeleCommand;
 import com.matejdro.bukkit.monsterhunt.commands.HuntZoneCommand;
 import com.matejdro.bukkit.monsterhunt.listeners.MonsterHuntListener;
 
 public class MonsterHunt extends JavaPlugin {
     public static Logger log = Logger.getLogger("Minecraft");
     private MonsterHuntListener entityListener;
     Timer timer;
 
     //public static HashMap<String,Integer> highscore = new HashMap<String,Integer>();
 
     public static MonsterHunt instance;
 
     @Override
     public void onDisable() {
         for (MonsterHuntWorld world : HuntWorldManager.getWorlds())
             world.stop();
     }
 
     @Override
     public void onEnable() {
         PluginDescriptionFile pdfFile = getDescription();
         String version = pdfFile.getVersion();
         this.getLogger().info("v" + version + "Loaded!");
         initialize();
 
         InputOutput.LoadSettings();
         InputOutput.PrepareDB();
 
         getServer().getPluginManager().registerEvents(entityListener, this);
 
         this.getCommand("hunt").setExecutor(new HuntCommand());
         this.getCommand("huntscore").setExecutor(new HuntScoreCommand());
         this.getCommand("huntstart").setExecutor(new HuntStartCommand());
         this.getCommand("huntstatus").setExecutor(new HuntStatusCommand());
         this.getCommand("huntstop").setExecutor(new HuntStopCommand());
         this.getCommand("huntzone").setExecutor(new HuntZoneCommand());
         this.getCommand("hunttele").setExecutor(new HuntTeleCommand());
         this.getCommand("huntreload").setExecutor(new HuntReloadCommand());
         HuntWorldManager.timer();
 
     }
 
     private void initialize() {
         entityListener = new MonsterHuntListener();
         instance = this;
     }
 
 }
