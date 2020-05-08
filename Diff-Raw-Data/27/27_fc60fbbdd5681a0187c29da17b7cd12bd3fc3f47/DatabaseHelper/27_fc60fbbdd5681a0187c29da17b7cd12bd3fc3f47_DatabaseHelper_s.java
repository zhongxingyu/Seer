 package adstimator.data;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import weka.experiment.DatabaseUtils;
 
 /**
  * Singleton for getting and setting connections to a database.
  * 
  * The object defaults to using the database specified by Weka in the DatabaseUtils.props file, but it can be
  * changed by setting a new connection URL (useful for example when testing). This URL is then used to generate new 
  * Connection objects.
  *
  * @author erikbrannstrom
  */
 public class DatabaseHelper
 {
 	private static DatabaseHelper instance;
 	private String connectionURL;
 
 	/**
 	 * Protected constructor, should not be called by other classes.
 	 */
 	protected DatabaseHelper()
 	{
 	}
 
 	/**
 	 * Returns the current connection URL. If no other URL has been set it defaults to the one set by Weka.
 	 * 
 	 * @return Connection URL
 	 */
 	public String getConnectionURL()
 	{
 		if (this.connectionURL == null) {
 			try {
 				DatabaseUtils wekaDbUtil = new DatabaseUtils();
 				this.connectionURL = wekaDbUtil.getDatabaseURL();
 			} catch (Exception ex) {
 				throw new RuntimeException(ex);
 			}
 		}
 		return this.connectionURL;
 	}
 
 	/**
 	 * Set the connection URL. Note that this does not affect connections which have been created previously.
 	 * 
 	 * @param connectionURL New connection URL
 	 */
 	public void setConnectionURL(String connectionURL)
 	{
 		this.connectionURL = connectionURL;
 	}
 	
 	/**
 	 * Initializes and returns a new Connection object with the current connection URL. No username or password is used.
 	 * 
 	 * @return New database connection
 	 */
 	public Connection getConnection()
 	{
 		try {
 			return DriverManager.getConnection(this.getConnectionURL(), null, null);
 		} catch (SQLException ex) {
			return null;
 		}
 	}
 
 	/**
 	 * Get the singleton instance.
 	 * 
 	 * @return Singleton
 	 */
 	public static DatabaseHelper instance()
 	{
 		if (instance == null) {
 			instance = new DatabaseHelper();
 		}
 		return instance;
 	}
 }
