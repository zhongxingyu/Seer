 package ch.ethz.mlmq.main;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.logging.Logger;
 
 import ch.ethz.mlmq.server.db.util.DatabaseInitializer;
 
 public class DbScriptMain {
 
 	private static final Logger logger = Logger.getLogger(DbScriptMain.class.getSimpleName());
 
 	/**
 	 * 
 	 * @param file
 	 *            optional SQL file to run. may be null
 	 * @param url
 	 *            jdbc url. Format jdbc:postgresql://host:port
 	 * @param databaseName
 	 *            name of the database to create/connect to
 	 * @param user
 	 *            db username
 	 * @param password
 	 *            password
 	 * @param createDatabase
 	 * @param createtables
 	 * @param dropDatabase
 	 * @throws IOException
 	 * @throws SQLException
 	 */
 	public static void run(String file, String url, String databaseName, String user, String password, boolean createDatabase, boolean createTables,
 			boolean dropDatabase) throws IOException, SQLException {
 
 		DatabaseInitializer dbInitializer = new DatabaseInitializer(url, user, password, databaseName);
 		try {
 
 			dbInitializer.connect();
 
			if (dropDatabase) {
				logger.info("Drop Database");
				dbInitializer.deleteDatabase();
			}

 			if (createDatabase) {
 				logger.info("Create Database");
 				dbInitializer.createDatabase();
 			}
 
			if (createTables) {
 				logger.info("Create Tables");
 				dbInitializer.createTables();
 			}
 
 			if (file != null) {
 				logger.info("Execute Script " + file);
 				dbInitializer.executeScript(file);
 			}
 
 		} finally {
 			dbInitializer.disconnect();
 		}
 
 	}
 }
