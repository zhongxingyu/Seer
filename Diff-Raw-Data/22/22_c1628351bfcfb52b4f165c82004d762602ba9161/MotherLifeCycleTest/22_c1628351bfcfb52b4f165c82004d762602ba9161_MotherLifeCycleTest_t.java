 package org.motechproject.care.reporting.ft;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.motechproject.care.reporting.ft.asserters.Asserter;
 import org.motechproject.care.reporting.ft.reporting.TableName;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 public class MotherLifeCycleTest extends BaseTestCase {
 
     private String flwId;
     private String groupId;
     private String caseId;
     private String receivedOn, receivedOnIST;
     private String newFormInstanceId;
     private String registrationFormInstanceId;
 
     @Autowired
     private Asserter asserter;
 
    private Map<String, String> placeholderMap = new HashMap<>();
    private Map<String, String> headerMap = new HashMap<>();
 
     @Before
     public void setUp() {
         caseId = UUID.randomUUID().toString();
         newFormInstanceId = UUID.randomUUID().toString();
         registrationFormInstanceId = UUID.randomUUID().toString();
         flwId = UUID.randomUUID().toString().replaceAll("-", "");
         groupId = UUID.randomUUID().toString().replaceAll("-", "");
         receivedOn = "2012-07-21T10:10:20.000Z";
         receivedOnIST = "2012-07-21 15:40:20.0";
 
         placeholderMap.put("caseId", caseId);
         placeholderMap.put("ownerId", groupId);
         placeholderMap.put("userId", flwId);
         placeholderMap.put("receivedOn", receivedOn);
         placeholderMap.put("receivedOnIST", receivedOnIST);
         asserter.setPlaceholder(placeholderMap);
 
         headerMap.put("received-on",receivedOn);
         asserter.setHeader(headerMap);
     }
 
     @After
     public void tearDown() throws InterruptedException {
         reportingDatabase().deleteMother(caseId);
         reportingDatabase().deleteFLW(flwId);
         reportingDatabase().deleteGroup(groupId);
 
         mrsDatabase().patients().delete(caseId, true);
     }
 
     @Test
     public void createNewAndRegisterMother() throws Exception {
         postNewFormAndAssert();
         postRegistrationFormAndAssert();
     }
 
     private void postNewFormAndAssert() {
         final String new_form = constructRequestTemplateUrl("new_form");
         final String expectedNewFormUrl = constructExpectedUrl("reporting/new_form");
         final String expectedMotherCaseUrl = constructExpectedUrl("reporting/mother_case");
         final String expectedFlwUrl = constructExpectedUrl("reporting/flw");
         final String expectedPatientUrl = constructExpectedUrl("couch/mother_after_new");
 
         placeholderMap.put("instanceId", newFormInstanceId);
 
         asserter.postForm(new_form);
 
         asserter.verifyTable(TableName.new_form, newFormInstanceId, expectedNewFormUrl);
         asserter.verifyTable(TableName.mother_case, caseId, expectedMotherCaseUrl);
         asserter.verifyFlwWithoutGroup(flwId, expectedFlwUrl, groupId);
 
         asserter.verifyCouchPatient(caseId, expectedPatientUrl);
     }
 
     private void postRegistrationFormAndAssert() {
         final String registration_form = constructRequestTemplateUrl("registration_form");
         final String expectedRegistrationFormUrl = constructExpectedUrl("reporting/registration_mother_form");
         final String expectedMotherCaseUrl = constructExpectedUrl("reporting/mother_case");
         final String expectedFlwUrl = constructExpectedUrl("reporting/flw");
         final String expectedPatientUrl = constructExpectedUrl("couch/mother_after_registration");
 
         placeholderMap.put("instanceId", registrationFormInstanceId);
         placeholderMap.put("newFormInstanceId", newFormInstanceId);
 
         asserter.postForm(registration_form);
 
         asserter.verifyTable(TableName.registration_mother_form, registrationFormInstanceId, expectedRegistrationFormUrl);
         asserter.verifyTable(TableName.mother_case, caseId, expectedMotherCaseUrl);
         asserter.verifyFlwWithoutGroup(flwId, expectedFlwUrl, groupId);
 
         asserter.verifyCouchPatient(caseId, expectedPatientUrl, 2);
     }
 
     @Override
     protected String getTestIdentifier() {
         return "createNewAndRegisterMother";
     }
 }
