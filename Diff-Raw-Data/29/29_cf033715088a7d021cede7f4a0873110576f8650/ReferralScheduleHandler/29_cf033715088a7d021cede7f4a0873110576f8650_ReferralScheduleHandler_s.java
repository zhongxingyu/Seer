 package org.wv.stepsovc.core.handlers;
 
 import org.motechproject.appointments.api.EventKeys;
 import org.motechproject.cmslite.api.model.ContentNotFoundException;
 import org.motechproject.cmslite.api.model.StringContent;
 import org.motechproject.cmslite.api.service.CMSLiteService;
 import org.motechproject.model.MotechEvent;
 import org.motechproject.server.event.annotations.MotechListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.wv.stepsovc.core.domain.Beneficiary;
 import org.wv.stepsovc.core.domain.Facility;
 import org.wv.stepsovc.core.domain.Referral;
 import org.wv.stepsovc.core.repository.AllBeneficiaries;
 import org.wv.stepsovc.core.repository.AllFacilities;
 import org.wv.stepsovc.core.repository.AllReferrals;
 import org.wv.stepsovc.core.repository.SMSGateway;
 
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import static java.lang.String.*;
 import static org.apache.commons.lang.StringUtils.join;
 import static org.springframework.util.CollectionUtils.isEmpty;
 import static org.wv.stepsovc.core.domain.SmsTemplateKeys.REFERRAL_ALERT;
 
 @Component
 public class ReferralScheduleHandler {
 
     private Logger logger = LoggerFactory.getLogger(ReferralScheduleHandler.class);
     private SMSGateway smsGateway;
     private AllReferrals allReferrals;
     private AllFacilities allFacilities;
     private CMSLiteService cmsLiteService;
     private AllBeneficiaries allBeneficiaries;
 
     @Autowired
     public ReferralScheduleHandler(SMSGateway gateway, AllReferrals referrals, AllFacilities allFacilities, CMSLiteService cmsLiteService, AllBeneficiaries allBeneficiaries) {
         this.smsGateway = gateway;
         this.allReferrals = referrals;
         this.allFacilities = allFacilities;
         this.cmsLiteService = cmsLiteService;
         this.allBeneficiaries = allBeneficiaries;
     }
 
     @MotechListener(subjects = {EventKeys.APPOINTMENT_REMINDER_EVENT_SUBJECT})
     public void handleAlert(MotechEvent motechEvent) {
         try {
             sendSMSToFacilityForAnAppointment(REFERRAL_ALERT, motechEvent);
         } catch (Exception e) {
            logger.error("<Appointment Alert Exception>: Encountered error while sending alert: ", e);
             throw new EventHandlerException(motechEvent, e);
         }
     }
 
     private void sendSMSToFacilityForAnAppointment(String smsKey, MotechEvent motechEvent) throws ContentNotFoundException {
 
         Map<String, Object> parameters = motechEvent.getParameters();
         Referral referral = allReferrals.findActiveByOvcId((String) parameters.get(EventKeys.EXTERNAL_ID_KEY));
         Facility facility = allFacilities.findFacilityByCode(referral.getFacilityCode());
         Beneficiary beneficiary = allBeneficiaries.findBeneficiary(referral.getBeneficiaryCode());
 
         List<String> phoneNumbers = facility.getPhoneNumbers();
         if (isEmpty(phoneNumbers)) {
             logger.warn("No Phone Numbers to send SMS.");
         } else {
             StringContent smsTemplate = cmsLiteService.getStringContent(Locale.ENGLISH.getLanguage(), smsKey);
             String smsContent = format(smsTemplate.getValue(), beneficiary.getName(), beneficiary.getCode(), join(referral.servicesReferred(), ","));
             smsGateway.send(phoneNumbers, smsContent);
         }
     }
 }
