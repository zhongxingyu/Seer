 package de.hswt.hrm.test.database;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Random;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 
 import de.hswt.hrm.common.database.DatabaseUtil;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 
 /**
  * Tests that need a database can inherit from this class which automatically creates a
  * test database and fills in some test data.
  * The test database is automatically deleted after all tests have run.
  * 
  * <b>Attention:</b>
  * The same database is used for all tests within this class. If you want to reset the
  * database after or before executing a specific test, use the "resetDatabase" method.
  * But don't forget that this will result in a delete and complete recreation of the
  * test database. 
  */
 public abstract class AbstractDatabaseTest {
 
 	private Connection con;
 	private String dbName;
 	
 	public Connection getConnection() throws DatabaseException {
         // load mariadb driver
         try {
             Class.forName("org.mariadb.jdbc.Driver");
         }
         catch (ClassNotFoundException e) {
             throw new DatabaseException("Database driver not found.", e);
         }
         
         // TODO load connection string from configuration
         String config = "jdbc:mysql://10.154.4.20";
         String username = "root";
         String password = "70b145pl4ch7";
         
         if (con == null) {
 	        try {
 	            con = DriverManager.getConnection(config, username, password);
 	        }
 	        catch (SQLException e) {
 	            // TODO maybe add specific information about the error
 	            throw new DatabaseException(e);
 	        }
         }
         
         return con;
     }
 	
 	private String getRandomString(int count) {
 		final String alphabet = "abcdefghijklmnopqrstuvwxyz";
 		
 		Random r = new Random();
 		
 		StringBuilder sb = new StringBuilder();		
 		for (int i = 0; i < count; i++) {
 			sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
 		}
 		
 		return sb.toString();
 	}
 	
 	private String createUniqueName() throws DatabaseException {
 		String prefix = "hrmtest_";
 		Connection con = getConnection();
 		
 		String name = prefix + getRandomString(5);
 		while (DatabaseUtil.dbExists(con, name)) {
 			name = prefix + getRandomString(5);
 		}
 		
 		return name;
 	}
 	
     @BeforeClass
     public void createDatabase() throws DatabaseException {
         Connection con = getConnection();
         String name = createUniqueName();
         
         DatabaseUtil.createDb(con, name);
         
         // Select database
         try {
 			Statement stmt = con.createStatement();
 			stmt.executeQuery("USE " + name);
 			dbName = name;
 		}
 		catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
     }
     
     @AfterClass
     public void dropDatabase() throws DatabaseException {
         try {
 			Statement stmt = con.createStatement();
			stmt.executeQuery("DROP DATABASE " + dbName);
 		}
 		catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
     }
     
     public void resetDatabase() throws DatabaseException {
         dropDatabase();
         createDatabase();
     }
 }
