 package au.com.dius.resilience.test.unit.service;
 
 import android.content.Intent;
 import android.os.Bundle;
 import au.com.dius.resilience.model.Feedback;
 import au.com.dius.resilience.model.Incident;
 import au.com.dius.resilience.persistence.repository.Repository;
 import au.com.dius.resilience.persistence.repository.impl.ParseRepository;
 import au.com.dius.resilience.service.CreateIncidentService;
 import au.com.dius.resilience.service.SendFeedbackService;
 import com.xtremelabs.robolectric.Robolectric;
 import com.xtremelabs.robolectric.RobolectricTestRunner;
 import junitx.util.PrivateAccessor;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.powermock.modules.junit4.PowerMockRunner;
 import roboguice.RoboGuice;
 
 import static au.com.dius.resilience.intent.Intents.RESILIENCE_FEEDBACK_SUBMITTED;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyObject;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.*;
 
 @RunWith(RobolectricTestRunner.class)
 public class SendFeedbackServiceTest {
 
   private SendFeedbackService service;
 
   Repository repository;
   Intent intent;
 
   @Before
   public void setup() {
     intent = new Intent(RESILIENCE_FEEDBACK_SUBMITTED);
     intent.putExtra(SendFeedbackService.EXTRA_FEEDBACK, new Feedback("some text", "dev-id123"));
 
     repository = mock(ParseRepository.class);
     when(repository.sendFeedback(any(Feedback.class))).thenReturn(true);
 
     service = spy(new SendFeedbackService());
     service.onCreate();
   }
 
   @Test
   public void shouldSendCompleteBroadcastOnSave() throws NoSuchFieldException {
    PrivateAccessor.setField(service, "repository", repository);
     service.onHandleIntent(intent);
     verify(service).sendBroadcast(any(Intent.class));
   }
 
   @Test
   public void shouldNotSendBroadcastOnFail() throws NoSuchFieldException {
     when(repository.sendFeedback(any(Feedback.class))).thenReturn(false);
    PrivateAccessor.setField(service, "repository", repository);
     service.onHandleIntent(intent);
     verify(service, never()).sendBroadcast(any(Intent.class));
   }
 }
