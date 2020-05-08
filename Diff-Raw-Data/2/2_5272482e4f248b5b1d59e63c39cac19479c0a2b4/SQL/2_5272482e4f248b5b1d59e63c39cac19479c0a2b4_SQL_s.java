 package org.ultralogger.logger.sql;
 
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.ultralogger.Main;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 public class SQL {
 
     public static final String driver = "com.mysql.jdbc.Driver";
     private Logger logger;
     private Connection conn;
     private Main plugin;
     private String dbms = "mysql";
     private String serverName,portNumber,userName,password,dbName,prefix;
 
     /*
      * Enable Sql logging at plugin enable
      */
 
     public SQL(Main head,String serverName,String portNumber,String userName,String password,String dbName,String prefix){
         this.plugin=head;
         this.serverName = serverName;
         this.portNumber = portNumber;
         this.userName = userName;
         this.password = password;
         this.dbName= dbName;
         this.prefix= prefix;
         logger = getLoggerSafely();
         connect();
     }
 
     /*
      * Connect to database and catch errors
      */
 
     public void connect(){
         try{
             Class.forName(driver);
             conn = this.getConnection();
         } catch (ClassNotFoundException e1) {
             e1.printStackTrace();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         //Create if not exists and use db
         Statement stmt = null;
         try {
             stmt = conn.createStatement();
             stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS "+dbName);
             stmt.executeQuery("USE "+dbName);
             stmt.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     /*
      * Establish connection and log to console
      */
 
     public Connection getConnection() throws SQLException {
         Connection conn = null;
         Properties connectionProps = new Properties();
         connectionProps.put("user", this.userName);
         connectionProps.put("password", this.password);
        conn = DriverManager.getConnection("jdbc:" + this.dbms + "://" + this.serverName + ":" + this.portNumber + "/",connectionProps);
         logger.info("Connected to database");
         return conn;
     }
 
     /*
      * Execute update and print errors to console
      * Can also be used to create Tables
      * TODO: Might be better to buffer it into little query packets instead of loads of single query calls
      */
 
     public void query(String query){
         Statement stmt = null;
         try {
             stmt = conn.createStatement();
             stmt.executeUpdate(query);
             stmt.close();
         } catch (SQLException e) {
             System.out.println("(ERROR) Can NOT execute query : "+query);
             e.printStackTrace();
         }
     }
 
     /**
      * Change ' into '' inside Strings => Block SQL-injection
      * TODO: Make it run for every String ever called
      */
     public String StringCheck(String check){
         String checked= null;
         try {
             checked = EscapeString.mysql_real_escape_string(conn, check);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return checked;
     }
     
     /**
 	 *  Get the Bukkit logger first, before we try to create our own
 	 */
 	private Logger getLoggerSafely() {
 		Logger log = null;
 		try {
 			log = plugin.getLogger();
 		} catch (Throwable e) {
 			// We'll handle it
 		}
 		if (log == null)
 			log = Logger.getLogger("Minecraft");
 		return log;
 	}
 
     /*
      * Register listeners for the Loggers
      */
     public void register(Listener l){
         plugin.getServer().getPluginManager().registerEvents(l, plugin);
     }
 
     public JavaPlugin getPlugin() {
         return plugin;
     }
 
     public String getprefix(){
         return prefix;
     }
 }
