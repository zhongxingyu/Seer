 /*****************************************************************************
  * 
  * Copyright (C) Zenoss, Inc. 2010-2011, all rights reserved.
  * 
  * This content is made available according to terms specified in
  * License.zenoss under the directory where your Zenoss product is installed.
  * 
  ****************************************************************************/
 
 
 package org.zenoss.zep.dao.impl;
 
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DuplicateKeyException;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
 import org.zenoss.protobufs.model.Model.ModelElementType;
 import org.zenoss.protobufs.zep.Zep.Event;
 import org.zenoss.protobufs.zep.Zep.Event.Builder;
 import org.zenoss.protobufs.zep.Zep.EventActor;
 import org.zenoss.protobufs.zep.Zep.EventAuditLog;
 import org.zenoss.protobufs.zep.Zep.EventDetail;
 import org.zenoss.protobufs.zep.Zep.EventDetail.EventDetailMergeBehavior;
 import org.zenoss.protobufs.zep.Zep.EventDetailSet;
 import org.zenoss.protobufs.zep.Zep.EventNote;
 import org.zenoss.protobufs.zep.Zep.EventSeverity;
 import org.zenoss.protobufs.zep.Zep.EventStatus;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.protobufs.zep.Zep.EventTag;
 import org.zenoss.protobufs.zep.Zep.SyslogPriority;
 import org.zenoss.zep.ClearFingerprintGenerator;
 import org.zenoss.zep.ZepConstants;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.dao.ConfigDao;
 import org.zenoss.zep.dao.EventArchiveDao;
 import org.zenoss.zep.dao.EventSummaryDao;
 import org.zenoss.zep.dao.impl.compat.DatabaseCompatibility;
 import org.zenoss.zep.impl.EventPreCreateContextImpl;
 import org.zenoss.zep.plugins.EventPreCreateContext;
 
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.Callable;
 import java.util.concurrent.CyclicBarrier;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import static org.junit.Assert.*;
 import static org.zenoss.zep.dao.impl.EventConstants.*;
 
 @ContextConfiguration({ "classpath:zep-config.xml" })
 public class EventSummaryDaoImplIT extends AbstractTransactionalJUnit4SpringContextTests {
 
     private static Random random = new Random();
 
     @Autowired
     public EventSummaryDao eventSummaryDao;
 
     @Autowired
     public EventArchiveDao eventArchiveDao;
 
     @Autowired
     public DatabaseCompatibility databaseCompatibility;
 
     @Autowired
     public ConfigDao configDao;
 
     private EventSummary createSummaryNew(Event event) throws ZepException {
         return createSummary(event, EventStatus.STATUS_NEW);
     }
 
     private EventSummary createSummary(Event event, EventStatus status)
             throws ZepException {
         Event eventStatus = Event.newBuilder(event).setStatus(status).build();
         String uuid = eventSummaryDao.create(eventStatus, new EventPreCreateContextImpl());
         return eventSummaryDao.findByUuid(uuid);
     }
 
     private EventSummary createSummaryClear(Event event, Set<String> clearClasses) throws ZepException {
         EventPreCreateContextImpl eventContext = new EventPreCreateContextImpl();
         eventContext.setClearClasses(clearClasses);
         String uuid = eventSummaryDao.create(event, eventContext);
         return eventSummaryDao.findByUuid(uuid);
     }
 
     private static void compareEvents(Event event, Event eventFromDb) {
         Event event1 = Event.newBuilder().mergeFrom(event).clearUuid()
                 .clearCreatedTime().clearTags().addAllTags(EventDaoHelper.buildTags(event)).build();
         Event event2 = Event.newBuilder().mergeFrom(eventFromDb).clearUuid()
                 .clearCreatedTime().build();
         assertEquals(event1, event2);
     }
 
     private static void compareSummary(EventSummary expected,
             EventSummary actual) {
         expected = EventSummary.newBuilder(expected).clearStatus()
                 .clearStatusChangeTime().clearUpdateTime().build();
         actual = EventSummary.newBuilder(expected).clearStatus()
                 .clearStatusChangeTime().clearUpdateTime().build();
         assertEquals(expected, actual);
     }
 
     @Test
     public void testSummaryInsert() throws ZepException, InterruptedException {
         Event event = EventTestUtils.createSampleEvent();
         EventSummary eventSummaryFromDb = createSummaryNew(event);
         Event eventFromSummary = eventSummaryFromDb.getOccurrence(0);
         compareEvents(event, eventFromSummary);
         assertEquals(1, eventSummaryFromDb.getCount());
         assertEquals(EventStatus.STATUS_NEW, eventSummaryFromDb.getStatus());
         assertEquals(eventFromSummary.getCreatedTime(),
                 eventSummaryFromDb.getFirstSeenTime());
         assertEquals(eventFromSummary.getCreatedTime(),
                 eventSummaryFromDb.getStatusChangeTime());
         assertEquals(eventFromSummary.getCreatedTime(),
                 eventSummaryFromDb.getLastSeenTime());
         assertFalse(eventSummaryFromDb.hasCurrentUserUuid());
         assertFalse(eventSummaryFromDb.hasCurrentUserName());
         assertFalse(eventSummaryFromDb.hasClearedByEventUuid());
 
         /*
          * Create event with same fingerprint but again with new message,
          * summary, details.
          */
         Event.Builder newEventBuilder = Event.newBuilder().mergeFrom(event);
         newEventBuilder.setUuid(UUID.randomUUID().toString());
         newEventBuilder.setCreatedTime(System.currentTimeMillis());
         newEventBuilder.setMessage(event.getMessage() + random.nextInt(500));
         newEventBuilder.setSummary(event.getSummary() + random.nextInt(1000));
         newEventBuilder.clearDetails();
         newEventBuilder.addDetails(createDetail("newname1", "newvalue1", "newvalue2"));
         Event newEvent = newEventBuilder.build();
 
         EventSummary newEventSummaryFromDb = createSummaryNew(newEvent);
 
         assertEquals(eventSummaryFromDb.getUuid(),
                 newEventSummaryFromDb.getUuid());
         Event newEventFromSummary = newEventSummaryFromDb.getOccurrence(0);
         assertTrue(newEventSummaryFromDb.getLastSeenTime() > newEventSummaryFromDb
                 .getFirstSeenTime());
         assertEquals(eventSummaryFromDb.getFirstSeenTime(),
                 newEventSummaryFromDb.getFirstSeenTime());
         assertEquals(newEventFromSummary.getCreatedTime(),
                 newEventSummaryFromDb.getLastSeenTime());
         assertEquals(newEvent.getCreatedTime(),
                 newEventSummaryFromDb.getLastSeenTime());
         // Verify status didn't change (two NEW events)
         assertEquals(event.getCreatedTime(),
                 newEventSummaryFromDb.getStatusChangeTime());
         assertEquals(newEvent.getMessage(), newEventFromSummary.getMessage());
         assertEquals(newEvent.getSummary(), newEventFromSummary.getSummary());
 
         List<EventDetail> combined = new ArrayList<EventDetail>();
         combined.addAll(event.getDetailsList());
         combined.addAll(newEvent.getDetailsList());
         assertEquals(combined, newEventFromSummary.getDetailsList());
     }
 
     @Test
     public void testSummaryMultiThreadDedup() throws ZepException, InterruptedException, ExecutionException {
         // Attempts to create the same event from multiple threads - verifies we get the appropriate de-duping behavior
         // for the count and that we are holding the lock on the database appropriately.
         int poolSize = 10;
         final CyclicBarrier barrier = new CyclicBarrier(poolSize);
         ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
         ExecutorCompletionService<String> ecs = new ExecutorCompletionService<String>(executorService);
         final Event event = EventTestUtils.createSampleEvent();
         final EventPreCreateContext context = new EventPreCreateContextImpl();
         for (int i = 0; i < poolSize; i++) {
             ecs.submit(new Callable<String>() {
                 @Override
                 public String call() throws Exception {
                     barrier.await();
                     return eventSummaryDao.create(event, context);
                 }
             });
         }
         String uuid = null;
         for (int i = 0; i < poolSize; i++) {
             String thisUuid = ecs.take().get();
             if (uuid == null) {
                 assertNotNull(thisUuid);
                 uuid = thisUuid;
             }
             else {
                 assertEquals(uuid, thisUuid);
             }
         }
         // Now look up the event and make sure the count is equal to the number of submitted workers
         assertEquals(poolSize, this.eventSummaryDao.findByUuid(uuid).getCount());
     }
 
     @Test
     public void testAcknowledgedToNew() throws ZepException {
         /*
          * Verify acknowledged events aren't changed to new with a new
          * occurrence.
          */
         Event event = Event.newBuilder(EventTestUtils.createSampleEvent())
                 .setStatus(EventStatus.STATUS_ACKNOWLEDGED).build();
         eventSummaryDao.create(event, new EventPreCreateContextImpl());
         Event.Builder newEventBuilder = Event.newBuilder().mergeFrom(event);
         newEventBuilder.setUuid(UUID.randomUUID().toString());
         newEventBuilder.setCreatedTime(event.getCreatedTime() + 50L);
         Event newEvent = newEventBuilder.build();
 
         EventSummary newEventSummaryFromDb = createSummaryNew(newEvent);
 
         assertEquals(EventStatus.STATUS_ACKNOWLEDGED,
                 newEventSummaryFromDb.getStatus());
         assertEquals(event.getCreatedTime(),
                 newEventSummaryFromDb.getStatusChangeTime());
     }
 
     @Test
     public void testAcknowledgedToSuppressed() throws ZepException {
         /*
          * Verify acknowledged events aren't changed to suppressed with a new
          * occurrence.
          */
         Event event = Event.newBuilder(EventTestUtils.createSampleEvent())
                 .setStatus(EventStatus.STATUS_ACKNOWLEDGED).build();
         eventSummaryDao.create(event, new EventPreCreateContextImpl());
         Event.Builder newEventBuilder = Event.newBuilder().mergeFrom(event);
         newEventBuilder.setUuid(UUID.randomUUID().toString());
         newEventBuilder.setCreatedTime(event.getCreatedTime() + 50L);
         newEventBuilder.setStatus(EventStatus.STATUS_SUPPRESSED);
         Event newEvent = newEventBuilder.build();
 
         EventSummary newEventSummaryFromDb = createSummaryNew(newEvent);
 
         assertEquals(EventStatus.STATUS_ACKNOWLEDGED,
                 newEventSummaryFromDb.getStatus());
         assertEquals(event.getCreatedTime(),
                 newEventSummaryFromDb.getStatusChangeTime());
     }
 
     private static String createRandomMaxString(int length) {
         final String alphabet = "abcdefghijklmnopqrstuvwxyz";
         final char[] chars = new char[length];
         for (int i = 0; i < chars.length; i++) {
             chars[i] = alphabet.charAt(random.nextInt(alphabet.length()));
         }
         return new String(chars);
     }
 
     @Test
     public void testSummaryMaxInsert() throws ZepException,
             InterruptedException {
         Event.Builder eventBuilder = Event.newBuilder(createUniqueEvent());
         EventActor.Builder actorBuilder = EventActor.newBuilder(eventBuilder.getActor());
         eventBuilder.setFingerprint(createRandomMaxString(MAX_FINGERPRINT + 1));
         actorBuilder.setElementIdentifier(createRandomMaxString(MAX_ELEMENT_IDENTIFIER + 1));
         actorBuilder.setElementTitle(createRandomMaxString(MAX_ELEMENT_TITLE + 1));
         actorBuilder.setElementSubIdentifier(createRandomMaxString(MAX_ELEMENT_SUB_IDENTIFIER + 1));
         actorBuilder.setElementSubTitle(createRandomMaxString(MAX_ELEMENT_SUB_TITLE + 1));
         eventBuilder.setEventClass(createRandomMaxString(MAX_EVENT_CLASS + 1));
         eventBuilder.setEventClassKey(createRandomMaxString(MAX_EVENT_CLASS_KEY + 1));
         eventBuilder.setEventKey(createRandomMaxString(MAX_EVENT_KEY + 1));
         eventBuilder.setMonitor(createRandomMaxString(MAX_MONITOR + 1));
         eventBuilder.setAgent(createRandomMaxString(MAX_AGENT + 1));
         eventBuilder.setEventGroup(createRandomMaxString(MAX_EVENT_GROUP + 1));
         eventBuilder.setSummary(createRandomMaxString(MAX_SUMMARY + 1));
         eventBuilder.setMessage(createRandomMaxString(MAX_MESSAGE + 1));
         eventBuilder.setActor(actorBuilder.build());
         final Event event = eventBuilder.build();
         final EventSummary summary = createSummaryNew(event);
         final Event eventFromDb = summary.getOccurrence(0);
         final EventActor actorFromDb = eventFromDb.getActor();
         assertEquals(MAX_FINGERPRINT, eventFromDb.getFingerprint().length());
         assertEquals(MAX_ELEMENT_IDENTIFIER, actorFromDb.getElementIdentifier().length());
         assertEquals(MAX_ELEMENT_TITLE, actorFromDb.getElementTitle().length());
         assertEquals(MAX_ELEMENT_SUB_IDENTIFIER, actorFromDb.getElementSubIdentifier().length());
         assertEquals(MAX_ELEMENT_SUB_TITLE, actorFromDb.getElementSubTitle().length());
         assertEquals(MAX_EVENT_CLASS, eventFromDb.getEventClass().length());
         assertEquals(MAX_EVENT_CLASS_KEY, eventFromDb.getEventClassKey().length());
         assertEquals(MAX_EVENT_KEY, eventFromDb.getEventKey().length());
         assertEquals(MAX_MONITOR, eventFromDb.getMonitor().length());
         assertEquals(MAX_AGENT, eventFromDb.getAgent().length());
         assertEquals(MAX_EVENT_GROUP, eventFromDb.getEventGroup().length());
         assertEquals(MAX_SUMMARY, eventFromDb.getSummary().length());
         assertEquals(MAX_MESSAGE, eventFromDb.getMessage().length());
 
         this.eventSummaryDao.acknowledge(Collections.singletonList(summary.getUuid()), UUID.randomUUID().toString(),
                 createRandomMaxString(MAX_CURRENT_USER_NAME + 1));
         final EventSummary summaryFromDb = this.eventSummaryDao.findByUuid(summary.getUuid());
         assertEquals(MAX_CURRENT_USER_NAME, summaryFromDb.getCurrentUserName().length());
     }
 
     public static Event createUniqueEvent() {
         Random r = new Random();
         Event.Builder eventBuilder = Event.newBuilder();
         eventBuilder.setUuid(UUID.randomUUID().toString());
         eventBuilder.setCreatedTime(System.currentTimeMillis());
         eventBuilder.addDetails(createDetail("foo", "bar", "baz"));
         eventBuilder.addDetails(createDetail("foo2", "bar2", "baz2"));
         eventBuilder.setActor(EventTestUtils.createSampleActor());
         eventBuilder.setAgent("agent");
         eventBuilder.setEventClass("/Unknown");
         eventBuilder.setEventClassKey("eventClassKey");
         eventBuilder.setEventClassMappingUuid(UUID.randomUUID().toString());
         eventBuilder.setEventGroup("event group");
         eventBuilder.setEventKey("event key");
         eventBuilder.setFingerprint("my|dedupid|foo|" + r.nextInt());
         eventBuilder.setMessage("my message");
         eventBuilder.setMonitor("monitor");
         eventBuilder.setNtEventCode(r.nextInt(50000));
         eventBuilder.setSeverity(EventSeverity.SEVERITY_CRITICAL);
         eventBuilder.setSummary("summary message");
         eventBuilder.setSyslogFacility(11);
         eventBuilder.setSyslogPriority(SyslogPriority.SYSLOG_PRIORITY_DEBUG);
         return eventBuilder.build();
     }
 
     @Test
     public void testListByUuid() throws ZepException {
         Set<String> uuidsToSearch = new HashSet<String>();
         for (int i = 0; i < 10; i++) {
             String uuid = createSummaryNew(createUniqueEvent()).getUuid();
             if ((i % 2) == 0) {
                 uuidsToSearch.add(uuid);
             }
         }
 
         List<EventSummary> result = eventSummaryDao
                 .findByUuids(new ArrayList<String>(uuidsToSearch));
         assertEquals(uuidsToSearch.size(), result.size());
         for (EventSummary event : result) {
             assertTrue(uuidsToSearch.contains(event.getUuid()));
         }
     }
 
     @Test
     public void testReopen() throws ZepException {
         EventSummary summary = createSummaryNew(createUniqueEvent());
         long origStatusChange = summary.getStatusChangeTime();
         long origUpdateTime = summary.getUpdateTime();
         assertEquals(EventStatus.STATUS_NEW, summary.getStatus());
         assertFalse(summary.hasCurrentUserUuid());
         assertFalse(summary.hasCurrentUserName());
 
         String userUuid = UUID.randomUUID().toString();
         String userName = "user" + random.nextInt(500);
 
         int numUpdated = eventSummaryDao.close(Collections.singletonList(summary.getUuid()), userUuid, userName);
         assertEquals(1, numUpdated);
         summary = eventSummaryDao.findByUuid(summary.getUuid());
         assertEquals(EventStatus.STATUS_CLOSED, summary.getStatus());
         assertTrue(summary.getStatusChangeTime() > origStatusChange);
         assertTrue(summary.getUpdateTime() > origUpdateTime);
         origStatusChange = summary.getStatusChangeTime();
         origUpdateTime = summary.getUpdateTime();
 
         /* Now reopen event */
         numUpdated = eventSummaryDao.reopen(Collections.singletonList(summary.getUuid()), userUuid, userName);
         assertEquals(1, numUpdated);
         EventSummary origSummary = summary;
         summary = eventSummaryDao.findByUuid(summary.getUuid());
        assertFalse(summary.hasCurrentUserUuid());
        assertFalse(summary.hasCurrentUserName());
         assertEquals(EventStatus.STATUS_NEW, summary.getStatus());
         assertTrue(summary.getStatusChangeTime() > origStatusChange);
         assertTrue(summary.getUpdateTime() > origUpdateTime);
 
         compareSummary(origSummary, summary);
     }
 
     @Test
     public void testAcknowledge() throws ZepException {
         EventSummary summary = createSummaryNew(createUniqueEvent());
         long origStatusChange = summary.getStatusChangeTime();
         long origUpdateTime = summary.getUpdateTime();
         assertEquals(EventStatus.STATUS_NEW, summary.getStatus());
         assertFalse(summary.hasCurrentUserUuid());
         assertFalse(summary.hasCurrentUserName());
 
         String userUuid = UUID.randomUUID().toString();
         String userName = "user" + random.nextInt(500);
         EventSummary origSummary = summary;
         int numUpdated = eventSummaryDao.acknowledge(
                 Collections.singletonList(summary.getUuid()), userUuid, userName);
         assertEquals(1, numUpdated);
         summary = eventSummaryDao.findByUuid(summary.getUuid());
         assertEquals(userUuid, summary.getCurrentUserUuid());
         assertEquals(userName, summary.getCurrentUserName());
         assertEquals(EventStatus.STATUS_ACKNOWLEDGED, summary.getStatus());
         assertTrue(summary.getStatusChangeTime() > origStatusChange);
         assertTrue(summary.getUpdateTime() > origUpdateTime);
 
         compareSummary(origSummary, summary);
 
         /* Try acknowleding event again with the same user / uuid */
         assertEquals(0, eventSummaryDao.acknowledge(Collections.singletonList(summary.getUuid()), userUuid, userName));
 
         /* Change uuid and verify acknowledgement goes through */
         userUuid = UUID.randomUUID().toString();
         assertEquals(1, eventSummaryDao.acknowledge(Collections.singletonList(summary.getUuid()), userUuid, userName));
 
         /* Change username and verify acknowledgement goes through */
         userName = userName + "_1";
         assertEquals(1, eventSummaryDao.acknowledge(Collections.singletonList(summary.getUuid()), userUuid, userName));
 
         /* Change uuid to null and verify acknowledgement goes through */
         userUuid = null;
         assertEquals(1, eventSummaryDao.acknowledge(Collections.singletonList(summary.getUuid()), userUuid, userName));
 
         /* Change username to null and verify acknowledgement goes through */
         userName = null;
         assertEquals(1, eventSummaryDao.acknowledge(Collections.singletonList(summary.getUuid()), userUuid, userName));
 
         /* Acknowledge again with both set to null - shouldn't change */
         assertEquals(0, eventSummaryDao.acknowledge(Collections.singletonList(summary.getUuid()), userUuid, userName));
     }
 
     @Test
     public void testAcknowledgeNullUserNameUuid() throws ZepException {
         EventSummary summary = createSummaryNew(createUniqueEvent());
         long origStatusChange = summary.getStatusChangeTime();
         long origUpdateTime = summary.getUpdateTime();
         assertEquals(EventStatus.STATUS_NEW, summary.getStatus());
         assertFalse(summary.hasCurrentUserUuid());
         assertFalse(summary.hasCurrentUserName());
 
         EventSummary origSummary = summary;
         int numUpdated = eventSummaryDao.acknowledge(Collections.singletonList(summary.getUuid()), null, null);
         assertEquals(1, numUpdated);
         summary = eventSummaryDao.findByUuid(summary.getUuid());
         assertFalse(summary.hasCurrentUserUuid());
         assertFalse(summary.hasCurrentUserName());
         assertEquals(EventStatus.STATUS_ACKNOWLEDGED, summary.getStatus());
         assertTrue(summary.getStatusChangeTime() > origStatusChange);
         assertTrue(summary.getUpdateTime() > origUpdateTime);
 
         compareSummary(origSummary, summary);
     }
 
     @Test
     public void testSuppress() throws ZepException {
         EventSummary summary = createSummaryNew(createUniqueEvent());
         long origStatusChange = summary.getStatusChangeTime();
         long origUpdateTime = summary.getUpdateTime();
         assertEquals(EventStatus.STATUS_NEW, summary.getStatus());
         assertFalse(summary.hasCurrentUserUuid());
         assertFalse(summary.hasCurrentUserName());
 
         EventSummary origSummary = summary;
         int numUpdated = eventSummaryDao.suppress(Collections
                 .singletonList(summary.getUuid()));
         assertEquals(1, numUpdated);
         summary = eventSummaryDao.findByUuid(summary.getUuid());
         assertEquals(EventStatus.STATUS_SUPPRESSED, summary.getStatus());
         assertTrue(summary.getStatusChangeTime() > origStatusChange);
         assertTrue(summary.getUpdateTime() > origUpdateTime);
 
         compareSummary(origSummary, summary);
     }
 
     @Test
     public void testClose() throws ZepException {
         EventSummary summary = createSummaryNew(createUniqueEvent());
         long origStatusChange = summary.getStatusChangeTime();
         long origUpdateTime = summary.getUpdateTime();
         assertEquals(EventStatus.STATUS_NEW, summary.getStatus());
         assertFalse(summary.hasCurrentUserUuid());
         assertFalse(summary.hasCurrentUserName());
 
         String userUuid = UUID.randomUUID().toString();
         String userName = "user" + random.nextInt(500);
         EventSummary origSummary = summary;
         int numUpdated = eventSummaryDao.close(Collections
                 .singletonList(summary.getUuid()), userUuid, userName);
         assertEquals(1, numUpdated);
         summary = eventSummaryDao.findByUuid(summary.getUuid());
         assertEquals(EventStatus.STATUS_CLOSED, summary.getStatus());
         assertTrue(summary.getStatusChangeTime() > origStatusChange);
         assertTrue(summary.getUpdateTime() > origUpdateTime);
        assertFalse(summary.hasCurrentUserUuid());
        assertFalse(summary.hasCurrentUserName());
 
         compareSummary(origSummary, summary);
     }
 
     @Test
     public void testAddNote() throws ZepException {
         EventSummary summary = createSummaryNew(createUniqueEvent());
         assertEquals(0, summary.getNotesCount());
         EventNote note = EventNote.newBuilder().setMessage("My Note")
                 .setUserName("pkw").setUserUuid(UUID.randomUUID().toString())
                 .build();
         assertEquals(1, eventSummaryDao.addNote(summary.getUuid(), note));
         EventNote note2 = EventNote.newBuilder().setMessage("My Note 2")
                 .setUserName("kww").setUserUuid(UUID.randomUUID().toString())
                 .build();
         assertEquals(1, eventSummaryDao.addNote(summary.getUuid(), note2));
         summary = eventSummaryDao.findByUuid(summary.getUuid());
         assertEquals(2, summary.getNotesCount());
         // Notes returned in descending order to match previous behavior
         assertEquals("My Note 2", summary.getNotes(0).getMessage());
         assertEquals("kww", summary.getNotes(0).getUserName());
         assertEquals("My Note", summary.getNotes(1).getMessage());
         assertEquals("pkw", summary.getNotes(1).getUserName());
     }
 
     @Test
     public void testUpdateDetailsReplace() throws ZepException {
         Event newEvent = createUniqueEvent();
         Event.Builder builder = Event.newBuilder(newEvent).clearDetails();
         builder.addDetails(createDetail("A", "A"));
         builder.addDetails(createDetail("B", "B"));
         builder.addDetails(createDetail("C", "C"));
         newEvent = builder.build();
 
         EventSummary summary = createSummaryNew(newEvent);
         Event storedEvent = summary.getOccurrence(0);
         assertEquals(3, storedEvent.getDetailsCount());
 
         List<EventDetail> newDetailsList = new ArrayList<EventDetail>();
         newDetailsList.add(createDetail("B", "B1"));
         newDetailsList.add(createDetail("C"));
         newDetailsList.add(createDetail("D", "D"));
         EventDetailSet newDetails = EventDetailSet.newBuilder().addAllDetails(newDetailsList).build();
 
         // ensure update works correctly
         assertEquals(1, eventSummaryDao.updateDetails(summary.getUuid(), newDetails));
 
         // verify new contents of details
         summary = eventSummaryDao.findByUuid(summary.getUuid());
 
         List<EventDetail> resultDetails = summary.getOccurrence(0).getDetailsList();
         assertEquals(3, resultDetails.size());
 
         Map<String, List<String>> resultDetailsMap = detailsToMap(resultDetails);
         assertEquals(Collections.singletonList("A"), resultDetailsMap.get("A"));
         assertEquals(Collections.singletonList("B1"), resultDetailsMap.get("B"));
         assertEquals(Collections.singletonList("D"), resultDetailsMap.get("D"));
     }
 
     @Test
     public void testUpdateDetailsAppend() throws ZepException {
         Event newEvent = createUniqueEvent();
         Event.Builder builder = Event.newBuilder(newEvent).clearDetails();
         builder.addDetails(createDetail("A", "A"));
         builder.addDetails(createDetail("B", "B"));
         builder.addDetails(createDetail("C", "C"));
         newEvent = builder.build();
 
         EventSummary summary = createSummaryNew(newEvent);
         Event storedEvent = summary.getOccurrence(0);
         assertEquals(3, storedEvent.getDetailsCount());
 
         List<EventDetail> newDetailsList = new ArrayList<EventDetail>();
         newDetailsList.add(createDetail("A", EventDetailMergeBehavior.APPEND, "A1", "A2"));
         EventDetailSet newDetails = EventDetailSet.newBuilder().addAllDetails(newDetailsList).build();
 
         // ensure update works correctly
         assertEquals(1, eventSummaryDao.updateDetails(summary.getUuid(), newDetails));
 
         // verify new contents of details
         summary = eventSummaryDao.findByUuid(summary.getUuid());
 
         List<EventDetail> resultDetails = summary.getOccurrence(0).getDetailsList();
         assertEquals(3, resultDetails.size());
 
         Map<String, List<String>> resultDetailsMap = detailsToMap(resultDetails);
         assertEquals(Arrays.asList("A", "A1", "A2"), resultDetailsMap.get("A"));
         assertEquals(Collections.singletonList("B"), resultDetailsMap.get("B"));
         assertEquals(Collections.singletonList("C"), resultDetailsMap.get("C"));
     }
 
     @Test
     public void testUpdateDetailsUnique() throws ZepException {
         Event newEvent = createUniqueEvent();
         Event.Builder builder = Event.newBuilder(newEvent).clearDetails();
         builder.addDetails(createDetail("A", "A1", "A2", "A3"));
         builder.addDetails(createDetail("B", "B"));
         builder.addDetails(createDetail("C", "C"));
         newEvent = builder.build();
 
         EventSummary summary = createSummaryNew(newEvent);
         Event storedEvent = summary.getOccurrence(0);
         assertEquals(3, storedEvent.getDetailsCount());
 
         List<EventDetail> newDetailsList = new ArrayList<EventDetail>();
         newDetailsList.add(createDetail("A", EventDetailMergeBehavior.UNIQUE, "A4", "A1", "A5", "A2"));
         EventDetailSet newDetails = EventDetailSet.newBuilder().addAllDetails(newDetailsList).build();
 
         // ensure update works correctly
         assertEquals(1, eventSummaryDao.updateDetails(summary.getUuid(), newDetails));
 
         // verify new contents of details
         summary = eventSummaryDao.findByUuid(summary.getUuid());
 
         List<EventDetail> resultDetails = summary.getOccurrence(0).getDetailsList();
         assertEquals(3, resultDetails.size());
 
         Map<String, List<String>> resultDetailsMap = detailsToMap(resultDetails);
         assertEquals(Arrays.asList("A1", "A2", "A3", "A4", "A5"), resultDetailsMap.get("A"));
         assertEquals(Collections.singletonList("B"), resultDetailsMap.get("B"));
         assertEquals(Collections.singletonList("C"), resultDetailsMap.get("C"));
     }
 
     private Event createOldEvent(long duration, TimeUnit unit,
             EventSeverity severity) {
         Event.Builder eventBuilder = Event.newBuilder(createUniqueEvent());
         eventBuilder.setCreatedTime(System.currentTimeMillis()
                 - unit.toMillis(duration));
         eventBuilder.setSeverity(severity);
         return eventBuilder.build();
     }
 
     @Test
     public void testAgeEvents() throws ZepException {
         EventSummary warning = createSummaryNew(createOldEvent(5,
                 TimeUnit.MINUTES, EventSeverity.SEVERITY_WARNING));
         EventSummary error = createSummaryNew(createOldEvent(5,
                 TimeUnit.MINUTES, EventSeverity.SEVERITY_ERROR));
         EventSummary info = createSummaryNew(createOldEvent(5,
                 TimeUnit.MINUTES, EventSeverity.SEVERITY_INFO));
 
         /* Aging should have aged WARNING and INFO events and left ERROR */
         int numAged = eventSummaryDao.ageEvents(4, TimeUnit.MINUTES,
                 EventSeverity.SEVERITY_ERROR, 100, false);
         assertEquals(2, numAged);
         assertEquals(error, eventSummaryDao.findByUuid(error.getUuid()));
         /* Compare ignoring status change time */
         EventSummary summaryWarning = eventSummaryDao.findByUuid(warning
                 .getUuid());
         assertTrue(summaryWarning.getStatusChangeTime() > warning
                 .getStatusChangeTime());
         assertEquals(EventStatus.STATUS_AGED, summaryWarning.getStatus());
         EventSummary summaryInfo = eventSummaryDao.findByUuid(info.getUuid());
         assertTrue(summaryInfo.getStatusChangeTime() > info
                 .getStatusChangeTime());
         assertEquals(EventStatus.STATUS_AGED, summaryInfo.getStatus());
         compareSummary(summaryWarning, warning);
         compareSummary(summaryInfo, info);
 
         int archived = eventSummaryDao.archive(4, TimeUnit.MINUTES, 100);
         assertEquals(2, archived);
         assertNull(eventSummaryDao.findByUuid(warning.getUuid()));
         assertNull(eventSummaryDao.findByUuid(info.getUuid()));
         /* Compare ignoring status change time */
         EventSummary archivedWarning = eventArchiveDao.findByUuid(warning
                 .getUuid());
         assertTrue(archivedWarning.getStatusChangeTime() > warning
                 .getStatusChangeTime());
         EventSummary archivedInfo = eventArchiveDao.findByUuid(info.getUuid());
         assertTrue(archivedInfo.getStatusChangeTime() > info
                 .getStatusChangeTime());
         compareSummary(archivedWarning, summaryWarning);
         compareSummary(archivedInfo, summaryInfo);
 
         /* Aging with inclusive severity should age WARNING, INFO, and ERROR */
         numAged = eventSummaryDao.ageEvents(4, TimeUnit.MINUTES,
                 EventSeverity.SEVERITY_ERROR, 100, true);
         assertEquals(1, numAged);
         EventSummary summaryError = eventSummaryDao.findByUuid(error.getUuid());
         assertEquals(EventStatus.STATUS_AGED, summaryError.getStatus());
     }
 
     @Test
     public void testArchiveEventsEmpty() throws ZepException {
         // Make sure we don't fail if there are no events to archive
         int numArchived = this.eventSummaryDao.archive(0L, TimeUnit.SECONDS,
                 500);
         assertEquals(0, numArchived);
     }
 
     @Test
     public void testClearEvents() throws ZepException {
         Event event = createUniqueEvent();
         EventSummary normalEvent = createSummaryNew(Event.newBuilder(event)
                 .setSeverity(EventSeverity.SEVERITY_WARNING)
                 .setEventKey("MyKey1").build());
         EventSummary clearEvent = createSummaryClear(
                 Event.newBuilder(createUniqueEvent())
                         .setSeverity(EventSeverity.SEVERITY_CLEAR)
                         .setActor(event.getActor()).setEventKey("MyKey1").build(),
                 Collections.singleton(normalEvent.getOccurrence(0)
                         .getEventClass()));
         assertEquals(EventStatus.STATUS_CLOSED, clearEvent.getStatus());
         EventSummary normalEventSummary = eventSummaryDao
                 .findByUuid(normalEvent.getUuid());
         assertEquals(EventStatus.STATUS_CLEARED, normalEventSummary.getStatus());
         assertEquals(clearEvent.getUuid(), normalEventSummary.getClearedByEventUuid());
         assertTrue(normalEventSummary.getStatusChangeTime() > normalEvent.getStatusChangeTime());
         compareSummary(normalEvent, normalEventSummary);
     }
 
     @Test
     public void testClearEventsCustomGenerator() throws ZepException {
         Event event = Event.newBuilder(createUniqueEvent()).setSeverity(EventSeverity.SEVERITY_WARNING)
                 .setEventKey("MyKey2").build();
         Event clear = Event.newBuilder(createUniqueEvent()).setSeverity(EventSeverity.SEVERITY_CLEAR)
                 .setActor(event.getActor()).setEventKey("MyKey2").build();
         ClearFingerprintGenerator generator = new ClearFingerprintGenerator() {
             @Override
             public String generateClearFingerprint(Event event) {
                 return EventDaoUtils.DEFAULT_GENERATOR.generateClearFingerprint(event) + "|MyCustomField";
             }
 
             @Override
             public List<String> generateClearFingerprints(Event event, Set<String> clearClasses) {
                 final List<String> original = EventDaoUtils.DEFAULT_GENERATOR.generateClearFingerprints(event,
                         clearClasses);
                 final List<String> updated = new ArrayList<String>(original.size());
                 for (String originalFingerprint : original) {
                     updated.add(originalFingerprint + "|MyCustomField");
                 }
                 return updated;
             }
         };
         EventPreCreateContextImpl ctx = new EventPreCreateContextImpl();
         ctx.setClearFingerprintGenerator(generator);
         EventSummary normalEvent = eventSummaryDao.findByUuid(eventSummaryDao.create(event, ctx));
         ctx.getClearClasses().add(event.getEventClass());
         EventSummary clearEvent = eventSummaryDao.findByUuid(eventSummaryDao.create(clear, ctx));
         assertEquals(EventStatus.STATUS_CLOSED, clearEvent.getStatus());
         EventSummary normalEventSummary = eventSummaryDao.findByUuid(normalEvent.getUuid());
         assertEquals(EventStatus.STATUS_CLEARED, normalEventSummary.getStatus());
         assertEquals(clearEvent.getUuid(), normalEventSummary.getClearedByEventUuid());
         assertTrue(normalEventSummary.getStatusChangeTime() > normalEvent.getStatusChangeTime());
         compareSummary(normalEvent, normalEventSummary);
     }
 
     @Test
     public void testMergeDuplicateDetails() throws ZepException {
         String name = "dup1";
         String val1 = "dupval";
         String val2 = "dupval2";
         String val3 = "dupval3";
         Event.Builder eventBuilder = Event.newBuilder(createUniqueEvent())
                 .clearDetails();
         eventBuilder.addDetails(createDetail(name, val1));
         eventBuilder.addDetails(createDetail(name, val2, val3));
         Event event = eventBuilder.build();
         EventSummary summary = createSummaryNew(event);
         assertEquals(1, summary.getOccurrence(0).getDetailsCount());
         EventDetail detailFromDb = summary.getOccurrence(0).getDetails(0);
         assertEquals(name, detailFromDb.getName());
         assertEquals(Arrays.asList(val1, val2, val3),
                 detailFromDb.getValueList());
     }
 
     @Test
     public void testFilterDuplicateTags() throws ZepException {
         Event.Builder eventBuilder = Event.newBuilder(createUniqueEvent())
                 .clearTags();
         String uuid = UUID.randomUUID().toString();
         eventBuilder.addTags(EventTag.newBuilder()
                 .setType(ModelElementType.DEVICE.name()).addUuid(uuid).build());
         eventBuilder.addTags(EventTag.newBuilder()
                 .setType(ModelElementType.DEVICE.name()).addUuid(uuid).build());
         eventBuilder.addTags(EventTag.newBuilder()
                 .setType(ModelElementType.DEVICE.name()).addUuid(uuid).build());
         Event event = eventBuilder.build();
         EventSummary summary = createSummaryNew(event);
         int numFound = 0;
         for (EventTag tag : summary.getOccurrence(0).getTagsList()) {
             for (String tagUuid : tag.getUuidList()) {
                 if (tagUuid.equals(uuid)) {
                     numFound++;
                 }
             }
         }
         assertEquals(1, numFound);
     }
 
     @Test
     public void testChangeSeverity() throws ZepException {
         // Verify that severity changes when new event with same fingerprint comes in with new severity
         Event.Builder firstBuilder = Event.newBuilder(createUniqueEvent());
         firstBuilder.setSeverity(EventSeverity.SEVERITY_WARNING);
         Event first = firstBuilder.build();
 
         EventSummary firstSummary = createSummaryNew(first);
 
         Event.Builder secondBuilder = Event.newBuilder(first);
         secondBuilder.setCreatedTime(first.getCreatedTime()+10);
         secondBuilder.setSeverity(EventSeverity.SEVERITY_INFO);
         Event second = secondBuilder.build();
 
         EventSummary secondSummary = createSummaryNew(second);
         assertEquals(firstSummary.getUuid(), secondSummary.getUuid());
         assertEquals(2, secondSummary.getCount());
         assertEquals(EventSeverity.SEVERITY_INFO, secondSummary.getOccurrence(0).getSeverity());
     }
 
     @Test
     public void testReidentifyDevice() throws ZepException {
         Event.Builder builder = Event.newBuilder(createUniqueEvent());
         EventActor.Builder actorBuilder = EventActor.newBuilder(builder.getActor());
         actorBuilder.clearElementSubIdentifier().clearElementSubTypeId().clearElementSubUuid();
         actorBuilder.clearElementUuid();
         builder.setActor(actorBuilder.build());
 
         EventSummary summary = createSummaryNew(builder.build());
         Event occurrence = summary.getOccurrence(0);
         assertFalse(occurrence.getActor().hasElementUuid());
 
         final String elementUuid = UUID.randomUUID().toString();
         int numRows = this.eventSummaryDao.reidentify(occurrence.getActor().getElementTypeId(),
                 occurrence.getActor().getElementIdentifier(), elementUuid, occurrence.getActor().getElementTitle(),
                 null);
         assertEquals(1, numRows);
         EventSummary summaryFromDb = this.eventSummaryDao.findByUuid(summary.getUuid());
         assertTrue(summaryFromDb.getUpdateTime() > summary.getUpdateTime());
         assertEquals(elementUuid, summaryFromDb.getOccurrence(0).getActor().getElementUuid());
     }
 
     @Test
     public void testReidentifyLongTitle() throws ZepException {
         Event.Builder builder = Event.newBuilder(createUniqueEvent());
         EventActor.Builder actorBuilder = EventActor.newBuilder(builder.getActor());
         actorBuilder.clearElementSubIdentifier().clearElementSubTypeId().clearElementSubUuid();
         actorBuilder.clearElementUuid().clearElementTitle();
         builder.setActor(actorBuilder.build());
 
         EventSummary summary = createSummaryNew(builder.build());
         Event occurrence = summary.getOccurrence(0);
         assertFalse(occurrence.getActor().hasElementUuid());
 
         final String elementUuid = UUID.randomUUID().toString();
         final String elementTitle = createRandomMaxString(EventConstants.MAX_ELEMENT_TITLE + 1);
         int numRows = this.eventSummaryDao.reidentify(occurrence.getActor().getElementTypeId(),
                 occurrence.getActor().getElementIdentifier(), elementUuid, elementTitle,
                 null);
         assertEquals(1, numRows);
         EventSummary summaryFromDb = this.eventSummaryDao.findByUuid(summary.getUuid());
         assertTrue(summaryFromDb.getUpdateTime() > summary.getUpdateTime());
         assertEquals(elementUuid, summaryFromDb.getOccurrence(0).getActor().getElementUuid());
         assertEquals(EventConstants.MAX_ELEMENT_TITLE,
                 summaryFromDb.getOccurrence(0).getActor().getElementTitle().length());
         assertEquals(elementTitle.substring(0, EventConstants.MAX_ELEMENT_TITLE),
                 summaryFromDb.getOccurrence(0).getActor().getElementTitle());
     }
 
     @Test
     public void testReidentifyComponent() throws ZepException, NoSuchAlgorithmException, UnsupportedEncodingException {
         Event.Builder builder = Event.newBuilder(createUniqueEvent());
         EventActor.Builder actorBuilder = EventActor.newBuilder(builder.getActor());
         actorBuilder.clearElementSubUuid();
         builder.setActor(actorBuilder.build());
 
         EventSummary summary = createSummaryNew(builder.build());
         Event occurrence = summary.getOccurrence(0);
         EventActor actor = occurrence.getActor();
         assertFalse(occurrence.getActor().hasElementSubUuid());
 
         final String elementSubUuid = UUID.randomUUID().toString();
         int numRows = this.eventSummaryDao.reidentify(actor.getElementSubTypeId(),
                 actor.getElementSubIdentifier(), elementSubUuid, actor.getElementSubTitle(), actor.getElementUuid());
         assertEquals(1, numRows);
         EventSummary summaryFromDb = this.eventSummaryDao.findByUuid(summary.getUuid());
         assertTrue(summaryFromDb.getUpdateTime() > summary.getUpdateTime());
         assertEquals(elementSubUuid, summaryFromDb.getOccurrence(0).getActor().getElementSubUuid());
         // Ensure clear_fingerprint_hash was updated
         String clearHashString = EventDaoUtils.join('|', elementSubUuid, occurrence.getEventClass(),
                 occurrence.getEventKey());
         byte[] clearHash = DaoUtils.sha1(clearHashString);
         Map<String,Object> fields = Collections.singletonMap(COLUMN_UUID,
                 databaseCompatibility.getUUIDConverter().toDatabaseType(summary.getUuid()));
         byte[] clearHashFromDb = this.simpleJdbcTemplate.query(
                 "SELECT clear_fingerprint_hash FROM event_summary WHERE uuid=:uuid",
                 new RowMapper<byte[]>() {
                     @Override
                     public byte[] mapRow(ResultSet rs, int rowNum) throws SQLException {
                         return rs.getBytes("clear_fingerprint_hash");
                     }
                 }, fields).get(0);
         assertArrayEquals(clearHash, clearHashFromDb);
     }
 
     @Test
     public void testDeidentifyDevice() throws ZepException {
         EventSummary summary = createSummaryNew(createUniqueEvent());
         Event occurrence = summary.getOccurrence(0);
         EventActor actor = occurrence.getActor();
         assertTrue(occurrence.getActor().hasElementUuid());
 
         int numRows = this.eventSummaryDao.deidentify(actor.getElementUuid());
         assertEquals(1, numRows);
         EventSummary summaryFromDb = this.eventSummaryDao.findByUuid(summary.getUuid());
         assertTrue(summaryFromDb.getUpdateTime() > summary.getUpdateTime());
         assertFalse(summaryFromDb.getOccurrence(0).getActor().hasElementUuid());
     }
 
     @Test
     public void testDeidentifyComponent() throws ZepException, NoSuchAlgorithmException, UnsupportedEncodingException {
         EventSummary summary = createSummaryNew(createUniqueEvent());
         Event occurrence = summary.getOccurrence(0);
         EventActor actor = occurrence.getActor();
         assertTrue(occurrence.getActor().hasElementSubUuid());
 
         int numRows = this.eventSummaryDao.deidentify(actor.getElementSubUuid());
         assertEquals(1, numRows);
         EventSummary summaryFromDb = this.eventSummaryDao.findByUuid(summary.getUuid());
         assertTrue(summaryFromDb.getUpdateTime() > summary.getUpdateTime());
         assertFalse(summaryFromDb.getOccurrence(0).getActor().hasElementSubUuid());
         // Ensure clear_fingerprint_hash was updated
         String clearHashString = EventDaoUtils.join('|', actor.getElementIdentifier(),
                 actor.getElementSubIdentifier(), occurrence.getEventClass(),
                 occurrence.getEventKey());
         byte[] clearHash = DaoUtils.sha1(clearHashString);
         Map<String,Object> fields = Collections.singletonMap(COLUMN_UUID,
                 databaseCompatibility.getUUIDConverter().toDatabaseType(summary.getUuid()));
         byte[] clearHashFromDb = this.simpleJdbcTemplate.query(
                 "SELECT clear_fingerprint_hash FROM event_summary WHERE uuid=:uuid",
                 new RowMapper<byte[]>() {
                     @Override
                     public byte[] mapRow(ResultSet rs, int rowNum) throws SQLException {
                         return rs.getBytes("clear_fingerprint_hash");
                     }
                 }, fields).get(0);
         assertArrayEquals(clearHash, clearHashFromDb);
     }
 
     private static Map<String,List<String>> detailsToMap(List<EventDetail> details) {
         Map<String,List<String>> detailsMap = new HashMap<String,List<String>>(details.size());
         for (EventDetail detail : details) {
             detailsMap.put(detail.getName(), detail.getValueList());
         }
         return detailsMap;
     }
 
     private static EventDetail createDetail(String name, String... values) {
         return createDetail(name, null, values);
     }
 
     private static EventDetail createDetail(String name, EventDetailMergeBehavior mergeBehavior, String... values) {
         EventDetail.Builder detailBuilder = EventDetail.newBuilder();
         detailBuilder.setName(name);
         if (mergeBehavior != null) {
             detailBuilder.setMergeBehavior(mergeBehavior);
         }
         for (String value : values) {
             detailBuilder.addValue(value);
         }
         return detailBuilder.build();
     }
 
     @Test
     public void testMergeDetailsReplace() throws ZepException {
         Event.Builder eventBuilder = Event.newBuilder(createUniqueEvent());
         eventBuilder.clearDetails();
         eventBuilder.addDetails(createDetail("foo", "bar", "baz"));
         eventBuilder.addDetails(createDetail("foo2", "bar2", "baz2"));
         final Event event = eventBuilder.build();
 
         EventSummary summary = createSummaryNew(event);
         compareEvents(event, summary.getOccurrence(0));
 
         /* Add a new detail, don't specify an old one, and replace an existing one */
         Event.Builder newEventBuilder = Event.newBuilder(event);
         newEventBuilder.clearDetails();
         /* Update foo */
         newEventBuilder.addDetails(createDetail("foo", "foobar", "foobaz"));
         /* Don't specify foo2 */
         /* Add a new detail foo3 */
         newEventBuilder.addDetails(createDetail("foo3", "foobar3", "foobaz3"));
         final Event newEvent = newEventBuilder.build();
 
         EventSummary newSummary = createSummaryNew(newEvent);
         assertEquals(2, newSummary.getCount());
         Map<String,List<String>> detailsMap = detailsToMap(newSummary.getOccurrence(0).getDetailsList());
         assertEquals(Arrays.asList("foobar","foobaz"), detailsMap.get("foo"));
         assertEquals(Arrays.asList("bar2", "baz2"), detailsMap.get("foo2"));
         assertEquals(Arrays.asList("foobar3", "foobaz3"), detailsMap.get("foo3"));
     }
 
     @Test
     public void testMergeDetailsAppend() throws ZepException {
         Event.Builder eventBuilder = Event.newBuilder(createUniqueEvent());
         eventBuilder.clearDetails();
         eventBuilder.addDetails(createDetail("foo", "bar", "baz"));
         eventBuilder.addDetails(createDetail("foo2", "bar2", "baz2"));
         final Event event = eventBuilder.build();
 
         EventSummary summary = createSummaryNew(event);
         compareEvents(event, summary.getOccurrence(0));
 
         /* Add a new detail, don't specify an old one, and replace an existing one */
         Event.Builder newEventBuilder = Event.newBuilder(event);
         newEventBuilder.clearDetails();
         /* Update foo */
         newEventBuilder.addDetails(createDetail("foo", EventDetailMergeBehavior.APPEND, "bar", "foobar", "foobaz"));
         /* Don't specify foo2 */
         /* Add a new detail foo3 */
         newEventBuilder.addDetails(createDetail("foo3", "foobar3", "foobaz3"));
         final Event newEvent = newEventBuilder.build();
 
         EventSummary newSummary = createSummaryNew(newEvent);
         assertEquals(2, newSummary.getCount());
         Map<String,List<String>> detailsMap = detailsToMap(newSummary.getOccurrence(0).getDetailsList());
         assertEquals(Arrays.asList("bar", "baz", "bar", "foobar","foobaz"), detailsMap.get("foo"));
         assertEquals(Arrays.asList("bar2", "baz2"), detailsMap.get("foo2"));
         assertEquals(Arrays.asList("foobar3", "foobaz3"), detailsMap.get("foo3"));
     }
 
     @Test
     public void testMergeDetailsUnique() throws ZepException {
         Event.Builder eventBuilder = Event.newBuilder(createUniqueEvent());
         eventBuilder.clearDetails();
         eventBuilder.addDetails(createDetail("foo", "bar", "baz"));
         eventBuilder.addDetails(createDetail("foo2", "bar2", "baz2"));
         final Event event = eventBuilder.build();
 
         EventSummary summary = createSummaryNew(event);
         compareEvents(event, summary.getOccurrence(0));
 
         /* Add a new detail, don't specify an old one, and replace an existing one */
         Event.Builder newEventBuilder = Event.newBuilder(event);
         newEventBuilder.clearDetails();
         /* Update foo */
         newEventBuilder.addDetails(createDetail("foo", EventDetailMergeBehavior.UNIQUE, "baz", "foobar", "foobaz"));
         /* Don't specify foo2 */
         /* Add a new detail foo3 */
         newEventBuilder.addDetails(createDetail("foo3", "foobar3", "foobaz3"));
         final Event newEvent = newEventBuilder.build();
 
         EventSummary newSummary = createSummaryNew(newEvent);
         assertEquals(2, newSummary.getCount());
         Map<String,List<String>> detailsMap = detailsToMap(newSummary.getOccurrence(0).getDetailsList());
         assertEquals(Arrays.asList("bar", "baz", "foobar","foobaz"), detailsMap.get("foo"));
         assertEquals(Arrays.asList("bar2", "baz2"), detailsMap.get("foo2"));
         assertEquals(Arrays.asList("foobar3", "foobaz3"), detailsMap.get("foo3"));
     }
 
     @Test
     public void testArchive() throws ZepException {
         EventSummary summary1 = createSummaryNew(createUniqueEvent());
         EventSummary summary2 = createSummary(createUniqueEvent(), EventStatus.STATUS_CLOSED);
         EventSummary summary3 = createSummary(createUniqueEvent(), EventStatus.STATUS_CLEARED);
 
         List<String> uuids = Arrays.asList(summary1.getUuid(), summary2.getUuid(), summary3.getUuid());
         int numArchived = this.eventSummaryDao.archive(uuids);
         assertEquals(uuids.size()-1, numArchived);
         List<EventSummary> summaries = this.eventSummaryDao.findByUuids(uuids);
         assertEquals(1, summaries.size());
         assertEquals(summary1, summaries.get(0));
 
         List<EventSummary> archived = this.eventArchiveDao.findByUuids(uuids);
         assertEquals(2, archived.size());
         boolean found2 = false, found3 = false;
         for (EventSummary archivedSummary : archived) {
             if (summary2.getUuid().equals(archivedSummary.getUuid())) {
                 found2 = true;
             }
             else if (summary3.getUuid().equals(archivedSummary.getUuid())) {
                 found3 = true;
             }
         }
         assertTrue(found2 && found3);
     }
 
     @Test
     public void testMergeDetailsNull() throws ZepException {
         Event firstEvent = Event.newBuilder(createUniqueEvent()).clearDetails().build();
 
         EventSummary firstSummary = createSummaryNew(firstEvent);
         assertEquals(0, firstSummary.getOccurrence(0).getDetailsCount());
 
         Event secondEvent = Event.newBuilder(firstEvent).addDetails(createDetail("detail1", "A"))
                 .setCreatedTime(System.currentTimeMillis()).build();
         EventSummary secondSummary = createSummaryNew(secondEvent);
         assertEquals(firstSummary.getUuid(), secondSummary.getUuid());
         assertEquals(1, secondSummary.getOccurrence(0).getDetailsCount());
         assertEquals(secondEvent.getDetailsList(), secondSummary.getOccurrence(0).getDetailsList());
     }
 
     public static EventSummary createRandomSummary() {
         Random r = new Random();
         Event event = EventTestUtils.createSampleEvent();
         EventSummary.Builder summaryBuilder = EventSummary.newBuilder();
         summaryBuilder.addOccurrence(event);
         summaryBuilder.addAuditLog(EventAuditLog.newBuilder().setNewStatus(EventStatus.STATUS_ACKNOWLEDGED)
                 .setTimestamp(System.currentTimeMillis()).setUserName("pkw").setUserUuid(UUID.randomUUID().toString()));
         summaryBuilder.addAuditLog(EventAuditLog.newBuilder().setNewStatus(EventStatus.STATUS_CLOSED)
                 .setTimestamp(System.currentTimeMillis()).setUserName("pkw2")
                 .setUserUuid(UUID.randomUUID().toString()));
         summaryBuilder.setClearedByEventUuid(UUID.randomUUID().toString());
         summaryBuilder.setCount(r.nextInt(1000)+1);
         summaryBuilder.setStatus(EventStatus.STATUS_CLEARED);
         summaryBuilder.setCurrentUserName("pkw3");
         summaryBuilder.setCurrentUserUuid(UUID.randomUUID().toString());
         summaryBuilder.setFirstSeenTime(System.currentTimeMillis() - 5000);
         summaryBuilder.setLastSeenTime(System.currentTimeMillis());
         summaryBuilder.setStatusChangeTime(System.currentTimeMillis());
         summaryBuilder.setUuid(UUID.randomUUID().toString());
         summaryBuilder.addNotes(EventNote.newBuilder().setCreatedTime(System.currentTimeMillis()-100).setMessage("Note 1")
                 .setUserName("pkw4").setUserUuid(UUID.randomUUID().toString()));
         summaryBuilder.addNotes(EventNote.newBuilder().setCreatedTime(System.currentTimeMillis()).setMessage("Note 2")
                 .setUserName("pkw5").setUserUuid(UUID.randomUUID().toString()));
         return summaryBuilder.build();
     }
 
     public static void compareImported(EventSummary original, EventSummary imported) {
         // First, verify migrate_update_time == update_time in imported event
         final long updateTime = imported.getUpdateTime();
         int migratedDetailIndex = -1;
         for (int detailIndex = 0; detailIndex < imported.getOccurrence(0).getDetailsCount(); detailIndex++) {
             EventDetail detail = imported.getOccurrence(0).getDetails(detailIndex);
             if (ZepConstants.DETAIL_MIGRATE_UPDATE_TIME.equals(detail.getName())) {
                 // No duplicate details
                 assertEquals(-1, migratedDetailIndex);
                 migratedDetailIndex = detailIndex;
                 assertEquals(Arrays.asList(Long.toString(updateTime)), detail.getValueList());
             }
         }
         assertTrue(migratedDetailIndex >= 0);
 
         // Import process creates a detail and also updates the update_time on the protobuf - clear these fields
         // so that comparison can work on all the rest of the fields.
         EventSummary.Builder importedBuilder = EventSummary.newBuilder(imported);
         Event.Builder occurrenceBuilder = importedBuilder.getOccurrenceBuilder(0);
         importedBuilder.clearUpdateTime();
         occurrenceBuilder.removeDetails(migratedDetailIndex);
 
         // Original event has a generated UUID on the occurrence but we don't store an occurrence for migrated events -
         // clear it and the created_time to enable comparison.
         EventSummary.Builder originalBuilder = EventSummary.newBuilder(original);
         Event.Builder originalOccurrenceBuilder = originalBuilder.getOccurrenceBuilder(0);
         originalOccurrenceBuilder.clearUuid();
         originalOccurrenceBuilder.setCreatedTime(occurrenceBuilder.getCreatedTime());
         originalOccurrenceBuilder.clearTags().addAllTags(EventDaoHelper.buildTags(original.getOccurrence(0)));
 
         assertEquals(originalBuilder.build(), importedBuilder.build());
     }
 
     @Test
     public void testImportEvent() throws ZepException {
         EventSummary summary = EventSummaryDaoImplIT.createRandomSummary();
         this.eventSummaryDao.importEvent(summary);
         compareImported(summary, this.eventSummaryDao.findByUuid(summary.getUuid()));
         // Verify we can't insert an event more than once
         try {
             this.eventSummaryDao.importEvent(summary);
             fail("Expected duplicate import event to fail");
         } catch (DuplicateKeyException e) {
             // Expected
         }
     }
 
     @Test
     public void testDedupOutOfOrder() throws ZepException {
         Event event = EventTestUtils.createSampleEvent();
         EventSummary eventSummaryFromDb = createSummaryNew(event);
         Event eventFromSummary = eventSummaryFromDb.getOccurrence(0);
         compareEvents(event, eventFromSummary);
 
         Event firstEvent = Event.newBuilder(event).setCreatedTime(event.getCreatedTime() - 10000)
                 .setSummary("New summary entry").build();
         EventSummary updatedEventSummaryFromDb = createSummaryNew(firstEvent);
         Event updatedEventFromSummary = updatedEventSummaryFromDb.getOccurrence(0);
         compareEvents(event, updatedEventFromSummary);
         assertEquals(firstEvent.getCreatedTime(), updatedEventSummaryFromDb.getFirstSeenTime());
         assertEquals(event.getCreatedTime(), updatedEventSummaryFromDb.getLastSeenTime());
         assertEquals(2, updatedEventSummaryFromDb.getCount());
 
         Event middleEvent = Event.newBuilder(event).setCreatedTime(event.getCreatedTime() - 5000)
                 .setSummary("New summary entry").build();
         updatedEventSummaryFromDb = createSummaryNew(middleEvent);
         updatedEventFromSummary = updatedEventSummaryFromDb.getOccurrence(0);
         compareEvents(event, updatedEventFromSummary);
         assertEquals(firstEvent.getCreatedTime(), updatedEventSummaryFromDb.getFirstSeenTime());
         assertEquals(event.getCreatedTime(), updatedEventSummaryFromDb.getLastSeenTime());
         assertEquals(3, updatedEventSummaryFromDb.getCount());
 
         // Compare old and new summary items to verify the old event didn't change anything but update time and
         // first seen time.
         EventSummary oldSummaryForComparison = EventSummary.newBuilder(eventSummaryFromDb).clearUpdateTime()
                 .clearFirstSeenTime().clearCount().build();
         EventSummary newSummaryForComparison = EventSummary.newBuilder(updatedEventSummaryFromDb).clearUpdateTime()
                 .clearFirstSeenTime().clearCount().build();
         assertEquals(oldSummaryForComparison, newSummaryForComparison);
     }
 
     @Test
     public void testDedupDetailsOutOfOrder() throws ZepException {
         Event.Builder eventBuilder = Event.newBuilder(EventTestUtils.createSampleEvent());
         eventBuilder.clearDetails();
         eventBuilder.addDetails(EventDetail.newBuilder().setName("a").addAllValue(Arrays.asList("b", "c")));
         eventBuilder.addDetails(EventDetail.newBuilder().setName("b").addAllValue(Arrays.asList("b1", "b2")));
         Event event = eventBuilder.build();
         EventSummary eventSummaryFromDb = createSummaryNew(event);
 
         Event earlierEvent = Event.newBuilder(event).setCreatedTime(event.getCreatedTime()-5000)
                 .clearDetails()
                 .addDetails(EventDetail.newBuilder().setName("a").addAllValue(Arrays.asList("c", "d")))
                 .addDetails(EventDetail.newBuilder().setName("c").addAllValue(Arrays.asList("c1", "c2"))).build();
         EventSummary updatedEventSummaryFromDb = createSummaryNew(earlierEvent);
         Event updatedEvent = updatedEventSummaryFromDb.getOccurrence(0);
         assertEquals(3, updatedEvent.getDetailsCount());
 
         // Detail values from the later event take precedence over the ones from the earlier event
         Map<String,List<String>> detailsMap = detailsToMap(updatedEvent.getDetailsList());
         assertEquals(Arrays.asList("b", "c"), detailsMap.get("a"));
         assertEquals(Arrays.asList("b1", "b2"), detailsMap.get("b"));
         assertEquals(Arrays.asList("c1", "c2"), detailsMap.get("c"));
     }
 
 
     private EventDetail createDetailOfSize(String name, int detailLength,
                                            String prefix, EventDetailMergeBehavior behavior) {
         EventDetail.Builder detailBuilder = EventDetail.newBuilder();
         detailBuilder.setName(name);
         detailBuilder.addValue(prefix + createRandomMaxString(detailLength));
 
         if (behavior != null) {
             detailBuilder.setMergeBehavior(behavior);
         }
         return detailBuilder.build();
     }
 
     private int getStringStepSize() throws ZepException {
         // Maximum bytes divided by 2 will give the ballpark length of the largest
         // string. Subdivide this length by 4 so we get a decent step length
         // and can control when we go over the maximum size.
         return (int) configDao.getConfig().getEventMaxSizeBytes() / 8;
     }
 
 
     /**
      * Test that normal events with large details do not get modified if they are
      * smaller than the event_max_size_bytes parameter.
      *
      * @throws ZepException
      */
     @Test
     public void testMaxEventSizeCreationNormal() throws ZepException {
         int stringStepSize = getStringStepSize();
 
         List<EventDetail> baseDetails = new ArrayList<EventDetail>();
 
         baseDetails.add(createDetailOfSize("foo_append", stringStepSize, "validDetails-",
                 EventDetailMergeBehavior.APPEND));
         baseDetails.add(createDetailOfSize("bar_replace", stringStepSize, "validDetails-",
                 EventDetailMergeBehavior.REPLACE));
         baseDetails.add(createDetailOfSize("baz_unique", stringStepSize, "validDetails-",
                 EventDetailMergeBehavior.UNIQUE));
 
         Builder baseEventBuilder = Event.newBuilder(EventTestUtils.createSampleEvent()).clearDetails();
         baseEventBuilder.addAllDetails(baseDetails);
 
         Event validEvent = baseEventBuilder.build();
         EventSummary validSummary = createSummaryNew(validEvent);
 
         // Verify large, but not too large, details get created and indexed
         assertTrue(validSummary != null);
 
         // Verify that the details haven't been changed as they were saved
         assertEquals(validSummary.getOccurrence(0).getDetailsList(), baseDetails);
     }
 
 
     /**
      * Test that during creation events with details that are too large have their
      * non-Zenoss details truncated.
      *
      * @throws ZepException
      */
     @Test
     public void testMaxEventSizeCreationLarge() throws ZepException {
         int stringStepSize = getStringStepSize();
 
         List<EventDetail> baseDetails = new ArrayList<EventDetail>();
 
         baseDetails.add(createDetailOfSize("foo_append", stringStepSize*50, "tooLarge-",
                 EventDetailMergeBehavior.APPEND));
         baseDetails.add(createDetailOfSize("bar_replace", stringStepSize*50, "tooLarge-",
                 EventDetailMergeBehavior.REPLACE));
         baseDetails.add(createDetailOfSize("baz_unique", stringStepSize*50, "tooLarge-",
                 EventDetailMergeBehavior.UNIQUE));
 
         // Note: The event for this is a new sample event. This is to verify that
         // when creating a new event, if the details data is too large, the event
         // will have it's non-zenoss details truncated completely.
         final Builder invalidEventBuilder = Event.newBuilder(
                 EventTestUtils.createSampleEvent()).clearDetails();
         invalidEventBuilder.addAllDetails(baseDetails);
 
         // Add a legitimate zenoss detail and make sure it's not removed.
         invalidEventBuilder.addDetails(EventDetail.newBuilder()
                 .setName(ZepConstants.DETAIL_DEVICE_PRODUCTION_STATE)
                 .addValue("1000"));
 
         EventSummary invalidSummary = createSummaryNew(invalidEventBuilder.build());
 
         for (EventDetail detail : invalidSummary.getOccurrence(0).getDetailsList()) {
             assertTrue("Detail should be a zenoss detail: " + detail.getName(),
                     detail.getName().startsWith("zenoss."));
         }
 
         final Map<String, List<String>> createdDetails = detailsToMap(
                 invalidSummary.getOccurrence(0).getDetailsList());
         assertTrue("Created event should contain Zenoss details (production state).",
                 createdDetails.keySet().contains(ZepConstants.DETAIL_DEVICE_PRODUCTION_STATE));
 
     }
 
 
     /**
      * Sets up an event for testing with detail size tests.
      *
      * @param stringStepSize The length of the string value for each detail.
      * @return Event The original event to use in tests.
      * @throws ZepException
      */
     private Event setupEventForDetails(int stringStepSize) throws ZepException {
         final List<EventDetail> baseDetails = new ArrayList<EventDetail>();
         baseDetails.add(createDetailOfSize("foo_append", stringStepSize, "baseDetail-",
                 EventDetailMergeBehavior.APPEND));
         baseDetails.add(createDetailOfSize("bar_replace", stringStepSize, "baseDetail-",
                 EventDetailMergeBehavior.REPLACE));
         baseDetails.add(createDetailOfSize("baz_unique", stringStepSize, "baseDetail-",
                 EventDetailMergeBehavior.UNIQUE));
 
         final Builder baseEventBuilder = Event.newBuilder(EventTestUtils.createSampleEvent()).clearDetails();
         baseEventBuilder.addAllDetails(baseDetails);
         final Event originalEvent = baseEventBuilder.build();
         final EventSummary originalSummary = createSummaryNew(originalEvent);
 
         return originalEvent;
     }
 
 
     /**
      * Three cases to test for UPDATE scenario:
      *
      *  1.) new details + old details > max size && new details <= max size
      *      drop old details, add new details
      *
      *  2.) new details + old details > max size && new details > max size
      *      drop old details, drop non-zenoss new details
      *
      *  3.) new details + old details <= max size
      *      merge details like normal
      */
 
     /**
      * Verify that old details get dropped if new details are less than max
      * size and combined details are greater than max size.
      *
      * @throws org.zenoss.zep.ZepException
      */
     @Test
     public void testMaxEventSizeUpdateDropOld() throws ZepException {
         final int stringStepSize = getStringStepSize();
         final Event originalEvent = setupEventForDetails(stringStepSize);
 
         final List<EventDetail> additionalDetails = new ArrayList<EventDetail>();
         additionalDetails.add(createDetailOfSize("foo_append", stringStepSize, "additionalDetail-",
                 EventDetailMergeBehavior.APPEND));
         additionalDetails.add(createDetailOfSize("bar_replace", stringStepSize, "additionalDetail-",
                 EventDetailMergeBehavior.REPLACE));
         additionalDetails.add(createDetailOfSize("baz_unique", stringStepSize, "additionalDetail-",
                 EventDetailMergeBehavior.UNIQUE));
 
         final Event.Builder newEventBuilder = Event.newBuilder(originalEvent);
         newEventBuilder.setCreatedTime(newEventBuilder.getCreatedTime() + 1);
         newEventBuilder.clearDetails();
         newEventBuilder.addAllDetails(additionalDetails);
 
         final Event newEvent = newEventBuilder.build();
         assertTrue("New details don't contain additional details at all?",
                 detailsToMap(newEvent.getDetailsList()).containsKey("foo_append"));
 
         final EventSummary newSummary = createSummaryNew(newEventBuilder.build());
 
         assertEquals("Old details should have been dropped.",
                 additionalDetails, newSummary.getOccurrence(0).getDetailsList());
     }
 
     /**
      * Verify that new details that are too large have non-Zenoss details dropped.
      *
      * If combined new and old details are too large and the new details alone
      * are too large, drop the old details as well as the non-Zenoss new details.
      *
      * @throws org.zenoss.zep.ZepException
      */
     @Test
     public void testMaxEventSizeUpdateDropNew() throws ZepException {
         final int stringStepSize = getStringStepSize();
         final Event originalEvent = setupEventForDetails(stringStepSize);
 
         final List<EventDetail> largeDetails = new ArrayList<EventDetail>();
         largeDetails.add(createDetailOfSize("foo_append", stringStepSize*30, "largeDetail-",
                 EventDetailMergeBehavior.APPEND));
         largeDetails.add(createDetailOfSize("bar_replace", stringStepSize*30, "largeDetail-",
                 EventDetailMergeBehavior.REPLACE));
         largeDetails.add(createDetailOfSize("baz_unique", stringStepSize*30, "largeDetail-",
                 EventDetailMergeBehavior.UNIQUE));
 
         final Event.Builder largeEventBuilder = Event.newBuilder(originalEvent);
         largeEventBuilder.setCreatedTime(largeEventBuilder.getCreatedTime() + 2);
         largeEventBuilder.clearDetails();
         largeEventBuilder.addAllDetails(largeDetails);
 
         // Add a legitimate Zenoss detail and verify that it's not removed.
         largeEventBuilder.addDetails(EventDetail.newBuilder()
                 .setName(ZepConstants.DETAIL_DEVICE_PRODUCTION_STATE)
                 .addValue("1000"));
 
         final EventSummary largeSummary = createSummaryNew(largeEventBuilder.build());
 
         // Only Zenoss details should remain...
         for (EventDetail detail : largeSummary.getOccurrence(0).getDetailsList()) {
             assertTrue("Detail should be a zenoss detail: " + detail.getName(),
                     detail.getName().startsWith("zenoss."));
         }
 
 
         final Map<String, List<String>> createdDetails = detailsToMap(
                 largeSummary.getOccurrence(0).getDetailsList());
         // ...and verify that our SPECIFIC detail was not somehow removed.
         assertTrue("Created event should contain Zenoss details (production state).",
                 createdDetails.keySet().contains(ZepConstants.DETAIL_DEVICE_PRODUCTION_STATE));
 
 
 
     }
 
     /**
      * Verify that new details and old details get merged correctly if less than
      * max size. This is normal behavior.
      *
      * @throws org.zenoss.zep.ZepException
      */
     @Test
     public void testMaxEventSizeUpdateMerge() throws ZepException {
         final int stringStepSize = getStringStepSize();
         final Event originalEvent = setupEventForDetails(stringStepSize);
 
         // This step size is much smaller because I want to add each detail and
         // make sure it doesn't go over the max size limit.
         final int additionalDetailStepSize = 8;
 
         final List<EventDetail> normalDetails = new ArrayList<EventDetail>();
         normalDetails.add(createDetailOfSize("foo_append", additionalDetailStepSize, "normalDetail-",
                 EventDetailMergeBehavior.APPEND));
         normalDetails.add(createDetailOfSize("bar_replace", additionalDetailStepSize, "normalDetail-",
                 EventDetailMergeBehavior.REPLACE));
         normalDetails.add(createDetailOfSize("baz_unique", additionalDetailStepSize, "normalDetail-",
                 EventDetailMergeBehavior.UNIQUE));
 
         final Event.Builder normalEventBuilder = Event.newBuilder(originalEvent);
         normalEventBuilder.setCreatedTime(normalEventBuilder.getCreatedTime() + 3);
         normalEventBuilder.clearDetails();
         normalEventBuilder.addAllDetails(normalDetails);
         final EventSummary normalSummary = createSummaryNew(normalEventBuilder.build());
 
         Map<String, List<String>> createdDetails = detailsToMap(normalSummary.getOccurrence(0).getDetailsList());
         Map<String, List<String>> mapNormalDetails = detailsToMap(normalDetails);
         Map<String, List<String>> mapOriginalDetails = detailsToMap(originalEvent.getDetailsList());
 
         // assert that normalDetails and originalDetails values for foo_append are present
         assertTrue("Created details should contain normal details 'foo_append' value.",
                 createdDetails.get("foo_append").contains(mapNormalDetails.get("foo_append").get(0)));
         assertTrue("Created details should contain original details 'foo_append' value.",
                 createdDetails.get("foo_append").contains(mapOriginalDetails.get("foo_append").get(0)));
 
         // assert that only normalDetails value for bar_replace is present
         assertTrue("Created details should contain normal details 'bar_replace' value.",
                 createdDetails.get("bar_replace").contains(mapNormalDetails.get("bar_replace").get(0)));
         assertFalse("Created details should NOT contain original details 'bar_replace' value.",
                 createdDetails.get("bar_replace").contains(mapOriginalDetails.get("bar_replace").get(0)));
 
         // assert that normalDetails and originalDetails values for baz_unique are present.
         assertTrue("Created details should contain normal details 'baz_unique' value.",
                 createdDetails.get("baz_unique").contains(mapNormalDetails.get("baz_unique").get(0)));
         assertTrue("Created details should contain original details 'baz_unique' value.",
                 createdDetails.get("baz_unique").contains(mapOriginalDetails.get("baz_unique").get(0)));
     }
 
     
     @Test
     public void testMaxEventSizeUpdateOutOfOrder() throws ZepException {
         final int stringStepSize = getStringStepSize();
         final Event newestEvent = setupEventForDetails(stringStepSize);
 
         final List<EventDetail> oldDetails = new ArrayList<EventDetail>();
         oldDetails.add(createDetailOfSize("foo_append", stringStepSize, "oldDetail-",
                 EventDetailMergeBehavior.APPEND));
         oldDetails.add(createDetailOfSize("bar_replace", stringStepSize, "oldDetail-",
                 EventDetailMergeBehavior.REPLACE));
         oldDetails.add(createDetailOfSize("baz_unique", stringStepSize, "oldDetail-",
                 EventDetailMergeBehavior.UNIQUE));
 
         final Event.Builder oldEventBuilder = Event.newBuilder(newestEvent);
         oldEventBuilder.setCreatedTime(oldEventBuilder.getCreatedTime() - 5000);
         oldEventBuilder.clearDetails();
         oldEventBuilder.addAllDetails(oldDetails);
 
         final EventSummary oldSummary = createSummaryNew(oldEventBuilder.build());
 
         assertEquals("Oldest details should have been dropped.",
                 newestEvent.getDetailsList(), oldSummary.getOccurrence(0).getDetailsList());
     }
 
     @Test
     public void testAgeEligibleEventCount() throws ZepException {
         long initial = eventSummaryDao.getAgeEligibleEventCount(0L, TimeUnit.SECONDS, EventSeverity.SEVERITY_CRITICAL,
                 true);
         createSummaryNew(EventTestUtils.createSampleEvent());
         long actual = eventSummaryDao.getAgeEligibleEventCount(0L, TimeUnit.SECONDS, EventSeverity.SEVERITY_CRITICAL,
                 true);
         assertEquals(initial + 1L, actual);
         
         // Test condition where aging is disabled
         assertEquals(0, eventSummaryDao.getAgeEligibleEventCount(0L, TimeUnit.SECONDS, EventSeverity.SEVERITY_CLEAR,
                 false));
     }
 
     @Test
     public void testArchiveEligibleEventCount() throws ZepException {
         long initial = eventSummaryDao.getArchiveEligibleEventCount(0L, TimeUnit.SECONDS);
         EventSummary summary = createSummaryNew(EventTestUtils.createSampleEvent());
         String userUuid = UUID.randomUUID().toString();
         String userName = "user" + random.nextInt(500);
         eventSummaryDao.close(Collections.singletonList(summary.getUuid()), userUuid, userName);
         long actual = eventSummaryDao.getArchiveEligibleEventCount(0L, TimeUnit.SECONDS);
         assertEquals(initial + 1L, actual);
     }
     
     @Test
     public void testNoExceptionWhenNoClearHashes() throws ZepException {
         Event event = Event.newBuilder(EventTestUtils.createSampleEvent()).setSeverity(EventSeverity.SEVERITY_CLEAR)
                 .build();
         eventSummaryDao.create(event, new EventPreCreateContext() {
             @Override
             public Set<String> getClearClasses() {
                 return Collections.emptySet();
             }
 
             @Override
             public void setClearClasses(Set<String> clearClasses) {
             }
 
             @Override
             public ClearFingerprintGenerator getClearFingerprintGenerator() {
                 return new ClearFingerprintGenerator() {
                     @Override
                     public String generateClearFingerprint(Event event) {
                         return null;
                     }
 
                     @Override
                     public List<String> generateClearFingerprints(Event event, Set<String> clearClasses) {
                         return Collections.emptyList();
                     }
                 };
             }
 
             @Override
             public void setClearFingerprintGenerator(ClearFingerprintGenerator clearFingerprintGenerator) {
             }
         });
     }
 
     @Test
     public void testIncomingEventCount() throws ZepException {
         Event event = Event.newBuilder(EventTestUtils.createSampleEvent()).setCount(500).build();
         String uuid = eventSummaryDao.create(event, new EventPreCreateContextImpl());
         String uuid2 = eventSummaryDao.create(Event.newBuilder(event).setCount(1000).build(),
                 new EventPreCreateContextImpl());
         assertEquals(uuid, uuid2);
 
         EventSummary eventSummary = eventSummaryDao.findByUuid(uuid);
         assertEquals(1500, eventSummary.getCount());
     }
 
     @Test
     public void testIncomingFirstTime() throws ZepException {
         long oneHourAgo = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(60);
         Event event = Event.newBuilder(EventTestUtils.createSampleEvent()).setFirstSeenTime(oneHourAgo).setCount(5)
                 .build();
         String uuid = eventSummaryDao.create(event, new EventPreCreateContextImpl());
         long twoHoursAgo = oneHourAgo - TimeUnit.MINUTES.toMillis(60);
         Event event2 = Event.newBuilder(event).setFirstSeenTime(twoHoursAgo).setCount(5).build();
         String uuid2 = eventSummaryDao.create(event2, new EventPreCreateContextImpl());
 
         assertEquals(uuid, uuid2);
         EventSummary eventSummary = eventSummaryDao.findByUuid(uuid);
         assertEquals(10, eventSummary.getCount());
         assertEquals(twoHoursAgo, eventSummary.getFirstSeenTime());
     }
 
 }
