 package org.motechproject.ananya.kilkari.obd.service;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ananya.kilkari.obd.builder.CampaignMessageCSVBuilder;
 import org.motechproject.ananya.kilkari.obd.domain.CampaignMessage;
 import org.motechproject.ananya.kilkari.obd.domain.InvalidCallRecord;
 import org.motechproject.ananya.kilkari.obd.gateway.OnMobileOBDGateway;
 import org.motechproject.ananya.kilkari.obd.repository.AllCampaignMessages;
 import org.motechproject.ananya.kilkari.obd.repository.AllInvalidCallRecords;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class CampaignMessageServiceTest {
 
     private CampaignMessageService campaignMessageService;
 
     @Mock
     private AllCampaignMessages allCampaignMessages;
 
     @Mock
     private OnMobileOBDGateway onMobileOBDGateway;
 
     @Mock
     private CampaignMessageCSVBuilder campaignMessageCSVBuilder;
 
     @Mock
     private AllInvalidCallRecords allInvalidCallRecords;
 
     @Rule
     public ExpectedException expectedException = ExpectedException.none();
 
     @Before
     public void setUp() {
         initMocks(this);
         campaignMessageService = new CampaignMessageService(allCampaignMessages, onMobileOBDGateway, campaignMessageCSVBuilder, allInvalidCallRecords);
     }
 
     @Test
     public void shouldSaveTheCampaignMessageToDB() {
         String subscriptionId = "subscriptionId";
         String messageId = "messageId";
         campaignMessageService.scheduleCampaignMessage(subscriptionId, messageId, "1234567890", null);
         ArgumentCaptor<CampaignMessage> captor = ArgumentCaptor.forClass(CampaignMessage.class);
         verify(allCampaignMessages).add(captor.capture());
 
         CampaignMessage value = captor.getValue();
 
         assertEquals(subscriptionId, value.getSubscriptionId());
         assertEquals(messageId, value.getMessageId());
     }
 
     @Test
     public void sendNewMessagesShouldFetchNewAndDidNotCallMessages() {
         CampaignMessage expectedCampaignMessage1 = new CampaignMessage("subsriptionId1", "messageId1", "1234567890", "operator1");
         CampaignMessage expectedCampaignMessage2 = new CampaignMessage("subsriptionId2", "messageId2", "1234567891", "operator2");
         List<CampaignMessage> campaignMessages = Arrays.asList(expectedCampaignMessage1, expectedCampaignMessage2);
         when(allCampaignMessages.getAllUnsentNewMessages()).thenReturn(campaignMessages);
         String csvContent = "csvContent";
         when(campaignMessageCSVBuilder.getCSV(campaignMessages)).thenReturn(csvContent);
         campaignMessageService.sendNewMessages();
 
         verify(allCampaignMessages).getAllUnsentNewMessages();
         verify(onMobileOBDGateway).sendNewMessages(csvContent);
 
         ArgumentCaptor<CampaignMessage> captor = ArgumentCaptor.forClass(CampaignMessage.class);
         verify(allCampaignMessages, times(2)).update(captor.capture());
 
         List<CampaignMessage> actualCampaignMessages = captor.getAllValues();
         assertEquals(2, actualCampaignMessages.size());
         CampaignMessage actualCampaignMessage1 = actualCampaignMessages.get(0);
         CampaignMessage actualCampaignMessage2 = actualCampaignMessages.get(1);
 
         assertEquals(expectedCampaignMessage1.getSubscriptionId(), actualCampaignMessage1.getSubscriptionId());
         assertEquals(expectedCampaignMessage1.getMessageId(), actualCampaignMessage1.getMessageId());
         assertTrue(actualCampaignMessage1.isSent());
 
         assertEquals(expectedCampaignMessage2.getSubscriptionId(), actualCampaignMessage2.getSubscriptionId());
         assertEquals(expectedCampaignMessage2.getMessageId(), actualCampaignMessage2.getMessageId());
         assertTrue(actualCampaignMessage2.isSent());
     }
 
     @Test
     public void shouldNotSaveNewCampaignMessagesAsSentIfSendingFailed() {
         expectedException.expect(RuntimeException.class);
         expectedException.expectMessage("myruntimeexception");
 
         CampaignMessage expectedCampaignMessage1 = new CampaignMessage("subsriptionId1", "messageId1", "1234567890", "operator1");
         CampaignMessage expectedCampaignMessage2 = new CampaignMessage("subsriptionId2", "messageId2", "1234567891", "operator2");
         List<CampaignMessage> campaignMessages = Arrays.asList(expectedCampaignMessage1, expectedCampaignMessage2);
         when(allCampaignMessages.getAllUnsentNewMessages()).thenReturn(campaignMessages);
 
         doThrow(new RuntimeException("myruntimeexception")).when(onMobileOBDGateway).sendNewMessages(anyString());
 
         try {
             campaignMessageService.sendNewMessages();
         } finally {
             verify(allCampaignMessages, never()).update(any(CampaignMessage.class));
         }
     }
 
     @Test
     public void sendRetryMessagesShouldFetchRetryMessages() {
         CampaignMessage expectedCampaignMessage1 = new CampaignMessage("subsriptionId1", "messageId1", "1234567890", "operator1");
         CampaignMessage expectedCampaignMessage2 = new CampaignMessage("subsriptionId2", "messageId2", "1234567891", "operator2");
         List<CampaignMessage> campaignMessages = Arrays.asList(expectedCampaignMessage1, expectedCampaignMessage2);
         when(allCampaignMessages.getAllUnsentRetryMessages()).thenReturn(campaignMessages);
         String csvContent = "csvContent";
         when(campaignMessageCSVBuilder.getCSV(campaignMessages)).thenReturn(csvContent);
 
 
         campaignMessageService.sendRetryMessages();
 
         verify(allCampaignMessages).getAllUnsentRetryMessages();
         ArgumentCaptor<CampaignMessage> captor = ArgumentCaptor.forClass(CampaignMessage.class);
         verify(allCampaignMessages, times(2)).update(captor.capture());
 
        verify(onMobileOBDGateway).sendNewMessages(csvContent);
 
         List<CampaignMessage> actualCampaignMessages = captor.getAllValues();
         assertEquals(2, actualCampaignMessages.size());
         CampaignMessage actualCampaignMessage1 = actualCampaignMessages.get(0);
         CampaignMessage actualCampaignMessage2 = actualCampaignMessages.get(1);
 
         assertEquals(expectedCampaignMessage1.getSubscriptionId(), actualCampaignMessage1.getSubscriptionId());
         assertEquals(expectedCampaignMessage1.getMessageId(), actualCampaignMessage1.getMessageId());
         assertTrue(actualCampaignMessage1.isSent());
 
         assertEquals(expectedCampaignMessage2.getSubscriptionId(), actualCampaignMessage2.getSubscriptionId());
         assertEquals(expectedCampaignMessage2.getMessageId(), actualCampaignMessage2.getMessageId());
         assertTrue(actualCampaignMessage2.isSent());
     }
 
 
     @Test
     public void shouldNotSaveRetryCampaignMessagesAsSentIfSendingFailed() {
         expectedException.expect(RuntimeException.class);
         expectedException.expectMessage("myruntimeexception");
 
         CampaignMessage expectedCampaignMessage1 = new CampaignMessage("subsriptionId1", "messageId1", "1234567890", "operator1");
         CampaignMessage expectedCampaignMessage2 = new CampaignMessage("subsriptionId2", "messageId2", "1234567891", "operator2");
         List<CampaignMessage> campaignMessages = Arrays.asList(expectedCampaignMessage1, expectedCampaignMessage2);
         when(allCampaignMessages.getAllUnsentRetryMessages()).thenReturn(campaignMessages);
 
        doThrow(new RuntimeException("myruntimeexception")).when(onMobileOBDGateway).sendNewMessages(anyString());
 
         try {
             campaignMessageService.sendRetryMessages();
         } finally {
             verify(allCampaignMessages, never()).update(any(CampaignMessage.class));
         }
     }
 
     @Test
     public void shouldNotSendNewMessagesIfNoneExist() {
         when(allCampaignMessages.getAllUnsentNewMessages()).thenReturn(new ArrayList<CampaignMessage>());
 
         campaignMessageService.sendNewMessages();
 
         verify(allCampaignMessages, never()).update(any(CampaignMessage.class));
         verifyZeroInteractions(onMobileOBDGateway);
     }
 
     @Test
     public void shouldNotSendRetryMessagesIfNoneExist() {
         when(allCampaignMessages.getAllUnsentRetryMessages()).thenReturn(new ArrayList<CampaignMessage>());
 
         campaignMessageService.sendRetryMessages();
 
         verify(allCampaignMessages, never()).update(any(CampaignMessage.class));
         verifyZeroInteractions(onMobileOBDGateway);
     }
 
     @Test
     public void shouldDeleteTheCampaignMessageOnlyIfItExists() {
         String subscriptionId = "subscriptionId";
         String campaignId = "campaignId";
 
         when(allCampaignMessages.find(subscriptionId, campaignId)).thenReturn(new CampaignMessage(subscriptionId, campaignId, "1234567890", null));
 
         campaignMessageService.deleteCampaignMessage(subscriptionId, campaignId);
 
         ArgumentCaptor<CampaignMessage> campaignMessageArgumentCaptor = ArgumentCaptor.forClass(CampaignMessage.class);
         verify(allCampaignMessages).delete(campaignMessageArgumentCaptor.capture());
         CampaignMessage campaignMessage = campaignMessageArgumentCaptor.getValue();
 
         assertEquals(subscriptionId, campaignMessage.getSubscriptionId());
         assertEquals(campaignId, campaignMessage.getMessageId());
     }
 
     @Test
     public void shouldNotDeleteTheCampaignMessageIfItDoesNotExists() {
         String subscriptionId = "subscriptionId";
         String campaignId = "campaignId";
 
         when(allCampaignMessages.find(subscriptionId, campaignId)).thenReturn(null);
 
         campaignMessageService.deleteCampaignMessage(subscriptionId, campaignId);
 
         verify(allCampaignMessages, never()).delete(any(CampaignMessage.class));
     }
     
     @Test
     public void shouldSaveInvalidCallRecords() {
         ArrayList<InvalidCallRecord> invalidCallRecords = new ArrayList<>();
         InvalidCallRecord invalidCallRecord1 = new InvalidCallRecord("msisdn1", "subscription1", "campaign1", "operator1", "description1");
         InvalidCallRecord invalidCallRecord2 = new InvalidCallRecord("msisdn2", "subscription2", "campaign2", "operator2", "description2");
         invalidCallRecords.add(invalidCallRecord1);
         invalidCallRecords.add(invalidCallRecord2);
 
         campaignMessageService.processInvalidCallRecords(invalidCallRecords);
 
         verify(allInvalidCallRecords).add(invalidCallRecord1);
         verify(allInvalidCallRecords).add(invalidCallRecord2);
     }
 }
