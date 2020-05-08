 package org.motechproject.ghana.national.handler;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.Period;
 import org.motechproject.cmslite.api.model.ContentNotFoundException;
 import org.motechproject.ghana.national.builder.IVRCallbackUrlBuilder;
 import org.motechproject.ghana.national.builder.IVRRequestBuilder;
 import org.motechproject.ghana.national.builder.RetryRequestBuilder;
 import org.motechproject.ghana.national.domain.ivr.MobileMidwifeAudioClips;
 import org.motechproject.ghana.national.domain.mobilemidwife.Medium;
 import org.motechproject.ghana.national.domain.mobilemidwife.MobileMidwifeEnrollment;
 import org.motechproject.ghana.national.domain.mobilemidwife.PhoneOwnership;
 import org.motechproject.ghana.national.exception.EventHandlerException;
 import org.motechproject.ghana.national.helper.MobileMidwifeWeekCalculator;import org.motechproject.ghana.national.repository.AllPatientsOutbox;
 import org.motechproject.ghana.national.repository.IVRGateway;
 import org.motechproject.ghana.national.repository.SMSGateway;
 import org.motechproject.ghana.national.service.MobileMidwifeService;
 import org.motechproject.ghana.national.service.PatientService;
 import org.motechproject.retry.service.RetryService;
 import org.motechproject.scheduler.domain.MotechEvent;
 import org.motechproject.server.event.annotations.MotechListener;
 import org.motechproject.server.messagecampaign.EventKeys;
 import org.motechproject.util.DateUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.Map;
 
 import static org.joda.time.Days.daysBetween;
 import static org.joda.time.Hours.hoursBetween;
 import static org.joda.time.Minutes.minutesBetween;
 import static org.motechproject.server.messagecampaign.EventKeys.MESSAGE_CAMPAIGN_FIRED_EVENT_SUBJECT;
 import static org.motechproject.util.DateUtil.newDate;
 
 @Component
 public class MobileMidwifeCampaignEventHandler {
 
     private Logger logger = LoggerFactory.getLogger(MobileMidwifeCampaignEventHandler.class);
 
     @Autowired
     private MobileMidwifeService mobileMidwifeService;
     @Autowired
     private AllPatientsOutbox allPatientsOutbox;
     @Autowired
     private SMSGateway smsGateway;
     @Autowired
     private PatientService patientService;
     @Autowired
     private IVRGateway ivrGateway;
     @Autowired
     private IVRCallbackUrlBuilder ivrCallbackUrlBuilder;
     @Autowired
     private RetryService retryService;
 
 
     @MotechListener(subjects = {MESSAGE_CAMPAIGN_FIRED_EVENT_SUBJECT})
     public void sendProgramMessage(MotechEvent event) {
         try {
             Map params = event.getParameters();
             String patientId = (String) params.get(EventKeys.EXTERNAL_ID_KEY);
             LocalDate campaignStartDate = (LocalDate) params.get(EventKeys.CAMPAIGN_START_DATE);
             String repeatInterval = (String) params.get(EventKeys.CAMPAIGN_REPEAT_INTERVAL);
 
             MobileMidwifeEnrollment enrollment = mobileMidwifeService.findActiveBy(patientId);
             Integer startWeek = Integer.parseInt(enrollment.messageStartWeekSpecificToServiceType());
 
             String messageKey = new MobileMidwifeWeekCalculator((String)params.get(EventKeys.CAMPAIGN_NAME_KEY)).getMessageKey(campaignStartDate, startWeek, repeatInterval);
 
            if (event.isLastEvent()) mobileMidwifeService.rollover(patientId, DateUtil.now());
             sendMessage(enrollment, messageKey);
         } catch (Exception e) {
             logger.error("<MobileMidwifeEvent>: Encountered error while sending alert: ", e);
             throw new EventHandlerException(event, e);
         }
     }
 
     private void sendMessage(MobileMidwifeEnrollment enrollment, String messageKey) throws ContentNotFoundException {
         if (Medium.SMS.equals(enrollment.getMedium())) {
             smsGateway.dispatchSMS(messageKey, enrollment.getLanguage().name(), enrollment.getPhoneNumber());
         } else if (Medium.VOICE.equals(enrollment.getMedium())) {
             placeMobileMidwifeMessagesToOutbox(enrollment, messageKey);
             if (!PhoneOwnership.PUBLIC.equals(enrollment.getPhoneOwnership())) {
                 retryService.schedule(RetryRequestBuilder.ivrRetryReqest(enrollment.getPatientId(), DateUtil.now()));
                 ivrGateway.placeCall(enrollment.getPhoneNumber(), IVRRequestBuilder.build(ivrCallbackUrlBuilder.outboundCallUrl(enrollment.getPatientId())));
             }
         }
     }
 
     private void placeMobileMidwifeMessagesToOutbox(MobileMidwifeEnrollment enrollment, String messageKey) {
         allPatientsOutbox.addMobileMidwifeMessage(enrollment.getPatientId(), MobileMidwifeAudioClips.instance(enrollment.getServiceType().getValue(), messageKey), Period.weeks(1));
     }
 }
