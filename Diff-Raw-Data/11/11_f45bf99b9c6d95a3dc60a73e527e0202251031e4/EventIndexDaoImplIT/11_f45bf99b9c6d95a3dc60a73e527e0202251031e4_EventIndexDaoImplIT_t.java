 /*
  * Copyright (C) 2010, Zenoss Inc.  All Rights Reserved.
  */
 package org.zenoss.zep.index.impl;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
 import org.zenoss.protobufs.model.Model;
 import org.zenoss.protobufs.zep.Zep;
 import org.zenoss.protobufs.zep.Zep.Event;
 import org.zenoss.protobufs.zep.Zep.EventDetail;
 import org.zenoss.protobufs.zep.Zep.EventDetailFilter;
 import org.zenoss.protobufs.zep.Zep.EventFilter;
 import org.zenoss.protobufs.zep.Zep.EventSeverity;
 import org.zenoss.protobufs.zep.Zep.EventSort;
 import org.zenoss.protobufs.zep.Zep.EventSort.Direction;
 import org.zenoss.protobufs.zep.Zep.EventSort.Field;
 import org.zenoss.protobufs.zep.Zep.EventStatus;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.protobufs.zep.Zep.EventSummaryRequest;
 import org.zenoss.protobufs.zep.Zep.EventSummaryResult;
 import org.zenoss.protobufs.zep.Zep.EventTag;
 import org.zenoss.protobufs.zep.Zep.EventTagFilter;
 import org.zenoss.protobufs.zep.Zep.EventTagSeverities;
 import org.zenoss.protobufs.zep.Zep.EventTagSeveritiesSet;
 import org.zenoss.protobufs.zep.Zep.EventTagSeverity;
 import org.zenoss.protobufs.zep.Zep.FilterOperator;
 import org.zenoss.zep.ZepConstants;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.dao.EventSummaryDao;
 import org.zenoss.zep.dao.impl.EventTestUtils;
 import org.zenoss.zep.impl.EventPreCreateContextImpl;
 import org.zenoss.zep.index.EventIndexDao;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import static org.junit.Assert.*;
 
 @ContextConfiguration({"classpath:zep-config.xml"})
 public class EventIndexDaoImplIT extends AbstractTransactionalJUnit4SpringContextTests {
     @Autowired
     public EventSummaryDao eventSummaryDao;
 
     @Autowired
     @Qualifier("summary")
     public EventIndexDao eventIndexDao;
 
     @Before
     public void setUp() throws ZepException {
         eventIndexDao.clear();
     }
 
     @After
     public void tearDown() throws ZepException {
         eventIndexDao.clear();
     }
 
     private EventSummary createSummaryNew(Event event) throws ZepException {
         return createSummary(event, EventStatus.STATUS_NEW);
     }
 
     private EventSummary createSummary(Event event, EventStatus status) throws ZepException {
         Event changedStatus = Event.newBuilder(event).setStatus(status).build();
         String uuid = eventSummaryDao.create(changedStatus, new EventPreCreateContextImpl());
         return eventSummaryDao.findByUuid(uuid);
     }
 
     @Test
     public void testList() throws ZepException {
         Set<String> uuidsToSearch = new HashSet<String>();
 
         EventSummary eventSummaryFromDb;
 
         eventSummaryFromDb = createSummaryNew(EventTestUtils.createSampleEvent());
         uuidsToSearch.add(eventSummaryFromDb.getUuid());
 
         eventIndexDao.index(eventSummaryFromDb);
 
         eventSummaryFromDb = createSummaryNew(EventTestUtils.createSampleEvent());
         uuidsToSearch.add(eventSummaryFromDb.getUuid());
 
         eventIndexDao.index(eventSummaryFromDb);
         eventIndexDao.commit(true);
 
         EventSummaryRequest.Builder requestBuilder = EventSummaryRequest.newBuilder();
         requestBuilder.setLimit(10);
         EventSummaryRequest request = requestBuilder.build();
 
         EventSummaryResult result = eventIndexDao.list(request);
 
         Set<String> uuidsFound = new HashSet<String>();
         for (EventSummary e : result.getEventsList()) {
             uuidsFound.add(e.getUuid());
         }
 
         assertEquals(uuidsToSearch, uuidsFound);
 
         assertEquals(10, result.getLimit());
         assertEquals(2, result.getEventsCount());
     }
 
     @Test
     public void testIndex() throws ZepException {
         Event event = EventTestUtils.createSampleEvent();
         EventSummary eventSummaryFromDb = createSummaryNew(event);
 
         eventIndexDao.index(eventSummaryFromDb);
     }
 
     @Test
     public void testFindByUuid() throws AssertionError, ZepException {
         Event event = EventTestUtils.createSampleEvent();
         EventSummary eventSummaryFromDb = createSummaryNew(event);
 
         eventIndexDao.index(eventSummaryFromDb);
 
         assertNotNull(eventIndexDao.findByUuid(eventSummaryFromDb.getUuid()));
     }
 
     @Test
     public void testDelete() throws AssertionError, ZepException {
         Event event = EventTestUtils.createSampleEvent();
         EventSummary eventSummaryFromDb = createSummaryNew(event);
 
         eventIndexDao.index(eventSummaryFromDb);
 
         assertNotNull(eventIndexDao.findByUuid(eventSummaryFromDb.getUuid()));
 
         eventIndexDao.delete(eventSummaryFromDb.getUuid());
 
         assertNull(eventIndexDao.findByUuid(eventSummaryFromDb.getUuid()));
     }
 
     @Test
     public void testIndexMany() throws ZepException {
         Event event = EventTestUtils.createSampleEvent();
         EventSummary eventSummaryFromDb = createSummaryNew(event);
 
         List<EventSummary> events = new ArrayList<EventSummary>();
         events.add(eventSummaryFromDb);
         events.add(eventSummaryFromDb);
 
         eventIndexDao.indexMany(events);
     }
 
     private EventSummary createEventWithSeverity(EventSeverity severity,
                                                  EventStatus status, String... tags) throws ZepException {
         final Event.Builder eventBuilder = Event.newBuilder(EventTestUtils.createSampleEvent());
         eventBuilder.setSeverity(severity);
         eventBuilder.clearTags();
         for (String tag : tags) {
             eventBuilder.addTags(EventTag.newBuilder().addUuid(tag).setType("zenoss.device").build());
         }
         final Event event = eventBuilder.build();
         EventSummary summary = createSummary(event, status);
         eventIndexDao.index(summary);
         return summary;
     }
 
     @Test
     public void testGetEventTagSeverities() throws ZepException {
         String tag1 = UUID.randomUUID().toString();
         String tag2 = UUID.randomUUID().toString();
         String tag3 = UUID.randomUUID().toString();
         EventTagFilter.Builder tagFilterBuilder =  EventTagFilter.newBuilder();
         tagFilterBuilder.addAllTagUuids(Arrays.asList(tag1, tag2, tag3));
         EventFilter.Builder eventFilterBuilder = EventFilter.newBuilder();
         eventFilterBuilder.addTagFilter(tagFilterBuilder.build());
         eventFilterBuilder.addAllStatus(Arrays.asList(EventStatus.STATUS_NEW, EventStatus.STATUS_ACKNOWLEDGED));
         EventFilter eventFilter = eventFilterBuilder.build();
 
         /* Create error severity with two tags */
         for (int i = 0; i < 5; i++) {
             createEventWithSeverity(EventSeverity.SEVERITY_ERROR,
                     EventStatus.STATUS_NEW, tag1, tag2);
             createEventWithSeverity(EventSeverity.SEVERITY_ERROR,
                     EventStatus.STATUS_ACKNOWLEDGED, tag1, tag2);
         }
         /* Create critical severity with one tag */
         for (int i = 0; i < 3; i++) {
             createEventWithSeverity(EventSeverity.SEVERITY_CRITICAL,
                     EventStatus.STATUS_NEW, tag2);
             createEventWithSeverity(EventSeverity.SEVERITY_CRITICAL,
                     EventStatus.STATUS_ACKNOWLEDGED, tag2);
         }
         /* Create some closed events for all tags - these should be ignored. */
         for (int i = 0; i < 2; i++) {
             createEventWithSeverity(EventSeverity.SEVERITY_CRITICAL,
                     EventStatus.STATUS_CLOSED, tag1, tag2, tag2);
             createEventWithSeverity(EventSeverity.SEVERITY_ERROR,
                     EventStatus.STATUS_CLOSED, tag1, tag2, tag2);
             createEventWithSeverity(EventSeverity.SEVERITY_INFO,
                     EventStatus.STATUS_CLEARED, tag1, tag2, tag2);
         }
 
         EventTagSeveritiesSet tagSeveritiesSet = eventIndexDao.getEventTagSeverities(eventFilter);
         Map<String, EventTagSeverities> tagSeveritiesMap = new HashMap<String, EventTagSeverities>();
         for (EventTagSeverities tagSeverities : tagSeveritiesSet.getSeveritiesList()) {
             tagSeveritiesMap.put(tagSeverities.getTagUuid(), tagSeverities);
         }
 
         EventTagSeverities tag1Severities = tagSeveritiesMap.get(tag1);
         assertEquals(10, tag1Severities.getTotal());
         assertEquals(1, tag1Severities.getSeveritiesCount());
         for (EventTagSeverity tagSeverity : tag1Severities.getSeveritiesList()) {
             assertEquals(EventSeverity.SEVERITY_ERROR, tagSeverity.getSeverity());
             assertEquals(10, tagSeverity.getCount());
             assertEquals(5, tagSeverity.getAcknowledgedCount());
         }
 
         EventTagSeverities tag2Severities = tagSeveritiesMap.get(tag2);
         assertEquals(16, tag2Severities.getTotal());
         assertEquals(2, tag2Severities.getSeveritiesCount());
         for (EventTagSeverity tagSeverity : tag1Severities.getSeveritiesList()) {
             switch (tagSeverity.getSeverity()) {
                 case SEVERITY_ERROR:
                     assertEquals(10, tagSeverity.getCount());
                     assertEquals(5, tagSeverity.getAcknowledgedCount());
                     break;
                 case SEVERITY_CRITICAL:
                     assertEquals(6, tagSeverity.getCount());
                     assertEquals(3, tagSeverity.getAcknowledgedCount());
                     break;
                 default:
                     throw new RuntimeException("Unexpected severity: " + tagSeverity.getSeverity());
             }
         }
 
         EventTagSeverities tag3Severities = tagSeveritiesMap.get(tag3);
         assertEquals(0, tag3Severities.getTotal());
         assertEquals(0, tag3Severities.getSeveritiesCount());
     }
 
     private EventSummaryRequest createTagRequest(FilterOperator op,
                                                  String... tags) {
         EventTagFilter.Builder tagBuilder = EventTagFilter.newBuilder();
         tagBuilder.addAllTagUuids(Arrays.asList(tags));
         tagBuilder.setOp(op);
 
         EventFilter.Builder filterBuilder = EventFilter.newBuilder();
         filterBuilder.addTagFilter(tagBuilder.build());
 
         EventSummaryRequest.Builder reqBuilder = EventSummaryRequest.newBuilder();
         reqBuilder.setEventFilter(filterBuilder.build());
         return reqBuilder.build();
     }
 
     @Test
     public void testTagFilterOp() throws ZepException {
         String tag1 = UUID.randomUUID().toString();
         String tag2 = UUID.randomUUID().toString();
 
         EventSummary eventBothTags = createEventWithSeverity(
                 EventSeverity.SEVERITY_ERROR, EventStatus.STATUS_NEW, tag1,
                 tag2);
         EventSummary eventTag1 = createEventWithSeverity(
                 EventSeverity.SEVERITY_ERROR, EventStatus.STATUS_NEW, tag1);
         EventSummary eventTag2 = createEventWithSeverity(
                 EventSeverity.SEVERITY_ERROR, EventStatus.STATUS_NEW, tag2);
 
         EventSummaryResult result = this.eventIndexDao.list(createTagRequest(
                 FilterOperator.OR, tag1, tag2));
         assertEquals(3, result.getEventsCount());
         Set<String> foundUuids = getUuidsFromResult(result);
         assertTrue(foundUuids.contains(eventBothTags.getUuid()));
         assertTrue(foundUuids.contains(eventTag1.getUuid()));
         assertTrue(foundUuids.contains(eventTag2.getUuid()));
 
         result = this.eventIndexDao.list(createTagRequest(FilterOperator.AND,
                 tag1, tag2));
         assertEquals(1, result.getEventsCount());
         assertEquals(eventBothTags.getUuid(), result.getEventsList().get(0)
                 .getUuid());
     }
 
     public static Zep.EventActor createSampleActor(String elementId, String elementSubId) {
         Zep.EventActor.Builder actorBuilder = Zep.EventActor.newBuilder();
         actorBuilder.setElementIdentifier(elementId);
         actorBuilder.setElementTypeId(Model.ModelElementType.DEVICE);
         actorBuilder.setElementUuid(UUID.randomUUID().toString());
         actorBuilder.setElementSubIdentifier(elementSubId);
         actorBuilder.setElementSubTypeId(Model.ModelElementType.COMPONENT);
         actorBuilder.setElementSubUuid(UUID.randomUUID().toString());
         return actorBuilder.build();
     }
 
     @Test
     public void testIdentifierInsensitive() throws ZepException {
         EventSummary summary = createEventWithSeverity(EventSeverity.SEVERITY_ERROR, EventStatus.STATUS_NEW);
         Event occurrence = Event.newBuilder(summary.getOccurrence(0)).setActor(
                 createSampleActor("MyHostName.zenoss.loc", "myCompName")).build();
         summary = EventSummary.newBuilder(summary).clearOccurrence().addOccurrence(occurrence).build();
         eventIndexDao.index(summary);
 
         List<String> queries = Arrays.asList("myhostname", "ZENOSS", "loc");
         for (String query : queries) {
             EventFilter.Builder filterBuilder = EventFilter.newBuilder();
             filterBuilder.addElementIdentifier(query);
             final EventFilter filter = filterBuilder.build();
             EventSummaryRequest.Builder reqBuilder = EventSummaryRequest.newBuilder();
             reqBuilder.setEventFilter(filter);
             final EventSummaryRequest req = reqBuilder.build();
             EventSummaryResult result = eventIndexDao.list(req);
             assertEquals(1, result.getEventsCount());
             assertEquals(summary, result.getEvents(0));
         }
     }
 
     public static Zep.EventSummary createSampleSummary(EventSummary summary, String elementId) {
         Event occurrence = Event.newBuilder(summary.getOccurrence(0)).setActor(
                 createSampleActor(elementId, "myCompName")).build();
         return EventSummary.newBuilder(summary).clearOccurrence().addOccurrence(occurrence).build();
     }
 
     @Test
     public void testIdentifierSortInsensitive() throws ZepException {
 
         EventSummary summarya = createEventWithSeverity(EventSeverity.SEVERITY_ERROR, EventStatus.STATUS_NEW);
         summarya = createSampleSummary(summarya, "a_device.zenoss.loc");
         eventIndexDao.index(summarya);
 
         EventSummary summaryB = createEventWithSeverity(EventSeverity.SEVERITY_ERROR, EventStatus.STATUS_NEW);
         summaryB = createSampleSummary(summaryB, "B_device.zenoss.loc");
         eventIndexDao.index(summaryB);
 
         EventSummary summaryx = createEventWithSeverity(EventSeverity.SEVERITY_ERROR, EventStatus.STATUS_NEW);
         summaryx = createSampleSummary(summaryx, "x_device.zenoss.loc");
         eventIndexDao.index(summaryx);
 
         EventSummary summaryZ = createEventWithSeverity(EventSeverity.SEVERITY_ERROR, EventStatus.STATUS_NEW);
         summaryZ = createSampleSummary(summaryZ, "Z_device.zenoss.loc");
         eventIndexDao.index(summaryZ);
 
         
         EventSort.Builder sortBuilder = EventSort.newBuilder();
         sortBuilder.setField(Field.ELEMENT_IDENTIFIER);
         sortBuilder.setDirection(Direction.DESCENDING);
 
         EventSummaryRequest.Builder reqBuilder = EventSummaryRequest.newBuilder();
         reqBuilder.addSort(sortBuilder.build());
 
         EventSummaryResult result = eventIndexDao.list(reqBuilder.build());
 
         assertEquals(4, result.getEventsCount());
         
         assertEquals(summaryZ, result.getEvents(0));
         assertEquals(summaryx, result.getEvents(1));
         assertEquals(summaryB, result.getEvents(2));
         assertEquals(summarya, result.getEvents(3));
 
         EventSort.Builder sortBuilderAsc = EventSort.newBuilder();
         sortBuilderAsc.setField(Field.ELEMENT_IDENTIFIER);
         sortBuilderAsc.setDirection(Direction.ASCENDING);
 
         EventSummaryRequest.Builder reqBuilderAsc = EventSummaryRequest.newBuilder();
         reqBuilderAsc.addSort(sortBuilderAsc.build());
 
         EventSummaryResult resultAsc = eventIndexDao.list(reqBuilderAsc.build());
 
         assertEquals(4, resultAsc.getEventsCount());
 
         assertEquals(summarya, resultAsc.getEvents(0));
         assertEquals(summaryB, resultAsc.getEvents(1));
         assertEquals(summaryx, resultAsc.getEvents(2));
         assertEquals(summaryZ, resultAsc.getEvents(3));
     }
 
     @Test
     public void testIdentifier() throws ZepException {
         EventSummary summary = createEventWithSeverity(EventSeverity.SEVERITY_ERROR, EventStatus.STATUS_NEW);
         Event occurrence = Event.newBuilder(summary.getOccurrence(0)).setActor(
                 createSampleActor("test-jboss.zenoss.loc", "myCompName")).build();
         summary = EventSummary.newBuilder(summary).clearOccurrence().addOccurrence(occurrence).build();
         eventIndexDao.index(summary);
 
         List<String> queries = Arrays.asList("tes*", "test", "test-jboss*", "zenoss", "loc", "test-jboss*",
                 "test-jboss.zenoss.loc", "\"test-jboss.zenoss.loc\"", "noss", "\"TEST-jBoss.Zenoss.lOc\"");
         for (String query : queries) {
             EventFilter.Builder filterBuilder = EventFilter.newBuilder();
             filterBuilder.addElementIdentifier(query);
             final EventFilter filter = filterBuilder.build();
             EventSummaryRequest.Builder reqBuilder = EventSummaryRequest.newBuilder();
             reqBuilder.setEventFilter(filter);
             final EventSummaryRequest req = reqBuilder.build();
             EventSummaryResult result = eventIndexDao.list(req);
             assertEquals(query, 1, result.getEventsCount());
             assertEquals(query, summary, result.getEvents(0));
         }
     }
 
     private static EventSummaryRequest createUuidRequest(Set<String> include, Set<String> exclude) {
         EventSummaryRequest.Builder reqBuilder = EventSummaryRequest.newBuilder();
 
         if (!include.isEmpty()) {
             EventFilter.Builder filterBuilder = EventFilter.newBuilder().addAllUuid(include);
             reqBuilder.setEventFilter(filterBuilder.build());
         }
 
         if (!exclude.isEmpty()) {
             EventFilter.Builder filterBuilder = EventFilter.newBuilder().addAllUuid(exclude);
             reqBuilder.setExclusionFilter(filterBuilder.build());
         }
 
         return reqBuilder.build();
     }
 
     private static Set<String> getUuidsFromResult(EventSummaryResult result) {
         Set<String> uuids = new HashSet<String>(result.getEventsCount());
         for (EventSummary summary : result.getEventsList()) {
             uuids.add(summary.getUuid());
         }
         return uuids;
     }
 
     @Test
     public void testExcludeUuids() throws ZepException {
         EventSummary event1 = createEventWithSeverity(EventSeverity.SEVERITY_ERROR, EventStatus.STATUS_NEW);
         EventSummary event2 = createEventWithSeverity(EventSeverity.SEVERITY_ERROR, EventStatus.STATUS_NEW);
         EventSummary event3 = createEventWithSeverity(EventSeverity.SEVERITY_ERROR, EventStatus.STATUS_NEW);
 
         Set<String> include = new HashSet<String>();
         Set<String> exclude = new HashSet<String>();
 
         // No filters should return all three events
         Set<String> foundUuids = getUuidsFromResult(this.eventIndexDao.list(createUuidRequest(include, exclude)));
         assertEquals(3, foundUuids.size());
         assertTrue(foundUuids.contains(event1.getUuid()));
         assertTrue(foundUuids.contains(event2.getUuid()));
         assertTrue(foundUuids.contains(event3.getUuid()));
 
         // Test filter excluding all events
         exclude.add(event1.getUuid());
         exclude.add(event2.getUuid());
         exclude.add(event3.getUuid());
         foundUuids = getUuidsFromResult(this.eventIndexDao.list(createUuidRequest(include, exclude)));
         assertEquals(0, foundUuids.size());
 
         // Test filter including 2 events
         include.clear();
         exclude.clear();
         include.add(event1.getUuid());
         include.add(event3.getUuid());
         foundUuids = getUuidsFromResult(this.eventIndexDao.list(createUuidRequest(include, exclude)));
         assertEquals(2, foundUuids.size());
         assertTrue(foundUuids.contains(event1.getUuid()));
         assertTrue(foundUuids.contains(event3.getUuid()));
 
         // Test filter including all events of SEVERITY_ERROR but excluding a UUID
         EventFilter filter = EventFilter.newBuilder().addSeverity(EventSeverity.SEVERITY_ERROR).build();
         EventFilter exclusion = EventFilter.newBuilder().addUuid(event1.getUuid()).build();
         EventSummaryRequest req = EventSummaryRequest.newBuilder().setEventFilter(filter)
                 .setExclusionFilter(exclusion).build();
         foundUuids = getUuidsFromResult(this.eventIndexDao.list(req));
         assertEquals(2, foundUuids.size());
         assertTrue(foundUuids.contains(event2.getUuid()));
         assertTrue(foundUuids.contains(event3.getUuid()));
     }
 
     private EventSummary createEventWithClass(String eventClass) throws ZepException {
         final Event.Builder eventBuilder = Event.newBuilder(EventTestUtils.createSampleEvent());
         eventBuilder.setEventClass(eventClass);
         final Event event = eventBuilder.build();
         EventSummary summary = createSummary(event, EventStatus.STATUS_NEW);
         eventIndexDao.index(summary);
         return summary;
     }
 
     private EventSummaryRequest createRequestForEventClass(String... eventClass) {
         EventFilter.Builder filterBuilder = EventFilter.newBuilder();
         filterBuilder.addAllEventClass(Arrays.asList(eventClass));
         final EventFilter filter = filterBuilder.build();
 
         EventSummaryRequest.Builder reqBuilder = EventSummaryRequest.newBuilder();
         reqBuilder.setEventFilter(filter);
         return reqBuilder.build();
     }
 
     @Test
     public void testListByEventClass() throws ZepException {
         EventSummary event1 = createEventWithClass("/Status/Ping");
         EventSummary event2 = createEventWithClass("/Status/Snmp");
         createEventWithClass("/Perf");
         EventSummary event4 = createEventWithClass("/App/Info/Status");
 
         // Exact match if it doesn't end with a slash
         EventSummaryResult res = this.eventIndexDao.list(createRequestForEventClass("/Status"));
         assertEquals(0, res.getEventsCount());
 
         // Matches this class and children
         for (String query : Arrays.asList("/Status/", "/Status*", "/STATUS/", "/status/*")) {
             res = this.eventIndexDao.list(createRequestForEventClass(query));
             assertEquals(2, res.getEventsCount());
             Set<String> uuids = getUuidsFromResult(res);
             assertTrue(uuids.contains(event1.getUuid()));
             assertTrue(uuids.contains(event2.getUuid()));
         }
 
         // Matches exact class
         for (String query : Arrays.asList("/Status/Ping", "/status/ping", "/STATUS/ping")) {
             res = this.eventIndexDao.list(createRequestForEventClass(query));
             assertEquals(1, res.getEventsCount());
             assertEquals(event1.getUuid(), res.getEvents(0).getUuid());
         }
 
         // Matches substring
         for (String query : Arrays.asList("Ping", "ping", "PING", "PIN*")) {
             res = this.eventIndexDao.list(createRequestForEventClass(query));
             assertEquals(1, res.getEventsCount());
             assertEquals(event1.getUuid(), res.getEvents(0).getUuid());
         }
 
         // Matches substring with slash
         for (String query : Arrays.asList("app/info", "app/inf*", "app/i*", "ap*/inf*", "a*/info")) {
             res = this.eventIndexDao.list(createRequestForEventClass(query));
             assertEquals(1, res.getEventsCount());
             assertEquals(event4.getUuid(), res.getEvents(0).getUuid());
         }
 
         // Ensure we don't match if substring order differs
         for (String query : Arrays.asList("info/app", "inf*/app", "info/app*", "app/information", "inform*")) {
             res = this.eventIndexDao.list(createRequestForEventClass(query));
             assertEquals(0, res.getEventsCount());
         }
     }
 
     @Test
     public void testOnlyExclusion() throws ZepException {
         EventSummary event1 = createEventWithSeverity(EventSeverity.SEVERITY_INFO, EventStatus.STATUS_NEW);
         /* This event should be excluded from result. */
         createEventWithSeverity(EventSeverity.SEVERITY_ERROR, EventStatus.STATUS_NEW);
 
         EventFilter.Builder exclusion = EventFilter.newBuilder();
         exclusion.addSeverity(EventSeverity.SEVERITY_ERROR);
 
         EventSummaryRequest req = EventSummaryRequest.newBuilder().setExclusionFilter(exclusion.build()).build();
 
         EventSummaryResult res = this.eventIndexDao.list(req);
         assertEquals(1, res.getEventsCount());
         assertEquals(event1, res.getEvents(0));
     }
 
     @Test
     public void testEventFilterOperator() throws ZepException {
         createEventWithSeverity(EventSeverity.SEVERITY_INFO, EventStatus.STATUS_NEW);
         createEventWithSeverity(EventSeverity.SEVERITY_DEBUG, EventStatus.STATUS_CLOSED);
 
         // First test OR query with INFO or CLOSED; should return both events
         EventFilter.Builder or_filter = EventFilter.newBuilder()
                 .addSeverity(EventSeverity.SEVERITY_INFO)
                 .addStatus(EventStatus.STATUS_CLOSED)
                 .setOperator(FilterOperator.OR);
 
         EventSummaryRequest or_req = EventSummaryRequest.newBuilder()
                 .setEventFilter(or_filter)
                 .build();
         EventSummaryResult or_res = this.eventIndexDao.list(or_req);
         assertEquals(2, or_res.getEventsCount());
 
         // Now AND query with INFO and CLOSED; should return zero
         EventFilter.Builder and_filter = EventFilter.newBuilder()
                 .addSeverity(EventSeverity.SEVERITY_INFO)
                 .addStatus(EventStatus.STATUS_CLOSED)
                 .setOperator(FilterOperator.AND);
 
         EventSummaryRequest and_req = EventSummaryRequest.newBuilder()
                 .setEventFilter(and_filter)
                 .build();
         EventSummaryResult and_res = this.eventIndexDao.list(and_req);
         assertEquals(0, and_res.getEventsCount());
     }
 
     @Test
     public void testDetailNumericFilter() throws ZepException {
 
         // This detail is currently defined as an integer.
         final String production_state_key = ZepConstants.DETAIL_DEVICE_PRODUCTION_STATE;
 
         // Test to make sure this string value gets correctly parsed into numeric value.
         final String test_value1 = "1234";
         createEventWithDetail(production_state_key, test_value1);
 
         final String low_value = "103";
         createEventWithDetail(production_state_key, low_value);
 
         final String high_value = "4154";
         createEventWithDetail(production_state_key, high_value);
 
         final EventSummaryResult result = findResultForDetail(production_state_key, test_value1);
         assertEquals(1, result.getEventsCount());
 
         final String test_value2 = "1234.0";
         Exception expectedException = null;
         try {
             findResultForDetail(production_state_key, test_value2);
         } catch (RuntimeException e) {
             expectedException = e;
         }
         assertNotNull("Querying with a float value against an integer value should fail.", expectedException);
 
         // find the low_value and the test_value1
         final String test_value3 = ":1235";
         final EventSummaryResult result3 = findResultForDetail(production_state_key, test_value3);
         assertEquals(2, result3.getEventsCount());
 
         // find the test_value1 and the high_value
         final String test_value4 = "1233:";
         final EventSummaryResult result4 = findResultForDetail(production_state_key, test_value4);
         assertEquals(2, result4.getEventsCount());
 
         // test that these filters get converted to ranges correctly and that the ranges find the value, 1234.
         // expecting: 1233-1235, 1239
         final List<String> test_value5 = new ArrayList<String>();
         test_value5.add("1233");
         test_value5.add("1234");
         test_value5.add("1235");
         test_value5.add("1239");
 
         final EventSummaryResult result5 = findResultForDetails(production_state_key, test_value5);
         assertEquals(1, result5.getEventsCount());
 
         // test that this value gets converted to range correctly and that the range finds the value, 1234.
         final List<String> test_value6 = new ArrayList<String>();
         test_value6.add("1230:1239");
         final EventSummaryResult result6 = findResultForDetails(production_state_key, test_value6);
         assertEquals(1, result6.getEventsCount());
 
 
         // test the edge of a range is inclusive.
         final List<String> test_value7 = new ArrayList<String>();
         test_value7.add("1230:1234");
         final EventSummaryResult result7 = findResultForDetails(production_state_key, test_value7);
         assertEquals(1, result7.getEventsCount());
 
         // Just get ridiculous. Test out  of order, numbers within a range, singles that span a range boundary
         // Produces:
         //  queries=
         //      [
         //          details.zenoss.device.production_state:[4155 TO 2147483647],
         //          details.zenoss.device.production_state:[-2147483648 TO 1234],
         //          details.zenoss.device.production_state:[300 TO 300]
         //          details.zenoss.device.production_state:[1232 TO 1233]
         //          details.zenoss.device.production_state:[1235 TO 1236]
         //          details.zenoss.device.production_state:[3467 TO 3467]
         //      ]
         //  }
         final List<String> test_value8 = new ArrayList<String>();
         test_value8.add("300"); // low ball
         test_value8.add("1232"); // start a sequence
         test_value8.add("1233");
         test_value8.add("3467"); // throw something in 'out of order'
         test_value8.add("4155:"); // does not include the highest value.
         test_value8.add(":1234"); // everything up to this int, counting the previous sequence
         test_value8.add("1235"); // continue the sequence over the previous range.
         test_value8.add("1236"); // continue the sequence over the previous range.
 
         // should find all but the highest test event, 4154
         final EventSummaryResult result8 = findResultForDetails(production_state_key, test_value8);
         assertEquals(2, result8.getEventsCount());
     }
 
     @Test
     @Ignore
     public void testDetailStringFilter() throws ZepException {
         // override the spring config to test a string detail
     }
 
     private EventSummary createEventWithDetail(String key, String val) throws ZepException {
         final EventDetail.Builder detailBuilder = EventDetail.newBuilder();
         detailBuilder.setName(key);
         detailBuilder.addValue(val);
         return createEventWithDetails(Collections.singletonList(detailBuilder.build()));
     }
 
     private EventSummary createEventWithDetails(List<EventDetail> details) throws ZepException {
         final Event.Builder eventBuilder = Event.newBuilder(EventTestUtils.createSampleEvent());
         eventBuilder.addAllDetails(details);
         final Event event = eventBuilder.build();
         EventSummary summary = createSummary(event, EventStatus.STATUS_NEW);
         eventIndexDao.index(summary);
         return summary;
     }
 
     private EventSummaryResult findResultForDetail(String key, String val) throws ZepException {
         return findResultForDetails(key, Collections.singletonList(val));
     }
 
     private EventSummaryResult findResultForDetails(String key, List<String> vals) throws ZepException {
         final EventDetailFilter.Builder edFilterBuilder = EventDetailFilter.newBuilder();
         edFilterBuilder.setKey(key);
         edFilterBuilder.addAllValue(vals);
 
         final EventDetailFilter edf = edFilterBuilder.build();
         final EventFilter.Builder filter = EventFilter.newBuilder().addAllDetails(Collections.singletonList(edf));
         EventSummaryRequest request = EventSummaryRequest.newBuilder().setEventFilter(filter).build();
         return this.eventIndexDao.list(request);
     }
 
     @Test
     public void testSummaryPhrase() throws ZepException {
         EventSummary event1 = createSummaryNew(Event.newBuilder(EventTestUtils.createSampleEvent())
                 .setSummary("Monkeys love to eat bananas").build());
         EventSummary event2 = createSummaryNew(Event.newBuilder(EventTestUtils.createSampleEvent())
                 .setSummary("Bananas love to eat monkeys").build());
         eventIndexDao.indexMany(Arrays.asList(event1, event2));
 
         EventFilter filter = EventFilter.newBuilder().addEventSummary("\"eat bananas\"").build();
         EventSummaryRequest request = EventSummaryRequest.newBuilder().setEventFilter(filter).build();
         EventSummaryResult result = this.eventIndexDao.list(request);
         assertEquals(1, result.getEventsCount());
         assertEquals(event1, result.getEvents(0));
 
         filter = EventFilter.newBuilder().addEventSummary("\"eat monkeys\"").build();
         request = EventSummaryRequest.newBuilder().setEventFilter(filter).build();
         result = this.eventIndexDao.list(request);
         assertEquals(1, result.getEventsCount());
         assertEquals(event2, result.getEvents(0));
 
         // Test unterminated phrase query (from live search, etc.)
         filter = EventFilter.newBuilder().addEventSummary("\"eat monkeys").build();
         request = EventSummaryRequest.newBuilder().setEventFilter(filter).build();
         result = this.eventIndexDao.list(request);
         assertEquals(1, result.getEventsCount());
         assertEquals(event2, result.getEvents(0));
 
         // Test wildcard multi-term
         filter = EventFilter.newBuilder().addEventSummary("\"eat monkey?\"").build();
         request = EventSummaryRequest.newBuilder().setEventFilter(filter).build();
         result = this.eventIndexDao.list(request);
         assertEquals(1, result.getEventsCount());
         assertEquals(event2, result.getEvents(0));
 
         // Test wildcard multi-term
         filter = EventFilter.newBuilder().addEventSummary("\"eat mon*ys\"").build();
         request = EventSummaryRequest.newBuilder().setEventFilter(filter).build();
         result = this.eventIndexDao.list(request);
         assertEquals(1, result.getEventsCount());
         assertEquals(event2, result.getEvents(0));
 
         // AND query where both match
         filter = EventFilter.newBuilder().addEventSummary("\"eat monkeys\" bananas").build();
         request = EventSummaryRequest.newBuilder().setEventFilter(filter).build();
         result = this.eventIndexDao.list(request);
         assertEquals(1, result.getEventsCount());
         assertEquals(event2, result.getEvents(0));
 
         // AND query where one doesn't match
         filter = EventFilter.newBuilder().addEventSummary("\"eat monkeys\" not-here").build();
         request = EventSummaryRequest.newBuilder().setEventFilter(filter).build();
         result = this.eventIndexDao.list(request);
         assertEquals(0, result.getEventsCount());
 
         // Test OR query where one doesn't match
         filter = EventFilter.newBuilder().addEventSummary("\"eat monkeys\"").addEventSummary("not-here").build();
         request = EventSummaryRequest.newBuilder().setEventFilter(filter).build();
         result = this.eventIndexDao.list(request);
         assertEquals(1, result.getEventsCount());
         assertEquals(event2, result.getEvents(0));
 
         filter = EventFilter.newBuilder().addEventSummary("\"eat mon*\" ban*").build();
         request = EventSummaryRequest.newBuilder().setEventFilter(filter).build();
         result = this.eventIndexDao.list(request);
         assertEquals(1, result.getEventsCount());
         assertEquals(event2, result.getEvents(0));
 
         List<String> bothMatchQueries = Arrays.asList("\"bana*\"", "bananas monkeys", "bananas", "monkeys", "ban*", "mon*k*");
         for (String bothMatchQuery : bothMatchQueries) {
             filter = EventFilter.newBuilder().addEventSummary(bothMatchQuery).build();
             request = EventSummaryRequest.newBuilder().setEventFilter(filter).build();
             result = this.eventIndexDao.list(request);
             assertEquals(2, result.getEventsCount());
             Map<String,EventSummary> expected = new HashMap<String,EventSummary>();
             expected.put(event1.getUuid(), event1);
             expected.put(event2.getUuid(), event2);
             for (EventSummary resultSummary : result.getEventsList()) {
                 EventSummary e = expected.remove(resultSummary.getUuid());
                 assertNotNull(e);
                 assertEquals(e, resultSummary);
             }
             assertTrue(expected.isEmpty());
         }
     }
 
     @Test
     public void testSortNumericFilter() throws ZepException {
         final String production_state_key = ZepConstants.DETAIL_DEVICE_PRODUCTION_STATE;
 
         List<EventSummary> sorted = new ArrayList<EventSummary>();
 
         sorted.add(createEventWithDetail(production_state_key, "5"));
         sorted.add(createEventWithDetail(production_state_key, "10"));
         sorted.add(createEventWithDetail(production_state_key, "25"));
         sorted.add(createEventWithDetail(production_state_key, "500"));
         sorted.add(createEventWithDetail(production_state_key, "1000"));
 
         EventSort sort = EventSort.newBuilder().setField(Field.DETAIL).setDetailKey(production_state_key).build();
         EventSummaryRequest request = EventSummaryRequest.newBuilder().addSort(sort).build();
         EventSummaryResult result = this.eventIndexDao.list(request);
         assertEquals(sorted, result.getEventsList());
 
         sort = EventSort.newBuilder().setField(Field.DETAIL).setDetailKey(production_state_key)
                 .setDirection(Direction.DESCENDING).build();
         request = EventSummaryRequest.newBuilder().addSort(sort).build();
         result = this.eventIndexDao.list(request);
         Collections.reverse(sorted);
         assertEquals(sorted, result.getEventsList());
     }
 
     @Test
     public void testListZeroLimit() throws ZepException {
         /* Create one event */
         EventSummary event = createSummaryNew(EventTestUtils.createSampleEvent());
         this.eventIndexDao.index(event);
 
         EventSummaryRequest request = EventSummaryRequest.newBuilder().setLimit(0).build();
         EventSummaryResult result = this.eventIndexDao.list(request);
         assertEquals(0, result.getEventsCount());
         assertEquals(0, result.getLimit());
         assertEquals(0, result.getNextOffset());
         assertEquals(1, result.getTotal());
     }
 
     @Test
     public void testIpv4Sort() throws ZepException {
         List<EventSummary> sorted = new ArrayList<EventSummary>();
         sorted.add(createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "192.168.1.2"));
         sorted.add(createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "192.168.1.10"));
         sorted.add(createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "192.168.1.20"));
 
         EventSort sort = EventSort.newBuilder().setField(Field.DETAIL)
                 .setDetailKey(ZepConstants.DETAIL_DEVICE_IP_ADDRESS).build();
         EventSummaryRequest request = EventSummaryRequest.newBuilder().addSort(sort).build();
         EventSummaryResult result = this.eventIndexDao.list(request);
         assertEquals(sorted, result.getEventsList());
 
         sort = EventSort.newBuilder(sort).setDirection(Direction.DESCENDING).build();
         request = EventSummaryRequest.newBuilder().addSort(sort).build();
         result = this.eventIndexDao.list(request);
         Collections.reverse(sorted);
         assertEquals(sorted, result.getEventsList());
     }
 
     @Test
     public void testIpv6Sort() throws ZepException {
         List<EventSummary> sorted = new ArrayList<EventSummary>();
         sorted.add(createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "::"));
         sorted.add(createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "::20"));
         sorted.add(createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "::1000"));
 
         EventSort sort = EventSort.newBuilder().setField(Field.DETAIL)
                 .setDetailKey(ZepConstants.DETAIL_DEVICE_IP_ADDRESS).build();
         EventSummaryRequest request = EventSummaryRequest.newBuilder().addSort(sort).build();
         EventSummaryResult result = this.eventIndexDao.list(request);
         assertEquals(sorted, result.getEventsList());
 
         sort = EventSort.newBuilder(sort).setDirection(Direction.DESCENDING).build();
         request = EventSummaryRequest.newBuilder().addSort(sort).build();
         result = this.eventIndexDao.list(request);
         Collections.reverse(sorted);
         assertEquals(sorted, result.getEventsList());
     }
 
     @Test
     public void testIpv4Before6Sort() throws ZepException {
         List<EventSummary> sorted = new ArrayList<EventSummary>();
         sorted.add(createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "0.0.0.0"));
         sorted.add(createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "192.168.1.2"));
         sorted.add(createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "::"));
         sorted.add(createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "192f::"));
 
         EventSort sort = EventSort.newBuilder().setField(Field.DETAIL)
                 .setDetailKey(ZepConstants.DETAIL_DEVICE_IP_ADDRESS).build();
         EventSummaryRequest request = EventSummaryRequest.newBuilder().addSort(sort).build();
         EventSummaryResult result = this.eventIndexDao.list(request);
         assertEquals(sorted, result.getEventsList());
 
         sort = EventSort.newBuilder(sort).setDirection(Direction.DESCENDING).build();
         request = EventSummaryRequest.newBuilder().addSort(sort).build();
         result = this.eventIndexDao.list(request);
         Collections.reverse(sorted);
         assertEquals(sorted, result.getEventsList());
     }
 
     private EventFilter createFilterForIpAddress(String value) {
         EventFilter.Builder filterBuilder = EventFilter.newBuilder();
         filterBuilder.addDetailsBuilder().setKey(ZepConstants.DETAIL_DEVICE_IP_ADDRESS).addValue(value);
         return filterBuilder.build();
     }
 
     private void assertContainsEvents(EventSummaryResult result, EventSummary... summaries) {
         final Map<String,EventSummary> summaryMap = new HashMap<String,EventSummary>();
         for (EventSummary summary : summaries) {
             summaryMap.put(summary.getUuid(), summary);
         }
         for (EventSummary event : result.getEventsList()) {
             EventSummary expected = summaryMap.remove(event.getUuid());
             assertEquals("Unable to find event in expected map: " + event, expected, event);
         }
         assertTrue("Expected empty map, still contains: " + summaryMap, summaryMap.isEmpty());
     }
 
     @Test
     public void testIpv4RangeQuery() throws ZepException {
         EventSummary ev5 = createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "192.168.1.5");
         EventSummary ev6 = createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "192.168.1.6");
         EventSummary ev7 = createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "192.168.1.7");
         EventSummary ev9 = createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "192.168.1.9");
 
         EventSummaryRequest request = EventSummaryRequest.newBuilder()
                 .setEventFilter(createFilterForIpAddress("192.168.1.5-192.168.1.7")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev5, ev6, ev7);
 
         request = EventSummaryRequest.newBuilder().setEventFilter(createFilterForIpAddress("192.168.1.5-7"))
                 .build();
         assertContainsEvents(this.eventIndexDao.list(request), ev5, ev6, ev7);
 
         request = EventSummaryRequest.newBuilder().setEventFilter(createFilterForIpAddress("192.168.1.5/24")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev5, ev6, ev7, ev9);
 
         request = EventSummaryRequest.newBuilder().setEventFilter(createFilterForIpAddress("192.168.1.5")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev5);
 
         request = EventSummaryRequest.newBuilder().setEventFilter(createFilterForIpAddress("192.168.1.7-10")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev7, ev9);
     }
     
     @Test
     public void testIpv6RangeQuery() throws ZepException {
         EventSummary ev1 = createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "::ffa0");
         EventSummary ev2 = createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "::ffa7");
         EventSummary ev3 = createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "::ffb0");
         EventSummary ev4 = createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "::ffc0");
 
         EventSummaryRequest request = EventSummaryRequest.newBuilder()
                 .setEventFilter(createFilterForIpAddress("::ffa0-::ffb0")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev1, ev2, ev3);
 
         request = EventSummaryRequest.newBuilder().setEventFilter(createFilterForIpAddress("::ffa0-ffb0"))
                 .build();
         assertContainsEvents(this.eventIndexDao.list(request), ev1, ev2, ev3);
 
         request = EventSummaryRequest.newBuilder().setEventFilter(createFilterForIpAddress("::ffa0/120")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev1, ev2, ev3, ev4);
 
         request = EventSummaryRequest.newBuilder().setEventFilter(createFilterForIpAddress("::ffa7")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev2);
 
         request = EventSummaryRequest.newBuilder().setEventFilter(createFilterForIpAddress("::ffb0-ffc0")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev3, ev4);
     }
     
     @Test
     public void testIpv4SubstringQuery() throws ZepException {
         EventSummary ev1 = createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "192.168.1.2");
         EventSummary ev2 = createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "192.168.1.200");
         EventSummary ev3 = createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "192.168.1.3");
         EventSummary ev4 = createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "192.1.2.3");
         
         EventSummaryRequest request = EventSummaryRequest.newBuilder()
                 .setEventFilter(createFilterForIpAddress("1.2")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev1, ev4);
 
         request = EventSummaryRequest.newBuilder().setEventFilter(createFilterForIpAddress("1.?"))
                 .build();
         assertContainsEvents(this.eventIndexDao.list(request), ev1, ev3, ev4);
 
         request = EventSummaryRequest.newBuilder().setEventFilter(createFilterForIpAddress("1.2*")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev1, ev2, ev4);
 
         request = EventSummaryRequest.newBuilder().setEventFilter(createFilterForIpAddress("1.*")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev1, ev2, ev3, ev4);
     }
     
     @Test
     public void testIpv6SubstringQuery() throws ZepException {
         EventSummary ev1 = createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "::ffa0:123");
         EventSummary ev2 = createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "::ffa1:123");
         EventSummary ev3 = createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "::ffa0:0");
         
         EventSummaryRequest request = EventSummaryRequest.newBuilder()
                 .setEventFilter(createFilterForIpAddress("ffa?")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev1, ev2, ev3);
 
         request = EventSummaryRequest.newBuilder().setEventFilter(createFilterForIpAddress("0123"))
                 .build();
         assertContainsEvents(this.eventIndexDao.list(request), ev1, ev2);
 
         request = EventSummaryRequest.newBuilder().setEventFilter(createFilterForIpAddress("123")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev1, ev2);
 
         request = EventSummaryRequest.newBuilder().setEventFilter(createFilterForIpAddress("ffa0:0*")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev3);
         
         request = EventSummaryRequest.newBuilder().setEventFilter(createFilterForIpAddress("ffa?:1*")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev1, ev2);
     }
     
     @Test
     public void testIpv6ExactMatch() throws ZepException {
         EventSummary ev1 = createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "::1");
         EventSummaryRequest request = EventSummaryRequest.newBuilder()
                 .setEventFilter(createFilterForIpAddress("0:0:0:0:0:0:0:1")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev1);
         
         request = EventSummaryRequest.newBuilder().setEventFilter(createFilterForIpAddress("::1")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev1);
         
         request = EventSummaryRequest.newBuilder()
                 .setEventFilter(createFilterForIpAddress("0000:0000:0000:0000:0000:0000:0000:0001")).build();
         assertContainsEvents(this.eventIndexDao.list(request), ev1);
     }
     
     @Test
     public void testInvalidIpAddress() throws ZepException {
         EventSummary ev1 = createEventWithDetail(ZepConstants.DETAIL_DEVICE_IP_ADDRESS, "::1");
         
         List<String> invalid = Arrays.asList(":::", ":", "....", "not an ip");
         for (String query : invalid) {
             EventSummaryRequest request = EventSummaryRequest.newBuilder()
                     .setEventFilter(createFilterForIpAddress(query)).build();
             assertEquals(0, this.eventIndexDao.list(request).getEventsCount());
         }
     }
 
     @Test
     public void testDetailOrganizerQueries() throws ZepException {
         List<String> detailNames = Arrays.asList(ZepConstants.DETAIL_DEVICE_GROUPS, ZepConstants.DETAIL_DEVICE_LOCATION,
                 ZepConstants.DETAIL_DEVICE_SYSTEMS);
         for (String detailName : detailNames) {
             EventSummary ev1 = createEventWithDetail(detailName, "/Group1/Nested2");
 
             List<String> matches = Arrays.asList("group*", "nested*", "/Group*", "Group1/Nes*", "Group1", "Nested2",
                     "/Group1/Nested2", "/Group1/Nested2/");
             for (String query : matches) {
                 EventSummaryResult result = findResultForDetail(detailName, query);
                 assertEquals(1, result.getEventsCount());
                 assertEquals(ev1.getUuid(), result.getEventsList().get(0).getUuid());
             }
 
             List<String> noMatches = Arrays.asList("Nested*/Group*", "/Nested*");
             for (String query : noMatches) {
                 EventSummaryResult result = findResultForDetail(detailName, query);
                 assertEquals(0, result.getEventsCount());
             }
         }
     }
 }
