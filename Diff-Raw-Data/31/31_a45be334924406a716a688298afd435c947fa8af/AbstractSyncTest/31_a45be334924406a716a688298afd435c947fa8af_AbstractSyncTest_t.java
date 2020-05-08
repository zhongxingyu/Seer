 package de.consistec.syncframework.impl.adapter;
 
 import static de.consistec.syncframework.common.conflict.ConflictStrategy.FIRE_EVENT;
 import static de.consistec.syncframework.common.conflict.ConflictStrategy.SERVER_WINS;
 import static de.consistec.syncframework.common.util.CollectionsUtil.newHashMap;
 import static de.consistec.syncframework.impl.adapter.ConnectionType.CLIENT;
 import static de.consistec.syncframework.impl.adapter.ConnectionType.SERVER;
 
 import de.consistec.syncframework.common.Config;
 import de.consistec.syncframework.common.IConflictListener;
 import de.consistec.syncframework.common.SyncContext;
 import de.consistec.syncframework.common.SyncContext.LocalContext;
 import de.consistec.syncframework.common.SyncData;
 import de.consistec.syncframework.common.SyncDirection;
 import de.consistec.syncframework.common.TableSyncStrategies;
 import de.consistec.syncframework.common.TableSyncStrategy;
 import de.consistec.syncframework.common.adapter.DatabaseAdapterFactory;
 import de.consistec.syncframework.common.adapter.IDatabaseAdapter;
 import de.consistec.syncframework.common.client.ClientChangesEnumerator;
 import de.consistec.syncframework.common.conflict.ConflictStrategy;
 import de.consistec.syncframework.common.exception.ContextException;
 import de.consistec.syncframework.common.exception.SyncException;
 import de.consistec.syncframework.common.exception.database_adapter.DatabaseAdapterException;
 import de.consistec.syncframework.common.server.ServerChangesEnumerator;
 import de.consistec.syncframework.impl.TestDatabase;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Map;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.rules.TestRule;
 import org.junit.rules.TestWatcher;
 import org.junit.runner.Description;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Base class for database adapters integrations tests.
  * This class hold instances of logger, junit test watcher, and loads logging facility's configuration.
  * <p/>
  * Methods names meaning:<ul>
  * <li><b><i>Uc</i></b> = Unchanged</li>
  * <li><b><i>Mod</i></b> = Modification</li>
  * <li><b><i>Del</i></b> = Delete</li>
  * <li><b><i>Add</i></b> = Add</li>
  * </ul>
  * <p/>
  *
  * @author markus
  * @company Consistec Engineering and Consulting GmbH
  * @date 19.04.12 15:47
  * @since 0.0.1-SNAPSHOT
  */
 @RunWith(Parameterized.class)
 public abstract class AbstractSyncTest {
 
     @Parameterized.Parameters(name = "{index}: {0}")
     public static Collection<Object[]> AllDatabases() {
 
         return Arrays.asList(new Object[][]{
                 {new TestDatabase(GenericDatabaseAdapter.MYSQL_CONFIG_FILE, DumpDataSource.SupportedDatabases.MYSQL)},
                {new TestDatabase(GenericDatabaseAdapter.SQLITE_CONFIG_FILE, DumpDataSource.SupportedDatabases.SQLITE)},
                {new TestDatabase(PostgresDatabaseAdapter.CONFIG_FILE, DumpDataSource.SupportedDatabases.POSTGRESQL)}
             });
     }
     /**
      * Test watcher, with methods invoked before and after of each tests.
      * This "watcher" prints test name populateWithTestData and after each test.
      */
     @Rule
     public TestRule watchman2 = new TestWatcher() {
         @Override
         protected void starting(Description description) {
             LOGGER.info("start {} for test class {} ...", description.getMethodName(),
                 description.getTestClass().getCanonicalName());
         }
 
         @Override
         protected void finished(Description description) {
             LOGGER.info("end {} for test class {}", description.getMethodName(),
                 description.getTestClass().getCanonicalName());
         }
     };
     private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSyncTest.class.getCanonicalName());
     protected static Config CONF;
     private static final String[] tableNames = new String[]{"categories_md", "categories", "items", "items_md"};
     private transient ExecuteStatementHelper helper;
     protected TestDatabase db;
 
     public AbstractSyncTest(TestDatabase db) {
         this.db = db;
 
         // initialize logging framework
         DOMConfigurator.configure(ClassLoader.getSystemResource("log4j.xml"));
     }
 
     @Before
     public void setUp() throws IOException, SQLException {
         db.init();
         CONF = Config.getInstance();
         helper = new ExecuteStatementHelper(db.getClientConnection(), db.getServerConnection());
     }
 
     /**
      * Closes server and client connection.
      */
     @After
     public void tearDown() throws SQLException {
         db.closeConnections();
     }
 
     /**
      * Populates client and server database with test data.
      *
      * @throws SyncException
      * @throws SQLException
      * @throws ContextException
      */
     public void populateWithTestData() throws SyncException, SQLException, ContextException {
         InputStream stream = null;
         try {
             resetClientAndServerDatabase();
             stream = getResourceAsStream("server_data.xml");
             helper.executeUpdate(SERVER, stream);
 
             stream = getResourceAsStream("client_data.xml");
             helper.executeUpdate(CLIENT, stream);
         } finally {
             if (stream != null) {
                 try {
                     stream.close();
                 } catch (IOException e) {
                     LOGGER.warn("Cannot close server input stream!");
                 }
             }
         }
     }
 
     public InputStream getResourceAsStream(String resourceName) {
         return ClassLoader.getSystemResourceAsStream(resourceName);
     }
 
     public void resetClientAndServerDatabase() throws SyncException, SQLException, ContextException {
         Statement stmt = null;
 
         try {
             LOGGER.info("Dropping tables...");
             db.dropTablesOnClient(tableNames);
             db.dropTablesOnServer(tableNames);
 
             LOGGER.info("Creating tables...");
             db.executeQueriesOnServer(getCreateTableQueries());
 
             syncWithoutCompare(SERVER_WINS);
 
         } finally {
             if (stmt != null) {
                 stmt.close();
             }
         }
     }
 
     /**
      * @return CREATE TABLE sql statements for test tables.
      */
     protected String[] getCreateTableQueries() {
         return new String[]{
                 "create table categories (categoryid INTEGER NOT NULL PRIMARY KEY ,categoryname VARCHAR (30000),description VARCHAR (30000));",
                 "create table items (itemid INTEGER NOT NULL PRIMARY KEY ,itemname VARCHAR (30000),description VARCHAR (30000));"};
     }
 
     protected void initAndSync(String resource, SyncDirection syncDirection, ConflictStrategy strategy,
         ConnectionType dbToUpdate, ConnectionType type, ConnectionType type2) throws SyncException,
         SQLException, ContextException {
         InputStream stream = null;
         try {
             populateWithTestData();
             stream = getResourceAsStream(resource);
             helper.executeUpdate(dbToUpdate, stream);
 
             sync(syncDirection, strategy, type, type2);
         } finally {
             if (stream != null) {
                 try {
                     stream.close();
                 } catch (IOException e) {
                     LOGGER.warn("Cannot close input stream!");
                 }
             }
         }
     }
 
     protected void initAndSync(String resource, SyncDirection syncDirection, ConflictStrategy strategy,
         ConnectionType dbToUpdate, ConnectionType type) throws SyncException,
         SQLException, ContextException {
         InputStream stream = null;
         try {
             populateWithTestData();
             stream = getResourceAsStream(resource);
             helper.executeUpdate(dbToUpdate, stream);
 
             sync(syncDirection, strategy, type);
         } finally {
             if (stream != null) {
                 try {
                     stream.close();
                 } catch (IOException e) {
                     LOGGER.warn("Cannot close input stream!");
                 }
             }
         }
     }
 
     protected void initClientAndServerWithSync(String resource1, String resource2, SyncDirection syncDirection,
         ConflictStrategy strategy, ConnectionType type) throws
         SyncException, SQLException, ContextException {
         InputStream stream = null;
         try {
             populateWithTestData();
             stream = getResourceAsStream(resource1);
             helper.executeUpdate(SERVER, stream);
             stream = getResourceAsStream(resource2);
             helper.executeUpdate(CLIENT, stream);
 
             sync(syncDirection, strategy, type);
         } finally {
             if (stream != null) {
                 try {
                     stream.close();
                 } catch (IOException e) {
                     LOGGER.warn("Cannot close input stream!");
                 }
             }
         }
     }
 
     protected void initClientAndServerWithoutSync(String resource1, String resource2) throws
         SyncException, SQLException, ContextException {
         InputStream stream = null;
         try {
             populateWithTestData();
             stream = getResourceAsStream(resource1);
             helper.executeUpdate(SERVER, stream);
             stream = getResourceAsStream(resource2);
             helper.executeUpdate(CLIENT, stream);
 
         } finally {
             if (stream != null) {
                 try {
                     stream.close();
                 } catch (IOException e) {
                     LOGGER.warn("Cannot close input stream!");
                 }
             }
         }
     }
 
     protected void initClientAndServerAndSync(String resource, SyncDirection syncDirection, ConflictStrategy strategy,
         ConnectionType type) throws SyncException,
         SQLException, ContextException {
         initClientAndServerWithSync(resource, resource, syncDirection, strategy, type);
     }
 
     /**
      * Sync.
      *
      * @throws SyncException
      * @throws SQLException
      * @throws ContextException
      */
     public void sync(ConnectionType type) throws SyncException, SQLException, ContextException {
         sync(SyncDirection.BIDIRECTIONAL, SERVER_WINS, type);
     }
 
     // was already ignored
     /**
      * Sync.
      *
      * @throws SyncException
      * @throws SQLException
      * @throws ContextException
      */
     public void sync() throws SyncException, SQLException, ContextException {
         sync(SyncDirection.BIDIRECTIONAL, SERVER_WINS);
     }
 
     /**
      * @param strategy
      * @throws SyncException
      * @throws SQLException
      * @throws ContextException
      */
     public void sync(final SyncDirection direction, final ConflictStrategy strategy, final ConnectionType type) throws
         SyncException, SQLException,
         ContextException {
 
         sync(direction, strategy);
 
         compareDatabases(type);
     }
 
     /**
      * @param strategy
      * @throws SyncException
      * @throws SQLException
      * @throws ContextException
      */
     public void sync(final SyncDirection direction, final ConflictStrategy strategy, final ConnectionType type,
         final ConnectionType type2) throws
         SyncException, SQLException,
         ContextException {
 
         sync(direction, strategy);
 
         compareDatabases(type, type2);
     }
 
     /**
      * @param strategy
      * @throws SyncException
      * @throws SQLException
      * @throws ContextException
      */
     public void sync(final SyncDirection direction, final ConflictStrategy strategy) throws
         SyncException, SQLException,
         ContextException {
 
         TableSyncStrategy tableSyncStrategy = new TableSyncStrategy(direction, strategy);
 
         TableSyncStrategies strategies = new TableSyncStrategies();
         strategies.addSyncStrategyForTable("categories", tableSyncStrategy);
 
         final LocalContext localCtx = SyncContext.local(db.getServerDs(), db.getClientDs(), strategies);
 
         if (strategy == FIRE_EVENT) {
             localCtx.setConflictListener(new IConflictListener() {
                 @Override
                 public Map<String, Object> resolve(Map<String, Object> serverData, Map<String, Object> clientData) {
                     return serverData;
                 }
             });
         }
 
         readTableContentBeforeSync();
 
         localCtx.synchronize();
     }
 
     /**
      * @param strategy
      * @throws SyncException
      * @throws SQLException
      * @throws ContextException
      */
     public void syncWithoutCompare(final ConflictStrategy strategy) throws SyncException, SQLException,
         ContextException {
 
         final LocalContext localCtx = SyncContext.local(db.getServerDs(), db.getClientDs());
 
         if (strategy == FIRE_EVENT) {
             localCtx.setConflictListener(new IConflictListener() {
                 @Override
                 public Map<String, Object> resolve(Map<String, Object> serverData, Map<String, Object> clientData) {
                     return serverData;
                 }
             });
         }
 
         localCtx.synchronize();
     }
 
 //    /**
 //     * @param strategy
 //     * @throws SyncException
 //     * @throws SQLException
 //     * @throws ContextException
 //     */
 //    public void syncWithoutCompare(final ConflictStrategy strategy) throws SyncException, SQLException,
 //        ContextException {
 //
 //        final LocalContext localCtx = SyncContext.LocalContext.create(getServerDataSource(), getClientDataSource());
 //
 //        if (strategy == FIRE_EVENT) {
 //            localCtx.setConflictListener(new IConflictListener() {
 //                @Override
 //                public Map<String, Object> resolve(Map<String, Object> serverData, Map<String, Object> clientData) {
 //                    return serverData;
 //                }
 //            });
 //        }
 //
 //        localCtx.synchronize();
 //    }
     private Map<String, String> initTestTableStatementMap() {
         Map<String, String> statementsToExecute = newHashMap();
         statementsToExecute.put("categories", "select * from categories order by categoryid asc");
         statementsToExecute.put("items", "select * from items order by itemid asc");
         statementsToExecute.put(String.format("categories%s", CONF.getMdTableSuffix()),
             String.format("select pk, mdv, rev from categories%s  order by pk asc", CONF.getMdTableSuffix()));
         statementsToExecute.put(String.format("items%s", CONF.getMdTableSuffix()),
             String.format("select pk, mdv, rev from items%s order by pk asc", CONF.getMdTableSuffix()));
         return statementsToExecute;
     }
 
     private void readTableContentBeforeSync() throws SQLException {
         Map<String, String> tableStatementMap = initTestTableStatementMap();
         helper.readTableContent(tableStatementMap);
     }
 
     /**
      * Compare databases.
      */
     private void compareDatabases(ConnectionType type) throws
         SyncException, SQLException {
         Map<String, String> statementsToExecute = initTestTableStatementMap();
 
         helper.executeStatementAndCompareResults(statementsToExecute, CONF, type);
     }
 
     /**
      * Compare databases.
      */
     private void compareDatabases(ConnectionType type, ConnectionType type2) throws
         SyncException, SQLException {
         Map<String, String> statementsToExecute = initTestTableStatementMap();
 
         helper.executeStatementAndCompareResults(statementsToExecute, CONF, type, type2);
     }
 
     public TableSyncStrategies setStrategyForTable(String tableName, ConflictStrategy strategy, SyncDirection direction) {
         TableSyncStrategies strategies = new TableSyncStrategies();
         TableSyncStrategy tablsSyncStrategy = new TableSyncStrategy(direction, strategy);
         strategies.addSyncStrategyForTable(tableName, tablsSyncStrategy);
         return strategies;
     }
 
     public TableSyncStrategies setGlobalStrategy(ConflictStrategy conflictStrategy, SyncDirection syncDirection) {
         Config configInstance = Config.getInstance();
         configInstance.setGlobalConflictStrategy(conflictStrategy);
         configInstance.setGlobalSyncDirection(syncDirection);
         return new TableSyncStrategies();
     }
 
     public SyncData getClientChanges(TableSyncStrategies strategies)
         throws SyncException, ContextException, SQLException, DatabaseAdapterException {
         IDatabaseAdapter adapter = null;
 
         try {
             adapter = DatabaseAdapterFactory.newInstance(DatabaseAdapterFactory.AdapterPurpose.CLIENT);
 
             ClientChangesEnumerator clientChangesEnumerator = new ClientChangesEnumerator(adapter, strategies);
 
             return clientChangesEnumerator.getChanges();
 
         } finally {
             if (adapter != null) {
                 if (adapter.getConnection() != null) {
                     adapter.getConnection().close();
                 }
             }
         }
     }
 
     public SyncData getServerChanges(TableSyncStrategies strategies)
         throws SyncException, ContextException, SQLException, DatabaseAdapterException {
         IDatabaseAdapter adapter = null;
 
         try {
             adapter = DatabaseAdapterFactory.newInstance(DatabaseAdapterFactory.AdapterPurpose.SERVER);
 
             ServerChangesEnumerator serverChangesEnumerator = new ServerChangesEnumerator(adapter, strategies);
 
             return serverChangesEnumerator.getChanges(1);
 
         } finally {
             if (adapter != null) {
                 if (adapter.getConnection() != null) {
                     adapter.getConnection().close();
                 }
             }
         }
     }
 }
