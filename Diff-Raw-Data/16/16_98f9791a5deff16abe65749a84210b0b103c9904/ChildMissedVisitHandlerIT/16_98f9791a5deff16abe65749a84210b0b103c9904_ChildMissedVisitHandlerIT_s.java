 package org.worldvision.sierraleone.task;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.ObjectNode;
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.motechproject.cmslite.api.model.StringContent;
 import org.motechproject.cmslite.api.service.CMSLiteService;
 import org.motechproject.commcare.service.impl.CommcareCaseServiceImpl;
 import org.motechproject.commcare.service.impl.CommcareFixtureServiceImpl;
 import org.motechproject.commcare.service.impl.CommcareUserServiceImpl;
 import org.motechproject.commcare.util.CommCareAPIHttpClient;
 import org.motechproject.commons.date.util.DateUtil;
 import org.motechproject.event.MotechEvent;
 import org.motechproject.event.listener.EventListener;
import org.motechproject.event.listener.EventListenerRegistry;
 import org.motechproject.sms.api.constants.EventSubjects;
 import org.motechproject.sms.api.service.SmsService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.worldvision.sierraleone.WorldVisionSettings;
 import org.worldvision.sierraleone.constants.Commcare;
 import org.worldvision.sierraleone.constants.EventKeys;
 import org.worldvision.sierraleone.repository.FixtureIdMap;
 
 import static java.util.Arrays.asList;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import static org.worldvision.sierraleone.task.ConsecutiveMissedVisitListener.CHILD_VISITS_MESSAGE_NAME;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {
         "classpath*:/META-INF/motech/applicationCommcareAPI.xml",
         "classpath*:/META-INF/motech/activemqConnection.xml",
         "classpath*:/META-INF/motech/eventQueuePublisher.xml",
         "classpath*:/META-INF/motech/eventQueueConsumer.xml",
         "classpath*:/META-INF/motech/applicationPlatformConfig.xml",
         "classpath*:/META-INF/motech/applicationCommonsCouchdbContext.xml",
         "classpath*:/META-INF/motech/applicationScheduler.xml",
         "classpath*:/META-INF/motech/applicationCmsLiteApi.xml",
         "classpath*:/META-INF/motech/applicationSmsAPI.xml"
 })
 public class ChildMissedVisitHandlerIT {
     private static final int ONE_MINUTE = 60 * 1000;
 
     private CommCareAPIHttpClient commCareAPIHttpClient;
 
     @Autowired
     private SmsService smsService;
 
     @Autowired
     private CMSLiteService cmsLiteService;
 
     @Autowired
     private EventListenerRegistry eventListenerRegistry;
 
     private WorldVisionSettings settings;
 
     private ConsecutiveMissedVisitListener listener;
 
     private ObjectMapper mapper;
 
     SendSmsListener sendSmsListener;
 
     final Object lock = new Object();
 
     @Before
     public void setUp() throws Exception {
         Resource language = new ClassPathResource("/wv-settings.properties", getClass());
 
         settings = new WorldVisionSettings();
         settings.setModuleName("sierra-leone");
         settings.setConfigFiles(asList(language));
 
         mapper = new ObjectMapper();
         sendSmsListener = new SendSmsListener();
 
         commCareAPIHttpClient = mock(CommCareAPIHttpClient.class);
 
         listener = new ConsecutiveMissedVisitListener(
                 new CommcareCaseServiceImpl(commCareAPIHttpClient),
                 new CommcareUserServiceImpl(commCareAPIHttpClient),
                 new FixtureIdMap(new CommcareFixtureServiceImpl(commCareAPIHttpClient)),
                 smsService, cmsLiteService, settings
         );
 
         eventListenerRegistry.registerListener(sendSmsListener, EventSubjects.SEND_SMS);
     }
 
     @After
     public void tearDown() throws Exception {
         eventListenerRegistry.clearListenersForBean(sendSmsListener.getIdentifier());
     }
 
     @Test
     public void shouldSendSmsWhenTwoAppointmentsAreMissed() throws Exception {
         String childCaseId = "childCaseId";
         String motherCaseId = "motherCaseId";
         String phuId = "phuId";
         String phone = "phone";
         String userId = "userId";
         String firstName = "firstName";
         String lastName = "lastName";
         DateTime lastVisitDate = DateUtil.now().minusMonths(3);
         DateTime aptDate1 = lastVisitDate.plusDays(7);
         DateTime aptDate2 = lastVisitDate.plusDays(14);
         DateTime aptDate3 = lastVisitDate.plusDays(21);
         DateTime aptDate4 = lastVisitDate.plusDays(28);
         DateTime aptDate5 = lastVisitDate.plusDays(35);
         DateTime aptDate6 = lastVisitDate.plusDays(42);
         DateTime aptDate7 = lastVisitDate.plusDays(49);
         DateTime aptDate8 = lastVisitDate.plusDays(56);
         DateTime aptDate9 = lastVisitDate.plusDays(63);
         DateTime aptDate10 = lastVisitDate.plusDays(70);
 
         ObjectNode motherProperties = mapper.createObjectNode();
         motherProperties.put(Commcare.PHU_ID, phuId);
 
         ObjectNode motherCase = mapper.createObjectNode();
         motherCase.put("user_id", userId);
         motherCase.put("properties", motherProperties);
 
         ObjectNode childCaseProperties = mapper.createObjectNode();
         childCaseProperties.put(Commcare.DATE_OF_VISIT, lastVisitDate.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_5A_DATE, aptDate1.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_5B_DATE, aptDate2.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_5C_DATE, aptDate3.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_5D_DATE, aptDate4.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_6_DATE, aptDate5.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_7_DATE, aptDate6.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_8_DATE, aptDate7.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_9_DATE, aptDate8.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_10_DATE, aptDate9.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_11_DATE, aptDate10.toString("yyyy-MM-dd"));
 
         ObjectNode childCase = mapper.createObjectNode();
         childCase.put("properties", childCaseProperties);
 
         ObjectNode fields = mapper.createObjectNode();
         fields.put(Commcare.PHU_ID, phuId);
         fields.put(Commcare.PHONE, phone);
 
         ObjectNode commcareFixture = mapper.createObjectNode();
         commcareFixture.put("id", "AAAAAAAA");
         commcareFixture.put("fixture_type", "phu");
         commcareFixture.put("fields", fields);
 
         ArrayNode array = mapper.createArrayNode();
         array.add(commcareFixture);
 
         ObjectNode fixtures = mapper.createObjectNode();
         fixtures.put("objects", array);
 
         ObjectNode commcareUser = mapper.createObjectNode();
         commcareUser.put("first_name", firstName);
         commcareUser.put("last_name", lastName);
         commcareUser.put("id", userId);
 
         ArrayNode userArray = mapper.createArrayNode();
         userArray.add(commcareUser);
 
         ObjectNode commcareUsers = mapper.createObjectNode();
         commcareUsers.put("objects", userArray);
 
         when(commCareAPIHttpClient.singleCaseRequest(childCaseId)).thenReturn(childCase.toString());
         when(commCareAPIHttpClient.singleCaseRequest(motherCaseId)).thenReturn(motherCase.toString());
         when(commCareAPIHttpClient.usersRequest()).thenReturn(commcareUsers.toString());
         when(commCareAPIHttpClient.fixturesRequest()).thenReturn(fixtures.toString());
         when(commCareAPIHttpClient.fixtureRequest("AAAAAAAA")).thenReturn(commcareFixture.toString());
 
         cmsLiteService.addContent(new StringContent(settings.getLanguage(), CHILD_VISITS_MESSAGE_NAME, "CHW %s has now missed 2 scheduled visits in a row, call to check on their progress."));
 
         MotechEvent event = new MotechEvent();
         event.getParameters().put(EventKeys.CHILD_CASE_ID, childCaseId);
         event.getParameters().put(EventKeys.MOTHER_CASE_ID, motherCaseId);
 
         synchronized (lock) {
             listener.childMissedVisitHandler(event);
             lock.wait(ONE_MINUTE);
         }
 
         assertTrue(sendSmsListener.received);
     }
 
     @Test
     public void shouldNotSendSmsIfPhoneIsNotSet() throws Exception {
         String childCaseId = "childCaseId";
         String motherCaseId = "motherCaseId";
         String phuId = "phuId";
         String phone = null;
         String userId = "userId";
         DateTime lastVisitDate = DateUtil.now().minusMonths(3);
         DateTime aptDate1 = lastVisitDate.plusDays(7);
         DateTime aptDate2 = lastVisitDate.plusDays(14);
         DateTime aptDate3 = lastVisitDate.plusDays(21);
         DateTime aptDate4 = lastVisitDate.plusDays(28);
         DateTime aptDate5 = lastVisitDate.plusDays(35);
         DateTime aptDate6 = lastVisitDate.plusDays(42);
         DateTime aptDate7 = lastVisitDate.plusDays(49);
         DateTime aptDate8 = lastVisitDate.plusDays(56);
         DateTime aptDate9 = lastVisitDate.plusDays(63);
         DateTime aptDate10 = lastVisitDate.plusDays(70);
 
         ObjectNode motherProperties = mapper.createObjectNode();
         motherProperties.put(Commcare.PHU_ID, phuId);
 
         ObjectNode motherCase = mapper.createObjectNode();
         motherCase.put("user_id", userId);
         motherCase.put("properties", motherProperties);
 
         ObjectNode childCaseProperties = mapper.createObjectNode();
         childCaseProperties.put(Commcare.DATE_OF_VISIT, lastVisitDate.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_5A_DATE, aptDate1.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_5B_DATE, aptDate2.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_5C_DATE, aptDate3.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_5D_DATE, aptDate4.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_6_DATE, aptDate5.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_7_DATE, aptDate6.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_8_DATE, aptDate7.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_9_DATE, aptDate8.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_10_DATE, aptDate9.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_11_DATE, aptDate10.toString("yyyy-MM-dd"));
 
         ObjectNode childCase = mapper.createObjectNode();
         childCase.put("properties", childCaseProperties);
 
         ObjectNode fields = mapper.createObjectNode();
         fields.put(Commcare.PHU_ID, phuId);
         fields.put(Commcare.PHONE, phone);
 
         ObjectNode commcareFixture = mapper.createObjectNode();
         commcareFixture.put("id", "AAAAAAAA");
         commcareFixture.put("fixture_type", "phu");
         commcareFixture.put("fields", fields);
 
         ArrayNode array = mapper.createArrayNode();
         array.add(commcareFixture);
 
         ObjectNode fixtures = mapper.createObjectNode();
         fixtures.put("objects", array);
 
         when(commCareAPIHttpClient.singleCaseRequest(childCaseId)).thenReturn(childCase.toString());
         when(commCareAPIHttpClient.singleCaseRequest(motherCaseId)).thenReturn(motherCase.toString());
         when(commCareAPIHttpClient.fixturesRequest()).thenReturn(fixtures.toString());
         when(commCareAPIHttpClient.fixtureRequest("AAAAAAAA")).thenReturn(commcareFixture.toString());
 
         MotechEvent event = new MotechEvent();
         event.getParameters().put(EventKeys.CHILD_CASE_ID, childCaseId);
         event.getParameters().put(EventKeys.MOTHER_CASE_ID, motherCaseId);
 
         listener.childMissedVisitHandler(event);
 
         assertFalse(sendSmsListener.received);
     }
 
     @Test
     public void shouldNotSendSmsIfAppointmentsAreNotMissed() throws Exception {
         String childCaseId = "childCaseId";
         String motherCaseId = "motherCaseId";
         String phuId = "phuId";
         String userId = "userId";
         DateTime lastVisitDate = DateUtil.now().minusMonths(3);
         DateTime aptDate4 = lastVisitDate.plusMonths(4);
         DateTime aptDate5 = lastVisitDate.plusMonths(5);
         DateTime aptDate6 = lastVisitDate.plusMonths(6);
         DateTime aptDate7 = lastVisitDate.plusMonths(7);
         DateTime aptDate8 = lastVisitDate.plusMonths(8);
         DateTime aptDate9 = lastVisitDate.plusMonths(9);
         DateTime aptDate10 = lastVisitDate.plusMonths(10);
 
         ObjectNode motherProperties = mapper.createObjectNode();
         motherProperties.put(Commcare.PHU_ID, phuId);
 
         ObjectNode motherCase = mapper.createObjectNode();
         motherCase.put("user_id", userId);
         motherCase.put("properties", motherProperties);
 
         ObjectNode childCaseProperties = mapper.createObjectNode();
         childCaseProperties.put(Commcare.DATE_OF_VISIT, lastVisitDate.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_5D_DATE, aptDate4.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_6_DATE, aptDate5.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_7_DATE, aptDate6.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_8_DATE, aptDate7.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_9_DATE, aptDate8.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_10_DATE, aptDate9.toString("yyyy-MM-dd"));
         childCaseProperties.put(Commcare.CHILD_VISIT_11_DATE, aptDate10.toString("yyyy-MM-dd"));
 
         ObjectNode childCase = mapper.createObjectNode();
         childCase.put("properties", childCaseProperties);
 
         when(commCareAPIHttpClient.singleCaseRequest(childCaseId)).thenReturn(childCase.toString());
         when(commCareAPIHttpClient.singleCaseRequest(motherCaseId)).thenReturn(motherCase.toString());
 
         MotechEvent event = new MotechEvent();
         event.getParameters().put(EventKeys.CHILD_CASE_ID, childCaseId);
         event.getParameters().put(EventKeys.MOTHER_CASE_ID, motherCaseId);
 
         listener.childMissedVisitHandler(event);
 
         assertFalse(sendSmsListener.received);
     }
 
     @Test
     public void shouldNotSendSmsIfPhuIDIsNull() throws Exception {
         String childCaseId = "childCaseId";
         String motherCaseId = "motherCaseId";
         String phuId = null;
         String userId = "userId";
 
         ObjectNode motherProperties = mapper.createObjectNode();
         motherProperties.put(Commcare.PHU_ID, phuId);
 
         ObjectNode motherCase = mapper.createObjectNode();
         motherCase.put("user_id", userId);
         motherCase.put("properties", motherProperties);
 
         ObjectNode childCase = mapper.createObjectNode();
         childCase.put("properties", mapper.createObjectNode());
 
         when(commCareAPIHttpClient.singleCaseRequest(childCaseId)).thenReturn(childCase.toString());
         when(commCareAPIHttpClient.singleCaseRequest(motherCaseId)).thenReturn(motherCase.toString());
 
         MotechEvent event = new MotechEvent();
         event.getParameters().put(EventKeys.CHILD_CASE_ID, childCaseId);
         event.getParameters().put(EventKeys.MOTHER_CASE_ID, motherCaseId);
 
         listener.childMissedVisitHandler(event);
 
         assertFalse(sendSmsListener.received);
     }
 
     @Test
     public void shouldNotSendSmsIfMotherCaseIsNull() throws Exception {
         String childCaseId = "childCaseId";
         String motherCaseId = "motherCaseId";
         String phuId = null;
 
         ObjectNode motherProperties = mapper.createObjectNode();
         motherProperties.put(Commcare.PHU_ID, phuId);
 
         ObjectNode childCase = mapper.createObjectNode();
         childCase.put("properties", mapper.createObjectNode());
 
         when(commCareAPIHttpClient.singleCaseRequest(childCaseId)).thenReturn(childCase.toString());
         when(commCareAPIHttpClient.singleCaseRequest(motherCaseId)).thenReturn(null);
 
         MotechEvent event = new MotechEvent();
         event.getParameters().put(EventKeys.CHILD_CASE_ID, childCaseId);
         event.getParameters().put(EventKeys.MOTHER_CASE_ID, motherCaseId);
 
         listener.childMissedVisitHandler(event);
 
         assertFalse(sendSmsListener.received);
     }
 
     @Test
     public void shouldNotSendSmsIfChildCaseIsNull() throws Exception {
         String childCaseId = "childCaseId";
         String motherCaseId = "motherCaseId";
 
         when(commCareAPIHttpClient.singleCaseRequest(childCaseId)).thenReturn(null);
 
         MotechEvent event = new MotechEvent();
         event.getParameters().put(EventKeys.CHILD_CASE_ID, childCaseId);
         event.getParameters().put(EventKeys.MOTHER_CASE_ID, motherCaseId);
 
         listener.childMissedVisitHandler(event);
 
         assertFalse(sendSmsListener.received);
     }
 
     public class SendSmsListener implements EventListener {
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
