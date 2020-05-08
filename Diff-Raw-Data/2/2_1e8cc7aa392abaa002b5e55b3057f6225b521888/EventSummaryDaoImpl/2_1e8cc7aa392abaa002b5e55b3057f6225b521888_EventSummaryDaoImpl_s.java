 /*****************************************************************************
  * 
  * Copyright (C) Zenoss, Inc. 2010-2012, all rights reserved.
  * 
  * This content is made available according to terms specified in
  * License.zenoss under the directory where your Zenoss product is installed.
  * 
  ****************************************************************************/
 
 
 package org.zenoss.zep.dao.impl;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.DataAccessException;
 import org.springframework.dao.DuplicateKeyException;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.RowMapperResultSetExtractor;
 import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
 import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
 import org.springframework.jdbc.support.MetaDataAccessException;
 import org.springframework.transaction.support.TransactionSynchronizationAdapter;
 import org.springframework.transaction.support.TransactionSynchronizationManager;
 import org.springframework.util.StringUtils;
 import org.zenoss.protobufs.JsonFormat;
 import org.zenoss.protobufs.model.Model.ModelElementType;
 import org.zenoss.protobufs.zep.Zep.Event;
 import org.zenoss.protobufs.zep.Zep.EventActor;
 import org.zenoss.protobufs.zep.Zep.EventAuditLog;
 import org.zenoss.protobufs.zep.Zep.EventDetail;
 import org.zenoss.protobufs.zep.Zep.EventDetailSet;
 import org.zenoss.protobufs.zep.Zep.EventNote;
 import org.zenoss.protobufs.zep.Zep.EventSeverity;
 import org.zenoss.protobufs.zep.Zep.EventStatus;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.protobufs.zep.Zep.EventSummaryOrBuilder;
 import org.zenoss.zep.StatisticsService;
 import org.zenoss.zep.UUIDGenerator;
 import org.zenoss.zep.ZepConstants;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.annotations.TransactionalReadOnly;
 import org.zenoss.zep.annotations.TransactionalRollbackAllExceptions;
 import org.zenoss.zep.dao.EventSummaryDao;
 import org.zenoss.zep.dao.impl.compat.DatabaseCompatibility;
 import org.zenoss.zep.dao.impl.compat.DatabaseType;
 import org.zenoss.zep.dao.impl.compat.NestedTransactionCallback;
 import org.zenoss.zep.dao.impl.compat.NestedTransactionContext;
 import org.zenoss.zep.dao.impl.compat.NestedTransactionService;
 import org.zenoss.zep.dao.impl.compat.TypeConverter;
 import org.zenoss.zep.dao.impl.compat.TypeConverterUtils;
 import org.zenoss.zep.plugins.EventPreCreateContext;
 import org.zenoss.zep.dao.impl.SimpleJdbcTemplateProxy;
 
 import java.lang.reflect.Proxy;
 import javax.sql.DataSource;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import static org.zenoss.zep.dao.impl.EventConstants.*;
 
 public class EventSummaryDaoImpl implements EventSummaryDao {
 
     private static final Logger logger = LoggerFactory.getLogger(EventSummaryDaoImpl.class);
 
     private final DataSource dataSource;
 
     private final SimpleJdbcOperations template;
 
     private final SimpleJdbcInsert insert;
 
     private volatile List<String> archiveColumnNames;
 
     private EventDaoHelper eventDaoHelper;
 
     private UUIDGenerator uuidGenerator;
 
     private DatabaseCompatibility databaseCompatibility;
 
     private TypeConverter<String> uuidConverter;
 
     private NestedTransactionService nestedTransactionService;
 
     private RowMapper<EventSummaryOrBuilder> eventDedupMapper;
 
     private StatisticsService statisticsService;
 
     public EventSummaryDaoImpl(DataSource dataSource) throws MetaDataAccessException {
         this.dataSource = dataSource;
         this.template = (SimpleJdbcOperations) Proxy.newProxyInstance(SimpleJdbcOperations.class.getClassLoader(), 
         		new Class[] {SimpleJdbcOperations.class}, new SimpleJdbcTemplateProxy(dataSource));
         this.insert = new SimpleJdbcInsert(dataSource).withTableName(TABLE_EVENT_SUMMARY);
     }
 
     public void setEventDaoHelper(EventDaoHelper eventDaoHelper) {
         this.eventDaoHelper = eventDaoHelper;
     }
 
     public void setUuidGenerator(UUIDGenerator uuidGenerator) {
         this.uuidGenerator = uuidGenerator;
     }
 
     public void setDatabaseCompatibility(final DatabaseCompatibility databaseCompatibility) {
         this.databaseCompatibility = databaseCompatibility;
         this.uuidConverter = databaseCompatibility.getUUIDConverter();
 
         // When we perform de-duping of events, we select a subset of just the fields we care about to determine
         // the de-duping behavior (depending on the timestamps on the event, we may perform merging or update either
         // the first_seen or last_seen dates appropriately). This mapper converts the subset of fields to an
         // EventSummaryOrBuilder object which has convenient accessor methods to retrieve the fields by name.
         this.eventDedupMapper = new RowMapper<EventSummaryOrBuilder>() {
             @Override
             public EventSummaryOrBuilder mapRow(ResultSet rs, int rowNum) throws SQLException {
                 final TypeConverter<Long> timestampConverter = databaseCompatibility.getTimestampConverter();
                 final EventSummary.Builder oldSummaryBuilder = EventSummary.newBuilder();
                 oldSummaryBuilder.setCount(rs.getInt(COLUMN_EVENT_COUNT));
                 oldSummaryBuilder.setFirstSeenTime(timestampConverter.fromDatabaseType(rs, COLUMN_FIRST_SEEN));
                 oldSummaryBuilder.setLastSeenTime(timestampConverter.fromDatabaseType(rs, COLUMN_LAST_SEEN));
                 oldSummaryBuilder.setStatus(EventStatus.valueOf(rs.getInt(COLUMN_STATUS_ID)));
                 oldSummaryBuilder.setUuid(uuidConverter.fromDatabaseType(rs, COLUMN_UUID));
 
                 final Event.Builder occurrenceBuilder = oldSummaryBuilder.addOccurrenceBuilder(0);
                 final String detailsJson = rs.getString(COLUMN_DETAILS_JSON);
                 if (detailsJson != null) {
                     try {
                         occurrenceBuilder.addAllDetails(JsonFormat.mergeAllDelimitedFrom(detailsJson,
                                 EventDetail.getDefaultInstance()));
                     } catch (IOException e) {
                         throw new SQLException(e.getLocalizedMessage(), e);
                     }
                 }
                 return oldSummaryBuilder;
             }
         };
     }
 
     public void setNestedTransactionService(NestedTransactionService nestedTransactionService) {
         this.nestedTransactionService = nestedTransactionService;
     }
 
     public void setStatisticsService(final StatisticsService statisticsService) {
         this.statisticsService = statisticsService;
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public String create(Event event, EventPreCreateContext context) throws ZepException {
         final TypeConverter<Long> timestampConverter = databaseCompatibility.getTimestampConverter();
         final Map<String, Object> fields = eventDaoHelper.createOccurrenceFields(event);
         final long created = event.getCreatedTime();
         final long firstSeen = event.hasFirstSeenTime() ? event.getFirstSeenTime() : created;
         final long updateTime = System.currentTimeMillis();
 
         /*
          * Clear events are dropped if they don't clear any corresponding events.
          */
         final List<String> clearedEventUuids;
         if (event.getSeverity() == EventSeverity.SEVERITY_CLEAR) {
             clearedEventUuids = this.clearEvents(event, context);
             if (clearedEventUuids.isEmpty()) {
                 logger.debug("Clear event didn't clear any events, dropping: {}", event);
                 return null;
             }
             // Clear events always get created in CLOSED status
             if (event.getStatus() != EventStatus.STATUS_CLOSED) {
                 event = Event.newBuilder(event).setStatus(EventStatus.STATUS_CLOSED).build();
             }
         }
         else {
             clearedEventUuids = Collections.emptyList();
             fields.put(COLUMN_CLEAR_FINGERPRINT_HASH, EventDaoUtils.createClearHash(event,
                     context.getClearFingerprintGenerator()));
         }
 
         fields.put(COLUMN_STATUS_ID, event.getStatus().getNumber());
         fields.put(COLUMN_UPDATE_TIME, timestampConverter.toDatabaseType(updateTime));
         fields.put(COLUMN_FIRST_SEEN, timestampConverter.toDatabaseType(firstSeen));
         fields.put(COLUMN_STATUS_CHANGE, timestampConverter.toDatabaseType(created));
         fields.put(COLUMN_LAST_SEEN, timestampConverter.toDatabaseType(created));
         fields.put(COLUMN_EVENT_COUNT, event.getCount());
 
         final String createdUuid = this.uuidGenerator.generate().toString();
         fields.put(COLUMN_UUID, uuidConverter.toDatabaseType(createdUuid));
 
         /*
          * Closed events have a unique fingerprint_hash in summary to allow multiple rows
          * but only allow one active event (where the de-duplication occurs).
          */
         if (ZepConstants.CLOSED_STATUSES.contains(event.getStatus())) {
             String uniqueFingerprint = (String) fields.get(COLUMN_FINGERPRINT) + '|' + updateTime;
             fields.put(COLUMN_FINGERPRINT_HASH, DaoUtils.sha1(uniqueFingerprint));
         }
         else {
             fields.put(COLUMN_FINGERPRINT_HASH, DaoUtils.sha1((String)fields.get(COLUMN_FINGERPRINT)));
         }
 
         final String sql = "SELECT event_count,first_seen,last_seen,details_json,status_id,uuid FROM event_summary" +
                 " WHERE fingerprint_hash=? FOR UPDATE";
         final List<EventSummaryOrBuilder> oldSummaryList = this.template.getJdbcOperations().query(sql,
                 new RowMapperResultSetExtractor<EventSummaryOrBuilder>(this.eventDedupMapper, 1),
                 fields.get(COLUMN_FINGERPRINT_HASH));
         final String uuid;
         if (!oldSummaryList.isEmpty()) {
             uuid = dedupEvent(oldSummaryList.get(0), event, fields);
         }
         else {
             insert.execute(fields);
             uuid = createdUuid;
 
             // Add the uuid to the event_summary_index_queue
             this.indexSignal(uuid, updateTime);
         }
 
         // Mark cleared events as cleared by this event
         if (!clearedEventUuids.isEmpty()) {
             final EventSummaryUpdateFields updateFields = new EventSummaryUpdateFields();
             updateFields.setClearedByEventUuid(uuid);
             update(clearedEventUuids, EventStatus.STATUS_CLEARED, updateFields, ZepConstants.OPEN_STATUSES);
         }
         return uuid;
     }
 
     private String dedupEvent(EventSummaryOrBuilder oldSummary, Event event, Map<String,Object> insertFields)
             throws ZepException {
         TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
             @Override
             public void afterCommit() {
                 statisticsService.addToDedupedEventCount(1);
             }
         });
         final Map<String,Object> updateFields = getUpdateFields(oldSummary, event, insertFields);
         final StringBuilder updateSql = new StringBuilder("UPDATE event_summary SET ");
         for (Iterator<String> it = updateFields.keySet().iterator(); it.hasNext(); ) {
             final String fieldName = it.next();
             updateSql.append(fieldName).append("=:").append(fieldName);
             if (it.hasNext()) {
                 updateSql.append(',');
             }
         }
         updateSql.append(" WHERE fingerprint_hash=:fingerprint_hash");
         updateFields.put(COLUMN_FINGERPRINT_HASH, insertFields.get(COLUMN_FINGERPRINT_HASH));
 
         long updateTime = System.currentTimeMillis();
         String indexSql = "INSERT INTO event_summary_index_queue (uuid, update_time) " +
                     "SELECT uuid, " + String.valueOf(updateTime) + " " +
                     "FROM event_summary " + 
                     "WHERE fingerprint_hash=:fingerprint_hash";
         this.template.update(indexSql, updateFields);
 
         this.template.update(updateSql.toString(), updateFields);
         return oldSummary.getUuid();
     }
 
     /**
      * When an event is de-duped, if the event occurrence has a created time greater than or equal to the current
      * last_seen for the event summary, these fields from the event summary row are overwritten by values from the new
      * event occurrence. Special handling is performed when de-duping for event status and event details.
      */
     private static final List<String> UPDATE_FIELD_NAMES = Arrays.asList(COLUMN_EVENT_GROUP_ID,
             COLUMN_EVENT_CLASS_ID, COLUMN_EVENT_CLASS_KEY_ID, COLUMN_EVENT_CLASS_MAPPING_UUID, COLUMN_EVENT_KEY_ID,
             COLUMN_SEVERITY_ID, COLUMN_ELEMENT_UUID, COLUMN_ELEMENT_TYPE_ID, COLUMN_ELEMENT_IDENTIFIER,
             COLUMN_ELEMENT_TITLE, COLUMN_ELEMENT_SUB_UUID, COLUMN_ELEMENT_SUB_TYPE_ID, COLUMN_ELEMENT_SUB_IDENTIFIER,
             COLUMN_ELEMENT_SUB_TITLE, COLUMN_LAST_SEEN, COLUMN_MONITOR_ID, COLUMN_AGENT_ID, COLUMN_SYSLOG_FACILITY,
             COLUMN_SYSLOG_PRIORITY, COLUMN_NT_EVENT_CODE, COLUMN_CLEAR_FINGERPRINT_HASH, COLUMN_SUMMARY, COLUMN_MESSAGE,
             COLUMN_TAGS_JSON);
 
     private Map<String,Object> getUpdateFields(EventSummaryOrBuilder oldSummary, Event occurrence,
                                                 Map<String,Object> insertFields) throws ZepException {
         TypeConverter<Long> timestampConverter = databaseCompatibility.getTimestampConverter();
         Map<String,Object> updateFields = new HashMap<String, Object>();
 
         // Always increment count
         updateFields.put(COLUMN_EVENT_COUNT, oldSummary.getCount() + occurrence.getCount());
 
         updateFields.put(COLUMN_UPDATE_TIME, insertFields.get(COLUMN_UPDATE_TIME));
 
         if (occurrence.getCreatedTime() >= oldSummary.getLastSeenTime()) {
 
             for (String fieldName : UPDATE_FIELD_NAMES) {
                 updateFields.put(fieldName, insertFields.get(fieldName));
             }
 
             // Update status except for ACKNOWLEDGED -> {NEW|SUPPRESSED}
             // Stays in ACKNOWLEDGED in these cases
             boolean updateStatus = true;
             EventStatus oldStatus = oldSummary.getStatus();
             EventStatus newStatus = EventStatus.valueOf((Integer) insertFields.get(COLUMN_STATUS_ID));
             switch (oldStatus) {
                 case STATUS_ACKNOWLEDGED:
                     switch (newStatus) {
                         case STATUS_NEW:
                         case STATUS_SUPPRESSED:
                             updateStatus = false;
                             break;
                     }
                     break;
             }
             if (updateStatus && oldStatus != newStatus) {
                 updateFields.put(COLUMN_STATUS_ID, insertFields.get(COLUMN_STATUS_ID));
                 updateFields.put(COLUMN_STATUS_CHANGE, timestampConverter.toDatabaseType(occurrence.getCreatedTime()));
             }
 
             // Merge event details
             List<EventDetail> newDetails = occurrence.getDetailsList();
             if (!newDetails.isEmpty()) {
                 final String mergedDetails = eventDaoHelper.mergeDetailsToJson(
                         oldSummary.getOccurrence(0).getDetailsList(), newDetails);
                 updateFields.put(COLUMN_DETAILS_JSON, mergedDetails);
             }
         }
         else {
             // This is the case where the event that we're processing is OLDER
             // than the last seen time on the summary.
 
             // Merge event details - order swapped b/c of out of order event
             List<EventDetail> oldDetails = occurrence.getDetailsList();
             if (!oldDetails.isEmpty()) {
                 final String mergedDetails = eventDaoHelper.mergeDetailsToJson(
                         oldDetails, oldSummary.getOccurrence(0).getDetailsList());
                 updateFields.put(COLUMN_DETAILS_JSON, mergedDetails);
             }
         }
 
         long firstSeen = occurrence.hasFirstSeenTime() ? occurrence.getFirstSeenTime() : occurrence.getCreatedTime();
         if (firstSeen < oldSummary.getFirstSeenTime()) {
             updateFields.put(COLUMN_FIRST_SEEN, timestampConverter.toDatabaseType(firstSeen));
         }
         return updateFields;
     }
 
     private List<String> clearEvents(Event event, EventPreCreateContext context)
             throws ZepException {
         TypeConverter<Long> timestampConverter = databaseCompatibility.getTimestampConverter();
         final List<byte[]> clearHashes = EventDaoUtils.createClearHashes(event, context);
         if (clearHashes.isEmpty()) {
             logger.debug("Clear event didn't contain any clear hashes: {}, {}", event, context);
             return Collections.emptyList();
         }
         final long lastSeen = event.getCreatedTime();
         
         Map<String,Object> fields = new HashMap<String,Object>(2);
         fields.put("_clear_created_time", timestampConverter.toDatabaseType(lastSeen));
         fields.put("_clear_hashes", clearHashes);
         fields.put("_closed_status_ids", CLOSED_STATUS_IDS);
 
         long updateTime = System.currentTimeMillis();
         String indexSql = "INSERT INTO event_summary_index_queue (uuid, update_time) " 
                 + "SELECT uuid, " + String.valueOf(updateTime) + " FROM event_summary " +
                 "WHERE last_seen <= :_clear_created_time " +
                 "AND clear_fingerprint_hash IN (:_clear_hashes) " +
                 "AND status_id NOT IN (:_closed_status_ids) ";
         this.template.update(indexSql, fields); 
 
         /* Find events that this clear event would clear. */
         final String sql = "SELECT uuid FROM event_summary " + 
                 "WHERE last_seen <= :_clear_created_time " +
                 "AND clear_fingerprint_hash IN (:_clear_hashes) " +
                 "AND status_id NOT IN (:_closed_status_ids) " + 
                 "FOR UPDATE";
 
         final List<String> results = this.template.query(sql, new RowMapper<String>() {
             @Override
             public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                 return uuidConverter.fromDatabaseType(rs, COLUMN_UUID);
             }
         }, fields);
         TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
             @Override
             public void afterCommit() {
                 statisticsService.addToClearedEventCount(results.size());
             }
         });
         return results;
     }
 
     /**
      * When re-identifying or de-identifying events, we recalculate the clear_fingerprint_hash for the event to either
      * include (re-identify) or exclude (de-identify) the UUID of the sub_element. This mapper retrieves a subset of
      * fields for the event in order to recalculate the clear_fingerprint_hash.
      */
     private static class IdentifyMapper implements RowMapper<Map<String,Object>>
     {
         private final Map<String,Object> fields;
         private final String elementSubUuid;
 
         public IdentifyMapper(Map<String,Object> fields, String elementSubUuid) {
             this.fields = Collections.unmodifiableMap(fields);
             this.elementSubUuid = elementSubUuid;
         }
 
         @Override
         public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
             Map<String,Object> updateFields = new HashMap<String, Object>(fields);
             Event.Builder event = Event.newBuilder();
             EventActor.Builder actor = event.getActorBuilder();
             actor.setElementIdentifier(rs.getString(COLUMN_ELEMENT_IDENTIFIER));
             String elementSubIdentifier = rs.getString(COLUMN_ELEMENT_SUB_IDENTIFIER);
             if (elementSubIdentifier != null) {
                 actor.setElementSubIdentifier(elementSubIdentifier);
             }
             if (this.elementSubUuid != null) {
                 actor.setElementSubUuid(this.elementSubUuid);
             }
             event.setEventClass(rs.getString("event_class_name"));
             String eventKey = rs.getString("event_key_name");
             if (eventKey != null) {
                 event.setEventKey(eventKey);
             }
 
             updateFields.put(COLUMN_UUID, rs.getObject(COLUMN_UUID));
             updateFields.put(COLUMN_CLEAR_FINGERPRINT_HASH, EventDaoUtils.createClearHash(event.build()));
             return updateFields;
         }
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int reidentify(ModelElementType type, String id, String uuid, String title, String parentUuid)
             throws ZepException {
         TypeConverter<Long> timestampConverter = databaseCompatibility.getTimestampConverter();
         long updateTime = System.currentTimeMillis();
 
         final Map<String, Object> fields = new HashMap<String, Object>();
         fields.put("_uuid", uuidConverter.toDatabaseType(uuid));
         fields.put("_uuid_str", uuid);
         fields.put("_type_id", type.getNumber());
         fields.put("_id", id);
         fields.put("_title", DaoUtils.truncateStringToUtf8(title, EventConstants.MAX_ELEMENT_TITLE));
         fields.put(COLUMN_UPDATE_TIME, timestampConverter.toDatabaseType(updateTime));
 
         String indexSql = "INSERT INTO event_summary_index_queue (uuid, update_time) " 
                 + "SELECT uuid, " + String.valueOf(updateTime) + " FROM event_summary " 
                 + "WHERE element_uuid IS NULL AND element_type_id=:_type_id AND element_identifier=:_id"; 
         this.template.update(indexSql, fields); 
 
         int numRows = 0;
         String updateSql = "UPDATE event_summary SET element_uuid=:_uuid, element_title=:_title," +
                 " update_time=:update_time WHERE element_uuid IS NULL AND element_type_id=:_type_id" +
                 " AND element_identifier=:_id";
         numRows += this.template.update(updateSql, fields);
 
         if (parentUuid != null) {
             fields.put("_parent_uuid", uuidConverter.toDatabaseType(parentUuid));
             indexSql = "INSERT INTO event_summary_index_queue (uuid, update_time) " +
                     "SELECT es.uuid, " + String.valueOf(updateTime) + " " +
                     "FROM event_summary es INNER JOIN event_class ON es.event_class_id = event_class.id " +
                     "LEFT JOIN event_key ON es.event_key_id = event_key.id " +
                     "WHERE es.element_uuid=:_parent_uuid AND es.element_sub_uuid IS NULL AND " +
                     "es.element_sub_type_id=:_type_id AND es.element_sub_identifier=:_id";
             this.template.update(indexSql, fields);
 
             String selectSql = "SELECT uuid,element_identifier,element_sub_identifier," +
                     "event_class.name AS event_class_name,event_key.name AS event_key_name FROM event_summary es" +
                     " INNER JOIN event_class ON es.event_class_id = event_class.id" +
                     " LEFT JOIN event_key on es.event_key_id = event_key.id" +
                     " WHERE es.element_uuid=:_parent_uuid AND es.element_sub_uuid IS NULL" +
                     " AND es.element_sub_type_id=:_type_id AND es.element_sub_identifier=:_id FOR UPDATE";
             // MySQL locks all joined rows, PostgreSQL requires you to specify the rows from each table to lock
             if (this.databaseCompatibility.getDatabaseType() == DatabaseType.POSTGRESQL) {
                 selectSql += " OF es";
             }
             List<Map<String,Object>> updateFields = this.template.query(selectSql, new IdentifyMapper(fields, uuid),
                     fields);
 
             String updateSubElementSql = "UPDATE event_summary SET element_sub_uuid=:_uuid, " +
                     "element_sub_title=:_title, update_time=:update_time, " +
                     "clear_fingerprint_hash=:clear_fingerprint_hash WHERE uuid=:uuid";
             int[] updated = this.template.batchUpdate(updateSubElementSql,
                     updateFields.toArray(new Map[updateFields.size()]));
             for (int updatedRows : updated) {
                 numRows += updatedRows;
             }
         }
         return numRows;
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int deidentify(String uuid) throws ZepException {
         TypeConverter<Long> timestampConverter = databaseCompatibility.getTimestampConverter();
         long updateTime = System.currentTimeMillis();
 
         final Map<String,Object> fields = new HashMap<String,Object>(2);
         fields.put("_uuid", uuidConverter.toDatabaseType(uuid));
         fields.put(COLUMN_UPDATE_TIME, timestampConverter.toDatabaseType(updateTime));
 
         String indexSql = "INSERT INTO event_summary_index_queue (uuid, update_time) " 
                 + "SELECT uuid, " + String.valueOf(updateTime) + " FROM event_summary " 
                 + "WHERE element_uuid=:_uuid"; 
         this.template.update(indexSql, fields);
 
         int numRows = 0;
         String updateElementSql = "UPDATE event_summary SET element_uuid=NULL, update_time=:update_time" +
                 " WHERE element_uuid=:_uuid";
         numRows += this.template.update(updateElementSql, fields);
 
         indexSql = "INSERT INTO event_summary_index_queue (uuid, update_time) " 
                 + "SELECT uuid, " + String.valueOf(updateTime) + " FROM event_summary " 
                 + "WHERE element_sub_uuid=:_uuid"; 
         this.template.update(indexSql, fields);
 
         String selectSql = "SELECT uuid,element_identifier,element_sub_identifier," +
                 "event_class.name AS event_class_name,event_key.name AS event_key_name FROM event_summary es" +
                 " INNER JOIN event_class ON es.event_class_id = event_class.id" +
                 " LEFT JOIN event_key on es.event_key_id = event_key.id WHERE element_sub_uuid=:_uuid FOR UPDATE";
         // MySQL locks all joined rows, PostgreSQL requires you to specify the rows from each table to lock
         if (this.databaseCompatibility.getDatabaseType() == DatabaseType.POSTGRESQL) {
             selectSql += " OF es";
         }
         List<Map<String,Object>> updateFields = this.template.query(selectSql, new IdentifyMapper(fields, null),
                 fields);
 
         String updateSubElementSql = "UPDATE event_summary SET element_sub_uuid=NULL, update_time=:update_time, " +
                 "clear_fingerprint_hash=:clear_fingerprint_hash WHERE uuid=:uuid";
         int[] updated = this.template.batchUpdate(updateSubElementSql,
                 updateFields.toArray(new Map[updateFields.size()]));
         for (int updatedRows : updated) {
             numRows += updatedRows;
         }
         return numRows;
     }
 
     @Override
     @TransactionalReadOnly
     public EventSummary findByUuid(String uuid) throws ZepException {
         final Map<String,Object> fields = Collections.singletonMap(COLUMN_UUID, uuidConverter.toDatabaseType(uuid));
         List<EventSummary> summaries = this.template.query("SELECT * FROM event_summary WHERE uuid=:uuid",
                 new EventSummaryRowMapper(this.eventDaoHelper, this.databaseCompatibility), fields);
         return (summaries.size() > 0) ? summaries.get(0) : null;
     }
 
     @Override
     @TransactionalReadOnly
     public List<EventSummary> findByUuids(final List<String> uuids)
             throws ZepException {
         if (uuids.isEmpty()) {
             return Collections.emptyList();
         }
         Map<String, List<Object>> fields = Collections.singletonMap("uuids",
                 TypeConverterUtils.batchToDatabaseType(uuidConverter, uuids));
         return this.template.query("SELECT * FROM event_summary WHERE uuid IN(:uuids)",
                 new EventSummaryRowMapper(this.eventDaoHelper, this.databaseCompatibility), fields);
     }
 
     @Override
     @TransactionalReadOnly
     public List<EventSummary> listBatch(String startingUuid, long maxUpdateTime, int limit) throws ZepException {
         return this.eventDaoHelper.listBatch(this.template, TABLE_EVENT_SUMMARY, startingUuid, maxUpdateTime, limit);
     }
 
     private static final EnumSet<EventStatus> AUDIT_LOG_STATUSES = EnumSet.of(
             EventStatus.STATUS_NEW, EventStatus.STATUS_ACKNOWLEDGED, EventStatus.STATUS_CLOSED,
             EventStatus.STATUS_CLEARED);
 
     private static final List<Integer> CLOSED_STATUS_IDS = Arrays.asList(
             EventStatus.STATUS_AGED.getNumber(),
             EventStatus.STATUS_CLEARED.getNumber(),
             EventStatus.STATUS_CLOSED.getNumber());
 
     private static List<Integer> getSeverityIds(EventSeverity maxSeverity, boolean inclusiveSeverity) {
         List<Integer> severityIds = EventDaoHelper.getSeverityIdsLessThan(maxSeverity);
         if (inclusiveSeverity) {
             severityIds.add(maxSeverity.getNumber());
         }
         return severityIds;
     }
 
     @Override
     public long getAgeEligibleEventCount(long duration, TimeUnit unit, EventSeverity maxSeverity,
                                          boolean inclusiveSeverity) {
         List<Integer> severityIds = getSeverityIds(maxSeverity, inclusiveSeverity);
         // Aging disabled.
         if (severityIds.isEmpty()) {
             return 0;
         }
         String sql = "SELECT count(*) FROM event_summary WHERE status_id NOT IN (:_closed_status_ids) AND " +
                 "last_seen < :_last_seen AND severity_id IN (:_severity_ids)";
         Map <String, Object> fields = createSharedFields(duration, unit);
         fields.put("_severity_ids", severityIds);
         return template.queryForInt(sql, fields);
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int ageEvents(long agingInterval, TimeUnit unit,
                          EventSeverity maxSeverity, int limit, boolean inclusiveSeverity) throws ZepException {
         TypeConverter<Long> timestampConverter = databaseCompatibility.getTimestampConverter();
         long agingIntervalMs = unit.toMillis(agingInterval);
         if (agingIntervalMs < 0 || agingIntervalMs == Long.MAX_VALUE) {
             throw new ZepException("Invalid aging interval: " + agingIntervalMs);
         }
         if (limit <= 0) {
             throw new ZepException("Limit can't be negative: " + limit);
         }
         List<Integer> severityIds = getSeverityIds(maxSeverity, inclusiveSeverity);
         if (severityIds.isEmpty()) {
             logger.debug("Not aging events - min severity specified");
             return 0;
         }
         long now = System.currentTimeMillis();
         long ageTs = now - agingIntervalMs;
 
         Map<String, Object> fields = new HashMap<String, Object>();
         fields.put(COLUMN_STATUS_ID, EventStatus.STATUS_AGED.getNumber());
         fields.put(COLUMN_STATUS_CHANGE, timestampConverter.toDatabaseType(now));
         fields.put(COLUMN_UPDATE_TIME, timestampConverter.toDatabaseType(now));
         fields.put(COLUMN_LAST_SEEN, timestampConverter.toDatabaseType(ageTs));
         fields.put("_severity_ids", severityIds);
         fields.put("_closed_status_ids", CLOSED_STATUS_IDS);
         fields.put("_limit", limit);
 
         final String updateSql;
         if (databaseCompatibility.getDatabaseType() == DatabaseType.MYSQL) {
             String indexSql = "INSERT INTO event_summary_index_queue (uuid, update_time) " +
                     "SELECT uuid, " + String.valueOf(now) + " " +
                     "FROM event_summary " + 
                     " WHERE last_seen < :last_seen AND" +
                     " severity_id IN (:_severity_ids) AND" +
                     " status_id NOT IN (:_closed_status_ids) LIMIT :_limit";
             this.template.update(indexSql, fields);
 
             // Use UPDATE ... LIMIT
             updateSql = "UPDATE event_summary SET" +
                     " status_id=:status_id,status_change=:status_change,update_time=:update_time" +
                     " WHERE last_seen < :last_seen AND severity_id IN (:_severity_ids)" +
                     " AND status_id NOT IN (:_closed_status_ids) LIMIT :_limit";
         }
         else if (databaseCompatibility.getDatabaseType() == DatabaseType.POSTGRESQL) {
             String indexSql = "INSERT INTO event_summary_index_queue (uuid, update_time) " +
                     "SELECT uuid, " + String.valueOf(now) + " " +
                     "FROM event_summary " + 
                     " WHERE uuid IN (SELECT uuid FROM event_summary WHERE" +
                     " last_seen < :last_seen AND severity_id IN (:_severity_ids)" +
                     " AND status_id NOT IN (:_closed_status_ids) LIMIT :_limit)";
             this.template.update(indexSql, fields);
 
             // Use UPDATE ... WHERE pk IN (SELECT ... LIMIT)
             updateSql = "UPDATE event_summary SET" +
                     " status_id=:status_id,status_change=:status_change,update_time=:update_time" +
                     " WHERE uuid IN (SELECT uuid FROM event_summary WHERE" +
                     " last_seen < :last_seen AND severity_id IN (:_severity_ids)" +
                     " AND status_id NOT IN (:_closed_status_ids) LIMIT :_limit)";
         }
         else {
             throw new IllegalStateException("Unsupported database type: " + databaseCompatibility.getDatabaseType());
         }
         final int numRows = this.template.update(updateSql, fields);
         if (numRows > 0) {
             TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                 @Override
                 public void afterCommit() {
                     statisticsService.addToAgedEventCount(numRows);
                 }
             });
         }
         return numRows;
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int addNote(String uuid, EventNote note) throws ZepException {
         // Add the uuid to the event_summary_index_queue
         final long updateTime =  System.currentTimeMillis();
         this.indexSignal(uuid, updateTime); 
 
         return this.eventDaoHelper.addNote(TABLE_EVENT_SUMMARY, uuid, note, template);
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int updateDetails(String uuid, EventDetailSet details)
             throws ZepException {
         // Add the uuid to the event_summary_index_queue
         final long updateTime =  System.currentTimeMillis();
         this.indexSignal(uuid, updateTime); 
 
         return this.eventDaoHelper.updateDetails(TABLE_EVENT_SUMMARY, uuid, details.getDetailsList(), template);
     }
 
     private static class EventSummaryUpdateFields {
         private String currentUserUuid;
         private String currentUserName;
         private String clearedByEventUuid;
 
         public static final EventSummaryUpdateFields EMPTY_FIELDS = new EventSummaryUpdateFields();
 
         public Map<String,Object> toMap(TypeConverter<String> uuidConverter) {
             Map<String,Object> m = new HashMap<String,Object>();
             Object currentUuid = null;
             if (this.currentUserUuid != null) {
                 currentUuid = uuidConverter.toDatabaseType(this.currentUserUuid);
             }
             m.put(COLUMN_CURRENT_USER_UUID, currentUuid);
             m.put(COLUMN_CURRENT_USER_NAME, currentUserName);
 
             Object clearedUuid = null;
             if (this.clearedByEventUuid != null) {
                 clearedUuid = uuidConverter.toDatabaseType(this.clearedByEventUuid);
             }
             m.put(COLUMN_CLEARED_BY_EVENT_UUID, clearedUuid);
             return m;
         }
 
         public String getCurrentUserUuid() {
             return currentUserUuid;
         }
 
         public void setCurrentUserUuid(String currentUserUuid) {
             this.currentUserUuid = currentUserUuid;
         }
 
         public String getCurrentUserName() {
             return currentUserName;
         }
 
         public void setCurrentUserName(String currentUserName) {
             if (currentUserName == null) {
                 this.currentUserName = null;
             }
             else {
                 this.currentUserName = DaoUtils.truncateStringToUtf8(currentUserName, MAX_CURRENT_USER_NAME);
             }
         }
 
         public String getClearedByEventUuid() {
             return clearedByEventUuid;
         }
 
         public void setClearedByEventUuid(String clearedByEventUuid) {
             this.clearedByEventUuid = clearedByEventUuid;
         }
     }
 
     private int update(final List<String> uuids, final EventStatus status, final EventSummaryUpdateFields updateFields,
                        final Collection<EventStatus> currentStatuses) throws ZepException {
         if (uuids.isEmpty()) {
             return 0;
         }
         TypeConverter<Long> timestampConverter = databaseCompatibility.getTimestampConverter();
         final long now = System.currentTimeMillis();
         final Map<String,Object> fields = updateFields.toMap(uuidConverter);
         fields.put(COLUMN_STATUS_ID, status.getNumber());
         fields.put(COLUMN_STATUS_CHANGE, timestampConverter.toDatabaseType(now));
         fields.put(COLUMN_UPDATE_TIME, timestampConverter.toDatabaseType(now));
         fields.put("_uuids", TypeConverterUtils.batchToDatabaseType(uuidConverter, uuids));
         // If we aren't acknowledging events, we need to clear out the current user name / UUID values
         if (status != EventStatus.STATUS_ACKNOWLEDGED) {
             fields.put(COLUMN_CURRENT_USER_NAME, null);
             fields.put(COLUMN_CURRENT_USER_UUID, null);
         }
 
         StringBuilder sb = new StringBuilder("SELECT uuid,fingerprint,audit_json FROM event_summary");
         StringBuilder sbw = new StringBuilder(" WHERE uuid IN (:_uuids)");
         /*
          * This is required to support well-defined transitions between states. We only allow
          * updates to move events between states that make sense.
          */
         if (!currentStatuses.isEmpty()) {
             final List<Integer> currentStatusIds = new ArrayList<Integer>(currentStatuses.size());
             for (EventStatus currentStatus : currentStatuses) {
                 currentStatusIds.add(currentStatus.getNumber());
             }
             fields.put("_current_status_ids", currentStatusIds);
             sbw.append(" AND status_id IN (:_current_status_ids)");
         }
         /*
          * Disallow acknowledging an event again as the same user name / user uuid. If the event is not
          * already acknowledged, we will allow it to be acknowledged (assuming state filter above doesn't
          * exclude it). Otherwise, we will only acknowledge it again if *either* the user name or user
          * uuid has changed. If neither of these fields have changed, it is a NO-OP.
          */
         if (status == EventStatus.STATUS_ACKNOWLEDGED) {
             fields.put("_status_acknowledged", EventStatus.STATUS_ACKNOWLEDGED.getNumber());
             sbw.append(" AND (status_id != :_status_acknowledged OR ");
             if (updateFields.getCurrentUserName() == null) {
                 sbw.append("current_user_name IS NOT NULL");
             }
             else {
                 sbw.append("(current_user_name IS NULL OR current_user_name != :current_user_name)");
             }
             sbw.append(" OR ");
             if (updateFields.getCurrentUserUuid() == null) {
                 sbw.append("current_user_uuid IS NOT NULL");
             }
             else {
                 sbw.append("(current_user_uuid IS NULL OR current_user_uuid != :current_user_uuid)");
             }
             sbw.append(")");
         }
         String selectSql = sb.toString() + sbw.toString() + " FOR UPDATE";
 
         final long updateTime =  System.currentTimeMillis();
         final String indexSql = "INSERT INTO event_summary_index_queue (uuid, update_time) "
                 + "SELECT uuid, " + String.valueOf(updateTime) + " "
                 + "FROM event_summary "
                 + sbw.toString();
         this.template.update(indexSql, fields);
 
         /*
          * If this is a significant status change, also add an audit note
          */
         final String newAuditJson;
         if (AUDIT_LOG_STATUSES.contains(status)) {
             EventAuditLog.Builder builder = EventAuditLog.newBuilder();
             builder.setTimestamp(now);
             builder.setNewStatus(status);
             if (updateFields.getCurrentUserUuid() != null) {
                 builder.setUserUuid(updateFields.getCurrentUserUuid());
             }
             if (updateFields.getCurrentUserName() != null) {
                 builder.setUserName(updateFields.getCurrentUserName());
             }
             try {
                 newAuditJson = JsonFormat.writeAsString(builder.build());
             } catch (IOException e) {
                 throw new ZepException(e);
             }
         }
         else {
             newAuditJson = null;
         }
 
         List<Map<String,Object>> result = this.template.query(selectSql, new RowMapper<Map<String,Object>>() {
             @Override
             public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                 final String fingerprint = rs.getString(COLUMN_FINGERPRINT);
                 final String currentAuditJson = rs.getString(COLUMN_AUDIT_JSON);
 
                 Map<String,Object> updateFields = new HashMap<String, Object>(fields);
                 final String newFingerprint;
                 // When closing an event, give it a unique fingerprint hash
                 if (ZepConstants.CLOSED_STATUSES.contains(status)) {
                     newFingerprint = EventDaoUtils.join('|', fingerprint, Long.toString(now));
                 }
                 // When re-opening an event, give it the true fingerprint_hash. This is required to correctly
                 // de-duplicate events.
                 else {
                     newFingerprint = fingerprint;
                 }
 
                 final StringBuilder auditJson = new StringBuilder();
                 if (newAuditJson != null) {
                     auditJson.append(newAuditJson);
                 }
                 if (currentAuditJson != null) {
                     if (auditJson.length() > 0) {
                         auditJson.append(",\n");
                     }
                     auditJson.append(currentAuditJson);
                 }
                 String updatedAuditJson = (auditJson.length() > 0) ? auditJson.toString() : null;
                 updateFields.put(COLUMN_FINGERPRINT_HASH, DaoUtils.sha1(newFingerprint));
                 updateFields.put(COLUMN_AUDIT_JSON, updatedAuditJson);
                 updateFields.put(COLUMN_UUID, rs.getObject(COLUMN_UUID));
                 return updateFields;
             }
         }, fields);
 
         final String updateSql = "UPDATE event_summary SET status_id=:status_id,status_change=:status_change," +
                 "update_time=:update_time,current_user_uuid=:current_user_uuid,current_user_name=:current_user_name," +
                 "cleared_by_event_uuid=:cleared_by_event_uuid,fingerprint_hash=:fingerprint_hash," +
                 "audit_json=:audit_json WHERE uuid=:uuid";
         int numRows = 0;
         for (final Map<String,Object> update : result) {
             try {
                 numRows += this.nestedTransactionService.executeInNestedTransaction(
                         new NestedTransactionCallback<Integer>() {
                             @Override
                             public Integer doInNestedTransaction(NestedTransactionContext context) throws DataAccessException {
                                 return template.update(updateSql, update);
                             }
                         });
             } catch (DuplicateKeyException e) {
                 /*
                  * Ignore duplicate key errors on update. This will occur if there is an active
                  * event with the same fingerprint.
                  */
             }
         }
         return numRows;
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int acknowledge(List<String> uuids, String userUuid, String userName)
             throws ZepException {
         /* NEW | ACKNOWLEDGED | SUPPRESSED -> ACKNOWLEDGED */
         Set<EventStatus> currentStatuses = ZepConstants.OPEN_STATUSES;
         EventSummaryUpdateFields userfields = new EventSummaryUpdateFields();
         userfields.setCurrentUserName(userName);
         userfields.setCurrentUserUuid(userUuid);
         return update(uuids, EventStatus.STATUS_ACKNOWLEDGED, userfields, currentStatuses);
     }
 
     private Map<String, Object> createSharedFields(long duration, TimeUnit unit) {
         TypeConverter<Long> timestampConverter = databaseCompatibility.getTimestampConverter();
         long delta = System.currentTimeMillis() - unit.toMillis(duration);
         Object lastSeen = timestampConverter.toDatabaseType(delta);
         Map<String, Object> fields = new HashMap<String, Object>();
         fields.put("_last_seen", lastSeen);
         fields.put("_closed_status_ids", CLOSED_STATUS_IDS);
         return fields;
     }
 
     @Override
     public long getArchiveEligibleEventCount(long duration, TimeUnit unit) {
         String sql = "SELECT COUNT(*) FROM event_summary WHERE status_id IN (:_closed_status_ids) AND last_seen < :_last_seen";
         Map<String, Object> fields = createSharedFields(duration, unit);
         return template.queryForInt(sql, fields);
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int archive(long duration, TimeUnit unit, int limit) throws ZepException {
         Map<String, Object> fields = createSharedFields(duration, unit);
         fields.put("_limit", limit);
 
         final String sql = "SELECT uuid FROM event_summary WHERE status_id IN (:_closed_status_ids) AND "
                 + "last_seen < :_last_seen LIMIT :_limit FOR UPDATE";
         final List<String> uuids = this.template.query(sql, new RowMapper<String>() {
             @Override
             public String mapRow(ResultSet rs, int rowNum)
                     throws SQLException {
                 return uuidConverter.fromDatabaseType(rs, COLUMN_UUID);
             }
         }, fields);
         return archive(uuids);
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int close(List<String> uuids, String userUuid, String userName) throws ZepException {
         /* NEW | ACKNOWLEDGED | SUPPRESSED -> CLOSED */
         List<EventStatus> currentStatuses = Arrays.asList(EventStatus.STATUS_NEW, EventStatus.STATUS_ACKNOWLEDGED,
                 EventStatus.STATUS_SUPPRESSED);
         EventSummaryUpdateFields userfields = new EventSummaryUpdateFields();
         userfields.setCurrentUserName(userName);
         userfields.setCurrentUserUuid(userUuid);
         return update(uuids, EventStatus.STATUS_CLOSED, userfields, currentStatuses);
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int reopen(List<String> uuids, String userUuid, String userName) throws ZepException {
         /* CLOSED | CLEARED | AGED | ACKNOWLEDGED -> NEW */
         List<EventStatus> currentStatuses = Arrays.asList(EventStatus.STATUS_CLOSED, EventStatus.STATUS_CLEARED,
                 EventStatus.STATUS_AGED, EventStatus.STATUS_ACKNOWLEDGED);
         EventSummaryUpdateFields userfields = new EventSummaryUpdateFields();
         userfields.setCurrentUserName(userName);
         userfields.setCurrentUserUuid(userUuid);
         return update(uuids, EventStatus.STATUS_NEW, userfields, currentStatuses);
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int suppress(List<String> uuids) throws ZepException {
         /* NEW -> SUPPRESSED */
         List<EventStatus> currentStatuses = Arrays.asList(EventStatus.STATUS_NEW);
         return update(uuids, EventStatus.STATUS_SUPPRESSED, EventSummaryUpdateFields.EMPTY_FIELDS, currentStatuses);
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public int archive(List<String> uuids) throws ZepException {
         if (uuids.isEmpty()) {
             return 0;
         }
         if (this.archiveColumnNames == null) {
             try {
                 this.archiveColumnNames = DaoUtils.getColumnNames(this.dataSource, TABLE_EVENT_ARCHIVE);
             } catch (MetaDataAccessException e) {
                 throw new ZepException(e.getLocalizedMessage(), e);
             }
         }
 
         TypeConverter<Long> timestampConverter = databaseCompatibility.getTimestampConverter();
         Map<String, Object> fields = new HashMap<String,Object>();
         fields.put(COLUMN_UPDATE_TIME, timestampConverter.toDatabaseType(System.currentTimeMillis()));
         fields.put("_uuids", TypeConverterUtils.batchToDatabaseType(uuidConverter, uuids));
         fields.put("_closed_status_ids", CLOSED_STATUS_IDS);
         StringBuilder selectColumns = new StringBuilder();
 
         for (Iterator<String> it = this.archiveColumnNames.iterator(); it.hasNext();) {
             String columnName = it.next();
             if (fields.containsKey(columnName)) {
                 selectColumns.append(':').append(columnName);
             } else {
                 selectColumns.append(columnName);
             }
             if (it.hasNext()) {
                 selectColumns.append(',');
             }
         }
 
         final long updateTime = System.currentTimeMillis();
         /* signal event_summary table rows to get indexed */ 
         this.template.update("INSERT INTO event_summary_index_queue (uuid, update_time) " 
             + "SELECT uuid, " + String.valueOf(updateTime) + " " 
             + "FROM event_summary" +
             " WHERE uuid IN (:_uuids) AND status_id IN (:_closed_status_ids)", 
                 fields); 
 
         String insertSql = String.format("INSERT INTO event_archive (%s) SELECT %s FROM event_summary" +
                " WHERE uuid IN (:_uuids) AND status_id IN (:_closed_status_ids)",
                 StringUtils.collectionToCommaDelimitedString(this.archiveColumnNames), selectColumns);
 
         this.template.update(insertSql, fields);
         final int updated = this.template.update("DELETE FROM event_summary WHERE uuid IN (:_uuids) AND status_id IN (:_closed_status_ids)",
                 fields);
         TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
             @Override
             public void afterCommit() {
                 statisticsService.addToArchivedEventCount(updated);
             }
         });
         return updated;
     }
 
     @Override
     @TransactionalRollbackAllExceptions
     public void importEvent(EventSummary eventSummary) throws ZepException {
         final long updateTime = System.currentTimeMillis();
         final EventSummary.Builder summaryBuilder = EventSummary.newBuilder(eventSummary);
         final Event.Builder eventBuilder = summaryBuilder.getOccurrenceBuilder(0);
         summaryBuilder.setUpdateTime(updateTime);
         EventDaoHelper.addMigrateUpdateTimeDetail(eventBuilder, updateTime);
 
         final EventSummary summary = summaryBuilder.build();
         final Map<String,Object> fields = this.eventDaoHelper.createImportedSummaryFields(summary);
 
         /*
          * Closed events have a unique fingerprint_hash in summary to allow multiple rows
          * but only allow one active event (where the de-duplication occurs).
          */
         if (ZepConstants.CLOSED_STATUSES.contains(eventSummary.getStatus())) {
             String uniqueFingerprint = (String) fields.get(COLUMN_FINGERPRINT) + '|' + updateTime;
             fields.put(COLUMN_FINGERPRINT_HASH, DaoUtils.sha1(uniqueFingerprint));
         }
         else {
             fields.put(COLUMN_FINGERPRINT_HASH, DaoUtils.sha1((String)fields.get(COLUMN_FINGERPRINT)));
         }
 
         if (eventSummary.getOccurrence(0).getSeverity() != EventSeverity.SEVERITY_CLEAR) {
             fields.put(COLUMN_CLEAR_FINGERPRINT_HASH, EventDaoUtils.createClearHash(eventSummary.getOccurrence(0)));
         }
 
         this.insert.execute(fields);
     }
 
     @TransactionalRollbackAllExceptions
     private void indexSignal(final String eventUuid, final long updateTime) throws ZepException {
         final String insertSql = "INSERT INTO event_summary_index_queue (uuid, update_time) "
                 + "VALUES (:uuid, :update_time)";
 
         Map<String, Object> fields = new HashMap<String,Object>();
         fields.put(COLUMN_UPDATE_TIME, updateTime);
         fields.put("uuid", this.uuidConverter.toDatabaseType(eventUuid));
         
         this.template.update(insertSql, fields);
     }
 }
