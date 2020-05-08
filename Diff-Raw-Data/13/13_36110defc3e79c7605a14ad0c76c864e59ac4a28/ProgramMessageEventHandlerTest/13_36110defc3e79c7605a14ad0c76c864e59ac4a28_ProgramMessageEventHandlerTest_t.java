 package org.motechproject.ghana.mtn.eventhandler;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.motechproject.ghana.mtn.domain.Subscription;
 import org.motechproject.ghana.mtn.process.SubscriptionMessenger;
 import org.motechproject.ghana.mtn.repository.AllSubscriptions;
 import org.motechproject.ghana.mtn.service.SubscriptionService;
 import org.motechproject.model.MotechEvent;
 import org.motechproject.server.messagecampaign.EventKeys;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static junit.framework.Assert.assertEquals;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class ProgramMessageEventHandlerTest {
 
     private ProgramMessageEventHandler programMessageEventHandler;
     @Mock
     private SubscriptionMessenger messenger;
     @Mock
     private SubscriptionService service;
 
     @Before
     public void setUp() {
         initMocks(this);
         programMessageEventHandler = new ProgramMessageEventHandler(messenger, service);
     }
 
     @Test
     public void shouldUseSubscriptionMessageSenderAfterPickingRightSubscription() {
         String subscriberNumber = "externalId";
         String programName = "pregnancy";
         Subscription subscription = mock(Subscription.class);
 

         Map params = new HashMap();
         params.put(EventKeys.CAMPAIGN_NAME_KEY, programName);
         params.put(EventKeys.EXTERNAL_ID_KEY, subscriberNumber);
         MotechEvent motechEvent = new MotechEvent(EventKeys.MESSAGE_CAMPAIGN_SEND_EVENT_SUBJECT, params);
 
        when(service.findBy(subscriberNumber, programName)).thenReturn(subscription);
        when(subscription.isCompleted()).thenReturn(true);
 
         programMessageEventHandler.sendMessageReminder(motechEvent);
 
         verify(messenger).process(subscription);
        verify(service).stop(subscription);
     }
 }
