 package de.ismll.database.pgsql;
 
 import java.io.File;
import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Properties;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.apache.commons.dbcp.DelegatingConnection;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 
 import de.ismll.database.ISqlStatementSource;
 import de.ismll.database.SqlStatement;
 import de.ismll.database.dao.DataStoreException;
 import de.ismll.database.dao.IEntityBacked;
 import de.ismll.utilities.Assert;
 import de.ismll.utilities.Buffer;
 
 /**
  * System properties
  * 
  * 
  * 
  * @author Andre
  * 
  */
 public class PostgresSQL implements AutoCloseable{
 
 	private Logger logger = LogManager.getLogger(getClass());
 
 	private BasicDataSource dataSource;
 
 	private boolean connected;
 
 	private Properties prop = new Properties();
 
 	private String databaseName;
 
 	public String getDatabaseName() {
 		return databaseName;
 	}
 
 	public PostgresSQL() throws IOException {
 		this((String)null);
 	}
 
 	public PostgresSQL(String databasename) {
 		super();
 		init(databasename);
 	}
 
 	public PostgresSQL(BasicDataSource ds) {
 		super();
 		this.dataSource = ds;
 		this.connected=!ds.isClosed();
 
 	}
 
 	private File findFile(String name, boolean terminate) {
 		return findFile(name, logger, terminate);
 	}
 
 	public static File findFile(String name, Logger logger, boolean terminate) {
 		File ret = null;
 		File prop_file0 = new File(name);
 		File prop_file_eclipse = new File("scripts/" + name);
 		File prop_file1 = new File("src/java/" + name);
 		File prop_file2 = new File(System.getProperty("user.home") + "/."
 				+ name);
 		String property = System.getProperty("de.ismll.database.pgsql." + name);
 		File prop_file3 = null;
 		if (property != null) {
 			prop_file3 = new File(property);
 		}
 		if (prop_file0.isFile())
 			ret = prop_file0;
 		if (prop_file_eclipse.isFile())
 			ret = prop_file_eclipse;
 		if (prop_file1.isFile())
 			ret = prop_file1;
 		if (prop_file2.isFile())
 			ret = prop_file2;
 		if (prop_file3 != null && !prop_file3.isFile()) {
 			logger.warn("DB-Configuration was specified through a System property, but could not be found directly. Searching in lookup-paths ...");
 			File findFile = findFile(property, logger, false);
 			if (findFile != null)
 				prop_file3 = findFile;
 		}
 		if (prop_file3 != null && prop_file3.isFile())
 			ret = prop_file3;
 
 		if (ret == null) {
 			logger.fatal("File " + name
 					+ " was not found at the following locations:");
 			logger.fatal("\t" + prop_file0);
 			logger.fatal("\t" + prop_file_eclipse);
 			logger.fatal("\t" + prop_file1);
 			logger.fatal("\t" + prop_file2);
 			if (prop_file3 != null)
 				logger.fatal("\t" + prop_file3);
 			logger.fatal("\t NOTE: When running from within Eclipse, make sure to locate the file in the main projects directory (usually not the Matrix-project)");
 			if (terminate) {
 				System.exit(1);
 				throw new RuntimeException(
 						"No "
 								+ name
 								+ " file found. You may want to define -Dde.ismll.database.mssql."
 								+ name + " system property.");
 			}
 		}
 		logger.debug("Using " + name + " from " + ret);
 		return ret;
 	}
 
 	public synchronized void init(String databasename) {
 		File db_properties = findFile("db.properties", true);
 
 		try (InputStream is=Buffer.newInputStream(db_properties)){
 			prop.load(is);
 		} catch (IOException e) {
 			logger.error(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		if (databasename == null) {
 			databaseName = prop.getProperty("PgSql.dbname", null).trim();
 			Assert.notNull(databaseName, db_properties + "[PgSql.dbname]");
 		}
 		else
 			databaseName = databasename;
 
 		String port = "5432";
 
 		if (prop.getProperty("PgSql.port")!=null) {
 			port = prop.getProperty("PgSql.port").trim();
 		}
 
 
 		if (prop.getProperty("PgSql.connectionString")==null) {
 			prop.put("PgSql.connectionString", "jdbc:postgresql://" + prop.getProperty("PgSql.hostname").trim() + ":" + port + "/" + databaseName);
 		}
 
 	}
 
 	public void connect() {
 
 		if (!connected) {
 			dataSource = setupDataSource(prop.getProperty("PgSql.connectionString"));
 
 		}
 		connected = true;
 	}
 
 	private void checkConnection() {
 		if (dataSource != null && dataSource.isClosed()) {
 			connect();
 		}
 		if (!connected) {
 			connect();
 		}
 	}
 
 	private BasicDataSource setupDataSource(String connectURI) {
 		logger.info("Creating Data Source...");
 		BasicDataSource ds;
 		try {
 			ds = (BasicDataSource) Class.forName(
 					"org.apache.commons.dbcp.BasicDataSource", true,
 					Thread.currentThread().getContextClassLoader())
 					.newInstance();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		ds.setAccessToUnderlyingConnectionAllowed(true);
 		ds.setDriverClassName("org.postgresql.ds.PGConnectionPoolDataSource");
 		ds.setUsername(prop.getProperty("PgSql.username"));
 		ds.setPassword(prop.getProperty("PgSql.password"));
 		ds.setUrl(connectURI);
 
 		return ds;
 	}
 
 	public static void printDataSourceStats(DataSource ds) {
 		BasicDataSource bds = (BasicDataSource) ds;
 		System.out.println("NumActive: " + bds.getNumActive());
 		System.out.println("NumIdle: " + bds.getNumIdle());
 	}
 
 	public static void shutdownDataSource(DataSource ds) throws SQLException {
 		if (ds instanceof BasicDataSource) {
 			BasicDataSource bds = (BasicDataSource) ds;
 			bds.close();
 		}
 	}
 
 	public Connection getConnection() throws SQLException {
 		checkConnection();
 		Connection connection;
 		try {
 			connection = dataSource.getConnection();
 		} catch (SQLException e) {
 			logger.error(
 					"Could not get Database connection: " + e.getMessage(), e);
 			throw e;
 		}
 
 		Connection postgisConn = null;
 		if (connection instanceof DelegatingConnection) {
 			postgisConn  = ((DelegatingConnection) connection).getInnermostDelegate();
 		}
 		if (postgisConn != null) {
 			try {
 				((org.postgresql.PGConnection)postgisConn).addDataType("geometry",Class.forName("org.postgis.PGgeometry"));
 				((org.postgresql.PGConnection)postgisConn).addDataType("box3d",Class.forName("org.postgis.PGbox3d"));
 			} catch (ClassNotFoundException e) {
 				logger.warn("Could not register native PostGIS datatypes", e);
 			}
 		} else {
 			logger.warn("Could not register native PostGIS datatypes");
 		}
 
 
 
 		return connection;
 	}
 
 	private File baseDir;
 
 	public void setBaseDir(File baseDir) {
 		this.baseDir = baseDir;
 	}
 
 	public File getBaseDir() {
 		return baseDir;
 	}
 
 	public void close() {
 		try {
 			shutdownDataSource(dataSource);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		if (preparedStatementConnection != null) {
 			try {
 				preparedStatementConnection.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private HashMap<String, PreparedStatement> stmts = new HashMap<String, PreparedStatement>();
 
 	private Connection preparedStatementConnection;
 
	public long executeNonQuery(ISqlStatementSource arg0, boolean writes)
			throws SQLException {
		final SqlStatement stmt = arg0.getStatement();

		return executeNonQuery(stmt, writes);
	}
 
 	public synchronized long executeNonQuery(SqlStatement stmt, boolean writes, boolean isDdlCommand, boolean retrieveKey) throws SQLException {
 
 		final String statementIdentifier = stmt.getStatementIdentifier();
 		PreparedStatement preparedStatement = null;
 		Connection c = null;
 		if (statementIdentifier != null) {
 			preparedStatement = stmts.get(statementIdentifier);
 			checkPreparedStatementConnection();
 			c = this.preparedStatementConnection;
 		} else {
 			c=getConnection();
 		}
 		if (preparedStatement==null) {
 			final String cmd = stmt.getCommandText();
 			logger.info("preparing SQL statement: " + cmd);
 			preparedStatement=c.prepareStatement(cmd, (retrieveKey?Statement.RETURN_GENERATED_KEYS:Statement.NO_GENERATED_KEYS));
 			if (statementIdentifier!=null) {
 				stmts.put(statementIdentifier, preparedStatement);
 			}
 		}
 
 		final Object[] parametervalues = stmt.getParametersAsObjectArray();
 		for (int i = 0; i < parametervalues.length; i++) {
 			// TODO: Extend this for more Datatypes!
 			if (parametervalues[i] instanceof String)
 				preparedStatement.setString(i+1, (String)parametervalues[i]);
 			else if (parametervalues[i] instanceof Integer)
 				preparedStatement.setLong(i+1, ((Integer)parametervalues[i]).longValue());
 			else if (parametervalues[i] instanceof Double)
 				preparedStatement.setDouble(i+1, ((Double)parametervalues[i]).doubleValue());
 		}
 		long ret = preparedStatement.executeUpdate();
 
 		if (retrieveKey) {
 			ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
 
 			if (generatedKeys.next() ) {
 				// Retrieve the auto generated key(s).
 				ret = generatedKeys.getInt(1);
 			}
 			generatedKeys.close();
 
 		}
 		if (statementIdentifier == null) {
 			preparedStatement.close();
 			c.close();
 		} else {
 			preparedStatement.clearParameters();
 
 		}
 
 		return ret;
 
 	}
 
 	private void checkPreparedStatementConnection() throws SQLException {
 		if (preparedStatementConnection != null && !preparedStatementConnection.isClosed()) return;
 
 		preparedStatementConnection=getConnection();
 	}
 
 	public static void main(String[] args) {
 		System.out.println("Checking global configuration ...");
 		System.out.println("");
 		System.out.print("connecting ...");
 		PostgresSQL sql;
 		try {
 			sql =new PostgresSQL();
 			System.out.println(" sucessfully connected to " + sql.getDatabaseName() + "!");
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 
 		Connection connection = null;
 		try {
 			connection = sql.getConnection();
 			System.out.println("");
 			System.out.println("Client properties:\t" + connection.getClientInfo().toString());
 			System.out.println("autocommit enabled?\t" + connection.getAutoCommit());
 			// SELECT postgis_full_version(); -- check whether PostGIS is available ...
 			System.out.println("");
 			System.out.println("== Avaliable databases ==");
 
 			Statement createStatement = connection.createStatement();
 			ResultSet executeQuery = createStatement.executeQuery("select datname from pg_database");
 			while (executeQuery.next()) {
 				System.out.println(executeQuery.getString(1));
 			}
 			System.out.println("");
 			executeQuery.close();
 			System.out.println("== tables for database " + sql.databaseName + " ==");
 
 			executeQuery = createStatement.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';");
 			while (executeQuery.next()) {
 				System.out.println(executeQuery.getString(1));
 			}
 			System.out.println("");
 
 
 
 
 			System.out.println("all right! Exiting now...");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (connection!=null)
 					connection.close();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			sql.close();
 		}
 
 	}
 
 	/**
 	 * Needs to be used on a try-with-resources construct, or needs to call close() at the end!
 	 * 
 	 * @param sql
 	 * @return
 	 * @throws DataStoreException
 	 */
 	public IEntityBacked query(SqlStatement sql) throws DataStoreException {
 		logger.warn("Naive implementation - considerable performance problems!");
 
 		Connection connection;
 		try {
 			connection = getConnection();
 			Statement createStatement = connection.createStatement();
 			ResultSet executeQuery = createStatement.executeQuery(sql.getCommandText());
 			return new IEntityBacked(executeQuery, connection);
 		} catch (SQLException e) {
 			throw new DataStoreException(e);
 		}
 	}
 
 }
