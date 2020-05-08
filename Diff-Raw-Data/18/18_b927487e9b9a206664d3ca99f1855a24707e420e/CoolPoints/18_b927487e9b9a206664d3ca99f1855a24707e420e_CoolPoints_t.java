 package com.gmail.ne0nx3r0.coolpoints;
 
 import com.gmail.ne0nx3r0.coolpoints.commands.CoolPointsCommandExecutor;
 import com.gmail.ne0nx3r0.coolpoints.listeners.CoolPointsPlayerListener;
 import com.gmail.ne0nx3r0.coolpoints.points.PlayerProfileManager;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
import java.sql.Date;
 import java.util.Calendar;
 import java.util.logging.Level;
 import lib.PatPeter.SQLibrary.SQLite;
 import net.h31ix.updater.Updater;
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CoolPoints extends JavaPlugin
 {
     public static Plugin self;
     public static PlayerProfileManager ppm;
     public static SQLite sqlite;
     public boolean UPDATE_AVAILABLE = false;
     public String UPDATE_NAME;
 
     @Override
     public void onEnable()
     {        
         CoolPoints.self = this;
         
 // Load config
         File configFile = new File(this.getDataFolder(), "config.yml");   
         
         if(!configFile.exists()){
             configFile.getParentFile().mkdirs();
             copy(this.getResource(configFile.getName()), configFile);
         }        
         
 // DB Setup
         sqlite = new SQLite(
             getLogger(),
             "CoolPoints",
             "points",
             this.getDataFolder().getAbsolutePath()
         );
         
         try 
         {
             sqlite.open();
         }
         catch(Exception e)
         {
             getLogger().info(e.getMessage());
             getPluginLoader().disablePlugin(this);
         }
         
         if(!sqlite.checkTable("player"))
         {
             sqlite.query("CREATE TABLE player("
                 + "username VARCHAR(16) PRIMARY KEY,"
                 + "points INT,"
                 + "giftedToday INT,"
                 + "receivedAllowanceToday INT,"
                 + "firstJoined INT"
             + ");");
             
             sqlite.query("CREATE TABLE points_given("
                 + "created INT PRIMARY KEY,"
                 + "fromName VARCHAR(16) PRIMARY KEY,"
                 + "toName VARCHAR(16) PRIMARY KEY,"
                 + "amount INT"
             + ");");
 
             getLogger().info("Cool Points tables created.");
         }
         
         ppm = new PlayerProfileManager();
         
 //register events
         getServer().getPluginManager().registerEvents(new CoolPointsPlayerListener(this), this);
         
 //register commands
         getCommand("cp").setExecutor(new CoolPointsCommandExecutor());
         
 //Setup daily point reset thingy
         this.getServer().getScheduler().scheduleSyncRepeatingTask(self,new Runnable()
         {
             @Override 
             public void run()
            {
                if(Calendar.getInstance().getTime().getHours() == 0)
                 {
                     Bukkit.broadcastMessage("[SERVER] Resetting CoolPoint daily stats!");
 
                     CoolPoints.ppm.dailyReset();
                 }
             }
         }, 
         20*60*60,20*60*60);
         
         if(getConfig().getBoolean("update-notifications"))
         {
             Updater updater = new Updater(
                     this,
                     "coolpoints",
                     this.getFile(),
                     Updater.UpdateType.NO_DOWNLOAD,
                     false); // Start Updater but just do a version check
 
             UPDATE_AVAILABLE = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
             
             if(UPDATE_AVAILABLE)
             {
                 UPDATE_NAME = updater.getLatestVersionString();
                 
                 getLogger().log(Level.INFO,"--------------------------------");
                 getLogger().log(Level.INFO,"    An update is available:");
                 getLogger().log(Level.INFO,"    "+UPDATE_NAME);
                 getLogger().log(Level.INFO,"--------------------------------");
             }
         }
     }
 
     @Override
     public void onDisable()
     {
         sqlite.close();
     }
     
     private void copy(InputStream in, File file) {
         try {
             OutputStream out = new FileOutputStream(file);
             byte[] buf = new byte[1024];
             int len;
             while((len=in.read(buf))>0){
                 out.write(buf,0,len);
             }
             out.close();
             in.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
