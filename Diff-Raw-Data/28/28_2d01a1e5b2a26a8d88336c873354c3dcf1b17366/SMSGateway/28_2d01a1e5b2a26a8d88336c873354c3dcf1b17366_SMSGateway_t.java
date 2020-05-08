 package org.motechproject.ghana.national.repository;
 
 import org.motechproject.MotechException;
 import org.motechproject.cmslite.api.model.ContentNotFoundException;
 import org.motechproject.cmslite.api.service.CMSLiteService;
 import org.motechproject.ghana.national.domain.SMSTextComparator;
 import org.motechproject.ghana.national.messagegateway.domain.NextMondayDispatcher;
 import org.motechproject.ghana.national.messagegateway.domain.SMS;
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
 
     public void dispatchSMSToAggregator(String templateKey, Map<String, String> templateValues, String phoneNumber, String identifier) {
         messageGateway.dispatch(SMS.fromTemplate(getSMSTemplate(templateKey), templateValues, phoneNumber, DateUtil.now(), new NextMondayDispatcher(), new SMSTextComparator<String>()), identifier);
     }
 
     public void dispatchSMS(String templateKey, Map<String, String> templateValues, String phoneNumber) {
         smsService.sendSMS(phoneNumber, SMS.fill(getSMSTemplate(templateKey), templateValues));
     }
 
     public void dispatchSMS(String templateKey, String language, String phoneNumber){
         dispatchSMS(phoneNumber, getSMSTemplate(language, templateKey));
     }
 
     public void dispatchSMS(String phoneNumber,String message){
         smsService.sendSMS(phoneNumber,message);
     }
 
     private String defaultLanguage() {
         return Locale.getDefault().getLanguage();
     }
 }
