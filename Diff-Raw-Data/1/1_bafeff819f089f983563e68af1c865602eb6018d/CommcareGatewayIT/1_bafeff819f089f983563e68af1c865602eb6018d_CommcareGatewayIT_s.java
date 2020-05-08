 package org.wv.stepsovc.commcare.gateway;
 
 import fixture.TestFixture;
 import junit.framework.Assert;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.wv.stepsovc.commcare.domain.User;
 import org.wv.stepsovc.commcare.factories.GroupFactory;
 import org.wv.stepsovc.commcare.repository.AllGroups;
 import org.wv.stepsovc.commcare.repository.AllUsers;
 import org.wv.stepsovc.commcare.vo.BeneficiaryInformation;
 import org.wv.stepsovc.commcare.vo.CaregiverInformation;
 import org.wv.stepsovc.commcare.vo.FacilityInformation;
 
 import java.util.HashMap;
 import java.util.UUID;
 
 import static fixture.TestFixture.getBeneficiaryInformation;
 import static fixture.XmlFixture.*;
 import static junit.framework.Assert.assertEquals;
 import static org.wv.stepsovc.commcare.gateway.CommcareGateway.*;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath*:applicationContext-stepsovc-commcare-api.xml")
@Ignore
 public class CommcareGatewayIT {
 
     @Autowired
     CommcareGateway commcareGateway;
     @Autowired
     AllGroups allGroups;
     @Autowired
     AllUsers allUsers;
 
     private User newUser1;
     private User newUser2;
     private String userId1;
     private String userId2;
     private String groupName = "group1";
 
     @Ignore
     public void shouldCreateNewBeneficiary() throws Exception {
         BeneficiaryInformation beneficiaryInformation = getBeneficiaryInformation("7ac0b33f0dac4a81c6d1fbf1bd9dfee0", "ggg2", UUID.randomUUID().toString(), "new-test-case-6", "new-test-case-6", "cg1", null);
         commcareGateway.createCase(beneficiaryInformation);
     }
 
     @Test
     public void shouldConvertObjectToXml() {
         BeneficiaryInformation beneficiaryInformation = getBeneficiaryInformation("f98589102c60fcc2e0f3c422bb361ebd", "cg1", "c7264b49-4e3d-4659-8df3-7316539829cb", "test-case", "XYZ/123", "cg1", "hw1");
         assertConversion(CommcareGateway.BENEFICIARY_FORM_KEY, beneficiaryInformation, BENEFICIARY_CASE_FORM_TEMPLATE_PATH, getExpectedBeneficiaryCaseXml());
         assertConversion(CommcareGateway.BENEFICIARY_FORM_KEY, beneficiaryInformation, OWNER_UPDATE_FORM_TEMPLATE_PATH, getExpectedUpdateOwnerXml());
 
         CaregiverInformation careGiverInformation = TestFixture.createCareGiverInformation();
         assertConversion(CommcareGateway.CARE_GIVER_FORM_KEY, careGiverInformation, USER_REGISTRATION_FORM_TEMPLATE_PATH, getExpectedUserFormXml());
 
         FacilityInformation facilityInformation = TestFixture.createFacilityInformation();
         assertConversion(CommcareGateway.FACILITY_FORM_KEY, facilityInformation, FACILITY_REGISTRATION_FORM_TEMPLATE_PATH, getExpectedFacilityXml());
 
     }
 
     @Test
     public void shouldCreateGroupWithUsers() {
 
         newUser1 = createUser("newUser1");
         newUser2 = createUser("newUser2");
 
         allUsers.add(newUser1);
         allUsers.add(newUser2);
 
         userId1 = allUsers.getUserByName(newUser1.getUsername()).getId();
         userId2 = allUsers.getUserByName(newUser2.getUsername()).getId();
 
         String domainName = "stepsovc";
 
         allGroups.add(GroupFactory.createGroup(groupName, new String[]{userId1, userId2}, domainName));
         Assert.assertNotNull(allGroups.getGroupByName(groupName));
         allGroups.removeByName(groupName);
         allUsers.removeByName(newUser1.getUsername());
         allUsers.removeByName(newUser2.getUsername());
     }
 
     private User createUser(String userName) {
         User newUser = new User();
         newUser.setBase_doc("CouchUser");
         newUser.setDomain("stepsovc");
         newUser.setUsername(userName);
 
         return newUser;
     }
 
     private void assertConversion(String key, Object entity, String formPath, String expectedXML) {
         HashMap<String, Object> model = new HashMap<String, Object>();
         model.put(key, entity);
         String actualXML = commcareGateway.getXmlFromObject(formPath, model);
         assertEquals(expectedXML, actualXML);
     }
 
 
 }
