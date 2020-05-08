 package org.motechproject.ghana.national.functional.mobile;
 
 import org.junit.runner.RunWith;
 import org.motechproject.ghana.national.domain.GeneralQueryType;
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
 
 import static org.hamcrest.Matchers.hasItem;
 import static org.junit.Assert.assertThat;
 import static org.testng.Assert.assertEquals;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
 public class GeneralQueryFormUploadTest extends LoggedInUserFunctionalTest {
 
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
 
         staffId = staffGenerator.createStaff(browser, homePage);
                 final String firstPatientNameGenerated = new DataGenerator().randomString(5);
         String patientName = firstPatientNameGenerated + "XXX Client First Name";
         patient = TestPatient.with(patientName, staffId)
                         .patientType(TestPatient.PATIENT_TYPE.PREGNANT_MOTHER)
                         .estimatedDateOfBirth(false);
         patientId = patientGenerator.createPatient(patient, browser, homePage);
 
     }
 
    @Test
     public void shouldUploadFormWithGeneralQueryTypeAsANCDefaulters() throws IOException {
         DataGenerator dataGenerator = new DataGenerator();
 
         String secondPatientName = dataGenerator.randomString(5)+"XXXXXXClient First ";
         TestPatient secondTestPatient = TestPatient.with(secondPatientName, staffId)
                 .patientType(TestPatient.PATIENT_TYPE.PREGNANT_MOTHER)
                 .estimatedDateOfBirth(false);
         patientGenerator.createPatient(secondTestPatient, browser, homePage);
 
         HashMap<String, String> inputParams = new HashMap<String, String>() {{
             put("facilityId", patient.facilityId());
             put("staffId", staffId);
             put("queryType", GeneralQueryType.ANC_DEFAULTERS.toString());
             put("responsePhoneNumber", "0987654321");
         }};
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.generalQueryForm(), inputParams);
         assertEquals(1, response.getSuccessCount());
     }
 
     @Test
     public void shouldCheckFormMandatoryFieldsForGeneralQuery() throws Exception {
         final XformHttpClient.XformResponse xformResponse = mobile.upload(MobileForm.generalQueryForm(), new HashMap<String, String>() {{
             put("facilityId", "12345");
             put("staffId", "123");
             put("responsePhoneNumber","123");
             put("queryType", GeneralQueryType.CWC_DEFAULTERS.toString());
         }});
         final List<XformHttpClient.Error> errors = xformResponse.getErrors();
         assertEquals(errors.size(), 1);
         final Map<String, List<String>> errorsMap = errors.iterator().next().getErrors();
         assertEquals(errorsMap.size(),3);
         assertThat(errorsMap.get("staffId"), hasItem("not found"));
         assertThat(errorsMap.get("facilityId"), hasItem("not found"));
         assertThat(errorsMap.get("responsePhoneNumber"), hasItem("wrong format"));
     }
 }
