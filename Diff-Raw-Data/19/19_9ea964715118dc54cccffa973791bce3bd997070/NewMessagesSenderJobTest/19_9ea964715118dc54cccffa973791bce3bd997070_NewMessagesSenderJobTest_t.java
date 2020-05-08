 package org.motechproject.ananya.kilkari.obd.scheduler;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ananya.kilkari.obd.service.CampaignMessageService;
 import org.motechproject.ananya.kilkari.obd.service.OBDProperties;
 import org.motechproject.retry.domain.RetryRequest;
 import org.motechproject.retry.service.RetryService;
 import org.motechproject.scheduler.domain.CronSchedulableJob;
 import org.motechproject.event.MotechEvent;
 
 import java.util.HashMap;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class NewMessagesSenderJobTest {
     @Mock
     private OBDProperties obdProperties;
 
     @Mock
     private RetryService retryService;
 
     @Mock
     private CampaignMessageService campaignMessageService;
     private NewMessagesSenderJob newMessagesSenderJob;
     private String cronJobExpression = "mycronjobexpression";
 
     @Before
     public void setUp() {
         initMocks(this);
        DateTime startTime = DateTime.now().minusMinutes(1);
        DateTime endTime = DateTime.now().plusMinutes(1);
 
         when(obdProperties.getFirstSlotStartTimeHour()).thenReturn(startTime.getHourOfDay());
         when(obdProperties.getFirstSlotStartTimeMinute()).thenReturn(startTime.getMinuteOfHour());
         when(obdProperties.getFirstSlotEndTimeHour()).thenReturn(endTime.getHourOfDay());
         when(obdProperties.getFirstSlotEndTimeMinute()).thenReturn(endTime.getMinuteOfHour());
         when(obdProperties.getNewMessageJobCronExpression()).thenReturn(cronJobExpression);
 
         newMessagesSenderJob = new NewMessagesSenderJob(campaignMessageService, retryService, obdProperties);
     }
 
     @Test
     public void shouldScheduleCronJobsAtConstruction() {
         CronSchedulableJob cronSchedulableJob = newMessagesSenderJob.getCronJob();
 
         assertEquals(cronJobExpression, cronSchedulableJob.getCronExpression());
         assertNull(cronSchedulableJob.getEndTime());
 
         DateTime startDateTime = new DateTime(cronSchedulableJob.getStartTime());
         assertFalse(startDateTime.isAfter(DateTime.now()));
 
         MotechEvent motechEvent = cronSchedulableJob.getMotechEvent();
         assertEquals("obd.send.new.messages", motechEvent.getSubject());
     }
 
     @Test
     public void shouldInvokeCampaignMessageServiceToSendNewMessagesToOBD() {
         DateTime before = DateTime.now();
 
         newMessagesSenderJob.sendMessages(new MotechEvent(""));
 
         DateTime after = DateTime.now();
         ArgumentCaptor<RetryRequest> captor = ArgumentCaptor.forClass(RetryRequest.class);
         verify(retryService).schedule(captor.capture());
         RetryRequest retryRequest = captor.getValue();
         assertEquals("obd-send-new-messages",retryRequest.getName() );
         assertNotNull(retryRequest.getExternalId());
         DateTime referenceTime = retryRequest.getReferenceTime();
         assertTrue(after.isEqual(referenceTime) || after.isAfter(referenceTime));
         assertTrue(before.isEqual(referenceTime) || before.isBefore(referenceTime));
     }
 
     @Test
     public void shouldNotScheduleRetryJobIfTimeIsNotWithinTheNewMessagesSlot() {
         DateTime startTime = DateTime.now().plusHours(1);
         DateTime endTime = DateTime.now().plusHours(3);
 
         when(obdProperties.getFirstSlotStartTimeHour()).thenReturn(startTime.getHourOfDay());
         when(obdProperties.getFirstSlotStartTimeMinute()).thenReturn(startTime.getMinuteOfHour());
         when(obdProperties.getFirstSlotEndTimeHour()).thenReturn(endTime.getHourOfDay());
         when(obdProperties.getFirstSlotEndTimeMinute()).thenReturn(endTime.getMinuteOfHour());
         when(obdProperties.getNewMessageJobCronExpression()).thenReturn(cronJobExpression);
 
         newMessagesSenderJob = new NewMessagesSenderJob(campaignMessageService, retryService, obdProperties);
 
         newMessagesSenderJob.sendMessages(new MotechEvent("subject"));
 
         verify(retryService, never()).schedule(any(RetryRequest.class));
     }
 
     @Test
     public void shouldInvokeCampaignMessageServiceToSendNewMessagesWithRetryAndFulfillTheRetryIfSuccessful() {
         newMessagesSenderJob.sendMessagesWithRetry(new MotechEvent("some subject", new HashMap<String, Object>(){{
             put("EXTERNAL_ID", "myExternalId");
         }}));
 
         verify(campaignMessageService).sendNewMessages();
 
         verify(retryService).fulfill("myExternalId", "obd-send-new-messages-group");
     }
 
     @Test
     public void shouldInvokeCampaignMessageServiceToSendNewMessagesWithRetryAndNotFulfillTheRetryIfNotSuccessful() {
         doThrow(new RuntimeException("some exception")).when(campaignMessageService).sendNewMessages();
 
         newMessagesSenderJob.sendMessagesWithRetry(new MotechEvent("some subject", new HashMap<String, Object>(){{
             put("EXTERNAL_ID", "myExternalId");
         }}));
 
 
         verifyZeroInteractions(retryService);
     }
 }
