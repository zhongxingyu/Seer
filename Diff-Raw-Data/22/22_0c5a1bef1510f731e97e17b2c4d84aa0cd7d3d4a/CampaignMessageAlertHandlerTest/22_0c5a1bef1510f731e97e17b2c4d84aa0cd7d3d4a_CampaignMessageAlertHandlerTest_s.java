 package org.motechproject.ananya.kilkari.handlers;
 
import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ananya.kilkari.service.KilkariCampaignService;
import org.motechproject.ananya.reports.kilkari.contract.request.CampaignScheduleAlertRequest;
 import org.motechproject.event.MotechEvent;
 import org.motechproject.server.messagecampaign.EventKeys;
 
 import java.util.HashMap;
 import java.util.Map;
 
import static junit.framework.Assert.assertEquals;
 import static org.mockito.Mockito.verify;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class CampaignMessageAlertHandlerTest {
 
     @Mock
     private KilkariCampaignService kilkariCampaignService;
 
     private CampaignMessageAlertHandler campaignMessageAlertHandler;
 
     @Before
     public void setUp() {
         initMocks(this);
         campaignMessageAlertHandler = new CampaignMessageAlertHandler(kilkariCampaignService);
     }
 
     @Test
     public void shouldInvokeCampaignServiceAndReportWhenMilestoneAlertIsRaised() {
         Map<String, Object> parameters = new HashMap<>();
         String externalId = "myexternalid";
         String campaignName = "mypack";
         parameters.put(EventKeys.EXTERNAL_ID_KEY, externalId);
         parameters.put(EventKeys.MESSAGE_KEY, "mymessagekey");
         parameters.put(EventKeys.CAMPAIGN_NAME_KEY, campaignName);
         parameters.put(EventKeys.MESSAGE_NAME_KEY, "mymessagenamekey");
         MotechEvent motechEvent = new MotechEvent(EventKeys.SEND_MESSAGE, parameters);
 
         campaignMessageAlertHandler.handleAlertEvent(motechEvent);
 
         verify(kilkariCampaignService).scheduleWeeklyMessage(externalId, campaignName);
        ArgumentCaptor<CampaignScheduleAlertRequest> captor = ArgumentCaptor.forClass(CampaignScheduleAlertRequest.class);
        CampaignScheduleAlertRequest campaignScheduleAlertRequest = captor.getValue();
        assertEquals(externalId, campaignScheduleAlertRequest.getSubscriptionId());
        assertEquals(campaignName, campaignScheduleAlertRequest.getCampaignName());
        assertEquals(DateTime.now().getMinuteOfDay(),campaignScheduleAlertRequest.getScheduledAt().getMinuteOfDay());
     }
 
     @Test
     public void shouldCompleteSubscriptionWhileHandlingCampaignCompletedEvent(){
         Map<String, Object> parameters = new HashMap<>();
         String subscriptionId = "subscriptionId";
         parameters.put(EventKeys.EXTERNAL_ID_KEY, subscriptionId);
         MotechEvent motechEvent = new MotechEvent(EventKeys.CAMPAIGN_COMPLETED, parameters);
 
         campaignMessageAlertHandler.handleCompletionEvent(motechEvent);
 
         verify(kilkariCampaignService).processCampaignCompletion(subscriptionId);
     }
 }
