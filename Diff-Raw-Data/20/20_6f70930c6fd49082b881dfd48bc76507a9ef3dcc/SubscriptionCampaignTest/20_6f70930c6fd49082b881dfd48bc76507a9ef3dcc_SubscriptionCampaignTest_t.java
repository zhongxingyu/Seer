 package org.motechproject.ghana.mtn.process;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ghana.mtn.domain.MessageBundle;
 import org.motechproject.ghana.mtn.domain.Subscription;
 import org.motechproject.ghana.mtn.domain.dto.SMSServiceRequest;
 import org.motechproject.ghana.mtn.service.SMSService;
 import org.motechproject.server.messagecampaign.contract.CampaignRequest;
 import org.motechproject.server.messagecampaign.service.MessageCampaignService;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class SubscriptionCampaignTest {
     private SubscriptionCampaign campaign;
     @Mock
     private SMSService smsService;
     @Mock
     private MessageBundle messageBundle;
     @Mock
     private MessageCampaignService campaignService;
 
     @Before
     public void setUp() {
         initMocks(this);
         campaign = new SubscriptionCampaign(smsService, messageBundle, campaignService);
     }
 
     @Test
     public void shouldAskSubscriptionForCampaignRequestAndSendToCampaignServiceForStart() {
         String message = "indie";
         Subscription subscription = mock(Subscription.class);
         CampaignRequest campaignRequest = new CampaignRequest();
         when(subscription.createCampaignRequest()).thenReturn(campaignRequest);
         when(messageBundle.get(MessageBundle.ENROLLMENT_SUCCESS)).thenReturn(message);
 
         Boolean reply = campaign.startFor(subscription);
         assertTrue(reply);
         verify(campaignService).startFor(campaignRequest);
         assertSMS(message);
     }
 
     @Test
     public void shouldAskSubscriptionForCampaignRequestAndSendToCampaignServiceForStop() {
         String message = "pop";
         Subscription subscription = mock(Subscription.class);
         CampaignRequest campaignRequest = new CampaignRequest();
         when(subscription.createCampaignRequest()).thenReturn(campaignRequest);
         when(messageBundle.get(MessageBundle.ENROLLMENT_STOPPED)).thenReturn(message);
 
         Boolean reply = campaign.stopExpired(subscription);
         assertTrue(reply);
         verify(campaignService).stopFor(campaignRequest);
         assertSMS(message);
     }
 
     @Test
     public void shouldAskSubscriptionForCampaignRequestAndSendToCampaignServiceForStopByUser() {
         String message = "pop";
         Subscription subscription = mock(Subscription.class);
         CampaignRequest campaignRequest = new CampaignRequest();
         when(subscription.createCampaignRequest()).thenReturn(campaignRequest);
        when(messageBundle.get(MessageBundle.STOP_PROGRAM_SUCCESS)).thenReturn(message);
 
         Boolean reply = campaign.stopByUser(subscription);
         assertTrue(reply);
         verify(campaignService).stopFor(campaignRequest);
         assertSMS(message);
     }
 
     @Test
     public void shouldRollOverUsingCampaignService() {
         String message = "rock & roll";
         Subscription source = mock(Subscription.class);
         Subscription target = mock(Subscription.class);
         CampaignRequest sourceRequest = mock(CampaignRequest.class);
         CampaignRequest targetRequest = mock(CampaignRequest.class);
 
         when(messageBundle.get(MessageBundle.ENROLLMENT_ROLlOVER)).thenReturn(message);
         when(source.createCampaignRequest()).thenReturn(sourceRequest);
         when(target.createCampaignRequest()).thenReturn(targetRequest);
 
         Boolean reply = campaign.rollOver(source, target);
 
         assertTrue(reply);
         verify(campaignService).stopFor(sourceRequest);
         verify(campaignService).startFor(targetRequest);
         assertSMS(message);
     }
 
     private void assertSMS(String message) {
         ArgumentCaptor<SMSServiceRequest> captor = ArgumentCaptor.forClass(SMSServiceRequest.class);
         verify(smsService).send(captor.capture());
         SMSServiceRequest captured = captor.getValue();
         assertEquals(message, captured.getMessage());
     }
 }
 
