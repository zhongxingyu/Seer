 package org.motechproject.ghana.national.functional.mobile;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.joda.time.LocalDate;
 import org.junit.runner.RunWith;
 import org.motechproject.ghana.national.domain.ClientQueryType;
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.functional.LoggedInUserFunctionalTest;
 import org.motechproject.ghana.national.functional.data.TestPatient;
 import org.motechproject.ghana.national.functional.framework.XformHttpClient;
 import org.motechproject.ghana.national.functional.mobileforms.MobileForm;
 import org.motechproject.ghana.national.functional.util.DataGenerator;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.hasItem;
 import static org.motechproject.util.DateUtil.today;
 import static org.testng.Assert.assertEquals;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
 public class ClientQueryFormUploadTest extends LoggedInUserFunctionalTest {
 
     @Value("#{functionalTestProperties['port']}")
     private String port;
 
     @Value("#{functionalTestProperties['host']}")
     private String host;
 
     String patientId;
 
     String staffId;
     private TestPatient patient;
 
     @Value("#{functionalTestProperties['delivery_path']}")
     private String deliveryPath;
 
     @Value("#{functionalTestProperties['delivery_clear_path']}")
     private String deliveryClearPath;
 
     @BeforeMethod
     public void setUp() throws IOException {
 
         clearMessageGateway();
 
         staffId = staffGenerator.createStaff(browser, homePage);
                 final String firstPatientNameGenerated = new DataGenerator().randomString(5);
         String patientName = firstPatientNameGenerated + "XXX Client First Name";
         patient = TestPatient.with(patientName, staffId)
                         .patientType(TestPatient.PATIENT_TYPE.PREGNANT_MOTHER)
                         .estimatedDateOfBirth(false);
         patientId = patientGenerator.createPatientWithStaff(patient, browser, homePage);
 
     }
 
     @Test
     public void shouldUploadFormWithClientQueryTypeAsFindClientID() throws IOException {
         DataGenerator dataGenerator = new DataGenerator();
 
         String secondPatientName = dataGenerator.randomString(5)+"XXXXXXClient First ";
         TestPatient secondTestPatient = TestPatient.with(secondPatientName, staffId)
                 .patientType(TestPatient.PATIENT_TYPE.PREGNANT_MOTHER)
                 .estimatedDateOfBirth(false);
         patientGenerator.createPatientWithStaff(secondTestPatient, browser, homePage);
 
         HashMap<String, String> inputParams = new HashMap<String, String>() {{
             put("facilityId", patient.facilityId());
             put("staffId", staffId);
             put("queryType", ClientQueryType.FIND_CLIENT_ID.toString());
             put("firstName", patient.firstName());
             put("sender", "0987654321");
         }};
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.queryClientForm(), inputParams);
         assertEquals(1, response.getSuccessCount());
 
         String responseBodyAsString = getMessageGatewayResponse();
 
         assertThat(responseBodyAsString, containsString(patientId));
         assertThat(responseBodyAsString, containsString(patient.firstName()));
     }
 
     @Test
     public void shouldCheckIfAtleastOneInfoIsEnteredForFindClientIDQuery() throws Exception {
         final XformHttpClient.XformResponse xformResponse = mobile.upload(MobileForm.queryClientForm(), new HashMap<String, String>() {
             {
                 put("facilityId", "-1");
                 put("staffId", "-1");
                 put("queryType", ClientQueryType.FIND_CLIENT_ID.toString());
                 put("sender", "0987654321");
             }
         });
         final List<XformHttpClient.Error> errors = xformResponse.getErrors();
 
         assertEquals(errors.size(), 1);
         final Map<String, List<String>> errorsMap = errors.iterator().next().getErrors();
 
         assertThat(errorsMap.get("queryType"), hasItem(Constants.INSUFFICIENT_SEARCH_CRITERIA));
     }
 
     @Test
     public void shouldCheckFormMandatoryFieldsForClientDetailsQuery() throws Exception {
         final XformHttpClient.XformResponse xformResponse = mobile.upload(MobileForm.queryClientForm(), new HashMap<String, String>() {{
             put("motechId", "-1");
             put("facilityId", "-1");
             put("staffId", "-1");
             put("queryType", ClientQueryType.CLIENT_DETAILS.toString());
         }});
         final List<XformHttpClient.Error> errors = xformResponse.getErrors();
         assertEquals(errors.size(), 1);
         final Map<String, List<String>> errorsMap = errors.iterator().next().getErrors();
 
         assertThat(errorsMap.get("staffId"), hasItem("not found"));
         assertThat(errorsMap.get("facilityId"), hasItem("not found"));
         assertThat(errorsMap.get("motechId"), hasItem("not found"));
     }
 
     @Test
     public void shouldQueryUpcomingCareSchedules() throws IOException {
         final LocalDate eddIn14thWeekForDue = today().plusWeeks(26);
         mobile.upload(MobileForm.registerANCForm(), new HashMap<String, String>() {{
             put("motechId", patientId);
             put("facilityId", patient.facilityId());
             put("staffId", patient.staffId());
             put("gravida", "1");
             put("parity", "1");
             put("height", "61");
             put("estDeliveryDate", eddIn14thWeekForDue.toString("yyyy-MM-dd"));
             put("date", today().toString("yyyy-MM-dd"));
             put("deliveryDateConfirmed", "Y");
             put("regDateToday", "TODAY");
             put("addHistory", "0");
             put("ancRegNumber", "123ABC");
         }});
 
         XformHttpClient.XformResponse  xformResponse = mobile.upload(MobileForm.queryClientForm(), new HashMap<String, String>() {{
            put("queryType", ClientQueryType.UPCOMING_CARE.toString());
             put("motechId", patientId);
             put("facilityId", patient.facilityId());
             put("staffId", staffId);
             put("sender", "0987654321");
         }});
         final List<XformHttpClient.Error> errors = xformResponse.getErrors();
         assertEquals(errors.size(), 0);
 
         String responseBodyAsString = getMessageGatewayResponse();
 
         assertThat(responseBodyAsString, containsString(patientId));
         assertThat(responseBodyAsString, containsString(patient.firstName()));
         assertThat(responseBodyAsString, containsString("IPT1"));
         assertThat(responseBodyAsString, containsString(today().toString(Constants.PATTERN_DD_MMM_YYYY)));
     }
 
     @Test
     public void shouldUploadFormWithClientQueryTypeAsClientDetails() throws IOException {
         DataGenerator dataGenerator = new DataGenerator();
 
         final String staffId = staffGenerator.createStaff(browser, homePage);
         String firstName = "First Name" + dataGenerator.randomString(5);
         TestPatient testPatient = TestPatient.with(firstName, staffId)
                 .patientType(TestPatient.PATIENT_TYPE.PREGNANT_MOTHER)
                 .estimatedDateOfBirth(false);
 
         final String patientId = patientGenerator.createPatientWithStaff(testPatient, browser, homePage);
         HashMap<String, String> inputParams = new HashMap<String, String>() {{
             put("facilityId", "13212");
             put("staffId", staffId);
             put("queryType", ClientQueryType.CLIENT_DETAILS.toString());
            put("motechId", patientId);
             put("sender", "0987654321");
         }};
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.queryClientForm(), inputParams);
         assertEquals(1, response.getSuccessCount());
 
         String responseBodyAsString = getMessageGatewayResponse();
                                    System.out.println(responseBodyAsString);
         assertThat(responseBodyAsString, containsString(patientId));
         assertThat(responseBodyAsString, containsString(firstName));
     }
 
     private String getMessageGatewayResponse() throws IOException {
         return httpGet(String.format("http://%s:%s/%s", host, port, deliveryPath));
     }
 
     private String clearMessageGateway() throws IOException {
         return httpGet(String.format("http://%s:%s/%s", host, port, deliveryClearPath));
     }
 
     private String httpGet(String url) throws IOException {
         GetMethod getMethod = new GetMethod(url);
         HttpClient httpClient = new HttpClient();
         httpClient.executeMethod(getMethod);
         return getMethod.getResponseBodyAsString();
     }
 
 }
