 package org.wv.stepsovc.core.services;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.log4j.Logger;
 import org.joda.time.LocalDate;
 import org.motechproject.aggregator.inbound.EventAggregationGateway;
 import org.motechproject.cmslite.api.model.ContentNotFoundException;
 import org.motechproject.cmslite.api.model.StringContent;
 import org.motechproject.cmslite.api.service.CMSLiteService;
 import org.motechproject.model.Time;
 import org.motechproject.sms.api.service.SmsService;
 import org.motechproject.util.StringUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.wv.stepsovc.core.aggregator.SMSMessage;
 import org.wv.stepsovc.core.configuration.VisitNames;
 import org.wv.stepsovc.core.domain.*;
 import org.wv.stepsovc.core.repository.AllBeneficiaries;
 import org.wv.stepsovc.core.repository.AllCaregivers;
 import org.wv.stepsovc.core.repository.AllFacilities;
 import org.wv.stepsovc.core.repository.AllReferrals;
 import org.wv.stepsovc.core.utils.DateUtils;
 
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import static java.lang.String.format;
 import static org.apache.commons.lang.StringUtils.join;
 import static org.motechproject.util.DateUtil.newDateTime;
 import static org.springframework.util.CollectionUtils.isEmpty;
 import static org.wv.stepsovc.core.aggregator.RecipientType.CAREGIVER;
 import static org.wv.stepsovc.core.aggregator.RecipientType.FACILITY;
 import static org.wv.stepsovc.core.aggregator.SMSGroupFactory.group;
 
 @Service
 public class StepsovcAlertService {
 
     @Autowired
     private CMSLiteService cmsLiteService;
     @Autowired
     private AllBeneficiaries allBeneficiaries;
     @Autowired
     private SmsService smsService;
     @Autowired
     private AllFacilities allFacilities;
     @Autowired
     private AllCaregivers allCaregivers;
     @Autowired
     private EventAggregationGateway<SMSMessage> eventAggregationGateway;
     @Autowired
     private AllReferrals allReferrals;
     //This does not have any significance in aggregation time. Change cron expression in aggregator.properties instead.
     private Time preferredAggregateTime = new Time(5, 0);
     private Logger logger = Logger.getLogger(this.getClass());
 
     public void sendInstantReferralAlertToFacility(Referral referral) {
         Facility facility = allFacilities.findFacilityByCode(referral.getFacilityCode());
         if (facility != null && facility.getPhoneNumbers() != null) {
             List<String> phoneNumbers = facility.getPhoneNumbers();
             String message = null;
             try {
                 StringContent stringContent = cmsLiteService.getStringContent(Locale.ENGLISH.getLanguage(), SmsTemplateKeys.IMPENDING_REFERRAL);
                 Beneficiary beneficiary = allBeneficiaries.findBeneficiaryByCode(referral.getBeneficiaryCode());
                 message = String.format(stringContent.getValue(), beneficiary.getName(), beneficiary.getCode(), facility.getFacilityName(), referral.getServiceDate());
                sendSMSToAll(phoneNumbers, message);
             } catch (ContentNotFoundException e) {
                 logger.error("Content for SMS not found - " + SmsTemplateKeys.IMPENDING_REFERRAL, e);
             }
         }
     }
 
 
     public void sendAggregatedReferralAlertToFacility(String ovcId, String window) {
         Referral referral = allReferrals.findActiveByOvcId(ovcId);
         Beneficiary beneficiary = allBeneficiaries.findBeneficiaryByCode(referral.getBeneficiaryCode());
         Facility facility = allFacilities.findFacilityByCode(referral.getFacilityCode());
         List<String> phoneNumbers = facility.getPhoneNumbers();
         if (isEmpty(phoneNumbers)) {
             logger.error("Facility - No Phone Numbers to send SMS.");
         } else {
             try {
                 StringContent stringContent = cmsLiteService.getStringContent(Locale.ENGLISH.getLanguage(), SmsTemplateKeys.BENEFICIARY_WITH_SERVICES);
                 String smsContent = format(stringContent.getValue(), beneficiary.getName(), beneficiary.getCode(), join(referral.referredServiceCodes(), ","));
                 logger.info("Sms Content : " + smsContent + "dateTime :" + preferredAggregateTime);
                 String patientDueDate = new SimpleDateFormat("dd-MMM-yyyy").format(DateUtils.getDate(referral.getServiceDate()));
                 for (String phoneNumber : phoneNumbers) {
                     eventAggregationGateway.dispatch(new SMSMessage(newDateTime(new LocalDate(), preferredAggregateTime), phoneNumber, smsContent, group(VisitNames.REFERRAL, window, FACILITY).key(), patientDueDate));
                 }
             } catch (ContentNotFoundException e) {
                 logger.error("Content for SMS not found - " + SmsTemplateKeys.BENEFICIARY_WITH_SERVICES, e);
             }
         }
     }
 
     public void sendInstantDefaultedAlertToCaregiver(String ovcId) {
         Referral referral = allReferrals.findActiveByOvcId(ovcId);
         Beneficiary beneficiary = allBeneficiaries.findBeneficiaryByCode(referral.getBeneficiaryCode());
         Caregiver caregiver = allCaregivers.findCaregiverById(referral.getCgId());
         String phoneNumber = caregiver.getPhoneNumber();
         if (StringUtil.isNullOrEmpty(phoneNumber)) {
             logger.error("Caregiver - No Phone Numbers to send SMS.");
         } else {
             try {
                 StringContent stringContent = cmsLiteService.getStringContent(Locale.ENGLISH.getLanguage(), SmsTemplateKeys.DEFAULTED_REFERRAL);
                 String smsContent = format(stringContent.getValue(), beneficiary.getName(), beneficiary.getCode(),
                         referral.getFacilityCode(), referral.getServiceDate());
                 logger.info("Sms Content : " + smsContent);
                 smsService.sendSMS(phoneNumber, smsContent);
             } catch (ContentNotFoundException e) {
                 logger.error("Content for SMS not found - " + SmsTemplateKeys.DEFAULTED_REFERRAL, e);
             }
         }
     }
 
     public void sendFollowUpAlertToCaregiver(String ovcId) {
         Referral referral = allReferrals.findActiveByOvcId(ovcId);
         Beneficiary beneficiary = allBeneficiaries.findBeneficiaryByCode(referral.getBeneficiaryCode());
         Caregiver caregiver = allCaregivers.findCaregiverById(referral.getCgId());
         String phoneNumber = caregiver.getPhoneNumber();
         try {
             StringContent stringContent = cmsLiteService.getStringContent(Locale.ENGLISH.getLanguage(), SmsTemplateKeys.BENEFICIARY_WITHOUT_SERVICES);
             String smsContent = format(stringContent.getValue(), beneficiary.getName(), beneficiary.getCode());
             logger.info("Sms Content : " + smsContent);
             eventAggregationGateway.dispatch(new SMSMessage(newDateTime(new LocalDate(), preferredAggregateTime), phoneNumber,
                     smsContent, group(VisitNames.FOLLOW_UP, "", CAREGIVER).key(), null));
         } catch (ContentNotFoundException e) {
             logger.error("Content for SMS not found - " + SmsTemplateKeys.BENEFICIARY_WITHOUT_SERVICES, e);
         }
     }
 
     public void sendInstantServiceUnavailabilityMsgToCareGivers(List<Referral> referrals, String facilityCode, String unavailableFromDate,
                                                                 String unavailableToDate, String nextAvailableDate) {
         Set<String> phoneNumbers = new HashSet<String>();
         for (Referral referral : referrals) {
             Caregiver caregiver = allCaregivers.findCaregiverById(referral.getCgId());
             if (caregiver != null) {
                 phoneNumbers.add(caregiver.getPhoneNumber());
             }
         }
         List<Caregiver> caregiversForFacility = allCaregivers.findCaregiverByFacilityCode(facilityCode);
 
         if (CollectionUtils.isNotEmpty(caregiversForFacility))
             for (Caregiver caregiver : caregiversForFacility) {
                 phoneNumbers.add(caregiver.getPhoneNumber());
             }
         String smsContent = getServiceUnavailableAlertMsg(facilityCode, unavailableFromDate, unavailableToDate, nextAvailableDate);
         if (CollectionUtils.isNotEmpty(phoneNumbers))
            sendSMSToAll(new ArrayList<>(phoneNumbers), smsContent);
    }

    private void sendSMSToAll(List<String> recipients, String smsContent) {
        for (String recipient : recipients) {
            smsService.sendSMS(recipient, smsContent);
        }
     }
 
     public void sendInstantServiceUnavailabilityMsgToCareGiverOfReferral(String caregiverId, String facilityCode, String unavailableFromDate,
                                                                          String unavailableToDate, String nextAvailableDate) {
         Caregiver caregiver = allCaregivers.findCaregiverById(caregiverId);
         String smsContent = getServiceUnavailableAlertMsg(facilityCode, unavailableFromDate, unavailableToDate, nextAvailableDate);
         if (caregiver != null && smsContent != null) {
             smsService.sendSMS(caregiver.getPhoneNumber(), smsContent);
         }
     }
 
     private String getServiceUnavailableAlertMsg(String facilityCode, String unavailableFromDate, String unavailableToDate, String nextAvailableDate) {
         String smsContent = null;
         try {
             StringContent stringContent = cmsLiteService.getStringContent(Locale.ENGLISH.getLanguage(), SmsTemplateKeys.FACILITY_SERVICE_UNAVAILABLE);
             smsContent = format(stringContent.getValue(), facilityCode, unavailableFromDate, unavailableToDate, nextAvailableDate);
             logger.info("Sms Content : " + smsContent);
         } catch (ContentNotFoundException e) {
             logger.error("Content for SMS not found - " + SmsTemplateKeys.FACILITY_SERVICE_UNAVAILABLE, e);
         }
         return smsContent;
     }
 
 }
