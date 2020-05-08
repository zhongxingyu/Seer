 package org.sakaiproject.nakamura.lite.storage.jdbc.migrate;
 
 import java.io.IOException;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.sakaiproject.nakamura.api.lite.ClientPoolException;
 import org.sakaiproject.nakamura.api.lite.Configuration;
 import org.sakaiproject.nakamura.api.lite.Feedback;
 import org.sakaiproject.nakamura.api.lite.MigrateContentService;
 import org.sakaiproject.nakamura.api.lite.PropertyMigrationException;
 import org.sakaiproject.nakamura.api.lite.PropertyMigrator;
 import org.sakaiproject.nakamura.api.lite.Repository;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
 import org.sakaiproject.nakamura.api.lite.content.Content;
 import org.sakaiproject.nakamura.lite.SessionImpl;
 import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImpl;
 import org.sakaiproject.nakamura.lite.content.BlockSetContentHelper;
 import org.sakaiproject.nakamura.lite.storage.DisposableIterator;
 import org.sakaiproject.nakamura.lite.storage.SparseRow;
 import org.sakaiproject.nakamura.lite.storage.StorageClient;
 import org.sakaiproject.nakamura.lite.storage.jdbc.Indexer;
 import org.sakaiproject.nakamura.lite.storage.jdbc.JDBCStorageClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 import com.google.common.collect.Maps;
 
 /**
  * This component performs migration for JDBC only. It goes direct to the JDBC
  * tables to get a lazy iterator of rowIDs direct from the StorageClient which
  * it then updates one by one. In general this approach to migration is only
  * suitable for the JDBC drivers since they are capable of producing a non in
  * memory list of rowids, a migrator that targets the ColumDBs should probably
  * use a MapReduce job to perform migration and avoid streaming all data through
  * a single node over the network.
  * 
  * At present, the migrator does not record if an item has been migrated. Which
  * means if a migration operation is stopped it will have to be restarted from
  * the beginning and records that have already been migrated will get
  * re-processed. To put a restart facility in place care will need to taken to
  * ensure that updates to existing rows and new rows are tracked as well as the
  * rows that have already been processed. In addition a performant way of
  * querying all objects to get a dense list of items to be migrated. Its not
  * impossible but needs some careful thought to make it work on realistic
  * datasets (think 100M records+, don't think 10K records)
  * 
  * @author ieb
  * 
  */
 @Component(immediate = true, enabled = true, metatype = true)
 @Service(value = MigrateContentService.class)
 public class MigrateContentComponent implements MigrateContentService {
 
     private static final String SYSTEM_MIGRATION_CONTENT_ITEM = "system/migration";
 
     private static final String DEFAULT_REDOLOG_LOCATION = "migrationlogs";
 
     @Property(value=DEFAULT_REDOLOG_LOCATION)
     private static final String PROP_REDOLOG_LOCATION = "redolog-location";
 
     private static final int DEFAULT_MAX_LOG_SIZE = 1024000;
     
     @Property(intValue=DEFAULT_MAX_LOG_SIZE)
     private static final String PROP_MAX_LOG_SIZE = "max-redo-log-size";
 
 
     public interface IdExtractor {
 
         String getKey(Map<String, Object> properties);
 
     }
 
     private static final Logger LOGGER = LoggerFactory.getLogger(MigrateContentComponent.class);
 
     @Reference
     private Repository repository;
 
     @Reference
     private Configuration configuration;
 
     @Reference
     private PropertyMigratorTracker propertyMigratorTracker;
 
     private String redoLogLocation;
 
     private Integer maxLogFileSize;
 
 
 
 
     @Activate
     public void activate(Map<String, Object> properties) throws StorageClientException,
             AccessDeniedException, IOException {
         redoLogLocation = StorageClientUtils.getSetting(properties.get(PROP_REDOLOG_LOCATION), DEFAULT_REDOLOG_LOCATION);
         maxLogFileSize = StorageClientUtils.getSetting(properties.get(PROP_MAX_LOG_SIZE), DEFAULT_MAX_LOG_SIZE);
     }
     
     
     public synchronized void migrate(boolean dryRun, int limit,  boolean reindexAll, Feedback feedback ) throws ClientPoolException, StorageClientException, AccessDeniedException, IOException, PropertyMigrationException {
         SessionImpl session = (SessionImpl) repository.loginAdministrative();
         StorageClient client = session.getClient();
         FileRedoLogger migrateRedoLog = new FileRedoLogger(redoLogLocation, maxLogFileSize, feedback);
         client.setStorageClientListener(migrateRedoLog);
         try{
             if (client instanceof JDBCStorageClient) {
                 JDBCStorageClient jdbcClient = (JDBCStorageClient) client;
                 String keySpace = configuration.getKeySpace();
     
                 Indexer indexer = jdbcClient.getIndexer();
     
                 PropertyMigrator[] propertyMigrators = propertyMigratorTracker.getPropertyMigrators();
                 
                 
                 DependencySequence migratorDependencySequence = getMigratorSequence(session, propertyMigrators);
 
                 for (PropertyMigrator p : migratorDependencySequence) {
                     LOGGER.info("DryRun:{} Using Property Migrator {} ", dryRun, p);
                     feedback.log("DryRun:{0} Using Property Migrator {1} ", dryRun, p);
                     
                 }
                 for (PropertyMigrator p : migratorDependencySequence.getUnresolved()) {
                     LOGGER.info("DryRun:{} Unresolved Property Migrator {} ", dryRun, p);
                     feedback.log("DryRun:{0} Unresolved Property Migrator {1} ", dryRun, p);
                 }
                 for (Entry<String, Object> p : migratorDependencySequence.getAlreadyRun().entrySet()) {
                     LOGGER.info("DryRun:{} Migrator Last Run {} ", dryRun, p);
                     feedback.log("DryRun:{0} Migrator Last Run {1} ", dryRun, p);
                 }
                 if ( migratorDependencySequence.hasUnresolved() ) {
                     throw new PropertyMigrationException("There are unresolved dependencies "+migratorDependencySequence.getUnresolved());
                 }
                 reindex(dryRun, jdbcClient, keySpace, configuration.getAuthorizableColumnFamily(),
                         indexer, migratorDependencySequence, new IdExtractor() {
     
                             public String getKey(Map<String, Object> properties) {
                                 if (properties.containsKey(Authorizable.ID_FIELD)) {
                                     return (String) properties.get(Authorizable.ID_FIELD);
                                 }
                                 return null;
                             }
                         }, limit, feedback, reindexAll);
                 reindex(dryRun, jdbcClient, keySpace, configuration.getContentColumnFamily(), indexer,
                         migratorDependencySequence, new IdExtractor() {
     
                             public String getKey(Map<String, Object> properties) {
                                 if (properties.containsKey(BlockSetContentHelper.CONTENT_BLOCK_ID)) {
                                     // blocks of a bit stream
                                     return (String) properties
                                             .get(BlockSetContentHelper.CONTENT_BLOCK_ID);
                                 } else if (properties.containsKey(Content.getUuidField())) {
                                     // a content item and content block item
                                     return (String) properties.get(Content.getUuidField());
                                 } else if (properties.containsKey(Content.STRUCTURE_UUID_FIELD)) {
                                     // a structure item
                                     return (String) properties.get(Content.PATH_FIELD);
                                 }
                                 return null;
                             }
                         }, limit, feedback, reindexAll);
     
                 reindex(dryRun, jdbcClient, keySpace, configuration.getAclColumnFamily(), indexer,
                         migratorDependencySequence, new IdExtractor() {
                             public String getKey(Map<String, Object> properties) {
                                 if (properties.containsKey(AccessControlManagerImpl._KEY)) {
                                     return (String) properties.get(AccessControlManagerImpl._KEY);
                                 }
                                 return null;
                             }
                         }, limit, feedback, reindexAll);
                 
                 saveMigratorSequence(session, migratorDependencySequence);
                 
             } else {
                 LOGGER.warn("This class will only re-index content for the JDBCStorageClients");
             }
         } finally {
             client.setStorageClientListener(null);
             migrateRedoLog.close();
             session.logout();
         }
         
     }
 
 
     private void saveMigratorSequence(SessionImpl session,
             DependencySequence migratorDependencySequence) throws AccessDeniedException,
             StorageClientException {
         Content runMigrators = session.getContentManager().get(SYSTEM_MIGRATION_CONTENT_ITEM);
         String ts = String.valueOf(System.currentTimeMillis());
         int i = 0;
         if (runMigrators == null) {
             Builder<String, Object> b = ImmutableMap.builder();
             for (PropertyMigrator pm : migratorDependencySequence) {
                 b.put(pm.getName(), ts + ";" + i);
             }
             runMigrators = new Content(SYSTEM_MIGRATION_CONTENT_ITEM, b.build());
         } else {
             for (PropertyMigrator pm : migratorDependencySequence) {
                 runMigrators.setProperty(pm.getName(), ts + ";" + i);
             }
         }
         session.getContentManager().update(runMigrators);
     }
 
     private DependencySequence getMigratorSequence(SessionImpl session,
             PropertyMigrator[] propertyMigrators) throws StorageClientException,
             AccessDeniedException {
         Content runMigrators = session.getContentManager().get(SYSTEM_MIGRATION_CONTENT_ITEM);
         Map<String, Object> runMigratorRecord = ImmutableMap.of();
         if (runMigrators != null) {
             runMigratorRecord = runMigrators.getProperties();
         }
         return new DependencySequence(propertyMigrators, runMigratorRecord);
     }
 
     private void reindex(boolean dryRun, StorageClient jdbcClient, String keySpace,
             String columnFamily, Indexer indexer, DependencySequence propertyMigrators,
             IdExtractor idExtractor, int limit, Feedback feedback, boolean reindexAll) throws StorageClientException {
         long objectCount = jdbcClient.allCount(keySpace, columnFamily);
         LOGGER.info("DryRun:{} Migrating {} objects in {} ", new Object[] { dryRun, objectCount,
                 columnFamily });
        feedback.log("DryRun:{0} Migrating {} objects in {1} ", new Object[] { dryRun, objectCount,
                 columnFamily });
         if (objectCount > 0) {
             DisposableIterator<SparseRow> allObjects = jdbcClient.listAll(keySpace, columnFamily);
             try {
                 long c = 0;
                 while (allObjects.hasNext()) {
                     Map<String, PreparedStatement> statementCache = Maps.newHashMap();
                     SparseRow r = allObjects.next();
                     c++;
                     if (c % 1000 == 0) {
                         LOGGER.info("DryRun:{} {}% remaining {} ", new Object[] { dryRun,
                                 ((c * 100) / objectCount), objectCount - c });
                         feedback.progress(dryRun, c, objectCount);
 
                     }
                     try {
                         Map<String, Object> properties = r.getProperties();
                         String rid = r.getRowId();
                         boolean save = false;
                         for (PropertyMigrator propertyMigrator : propertyMigrators) {
                             save = propertyMigrator.migrate(rid, properties) || save;
                         }
                         String key = idExtractor.getKey(properties);
                         if (key != null) {
                             if (!dryRun) {
                                 if (save) {
                                     jdbcClient.insert(keySpace, columnFamily, key, properties,
                                             false);
                                 } else if ( reindexAll ) {
                                     indexer.index(statementCache, keySpace, columnFamily, key, rid,
                                             properties);
                                 }
                             } else {
                                 if (c > limit) {
                                     LOGGER.info("Dry Run Migration Stoped at {} Objects ", limit);
                                     feedback.log("Dry Run Migration Stoped at {0} Objects ", limit);
                                     break;
                                 }
                             }
                         } else {
                             LOGGER.info("DryRun:{} Skipped Reindexing, no key in  {}", dryRun,
                                     properties);
                             feedback.log("DryRun:{0} Skipped Reindexing, no key in  {1}", dryRun,
                                     properties);
                         }
                     } catch (SQLException e) {
                         LOGGER.warn(e.getMessage(), e);
                         feedback.exception(e);
                     } catch (StorageClientException e) {
                         LOGGER.warn(e.getMessage(), e);
                         feedback.exception(e);
                     }
                 }
             } finally {
                 allObjects.close();
             }
         }
     }
 }
