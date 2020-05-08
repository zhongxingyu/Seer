 package me.greatman.plugins.inn;
 
 import java.io.File;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import me.greatman.plugins.inn.commands.InnCmd;
 import me.greatman.plugins.inn.extras.CommandManager;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.alta189.sqlLibrary.SQLite.sqlCore;
 import com.nijikokun.register.payment.Method;
 
 public class Inn extends JavaPlugin {
 	//We create required variables and Listener variables
 	public static String name,version;
 	private Map<String, PlayerData> playerData;
 	private final CommandManager commandManager = new CommandManager(this);
 	private final IPlayerListener playerListener = new IPlayerListener(this);
 	private final IServerListener serverListener = new IServerListener(this);
 	private final IBlockListener blockListener = new IBlockListener(this);
 	public Method Method;
 	public static double cost;
 	public static int timeout;
 	public File pFolder = new File("plugins" + File.separator + "Inn");
 	public static sqlCore manageSQLite; // SQLite handler
 	public Logger log = Logger.getLogger("Minecraft");
 	public String logPrefix = "[" + Inn.name + " " + Inn.version + "] ";
     public void onDisable() {
     	//We disable SQLite and the plugin
     	manageSQLite.close();
         System.out.println(this + " is now disabled!");
     }
 
     public void onEnable() {
     	//We sync the hashmap
     	setPlayerData(Collections.synchronizedMap(new HashMap<String, PlayerData>()));
     	//We set the name and version
     	name = this.getDescription().getName();
 		version = this.getDescription().getVersion();
 		//We load our listeners
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_BED_ENTER, playerListener, Priority.Lowest, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor,this);
 		pm.registerEvent(Event.Type.PLAYER_MOVE,playerListener,Priority.Monitor,this);
 		pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener,Priority.Monitor,this);
 		pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener,Priority.Monitor,this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Monitor, this);
 		//We load our class
 		ILogger.initialize(Logger.getLogger("Minecraft"));
 		IConfig IConfig = new IConfig(this);
 		IConfig.configCheck();
 		IPermissions.initialize(this);
 		//We add the inn command
 		InnCmd InnCmd = new InnCmd(this);
 		addCommand("inn",InnCmd);
 	    //We initialize SQLite
 	    manageSQLite = new sqlCore(this.log, this.logPrefix, "Inn", pFolder.getPath());
 	    manageSQLite.initialize();
 	    String tableQuery;
 	    //We check if the table exists and if not, we create it
 	    if (!manageSQLite.checkTable("doors")) {
 	    	ILogger.info("Creating Doors table");
         	 tableQuery = "CREATE TABLE doors ("
 						+"id  INTEGER,"
 						+"x  INTEGER,"
 						+"y  INTEGER,"
 						+"z  INTEGER,"
 						+"owner  STRING,"
 						+"price  INTEGER,"
 						+"PRIMARY KEY (id ASC)"
 						+");";
         	 if(manageSQLite.createTable(tableQuery))
         		 ILogger.info("Table doors created!");
         	 else
         		 ILogger.warning("Error while creating doors table");
 	    }
 	    if (!manageSQLite.checkTable("time")) {
 	    	ILogger.info("Creating Time table");
         	 tableQuery = "CREATE TABLE time ("
 						+"doorid  INTEGER,"
 						+"player  INTEGER,"
 						+"time  INTEGER"
 						+");";
         	 if(manageSQLite.createTable(tableQuery))
         		 ILogger.info("Table Time created!");
         	 else
         		 ILogger.warning("Error while creating Time table");
 	    }
 	    ILogger.info("Initialized!");
 	    
     }
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         return commandManager.dispatch(sender, cmd, label, args);
     }
 
     /*
      * Adds the specified command to the command manager and server.
      * 
      * @param command   The label of the command.
      * @param executor  The command class that excecutes the command.
      */
     private void addCommand(String command, CommandExecutor executor) {
         getCommand(command).setExecutor(executor);
         commandManager.addCommand(command, executor);
     }
     public void setPlayerData(Map<String, PlayerData> playerData) {
         this.playerData = playerData;
     }
 
     public Map<String, PlayerData> getPlayerData() {
         return playerData;
     }
     public Player getPlayer(CommandSender sender) {
         Player player = null;
         if (isPlayer(sender)) {
             player = (Player) sender;
         }
         return player;
     }
     public boolean isPlayer(CommandSender sender) {
         return sender != null && sender instanceof Player;
     }
     /*
      * Checks if a door is already registered in the system
      * 
      * @param x  The coords X
      * @param y  The coords Y
      * @param z  The coords Z
      */
     public static boolean doorAlreadyExists(int x, int y, int z){
     	//We set th
     	String query = "SELECT id FROM doors WHERE x="+ x + " AND y=" + y + " AND z=" + z;
     	ResultSet result = Inn.manageSQLite.sqlQuery(query);
     	int id = 0;
     	try {
     		//Is there something?
 			if (result.next()){
 				id = result.getInt("id");
 				//Is it a real ID?
 				if (id != 0)
 					return true;
 				else
 					return false;
 			}else
 				return false;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
     	return false;
     }
     /*
      * Get the door price
      * 
      * @param x  The coords X
      * @param y  The coords Y
      * @param z  The coords Z
      */
     public static int getDoorPrice(int x, int y, int z){
     	if (doorAlreadyExists(x,y,z)){
     		String query = "SELECT price FROM doors WHERE x="+ x + " AND y=" + y + " AND z=" + z;
     		ResultSet result = Inn.manageSQLite.sqlQuery(query);
     		try {
 				if (result.next()){
 					return result.getInt("price");
 				}else
 					
 					return -1;
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
     	}else
     		return -1;
     return -1;
     }
     /*
      * Get the door owner
      * 
      * @param x  The coords X
      * @param y  The coords Y
      * @param z  The coords Z
      */
     public static String getOwner(int x, int y, int z){
     	if (doorAlreadyExists(x,y,z)){
     		String query = "SELECT owner FROM doors WHERE x="+ x + " AND y=" + y + " AND z=" + z;
     		ResultSet result = Inn.manageSQLite.sqlQuery(query);
     		try {
 				if (result.next()){
 					return result.getString("owner");
 				}else
 					return "";
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
     	}else
     		return "";
     return "";
     }
     /*
      * Get the door ID
      * 
      * @param x  The coords X
      * @param y  The coords Y
      * @param z  The coords Z
      */
     public static int getDoorID(int x, int y, int z){
     	if (doorAlreadyExists(x,y,z)){
     		String query = "SELECT id FROM doors WHERE x="+ x + " AND y=" + y + " AND z=" + z;
     		ResultSet result = Inn.manageSQLite.sqlQuery(query);
     		try {
 				if (result.next()){
 					return result.getInt("id");
 				}else
 					
 					return -1;
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
     	}else
     		return -1;
     return -1;
     }
     public static boolean addTimeout(int x, int y, int z, String playerName){
     	if (doorAlreadyExists(x,y,z)){
     		int doorid = getDoorID(x,y,z);
    		int doorid2 = getDoorID(x,y-1,z);
    		int doorid3 = getDoorID(x,y+1,z);
    		String query;
    		if (doorid2 != -1){
    			query = "INSERT INTO time(doorid,player,time) VALUES(" + doorid2 + ",'" + playerName + "',strftime('%s','now'))";
    			Inn.manageSQLite.insertQuery(query);
    		}
    		if (doorid3 != -1){
    			query = "INSERT INTO time(doorid,player,time) VALUES(" + doorid3 + ",'" + playerName + "',strftime('%s','now'))";
    			Inn.manageSQLite.insertQuery(query);
    		}
    		
    		query = "INSERT INTO time(doorid,player,time) VALUES(" + doorid + ",'" + playerName + "',strftime('%s','now'))";
     		Inn.manageSQLite.insertQuery(query);
     		return true;
     	}
     	return false;
     }
     public static int getTimeout(int x, int y, int z, String playerName){
     	if (doorAlreadyExists(x,y,z)){
     		int doorid = getDoorID(x,y,z);
     		String query = "SELECT time FROM time WHERE doorid=" + doorid + " AND player='" + playerName +"'";
     		ResultSet result = Inn.manageSQLite.sqlQuery(query);
     		try {
 				if (result.next()){
 					return result.getInt("time");
 				}else
 					
 					return -1;
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
     	}
     	return -1;
     }
     public static boolean isTimeoutExpired(int x, int y, int z, String playerName){
     	if (doorAlreadyExists(x,y,z)){
     		//We grab the guy timeout in the database
     		int playerTimeout = getTimeout(x,y,z,playerName);
     		if (playerTimeout != -1){
     			ResultSet result = Inn.manageSQLite.sqlQuery("SELECT strftime('%s','now') AS time");
     			try {
 					if (result.next()){
 						//Actual time
 						int currentTime = result.getInt("time");
 						//Guy timeout * inn timeout + 60 = Maximum time
 						int timeoutTime = playerTimeout + (Inn.timeout * 60);
 						if (timeoutTime < currentTime)
 							return true;
 						else
 							return false;
 					}
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
     		}else
     			return true;
     	}
     	return true;
     }
     
 }
