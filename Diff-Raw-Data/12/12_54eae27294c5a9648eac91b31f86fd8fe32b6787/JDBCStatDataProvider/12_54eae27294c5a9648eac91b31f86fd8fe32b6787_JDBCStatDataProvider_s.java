 package com.tehbeard.beardstat.dataproviders;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.annotation.Annotation;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.MatchResult;
 
 import com.tehbeard.beardstat.BeardStatRuntimeException;
 import com.tehbeard.beardstat.DatabaseConfiguration;
 import com.tehbeard.beardstat.DbPlatform;
 import com.tehbeard.beardstat.containers.EntityStatBlob;
 import com.tehbeard.beardstat.containers.IStat;
 import com.tehbeard.beardstat.containers.StatBlobRecord;
 import com.tehbeard.beardstat.containers.documents.docfile.DocumentFile;
 import com.tehbeard.beardstat.containers.documents.docfile.DocumentFileRef;
 import com.tehbeard.beardstat.dataproviders.identifier.IdentifierService;
 import com.tehbeard.beardstat.dataproviders.metadata.CategoryMeta;
 import com.tehbeard.beardstat.dataproviders.metadata.DomainMeta;
 import com.tehbeard.beardstat.dataproviders.metadata.StatisticMeta;
 import com.tehbeard.beardstat.dataproviders.metadata.StatisticMeta.Formatting;
 import com.tehbeard.beardstat.dataproviders.metadata.WorldMeta;
 import com.tehbeard.utils.misc.CallbackMatcher;
 import com.tehbeard.utils.misc.CallbackMatcher.Callback;
 import com.tehbeard.utils.mojang.api.profiles.HttpProfileRepository;
 import com.tehbeard.utils.mojang.api.profiles.Profile;
 import com.tehbeard.utils.mojang.api.profiles.ProfileCriteria;
 //import org.bukkit.Bukkit;
 //import org.bukkit.ChatColor;
 
 /**
  * base class for JDBC based data providers Allows easy development of data providers that make use of JDBC
  *
  * @author James
  *
  */
 public abstract class JDBCStatDataProvider implements IStatDataProvider {
 
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.METHOD)
     public @interface dbVersion {
 
         int value();
     }
 
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.METHOD)
     public @interface preUpgrade {
     }
 
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.METHOD)
     public @interface postUpgrade {
     }
     //Maintenence scripts
     public static final String SQL_METADATA_CATEGORY = "sql/maintenence/metadata/category";
     public static final String SQL_METADATA_STATISTIC = "sql/maintenence/metadata/statistic";
     public static final String SQL_METADATA_STATIC_STATS = "sql/maintenence/metadata/staticstats";
     public static final String SQL_CREATE_TABLES = "sql/maintenence/create.tables";
     public static final String SQL_KEEP_ALIVE = "sql/maintenence/keepAlive";
     //Entity scripts
     public static final String SQL_SAVE_ENTITY = "sql/entity/saveEntity";
     public static final String SQL_SAVE_STAT = "sql/entity/saveStat";
     public static final String SQL_LOAD_ENTITY_DATA = "sql/entity/getEntityData";
     //Component scripts
     public static final String SQL_LOAD_DOMAINS = "sql/components/load/getDomains";
     public static final String SQL_LOAD_WORLDS = "sql/components/load/getWorlds";
     public static final String SQL_LOAD_CATEGORIES = "sql/components/load/getCategories";
     public static final String SQL_LOAD_STATISTICS = "sql/components/load/getStatistics";
     public static final String SQL_SAVE_DOMAIN = "sql/components/save/saveDomain";
     public static final String SQL_SAVE_WORLD = "sql/components/save/saveWorld";
     public static final String SQL_SAVE_CATEGORY = "sql/components/save/saveCategory";
     public static final String SQL_SAVE_STATISTIC = "sql/components/save/saveStatistic";
     //Connection
     protected Connection conn;
     // Load components
     protected PreparedStatement loadDomainsList;
     protected PreparedStatement loadWorldsList;
     protected PreparedStatement loadCategoriesList;
     protected PreparedStatement loadStatisticsList;
     // save components
     protected PreparedStatement saveDomain;
     protected PreparedStatement saveWorld;
     protected PreparedStatement saveCategory;
     protected PreparedStatement saveStatistic;
     // Load data from db
     protected PreparedStatement loadEntityData;
     // save to db
     protected PreparedStatement saveEntity;
     protected PreparedStatement saveEntityData;
     // Maintenance
     protected PreparedStatement keepAlive;
     protected PreparedStatement deleteEntity;
     protected PreparedStatement createTable;
 
     //Utility
     protected PreparedStatement setUUID;
     // default connection related configuration
     protected String connectionUrl = "";
     protected Properties connectionProperties = new Properties();
     protected String tblPrefix = "stats";
     private String scriptSuffix = "sql";
     // ID Cache
     private final Map<String, DomainMeta> domainMetaMap = new HashMap<String, DomainMeta>();
     private final Map<String, WorldMeta> worldMetaMap = new HashMap<String, WorldMeta>();
     private final Map<String, CategoryMeta> categoryMetaMap = new HashMap<String, CategoryMeta>();
     private final Map<String, StatisticMeta> statisticMetaMap = new HashMap<String, StatisticMeta>();
     // Write queue
     private HashMap<Integer, StatBlobRecord> writeCache = new HashMap<Integer, StatBlobRecord>();
     //Configuration/env
     protected DbPlatform platform;
     protected DatabaseConfiguration config;
     
     public JDBCStatDataProvider(DbPlatform platform, String scriptSuffix, String driverClass, DatabaseConfiguration config) {
         try {
             this.connectionProperties.put("allowMultiQuery", "true");
             this.scriptSuffix = scriptSuffix;
             this.platform = platform;
             Class.forName(driverClass);// load driver
         } catch (ClassNotFoundException ex) {
             throw new BeardStatRuntimeException("Could not locate driver library.", ex, false);
         }
         this.config = config;
     }
 
     /**
      * Boots up the data provider, this entails:
      * <ol>
      * <li>Open connection</li>
      * <li>Migration check</li>
      * <li>Create tables</li>
      * <li>Load SQL statements</li>
      * <li>Update metadata tables</li>
      * <li>Cache data as needed</li>
      * </ol>
      *
      * @throws BeardStatRuntimeException
      */
     protected void initialise() throws BeardStatRuntimeException {
         try {
             createConnection();
 
             checkForMigration();
 
             checkAndMakeTable();
             prepareStatements();
 
             executeScript(SQL_METADATA_CATEGORY);
             executeScript(SQL_METADATA_STATISTIC);
             executeScript(SQL_METADATA_STATIC_STATS);
 
 
             cacheComponents();
         } catch (SQLException ex) {
             throw new BeardStatRuntimeException("Error during init", ex, false);
         }
     }
 
     /**
      * checks config in data folder against default (current versions config) If version conflicts it will attempt to run migration scripts sequentially to upgrade
      *
      * @throws SQLException
      */
     private void checkForMigration() {
 
         int installedVersion = config.version;
 
         if (!platform.configValueIsSet("stats.database.sql_db_version")) {
             platform.configValueSet("stats.database.sql_db_version", 1);
             platform.saveConfig();
             installedVersion = 1;
         }
 
 
         if (installedVersion > config.latestVersion) {
             throw new RuntimeException("database version is higher than the one this version of BeardStat knows, You appear to be running an out of date plugin!");
         }
 
         if (installedVersion < config.latestVersion) {
             // Swap to transaction based mode,
             // Execute each migration script in sequence,
             // commit if successful,
             // rollback and error out if not
             // Should support partial recovery of migration effort, saves
             // current version if successful commit
 
             this.platform.getLogger().info("Updating database to latest version");
             this.platform.getLogger().info("Your database: " + installedVersion +" latest: " + config.latestVersion);
 
             if (config.backups) {
                 try {
                     this.platform.getLogger().info("Creating database backup, if shit hits the fan and the rollback fails, you can use this.");
                     File f = new File(platform.getDataFolder(), "backup." + scriptSuffix);
                     f.delete();
 
                     f.createNewFile();
 
                     generateBackup(f);
                 } catch (IOException ex) {
                     Logger.getLogger(JDBCStatDataProvider.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
 
             for (int i = 0; i < 3; i++) {
                 platform.getLogger().warning("WARNING: DATABASE MIGRATION WILL TAKE A LONG TIME ON LARGE DATABASES.");
             }
             int migrateToVersion = 0;
             try {
                 this.conn.setAutoCommit(false);
 
                 for (migrateToVersion = installedVersion + 1; migrateToVersion <= config.latestVersion; migrateToVersion++) {
 
                     Map<String, String> k = new HashMap<String, String>();
 
                     //Run premigration method
                     try {
                         runCodeFor(migrateToVersion, preUpgrade.class);
                     } catch (InvocationTargetException ex) {
                         if (ex.getCause() instanceof SQLException) {
                             this.platform.mysqlError((SQLException) ex.getCause(), "@CLASS/PREUPGRADE/" + migrateToVersion);
                             throw (SQLException) ex.getCause();
                         }
                     } catch (IllegalAccessException ex) {
                         Logger.getLogger(JDBCStatDataProvider.class.getName()).log(Level.SEVERE, null, ex);
                         throw new SQLException("IllegalAccessException encountered", ex);
                     } catch (IllegalArgumentException ex) {
                         Logger.getLogger(JDBCStatDataProvider.class.getName()).log(Level.SEVERE, null, ex);
                         throw new SQLException("IllegalArgumentException encountered", ex);
                     }
 
                     //Run script
                     try {
                         executeScript("sql/maintenence/migration/migrate." + migrateToVersion, k);
                     } catch (SQLException ex) {
                         this.platform.mysqlError(ex, "sql/maintenence/migration/migrate." + migrateToVersion);
                         throw ex;
                     }
 
                     //run post migration method
                     try {
                         runCodeFor(migrateToVersion, postUpgrade.class);
                     } catch (InvocationTargetException ex) {
                         if (ex.getCause() instanceof SQLException) {
                             this.platform.mysqlError((SQLException) ex.getCause(), "@CLASS/POSTUPGRADE/" + migrateToVersion);
                             throw (SQLException) ex.getCause();
                         }
                     } catch (IllegalAccessException ex) {
                         Logger.getLogger(JDBCStatDataProvider.class.getName()).log(Level.SEVERE, null, ex);
                         throw new SQLException("IllegalAccessException encountered", ex);
                     } catch (IllegalArgumentException ex) {
                         Logger.getLogger(JDBCStatDataProvider.class.getName()).log(Level.SEVERE, null, ex);
                         throw new SQLException("IllegalArgumentException encountered", ex);
                     }
                     this.conn.commit();
                     platform.configValueSet("stats.database.sql_db_version", migrateToVersion);
                     platform.saveConfig();
 
                 }
 
             } catch (SQLException e) {
 
                 this.platform.getLogger().log(Level.SEVERE, "An error occured while migrating the database, initiating rollback to version {0}", (migrateToVersion - 1));
                 try {
                     this.conn.rollback();
                     throw new BeardStatRuntimeException("Failed to migrate database", e, false);
                 } catch (SQLException se) {
                     this.platform.getLogger().severe("Failed to rollback");
                     platform.mysqlError(se, null);
                 }
 
 
             }
 
             this.platform.getLogger().info("Migration successful");
             try {
                 this.conn.setAutoCommit(true);
             } catch (SQLException e) {
                 throw new BeardStatRuntimeException("Failed to start autocommit", e, false);
             }
 
         }
     }
 
     /**
      * Connects to the database
      *
      * @throws SQLException
      */
     private void createConnection() {
 
         this.platform.getLogger().info("Connecting....");
 
         try {
             this.conn = DriverManager.getConnection(this.connectionUrl, this.connectionProperties);
 
             // conn.setAutoCommit(false);
         } catch (SQLException e) {
             this.platform.mysqlError(e, null);
             this.conn = null;
         }
 
     }
 
     /**
      * Returns true if connection is still there.
      *
      * @return
      */
     private synchronized boolean checkConnection() {
         this.platform.getLogger().fine("Checking connection");
         try {
             if ((this.conn == null) || !this.conn.isValid(0)) {
                 this.platform.getLogger().fine("Something is derp, rebooting connection.");
                 createConnection();
                 if (this.conn != null) {
                     this.platform.getLogger().fine("Rebuilding statements");
                     prepareStatements();
                 } else {
                     this.platform.getLogger().fine("Reboot failed!");
                 }
 
             }
         } catch (SQLException e) {
             this.conn = null;
             return false;
         } catch (AbstractMethodError e) {
             //Catch SQLite error??
         }
 
         return this.conn != null;
     }
 
     /**
      * Constructs the tables.
      */
     protected void checkAndMakeTable() throws SQLException {
         this.platform.getLogger().info("Constructing missing tables.");
         executeScript(SQL_CREATE_TABLES);
     }
 
     /**
      * Load statements from jar
      */
     protected void prepareStatements() {
         this.platform.getLogger().config("Preparing statements");
 
         this.loadEntityData = getStatementFromScript(SQL_LOAD_ENTITY_DATA);
 
         // Load components
         this.loadDomainsList = getStatementFromScript(SQL_LOAD_DOMAINS);
         this.loadWorldsList = getStatementFromScript(SQL_LOAD_WORLDS);
         this.loadCategoriesList = getStatementFromScript(SQL_LOAD_CATEGORIES);
         this.loadStatisticsList = getStatementFromScript(SQL_LOAD_STATISTICS);
 
         // save components
         this.saveDomain = getStatementFromScript(SQL_SAVE_DOMAIN, Statement.RETURN_GENERATED_KEYS);
         this.saveWorld = getStatementFromScript(SQL_SAVE_WORLD, Statement.RETURN_GENERATED_KEYS);
         this.saveCategory = getStatementFromScript(SQL_SAVE_CATEGORY,
                 Statement.RETURN_GENERATED_KEYS);
         this.saveStatistic = getStatementFromScript(SQL_SAVE_STATISTIC,
                 Statement.RETURN_GENERATED_KEYS);
 
         // save to db
         this.saveEntity = getStatementFromScript(SQL_SAVE_ENTITY, Statement.RETURN_GENERATED_KEYS);
         this.saveEntityData = getStatementFromScript(SQL_SAVE_STAT);
 
         // Maintenance
         this.keepAlive = getStatementFromScript(SQL_KEEP_ALIVE);
         // deleteEntity =
         // conn.prepareStatement(platform.readSQL(type,"sql/maintenence/deletePlayerFully",
         // tblPrefix));
         
         this.setUUID = getStatementFromScript("sql/save/setUUID");
 
         this.platform.getLogger().config("Set player stat statement created");
     }
 
     /**
      * Cache entries for quicker resolvement on our end.
      */
     public void cacheComponents() {
         ResultSet rs;
         try {
             //Domains
             rs = loadDomainsList.executeQuery();
             domainMetaMap.clear();
             while (rs.next()) {
                 DomainMeta dm = new DomainMeta(
                         rs.getInt("domainId"),
                         rs.getString("domain"));
                 domainMetaMap.put(rs.getString("domain"), dm);
             }
             rs.close();
         } catch (SQLException e) {
             this.platform.mysqlError(e, SQL_LOAD_DOMAINS);
         }
         try {
             //Worlds
             rs = loadWorldsList.executeQuery();
             worldMetaMap.clear();
             while (rs.next()) {
                 WorldMeta wm = new WorldMeta(
                         rs.getInt("worldId"),
                         rs.getString("world"),
                         rs.getString("name"));
                 worldMetaMap.put(rs.getString("world"), wm);
             }
             rs.close();
         } catch (SQLException e) {
             this.platform.mysqlError(e, SQL_LOAD_WORLDS);
         }
         try {
             //Worlds
             rs = loadCategoriesList.executeQuery();
             categoryMetaMap.clear();
             while (rs.next()) {
                 CategoryMeta cm = new CategoryMeta(
                         rs.getInt("categoryId"),
                         rs.getString("category"),
                         rs.getString("statwrapper"));
                 categoryMetaMap.put(rs.getString("category"), cm);
             }
             rs.close();
         } catch (SQLException e) {
             this.platform.mysqlError(e, SQL_LOAD_CATEGORIES);
         }
         try {
             //Worlds
             rs = loadStatisticsList.executeQuery();
             statisticMetaMap.clear();
             while (rs.next()) {
                 StatisticMeta sm = new StatisticMeta(
                         rs.getInt("statisticId"),
                         rs.getString("statistic"),
                         rs.getString("name"),
                         Formatting.valueOf(rs.getString("formatting")));
                 statisticMetaMap.put(rs.getString("statistic"), sm);
             }
             rs.close();
 
         } catch (SQLException e) {
             this.platform.mysqlError(e, SQL_LOAD_STATISTICS);
         }
 
     }
 
     @Override
     public ProviderQueryResult[] queryDatabase(ProviderQuery query) {
         if (query.name == null && query.type == null && query.uuid == null) {
             throw new IllegalStateException("Invalid ProviderQuery passed.");
         }
         String sql = "SELECT `entityId`,`name`,`type`,`uuid` FROM `" + tblPrefix + "_entity` WHERE ";
         boolean addAnd = false;
         if (query.name != null) {
             if (query.likeName) {
                 sql += "`name` LIKE ? ";
                 addAnd = true;
             } else {
                 sql += "`name`=? ";
                 addAnd = true;
             }
         }
         if (query.type != null) {
             if (addAnd) {
                 sql += "AND ";
             }
             sql += "`type`=? ";
             addAnd = true;
         }
         if (query.uuid != null) {
             if (addAnd) {
                 sql += "AND ";
             }
             sql += "`uuid`=? ";
         }
         try {
             PreparedStatement qryStmt = conn.prepareStatement(sql);
             int colId = 1;
             if (query.name != null) {
                 String sqlName = (query.likeName ? "%" : "") + query.name + (query.likeName ? "%" : "");
                 qryStmt.setString(colId, sqlName);
                 colId++;
             }
             if (query.type != null) {
                 qryStmt.setString(colId, query.type);
                 colId++;
             }
             if (query.uuid != null) {
                 qryStmt.setString(colId, query.uuid);
                 colId++;
             }
             ResultSet rs = qryStmt.executeQuery();
             List<ProviderQueryResult> results = new ArrayList<ProviderQueryResult>();
             while (rs.next()) {
                 results.add(new ProviderQueryResult(
                         rs.getInt("entityId"),
                         rs.getString("name"),
                         rs.getString("type"),
                         rs.getString("uuid") == null ? null : rs.getString("uuid")));
             }
             rs.close();
             return results.toArray(new ProviderQueryResult[0]);
 
         } catch (SQLException e) {
             platform.mysqlError(e, "AUTOGEN: " + sql);
         }
         return new ProviderQueryResult[0];
     }
 
     @Override
     public EntityStatBlob pullEntityBlob(ProviderQuery query) {
         try {
             if (!checkConnection()) {
                 platform.getLogger().severe("Database connection error!");
                 return null;
             }
             long t1 = (new Date()).getTime();
             ProviderQueryResult result = getSingleEntity(query);
             EntityStatBlob esb = null;
             ResultSet rs;
 
             if (result != null) {
 
                 esb = new EntityStatBlob(result.name, result.dbid, result.type, result.type, this);//Create the damn esb
                 // load all stats data
                 loadEntityData.setInt(1, esb.getEntityID());
                 rs = loadEntityData.executeQuery();
 
                 while (rs.next()) {
                     // `domain`,`world`,`category`,`statistic`,`value`
                     IStat ps = esb.getStat(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
                     ps.setValue(rs.getInt(5));
                     ps.clearArchive();
                 }
                 rs.close();
             } else if (result == null && query.create) {
 
 
                 saveEntity.setString(1, query.name);
                 saveEntity.setString(2, query.type);
                 saveEntity.setString(3, query.uuid == null ? "" : query.uuid.toString());
                 saveEntity.executeUpdate();
                 rs = saveEntity.getGeneratedKeys();
                 rs.next();// load player id
 
                 // make the player object, close out result set.
                 esb = new EntityStatBlob(query.name, rs.getInt(1), query.type, query.uuid, this);
                 rs.close();
             }
             //Didn't get a esb, kill it.
             if (esb == null) {
                 return null;
             }
 
             platform.loadEvent(esb);
             platform.getLogger().log(Level.CONFIG, "time taken to retrieve: {0} Milliseconds", ((new Date()).getTime() - t1));
             return esb;
         } catch (SQLException e) {
             platform.mysqlError(e, SQL_LOAD_ENTITY_DATA);
         }
         return null;
     }
 
     protected ProviderQueryResult getSingleEntity(ProviderQuery query) throws IllegalStateException {
         ProviderQueryResult[] results = queryDatabase(query);
         if (results.length > 1) {
             throw new IllegalStateException("Invalid Query provided, more than one entity returned.");
         }
         return results.length == 1 ? results[0] : null;
     }
 
     @Override
     public boolean hasEntityBlob(ProviderQuery query) {
         return queryDatabase(query).length > 1;
     }
 
     @Override
     public boolean deleteEntityBlob(EntityStatBlob blob) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public void pushEntityBlob(EntityStatBlob player) {
 
         synchronized (this.writeCache) {
 
             StatBlobRecord copy = player.cloneForArchive();
 
             if (!this.writeCache.containsKey(player.getName())) {
                 this.writeCache.put(copy.entityId, copy);
             }
         }
 
     }
     /**
      * Runner used to flush to database async.
      */
     private Runnable flush = new Runnable() {
         @Override
         public void run() {
             synchronized (writeCache) {
                 try {
                     keepAlive.execute();
                 } catch (SQLException e1) {
                 }
 
                 if (!checkConnection()) {
                     platform.getLogger().warning("Could not restablish connection, will try again later, WARNING: CACHE WILL GROW WHILE THIS HAPPENS");
                 } else {
                     platform.getLogger().config("Saving to database");
                     for (Entry<Integer, StatBlobRecord> entry : writeCache
                             .entrySet()) {
 
                         StatBlobRecord updateRecord = entry.getValue();
                         IStat stat = null;
                         try {
                             saveEntityData.clearBatch();
                             for (Iterator<IStat> it = updateRecord.stats.iterator(); it.hasNext();) {
                                 stat = it.next();
                                 saveEntityData.setInt(1, updateRecord.entityId);
                                 saveEntityData.setInt(2, getDomain(stat.getDomain(),true).getDbId());
                                 saveEntityData.setInt(3, getWorld(stat.getWorld(),true).getDbId());
                                 saveEntityData.setInt(4, getCategory(stat.getCategory(),true).getDbId());
                                 saveEntityData.setInt(5, getStatistic(stat.getStatistic(),true).getDbId());
                                 saveEntityData.setInt(6, stat.getValue());
                                 saveEntityData.addBatch();
                             }
                             saveEntityData.executeBatch();
 
                             for (DocumentFileRef ref : updateRecord.files) {
                                 try {
                                     DocumentFile newDoc = pushDocument(updateRecord.entityId, ref.getRef());
                                     ref.getRef().invalidateDocument();
                                     ref.setRef(newDoc);
                                 } catch (RevisionMismatchException ex) {
                                     ref.invalidateRef();
                                     platform.getLogger().log(Level.SEVERE, "Document {0}:{1} failed to save.", new Object[]{ref.getRef().getDomain(), ref.getRef().getKey()});
                                     platform.getLogger().severe("Another process has stored a new revision at this address.");
                                     platform.getLogger().severe("No Revision Merge strategy found. Changes not saved.");
                                 } catch (DocumentTooLargeException e) {
                                     platform.getLogger().log(Level.SEVERE, "Document {0}:{1} failed to save.", new Object[]{ref.getRef().getDomain(), ref.getRef().getKey()});
                                     platform.getLogger().severe("The document was too large to save to the database.");
                                 }
                             }
 
                         } catch (SQLException e) {
                             platform.getLogger().log(Level.WARNING, "entity id: {0}}", new Object[]{updateRecord.entityId});
                             platform.getLogger().log(Level.WARNING, "domain: {0} :: {1}", new Object[]{stat.getDomain(), getDomain(stat.getDomain(),true).getDbId()});
                             platform.getLogger().log(Level.WARNING, "world: {0} :: {1}", new Object[]{stat.getWorld(), getWorld(stat.getWorld(),true).getDbId()});
                             platform.getLogger().log(Level.WARNING, "category: {0} :: {1}", new Object[]{stat.getCategory(), getCategory(stat.getCategory(),true).getDbId()});
                             platform.getLogger().log(Level.WARNING, "statistic: {0} :: {1}", new Object[]{stat.getStatistic(), getStatistic(stat.getStatistic(),true).getDbId()});
                             platform.getLogger().log(Level.WARNING, "Value: {0}", stat.getValue());
                             platform.mysqlError(e, SQL_SAVE_STAT);
                             checkConnection();
                         }
                     }
                     platform.getLogger().config("Clearing write cache");
                     writeCache.clear();
                 }
             }
 
         }
     };
 
     @Override
     public void flushSync() {
         this.platform.getLogger().info("Flushing in main thread! Game will lag!");
         this.flush.run();
         this.platform.getLogger().info("Flushed!");
     }
 
     @Override
     public void flush() {
 
         new Thread(this.flush).start();
     }
 
     public void executeScript(String scriptName) throws SQLException {
         executeScript(scriptName, new HashMap<String, String>());
     }
 
     /**
      * Execute a script
      *
      * @param scriptName name of script (sql/load/loadEntity)
      * @param keys (list of non-standard keys ${KEY_NAME} to replace)
      *
      * Scripts support # for status comments and #!/script/path/here to execute subscripts
      * @throws SQLException
      */
     public void executeScript(String scriptName, final Map<String, String> keys) throws SQLException {
         CallbackMatcher matcher = new CallbackMatcher("\\$\\{([A-Za-z0-9_]*)\\}");
 
         String[] sqlStatements = readSQL(this.scriptSuffix, scriptName, this.tblPrefix).split("\\;");
         for (String s : sqlStatements) {
             String statement = matcher.replaceMatches(s, new Callback() {
                 @Override
                 public String foundMatch(MatchResult result) {
                     if (keys.containsKey(result.group(1))) {
                         return keys.get(result.group(1));
                     }
                     return "";
                 }
             });
 
             if (statement.startsWith("#!")) {
                 String subScript = statement.substring(2);
                 platform.getLogger().log(Level.INFO, "Executing : {0}", subScript);
                 executeScript(subScript, keys);
                 continue;
             } else if (statement.startsWith("#")) {
                 platform.getLogger().log(Level.INFO, "Status : {0}", statement.substring(1));
             } else {
 
                 this.conn.prepareStatement(statement).execute();
 
 
             }
         }
 
     }
 
     public PreparedStatement getStatementFromScript(String scriptName, int flags) {
         try {
             return this.conn.prepareStatement(readSQL(this.scriptSuffix, scriptName, this.tblPrefix), flags);
         } catch (SQLException ex) {
             this.platform.mysqlError(ex, scriptName);
             throw new BeardStatRuntimeException("Failed to create SQL statement for a script", ex, false);
         }
     }
 
     public PreparedStatement getStatementFromScript(String scriptName) {
         try {
             return this.conn.prepareStatement(readSQL(this.scriptSuffix, scriptName, this.tblPrefix));
         } catch (SQLException ex) {
             this.platform.mysqlError(ex, scriptName);
             throw new BeardStatRuntimeException("Failed to create SQL statement for a script", ex, false);
         }
     }
 
     @Override
     public DomainMeta getDomain(String gameTag, boolean create) {
         String qGameTag = sanitizeTag(gameTag);
         if (!domainMetaMap.containsKey(qGameTag) && create) {
             try {
 
                 saveDomain.setString(1, qGameTag);
                 saveDomain.execute();
                 ResultSet rs = saveDomain.getGeneratedKeys();
                 rs.next();
                 domainMetaMap.put(gameTag, new DomainMeta(rs.getInt(1), gameTag));
                 rs.close();
             } catch (SQLException ex) {
                 platform.mysqlError(ex, SQL_SAVE_DOMAIN);
             }
         }
 
         return domainMetaMap.get(qGameTag);
     }
 
     @Override
     public WorldMeta getWorld(String gameTag, boolean create) {
 
         if (!worldMetaMap.containsKey(gameTag) && create) {
             try {
                 saveWorld.setString(1, gameTag);
                 saveWorld.setString(2, gameTag.replaceAll("_", " "));
                 saveWorld.execute();
                 ResultSet rs = saveWorld.getGeneratedKeys();
                 rs.next();
                 worldMetaMap.put(gameTag, new WorldMeta(rs.getInt(1), gameTag, gameTag.replaceAll("_", " ")));
                 rs.close();
             } catch (SQLException ex) {
                 platform.mysqlError(ex, SQL_SAVE_WORLD + " @ " + gameTag + " cache size: " + worldMetaMap.size());
             }
         }
 
         return worldMetaMap.get(gameTag);
     }
 
     @Override
     public CategoryMeta getCategory(String gameTag, boolean create) {
         if (!categoryMetaMap.containsKey(gameTag) && create) {
             try {
                 saveCategory.setString(1, gameTag);
                 saveCategory.execute();
                 ResultSet rs = saveCategory.getGeneratedKeys();
                 rs.next();
                 categoryMetaMap.put(gameTag, new CategoryMeta(rs.getInt(1), gameTag, gameTag.replaceAll("_", " ")));
                 rs.close();
             } catch (SQLException ex) {
                 platform.mysqlError(ex, SQL_SAVE_CATEGORY);
             }
         }
 
         return categoryMetaMap.get(gameTag);
     }
 
     @Override
     public StatisticMeta getStatistic(String gameTag, boolean create) {
         if (!statisticMetaMap.containsKey(gameTag) && create) {
             try {
                 saveStatistic.setString(1, gameTag);
                 saveStatistic.setString(2, IdentifierService.getHumanName(gameTag));
                 saveStatistic.setString(3, Formatting.none.toString().toLowerCase());
                 saveStatistic.execute();
                 ResultSet rs = saveStatistic.getGeneratedKeys();
                 rs.next();
                 statisticMetaMap.put(gameTag, new StatisticMeta(rs.getInt(1), gameTag, gameTag.replaceAll("_", " "), Formatting.none));
                 rs.close();
             } catch (SQLException ex) {
                 platform.mysqlError(ex, SQL_SAVE_STATISTIC);
             }
         }
 
         return statisticMetaMap.get(gameTag);
     }
 
     private String sanitizeTag(String gameTag) {
         String truncatedName = gameTag.toLowerCase();
         if (truncatedName.length() > 64) {
             truncatedName = truncatedName.substring(0, 64);
         }
         return truncatedName;
     }
 
     protected void runCodeFor(int version, Class<? extends Annotation> ann) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
         for (Method m : getClass().getMethods()) {
             if (m.isAnnotationPresent(ann)) {
                 if (m.getAnnotation(dbVersion.class).value() == version) {
                     m.invoke(this);
                 }
             }
         }
     }
     public final static int MAX_UUID_REQUESTS_PER = 2 * 64;
 
     /**
      * Add UUIDS for all players
      *
      * @throws SQLException
      */
     @postUpgrade
     @dbVersion(6)
     public void upgradeWriteUUIDS() throws SQLException {
 
         PreparedStatement stmt = conn.prepareStatement("UPDATE `" + tblPrefix + "_entity` SET `uuid`=? WHERE `name`=? and `type`=?");
         stmt.setString(3, IStatDataProvider.PLAYER_TYPE);
         ProviderQueryResult[] result = queryDatabase(new ProviderQuery(null, IStatDataProvider.PLAYER_TYPE, null, false));
         platform.getLogger().log(Level.INFO, "Found {0} player entries, processing in batches of {1}", new Object[]{result.length, MAX_UUID_REQUESTS_PER});
         for (int i = 0; i < result.length; i += MAX_UUID_REQUESTS_PER) {
             String[] toGet = new String[Math.min(MAX_UUID_REQUESTS_PER, result.length)];
             for (int k = 0; k < toGet.length; k++) {
                 toGet[k] = result[i + k].name;
             }
             Map<String, String> map = getUUIDS(toGet);
             for (Entry<String, String> e : map.entrySet()) {
                 stmt.setString(2, e.getKey());
                 stmt.setString(1, e.getValue());
                 stmt.executeUpdate();
                 //System.out.println(e.getKey() + " = " + e.getValue());
             }
             platform.getLogger().log(Level.INFO, "Updated {0} entries", map.size());
         }
     }
 
     private Map<String, String> getUUIDS(String... players) {
         Map<String, String> mapping = new HashMap<String, String>();
 
         List<ProfileCriteria> criteria = new ArrayList<ProfileCriteria>(players.length);
         for (String player : players) {
             criteria.add(new ProfileCriteria(player, "minecraft"));
         }
 
         Profile[] results = new HttpProfileRepository().findProfilesByCriteria(criteria.toArray(new ProfileCriteria[0]));
         for (Profile profile : results) {
 
             mapping.put(
                     profile.getName(),
                     profile.getId());
         }
 
 
         return mapping;
     }
 
     /**
      * Utility method to load SQL commands from files in JAR
      *
      * @param type extension of file to load, if not found will try load sql type (which is the type for MySQL syntax)
      * @param filename file to load, minus extension
      * @param prefix table prefix, replaces ${PREFIX} in loaded files
      * @return SQL commands loaded from file.
      */
     public String readSQL(String type, String filename, String prefix) {
         platform.getLogger().fine("Loading SQL: " + filename);
         InputStream is = platform.getResource(filename + "." + type);
         if (is == null) {
             is = platform.getResource(filename + ".sql");
         }
         if (is == null) {
             throw new IllegalArgumentException("No SQL file found with name " + filename + "." + scriptSuffix);
         }
         Scanner scanner = new Scanner(is);
         String sql = scanner.useDelimiter("\\Z").next().replaceAll("\\Z", "").replaceAll("\\n|\\r", "");
         scanner.close();
         return sql.replaceAll("\\$\\{PREFIX\\}", prefix);
 
     }
 
     public String byteArrayToHexString(byte[] b) {
         String result = "";
         for (int i = 0; i < b.length; i++) {
             result +=
                     Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
         }
         return result;
     }
 
     public Connection getConnection() {
         return conn;
     }
     
     public void setUUID(String player, String uuid){
         try{
         setUUID.setString(1, uuid);
         setUUID.setString(2,player);
         setUUID.setString(3,IStatDataProvider.PLAYER_TYPE);
         setUUID.executeUpdate();
         }catch(SQLException e){
             platform.mysqlError(e, "setUUID");
         }
         
     }
 }
