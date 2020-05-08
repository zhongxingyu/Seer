 package com.fingy.robocall.service.impl;
 
 import com.fingy.robocall.dao.CallRequestRepository;
 import com.fingy.robocall.model.CallRequest;
 import com.fingy.robocall.model.dto.RoboCallRequest;
 import com.fingy.robocall.service.ApplicationPropertiesHolder;
 import com.fingy.robocall.service.TwilioService;
 import com.twilio.sdk.TwilioRestClient;
 import com.twilio.sdk.TwilioRestException;
 import com.twilio.sdk.resource.factory.CallFactory;
 import com.twilio.sdk.resource.instance.Call;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
 import org.springframework.scheduling.TaskScheduler;
 import org.springframework.stereotype.Service;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import static com.fingy.robocall.util.DateTimeUtil.*;
 
 @Service
 public class TwilioServiceImpl implements TwilioService {
 
     private static final String URL_PARAM_NAME = "Url";
     private static final String TO_PARAM_NAME = "To";
     private static final String FROM_PARAM_NAME = "From";
     private static final String STATUS_CALLBACK_PARAM_NAME = "StatusCallback";
 
     private static final String TWILIO_CALLBACK = "/twilio-callback?";
     private static final String TWILIO_STATUS_CALLBACK = "/twilio-status-callback";
     private static final String TIME_ZONE = "Europe/Rome";
 
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     private @Autowired ApplicationPropertiesHolder properties;
     private @Autowired CallRequestRepository requestRepository;
     private @Autowired TaskScheduler taskScheduler;
 
     @Override
     public void save(CallRequest callRequest) {
         requestRepository.save(callRequest);
     }
 
     @Override
     public void delete(CallRequest callRequest) {
         requestRepository.delete(callRequest);
     }
 
     @Override
     public CallRequest placeCall(RoboCallRequest callRequest, String rootUrl) throws TwilioRestException, UnsupportedEncodingException {
         List<NameValuePair> params = createCallParameters(callRequest, rootUrl);
         String sid = doCall(params);
         CallRequest callDetails = toCallRequest(callRequest, rootUrl, sid);
         return requestRepository.save(callDetails);
     }
 
     private List<NameValuePair> createCallParameters(RoboCallRequest callRequest, String rootUrl) throws UnsupportedEncodingException {
         String callbackUrl = rootUrl + TWILIO_CALLBACK + callRequest.toQueryParamString();
         String statusCallbackUrl = rootUrl + TWILIO_STATUS_CALLBACK;
 
         List<NameValuePair> params = new ArrayList<>();
         params.add(new BasicNameValuePair(URL_PARAM_NAME, callbackUrl));
         params.add(new BasicNameValuePair(TO_PARAM_NAME, callRequest.getPhoneNumber()));
         params.add(new BasicNameValuePair(FROM_PARAM_NAME, properties.getTwilioPhoneNumber()));
         params.add(new BasicNameValuePair(STATUS_CALLBACK_PARAM_NAME, statusCallbackUrl));
         return params;
     }
 
     private String doCall(List<NameValuePair> params) throws TwilioRestException {
         TwilioRestClient client = new TwilioRestClient(properties.getAccountSid(), properties.getAuthenticationToken());
         CallFactory callFactory = client.getAccount().getCallFactory();
         Call call = callFactory.create(params);
         return call.getSid();
     }
 
     private CallRequest toCallRequest(RoboCallRequest callRequest, String rootUrl, String sid) {
         return new CallRequest(sid, callRequest.getText(), callRequest.getLanguage(), callRequest.getPhoneNumber(), rootUrl);
     }
 
     @Override
     public void statusUpdate(String callSid, String callStatus) {
         if ("busy".equals(callStatus) || "no-answer".equals(callStatus)) {
             scheduleRedial(callSid);
         } else if ("completed".equals(callStatus) || "failed".equals(callStatus)) {
             CallRequest toDelete = requestRepository.findBySid(callSid);
             requestRepository.delete(toDelete);
         }
     }
 
     @Override
     public void scheduleRedial(String callSid) {
         CallRequest callRequest = requestRepository.findBySid(callSid);
 
         if (callRequest.getRedialCount() < properties.getRedialThreshold()) {
             scheduleRedial(callRequest);
         } else {
             cancelRedial(callRequest);
         }
     }
 
     private void scheduleRedial(CallRequest callRequest) {
         DateTime current = new DateTime(DateTimeZone.forID(TIME_ZONE));
         DateTime redialTime = shouldReschedule(current) ? toEightThirtyAmTomorrow(current) : current.plusMinutes(15);
         doScheduleRedial(callRequest, redialTime);
     }
 
     private boolean shouldReschedule(DateTime redialTime) {
         return isAfterHalfPastEightPm(redialTime) || isBeforeHalfPastEightAm(redialTime);
     }
 
     private void doScheduleRedial(CallRequest callRequest, DateTime redialTime) {
         logger.info("Scheduling a redial for SID {}, redial time is {}", callRequest.getSid(), redialTime);
         callRequest.setRedialTime(redialTime.toDate());
         CallRequest saved = requestRepository.save(callRequest);
         taskScheduler.schedule(new RedialTask(saved, this, properties), redialTime.toDate());
     }
 
     private void cancelRedial(CallRequest callRequest) {
         logger.warn("Redial limit reached, cancelling redial for {}", callRequest.getSid());
         requestRepository.delete(callRequest);
     }
 
 }
