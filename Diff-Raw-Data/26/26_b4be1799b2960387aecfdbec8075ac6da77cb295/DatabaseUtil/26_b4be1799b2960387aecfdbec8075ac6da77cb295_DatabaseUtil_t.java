 package de.hswt.hrm.common.database;
 
 import static com.google.common.base.Preconditions.*;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 
 /**
  * Common utility methods for database handling. 
  */
public final class DatabaseUtil {
	
	private DatabaseUtil() { }
 
 	/**
 	 * Check if a database exists on the server.
 	 * 
 	 * @param con Initialized and connected Connection object.
 	 * @param name Name of the database to check for.
 	 * @return True if the database exists, otherwise false.
 	 * @throws DatabaseException If an exception occurs during query.
 	 */
 	public static boolean dbExists(final Connection con, final String name) throws DatabaseException {
 		checkNotNull(con, "Connection must not be null");
 		checkNotNull(name, "Name must not be null.");
 		
 		try {
 			checkArgument((!con.isClosed()), "Connection must not be closed.");
 			ResultSet resultSet = con.getMetaData().getCatalogs();
 			
 			while (resultSet.next()) {
 				if (name.equals(resultSet.getString(1))) {
 					return true;
 				}
 			}
 		}
 		catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Create a database.
 	 * 
 	 * @param con Connection to the database server.
 	 * @param name Name of the database.
 	 * @throws DatabaseException
 	 */
 	public static void createDb(final Connection con, final String name) throws DatabaseException {
 		checkNotNull(con, "Connection must not be null.");
 		checkNotNull(name, "Name must not be null.");
 		
 		try {
 			checkArgument((!con.isClosed()), "Connection must not be closed.");
 			
 			Statement stmt = con.createStatement();
 			// TODO validate name
 			stmt.executeQuery("CREATE DATABASE " + name);
 		}
 		catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
 	}
 }
