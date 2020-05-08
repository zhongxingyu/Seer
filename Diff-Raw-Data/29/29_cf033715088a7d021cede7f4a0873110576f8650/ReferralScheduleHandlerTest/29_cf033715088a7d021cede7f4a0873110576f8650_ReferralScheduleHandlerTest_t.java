 package org.wv.stepsovc.core.handlers;
 
import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.motechproject.appointments.api.EventKeys;
 import org.motechproject.cmslite.api.model.ContentNotFoundException;
 import org.motechproject.cmslite.api.model.StringContent;
 import org.motechproject.cmslite.api.service.CMSLiteService;
 import org.motechproject.model.MotechEvent;
 import org.wv.stepsovc.core.domain.Beneficiary;
 import org.wv.stepsovc.core.domain.Facility;
 import org.wv.stepsovc.core.domain.Referral;
 import org.wv.stepsovc.core.mapper.ReferralMapper;
 import org.wv.stepsovc.core.repository.AllBeneficiaries;
 import org.wv.stepsovc.core.repository.AllFacilities;
 import org.wv.stepsovc.core.repository.AllReferrals;
 import org.wv.stepsovc.core.repository.SMSGateway;
 
 import java.util.*;
 
 import static org.apache.commons.lang.StringUtils.join;
import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.wv.stepsovc.core.domain.SmsTemplateKeys.REFERRAL_ALERT;
 import static org.wv.stepsovc.core.mapper.ReferralMapperTest.createCaseForReferral;
 
 public class ReferralScheduleHandlerTest {
     @Mock
     private SMSGateway mockSMSGateway;
     @Mock
     private AllReferrals mockAllReferrals;
 
     @Mock
     private AllFacilities mockAllFacilities;
     @Mock
     private CMSLiteService mockCmsLiteService;
     @Mock
     private AllBeneficiaries mockAllBeneficiaries;
 
     private Referral referral;
     private Facility facility;
     private Beneficiary beneficiary;
     private ReferralScheduleHandler referralScheduleHandler;
 
     @Before
     public void setUp() {
         initMocks(this);
         referralScheduleHandler = new ReferralScheduleHandler(mockSMSGateway, mockAllReferrals, mockAllFacilities, mockCmsLiteService, mockAllBeneficiaries);
         referral = new Referral();
         facility= new Facility();
         beneficiary=new Beneficiary();
     }
 
     @Test
     public void shouldSendSmsToAllFacilityNumbers() throws ContentNotFoundException {
 
         String facilityCode = "code";
         String bencode = "bencode";
         List<String> phoneNumbers = Arrays.asList("989898089", "8980890231");
         referral = new ReferralMapper().map(createCaseForReferral(bencode, "2012-12-3", facilityCode));
         facility.setPhoneNumber(phoneNumbers).setFacilityCode(facilityCode);
         beneficiary.setCode(bencode).setName("test").setCaregiverCode("caregivercode");
 
         when(mockAllReferrals.findActiveByOvcId("someID")).thenReturn(referral);
         when(mockAllFacilities.findFacilityByCode(facilityCode)).thenReturn(facility);
         when(mockAllBeneficiaries.findBeneficiary(bencode)).thenReturn(beneficiary);
 
         StringContent templateString= new StringContent(null, null, "%s (%s) Services (%s)");
         when(mockCmsLiteService.getStringContent(Locale.ENGLISH.getLanguage(),REFERRAL_ALERT)).thenReturn(templateString);
 
         Map<String, Object> parameters= new HashMap<String, Object>();
         parameters.put(EventKeys.EXTERNAL_ID_KEY,"someID");
         MotechEvent motechEvent= new MotechEvent("subject",parameters);
         referralScheduleHandler.handleAlert(motechEvent);
         verify(mockSMSGateway).send(phoneNumbers, "test (bencode) Services (" + join(referral.servicesReferred(), ",") + ")");
     }

    @Test
    public void shouldThrowEventHandlerExceptionOnAnyFailure() {

        when(mockAllReferrals.findActiveByOvcId(anyString())).thenThrow(new RuntimeException("some exception"));
        try {
            referralScheduleHandler.handleAlert(new MotechEvent(""));
            Assert.fail("expected exception here");
        } catch (EventHandlerException ehe) {
        }
    }
 }
