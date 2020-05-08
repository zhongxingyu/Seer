 package de.consistec.syncframework.impl.adapter.it_postgres;
 
 import static de.consistec.syncframework.common.adapter.DatabaseAdapterFactory.AdapterPurpose.CLIENT;
 import static de.consistec.syncframework.common.adapter.DatabaseAdapterFactory.AdapterPurpose.SERVER;
 import de.consistec.syncframework.common.Config;
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
import de.consistec.syncframework.impl.adapter.DumpDataSource;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.Collection;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 /**
  * @author marcel
  * @company Consistec Engineering and Consulting GmbH
  * @date 28.01.13 15:44
  */
@RunWith(value = Parameterized.class)
 public class ChangesEnumeratorTest {
 
     protected static String[] tableNames = new String[]{"categories", "categories_md", "items", "items_md"};
     protected static String[] createQueries = new String[]{
         "CREATE TABLE categories (categoryid INTEGER NOT NULL PRIMARY KEY ,categoryname VARCHAR (300),description VARCHAR (300));",
         "CREATE TABLE categories_md (pk INTEGER NOT NULL PRIMARY KEY, mdv VARCHAR (300), rev INTEGER DEFAULT 1, f INTEGER DEFAULT 0);",
         "CREATE TABLE items (id INTEGER NOT NULL PRIMARY KEY ,name VARCHAR (300),description VARCHAR (300));",
         "CREATE TABLE items_md (pk INTEGER NOT NULL PRIMARY KEY, mdv VARCHAR (300), rev INTEGER DEFAULT 1, f INTEGER DEFAULT 0);"};
     private static ConflictStrategy savedConflictStrategy;
     private static SyncDirection savedSyncDirection;
     protected TestDatabase db;
 
     @Parameterized.Parameters(name = "{index}: {0}")
     public static Collection<Object[]> AllScenarii() {
 
         return Arrays.asList(new Object[][]{
                {new TestDatabase("/config_sqlite.properties", DumpDataSource.SupportedDatabases.SQLITE)},
                {new TestDatabase("/config_mysql.properties", DumpDataSource.SupportedDatabases.MYSQL)},
                {new TestDatabase("/config_postgre.properties", DumpDataSource.SupportedDatabases.POSTGRESQL)}});
 
     }
 
     public ChangesEnumeratorTest(TestDatabase db) {
         this.db = db;
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         savedConflictStrategy = Config.getInstance().getGlobalConflictStrategy();
         savedSyncDirection = Config.getInstance().getGlobalSyncDirection();
     }
 
     @Before
     public void setUp() throws IOException, SQLException {
         db.init();
 
         db.dropTablesOnServer(tableNames);
         db.dropTablesOnClient(tableNames);
 
         db.executeQueriesOnClient(createQueries);
         db.executeQueriesOnServer(createQueries);
 
         Config.getInstance().setGlobalConflictStrategy(savedConflictStrategy);
         Config.getInstance().setGlobalSyncDirection(savedSyncDirection);
     }
 
     protected SyncData getChangesGlobalOnClient(ConflictStrategy strategy, SyncDirection direction) throws
         SyncException, ContextException, SQLException, DatabaseAdapterException {
 
         Config configInstance = Config.getInstance();
         configInstance.setGlobalConflictStrategy(strategy);
         configInstance.setGlobalSyncDirection(direction);
 
         return getChangesWithStrategies(new TableSyncStrategies(), direction, CLIENT);
     }
 
     protected SyncData getChangesGlobalOnServer(ConflictStrategy strategy, SyncDirection direction) throws
         SyncException, ContextException, SQLException, DatabaseAdapterException {
 
         Config configInstance = Config.getInstance();
         configInstance.setGlobalConflictStrategy(strategy);
         configInstance.setGlobalSyncDirection(direction);
 
         return getChangesWithStrategies(new TableSyncStrategies(), direction, SERVER);
     }
 
     protected SyncData getChangesPerTableOnClient(ConflictStrategy strategy, SyncDirection direction) throws
         SyncException, ContextException, SQLException, DatabaseAdapterException {
 
         TableSyncStrategies strategies = new TableSyncStrategies();
         TableSyncStrategy tablsSyncStrategy = new TableSyncStrategy(direction, strategy);
         strategies.addSyncStrategyForTable("categories", tablsSyncStrategy);
 
         return getChangesWithStrategies(strategies, direction, CLIENT);
     }
 
     protected SyncData getChangesPerTableOnServer(ConflictStrategy strategy, SyncDirection direction) throws
         SyncException, ContextException, SQLException, DatabaseAdapterException {
 
         TableSyncStrategies strategies = new TableSyncStrategies();
         TableSyncStrategy tablsSyncStrategy = new TableSyncStrategy(direction, strategy);
         strategies.addSyncStrategyForTable("categories", tablsSyncStrategy);
 
         return getChangesWithStrategies(strategies, direction, SERVER);
     }
 
     private SyncData getChangesWithStrategies(TableSyncStrategies strategies, SyncDirection direction,
         DatabaseAdapterFactory.AdapterPurpose side) throws SyncException, ContextException, SQLException,
         DatabaseAdapterException {
 
         IDatabaseAdapter adapter = null;
         try {
             adapter = DatabaseAdapterFactory.newInstance(side);
 
             if (side.equals(CLIENT)) {
                 ClientChangesEnumerator clientChangesEnumerator = new ClientChangesEnumerator(adapter, strategies);
                 return clientChangesEnumerator.getChanges();
             } else {
                 ServerChangesEnumerator serverChangesEnumerator = new ServerChangesEnumerator(adapter, strategies);
                 return serverChangesEnumerator.getChanges(1);
             }
 
         } finally {
             if (adapter != null) {
                 if (adapter.getConnection() != null) {
                     adapter.getConnection().close();
                 }
             }
         }
     }
 
     /**
      * Closes server and client connection.
      */
     @After
     public void tearDownClass() throws SQLException {
         db.closeConnections();
     }
 }
