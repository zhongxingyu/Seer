 package psy.ActivityHistory;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import psy.ActivityHistory.cmd.DatabasePQCE;
 import psy.ActivityHistory.cmd.DisabledGQCE;
 import psy.ActivityHistory.cmd.DisabledPQCE;
 import psy.ActivityHistory.cmd.FileGQCE;
 import psy.ActivityHistory.cmd.FilePQCE;
 import psy.ActivityHistory.cmd.GroupQueryCommandExecutor;
 import psy.ActivityHistory.cmd.PlayerQueryCommandExecutor;
 
 public class ActivityHistory extends JavaPlugin{
     private static final Logger logger = Logger.getLogger("Minecraft");
     /**
      * The Permissions handler. Null unless {@link vaultEnabled} is true.
      */
     public static Permission perms = null;
     /**
      * The plugin configuration.
      */
     private FileConfiguration config;
     /**
      * Stores whether Vault was detected when enabling.
      */
     public static boolean vaultEnabled;
     /**
      * The {@link CommandExecutor} in charge of player queries
      */
     PlayerQueryCommandExecutor playerExec;
     /**
      * The {@link CommandExecutor} in charge of group queries
      */
     GroupQueryCommandExecutor groupExec;
     public static DatabaseManager dbm;
     /**
      * The info and error messages as defined by the localization file in use.
      */
     public static YamlConfiguration messages;
     
     @Override
     public void onEnable(){
         saveDefaultConfig();
         config = this.getConfig();
         vaultEnabled = (this.getServer().getPluginManager().getPlugin("Vault")) != null;
         
         //Load the localization of messages
         InputStream stream = getResource(config.getString("general.language").toLowerCase() + ".yml");
         if (stream != null)
             messages = YamlConfiguration.loadConfiguration(stream);
         
         String logpath = config.getString("logFilesLocation");
         if(logpath==null) logpath="plugins/ActivityHistory/logs";
         File logfolder = new File(logpath);
         if(!logfolder.exists()) logfolder.mkdir();
         
         if(config.getBoolean("players.enabled")){
             if(config.getString("general.storageType").equalsIgnoreCase("file"))
                 playerExec = new FilePQCE(this);
             else if(config.getString("general.storageType").equalsIgnoreCase("sql"))
                 try{    
                     dbm = new DatabaseManager(this);
                     playerExec = new DatabasePQCE(this);
                 }catch(SQLException e){
                     logger.log(Level.SEVERE, messages.getString("errors.dbConnect"));
                     dbm = null;
                    playerExec = new DisabledPQCE(this);
                 }
         }else
             playerExec = new DisabledPQCE(this);
             
         if(vaultEnabled)
             setupPermissions();
         if(config.getBoolean("players.enabled") || (vaultEnabled && config.getBoolean("groups.enabled")))
             scheduleSurvey();
             
         if(vaultEnabled && config.getBoolean("groups.enabled"))
             groupExec = new FileGQCE(this);
         else
             groupExec = new DisabledGQCE(this);
         
         getCommand("ppercent").setExecutor(playerExec);
         getCommand("ptotal").setExecutor(playerExec);
         getCommand("phours").setExecutor(playerExec);
         
         try {
             Metrics metrics = new Metrics(this);
             metrics.start();
         } catch (IOException e) {
         }
     }
     
     public FileConfiguration accessConfig(){
         return config;
     }
     
     private boolean setupPermissions() {
         RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
         perms = rsp.getProvider();
         return perms != null;
     }
     
     @SuppressWarnings("deprecation")
     private void scheduleSurvey(){
         Date time = new Date();
         int minute = time.getMinutes();
         int offset = 0;
         int interval = config.getInt("general.surveyInterval");
         //Set initial delay to the amount of time until the time is divisible by interval
         do{
             minute++;
             offset++;
         }while(minute%interval != 0);
         offset *= 60;
         offset -= time.getSeconds();
         offset *= 20;
         if(config.getString("general.storageType").equalsIgnoreCase("file")){
             this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new FileSurveyer(this), offset, (interval*60*20));
         }else if(config.getString("general.storageType").equalsIgnoreCase("sql") && dbm != null){
             this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, dbm.getSurveyer(), offset, (interval*60*20));
         }
     }
 }
 
