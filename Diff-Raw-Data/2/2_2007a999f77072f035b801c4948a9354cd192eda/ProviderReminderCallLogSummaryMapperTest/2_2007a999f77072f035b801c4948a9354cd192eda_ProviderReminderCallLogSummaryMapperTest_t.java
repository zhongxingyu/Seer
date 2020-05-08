 package org.motechproject.whp.reports.webservice.mapper;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.motechproject.util.DateUtil;
 import org.motechproject.whp.reports.contract.enums.ReminderDisconnectionType;
 import org.motechproject.whp.reports.contract.enums.ReminderType;
 import org.motechproject.whp.reports.contract.enums.YesNo;
 import org.motechproject.whp.reports.domain.measure.ProviderReminderCallLog;
 import org.motechproject.whp.reports.webservice.model.ProviderReminderCallLogSummary;
 
 import java.sql.Timestamp;
 import java.util.List;
 
 import static java.util.Arrays.asList;
 import static junit.framework.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.motechproject.model.DayOfWeek.getDayOfWeek;
 
 public class ProviderReminderCallLogSummaryMapperTest {
 
     private final ProviderReminderCallLogSummaryMapper providerReminderCallLogSummaryMapper = new ProviderReminderCallLogSummaryMapper();
 
     @Test
     public void shouldMapProviderReminderCallLogToSummary() {
 
         ProviderReminderCallLog callLog = new ProviderReminderCallLog();
         DateTime now = DateUtil.now();
         String providerId = "providerId";
         String callId = "callId";
         String disconnectionType = ReminderDisconnectionType.CALL_COMPLETE.name();
         String callAnswered = YesNo.Yes.name();
         String reminderType = ReminderType.ADHERENCE_NOT_REPORTED.name();
         Integer callAttempt = 2;
         Integer callDuration = 25;
         Timestamp attemptTime = new Timestamp(now.minusSeconds(callDuration).toDate().getTime());
         Timestamp startTime = new Timestamp(now.minusSeconds(callDuration).toDate().getTime());
         Timestamp endTime = new Timestamp(now.toDate().getTime());
 
         callLog.setCallId(callId);
         callLog.setRequestId("requestId");
         callLog.setDisconnectionType(disconnectionType);
         callLog.setAttempt(callAttempt);
         callLog.setEndTime(endTime);
         callLog.setStartTime(startTime);
         callLog.setAttemptTime(attemptTime);
         callLog.setMobileNumber("1234567890");
         callLog.setReminderType(reminderType);
         callLog.setDisconnectionType(disconnectionType);
         callLog.setProviderId(providerId);
         callLog.setCallAnswered(callAnswered);
         callLog.setAdherenceReported(true);
 
         List<ProviderReminderCallLogSummary> summaryList = providerReminderCallLogSummaryMapper.map(asList(callLog));
 
         assertEquals(1, summaryList.size());
         ProviderReminderCallLogSummary summary = summaryList.get(0);
         assertEquals(callId, summary.getCallId());
         assertEquals(providerId, summary.getProviderId());
         assertEquals(disconnectionType, summary.getDisconnectionType());
         assertEquals(callAnswered, summary.getCallAnswered());
         assertEquals("Yes", summary.getAdherenceReported());
         assertEquals(getDayOfWeek(now.getDayOfWeek()).name(), summary.getReminderDay());
         assertEquals(reminderType, summary.getReminderType());
         assertEquals(callAttempt, summary.getAttempt());
         assertEquals(attemptTime, summary.getAttemptTime());
         assertEquals(callDuration, summary.getDuration());
         assertEquals(startTime, summary.getStartTime());
     }
 
     @Test
     public void shouldMapNullValues() {
         ProviderReminderCallLog callLogWithNullValues = new ProviderReminderCallLog();
         List<ProviderReminderCallLogSummary> summaries = providerReminderCallLogSummaryMapper.map(asList(callLogWithNullValues));
 
         ProviderReminderCallLogSummary summary = summaries.get(0);
         assertNull(summary.getCallId());
         assertNull(summary.getProviderId());
         assertNull(summary.getDisconnectionType());
         assertNull(summary.getCallAnswered());
        assertEquals("No", summary.getAdherenceReported());
         assertNull(summary.getReminderDay());
         assertNull(summary.getReminderType());
         assertNull(summary.getAttempt());
         assertNull(summary.getAttemptTime());
         assertNull(summary.getDuration());
         assertNull(summary.getStartTime());
     }
 }
