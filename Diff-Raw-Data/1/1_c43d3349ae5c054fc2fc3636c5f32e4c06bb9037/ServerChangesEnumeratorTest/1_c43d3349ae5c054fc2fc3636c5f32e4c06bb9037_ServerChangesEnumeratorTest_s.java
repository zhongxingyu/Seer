 package de.consistec.syncframework.impl.adapter.it_postgres;
 
 import static org.junit.Assert.assertTrue;
 
 import de.consistec.syncframework.common.Config;
 import de.consistec.syncframework.common.SyncDirection;
 import de.consistec.syncframework.common.TableSyncStrategies;
 import de.consistec.syncframework.common.TableSyncStrategy;
 import de.consistec.syncframework.common.Tuple;
 import de.consistec.syncframework.common.adapter.DatabaseAdapterFactory;
 import de.consistec.syncframework.common.adapter.IDatabaseAdapter;
 import de.consistec.syncframework.common.conflict.ConflictStrategy;
 import de.consistec.syncframework.common.data.Change;
 import de.consistec.syncframework.common.exception.ContextException;
 import de.consistec.syncframework.common.exception.SyncException;
 import de.consistec.syncframework.common.exception.database_adapter.DatabaseAdapterException;
 import de.consistec.syncframework.common.server.ServerChangesEnumerator;
 import de.consistec.syncframework.impl.adapter.AbstractSyncTest;
 import de.consistec.syncframework.impl.adapter.ConnectionType;
 import de.consistec.syncframework.impl.adapter.DumpDataSource;
 import de.consistec.syncframework.impl.adapter.TestUtil;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.List;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
import org.junit.Ignore;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class tests the correct handling of getChanges for server side.
  *
  * @author marcel
  * @company Consistec Engineering and Consulting GmbH
  * @date 13.12.12 15:10
  */
 public class ServerChangesEnumeratorTest extends AbstractSyncTest {
 
 //<editor-fold defaultstate="expanded" desc=" Class fields " >
 
 //</editor-fold>
 
 //<editor-fold defaultstate="expanded" desc=" Class constructors " >
 
 //</editor-fold>
 
 //<editor-fold defaultstate="collapsed" desc=" Class accessors and mutators " >
 
 //</editor-fold>
 
 //<editor-fold defaultstate="expanded" desc=" Class methods " >
 
 //</editor-fold>
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ServerChangesEnumeratorTest.class.getCanonicalName());
 
     public static final String CONFIG_FILE = "/config_postgre.properties";
 
     protected static final DumpDataSource clientDs = new DumpDataSource(DumpDataSource.SupportedDatabases.POSTGRESQL,
         ConnectionType.CLIENT);
     protected static final DumpDataSource serverDs = new DumpDataSource(DumpDataSource.SupportedDatabases.POSTGRESQL,
         ConnectionType.SERVER);
 
     /**
      * Jdbc connection for client database.
      * Use this connection to prepare the data for tests.
      */
     protected static Connection clientConnection;
     /**
      * Jdbc connection for server database.
      * Use this connection to prepare the data for tests.
      */
     protected static Connection serverConnection;
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         clientConnection = clientDs.getConnection();
         serverConnection = serverDs.getConnection();
     }
 
     @Before
     public void setUp() throws IOException {
         TestUtil.initConfig(getClass(), CONFIG_FILE);
     }
 
     @Override
     public Connection getServerConnection() {
         return serverConnection;
     }
 
     @Override
     public Connection getClientConnection() {
         return clientConnection;
     }
 
     /**
      * Closes server and client connection.
      *
      * @throws java.sql.SQLException
      */
     @AfterClass
     public static void tearDownClass() throws SQLException {
 
         if (clientConnection != null) {
             try {
                 clientConnection.close();
                 clientConnection = null;
             } catch (SQLException e) {
                 LOGGER.error("could not close client connection!", e);
                 throw e;
             }
         }
 
         if (serverConnection != null) {
             try {
                 serverConnection.close();
                 serverConnection = null;
             } catch (SQLException e) {
                 LOGGER.warn("could not close server connection!", e);
                 throw e;
             }
         }
     }
 
     private Tuple<Integer, List<Change>> testGetChangesGlobal(ConflictStrategy strategy, SyncDirection direction) throws
         SyncException,
         ContextException, SQLException, DatabaseAdapterException {
         String resource = "category8_b_insert.xml";
         String resource2 = "category8_a_insert.xml";
         IDatabaseAdapter adapter = null;
         Tuple<Integer, List<Change>> serverChanges = null;
 
         try {
             // init db with data
             initClientAndServerWithoutSync(resource, resource2);
 
             adapter = DatabaseAdapterFactory.newInstance(DatabaseAdapterFactory.AdapterPurpose.SERVER);
             Config configInstance = Config.getInstance();
             configInstance.setGlobalConflictStrategy(strategy);
             configInstance.setGlobalSyncDirection(direction);
             ServerChangesEnumerator serverChangesEnumerator = new ServerChangesEnumerator(adapter,
                 new TableSyncStrategies());
 
             serverChanges = serverChangesEnumerator.getChanges(1);
 
         } finally {
             if (adapter != null) {
                 if (adapter.getConnection() != null) {
                     adapter.getConnection().close();
                 }
             }
         }
         return serverChanges;
     }
 
     private Tuple<Integer, List<Change>> testGetChangesPerTable(ConflictStrategy strategy, SyncDirection direction
     ) throws
         SyncException,
         ContextException, SQLException, DatabaseAdapterException {
         String resource = "category9_b_insert.xml";
         String resource2 = "category9_a_insert.xml";
         IDatabaseAdapter adapter = null;
         Tuple<Integer, List<Change>> serverChanges = null;
 
         try {
             // init db with data
             initClientAndServerWithoutSync(resource, resource2);
 
             adapter = DatabaseAdapterFactory.newInstance(DatabaseAdapterFactory.AdapterPurpose.SERVER);
 
             TableSyncStrategies strategies = new TableSyncStrategies();
             TableSyncStrategy tablsSyncStrategy = new TableSyncStrategy(direction, strategy);
             strategies.addSyncStrategyForTable("categories", tablsSyncStrategy);
 
             ServerChangesEnumerator serverChangesEnumerator = new ServerChangesEnumerator(adapter, strategies);
 
             serverChanges = serverChangesEnumerator.getChanges(1);
 
         } finally {
             if (adapter != null) {
                 if (adapter.getConnection() != null) {
                     adapter.getConnection().close();
                 }
             }
         }
         return serverChanges;
     }
 
     @Test
     public void getChangesServerToClient() throws ContextException, SyncException, DatabaseAdapterException,
         SQLException
 
     {
         Tuple<Integer, List<Change>> serverChanges = testGetChangesGlobal(ConflictStrategy.SERVER_WINS,
             SyncDirection.SERVER_TO_CLIENT);
 
         assertTrue(serverChanges.getValue2().size() == 1);
         assertTrue(serverChanges.getValue1() == 3);
     }
 
     @Test
     public void getChangesClientToServer() throws SyncException, ContextException, SQLException,
         DatabaseAdapterException {
 
         Tuple<Integer, List<Change>> serverChanges = testGetChangesGlobal(ConflictStrategy.CLIENT_WINS,
             SyncDirection.CLIENT_TO_SERVER);
 
         assertTrue(serverChanges.getValue2().size() == 0);
         assertTrue(serverChanges.getValue1() == 3);
     }
 
     @Test
     public void getChangesBidirectional() throws SyncException, ContextException, SQLException,
         DatabaseAdapterException {
 
         Tuple<Integer, List<Change>> serverChanges = testGetChangesGlobal(ConflictStrategy.CLIENT_WINS,
             SyncDirection.BIDIRECTIONAL);
 
         assertTrue(serverChanges.getValue2().size() == 1);
         assertTrue(serverChanges.getValue1() == 3);
     }
 
     @Test
     public void getChangesServerToClientPerTable() throws ContextException, SyncException, DatabaseAdapterException,
         SQLException
 
     {
         Tuple<Integer, List<Change>> serverChanges = testGetChangesPerTable(ConflictStrategy.SERVER_WINS,
             SyncDirection.SERVER_TO_CLIENT);
 
         assertTrue(serverChanges.getValue2().size() == 2);
         assertTrue(serverChanges.getValue1() == 3);
     }
 
     @Test
     public void getChangesClientToServerPerTable() throws SyncException, ContextException, SQLException,
         DatabaseAdapterException {
 
         Tuple<Integer, List<Change>> serverChanges = testGetChangesPerTable(ConflictStrategy.CLIENT_WINS,
             SyncDirection.CLIENT_TO_SERVER);
 
         assertTrue(serverChanges.getValue2().size() == 1);
         assertTrue(serverChanges.getValue1() == 3);
     }
 
     @Test
     public void getChangesBidirectionalPerTable() throws SyncException, ContextException, SQLException,
         DatabaseAdapterException {
 
         Tuple<Integer, List<Change>> serverChanges = testGetChangesPerTable(ConflictStrategy.CLIENT_WINS,
             SyncDirection.BIDIRECTIONAL);
 
         assertTrue(serverChanges.getValue2().size() == 2);
         assertTrue(serverChanges.getValue1() == 3);
     }
 }
