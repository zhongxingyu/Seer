 /***************************************************************************
  *                                                                         *
  * H2O                                                                     *
  * Copyright (C) 2010 Distributed Systems Architecture Research Group      *
  * University of St Andrews, Scotland                                      *
  * http://blogs.cs.st-andrews.ac.uk/h2o/                                   *
  *                                                                         *
  * This file is part of H2O, a distributed database based on the open      *
  * source database H2 (www.h2database.com).                                *
  *                                                                         *
  * H2O is free software: you can redistribute it and/or                    *
  * modify it under the terms of the GNU General Public License as          *
  * published by the Free Software Foundation, either version 3 of the      *
  * License, or (at your option) any later version.                         *
  *                                                                         *
  * H2O is distributed in the hope that it will be useful,                  *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of          *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           *
  * GNU General Public License for more details.                            *
  *                                                                         *
  * You should have received a copy of the GNU General Public License       *
  * along with H2O.  If not, see <http://www.gnu.org/licenses/>.            *
  *                                                                         *
  ***************************************************************************/
 
 package org.h2o;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.h2.engine.Constants;
 import org.h2.engine.Database;
 import org.h2.engine.Engine;
 import org.h2.tools.DeleteDbFiles;
 import org.h2.tools.Server;
 import org.h2.util.FileUtils;
 import org.h2.util.NetUtils;
 import org.h2.util.SortedProperties;
 import org.h2o.autonomic.settings.Settings;
 import org.h2o.db.id.DatabaseID;
 import org.h2o.db.id.DatabaseURL;
 import org.h2o.db.manager.PersistentSystemTable;
 import org.h2o.test.fixture.DatabaseType;
 import org.h2o.util.H2ONetUtils;
 import org.h2o.util.H2OPropertiesWrapper;
 import org.h2o.util.exceptions.StartupException;
 
 import uk.ac.standrews.cs.nds.p2p.interfaces.IKey;
 import uk.ac.standrews.cs.nds.p2p.keys.Key;
 import uk.ac.standrews.cs.nds.p2p.util.SHA1KeyFactory;
 import uk.ac.standrews.cs.nds.util.CommandLineArgs;
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 import uk.ac.standrews.cs.nds.util.UndefinedDiagnosticLevelException;
 
 /**
  * This class starts an instance of an H2O database. It can be run from the command line (see the main method for applicable arguments), or
  * programmatically. The instance can start in standalone mode, or as part of a more customisable set-up:
  * <ul>
  * <li>Standalone start-up. A single H2O instance will be started with its own locator server. This requires minimum options at
  * initialisation. This option restricts the H2O locator server to the same process as the H2O instance, and is not recommended for
  * multi-machine database set-ups.</li>
  * <li>Custom start-up. An H2O instance will be started and will connect to the database system via a database descriptor file. This file
  * should already exist, and should specify the location of an H2O Locator server. The locator server should already be running at the
  * address specified. For information on how to create a descriptor file and naming server see the {@link H2OLocator} class.</li>
  * </ul>
  * <p>
  * If the H2O web interface is required please use one of the constructors that requires a web port as a parameter. If this interface is not
  * required, use another constructor.
  * 
  * @author Angus Macdonald (angus AT cs.st-andrews.ac.uk)
  */
 public class H2O {
 
     public static final String DEFAULT_DATABASE_DIRECTORY_PATH = "db_files";
     public static final String DEFAULT_DATABASE_NAME = "database";
     public static final int DEFAULT_TCP_PORT = 9090;
 
     private static final DiagnosticLevel DEFAULT_DIAGNOSTIC_LEVEL = DiagnosticLevel.FINAL;
 
     private String databaseSystemName;
     private int tcpPort = DEFAULT_TCP_PORT;
     private int webPort;
 
     private String databaseDescriptorLocation;
     private String databaseBaseDirectoryPath;
     private DiagnosticLevel diagnosticLevel;
 
     private Connection connection;
     private H2OLocator locator;
     private Server server;
     private DatabaseType databaseType;
 
     private String databaseInstanceIdentifier;
 
     private DatabaseID databaseID;
     private String sysOutLocation = null;
     private String sysErrLocation = null;
 
     // -------------------------------------------------------------------------------------------------------
 
     /**
      * Starts a H2O database instance.
      * 
      * @param args
      *            <ul>
      *            <li><em>-i<name></em>. The name of the database instance (i.e. the name of the database instance that is being started, NOT of the whole database system).</li>
      *            <li><em>-n<name></em>. The name of the database system (i.e. the name of the database in the descriptor file, the global system).</li>
      *            <li><em>-I<port></em>. The unique ID of this database. If none is specified a new database will be created with a new unique ID.</li>
      *            <li><em>-w<port></em>. Optional. Specifies that a web port should be opened and the web interface should be started.</li>
      *            <li><em>-d<descriptor></em>. Optional. Specifies the URL or local file path of the database descriptor file. If not specified the database will create a new descriptor file in the database directory.</li>
      *            <li><em>-f<directory></em>. Optional. Specifies the directory containing the persistent database state. The default is the current working directory.</li>
      *            <li><em>-D<level></em>. Optional. Specifies a diagnostic level from 0 (most detailed) to 6 (least detailed).</li>
      *            <li><em>-M</em>. Optional. Specifies an in-memory database. If specified, the -p, -w and -f flags are ignored.</li>
      *            <li><em>-p</em>. Optional. The TCP port to use. If this port isn't free another port will be used instead. If '0' is specified, the default port, 9090, will be used.</li>
      *            <li><em>-o</em>. Optional. Specifies where System.out messages should be redirected, if they should be redirected at all.</li>
      *            <li><em>-e</em>. Optional. Specifies where System.err messages should be redirected, if they should be redirected at all.</li>
      *            </ul>
      *            </p>
      *            <p>
      *            <em>Example: java H2O -nMyFirstDatabase -p9999 -d'config\MyFirstDatabase.h2od'</em>. This creates a new
      *            database instance for the database called <em>MyFirstDatabase</em> on port 9999, and initializes by connecting to the
      *            locator files specified in the file <em>'config\MyFirstDatabase.h2od'</em>.
      *            
      * @throws StartupException if an error occurs while parsing the command line arguments
      * @throws IOException if the server properties cannot be written
      * @throws SQLException if the server properties cannot be opened
      */
     public static void main(final String[] args) throws StartupException, IOException, SQLException {
 
         final H2O db = new H2O(args);
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Starting H2O Server Instance.");
 
         db.startDatabase();
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     /**
      * Starts a new H2O instance using the specified descriptor file to find an existing, running, locator server. This option does not start
      * H2O's web interface.
      * 
      * @param databaseName the name of the database
      * @param tcpPort the port for this database's TCP server
      * @param databaseDirectoryPath the directory in which database files are stored
      * @param databaseDescriptorLocation the location (file path or URL) of the database descriptor file
      */
     public H2O(final String databaseName, final String databaseInstanceIdentifier, final String databaseDirectoryPath, final String databaseDescriptorLocation, final DiagnosticLevel diagnosticLevel, final String sysOutLocation, final String sysErrLocation, final int tcpPort) {
 
         init(databaseName, databaseInstanceIdentifier, 0, databaseDirectoryPath, databaseDescriptorLocation, diagnosticLevel, sysOutLocation, sysErrLocation, tcpPort);
     }
 
     /**
      * Starts a local H2O instance with a running TCP server <strong>and web interface</strong>. This will automatically start a local
      * locator file, and doesn't need a descriptor file to run. A descriptor file will be created within the database directory.
      * 
      * @param databaseName the name of the database
      * @param tcpPort the port for this database's TCP server
      * @param webPort the port for this database's web interface
      * @param databaseDirectoryPath the directory in which database files are stored
      */
     public H2O(final String databaseName, final String databaseInstanceIdentifier, final int webPort, final int tcpPort, final String databaseDirectoryPath, final DiagnosticLevel diagnosticLevel, final String sysOutLocation, final String sysErrLocation) {
 
         init(databaseName, databaseInstanceIdentifier, webPort, databaseDirectoryPath, null, diagnosticLevel, sysOutLocation, sysErrLocation, tcpPort);
     }
 
     public H2O(final String[] args) throws StartupException {
 
         try {
             init(args);
         }
         catch (final UndefinedDiagnosticLevelException e) {
             throw new StartupException("invalid diagnostic level");
         }
     }
 
     /**
      * Starts an in-memory database.
      * 
      * @param databaseName
      * @param databaseDescriptorLocation
      * @param diagnosticLevel
      */
     public H2O(final String databaseName, final String databaseBaseDirectoryPath, final String databaseDescriptorLocation, final DiagnosticLevel diagnosticLevel, final String databaseInstanceIdentifier) {
 
         init(databaseName, databaseInstanceIdentifier, databaseDescriptorLocation, diagnosticLevel);
     }
 
     public H2O(final String databaseName, final String databaseInstanceIdentifier, final int webPort, final int tcpPort, final String databaseDirectoryPath, final DiagnosticLevel diagnosticLevel) {
 
         this(databaseName, databaseInstanceIdentifier, webPort, tcpPort, databaseDirectoryPath, diagnosticLevel, null, null);
     }
 
     public H2O(final String databaseName, final String databaseInstanceIdentifier, final String databaseDirectoryPath, final String databaseDescriptorLocation, final DiagnosticLevel diagnosticLevel, final int tcpPort) {
 
         this(databaseName, databaseInstanceIdentifier, databaseDirectoryPath, databaseDescriptorLocation, diagnosticLevel, null, null, tcpPort);
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     /**
      * Start a new H2O instance using the specified descriptor file to find an existing, running, locator server. This also starts H2O's web interface.
      * 
      * @param databaseName the name of the database
      * @param tcpPort the port for this database's TCP server
      * @param webPort the port for this database's web interface
      * @param databaseBaseDirectoryPath the directory in which database files are stored
      * @param databaseDescriptorLocation the location (file path or URL) of the database descriptor file
      * @param sysErrLocation Location where System.err messages should be redirected.
      * @param sysOutLocation Location where System.out messages should be redirected.
      * @param tcpPort2 
      */
     private void init(final String databaseSystemName, final String databaseInstanceIdentifier, final int webPort, final String databaseBaseDirectoryPath, final String databaseDescriptorLocation, final DiagnosticLevel diagnosticLevel, final String sysOutLocation, final String sysErrLocation,
                     final int tcpPort) {
 
         this.databaseSystemName = databaseSystemName;
         this.databaseInstanceIdentifier = databaseInstanceIdentifier;
         this.webPort = webPort;
         if (tcpPort <= 0) {
             this.tcpPort = DEFAULT_TCP_PORT;
         }
         else {
             this.tcpPort = tcpPort;
         }
         this.databaseBaseDirectoryPath = databaseBaseDirectoryPath;
         this.databaseDescriptorLocation = databaseDescriptorLocation;
         this.diagnosticLevel = diagnosticLevel;
 
         this.sysOutLocation = sysOutLocation;
         this.sysErrLocation = sysErrLocation;
 
         databaseType = DatabaseType.DISK;
 
         Diagnostic.setLevel(diagnosticLevel);
     }
 
     private void init(final String databaseSystemName, final String databaseInstanceIdentifier, final String databaseDescriptorLocation, final DiagnosticLevel diagnosticLevel) {
 
         this.databaseSystemName = databaseSystemName;
         this.databaseInstanceIdentifier = databaseInstanceIdentifier;
 
         this.databaseDescriptorLocation = databaseDescriptorLocation;
         this.diagnosticLevel = diagnosticLevel;
 
         databaseType = DatabaseType.MEMORY;
 
         Diagnostic.setLevel(diagnosticLevel);
     }
 
     private void init(final String[] args) throws StartupException, UndefinedDiagnosticLevelException {
 
         final Map<String, String> arguments = CommandLineArgs.parseCommandLineArgs(args);
 
        if (arguments.size() == 0 || arguments.size() == 1 && arguments.containsKey("-p")) {
             arguments.put("-n", "test");
             arguments.put("-i", "testDB");
             arguments.put("-w", "9898");
         }
 
         final String databaseSystemName = processDatabaseSystemName(arguments.get("-n"));
         final String databaseInstanceIdentifier = processDatabaseInstanceName(arguments.get("-i"));
         final String databaseDirectoryPath = processDatabaseDirectoryPath(arguments.get("-f"));
 
         final String databaseDescriptorLocation = processDatabaseDescriptorLocation(arguments.get("-d"));
         final int webPort = processPort(arguments.get("-w"));
         int tcpPort = DEFAULT_TCP_PORT;
 
         if (arguments.containsKey("-p")) {
             tcpPort = processPort(arguments.get("-p"));
         }
 
         final DiagnosticLevel diagnosticLevel = DiagnosticLevel.getDiagnosticLevelFromCommandLineArg(arguments.get("-D"), DEFAULT_DIAGNOSTIC_LEVEL);
 
         final String sysOutLocation = removeQuotes(arguments.get("-o"));
         final String sysErrLocation = removeQuotes(arguments.get("-e"));
 
         final DatabaseType databaseType = processDatabaseType(arguments.get("-M"));
 
         switch (databaseType) {
             case MEMORY: {
 
                 init(databaseSystemName, databaseInstanceIdentifier, databaseDescriptorLocation, diagnosticLevel);
                 break;
             }
             case DISK: {
 
                 init(databaseSystemName, databaseInstanceIdentifier, webPort, databaseDirectoryPath, databaseDescriptorLocation, diagnosticLevel, sysOutLocation, sysErrLocation, tcpPort);
                 break;
             }
             default: {
                 ErrorHandling.hardError("unexpected database type");
             }
         }
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     /**
      * Starts up an H2O server and initializes the database.
      * 
      * @throws IOException if the server properties cannot be written
      * @throws SQLException if the server properties cannot be opened
      */
     public void startDatabase() throws SQLException, IOException {
 
         if (databaseDescriptorLocation == null) {
 
             // A new locator server should be started.
             final int locatorPort = tcpPort + 1;
 
             locator = new H2OLocator(databaseSystemName, locatorPort, true, databaseBaseDirectoryPath);
 
             databaseDescriptorLocation = locator.start();
         }
 
         final DatabaseID databaseID = generateDatabaseIDandURL();
 
         startServer(databaseID);
         initializeDatabase(databaseID, sysOutLocation, sysErrLocation);
     }
 
     /**
      * Shuts down the H2O server.
      * @throws SQLException if an error occurs while shutting down the H2O server
      */
     public void shutdown() throws SQLException {
 
         if (connection != null) {
             connection.close();
         }
 
         final Collection<Database> dbs = Engine.getInstance().getAllDatabases();
 
         for (final Database db : dbs) {
 
             if (db.getShortName().equalsIgnoreCase(getDatabaseName()) || db.getShortName().equalsIgnoreCase(Constants.MANAGEMENT_DB_PREFIX + tcpPort)) {
                 db.close(true);
                 db.shutdownImmediately();
             }
         }
 
         if (locator != null) {
             locator.shutdown();
         }
 
         shutdownServer();
     }
 
     private String getDatabaseName() {
 
         return databaseID.getID();
     }
 
     /**
      * Deletes the persistent database state.
      * @throws SQLException if the state cannot be deleted
      */
     public void deletePersistentState() throws SQLException {
 
         DeleteDbFiles.execute(databaseBaseDirectoryPath, getDatabaseName(), false);
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     private String processDatabaseSystemName(final String arg) {
 
         return arg == null ? DEFAULT_DATABASE_NAME : arg;
     }
 
     private String processDatabaseInstanceName(final String arg) {
 
         return arg == null ? generateNewDatabaseID() : arg;
     }
 
     private String processDatabaseDirectoryPath(final String arg) {
 
         return arg == null ? DEFAULT_DATABASE_DIRECTORY_PATH : removeQuotes(arg);
     }
 
     private String processDatabaseDescriptorLocation(final String arg) {
 
         return arg == null ? null : removeQuotes(arg);
     }
 
     private int processPort(final String arg) throws StartupException {
 
         try {
             return arg == null ? 0 : Integer.parseInt(arg);
         }
         catch (final NumberFormatException e) {
             throw new StartupException("Invalid port: " + arg);
         }
     }
 
     private DatabaseType processDatabaseType(final String arg) {
 
         return arg == null ? DatabaseType.DISK : DatabaseType.MEMORY;
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     /**
      * Call the H2O server class with the required parameters to initialize the TCP server.
      * 
      * @param databaseID the database URL
      * @throws IOException if the server properties cannot be written
      * @throws SQLException if the server properties cannot be opened
      */
     private void startServer(final DatabaseID databaseID) throws SQLException, IOException {
 
         final List<String> h2oArgs = new LinkedList<String>(); // Arguments to be passed to the H2 server.
         h2oArgs.add("-tcp");
 
         h2oArgs.add("-tcpPort");
         h2oArgs.add(String.valueOf(databaseID.getPort()));
 
         h2oArgs.add("-tcpAllowOthers"); // allow remote connections.
         h2oArgs.add("-webAllowOthers");
 
         // Web Interface.
 
         webPort = H2ONetUtils.getInactiveTCPPort(webPort);
 
         if (webPort != 0) {
             h2oArgs.add("-web");
             h2oArgs.add("-webPort");
             h2oArgs.add(String.valueOf(webPort));
             h2oArgs.add("-browser");
         }
 
         // Set URL to be displayed in browser.
         setUpWebLink(databaseID);
 
         server = new Server();
         server.run(h2oArgs.toArray(new String[0]), System.out);
     }
 
     private void shutdownServer() {
 
         server.shutdown();
     }
 
     /**
      * Connects to the server and initializes the database at a particular location on disk.
      * 
      * @param databaseID
      * @param sysOutLocation 
      * @param sysErrLocation 
      * @throws SQLException 
      * @throws IOException 
      */
     private void initializeDatabase(final DatabaseID databaseID, final String sysOutLocation, final String sysErrLocation) throws SQLException, IOException {
 
         initializeDatabaseProperties(databaseID, diagnosticLevel, databaseDescriptorLocation, databaseSystemName, sysOutLocation, sysErrLocation);
 
         // Create a connection so that the database starts up, but don't do anything with it here.
         connection = DriverManager.getConnection(databaseID.getURLandID(), PersistentSystemTable.USERNAME, PersistentSystemTable.PASSWORD);
     }
 
     public static void initializeDatabaseProperties(final DatabaseID databaseID, final DiagnosticLevel diagnosticLevel, final String databaseDescriptorLocation, final String databaseName, final String sysOutLocation, final String sysErrLocation) throws IOException {
 
         final H2OPropertiesWrapper properties = H2OPropertiesWrapper.getWrapper(databaseID);
 
         try {
             properties.loadProperties();
         }
         catch (final IOException e) {
             properties.createNewFile();
         }
 
         // Overwrite these properties regardless of whether properties file exists or not.
         properties.setProperty("diagnosticLevel", diagnosticLevel.toString());
 
         if (sysOutLocation != null) {
             properties.setProperty("sysOutLocation", sysOutLocation);
         }
 
         if (sysErrLocation != null) {
             properties.setProperty("sysErrLocation", sysErrLocation);
         }
         properties.setProperty("descriptor", databaseDescriptorLocation);
         properties.setProperty("databaseName", databaseName);
 
         properties.saveAndClose();
     }
 
     /**
      * Set the primary database URL in the browser to equal the URL of this database.
      * 
      * @param databaseID the database URL
      * @throws IOException if the server properties cannot be written
      * @throws SQLException if the server properties cannot be opened
      */
     private void setUpWebLink(final DatabaseID databaseID) throws IOException, SQLException {
 
         final Properties serverProperties = loadServerProperties();
         final List<String> servers = new LinkedList<String>();
         final String url_as_string = databaseID.getURL();
 
         for (int i = 0;; i++) {
             final String data = serverProperties.getProperty(String.valueOf(i));
             if (data == null) {
                 break;
             }
             if (!data.contains(url_as_string)) {
                 servers.add(data);
             }
 
             serverProperties.remove(String.valueOf(i));
         }
 
         int i = 0;
         for (final String server : servers) {
             serverProperties.setProperty(i + "", server);
             i++;
         }
 
         serverProperties.setProperty(i + "", "QuickStart-H2O-Database|org.h2.Driver|" + url_as_string + "|sa");
 
         final OutputStream out = FileUtils.openFileOutputStream(getPropertiesFileName(), false);
         serverProperties.store(out, Constants.SERVER_PROPERTIES_TITLE);
 
         out.close();
     }
 
     private String getPropertiesFileName() {
 
         // Store the properties in the user directory.
         return FileUtils.getFileInUserHome(Constants.SERVER_PROPERTIES_FILE);
     }
 
     private Properties loadServerProperties() {
 
         final String fileName = getPropertiesFileName();
         try {
             return SortedProperties.loadProperties(fileName);
         }
         catch (final IOException e) {
             return new Properties();
         }
     }
 
     private static String removeQuotes(String text) {
 
         if (text == null) { return null; }
 
         if (text.startsWith("'") && text.endsWith("'")) {
             text = text.substring(1, text.length() - 1);
         }
         return text;
     }
 
     private DatabaseID generateDatabaseIDandURL() {
 
         if (databaseInstanceIdentifier == null) {
             databaseInstanceIdentifier = generateNewDatabaseID();
         }
 
         final int tcpPortToUse = H2ONetUtils.getInactiveTCPPort(tcpPort);
 
         switch (databaseType) {
             case DISK: {
                 databaseID = new DatabaseID(new DatabaseURL(tcpPortToUse, databaseBaseDirectoryPath, databaseInstanceIdentifier));
                 break;
             }
             case MEMORY: {
                 databaseID = new DatabaseID(new DatabaseURL(databaseInstanceIdentifier));
                 break;
             }
             default: {
                 ErrorHandling.hardError("unknown database type");
                 return null;
             }
         }
 
         return databaseID;
     }
 
     /**
      * Generate a new unique database ID.
      * @return
      */
     private String generateNewDatabaseID() {
 
         final IKey my_key = new SHA1KeyFactory().generateKey((NetUtils.getLocalAddress() + new Date().getTime()).getBytes()); // use the time + location seed a unique id for this node.
         return my_key.toString(Key.DEFAULT_RADIX);
     }
 
     public String getURL() {
 
         return databaseID.getURL();
     }
 
     /**
      * Get the port on which a database has started up its JDBC port. This is specified in the databases properties file,
      * which is passed in as a parameter to this method.
      * @param pathToDatabase the path to the database files (i.e. the folders it is contained in - for example path/to/database, which is relative
      *  to the current working directory.
      *  @param databaseInstanceName the name of the database (e.g. databaseOne).
      * @return The port on which the databases JDBC server is running.
      */
     public static int getDatabasesJDBCPort(final String pathToDatabase, final String databaseInstanceName) {
 
         return getDatabasesJDBCPort(pathToDatabase, databaseInstanceName, 1);
     }
 
     /**
      * Get the port on which a database has started up its JDBC port. This is specified in the databases properties file,
      * which is passed in as a parameter to this method.
      * @param pathToDatabase the path to the database files (i.e. the folders it is contained in - for example path/to/database, which is relative
      *  to the current working directory.
      *  @param maxAttempts           The maximum number of attempts to get this information. If the properties file is not located
      *  @param databaseInstanceName the name of the database (e.g. databaseOne).
      * @return The port on which the databases JDBC server is running.
      */
     public static int getDatabasesJDBCPort(String pathToDatabase, final String databaseInstanceName, final int maxAttempts) {
 
         if (pathToDatabase.endsWith("/") || pathToDatabase.endsWith("\\")) {
             pathToDatabase = pathToDatabase.substring(0, pathToDatabase.length() - 1);
         }
 
         final String databasePathAndName = pathToDatabase + File.separator + databaseInstanceName;
         final String propertiesFilePath = pathToDatabase + File.separator + DatabaseURL.getPropertiesFileName(databasePathAndName) + ".properties";
 
         return getDatabasesJDBCPort(propertiesFilePath, maxAttempts);
     }
 
     /**
      * Get the port on which a database has started up its JDBC port. This is specified in the databases properties file,
      * which is passed in as a parameter to this method.
      * @param propertiesFilePath    Path to and name of the properties file for the database in question.
      * @return The port on which the databases JDBC server is running.
      */
     public static int getDatabasesJDBCPort(final String propertiesFilePath) {
 
         return getDatabasesJDBCPort(propertiesFilePath, 1);
     }
 
     /**
      * Get the port on which a database has started up its JDBC port. This is specified in the databases properties file,
      * which is passed in as a parameter to this method.
      * @param propertiesFilePath    Path to and name of the properties file for the database in question.
      * @param maxAttempts           The maximum number of attempts to get this information. If the properties file is not located
      * this method will sleep for a second then try to get the database's port again. It will do this up until a maximum number of attempts.
      * @return The port on which the databases JDBC server is running.
      */
     public static int getDatabasesJDBCPort(final String propertiesFilePath, final int maxAttempts) {
 
         final H2OPropertiesWrapper localSettings = H2OPropertiesWrapper.getWrapper(propertiesFilePath);
 
         String port = null;
 
         int attempts = 0;
         while (port == null && attempts < maxAttempts) {
 
             try {
                 localSettings.loadProperties();
 
                 port = localSettings.getProperty(Settings.JDBC_PORT);
 
                 localSettings.saveAndClose();
             }
             catch (final Exception e) {
                 attempts++;
 
                 if (attempts < maxAttempts) {
                     try {
                         System.err.println("sleeping...");
                         Thread.sleep(1000);
                     }
                     catch (final InterruptedException e1) {
                     }
                 }
             }
         }
 
         if (port == null) { throw new IllegalArgumentException("Couldn't access properties file at path '" + propertiesFilePath + "' after " + maxAttempts + " attempts."); }
 
         final int iPort = Integer.parseInt(port);
         return iPort;
     }
 }
