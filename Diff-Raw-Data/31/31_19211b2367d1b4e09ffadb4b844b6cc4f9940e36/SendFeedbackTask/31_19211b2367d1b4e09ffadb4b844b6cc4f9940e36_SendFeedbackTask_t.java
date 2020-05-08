 package au.com.dius.resilience.persistence.async;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import au.com.dius.resilience.R;
 import au.com.dius.resilience.model.Feedback;
 import au.com.dius.resilience.model.FeedbackResult;
 import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 public class SendFeedbackTask extends AsyncTask<Feedback, Void, FeedbackResult> {
 
   private static final String JSON_ATTRIBUTE_EMAIL = "email";
   private static final String JSON_ATTRIBUTE_COMMENT = "comment";
   private static final String OPEN311_REQUEST_PAGE = "feedback.json";
   private static final int TIMEOUT_MILLISEC = 10000;
  private static final int FEEDBACK_SUCCESSFUL_STATUS = 201;
 
   public interface SendFeedbackCallback {
     public void onTaskDone(FeedbackResult result);
   }
 
   private SendFeedbackCallback callback;
   private Activity activity;
 
   public SendFeedbackTask(Activity activity, SendFeedbackCallback callback) {
     this.activity = activity;
     this.callback = callback;
   }
 
   @Override
   protected FeedbackResult doInBackground(Feedback... params) {
 
     FeedbackResult result = null;
 
     for (int i = 0; i < params.length; i++) {
 
       result = sendFeedback(params[i]);
 
     }
 
     return result;
   }
 
   private FeedbackResult sendFeedback(Feedback f) {
     JSONObject json = null;
     HttpParams httpParams = null;
     HttpClient client = null;
     StringBuilder address = null;
     HttpPost request = null;
    HttpResponse response = null;
 
     // Create JSON object
     try {
       json = new JSONObject();
       json.put(JSON_ATTRIBUTE_EMAIL, f.getDeviceId());
       json.put(JSON_ATTRIBUTE_COMMENT, f.getText());
     } catch (JSONException e) {
 
       return new FeedbackResult(false, e);
     }
 
     // Create HTTP parameters
     httpParams = new BasicHttpParams();
     HttpConnectionParams.setConnectionTimeout(httpParams,
         TIMEOUT_MILLISEC);
     HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
     client = new DefaultHttpClient(httpParams);
 
     // Create URL
     address = new StringBuilder(activity.getString(R.string.open_311_base_url));
     address.append("/");
     address.append(OPEN311_REQUEST_PAGE);
 
     // Create request
     try {
       request = new HttpPost(address.toString());
       request.setEntity(new ByteArrayEntity(json.toString().getBytes(
           "UTF8")));
       request.setHeader("json", json.toString());
     } catch (UnsupportedEncodingException e) {
 
       return new FeedbackResult(false, e);
     }
 
     // Send request
     try {
      response = client.execute(request);
 
     } catch (IOException e) {
 
       return new FeedbackResult(false, e);
     }
 
    // Get response
    StatusLine status = response.getStatusLine();

    return new FeedbackResult(status.getStatusCode() == FEEDBACK_SUCCESSFUL_STATUS, null);
   }
 
   @Override
   protected void onPostExecute(FeedbackResult result) {
     callback.onTaskDone(result);
   }

 }
