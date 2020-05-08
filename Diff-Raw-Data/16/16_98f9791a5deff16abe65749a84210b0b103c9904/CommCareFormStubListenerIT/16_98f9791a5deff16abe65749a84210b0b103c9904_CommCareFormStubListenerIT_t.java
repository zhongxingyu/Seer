 package org.worldvision.sierraleone.listener;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ObjectNode;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.motechproject.commcare.events.constants.EventDataKeys;
 import org.motechproject.commcare.events.constants.EventSubjects;
 import org.motechproject.commcare.service.impl.CommcareCaseServiceImpl;
 import org.motechproject.commcare.service.impl.CommcareFormServiceImpl;
 import org.motechproject.commcare.util.CommCareAPIHttpClient;
 import org.motechproject.event.MotechEvent;
 import org.motechproject.event.listener.EventListener;
import org.motechproject.event.listener.impl.EventListenerRegistry;
 import org.motechproject.event.listener.EventRelay;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.worldvision.sierraleone.constants.Commcare;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import static org.worldvision.sierraleone.constants.EventKeys.MOTHER_REFERRAL_SUBJECT;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {
         "classpath*:/META-INF/motech/applicationCommcareAPI.xml",
         "classpath*:/META-INF/motech/activemqConnection.xml",
         "classpath*:/META-INF/motech/eventQueuePublisher.xml",
         "classpath*:/META-INF/motech/eventQueueConsumer.xml",
         "classpath*:/META-INF/motech/applicationPlatformConfig.xml",
         "classpath*:/META-INF/motech/applicationCommonsCouchdbContext.xml"
 })
 public class CommCareFormStubListenerIT {
     private static final int HALF_MINUTE = 30 * 1000;
 
     private CommCareAPIHttpClient commCareAPIHttpClient;
 
     @Autowired
     private EventRelay eventRelay;
 
     @Autowired
     private EventListenerRegistry eventListenerRegistry;
 
     private CommCareFormStubListener listener;
 
     private ObjectMapper mapper;
 
     MotherReferralListener motherReferralListener;
 
     final Object lock = new Object();
 
     @Before
     public void setUp() throws Exception {
         commCareAPIHttpClient = mock(CommCareAPIHttpClient.class);
 
         listener = new CommCareFormStubListener(
                 new CommcareFormServiceImpl(commCareAPIHttpClient),
                 new CommcareCaseServiceImpl(commCareAPIHttpClient),
                 eventRelay
         );
 
         motherReferralListener = new MotherReferralListener();
         mapper = new ObjectMapper();
 
         eventListenerRegistry.registerListener(motherReferralListener, MOTHER_REFERRAL_SUBJECT);
     }
 
     @After
     public void tearDown() throws Exception {
         eventListenerRegistry.clearListenersForBean(motherReferralListener.getIdentifier());
     }
 
     @Test
     public void shouldNotPublishEventIfEventNotContainFormId() {
         listener.handle(CommcareFormStubEvent(null));
 
         assertFalse(motherReferralListener.received);
     }
 
     @Test
     public void shouldNotPublishEventIfFormNotFound() {
         listener.handle(CommcareFormStubEvent("formId"));
 
         assertFalse(motherReferralListener.received);
     }
 
     @Test
     public void shouldNotPublishEventFromFormWeDontNeed() {
         String formId = "formId";
         String json = "{\"form\": {\"@name\": \"A Form We Don't Care About\"}}";
 
         when(commCareAPIHttpClient.formRequest(formId)).thenReturn(json);
 
         listener.handle(CommcareFormStubEvent(formId));
 
         assertFalse(motherReferralListener.received);
     }
 
     @Test
     public void postPartumVisitReferralShouldPublish() throws InterruptedException {
         String formId = "formId";
         String referralCaseId = "referralCaseId";
         String motherCaseId = "motherCaseId";
 
         ObjectNode formCase = mapper.createObjectNode();
         formCase.put(String.format("@%s", Commcare.CASE_ID), motherCaseId);
 
         ObjectNode postPartumVisit = mapper.createObjectNode();
         postPartumVisit.put(Commcare.DATE_OF_BIRTH, "2013-01-03");
         postPartumVisit.put(Commcare.ATTENDED_POSTNATAL, "yes");
         postPartumVisit.put(Commcare.PLACE_OF_BIRTH, "home");
 
         ObjectNode form = mapper.createObjectNode();
         form.put(String.format("@%s", Commcare.NAME), "Post-Partum Visit");
         form.put(Commcare.STILL_ALIVE, "yes");
         form.put(Commcare.GAVE_BIRTH, "yes");
         form.put(Commcare.CREATE_REFERRAL, "yes");
         form.put(Commcare.REFERRAL_ID, "referralId");
         form.put(Commcare.DATE_OF_VISIT, "2013-01-03");
         form.put(Commcare.NEXT_VISIT_DATE_PLUS_1, "2013-01-03");
         form.put(Commcare.CASE, formCase);
         form.put(Commcare.POST_PARTUM_VISIT, postPartumVisit);
 
         ObjectNode commcareForm = mapper.createObjectNode();
         commcareForm.put("form", form);
 
         ObjectNode referralProperties = mapper.createObjectNode();
         referralProperties.put(Commcare.CASE_TYPE, "referral");
 
         ObjectNode referralCase = mapper.createObjectNode();
         referralCase.put("properties", referralProperties);
 
         ObjectNode motherProperties = mapper.createObjectNode();
         motherProperties.put(Commcare.CASE_TYPE, "mother");
 
         ObjectNode motherCase = mapper.createObjectNode();
         motherCase.put("properties", motherProperties);
 
         when(commCareAPIHttpClient.formRequest(formId)).thenReturn(commcareForm.toString());
         when(commCareAPIHttpClient.singleCaseRequest(referralCaseId)).thenReturn(referralCase.toString());
         when(commCareAPIHttpClient.singleCaseRequest(motherCaseId)).thenReturn(motherCase.toString());
 
         List<String> list = Arrays.asList(motherCaseId, referralCaseId);
 
         synchronized (lock) {
             listener.handle(CommcareFormStubEvent(formId, list));
             lock.wait(HALF_MINUTE);
         }
 
         assertTrue(motherReferralListener.received);
     }
 
     @Test
     public void pregnancyVisitWithoutReferralShouldPublishNoEvent() {
         String formId = "formId";
 
         ObjectNode formCase = mapper.createObjectNode();
         formCase.put(String.format("@%s", Commcare.CASE_ID), "caseId");
 
         ObjectNode form = mapper.createObjectNode();
         form.put(String.format("@%s", Commcare.NAME), "Pregnancy Visit");
         form.put(Commcare.CREATE_REFERRAL, "no");
         form.put(Commcare.REFERRAL_ID, "");
         form.put(Commcare.DATE_OF_VISIT, "2013-01-03");
         form.put(Commcare.CASE, formCase);
 
         ObjectNode commcareForm = mapper.createObjectNode();
         commcareForm.put("form", form);
 
         when(commCareAPIHttpClient.formRequest(formId)).thenReturn(commcareForm.toString());
 
         listener.handle(CommcareFormStubEvent(formId));
 
         assertFalse(motherReferralListener.received);
     }
 
     @Test
     public void pregnancyVisitWithReferralShouldPublishOneEvent() throws InterruptedException {
         String formId = "formId";
         String referralCaseId = "referralCaseId";
         String motherCaseId = "motherCaseId";
 
         ObjectNode formCase = mapper.createObjectNode();
         formCase.put(String.format("@%s", Commcare.CASE_ID), motherCaseId);
 
         ObjectNode form = mapper.createObjectNode();
         form.put(String.format("@%s", Commcare.NAME), "Pregnancy Visit");
         form.put(Commcare.CREATE_REFERRAL, "yes");
         form.put(Commcare.REFERRAL_ID, "referralId");
         form.put(Commcare.DATE_OF_VISIT, "2013-01-03");
         form.put(Commcare.CASE, formCase);
 
         ObjectNode commcareForm = mapper.createObjectNode();
         commcareForm.put("form", form);
 
         ObjectNode referralProperties = mapper.createObjectNode();
         referralProperties.put(Commcare.CASE_TYPE, "referral");
 
         ObjectNode referralCase = mapper.createObjectNode();
         referralCase.put("properties", referralProperties);
 
         ObjectNode motherProperties = mapper.createObjectNode();
         motherProperties.put(Commcare.CASE_TYPE, "mother");
 
         ObjectNode motherCase = mapper.createObjectNode();
         motherCase.put("properties", motherProperties);
 
         when(commCareAPIHttpClient.formRequest(formId)).thenReturn(commcareForm.toString());
         when(commCareAPIHttpClient.singleCaseRequest(referralCaseId)).thenReturn(referralCase.toString());
         when(commCareAPIHttpClient.singleCaseRequest(motherCaseId)).thenReturn(motherCase.toString());
 
         List<String> list = Arrays.asList(motherCaseId, referralCaseId);
 
         synchronized (lock) {
             listener.handle(CommcareFormStubEvent(formId, list));
             lock.wait(HALF_MINUTE);
         }
 
         assertTrue(motherReferralListener.received);
     }
 
     @Test
     public void pregnancyVisitWithReferralWithMissingFieldShouldNotPublishEvent() throws InterruptedException {
         String formId = "formId";
         String referralCaseId = "referralCaseId";
         String motherCaseId = "motherCaseId";
 
         ObjectNode formCase = mapper.createObjectNode();
         formCase.put(String.format("@%s", Commcare.CASE_ID), motherCaseId);
 
         ObjectNode form = mapper.createObjectNode();
         form.put(String.format("@%s", Commcare.NAME), "Pregnancy Visit");
         form.put(Commcare.CREATE_REFERRAL, "yes");
         form.put(Commcare.DATE_OF_VISIT, "2013-01-03");
         form.put(Commcare.CASE, formCase);
 
         ObjectNode commcareForm = mapper.createObjectNode();
         commcareForm.put("form", form);
 
         ObjectNode referralProperties = mapper.createObjectNode();
         referralProperties.put(Commcare.CASE_TYPE, "referral");
 
         ObjectNode referralCase = mapper.createObjectNode();
         referralCase.put("properties", referralProperties);
 
         ObjectNode motherProperties = mapper.createObjectNode();
         motherProperties.put(Commcare.CASE_TYPE, "mother");
 
         ObjectNode motherCase = mapper.createObjectNode();
         motherCase.put("properties", motherProperties);
 
         when(commCareAPIHttpClient.formRequest(formId)).thenReturn(commcareForm.toString());
         when(commCareAPIHttpClient.singleCaseRequest(referralCaseId)).thenReturn(referralCase.toString());
         when(commCareAPIHttpClient.singleCaseRequest(motherCaseId)).thenReturn(motherCase.toString());
 
         List<String> list = Arrays.asList(motherCaseId, referralCaseId);
 
         synchronized (lock) {
             listener.handle(CommcareFormStubEvent(formId, list));
             lock.wait(HALF_MINUTE);
         }
 
         assertTrue(motherReferralListener.received);
     }
 
     private MotechEvent CommcareFormStubEvent(String formId) {
         MotechEvent event = new MotechEvent(EventSubjects.FORM_STUB_EVENT);
         event.getParameters().put(EventDataKeys.FORM_ID, formId);
 
         return event;
     }
 
     private MotechEvent CommcareFormStubEvent(String formId, List<String> cases) {
         MotechEvent event = CommcareFormStubEvent(formId);
         event.getParameters().put(EventDataKeys.CASE_IDS, cases);
 
         return event;
     }
 
     public class MotherReferralListener implements EventListener {
         private boolean received;
 
         public void handle(MotechEvent event) {
             received = true;
 
             synchronized (lock) {
                 lock.notify();
             }
         }
 
         @Override
         public String getIdentifier() {
             return getClass().getSimpleName();
         }
     }
 }
