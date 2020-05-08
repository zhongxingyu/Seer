 package au.com.dius.resilience.ui.fragment;
 
import android.app.Activity;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import au.com.dius.resilience.R;
 import au.com.dius.resilience.model.Device;
 import au.com.dius.resilience.model.Feedback;
 import au.com.dius.resilience.service.SendFeedbackService;
 
 public class FeedbackFragment extends Fragment {
 
   private TextView feedbackText;
 
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     return inflater.inflate(R.layout.fragment_feedback, container);
   }
 
   @Override
   public void onActivityCreated(Bundle state) {
     feedbackText = (TextView) getActivity().findViewById(R.id.feedback_text);
     super.onActivityCreated(state);
   }
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
   }
 
   public void onFeedbackSubmitClick(View button) {
     String deviceId = Device.getDeviceId(getActivity());
     String text = feedbackText.getText().toString();
     Feedback feedback = new Feedback(text, deviceId);
 
     if (text != null && text.trim().length() > 0) {
       button.setEnabled(false);
       getActivity().startService(SendFeedbackService.createFeedbackSaveCompleteIntent(getActivity(), feedback));
     }
   }
 }
