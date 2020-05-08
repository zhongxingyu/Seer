 package sk.uniza.fri.comp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class SMSGatewayAsyncTask extends AsyncTask<String, Void, JSONObject> {
 	private static final String TAG = SMSGatewayAsyncTask.class.getSimpleName();
 
 	private static final String PARAM_EVENT = "event";
 	private static final String PARAM_SMS_RECEIVED_ID = "id";
 	private static final String PARAM_SMS_TEXT = "sms";
 
 	private Context mContext;
 
 	public SMSGatewayAsyncTask(Context context) {
 		this.mContext = context;
 	}
 
 	/**
 	 * @param args[0] - URL
 	 * @param args[1] - event ID
 	 * @param args[2] - SMS received ID (from DB)
 	 * @param args[3] - SMS text
 	 * */
 	@Override
 	protected JSONObject doInBackground(String... args) {
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpPost httppost = new HttpPost(args[0]);
 
 		try {
 			// Add POST params
 			List<NameValuePair> postParamsPair = new ArrayList<NameValuePair>(3);
 			postParamsPair.add(new BasicNameValuePair(PARAM_EVENT, args[1]));
 			postParamsPair.add(new BasicNameValuePair(PARAM_SMS_RECEIVED_ID,
 					args[2]));
 			postParamsPair.add(new BasicNameValuePair(PARAM_SMS_TEXT, args[3]));
 			httppost.setEntity(new UrlEncodedFormEntity(postParamsPair));
 
 			// Execute HTTP Post Request
 			ResponseHandler<String> responseHandler = new BasicResponseHandler();
 			String responseBody = httpclient.execute(httppost, responseHandler);
 			JSONObject response = new JSONObject(responseBody);
 			Log.e("RESP", response.toString());
 			return response;
 
 		} catch (Exception e) {
 			Log.e(TAG, e.toString());
 		}
 
 		return null;
 	}
 
 	@Override
 	protected void onPostExecute(JSONObject response) {
 		// Handle HTTP Response
 		JSONObject resp;
 		try {
 			resp = response.getJSONObject("data");
 			Object id = resp.get("id");
			Object sms = resp.get("sms");
			Log.e("RESP", resp.toString());
 			Log.e("RESP id", id.toString());
			Log.e("RESP sms", sms.toString());
 
 		} catch (JSONException e) {
 			Log.e(TAG, e.toString());
 		}
 	}
 }
