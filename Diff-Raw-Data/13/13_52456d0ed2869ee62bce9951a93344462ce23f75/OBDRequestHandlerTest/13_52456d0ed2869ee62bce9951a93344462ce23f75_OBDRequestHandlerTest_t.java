 package org.motechproject.ananya.kilkari.handlers;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.motechproject.ananya.kilkari.factory.OBDServiceOptionFactory;
 import org.motechproject.ananya.kilkari.handlers.callback.obd.ServiceOptionHandler;
 import org.motechproject.ananya.kilkari.obd.contract.InvalidCallRecordRequestObject;
 import org.motechproject.ananya.kilkari.obd.contract.InvalidCallRecordsRequest;
 import org.motechproject.ananya.kilkari.obd.domain.InvalidCallRecord;
 import org.motechproject.ananya.kilkari.obd.domain.OBDEventKeys;
 import org.motechproject.ananya.kilkari.obd.domain.ServiceOption;
 import org.motechproject.ananya.kilkari.obd.service.CallRecordsService;
 import org.motechproject.ananya.kilkari.obd.service.CampaignMessageService;
 import org.motechproject.ananya.kilkari.request.OBDSuccessfulCallRequest;
 import org.motechproject.ananya.kilkari.request.OBDSuccessfulCallRequestWrapper;
 import org.motechproject.ananya.kilkari.service.KilkariCampaignService;
 import org.motechproject.ananya.kilkari.subscription.domain.Channel;
 import org.motechproject.ananya.kilkari.subscription.exceptions.ValidationException;
 import org.motechproject.ananya.kilkari.validators.OBDSuccessfulCallRequestValidator;
 import org.motechproject.scheduler.domain.MotechEvent;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import static junit.framework.Assert.assertEquals;
 import static org.mockito.Mockito.*;
 
 @RunWith(MockitoJUnitRunner.class)
 public class OBDRequestHandlerTest {
 
     @Mock
     private OBDServiceOptionFactory obdServiceOptionFactory;
     @Mock
     private KilkariCampaignService kilkariCampaignService;
     @Mock
     private ServiceOptionHandler serviceOptionHandler;
     @Mock
     private OBDSuccessfulCallRequestValidator successfulCallRequestValidator;
     @Mock
     private CampaignMessageService campaignMessageService;
     @Mock
     private CallRecordsService callRecordsService;
 
     private OBDRequestHandler obdRequestHandler;
 
     @Before
     public void setUp() {
         obdRequestHandler = new OBDRequestHandler(obdServiceOptionFactory, kilkariCampaignService, successfulCallRequestValidator, callRecordsService);
     }
 
     @Test
     public void shouldHandleAOBDCallBackRequest() {
         HashMap<String, Object> stringObjectHashMap = new HashMap<>();
         OBDSuccessfulCallRequest successfulCallRequest = new OBDSuccessfulCallRequest();
         successfulCallRequest.setServiceOption(ServiceOption.HELP.name());
         OBDSuccessfulCallRequestWrapper expectedObdRequest = new OBDSuccessfulCallRequestWrapper(successfulCallRequest, "subscriptionId", DateTime.now(), Channel.IVR);
         stringObjectHashMap.put("0", expectedObdRequest);
         when(obdServiceOptionFactory.getHandler(ServiceOption.HELP)).thenReturn(serviceOptionHandler);
 
         obdRequestHandler.handleOBDCallbackRequest(new MotechEvent(OBDEventKeys.PROCESS_SUCCESSFUL_CALL_REQUEST_SUBJECT, stringObjectHashMap));
 
         verify(kilkariCampaignService).processSuccessfulMessageDelivery(expectedObdRequest);
         verify(serviceOptionHandler).process(expectedObdRequest);
     }
 
     @Test
     public void shouldHandleAOBDCallBackRequestWithDeactivation() {
         HashMap<String, Object> stringObjectHashMap = new HashMap<>();
         OBDSuccessfulCallRequest successfulCallRequest = new OBDSuccessfulCallRequest();
         successfulCallRequest.setServiceOption(ServiceOption.UNSUBSCRIBE.name());
         OBDSuccessfulCallRequestWrapper expectedObdRequest = new OBDSuccessfulCallRequestWrapper(successfulCallRequest, "subscriptionId", DateTime.now(), Channel.IVR);
         stringObjectHashMap.put("0", expectedObdRequest);
         when(obdServiceOptionFactory.getHandler(ServiceOption.UNSUBSCRIBE)).thenReturn(serviceOptionHandler);
 
         obdRequestHandler.handleOBDCallbackRequest(new MotechEvent(OBDEventKeys.PROCESS_SUCCESSFUL_CALL_REQUEST_SUBJECT, stringObjectHashMap));
 
         verify(kilkariCampaignService).processSuccessfulMessageDelivery(expectedObdRequest);
         verify(serviceOptionHandler).process(expectedObdRequest);
     }
 
     @Test(expected = ValidationException.class)
     public void shouldInvalidateTheOBDRquest() {
         HashMap<String, Object> stringObjectHashMap = new HashMap<>();
         OBDSuccessfulCallRequest successfulCallRequest = new OBDSuccessfulCallRequest();
         successfulCallRequest.setServiceOption("Random");
         OBDSuccessfulCallRequestWrapper expectedObdRequest = new OBDSuccessfulCallRequestWrapper(successfulCallRequest, "subscriptionId", DateTime.now(), Channel.IVR);
         stringObjectHashMap.put("0", expectedObdRequest);
         when(obdServiceOptionFactory.getHandler(ServiceOption.HELP)).thenReturn(serviceOptionHandler);
         ArrayList<String> errors = new ArrayList<String>() {{
             add("Invalid service option");
         }};
         when(successfulCallRequestValidator.validate(expectedObdRequest)).thenReturn(errors);
 
         obdRequestHandler.handleOBDCallbackRequest(new MotechEvent(OBDEventKeys.PROCESS_SUCCESSFUL_CALL_REQUEST_SUBJECT, stringObjectHashMap));
 
         verify(kilkariCampaignService, never()).processSuccessfulMessageDelivery(expectedObdRequest);
         verify(serviceOptionHandler, never()).process(expectedObdRequest);
     }
 
     @Test
     public void shouldNotThrowExceptionIfHandlerIsNotThereForServiceOption() {
         Map<String, Object> map = new HashMap<>();
         OBDSuccessfulCallRequest successfulCallRequest = new OBDSuccessfulCallRequest();
         successfulCallRequest.setServiceOption("");
         OBDSuccessfulCallRequestWrapper successfulCallRequestWrapper = new OBDSuccessfulCallRequestWrapper(successfulCallRequest, "subscriptionId", DateTime.now(), Channel.IVR);
         map.put("0", successfulCallRequestWrapper);
 
         obdRequestHandler.handleOBDCallbackRequest(new MotechEvent(OBDEventKeys.PROCESS_INVALID_CALL_RECORDS_REQUEST_SUBJECT, map));
     }
 
     @Test
     public void shouldProcessInvalidCallRecords() {
         String msisdn1 = "msisdn1";
         String campaign1 = "campaign1";
         String desc1 = "desc1";
         String operator1 = "operator1";
         String sub1 = "sub1";
 
         InvalidCallRecordsRequest invalidCallRecordsRequest = Mockito.mock(InvalidCallRecordsRequest.class);
         ArrayList<InvalidCallRecordRequestObject> invalidCallRecordRequestObjects = new ArrayList<InvalidCallRecordRequestObject>();
         InvalidCallRecordRequestObject invalidCallRecordRequestObject1 = Mockito.mock(InvalidCallRecordRequestObject.class);
         when(invalidCallRecordRequestObject1.getMsisdn()).thenReturn(msisdn1);
         when(invalidCallRecordRequestObject1.getCampaignId()).thenReturn(campaign1);
         when(invalidCallRecordRequestObject1.getDescription()).thenReturn(desc1);
         when(invalidCallRecordRequestObject1.getOperator()).thenReturn(operator1);
         when(invalidCallRecordRequestObject1.getSubscriptionId()).thenReturn(sub1);
 
         InvalidCallRecordRequestObject invalidCallRecordRequestObject2 = Mockito.mock(InvalidCallRecordRequestObject.class);
         invalidCallRecordRequestObjects.add(invalidCallRecordRequestObject1);
         invalidCallRecordRequestObjects.add(invalidCallRecordRequestObject2);
 
 
         when(invalidCallRecordsRequest.getCallrecords()).thenReturn(invalidCallRecordRequestObjects);
 
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("0", invalidCallRecordsRequest);

        obdRequestHandler.handleInvalidCallRecordsRequest(new MotechEvent(OBDEventKeys.PROCESS_INVALID_CALL_RECORDS_REQUEST_SUBJECT, parameters));
 
         ArgumentCaptor<ArrayList> captor = ArgumentCaptor.forClass(ArrayList.class);
         verify(callRecordsService).processInvalidCallRecords(captor.capture());
 
         ArrayList actualInvalidCallRecords = captor.getValue();
         assertEquals(2, actualInvalidCallRecords.size());
         InvalidCallRecord actualInvalidCallRecord1 = (InvalidCallRecord) actualInvalidCallRecords.get(0);
         assertEquals(campaign1, actualInvalidCallRecord1.getCampaignId());
         assertEquals(sub1, actualInvalidCallRecord1.getSubscriptionId());
         assertEquals(msisdn1, actualInvalidCallRecord1.getMsisdn());
         assertEquals(operator1, actualInvalidCallRecord1.getOperator());
         assertEquals(desc1, actualInvalidCallRecord1.getDescription());
     }
 }
