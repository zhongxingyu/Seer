 package org.motechproject.ananya.kilkari.obd.service;
 
 
 import org.apache.log4j.Logger;
 import org.motechproject.ananya.kilkari.obd.domain.ValidFailedCallReport;
 import org.motechproject.ananya.kilkari.obd.repository.OnMobileOBDGateway;
 import org.motechproject.ananya.kilkari.obd.service.request.FailedCallReports;
 import org.motechproject.ananya.kilkari.obd.service.request.InvalidFailedCallReports;
 import org.motechproject.event.MotechEvent;
 import org.motechproject.event.annotations.MotechListener;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class CallDeliveryFailureRecordHandler {
     Logger logger = Logger.getLogger(CallDeliveryFailureRecordHandler.class);
     private OnMobileOBDGateway onMobileOBDGateway;
     private CampaignMessageService campaignMessageService;
     private OBDCallDetailsService obdCallDetailsService;
 
     @Autowired
     public CallDeliveryFailureRecordHandler(OnMobileOBDGateway onMobileOBDGateway, CampaignMessageService campaignMessageService,
                                             OBDCallDetailsService obdCallDetailsService) {
         this.onMobileOBDGateway = onMobileOBDGateway;
         this.campaignMessageService = campaignMessageService;
         this.obdCallDetailsService = obdCallDetailsService;
     }
 
     @MotechListener(subjects = {CallDeliveryFailureEventKeys.PROCESS_INVALID_CALL_DELIVERY_FAILURE_RECORD})
     public void handleInvalidCallDeliveryFailureRecord(MotechEvent motechEvent) {
         InvalidFailedCallReports invalidFailedCallReports = (InvalidFailedCallReports) motechEvent.getParameters().get("0");
         logger.info("Handling OBD invalid call delivery failure records");
         onMobileOBDGateway.sendInvalidFailureRecord(invalidFailedCallReports);
     }
 
     @MotechListener(subjects = {CallDeliveryFailureEventKeys.PROCESS_VALID_CALL_DELIVERY_FAILURE_RECORD})
     public void handleValidCallDeliveryFailureRecord(MotechEvent motechEvent) {
         ValidFailedCallReport validFailedCallReport = (ValidFailedCallReport) motechEvent.getParameters().get("0");
        logger.info("Handling OBD invalid call delivery failure records");
         campaignMessageService.processValidCallDeliveryFailureRecords(validFailedCallReport);
     }
 
     @MotechListener(subjects = {CallDeliveryFailureEventKeys.PROCESS_CALL_DELIVERY_FAILURE_REQUEST})
     public void handleCallDeliveryFailureRecord(MotechEvent motechEvent) {
         FailedCallReports failedCallReports = (FailedCallReports) motechEvent.getParameters().get("0");
         logger.info("Handling OBD call delivery failure record");
         obdCallDetailsService.processCallDeliveryFailureRecord(failedCallReports);
     }
 
 }
