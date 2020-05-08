 package de.hotmail.gurkilein.bankcraft.database.mysql;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 
 import de.hotmail.gurkilein.bankcraft.Bankcraft;
 import de.hotmail.gurkilein.bankcraft.database.DatabaseManagerInterface;
 
 public class DatabaseManagerMysql implements DatabaseManagerInterface{
 
 	  private Connection conn = null;
 	  
 	  // Hostname
 	  private String dbHost;
 	 
 	  // Port -- Standard: 3306
 	  private String dbPort;
 	 
 	  // Databankname
 	  private String database;
 	 
 	  // Databank username
 	  private String dbUser;
 	 
 	  // Databank password
 	  private String dbPassword;
 
 	private Bankcraft bankcraft;
 	  
 	public DatabaseManagerMysql(Bankcraft bankcraft) {
 		this.bankcraft = bankcraft;
 		
 		setupDatabase();
 	}
 
 	@Override
 	public boolean setupDatabase() {
         try {
        	 	//Load Drivers
             Class.forName("com.mysql.jdbc.Driver");
             
             dbHost = bankcraft.getConfigurationHandler().getString("database.mysql.host");
             dbPort = bankcraft.getConfigurationHandler().getString("database.mysql.port");
             database = bankcraft.getConfigurationHandler().getString("database.mysql.databaseName");
             dbUser = bankcraft.getConfigurationHandler().getString("database.mysql.user");
             dbPassword = bankcraft.getConfigurationHandler().getString("database.mysql.password");
             
             //Connect to database
             conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":"
                 + dbPort + "/" + database + "?" + "user=" + dbUser + "&"
                 + "password=" + dbPassword);
            
           } catch (ClassNotFoundException e) {
             System.out.println("Could not locate drivers");
             return false;
           } catch (SQLException e) {
             System.out.println("Could not connect");
             return false;
           }
         
         //Create tables if needed
 	      Statement query;
 	      try {
 	        query = conn.createStatement();
 	        
	        String accounts = "CREATE TABLE IF NOT EXISTS `bc_accounts` (id int(10) AUTO_INCREMENT, player_name varchar(50) NOT NULL, balance varchar(50) NOT NULL, balance_xp varchar(50) NOT NULL, PRIMARY KEY(id));";
 	        String signs = "CREATE TABLE IF NOT EXISTS `bc_signs` (id int(10) AUTO_INCREMENT, x int(10) NOT NULL, y int(10) NOT NULL, z int(10) NOT NULL, world varchar(100) NOT NULL, type int(10) NOT NULL, amount varchar(250) NOT NULL, PRIMARY KEY(id));";
 	        query.executeUpdate(accounts);
 	        query.executeUpdate(signs);
 	      } catch (SQLException e) {
 	        e.printStackTrace();
 	        return false;
 	      }
         Bankcraft.log.info("Mysql has been set up!");
 		return true;
 	}
 	
 	public Connection getConnection() {
 		return conn;
 	}
 
 	@Override
 	public boolean closeDatabase() {
 		try {
 			conn.close();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 }
