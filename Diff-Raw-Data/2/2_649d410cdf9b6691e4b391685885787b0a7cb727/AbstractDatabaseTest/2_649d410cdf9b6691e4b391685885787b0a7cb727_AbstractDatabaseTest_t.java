 package de.hswt.hrm.test.database;
 
 import java.io.IOException;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Random;
 
 import static com.google.common.base.Strings.*;
 
 import org.junit.After;
 import org.junit.Before;
 
 import de.hswt.hrm.common.Config;
 import de.hswt.hrm.common.Config.Keys;
 import de.hswt.hrm.common.database.DatabaseFactory;
 import de.hswt.hrm.common.database.DatabaseUtil;
 import de.hswt.hrm.common.database.ScriptParser;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 
 /**
  * Tests that need a database can inherit from this class which automatically creates a
  * test database and fills in some test data before a test.
  * The test database is automatically deleted after a tests is run.
  *
  * You can use {@link #resetDatabase()} to reset the database during a test.
  */
 public abstract class AbstractDatabaseTest {
 	private String dbName;
 	
 	private String getRandomString(int count) {
 		final String alphabet = "abcdefghijklmnopqrstuvwxyz";
 		
 		Random r = new Random();
 		
 		StringBuilder sb = new StringBuilder();		
 		for (int i = 0; i < count; i++) {
 			sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
 		}
 		
 		return sb.toString();
 	}
 	
 	private String createUniqueName() throws DatabaseException, IOException {
 	    String prefix = "hrmtest_";
 		Connection con = DatabaseFactory.getConnection();
 		
 		String name = prefix + getRandomString(5);
 		while (DatabaseUtil.dbExists(con, name)) {
 			name = prefix + getRandomString(5);
 		}
 		
 		return name;
 	}
 	
 	private void applyScheme(Connection con) throws SQLException, IOException {
	    Path path = Paths.get("..", "resources", "scripts.db", "create_tables.sql");
 	    ScriptParser parser = new ScriptParser(con.createStatement());
 	    try (Statement stmt = parser.parse(path)) {
 	        con.setAutoCommit(false);
 	        stmt.executeBatch();
 	        con.commit();
 	        con.setAutoCommit(true);
 	    }
 	}
 	
     @Before
     public void createDatabase() throws DatabaseException, IOException {
         // load configuration
         Config config = Config.getInstance();
         config.load(Paths.get("../resources/hrm.properties"));
         
         // remove database name
         config.setProperty(Keys.DB_NAME, "");
         
         // Create database
         try (Connection con = DatabaseFactory.getConnection()) {
             String name = createUniqueName();
             
             DatabaseUtil.createDb(con, name);
             
             // Select database
 			Statement stmt = con.createStatement();
 			stmt.executeQuery("USE " + name + ";");
 			dbName = name;
 			
 			// Configure new db name
 			config.setProperty(Keys.DB_NAME, name);
 
 			applyScheme(con);
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
     
     @After
     public void dropDatabase() throws DatabaseException {
         if (isNullOrEmpty(dbName)) {
             throw new IllegalStateException("Database name must not be null!");
         }
         
         // Reset database name to get database less connection
         Config.getInstance().setProperty(Keys.DB_NAME, "");
         try (Connection con = DatabaseFactory.getConnection()) {
 			Statement stmt = con.createStatement();
 			stmt.executeQuery("DROP DATABASE " + dbName + ";");
 		}
 		catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
     }
     
     /**
      * Reset the test database.
      * 
      * @throws DatabaseException If an error occurs during reset.
      * @throws IOException 
      */
     public void resetDatabase() throws DatabaseException, IOException {
         dropDatabase();
         createDatabase();
     }
 }
