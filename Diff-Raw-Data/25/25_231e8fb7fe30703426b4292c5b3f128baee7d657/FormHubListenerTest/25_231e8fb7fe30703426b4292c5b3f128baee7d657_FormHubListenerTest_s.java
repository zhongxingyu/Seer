 package org.who.formhub.listener;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.motechproject.scheduler.gateway.OutboundEventGateway;
 import org.who.formhub.api.contract.FormHubFormInstance;
 import org.who.formhub.api.service.FormHubImportService;
 import org.who.formhub.listener.event.FormHubFormEvent;
 
 import static java.util.Arrays.asList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.who.formhub.api.util.EasyMap.mapOf;
 
 public class FormHubListenerTest {
     @Mock
     private FormHubImportService formHubImportService;
     @Mock
     private OutboundEventGateway outboundEventGateway;
 
     private FormHubListener formHubListener;
 
     @Before
     public void setUp() throws Exception {
         initMocks(this);
         formHubListener = new FormHubListener(formHubImportService, outboundEventGateway);
     }
 
     @Test
     public void shouldFetchAllFormsAndCreateEventsForThem() throws Exception {
         FormHubFormInstance firstFormInstance = new FormHubFormInstanceBuilder().withName("Form1").withMapping("Key1", "KeyName1").withContent(mapOf("Key1", "Value1")).build();
         FormHubFormInstance secondFormInstance = new FormHubFormInstanceBuilder().withName("Form2").withMapping("Key2", "KeyName2").withContent(mapOf("Key2", "Value2")).build();
         when(formHubImportService.fetchForms()).thenReturn(asList(firstFormInstance, secondFormInstance));
 
         formHubListener.fetchFromServer();
 
         verify(outboundEventGateway).sendEventMessage(new FormHubFormEvent(asList(firstFormInstance, secondFormInstance)).toMotechEvent());
     }
 
     @Test
     public void shouldNotInitiateFetchIfFetchingIsAlreadyInProgress() throws Exception {
         when(formHubImportService.fetchForms()).then(new Answer<Object>() {
             @Override
             public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                 try {
                     Thread.sleep(5000);
                } catch (InterruptedException e) {
                 }
                 return null;
             }
         });
 
         Thread thread = new Thread(new Runnable() {
             @Override
             public void run() {
                 formHubListener.fetchFromServer();
             }
         });
         thread.start();
 

         new Thread(new Runnable() {
             @Override
             public void run() {
                 formHubListener.fetchFromServer();
             }
         }).start();
 
         verify(formHubImportService, times(1)).fetchForms();
        thread.interrupt();
     }
 }
