 package org.motechproject.ananya.kilkari.obd.scheduler;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ananya.kilkari.obd.service.CampaignMessageService;
 import org.motechproject.ananya.kilkari.obd.service.OBDProperties;
import org.motechproject.event.MotechEvent;
 import org.motechproject.retry.domain.RetryRequest;
 import org.motechproject.retry.service.RetryService;
 import org.motechproject.scheduler.domain.CronSchedulableJob;
 
 import java.util.HashMap;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class RetryMessagesSenderJobTest {
     @Mock
     private OBDProperties obdProperties;
     @Mock
     private CampaignMessageService campaignMessageService;
     @Mock
     private RetryService retryService;
     private RetryMessagesSenderJob retryMessagesSenderJob;
     private String cronJobExpression = "mycronjobexpression";
 
     @Before
     public void setUp() {
         initMocks(this);
        DateTime startTime = DateTime.now().minusMinutes(1);
        DateTime endTime = DateTime.now().plusMinutes(1);
 
         when(obdProperties.getSecondSlotStartTimeHour()).thenReturn(startTime.getHourOfDay());
         when(obdProperties.getSecondSlotStartTimeMinute()).thenReturn(startTime.getMinuteOfHour());
         when(obdProperties.getSecondSlotEndTimeHour()).thenReturn(endTime.getHourOfDay());
         when(obdProperties.getSecondSlotEndTimeMinute()).thenReturn(endTime.getMinuteOfHour());
         when(obdProperties.getRetryMessageJobCronExpression()).thenReturn(cronJobExpression);
 
         retryMessagesSenderJob = new RetryMessagesSenderJob(campaignMessageService, retryService, obdProperties);
     }
 
     @Test
     public void shouldScheduleCronJobsAtConstruction() {
         CronSchedulableJob cronSchedulableJob = retryMessagesSenderJob.getCronJob();
 
         assertEquals(cronJobExpression, cronSchedulableJob.getCronExpression());
         assertNull(cronSchedulableJob.getEndTime());
 
         DateTime startDateTime = new DateTime(cronSchedulableJob.getStartTime());
         assertFalse(startDateTime.isAfter(DateTime.now()));
 
         MotechEvent motechEvent = cronSchedulableJob.getMotechEvent();
         assertEquals("obd.send.retry.messages", motechEvent.getSubject());
     }
 
     @Test
     public void shouldInvokeCampaignMessageServiceToSendRetryMessagesToOBD() {
         DateTime before = DateTime.now();
 
         retryMessagesSenderJob.sendMessages(new MotechEvent(""));
 
         DateTime after = DateTime.now();
 
         ArgumentCaptor<RetryRequest> captor = ArgumentCaptor.forClass(RetryRequest.class);
         verify(retryService).schedule(captor.capture());
         RetryRequest retryRequest = captor.getValue();
         assertEquals("obd-send-retry-messages",retryRequest.getName() );
         assertNotNull(retryRequest.getExternalId());
         DateTime referenceTime = retryRequest.getReferenceTime();
         assertTrue(after.isEqual(referenceTime) || after.isAfter(referenceTime));
         assertTrue(before.isEqual(referenceTime) || before.isBefore(referenceTime));
     }
 
     @Test
     public void shouldInvokeCampaignMessageServiceToSendRetryMessagesWithRetryAndFulfillTheRetryIfSuccessful() {
         retryMessagesSenderJob.sendMessagesWithRetry(new MotechEvent("some subject", new HashMap<String, Object>() {{
             put("EXTERNAL_ID", "myExternalId");
         }}));
 
         verify(campaignMessageService).sendRetryMessages();
 
         verify(retryService).fulfill("myExternalId", "obd-send-retry-messages-group");
     }
 
     @Test
     public void shouldInvokeCampaignMessageServiceToSendRetryMessagesWithRetryAndNotFulfillTheRetryIfNotSuccessful() {
         doThrow(new RuntimeException("some exception")).when(campaignMessageService).sendRetryMessages();
 
         retryMessagesSenderJob.sendMessagesWithRetry(new MotechEvent("some subject", new HashMap<String, Object>() {{
             put("EXTERNAL_ID", "myExternalId");
         }}));
 
         verifyZeroInteractions(retryService);
     }
 
     @Test
     public void shouldNotScheduleRetryJobIfTimeIsNotWithinTheRetryMessagesSlot() {
         DateTime startTime = DateTime.now().plusHours(1);
         DateTime endTime = DateTime.now().plusHours(3);
 
         when(obdProperties.getSecondSlotStartTimeHour()).thenReturn(startTime.getHourOfDay());
         when(obdProperties.getSecondSlotStartTimeMinute()).thenReturn(startTime.getMinuteOfHour());
         when(obdProperties.getSecondSlotEndTimeHour()).thenReturn(endTime.getHourOfDay());
         when(obdProperties.getSecondSlotEndTimeMinute()).thenReturn(endTime.getMinuteOfHour());
         retryMessagesSenderJob = new RetryMessagesSenderJob(campaignMessageService, retryService, obdProperties);
 
         retryMessagesSenderJob.sendMessages(new MotechEvent("subject"));
 
         verify(retryService, never()).schedule(any(RetryRequest.class));
     }
 }
