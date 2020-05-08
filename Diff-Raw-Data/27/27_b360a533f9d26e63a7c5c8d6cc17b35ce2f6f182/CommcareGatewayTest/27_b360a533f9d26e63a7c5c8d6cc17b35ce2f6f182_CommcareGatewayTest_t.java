 package org.wv.stepsovc.commcare.gateway;
 
 import org.apache.velocity.app.VelocityEngine;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.http.client.service.HttpClientService;
 import org.springframework.test.util.ReflectionTestUtils;
 import org.wv.stepsovc.commcare.domain.CaseType;
 import org.wv.stepsovc.commcare.domain.Group;
 import org.wv.stepsovc.commcare.repository.AllGroups;
 import org.wv.stepsovc.commcare.repository.AllUsers;
 import org.wv.stepsovc.commcare.vo.BeneficiaryInformation;
 import org.wv.stepsovc.commcare.vo.CaregiverInformation;
 import org.wv.stepsovc.commcare.vo.FacilityInformation;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import static fixture.XmlFixture.*;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.wv.stepsovc.commcare.gateway.CommcareGateway.*;
 
 
 public class CommcareGatewayTest {
 
     CommcareGateway commcareGateway;
 
     @Mock
     HttpClientService mockHttpClientService;
 
     @Mock
     VelocityEngine mockVelocityEngine;
 
     @Mock
     AllGroups allGroups;
 
     @Mock
     private AllUsers allUsers;
 
     private Map<String, Object> model;
 
     private CommcareGateway spyCommcareGateway;
 
    private String someUrl;

 
     @Before
     public void setup() {
         initMocks(this);
        someUrl = "http://localhost:8000";
        commcareGateway = new CommcareGateway();
         spyCommcareGateway = spy(commcareGateway);
         ReflectionTestUtils.setField(spyCommcareGateway, "httpClientService", mockHttpClientService);
         ReflectionTestUtils.setField(spyCommcareGateway, "velocityEngine", mockVelocityEngine);
         ReflectionTestUtils.setField(spyCommcareGateway, "allGroups", allGroups);
         ReflectionTestUtils.setField(spyCommcareGateway, "allUsers", allUsers);
        ReflectionTestUtils.setField(spyCommcareGateway, "COMMCARE_RECIEVER_URL", someUrl);
         model = new HashMap<String, Object>();
 
     }
 
 
     @Test
     public void shouldSubmitBeneficiaryCaseForm() throws Exception {
         BeneficiaryInformation beneficiaryInformation = getBeneficiaryInformation("f98589102c60fcc2e0f3c422bb361ebd", "cg1", UUID.randomUUID().toString(), "Albie-case", "ABC", "cg1", "null");
         model.put(CommcareGateway.BENEFICIARY_FORM_KEY, beneficiaryInformation);
         doReturn(getExpectedBeneficiaryCaseXml()).when(spyCommcareGateway).getXmlFromObject(eq(BENEFICIARY_CASE_FORM_TEMPLATE_PATH), eq(model));
 
         spyCommcareGateway.createCase(beneficiaryInformation);
 
        verify(mockHttpClientService).post(someUrl, getExpectedBeneficiaryCaseXml());
     }
 
     @Test
     public void shouldCreateCareGiverInCommcare() {
         CaregiverInformation careGiverInformation = getCareGiverInformation("7ac0b33f0dac4a81c6d1fbf1bd9dfee0", "EW/123", "9089091");
         model.put(CommcareGateway.CARE_GIVER_FORM_KEY, careGiverInformation);
         doReturn(getExpectedUserFormXml()).when(spyCommcareGateway).getXmlFromObject(eq(USER_REGISTRATION_FORM_TEMPLATE_PATH), eq(model));
         spyCommcareGateway.registerCaregiver(careGiverInformation);
        verify(mockHttpClientService).post(someUrl, getExpectedUserFormXml());
     }
 
 
     @Test
     public void shouldSubmitUpdateOwnerFormForGroupOwnershipRequest() {
         BeneficiaryInformation beneficiaryInformation = getBeneficiaryInformation("f98589102c60fcc2e0f3c422bb361ebd", "cg1", UUID.randomUUID().toString(), "Albie-case", "ABC", "cg1", "null");
         String currentOwnerId = beneficiaryInformation.getCareGiverId();
         model.put(CommcareGateway.BENEFICIARY_FORM_KEY, beneficiaryInformation);
         doReturn(getExpectedBeneficiaryCaseXml()).when(spyCommcareGateway).getXmlFromObject(eq(OWNER_UPDATE_FORM_TEMPLATE_PATH), eq(model));
         String someGroup = "someGroup";
         Group group = new Group();
         group.setId("somegroupId");
         doReturn(group).when(allGroups).getGroupByName(someGroup);
         ArgumentCaptor<BeneficiaryInformation> beneficiaryInfoCaptor = ArgumentCaptor.forClass(BeneficiaryInformation.class);
 
         spyCommcareGateway.addGroupOwnership(beneficiaryInformation, someGroup);
 
         verify(spyCommcareGateway).postOwnerUpdate(beneficiaryInfoCaptor.capture());
        verify(mockHttpClientService).post(someUrl, getExpectedBeneficiaryCaseXml());
 
         assertThat(beneficiaryInfoCaptor.getValue().getOwnerId(), is(currentOwnerId + "," + group.getId()));
     }
 
     @Test
     public void shouldSendOwnershipUpdateXmlForAddUserOwnershipRequest() throws Exception {
         BeneficiaryInformation beneficiaryInformation = getBeneficiaryInformation("f98589102c60fcc2e0f3c422bb361ebd", "cg1", UUID.randomUUID().toString(), "Albie-case", "ABC", "cg1", "null");
         String currentOwnerId = beneficiaryInformation.getCareGiverId();
         model.put(CommcareGateway.BENEFICIARY_FORM_KEY, beneficiaryInformation);
         doReturn(getExpectedBeneficiaryCaseXml()).when(spyCommcareGateway).getXmlFromObject(eq(OWNER_UPDATE_FORM_TEMPLATE_PATH), eq(model));
         String userId = "userId";
         ArgumentCaptor<BeneficiaryInformation> beneficiaryInfoCaptor = ArgumentCaptor.forClass(BeneficiaryInformation.class);
 
         spyCommcareGateway.addUserOwnership(beneficiaryInformation, userId);
 
         verify(spyCommcareGateway).postOwnerUpdate(beneficiaryInfoCaptor.capture());
         verify(mockHttpClientService).post(spyCommcareGateway.getCOMMCARE_RECIEVER_URL(), getExpectedBeneficiaryCaseXml());
 
         assertThat(beneficiaryInfoCaptor.getValue().getOwnerId(), is(currentOwnerId + "," + userId));
 
     }
 
     @Test
     public void shouldRegisterFacility() {
         FacilityInformation facilityInformation = new FacilityInformation();
         model.put(FACILITY_FORM_KEY, facilityInformation);
         doReturn(getExpectedFacilityXml()).when(spyCommcareGateway).getXmlFromObject(FACILITY_REGISTRATION_FORM_TEMPLATE_PATH, model);
         spyCommcareGateway.registerFacility(facilityInformation);
         verify(mockHttpClientService).post(spyCommcareGateway.getCOMMCARE_RECIEVER_URL(), getExpectedFacilityXml());
 
     }
 
 
     private CaregiverInformation getCareGiverInformation(String cgId, String cgCode, String phoneNumber) {
         CaregiverInformation caregiverInfo = new CaregiverInformation();
         caregiverInfo.setCaregiverId(cgId);
         caregiverInfo.setCaregiverCode(cgCode);
         caregiverInfo.setFirstName("fName");
         caregiverInfo.setMiddleName("mName");
         caregiverInfo.setLastName("lName");
         caregiverInfo.setGender("male");
         caregiverInfo.setPhoneNumber(phoneNumber);
         return caregiverInfo;
     }
 
     private BeneficiaryInformation getBeneficiaryInformation(String caregiverId, String caregiverCode, String caseId, String caseName, String beneficiaryCode, String caregiverName, String ownerId) {
         BeneficiaryInformation beneficiaryInformation = new BeneficiaryInformation();
         beneficiaryInformation.setBeneficiaryId(caseId);
         beneficiaryInformation.setBeneficiaryCode(beneficiaryCode);
         beneficiaryInformation.setBeneficiaryName(caseName);
         beneficiaryInformation.setBeneficiaryDob("12-12-1988");
         beneficiaryInformation.setBeneficiarySex("male");
         beneficiaryInformation.setBeneficiaryTitle("MR");
         beneficiaryInformation.setReceivingOrganization("XAQ");
         beneficiaryInformation.setCareGiverCode(caregiverCode);
         beneficiaryInformation.setCareGiverId(caregiverId);
         beneficiaryInformation.setCareGiverName(caregiverName);
         beneficiaryInformation.setCaseType(CaseType.BENEFICIARY_CASE.getType());
         beneficiaryInformation.setDateModified("2012-05-02T22:18:45.071+05:30");
         beneficiaryInformation.setOwnerId(ownerId);
         return beneficiaryInformation;
     }
 
 
 }
