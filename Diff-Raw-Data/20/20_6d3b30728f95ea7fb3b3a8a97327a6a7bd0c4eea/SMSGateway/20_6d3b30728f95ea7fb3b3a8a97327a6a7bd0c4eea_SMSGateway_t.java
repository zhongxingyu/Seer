 package org.motechproject.ghana.national.repository;
 
 import org.motechproject.MotechException;
 import org.motechproject.cmslite.api.model.ContentNotFoundException;
 import org.motechproject.cmslite.api.service.CMSLiteService;
 import org.motechproject.ghana.national.configuration.CountryCodeAppender;
 import org.motechproject.ghana.national.domain.SMSPayload;
 import org.motechproject.ghana.national.messagegateway.domain.MessageRecipientType;
 import org.motechproject.ghana.national.messagegateway.domain.NextMondayDispatcher;
 import org.motechproject.ghana.national.messagegateway.service.MessageGateway;
 import org.motechproject.sms.api.service.SmsService;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Repository;
 
 import java.util.Locale;
 import java.util.Map;
 
 @Repository
 public class SMSGateway {
     @Autowired
     @Qualifier("messageGateway")
     MessageGateway messageGateway;
 
     @Autowired
     SmsService smsService;
 
     @Autowired
     private CMSLiteService cmsLiteService;
 
     @Autowired
     private CountryCodeAppender countryCodeAppender;
 
     private String getSMSTemplate(String language, String key) {
         try {
             return cmsLiteService.getStringContent(language, key).getValue();
         } catch (ContentNotFoundException e) {
             throw new MotechException(String.format("Encountered error while retrieving SMS template for %s - %s", key, language), e);
         }
     }
 
     public String getSMSTemplate(String key) {
         return getSMSTemplate(defaultLanguage(), key);
     }
 
     public void dispatchSMSToAggregator(String templateKey, Map<String, String> templateValues, String recipientIdentifier, String smsIdentifier, MessageRecipientType messageRecipientType) {
         messageGateway.dispatch(SMSPayload.fromTemplate(getSMSTemplate(templateKey), templateValues, recipientIdentifier, DateUtil.now(), new NextMondayDispatcher(), messageRecipientType), smsIdentifier);
     }
 
     public void dispatchSMS(String templateKey, Map<String, String> templateValues, String phoneNumber) {
         dispatchSMS(phoneNumber, SMSPayload.fill(getSMSTemplate(templateKey), templateValues));
     }
 
     public void dispatchSMS(String templateKey, String language, String phoneNumber) {
         dispatchSMS(phoneNumber, getSMSTemplate(language, templateKey));
     }
 
     public void dispatchSMS(String phoneNumber, String message) {
        if (phoneNumber != null) {
            phoneNumber = applyCountryCode(phoneNumber);
            smsService.sendSMS(phoneNumber, message);
        }
     }
 
     private String applyCountryCode(String phoneNumber) {
         return countryCodeAppender.apply(phoneNumber);
     }
 
     private String defaultLanguage() {
         return Locale.getDefault().getLanguage();
     }
 }
