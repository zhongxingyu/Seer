 package org.motechproject.ghana.national.web;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.motechproject.ghana.national.domain.IVRClipManager;
 import org.motechproject.ghana.national.domain.mobilemidwife.Language;
 import org.motechproject.ghana.national.repository.AllPatientsOutbox;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.test.util.ReflectionTestUtils;
 
 import java.util.Arrays;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.mockito.Matchers.isNotNull;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class OutgoingCallControllerTest {
 
     private OutgoingCallController controller = new OutgoingCallController();
 
     @Mock
     private AllPatientsOutbox mockAllPatientsOutbox;
     @Mock
     private IVRClipManager mockIvrClipManager;
 
     @Before
     public void setUp() {
         initMocks(this);
         ReflectionTestUtils.setField(controller, "allPatientsOutbox", mockAllPatientsOutbox);
         ReflectionTestUtils.setField(controller, "ivrClipManager", mockIvrClipManager);
     }
 
     @Test
     public void shouldPickMessagesFromUrlAndPlayThem_GivenMotechIdAndLanguage() {
         String motechId = "1234567";
         String language = "EN";
         MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
         mockHttpServletRequest.setParameter("motechId", motechId);
         mockHttpServletRequest.setParameter("ln", language);
 
         String fileName1 = "xyz";
         String fileName2 = "abc";
         when(mockAllPatientsOutbox.getAudioFileNames(motechId)).thenReturn(Arrays.asList(fileName1, fileName2));
         String urlForAudio1 = "http://blah";
         String urlForAudio2 = "http://blahblah";
         when(mockIvrClipManager.urlFor(fileName1, Language.valueOf(language))).thenReturn(urlForAudio1);
         when(mockIvrClipManager.urlFor(fileName2, Language.valueOf(language))).thenReturn(urlForAudio2);
 
         String response = controller.call(mockHttpServletRequest);
         assertThat(response, is("<Response><Play loop=\"1\">" + urlForAudio1 + "</Play><Play loop=\"1\">"
                + urlForAudio2 + "</Play><Redirect method=\"POST\">null</Redirect></Response>"));
 
     }
 
     @Test
     public void shouldShowErrorForBlankMotechId(){
         MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
         mockHttpServletRequest.setParameter("motechId", "");
         String response = controller.call(mockHttpServletRequest);
         assertThat(response, is("<Response>\n" +
                 "<Say>Unexpected error</Say>\n" +
                 "</Response>"));
 
     }
 }
