 /**
  * 
  */
 package imp.model;
 
 import java.sql.DriverManager;
 import java.util.ArrayList;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import com.mysql.jdbc.Driver;
 
 import imp.core.IPObject;
 import imp.core.serverType;
 
 /**
  * @author Ransom Roberson
  * @date 11.06.13
  */
 public class SQLAudit {
 	private IPObject target;
 	private String JDBC_DRIVER;
 	private String DB_URL;
 	private String USER;
 	private String PASS;
 	private String PORT;
 	private serverType type;
 
 	/**
 	 * This constructor will setup the sql scanning parameters
 	 * 
 	 * @param target
 	 *            IPObject of target server
 	 * @param type
 	 *            serverType enum to declare what kind of scan we're performing
 	 */
 	public SQLAudit(IPObject target, serverType type) {
 		Config cfg = new Config();
 		this.target = target;
 		this.type = type;
 		if (type == imp.core.serverType.MYSQL) {
 			USER = cfg.getProperty("mysql.user");
 			PASS = cfg.getProperty("mysql.pass");
 			PORT = cfg.getProperty("mysql.port");
 			
 			JDBC_DRIVER = "com.mysql.jdbc.Driver";
 			DB_URL = "jdbc:mysql://" + target.toString() + ":" + PORT;
 		} else if (type == imp.core.serverType.MSSQL) {
 			USER = cfg.getProperty("mssql.user");
 			PASS = cfg.getProperty("mssql.pass");
 			PORT = cfg.getProperty("mssql.port");
 			
 			JDBC_DRIVER = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
 			DB_URL = "jdbc:microsoft:sqlserver://" + target.toString() + ":" + PORT;
 		} else {
 			// Scan both
 		}
 
 		this.run();
 	}
 
 	/**
 	 * Attempts to connect to the database
 	 */
 	private void run() {
 
 		try {
 			Class.forName(JDBC_DRIVER);
 		} catch (Exception e) {
 			System.out.println("Could not create JDBC driver");
 			e.printStackTrace();
 			return;
 		}
 		
 		System.out.println("Starting connection...");
 		Connection con = null;
 		try {
 			DriverManager.setLoginTimeout(2);
 			con = DriverManager.getConnection(DB_URL, USER, PORT);
 
 		} catch (SQLException e) {
			System.out.println("Connection failed.");
			target.scanned = false;
			target.vulnderable = false;
			target.reachable = false;
 			return;
 		}
 		
 		if (con != null) {
 			System.out.println("Connection established.");
 			target.scanned = true;
 			target.vulnderable = false;
 			target.serverType = this.type;
 			target.reachable = true;
 		} else {
 			System.out.println("Connection failed.");
 			target.scanned = true;
 			target.vulnderable = false;
 			target.reachable = false;
 			return;
 		}
 		try {
 			con.close();
 		} catch (SQLException e) {
 			System.out.println("Couldn't close connection.");
 		}
 	}
 
 	public IPObject get() {
 		return target;
 	}
 
 }
