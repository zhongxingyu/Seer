 package org.motechproject.ananya.kilkari.handlers;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.motechproject.ananya.kilkari.factory.OBDServiceOptionFactory;
 import org.motechproject.ananya.kilkari.obd.contract.InvalidCallRecordRequestObject;
 import org.motechproject.ananya.kilkari.obd.contract.InvalidCallRecordsRequest;
 import org.motechproject.ananya.kilkari.obd.domain.InvalidCallRecord;
 import org.motechproject.ananya.kilkari.obd.domain.OBDEventKeys;
 import org.motechproject.ananya.kilkari.obd.domain.ServiceOption;
 import org.motechproject.ananya.kilkari.obd.service.CallRecordsService;
 import org.motechproject.ananya.kilkari.request.OBDSuccessfulCallRequestWrapper;
 import org.motechproject.ananya.kilkari.service.KilkariCampaignService;
 import org.motechproject.ananya.kilkari.subscription.exceptions.ValidationException;
 import org.motechproject.ananya.kilkari.validators.OBDSuccessfulCallRequestValidator;
 import org.motechproject.scheduler.domain.MotechEvent;
 import org.motechproject.server.event.annotations.MotechListener;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Component
 public class OBDRequestHandler {
 
     Logger logger = Logger.getLogger(OBDRequestHandler.class);
     private OBDServiceOptionFactory obdServiceOptionFactory;
     private KilkariCampaignService kilkariCampaignService;
     private OBDSuccessfulCallRequestValidator successfulCallRequestValidator;
     private CallRecordsService callRecordsService;
 
     @Autowired
     public OBDRequestHandler(OBDServiceOptionFactory obdServiceOptionFactory, KilkariCampaignService kilkariCampaignService, OBDSuccessfulCallRequestValidator successfulCallRequestValidator, CallRecordsService callRecordsService) {
         this.obdServiceOptionFactory = obdServiceOptionFactory;
         this.kilkariCampaignService = kilkariCampaignService;
         this.successfulCallRequestValidator = successfulCallRequestValidator;
         this.callRecordsService = callRecordsService;
     }
 
     @MotechListener(subjects = {OBDEventKeys.PROCESS_SUCCESSFUL_CALL_REQUEST_SUBJECT})
     public void handleOBDCallbackRequest(MotechEvent motechEvent) {
         OBDSuccessfulCallRequestWrapper successfulCallRequestWrapper = (OBDSuccessfulCallRequestWrapper) motechEvent.getParameters().get("0");
         logger.info("Handling OBD callback for : " + successfulCallRequestWrapper.getSubscriptionId());
 
         validateSuccessfulCallRequest(successfulCallRequestWrapper);
 
         kilkariCampaignService.processSuccessfulMessageDelivery(successfulCallRequestWrapper);
         ServiceOption serviceOption = ServiceOption.getFor(successfulCallRequestWrapper.getSuccessfulCallRequest().getServiceOption());
         if (obdServiceOptionFactory.getHandler(serviceOption) != null) {
             obdServiceOptionFactory.getHandler(serviceOption).process(successfulCallRequestWrapper);
         }
         logger.info("Completed handling OBD callback for : " + successfulCallRequestWrapper.getSubscriptionId());
     }
 
     private void validateSuccessfulCallRequest(OBDSuccessfulCallRequestWrapper successfulCallRequestWrapper) {
         List<String> validationErrors = successfulCallRequestValidator.validate(successfulCallRequestWrapper);
         if (!(validationErrors.isEmpty())) {
             throw new ValidationException(String.format("OBD Request Invalid: %s", StringUtils.join(validationErrors.toArray(), ",")));
         }
     }
 
     @MotechListener(subjects = {OBDEventKeys.PROCESS_INVALID_CALL_RECORDS_REQUEST_SUBJECT})
    public void handleInvalidCallRecordsRequest(InvalidCallRecordsRequest invalidCallRecordsRequest) {
         ArrayList<InvalidCallRecordRequestObject> requestCallRecords = invalidCallRecordsRequest.getCallrecords();
         ArrayList<InvalidCallRecord> invalidCallRecords = new ArrayList<>();
         for (InvalidCallRecordRequestObject requestObject : requestCallRecords) {
             invalidCallRecords.add(new InvalidCallRecord(requestObject.getMsisdn(), requestObject.getSubscriptionId(),
                     requestObject.getCampaignId(), requestObject.getOperator(), requestObject.getDescription()));
         }
         callRecordsService.processInvalidCallRecords(invalidCallRecords);
     }
 }
