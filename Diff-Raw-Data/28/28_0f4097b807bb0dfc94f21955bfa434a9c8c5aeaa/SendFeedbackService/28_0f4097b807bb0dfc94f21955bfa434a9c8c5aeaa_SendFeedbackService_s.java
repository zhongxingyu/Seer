 package au.com.dius.resilience.service;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import au.com.dius.resilience.model.Feedback;
 import au.com.dius.resilience.persistence.repository.Repository;
 import com.google.inject.Inject;
 import roboguice.service.RoboIntentService;
 
 import static au.com.dius.resilience.intent.Intents.RESILIENCE_FEEDBACK_SUBMITTED;
 
 public class SendFeedbackService extends RoboIntentService {
 
   @Inject
   private Repository repository;
 
   public static final String EXTRA_FEEDBACK = SendFeedbackService.class.getName() + ".Feedback";
   private String TAG = SendFeedbackService.class.getName();
 
   public SendFeedbackService() {
     super("ResilienceSendFeedbackIncidentService");
   }
 
   public static Intent createFeedbackSaveCompleteIntent(Context context, Feedback feedback) {
     Intent feedbackIntent = new Intent(context, SendFeedbackService.class);
     feedbackIntent.putExtra(EXTRA_FEEDBACK, feedback);
 
     return feedbackIntent;
   }
 
   @Override
   public void onHandleIntent(Intent intent) {
     Feedback feedback = (Feedback) intent.getExtras().getSerializable(EXTRA_FEEDBACK);

    sendBroadcast(new Intent(RESILIENCE_FEEDBACK_SUBMITTED));
 
     if (repository.sendFeedback(feedback)) {
       Log.d(TAG, "Sent " + RESILIENCE_FEEDBACK_SUBMITTED + " broadcast");
     }
     else {
       Log.d(TAG, "Sending feedback failed.");
     }
   }
 }
