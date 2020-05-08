 package ro.gagarin.jdbc;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import org.apache.log4j.Logger;
 
 import ro.gagarin.BaseDAO;
 import ro.gagarin.ConfigurationManager;
 import ro.gagarin.ModelFactory;
 import ro.gagarin.config.Config;
 import ro.gagarin.session.Session;
 
 public class BaseJdbcDAO implements BaseDAO {
 	private static final transient Logger LOG = Logger.getLogger(BaseJdbcDAO.class);
 
 	private static boolean DRIVER_LOADED;
 
 	private boolean ourConnection = false;
 
 	private Connection connection;
 
 	private boolean rollback = false;
 
 	public BaseJdbcDAO(Session session) {
 		if (session == null)
 			throw new NullPointerException("attempt to initialize BaseHibernateManager with null");
 
 		ConfigurationManager cfgManager = ModelFactory.getConfigurationManager(session);
 		checkLoadDBDriver(cfgManager);
 
 		synchronized (session) {
 			session.setBusy(true);
			Object property = session.getProperty(BaseJdbcDAO.class);
 			if (property instanceof BaseJdbcDAO) {
 
 				Connection connection = ((BaseJdbcDAO) property).getConnection();
 				this.connection = connection;
 				this.ourConnection = false;
 			} else {
 				// null -- or wrong object
 				if (property != null) {
 					throw new RuntimeException("Wrong object type was found on session for "
 							+ BaseJdbcDAO.class.getName() + "; found:"
 							+ property.getClass().getName());
 				}
 				this.connection = createConnection(cfgManager);
 				this.ourConnection = true;
				session.setProperty(BaseJdbcDAO.class, this);
 				LOG.debug("Created Connection Instance " + connection.toString());
 			}
 		}
 	}
 
 	private Connection createConnection(ConfigurationManager cfgManager) {
 		String url = cfgManager.getString(Config.JDBC_CONNECTION_URL);
 		String user = cfgManager.getString(Config.JDBC_DB_USER);
 		String password = cfgManager.getString(Config.JDBC_DB_PASSWORD);
 		try {
 			Connection connection = DriverManager.getConnection(url, user, password);
 			connection.setAutoCommit(false);
 			return connection;
 		} catch (SQLException e) {
 			LOG.error("Could not get DB connection for url:" + url, e);
 			throw new RuntimeException("Database is unavailable", e);
 		}
 
 	}
 
 	protected Connection getConnection() {
 		return this.connection;
 	}
 
 	private void checkLoadDBDriver(ConfigurationManager cfgManager) {
 		if (DRIVER_LOADED)
 			return;
 		try {
 			// Load the JDBC driver
 			String driverName = cfgManager.getString(Config.JDBC_DB_DRIVER);
 			LOG.info("Loading DB driver " + driverName);
 			Class.forName(driverName);
 			BaseJdbcDAO.DRIVER_LOADED = true;
 		} catch (ClassNotFoundException e) {
 			BaseJdbcDAO.DRIVER_LOADED = false;
 			LOG.error("Could not load DB Driver class", e);
 		}
 
 	}
 
 	public void release() {
 
 		Connection tmpConn = this.connection;
 		this.connection = null;
 
 		if (this.ourConnection) {
 
 			RuntimeException exception = null;
 			if (!this.rollback) {
 				LOG.debug("Committing connection " + tmpConn.toString());
 				try {
 					tmpConn.commit();
 					tmpConn.close();
 					LOG.debug("Released connection " + tmpConn.toString());
 					return;
 				} catch (SQLException e) {
 					// this is the most relevant exception, so keep it then
 					// throw it
 					exception = new RuntimeException(e);
 					LOG.error("Exception on commit:", e);
 				}
 			}
 			LOG.debug("Rollback connection " + tmpConn.toString());
 			try {
 				tmpConn.rollback();
 			} catch (SQLException e) {
 				if (exception == null)
 					exception = new RuntimeException(e);
 				LOG.error("Exception on rollback:", e);
 			}
 			try {
 				tmpConn.close();
 			} catch (SQLException e) {
 				if (exception == null)
 					exception = new RuntimeException(e);
 				LOG.error("Exception on close:", e);
 			}
 			if (exception != null)
 				throw exception;
 		}
 
 	}
 
 	public void markRollback() {
 		this.rollback = true;
 	}
 
 	public void checkCreateDependencies() {
 		TableEnum[] values = TableEnum.values();
 		try {
 			for (TableEnum table : values) {
 				PreparedStatement prepareStatement;
 				try {
 					prepareStatement = getConnection().prepareStatement(table.getTest());
 					if (prepareStatement.execute())
 						continue;
 				} catch (SQLException e) {
 					LOG.info("Test " + table.name() + " failed; Creating...");
 				}
 				prepareStatement = getConnection().prepareStatement(table.getCreate());
 				if (prepareStatement.execute()) {
 					LOG.info(table.name() + " Created.");
 				} else {
 					LOG.info(table.name() + " Not created.");
 				}
 
 			}
 		} catch (SQLException e) {
 			LOG.error("Unexpected exception during the table verification", e);
 			throw new RuntimeException(e);
 		}
 	}
 }
