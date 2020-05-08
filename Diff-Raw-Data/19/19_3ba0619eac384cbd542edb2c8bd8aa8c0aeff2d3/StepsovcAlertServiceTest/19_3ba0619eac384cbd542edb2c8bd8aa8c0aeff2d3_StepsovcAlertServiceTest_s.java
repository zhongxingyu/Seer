 package org.wv.stepsovc.core.services;
 
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.aggregator.inbound.EventAggregationGateway;
 import org.motechproject.cmslite.api.model.ContentNotFoundException;
 import org.motechproject.cmslite.api.model.StringContent;
 import org.motechproject.cmslite.api.service.CMSLiteService;
 import org.motechproject.sms.api.service.SmsService;
 import org.springframework.test.util.ReflectionTestUtils;
 import org.wv.stepsovc.core.aggregator.SMSMessage;
 import org.wv.stepsovc.core.domain.*;
 import org.wv.stepsovc.core.mapper.ReferralMapper;
 import org.wv.stepsovc.core.repository.AllBeneficiaries;
 import org.wv.stepsovc.core.repository.AllCaregivers;
 import org.wv.stepsovc.core.repository.AllFacilities;
 import org.wv.stepsovc.core.repository.AllReferrals;
 import org.wv.stepsovc.core.request.StepsovcCase;
 
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.isIn;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.util.DateUtil.newDateTime;
 import static org.wv.stepsovc.core.domain.SmsTemplateKeys.BENEFICIARY_WITH_SERVICES;
 import static org.wv.stepsovc.core.domain.SmsTemplateKeys.FACILITY_SERVICE_UNAVAILABLE;
 import static org.wv.stepsovc.core.fixtures.StepsovcCaseFixture.createCaseForReferral;
 
 public class StepsovcAlertServiceTest {
     @Mock
     private AllReferrals mockAllReferrals;
     @Mock
     private AllFacilities mockAllFacilities;
     @Mock
     private CMSLiteService mockCmsLiteService;
     @Mock
     private AllBeneficiaries mockAllBeneficiaries;
     @Mock
     private EventAggregationGateway<SMSMessage> mockEventAggregationGateway;
     @Mock
     private SmsService mockSmsService;
     @Mock
     private AllCaregivers mockAllCaregivers;
 
     private StepsovcAlertService stepsovcAlertService;
     private Referral referral;
     private Facility facility;
     private Beneficiary beneficiary;
 
     @Before
     public void setUp() {
         initMocks(this);
         stepsovcAlertService = new StepsovcAlertService();
         ReflectionTestUtils.setField(stepsovcAlertService, "allCaregivers", mockAllCaregivers);
         ReflectionTestUtils.setField(stepsovcAlertService, "allBeneficiaries", mockAllBeneficiaries);
         ReflectionTestUtils.setField(stepsovcAlertService, "allFacilities", mockAllFacilities);
         ReflectionTestUtils.setField(stepsovcAlertService, "allReferrals", mockAllReferrals);
         ReflectionTestUtils.setField(stepsovcAlertService, "cmsLiteService", mockCmsLiteService);
         ReflectionTestUtils.setField(stepsovcAlertService, "eventAggregationGateway", mockEventAggregationGateway);
         ReflectionTestUtils.setField(stepsovcAlertService, "smsService", mockSmsService);
         referral = new Referral();
         facility = new Facility();
         beneficiary = new Beneficiary();
         referral.setBeneficiaryCode("someBenCode");
         referral.setServiceDate("2013-12-12");
         referral.setFacilityCode("FAC001");
         beneficiary.setName("Smith");
         beneficiary.setCode("BEN001");
     }
 
     @Test
     public void shouldSendSmsToAllFacilityNumbers() throws ContentNotFoundException {
 
         String facilityCode = "code";
         String bencode = "bencode";
         List<String> phoneNumbers = Arrays.asList("989898089", "8980890231");
         referral = new ReferralMapper().map(createCaseForReferral(bencode, "2012-12-3", facilityCode));
         facility.setPhoneNumber(phoneNumbers).setFacilityCode(facilityCode);
         beneficiary.setCode(bencode).setName("test").setCaregiverCode("caregivercode");
 
         String externalId = "someID";
         when(mockAllReferrals.findActiveByOvcId(externalId)).thenReturn(referral);
         when(mockAllFacilities.findFacilityByCode(facilityCode)).thenReturn(facility);
         when(mockAllBeneficiaries.findBeneficiaryByCode(bencode)).thenReturn(beneficiary);
 
         StringContent templateString = new StringContent(null, null, "%s (%s) Services (%s)");
         when(mockCmsLiteService.getStringContent(Locale.ENGLISH.getLanguage(), BENEFICIARY_WITH_SERVICES)).thenReturn(templateString);
 
         stepsovcAlertService.sendAggregatedReferralAlertToFacility(externalId, "due");
         ArgumentCaptor<SMSMessage> smsCaptor = ArgumentCaptor.forClass(SMSMessage.class);
         verify(mockEventAggregationGateway, times(2)).dispatch(smsCaptor.capture());
         assertThat(smsCaptor.getAllValues().size(), is(2));
         assertThat(smsCaptor.getAllValues().get(0).phoneNumber(), isIn(phoneNumbers));
         assertThat(smsCaptor.getAllValues().get(1).phoneNumber(), isIn(phoneNumbers));
         assertThat(smsCaptor.getAllValues().get(0).content(), is("test (bencode) Services (001,003,004,006,007,008,009,010)"));
         assertThat(smsCaptor.getAllValues().get(1).content(), is("test (bencode) Services (001,003,004,006,007,008,009,010)"));
     }
 
     @Test
     public void shouldSendSMSToCareGiverForDefaultedReferral() throws ContentNotFoundException {
         String externalId = "ovcId";
         String caregiverId = "caregiverId";
         String phoneNumber = "9812345678";
         String msg = "Smith (BEN001) has not fulfilled the referral to FAC001 due on 2013-12-12";
         StringContent template = new StringContent("", "", "%s (%s) has not fulfilled the referral to %s due on %s");
         referral.setCgId(caregiverId);
         Caregiver caregiver = new Caregiver();
         caregiver.setPhoneNumber(phoneNumber);
 
         doReturn(referral).when(mockAllReferrals).findActiveByOvcId(externalId);
         doReturn(beneficiary).when(mockAllBeneficiaries).findBeneficiaryByCode(referral.getBeneficiaryCode());
         doReturn(caregiver).when(mockAllCaregivers).findCaregiverById(caregiverId);
         doReturn(template).when(mockCmsLiteService).getStringContent("en", SmsTemplateKeys.DEFAULTED_REFERRAL);
         stepsovcAlertService.sendInstantDefaultedAlertToCaregiver(externalId);
         verify(mockSmsService).sendSMS(phoneNumber, msg);
 
     }
 
     @Test
     public void shouldSendInstantReferralAlertSMSToFacilityNumbers() throws ContentNotFoundException {
 
         String benCode = "BEN001";
         String serviceDate = "2012-05-30";
         StepsovcCase stepsovcCase = org.wv.stepsovc.core.fixtures.StepsovcCaseFixture.createCaseForReferral(benCode, serviceDate, "FAC001");
         stepsovcCase.setBeneficiary_code(benCode);
         Referral referral = new ReferralMapper().map(stepsovcCase);
 
         String ovcId = "someOvcId";
         referral.setOvcId(ovcId);
         Facility facility = new Facility();
         Beneficiary beneficiary = new Beneficiary();
         List<String> phoneNumbers = Arrays.asList("0123456789", "1234555456");
         facility.setPhoneNumber(phoneNumbers);
         facility.setFacilityName("FAC001");
         beneficiary.setName("Ben Name");
         beneficiary.setCode(benCode);
 
         doReturn(facility).when(mockAllFacilities).findFacilityByCode(referral.getFacilityCode());
         doReturn(beneficiary).when(mockAllBeneficiaries).findBeneficiaryByCode(benCode);
 
         StringContent template = new StringContent(Locale.ENGLISH.getLanguage(), SmsTemplateKeys.IMPENDING_REFERRAL, "%s (%s) will be coming to your facility %s on %s. Please make the necessary arrangements.");
         doReturn(template).when(mockCmsLiteService).getStringContent(Locale.ENGLISH.getLanguage(), SmsTemplateKeys.IMPENDING_REFERRAL);
 
         stepsovcAlertService.sendInstantReferralAlertToFacility(referral);
 
        verify(mockSmsService).sendSMS(phoneNumbers, "Ben Name (BEN001) will be coming to your facility FAC001 on 2012-05-30. Please make the necessary arrangements.");
 
     }
 
     @Test
     public void shouldSendFollowUpAlertToCaregiver() throws Exception {
         String externalId = "someOvcId";
         String benCode = "someBenCode";
         String cgId = "someCgId";
         Referral toBeReturned = getReferral(cgId);
         toBeReturned.setBeneficiaryCode(benCode);
         Caregiver caregiver = new Caregiver();
         caregiver.setPhoneNumber("somePhoneNumber");
         Beneficiary beneficiary1 = new Beneficiary();
         beneficiary1.setCode("someCode");
         beneficiary1.setName("someName");
 
         doReturn(toBeReturned).when(mockAllReferrals).findActiveByOvcId(externalId);
         doReturn(caregiver).when(mockAllCaregivers).findCaregiverById(cgId);
         doReturn(beneficiary1).when(mockAllBeneficiaries).findBeneficiaryByCode(benCode);
         doReturn(new StringContent("", "", "%s (%s)")).when(mockCmsLiteService).getStringContent("en", SmsTemplateKeys.BENEFICIARY_WITHOUT_SERVICES);
 
         ArgumentCaptor<SMSMessage> smsMessageCaptor = ArgumentCaptor.forClass(SMSMessage.class);
         stepsovcAlertService.sendFollowUpAlertToCaregiver(externalId);
         verify(mockEventAggregationGateway).dispatch(smsMessageCaptor.capture());
 
         assertThat(smsMessageCaptor.getValue().phoneNumber(), is(caregiver.getPhoneNumber()));
         assertThat(smsMessageCaptor.getValue().content(), is("someName (someCode)"));
         assertThat(smsMessageCaptor.getValue().group(), is("G-CAREGIVER-FollowUp-"));
         assertThat(smsMessageCaptor.getValue().deliveryTime(), is(newDateTime(new LocalDate(), 5, 0, 0)));
     }
 
     @Test
     public void shouldSendInstantServiceUnavailabilityMsgToCareGivers() throws ContentNotFoundException {
         String facilityCode = "FAC001";
 
         String cg1 = "CG1";
         String cg2 = "CG2";
         String cg3 = "CG3";
         String cg4 = "CG4";
         String cg5 = "CG5";
 
         String phoneNumber1 = "111111";
         String phoneNumber2 = "222222";
         String phoneNumber3 = "333333";
         String phoneNumber4 = "444444";
         String phoneNumber5 = "555555";
 
         List<Caregiver> caregiversForFacility = Arrays.asList(getCaregiver(cg3, phoneNumber3), getCaregiver(cg4, phoneNumber4), getCaregiver(cg5, phoneNumber5));
         doReturn(caregiversForFacility).when(mockAllCaregivers).findCaregiverByFacilityCode(facilityCode);
 
         doReturn(getCaregiver(cg1, phoneNumber1)).when(mockAllCaregivers).findCaregiverById(cg1);
         doReturn(getCaregiver(cg2, phoneNumber2)).when(mockAllCaregivers).findCaregiverById(cg2);
         doReturn(getCaregiver(cg3, phoneNumber3)).when(mockAllCaregivers).findCaregiverById(cg3);
 
         doReturn(new StringContent("", "", "%s will be closed from %s to %s. Referral moved to %s.")).when(mockCmsLiteService)
                 .getStringContent(Locale.ENGLISH.getLanguage(), FACILITY_SERVICE_UNAVAILABLE);
 
         List<Referral> referrals = Arrays.asList(getReferral(cg1), getReferral(cg2), getReferral(cg3));
         stepsovcAlertService.sendInstantServiceUnavailabilityMsgToCareGivers(referrals,
                 facilityCode, "2012-12-12", "2012-12-12", "2012-12-13");
 
        ArgumentCaptor<ArrayList> phoneNumberCaptor = ArgumentCaptor.forClass(ArrayList.class);
        verify(mockSmsService).sendSMS(phoneNumberCaptor.capture(), eq(facilityCode + " will be closed from 2012-12-12 to 2012-12-12. Referral moved to 2012-12-13."));
        List phoneNumbers = phoneNumberCaptor.getAllValues().get(0);
         assertThat(phoneNumbers.size(), is(5));
         assertThat(phoneNumber1, isIn(phoneNumbers));
         assertThat(phoneNumber2, isIn(phoneNumbers));
         assertThat(phoneNumber3, isIn(phoneNumbers));
         assertThat(phoneNumber4, isIn(phoneNumbers));
         assertThat(phoneNumber5, isIn(phoneNumbers));
     }
 
     @Test
     public void shouldHandleNilCaregiverAssociationToFacilityWhileServiceUnavailability() throws ContentNotFoundException {
         String facilityCode = "FAC001";
 
         String cg1 = "CG1";
         String cg2 = "CG2";
         String cg3 = "CG3";
         String cg4 = "CG4";
         String cg5 = "CG5";
 
         String phoneNumber1 = "111111";
         String phoneNumber2 = "222222";
         String phoneNumber3 = "333333";
         String phoneNumber4 = "444444";
         String phoneNumber5 = "555555";
 
         List<Caregiver> caregiversForFacility = Arrays.asList(getCaregiver(cg3, phoneNumber3), getCaregiver(cg4, phoneNumber4), getCaregiver(cg5, phoneNumber5));
         doReturn(null).when(mockAllCaregivers).findCaregiverByFacilityCode(facilityCode);
 
         doReturn(getCaregiver(cg1, phoneNumber1)).when(mockAllCaregivers).findCaregiverById(cg1);
         doReturn(getCaregiver(cg2, phoneNumber2)).when(mockAllCaregivers).findCaregiverById(cg2);
         doReturn(getCaregiver(cg3, phoneNumber3)).when(mockAllCaregivers).findCaregiverById(cg3);
 
         doReturn(new StringContent("", "", "%s will be closed from %s to %s. Referral moved to %s.")).when(mockCmsLiteService)
                 .getStringContent(Locale.ENGLISH.getLanguage(), FACILITY_SERVICE_UNAVAILABLE);
 
         List<Referral> referrals = Arrays.asList(getReferral(cg1), getReferral(cg2), getReferral(cg3));
         stepsovcAlertService.sendInstantServiceUnavailabilityMsgToCareGivers(referrals,
                 facilityCode, "2012-12-12", "2012-12-12", "2012-12-13");
 
        ArgumentCaptor<ArrayList> phoneNumberCaptor = ArgumentCaptor.forClass(ArrayList.class);
        verify(mockSmsService).sendSMS(phoneNumberCaptor.capture(), eq(facilityCode + " will be closed from 2012-12-12 to 2012-12-12. Referral moved to 2012-12-13."));
        List phoneNumbers = phoneNumberCaptor.getAllValues().get(0);
         assertThat(phoneNumbers.size(), is(3));
         assertThat(phoneNumber1, isIn(phoneNumbers));
         assertThat(phoneNumber2, isIn(phoneNumbers));
         assertThat(phoneNumber3, isIn(phoneNumbers));
 
     }
 
     @Test
     public void shouldSendInstantServiceUnavailableMsgToGivenCaregiver() throws ContentNotFoundException {
         String caregiverId = "CG1";
         String facilityCode = "FAC001";
         String phoneNumber = "11111";
         String unavailableFromDate = "2012-12-12";
         String unavailableToDate = "2012-12-13";
         String nextAvailableDate = "2012-12-14";
         doReturn(getCaregiver(caregiverId, phoneNumber)).when(mockAllCaregivers).findCaregiverById(caregiverId);
         doReturn(new StringContent("", "", "%s will be closed from %s to %s. Referral moved to %s.")).when(mockCmsLiteService)
                 .getStringContent(Locale.ENGLISH.getLanguage(), FACILITY_SERVICE_UNAVAILABLE);
 
         stepsovcAlertService.sendInstantServiceUnavailabilityMsgToCareGiverOfReferral(caregiverId, facilityCode, unavailableFromDate, unavailableToDate, nextAvailableDate);
 
         String expectedMsg = facilityCode + " will be closed from " + unavailableFromDate + " to " + unavailableToDate + ". Referral moved to " + nextAvailableDate + ".";
         verify(mockSmsService).sendSMS(phoneNumber, expectedMsg);
     }
 
     private Caregiver getCaregiver(String caregiverId, String phoneNumber) {
         Caregiver caregiver3 = new Caregiver();
         caregiver3.setCgId(caregiverId);
         caregiver3.setPhoneNumber(phoneNumber);
         return caregiver3;
     }
 
     private Referral getReferral(String cg1) {
         Referral referral1 = new Referral();
         referral1.setCgId(cg1);
         return referral1;
     }
 
 }
