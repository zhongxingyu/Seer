 package org.motechproject.ghana.national.repository;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ghana.national.domain.IVRChannelMapping;
 import org.motechproject.ivr.service.CallRequest;
 import org.motechproject.server.verboice.VerboiceIVRService;
 import org.springframework.test.util.ReflectionTestUtils;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.reset;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 
 public class IVRGatewayTest {
 
     IVRGateway gateway;
 
     @Mock
     VerboiceIVRService mockIVRService;
     @Mock
     AllIvrChannelMappings mockAllIvrChannelMappings;
 
     @Before
     public void setUp() {
         initMocks(this);
         gateway = new IVRGateway();
         ReflectionTestUtils.setField(gateway, "verboiceIVRService", mockIVRService);
         ReflectionTestUtils.setField(gateway, "allIvrChannelMappings", mockAllIvrChannelMappings);
         when(mockAllIvrChannelMappings.allMappings()).thenReturn(Arrays.asList(
                 new IVRChannelMapping().ivrChannel("MTN").phoneNumberPattern("^0(2|5)4[0-9]+$"),
                 new IVRChannelMapping().ivrChannel("Vodafone").phoneNumberPattern("^0(2|3)0[0-9]+$"),
                 new IVRChannelMapping().ivrChannel("Tigo").phoneNumberPattern("^0(2|5)7[0-9]+$"),
                 new IVRChannelMapping().ivrChannel("Airtel").phoneNumberPattern("^026[0-9]+$"),
                new IVRChannelMapping().ivrChannel("Expresso").phoneNumberPattern("^$028[0-9]+")));
     }
 
     @Test
     public void shouldInitiateACall() {
         String phoneNumber = "0267654321"; //^026[0-9]+$
         Map<String, String> params = new HashMap<String, String>();
         gateway.placeCall(phoneNumber, params);
         ArgumentCaptor<CallRequest> callRequestCaptor = ArgumentCaptor.forClass(CallRequest.class);
         verify(mockIVRService).initiateCall(callRequestCaptor.capture());
         CallRequest callRequest = callRequestCaptor.getValue();
         assertThat(callRequest.getPhone(), is(phoneNumber));
         assertThat(callRequest.getPayload(), is(params));
         assertThat(callRequest.getCallBackUrl(), is("Airtel"));
 
         reset(mockIVRService);
         phoneNumber = "02401111111";  // ^0(2|5)4[0-9]+$
         gateway.placeCall(phoneNumber, params);
         verify(mockIVRService).initiateCall(callRequestCaptor.capture());
         assertThat(callRequestCaptor.getValue().getCallBackUrl(), is("MTN"));
 
         reset(mockIVRService);
         phoneNumber = "09991111111"; // no pattern so the first channel
         gateway.placeCall(phoneNumber, params);
         verify(mockIVRService).initiateCall(callRequestCaptor.capture());
         assertThat(callRequestCaptor.getValue().getCallBackUrl(), is("MTN"));
     }
 }
