 package com.github.CubieX.Assignment;
 
 import java.io.File;
 import java.sql.ResultSet;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import net.milkbowl.vault.economy.Economy;
 import com.alta189.sqlLibrary.SQLite.sqlCore;
 
 public class Assignment extends JavaPlugin implements Listener
 {    
     private static final Logger log = Logger.getLogger("Minecraft");
     public static String logPrefix = "[Assignment] "; // Prefix to go in front of all log entries
     private ASSConfigHandler cHandler = null;
     private ASSEntityListener eListener = null;
     private ASSSchedulerHandler schedHandler = null;
     private ASSCommandHandler comHandler = null;
     public static Economy econ = null;
     public File pFolder = new File("plugins" + File.separator + "Assignment"); // Folder to store plugin settings file and database
     public sqlCore manageSQLite; // SQLite handler
 
     private Player assigner = null;
     static String completedAssTag = "<E>";
     static String openAssignmentTitle = "<A>";
     static String rightClickText = "rechtsklicken!";
     static String assignmentStateCompleted = "completed";
     private boolean debug = false;
     public Boolean blockNextBlockPlacing = false;
 
     //************************************************
     static String usedConfigVersion = "2"; // Update this every time the config file version changes, so the plugin knows, if there is a suiting config present
     //************************************************
 
     @Override
     public void onEnable()
     {  
         cHandler = new ASSConfigHandler(this);
 
         if (!setupEconomy())               
         {
             log.severe(logPrefix + "will be disabled now. Vault was not found!");
             disablePlugin();
             return;
         }
 
         if(!checkConfigFileVersion())
         {
             log.severe(logPrefix + "Outdated or corrupted config file. Please delete your current config file, so Assignment can create a new one!");
             log.severe(logPrefix + "will be disabled now. Config file is outdated or corrupted.");
             disablePlugin();
             return;
         }
 
         log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is enabled!");
 
         eListener = new ASSEntityListener(this, log);
         schedHandler = new ASSSchedulerHandler(this);
         comHandler = new ASSCommandHandler(this, log, cHandler);
         getCommand("ass").setExecutor(comHandler);
 
         setStaticValues();
 
         // Initializing SQLite ++++++++++++++++++++++++++++++++++++++++++++
         log.info(logPrefix + "SQLite Initializing");
 
         // Declare SQLite handler
         this.manageSQLite = new sqlCore(log, "AssignmentDB", pFolder.getPath());
 
         // Initialize SQLite handler
         this.manageSQLite.initialize();
 
         // Check if the table exists, if it doesn't create it
         if (!this.manageSQLite.checkTable("signs")) {
             log.info(logPrefix + "Creating table signs");
             String query = "CREATE TABLE signs (id INTEGER PRIMARY KEY AUTOINCREMENT, assigner VARCHAR(32) NOT NULL, x int NOT NULL, y int NOT NULL, z int NOT NULL, world VARCHAR(32) NOT NULL, reward int NOT NULL, state VARCHAR(32) NOT NULL)";
             this.manageSQLite.createTable(query); // Use sqlCore.createTable(query) to create tables 
         }
 
         schedHandler.startCleanupScheduler_SyncRep();
     }
 
     public void setStaticValues()
     {
         completedAssTag = this.getConfig().getString("CompletedAssignmentTag");
         openAssignmentTitle = this.getConfig().getString("OpenAssignmentTitle");
         rightClickText = this.getConfig().getString("RightClickText");
     }
 
     private boolean setupEconomy() 
     {
         if (getServer().getPluginManager().getPlugin("Vault") == null) {
             return false;
         }
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
             return false;
         }
         econ = rsp.getProvider();
         return econ != null;
     }
 
     private boolean checkConfigFileVersion()
     {
         boolean res = false;
        String configVersion = this.getConfig().getString("config_version");
 
        if(configVersion.equals(usedConfigVersion))
         {
            res = true;
        }        
 
         return (res);
     }
 
     void disablePlugin()
     {
         getServer().getPluginManager().disablePlugin(this);        
     }
 
     @Override
     public void onDisable()
     {
         assigner = null;
         eListener = null;
         cHandler = null;
         econ = null;
         pFolder = null;
         manageSQLite = null;
         schedHandler = null;
         comHandler = null;
         log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
     }        
 
     public void cleanupAssignmentsInDB(CommandSender sender)
     {        
         // order is mandatory! First clause prevents exception if sender is null for second statement (will then not be evaluated).
         if((null == sender) ||
                 sender.hasPermission("assignment.*"))
         {          
             int signCount = 0;
             int deletedCount = 0;                        
             String query = "SELECT COUNT(*) as 'count' FROM signs";
             ResultSet resSet = null;
             //World currentWorld = sender.getName()
             try
             {
                 resSet = this.manageSQLite.sqlQuery(query);
                 resSet.next();       //set pointer to first row                     
                 signCount = resSet.getInt(resSet.getRow());
             }
             catch(Exception e)
             {
                 if(null != sender) // is null if called by scheduler
                 {
                     sender.sendMessage(ChatColor.YELLOW + "Die Datenbank scheint leer zu sein.");
                 }
                 else
                 {
                     log.info(logPrefix + "Database seems to be empty.");
                 }
             }
 
             if(signCount > 0)
             {                           
                 query = "SELECT x, y, z, world FROM signs";
                 try
                 {
                     resSet = this.manageSQLite.sqlQuery(query);
                     resSet.next();  //set pointer to first row
 
                     Location loc;
                     boolean isAssSign = false;                                
 
                     Block blToDel;
 
                     for(int i = 0; i < signCount; i++)
                     {
                         isAssSign = false;
                         loc = new Location(getServer().getWorld(resSet.getString("world")),resSet.getDouble("x"),resSet.getDouble("y"),resSet.getDouble("z"));
                         blToDel = loc.getBlock();
 
                         //check if block is a sign, and if not, delete it from db.
                         if((blToDel.getTypeId() == 63) || // is there a sign post or wall sign?
                                 (blToDel.getTypeId() == 68))
                         {              
                             Sign sign = (Sign) blToDel.getState();
 
                             if(sign.getLine(0).contains("<A>") || sign.getLine(0).contains("<a>") || sign.getLine(0).contains("<" + completedAssTag + ">") || sign.getLine(0).contains("<" + openAssignmentTitle + ">")) // is Assignment sign?
                             {
                                 isAssSign = true;
                             }                                                                            
                         }                        
 
                         if(!isAssSign)
                         {   
                             query = "DELETE FROM signs WHERE x=" + resSet.getInt("x") + " AND y=" + resSet.getInt("y") + " AND z=" + resSet.getInt("z") + " AND world='" + resSet.getString("world") +"'";
                             this.manageSQLite.deleteQuery(query);                                       
                             deletedCount++;   
                         }
 
                         resSet.next();  //set pointer to next row
                     }
                     if(null != sender) // is null if called by scheduler
                     {
                         sender.sendMessage(ChatColor.GREEN + "Es wurden " + deletedCount + " ungueltige Auftraege aus der DB geloescht.");
                     }
                     else // is null if called by scheduler
                     {
                         log.info(logPrefix + deletedCount + " invalid sign entries have been deleted from the database.");
                     }
                 }
                 catch(Exception e)
                 {
                     log.severe(logPrefix + e.getMessage());
                 }
             }
             else
             {
                 sender.sendMessage(ChatColor.GREEN + "Es wurden keine ungueltigen Auftraege gefunden.");
             }
         }
         else
         {
             if(null != sender) // is null if called by scheduler
             {
                 sender.sendMessage(ChatColor.RED + "You du not have sufficient permission to cleanup the database");
             }
         }        
     } //END cleanup
 
     //==============================================================================
     // database queries
     public Player getAssignerFromDB(int x, int y, int z, String world)
     {
         String query = "SELECT assigner FROM signs WHERE x='" + x + "' AND y='" + y + "' AND z='" + z+ "' AND world='" + world + "'";        
 
         try
         {            
             ResultSet queryRes = this.manageSQLite.sqlQuery(query);
             queryRes.next(); //move cursor to first row of result (should only have one though) 
             if(queryRes.getRow() == 1) // query has a result, so it's a registeres sign
             { 
                 String assignerName = queryRes.getString("assigner");
                 assigner = Bukkit.getServer().getPlayer(assignerName); //ACHTUNG: DAS GEHT NUR GUT, WENN DER ASSIGNER ONLINE IST!!!
                 //Wenn er auch nicht online sein kann, dann die Methode "getAssignerNameFromDB" nutzen!
                 if(this.getConfig().getBoolean("debug")){log.info("Schild ist registriertes Assignment Schild");}  
             }
         }
         catch (Exception e)
         {
             assigner = null;            
         }       
         return assigner;
     }
 
     public String getAssignerNameFromDB(int x, int y, int z, String world)
     {
         String assignerName = "";
         String query = "SELECT assigner FROM signs WHERE x=" + x + " AND y=" + y + " AND z=" + z+ " AND world='" + world + "'";        
 
         try
         {            
             ResultSet queryRes = this.manageSQLite.sqlQuery(query);
             queryRes.next(); //move cursor to first row of result (should only have one though) 
             if(queryRes.getRow() == 1) // query has a result, so it's a registeres sign
             { 
                 assignerName = queryRes.getString("assigner");                    
                 if(this.getConfig().getBoolean("debug")){log.info("Schild ist registriertes Assignment Schild");}  
             }
         }
         catch (Exception e)
         {
             assignerName = "";            
         }       
         return assignerName;
     }
 
     public void deleteSignFromDB(int x, int y, int z, String world)
     {
         String query = "DELETE FROM signs WHERE x=" + x + " AND y=" + y + " AND z=" + z + " AND world='" + world + "'";       
 
         try
         {            
             this.manageSQLite.deleteQuery(query);
             if(this.getConfig().getBoolean("debug")){log.info("Schild wurde erfolgreich geloescht.");}  
         }
         catch (Exception e)
         {
             log.severe(logPrefix + "Error on deleting sign from DB. No sign found here.");
         }   
     }
 
     public void updateSignInDB(int x, int y, int z, String world, String state)
     {
         String query = "UPDATE signs SET state='" + state + "' WHERE x=" + x + " AND y=" + y + " AND z=" + z + " AND world='" + world + "'";       
 
         try
         {            
             this.manageSQLite.updateQuery(query);
             if(this.getConfig().getBoolean("debug")){log.info("Schild wurde erfolgreich geloescht.");}  
         }
         catch (Exception e)
         {
             log.severe(logPrefix + "Error on updating sign in DB. No sign found here.");
         }   
     }
 
     public int getCompletedAssignmentsFromDB(String loggedInPlayerName)
     {
         int assignmentsReadyForPickup = 0;
         String query = "SELECT COUNT(*) AS amount FROM signs WHERE assigner='" + loggedInPlayerName + "' AND state='" + assignmentStateCompleted + "'";        
 
         try
         {            
             ResultSet queryRes = this.manageSQLite.sqlQuery(query);
             queryRes.next(); //move cursor to first row of result (should only have one though) 
             if(queryRes.getRow() == 1) // query has a result, so there are completed assignments for the newly logged in player to pick up
             { 
                 assignmentsReadyForPickup = queryRes.getInt("amount");                    
                 if(this.getConfig().getBoolean("debug")){log.info("Neu eingeloggter Spieler hat beendete Aufträge, die er abholen kann");}  
             }
         }
         catch (Exception e)
         {
             log.severe(e.getMessage());
         }       
         return assignmentsReadyForPickup;
     }
 
     public ResultSet getAssignmentList(String queriedPlayerName)
     {        
         String query = "SELECT x, y, z, world, state FROM signs WHERE assigner LIKE '" + queriedPlayerName + "'";
 
         try
         {            
             ResultSet queryRes = this.manageSQLite.sqlQuery(query);            
             return (queryRes);
         }
         catch (Exception e)
         {           
             return null;
         }
     }
 }
 
