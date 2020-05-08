 package org.motechproject.ananya.kilkari.messagecampaign.service;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ananya.kilkari.messagecampaign.request.KilkariMessageCampaignRequest;
 import org.motechproject.model.Time;
 import org.motechproject.server.messagecampaign.contract.CampaignRequest;
 import org.motechproject.server.messagecampaign.service.MessageCampaignService;
 
 import static junit.framework.Assert.assertEquals;
 import static org.mockito.Mockito.verify;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class KilkariMessageCampaignServiceTest {
 
     private KilkariMessageCampaignService kilkariMessageCampaignService;
 
     @Mock
     private MessageCampaignService messageCampaignService;
 
     @Before
     public void setUp() {
         initMocks(this);
         kilkariMessageCampaignService = new KilkariMessageCampaignService(messageCampaignService);
     }
 
     @Test
     public void shouldStartNewCampaignForTheGivenRequest() {
         KilkariMessageCampaignRequest kilkariMessageCampaignRequest = new KilkariMessageCampaignRequest("externalId", "campaignName", DateTime.now(), DateTime.now());
 
         kilkariMessageCampaignService.start(kilkariMessageCampaignRequest);
 
         ArgumentCaptor<CampaignRequest> campaignRequestArgumentCaptor = ArgumentCaptor.forClass(CampaignRequest.class);
         verify(messageCampaignService).startFor(campaignRequestArgumentCaptor.capture());
         CampaignRequest campaignRequest = campaignRequestArgumentCaptor.getValue();
         assertRequestParameters(kilkariMessageCampaignRequest, campaignRequest);
     }
 
     @Test
     public void shouldStop() {
         KilkariMessageCampaignRequest kilkariMessageCampaignRequest = new KilkariMessageCampaignRequest("externalId", "campaignName", DateTime.now(), DateTime.now());
 
         kilkariMessageCampaignService.stop(kilkariMessageCampaignRequest);
 
         ArgumentCaptor<CampaignRequest> campaignRequestArgumentCaptor = ArgumentCaptor.forClass(CampaignRequest.class);
         verify(messageCampaignService).stopAll(campaignRequestArgumentCaptor.capture());
         CampaignRequest campaignRequest = campaignRequestArgumentCaptor.getValue();
         assertRequestParameters(kilkariMessageCampaignRequest, campaignRequest);
     }
 
     @Test
     public void shouldGetMessageTimings() {
         String subscriptionId = "subscriptionId";
         String campaignName = "campaignName";
 
         kilkariMessageCampaignService.getMessageTimings(subscriptionId, campaignName);
 
        // TODO: introduce back after platform fix
        //verify(messageCampaignService).getMessageTimings(subscriptionId, campaignName);
     }
 
     private void assertRequestParameters(KilkariMessageCampaignRequest kilkariMessageCampaignRequest, CampaignRequest campaignRequest) {
         DateTime reminderTime = kilkariMessageCampaignRequest.getReminderTime();
         DateTime referenceDate = kilkariMessageCampaignRequest.getReferenceDate();
 
         assertEquals(kilkariMessageCampaignRequest.getExternalId(), campaignRequest.externalId());
         assertEquals(kilkariMessageCampaignRequest.getCampaignName(), campaignRequest.campaignName());
         assertEquals(new Time(reminderTime.getHourOfDay(), reminderTime.getMinuteOfHour()), campaignRequest.reminderTime());
         assertEquals(new LocalDate(referenceDate.getYear(), referenceDate.getMonthOfYear(), referenceDate.getDayOfMonth()), campaignRequest.referenceDate());
     }
 }
