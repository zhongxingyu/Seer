 /*
  * Copyright (C) 2010, Zenoss Inc.  All Rights Reserved.
  */
 
 package org.zenoss.zep.index.impl;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jdbc.core.RowCallbackHandler;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
 import org.springframework.transaction.annotation.Transactional;
 import org.zenoss.protobufs.zep.Zep.EventDetailItem;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.zep.EventPostProcessingPlugin;
 import org.zenoss.zep.PluginService;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.dao.EventDetailsConfigDao;
 import org.zenoss.zep.dao.IndexMetadata;
 import org.zenoss.zep.dao.IndexMetadataDao;
 import org.zenoss.zep.dao.impl.DaoUtils;
 import org.zenoss.zep.dao.impl.EventDaoHelper;
 import org.zenoss.zep.dao.impl.EventSummaryRowMapper;
 import org.zenoss.zep.index.EventIndexDao;
 import org.zenoss.zep.index.EventIndexer;
 
 import javax.sql.DataSource;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.concurrent.TimeUnit;
 
 import static org.zenoss.zep.dao.impl.EventConstants.*;
 
 public class EventIndexerImpl implements EventIndexer {
     private static final Logger logger = LoggerFactory.getLogger(EventIndexerImpl.class);
 
     private static final int INDEX_LIMIT = 100;
     
     private final NamedParameterJdbcTemplate template;
     private EventIndexDao eventSummaryIndexDao;
     private EventIndexDao eventArchiveIndexDao;
     private PluginService pluginService;
     private IndexMetadataDao indexMetadataDao;
     private EventSummaryRowMapper eventSummaryRowMapper;
     private EventDetailsConfigDao eventDetailsConfigDao;
     private byte[] indexVersionHash;
     private final long indexRangeMillis;
 
     public EventIndexerImpl(DataSource dataSource, int indexIntervalSeconds, int indexIntervalWindow)
             throws ZepException {
         this.template = new NamedParameterJdbcTemplate(dataSource);
         this.indexRangeMillis = TimeUnit.SECONDS.toMillis(indexIntervalSeconds * indexIntervalWindow);
     }
 
     public void setEventSummaryIndexDao(EventIndexDao eventSummaryIndexDao) {
         this.eventSummaryIndexDao = eventSummaryIndexDao;
     }
 
     public void setEventArchiveIndexDao(EventIndexDao eventIndexDao) {
         this.eventArchiveIndexDao = eventIndexDao;
     }
 
     public void setEventDaoHelper(EventDaoHelper eventDaoHelper) {
         this.eventSummaryRowMapper = new EventSummaryRowMapper(eventDaoHelper);
     }
     
     public void setPluginService(PluginService pluginService) {
         this.pluginService = pluginService;
     }
 
     public void setIndexMetadataDao(IndexMetadataDao indexMetadataDao) {
         this.indexMetadataDao = indexMetadataDao;
     }
 
     public void setEventDetailsConfigDao(EventDetailsConfigDao eventDetailsConfigDao) {
         this.eventDetailsConfigDao = eventDetailsConfigDao;
     }
 
     private static byte[] calculateIndexVersionHash(Map<String,EventDetailItem> detailItems) throws ZepException {
         TreeMap<String,EventDetailItem> sorted = new TreeMap<String,EventDetailItem>(detailItems);
         StringBuilder indexConfigStr = new StringBuilder();
         for (EventDetailItem item : sorted.values()) {
             // Only key and type affect the indexing behavior - ignore changes to display name
             indexConfigStr.append('|');
             indexConfigStr.append(item.getKey());
             indexConfigStr.append('|');
             indexConfigStr.append(item.getType().name());
             indexConfigStr.append('|');
         }
         if (indexConfigStr.length() == 0) {
             return null;
         }
         return DaoUtils.sha1(indexConfigStr.toString());
     }
 
     @Override
     @Transactional
     public synchronized void init() throws ZepException {
         Map<String,EventDetailItem> detailItems = this.eventDetailsConfigDao.getEventDetailItemsByName();
         for (EventDetailItem item : detailItems.values()) {
             logger.info("Indexed event detail: {}", item);
         }
         this.indexVersionHash = calculateIndexVersionHash(detailItems);
         this.eventSummaryIndexDao.setIndexDetails(detailItems);
         this.eventArchiveIndexDao.setIndexDetails(detailItems);
 
         rebuildIndex(this.eventSummaryIndexDao, TABLE_EVENT_SUMMARY);
         rebuildIndex(this.eventArchiveIndexDao, TABLE_EVENT_ARCHIVE);
     }
 
     private void rebuildIndex(final EventIndexDao dao, final String tableName)
             throws ZepException {
         final IndexMetadata indexMetadata = this.indexMetadataDao.findIndexMetadata(dao.getName());
         final int numDocs = dao.getNumDocs();
         
         // Rebuild index if we detect that we have never indexed before.
         if (indexMetadata == null) {
             if (numDocs > 0) {
                 logger.info("Inconsistent state between index and database. Clearing index.");
                 dao.clear();
             }
             /* Recreate the index */
             recreateIndex(dao, tableName);
         }
         // Rebuild index if the version changed.
         else if (indexVersionChanged(indexMetadata)) {
             dao.reindex();
             this.indexMetadataDao.updateIndexVersion(dao.getName(), IndexConstants.INDEX_VERSION,
                     this.indexVersionHash);
         }
         // Rebuild index if it has potentially been manually wiped from disk.
         else if (numDocs == 0) {
             /* Recreate the index */
             recreateIndex(dao, tableName);
         }
     }
 
     private boolean indexVersionChanged(IndexMetadata indexMetadata) {
         boolean changed = false;
         if (IndexConstants.INDEX_VERSION != indexMetadata.getIndexVersion()) {
             logger.info("Index version changed: previous={}, new={}", indexMetadata.getIndexVersion(),
                     IndexConstants.INDEX_VERSION);
             changed = true;
         }
         else if (!Arrays.equals(this.indexVersionHash, indexMetadata.getIndexVersionHash())) {
             logger.info("Index configuration changed.");
             changed = true;
         }
         return changed;
     }
 
     private void recreateIndex(final EventIndexDao dao, String tableName) throws ZepException {
         logger.info("Recreating index for table {}", tableName);
         
         final Map<String,Integer> fields = Collections.singletonMap(COLUMN_INDEXED, 1);
         final String sql = "SELECT * FROM " + tableName + " WHERE indexed=:indexed";
         
         final int[] numRows = new int[1];
         this.template.query(sql, fields, new RowCallbackHandler() {
             @Override
             public void processRow(ResultSet rs) throws SQLException {
                 final EventSummary summary = eventSummaryRowMapper.mapRow(rs, rs.getRow());
                 try {
                     dao.stage(summary);
                 } catch (ZepException e) {
                     throw new SQLException(e.getLocalizedMessage(), e);
                 }
                 ++numRows[0];
             }
         });
         if (numRows[0] > 0) {
             logger.info("Committing changes to index on table: {}", tableName);
             dao.commit(true);
         }
         this.indexMetadataDao.updateIndexVersion(dao.getName(), IndexConstants.INDEX_VERSION, this.indexVersionHash);
         logger.info("Finished recreating index for {} events on table: {}", numRows[0], tableName);
     }
 
     @Override
     @Transactional
     public synchronized int index(long throughTime) throws ZepException {
         int numIndexed = doIndex(eventSummaryIndexDao, TABLE_EVENT_SUMMARY, throughTime);
         if (numIndexed == 0) {
             numIndexed = doIndex(eventArchiveIndexDao, TABLE_EVENT_ARCHIVE, throughTime);
         }
         return numIndexed;
     }
 
     private int doIndex(final EventIndexDao dao, final String tableName, long throughTime) throws ZepException {
         final Map<String,Object> fields = new HashMap<String,Object>();
         fields.put("_min_update_time", throughTime - this.indexRangeMillis);
         fields.put("_max_update_time", throughTime);
         fields.put(COLUMN_INDEXED, 0);
         fields.put("_limit", INDEX_LIMIT);
 
        final String sql = "SELECT * FROM " + tableName + " WHERE update_time > :_min_update_time AND " + 
                 "update_time <= :_max_update_time AND indexed = :indexed LIMIT :_limit FOR UPDATE";
         final List<EventPostProcessingPlugin> plugins = this.pluginService.getPostProcessingPlugins();
         final List<byte[]> uuids = this.template.query(sql, fields, new RowMapper<byte[]>() {
             @Override
             public byte[] mapRow(ResultSet rs, int rowNum) throws SQLException {
                 final EventSummary summary = eventSummaryRowMapper.mapRow(rs, rowNum);
                 try {
                     dao.stage(summary);
                 } catch (ZepException e) {
                     throw new SQLException(e);
                 }
 
                 for (EventPostProcessingPlugin plugin : plugins) {
                     try {
                         plugin.processEvent(summary);
                     } catch (Exception e) {
                         // Post-processing plug-in failures are not fatal errors.
                         logger.warn("Failed to run post-processing plug-in on event: " + summary, e);
                     }
                 }
                 return DaoUtils.uuidToBytes(summary.getUuid());
             }
         });
 
         final int numIndexed = uuids.size();
         if (numIndexed > 0) {
             dao.commit();
             
             final Map<String,Object> updateFields = new HashMap<String,Object>();
             updateFields.put(COLUMN_INDEXED, 1);
             updateFields.put("_uuids", uuids);
             final String updateSql = "UPDATE " + tableName + " SET indexed=:indexed WHERE uuid IN (:_uuids)";
             template.update(updateSql, updateFields);
 
             logger.debug("Completed indexing {} events on {}", numIndexed, tableName);
         }
 
         return numIndexed;
     }
 }
