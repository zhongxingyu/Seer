 package org.motechproject.ghana.national.messagegateway.service;
 
 import org.motechproject.ghana.national.messagegateway.domain.SMS;
 import org.motechproject.sms.api.service.SmsService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service("smsDispatcher")
 public class SMSDispatcher {
 
     @Autowired
     SmsService smsService;
 
     public void dispatch(SMS sms){
         smsService.sendSMS(sms.getPhoneNumber(),sms.getText());
        System.out.println("SMSksjflkdsjfklkdjsflkjdsflkjdslkfj" + sms.getText() + "----" + sms.getPhoneNumber());
     }
 }
