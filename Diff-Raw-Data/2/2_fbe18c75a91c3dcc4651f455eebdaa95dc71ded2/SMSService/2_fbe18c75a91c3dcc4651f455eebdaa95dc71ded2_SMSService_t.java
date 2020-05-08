 package org.motechproject.ghana.telco.service;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.motechproject.ghana.telco.domain.SMSAudit;
 import org.motechproject.ghana.telco.domain.dto.SMSServiceRequest;
 import org.motechproject.ghana.telco.domain.dto.SMSServiceResponse;
 import org.motechproject.ghana.telco.repository.AllSMSAudits;
 import org.motechproject.ghana.telco.sms.SMSProvider;
 import org.motechproject.model.Time;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class SMSService {
     private final static Logger log = Logger.getLogger(SMSService.class);
     private SMSProvider smsProvider;
     private AllSMSAudits allSMSAudits;
 
     @Autowired
     public SMSService(SMSProvider smsProvider, AllSMSAudits allSMSAudits) {
         this.smsProvider = smsProvider;
         this.allSMSAudits = allSMSAudits;
     }
 
     public SMSServiceResponse send(SMSServiceRequest request) {
         String mobileNumber = request.getMobileNumber();
         String message = request.getMessage();
         String program = request.programKey();
         DateTime now = DateUtil.now();
 
         Time deliveryTime = request.getDeliveryTime();
         smsProvider.send(mobileNumber, message, deliveryTime);
         log.info("Subscriber: " + mobileNumber + ":" + message + " : @" + now);
 
        allSMSAudits.add(new SMSAudit(mobileNumber, program, now, message, (deliveryTime != null) ? deliveryTime.toString() : null));
         return new SMSServiceResponse();
     }
 }
