 package net.pureessence.controller;
 
 import net.pureessence.util.HttpMethodHelper;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Answers;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.when;
 
 @RunWith(MockitoJUnitRunner.class)
 public class WebServiceCallerTest {
     private static final String STATUS_URL = "http://localhost/status";
 
     @Mock(answer = Answers.RETURNS_DEEP_STUBS)
     private HttpMethodHelper httpMethodHelper;
 
     @InjectMocks
     private WebServiceCaller webServiceCaller = new WebServiceCaller();
 
     @Before
     public void setUp() throws Exception {
         webServiceCaller.setStatusUrl(STATUS_URL);
        webServiceCaller.setTimeout(2000);
     }
 
     @Test
     public void testIsJobFinished() throws Exception {
         when(httpMethodHelper.createPostMethod(STATUS_URL).getResponseBodyAsString()).thenReturn("false", "false", "true");
 
         boolean result = webServiceCaller.isJobFinished();
         assertTrue(result);
     }
 
     @Test(expected = RuntimeException.class)
     public void testIsJobFinishedTimeout() throws Exception {
         when(httpMethodHelper.createPostMethod(STATUS_URL).getResponseBodyAsString()).thenReturn("false", "false", "false", "true");
 
         boolean result = webServiceCaller.isJobFinished();
         assertTrue(result);
     }
 }
