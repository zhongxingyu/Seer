 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.migratory.metadata;
 
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Maps.EntryTransformer;
 
 import org.skife.jdbi.v2.Handle;
 import org.skife.jdbi.v2.Query;
 import org.skife.jdbi.v2.Update;
 import org.skife.jdbi.v2.exceptions.CallbackFailedException;
 import org.skife.jdbi.v2.tweak.HandleCallback;
 
 import com.nesscomputing.logging.Log;
 import com.nesscomputing.migratory.MigratoryConfig;
 import com.nesscomputing.migratory.MigratoryContext;
 import com.nesscomputing.migratory.MigratoryException;
 import com.nesscomputing.migratory.MigratoryException.Reason;
 import com.nesscomputing.migratory.MigratoryOption;
 import com.nesscomputing.migratory.migration.DbMigrator;
 import com.nesscomputing.migratory.migration.MigrationManager;
 import com.nesscomputing.migratory.migration.MigrationPlanner;
 import com.nesscomputing.migratory.migration.MigrationPlanner.MigrationDirection;
 import com.nesscomputing.migratory.migration.MigrationResult;
 
 public class MetadataManager
 {
     private static final Log LOG = Log.findLog();
 
     public static final String METADATA_MIGRATION_NAME = "migratory_metadata";
 
     public static final String METADATA_PREFIX = "#metadata:";
 
     private final MigratoryContext migratoryContext;
     private final MigratoryConfig migratoryConfig;
 
     private Handle transactionHandle = null;
 
     public MetadataManager(final MigratoryContext migratoryContext)
     {
         this.migratoryContext = migratoryContext;
         this.migratoryConfig = migratoryContext.getConfig();
     }
 
     /**
      * Make sure that the metadata exists. If it does not exist, create
      * the metadata table from scratch and register its creation.
      * @return A migration result if the table was created or null if it already existed.
      */
     public List<MetadataInfo> ensureMetadata(final MigratoryOption [] options)
         throws MigratoryException
     {
         if (migratoryContext.getDbSupport().tableExists(migratoryConfig.getMetadataTableName()))
         {
             return null;
         }
 
         if (migratoryConfig.isReadOnly()) {
             throw new MigratoryException(Reason.IS_READONLY);
         }
         // Table does not exist. The way we get one, is that we run a special migration for the internal metadata schema
         final MigrationPlanner migrationPlanner = new MigrationPlanner(new MigrationManager(migratoryContext, METADATA_MIGRATION_NAME), 0, Integer.MAX_VALUE);
 
         migrationPlanner.plan();
 
         if (migrationPlanner.getDirection() != MigrationDirection.FORWARD) {
             throw new MigratoryException(Reason.INTERNAL, "Migration planner could not plan a migration for the metadata table!");
         }
 
         final DbMigrator migrator = new DbMigrator(migratoryContext, migrationPlanner);
 
         try {
             lock(METADATA_MIGRATION_NAME);
             final List<MigrationResult> results = migrator.migrate(options);
             return commit(results);
         } catch (MigratoryException e) {
             rollback();
             throw e;
         } catch (RuntimeException e) {
             rollback();
             throw e;
         }
     }
 
     public void lock(final String personalityName)
     {
         transactionHandle = migratoryContext.getDBI().open();
         transactionHandle.begin();
 
         if (METADATA_MIGRATION_NAME.equals(personalityName)) {
             // Can't lock the actual metadata migration, because the table might not exist yet.
             return;
         }
         if (migratoryContext.getDbSupport().supportsLocking()) {
             transactionHandle.createStatement(METADATA_PREFIX + "lock")
                 .bind("personality_name", personalityName)
                 .execute();
         }
     }
 
     public List<MetadataInfo> commit(final List<MigrationResult> migrationResults)
     {
         if (transactionHandle == null) {
             throw new MigratoryException(Reason.INTERNAL, "commit before lock!");
         }
 
         try {
             final List<MetadataInfo> results = Lists.newArrayList();
 
             if (migrationResults != null) {
                 for (MigrationResult migrationResult : migrationResults) {
                     final MetadataInfo metadataInfo = new MetadataInfo(migrationResult);
 
                     final Update update = metadataInfo.bindToHandle(transactionHandle.createStatement(METADATA_PREFIX + "insert_metadata"));
                     final int count = update.execute();
                     LOG.debug("%d rows changed by inserting %s.", count, metadataInfo);
                     results.add(metadataInfo);
                 }
             }
             LOG.debug("Metadata Insert: %d", results.size());
             return results;
         }
         finally {
             transactionHandle.commit();
             transactionHandle.close();
             transactionHandle = null;
         }
     }
 
     public void rollback()
     {
        transactionHandle.rollback();
        transactionHandle.close();
        transactionHandle = null;
     }
 
     public Map<String, List<MetadataInfo>> getHistory(final Collection<String> personalities, final MigratoryOption [] options)
     {
         return performCallback(new HandleCallback<Map<String, List<MetadataInfo>>>() {
                 @Override
                 public Map<String, List<MetadataInfo>> withHandle(final Handle handle) throws SQLException {
                     final Map<String, List<MetadataInfo>> results = Maps.newTreeMap();
 
                     if (personalities != null) {
                         for (final String personality : personalities) {
                             final List<MetadataInfo> result =
                                 new HistoryCallback(personality, options).withHandle(handle);
 
                             if (!result.isEmpty()) {
                                 results.put(personality, result);
                             }
                         }
                     }
                     else {
                         final List<MetadataInfo> result = HistoryCallback.getHistoryQuery(handle, null, options).list();
 
                         for (final MetadataInfo metadataInfo : result) {
                             List<MetadataInfo> resultList = results.get(metadataInfo.getPersonalityName());
                             if (resultList == null) {
                                 resultList = new ArrayList<MetadataInfo>();
                                 results.put(metadataInfo.getPersonalityName(), resultList);
                             }
                             resultList.add(metadataInfo);
                         }
                     }
 
                     return results;
                 }
             });
     }
 
     public Map<String, MetadataInfo> getStatus(final Collection<String> personalities, final MigratoryOption ... options)
     {
         return performCallback(new HandleCallback<Map<String, MetadataInfo>>() {
                 @Override
                 public Map<String, MetadataInfo> withHandle(final Handle handle) throws SQLException {
 
                     final Map<String, MetadataInfo> results = Maps.newHashMap();
 
                     if (personalities != null) {
                         for (final String personality : personalities) {
                             final MetadataInfo result =
                                 new StatusCallback(personality, options).withHandle(handle);
 
                             if (result != null) {
                                 results.put(personality, result);
                             }
                         }
                     }
                     else {
                         final List<MetadataInfo> result = StatusCallback.getStatusQuery(handle, null, options).list();
 
                         for (final MetadataInfo metadataInfo : result) {
                             results.put(metadataInfo.getPersonalityName(), metadataInfo);
                         }
                     }
 
                     return results;
                 }
             });
     }
 
     private <T> T performCallback(final HandleCallback<T> callback) {
         if (transactionHandle != null) {
             try {
                 return callback.withHandle(transactionHandle);
             } catch (Exception e) {
                 throw new CallbackFailedException(e);
             }
         }
         else {
             return migratoryContext.getDBI().withHandle(callback);
         }
     }
 
     public Map<String, Integer> retrieveCurrentVersions(final MigratoryOption ... options)
     {
         return Maps.transformEntries(getStatus(null, options), new EntryTransformer<String, MetadataInfo, Integer>() {
                 @Override
                 public Integer transformEntry(final String name, final MetadataInfo info) {
                     return info.getEndVersion();
                 }
             });
     }
 
     public Collection<String> retrieveExistingPersonalities(final MigratoryOption ... options)
     {
         return getStatus(null, options).keySet();
     }
 
     public MetadataInfo getPersonalityStatus(final String personality, final MigratoryOption ... options)
     {
         return performCallback(new StatusCallback(personality, options));
     }
 
     public List<MetadataInfo> getPersonalityHistory(final String personality, final MigratoryOption ... options)
     {
         return performCallback(new HistoryCallback(personality, options));
     }
 
 
     public Integer getCurrentVersion(final String personality, final MigratoryOption ... options)
     {
         final MetadataInfo metadataInfo = getPersonalityStatus(personality, options);
         return (metadataInfo == null) ? null : metadataInfo.getEndVersion();
     }
 
 
     private static class StatusCallback implements HandleCallback<MetadataInfo>
     {
         private final String personality;
         private final MigratoryOption [] options;
 
         private StatusCallback(final String personality, final MigratoryOption [] options)
         {
             this.personality = personality;
             this.options = options;
         }
 
         @Override
         public MetadataInfo withHandle(final Handle handle) throws SQLException {
             return getStatusQuery(handle, personality, options).first();
         }
 
         private static Query<MetadataInfo> getStatusQuery(final Handle handle, final String personality, final MigratoryOption [] options)
         {
             final Query<MetadataInfo> query = handle.createQuery(METADATA_PREFIX + "query_status")
                 .define("include_internal", MigratoryOption.containsOption(MigratoryOption.INCLUDE_INTERNAL, options))
                 .define("include_failed", MigratoryOption.containsOption(MigratoryOption.INCLUDE_FAILED, options))
                 .define("personality_name", personality)
                 .bind("personality_name", personality)
                 .map(MetadataInfo.MAPPER);
 
             return query;
         }
     }
 
     private static class HistoryCallback implements HandleCallback<List<MetadataInfo>>
     {
         private final String personality;
         private final MigratoryOption [] options;
 
         private HistoryCallback(final String personality, final MigratoryOption [] options)
         {
             this.personality = personality;
             this.options = options;
         }
 
         @Override
         public List<MetadataInfo> withHandle(final Handle handle) throws SQLException {
             return getHistoryQuery(handle, personality, options).list();
         }
 
         private static Query<MetadataInfo> getHistoryQuery(final Handle handle, final String personality, final MigratoryOption [] options)
         {
             final Query<MetadataInfo> query = handle.createQuery(METADATA_PREFIX + "query_history")
                 .define("include_internal", MigratoryOption.containsOption(MigratoryOption.INCLUDE_INTERNAL, options))
                 .define("personality_name", personality)
                 .bind("personality_name", personality)
                 .map(MetadataInfo.MAPPER);
 
             return query;
         }
     }
 }
