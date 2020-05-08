 package org.motechproject.ghana.national.handler;
 
 import junit.framework.Assert;
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.cmslite.api.model.ContentNotFoundException;
 import org.motechproject.ghana.national.builder.IVRCallbackUrlBuilder;
 import org.motechproject.ghana.national.builder.IVRRequestBuilder;
 import org.motechproject.ghana.national.domain.ivr.MobileMidwifeAudioClips;
 import org.motechproject.ghana.national.domain.mobilemidwife.*;
 import org.motechproject.ghana.national.exception.EventHandlerException;
 import org.motechproject.ghana.national.repository.AllPatientsOutbox;
 import org.motechproject.ghana.national.repository.IVRGateway;
 import org.motechproject.ghana.national.repository.SMSGateway;
 import org.motechproject.ghana.national.service.MobileMidwifeService;
 import org.motechproject.model.DayOfWeek;
 import org.motechproject.model.Time;
 import org.motechproject.retry.domain.RetryRequest;
 import org.motechproject.retry.service.RetryService;
 import org.motechproject.scheduler.domain.MotechEvent;
 import org.motechproject.server.messagecampaign.EventKeys;
 import org.motechproject.testing.utils.BaseUnitTest;
 import org.motechproject.util.DateUtil;
 import org.springframework.test.util.ReflectionTestUtils;
 
 import java.util.HashMap;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
import static org.joda.time.DateTime.now;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.server.messagecampaign.EventKeys.MESSAGE_CAMPAIGN_FIRED_EVENT_SUBJECT;
 import static org.motechproject.util.DateUtil.newDateTime;
 
 public class MobileMidwifeCampaignEventHandlerTest extends BaseUnitTest {
 
     MobileMidwifeCampaignEventHandler handler = new MobileMidwifeCampaignEventHandler();
     @Mock
     MobileMidwifeService mockMobileMidwifeService;
     @Mock
     SMSGateway mockSMSGateway;
     @Mock
     IVRGateway mockIVRGateway;
     @Mock
     AllPatientsOutbox mockAllPatientsOutbox;
     @Mock
     IVRCallbackUrlBuilder mockIVRCallbackUrlBuilder;
     @Mock
     RetryService mockRetryService;
     private DateTime now = DateUtil.newDateTime(DateUtil.newDate(2012, 7, 5), new Time(10, 10));
 
     @Before
     public void init() {
         initMocks(this);
         ReflectionTestUtils.setField(handler, "ivrCallbackUrlBuilder", mockIVRCallbackUrlBuilder);
         ReflectionTestUtils.setField(handler, "mobileMidwifeService", mockMobileMidwifeService);
         ReflectionTestUtils.setField(handler, "smsGateway", mockSMSGateway);
         ReflectionTestUtils.setField(handler, "ivrGateway", mockIVRGateway);
         ReflectionTestUtils.setField(handler, "allPatientsOutbox", mockAllPatientsOutbox);
         ReflectionTestUtils.setField(handler, "retryService", mockRetryService);
         super.mockCurrentDate(now);
 
     }
 
     @Test
     public void shouldSendSMSForEnrollmentWithSMSMedium() throws ContentNotFoundException {
         String patientId = "1234568";
         String mobileNumber = "9845312345";
         Language language = Language.EN;
         MobileMidwifeEnrollment mobileMidwifeEnrollment = new MobileMidwifeEnrollment(newDateTime(DateUtil.newDate(2012, 7, 4), new Time(10, 10))).setPatientId(patientId)
                 .setServiceType(ServiceType.CHILD_CARE).setMedium(Medium.SMS).setLanguage(language).setPhoneNumber(mobileNumber).setMessageStartWeek("50");
 
         when(mockMobileMidwifeService.findActiveBy(patientId)).thenReturn(mobileMidwifeEnrollment);
 
         handler.sendProgramMessage(motechEvent(patientId, "CHILD_CARE_SMS", mobileMidwifeEnrollment.getEnrollmentDateTime()));
         verify(mockSMSGateway).dispatchSMS("CHILD_CARE-cw10-Thursday", language.name(), mobileNumber);
     }
 
     @Test
     public void shouldSendUnRegisteredUserIfItIsTheLastEventForTheProgram() throws ContentNotFoundException {
 
         String patientId = "1234568";
         Language language = Language.EN;
         MobileMidwifeEnrollment mobileMidwifeEnrollment = new MobileMidwifeEnrollment(now()).setPatientId(patientId)
                 .setServiceType(ServiceType.CHILD_CARE).setMedium(Medium.SMS).setLanguage(language).setPhoneNumber("9845312345").setMessageStartWeek("10");
 
         when(mockMobileMidwifeService.findActiveBy(patientId)).thenReturn(mobileMidwifeEnrollment);
 
         MotechEvent lastEvent = motechEvent(patientId, "CHILD_CARE_SMS", mobileMidwifeEnrollment.getEnrollmentDateTime()).setLastEvent(true);
         handler.sendProgramMessage(lastEvent);
         ArgumentCaptor<DateTime> dateArgumentCaptor = ArgumentCaptor.forClass(DateTime.class);
         ArgumentCaptor<String> idArgumentCaptor = ArgumentCaptor.forClass(String.class);
         verify(mockMobileMidwifeService).rollover(idArgumentCaptor.capture(), dateArgumentCaptor.capture());
         assertThat(idArgumentCaptor.getValue(), is(equalTo(patientId)));
        assertThat(dateArgumentCaptor.getValue().toLocalDate(), is(equalTo(DateTime.now().toLocalDate())));
     }
 
     @Test
     public void shouldThrowOnAnyFailureInHandlingAlerts() {
         doThrow(new RuntimeException("some")).when(mockMobileMidwifeService).findActiveBy(anyString());
         final MotechEvent event = new MotechEvent("subjectMM", new HashMap<String, Object>());
         try {
             handler.sendProgramMessage(event);
             Assert.fail("expected scheduler handler exception");
         } catch (EventHandlerException she) {
             assertThat(she.getMessage(), is(event.toString()));
         }
     }
 
     @Test
     public void shouldAddMessagesToOutboxAndNotPlaceCallForRegistrationsWithPublicPhone() {
         ServiceType serviceType = ServiceType.PREGNANCY;
         String patientId = "1234568";
         final String url = "http://ivr";
         Language language = Language.EN;
 
         MobileMidwifeEnrollment mobileMidwifeEnrollment = new MobileMidwifeEnrollment(now()).setPatientId(patientId)
                 .setServiceType(serviceType).setMedium(Medium.VOICE).setPhoneOwnership(PhoneOwnership.PUBLIC).setLanguage(language).setMessageStartWeek("10").setDayOfWeek(DayOfWeek.Thursday);
         when(mockMobileMidwifeService.findActiveBy(patientId)).thenReturn(mobileMidwifeEnrollment);
         when(mockIVRCallbackUrlBuilder.outboundCallUrl(patientId)).thenReturn(url);
 
         handler.sendProgramMessage(motechEvent(patientId, "PREGNANCY_VOICE", mobileMidwifeEnrollment.getEnrollmentDateTime()));
 
         verify(mockAllPatientsOutbox).addMobileMidwifeMessage(patientId, MobileMidwifeAudioClips.instance(mobileMidwifeEnrollment.getServiceType().getValue(), "10"), Period.weeks(1));
         verify(mockRetryService, never()).schedule(eq(new RetryRequest("retry-ivr-every-2hrs-and-30mins", patientId, now)));
         verify(mockIVRGateway, never()).placeCall(mobileMidwifeEnrollment.getPhoneNumber(), new HashMap<String, String>() {{
             put(IVRRequestBuilder.CALLBACK_URL, url);
         }});
     }
 
 
     //this test should be maintained last because we are modifying current day
     @Test
     public void shouldPlaceCallWhenTheIVRCallEventIsReceived() {
         ServiceType serviceType = ServiceType.PREGNANCY;
         String patientId = "1234568";
         final String url = "http://ivr";
         Language language = Language.EN;
 
 
         // Preferred dayofweek is after registration dayofweek - 10 week message should be triggered
         MobileMidwifeEnrollment mobileMidwifeEnrollment = new MobileMidwifeEnrollment(newDateTime(DateUtil.newDate(2012, 7, 4), new Time(10, 10))).setPatientId(patientId)
                 .setServiceType(serviceType).setMedium(Medium.VOICE).setPhoneNumber("9845312345").setLanguage(language).setMessageStartWeek("10").setDayOfWeek(DayOfWeek.Thursday);
         when(mockMobileMidwifeService.findActiveBy(patientId)).thenReturn(mobileMidwifeEnrollment);
         when(mockIVRCallbackUrlBuilder.outboundCallUrl(patientId)).thenReturn(url);
 
         handler.sendProgramMessage(motechEvent(patientId, "PREGNANCY_VOICE", mobileMidwifeEnrollment.getEnrollmentDateTime()));
 
 
         verify(mockAllPatientsOutbox).addMobileMidwifeMessage(patientId, MobileMidwifeAudioClips.instance(mobileMidwifeEnrollment.getServiceType().getValue(), "10"), Period.weeks(1));
         verify(mockRetryService).schedule(eq(new RetryRequest("retry-ivr-every-2hrs-and-30mins", patientId, now)));
         verify(mockIVRGateway).placeCall(mobileMidwifeEnrollment.getPhoneNumber(), new HashMap<String, String>() {{
             put(IVRRequestBuilder.CALLBACK_URL, url);
         }});
 
         // Preferred dayofweek is before registration dayofweek - 10 week message should be triggered on the preferred day of next week
 
         reset(mockMobileMidwifeService, mockIVRGateway, mockRetryService, mockAllPatientsOutbox);
         mobileMidwifeEnrollment = new MobileMidwifeEnrollment(newDateTime(DateUtil.newDate(2012, 7, 4), new Time(10, 10))).setPatientId(patientId)
                 .setServiceType(serviceType).setMedium(Medium.VOICE).setPhoneNumber("9845312345").setLanguage(language).setMessageStartWeek("10").setDayOfWeek(DayOfWeek.Tuesday);
         when(mockMobileMidwifeService.findActiveBy(patientId)).thenReturn(mobileMidwifeEnrollment);
         when(mockIVRCallbackUrlBuilder.outboundCallUrl(patientId)).thenReturn(url);
 
         DateTime currentDateTime = DateUtil.newDateTime(DateUtil.newDate(2012, 7, 10));
         super.mockCurrentDate(currentDateTime);
 
         handler.sendProgramMessage(motechEvent(patientId, "PREGNANCY_VOICE", mobileMidwifeEnrollment.getEnrollmentDateTime()));
 
 
         verify(mockAllPatientsOutbox).addMobileMidwifeMessage(patientId, MobileMidwifeAudioClips.instance(mobileMidwifeEnrollment.getServiceType().getValue(), "10"), Period.weeks(1));
         verify(mockRetryService).schedule(eq(new RetryRequest("retry-ivr-every-2hrs-and-30mins", patientId, currentDateTime)));
         verify(mockIVRGateway).placeCall(mobileMidwifeEnrollment.getPhoneNumber(), new HashMap<String, String>() {{
             put(IVRRequestBuilder.CALLBACK_URL, url);
         }});
 
     }
 
 
     private MotechEvent motechEvent(String externalId, String campaignName, DateTime messageStartDate) {
         HashMap<String, Object> parameters = new HashMap<String, Object>();
         parameters.put(EventKeys.CAMPAIGN_NAME_KEY, campaignName);
         parameters.put(EventKeys.EXTERNAL_ID_KEY, externalId);
         parameters.put(EventKeys.CAMPAIGN_START_DATE, messageStartDate.toLocalDate());
         if ("PREGNANCY_VOICE".equals(campaignName) || "CHILD_CARE_VOICE".equals(campaignName))
             parameters.put(EventKeys.CAMPAIGN_REPEAT_INTERVAL, "1 Week");
         return new MotechEvent(MESSAGE_CAMPAIGN_FIRED_EVENT_SUBJECT, parameters);
     }
 }
