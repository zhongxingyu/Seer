 package net.aparsons.sqldump.db;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.io.FileExistsException;
 import org.apache.commons.io.FileUtils;
 
 import au.com.bytecode.opencsv.CSVWriter;
 
 import net.aparsons.sqldump.db.Connectors.Connector;
 
 
 public final class SQLDump implements Runnable {
 
 	public static final String VERSION = "0.4";
 	
 	private final Connector driver;
 	private final String url, username, password, sql;
 	
 	private boolean headers = false;
 	private File file;
 	
 
 	public SQLDump(Connector driver, String url, String username, String password, String sql) {
 		this.driver = driver;
 		this.url = url;
 		this.username = username;
 		this.password = password;
 		this.sql = sql;
 	}
 		
 	public void setFile(File file) {
 		this.file = file;
 	}
 
 	public void setHeaders(boolean headers) {
 		this.headers = headers;
 	}
 	
 	@Override
 	public void run() {		
 		try {
 			// Load Driver
 			Connectors.load(driver);
 			
 			// Init Files
 			if (file == null) file = new File(Long.toString(System.currentTimeMillis()) + ".csv");
 			if (file.exists()) throw new FileExistsException("File already exists [" + file.getName() + "]");
 			
 			File tempFile = new File(file + ".tmp");
			if (file.exists()) throw new FileExistsException("File already exists [" + tempFile.getName() + "]");

 			
 			Connection conn = null;
 			Statement stmt = null;
 			ResultSet rs = null;
 			try {
 				// Establish Connection
 				Logger.getLogger(SQLDump.class.getName()).log(Level.INFO, "Connecting to database [" + url + "]");
 				conn = DriverManager.getConnection(url, username, password);
 				
 				if (conn != null) {
 					Logger.getLogger(SQLDump.class.getName()).log(Level.INFO, "Successfully connected to database [" + url + "]");
 					
 					// Get statement from the connection
 					stmt = conn.createStatement();
 					
 					// Execute the query
 					Logger.getLogger(SQLDump.class.getName()).log(Level.INFO, "Executing statment [" + sql + "]");
 					rs = stmt.executeQuery(sql);
 					
 					// Write to file
 					CSVWriter writer = null;
 					try {
 						writer = new CSVWriter(new FileWriter(tempFile));
 						
 						Logger.getLogger(SQLDump.class.getName()).log(Level.INFO, "Writing to temporary file [" + tempFile + "]");
 						writer.writeAll(rs, headers);
 						writer.close();
 						
 						// Rename File
 						Logger.getLogger(SQLDump.class.getName()).log(Level.INFO, "Renaming temporary file [" + tempFile + "] to [" + file + "]");
 						try {
 							FileUtils.moveFile(tempFile, file);
 						} catch (IOException ioe) {
 							Logger.getLogger(SQLDump.class.getName()).log(Level.SEVERE, "Error renaming file", ioe);
 						}
 					} catch (IOException ioe) {
 						Logger.getLogger(SQLDump.class.getName()).log(Level.SEVERE, "Error writing to file", ioe);
 					} finally {
 						if (writer != null) try { writer.close(); } catch (IOException ioe) { }
 					}
 				}
 			} catch (SQLException sqle) {
 				Logger.getLogger(SQLDump.class.getName()).log(Level.SEVERE, "Database connection failed", sqle);
 			} finally {
 				if (rs != null)   try { rs.close();   } catch (SQLException sqle) { }
 				if (stmt != null) try { stmt.close(); } catch (SQLException sqle) { }
 				if (conn != null) try { conn.close(); } catch (SQLException sqle) { }
 			}
 		} catch (ClassNotFoundException cnfe) {
 			Logger.getLogger(SQLDump.class.getName()).log(Level.SEVERE, "Database driver not found", cnfe);
 		} catch (FileExistsException fee) {
 			Logger.getLogger(SQLDump.class.getName()).log(Level.SEVERE, "File already exists", fee);
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return "SQLDump-" + VERSION + " [url=" + url + ", username=" + username + ", sql=" + sql + "]";
 	}
 
 }
