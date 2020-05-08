 /*****************************************************************************
  * 
  * Copyright (C) Zenoss, Inc. 2010, all rights reserved.
  * 
  * This content is made available according to terms specified in
  * License.zenoss under the directory where your Zenoss product is installed.
  * 
  ****************************************************************************/
 
 
 package org.zenoss.zep.index.impl;
 
 import com.google.common.collect.Lists;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.FieldSelector;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanClause.Occur;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.Collector;
 import org.apache.lucene.search.DocIdSetIterator;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.FieldComparator;
 import org.apache.lucene.search.FieldComparatorSource;
 import org.apache.lucene.search.MatchAllDocsQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.Scorer;
 import org.apache.lucene.search.Sort;
 import org.apache.lucene.search.SortField;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.util.OpenBitSet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.TaskScheduler;
 import org.zenoss.protobufs.zep.Zep.Event;
 import org.zenoss.protobufs.zep.Zep.EventActor;
 import org.zenoss.protobufs.zep.Zep.EventDetailItem;
 import org.zenoss.protobufs.zep.Zep.EventFilter;
 import org.zenoss.protobufs.zep.Zep.EventQuery;
 import org.zenoss.protobufs.zep.Zep.EventSeverity;
 import org.zenoss.protobufs.zep.Zep.EventSort;
 import org.zenoss.protobufs.zep.Zep.EventSort.Direction;
 import org.zenoss.protobufs.zep.Zep.EventStatus;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.protobufs.zep.Zep.EventSummaryRequest;
 import org.zenoss.protobufs.zep.Zep.EventSummaryResult;
 import org.zenoss.protobufs.zep.Zep.EventTag;
 import org.zenoss.protobufs.zep.Zep.EventTagFilter;
 import org.zenoss.protobufs.zep.Zep.EventTagSeverities;
 import org.zenoss.protobufs.zep.Zep.EventTagSeveritiesSet;
 import org.zenoss.protobufs.zep.Zep.EventTagSeverity;
 import org.zenoss.zep.Messages;
 import org.zenoss.zep.UUIDGenerator;
 import org.zenoss.zep.ZepConstants;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.ZepUtils;
 import org.zenoss.zep.dao.EventSummaryBaseDao;
 import org.zenoss.zep.impl.ThreadRenamingRunnable;
 import org.zenoss.zep.index.EventIndexDao;
 import org.zenoss.zep.index.IndexedDetailsConfiguration;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import static org.zenoss.zep.index.impl.IndexConstants.*;
 
 public class EventIndexDaoImpl implements EventIndexDao {
     private final IndexWriter writer;
     private final IndexSearcherCache indexSearcherCache;
     private final String name;
     private final boolean archive;
     private final EventSummaryBaseDao eventSummaryBaseDao;
     
     @Autowired
     private UUIDGenerator uuidGenerator;
 
     @Autowired
     private Messages messages;
 
     private int queryLimit = ZepConstants.DEFAULT_QUERY_LIMIT;
     
     @Autowired
     private TaskScheduler scheduler;
     private final Map<String,SavedSearch> savedSearches = new ConcurrentHashMap<String,SavedSearch>();
     private IndexedDetailsConfiguration indexedDetailsConfiguration;
 
     private static final Logger logger = LoggerFactory.getLogger(EventIndexDaoImpl.class);
 
     public EventIndexDaoImpl(String name, IndexWriter writer, EventSummaryBaseDao eventSummaryBaseDao)
             throws IOException {
         this.name = name;
         this.writer = writer;
         this.indexSearcherCache = new IndexSearcherCache(writer);
         this.eventSummaryBaseDao = eventSummaryBaseDao;
         this.archive = "event_archive".equals(name);
     }
 
     public synchronized void shutdown() throws IOException {
         for (Iterator<Map.Entry<String,SavedSearch>> it = savedSearches.entrySet().iterator(); it.hasNext(); ) {
             it.next().getValue().close();
             it.remove();
         }
         this.indexSearcherCache.close();
     }
 
     public void setIndexDetailsConfiguration(IndexedDetailsConfiguration indexedDetailsConfiguration) {
         this.indexedDetailsConfiguration = indexedDetailsConfiguration;
     }
 
     /**
      * Sets the maximum number of results returned in a query from ZEP.
      *
      * @param limit Maximum number of results returned in a query from ZEP.
      */
     public void setQueryLimit(int limit) {
         if (limit > 0) {
             this.queryLimit = limit;
         }
         else {
             logger.warn("Invalid query limit: {}, using default: {}", limit, this.queryLimit);
         }
     }
 
     @Override
     public String getName() {
         return this.name;
     }
 
     @Override
     public int getNumDocs() throws ZepException {
         try {
             return this.writer.numDocs();
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         }
     }
 
     @Override
     public void index(EventSummary event) throws ZepException {
         stage(event);
         commit();
     }
 
     @Override
     public void stage(EventSummary event) throws ZepException {
         Document doc = EventIndexMapper.fromEventSummary(event,
                 indexedDetailsConfiguration.getEventDetailItemsByName(), this.archive);
         try {
             this.writer.updateDocument(new Term(FIELD_UUID, event.getUuid()), doc);
         } catch (IOException e) {
             throw new ZepException(e);
         }
     }
 
     @Override
     public void stageDelete(String eventUuid) throws ZepException {
         try {
             writer.deleteDocuments(new Term(FIELD_UUID, eventUuid));
         } catch (IOException e) {
             throw new ZepException(e);
         }
     }
 
     @Override
     public void commit() throws ZepException {
         try {
             this.writer.commit();
         } catch (IOException e) {
             throw new ZepException(e);
         }
     }
 
     private static class IndexSearcherCache {
         private final IndexWriter indexWriter;
         private IndexReader indexReader = null;
 
         /**
          * Creates a NRT IndexSearcher from the given IndexWriter.
          * 
          * @param indexWriter Index writer used to create reader.
          */
         public IndexSearcherCache(IndexWriter indexWriter) {
             this.indexWriter = indexWriter;
         }
 
         /**
          * Returns an IndexSearcher (using a cached/reopened one where possible).
          *
          * @return An IndexSearcher which can be used to search the index.
          * @throws IOException If an exception occurs opening the searcher.
          */
         public synchronized IndexSearcher getIndexSearcher() throws IOException {
             if (this.indexReader == null) {
                 this.indexReader = IndexReader.open(this.indexWriter, true);
             }
             else {
                 IndexReader oldReader = this.indexReader;
                 IndexReader newReader = IndexReader.openIfChanged(oldReader);
                 if (newReader != null) {
                     this.indexReader = newReader;
                     oldReader.close();
                 }
             }
             this.indexReader.incRef();
             return new IndexSearcher(indexReader);
         }
 
         /**
          * Returns the IndexSearcher after it has been used.
          *
          * @param indexSearcher IndexSearcher to return.
          * @throws IOException If an exception occurs closing the IndexReader.
          */
         public synchronized void returnIndexSearcher(IndexSearcher indexSearcher) throws IOException {
             IndexReader indexReader = indexSearcher.getIndexReader();
             indexReader.decRef();
         }
 
         /**
          * Closes the cached IndexReader (used when responding to an OutOfMemoryError to allow memory to be
          * returned to the program. This will cause the next call to retrieve an IndexReader to open a new one.
          */
         public synchronized void close() {
             if (this.indexReader != null) {
                 ZepUtils.close(this.indexReader);
                 this.indexReader = null;
             }
         }
     }
 
     private IndexSearcher getSearcher() throws IOException {
         return this.indexSearcherCache.getIndexSearcher();
     }
 
     private void returnSearcher(IndexSearcher searcher) throws ZepException {
         try {
             this.indexSearcherCache.returnIndexSearcher(searcher);
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         }
     }
 
     @Override
     public void commit(boolean forceOptimize) throws ZepException {
         commit();
     }
 
     @Override
     public void indexMany(List<EventSummary> events) throws ZepException {
         for ( EventSummary event : events ) {            
             stage(event);
         }
         commit();
     }
 
     // Load the serialized protobuf (entire event)
     private static final FieldSelector PROTO_SELECTOR = new SingleFieldSelector(FIELD_PROTOBUF);
 
     // Load just the event UUID
     private static final FieldSelector UUID_SELECTOR = new SingleFieldSelector(FIELD_UUID);
     
     @Override
     public EventSummaryResult list(EventSummaryRequest request) throws ZepException {
         return listInternal(request, PROTO_SELECTOR);
     }
 
     @Override
     public EventSummaryResult listUuids(EventSummaryRequest request) throws ZepException {
         return listInternal(request, UUID_SELECTOR);
     }
 
     private EventSummaryResult listInternal(EventSummaryRequest request, FieldSelector selector) throws ZepException {
         IndexSearcher searcher = null;
         try {
             searcher = getSearcher();
             Query query = buildQuery(searcher.getIndexReader(), request.getEventFilter(), request.getExclusionFilter());
             Sort sort = buildSort(request.getSortList());
             return searchToEventSummaryResult(searcher, query, sort, selector, request.getOffset(), request.getLimit());
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         } catch (OutOfMemoryError oome) {
             this.indexSearcherCache.close();
             throw oome;
         } finally {
             returnSearcher(searcher);
         }
     }
 
     private EventSummaryResult searchToEventSummaryResult(IndexSearcher searcher, Query query, Sort sort,
                                                           FieldSelector selector, int offset, int limit)
             throws IOException, ZepException {
         if (limit < 0) {
             throw new ZepException(messages.getMessage("invalid_query_limit", limit));
         }
         if (limit > queryLimit) {
             limit = queryLimit;
         }
         if (offset < 0) {
             offset = 0;
         }
         final TopDocs docs;
 
         // Lucene doesn't like querying for 0 documents - search for at least one here
         final int numDocs = Math.max(limit + offset, 1);
         if (sort != null) {
             logger.debug("Query: {}, Sort: {}, Offset: {}, Limit: {}", new Object[] { query, sort, offset, limit });
             docs = searcher.search(query, null, numDocs, sort);
         }
         else {
             logger.debug("Query: {}, Offset: {}, Limit: {}", new Object[] { query, offset, limit });
             docs = searcher.search(query, null, numDocs);
         }
         logger.debug("Found {} results", docs.totalHits);
         EventSummaryResult.Builder result = EventSummaryResult.newBuilder();
         result.setTotal(docs.totalHits);
         result.setLimit(limit);
         if (docs.totalHits > limit + offset) {
             result.setNextOffset(limit + offset);
         }
 
         // Return the number of results they asked for (the query has to return at least one match
         // but the request may specified a limit of zero).
         final int lastDocument = Math.min(limit + offset, docs.scoreDocs.length);
         Map<String,EventSummary> archiveResultsFromDb = null;
 
         for (int i = offset; i < lastDocument; i++) {
             // Event archive only stores UUIDs - have to query results from database
             if (this.archive && selector != UUID_SELECTOR) {
                 if (archiveResultsFromDb == null) {
                     archiveResultsFromDb = new LinkedHashMap<String, EventSummary>();
                 }
                 Document doc = searcher.doc(docs.scoreDocs[i].doc, UUID_SELECTOR);
                 archiveResultsFromDb.put(doc.get(FIELD_UUID), null);
             }
             else {
                 result.addEvents(EventIndexMapper.toEventSummary(searcher.doc(docs.scoreDocs[i].doc, selector)));
             }
         }
 
         if (archiveResultsFromDb != null && !archiveResultsFromDb.isEmpty()) {
             // Perform a batch query against the event archive for the found UUIDs
             List<EventSummary> eventSummaries =
                     this.eventSummaryBaseDao.findByUuids(Lists.newArrayList(archiveResultsFromDb.keySet()));
 
             // Log if we detect an inconsistency between the index and the database
             if (eventSummaries.size() != archiveResultsFromDb.size()) {
                 logger.info("Event archive index out of sync - expected {} results, found {} results",
                         archiveResultsFromDb.size(), eventSummaries.size());
             }
 
             // Add back the events to the linked hash map (this preserves the lucene sort).
             for (EventSummary eventSummary : eventSummaries) {
                 archiveResultsFromDb.put(eventSummary.getUuid(), eventSummary);
             }
 
             // Iterate over the linked hash map and add non-null events to the results
             for (EventSummary eventSummaryInOrder : archiveResultsFromDb.values()) {
                 if (eventSummaryInOrder != null) {
                     result.addEvents(eventSummaryInOrder);
                 }
             }
         }
 
         return result.build();
     }
 
     @Override
     public void delete(String uuid) throws ZepException {
         try {
             writer.deleteDocuments(new Term(FIELD_UUID, uuid));
             commit();
         } catch (IOException e) {
             throw new ZepException(e);
         }
     }
 
     @Override
     public void delete(List<String> uuids) throws ZepException {
         try {
             if (uuids.isEmpty()) {
                 return;
             }
             List<Term> terms = new ArrayList<Term>(uuids.size());
             for (String uuid : uuids) {
                 terms.add(new Term(FIELD_UUID, uuid));
             }
             writer.deleteDocuments(terms.toArray(new Term[terms.size()]));
             commit();
         } catch (IOException e) {
             throw new ZepException(e);
         }
     }
 
     @Override
     public EventSummary findByUuid(String uuid) throws ZepException {
         TermQuery query = new TermQuery(new Term(FIELD_UUID, uuid));
         EventSummary summary = null;
         IndexSearcher searcher = null;
         try {
             searcher = getSearcher();
             TopDocs docs = searcher.search(query, 1);
             if (docs.scoreDocs.length > 0) {
                 // Not the most efficient way to search the archive, however this should give consistent results
                 // with other queries of the index (only returning indexed documents).
                 if (this.archive) {
                     Document doc = searcher.doc(docs.scoreDocs[0].doc, UUID_SELECTOR);
                     summary = eventSummaryBaseDao.findByUuid(doc.get(FIELD_UUID));
                     if (summary == null) {
                         logger.info("Event archive index out of sync - expected event {} not found", uuid);
                     }
                 }
                 else {
                     summary = EventIndexMapper.toEventSummary(searcher.doc(docs.scoreDocs[0].doc));
                 }
             }
         } catch (IOException e) {
             throw new ZepException(e);
         } catch (OutOfMemoryError oome) {
             this.indexSearcherCache.close();
             throw oome;
         } finally {
             returnSearcher(searcher);
         }
         return summary;
     }
 
     @Override
     public void clear() throws ZepException {
         logger.debug("Deleting all events for: {}", this.name);
         try {
             this.writer.deleteAll();
             commit();
         } catch (IOException e) {
             throw new ZepException(e);
         }
     }
 
     @Override
     public void delete(EventSummaryRequest request) throws ZepException {
         IndexSearcher searcher = null;
         try {
             searcher = getSearcher();
             Query query = buildQuery(searcher.getIndexReader(), request.getEventFilter(), request.getExclusionFilter());
             logger.debug("Deleting events matching: {}", query);
             this.writer.deleteDocuments(query);
             commit();
         } catch (IOException e) {
             throw new ZepException(e);
         } catch (OutOfMemoryError oome) {
             this.indexSearcherCache.close();
             throw oome;
         } finally {
             returnSearcher(searcher);
         }
     }
 
     @Override
     public void purge(int duration, TimeUnit unit) throws ZepException {
         if (duration < 0) {
             throw new IllegalArgumentException("Duration must be >= 0");
         }
         final long pruneTimestamp = System.currentTimeMillis() - unit.toMillis(duration);
 
         QueryBuilder query = new QueryBuilder();
         query.addRange(FIELD_LAST_SEEN_TIME, null, pruneTimestamp);
 
         logger.info("Purging events older than {}", new Date(pruneTimestamp));
         try {
             this.writer.deleteDocuments(query.build());
             commit();
         } catch (IOException e) {
             throw new ZepException(e);
         }
     }
 
     private Sort buildSort(List<EventSort> sortList) throws ZepException {
         if (sortList.isEmpty()) {
             return null;
         }
         List<SortField> fields = new ArrayList<SortField>(sortList.size());
         for (EventSort sort : sortList) {
             fields.addAll(createSortField(sort));
         }
         return new Sort(fields.toArray(new SortField[fields.size()]));
     }
 
     private List<SortField> createSortField(EventSort sort) throws ZepException {
         final List<SortField> sortFields = new ArrayList<SortField>(2);
         boolean reverse = (sort.getDirection() == Direction.DESCENDING);
 
         FieldComparatorSource comparator = new NaturalFieldComparatorSource();
         switch (sort.getField()) {
             case COUNT:
                 sortFields.add(new SortField(FIELD_COUNT, SortField.INT, reverse));
                 break;
             case ELEMENT_IDENTIFIER:
                 sortFields.add(new SortField(FIELD_ELEMENT_IDENTIFIER_NOT_ANALYZED, comparator, reverse));
                 break;
             case ELEMENT_SUB_IDENTIFIER:
                 sortFields.add(new SortField(FIELD_ELEMENT_SUB_IDENTIFIER_NOT_ANALYZED, comparator, reverse));
                 break;
             case ELEMENT_TITLE:
                 sortFields.add(new SortField(FIELD_ELEMENT_TITLE_NOT_ANALYZED, comparator, reverse));
                 break;
             case ELEMENT_SUB_TITLE:
                 sortFields.add(new SortField(FIELD_ELEMENT_SUB_TITLE_NOT_ANALYZED, comparator, reverse));
                 break;
             case EVENT_CLASS:
                 sortFields.add(new SortField(FIELD_EVENT_CLASS_NOT_ANALYZED, SortField.STRING, reverse));
                 break;
             case EVENT_SUMMARY:
                 sortFields.add(new SortField(FIELD_SUMMARY_NOT_ANALYZED, SortField.STRING, reverse));
                 break;
             case FIRST_SEEN:
                 sortFields.add(new SortField(FIELD_FIRST_SEEN_TIME, SortField.LONG, reverse));
                 break;
             case LAST_SEEN:
                 sortFields.add(new SortField(FIELD_LAST_SEEN_TIME, SortField.LONG, reverse));
                 break;
             case SEVERITY:
                 sortFields.add(new SortField(FIELD_SEVERITY, SortField.INT, reverse));
                 break;
             case STATUS:
                 sortFields.add(new SortField(FIELD_STATUS, SortField.INT, reverse));
                 break;
             case STATUS_CHANGE:
                 sortFields.add(new SortField(FIELD_STATUS_CHANGE_TIME, SortField.LONG, reverse));
                 break;
             case UPDATE_TIME:
                 sortFields.add(new SortField(FIELD_UPDATE_TIME, SortField.LONG, reverse));
                 break;
             case CURRENT_USER_NAME:
                 sortFields.add(new SortField(FIELD_CURRENT_USER_NAME, SortField.STRING, reverse));
                 break;
             case AGENT:
                 sortFields.add(new SortField(FIELD_AGENT, SortField.STRING, reverse));
                 break;
             case MONITOR:
                 sortFields.add(new SortField(FIELD_MONITOR, SortField.STRING, reverse));
                 break;
             case EVENT_KEY:
                 sortFields.add(new SortField(FIELD_EVENT_KEY, SortField.STRING, reverse));
                 break;
             case UUID:
                 sortFields.add(new SortField(FIELD_UUID, SortField.STRING, reverse));
                 break;
             case FINGERPRINT:
                 sortFields.add(new SortField(FIELD_FINGERPRINT, SortField.STRING, reverse));
                 break;
             case DETAIL:
                 EventDetailItem item = indexedDetailsConfiguration.getEventDetailItemsByName().get(sort.getDetailKey());
                 if (item == null) {
                     throw new IllegalArgumentException("Unknown event detail: " + sort.getDetailKey());
                 }
                 final String fieldName = EventIndexMapper.DETAIL_INDEX_PREFIX + sort.getDetailKey();
                 switch (item.getType()) {
                     case DOUBLE:
                         sortFields.add(new SortField(fieldName, SortField.DOUBLE, reverse));
                         break;
                     case INTEGER:
                         sortFields.add(new SortField(fieldName, SortField.INT, reverse));
                         break;
                     case STRING:
                         sortFields.add(new SortField(fieldName, SortField.STRING, reverse));
                         break;
                     case FLOAT:
                         sortFields.add(new SortField(fieldName, SortField.FLOAT, reverse));
                         break;
                     case LONG:
                         sortFields.add(new SortField(fieldName, SortField.LONG, reverse));
                         break;
                     case IP_ADDRESS:
                         // Sort IPv4 before IPv6
                         sortFields.add(new SortField(fieldName + IP_ADDRESS_TYPE_SUFFIX, SortField.STRING, reverse));
                         sortFields.add(new SortField(fieldName + SORT_SUFFIX, SortField.STRING, reverse));
                         break;
                     case PATH:
                         sortFields.add(new SortField(fieldName + SORT_SUFFIX, SortField.STRING, reverse));
                         break;
                     default:
                         throw new IllegalArgumentException("Unsupported detail type: " + item.getType());
                 }
                 break;
             case EVENT_CLASS_KEY:
                 sortFields.add(new SortField(FIELD_EVENT_CLASS_KEY, SortField.STRING, reverse));
                 break;
             case EVENT_GROUP:
                 sortFields.add(new SortField(FIELD_EVENT_GROUP, SortField.STRING, reverse));
                 break;
         }
         if (sortFields.isEmpty()) {
             throw new IllegalArgumentException("Unsupported sort field: " + sort.getField());
         }
         return sortFields;
     }
 
     private Query buildQuery(IndexReader reader, EventFilter filter, EventFilter exclusionFilter) throws ZepException {
         final BooleanQuery filterQuery = buildQueryFromFilter(reader, filter);
         final BooleanQuery exclusionQuery = buildQueryFromFilter(reader, exclusionFilter);
         final Query query;
 
         if (filterQuery == null && exclusionQuery == null) {
             query = new MatchAllDocsQuery();
         }
         else if (filterQuery != null) {
             if (exclusionQuery != null) {
                 filterQuery.add(exclusionQuery, Occur.MUST_NOT);
             }
             query = filterQuery;
         }
         else {
             BooleanQuery bq = new BooleanQuery();
             bq.add(exclusionQuery, Occur.MUST_NOT);
             bq.add(new MatchAllDocsQuery(), Occur.MUST);
             query = bq;
         }
         logger.debug("Filter: {}, Exclusion filter: {}, Query: {}", new Object[] { filter, exclusionFilter, query });
         
         return query;
     }
 
     private BooleanQuery buildQueryFromFilter(IndexReader reader, EventFilter filter) throws ZepException {
         if (filter == null) {
             return null;
         }
         
         QueryBuilder qb = new QueryBuilder(filter.getOperator());
 
         qb.addRanges(FIELD_COUNT, filter.getCountRangeList());
         qb.addWildcardFields(FIELD_CURRENT_USER_NAME, filter.getCurrentUserNameList(), false);
 
 
         qb.addIdentifierFields(FIELD_ELEMENT_IDENTIFIER, FIELD_ELEMENT_IDENTIFIER_NOT_ANALYZED,
                 filter.getElementIdentifierList(), this.writer.getAnalyzer());
         qb.addIdentifierFields(FIELD_ELEMENT_TITLE, FIELD_ELEMENT_TITLE_NOT_ANALYZED,
                 filter.getElementTitleList(), this.writer.getAnalyzer());
 
         qb.addIdentifierFields(FIELD_ELEMENT_SUB_IDENTIFIER, FIELD_ELEMENT_SUB_IDENTIFIER_NOT_ANALYZED,
                 filter.getElementSubIdentifierList(), this.writer.getAnalyzer());
         qb.addIdentifierFields(FIELD_ELEMENT_SUB_TITLE, FIELD_ELEMENT_SUB_TITLE_NOT_ANALYZED,
                 filter.getElementSubTitleList(), this.writer.getAnalyzer());
 
         qb.addWildcardFields(FIELD_FINGERPRINT, filter.getFingerprintList(), false);
         qb.addFullTextFields(FIELD_SUMMARY, filter.getEventSummaryList(), reader, this.writer.getAnalyzer());
         qb.addFullTextFields(FIELD_MESSAGE, filter.getMessageList(), reader, this.writer.getAnalyzer());
         qb.addTimestampRanges(FIELD_FIRST_SEEN_TIME, filter.getFirstSeenList());
         qb.addTimestampRanges(FIELD_LAST_SEEN_TIME, filter.getLastSeenList());
         qb.addTimestampRanges(FIELD_STATUS_CHANGE_TIME, filter.getStatusChangeList());
         qb.addTimestampRanges(FIELD_UPDATE_TIME, filter.getUpdateTimeList());
         qb.addFieldOfEnumNumbers(FIELD_STATUS, filter.getStatusList());
         qb.addFieldOfEnumNumbers(FIELD_SEVERITY, filter.getSeverityList());
         qb.addWildcardFields(FIELD_AGENT, filter.getAgentList(), false);
         qb.addWildcardFields(FIELD_MONITOR, filter.getMonitorList(), false);
         qb.addWildcardFields(FIELD_EVENT_KEY, filter.getEventKeyList(), false);
         qb.addWildcardFields(FIELD_EVENT_CLASS_KEY, filter.getEventClassKeyList(), false);
         qb.addWildcardFields(FIELD_EVENT_GROUP, filter.getEventGroupList(), false);
 
         qb.addPathFields(FIELD_EVENT_CLASS, FIELD_EVENT_CLASS_NOT_ANALYZED, filter.getEventClassList(),
                 reader);
 
         for (EventTagFilter tagFilter : filter.getTagFilterList()) {
             qb.addField(FIELD_TAGS, tagFilter.getTagUuidsList(), tagFilter.getOp());
         }
 
         qb.addWildcardFields(FIELD_UUID, filter.getUuidList(), true);
 
         qb.addDetails(filter.getDetailsList(), this.indexedDetailsConfiguration.getEventDetailItemsByName(), reader);
         
         for (EventFilter subfilter : filter.getSubfilterList()) {
             qb.addSubquery(buildQueryFromFilter(reader, subfilter));
         }
 
         return qb.build();
     }
 
     private static class TagSeveritiesCount {
         private int count = 0;
         private int acknowledged_count = 0;
     }
 
     private static class TagSeverities {
         final String tagUuid;
         private final Map<EventSeverity, TagSeveritiesCount> severityCount =
                 new EnumMap<EventSeverity,TagSeveritiesCount>(EventSeverity.class);
         private int total = 0;
 
         public TagSeverities(String tagUuid) {
             this.tagUuid = tagUuid;
         }
 
         public void updateSeverityCount(final EventSeverity severity, final int count, boolean isAcknowledged) {
             this.total += count;
             TagSeveritiesCount severitiesCount = severityCount.get(severity);
             if (severitiesCount == null) {
                 severitiesCount = new TagSeveritiesCount();
                 this.severityCount.put(severity, severitiesCount);
             }
             ++severitiesCount.count;
             if (isAcknowledged) {
                 ++severitiesCount.acknowledged_count;
             }
         }
 
         public EventTagSeverities toEventTagSeverities() {
             EventTagSeverities.Builder builder = EventTagSeverities.newBuilder();
             builder.setTagUuid(tagUuid);
             for (Map.Entry<EventSeverity, TagSeveritiesCount> entry : severityCount.entrySet()) {
                 TagSeveritiesCount severitiesCount = entry.getValue();
                 builder.addSeverities(EventTagSeverity.newBuilder().setSeverity(entry.getKey())
                         .setCount(severitiesCount.count).setAcknowledgedCount(severitiesCount.acknowledged_count).build());
             }
             builder.setTotal(this.total);
             return builder.build();
         }
     }
 
     private EventTagSeveritiesSet tagSeveritiesMapToSet(Map<String, TagSeverities> tagSeveritiesMap) {
         EventTagSeveritiesSet.Builder builder = EventTagSeveritiesSet.newBuilder();
         for (TagSeverities tagSeverities : tagSeveritiesMap.values()) {
             builder.addSeverities(tagSeverities.toEventTagSeverities());
         }
         return builder.build();
     }
 
     @Override
     public EventTagSeveritiesSet getEventTagSeverities(EventFilter filter)
             throws ZepException {
         final Map<String, TagSeverities> tagSeveritiesMap = new HashMap<String, TagSeverities>();
         final boolean hasTagsFilter = filter.getTagFilterCount() > 0;
         for (EventTagFilter eventTagFilter: filter.getTagFilterList()) {
             for (String eventTagUuid : eventTagFilter.getTagUuidsList()) {
                 tagSeveritiesMap.put(eventTagUuid, new TagSeverities(eventTagUuid));
             }
         }
         IndexSearcher searcher = null;
         try {
             searcher = getSearcher();
             final Query query = buildQueryFromFilter(searcher.getIndexReader(), filter);
             final OpenBitSet docs = new OpenBitSet(searcher.maxDoc());
             searcher.search(query, new Collector() {
                 private int docBase;
 
                 @Override
                 public void setScorer(Scorer scorer) throws IOException {
                 }
 
                 @Override
                 public void collect(int doc) throws IOException {
                     docs.set(docBase + doc);
                 }
 
                 @Override
                 public void setNextReader(IndexReader reader, int docBase) throws IOException {
                     this.docBase = docBase;
                 }
 
                 @Override
                 public boolean acceptsDocsOutOfOrder() {
                     return true;
                 }
             });
             int docId;
             final DocIdSetIterator it = docs.iterator();
             while ((docId = it.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                 final EventSummary summary;
                 if (this.archive) {
                     // TODO: This isn't very cheap - would be better to batch by UUID in separate calls
                     // This doesn't get called on the event archive right now, so leave it until need to optimize.
                     Document doc = searcher.doc(docId, UUID_SELECTOR);
                     summary = this.eventSummaryBaseDao.findByUuid(doc.get(FIELD_UUID));
                 } else {
                     Document doc = searcher.doc(docId);
                     // this is an optimization for getting the non-archived tags from an organizer for ticket
                     // see ZEN-7239. For this ticket we updated the index to store what we needed for generating the
                     // tags severities. Since we do not want a migrate of completely deleting the index this
                     // method is backwards compatible by uncompressing the protobuf
                     if (doc.get(FIELD_SEVERITY) != null) {
                         updateTagSeverityFromDocument(tagSeveritiesMap, hasTagsFilter, doc);
                         continue;
                     } else {
                         summary = EventIndexMapper.toEventSummary(doc);
                     }
                 }
                 final boolean isAcknowledged = (summary.getStatus() == EventStatus.STATUS_ACKNOWLEDGED);
                 final Event occurrence = summary.getOccurrence(0);
                 final EventActor actor = occurrence.getActor();
                 // Build tags from element_uuids - no tags specified in filter
                 if (!hasTagsFilter) {
                     if (actor.hasElementUuid()) {
                         TagSeverities tagSeverities = tagSeveritiesMap.get(actor.getElementUuid());
                         if (tagSeverities == null) {
                             tagSeverities = new TagSeverities(actor.getElementUuid());
                             tagSeveritiesMap.put(tagSeverities.tagUuid, tagSeverities);
                         }
                         tagSeverities.updateSeverityCount(occurrence.getSeverity(), occurrence.getCount(), isAcknowledged);
                     }
                 }
                 // Build tag severities from passed in filter
                 else {
                     List<String> uuids = Arrays.asList(actor.getElementUuid(), actor.getElementSubUuid());
                     for (String uuid : uuids) {
                         TagSeverities tagSeverities = tagSeveritiesMap.get(uuid);
                         if (tagSeverities != null) {
                             tagSeverities.updateSeverityCount(occurrence.getSeverity(), occurrence.getCount(),
                                     isAcknowledged);
                         }
                     }
                     for (EventTag tag : occurrence.getTagsList()) {
                         for (String tagUuid : tag.getUuidList()) {
                             TagSeverities tagSeverities = tagSeveritiesMap.get(tagUuid);
                             if (tagSeverities != null) {
                                 tagSeverities.updateSeverityCount(occurrence.getSeverity(), occurrence.getCount(),
                                         isAcknowledged);
                             }
                         }
                     }
                 }
             }
             return tagSeveritiesMapToSet(tagSeveritiesMap);
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         } catch (OutOfMemoryError oome) {
             this.indexSearcherCache.close();
             throw oome;
         } finally {
             returnSearcher(searcher);
         }
     }
 
     /**
      * This method updates the tag severities map from the document directly without
      * uncompressing the protobuf
      * @param tagSeveritiesMap The map to update
      * @param hasTagsFilter If we have a tag filter or not
      * @param doc the lucene document we are pulling information from
      */
     private void updateTagSeverityFromDocument(Map<String, TagSeverities> tagSeveritiesMap, boolean hasTagsFilter, Document doc) {
         int count = Integer.parseInt(doc.get(FIELD_COUNT));
         EventStatus status = EventStatus.valueOf(Integer.parseInt(doc.get(FIELD_STATUS)));
         EventSeverity severity = EventSeverity.valueOf(Integer.parseInt(doc.get(FIELD_SEVERITY)));
         boolean isAcknowledged = (status == EventStatus.STATUS_ACKNOWLEDGED);

        // get the map for each filter and update the count
        for (String tag : doc.getValues(FIELD_TAGS)) {
            TagSeverities tagSeverities = tagSeveritiesMap.get(tag);
            if (hasTagsFilter) {
                 if (tagSeverities != null) {
                     tagSeverities.updateSeverityCount(severity, count, isAcknowledged);
                 }
             }
            else {
                if (tagSeverities == null) {
                    tagSeveritiesMap.put(tag, new TagSeverities(tag));
                    tagSeverities = tagSeveritiesMap.get(tag);
                }
                tagSeverities.updateSeverityCount(severity, count, isAcknowledged);
            }
         }
     }
 
     private static class SavedSearch implements Closeable {
         private final String uuid;
         private IndexReader reader;
         private final Query query;
         private final Sort sort;
         private final int timeout;
         private ScheduledFuture<?> timeoutFuture;
 
         public SavedSearch(String uuid, IndexReader reader, Query query, Sort sort, int timeout) {
             this.uuid = uuid;
             this.reader = reader;
             this.query = query;
             this.sort = sort;
             this.timeout = timeout;
         }
 
         public String getUuid() {
             return uuid;
         }
 
         public IndexReader getReader() {
             return this.reader;
         }
 
         public Query getQuery() {
             return this.query;
         }
 
         public Sort getSort() {
             return sort;
         }
 
         public int getTimeout() {
             return timeout;
         }
 
         public synchronized void setTimeoutFuture(ScheduledFuture<?> timeoutFuture) throws ZepException {
             if (this.timeoutFuture != null) {
                 this.timeoutFuture.cancel(false);
             }
             this.timeoutFuture = timeoutFuture;
         }
 
         public synchronized void close() throws IOException {
             if (this.reader != null) {
                 this.reader.decRef();
                 this.reader = null;
             }
         }
 
         @Override
         public String toString() {
             return "SavedSearch{" +
                     "uuid='" + uuid + '\'' +
                     ", reader=" + reader +
                     ", query=" + query +
                     ", sort=" + sort +
                     ", timeout=" + timeout +
                     ", timeoutFuture=" + timeoutFuture +
                     '}';
         }
     }
 
     @Override
     public String createSavedSearch(EventQuery eventQuery) throws ZepException {
         if (eventQuery.getTimeout() < 1) {
             throw new ZepException("Invalid timeout: " + eventQuery.getTimeout());
         }
         final String uuid = this.uuidGenerator.generate().toString();
         IndexReader reader = null;
         try {
             reader = getSearcher().getIndexReader();
             final Query query = buildQuery(reader, eventQuery.getEventFilter(), eventQuery.getExclusionFilter());
             final Sort sort = buildSort(eventQuery.getSortList());
             final SavedSearch search = new SavedSearch(uuid, reader, query, sort, eventQuery.getTimeout());
             savedSearches.put(uuid, search);
             scheduleSearchTimeout(search);
         } catch (Exception e) {
             logger.warn("Exception creating saved search", e);
             if (reader != null) {
                 try {
                     reader.decRef();
                 } catch (IOException ex) {
                     logger.warn("Exception decrementing reference count", ex);
                 }
             }
             if (e instanceof ZepException) {
                 throw (ZepException) e;
             }
             throw new ZepException(e.getLocalizedMessage(), e);
         }
         return uuid;
     }
 
     private void cancelSearchTimeout(final SavedSearch search) throws ZepException {
         logger.debug("Canceling timeout for saved search: {}", search.getUuid());
         search.setTimeoutFuture(null);
     }
 
     private void scheduleSearchTimeout(final SavedSearch search) throws ZepException {
         logger.debug("Scheduling saved search {} for expiration in {} seconds", search.getUuid(), search.getTimeout());
         Date d = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(search.getTimeout()));
         search.setTimeoutFuture(scheduler.schedule(new ThreadRenamingRunnable(new Runnable() {
             @Override
             public void run() {
                 logger.debug("Saved search timed out: {}", search.getUuid());
                 savedSearches.remove(search.getUuid());
                 try {
                     search.close();
                 } catch (IOException e) {
                     logger.warn("Failed closing saved search", e);
                 }
             }
         }, "ZEP_SAVED_SEARCH_TIMEOUT"), d));
     }
 
     @Override
     public EventSummaryResult savedSearch(String uuid, int offset, int limit) throws ZepException {
         return savedSearchInternal(uuid, offset, limit, PROTO_SELECTOR);
     }
 
     @Override
     public EventSummaryResult savedSearchUuids(String uuid, int offset, int limit) throws ZepException {
         return savedSearchInternal(uuid, offset, limit, UUID_SELECTOR);
     }
 
     private EventSummaryResult savedSearchInternal(String uuid, int offset, int limit, FieldSelector selector)
             throws ZepException {
         final SavedSearch search = savedSearches.get(uuid);
         if (search == null) {
             throw new ZepException(messages.getMessage("saved_search_not_found", uuid));
         }
 
         IndexSearcher searcher = null;
         try {
             /* Cancel the timeout for the saved search to prevent it expiring while in use */
             cancelSearchTimeout(search);
             IndexReader reader = search.getReader();
             reader.incRef();
             
             searcher = new IndexSearcher(reader);
             return searchToEventSummaryResult(searcher, search.getQuery(), search.getSort(), selector, offset, limit);
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         } catch (OutOfMemoryError oome) {
             this.indexSearcherCache.close();
             throw oome;
         } finally {
             try {
                 returnSearcher(searcher);
             } finally {
                 scheduleSearchTimeout(search);
             }
         }
     }
 
     @Override
     public String deleteSavedSearch(String uuid) throws ZepException {
         final SavedSearch search = savedSearches.remove(uuid);
         if (search == null) {
             return null;
         }
         logger.debug("Deleting saved search: {}", uuid);
         cancelSearchTimeout(search);
         try {
             search.close();
         } catch (IOException e) {
             logger.warn("Failed closing reader", e);
         }
         return search.getUuid();
     }
 
     @Override
     public long getSize() {
         try {
             Directory directory = writer.getDirectory();
             long size = 0L;
             for(String name: directory.listAll()) {
                 size += directory.fileLength(name);
             }
             return size;
         } catch (IOException e) {
             logger.warn("Cannot get index size.");
             return -1L;
         }
     }
 
     private static final class NaturalFieldComparatorSource extends FieldComparatorSource
     {
         public FieldComparator<?> newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
         {
             return new NaturalStringValueComparator(numHits, fieldname);
         }
     }
 }
