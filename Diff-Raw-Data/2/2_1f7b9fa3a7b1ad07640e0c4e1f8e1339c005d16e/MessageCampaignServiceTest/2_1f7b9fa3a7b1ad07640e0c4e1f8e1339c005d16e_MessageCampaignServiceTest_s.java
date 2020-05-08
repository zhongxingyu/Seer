 package org.motechproject.ananya.kilkari.messagecampaign.service;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ananya.kilkari.messagecampaign.domain.MessageCampaignPack;
 import org.motechproject.ananya.kilkari.messagecampaign.request.MessageCampaignRequest;
 import org.motechproject.model.Time;
 import org.motechproject.server.messagecampaign.contract.CampaignRequest;
 import org.motechproject.server.messagecampaign.domain.campaign.CampaignEnrollmentStatus;
 import org.motechproject.server.messagecampaign.service.CampaignEnrollmentRecord;
 import org.motechproject.server.messagecampaign.service.CampaignEnrollmentsQuery;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class MessageCampaignServiceTest {
 
     private MessageCampaignService messageCampaignService;
 
     @Mock
     private org.motechproject.server.messagecampaign.service.MessageCampaignService platformMessageCampaignService;
 
     @Before
     public void setUp() {
         initMocks(this);
         this.messageCampaignService = new MessageCampaignService(platformMessageCampaignService);
     }
 
     @Test
     public void shouldStartNewCampaignForTheGivenRequest() {
         String externalId = "externalId";
         String subscriptionPack = MessageCampaignPack.CHOTI_KILKARI.getCampaignName();
         DateTime subscriptionStartDate = DateTime.now();
         int campaignScheduleDeltaDays = 2;
         int campaignScheduleDeltaMinutes = 30;
         MessageCampaignRequest messageCampaignRequest = new MessageCampaignRequest(
                 externalId, subscriptionPack, subscriptionStartDate);
 
         this.messageCampaignService.start(messageCampaignRequest, campaignScheduleDeltaDays, campaignScheduleDeltaMinutes);
 
         ArgumentCaptor<CampaignRequest> campaignRequestArgumentCaptor = ArgumentCaptor.forClass(CampaignRequest.class);
         verify(platformMessageCampaignService).startFor(campaignRequestArgumentCaptor.capture());
         CampaignRequest campaignRequest = campaignRequestArgumentCaptor.getValue();
 
         assertEquals(externalId, campaignRequest.externalId());
         assertEquals(subscriptionPack, campaignRequest.campaignName());
        assertEquals(subscriptionStartDate.toLocalDate().plusDays(campaignScheduleDeltaDays), campaignRequest.referenceDate());
         assertEquals(new Time(subscriptionStartDate.toLocalTime().plusMinutes(campaignScheduleDeltaMinutes)), campaignRequest.deliverTime());
     }
 
     @Test
     public void shouldStop() {
         String externalId = "externalId";
         String subscriptionPack = MessageCampaignPack.CHOTI_KILKARI.getCampaignName();
         DateTime subscriptionStartDate = DateTime.now();
         MessageCampaignRequest messageCampaignRequest = new MessageCampaignRequest(
                 externalId, subscriptionPack, subscriptionStartDate);
 
         this.messageCampaignService.stop(messageCampaignRequest);
 
         ArgumentCaptor<CampaignRequest> campaignRequestArgumentCaptor = ArgumentCaptor.forClass(CampaignRequest.class);
         verify(platformMessageCampaignService).stopAll(campaignRequestArgumentCaptor.capture());
         CampaignRequest campaignRequest = campaignRequestArgumentCaptor.getValue();
 
         assertEquals(externalId, campaignRequest.externalId());
         assertEquals(subscriptionPack, campaignRequest.campaignName());
         assertEquals(subscriptionStartDate.toLocalDate(), campaignRequest.referenceDate());
         assertEquals(new Time(subscriptionStartDate.toLocalTime()), campaignRequest.deliverTime());
     }
 
     @Test
     public void shouldGetMessageTimingsForASubscription() {
         DateTime startDate = DateTime.now();
         String subscriptionId = "abcd1234";
         MessageCampaignPack messageCampaignPack = MessageCampaignPack.NANHI_KILKARI;
         DateTime endDate = startDate.plusYears(2);
         Date messageTime = DateTime.now().toDate();
 
         HashMap<String, List<Date>> campaignTimings = new HashMap<>();
         ArrayList<Date> dates = new ArrayList<Date>();
         dates.add(messageTime);
         campaignTimings.put(MessageCampaignService.CAMPAIGN_MESSAGE_NAME, dates);
         when(platformMessageCampaignService.getCampaignTimings(subscriptionId, messageCampaignPack.getCampaignName(),
                 startDate.toDate(), endDate.toDate())).thenReturn(campaignTimings);
 
         ArrayList<CampaignEnrollmentRecord> campaignEnrollmentRecords = new ArrayList<CampaignEnrollmentRecord>();
         campaignEnrollmentRecords.add(new CampaignEnrollmentRecord(subscriptionId, MessageCampaignPack.BARI_KILKARI.getCampaignName(), startDate.toLocalDate(), CampaignEnrollmentStatus.COMPLETED));
         campaignEnrollmentRecords.add(new CampaignEnrollmentRecord(subscriptionId, messageCampaignPack.getCampaignName(), startDate.toLocalDate(), CampaignEnrollmentStatus.ACTIVE));
         when(platformMessageCampaignService.search(any(CampaignEnrollmentsQuery.class))).thenReturn(campaignEnrollmentRecords);
 
         List<DateTime> messageTimings = this.messageCampaignService.getMessageTimings(
                 subscriptionId, startDate, endDate);
 
         verify(platformMessageCampaignService).getCampaignTimings(subscriptionId, messageCampaignPack.getCampaignName(),
                 startDate.toDate(), endDate.toDate());
 
         assertEquals(new DateTime(messageTime), messageTimings.get(0));
     }
 
     @Test
     public void shouldNotGetMessageTimingsForANonActiveSubscription() {
         DateTime startDate = DateTime.now();
         String subscriptionId = "abcd1234";
         DateTime endDate = startDate.plusYears(2);
 
         when(platformMessageCampaignService.search(any(CampaignEnrollmentsQuery.class))).thenReturn(new ArrayList<CampaignEnrollmentRecord>());
 
         List<DateTime> messageTimings = this.messageCampaignService.getMessageTimings(subscriptionId, startDate, endDate);
 
         verify(platformMessageCampaignService, never()).getCampaignTimings(anyString(), anyString(), any(Date.class), any(Date.class));
         assertTrue(messageTimings.isEmpty());
     }
 
     @Test
     public void shouldGetCampaignStartDateForGivenCampaignSubscription() {
         String subscriptionId = "abcd1234";
         String campaignName = "twelve_months";
         DateTime startDate = DateTime.now();
 
         ArrayList<CampaignEnrollmentRecord> campaignEnrollmentRecords = new ArrayList<>();
         campaignEnrollmentRecords.add(new CampaignEnrollmentRecord(null, "fifteen_months", startDate.minusYears(1).toLocalDate(), CampaignEnrollmentStatus.ACTIVE));
         campaignEnrollmentRecords.add(new CampaignEnrollmentRecord(null, campaignName, startDate.toLocalDate(), CampaignEnrollmentStatus.ACTIVE));
         when(platformMessageCampaignService.search(any(CampaignEnrollmentsQuery.class))).thenReturn(campaignEnrollmentRecords);
 
         DateTime campaignStartDate = this.messageCampaignService.getCampaignStartDate(subscriptionId, campaignName);
 
         assertEquals(startDate.toLocalDate(), campaignStartDate.toLocalDate());
     }
 
     @Test
     public void shouldGetActiveCampaignStartDateForGivenSubscriptionId() {
         String subscriptionId = "abcd1234";
         String activeCampaignName = "twelve_months";
         DateTime startDate = DateTime.now();
 
         ArrayList<CampaignEnrollmentRecord> campaignEnrollmentRecords = new ArrayList<>();
         campaignEnrollmentRecords.add(new CampaignEnrollmentRecord(null, "fifteen_months", startDate.minusYears(1).toLocalDate(), CampaignEnrollmentStatus.COMPLETED));
         campaignEnrollmentRecords.add(new CampaignEnrollmentRecord(null, activeCampaignName, startDate.toLocalDate(), CampaignEnrollmentStatus.ACTIVE));
         when(platformMessageCampaignService.search(any(CampaignEnrollmentsQuery.class))).thenReturn(campaignEnrollmentRecords);
 
         DateTime actualCampaignStartDate = this.messageCampaignService.getActiveCampaignStartDate(subscriptionId);
 
         assertEquals(startDate.toLocalDate(), actualCampaignStartDate.toLocalDate());
     }
 
     @Test
     public void shouldReturnActiveCampaignStartDateAsNullForAnInActiveSubscription() {
         String subscriptionId = "abcd1234";
         DateTime startDate = DateTime.now();
 
         ArrayList<CampaignEnrollmentRecord> campaignEnrollmentRecords = new ArrayList<>();
         campaignEnrollmentRecords.add(new CampaignEnrollmentRecord(null, "fifteen_months", startDate.minusYears(1).toLocalDate(), CampaignEnrollmentStatus.COMPLETED));
         when(platformMessageCampaignService.search(any(CampaignEnrollmentsQuery.class))).thenReturn(campaignEnrollmentRecords);
 
         DateTime actualCampaignStartDate = this.messageCampaignService.getActiveCampaignStartDate(subscriptionId);
 
         assertNull(actualCampaignStartDate);
     }
 
     @Test
     public void shouldNotStopACampaignIfCampaignNameDoesNotExist() {
         messageCampaignService.stop(new MessageCampaignRequest("externalId", null, DateTime.now()));
         
         verify(platformMessageCampaignService, never()).stopAll(any(CampaignRequest.class));
     }
 }
