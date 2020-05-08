 package com.tehbeard.beardstat.dataproviders;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.regex.MatchResult;
 
 import net.dragonzone.promise.Promise;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 
 import com.tehbeard.beardstat.BeardStat;
 import com.tehbeard.beardstat.BeardStatRuntimeException;
 import com.tehbeard.beardstat.dataproviders.metadata.CategoryMeta;
 import com.tehbeard.beardstat.dataproviders.metadata.DomainMeta;
 import com.tehbeard.beardstat.containers.EntityStatBlob;
 import com.tehbeard.beardstat.containers.IStat;
 import com.tehbeard.beardstat.dataproviders.metadata.StatisticMeta;
 import com.tehbeard.beardstat.dataproviders.metadata.StatisticMeta.Formatting;
 import com.tehbeard.beardstat.dataproviders.metadata.WorldMeta;
 import com.tehbeard.beardstat.NoRecordFoundException;
 import com.tehbeard.beardstat.utils.HumanNameGenerator;
 import com.tehbeard.utils.misc.CallbackMatcher;
 import com.tehbeard.utils.misc.CallbackMatcher.Callback;
 import com.tehbeard.utils.mojang.api.profiles.HttpProfileRepository;
 import com.tehbeard.utils.mojang.api.profiles.Profile;
 import com.tehbeard.utils.mojang.api.profiles.ProfileCriteria;
 import java.io.File;
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.dragonzone.promise.Deferred;
 
 /**
  * base class for JDBC based data providers Allows easy development of data providers that make use of JDBC
  *
  * @author James
  *
  */
 public abstract class JDBCStatDataProvider implements IStatDataProvider {
 
     private final boolean backups;
 
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
     /**
      * SQL SCRIPT NAME BLOCK
      */
     public static final String SQL_METADATA_CATEGORY = "sql/maintenence/metadata/category";
     public static final String SQL_METADATA_STATISTIC = "sql/maintenence/metadata/statistic";
     public static final String SQL_METADATA_STATIC_STATS = "sql/maintenence/metadata/staticstats";
     public static final String SQL_CREATE_TABLES = "sql/maintenence/create.tables";
     public static final String SQL_LOAD_ENTITY = "sql/load/getEntity";
     public static final String SQL_LOAD_ENTITY_DATA = "sql/load/getEntityData";
     public static final String SQL_LOAD_DOMAINS = "sql/load/components/getDomains";
     public static final String SQL_LOAD_WORLDS = "sql/load/components/getWorlds";
     public static final String SQL_LOAD_CATEGORIES = "sql/load/components/getCategories";
     public static final String SQL_LOAD_STATISTICS = "sql/load/components/getStatistics";
     public static final String SQL_SAVE_DOMAIN = "sql/save/components/saveDomain";
     public static final String SQL_SAVE_WORLD = "sql/save/components/saveWorld";
     public static final String SQL_SAVE_CATEGORY = "sql/save/components/saveCategory";
     public static final String SQL_SAVE_STATISTIC = "sql/save/components/saveStatistic";
     public static final String SQL_SAVE_ENTITY = "sql/save/saveEntity";
     public static final String SQL_SAVE_STAT = "sql/save/saveStat";
     public static final String SQL_KEEP_ALIVE = "sql/maintenence/keepAlive";
     public static final String SQL_LIST_ENTITIES = "sql/maintenence/listEntities";
     // Database connection
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
     protected PreparedStatement listEntities;
     protected PreparedStatement deleteEntity;
     protected PreparedStatement createTable;
     private HashMap<String, EntityStatBlob> writeCache = new HashMap<String, EntityStatBlob>();
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
     private ExecutorService loadQueue = Executors.newSingleThreadExecutor();
     protected BeardStat plugin;
 
     public JDBCStatDataProvider(BeardStat plugin, String scriptSuffix, String driverClass, boolean backups) {
         try {
             this.scriptSuffix = scriptSuffix;
             this.plugin = plugin;
             Class.forName(driverClass);// load driver
         } catch (ClassNotFoundException ex) {
             throw new BeardStatRuntimeException("Could not locate driver library.", ex, false);
         }
         this.backups = backups;
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
         int latestVersion = this.plugin.getConfig().getDefaults().getInt("stats.database.sql_db_version");
 
         if (!this.plugin.getConfig().isSet("stats.database.sql_db_version")) {
             this.plugin.getConfig().set("stats.database.sql_db_version", 1);
             this.plugin.saveConfig();
         }
         int installedVersion = this.plugin.getConfig().getInt("stats.database.sql_db_version", 1);
 
         if (installedVersion > latestVersion) {
             throw new RuntimeException("database version > this one, You appear to be running an out of date plugin!");
         }
 
         if (installedVersion < latestVersion) {
             // Swap to transaction based mode,
             // Execute each migration script in sequence,
             // commit if successful,
             // rollback and error out if not
             // Should support partial recovery of migration effort, saves
             // current version if successful commit
 
             this.plugin.getLogger().info("Updating database to latest version");
             this.plugin.getLogger().info("Your database: " + installedVersion + " latest: " + latestVersion);
 
             if (backups) {
                 try {
                     this.plugin.getLogger().info("Creating database backup, if shit hits the fan and the rollback fails, you can use this.");
                     File f = new File(plugin.getDataFolder(), "backup." + scriptSuffix);
                     f.delete();
 
                     f.createNewFile();
 
                     generateBackup(f);
                 } catch (IOException ex) {
                     Logger.getLogger(JDBCStatDataProvider.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
 
             for (int i = 0; i < 3; i++) {
                 Bukkit.getConsoleSender().sendMessage(
                         ChatColor.RED + "WARNING: DATABASE MIGRATION WILL TAKE A LONG TIME ON LARGE DATABASES.");
             }
             int migrateToVersion = 0;
             try {
                 this.conn.setAutoCommit(false);
 
                 for (migrateToVersion = installedVersion + 1; migrateToVersion <= latestVersion; migrateToVersion++) {
 
                     Map<String, String> k = new HashMap<String, String>();
                     k.put("OLD_TBL", this.plugin.getConfig().getString("stats.database.table", ""));
 
                     //Run premigration method
                     try {
                         runCodeFor(migrateToVersion, preUpgrade.class);
                     } catch (InvocationTargetException ex) {
                         if (ex.getCause() instanceof SQLException) {
                             this.plugin.mysqlError((SQLException) ex.getCause(), "@CLASS/PREUPGRADE/" + migrateToVersion);
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
                         this.plugin.mysqlError(ex, "sql/maintenence/migration/migrate." + migrateToVersion);
                         throw ex;
                     }
 
                     //run post migration method
                     try {
                         runCodeFor(migrateToVersion, postUpgrade.class);
                     } catch (InvocationTargetException ex) {
                         if (ex.getCause() instanceof SQLException) {
                             this.plugin.mysqlError((SQLException) ex.getCause(), "@CLASS/POSTUPGRADE/" + migrateToVersion);
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
                     this.plugin.getConfig().set("stats.database.sql_db_version", migrateToVersion);
                     this.plugin.saveConfig();
 
                 }
 
             } catch (SQLException e) {
 
                 this.plugin.getLogger().severe("An error occured while migrating the database, initiating rollback to version "
                         + (migrateToVersion - 1));
                 try {
                     this.conn.rollback();
                     throw new BeardStatRuntimeException("Failed to migrate database", e, false);
                 } catch (SQLException se) {
                     this.plugin.getLogger().severe("Failed to rollback");
                     plugin.mysqlError(se, null);
                 }
 
 
             }
 
             this.plugin.getLogger().info("Migration successful");
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
 
         this.plugin.getLogger().info("Connecting....");
 
         try {
             this.conn = DriverManager.getConnection(this.connectionUrl, this.connectionProperties);
 
             // conn.setAutoCommit(false);
         } catch (SQLException e) {
             this.plugin.mysqlError(e, null);
             this.conn = null;
         }
 
     }
 
     /**
      * Returns true if connection is still there.
      *
      * @return
      */
     private synchronized boolean checkConnection() {
         this.plugin.getLogger().fine("Checking connection");
         try {
             if ((this.conn == null) || !this.conn.isValid(0)) {
                 this.plugin.getLogger().fine("Something is derp, rebooting connection.");
                 createConnection();
                 if (this.conn != null) {
                     this.plugin.getLogger().fine("Rebuilding statements");
                     prepareStatements();
                 } else {
                     this.plugin.getLogger().fine("Reboot failed!");
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
         this.plugin.getLogger().info("Constructing missing tables.");
         executeScript(SQL_CREATE_TABLES);
     }
 
     /**
      * Load statements from jar
      */
     protected void prepareStatements() {
         this.plugin.getLogger().config("Preparing statements");
 
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
         this.listEntities = getStatementFromScript(SQL_LIST_ENTITIES);
         // deleteEntity =
         // conn.prepareStatement(plugin.readSQL(type,"sql/maintenence/deletePlayerFully",
         // tblPrefix));
 
         this.plugin.getLogger().config("Set player stat statement created");
     }
 
     /**
      * Cache entries for quicker resolvement on our end.
      */
     private void cacheComponents() {
         ResultSet rs;
         try {
             //Domains
             rs = loadDomainsList.executeQuery();
             while (rs.next()) {
                 DomainMeta dm = new DomainMeta(
                         rs.getInt("domainId"),
                         rs.getString("domain"));
                 domainMetaMap.put(rs.getString("domain"), dm);
             }
             rs.close();
         } catch (SQLException e) {
             this.plugin.mysqlError(e, SQL_LOAD_DOMAINS);
         }
         try {
             //Worlds
             rs = loadWorldsList.executeQuery();
             while (rs.next()) {
                 WorldMeta wm = new WorldMeta(
                         rs.getInt("worldId"),
                         rs.getString("world"),
                         rs.getString("name"));
                 worldMetaMap.put(rs.getString("world"), wm);
             }
             rs.close();
         } catch (SQLException e) {
             this.plugin.mysqlError(e, SQL_LOAD_WORLDS);
         }
         try {
             //Worlds
             rs = loadCategoriesList.executeQuery();
             while (rs.next()) {
                 CategoryMeta cm = new CategoryMeta(
                         rs.getInt("categoryId"),
                         rs.getString("category"),
                         rs.getString("statwrapper"));
                 categoryMetaMap.put(rs.getString("category"), cm);
             }
             rs.close();
         } catch (SQLException e) {
             this.plugin.mysqlError(e, SQL_LOAD_CATEGORIES);
         }
         try {
             //Worlds
             rs = loadStatisticsList.executeQuery();
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
             this.plugin.mysqlError(e, SQL_LOAD_STATISTICS);
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
             sql += "`name`=? ";
             addAnd = true;
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
                 qryStmt.setString(colId, query.name);
                 colId++;
             }
             if (query.type != null) {
                 qryStmt.setString(colId, query.type);
                 colId++;
             }
             if (query.uuid != null) {
                 qryStmt.setString(colId, query.uuid.toString());
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
             plugin.mysqlError(e, "AUTOGEN: " + sql);
         }
         return new ProviderQueryResult[0];
     }
 
     @Override
     public Promise<EntityStatBlob> pullEntityBlob(final ProviderQuery query) {
 
         final Deferred<EntityStatBlob> promise = new Deferred<EntityStatBlob>();
 
         Runnable run = new Runnable() {
             @Override
             public void run() {
                 try {
                     if (!checkConnection()) {
                         plugin.getLogger().info("Database connection error!");
                         promise.reject(new SQLException("Error connecting to database"));
                         return;
                     }
                     long t1 = (new Date()).getTime();
                     ProviderQueryResult[] results = queryDatabase(query);
                     if (results.length > 1) {
                         throw new IllegalStateException("Invalid Query provided, more than one entity returned.");
                     }
                     EntityStatBlob esb = null;
                     ResultSet rs;
 
                     if (results.length == 1) {
                         
                         esb = new EntityStatBlob(results[0].name, results[0].dbid, results[0].type, results[0].type);//Create the damn esb
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
                     } else if (results.length == 0 && query.create) {
 
                         saveEntity.setString(1, query.name);
                         saveEntity.setString(2, query.type);
                         saveEntity.setString(3, query.uuid == null ? "" : query.uuid.toString());
                         saveEntity.executeUpdate();
                         rs = saveEntity.getGeneratedKeys();
                         rs.next();// load player id
 
                         // make the player object, close out result set.
                         esb = new EntityStatBlob(query.name, rs.getInt(1), query.type, query.uuid);
                         rs.close();
                     }
                     //Didn't get a esb, kill it.
                     if (esb == null) {
                         promise.reject(new NoRecordFoundException());
                         return;
                     }
 
 
 
                     plugin.getLogger().log(Level.CONFIG, "time taken to retrieve: {0} Milliseconds", ((new Date()).getTime() - t1));
 
                     promise.resolve(esb);
                 } catch (SQLException e) {
                     plugin.mysqlError(e, SQL_LOAD_ENTITY_DATA);
                     promise.reject(e);
                 }
             }
         };
 
         this.loadQueue.execute(run);
 
         return promise;
 
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
 
             EntityStatBlob copy = player.cloneForArchive();
 
             if (!this.writeCache.containsKey(player.getName())) {
                 this.writeCache.put(player.getName(), copy);
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
                     Bukkit.getConsoleSender()
                             .sendMessage(
                             ChatColor.RED
                             + "Could not restablish connection, will try again later, WARNING: CACHE WILL GROW WHILE THIS HAPPENS");
                 } else {
                     plugin.getLogger().config("Saving to database");
                     for (Entry<String, EntityStatBlob> entry : writeCache
                             .entrySet()) {
 
                         EntityStatBlob pb = entry.getValue();
                         IStat stat = null;
                         try {
                             saveEntityData.clearBatch();
                             for (Iterator<IStat> it = pb.getStats().iterator(); it.hasNext();) {
                                 stat = it.next();
                                 saveEntityData.setInt(1, pb.getEntityID());
                                 saveEntityData.setInt(2, getDomain(stat.getDomain()).getDbId());
                                 saveEntityData.setInt(3, getWorld(stat.getWorld()).getDbId());
                                 saveEntityData.setInt(4, getCategory(stat.getCategory()).getDbId());
                                 saveEntityData.setInt(5, getStatistic(stat.getStatistic()).getDbId());
                                 saveEntityData.setInt(6, stat.getValue());
                                 saveEntityData.addBatch();
                             }
                             saveEntityData.executeBatch();
 
                         } catch (SQLException e) {
                             plugin.getLogger().log(Level.WARNING, "entity id: {0} :: {1}", new Object[]{pb.getName(), pb.getEntityID()});
                             plugin.getLogger().log(Level.WARNING, "domain: {0} :: {1}", new Object[]{stat.getDomain(), getDomain(stat.getDomain()).getDbId()});
                             plugin.getLogger().log(Level.WARNING, "world: {0} :: {1}", new Object[]{stat.getWorld(), getWorld(stat.getWorld()).getDbId()});
                             plugin.getLogger().log(Level.WARNING, "category: {0} :: {1}", new Object[]{stat.getCategory(), getCategory(stat.getCategory()).getDbId()});
                             plugin.getLogger().log(Level.WARNING, "statistic: {0} :: {1}", new Object[]{stat.getStatistic(), getStatistic(stat.getStatistic()).getDbId()});
                             plugin.getLogger().log(Level.WARNING, "Value: {0}", stat.getValue());
                             plugin.mysqlError(e, SQL_SAVE_STAT);
                             checkConnection();
                         }
                     }
                     plugin.getLogger().config("Clearing write cache");
                     writeCache.clear();
                 }
             }
 
         }
     };
 
     @Override
     public void flushSync() {
         this.plugin.getLogger().info("Flushing in main thread! Game will lag!");
         this.flush.run();
         this.plugin.getLogger().info("Flushed!");
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
 
         String[] sqlStatements = this.plugin.readSQL(this.scriptSuffix, scriptName, this.tblPrefix).split("\\;");
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
                 Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Executing : " + subScript);
                 executeScript(subScript, keys);
                 continue;
             } else if (statement.startsWith("#")) {
                 Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Status : " + statement.substring(1));
             } else {
 
                 this.conn.prepareStatement(statement).execute();
 
 
             }
         }
 
     }
 
     public PreparedStatement getStatementFromScript(String scriptName, int flags) {
         try {
             return this.conn.prepareStatement(this.plugin.readSQL(this.scriptSuffix, scriptName, this.tblPrefix), flags);
         } catch (SQLException ex) {
             this.plugin.mysqlError(ex, scriptName);
             throw new BeardStatRuntimeException("Failed to create SQL statement for a script", ex, false);
         }
     }
 
     public PreparedStatement getStatementFromScript(String scriptName) {
         try {
             return this.conn.prepareStatement(this.plugin.readSQL(this.scriptSuffix, scriptName, this.tblPrefix));
         } catch (SQLException ex) {
             this.plugin.mysqlError(ex, scriptName);
             throw new BeardStatRuntimeException("Failed to create SQL statement for a script", ex, false);
         }
     }
 
     @Override
     public DomainMeta getDomain(String gameTag) {
         String qGameTag = sanitizeTag(gameTag);
         if (!domainMetaMap.containsKey(qGameTag)) {
             try {
 
                 saveDomain.setString(1, qGameTag);
                 saveDomain.execute();
                 ResultSet rs = saveDomain.getGeneratedKeys();
                 rs.next();
                 domainMetaMap.put(gameTag, new DomainMeta(rs.getInt(1), gameTag));
                 rs.close();
             } catch (SQLException ex) {
                 plugin.mysqlError(ex, SQL_SAVE_DOMAIN);
             }
         }
 
         return domainMetaMap.get(qGameTag);
     }
 
     @Override
     public WorldMeta getWorld(String gameTag) {
 
         if (!worldMetaMap.containsKey(gameTag)) {
             try {
                 saveWorld.setString(1, gameTag);
                 saveWorld.setString(2, gameTag.replaceAll("_", " "));
                 saveWorld.execute();
                 ResultSet rs = saveWorld.getGeneratedKeys();
                 rs.next();
                 worldMetaMap.put(gameTag, new WorldMeta(rs.getInt(1), gameTag, gameTag.replaceAll("_", " ")));
                 rs.close();
             } catch (SQLException ex) {
                 plugin.mysqlError(ex, SQL_SAVE_WORLD);
             }
         }
 
         return worldMetaMap.get(gameTag);
     }
 
     @Override
     public CategoryMeta getCategory(String gameTag) {
         if (!categoryMetaMap.containsKey(gameTag)) {
             try {
                 saveCategory.setString(1, gameTag);
                saveCategory.setString(2, gameTag.replaceAll("_", " "));
                 saveCategory.execute();
                 ResultSet rs = saveCategory.getGeneratedKeys();
                 rs.next();
                 categoryMetaMap.put(gameTag, new CategoryMeta(rs.getInt(1), gameTag, gameTag.replaceAll("_", " ")));
                 rs.close();
             } catch (SQLException ex) {
                 plugin.mysqlError(ex, SQL_SAVE_CATEGORY);
             }
         }
 
         return categoryMetaMap.get(gameTag);
     }
 
     @Override
     public StatisticMeta getStatistic(String gameTag) {
         if (!statisticMetaMap.containsKey(gameTag)) {
             try {
                 saveStatistic.setString(1, gameTag);
                 saveStatistic.setString(2, HumanNameGenerator.getNameOf(gameTag));
                 saveStatistic.setString(3, Formatting.none.toString().toLowerCase());
                 saveStatistic.execute();
                 ResultSet rs = saveStatistic.getGeneratedKeys();
                 rs.next();
                 statisticMetaMap.put(gameTag, new StatisticMeta(rs.getInt(1), gameTag, gameTag.replaceAll("_", " "), Formatting.none));
                 rs.close();
             } catch (SQLException ex) {
                 plugin.mysqlError(ex, SQL_SAVE_STATISTIC);
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
 
     @Override
     public EntityStatBlob pullEntityBlobDirect(ProviderQuery query) {
         return pullEntityBlob(query).getValue();
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
         plugin.getLogger().info("Found " + result.length + " player entries, processing in batches of " + MAX_UUID_REQUESTS_PER);
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
             plugin.getLogger().info("Updated " + map.size() + " entries");
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
 }
