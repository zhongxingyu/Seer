 /*
  * This program is part of Zenoss Core, an open source monitoring platform.
  * Copyright (C) 2010, Zenoss Inc.
  * 
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 2 as published by
  * the Free Software Foundation.
  * 
  * For complete information please visit: http://www.zenoss.com/oss/
  */
 package org.zenoss.zep.index.impl;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.FieldSelector;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanClause.Occur;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.MatchAllDocsQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.Sort;
 import org.apache.lucene.search.SortField;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.TopFieldDocs;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zenoss.protobufs.zep.Zep.EventFilter;
 import org.zenoss.protobufs.zep.Zep.EventSeverity;
 import org.zenoss.protobufs.zep.Zep.EventSort;
 import org.zenoss.protobufs.zep.Zep.EventSort.Field;
 import org.zenoss.protobufs.zep.Zep.EventStatus;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.protobufs.zep.Zep.EventSummaryRequest;
 import org.zenoss.protobufs.zep.Zep.EventSummaryResult;
 import org.zenoss.protobufs.zep.Zep.EventTagFilter;
 import org.zenoss.protobufs.zep.Zep.FilterOperator;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.ZepUtils;
 import org.zenoss.zep.index.EventIndexDao;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import static org.zenoss.zep.index.impl.IndexConstants.*;
 
 public class EventIndexDaoImpl implements EventIndexDao {
     private final IndexWriter writer;
     // Don't use searcher directly - use getSearcher()/returnSearcher()
     private IndexSearcher _searcher;
     private final String name;
 
     public static final int MAX_RESULTS = 100;
     public static final int OPTIMIZE_AT_NUM_EVENTS = 5000;
     private AtomicInteger eventsSinceOptimize = new AtomicInteger(0);
 
     private static Logger logger = LoggerFactory.getLogger(EventIndexDaoImpl.class);
 
     public EventIndexDaoImpl(String name, IndexWriter writer) throws IOException {
         this.name = name;
         this.writer = writer;
         this._searcher = new IndexSearcher(IndexReader.open(this.writer.getDirectory(), true));
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
         Document doc = EventIndexMapper.fromEventSummary(event);
         logger.debug("Indexing {}", event.getUuid());
 
         try {
             this.writer.updateDocument(new Term(FIELD_UUID, event.getUuid()), doc);
             eventsSinceOptimize.incrementAndGet();
         } catch (IOException e) {
             throw new ZepException(e);
         }
     }
 
     @Override
     public void commit() throws ZepException {
         commit(false);
     }
 
     private synchronized void reopenReader() throws IOException {
         IndexReader oldReader = this._searcher.getIndexReader();
         IndexReader newReader = oldReader.reopen();
         if (oldReader != newReader) {
             this._searcher = new IndexSearcher(newReader);
             ZepUtils.close(oldReader);
         }
     }
 
     private synchronized IndexSearcher getSearcher() {
         this._searcher.getIndexReader().incRef();
         return this._searcher;
     }
 
     private synchronized void returnSearcher(IndexSearcher searcher) throws ZepException {
         if (searcher != null) {
             try {
                 searcher.getIndexReader().decRef();
             } catch (IOException e) {
                 throw new ZepException(e.getLocalizedMessage(), e);
             }
         }
     }
 
     @Override
     public void commit(boolean forceOptimize) throws ZepException {
         try {
             this.writer.commit();
             reopenReader();
             if ( forceOptimize || eventsSinceOptimize.get() >= OPTIMIZE_AT_NUM_EVENTS ) {
                 this.writer.optimize();
                 eventsSinceOptimize.set(0);
             }
         } catch (IOException e) {
             throw new ZepException(e);
         }
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
         Query query = buildQuery(request.getEventFilter(), request.getExclusionFilter());
         Sort sort = buildSort(request.getSortList());
 
         int limit = request.getLimit();
         if ( limit > MAX_RESULTS || limit < 1 ) {
             limit = MAX_RESULTS;
         }
         int offset = request.getOffset();
         if ( offset < 0 ) {
             offset = 0;
         }
 
         IndexSearcher searcher = null;
         try {
             searcher = getSearcher();
             logger.debug("Querying for events matching: {}", query);
             TopDocs docs = searcher.search(query, null, limit + offset, sort);
             logger.debug("Found {} results", docs.totalHits);
 
             EventSummaryResult.Builder result = EventSummaryResult.newBuilder();
             result.setTotal(docs.totalHits);
             result.setLimit(limit);
             if ( result.getTotal() > limit + offset ) {
                 result.setNextOffset(limit + offset + 1);
             }
 
             for (int i = offset; i < docs.scoreDocs.length; i++) {
                 result.addEvents(EventIndexMapper.toEventSummary(searcher.doc(docs.scoreDocs[i].doc, selector)));
             }
             return result.build();
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         } finally {
             returnSearcher(searcher);
         }
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
             // Optimize only when we delete in batches as optimizing one at a time would be expensive
             commit(true);
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
                 summary = EventIndexMapper.toEventSummary(searcher.doc(docs.scoreDocs[0].doc));
             }
         } catch (IOException e) {
             throw new ZepException(e);
         } finally {
             returnSearcher(searcher);
         }
         return summary;
     }
 
     @Override
     public void clear() throws ZepException {
         logger.info("Deleting all events");
 
         try {
             this.writer.deleteAll();
             commit(true);
         } catch (IOException e) {
             throw new ZepException(e);
         }
     }
 
     @Override
     public void delete(EventSummaryRequest request) throws ZepException {
         Query query = buildQuery(request.getEventFilter(), request.getExclusionFilter());
 
         logger.info("Deleting events matching: {}", query);
 
         try {
             this.writer.deleteDocuments(query);
             commit(true);
         } catch (IOException e) {
             throw new ZepException(e);
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
             commit(true);
         } catch (IOException e) {
             throw new ZepException(e);
         }
     }
 
     private static final EventSort DEFAULT_EVENT_SORT = EventSort.newBuilder().setDirection(EventSort.Direction.DESCENDING).setField(Field.LAST_SEEN).build();
     private static final Sort DEFAULT_SORT = new Sort(createSortField(DEFAULT_EVENT_SORT));
 
     private static Sort buildSort(List<EventSort> sortList) {
         if (sortList.isEmpty()) {
             return DEFAULT_SORT;
         }
         List<SortField> fields = new ArrayList<SortField>(sortList.size());
         for (EventSort sort : sortList) {
             fields.add(createSortField(sort));
         }
         return new Sort(fields.toArray(new SortField[fields.size()]));
     }
 
     private static SortField createSortField(EventSort sort) {
         boolean reverse = (sort.getDirection() == EventSort.Direction.DESCENDING);
         switch (sort.getField()) {
             case COUNT:
                 return new SortField(FIELD_COUNT, SortField.INT, reverse);
             case ELEMENT_IDENTIFIER:
                 return new SortField(FIELD_ELEMENT_IDENTIFIER_NOT_ANALYZED, SortField.STRING, reverse);
             case ELEMENT_SUB_IDENTIFIER:
                return new SortField(FIELD_ELEMENT_SUB_IDENTIFIER_NOT_ANALYZED, SortField.STRING, reverse);
             case EVENT_CLASS:
                 return new SortField(FIELD_EVENT_CLASS, SortField.STRING, reverse);
             case EVENT_SUMMARY:
                 return new SortField(FIELD_SUMMARY_NOT_ANALYZED, SortField.STRING, reverse);
             case FIRST_SEEN:
                 return new SortField(FIELD_FIRST_SEEN_TIME, SortField.LONG, reverse);
             case LAST_SEEN:
                 return new SortField(FIELD_LAST_SEEN_TIME, SortField.LONG, reverse);
             case SEVERITY:
                 return new SortField(FIELD_SEVERITY, SortField.INT, reverse);
             case STATUS:
                 return new SortField(FIELD_STATUS, SortField.INT, reverse);
             case STATUS_CHANGE:
                 return new SortField(FIELD_STATUS_CHANGE_TIME, SortField.LONG, reverse);
             case UPDATE_TIME:
                 return new SortField(FIELD_UPDATE_TIME, SortField.LONG, reverse);
             case ACKNOWLEDGED_BY_USER_NAME:
                 return new SortField(FIELD_ACKNOWLEDGED_BY_USER_NAME, SortField.STRING, reverse);
         }
         throw new IllegalArgumentException("Unsupported sort field: " + sort.getField());
     }
 
     private Query buildQuery(EventFilter filter, EventFilter exclusionFilter) throws ZepException {
         final BooleanQuery filterQuery = buildQueryFromFilter(filter);
         final BooleanQuery exclusionQuery = buildQueryFromFilter(exclusionFilter);
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
 
     private BooleanQuery buildQueryFromFilter(EventFilter filter) throws ZepException {
         if (filter == null) {
             return null;
         }
         
         QueryBuilder qb = new QueryBuilder();
 
         qb.addRanges(FIELD_COUNT, filter.getCountRangeList());
         qb.addWildcardFields(FIELD_ACKNOWLEDGED_BY_USER_NAME, filter.getAcknowledgedByUserNameList(), false);
         qb.addIdentifierFields(FIELD_ELEMENT_IDENTIFIER, FIELD_ELEMENT_IDENTIFIER_NOT_ANALYZED,
                 filter.getElementIdentifierList(), this.writer.getAnalyzer());
         qb.addIdentifierFields(FIELD_ELEMENT_SUB_IDENTIFIER, FIELD_ELEMENT_SUB_IDENTIFIER_NOT_ANALYZED,
                 filter.getElementSubIdentifierList(), this.writer.getAnalyzer());
         qb.addField(FIELD_FINGERPRINT, filter.getFingerprintList());
         qb.addWildcardFields(FIELD_SUMMARY, filter.getEventSummaryList(), true);
         qb.addTimestampRanges(FIELD_FIRST_SEEN_TIME, filter.getFirstSeenList());
         qb.addTimestampRanges(FIELD_LAST_SEEN_TIME, filter.getLastSeenList());
         qb.addTimestampRanges(FIELD_STATUS_CHANGE_TIME, filter.getStatusChangeList());
         qb.addTimestampRanges(FIELD_UPDATE_TIME, filter.getUpdateTimeList());
         qb.addFieldOfEnumNumbers(FIELD_STATUS, filter.getStatusList());
         qb.addFieldOfEnumNumbers(FIELD_SEVERITY, filter.getSeverityList());
 
         List<String> eventClasses = new ArrayList<String>(filter.getEventClassCount());
         for (String ec : filter.getEventClassList()) {
             if ( ec.endsWith("/") ) {
                 // This is a "startswith" search
                 ec += '*';
             }
             else if ( !ec.endsWith("*") ) {
                 // This is an exact match
                 ec += '/';
             }
             eventClasses.add(ec);
         }
         qb.addWildcardFields(FIELD_EVENT_CLASS, eventClasses, false);
 
         for (EventTagFilter tagFilter : filter.getTagFilterList()) {
             qb.addField(FIELD_TAGS, tagFilter.getTagUuidsList(), tagFilter.getOp());
         }
 
         qb.addField(FIELD_UUID, filter.getUuidList(), FilterOperator.OR);
 
         return qb.build();
     }
 
     private static final FieldSelector SEVERITY_SELECTOR = new SingleFieldSelector(FIELD_SEVERITY);
     
     private Map<EventSeverity,Integer> countSeveritiesForTag(String tag) throws ZepException {
         QueryBuilder builder = new QueryBuilder();
         builder.addField(IndexConstants.FIELD_TAGS, tag);
         List<EventStatus> status = Arrays.asList(EventStatus.STATUS_NEW, EventStatus.STATUS_ACKNOWLEDGED);
         builder.addFieldOfEnumNumbers(IndexConstants.FIELD_STATUS, status);
         final Query query = builder.build();
 
         IndexSearcher searcher = null;
         try {
             searcher = getSearcher();
             Map<EventSeverity, Integer> severities = null;
             int maxDoc = searcher.maxDoc();
             if (maxDoc > 0) {
                 TopDocs docs = searcher.search(query, maxDoc);
                 if (docs.scoreDocs.length > 0) {
                     severities = new EnumMap<EventSeverity,Integer>(EventSeverity.class);
                     for (ScoreDoc scoreDoc : docs.scoreDocs) {
                         Document doc = searcher.doc(scoreDoc.doc, SEVERITY_SELECTOR);
                         EventSeverity severity = EventSeverity.valueOf(Integer.valueOf(doc.get(FIELD_SEVERITY)));
                         Integer count = severities.get(severity);
                         if (count == null) {
                             count = 1;
                         } else {
                             ++count;
                         }
                         severities.put(severity, count);
                     }
                 }
             }
             return severities;
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         } finally {
             returnSearcher(searcher);
         }
     }
 
     @Override
     public Map<String,Map<EventSeverity,Integer>> countSeverities(Set<String> tags)
             throws ZepException {
         Map<String,Map<EventSeverity,Integer>> severities = new HashMap<String, Map<EventSeverity,Integer>>(tags.size());
         for (String tag : tags) {
             Map<EventSeverity,Integer> tagSeverities = countSeveritiesForTag(tag);
             if (tagSeverities != null) {
                 severities.put(tag, tagSeverities);
             }
         }
         return severities;
     }
     
     private EventSeverity findWorstSeverity(String tag) throws ZepException {
         QueryBuilder builder = new QueryBuilder();
         builder.addField(IndexConstants.FIELD_TAGS, tag);
         List<EventStatus> status = Arrays.asList(EventStatus.STATUS_NEW, EventStatus.STATUS_ACKNOWLEDGED);
         builder.addFieldOfEnumNumbers(IndexConstants.FIELD_STATUS, status);
         final Query query = builder.build();
 
         // Sort by worst severity
         EventSort.Builder eventSortBuilder = EventSort.newBuilder();
         eventSortBuilder.setDirection(EventSort.Direction.DESCENDING);
         eventSortBuilder.setField(Field.SEVERITY);
         final EventSort eventSort = eventSortBuilder.build();
         final Sort sort = buildSort(Collections.singletonList(eventSort));
 
         IndexSearcher searcher = null;
 
         try {
             searcher = getSearcher();
             TopFieldDocs docs = searcher.search(query, null, 1, sort);
             EventSeverity severity = null;
             if (docs.scoreDocs.length > 0) {
                 Document doc = searcher.doc(docs.scoreDocs[0].doc, SEVERITY_SELECTOR);
                 severity = EventSeverity.valueOf(Integer.valueOf(doc.get(FIELD_SEVERITY)));
             }
             return severity;
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         } finally {
             returnSearcher(searcher);
         }
     }
 
     @Override
     public Map<String, EventSeverity> findWorstSeverity(Set<String> tags)
             throws ZepException {
         Map<String,EventSeverity> severities = new LinkedHashMap<String, EventSeverity>(tags.size());
         for (String tag : tags) {
             EventSeverity worstSeverity = findWorstSeverity(tag);
             if (worstSeverity != null) {
                 severities.put(tag, worstSeverity);
             }
         }
         return severities;
     }
 }
