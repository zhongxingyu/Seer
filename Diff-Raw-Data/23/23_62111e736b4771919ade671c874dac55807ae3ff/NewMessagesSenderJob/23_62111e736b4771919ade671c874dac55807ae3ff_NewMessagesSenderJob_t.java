 package org.motechproject.ananya.kilkari.obd.scheduler;
 
 import org.joda.time.DateTime;
 import org.motechproject.ananya.kilkari.obd.service.CampaignMessageService;
 import org.motechproject.ananya.kilkari.obd.service.OBDProperties;
 import org.motechproject.event.MotechEvent;
 import org.motechproject.event.annotations.MotechListener;
 import org.motechproject.retry.EventKeys;
 import org.motechproject.retry.domain.RetryRequest;
 import org.motechproject.retry.service.RetryService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 @Component
 public class NewMessagesSenderJob extends MessagesSenderJob {
 
     private static final String SLOT_EVENT_SUBJECT = "obd.send.new.messages";
     private static final String RETRY_EVENT_SUBJECT = "obd.send.new.messages.with.retry";
 
     private static final String RETRY_GROUP_NAME = "obd-send-new-messages-group";
     private static final String RETRY_NAME = "obd-send-new-messages";
 
     private static final Logger logger = LoggerFactory.getLogger(NewMessagesSenderJob.class);
     private CampaignMessageService campaignMessageService;
     private RetryService retryService;
     private OBDProperties obdProperties;
 
     @Autowired
     public NewMessagesSenderJob(CampaignMessageService campaignMessageService, RetryService retryService, final OBDProperties obdProperties) {
         super(SLOT_EVENT_SUBJECT,
                 new HashMap<SubSlot, String>() {{
                     put(SubSlot.ONE, obdProperties.getMainSlotCronJobExpressionFor(SubSlot.ONE.name()));
                 }}
         );
         this.campaignMessageService = campaignMessageService;
         this.retryService = retryService;
         this.obdProperties = obdProperties;
     }
 
     @MotechListener(subjects = {NewMessagesSenderJob.SLOT_EVENT_SUBJECT})
     public void sendMessages(MotechEvent motechEvent) {
         logger.info("Handling send new messages event");
         RetryRequest retryRequest = new RetryRequest(NewMessagesSenderJob.RETRY_NAME, UUID.randomUUID().toString(), DateTime.now());
         HashMap<String, Object> parameters = new HashMap<>();
         parameters.put(SUB_SLOT_KEY, motechEvent.getParameters().get(SUB_SLOT_KEY));
        retryService.schedule(retryRequest, parameters);
     }
 
     @MotechListener(subjects = {NewMessagesSenderJob.RETRY_EVENT_SUBJECT})
     public void sendMessagesWithRetry(MotechEvent motechEvent) {
         logger.info("Handling send new messages with retry event");
 
         Map<String, Object> parameters = motechEvent.getParameters();
         SubSlot subSlot = (SubSlot) parameters.get(SUB_SLOT_KEY);
 
         if (!canSendMessages(obdProperties.getMainSlotStartTimeLimitFor(subSlot.name()))) {
             retryService.fulfill((String) parameters.get(EventKeys.EXTERNAL_ID), NewMessagesSenderJob.RETRY_GROUP_NAME);
             return;
         }
 
         try {
             campaignMessageService.sendNewMessages(subSlot);
             retryService.fulfill((String) parameters.get(EventKeys.EXTERNAL_ID), NewMessagesSenderJob.RETRY_GROUP_NAME);
         } catch (Exception ex) {
             logger.error("Error occurred while sending new messages to obd", ex);
         }
     }
 }
