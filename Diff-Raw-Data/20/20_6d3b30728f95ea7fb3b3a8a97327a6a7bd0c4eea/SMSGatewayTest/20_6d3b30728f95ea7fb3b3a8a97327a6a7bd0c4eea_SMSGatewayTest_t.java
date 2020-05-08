 package org.motechproject.ghana.national.repository;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.motechproject.cmslite.api.model.ContentNotFoundException;
 import org.motechproject.cmslite.api.model.StringContent;
 import org.motechproject.cmslite.api.service.CMSLiteService;
 import org.motechproject.ghana.national.configuration.CountryCodeAppender;
 import org.motechproject.ghana.national.messagegateway.domain.MessageRecipientType;
 import org.motechproject.ghana.national.messagegateway.domain.NextMondayDispatcher;
 import org.motechproject.ghana.national.domain.SMSPayload;
 import org.motechproject.ghana.national.messagegateway.service.MessageGateway;
 import org.motechproject.model.Time;
 import org.motechproject.sms.api.service.SmsService;
 import org.motechproject.testing.utils.BaseUnitTest;
 import org.motechproject.util.DateUtil;
 
 import java.util.HashMap;
 import java.util.Locale;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.springframework.test.util.ReflectionTestUtils.setField;
 
 public class SMSGatewayTest extends BaseUnitTest{
 
     SMSGateway smsGateway;
     @Mock
     CMSLiteService mockCMSLiteService;
     @Mock
     SmsService mockSMSService;
     @Mock
     MessageGateway mockMessageGateway;
     @Mock
     private CountryCodeAppender mockCountryCodeAppender;
 
     @Before
     public void init() {
         initMocks(this);
         smsGateway = new SMSGateway();
         setField(smsGateway, "smsService", mockSMSService);
         setField(smsGateway, "cmsLiteService", mockCMSLiteService);
         setField(smsGateway, "messageGateway", mockMessageGateway);
         setField(smsGateway, "countryCodeAppender", mockCountryCodeAppender);
         mockCurrentDate(DateUtil.newDateTime(DateUtil.today(), new Time(10, 10)));
     }
                                       
     @Test
     public void shouldFetchMessageTextWithTemplateKeyAndDispatchSMS() throws ContentNotFoundException {
         String phoneNumber = "0TenDigits";
         String language = "language";
         String templateKey = "templateKey";
         String smsText = "smsMessage";
         final StringContent stringContent = createStringContent(smsText);
         when(mockCMSLiteService.getStringContent(language, templateKey)).thenReturn(stringContent);
         when(mockCountryCodeAppender.apply(phoneNumber)).thenReturn("233" + phoneNumber);
         smsGateway.dispatchSMS(templateKey, language, phoneNumber);
         verify(mockSMSService).sendSMS("233" + phoneNumber, smsText);
     }
 
     @Test
     public void shouldFillInTemplateWithRuntimeValuesAndDispatchSMS() throws ContentNotFoundException {
         String templateKey = "templateKey";
         String smsText = "smsMessage-${RTKey}";
         final String phoneNumber = "phoneNumber";
         final StringContent stringContent = createStringContent(smsText);
         when(mockCMSLiteService.getStringContent(anyString(), eq(templateKey))).thenReturn(stringContent);
         when(mockCountryCodeAppender.apply(phoneNumber)).thenReturn("233" + phoneNumber);
 
         smsGateway.dispatchSMS(templateKey, new HashMap<String, String>(){{put("${RTKey}", "RTValue");}}, phoneNumber);
         verify(mockSMSService).sendSMS("233" + phoneNumber, "smsMessage-RTValue");
     }
 
     @Test
     public void shouldCreateSMSMessageAndDispatchItToTheAggregator() throws ContentNotFoundException {
         String templateKey = "templateKey";
         final String template = "template";
         final String phoneNumber = "phoneNumber";
         final String smsText = "value";
 
         final StringContent stringContent = createStringContent(template);
         when(mockCMSLiteService.getStringContent(Locale.getDefault().getLanguage(), templateKey)).thenReturn(stringContent);
 
         smsGateway.dispatchSMSToAggregator(templateKey, new HashMap<String, String>() {{
             put(template, smsText);
         }}, phoneNumber, "identifier", MessageRecipientType.FACILITY);
 
 
         ArgumentCaptor<SMSPayload> smsArgumentCaptor = ArgumentCaptor.forClass(SMSPayload.class);
         verify(mockMessageGateway).dispatch(smsArgumentCaptor.capture(), eq("identifier"));
         final SMSPayload smsPayloadSentToGateway = smsArgumentCaptor.getValue();
         assertThat(smsPayloadSentToGateway.getText(), is(equalTo(smsText)));
         assertThat(smsPayloadSentToGateway.getUniqueId(), is(equalTo(phoneNumber)));
         assertThat(smsPayloadSentToGateway.getGenerationTime(), is(equalTo(DateUtil.now())));
         assertThat(smsPayloadSentToGateway.getDeliveryStrategy().getClass(), is(typeCompatibleWith(NextMondayDispatcher.class)));
     }
 
    @Test
    public void shouldNotSendMessagesForNullPhoneNumbers() {
        smsGateway.dispatchSMS(null, "sample message");
        verify(mockSMSService, never()).sendSMS(Matchers.any(String.class), Matchers.any(String.class));
    }


     private StringContent createStringContent(String value) {
         StringContent stringContent = mock(StringContent.class);
         when(stringContent.getValue()).thenReturn(value);
         return stringContent;
     }
 
 }
