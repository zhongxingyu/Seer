 package uk.ac.warwick.dcs.boss.model.dao.impl;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.openide.util.Lookup;
 
 import boss.plugins.MySQLPluginEntityDAO;
 import boss.plugins.spi.dao.IPluginDBMapping;
 import boss.plugins.spi.dao.IPluginEntity;
 
 import uk.ac.warwick.dcs.boss.model.dao.DAOException;
 import uk.ac.warwick.dcs.boss.model.dao.IAdminInterfaceQueriesDAO;
 import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
 import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
 import uk.ac.warwick.dcs.boss.model.dao.IDeadlineRevisionDAO;
 import uk.ac.warwick.dcs.boss.model.dao.IEntityDAO;
 import uk.ac.warwick.dcs.boss.model.dao.ILocalisationDAO;
 import uk.ac.warwick.dcs.boss.model.dao.IMarkDAO;
 import uk.ac.warwick.dcs.boss.model.dao.IMarkingAssignmentDAO;
 import uk.ac.warwick.dcs.boss.model.dao.IMarkingCategoryDAO;
 import uk.ac.warwick.dcs.boss.model.dao.IModelDAO;
 import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
 import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
 import uk.ac.warwick.dcs.boss.model.dao.IPluginMetadataDAO;
 import uk.ac.warwick.dcs.boss.model.dao.IResourceDAO;
 import uk.ac.warwick.dcs.boss.model.dao.IResultDAO;
 import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
 import uk.ac.warwick.dcs.boss.model.dao.IStudentInterfaceQueriesDAO;
 import uk.ac.warwick.dcs.boss.model.dao.ISubmissionDAO;
 import uk.ac.warwick.dcs.boss.model.dao.ITestDAO;
 
 public class MySQLDAOProducer implements IDAOSession {
 	
 	public static final int STORAGE_VERSION_1_0 = 1;  // 1.0 had no testing framework
 	public static final int STORAGE_VERSION_1_1 = 2;  // 1.1 introduced testing
 	public static final int STORAGE_VERSION_1_2 = 3;  // 1.2 is just a fix: forgot the moderator field!
 	
 	private Connection connection;
 	private long connectionTimestamp;
 	private ReentrantLock transactionLock;
 	
 	private String server;
 	private short port;
 	private String database;
 	private String username;
 	private String password;
 	private int connectionLifeSpan;
 	private int transactionTimeout;
 	
 	private String resourceDirectory;
 	private HashSet<Long> modifiedResources = new HashSet<Long>();
 			
 	public MySQLDAOProducer(Properties configuration) throws DAOException {
 		connection = null;
 		connectionTimestamp = Long.MIN_VALUE;
 		
 		this.server = configuration.getProperty("db.host");
 		this.database = configuration.getProperty("db.db");
 		this.username = configuration.getProperty("db.username");
 		this.password = configuration.getProperty("db.password");
 		this.port = Short.valueOf(configuration.getProperty("db.port"));
 		this.connectionLifeSpan = 60;
 		this.resourceDirectory = configuration.getProperty("db.resource_dir");
 		this.modifiedResources = new HashSet<Long>();
 		this.transactionTimeout = 300;
 		
 		this.transactionLock = new ReentrantLock(true);
 	}
 	
 	static private void executeAndLog(Logger logger, Statement statement, String sql) throws SQLException {
 		logger.log(Level.TRACE, "Executing: " + sql);
 		statement.executeUpdate(sql);
 	}
 	
 	public void initialiseStorage(boolean deleteExisting) throws DAOException {
 		Logger logger = Logger.getLogger("mysql");
 		logger.log(Level.INFO, "Initialising storage...");
 		
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("need to be in a transaction to initialise the database");
 		}
 	
 		if (deleteExisting) {		
 			logger.log(Level.INFO, "deleteExisting is true; dropping tables");
 			try {
 				Statement statement = connection.createStatement();
 				
 				// 1.0 tables
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS assignment_moderators");
 				
 				// 1.2 tables
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS assignment_markers");
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS assignment_requiredfilenames");
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS module_administrators");
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS module_students");
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS test_parameters");
 				
 
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS deadlinerevision");
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS i18n");
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS result");
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS mark");
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS markingassignment");
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS markingcategory");
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS submission");
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS test");
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS assignment");
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS module");
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS model");
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS resource");
 				
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS person");
 
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS version");
 				
 				executeAndLog(logger, statement, "DROP TABLE IF EXISTS pluginmetadata");
 				
 				statement.close();
 				
 			} catch (SQLException e) {
 				throw new DAOException("sql error - database in inconsistent state", e);
 			}
 			
 			// Destroy storage
 			deleteDirectory(new File(resourceDirectory));
 		}
 		
 		// Check version.
 		long currentVersion = -1;
 		boolean createDatabase = false;
 
 		try {
 			Statement statement = connection.createStatement();
 			logger.log(Level.TRACE, "Executing: SELECT version FROM version");
 			ResultSet rs = statement.executeQuery("SELECT version FROM version");
 
 			if (rs.first()) {
 				currentVersion = rs.getLong(1);
 			}
 		} catch (SQLException e) {
 			createDatabase = true;
 		}
 
 		if (createDatabase) {
 			logger.log(Level.INFO, "No storage found, creating version 1.2...");
 
 			try {
 				Statement statement = connection.createStatement();
 			
 				logger.log(Level.DEBUG, "Creating resource directory...");
 				new File(resourceDirectory).mkdirs();
 
 				//
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS i18n (" +
 						"  original VARCHAR(64) NOT NULL PRIMARY KEY," +
 						"  local CHAR(8) NOT NULL," + 
 						"  translation TEXT CHARACTER SET utf8 NOT NULL" +
 				") ENGINE=InnoDB");
 				//
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS resource (" +
 						"  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
 						"  filename VARCHAR(64) NOT NULL," +
 						"  mimetype VARCHAR(64) NOT NULL," +
 						"  timestamp DATETIME NOT NULL" +
 				") ENGINE=InnoDB");
 				//
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS model (" +
 						"  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
 						"  name VARCHAR(64) NOT NULL," +
 						"  uniq CHAR(16) NOT NULL UNIQUE KEY" +
 				") ENGINE=InnoDB");
 				//
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS person (" +
 						"  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
 						"  chosen_name VARCHAR(64) NOT NULL," +
 						"  uniq CHAR(16) NOT NULL UNIQUE KEY," +
 						"  email VARCHAR(64) NOT NULL," +
 						"  password CHAR(64) NOT NULL," +
 						"  administrator BOOLEAN NOT NULL" +
 				") ENGINE=InnoDB");
 				//
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS module (" +
 						"  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
 						"  name VARCHAR(64) NOT NULL," +
 						"  uniq CHAR(16) NOT NULL UNIQUE KEY," +
 						"  registration_required BOOLEAN NOT NULL," +
 						"  model_id INT NOT NULL," +
 						"  FOREIGN KEY (model_id) REFERENCES model(id) ON DELETE RESTRICT" +
 				") ENGINE=InnoDB");
 				//
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS assignment (" +
 						"  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
 						"  name VARCHAR(64) NOT NULL," +
 						"  deadline DATETIME NOT NULL," +
 						"  opening DATETIME NOT NULL," +
 						"  closing DATETIME NOT NULL," +
 						"  resource_id INT NOT NULL," +
 						"  module_id INT NOT NULL," +
 						"  allow_deletion BOOLEAN NOT NULL," +
 						"  FOREIGN KEY (module_id) REFERENCES module(id) ON DELETE RESTRICT," +
 						"  FOREIGN KEY (resource_id) REFERENCES resource(id) ON DELETE RESTRICT" +
 				") ENGINE=InnoDB");
 				//
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS submission (" +
 						"  id INT NOT NULL AUTO_INCREMENT UNIQUE KEY," +
 						"  submission_time DATETIME NOT NULL," +
 						"  resource_id INT NOT NULL," +
 						"  resource_subdirectory VARCHAR(64) NOT NULL," +
 						"  person_id INT NOT NULL," +
 						"  assignment_id INT NOT NULL," +
 						"  security_code CHAR(64) NOT NULL," +
 						"  active BOOLEAN NOT NULL," +
 						"  FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE RESTRICT," +
 						"  FOREIGN KEY (assignment_id) REFERENCES assignment(id) ON DELETE RESTRICT," +
 						"  FOREIGN KEY (resource_id) REFERENCES resource(id) ON DELETE RESTRICT" +
 				") ENGINE=InnoDB");
 				//
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS deadlinerevision (" +
 						"  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
 						"  comment TEXT NOT NULL," +
 						"  deadline DATETIME NOT NULL," +
 						"  person_id INT NOT NULL," +
 						"  assignment_id INT NOT NULL," +
 						"  FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE RESTRICT," +
 						"  FOREIGN KEY (assignment_id) REFERENCES assignment(id) ON DELETE RESTRICT" +
 				") ENGINE=InnoDB");
 				//
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS markingcategory (" +
 						"  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
 						"  name VARCHAR(64) NOT NULL," +
 						"  weighting INT NOT NULL," +
 						"  max_mark INT NOT NULL," +
 						"  assignment_id INT NOT NULL," +
 						"  FOREIGN KEY (assignment_id) REFERENCES assignment(id) ON DELETE RESTRICT" +
 				") ENGINE=InnoDB");
 				//
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS test (" +
 						"  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
 						"  student_test BOOLEAN NOT NULL," +
 						"  name VARCHAR(64) NOT NULL," +
 						"  classname VARCHAR(128) NOT NULL," +
 						"  executor_classname VARCHAR(128) NOT NULL," +
 						"  max_time INTEGER NOT NULL," +
 						"  command VARCHAR(255) NOT NULL," +
 						"  assignment_id INT NOT NULL," +
 						"  resource_id INT NOT NULL," +
 						"  FOREIGN KEY (assignment_id) REFERENCES assignment(id) ON DELETE RESTRICT," +
 						"  FOREIGN KEY (resource_id) REFERENCES resource(id) ON DELETE RESTRICT" +
 				") ENGINE=InnoDB");
 				//
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS markingassignment (" +
 						"  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
 						"  assignment_id INT NOT NULL," +
 						"  student_id INT NOT NULL," +
 						"  marker_id INT NOT NULL," +
 						"  blind BOOLEAN NOT NULL," +
 						"  moderator BOOLEAN NOT NULL," +
 						"  FOREIGN KEY (assignment_id) REFERENCES assignment(id) ON DELETE RESTRICT," +
 						"  FOREIGN KEY (student_id) REFERENCES person(id) ON DELETE RESTRICT," +
 						"  FOREIGN KEY (marker_id) REFERENCES person(id) ON DELETE RESTRICT" +
 				") ENGINE=InnoDB");
 				//
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS mark (" +
 						"  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
 						"  comment TEXT NOT NULL," +
 						"  timestamp DATETIME NOT NULL," +
 						"  value INT NOT NULL," +
 						"  markingcategory_id INT NOT NULL," +
 						"  markingassignment_id INT NOT NULL," +
 						"  FOREIGN KEY (markingcategory_id) REFERENCES markingcategory(id)," +
 						"  FOREIGN KEY (markingassignment_id) REFERENCES markingassignment(id)" +
 				") ENGINE=InnoDB");
 				//
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS result (" +
 						"  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
 						"  result DOUBLE NOT NULL," +
 						"  timestamp DATETIME NOT NULL," +
 						"  incomplete_marking BOOLEAN NOT NULL," +
 						"  assignment_id INT NOT NULL," +
 						"  student_id INT NOT NULL," +
 						"  FOREIGN KEY (assignment_id) REFERENCES assignment(id)," +
 						"  FOREIGN KEY (student_id) REFERENCES person(id)" +
 						") ENGINE=InnoDB"
 				);
 				//
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS assignment_markers (" +
 						"  assignment_id INT NOT NULL," +
 						"  marker_id INT NOT NULL," +
 						"  FOREIGN KEY (assignment_id) REFERENCES assignment(id) ON DELETE RESTRICT," +
 						"  FOREIGN KEY (marker_id) REFERENCES person(id) ON DELETE RESTRICT" +
 				") ENGINE=InnoDB");
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS assignment_requiredfilenames (" +
 						"  assignment_id INT NOT NULL," +
 						"  filename VARCHAR(64) NOT NULL," +
 						"  FOREIGN KEY (assignment_id) REFERENCES assignment(id) ON DELETE RESTRICT" +
 				") ENGINE=InnoDB");		
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS module_administrators (" +
 						"  module_id INT NOT NULL," +
 						"  administrator_id INT NOT NULL," +
 						"  FOREIGN KEY (module_id) REFERENCES module(id) ON DELETE RESTRICT," +
 						"  FOREIGN KEY (administrator_id) REFERENCES person(id) ON DELETE RESTRICT" +
 				") ENGINE=InnoDB");
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS module_students (" +
 						"  module_id INT NOT NULL," +
 						"  student_id INT NOT NULL," +
 						"  FOREIGN KEY (module_id) REFERENCES module(id) ON DELETE RESTRICT," +
 						"  FOREIGN KEY (student_id) REFERENCES person(id) ON DELETE RESTRICT" +
 				") ENGINE=InnoDB");
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS test_parameters (" +
 						"  test_id INT NOT NULL," +
 						"  key_name CHAR(32) NOT NULL," +
 						"  value TEXT NOT NULL," +
 						"  FOREIGN KEY (test_id) REFERENCES test(id) ON DELETE RESTRICT" +
 				") ENGINE=InnoDB");
 
 				executeAndLog(logger, statement, "CREATE TABLE IF NOT EXISTS version (version INT NOT NULL)");
 				executeAndLog(logger, statement, "DELETE FROM version");
 				executeAndLog(logger, statement, "INSERT INTO version (version) VALUES (" + STORAGE_VERSION_1_2 + ")");
 				
 				
 				executeAndLog(logger, statement, "CREATE TABLE pluginmetadata (" +
 						"  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
 						"  plugin_id varchar(30) NOT NULL," +
 						"  name varchar(64) NOT NULL," +
 						"  author varchar(64) DEFAULT NULL," +
 						"  email varchar(64) DEFAULT NULL," +
 						"  version varchar(20) NOT NULL," +
 						"  description text," +
 						"  lib_filenames text," +
 						"  enable BOOLEAN NOT NULL," +
 						"  configurable BOOLEAN NOT NULL," +
						"  PRIMARY KEY (id)," +
 						"  UNIQUE KEY plugin_id (plugin_id)" +
 						") ENGINE=InnoDB");
 				
 				statement.close();
 			} catch (SQLException e) {
 				throw new DAOException("sql error", e);
 			}
 		} else if (currentVersion == STORAGE_VERSION_1_2) {
 			logger.log(Level.INFO, "Found storage version 1.2, no update necessary.");
 
 		} else if (currentVersion == STORAGE_VERSION_1_1) {
 			logger.log(Level.INFO, "Found storage version 1.1, upgrading to 1.2.");
 
 			try {
 				Statement statement = connection.createStatement();
 
 				executeAndLog(logger, statement, "ALTER TABLE markingassignment" +
 						" ADD COLUMN moderator BOOLEAN NOT NULL" +
 				" AFTER blind");
 
 				executeAndLog(logger, statement, "DELETE FROM version");
 				statement.executeUpdate("INSERT INTO version (version) VALUES (" + STORAGE_VERSION_1_2 + ")");
 				
 				statement.close();
 			} catch (SQLException e) {
 				throw new DAOException("sql error", e);
 			}
 		} else if (currentVersion == STORAGE_VERSION_1_0) {
 			throw new DAOException("Storage version 1.0 detected.  This is too old to update safely.");
 		} else {
 			throw new DAOException("Storage already exists but is of unsupported version");
 		}
 		
 	}
 	
 	public void abortTransaction(){
 		if (!transactionLock.isHeldByCurrentThread()) {
 			Logger.getLogger("mysql").log(Level.WARN, "Aborting without a transaction in place");
 			return;
 		}
 		
 		try {
 			if (this.connection != null) {
 				connection.rollback();
 			}
 		} catch (SQLException e) {
 			throw new Error("SQL error during transaction abort", e);
 		}
 		
 		try {
 			MySQLResourceDAO.abortTransaction(resourceDirectory, modifiedResources);
 		} catch (DAOException e) {
 			throw new Error("DAO exception in transaction abort", e);
 		}
 		
 		transactionLock.unlock();
 	}
 
 	public void beginTransaction() throws DAOException {
 		try {
 			if (transactionLock.isHeldByCurrentThread()) {
 				transactionLock.unlock();
 				throw new DAOException("The previous transaction by this thread has not finished!  To avoid infinite deadlock the lock has been released, but please report this with the FULL output to an administrator.");
 			}
 			
 			if (!transactionLock.tryLock(this.transactionTimeout, TimeUnit.MILLISECONDS)) {
 				throw new DAOException("timeout reached on transaction");
 			}
 		} catch (InterruptedException e) {
 			throw new DAOException("interrupted waiting on transaction", e);
 		}
 		
 		assureConnectivity();		
 	}
 
 	public void endTransaction() throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction to abort");
 		}
 
 		try {
 			connection.commit();
 		} catch (SQLException e) {
 			throw new DAOException("SQL error", e);
 		}
 		
 		MySQLResourceDAO.endTransaction(resourceDirectory, modifiedResources);
 		
 		transactionLock.unlock();
 	}
 
 	public IResourceDAO getResourceDAOInstance() throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 		
 		return new MySQLResourceDAO(connection, resourceDirectory, modifiedResources);
 
 	}
 	
 	public IAssignmentDAO getAssignmentDAOInstance() throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 		
 		return new MySQLAssignmentDAO(connection);
 	}
 
 	public IDeadlineRevisionDAO getDeadlineRevisionDAOInstance()
 			throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 		
 		return new MySQLDeadlineRevisionDAO(connection);
 	}
 
 	public IMarkDAO getMarkDAOInstance() throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 		
 		return new MySQLMarkDAO(connection);
 	}
 
 	public IMarkingCategoryDAO getMarkingCategoryDAOInstance()
 			throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 		
 		return new MySQLMarkingCategoryDAO(connection);
 	}
 
 	public IModelDAO getModelDAOInstance() throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 		
 		return new MySQLModelDAO(connection);
 	}
 	
 	public IModuleDAO getModuleDAOInstance() throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 		
 		return new MySQLModuleDAO(connection);
 	}
 
 	public IPersonDAO getPersonDAOInstance() throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 		
 		return new MySQLPersonDAO(connection);
 	}
 
 	public IMarkingAssignmentDAO getMarkingAssignmentDAOInstance() throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 		
 		return new MySQLMarkingAssignmentDAO(connection);
 	}
 
 	public ISubmissionDAO getSubmissionDAOInstance() throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 		
 		return new MySQLSubmissionDAO(connection);
 	}
 
 	public ITestDAO getTestDAOInstance() throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 		
 		return new MySQLTestDAO(connection);
 	}
 
 	public IResultDAO getResultDAOInstance() throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 		
 		return new MySQLResultDAO(connection);
 	}
 	
 	public IStudentInterfaceQueriesDAO getStudentInterfaceQueriesDAOInstance() throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 		
 		return new MySQLStudentInterfaceQueriesDAO(connection);
 	}
 	
 	public uk.ac.warwick.dcs.boss.model.dao.IMarkerInterfaceQueriesDAO getMarkerInterfaceQueriesDAOInstance() throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 		
 		return new MySQLMarkerInterfaceQueriesDAO(connection);
 	}
 
 	public IStaffInterfaceQueriesDAO getStaffInterfaceQueriesDAOInstance() throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 		
 		return new MySQLStaffInterfaceQueriesDAO(connection);
 	}
 
 	public IAdminInterfaceQueriesDAO getAdminInterfaceQueriesDAOInstance()
 			throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 		
 		return new MySQLAdminInterfaceQueriesDAO(connection);
 	}
 	
 	public ILocalisationDAO getLocalisationDAO() throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 
 		return new MySQLLocalisationDAO(connection);
 	}	
 	
 	protected void assureConnectivity() throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 
 		if (connection == null || new Date().getTime() > connectionTimestamp + connectionLifeSpan) {
 			Logger logger = Logger.getLogger("mysql");
 			logger.log(Level.DEBUG, "Reconnecting to MySQL");
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 					logger.log(Level.WARN, e);
 				}
 			}
 			
 			try {
 				Class.forName("com.mysql.jdbc.Driver");
 				connection = DriverManager.getConnection("jdbc:mysql://" + server + ":" + port + "/" + database, username, password);
 				connection.setAutoCommit(false);
 			} catch (SQLException e) {
 				throw new DAOException("SQL error", e);
 			} catch (ClassNotFoundException e) {
 				throw new DAOException("No MySQL driver", e);
 			}
 			connectionTimestamp = new Date().getTime();
 		}
 	}
 	
 	private static void deleteDirectory(File dir)
 	{
 		File[] fileArray = dir.listFiles();
 
 		if (fileArray != null)
 		{
 			for (int i = 0; i < fileArray.length; i++)
 			{
 				if (fileArray[i].isDirectory())
 					deleteDirectory(fileArray[i]);
 				else
 					fileArray[i].delete();
 			}
 		}
 		dir.delete();
 	}
 	
 	public void cleanUp() {
 		Logger.getLogger("mysql").log(Level.INFO, "Shutting down MySQL connection");
 		try {
 			if (this.connection != null) {
 				this.connection.rollback();
 			}
 		} catch (SQLException e) {
 			
 		}
 		try {
 			if (this.connection != null) {
 				this.connection.close();
 			}
 		} catch (SQLException e) {
 		
 		}
 	}
 
 //	public <T extends PluginEntity> IEntityDAO<T> getAdditionalDAOInstance(Class<T> clazz){
 //		return null;
 //	} 
 	
 	@SuppressWarnings("unchecked")
 	public <T extends IPluginEntity> IEntityDAO<T> getPluginDAOInstance (Class<T> clazz) throws DAOException {
 		Iterator<? extends IPluginDBMapping> ite = Lookup.getDefault().lookupAll(IPluginDBMapping.class).iterator();
 		while (ite.hasNext()) {
 			IPluginDBMapping pluginEntityDao = ite.next();
 			if (pluginEntityDao.getEntityType().equals(clazz)) {
 				MySQLPluginEntityDAO<T> mySQLPluginEntityDao = new MySQLPluginEntityDAO<T>(connection, (IPluginDBMapping<T>)pluginEntityDao);
 				if (!transactionLock.isHeldByCurrentThread()) {
 					throw new DAOException("no transaction in process");
 				}
 				return mySQLPluginEntityDao;
 			}
 		}	
 		throw new DAOException("no DAO can be found for entity: " + clazz.getSimpleName());
 	}
 
 	public IPluginMetadataDAO getPluginMetadataDAOInstance()
 			throws DAOException {
 		if (!transactionLock.isHeldByCurrentThread()) {
 			throw new DAOException("no transaction in process");
 		}
 		
 		return new MySQLPluginMetadataDAO(connection);
 	}
 
 	
 }
