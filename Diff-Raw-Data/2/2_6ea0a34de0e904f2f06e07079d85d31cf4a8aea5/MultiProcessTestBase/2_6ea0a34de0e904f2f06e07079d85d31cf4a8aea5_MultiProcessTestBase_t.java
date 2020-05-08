 package org.h2o.test.fixture;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeoutException;
 
 import org.h2.engine.Constants;
 import org.h2.tools.DeleteDbFiles;
 import org.h2o.H2O;
 import org.h2o.db.id.DatabaseID;
 import org.h2o.db.id.DatabaseURL;
 import org.h2o.db.manager.PersistentSystemTable;
 import org.h2o.db.manager.recovery.LocatorException;
 import org.h2o.db.remote.ChordRemote;
 import org.h2o.locator.client.H2OLocatorInterface;
 import org.h2o.locator.server.LocatorServer;
 import org.h2o.run.AllTests;
 import org.h2o.util.H2OPropertiesWrapper;
 import org.h2o.util.exceptions.StartupException;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 
 import uk.ac.standrews.cs.nds.madface.HostDescriptor;
 import uk.ac.standrews.cs.nds.madface.JavaProcessDescriptor;
 import uk.ac.standrews.cs.nds.madface.PlatformDescriptor;
 import uk.ac.standrews.cs.nds.madface.exceptions.UnknownPlatformException;
 import uk.ac.standrews.cs.nds.madface.exceptions.UnsupportedPlatformException;
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 
 import com.mindbright.ssh2.SSH2Exception;
 
 public class MultiProcessTestBase extends TestBase {
 
     private static final String BASEDIR = "db_data/multiprocesstests/";
 
     private static final String DATABASE_NAME = "testDB";
 
     private LocatorServer ls;
 
     protected static String[] dbs = {"one", "two", "three"};
 
     protected String[] fullDbName = null;
 
     Map<String, Process> processes;
 
     protected Connection[] connections;
 
     /**
      * Whether the System Table state has been replicated yet.
      */
     static boolean isReplicated = false;
 
     @BeforeClass
     public static void initialSetUp() {
 
         Diagnostic.setLevel(DiagnosticLevel.FULL);
         Constants.IS_TEST = true;
         Constants.IS_NON_SM_TEST = false;
 
         setReplicated(false);
         deleteDatabaseData();
         ChordRemote.setCurrentPort(40000);
 
     }
 
     public static synchronized void setReplicated(final boolean b) {
 
         isReplicated = b;
     }
 
     public static synchronized boolean isReplicated() {
 
         return isReplicated;
     }
 
     /**
      * Delete all of the database files created in these tests
      */
     private static void deleteDatabaseData() {
 
         try {
             for (final String db : dbs) {
                 DeleteDbFiles.execute(BASEDIR, db, true);
             }
         }
         catch (final SQLException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     @Before
     public void setUp() throws Exception {
 
         killExistingProcessesIfNotOnWindows();
 
         deleteDatabaseData();
 
         ls = new LocatorServer(29999, "junitLocator");
         ls.createNewLocatorFile();
 
         Constants.IS_TEAR_DOWN = false;
 
         org.h2.Driver.load();
 
         processes = new HashMap<String, Process>();
 
         fullDbName = getFullDatabaseName();
 
         for (final String location : fullDbName) {
             final H2OPropertiesWrapper properties = H2OPropertiesWrapper.getWrapper(DatabaseID.parseURL(location));
             properties.createNewFile();
             properties.setProperty("descriptor", AllTests.TEST_DESCRIPTOR_FILE);
             properties.setProperty("databaseName", "testDB");
             properties.setProperty("diagnosticLevel", "FULL");
             properties.setProperty("NUMONIC_MONITORING_ENABLED", "false");
 
             properties.saveAndClose();
         }
 
         ls = new LocatorServer(29999, "junitLocator");
         ls.createNewLocatorFile();
         ls.start();
 
         startDatabases(true);
 
         createShutdownHook();
 
         sleep(2000);
         createConnectionsToDatabases();
     }
 
     public void createShutdownHook() {
 
         final MultiProcessCloser shutdownHook = new MultiProcessCloser(processes);
         Runtime.getRuntime().addShutdownHook(shutdownHook);
     }
 
     private void killExistingProcessesIfNotOnWindows() throws IOException, UnsupportedPlatformException {
 
         final HostDescriptor host_descriptor = new HostDescriptor();
 
         if (!host_descriptor.getPlatform().getName().equals(PlatformDescriptor.NAME_WINDOWS)) {
             try {
                 host_descriptor.getProcessManager().killMatchingProcesses(StartDatabaseInstance.class.getSimpleName());
             }
             catch (final SSH2Exception e) {
                 ErrorHandling.error("unexpected exception on local host");
             }
             catch (final TimeoutException e) {
                 ErrorHandling.error("unexpected exception on local host");
             }
             catch (final InterruptedException e) {
                 ErrorHandling.error("unexpected exception on local host");
             }
             catch (final UnknownPlatformException e) {
                 ErrorHandling.error("unexpected exception on local host");
             }
         }
     }
 
     @Override
     @After
     public void tearDown() {
 
         Constants.IS_TEAR_DOWN = true;
 
         killDatabases();
 
         try {
             Thread.sleep(1000);
         }
         catch (final InterruptedException e1) {
         };
 
         deleteDatabaseData();
 
         ls.setRunning(false);
 
         while (!ls.isFinished()) {
         };
     }
 
     protected void executeUpdateOnFirstMachine(final String sql) throws SQLException {
 
         Statement s = null;
 
         try {
             s = connections[0].createStatement();
             s.executeUpdate(sql);
         }
         finally {
             s.close();
         }
     }
 
     protected void sleep(final String message, final int time) throws InterruptedException {
 
         Diagnostic.traceNoEvent(DiagnosticLevel.INIT, message.toUpperCase() + " SLEEPING FOR " + time / 1000 + " SECONDS.");
         Thread.sleep(time);
     }
 
     protected void sleep(final int time) throws InterruptedException {
 
         Diagnostic.traceNoEvent(DiagnosticLevel.INIT, ">>>>> SLEEPING FOR " + time / 1000 + " SECONDS.");
         Thread.sleep(time);
     }
 
     protected void executeUpdateOnSecondMachine(final String sql) throws SQLException {
 
         executeUpdateOnNthMachine(sql, 1);
     }
 
     protected void executeUpdateOnNthMachine(final String sql, final int machineNumber) throws SQLException {
 
         try {
             final Statement s = connections[machineNumber].createStatement();
             try {
                 s.executeUpdate(sql);
             }
             finally {
                 s.close();
             }
         }
         catch (final Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Get a set of all database instances which hold system table state
      * @throws IOException
      * @throws LocatorException
      */
     private List<String> findSystemTableInstances() throws IOException, LocatorException {
 
         final H2OPropertiesWrapper persistedInstanceInformation = H2OPropertiesWrapper.getWrapper(DatabaseID.parseURL(fullDbName[0]));
 
         persistedInstanceInformation.loadProperties();
 
         /*
          * Contact descriptor for SM locations.
          */
         final String descriptorLocation = persistedInstanceInformation.getProperty("descriptor");
 
         final H2OLocatorInterface dl = new H2OLocatorInterface(descriptorLocation);
         final List<String> locations = dl.getLocations();
 
         /*
          * Parse these locations to ensure they are of the correct form.
          */
         final List<String> parsedLocations = new LinkedList<String>();
         for (final String l : locations) {
             parsedLocations.add(DatabaseURL.parseURL(l).getName());
         }
 
         return parsedLocations;
     }
 
     protected void delayQueryCommit(final int dbName) throws IOException {
 
         final String location = fullDbName[dbName];
         final H2OPropertiesWrapper properties = H2OPropertiesWrapper.getWrapper(DatabaseID.parseURL(location));
         properties.createNewFile();
         properties.setProperty("descriptor", AllTests.TEST_DESCRIPTOR_FILE);
         properties.setProperty("databaseName", "testDB");
         properties.setProperty("DELAY_QUERY_COMMIT", "true");
         properties.saveAndClose();
     }
 
     protected String findSystemTableInstance() throws IOException, LocatorException {
 
         return findSystemTableInstances().get(0);
     }
 
     protected Connection getSystemTableConnection() throws IOException, LocatorException {
 
         for (final String instance : findSystemTableInstances()) {
             final DatabaseURL dbURL = DatabaseURL.parseURL(instance);
             for (final Connection connection : connections) {
                 String connectionURL;
                 try {
                     connectionURL = connection.getMetaData().getURL();
                     if (connectionURL.equals(dbURL.getURL())) { return connection; }
                 }
                 catch (final SQLException e) {
                     e.printStackTrace();
                 }
             }
         }
 
         return null; // none found.
     }
 
     /**
      * Get a set of all database instances which don't hold System Table state.
      * @throws LocatorException
      * @throws IOException
      */
     protected List<String> findNonSystemTableInstances() throws IOException, LocatorException {
 
         final List<String> systemTableInstances = findSystemTableInstances();
 
         final List<String> nonSystemTableInstances = new LinkedList<String>();
 
         for (final String instance : dbs) {
 
             if (!systemTableInstances.contains(instance)) {
                 nonSystemTableInstances.add(instance);
             }
         }
 
         return nonSystemTableInstances;
     }
 
     /**
      * Query the System Table's persisted state (specifically the H2O_TABLE table) and check that there are the correct number of entries.
      *
      * @param connection
      *            Connection to execute the query on.
      * @param expectedEntries
      *            Number of entries expected in the table.
      * @throws SQLException
      */
     protected void assertMetaDataExists(final Connection connection, final int expectedEntries) throws SQLException {
 
         String tableName = "H2O.H2O_TABLE"; // default value.
         tableName = getTableMetaTableName();
 
         /*
          * Query database.
          */
         final Statement s = connection.createStatement();
         final ResultSet rs = s.executeQuery("SELECT * FROM " + tableName);
 
         int actualEntries = 0;
         while (rs.next()) {
             actualEntries++;
         }
 
         assertEquals(expectedEntries, actualEntries);
 
         rs.close();
         s.close();
     }
 
     /**
      * Query the System Table's persisted state (specifically the H2O.H2O_TABLEMANAGER_STATE table) and check that there are the correct
      * number of entries.
      *
      * @param connection
      *            Connection to execute the query on.
      * @param expectedEntries
      *            Number of entries expected in the table.
      * @throws SQLException
      */
     protected void assertTableManagerMetaDataExists(final Connection connection, final int expectedEntries) throws SQLException {
 
         final String tableName = "H2O.H2O_TABLEMANAGER_STATE";
 
         /*
          * Query database.
          */
         final Statement s = connection.createStatement();
         final ResultSet rs = s.executeQuery("SELECT * FROM " + tableName);
 
         int actualEntries = 0;
         while (rs.next()) {
             actualEntries++;
         }
 
         assertEquals(expectedEntries, actualEntries);
 
         rs.close();
         s.close();
     }
 
     /**
      * Get the name of the H2O meta table holding table information in the System Table. Uses reflection to access this value.
      *
      * @return This value will be something like 'H2O.H2O_TABLE', or null if the method couldn't find the value using reflection.
      */
     private String getTableMetaTableName() {
 
         String tableName = null;
 
         try {
             final Field field = PersistentSystemTable.class.getDeclaredField("TABLES");
             field.setAccessible(true);
             tableName = (String) field.get(String.class);
         }
         catch (final Exception e) {
         }
 
         return tableName;
     }
 
     protected void assertTestTableExists(final int expectedEntries, final int databaseNumber) throws SQLException {
 
         assertTestTableExists(connections[databaseNumber], expectedEntries, true);
     }
 
     /**
      * Select all entries from the test table. Checks that the number of entries in the table matches the number of entries expected.
      * Matches the contents of the first two entries as well.
      *
      * @param expectedEntries
      *            The number of entries that should be in the test table.
      * @param localOnly
      * @return true if the connection was active. false if the connection wasn't open.
      * @throws SQLException
      */
     protected void assertTestTableExists(final Connection connnection, final int expectedEntries, final boolean localOnly) throws SQLException {
 
         Statement s = null;
         ResultSet rs = null;
 
         // Query database.
 
         assertFalse(connnection == null || connnection.isClosed());
 
         try {
             s = connnection.createStatement();
             if (localOnly) {
                 rs = s.executeQuery("SELECT LOCAL ONLY * FROM " + "PUBLIC.TEST" + ";");
             }
             else {
                 rs = s.executeQuery("SELECT * FROM " + "PUBLIC.TEST" + ";");
             }
 
             int actualEntries = 0;
             while (rs.next()) {
 
                 if (actualEntries == 0) {
                     assertEquals(1, rs.getInt(1));
                     assertEquals("Hello", rs.getString(2));
                 }
                 else if (actualEntries == 1) {
                     assertEquals(2, rs.getInt(1));
                     assertEquals("World", rs.getString(2));
                 }
 
                 actualEntries++;
             }
             assertEquals(expectedEntries, actualEntries);
         }
         finally {
             if (rs != null) {
                 rs.close();
             }
             if (s != null) {
                 s.close();
             }
         }
     }
 
     protected void assertTestTableExistsLocally(final Connection connnection, final int expectedEntries) throws SQLException {
 
         assertTestTableExists(connnection, expectedEntries, true);
     }
 
     protected void assertTest2TableExists(final Connection connnection, final int expectedEntries) throws SQLException {
 
         Statement s = null;
         ResultSet rs = null;
 
         /*
          * Query database.
          */
 
         assertFalse(connnection == null || connnection.isClosed());
 
         try {
             s = connnection.createStatement();
             rs = s.executeQuery("SELECT * FROM " + "PUBLIC.TEST2" + ";");
 
             int actualEntries = 0;
             while (rs.next()) {
 
                 if (actualEntries == 0) {
                     assertEquals(4, rs.getInt(1));
                     assertEquals("Meh", rs.getString(2));
                 }
                 else if (actualEntries == 1) {
                     assertEquals(5, rs.getInt(1));
                     assertEquals("Heh", rs.getString(2));
                 }
 
                 actualEntries++;
             }
             assertEquals(expectedEntries, actualEntries);
         }
         finally {
             if (rs != null) {
                 rs.close();
             }
             if (s != null) {
                 s.close();
             }
         }
     }
 
     protected void assertTest3TableExists(final Connection connnection, final int expectedEntries) throws SQLException {
 
         Statement s = null;
         ResultSet rs = null;
 
         /*
          * Query database.
          */
 
         assertFalse(connnection == null || connnection.isClosed());
 
         try {
             s = connnection.createStatement();
             rs = s.executeQuery("SELECT * FROM " + "PUBLIC.TEST3" + ";");
 
             int actualEntries = 0;
             while (rs.next()) {
 
                 if (actualEntries == 0) {
                     assertEquals(4, rs.getInt(1));
                     assertEquals("Clouds", rs.getString(2));
                 }
                 else if (actualEntries == 1) {
                     assertEquals(5, rs.getInt(1));
                     assertEquals("Rainbows", rs.getString(2));
                 }
 
                 actualEntries++;
             }
             assertEquals(expectedEntries, actualEntries);
         }
         finally {
             if (rs != null) {
                 rs.close();
             }
             if (s != null) {
                 s.close();
             }
         }
     }
 
     public MultiProcessTestBase() {
 
         super();
     }
 
     private String[] getFullDatabaseName() {
 
         fullDbName = new String[dbs.length];
         for (int i = 0; i < dbs.length; i++) {
             final int port = 9080 + i;
             fullDbName[i] = "jdbc:h2:sm:tcp://localhost:" + port + "/db_data/multiprocesstests/" + dbs[i];
             fullDbName[i] = DatabaseURL.parseURL(fullDbName[i]).getURL();
         }
 
         return fullDbName;
     }
 
     /**
      * Starts all databases, ensuring the first database, 'one', will be the initial System Table if the parameter is true.
      *
      * @throws InterruptedException
      */
     private void startDatabases(final boolean guaranteeOneIsSystemTable) throws InterruptedException {
 
         //        final String databaseInstanceName, final String databaseDirectoryPath, final String databaseDescriptorLocation, final String databaseName
 
         for (int i = 0; i < dbs.length; i++) {
             startDatabaseAndObtainJDBCURL(i);
 
             if (guaranteeOneIsSystemTable && i == 0) {
                 sleep(1000);
             }
         }
     }
 
     private void startDatabaseAndObtainJDBCURL(final int i) {
 
         startDatabase(dbs[i], BASEDIR, AllTests.TEST_DESCRIPTOR_FILE, DATABASE_NAME);
         final int port = H2O.getDatabasesJDBCPort(BASEDIR, dbs[i], 20);
         fullDbName[i] = "jdbc:h2:sm:tcp://localhost:" + port + "/" + BASEDIR + dbs[i];
     }
 
     protected void startDatabase(final int i) {
 
         startDatabase(dbs[i], BASEDIR, AllTests.TEST_DESCRIPTOR_FILE, DATABASE_NAME);
         final int port = H2O.getDatabasesJDBCPort(BASEDIR, dbs[i], 5);
         fullDbName[i] = "jdbc:h2:tcp://localhost:" + port + "/" + BASEDIR + dbs[i];
     }
 
     /**
      * Start the specified database on the specified port.
      *
      * @param connectionString
      *            Connection string for the database being started.
      * @param port
      *            Port the database will run on.
      */
     private void startDatabase(final String databaseInstanceName, final String databaseDirectoryPath, final String databaseDescriptorLocation, final String databaseName) {
 
         final List<String> args = new LinkedList<String>();
 
         args.add("-n" + databaseName);
         args.add("-i" + databaseInstanceName);
         args.add("-f" + databaseDirectoryPath);
         args.add("-d" + databaseDescriptorLocation);
 
         try {
             processes.put(databaseInstanceName, new HostDescriptor().getProcessManager().runProcess(new JavaProcessDescriptor().classToBeInvoked(StartDatabaseInstance.class).args(args)));
         }
         catch (final Exception e) {
             ErrorHandling.error("Failed to create new database process.");
         }
     }
 
     /**
      * Create JDBC connections to every database in the LocatorDatabaseTests.dbs string array.
      * @throws StartupException 
      */
     private void createConnectionsToDatabases() throws StartupException {
 
         connections = new Connection[dbs.length];
         for (int i = 0; i < dbs.length; i++) {
             createConnectionsToDatabase(i);
         }
     }
 
     protected void createConnectionsToDatabase(final int i) throws StartupException {
 
         connections[i] = createConnectionToDatabase(fullDbName[i]);
     }
 
     /**
      * Create a connection to the database specified by the connection string parameter.
      *
      * @param connectionString
      *            Database URL of the database which this method connects to.
      * @return The newly created connection.
      */
     public static Connection createConnectionToDatabase(final String connectionString) throws StartupException {
 
         try {
 
             return DriverManager.getConnection(connectionString, PersistentSystemTable.USERNAME, PersistentSystemTable.PASSWORD);
         }
         catch (final SQLException e) {
             ErrorHandling.exceptionError(e, "Failed to connect to " + connectionString);
            throw new StartupException("Couldn't connect to " + connectionString + ": " + e.getMessage());
         }
     }
 
     /**
      * Kill all of the running database processes.
      */
     private void killDatabases() {
 
         for (final Process process : processes.values()) {
             process.destroy();
         }
 
         processes.clear(); // clear the map so the shutdown hook doesn't do anything.
     }
 
     protected void killDatabase(final String instance) {
 
         final Process p = processes.get(instance);
         if (p == null) {
             fail("Test failed to work as expected.");
         }
         else {
             p.destroy();
             Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Killed off the database process running " + instance);
         }
     }
 
     protected void killDatabase(final int i) {
 
         killDatabase(dbs[i]);
     }
 
 }
