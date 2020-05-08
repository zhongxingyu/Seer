 package de.uni.leipzig.IR15.Importer;
 
 import java.io.File;
 import java.security.InvalidParameterException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.apache.log4j.Logger;
 
 import de.uni.leipzig.IR15.Connectors.MySQLConnector;
 import de.uni.leipzig.IR15.Support.Configuration;
 
 public abstract class Importer {
 	protected static Configuration graphConfiguration;
 	protected static Configuration mySQLConfiguration;
 	protected static Connection mySQLConnection;
 
 	protected static Logger log = Logger.getLogger(Importer.class);
 
 	/**
 	 * deletes all files associated with the repective graph database
 	 */
 	public void reset() {
 		File location = new File(
 				graphConfiguration.getPropertyAsString("location"));
 		if (location.exists()) {
 			if (location.isDirectory()) {
 				recursiveDeleteDirectory(location);
 			}
 
 			if (location.isFile()) {
 				location.delete();
 			}
 		} else {
			throw new InvalidParameterException(String.format(
					"location %s does not exist", location));
 		}
 	}
 
 	/**
 	 * Initializes the configuration instances
 	 * 
 	 * @param graphDBName
 	 *            the name of the graph database which is used (e.g. neo4j,
 	 *            orientdb, dex)
 	 */
 	public void setUp(String graphDBName) {
 		// load properties
 		graphConfiguration = Configuration.getInstance(graphDBName);
 		mySQLConnection = MySQLConnector.getConnection();
 	}
 
 	/**
 	 * This method is called before the import is started
 	 */
 	public abstract void setUp();
 
 	/**
 	 * This method imports the data
 	 */
 	public abstract void importData();
 
 	/**
 	 * Returns the name of the importer
 	 * 
 	 * @return importer's name
 	 */
 	public abstract String getName();
 
 	/**
 	 * Returns the associated database instance
 	 * 
 	 * @return the database instance the importer is using
 	 */
 //	public abstract Object getDatabaseInstance();
 
 	public void tearDown() {
 		MySQLConnector.destroyConnection();
 	}
 
 	private void recursiveDeleteDirectory(File path) {
 		for (File file : path.listFiles()) {
 			if (file.isDirectory())
 				recursiveDeleteDirectory(file);
 			file.delete();
 		}
 		path.delete();
 	}
 
 	protected Integer getMysqlRowCount(String table) {
 		String query = "SELECT COUNT(*) FROM " + table;
 		Integer count = null;
 		try {
 			Statement st = mySQLConnection.createStatement();
 			ResultSet rs = st.executeQuery(query);
 
 			while (rs.next()) {
 				count = Integer.valueOf(rs.getInt("COUNT(*)"));
 			}
 		} catch (SQLException ex) {
 			System.err.println(ex.getMessage());
 		}
 		return count;
 	}
 }
