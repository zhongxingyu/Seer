 package ch.ethz.mlmq.test.db;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Logger;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import ch.ethz.mlmq.dto.QueueDto;
 import ch.ethz.mlmq.logging.LoggerUtil;
import ch.ethz.mlmq.logging.PerformanceLoggerManager;
 import ch.ethz.mlmq.server.BrokerConfiguration;
 import ch.ethz.mlmq.server.db.dao.ClientDao;
 import ch.ethz.mlmq.server.db.dao.QueueDao;
 import ch.ethz.mlmq.server.db.util.DatabaseInitializer;
 
 public class DaoTest {
 
 	private static BrokerConfiguration config;
 
 	private static final Logger logger = Logger.getLogger("DaoTest");
 
 	private static DatabaseInitializer dbInitializer;
 	private static String dbName = "mlmqunittest" + System.currentTimeMillis();
 
 	private Connection connection;
 
 	@BeforeClass
 	public static void beforeClass() throws IOException, SQLException {
		PerformanceLoggerManager.configureDisabled();
 		LoggerUtil.initConsoleDebug();
 
 		config = BrokerConfiguration.loadFromJar("brokerconfig.properties");
 
 		dbInitializer = new DatabaseInitializer(config.getDbUrl(), config.getDbUserName(), config.getDbPassword(), dbName);
 
 		dbInitializer.connect();
 		dbInitializer.createDatabase();
 		dbInitializer.createTables();
 	}
 
 	@AfterClass
 	public static void afterClass() throws SQLException {
 		dbInitializer.deleteDatabase();
 	}
 
 	@Before
 	public void before() throws SQLException {
 		String url = config.getDbUrl() + "/" + dbName;
 		String userName = config.getDbUserName();
 		String password = config.getDbPassword();
 
 		connection = DriverManager.getConnection(url, userName, password);
 
 	}
 
 	@After
 	public void after() throws SQLException {
 		connection.close();
 	}
 
 	@Test
 	public void testQueueDao() throws SQLException {
 		try (QueueDao queueDao = new QueueDao()) {
 
 			queueDao.init(connection);
 
 			int beforeCreate = getQueueCount(connection);
 			QueueDto queue = queueDao.createQueue("SampleQueue");
 			int afterCreate = getQueueCount(connection);
 
 			Assert.assertNotNull(queue);
 			logger.info("Created Queue with id " + queue.getId());
 
 			queueDao.deleteQueue(queue.getId());
 			int afterDelete = getQueueCount(connection);
 
 			Assert.assertEquals(beforeCreate + 1, afterCreate);
 			Assert.assertEquals(beforeCreate, afterDelete);
 		}
 	}
 
 	@Test
 	public void testClientDao() throws SQLException {
 		try (ClientDao clientDao = new ClientDao()) {
 			clientDao.init(connection);
 
 			String name = "Blub : strange _ Chars " + System.currentTimeMillis();
 
 			Integer clientIdInteger = clientDao.getClientId(name);
 			Assert.assertNull(clientIdInteger);
 
 			int clientId = clientDao.insertNewClient(name);
 			logger.info("New ClientId is " + clientId);
 
 			clientIdInteger = clientDao.getClientId(name);
 			Assert.assertNotNull(clientIdInteger);
 
 			try {
 
 				clientDao.insertNewClient(name);
 				Assert.fail("Expected an exception");
 			} catch (Exception ex) {
 				logger.info("Expected Exception is " + ex);
 			}
 		}
 	}
 
 	private int getQueueCount(Connection connection) throws SQLException {
 		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM queue");) {
 			Assert.assertTrue(rs.next());
 			return rs.getInt(1);
 		}
 	}
 }
