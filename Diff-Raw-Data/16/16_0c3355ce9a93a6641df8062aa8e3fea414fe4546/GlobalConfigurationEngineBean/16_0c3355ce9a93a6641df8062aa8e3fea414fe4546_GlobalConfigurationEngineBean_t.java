 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.ejb.beans.configuration;
 
 import com.flexive.core.Database;
 import com.flexive.core.DatabaseConst;
 import static com.flexive.core.DatabaseConst.TBL_CONFIG_GLOBAL;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.ejb.mbeans.FxCache;
 import com.flexive.shared.*;
 import com.flexive.shared.cache.FxCacheException;
 import com.flexive.shared.configuration.DivisionData;
 import com.flexive.shared.configuration.ParameterScope;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxLoadException;
 import com.flexive.shared.exceptions.FxNoAccessException;
 import com.flexive.shared.interfaces.GlobalConfigurationEngine;
 import com.flexive.shared.interfaces.GlobalConfigurationEngineLocal;
 import com.flexive.shared.mbeans.FxCacheMBean;
 import com.flexive.shared.mbeans.MBeanHelper;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.ejb.*;
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.MBeanRegistrationException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.ObjectName;
 import javax.naming.NamingException;
 import java.io.Serializable;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.atomic.AtomicLong;
 import javax.annotation.sql.DataSourceDefinition;
 import javax.annotation.sql.DataSourceDefinitions;
 
 /**
  * Global configuration MBean.
  *
  * <p> This bean also configures a default data source for Java EE 6 containers, using an embedded (file-based) H2 database.
  * This will be used as a fallback <strong>for division 1</strong> if no database connection is configured.
  * Also, a fallback global data source is configured.</p>
  *
  * <p>The data source uses a relative path for the database file,
  * so it will usually be created somewhere in the application server's directory. This has the advantage over
  * an absolute URL that multiple installations are possible for the same user, although all applications
  * in that application server will necessarily share the same default data source.
  * </p>
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 
 @DataSourceDefinitions({
     @DataSourceDefinition(
         name = GlobalConfigurationEngineBean.DEFAULT_DS,
         className = "org.h2.jdbcx.JdbcDataSource",
         properties="URL=jdbc:h2:h2/flexive;SCHEMA=flexive;LOCK_TIMEOUT=10000;MVCC=TRUE;TRACE_LEVEL_FILE=0",
         user = "sa",
         password = "sa",
         transactional=true
//        url="jdbc:h2:h2/flexive;SCHEMA=flexive;LOCK_TIMEOUT=10000;MVCC=TRUE;TRACE_LEVEL_FILE=0"
     ),
     @DataSourceDefinition(
         name = GlobalConfigurationEngineBean.DEFAULT_DS + "NoTX",
         className = "org.h2.jdbcx.JdbcDataSource",
         properties="URL=jdbc:h2:h2/flexive;SCHEMA=flexive;LOCK_TIMEOUT=10000;MVCC=TRUE;TRACE_LEVEL_FILE=0",
         user = "sa",
         password = "sa",
         transactional=false
//        url="jdbc:h2:h2/flexive;SCHEMA=flexive;LOCK_TIMEOUT=10000;MVCC=TRUE;TRACE_LEVEL_FILE=0"
     ),
     @DataSourceDefinition(
         name = GlobalConfigurationEngineBean.DEFAULT_DS_CONFIG,
         className = "org.h2.jdbcx.JdbcDataSource",
         properties = "URL=jdbc:h2:h2/flexive;SCHEMA=flexiveConfiguration;LOCK_TIMEOUT=10000;MVCC=TRUE;TRACE_LEVEL_FILE=0",
         user = "sa",
         password = "sa",
         transactional=true
//        url="jdbc:h2:h2/flexive;SCHEMA=flexiveConfiguration;LOCK_TIMEOUT=10000;MVCC=TRUE;TRACE_LEVEL_FILE=0"
     ),
     @DataSourceDefinition(
         name = GlobalConfigurationEngineBean.DEFAULT_DS_INIT,
         className = "org.h2.jdbcx.JdbcDataSource",
         properties= "URL=jdbc:h2:h2/flexive;LOCK_TIMEOUT=10000;MVCC=TRUE;TRACE_LEVEL_FILE=0",
         user = "sa",
         password = "sa",
         transactional=false
//        url="jdbc:h2:h2/flexive;SCHEMA=flexive;LOCK_TIMEOUT=10000;MVCC=TRUE;TRACE_LEVEL_FILE=0"
     )
 })
 @TransactionManagement(TransactionManagementType.CONTAINER)
 @TransactionAttribute(TransactionAttributeType.REQUIRED)
 @Stateless(name = "GlobalConfigurationEngine", mappedName = "GlobalConfigurationEngine")
 public class GlobalConfigurationEngineBean extends GenericConfigurationImpl implements GlobalConfigurationEngine, GlobalConfigurationEngineLocal {
 
     /**
      * The default data source bound in JEE 6 containers.
      */
     public static final String DEFAULT_DS = "java:global/flexive-ejb/DefaultDS";
     /**
      * The default configuration data source bound in JEE 6 containers.
      */
     public static final String DEFAULT_DS_CONFIG = "java:global/flexive-ejb/DefaultConfigurationDS";
     /**
      * An extra data source without a schema for initialization.
      */
     public static final String DEFAULT_DS_INIT = "java:global/flexive-ejb/DefaultInitDS";
 
 
    /**
      * Maximum number of cached domains per hit/miss cache
      * This should be at least roughly equal to the number of configured
      * domains since the miss cache will likely be thrashed otherwise.
      */
     private static final int MAX_CACHED_DOMAINS = 1000;
 
     /**
      * Cache path for storing config parameters
      */
     private static final String CACHE_CONFIG = "/globalconfig/";
     /**
      * Cache path for storing other values
      */
     private static final String CACHE_BEAN = "/globalconfigMBean/";
     /**
      * Cache path suffix for storing division data
      */
     private static final String CACHE_DIVISIONS = "divisionData";
     /**
      * Cache key for the timestamp of the last change in the division mapping tables.
      */
     private static final String CACHE_TIMESTAMP = "timestamp";
 
     private static final Log LOG = LogFactory.getLog(GlobalConfigurationEngineBean.class);
 
     /**
      * Cached local copy of divisions, must be cleared if the cache is cleared
      */
     private static final List<DivisionData> divisions = new CopyOnWriteArrayList<DivisionData>();
     private static final AtomicLong divisionsTimestamp = new AtomicLong(-1);
 
     /**
      * Cache for mapping domain names to division IDs. Cleared when the division cache is cleared.
      */
     private static final ConcurrentMap<String, Integer> domainCache = new ConcurrentHashMap<String, Integer>(MAX_CACHED_DOMAINS);
     private static final AtomicLong domainCacheTimestamp = new AtomicLong(-1);
 
     /**
      * Simple cache stats (displayed on shutdown)
      */
     private static final SimpleCacheStats cacheStats = new SimpleCacheStats("Global get");
 
     /**
      * {@inheritDoc}
      */
     public void create() throws Exception {
 //		System.out.println("************ Creating global config ***************");
     }
 
     /**
      * {@inheritDoc}
      */
     public void destroy() throws Exception {
         System.out.println("Global config cache stats: ");
         System.out.println(cacheStats.toString());
         System.out.println();
     }
 
     // implement Configuration methods
 
     @Override
     protected ParameterScope getDefaultScope() {
         return ParameterScope.GLOBAL;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected Connection getConnection() throws SQLException {
         return Database.getGlobalDbConnection();
     }
 
     /**
      * Get the global configuration table name including the correct schema
      *
      * @param con an open and valid connection to determine the correct storage vendor
      * @return global configuration table name including the escaped schema
      */
     private String getConfigurationTable(Connection con) {
         try {
             if (StorageManager.getStorageImpl(con.getMetaData().getDatabaseProductName()).requiresConfigSchema()) {
                 if (DatabaseConst.getConfigSchema().endsWith("."))
                     return DatabaseConst.getConfigSchema() + TBL_CONFIG_GLOBAL;
                 else
                     return DatabaseConst.getConfigSchema() + "." + TBL_CONFIG_GLOBAL;
             }
         } catch (SQLException e) {
             LOG.warn(e);
         }
         return TBL_CONFIG_GLOBAL;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected PreparedStatement getSelectStatement(Connection conn, String path, String key) throws SQLException {
         String sql = "SELECT cvalue FROM " + getConfigurationTable(conn) + " WHERE cpath=? and ckey=?";
         PreparedStatement stmt = conn.prepareStatement(sql);
         stmt.setString(1, path);
         stmt.setString(2, key);
         return stmt;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected PreparedStatement getSelectStatement(Connection conn, String path) throws SQLException {
         String sql = "SELECT ckey, cvalue FROM " + getConfigurationTable(conn) + " WHERE cpath=?";
         PreparedStatement stmt = conn.prepareStatement(sql);
         stmt.setString(1, path);
         return stmt;
     }
 
     @Override
     protected PreparedStatement getSelectStatement(Connection conn) throws SQLException {
         throw new UnsupportedOperationException("Select of ALL parameters not supported in global configuration.");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected PreparedStatement getUpdateStatement(Connection conn, String path, String key, String value, String className)
             throws SQLException, FxNoAccessException {
         if (!isAuthorized()) {
             throw new FxNoAccessException("ex.configuration.update.perm.global");
         }
         // TODO: support className/getAll() in global configuration?
         String sql = "UPDATE " + getConfigurationTable(conn) + " SET cvalue=? WHERE cpath=? AND ckey=?";
         PreparedStatement stmt = conn.prepareStatement(sql);
         StorageManager.setBigString(stmt, 1, value);
         stmt.setString(2, path);
         stmt.setString(3, key);
         return stmt;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected PreparedStatement getInsertStatement(Connection conn, String path, String key, String value, String className)
             throws SQLException, FxNoAccessException {
         if (!isAuthorized()) {
             throw new FxNoAccessException("ex.configuration.update.perm.global");
         }
         // TODO: support className/getAll() in global configuration?
         String sql = "INSERT INTO " + getConfigurationTable(conn) + "(cpath, ckey, cvalue) VALUES (?, ?, ?)";
         PreparedStatement stmt = conn.prepareStatement(sql);
         stmt.setString(1, path);
         stmt.setString(2, key);
         StorageManager.setBigString(stmt, 3, value);
         return stmt;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected PreparedStatement getDeleteStatement(Connection conn, String path, String key)
             throws SQLException, FxNoAccessException {
         if (!isAuthorized()) {
             throw new FxNoAccessException("ex.configuration.delete.perm.global");
         }
         String sql = "DELETE FROM " + getConfigurationTable(conn) + " WHERE cpath=? "
                 + (key != null ? " AND ckey=?" : "");
         PreparedStatement stmt = conn.prepareStatement(sql);
         stmt.setString(1, path);
         if (key != null) {
             stmt.setString(2, key);
         }
         return stmt;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected String getCachePath(String path) {
         // global parameters have no dynamic context
         return CACHE_CONFIG + path;
     }
 
     // add global configuration-specific methods
 
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public int[] getDivisionIds() throws FxApplicationException {
         try {
             // check cache
             int[] cachedDivisionIds = (int[]) getCache(getBeanPath(CACHE_DIVISIONS), "allDivisions");
             if (cachedDivisionIds != null) {
                 return cachedDivisionIds;
             }
         } catch (FxCacheException e) {
             LOG.error("Cache failure (ignored): " + e.getMessage(), e);
         }
 
         // get list of all configured divisions
         Map<String, String> domainMappings = getAll(SystemParameters.GLOBAL_DIVISIONS_DOMAINS);
         int[] divisionIds = new int[domainMappings.keySet().size()];
         int ctr = 0;
         for (String divisionId : domainMappings.keySet()) {
             divisionIds[ctr++] = Integer.parseInt(divisionId);
         }
         Arrays.sort(divisionIds);
         putCache(getBeanPath(CACHE_DIVISIONS), "allDivisions", divisionIds);
         return divisionIds;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public DivisionData[] getDivisions() throws FxApplicationException {
         final long timestamp = getTimestamp();
         if (divisions.isEmpty() || divisionsTimestamp.get() < timestamp) {
             synchronized (divisions) {
                 if (divisions.isEmpty()) {
                     divisionsTimestamp.set(timestamp);
                     final int[] divisionIds = getDivisionIds();
                     final List<DivisionData> divisionList = new ArrayList<DivisionData>(divisionIds.length);
                     for (int divisionId : divisionIds) {
                         try {
                             divisionList.add(getDivisionData(divisionId));
                         } catch (Exception e) {
                             LOG.error("Invalid division data (ignored): " + e.getMessage());
                         }
                     }
                     divisions.addAll(divisionList);
                 }
             }
         }
         return divisions.toArray(new DivisionData[divisions.size()]);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public DivisionData getDivisionData(int division) throws FxApplicationException {
         try {
             DivisionData data = (DivisionData) getCache(getBeanPath(CACHE_DIVISIONS), "" + division);
             if (data != null) {
                 return data;
             }
         } catch (FxCacheException e) {
             LOG.error("Cache failure (ignored): " + e.getMessage(), e);
         }
         // get datasource
         String dataSource = get(SystemParameters.GLOBAL_DATASOURCES, "" + division);
         String domainRegEx = get(SystemParameters.GLOBAL_DIVISIONS_DOMAINS, "" + division);
         DivisionData data = createDivisionData(division, dataSource, domainRegEx);
         // put in cache
         putCache(getBeanPath(CACHE_DIVISIONS), "" + division, data);
         return data;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public DivisionData createDivisionData(int divisionId, String dataSource, String domainRegEx) {
         String dbVendor = "unknown";
         String dbVersion = "unknown";
         String dbDriverVersion = "unknown";
         boolean available = false;
         Connection con = null;
         try {
             // lookup non-transactional datasource to avoid issues with the default JEE6 data source in Glassfish
             con = Database.getDataSource(dataSource + "NoTX").getConnection();
             DatabaseMetaData dbmd = con.getMetaData();
             dbVendor = dbmd.getDatabaseProductName();
             dbVersion = dbmd.getDatabaseProductVersion();
             dbDriverVersion = dbmd.getDriverName() + " " + dbmd.getDriverVersion();
             available = true;
         } catch (NamingException e) {
             LOG.error("Failed to get datasource " + dataSource + " (flagged inactive)");
         } catch (SQLException e) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Failed to get database meta information: " + e.getMessage(), e);
             }
         } finally {
             Database.closeObjects(GlobalConfigurationEngineBean.class, con, null);
         }
         return new DivisionData(divisionId, available, dataSource, domainRegEx, dbVendor, dbVersion, dbDriverVersion);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public int getDivisionId(String serverName) throws FxApplicationException {
         final long timestamp = getTimestamp();
         if (domainCacheTimestamp.get() >= timestamp) {
             Integer cachedDivisionId = domainCache.get(serverName);
             if (cachedDivisionId != null) {
                 return cachedDivisionId;
             }
         } else {
             synchronized (domainCache) {
                 domainCache.clear();
                 domainCacheTimestamp.set(timestamp);
             }
         }
         DivisionData[] divisionIds = getDivisions();
         int divisionId = -1;
         for (DivisionData division : divisionIds) {
             if (division.isMatchingDomain(serverName)) {
                 divisionId = division.getId();
                 break;
             }
         }
         synchronized (domainCache) {
             if (domainCache.size() > MAX_CACHED_DOMAINS) {
                 domainCache.clear();
             }
             domainCache.put(serverName, divisionId);
         }
         return divisionId;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void saveDivisions(List<? extends DivisionData> divisions) throws FxApplicationException {
         removeAll(SystemParameters.GLOBAL_DATASOURCES);
         removeAll(SystemParameters.GLOBAL_DIVISIONS_DOMAINS);
         clearDivisionCache();
         for (DivisionData division : divisions) {
             // remove the "java:" prefix that may have been appended on some containers, use the canonical JDBC string
             final String storedDataSource = division.getDataSource().replaceFirst("^java:", "");
             // store parameters
             put(SystemParameters.GLOBAL_DATASOURCES, String.valueOf(division.getId()), storedDataSource);
             put(SystemParameters.GLOBAL_DIVISIONS_DOMAINS, String.valueOf(division.getId()), division.getDomainRegEx());
         }
         updateTimestamp();
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public String getRootLogin() throws FxApplicationException {
         return get(SystemParameters.GLOBAL_ROOT_LOGIN);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public String getRootPassword() throws FxApplicationException {
         return get(SystemParameters.GLOBAL_ROOT_PASSWORD);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public boolean isMatchingRootPassword(String userPassword) throws FxApplicationException {
         String hashedPassword = getRootPassword();
         return getHashedPassword(userPassword).matches(hashedPassword);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void setRootLogin(String value) throws FxApplicationException {
         put(SystemParameters.GLOBAL_ROOT_LOGIN, value);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void setRootPassword(String value) throws FxApplicationException {
         put(SystemParameters.GLOBAL_ROOT_PASSWORD, getHashedPassword(value));
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void clearDivisionCache() {
         FxCacheMBean cache = CacheAdmin.getInstance();
         try {
             // clear local caches
             synchronized (divisions) {
                 divisions.clear();
             }
             synchronized (domainCache) {
                 domainCache.clear();
             }
             // clear shared cache
             cache.globalRemove(getBeanPath(CACHE_DIVISIONS));
         } catch (FxCacheException e) {
             LOG.error("Failed to clear cache: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void registerCacheMBean(ObjectName name) throws MBeanRegistrationException, NotCompliantMBeanException, InstanceAlreadyExistsException {
         //TODO: some exception handling and cross checks for prev. registrations wouldn't hurt ...
         //TODO: maybe create a system beans and move this method there
         MBeanHelper.locateServer().registerMBean(new FxCache(), name);
     }
 
     /**
      * Get the complete cache path for miscellaneous internal paths.
      *
      * @param path cache path to be stored
      * @return the complete cache path for miscellaneous internal paths.
      */
     private String getBeanPath(String path) {
         return CACHE_BEAN + path;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void putCache(String path, String key, Serializable value) {
         // put parameter in the global (without division data) cache
         try {
             CacheAdmin.getInstance().globalPut(path, key, value);
         } catch (FxCacheException e) {
             LOG.error("Failed to update cache (ignored): " + e.getMessage());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void deleteCache(String path, String key) {
         try {
             CacheAdmin.getInstance().globalRemove(path, key);
         } catch (FxCacheException e) {
             LOG.error("Failed to update cache (ignored): " + e.getMessage());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void deleteCache(String path) {
         try {
             CacheAdmin.getInstance().globalRemove(path);
         } catch (FxCacheException e) {
             LOG.error("Failed to update cache (ignored): " + e.getMessage());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected Serializable getCache(String path, String key) throws FxCacheException {
         FxCacheMBean cache = CacheAdmin.getInstance();
         return (Serializable) cache.globalGet(path, key);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void logCacheHit(String path, String key) {
         cacheStats.addHit();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void logCacheMiss(String path, String key) {
         cacheStats.addMiss();
     }
 
     /**
      * Return the timestamp of the last modification on the division data table.
      *
      * @return the timestamp of the last modification on the division data table.
      */
     private long getTimestamp() {
         Long timestamp;
         try {
             timestamp = (Long) getCache(getBeanPath(""), CACHE_TIMESTAMP);
         } catch (FxCacheException e) {
             throw new FxLoadException(e).asRuntimeException();
         }
         if (timestamp == null) {
             timestamp = System.currentTimeMillis();
             putCache(getBeanPath(""), CACHE_TIMESTAMP, timestamp);
         }
         return timestamp;
     }
 
     /**
      * Update the division data table timestamp.
      */
     private void updateTimestamp() {
         putCache(getBeanPath(""), CACHE_TIMESTAMP, System.currentTimeMillis());
     }
 
     /**
      * Returns true if the calling user is authorized for manipulating
      * the global configuration.
      *
      * @return true if the calling user is authorized for manipulating
      *         the global configuration.
      */
     private boolean isAuthorized() {
         return FxContext.get().isGlobalAuthenticated();
     }
 
     /**
      * Compute the hashed password for the given input.
      *
      * @param userPassword the password to be hashed
      * @return the hashed password for the given input.
      */
     private String getHashedPassword(String userPassword) {
         return FxSharedUtils.hashPassword(31289, userPassword);
     }
 
 
 }
