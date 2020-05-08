 /*****************************************************************************
  * 
  * Copyright (C) Zenoss, Inc. 2010, all rights reserved.
  * 
  * This content is made available according to terms specified in
  * License.zenoss under the directory where your Zenoss product is installed.
  * 
  ****************************************************************************/
 
 
 package org.zenoss.zep.rest;
 
 import org.apache.commons.httpclient.HttpStatus;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
 import org.zenoss.protobufs.util.Util.TimestampRange;
 import org.zenoss.protobufs.zep.Zep.Event;
 import org.zenoss.protobufs.zep.Zep.EventFilter;
 import org.zenoss.protobufs.zep.Zep.EventNote;
 import org.zenoss.protobufs.zep.Zep.EventQuery;
 import org.zenoss.protobufs.zep.Zep.EventStatus;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.protobufs.zep.Zep.EventSummaryUpdate;
 import org.zenoss.protobufs.zep.Zep.EventSummaryUpdateRequest;
 import org.zenoss.protobufs.zep.Zep.EventSummaryUpdateResponse;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.dao.EventArchiveDao;
 import org.zenoss.zep.dao.EventSummaryDao;
 import org.zenoss.zep.dao.impl.EventArchiveDaoImplIT;
 import org.zenoss.zep.dao.impl.EventSummaryDaoImplIT;
 import org.zenoss.zep.impl.EventPreCreateContextImpl;
 import org.zenoss.zep.rest.RestClient.RestResponse;
 
 import javax.sql.DataSource;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import static org.junit.Assert.*;
 
 @ContextConfiguration({ "classpath:zep-config.xml" })
 public class EventsResourceIT extends AbstractJUnit4SpringContextTests {
 
     private static final String EVENTS_URI = "/zeneventserver/api/1.0/events";
 
     @Autowired
     public EventSummaryDao summaryDao;
 
     @Autowired
     public EventArchiveDao archiveDao;
 
     @Autowired
     public DataSource ds;
 
     private RestClient client;
     private EventSummary summaryEvent;
     private EventSummary archiveEvent;
 
     private EventSummary createSummaryNew(Event event) throws ZepException {
         return createSummary(event, EventStatus.STATUS_NEW);
     }
 
     private EventSummary createSummary(Event event, EventStatus status)
             throws ZepException {
         Event statusEvent = Event.newBuilder(event).setStatus(status).build();
         String uuid = summaryDao.create(statusEvent, new EventPreCreateContextImpl());
         return summaryDao.findByUuid(uuid);
     }
 
     private EventSummary createArchive(Event event) throws ZepException {
         Event statusEvent = Event.newBuilder(event).setStatus(EventStatus.STATUS_CLOSED).build();
         return archiveDao.findByUuid(archiveDao.create(statusEvent, new EventPreCreateContextImpl()));
     }
 
     @Before
     public void setup() throws ZepException {
         this.client = new RestClient(EventSummary.getDefaultInstance(),
                 EventNote.getDefaultInstance(),
                 EventSummaryUpdateRequest.getDefaultInstance(),
                 EventSummaryUpdateResponse.getDefaultInstance());
         this.summaryEvent = createSummaryNew(EventSummaryDaoImplIT
                 .createUniqueEvent());
         this.archiveEvent = createArchive(EventArchiveDaoImplIT.createEvent());
     }
 
     @After
     public void shutdown() throws IOException, ZepException {
         SimpleJdbcTemplate template = new SimpleJdbcTemplate(ds);
         template.update("DELETE FROM event_summary");
         template.update("DELETE FROM event_archive");
         client.close();
     }
 
     @Test
     public void testGetByUuid() throws ZepException, IOException {
         assertEquals(summaryEvent,
                 client.getJson(EVENTS_URI + "/" + summaryEvent.getUuid())
                         .getMessage());
         assertEquals(summaryEvent,
                 client.getProtobuf(EVENTS_URI + "/" + summaryEvent.getUuid())
                         .getMessage());
         assertEquals(archiveEvent,
                 client.getJson(EVENTS_URI + "/" + archiveEvent.getUuid())
                         .getMessage());
         assertEquals(archiveEvent,
                 client.getProtobuf(EVENTS_URI + "/" + archiveEvent.getUuid())
                         .getMessage());
     }
 
     @Test
     public void testAddNote() throws IOException {
         EventNote note = EventNote.newBuilder().setMessage("My Message")
                 .setUserName("pkw").setUserUuid(UUID.randomUUID().toString())
                 .build();
         client.postJson(EVENTS_URI + "/" + summaryEvent.getUuid() + "/notes",
                 note);
         note = EventNote.newBuilder().setMessage("My Message 2")
                 .setUserName("pkw").setUserUuid(UUID.randomUUID().toString())
                 .build();
         client.postProtobuf(EVENTS_URI + "/" + summaryEvent.getUuid()
                 + "/notes", note);
         EventSummary summary = (EventSummary) client.getJson(
                 EVENTS_URI + "/" + summaryEvent.getUuid()).getMessage();
         assertEquals(2, summary.getNotesCount());
         // Notes returned in reverse order to match previous behavior
         assertEquals("My Message 2", summary.getNotes(0).getMessage());
         assertEquals("My Message", summary.getNotes(1).getMessage());
 
         note = EventNote.newBuilder(note).setMessage("My Message 3").build();
         client.postJson(EVENTS_URI + "/" + archiveEvent.getUuid() + "/notes",
                 note);
         note = EventNote.newBuilder(note).setMessage("My Message 4").build();
         client.postProtobuf(EVENTS_URI + "/" + archiveEvent.getUuid()
                 + "/notes", note);
         EventSummary archive = (EventSummary) client.getProtobuf(
                 EVENTS_URI + "/" + archiveEvent.getUuid()).getMessage();
         assertEquals(2, archive.getNotesCount());
         // Notes returned in reverse order to match previous behavior
         assertEquals("My Message 4", archive.getNotes(0).getMessage());
         assertEquals("My Message 3", archive.getNotes(1).getMessage());
     }
 
     @Test
     public void testUpdateEventSummary() throws ZepException, IOException {
         List<String> uuids = new ArrayList<String>(100);
         for (int i = 0; i < 100; i++) {
             uuids.add(createSummaryNew(EventSummaryDaoImplIT.createUniqueEvent()).getUuid());
         }
 
         // Update first 50
         EventStatus status = EventStatus.STATUS_ACKNOWLEDGED;
         String ackUuid = UUID.randomUUID().toString();
         String ackName = "testuser123";
 
         EventQuery.Builder queryBuilder = EventQuery.newBuilder();
         queryBuilder.setEventFilter(EventFilter.newBuilder().addAllUuid(uuids).build());
         EventQuery query = queryBuilder.build();
         RestResponse restResponse = client.postProtobuf(EVENTS_URI + "/search", query);
         assertEquals(HttpStatus.SC_CREATED, restResponse.getResponseCode());
         String location = restResponse.getHeaders().get("location").get(0);
         String query_uuid = location.substring(location.lastIndexOf('/') + 1);
 
         final EventSummaryUpdate updateFields = EventSummaryUpdate.newBuilder()
                 .setCurrentUserUuid(ackUuid).setCurrentUserName(ackName).setStatus(status).build();
         EventSummaryUpdateRequest.Builder reqBuilder = EventSummaryUpdateRequest.newBuilder();
         reqBuilder.setLimit(50);
         reqBuilder.setUpdateFields(updateFields);
         EventSummaryUpdateRequest req = reqBuilder.build();
 
         restResponse = client.putProtobuf(location, req);
         EventSummaryUpdateResponse response = (EventSummaryUpdateResponse) restResponse.getMessage();
         assertEquals(EventSummaryUpdateRequest.newBuilder(req).setOffset(50).setEventQueryUuid(query_uuid).build(),
                 response.getNextRequest());
         assertEquals(uuids.size(), response.getTotal());
         assertEquals(50, response.getUpdated());
 
         // Repeat request for last 50
         restResponse = client.putProtobuf(location, response.getNextRequest());
         EventSummaryUpdateResponse newResponse = (EventSummaryUpdateResponse) restResponse.getMessage();
         assertFalse(newResponse.hasNextRequest());
         assertEquals(50, response.getUpdated());
         assertEquals(uuids.size(), response.getTotal());
 
         assertEquals(HttpStatus.SC_NO_CONTENT, client.delete(location).getResponseCode());
         assertEquals(HttpStatus.SC_NOT_FOUND, client.getProtobuf(location).getResponseCode());
 
         // Verify updates hit the database
         List<EventSummary> summaries = summaryDao.findByUuids(uuids);
         assertEquals(uuids.size(), summaries.size());
         for (EventSummary summary : summaries) {
             assertEquals(status, summary.getStatus());
             assertEquals(ackUuid, summary.getCurrentUserUuid());
             assertEquals(ackName, summary.getCurrentUserName());
         }
     }
 
     @Test
     public void testUpdateEventSummaryExclusions() throws ZepException, IOException {
         long firstSeen = System.currentTimeMillis();
         TimestampRange firstSeenRange = TimestampRange.newBuilder().setStartTime(firstSeen).setEndTime(firstSeen).build();
         Set<String> uuids = new HashSet<String>();
         Map<String,EventSummary> excludedUuids = new HashMap<String,EventSummary>();
         for (int i = 0; i < 5; i++) {
             Event event = Event.newBuilder(EventSummaryDaoImplIT.createUniqueEvent()).setCreatedTime(firstSeen).build();
             EventSummary summary = createSummaryNew(event);
             if ((i % 2) == 0) {
                 uuids.add(summary.getUuid());
             }
             else {
                 excludedUuids.put(summary.getUuid(), summary);
             }
         }
 
         EventQuery.Builder queryBuilder = EventQuery.newBuilder();
         queryBuilder.setEventFilter(EventFilter.newBuilder().addFirstSeen(firstSeenRange).build());
         queryBuilder.setExclusionFilter(EventFilter.newBuilder().addAllUuid(excludedUuids.keySet()).build());
         EventQuery query = queryBuilder.build();
         RestResponse restResponse = client.postProtobuf(EVENTS_URI + "/search", query);
         assertEquals(HttpStatus.SC_CREATED, restResponse.getResponseCode());
         String location = restResponse.getHeaders().get("location").get(0);
 
         // Update first 10
         EventStatus status = EventStatus.STATUS_ACKNOWLEDGED;
         String ackUuid = UUID.randomUUID().toString();
         String ackName = "testuser123";
 
         final EventSummaryUpdate updateFields = EventSummaryUpdate.newBuilder()
                 .setCurrentUserUuid(ackUuid).setCurrentUserName(ackName).setStatus(status).build();
         EventSummaryUpdateRequest.Builder reqBuilder = EventSummaryUpdateRequest.newBuilder();
         reqBuilder.setLimit(10);
         reqBuilder.setUpdateFields(updateFields);
         EventSummaryUpdateRequest req = reqBuilder.build();
 
         EventSummaryUpdateResponse response = (EventSummaryUpdateResponse) client
                 .putProtobuf(location, req).getMessage();
         assertFalse(response.hasNextRequest());
         assertEquals(uuids.size(), response.getUpdated());
         assertEquals(uuids.size(), response.getTotal());
 
         assertEquals(HttpStatus.SC_NO_CONTENT, client.delete(location).getResponseCode());
         assertEquals(HttpStatus.SC_NOT_FOUND, client.getProtobuf(location).getResponseCode());
 
         // Verify updates hit the database
         List<String> allUuids = new ArrayList<String>();
         allUuids.addAll(uuids);
         allUuids.addAll(excludedUuids.keySet());
         List<EventSummary> summaries = summaryDao.findByUuids(allUuids);
         assertEquals(allUuids.size(), summaries.size());
         for (EventSummary summary : summaries) {
             if (uuids.contains(summary.getUuid())) {
                 assertEquals(status, summary.getStatus());
                 assertEquals(ackUuid, summary.getCurrentUserUuid());
                 assertEquals(ackName, summary.getCurrentUserName());
             }
             else {
                 // Excluded UUIDs shouldn't have changed
                 assertEquals(excludedUuids.get(summary.getUuid()), summary);
             }
         }
     }
 }
