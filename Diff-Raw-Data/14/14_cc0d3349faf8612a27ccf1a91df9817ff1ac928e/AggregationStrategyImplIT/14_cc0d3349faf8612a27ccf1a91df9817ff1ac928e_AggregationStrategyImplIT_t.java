 package org.motechproject.ghana.national.functional.aggregator;
 
 
 import org.hamcrest.MatcherAssert;
 import org.joda.time.DateTime;
 import org.junit.runner.RunWith;
import org.motechproject.ghana.national.domain.*;
 import org.motechproject.ghana.national.functional.OpenMRSAwareFunctionalTest;
 import org.motechproject.ghana.national.functional.data.TestFacility;
 import org.motechproject.ghana.national.functional.data.TestMobileMidwifeEnrollment;
 import org.motechproject.ghana.national.functional.data.TestPatient;
 import org.motechproject.ghana.national.functional.pages.BasePage;
 import org.motechproject.ghana.national.functional.pages.patient.MobileMidwifeEnrollmentPage;
 import org.motechproject.ghana.national.functional.pages.patient.PatientEditPage;
 import org.motechproject.ghana.national.functional.pages.patient.SearchPatientPage;
 import org.motechproject.ghana.national.functional.util.DataGenerator;
import org.motechproject.ghana.national.messagegateway.domain.AggregationStrategy;
import org.motechproject.ghana.national.messagegateway.domain.MessageRecipientType;
import org.motechproject.ghana.national.messagegateway.domain.NextMondayDispatcher;
import org.motechproject.ghana.national.messagegateway.domain.Payload;
 import org.motechproject.ghana.national.repository.SMSGateway;
 import org.motechproject.ghana.national.tools.Utility;
 import org.motechproject.openmrs.security.OpenMRSSession;
 import org.motechproject.util.DateUtil;
 import org.quartz.SchedulerException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.*;
 
 import static ch.lambdaj.Lambda.join;
import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.motechproject.ghana.national.configuration.TextMessageTemplateVariables.*;
 import static org.motechproject.ghana.national.domain.AlertWindow.DUE;
 import static org.motechproject.ghana.national.domain.AlertWindow.UPCOMING;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
 public class AggregationStrategyImplIT extends OpenMRSAwareFunctionalTest {
     @Autowired
     private AggregationStrategy aggregationStrategy;
     @Autowired
     private SMSGateway smsGateway;
     @Autowired
     private NextMondayDispatcher deliveryStrategy;
 
     @Value("#{openmrsProperties['openmrs.admin.username']}")
     private String userName;
 
     @Value("#{openmrsProperties['openmrs.admin.password']}")
     private String password;
     @Autowired
     private OpenMRSSession openMRSSession;
 
     DataGenerator dataGenerator = new DataGenerator();
 
     String facilityName;
     TestFacility facility;
     String facilityId;
     String staffId;
 
     @BeforeMethod
     public void setUp() {
         SecurityContext context = SecurityContextHolder.getContext();
         context.setAuthentication(new UsernamePasswordAuthenticationToken(OpenMRSSession.login(userName, password), password));
         openMRSSession.open();
         openMRSSession.authenticate();
         facilityName = "Facility Name" + dataGenerator.randomString(5);
         facility = TestFacility.with(facilityName);
         facilityId = facilityGenerator.createFacility(facility, browser, homePage);
         staffId = staffGenerator.createStaff(browser, homePage);
     }
 
     @Test
     public void shouldAggregateMessagesAndSendToPatientMobileNumberGivenDuringRegistrationIfThereISNoMMEnrollment() {
 
         String patientPhoneNumber = dataGenerator.randomPhoneNumber();
         TestPatient patientWithoutMM = TestPatient.with("firstName" + dataGenerator.randomString(5), staffId).phoneNumber(patientPhoneNumber);
         String patientId = patientGenerator.createPatient(patientWithoutMM, browser, homePage);
 
         SMSPayload payload1 = setUpPayload(SmsTemplateKeys.BCG_SMS_KEY, "milestone1", "early", "serialNumber1", patientId, patientWithoutMM, MessageRecipientType.PATIENT);
         SMSPayload payload2 = setUpPayload(SmsTemplateKeys.CWC_MEASLES_SMS_KEY, "milestone1", "late", "serialNumber2", patientId, patientWithoutMM, MessageRecipientType.PATIENT);
         SMSPayload payload3 = setUpPayload(SmsTemplateKeys.CWC_YF_SMS_KEY, "milestone1", "due", "serialNumber3", patientId, patientWithoutMM, MessageRecipientType.PATIENT);
         List<Payload> aggregatedPayload = aggregationStrategy.aggregate(Arrays.<Payload>asList(payload1, payload2, payload3));
         String expectedText = new StringBuilder().append(payload1.getText()).append(Constants.SMS_SEPARATOR)
                 .append(payload2.getText()).append(Constants.SMS_SEPARATOR)
                 .append(payload3.getText()).append(Constants.SMS_SEPARATOR).toString();
         List<SMSPayload> aggregatedSMS = Utility.cast(aggregatedPayload);
         assertThat(aggregatedSMS.size(), is(equalTo(1)));
         assertThat(aggregatedSMS.get(0).getText(), is(equalTo(expectedText)));
         assertThat(aggregatedSMS.get(0).getPhoneNumber(), is(equalTo(patientPhoneNumber)));
 
 
     }
 
     @Test
     public void shouldAggregateMessagesAndSendToPatientIfThereIsMMEnrollment() {
         TestPatient patientWithMM = TestPatient.with("firstName" + dataGenerator.randomString(5), staffId);
         String patientId = patientGenerator.createPatient(patientWithMM, browser, homePage);
 
         TestMobileMidwifeEnrollment enrollmentDetails = TestMobileMidwifeEnrollment.with(staffId).patientId(patientId);
 
         MobileMidwifeEnrollmentPage enrollmentPage = toMobileMidwifeEnrollmentPage(patientWithMM, homePage);
         enrollmentPage.enroll(enrollmentDetails);
 
         SMSPayload payload1 = setUpPayload(SmsTemplateKeys.BCG_SMS_KEY, "milestone1", "early", "serialNumber1", patientId, patientWithMM, MessageRecipientType.PATIENT);
         SMSPayload payload2 = setUpPayload(SmsTemplateKeys.CWC_MEASLES_SMS_KEY, "milestone1", "late", "serialNumber2", patientId, patientWithMM, MessageRecipientType.PATIENT);
         SMSPayload payload3 = setUpPayload(SmsTemplateKeys.CWC_YF_SMS_KEY, "milestone1", "due", "serialNumber3", patientId, patientWithMM, MessageRecipientType.PATIENT);
         List<Payload> aggregatedSMS = aggregationStrategy.aggregate(Arrays.<Payload>asList(payload1, payload2, payload3));
         String expectedText = new StringBuilder().append(payload1.getText()).append(Constants.SMS_SEPARATOR)
                 .append(payload2.getText()).append(Constants.SMS_SEPARATOR)
                 .append(payload3.getText()).append(Constants.SMS_SEPARATOR).toString();
         assertThat(aggregatedSMS.size(), is(equalTo(1)));
         assertThat(Utility.<Payload,SMSPayload>cast(aggregatedSMS).get(0).getText(), is(equalTo(expectedText)));
         assertThat(Utility.<Payload,SMSPayload>cast(aggregatedSMS).get(0).getPhoneNumber(), is(equalTo(enrollmentDetails.phoneNumber())));
 
 
     }
 
     @Test
     public void shouldSendDefaultMessagesToFacilityIfThereAreNoValidMessages() {
         HashMap templateValues = new HashMap<String, String>() {{
             put(WINDOW_NAMES, join(AlertWindow.ghanaNationalWindowNames(), ", "));
             put(FACILITY, facilityName);
         }};
         SMSPayload smsPayload = setUpDefaultPayloadForFacility(SmsTemplateKeys.FACILITIES_DEFAULT_MESSAGE_KEY, templateValues);
 
         List<Payload> aggregatedPayload = aggregationStrategy.aggregate(Arrays.<Payload>asList(smsPayload));
         String expectedText = facilityName + " has no " + join(AlertWindow.ghanaNationalWindowNames(), ", ") + " cares for this week";
         List<SMSPayload> aggregatedSMS = Utility.cast(aggregatedPayload);
         assertThat(aggregatedSMS.size(), is(equalTo(1)));
         assertThat(aggregatedSMS.get(0).getText(), is(equalTo(expectedText)));
         assertThat(aggregatedSMS.get(0).getPhoneNumber(), is(equalTo(facility.phoneNumber())));
     }
 
     @Test
     public void shouldNotSendDefaultMessagesToFacilityIfThereAreValidMessages() {
 
         final HashMap defaultTemplateValues = new HashMap<String, String>() {{
             put(WINDOW_NAMES, join(AlertWindow.ghanaNationalWindowNames(), ", "));
             put(FACILITY, facilityName);
         }};
         List<Payload> messagesList = new ArrayList<Payload>() {{
             add(SMSPayload.fromText(UPCOMING.getName() + ",milestoneName1,motechId,serialNumber,firstName,lastName", facilityId, null, null, MessageRecipientType.FACILITY));
             add(SMSPayload.fromText(UPCOMING.getName() + ",milestoneName2,motechId,serialNumber,firstName,lastName", facilityId, null, null, MessageRecipientType.FACILITY));
             add(SMSPayload.fromText(DUE.getName() + ",milestoneName,motechId,serialNumber,firstName,lastName", facilityId, null, null, MessageRecipientType.FACILITY));
             add(SMSPayload.fromText(DUE.getName() + ",milestoneName,motechId2,serialNumber,firstName2,lastName3", facilityId, null, null, MessageRecipientType.FACILITY));
             add(SMSPayload.fromText(DUE.getName() + ",milestoneName,motechId3,serialNumber,firstName2,lastName3", facilityId, null, null, MessageRecipientType.FACILITY));
             add(setUpDefaultPayloadForFacility(SmsTemplateKeys.FACILITIES_DEFAULT_MESSAGE_KEY, defaultTemplateValues));
         }};
 
         List<SMSPayload> aggregatedSMS = Utility.cast(aggregationStrategy.aggregate(messagesList));
         assertThat(aggregatedSMS.size(), is(equalTo(3)));
         MatcherAssert.assertThat(aggregatedSMS.get(0).getText(), is(equalTo(UPCOMING.getName() + ": firstName lastName, motechId, serialNumber, milestoneName1, milestoneName2")));
         MatcherAssert.assertThat(aggregatedSMS.get(1).getText(), is(equalTo((DUE.getName() + ": firstName lastName, motechId, serialNumber, milestoneName, firstName2 lastName3, motechId2, serialNumber, milestoneName, firstName2 lastName3, motechId3, serialNumber, milestoneName"))));
         MatcherAssert.assertThat(aggregatedSMS.get(2).getText(), is(equalTo(facilityName + " has no Overdue cares for this week")));
         MatcherAssert.assertThat(aggregatedSMS.get(0).getPhoneNumber(), is(equalTo(facility.phoneNumber())));
         MatcherAssert.assertThat(aggregatedSMS.get(1).getPhoneNumber(), is(equalTo(facility.phoneNumber())));
         MatcherAssert.assertThat(aggregatedSMS.get(2).getPhoneNumber(), is(equalTo(facility.phoneNumber())));
     }
 
     private SMSPayload setUpPayload(String smsKey, String milestoneName, String windowName, String serialNumber, String uniqueId, TestPatient patient, MessageRecipientType messageRecipientType) {
         return SMSPayload.fromTemplate(smsGateway.getSMSTemplate(smsKey),
                 populatePatientDetails(new SMSTemplate().fillScheduleDetails(milestoneName, windowName).fillSerialNumber(serialNumber).getRuntimeVariables(), patient),
                 uniqueId, DateTime.now(), deliveryStrategy, messageRecipientType);
     }
 
     private SMSPayload setUpDefaultPayloadForFacility(String smsKey, Map<String, String> templateValues) {
         return SMSPayload.fromTemplate(smsGateway.getSMSTemplate(smsKey), templateValues, facilityId, DateUtil.now(), deliveryStrategy, MessageRecipientType.FACILITY);
     }
 
     private Map<String, String> populatePatientDetails(HashMap<String, String> runtimeVariables, TestPatient patient) {
         runtimeVariables.put(MOTECH_ID, patient.motechId());
         runtimeVariables.put(FIRST_NAME, patient.firstName());
         runtimeVariables.put(LAST_NAME, patient.lastName());
         runtimeVariables.put(GENDER, "F");
         return runtimeVariables;
     }
 
     private MobileMidwifeEnrollmentPage toMobileMidwifeEnrollmentPage(TestPatient patient, BasePage basePage) {
         SearchPatientPage searchPatientPage = browser.toSearchPatient(basePage);
         searchPatientPage.searchWithName(patient.firstName());
         PatientEditPage patientEditPage = browser.toPatientEditPage(searchPatientPage, patient);
         return browser.toMobileMidwifeEnrollmentForm(patientEditPage);
     }
 
     @AfterMethod
     public void tearDown() throws SchedulerException {
         openMRSSession.close();
     }
 }
