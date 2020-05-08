 /**
  * 
  */
 package server.database;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 /**
  * @author Derek Carr
  *
  */
 public class Database {
 
 	private BrowserDB browser;
 	private JobDB job;
 	private VirtualMachineDB virtualMachine;
 	
 	private Connection connection;
 	
 	private String dbFile;
 	private String dbName;
 	private String connectionURL;
 	private String dbUser = "root";
 	private String dbPass = "plopper2";
  private String serverName;
  private String databaseName;
 	private static String driver = "com.mysql.jdbc.Driver";
 	
 	public Database(String env) {
 		browser = new BrowserDB(this);
 		virtualMachine = new VirtualMachineDB(this);
 		
 		if (env.equalsIgnoreCase("test")) {
 			this.dbFile = null;
 			this.dbName = null;
 			this.connectionURL = null;
 		} else if (env.equalsIgnoreCase("prod")) {
      this.serverName = "10.0.10.53";
      this.databaseName = "insidesales";
			this.connectionURL = "jdbc:mysql://"+serverName+"/"+databaseName;
 		} else {
 			
 		}
 	}
 	
 	public static void initialize() {
 		try {
 			Class.forName(driver);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
   /**
    * Gets the database connection
    * @return this.connection
    */
   public Connection getConnection(){
   	return this.connection;
   }
   
   /**
    * 
    * @return
    */
   public BrowserDB getBrowserDB() {
   	return this.browser;
   }
   
   /**
    * 
    * @return
    */
   public VirtualMachineDB getVirtualMachineDB() {
   	return this.virtualMachine;
   }
   
   /**
    * 
    * @throws SQLException
    */
   public void startTransaction() throws SQLException {
  	System.out.println(connectionURL);
    connection = DriverManager.getConnection(connectionURL, dbUser, dbPass);
 		connection.setAutoCommit(false);
   }
   
   /**
    * 
    * @param commit
    * @throws SQLException
    */
   public void endTransaction(boolean commit) throws SQLException {
   	try {
 			if (commit == true) {
 				connection.commit();
 			} else {
 				connection.rollback();
 			}
 		} 
 		catch (SQLException e) { e.printStackTrace(); }
 		finally { connection.close();}
 		
 		connection = null;
   }
   
 }
