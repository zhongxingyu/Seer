 /**
  * SOEN 490
  * Capstone 2011
  * Table Data Gateway for the User Domain Object
  * Team members: 	Sotirios Delimanolis
  * 			Filipe Martinho
  * 			Adam Harrison
  * 			Vahe Chahinian
  * 			Ben Crudo
  * 			Anthony Boyer
  * 
  * @author Capstone 490 Team Moving Target
  *
  */
 
 package foundation;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Properties;
 
 //import java.util.Stack;
 
 public class Database {
 	private static Database singleton = null;
 	private Properties prop = new Properties();
 	//private Stack<Connection> freeConnections = new Stack<Connection>();
 	//private static final long connectionPool = 10;
 
 	// If we want to pool connections we'd put the code in here; create at startup, and allocate connections on getConnection and freeConnection.
 	private Database() throws IOException 
 	{
		String path = ServletInformation.getInstance().resolvePath("WEB-INF/Database.properties");
		prop.load(new FileInputStream(path));
 	}
 	
 	public boolean canConnect() throws SQLException
 	{
 		Connection connection = getConnection();
 		if (connection == null)
 			return false;
 		freeConnection(connection);
 		return true;
 	}
 	
 	private synchronized Connection getConnection() throws SQLException
 	{
 		String driverLoadingString = "jdbc:mysql://" + prop.getProperty("hostname") + "/" + prop.getProperty("database") + "?"
 				+ "user=" + prop.getProperty("username") + "&password=" + prop.getProperty("password");
 		try {
 			// This will load the MySQL driver, each DB has its own driver
 			Class.forName("com.mysql.jdbc.Driver");
 			// Setup the connection with the DB
 			Connection connection = DriverManager
 					.getConnection(driverLoadingString);
 			return connection;
 		}
 		catch (ClassNotFoundException e) {
 			throw new SQLException(e);
 		}
 	}
 	
 	private synchronized void freeConnection(Connection connection) throws SQLException
 	{
 		connection.close();
 	}
 	
 	public ResultSet query(String queryString, Object[] objects) throws SQLException
 	{
 		Connection connection = getConnection();
 		if (connection == null)
 			return null;
 		PreparedStatement statement = connection.prepareStatement(queryString);
 		if (objects != null) {
 			for (int c = 0; c < objects.length; ++c)
 				statement.setObject(c, objects[c]);
 		}
 		ResultSet rs = statement.executeQuery();
 		freeConnection(connection);
 		return rs;
 	}
 	
 	public ResultSet query(String queryString) throws SQLException
 	{
 		return query(queryString, null);
 	}
 	
 	public int update(String queryString, Object[] objects) throws SQLException
 	{
 		Connection connection = getConnection();
 		PreparedStatement statement = connection.prepareStatement(queryString);
 		if (objects != null) {
 			for (int c = 0; c < objects.length; ++c)
 				statement.setObject(c, objects[c]);
 		}
 		int result = statement.executeUpdate();
 		freeConnection(connection);
 		return result;
 	}
 	
 	public int update(String queryString) throws SQLException
 	{
 		return update(queryString, null);
 	}
 	
 	public boolean runFile(String path) throws IOException, SQLException
 	{
 		BufferedReader fileReader = new BufferedReader(new FileReader(path));
 		String line;
 		while ((line = fileReader.readLine()) != null)
 		{
 			update(line, null);
 		}
 		return true;
 	}
 	
 	public boolean hasTable(String tableName) throws SQLException
 	{
 		Connection connection = getConnection();
 		DatabaseMetaData metaData = connection.getMetaData();
 		ResultSet tables = metaData.getTables(null, null, tableName, null);
 		return tables.next();
 	}
 	
 	public PreparedStatement getStatement(String query) throws SQLException
 	{
 		Connection connection = getConnection();
 		return connection.prepareStatement(query);
 	}
 
 	public static Database getInstance() throws IOException
 	{
 		if (singleton == null)
 			singleton = new Database();
 		return singleton;
 	}
 	
 	public void createDatabase() throws SQLException, IOException
 	{
 		if (!hasTable(UserTDG.TABLE))
 			UserTDG.create();
 		if (!hasTable(MessageTDG.TABLE))
 			MessageTDG.create();
 	}
 	
 	public void dropDatabase() throws SQLException, IOException
 	{
 		if (hasTable(UserTDG.TABLE))
 			UserTDG.drop();
 		if (hasTable(MessageTDG.TABLE))
 			MessageTDG.drop();
 	}
 	
 	public boolean isDatabaseCreated() throws SQLException
 	{
 		return hasTable(UserTDG.TABLE) && hasTable(MessageTDG.TABLE); 
 	}
 }
