 package com.CC.General;
 
 import com.CC.Arenas.GameManager;
 import com.CC.Commands.*;
 import com.CC.Listeners.*;
 import com.CC.Messages.PlayerMessages;
 import com.CC.MySQL.MySQL;
 import com.CC.Party.Storage;
 import com.CC.WorldGeneration.WorldGeneration;
 import java.sql.SQLException;
 import java.util.logging.Logger;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 	
  public class onStartup extends JavaPlugin implements Listener 
  {
 		 
 		 
         public static boolean debugmode;
         public LobbyListener ll;
         public PlayerAuthListener pal;
         private GameManager gm;
         private Storage parties;
         private MySQL con;
         public Logger log;
         private UserManager um;
         private WorldGeneration worldgen;
         private PlayerMessages messages;
         int TimeofGame;
         int WarningTime;
 		 
         public onStartup()
         {
             
         }
          
         @Override
         public void onEnable() 
         {
             this.log = this.getLogger();
             parties = new Storage();
             um = new UserManager(this);
             pal = new PlayerAuthListener(this);
             messages = new PlayerMessages();
             //gm needs to be made before worldgen
             gm = new GameManager(this);
             //also worldgen needs to be made before gm
             worldgen = new WorldGeneration(this);
             gm.setWorldGenerator(worldgen);
             ll = new LobbyListener(this);
             getCommand("party").setExecutor(new PartyCommands(this));
             CommandExecutor relationsCommand = new RelationCommand(this);
             getCommand("friend").setExecutor(relationsCommand);
             getCommand("enemy").setExecutor(relationsCommand);
             getCommand("relation").setExecutor(relationsCommand);
             getCommand("endgame").setExecutor(new StaffCommands(this));
             PluginManager pm = getServer().getPluginManager();
             //System.out.println("Registering LobbyListener");
             pm.registerEvents(ll, this);
             pm.registerEvents(pal, this);
             getLogger().info("Plugin Is Enabled");
            getServer().getPluginManager().registerEvents(this,this);
            
             // Config :P. Not sure if correct, I usually use the not-by-bukkit-implemented-way
             getConfig().options().copyDefaults(true);
             saveConfig();
             String host = getConfig().getString("mysql.host");
             String port = getConfig().getString("mysql.port"); // Yes this is actually a String
             String database = getConfig().getString("mysql.database");
             String username = getConfig().getString("mysql.username");
             String password = getConfig().getString("mysql.password");
             TimeofGame = getConfig().getInt("GameSettings.TimePerGame", 600);
         	getConfig().set("GameSettings.TimePerGame", TimeofGame);
         	WarningTime = getConfig().getInt("GameSettings.TimeBeforeGameStarts", 120);
         	getConfig().set("GameSettings.TimeBeforeGameStarts", WarningTime);
         	saveConfig();
             
             // Establishing the MySQL connection            
             con = new MySQL(log, "[CC]", host, port, database, username, password);
             try 
             {
                 con.open();
                 if(!con.checkConnection())
                 {
                     throw new SQLException("MySQL server did not respond. Please confirm that the server is up!");
                 }
                 
                 if(!createTables())
                 {
                     throw new SQLException("Failed to create tables: cause unknown"); // Either this is thrown or the function throws one from inside
                 }
             } 
             catch (SQLException ex)
             {
                 log.info("Failed to connect to MySQL server with credentials 'localhost@root' using database 'hw'");
                 log.info("Exception thrown: "+ex.getMessage());
                 log.info("Disabling plugin...");
                 // Our plugin is MySQL dependant right :3?
                 //Bukkit.getPluginManager().disablePlugin(this); NOT :D 
                 return;
             }
             
             
         }
 
         @Override
         public void onDisable() 
         {
             if(con != null && con.checkConnection())
             {
                 con.close();
                 con = null; // removing any reference just in case
             }
             getLogger().info("Plugin Is Disabled");	
         }	 
         
         public GameManager getGameManager()
         {
             return this.gm;
         }
         
         public Storage getParties()
         {
             return this.parties;
         }
         
         public MySQL getConnection()
         {
             return this.con;
         }
         
         public UserManager getUserManager()
         {
         	return this.um;
         }
         
         public WorldGeneration getWorldGenerator()
         {	
             return this.worldgen;
         }
 	
         // INCOMPLETE, ITS JUST A PRESET
         private boolean createTables()
         {
             String players = new StringBuilder("CREATE TABLE players ( id INT NOT NULL AUTO_INCREMENT,")
                                                  .append(" PRIMARY KEY(id),")
                                                  .append(" name VARCHAR(16) NOT NULL")
                                                  .append(");").toString();
             String reputation = new StringBuilder("CREATE TABLE reputation ( player_id INT NOT NULL,")
                                                  .append(" reputation FLOAT(4,2) NOT NULL,")
                                                  .append(" FOREIGN KEY(player_id)")
                                                  .append("    REFERENCES players(id)")
                                                  .append(");").toString();      
             String stats = new StringBuilder("CREATE TABLE stats     ( player_id INT NOT NULL,")
                                                  .append(" points INT DEFAULT 0,")
                                                  .append(" kills INT DEFAULT 0,")
                                                  .append(" deaths INT DEFAULT 0,")
                                                  .append(" onRed INT DEFAULT 0,")
                                                  .append(" onBlue INT DEFAULT 0,")
                                                  .append(" FOREIGN KEY(player_id)")
                                                  .append("    REFERENCES players(id)")
                                                  .append(");").toString();
             // Nakama :3
             String relation = new StringBuilder("CREATE TABLE relation ( player_id INT NOT NULL,")
                                                  .append(" rel_id INT NOT NULL,")
                                                  .append(" isfoe BOOLEAN DEFAULT FALSE,")
                                                  .append(" FOREIGN KEY(player_id)")
                                                  .append("    REFERENCES players(id),")
                                                  .append(" FOREIGN KEY(rel_id)")
                                                  .append("    REFERENCES players(id)")
                                                  .append(");").toString();
             if(!getConnection().checkTable("players"))
             {
                 getConnection().createTable(players);
             }
             if(!getConnection().checkTable("reputation"))
             {
                 getConnection().createTable(reputation);
             }
             if(!getConnection().checkTable("stats"))
             {
                 getConnection().createTable(stats);
             }
             if(!getConnection().checkTable("relation"))
             {
                 getConnection().createTable(relation);
             }
             
             return  getConnection().checkTable("players") && getConnection().checkTable("reputation")
                     && getConnection().checkTable("stats") && getConnection().checkTable("relation");
         }
         
         public PlayerMessages getMessages(){
         	return this.messages;
         }
         
         public int getGameTime(){
         	return this.TimeofGame;
         }
         
         public int getWarningTime(){
         	return this.WarningTime;
         }
  }
 	
