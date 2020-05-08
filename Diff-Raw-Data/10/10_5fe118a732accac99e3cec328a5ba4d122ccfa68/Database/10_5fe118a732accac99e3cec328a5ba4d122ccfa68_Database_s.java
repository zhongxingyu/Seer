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
 
 package org.h2.engine;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Properties;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.h2.api.DatabaseEventListener;
 import org.h2.command.dml.SetTypes;
 import org.h2.constant.ErrorCode;
 import org.h2.constant.LocationPreference;
 import org.h2.constant.SysProperties;
 import org.h2.constraint.Constraint;
 import org.h2.index.Cursor;
 import org.h2.index.Index;
 import org.h2.index.IndexType;
 import org.h2.log.LogSystem;
 import org.h2.message.Message;
 import org.h2.message.Trace;
 import org.h2.message.TraceSystem;
 import org.h2.result.Row;
 import org.h2.result.SearchRow;
 import org.h2.schema.Schema;
 import org.h2.schema.SchemaObject;
 import org.h2.schema.Sequence;
 import org.h2.schema.TriggerObject;
 import org.h2.store.DataHandler;
 import org.h2.store.DataPage;
 import org.h2.store.DiskFile;
 import org.h2.store.FileLock;
 import org.h2.store.FileStore;
 import org.h2.store.PageStore;
 import org.h2.store.RecordReader;
 import org.h2.store.Storage;
 import org.h2.store.WriterThread;
 import org.h2.store.fs.FileSystem;
 import org.h2.table.Column;
 import org.h2.table.IndexColumn;
 import org.h2.table.MetaTable;
 import org.h2.table.ReplicaSet;
 import org.h2.table.Table;
 import org.h2.table.TableData;
 import org.h2.table.TableLinkConnection;
 import org.h2.table.TableView;
 import org.h2.tools.DeleteDbFiles;
 import org.h2.tools.Server;
 import org.h2.util.BitField;
 import org.h2.util.ByteUtils;
 import org.h2.util.CacheLRU;
 import org.h2.util.ClassUtils;
 import org.h2.util.FileUtils;
 import org.h2.util.IntHashMap;
 import org.h2.util.NetUtils;
 import org.h2.util.ObjectArray;
 import org.h2.util.ObjectUtils;
 import org.h2.util.SmallLRUCache;
 import org.h2.util.StringUtils;
 import org.h2.util.TempFileDeleter;
 import org.h2.value.CompareMode;
 import org.h2.value.Value;
 import org.h2.value.ValueInt;
 import org.h2.value.ValueLob;
 import org.h2o.autonomic.numonic.ISystemStatus;
 import org.h2o.autonomic.numonic.NumonicReporter;
 import org.h2o.autonomic.numonic.ThresholdChecker;
 import org.h2o.autonomic.numonic.interfaces.INumonic;
 import org.h2o.autonomic.numonic.threshold.Threshold;
 import org.h2o.autonomic.settings.Settings;
 import org.h2o.autonomic.settings.TestingSettings;
 import org.h2o.db.DatabaseInstanceServer;
 import org.h2o.db.id.DatabaseID;
 import org.h2o.db.interfaces.IDatabaseInstanceRemote;
 import org.h2o.db.manager.SystemTable;
 import org.h2o.db.manager.SystemTableReference;
 import org.h2o.db.manager.SystemTableServer;
 import org.h2o.db.manager.TableManager;
 import org.h2o.db.manager.TableManagerInstanceServer;
 import org.h2o.db.manager.interfaces.ISystemTableMigratable;
 import org.h2o.db.manager.interfaces.ISystemTableReference;
 import org.h2o.db.manager.monitorthreads.MetaDataReplicationThread;
 import org.h2o.db.manager.recovery.LocatorException;
 import org.h2o.db.query.TableProxyManager;
 import org.h2o.db.query.asynchronous.AsynchronousQueryManager;
 import org.h2o.db.remote.ChordRemote;
 import org.h2o.db.remote.IChordInterface;
 import org.h2o.db.remote.IDatabaseRemote;
 import org.h2o.db.replication.MetaDataReplicaManager;
 import org.h2o.db.wrappers.DatabaseInstanceWrapper;
 import org.h2o.locator.client.H2OLocatorInterface;
 import org.h2o.util.H2ONetUtils;
 import org.h2o.util.H2OPropertiesWrapper;
 import org.h2o.util.TransactionNameGenerator;
 import org.h2o.util.exceptions.MovedException;
 import org.h2o.util.exceptions.StartupException;
 import org.h2o.viewer.H2OEventBus;
 import org.h2o.viewer.H2OEventConsumer;
 import org.h2o.viewer.gwt.client.DatabaseStates;
 import org.h2o.viewer.gwt.client.H2OEvent;
 import org.h2o.viewer.server.KeepAliveMessageThread;
 
 import uk.ac.standrews.cs.nds.events.bus.EventBus;
 import uk.ac.standrews.cs.nds.events.bus.interfaces.IEventBus;
 import uk.ac.standrews.cs.nds.rpc.RPCException;
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 import uk.ac.standrews.cs.numonic.data.ResourceType;
 
 /**
  * There is one database object per open database.
  *
  * The format of the meta data table is: id int, headPos int (for indexes), objectType int, sql varchar
  *
  * @since 2004-04-15 22:49
  */
 public class Database implements DataHandler, Observer, ISystemStatus {
 
     private static int initialPowerOffCount;
 
     private final boolean persistent;
 
     private final String databaseName;
 
     private final String databaseShortName;
 
     private final String databaseURL;
 
     private final String cipher;
 
     private final byte[] filePasswordHash;
 
     private final HashMap<String, DbObject> roles = new HashMap<String, DbObject>();
 
     private final HashMap<String, DbObject> users = new HashMap<String, DbObject>();
 
     private final HashMap<String, DbObject> settings = new HashMap<String, DbObject>();
 
     private final HashMap<String, DbObject> schemas = new HashMap<String, DbObject>();
 
     private final HashMap<String, DbObject> rights = new HashMap<String, DbObject>();
 
     private final HashMap<String, DbObject> functionAliases = new HashMap<String, DbObject>();
 
     private final HashMap<String, DbObject> userDataTypes = new HashMap<String, DbObject>();
 
     private final HashMap<String, DbObject> aggregates = new HashMap<String, DbObject>();
 
     private final HashMap<String, DbObject> comments = new HashMap<String, DbObject>();
 
     private final IntHashMap tableMap = new IntHashMap();
 
     private final HashMap<Integer, DbObject> databaseObjects = new HashMap<Integer, DbObject>();
 
     private final Set<Session> userSessions = Collections.synchronizedSet(new HashSet<Session>());
 
     private Session exclusiveSession;
 
     private final BitField objectIds = new BitField();
 
     private final Object lobSyncObject = new Object();
 
     private Schema mainSchema;
 
     private Schema infoSchema;
 
     private User systemUser;
 
     private Session systemSession;
 
     private TableData meta;
 
     private Index metaIdIndex;
 
     private FileLock lock;
 
     private LogSystem log;
 
     private WriterThread writer;
 
     private final IntHashMap storageMap = new IntHashMap();
 
     private boolean starting;
 
     private DiskFile fileData, fileIndex;
 
     private TraceSystem traceSystem;
 
     private DataPage dummy;
 
     private final int fileLockMethod;
 
     private Role publicRole;
 
     private long modificationDataId;
 
     private long modificationMetaId;
 
     private CompareMode compareMode;
 
     private String cluster = Constants.CLUSTERING_DISABLED;
 
     private boolean readOnly;
 
     private boolean noDiskSpace;
 
     private int writeDelay = Constants.DEFAULT_WRITE_DELAY;
 
     private DatabaseEventListener eventListener;
 
     private int maxMemoryRows = Constants.DEFAULT_MAX_MEMORY_ROWS;
 
     private int maxMemoryUndo = SysProperties.DEFAULT_MAX_MEMORY_UNDO;
 
     private int lockMode = SysProperties.DEFAULT_LOCK_MODE;
 
     private boolean logIndexChanges;
 
     private int logLevel = 1;
 
     private int maxLengthInplaceLob = Constants.DEFAULT_MAX_LENGTH_INPLACE_LOB;
 
     private int allowLiterals = Constants.DEFAULT_ALLOW_LITERALS;
 
     private int powerOffCount = initialPowerOffCount;
 
     private int closeDelay;
 
     private DatabaseCloser delayedCloser;
 
     private boolean recovery;
 
     private volatile boolean closing;
 
     private boolean ignoreCase;
 
     private boolean deleteFilesOnDisconnect;
 
     private String lobCompressionAlgorithm;
 
     private boolean optimizeReuseResults = true;
 
     private final String cacheType;
 
     private boolean indexSummaryValid = true;
 
     private String accessModeLog;
 
     private final String accessModeData;
 
     private boolean referentialIntegrity = true;
 
     private DatabaseCloser closeOnExit;
 
     private Mode mode = Mode.getInstance(Mode.REGULAR);
 
     private int maxOperationMemory = SysProperties.DEFAULT_MAX_OPERATION_MEMORY;
 
     private boolean lobFilesInDirectories = SysProperties.LOB_FILES_IN_DIRECTORIES;
 
     private final SmallLRUCache lobFileListCache = new SmallLRUCache(128);
 
     private final boolean autoServerMode;
 
     private Server server;
 
     private HashMap<TableLinkConnection, TableLinkConnection> linkConnections;
 
     private final TempFileDeleter tempFileDeleter = TempFileDeleter.getInstance();
 
     private PageStore pageStore;
 
     private Properties reconnectLastLock;
 
     private long reconnectCheckNext;
 
     private boolean reconnectChangePending;
 
     private final ISystemTableReference systemTableRef;
 
     private MetaDataReplicaManager metaDataReplicaManager;
 
     private MetaDataReplicationThread metaDataReplicationThread;
 
     private boolean running = false;
 
     public MetaDataReplicaManager getMetaDataReplicaManager() {
 
         return metaDataReplicaManager;
     }
 
     /**
      * Interface for this database instance to the rest of the database system.
      */
     private final ChordRemote databaseRemote;
 
     private final AsynchronousQueryManager asynchronousQueryManager;
 
     private User h2oSchemaUser;
 
     private Session h2oSession;
 
     private User h2oSystemUser;
 
     private Session h2oSystemSession;
 
     private Settings databaseSettings;
 
     private final TransactionNameGenerator transactionNameGenerator;
 
     private final Set<String> localSchema = new HashSet<String>();
 
     private H2OEventConsumer eventConsumer;
 
     private KeepAliveMessageThread keepAliveMessageThread;
 
     private TableManagerInstanceServer table_manager_instance_server;
 
     private DatabaseInstanceServer database_instance_server;
 
     private SystemTableServer system_table_server = null;
 
     private INumonic numonic = null;
 
     private boolean connected = false;
 
     public Database(final String name, final ConnectionInfo ci, final String cipher) throws SQLException {
 
         localSchema.add(Constants.H2O_SCHEMA);
         localSchema.add(Constants.SCHEMA_INFORMATION);
 
         databaseName = name;
         databaseShortName = parseDatabaseShortName();
 
         final DatabaseID localMachineLocation = DatabaseID.parseURL(ci.getOriginalURL());
 
         // Ensure testing constants are all set to false.
         TestingSettings.IS_TESTING_PRE_COMMIT_FAILURE = false;
         TestingSettings.IS_TESTING_PRE_PREPARE_FAILURE = false;
         TestingSettings.IS_TESTING_QUERY_FAILURE = false;
         TestingSettings.IS_TESTING_CREATETABLE_FAILURE = false;
 
         transactionNameGenerator = new TransactionNameGenerator(localMachineLocation);
         asynchronousQueryManager = new AsynchronousQueryManager(this);
 
         compareMode = new CompareMode(null, null, 0);
         systemTableRef = new SystemTableReference(this);
         databaseRemote = new ChordRemote(localMachineLocation, systemTableRef);
 
         persistent = ci.isPersistent();
         filePasswordHash = ci.getFilePasswordHash();
 
         if (!isManagementDB()) {
 
             if (Constants.LOG_INCOMING_UPDATES) {
                 ErrorHandling.errorNoEvent("WARNING: LOGGING OF QUERIES IS ENABLED IN THE TABLEPROXYMANAGER. This uses massive amounts of memory and should only be used when debugging.");
             }
 
             if (Constants.DO_LOCK_LOGGING) {
                 ErrorHandling.errorNoEvent("WARNING: LOGGING OF LOCK REQUESTS IS ENABLED. This uses massive amounts of memory and should only be used when debugging.");
             }
             /*
              * Get Settings for Database.
              */
             final H2OPropertiesWrapper localSettings = setUpLocalDatabaseProperties(localMachineLocation);
 
             setDiagnosticLevel(localMachineLocation);
             Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "H2O, Database '" + localMachineLocation.getURL() + "'.");
 
             try {
                 final H2OLocatorInterface locatorInterface = databaseRemote.getLocatorInterface();
 
                 databaseSettings = new Settings(localSettings, locatorInterface.getDescriptor());
             }
             catch (final LocatorException e) {
                 e.printStackTrace();
                 throw new SQLException(e.getMessage());
             }
             catch (final StartupException e) {
                 throw new SQLException(e.getMessage());
             }
 
             /*
              * Set up events.
              */
             if (databaseSettings.get("DATABASE_EVENTS_ENABLED").equals("true")) {
                 final IEventBus bus = new EventBus();
                 H2OEventBus.setBus(bus);
 
                 eventConsumer = new H2OEventConsumer(this);
                 bus.register(eventConsumer);
 
                 H2OEventBus.publish(new H2OEvent(localMachineLocation.getURL(), DatabaseStates.DATABASE_STARTUP, localMachineLocation.getURL()));
 
                 keepAliveMessageThread = new KeepAliveMessageThread(localMachineLocation.getURL());
                 keepAliveMessageThread.start();
             }
 
         }
 
         this.cipher = cipher;
         final String lockMethodName = ci.getProperty("FILE_LOCK", null);
         accessModeLog = ci.getProperty("ACCESS_MODE_LOG", "rw").toLowerCase();
         accessModeData = ci.getProperty("ACCESS_MODE_DATA", "rw").toLowerCase();
         autoServerMode = ci.getProperty("AUTO_SERVER", false);
         if ("r".equals(accessModeData)) {
             readOnly = true;
             accessModeLog = "r";
         }
         fileLockMethod = FileLock.getFileLockMethod(lockMethodName);
         if (fileLockMethod == FileLock.LOCK_SERIALIZED) {
             writeDelay = SysProperties.MIN_WRITE_DELAY;
         }
         databaseURL = ci.getURL();
         eventListener = ci.getDatabaseEventListenerObject();
         ci.removeDatabaseEventListenerObject();
         if (eventListener == null) {
             String listener = ci.removeProperty("DATABASE_EVENT_LISTENER", null);
             if (listener != null) {
                 listener = StringUtils.trim(listener, true, true, "'");
                 setEventListenerClass(listener);
             }
         }
         final String log = ci.getProperty(SetTypes.LOG, null);
         if (log != null) {
             logIndexChanges = "2".equals(log);
         }
         final String ignoreSummary = ci.getProperty("RECOVER", null);
         if (ignoreSummary != null) {
             recovery = true;
         }
 
         final boolean closeAtVmShutdown = ci.getProperty("DB_CLOSE_ON_EXIT", true);
         final int traceLevelFile = ci.getIntProperty(SetTypes.TRACE_LEVEL_FILE, TraceSystem.DEFAULT_TRACE_LEVEL_FILE);
         final int traceLevelSystemOut = ci.getIntProperty(SetTypes.TRACE_LEVEL_SYSTEM_OUT, TraceSystem.DEFAULT_TRACE_LEVEL_SYSTEM_OUT);
         cacheType = StringUtils.toUpperEnglish(ci.removeProperty("CACHE_TYPE", CacheLRU.TYPE_NAME));
         openDatabase(traceLevelFile, traceLevelSystemOut, closeAtVmShutdown, ci, localMachineLocation);
 
         if (!isManagementDB()) {
             final boolean metaDataReplicationEnabled = Boolean.parseBoolean(databaseSettings.get("METADATA_REPLICATION_ENABLED"));
 
             if (!Constants.IS_NON_SM_TEST && metaDataReplicationEnabled) {
                 metaDataReplicationThread.start();
             }
             Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Started database at " + getID());
 
             try {
                 final String fileSystemName = getFileSystemName(databaseName);
                 numonic = new NumonicReporter(databaseSettings.get("NUMONIC_MONITORING_FILE_LOCATION"), fileSystemName, getID(), systemTableRef, this, databaseSettings.get("NUMONIC_THRESHOLDS_FILE_LOCATION"));
 
                 if (Boolean.parseBoolean(databaseSettings.get("NUMONIC_MONITORING_ENABLED"))) {
                     numonic.start();
                 }
             }
             catch (final IOException e) {
                 ErrorHandling.exceptionError(e, "Failed to start numonic reporter for H2O.");
             }
         }
 
         running = true;
         connected = true;
     }
 
     /**
      * Get the name of the filesystem that this database is storing data on (e.g. "C:\" or "/");
      * @param databaseName
      * @return
      */
     private String getFileSystemName(final String databaseName) {
 
         if (databaseName.indexOf(File.separator) >= 0) {
             final String nameBeforeSlash = databaseName.substring(0, databaseName.indexOf(File.separator));
             return nameBeforeSlash + File.separator;
         }
         else {
             return null;
         }
     }
 
     private H2OPropertiesWrapper setUpLocalDatabaseProperties(final DatabaseID localMachineLocation) {
 
         final H2OPropertiesWrapper localSettings = H2OPropertiesWrapper.getWrapper(localMachineLocation);
         try {
             localSettings.loadProperties();
             localSettings.setProperty(Settings.JDBC_PORT, localMachineLocation.getPort() + "");
             localSettings.saveAndClose();
             localSettings.loadProperties();
         }
         catch (final IOException e) {
             try {
                 localSettings.createNewFile();
                 localSettings.loadProperties();
                 localSettings.setProperty(Settings.JDBC_PORT, localMachineLocation.getPort() + "");
                 localSettings.saveAndClose();
                 localSettings.loadProperties();
             }
             catch (final IOException e1) {
                 ErrorHandling.exceptionError(e1, "Failed to create properties file for database.");
             }
         }
         return localSettings;
     }
 
     private void openDatabase(final int traceLevelFile, final int traceLevelSystemOut, final boolean closeAtVmShutdown, final ConnectionInfo ci, final DatabaseID localMachineLocation) throws SQLException {
 
         try {
             open(traceLevelFile, traceLevelSystemOut, ci, localMachineLocation);
             if (closeAtVmShutdown) {
                 closeOnExit = new DatabaseCloser(this, 0, true);
                 try {
                     Runtime.getRuntime().addShutdownHook(closeOnExit);
                 }
                 catch (final IllegalStateException e) {
                     // shutdown in progress - just don't register the handler
                     // (maybe an application wants to write something into a
                     // database at shutdown time)
                 }
                 catch (final SecurityException e) {
                     // applets may not do that - ignore
                 }
             }
         }
         catch (final Throwable e) {
 
             ErrorHandling.exceptionError(e, "Error starting database. It will now shut down.");
 
             if (traceSystem != null) {
                 if (e instanceof SQLException) {
                     final SQLException e2 = (SQLException) e;
                     if (e2.getErrorCode() != ErrorCode.DATABASE_ALREADY_OPEN_1) {
                         // only write if the database is not already in use
                         traceSystem.getTrace(Trace.DATABASE).error("opening " + databaseName, e);
                     }
                 }
                 traceSystem.close();
             }
             closeOpenFilesAndUnlock();
             throw Message.convert(e);
         }
     }
 
     public static void setInitialPowerOffCount(final int count) {
 
         initialPowerOffCount = count;
     }
 
     public void setPowerOffCount(final int count) {
 
         if (powerOffCount == -1) { return; }
         powerOffCount = count;
     }
 
     /**
      * Check if two values are equal with the current comparison mode.
      *
      * @param a
      *            the first value
      * @param b
      *            the second value
      * @return true if both objects are equal
      */
     public boolean areEqual(final Value a, final Value b) throws SQLException {
 
         return a.compareTo(b, compareMode) == 0;
     }
 
     /**
      * Compare two values with the current comparison mode. The values may not be of the same type.
      *
      * @param a
      *            the first value
      * @param b
      *            the second value
      * @return 0 if both values are equal, -1 if the first value is smaller, and 1 otherwise
      */
     public int compare(final Value a, final Value b) throws SQLException {
 
         return a.compareTo(b, compareMode);
     }
 
     /**
      * Compare two values with the current comparison mode. The values must be of the same type.
      *
      * @param a
      *            the first value
      * @param b
      *            the second value
      * @return 0 if both values are equal, -1 if the first value is smaller, and 1 otherwise
      */
     @Override
     public int compareTypeSave(final Value a, final Value b) throws SQLException {
 
         return a.compareTypeSave(b, compareMode);
     }
 
     public long getModificationDataId() {
 
         return modificationDataId;
     }
 
     private void reconnectModified(final boolean pending) {
 
         if (readOnly || pending == reconnectChangePending || lock == null) { return; }
         try {
             if (pending) {
                 getTrace().debug("wait before writing");
                 Thread.sleep((int) (SysProperties.RECONNECT_CHECK_DELAY * 1.1));
             }
             lock.setProperty("modificationDataId", Long.toString(modificationDataId));
             lock.setProperty("modificationMetaId", Long.toString(modificationMetaId));
             lock.setProperty("changePending", pending ? "true" : null);
             lock.save();
             reconnectLastLock = lock.load();
             reconnectChangePending = pending;
         }
         catch (final Exception e) {
             getTrace().error("pending:" + pending, e);
         }
     }
 
     public long getNextModificationDataId() {
 
         return ++modificationDataId;
     }
 
     public long getModificationMetaId() {
 
         return modificationMetaId;
     }
 
     public long getNextModificationMetaId() {
 
         // if the meta data has been modified, the data is modified as well
         // (because MetaTable returns modificationDataId)
         modificationDataId++;
         return modificationMetaId++;
     }
 
     public int getPowerOffCount() {
 
         return powerOffCount;
     }
 
     @Override
     public void checkPowerOff() throws SQLException {
 
         if (powerOffCount == 0) { return; }
         if (powerOffCount > 1) {
             powerOffCount--;
             return;
         }
         if (powerOffCount != -1) {
             try {
                 powerOffCount = -1;
                 if (log != null) {
                     try {
                         stopWriter();
                         log.close();
                     }
                     catch (final SQLException e) {
                         // ignore
                     }
                     log = null;
                 }
                 if (fileData != null) {
                     try {
                         fileData.close();
                     }
                     catch (final SQLException e) {
                         // ignore
                     }
                     fileData = null;
                 }
                 if (fileIndex != null) {
                     try {
                         fileIndex.close();
                     }
                     catch (final SQLException e) {
                         // ignore
                     }
                     fileIndex = null;
                 }
                 if (pageStore != null) {
                     try {
                         pageStore.close();
                     }
                     catch (final SQLException e) {
                         // ignore
                     }
                     pageStore = null;
                 }
                 if (lock != null) {
                     stopServer();
                     if (fileLockMethod != FileLock.LOCK_SERIALIZED) {
                         // allow testing shutdown
                         lock.unlock();
                     }
                     lock = null;
                 }
             }
             catch (final Exception e) {
                 TraceSystem.traceThrowable(e);
             }
         }
         Engine.getInstance().close(databaseName);
         throw Message.getSQLException(ErrorCode.SIMULATED_POWER_OFF);
     }
 
     /**
      * Check if a database with the given name exists.
      *
      * @param name
      *            the name of the database (including path)
      * @return true if one exists
      */
     public static boolean exists(final String name) {
 
         return FileUtils.exists(name + Constants.SUFFIX_DATA_FILE);
     }
 
     /**
      * Get the trace object for the given module.
      *
      * @param module
      *            the module name
      * @return the trace object
      */
     public Trace getTrace(final String module) {
 
         return traceSystem.getTrace(module);
     }
 
     @Override
     public FileStore openFile(final String name, final String mode, final boolean mustExist) throws SQLException {
 
         if (mustExist && !FileUtils.exists(name)) { throw Message.getSQLException(ErrorCode.FILE_NOT_FOUND_1, name); }
         final FileStore store = FileStore.open(this, name, mode, cipher, filePasswordHash);
         try {
             store.init();
         }
         catch (final SQLException e) {
             store.closeSilently();
             throw e;
         }
         return store;
     }
 
     /**
      * Check if the file password hash is correct.
      *
      * @param cipher
      *            the cipher algorithm
      * @param hash
      *            the hash code
      * @return true if the cipher algorithm and the password match
      */
     public boolean validateFilePasswordHash(final String cipher, final byte[] hash) throws SQLException {
 
         if (!StringUtils.equals(cipher, this.cipher)) { return false; }
         return ByteUtils.compareSecure(hash, filePasswordHash);
     }
 
     private void openFileData() throws SQLException {
 
         fileData = new DiskFile(this, databaseName + Constants.SUFFIX_DATA_FILE, accessModeData, true, true, SysProperties.CACHE_SIZE_DEFAULT);
     }
 
     private void openFileIndex() throws SQLException {
 
         fileIndex = new DiskFile(this, databaseName + Constants.SUFFIX_INDEX_FILE, accessModeData, false, logIndexChanges, SysProperties.CACHE_SIZE_INDEX_DEFAULT);
     }
 
     public DataPage getDataPage() {
 
         return dummy;
     }
 
     private String parseDatabaseShortName() {
 
         String n = databaseName;
         if (n.endsWith(":")) {
             n = null;
         }
         if (n != null) {
             final StringTokenizer tokenizer = new StringTokenizer(n, "/\\:,;");
             while (tokenizer.hasMoreTokens()) {
                 n = tokenizer.nextToken();
             }
         }
         if (n == null || n.length() == 0) {
             n = "UNNAMED";
         }
         return StringUtils.toUpperEnglish(n);
     }
 
     private synchronized void open(final int traceLevelFile, final int traceLevelSystemOut, final ConnectionInfo ci, final DatabaseID localMachineLocation) throws SQLException, StartupException {
 
         boolean databaseExists = false; // whether the database already exists
         // on disk. i.e. with .db.data files,
         // etc.
 
         if (persistent) {
 
             if (SysProperties.PAGE_STORE) {
                 final String pageFileName = databaseName + Constants.SUFFIX_PAGE_FILE;
                 if (FileUtils.exists(pageFileName) && FileUtils.isReadOnly(pageFileName)) {
                     readOnly = true;
                 }
             }
             final String dataFileName = databaseName + Constants.SUFFIX_DATA_FILE;
 
             databaseExists = FileUtils.exists(dataFileName);
 
             if (databaseExists) {
                 Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Database already exists at: " + dataFileName);
             }
             else {
                 Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Database doesn't exist at: " + dataFileName);
             }
 
             if (FileUtils.exists(dataFileName)) {
                 // if it is already read-only because ACCESS_MODE_DATA=r
                 readOnly = readOnly | FileUtils.isReadOnly(dataFileName);
             }
             if (readOnly) {
                 traceSystem = new TraceSystem(null, false);
             }
             else {
                 traceSystem = new TraceSystem(databaseName + Constants.SUFFIX_TRACE_FILE, true);
             }
             traceSystem.setLevelFile(traceLevelFile);
             traceSystem.setLevelSystemOut(traceLevelSystemOut);
             traceSystem.getTrace(Trace.DATABASE).info("opening " + databaseName + " (build " + Constants.BUILD_ID + ")");
             if (autoServerMode) {
                 if (readOnly || fileLockMethod == FileLock.LOCK_NO) { throw Message.getSQLException(ErrorCode.FEATURE_NOT_SUPPORTED); }
             }
             if (!readOnly && fileLockMethod != FileLock.LOCK_NO) {
                 lock = new FileLock(traceSystem, databaseName + Constants.SUFFIX_LOCK_FILE, Constants.LOCK_SLEEP);
                 lock.lock(fileLockMethod);
                 if (autoServerMode) {
                     startServer(lock.getUniqueId());
                 }
             }
             // wait until pending changes are written
             isReconnectNeeded();
             if (SysProperties.PAGE_STORE) {
                 final PageStore store = getPageStore();
                 store.recover();
             }
             if (FileUtils.exists(dataFileName)) {
                 lobFilesInDirectories &= !ValueLob.existsLobFile(getDatabasePath());
                 lobFilesInDirectories |= FileUtils.exists(databaseName + Constants.SUFFIX_LOBS_DIRECTORY);
             }
             dummy = DataPage.create(this, 0);
             deleteOldTempFiles();
             log = new LogSystem(this, databaseName, readOnly, accessModeLog, pageStore);
             if (pageStore == null) {
                 openFileData();
                 log.open();
                 openFileIndex();
                 log.recover();
                 fileData.init();
                 try {
                     fileIndex.init();
                 }
                 catch (final Exception e) {
                     if (recovery) {
                         traceSystem.getTrace(Trace.DATABASE).error("opening index", e);
                         final ArrayList<DbObject> list = new ArrayList<DbObject>(storageMap.values());
                         for (int i = 0; i < list.size(); i++) {
                             final Storage s = (Storage) list.get(i);
                             if (s.getDiskFile() == fileIndex) {
                                 removeStorage(s.getId(), fileIndex);
                             }
                         }
                         fileIndex.delete();
                         openFileIndex();
                     }
                     else {
                         throw Message.convert(e);
                     }
                 }
             }
             reserveLobFileObjectIds();
             writer = WriterThread.create(this, writeDelay);
         }
         else {
             traceSystem = new TraceSystem(null, false);
             log = new LogSystem(null, null, false, null, null);
         }
         systemUser = new User(this, 0, Constants.DBA_NAME, true);
         h2oSchemaUser = new User(this, 1, "H2O", true);
         h2oSystemUser = new User(this, 1, "system", true);
 
         mainSchema = new Schema(this, 0, Constants.SCHEMA_MAIN, systemUser, true);
         infoSchema = new Schema(this, -1, Constants.SCHEMA_INFORMATION, systemUser, true);
 
         schemas.put(mainSchema.getName(), mainSchema);
         schemas.put(infoSchema.getName(), infoSchema);
         publicRole = new Role(this, 0, Constants.PUBLIC_ROLE_NAME, true);
         roles.put(Constants.PUBLIC_ROLE_NAME, publicRole);
         systemUser.setAdmin(true);
         h2oSchemaUser.setAdmin(true);
         h2oSystemUser.setAdmin(true);
 
         systemSession = new Session(this, systemUser);
         h2oSession = new Session(this, h2oSchemaUser);
         h2oSystemSession = new Session(this, h2oSystemUser);
 
         final ObjectArray cols = new ObjectArray();
         final Column columnId = new Column("ID", Value.INT);
         columnId.setNullable(false);
         cols.add(columnId);
         cols.add(new Column("HEAD", Value.INT));
         cols.add(new Column("TYPE", Value.INT));
         cols.add(new Column("SQL", Value.STRING));
         int headPos = 0;
         if (pageStore != null) {
             headPos = pageStore.getMetaTableHeadPos();
         }
         meta = mainSchema.createTable("SYS", 0, cols, persistent, false, headPos);
         tableMap.put(0, meta);
         final IndexColumn[] pkCols = IndexColumn.wrap(new Column[]{columnId});
         metaIdIndex = meta.addIndex(systemSession, "SYS_ID", 0, pkCols, IndexType.createPrimaryKey(false, false), Index.EMPTY_HEAD, null);
         objectIds.set(0);
         // there could be views on system tables, so they must be added first
         for (int i = 0; i < MetaTable.getMetaTableTypeCount(); i++) {
             addMetaData(i);
         }
 
         starting = true;
 
         /*
          * H2O STARTUP CODE FOR System Table, TABLE INSTANITATION
          */
         if (!isManagementDB()) { // don't run this code with the TCP server management DB
 
             databaseRemote.connectToDatabaseSystem(h2oSystemSession, databaseSettings);
 
             // Establish Proxies
             startTableManagerInstanceServer();
             startDatabaseInstanceServer();
 
             createMetaDataReplicationThread(); //Must be executed after call to databaseRemote because of getLocalDatabaseInstanceInWrapper() call.
         }
 
         /*
          * ###################################################################### ###### END OF System Table STARTUP CODE At this point in
          * the code this database instance will be connected to a System Table, so when tables are generated (below), it will be possible
          * for them to re-instantiate Table Managers where possible. ######################################################################
          * ######
          */
 
         final Cursor cursor = metaIdIndex.find(systemSession, null, null);
         // first, create all function aliases and sequences because
         // they might be used in create table / view / constraints and so on
 
         final ObjectArray records = new ObjectArray();
 
         while (cursor.next()) {
             final MetaRecord rec = new MetaRecord(cursor.get());
             objectIds.set(rec.getId());
 
             records.add(rec);
         }
 
         MetaRecord.sort(records);
 
         if (!isManagementDB() && databaseExists) {
             /*
              * Create or connect to a new System Table instance if this node already has tables on it.
              */
             Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Database already exists. No need to recreate the System Table.");
             createSystemTableOrGetReferenceToIt(true, systemTableRef.isSystemTableLocal(), false);
         }
 
         if (records.size() > 0) {
             final TableProxyManager proxyManager = new TableProxyManager(this, systemSession, true);
 
             for (int i = 0; i < records.size(); i++) {
                 final MetaRecord rec = (MetaRecord) records.get(i);
 
                 if (rec.getSQL().startsWith("CREATE FORCE LINKED TABLE")) {
                     continue;
                 }
 
                rec.execute(this, systemSession, eventListener, proxyManager);
             }
 
             proxyManager.finishTransaction(true, true, this);
         }
 
         if (!isManagementDB()) {
             Diagnostic.traceNoEvent(DiagnosticLevel.INIT, " Executed meta-records.");
         }
 
         // try to recompile the views that are invalid
         recompileInvalidViews(systemSession);
 
         starting = false;
         addDefaultSetting(systemSession, SetTypes.DEFAULT_LOCK_TIMEOUT, null, Constants.INITIAL_LOCK_TIMEOUT);
         addDefaultSetting(systemSession, SetTypes.DEFAULT_TABLE_TYPE, null, Constants.DEFAULT_TABLE_TYPE);
         addDefaultSetting(systemSession, SetTypes.CACHE_SIZE, null, SysProperties.CACHE_SIZE_DEFAULT);
         addDefaultSetting(systemSession, SetTypes.CLUSTER, Constants.CLUSTERING_DISABLED, 0);
         addDefaultSetting(systemSession, SetTypes.WRITE_DELAY, null, Constants.DEFAULT_WRITE_DELAY);
         addDefaultSetting(systemSession, SetTypes.CREATE_BUILD, null, Constants.BUILD_ID);
         if (!readOnly) {
             removeUnusedStorages(systemSession);
         }
 
         systemSession.commit(true);
         traceSystem.getTrace(Trace.DATABASE).info("opened " + databaseName);
 
         if (!isManagementDB() && (!databaseExists || !systemTableRef.isSystemTableLocal())) {
             // don't run this code with the TCP server management DB
 
             try {
                 createH2OTables(false, databaseExists);
             }
             catch (final Exception e) {
                 ErrorHandling.exceptionError(e, "Error creating H2O tables.");
             }
 
             // called here, because at this point the system is ready to replicate TM state.
             databaseRemote.setAsReadyToReplicateMetaData(metaDataReplicaManager);
         }
         else if (!isManagementDB() && databaseExists && systemTableRef.isSystemTableLocal()) {
             /*
              * This is the System Table. Reclaim previously held state.
              */
             try {
                 createH2OTables(true, databaseExists);
                 systemTableRef.getSystemTable().recreateInMemorySystemTableFromLocalPersistedState();
                 // called here, because at this point the system is ready to replicate TM state.
                 databaseRemote.setAsReadyToReplicateMetaData(metaDataReplicaManager);
 
                 Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Re-created System Table state.");
             }
             catch (final Exception e) {
                Diagnostic.trace(DiagnosticLevel.FULL, "error creating H2O tables");
             }
         }
     }
 
     public void startDatabaseInstanceServer() {
 
         int preferredDatabaseInstancePort = Integer.parseInt(databaseSettings.get("DATABASE_INSTANCE_SERVER_PORT"));
         preferredDatabaseInstancePort = H2ONetUtils.getInactiveTCPPort(preferredDatabaseInstancePort);
 
         database_instance_server = new DatabaseInstanceServer(getLocalDatabaseInstance(), preferredDatabaseInstancePort, getRemoteInterface().getApplicationRegistryIDForLocalDatabase());
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, getID() + ": Database instance server started on port " + preferredDatabaseInstancePort);
 
         try {
             database_instance_server.start(true); // true means: allow registry entry for this database ID to be overwritten
             Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Database Instance Server started for database " + getID() + " on port " + preferredDatabaseInstancePort + ".");
         }
         catch (final Exception e) {
             ErrorHandling.hardExceptionError(e, "Couldn't start database instance server.");
         }
 
         databaseRemote.setDatabaseInstanceServerPort(preferredDatabaseInstancePort);
     }
 
     public void startTableManagerInstanceServer() {
 
         int preferredTableManagerPort = Integer.parseInt(databaseSettings.get("TABLE_MANAGER_SERVER_PORT"));
         preferredTableManagerPort = H2ONetUtils.getInactiveTCPPort(preferredTableManagerPort);
 
         table_manager_instance_server = new TableManagerInstanceServer(preferredTableManagerPort);
 
         try {
             table_manager_instance_server.start();
             Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Table Manager Instance Server started for database " + getID() + " on port " + preferredTableManagerPort + ".");
         }
         catch (final Exception e) {
             ErrorHandling.hardExceptionError(e, "Couldn't start table manager instance server.");
         }
     }
 
     public void createMetaDataReplicationThread() {
 
         final boolean metaDataReplicationEnabled = Boolean.parseBoolean(databaseSettings.get("METADATA_REPLICATION_ENABLED"));
         final int systemTableReplicationFactor = Integer.parseInt(databaseSettings.get("SYSTEM_TABLE_REPLICATION_FACTOR"));
         final int tableManagerReplicationFactor = Integer.parseInt(databaseSettings.get("TABLE_MANAGER_REPLICATION_FACTOR"));
 
         final int replicationThreadSleepTime = Integer.parseInt(databaseSettings.get("METADATA_REPLICATION_THREAD_SLEEP_TIME"));
 
         metaDataReplicaManager = new MetaDataReplicaManager(metaDataReplicationEnabled, systemTableReplicationFactor, tableManagerReplicationFactor, getLocalDatabaseInstanceInWrapper(), this);
         metaDataReplicationThread = new MetaDataReplicationThread(metaDataReplicaManager, systemTableRef, this, replicationThreadSleepTime);
         metaDataReplicationThread.setName("MetaDataReplicationThread");
     }
 
     private void createSystemTableOrGetReferenceToIt(final boolean databaseExists, final boolean persistedTablesExist, final boolean createTables) throws SQLException {
 
         if (systemTableRef.isSystemTableLocal()) {
             // Create the System Table tables and immediately add local tables to this manager.
 
             SystemTable systemTable = null;
             try {
                 systemTable = new SystemTable(this, createTables);
             }
             catch (final Exception e) {
                 e.printStackTrace();
                 ErrorHandling.hardError(e.getMessage());
             }
 
             startSystemTableServer(systemTable);
 
             systemTableRef.setSystemTable(systemTable);
 
             final boolean successfullyCreated = databaseRemote.commitSystemTableCreation();
 
             if (successfullyCreated) {
                 Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Created new System Table locally.");
             }
             else {
                 //In the case that another instance has created a system table at the same time, try to get a refrence to this:
                 connectToRemoteSystemTable();
             }
         }
         else {
             connectToRemoteSystemTable();
         }
     }
 
     /**
      * Try to create a connection to an active remote system table.
      * @throws SQLException thrown if no connection could be made.
      */
     public void connectToRemoteSystemTable() throws SQLException {
 
         systemTableRef.findSystemTable();
         Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Obtained reference to existing System Table.");
     }
 
     public void startSystemTableServer(final ISystemTableMigratable newSystemTable) {
 
         int preferredSystemTablePort = Integer.parseInt(databaseSettings.get("SYSTEM_TABLE_SERVER_PORT"));
         preferredSystemTablePort = H2ONetUtils.getInactiveTCPPort(preferredSystemTablePort);
 
         system_table_server = new SystemTableServer(newSystemTable, preferredSystemTablePort); // added if we become a system table.
 
         try {
             system_table_server.start();
         }
         catch (final Exception e) {
             ErrorHandling.hardExceptionError(e, "Couldn't start system table instance server.");
         }
     }
 
     public DatabaseID getID() {
 
         return databaseRemote.getLocalMachineLocation();
     }
 
     public Schema getMainSchema() {
 
         return mainSchema;
     }
 
     private void startServer(final String key) throws SQLException {
 
         server = Server.createTcpServer(new String[]{"-tcpPort", "0", "-tcpAllowOthers", "true", "-key", key, databaseName});
         server.start();
         final String address = NetUtils.getLocalAddress() + ":" + server.getPort();
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Server started on: " + address);
         lock.setProperty("server", address);
         lock.save();
     }
 
     private void stopServer() {
 
         if (server != null) {
             final Server s = server;
             // avoid calling stop recursively
             // because stopping the server will
             // try to close the database as well
             server = null;
             s.stop();
         }
     }
 
     private void recompileInvalidViews(final Session session) {
 
         boolean recompileSuccessful;
         do {
             recompileSuccessful = false;
 
             final Set<Table> alltables = getAllReplicas();
 
             for (final Table table : alltables) {
                 if (table instanceof TableView) {
                     final TableView view = (TableView) table;
                     if (view.getInvalid()) {
                         try {
                             view.recompile(session);
                         }
                         catch (final SQLException e) {
                             // ignore
                         }
                         if (!view.getInvalid()) {
                             recompileSuccessful = true;
                         }
                     }
                 }
             }
         }
         while (recompileSuccessful);
         // when opening a database, views are initialized before indexes,
         // so they may not have the optimal plan yet
         // this is not a problem, it is just nice to see the newest plan
 
         final Set<Table> allTables = getAllReplicas();
 
         for (final Table table : allTables) {
             if (table instanceof TableView) {
                 final TableView view = (TableView) table;
                 if (!view.getInvalid()) {
                     try {
                         view.recompile(systemSession);
                     }
                     catch (final SQLException e) {
                         // ignore
                     }
                 }
             }
         }
     }
 
     private void removeUnusedStorages(final Session session) throws SQLException {
 
         if (persistent) {
             final ObjectArray storages = getAllStorages();
             for (int i = 0; i < storages.size(); i++) {
                 final Storage storage = (Storage) storages.get(i);
                 if (storage != null && storage.getRecordReader() == null) {
                     storage.truncate(session);
                 }
             }
         }
     }
 
     private void addDefaultSetting(final Session session, final int type, final String stringValue, final int intValue) throws SQLException {
 
         if (readOnly) { return; }
         final String name = SetTypes.getTypeName(type);
         if (settings.get(name) == null) {
             final Setting setting = new Setting(this, allocateObjectId(false, true), name);
             if (stringValue == null) {
                 setting.setIntValue(intValue);
             }
             else {
                 setting.setStringValue(stringValue);
             }
             addDatabaseObject(session, setting);
         }
     }
 
     /**
      * Remove the storage object from the file.
      *
      * @param id
      *            the storage id
      * @param file
      *            the file
      */
     public void removeStorage(final int id, final DiskFile file) {
 
         if (SysProperties.CHECK) {
             final Storage s = (Storage) storageMap.get(id);
             if (s == null || s.getDiskFile() != file) {
                 Message.throwInternalError();
             }
         }
         storageMap.remove(id);
     }
 
     /**
      * Get the storage object for the given file. An new object is created if required.
      *
      * @param id
      *            the storage id
      * @param file
      *            the file
      * @return the storage object
      */
     public Storage getStorage(final int id, final DiskFile file) {
 
         Storage storage = (Storage) storageMap.get(id);
         if (storage != null) {
             if (SysProperties.CHECK && storage.getDiskFile() != file) {
                 Message.throwInternalError();
             }
         }
         else {
             storage = new Storage(this, file, null, id);
             storageMap.put(id, storage);
         }
         return storage;
     }
 
     private void addMetaData(final int type) throws SQLException {
 
         final MetaTable m = new MetaTable(infoSchema, -1 - type, type);
         infoSchema.add(m);
     }
 
     private synchronized void addMeta(final Session session, final DbObject obj) throws SQLException {
 
         final int id = obj.getId();
         if (id > 0 && !starting && !obj.getTemporary()) {
             final Row r = meta.getTemplateRow();
             final MetaRecord rec = new MetaRecord(obj);
             rec.setRecord(r);
             objectIds.set(id);
             meta.lock(session, true, true);
             meta.addRow(session, r);
         }
         if (SysProperties.PAGE_STORE && id > 0) {
             databaseObjects.put(ObjectUtils.getInteger(id), obj);
         }
     }
 
     /**
      * Remove the given object from the meta data.
      *
      * @param session
      *            the session
      * @param id
      *            the id of the object to remove
      */
     public synchronized void removeMeta(final Session session, final int id) throws SQLException {
 
         if (id > 0 && !starting) {
             final SearchRow r = meta.getTemplateSimpleRow(false);
             r.setValue(0, ValueInt.get(id));
             final Cursor cursor = metaIdIndex.find(session, r, r);
             if (cursor.next()) {
                 final Row found = cursor.get();
                 meta.lock(session, true, true);
                 meta.removeRow(session, found);
 
                 objectIds.clear(id);
                 if (SysProperties.CHECK) {
                     checkMetaFree(session, id);
                 }
             }
         }
         if (SysProperties.PAGE_STORE) {
             databaseObjects.remove(ObjectUtils.getInteger(id));
         }
     }
 
     private HashMap<String, DbObject> getMap(final int type) {
 
         switch (type) {
             case DbObject.USER:
                 return users;
             case DbObject.SETTING:
                 return settings;
             case DbObject.ROLE:
                 return roles;
             case DbObject.RIGHT:
                 return rights;
             case DbObject.FUNCTION_ALIAS:
                 return functionAliases;
             case DbObject.SCHEMA:
                 return schemas;
             case DbObject.USER_DATATYPE:
                 return userDataTypes;
             case DbObject.COMMENT:
                 return comments;
             case DbObject.AGGREGATE:
                 return aggregates;
             default:
                 throw Message.throwInternalError("type=" + type);
         }
     }
 
     /**
      * Add a schema object to the database.
      *
      * @param session
      *            the session
      * @param obj
      *            the object to add
      */
     public synchronized void addSchemaObject(final Session session, final SchemaObject obj) throws SQLException {
 
         final int id = obj.getId();
         if (id > 0 && !starting) {
             checkWritingAllowed();
         }
         obj.getSchema().add(obj);
         addMeta(session, obj);
         if (obj instanceof TableData) {
             tableMap.put(id, obj);
         }
     }
 
     /**
      * Add an object to the database.
      *
      * @param session
      *            the session
      * @param obj
      *            the object to add
      */
     public synchronized void addDatabaseObject(final Session session, final DbObject obj) throws SQLException {
 
         final int id = obj.getId();
         if (id > 0 && !starting) {
             checkWritingAllowed();
         }
         final HashMap<String, DbObject> map = getMap(obj.getType());
         if (obj.getType() == DbObject.USER) {
             final User user = (User) obj;
             if (user.getAdmin() && systemUser.getName().equals(Constants.DBA_NAME)) {
                 systemUser.rename(user.getName());
             }
         }
         final String name = obj.getName();
         if (SysProperties.CHECK && map.get(name) != null) {
             Message.throwInternalError("object already exists");
         }
         addMeta(session, obj);
         map.put(name, obj);
     }
 
     /**
      * Get the user defined aggregate function if it exists, or null if not.
      *
      * @param name
      *            the name of the user defined aggregate function
      * @return the aggregate function or null
      */
     public UserAggregate findAggregate(final String name) {
 
         return (UserAggregate) aggregates.get(name);
     }
 
     /**
      * Get the comment for the given database object if one exists, or null if not.
      *
      * @param object
      *            the database object
      * @return the comment or null
      */
     public Comment findComment(final DbObject object) {
 
         if (object.getType() == DbObject.COMMENT) { return null; }
         final String key = Comment.getKey(object);
         return (Comment) comments.get(key);
     }
 
     /**
      * Get the user defined function if it exists, or null if not.
      *
      * @param name
      *            the name of the user defined function
      * @return the function or null
      */
     public FunctionAlias findFunctionAlias(final String name) {
 
         return (FunctionAlias) functionAliases.get(name);
     }
 
     /**
      * Get the role if it exists, or null if not.
      *
      * @param roleName
      *            the name of the role
      * @return the role or null
      */
     public Role findRole(final String roleName) {
 
         return (Role) roles.get(roleName);
     }
 
     /**
      * Get the schema if it exists, or null if not.
      *
      * @param schemaName
      *            the name of the schema
      * @return the schema or null
      */
     public Schema findSchema(final String schemaName) {
 
         return (Schema) schemas.get(schemaName);
     }
 
     /**
      * Get the setting if it exists, or null if not.
      *
      * @param name
      *            the name of the setting
      * @return the setting or null
      */
     public Setting findSetting(final String name) {
 
         return (Setting) settings.get(name);
     }
 
     /**
      * Get the user if it exists, or null if not.
      *
      * @param name
      *            the name of the user
      * @return the user or null
      */
     public User findUser(final String name) {
 
         return (User) users.get(name);
     }
 
     /**
      * Get the user defined data type if it exists, or null if not.
      *
      * @param name
      *            the name of the user defined data type
      * @return the user defined data type or null
      */
     public UserDataType findUserDataType(final String name) {
 
         return (UserDataType) userDataTypes.get(name);
     }
 
     /**
      * Get user with the given name. This method throws an exception if the user does not exist.
      *
      * @param name
      *            the user name
      * @return the user
      * @throws SQLException
      *             if the user does not exist
      */
     public User getUser(final String name) throws SQLException {
 
         User user = findUser(name);
         // H2O bug fix (where admin user is not found on startup queries
         if (user == null) {
             user = systemUser;
         }
         if (user == null) { throw Message.getSQLException(ErrorCode.USER_NOT_FOUND_1, name); }
         return user;
     }
 
     /**
      * Create a session for the given user.
      *
      * @param user
      *            the user
      * @return the session
      * @throws SQLException
      *             if the database is in exclusive mode
      */
     public synchronized Session createSession(final User user) throws SQLException {
 
         if (exclusiveSession != null) { throw Message.getSQLException(ErrorCode.DATABASE_IS_IN_EXCLUSIVE_MODE); }
         final Session session = new Session(this, user);
         userSessions.add(session);
         traceSystem.getTrace(Trace.SESSION).info("connecting #" + session.getSessionId() + " to " + databaseName);
         if (delayedCloser != null) {
             delayedCloser.reset();
             delayedCloser = null;
         }
         return session;
     }
 
     /**
      * Remove a session. This method is called after the user has disconnected.
      *
      * @param session
      *            the session
      */
     public synchronized void removeSession(final Session session) {
 
         if (session != null) {
             if (exclusiveSession == session) {
                 exclusiveSession = null;
             }
             userSessions.remove(session);
             if (session != systemSession) {
                 traceSystem.getTrace(Trace.SESSION).info("disconnecting #" + session.getSessionId());
             }
         }
         if (userSessions.size() == 0 && session != systemSession) {
 
             if (closeDelay == 0) {
                 close(false);
             }
             else if (closeDelay < 0) {
                 return;
             }
             else {
                 delayedCloser = new DatabaseCloser(this, closeDelay * 1000, false);
                 delayedCloser.setName("H2 Close Delay " + getShortName());
                 delayedCloser.setDaemon(true);
                 delayedCloser.start();
             }
         }
         if (session != systemSession && session != null) {
             traceSystem.getTrace(Trace.SESSION).info("disconnected #" + session.getSessionId());
         }
     }
 
     /**
      * Close the database.
      *
      * @param fromShutdownHook true if this method is called from the shutdown hook
      */
     public synchronized void close(final boolean fromShutdownHook) {
 
         if (closing) { return; }
 
         Diagnostic.traceNoEvent(DiagnosticLevel.INIT, getID().getURL());
 
         closing = true;
         stopServer();
         if (!isManagementDB() && !fromShutdownHook) {
             H2OEventBus.publish(new H2OEvent(getID().getURL(), DatabaseStates.DATABASE_SHUTDOWN, null));
 
             metaDataReplicationThread.setRunning(false);
             running = false;
             removeLocalDatabaseInstance();
 
             try {
                 table_manager_instance_server.stop();
                 if (system_table_server != null) {
                     system_table_server.stop();
                 }
                 database_instance_server.stop();
             }
             catch (final Exception e) {
                 ErrorHandling.exceptionErrorNoEvent(e, "Failed to shutdown one of the H2O servers on database shutdown.");
             }
 
             if (numonic != null) {
                 numonic.shutdown();
             }
         }
 
         if (userSessions.size() > 0) {
             if (!fromShutdownHook) { return; }
             traceSystem.getTrace(Trace.DATABASE).info("closing " + databaseName + " from shutdown hook");
             final Session[] all = new Session[userSessions.size()];
             userSessions.toArray(all);
             for (final Session s : all) {
                 try {
                     // Must roll back, otherwise the session is removed and the log file that contains its uncommitted operations as well.
                     s.rollback();
                     s.close();
                 }
                 catch (final SQLException e) {
                     traceSystem.getTrace(Trace.SESSION).error("disconnecting #" + s.getSessionId(), e);
                 }
             }
         }
         if (log != null) {
             log.setDisabled(false);
         }
         traceSystem.getTrace(Trace.DATABASE).info("closing " + databaseName);
         if (eventListener != null) {
             // allow the event listener to connect to the database
             closing = false;
             final DatabaseEventListener e = eventListener;
             // set it to null, to make sure it's called only once
             eventListener = null;
             e.closingDatabase();
             if (userSessions.size() > 0) {
                 // if a connection was opened, we can't close the database
                 return;
             }
             closing = true;
         }
         try {
             if (systemSession != null) {
                 final Set<Table> alltables = getAllReplicas();
 
                 for (final Table table : alltables) {
                     table.close(systemSession);
                 }
                 final ObjectArray sequences = getAllSchemaObjects(DbObject.SEQUENCE);
                 for (int i = 0; i < sequences.size(); i++) {
                     final Sequence sequence = (Sequence) sequences.get(i);
                     sequence.close();
                 }
                 final ObjectArray triggers = getAllSchemaObjects(DbObject.TRIGGER);
                 for (int i = 0; i < triggers.size(); i++) {
                     final TriggerObject trigger = (TriggerObject) triggers.get(i);
                     trigger.close();
                 }
                 meta.close(systemSession);
                 indexSummaryValid = true;
             }
         }
         catch (final SQLException e) {
             traceSystem.getTrace(Trace.DATABASE).error("close", e);
         }
         // remove all session variables
         if (persistent) {
             try {
                 ValueLob.removeAllForTable(this, ValueLob.TABLE_ID_SESSION_VARIABLE);
             }
             catch (final SQLException e) {
                 traceSystem.getTrace(Trace.DATABASE).error("close", e);
             }
         }
         tempFileDeleter.deleteAll();
         try {
             closeOpenFilesAndUnlock();
         }
         catch (final SQLException e) {
             traceSystem.getTrace(Trace.DATABASE).error("close", e);
         }
         traceSystem.getTrace(Trace.DATABASE).info("closed");
         traceSystem.close();
         if (closeOnExit != null) {
             closeOnExit.reset();
             try {
                 Runtime.getRuntime().removeShutdownHook(closeOnExit);
             }
             catch (final IllegalStateException e) {
                 // ignore
             }
             catch (final SecurityException e) {
                 // applets may not do that - ignore
             }
             closeOnExit = null;
         }
         Engine.getInstance().close(databaseName);
         if (deleteFilesOnDisconnect && persistent) {
             deleteFilesOnDisconnect = false;
             try {
                 final String directory = FileUtils.getParent(databaseName);
                 final String name = FileUtils.getFileName(databaseName);
                 DeleteDbFiles.execute(directory, name, true);
             }
             catch (final Exception e) {
                 // ignore (the trace is closed already)
             }
         }
         closing = false;
     }
 
     private void stopWriter() {
 
         if (writer != null) {
             try {
                 writer.stopThread();
             }
             catch (final SQLException e) {
                 traceSystem.getTrace(Trace.DATABASE).error("close", e);
             }
             writer = null;
         }
     }
 
     private synchronized void closeOpenFilesAndUnlock() throws SQLException {
 
         if (log != null) {
             stopWriter();
             try {
                 log.close();
             }
             catch (final Throwable e) {
                 traceSystem.getTrace(Trace.DATABASE).error("close", e);
             }
             log = null;
         }
         if (pageStore != null) {
             pageStore.checkpoint();
         }
         closeFiles();
         if (persistent && lock == null && fileLockMethod != FileLock.LOCK_NO) {
             // everything already closed (maybe in checkPowerOff)
             // don't delete temp files in this case because
             // the database could be open now (even from within another process)
             return;
         }
         if (persistent) {
             deleteOldTempFiles();
         }
         if (systemSession != null) {
             systemSession.close();
             systemSession = null;
         }
         if (lock != null) {
             lock.unlock();
             lock = null;
         }
     }
 
     private void closeFiles() {
 
         try {
             if (fileData != null) {
                 fileData.close();
                 fileData = null;
             }
             if (fileIndex != null) {
                 fileIndex.close();
                 fileIndex = null;
             }
             if (pageStore != null) {
                 pageStore.close();
                 pageStore = null;
             }
         }
         catch (final SQLException e) {
             traceSystem.getTrace(Trace.DATABASE).error("close", e);
         }
         storageMap.clear();
     }
 
     private void checkMetaFree(final Session session, final int id) throws SQLException {
 
         final SearchRow r = meta.getTemplateSimpleRow(false);
         r.setValue(0, ValueInt.get(id));
         final Cursor cursor = metaIdIndex.find(session, r, r);
         if (cursor.next()) {
             Message.throwInternalError();
         }
     }
 
     @Override
     public synchronized int allocateObjectId(boolean needFresh, final boolean dataFile) {
 
         // TODO refactor: use hash map instead of bit field for object ids
         needFresh = true;
         int i;
         if (needFresh) {
             i = objectIds.getLastSetBit() + 1;
             if ((i & 1) != (dataFile ? 1 : 0)) {
                 i++;
             }
 
             while (storageMap.get(i) != null || objectIds.get(i)) {
                 i++;
                 if ((i & 1) != (dataFile ? 1 : 0)) {
                     i++;
                 }
             }
         }
         else {
             i = objectIds.nextClearBit(0);
         }
         if (SysProperties.CHECK && objectIds.get(i)) {
             Message.throwInternalError();
         }
         objectIds.set(i);
         return i;
     }
 
     public ObjectArray getAllAggregates() {
 
         return new ObjectArray(aggregates.values());
     }
 
     public ObjectArray getAllComments() {
 
         return new ObjectArray(comments.values());
     }
 
     public ObjectArray getAllFunctionAliases() {
 
         return new ObjectArray(functionAliases.values());
     }
 
     public int getAllowLiterals() {
 
         if (starting) { return Constants.ALLOW_LITERALS_ALL; }
         return allowLiterals;
     }
 
     public ObjectArray getAllRights() {
 
         return new ObjectArray(rights.values());
     }
 
     public ObjectArray getAllRoles() {
 
         return new ObjectArray(roles.values());
     }
 
     /**
      * Get all schema objects of the given type.
      *
      * @param type
      *            the object type
      * @return all objects of that type
      */
     public ObjectArray getAllSchemaObjects(final int type) {
 
         final ObjectArray list = new ObjectArray();
         for (final DbObject dbObject : schemas.values()) {
             final Schema schema = (Schema) dbObject;
             list.addAll(schema.getAll(type));
         }
         return list;
     }
 
     /**
      * Get all tables. Replaces the getAllSchemaObjects method for this particular call.
      *
      * @param type
      *            the object type
      * @return all objects of that type
      */
     public Set<ReplicaSet> getAllTables() {
 
         final Set<ReplicaSet> list = new HashSet<ReplicaSet>();
         for (final DbObject dbObject : schemas.values()) {
             final Schema schema = (Schema) dbObject;
             list.addAll(schema.getTablesAndViews().values());
         }
         return list;
     }
 
     /**
      * Get every single table instance, including replicas for the same table.
      *
      * @return
      */
     public Set<Table> getAllReplicas() {
 
         final Set<ReplicaSet> allReplicaSets = getAllTables();
 
         final Set<Table> alltables = new HashSet<Table>();
         for (final ReplicaSet tableSet : allReplicaSets) {
             alltables.addAll(tableSet.getAllCopies());
         }
 
         return alltables;
     }
 
     public ObjectArray getAllSchemas() {
 
         return new ObjectArray(schemas.values());
     }
 
     public ObjectArray getAllSettings() {
 
         return new ObjectArray(settings.values());
     }
 
     public ObjectArray getAllStorages() {
 
         return new ObjectArray(storageMap.values());
     }
 
     public ObjectArray getAllUserDataTypes() {
 
         return new ObjectArray(userDataTypes.values());
     }
 
     public ObjectArray getAllUsers() {
 
         return new ObjectArray(users.values());
     }
 
     public String getCacheType() {
 
         return cacheType;
     }
 
     @Override
     public int getChecksum(final byte[] data, int start, final int end) {
 
         int x = 0;
         while (start < end) {
             x += data[start++];
         }
         return x;
     }
 
     public String getCluster() {
 
         return cluster;
     }
 
     public CompareMode getCompareMode() {
 
         return compareMode;
     }
 
     @Override
     public String getDatabasePath() {
 
         if (persistent) { return FileUtils.getAbsolutePath(databaseName); }
         return null;
     }
 
     public String getShortName() {
 
         return databaseShortName;
     }
 
     public String getName() {
 
         return databaseName;
     }
 
     public LogSystem getLog() {
 
         return log;
     }
 
     /**
      * Get all sessions that are currently connected to the database.
      *
      * @param includingSystemSession
      *            if the system session should also be included
      * @return the list of sessions
      */
     public Session[] getSessions(final boolean includingSystemSession) {
 
         final ArrayList<Session> list = new ArrayList<Session>(userSessions);
         if (includingSystemSession && systemSession != null) {
             list.add(systemSession);
         }
         final Session[] array = new Session[list.size()];
         list.toArray(array);
         return array;
     }
 
     /**
      * Update an object in the system table.
      *
      * @param session
      *            the session
      * @param obj
      *            the database object
      */
     public synchronized void update(final Session session, final DbObject obj) throws SQLException {
 
         final int id = obj.getId();
         removeMeta(session, id);
         addMeta(session, obj);
     }
 
     /**
      * Rename a schema object.
      *
      * @param session
      *            the session
      * @param obj
      *            the object
      * @param newName
      *            the new name
      */
     public synchronized void renameSchemaObject(final Session session, final SchemaObject obj, final String newName) throws SQLException {
 
         checkWritingAllowed();
         obj.getSchema().rename(obj, newName);
         updateWithChildren(session, obj);
     }
 
     private synchronized void updateWithChildren(final Session session, final DbObject obj) throws SQLException {
 
         final ObjectArray list = obj.getChildren();
         final Comment comment = findComment(obj);
         if (comment != null) {
             Message.throwInternalError();
         }
         update(session, obj);
         // remember that this scans only one level deep!
         for (int i = 0; list != null && i < list.size(); i++) {
             final DbObject o = (DbObject) list.get(i);
             if (o.getCreateSQL() != null) {
                 update(session, o);
             }
         }
     }
 
     /**
      * Rename a database object.
      *
      * @param session
      *            the session
      * @param obj
      *            the object
      * @param newName
      *            the new name
      */
     public synchronized void renameDatabaseObject(final Session session, final DbObject obj, final String newName) throws SQLException {
 
         checkWritingAllowed();
         final int type = obj.getType();
         final HashMap<String, DbObject> map = getMap(type);
         if (SysProperties.CHECK) {
             if (!map.containsKey(obj.getName())) {
                 Message.throwInternalError("not found: " + obj.getName());
             }
             if (obj.getName().equals(newName) || map.containsKey(newName)) {
                 Message.throwInternalError("object already exists: " + newName);
             }
         }
         obj.checkRename();
         final int id = obj.getId();
         removeMeta(session, id);
         map.remove(obj.getName());
         obj.rename(newName);
         map.put(newName, obj);
         updateWithChildren(session, obj);
     }
 
     @Override
     public String createTempFile() throws SQLException {
 
         try {
             final boolean inTempDir = readOnly;
             String name = databaseName;
             if (!persistent) {
                 name = FileSystem.PREFIX_MEMORY + name;
             }
             return FileUtils.createTempFile(name, Constants.SUFFIX_TEMP_FILE, true, inTempDir);
         }
         catch (final IOException e) {
             throw Message.convertIOException(e, databaseName);
         }
     }
 
     private void reserveLobFileObjectIds() throws SQLException {
 
         final String prefix = FileUtils.normalize(databaseName) + ".";
         final String path = FileUtils.getParent(databaseName);
         final String[] list = FileUtils.listFiles(path);
         for (final String element : list) {
             String name = element;
             if (name.endsWith(Constants.SUFFIX_LOB_FILE) && FileUtils.fileStartsWith(name, prefix)) {
                 name = name.substring(prefix.length());
                 name = name.substring(0, name.length() - Constants.SUFFIX_LOB_FILE.length());
                 final int dot = name.indexOf('.');
                 if (dot >= 0) {
                     final String id = name.substring(dot + 1);
                     final int objectId = Integer.parseInt(id);
                     objectIds.set(objectId);
                 }
             }
         }
     }
 
     private void deleteOldTempFiles() throws SQLException {
 
         final String path = FileUtils.getParent(databaseName);
         final String prefix = FileUtils.normalize(databaseName);
         final String[] list = FileUtils.listFiles(path);
         for (final String name : list) {
             if (name.endsWith(Constants.SUFFIX_TEMP_FILE) && FileUtils.fileStartsWith(name, prefix)) {
                 // can't always delete the files, they may still be open
                 FileUtils.tryDelete(name);
             }
         }
     }
 
     /**
      * Get or create the specified storage object.
      *
      * @param reader
      *            the record reader
      * @param id
      *            the object id
      * @param dataFile
      *            true if the data is in the data file
      * @return the storage
      */
     public Storage getStorage(final RecordReader reader, final int id, final boolean dataFile) {
 
         DiskFile file;
         if (dataFile) {
             file = fileData;
         }
         else {
             file = fileIndex;
         }
         final Storage storage = getStorage(id, file);
         storage.setReader(reader);
         return storage;
     }
 
     /**
      * Get the schema. If the schema does not exist, an exception is thrown.
      *
      * @param schemaName
      *            the name of the schema
      * @return the schema
      * @throws SQLException
      *             no schema with that name exists
      */
     public Schema getSchema(final String schemaName) throws SQLException {
 
         final Schema schema = findSchema(schemaName);
         if (schema == null) { throw Message.getSQLException(ErrorCode.SCHEMA_NOT_FOUND_1, schemaName); }
         return schema;
     }
 
     /**
      * Remove the object from the database.
      *
      * @param session
      *            the session
      * @param obj
      *            the object to remove
      */
     public synchronized void removeDatabaseObject(final Session session, final DbObject obj) throws SQLException {
 
         checkWritingAllowed();
         final String objName = obj.getName();
         final int type = obj.getType();
         final HashMap<String, DbObject> map = getMap(type);
         if (SysProperties.CHECK && !map.containsKey(objName)) {
             Message.throwInternalError("not found: " + objName);
         }
         final Comment comment = findComment(obj);
         if (comment != null) {
             removeDatabaseObject(session, comment);
         }
         final int id = obj.getId();
         obj.removeChildrenAndResources(session);
         map.remove(objName);
         removeMeta(session, id);
     }
 
     /**
      * Get the first table that depends on this object.
      *
      * @param obj
      *            the object to find
      * @param except
      *            the table to exclude (or null)
      * @return the first dependent table, or null
      */
     public ReplicaSet getDependentTable(final SchemaObject obj, final Table except) {
 
         switch (obj.getType()) {
             case DbObject.COMMENT:
             case DbObject.CONSTRAINT:
             case DbObject.INDEX:
             case DbObject.RIGHT:
             case DbObject.TRIGGER:
             case DbObject.USER:
                 return null;
             default:
         }
         final Set<ReplicaSet> list = getAllTables();
         final Set set = new HashSet();
 
         final Set<ReplicaSet> allreplicas = getAllTables();
 
         for (final ReplicaSet replicaSet : allreplicas) {
 
             if (except != null && replicaSet.getACopy() != null && except.getName().equalsIgnoreCase(replicaSet.getACopy().getName())) {
                 continue;
             }
 
             set.clear();
             replicaSet.addDependencies(set);
             if (set.contains(obj)) { return replicaSet; }
         }
         return null;
     }
 
     private String getFirstInvalidTable(final Session session) {
 
         String conflict = null;
         try {
             final Set<ReplicaSet> list = getAllTables();
             for (final ReplicaSet replicaSet : list) {
 
                 conflict = replicaSet.getSQL();
                 session.prepare(replicaSet.getCreateSQL());
             }
         }
         catch (final SQLException e) {
             return conflict;
         }
         return null;
     }
 
     /**
      * Remove an object from the system table.
      *
      * @param session
      *            the session
      * @param obj
      *            the object to be removed
      */
     public synchronized void removeSchemaObject(final Session session, final SchemaObject obj) throws SQLException {
 
         final int type = obj.getType();
         if (type == DbObject.TABLE_OR_VIEW) {
             final Table table = (Table) obj;
             if (table.getTemporary() && !table.getGlobalTemporary()) {
                 session.removeLocalTempTable(table);
                 return;
             }
         }
         else if (type == DbObject.INDEX) {
             final Index index = (Index) obj;
             final Table table = index.getTable();
             if (table.getTemporary() && !table.getGlobalTemporary()) {
                 session.removeLocalTempTableIndex(index);
                 return;
             }
         }
         else if (type == DbObject.CONSTRAINT) {
             final Constraint constraint = (Constraint) obj;
             final Table table = constraint.getTable();
             if (table.getTemporary() && !table.getGlobalTemporary()) {
                 session.removeLocalTempTableConstraint(constraint);
                 return;
             }
         }
         checkWritingAllowed();
         final Comment comment = findComment(obj);
         if (comment != null) {
             removeDatabaseObject(session, comment);
         }
         obj.getSchema().remove(obj);
         if (!starting) {
             String invalid;
             if (SysProperties.OPTIMIZE_DROP_DEPENDENCIES) {
                 final ReplicaSet replicaSet = getDependentTable(obj, null);
                 invalid = replicaSet == null ? null : replicaSet.getSQL();
             }
             else {
                 invalid = getFirstInvalidTable(session);
             }
             if (invalid != null) {
                 obj.getSchema().add(obj);
                 throw Message.getSQLException(ErrorCode.CANNOT_DROP_2, new String[]{obj.getSQL(), invalid});
             }
             obj.removeChildrenAndResources(session);
         }
         final int id = obj.getId();
         removeMeta(session, id);
         if (obj instanceof TableData) {
             tableMap.remove(id);
         }
     }
 
     /**
      * Check if this database disk-based.
      *
      * @return true if it is disk-based, false it it is in-memory only.
      */
     public boolean isPersistent() {
 
         return persistent;
     }
 
     public TraceSystem getTraceSystem() {
 
         return traceSystem;
     }
 
     public DiskFile getDataFile() {
 
         return fileData;
     }
 
     public DiskFile getIndexFile() {
 
         return fileIndex;
     }
 
     public synchronized void setCacheSize(final int kb) throws SQLException {
 
         if (fileData != null) {
             fileData.getCache().setMaxSize(kb);
             final int valueIndex = kb <= 32 ? kb : kb >>> SysProperties.CACHE_SIZE_INDEX_SHIFT;
             fileIndex.getCache().setMaxSize(valueIndex);
         }
     }
 
     public synchronized void setMasterUser(final User user) throws SQLException {
 
         addDatabaseObject(systemSession, user);
         systemSession.commit(true);
     }
 
     public Role getPublicRole() {
 
         return publicRole;
     }
 
     /**
      * Get a unique temporary table name.
      *
      * @param sessionId
      *            the session id
      * @return a unique name
      */
     public String getTempTableName(final int sessionId) {
 
         String tempName;
         for (int i = 0;; i++) {
             tempName = Constants.TEMP_TABLE_PREFIX + sessionId + "_" + i;
             if (mainSchema.findTableOrView(null, tempName, LocationPreference.NO_PREFERENCE) == null) {
                 break;
             }
         }
         return tempName;
     }
 
     public void setCompareMode(final CompareMode compareMode) {
 
         this.compareMode = compareMode;
     }
 
     public void setCluster(final String cluster) {
 
         this.cluster = cluster;
     }
 
     @Override
     public void checkWritingAllowed() throws SQLException {
 
         if (readOnly) { throw Message.getSQLException(ErrorCode.DATABASE_IS_READ_ONLY); }
         if (noDiskSpace) { throw Message.getSQLException(ErrorCode.NO_DISK_SPACE_AVAILABLE); }
     }
 
     public boolean getReadOnly() {
 
         return readOnly;
     }
 
     public void setWriteDelay(final int value) {
 
         writeDelay = value;
         if (writer != null) {
             writer.setWriteDelay(value);
         }
     }
 
     /**
      * Delete an unused log file. It is deleted immediately if no writer thread is running, or deleted later on if one is running. Deleting
      * is delayed because the hard drive otherwise may delete the file a bit before the data is written to the new file, which can cause
      * problems when recovering.
      *
      * @param fileName
      *            the name of the file to be deleted
      */
     public void deleteLogFileLater(final String fileName) throws SQLException {
 
         if (writer != null) {
             writer.deleteLogFileLater(fileName);
         }
         else {
             FileUtils.delete(fileName);
         }
     }
 
     public void setEventListener(final DatabaseEventListener eventListener) {
 
         this.eventListener = eventListener;
     }
 
     public void setEventListenerClass(final String className) throws SQLException {
 
         if (className == null || className.length() == 0) {
             eventListener = null;
         }
         else {
             try {
                 eventListener = (DatabaseEventListener) ClassUtils.loadUserClass(className).newInstance();
                 String url = databaseURL;
                 if (cipher != null) {
                     url += ";CIPHER=" + cipher;
                 }
                 eventListener.init(url);
             }
             catch (final Throwable e) {
                 throw Message.getSQLException(ErrorCode.ERROR_SETTING_DATABASE_EVENT_LISTENER_2, new String[]{className, e.toString()}, e);
             }
         }
     }
 
     @Override
     public synchronized void freeUpDiskSpace() throws SQLException {
 
         if (eventListener != null) {
             eventListener.diskSpaceIsLow(0);
         }
     }
 
     /**
      * Set the progress of a long running operation. This method calls the {@link DatabaseEventListener} if one is registered.
      *
      * @param state
      *            the {@link DatabaseEventListener} state
      * @param name
      *            the object name
      * @param x
      *            the current position
      * @param max
      *            the highest value
      */
 
     public void setProgress(final int state, final String name, final int x, final int max) {
 
         if (eventListener != null) {
             try {
                 eventListener.setProgress(state, name, x, max);
             }
             catch (final Exception e2) {
                 // ignore this second (user made) exception
             }
         }
     }
 
     /**
      * This method is called after an exception occurred, to inform the database event listener (if one is set).
      *
      * @param e
      *            the exception
      * @param sql
      *            the SQL statement
      */
     public void exceptionThrown(final SQLException e, final String sql) {
 
         if (eventListener != null) {
             try {
                 eventListener.exceptionThrown(e, sql);
             }
             catch (final Exception e2) {
                 // ignore this second (user made) exception
             }
         }
     }
 
     /**
      * Synchronize the files with the file system. This method is called when executing the SQL statement CHECKPOINT SYNC.
      */
     public void sync() throws SQLException {
 
         if (log != null) {
             log.sync();
         }
         if (fileData != null) {
             fileData.sync();
         }
         if (fileIndex != null) {
             fileIndex.sync();
         }
     }
 
     public int getMaxMemoryRows() {
 
         return maxMemoryRows;
     }
 
     public void setMaxMemoryRows(final int value) {
 
         maxMemoryRows = value;
     }
 
     public void setMaxMemoryUndo(final int value) {
 
         maxMemoryUndo = value;
     }
 
     public int getMaxMemoryUndo() {
 
         return maxMemoryUndo;
     }
 
     public void setLockMode(final int lockMode) throws SQLException {
 
         switch (lockMode) {
             case Constants.LOCK_MODE_OFF:
             case Constants.LOCK_MODE_READ_COMMITTED:
             case Constants.LOCK_MODE_TABLE:
             case Constants.LOCK_MODE_TABLE_GC:
                 break;
             default:
                 throw Message.getInvalidValueException("lock mode", "" + lockMode);
         }
         this.lockMode = lockMode;
     }
 
     public int getLockMode() {
 
         return lockMode;
     }
 
     public synchronized void setCloseDelay(final int value) {
 
         closeDelay = value;
     }
 
     public boolean getLogIndexChanges() {
 
         return logIndexChanges;
     }
 
     public synchronized void setLog(final int level) throws SQLException {
 
         if (logLevel == level) { return; }
         boolean logData;
         boolean logIndex;
         switch (level) {
             case 0:
                 logData = false;
                 logIndex = false;
                 break;
             case 1:
                 logData = true;
                 logIndex = false;
                 break;
             case 2:
                 logData = true;
                 logIndex = true;
                 break;
             default:
                 throw Message.throwInternalError("level=" + level);
         }
         if (fileIndex != null) {
             fileIndex.setLogChanges(logIndex);
         }
         logIndexChanges = logIndex;
         if (log != null) {
             log.setDisabled(!logData);
             log.checkpoint();
         }
         traceSystem.getTrace(Trace.DATABASE).error("SET LOG " + level, null);
         logLevel = level;
     }
 
     public boolean getRecovery() {
 
         return recovery;
     }
 
     public Session getSystemSession() {
 
         return systemSession;
     }
 
     @Override
     public void handleInvalidChecksum() throws SQLException {
 
         final SQLException e = Message.getSQLException(ErrorCode.FILE_CORRUPTED_1, "wrong checksum");
         if (!recovery) { throw e; }
         traceSystem.getTrace(Trace.DATABASE).error("recover", e);
     }
 
     /**
      * Check if the database is in the process of closing.
      *
      * @return true if the database is closing
      */
     public boolean isClosing() {
 
         return closing;
     }
 
     public void setMaxLengthInplaceLob(final int value) {
 
         maxLengthInplaceLob = value;
     }
 
     @Override
     public int getMaxLengthInplaceLob() {
 
         return persistent ? maxLengthInplaceLob : Integer.MAX_VALUE;
     }
 
     public void setIgnoreCase(final boolean b) {
 
         ignoreCase = b;
     }
 
     public boolean getIgnoreCase() {
 
         if (starting) {
             // tables created at startup must not be converted to ignorecase
             return false;
         }
         return ignoreCase;
     }
 
     public synchronized void setDeleteFilesOnDisconnect(final boolean b) {
 
         deleteFilesOnDisconnect = b;
     }
 
     @Override
     public String getLobCompressionAlgorithm(final int type) {
 
         return lobCompressionAlgorithm;
     }
 
     public void setLobCompressionAlgorithm(final String stringValue) {
 
         lobCompressionAlgorithm = stringValue;
     }
 
     /**
      * Called when the size if the data or index file has been changed.
      *
      * @param length
      *            the new file size
      */
     public void notifyFileSize(final long length) {
 
         // ignore
     }
 
     public synchronized void setMaxLogSize(final long value) {
 
         getLog().setMaxLogSize(value);
     }
 
     public void setAllowLiterals(final int value) {
 
         allowLiterals = value;
     }
 
     public boolean getOptimizeReuseResults() {
 
         return optimizeReuseResults;
     }
 
     public void setOptimizeReuseResults(final boolean b) {
 
         optimizeReuseResults = b;
     }
 
     /**
      * Called when the summary of the index in the log file has become invalid. This method is only called if index changes are not logged,
      * and if an index has been changed.
      */
     public void invalidateIndexSummary() throws SQLException {
 
         if (indexSummaryValid) {
             indexSummaryValid = false;
 
             if (log == null) {
                 log = new LogSystem(this, databaseName, readOnly, accessModeLog, pageStore);
             }
             log.invalidateIndexSummary();
         }
     }
 
     public boolean getIndexSummaryValid() {
 
         return indexSummaryValid;
     }
 
     @Override
     public Object getLobSyncObject() {
 
         return lobSyncObject;
     }
 
     public int getSessionCount() {
 
         return userSessions.size();
     }
 
     public void setReferentialIntegrity(final boolean b) {
 
         referentialIntegrity = b;
     }
 
     public boolean getReferentialIntegrity() {
 
         return referentialIntegrity;
     }
 
     /**
      * Check if the database is currently opening. This is true until all stored SQL statements have been executed.
      *
      * @return true if the database is still starting
      */
     public boolean isStarting() {
 
         return starting;
     }
 
     /**
      * Called after the database has been opened and initialized. This method notifies the event listener if one has been set.
      */
     public void opened() {
 
         if (eventListener != null) {
             eventListener.opened();
         }
     }
 
     public void setMode(final Mode mode) {
 
         this.mode = mode;
     }
 
     public Mode getMode() {
 
         return mode;
     }
 
     public void setMaxOperationMemory(final int maxOperationMemory) {
 
         this.maxOperationMemory = maxOperationMemory;
     }
 
     public int getMaxOperationMemory() {
 
         return maxOperationMemory;
     }
 
     public Session getExclusiveSession() {
 
         return exclusiveSession;
     }
 
     public void setExclusiveSession(final Session session) {
 
         exclusiveSession = session;
     }
 
     @Override
     public boolean getLobFilesInDirectories() {
 
         return lobFilesInDirectories;
     }
 
     @Override
     public SmallLRUCache getLobFileListCache() {
 
         return lobFileListCache;
     }
 
     /**
      * Checks if the system table (containing the catalog) is locked.
      *
      * @return true if it is currently locked
      */
     public boolean isSysTableLocked() {
 
         return meta.isLockedExclusively();
     }
 
     /**
      * Open a new connection or get an existing connection to another database.
      *
      * @param driver
      *            the database driver or null
      * @param url
      *            the database URL
      * @param user
      *            the user name
      * @param password
      *            the password
      * @param clearLinkConnectionCache 
      * @return the connection
      */
     public TableLinkConnection getLinkConnection(final String driver, final String url, final String user, final String password, final boolean clearLinkConnectionCache) throws SQLException {
 
         if (linkConnections == null || clearLinkConnectionCache) {
             linkConnections = new HashMap<TableLinkConnection, TableLinkConnection>();
         }
 
         return TableLinkConnection.open(linkConnections, driver, url, user, password);
     }
 
     @Override
     public String toString() {
 
         return databaseShortName + ":" + super.toString();
     }
 
     /**
      * Immediately close the database.
      */
     public void shutdownImmediately() {
 
         setPowerOffCount(1);
         try {
             checkPowerOff();
         }
         catch (final SQLException e) {
             // ignore
         }
     }
 
     @Override
     public TempFileDeleter getTempFileDeleter() {
 
         return tempFileDeleter;
     }
 
     @Override
     public Trace getTrace() {
 
         return getTrace(Trace.DATABASE);
     }
 
     public PageStore getPageStore() throws SQLException {
 
         if (pageStore == null && SysProperties.PAGE_STORE) {
             pageStore = new PageStore(this, databaseName + Constants.SUFFIX_PAGE_FILE, accessModeData, SysProperties.CACHE_SIZE_DEFAULT);
             pageStore.open();
         }
         return pageStore;
     }
 
     /**
      * Redo a change in a table.
      *
      * @param tableId
      *            the object id of the table
      * @param row
      *            the row
      * @param add
      *            true if the record is added, false if deleted
      */
     public void redo(final int tableId, final Row row, final boolean add) throws SQLException {
 
         final TableData table = (TableData) tableMap.get(tableId);
         if (add) {
             table.addRow(systemSession, row);
         }
         else {
             table.removeRow(systemSession, row);
         }
         if (tableId == 0) {
             final MetaRecord m = new MetaRecord(row);
             if (add) {
                 objectIds.set(m.getId());
 
                 final TableProxyManager proxyManager = new TableProxyManager(this, systemSession, true);
 
                 m.execute(this, systemSession, eventListener, proxyManager);
             }
             else {
                 m.undo(this, systemSession, eventListener);
             }
         }
     }
 
     /**
      * Get the first user defined table.
      *
      * @return the table or null if no table is defined
      */
     public ReplicaSet getFirstUserTable() {
 
         final Set<ReplicaSet> list = getAllTables();
         for (final ReplicaSet replicaSet : list) {
 
             if (replicaSet.getCreateSQL() != null) { return replicaSet; }
         }
 
         return null;
     }
 
     public boolean isReconnectNeeded() {
 
         if (fileLockMethod != FileLock.LOCK_SERIALIZED) { return false; }
         final long now = System.currentTimeMillis();
         if (now < reconnectCheckNext) { return false; }
         reconnectCheckNext = now + SysProperties.RECONNECT_CHECK_DELAY;
         if (lock == null) {
             lock = new FileLock(traceSystem, databaseName + Constants.SUFFIX_LOCK_FILE, Constants.LOCK_SLEEP);
         }
         Properties prop;
         try {
             while (true) {
                 prop = lock.load();
                 if (prop.equals(reconnectLastLock)) { return false; }
                 if (prop.getProperty("changePending", null) == null) {
                     break;
                 }
                 getTrace().debug("delay (change pending)");
                 Thread.sleep(SysProperties.RECONNECT_CHECK_DELAY);
             }
             reconnectLastLock = prop;
         }
         catch (final Exception e) {
             getTrace().error("readOnly:" + readOnly, e);
             // ignore
         }
         return true;
     }
 
     /**
      * This method is called after writing to the database.
      */
     public void afterWriting() throws SQLException {
 
         if (fileLockMethod != FileLock.LOCK_SERIALIZED || readOnly) { return; }
         reconnectCheckNext = System.currentTimeMillis() + 1;
     }
 
     /**
      * Flush all changes when using the serialized mode, and if there are pending changes.
      */
     public void checkpointIfRequired() throws SQLException {
 
         if (fileLockMethod != FileLock.LOCK_SERIALIZED || readOnly || !reconnectChangePending) { return; }
         final long now = System.currentTimeMillis();
         if (now > reconnectCheckNext) {
             getTrace().debug("checkpoint");
             checkpoint();
             reconnectModified(false);
         }
     }
 
     /**
      * Flush all changes and open a new log file.
      */
     public void checkpoint() throws SQLException {
 
         if (SysProperties.PAGE_STORE) {
             pageStore.checkpoint();
         }
         getLog().checkpoint();
         getTempFileDeleter().deleteUnused();
     }
 
     /**
      * This method is called before writing to the log file.
      */
     public void beforeWriting() {
 
         if (fileLockMethod == FileLock.LOCK_SERIALIZED) {
             reconnectModified(true);
         }
     }
 
     /**
      * Get a database object.
      *
      * @param id
      *            the object id
      * @return the database object
      */
     DbObject getDbObject(final int id) {
 
         return databaseObjects.get(ObjectUtils.getInteger(id));
     }
 
     /**
      * H2O Creates H2O schema meta-data tables, including System Table tables if this machine is a System Table.
      *
      * @throws Exception
      * @throws SQLException
      */
     private void createH2OTables(final boolean persistedSchemaTablesExist, final boolean databaseExists) throws Exception {
 
         if (!databaseExists) {
             createSystemTableOrGetReferenceToIt(databaseExists, persistedSchemaTablesExist, true);
 
         }
 
         if (!persistedSchemaTablesExist) {
             try {
                 TableManager.createTableManagerTables(systemSession);
             }
             catch (final SQLException e) {
 
                 e.printStackTrace();
             }
         }
 
         systemTableRef.getSystemTable().addConnectionInformation(getID(), new DatabaseInstanceWrapper(getID(), databaseRemote.getLocalDatabaseInstance(), true));
 
     }
 
     public ISystemTableReference getSystemTableReference() {
 
         return systemTableRef;
     }
 
     public ISystemTableMigratable getSystemTable() {
 
         return systemTableRef.getSystemTable();
     }
 
     /**
      * @param dbLocation
      * @return
      */
     public boolean isLocal(final DatabaseInstanceWrapper dbLocation) {
 
         return dbLocation.getDatabaseInstance().equals(getLocalDatabaseInstance());
     }
 
     /**
      * Examples:
      * <ul>
      * <li><code>//mem:management_db_9081</code></li>
      * <li><code>//db_data/unittests/schema_test</code></li>
      * </ul>
      *
      * @return the databaseLocation
      */
     public String getDatabaseLocation() {
 
         return getID().getDbLocation();
     }
 
     /**
      * Is this database instance an H2 management database?
      *
      * @return
      */
     public boolean isManagementDB() {
 
         return databaseShortName.startsWith("MANAGEMENT_DB_");
     }
 
     /**
      * @return the localMachineAddress
      */
     public String getLocalMachineAddress() {
 
         return getID().getHostname();
     }
 
     /**
      * @return the localMachinePort
      */
     public int getLocalMachinePort() {
 
         return getID().getPort();
     }
 
     /**
      * Gets the full address of the database - i.e. one that can be used to connect to it remotely through JDBC. An example path:
      * jdbc:h2:sm:tcp://localhost:9090/db_data/one/test_db
      *
      * @return
      */
     public String getFullDatabasePath() {
 
         // String isTCP = (getLocalMachinePort() == -1 &&
         // getDatabaseLocation().contains("mem"))? "": "tcp:";
         //
         // String url = "";
         // if (isTCP.equals("tcp:")){
         // url = getLocalMachineAddress() + ":" + getLocalMachinePort() + "/";
         // }
         //
         // return "jdbc:h2:" + ((systemTableRef.isSystemTableLocal())? "sm:":
         // "") + isTCP + url + getDatabaseLocation();
 
         return databaseRemote.getLocalMachineLocation().getURL();
     }
 
     /**
      * Returns the type of connection this database is open on (e.g. tcp, mem).
      *
      * @return
      */
     public String getConnectionType() {
 
         return getID().getConnectionType();
     }
 
     /**
      * @param replicaLocationString
      * @return
      */
 
     public IDatabaseInstanceRemote getDatabaseInstance(final DatabaseID databaseURL) {
 
         try {
             return systemTableRef.getSystemTable().getDatabaseInstance(databaseURL);
         }
         catch (final RPCException e) {
             e.printStackTrace();
         }
         catch (final MovedException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     /**
      * @return
      */
     public IDatabaseInstanceRemote getLocalDatabaseInstance() {
 
         return databaseRemote.getLocalDatabaseInstance();
     }
 
     /**
      * @return
      */
     public DatabaseInstanceWrapper getLocalDatabaseInstanceInWrapper() {
 
         return new DatabaseInstanceWrapper(getID(), databaseRemote.getLocalDatabaseInstance(), true);
     }
 
     public void removeLocalDatabaseInstance() {
 
         try {
 
             getLocalDatabaseInstance().setAlive(false);
             // this.systemTableRef.getSystemTable(true).removeConnectionInformation(this.databaseRemote.getLocalDatabaseInstance());
             // new
             // RemoveConnectionInfo(this.systemTableRef.getSystemTable(true),
             // this.databaseRemote.getLocalDatabaseInstance()).start();
         }
         catch (final Exception e) {
             // An error here isn't critical.
         }
 
     }
 
     /**
      *
      */
     public IDatabaseRemote getRemoteInterface() {
 
         return databaseRemote;
     }
 
     /**
      *
      */
     public IChordInterface getChordInterface() {
 
         return databaseRemote;
     }
 
     /**
      * @return
      */
     public Session getH2OSession() {
 
         return h2oSession;
     }
 
     /**
      * @return
      */
     public int getUserSessionsSize() {
 
         return userSessions.size();
     }
 
     public Settings getDatabaseSettings() {
 
         return databaseSettings;
     }
 
     public TransactionNameGenerator getTransactionNameGenerator() {
 
         return transactionNameGenerator;
     }
 
     public synchronized boolean isRunning() {
 
         return running;
     }
 
     public Set<String> getLocalSchema() {
 
         return localSchema;
     }
 
     public boolean isTableLocal(final Schema schema) {
 
         return localSchema.contains(schema.getName());
     }
 
     private void setDiagnosticLevel(final DatabaseID localMachineLocation) {
 
         final H2OPropertiesWrapper databaseProperties = H2OPropertiesWrapper.getWrapper(localMachineLocation);
         try {
             databaseProperties.loadProperties();
         }
         catch (final IOException e) {
             ErrorHandling.exceptionErrorNoEvent(e, "Failed to load database properties for database: " + localMachineLocation);
         }
 
         redirectSystemOut(databaseProperties);
         redirectSystemErr(databaseProperties);
 
         final String diagnosticLevel = databaseProperties.getProperty("diagnosticLevel");
 
         if (diagnosticLevel != null) {
 
             Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Setting diagnostic level to " + diagnosticLevel);
 
             if (diagnosticLevel.equals("FINAL")) {
                 Diagnostic.setLevel(DiagnosticLevel.FINAL);
             }
             else if (diagnosticLevel.equals("NONE")) {
                 Diagnostic.setLevel(DiagnosticLevel.NONE);
             }
             else if (diagnosticLevel.equals("INIT")) {
                 Diagnostic.setLevel(DiagnosticLevel.INIT);
             }
             else if (diagnosticLevel.equals("FULL")) {
                 Diagnostic.setLevel(DiagnosticLevel.FULL);
             }
         }
 
         Diagnostic.addIgnoredPackage("uk.ac.standrews.cs.stachord");
         Diagnostic.addIgnoredPackage("uk.ac.standrews.cs.nds");
         Diagnostic.addIgnoredPackage("uk.ac.standrews.cs.numonic");
         // Diagnostic.addIgnoredPackage("org.h2o.autonomic.numonic.ranking");
         Diagnostic.setTimestampFlag(true);
         Diagnostic.setTimestampFormat(new SimpleDateFormat("HH:mm:ss:SSS "));
         Diagnostic.setTimestampDelimiterFlag(false);
 
         ErrorHandling.setTimestampFlag(false);
     }
 
     public void redirectSystemOut(final H2OPropertiesWrapper databaseProperties) {
 
         final String sysOutFileLocation = databaseProperties.getProperty("sysOutLocation");
 
         if (sysOutFileLocation != null) {
             try {
                 final File sysOutFile = new File(sysOutFileLocation);
 
                 if (!sysOutFile.exists()) {
                     final File parentFolder = sysOutFile.getParentFile();
                     if (!parentFolder.exists()) {
                         parentFolder.mkdirs();
                     }
                     sysOutFile.createNewFile();
                 }
 
                 final PrintStream printStream = new PrintStream(new FileOutputStream(sysOutFile, true));
 
                 System.setOut(printStream);
             }
             catch (final IOException e) {
                 ErrorHandling.exceptionErrorNoEvent(e, "Failed to redirect System.out messages to file located at: " + sysOutFileLocation);
             }
         }
     }
 
     public void redirectSystemErr(final H2OPropertiesWrapper databaseProperties) {
 
         final String sysErrFileLocation = databaseProperties.getProperty("sysErrLocation");
 
         if (sysErrFileLocation != null) {
             try {
                 final File sysErrFile = new File(sysErrFileLocation);
 
                 if (!sysErrFile.exists()) {
                     final File parentFolder = sysErrFile.getParentFile();
                     if (!parentFolder.exists()) {
                         parentFolder.mkdirs();
                     }
                     sysErrFile.createNewFile();
                 }
 
                 final PrintStream printStream = new PrintStream(new FileOutputStream(sysErrFile, true));
 
                 System.setErr(printStream);
             }
             catch (final IOException e) {
                 ErrorHandling.exceptionErrorNoEvent(e, "Failed to redirect System.out messages to file located at: " + databaseProperties.getProperty("systemOutLocation"));
             }
         }
     }
 
     public AsynchronousQueryManager getAsynchronousQueryManager() {
 
         return asynchronousQueryManager;
     }
 
     public TableManagerInstanceServer getTableManagerServer() {
 
         return table_manager_instance_server;
     }
 
     public SystemTableServer getSystemTableServer() {
 
         return system_table_server;
     }
 
     public DatabaseInstanceServer getDatabaseInstanceServer() {
 
         return database_instance_server;
     }
 
     public INumonic getNumonic() {
 
         return numonic;
     }
 
     @Override
     public void update(final Observable o, final Object arg) {
 
         final Threshold threshold = ThresholdChecker.getThresholdObject(arg);
 
         if (threshold.resourceName == ResourceType.CPU_USER && threshold.above) {
             //CPU utilization has been exceeded.
 
             //Act on this.
         }
     }
 
     @Override
     public boolean isConnected() {
 
         return connected;
     }
 
     @Override
     public void setConnected(final boolean connected) {
 
         this.connected = connected;
 
     }
 
 }
