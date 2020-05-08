 package org.wv.stepsovc.web.handlers;
 
 import ch.lambdaj.Lambda;
 import org.apache.log4j.Logger;
 import org.motechproject.aggregator.aggregation.AggregateMotechEvent;
 import org.motechproject.cmslite.api.model.StringContent;
 import org.motechproject.cmslite.api.service.CMSLiteService;
 import org.motechproject.scheduler.domain.MotechEvent;
 import org.motechproject.server.event.annotations.MotechListener;
 import org.motechproject.sms.api.service.SmsService;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.integration.message.GenericMessage;
 import org.springframework.stereotype.Component;
 import org.wv.stepsovc.core.aggregator.SMSMessage;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import static ch.lambdaj.Lambda.joinFrom;
 import static ch.lambdaj.Lambda.on;
 import static java.lang.String.format;
 
 @Component
 public class AggregateSMSEventHandler {
 
     @Autowired
     CMSLiteService cmsLiteService;
    @Qualifier("stepsovcSmsServiceImpl")
     @Autowired
     SmsService smsService;
 
     private Logger logger = Logger.getLogger(this.getClass());
 
     @MotechListener(subjects = {AggregateMotechEvent.SUBJECT})
     public void handleAggregatedSMSByIdentifier(MotechEvent event) {
         try {
             if (((List) event.getParameters().get(AggregateMotechEvent.VALUES_KEY)).size() > 0) {
                 List<SMSMessage> smsMessages = new ArrayList<SMSMessage>();
                 for (GenericMessage<SMSMessage> genericMessage : (List<GenericMessage<SMSMessage>>) event.getParameters().get(AggregateMotechEvent.VALUES_KEY)) {
                     smsMessages.add(genericMessage.getPayload());
                 }
                 aggregateForFacility(smsMessages);
             }
         } catch (Exception e) {
             throw new RuntimeException("problem in handling aggregate sms", e);
         }
     }
 
     private void aggregateForFacility(List<SMSMessage> smsMessages) throws Exception {
         StringContent template = cmsLiteService.getStringContent(Locale.ENGLISH.getLanguage(), smsMessages.get(0).group());
         List<SMSMessage> sortedSMSes = Lambda.sort(smsMessages, on(SMSMessage.class).content());
         String allSMSes = joinFrom(sortedSMSes, SMSMessage.class, ", ").content();
         logger.info("Aggregated Sms : " + allSMSes + ", Due Date:" + smsMessages.get(0).getPatientDueDate());
         smsService.sendSMS(smsMessages.get(0).phoneNumber(), format(template.getValue(), allSMSes, smsMessages.get(0).getPatientDueDate()));
     }
 
 
 }
