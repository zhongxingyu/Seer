 package edu.channel4.mm.db.android.network;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 
 import android.content.Context;
 import android.util.Log;
 import android.widget.Toast;
 import edu.channel4.mm.db.android.database.TempoDatabase;
 import edu.channel4.mm.db.android.model.description.AppDescription;
 import edu.channel4.mm.db.android.model.description.AttributeDescription;
 import edu.channel4.mm.db.android.util.BaseAsyncTask;
 import edu.channel4.mm.db.android.util.Keys;
 
 public class SalesforceConn {
 	static private SalesforceConn instance;
	protected static final String SALESFORCE_BASE_REST_URL = "%s/services/apexrest/%s/";
 	protected static final String APPS_URL_SUFFIX = "channel4_apps";
 	protected static final String ATTRIBS_URL_SUFFIX = "channel4_attributes";
 	protected static final String GRAPH_VIEW_BASE_URL = "%s/apex/graphView?oauth_token=%s";
 
 	private Context context;
 	protected HttpClient client;
 
 	private SalesforceConn(Context context) {
 		this.context = context;
 
 		client = new DefaultHttpClient();
 	}
 
 	/**
 	 * Public instantiator for a SalesforceConn object. Ensures no more than one
 	 * object exists.
 	 * 
 	 * @param context
 	 *            The application-level context.
 	 * @return A SalesforceConn operating within the given context.
 	 */
 	// TODO(Girum): Remove singletons. We shouldn't statically refer to a
 	// Context object, since the Context could potentially be different after a Force Close.
 	// http://stackoverflow.com/questions/137975/what-is-so-bad-about-singletons
 	// http://blogs.msdn.com/b/scottdensmore/archive/2004/05/25/140827.aspx
 	public static SalesforceConn getInstance(Context context) {
 		if (instance == null)
 			instance = new SalesforceConn(context);
 		else
 			instance.context = context;
 		return instance;
 	}
 
 	public String getGraphViewingURL() {
 		String accessToken = context.getSharedPreferences(Keys.PREFS_NS, 0)
 				.getString(Keys.ACCESS_TOKEN, null);
 		String instanceUrl = context.getSharedPreferences(Keys.PREFS_NS, 0)
 				.getString(Keys.INSTANCE_URL, null);
 
		String url = String.format(GRAPH_VIEW_BASE_URL, instanceUrl, accessToken);
 
 		return url;
 	}
 	
 	//TODO: add getEventList() once APEX RestResource is written
 
 	/**
 	 * Gets the list of attributes valid for this particular type of graph
 	 */
 	public void getAttribList() {
 		new GetAttribListTask(context).execute();
 	}
 
 	protected class GetAttribListTask extends BaseAsyncTask {
 		private String responseString = null;
 		private final String TAG = GetAppListTask.class.getSimpleName();
 
 		public GetAttribListTask(Context context) {
 			super(context);
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			Log.i(TAG, "Sending GET request to get app list");
 
 			String accessToken = context.getSharedPreferences(Keys.PREFS_NS, 0)
 					.getString(Keys.ACCESS_TOKEN, null);
 			String instanceUrl = context.getSharedPreferences(Keys.PREFS_NS, 0)
 					.getString(Keys.INSTANCE_URL, null);
 
 			if (accessToken == null) {
 				Log.e(TAG, "No access token currently saved");
 				return null;
 			}
 
 			// Put together the HTTP Request to be sent to Salesforce for the Attibute list
 			HttpPost attribRequest = new HttpPost(String.format(
 					SALESFORCE_BASE_REST_URL, instanceUrl, ATTRIBS_URL_SUFFIX));
 			attribRequest.setHeader("Authorization", "Bearer " + accessToken);
 			
 			// Add AppLabel parameter
 			String appLabel = context.getSharedPreferences(Keys.PREFS_NS, 0).getString(Keys.APP_LABEL, null);
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 			nameValuePairs.add(new BasicNameValuePair("appLabel", appLabel));
 
 			try {
 				attribRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 				// Get the response string, the Attribute List in JSON form
 				responseString = EntityUtils.toString(client
 						.execute(attribRequest).getEntity());
 			} catch (ClientProtocolException e) {
 				Log.e(TAG, e.getMessage());
 			} catch (IOException e) {
 				Log.e(TAG, e.getMessage());
 			}
 
 			Log.d(TAG, "Got JSON result: " + responseString);
 
 			return responseString;
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			super.onPostExecute(result);
 			
 			if (responseString == null) {
 				final String errorMessage = "ERROR: Attempted to parse null App list.";
 				Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT)
 						.show();
 				Log.e(TAG, errorMessage);
 				return;
 			}
 
 			// Try to parse the resulting JSON
 			List<AttributeDescription> attributeList = null;
 			try {
 				attributeList = AttributeDescription.parseList(responseString);
 
 				TempoDatabase tempoDatabase = TempoDatabase.getInstance();
 				tempoDatabase.setAttributeDescriptions(attributeList);
 			} catch (JSONException e) {
 				Log.e(TAG, e.getMessage());
 				return;
 			}
 		}
 	}
 
 	/**
 	 * Gets the app list from Salesforce.
 	 */
 	public void getAppList() {
 		new GetAppListTask(context).execute();
 	}
 
 	protected class GetAppListTask extends BaseAsyncTask {
 		private final String TAG = GetAppListTask.class.getSimpleName();
 		private String responseString = null;
 
 		public GetAppListTask(Context context) {
 			super(context);
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			Log.i(TAG, "Sending GET request to get app list");
 
 			String accessToken = context.getSharedPreferences(Keys.PREFS_NS, 0)
 					.getString(Keys.ACCESS_TOKEN, null);
 			String instanceUrl = context.getSharedPreferences(Keys.PREFS_NS, 0)
 					.getString(Keys.INSTANCE_URL, null);
 
 			if (accessToken == null) {
 				Log.e(TAG, "No access token currently saved");
 				return null;
 			}
 
 			Log.d(TAG, "Access token: " + accessToken);
 			Log.d(TAG, "Instance URL: " + instanceUrl);
 
 			HttpUriRequest getRequest = new HttpGet(String.format(
 					SALESFORCE_BASE_REST_URL, instanceUrl, APPS_URL_SUFFIX));
 			getRequest.setHeader("Authorization", "Bearer " + accessToken);
 
 			try {
 				responseString = EntityUtils.toString(client
 						.execute(getRequest).getEntity());
 			} catch (Exception e) {
 				Log.e(TAG, e.getMessage());
 			}
 
 			Log.d(TAG, "Got JSON result: " + responseString);
 
 			return responseString;
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			super.onPostExecute(result);
 
 			if (responseString == null) {
 				final String errorMessage = "ERROR: Attempted to parse null App list.";
 				Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT)
 						.show();
 				Log.e(TAG, errorMessage);
 				return;
 			}
 
 			// Try to parse the resulting JSON
 			List<AppDescription> appList = null;
 			try {
 				appList = AppDescription.parseList(responseString);
 
 				TempoDatabase tempoDatabase = TempoDatabase.getInstance();
 				tempoDatabase.setAppDescriptions(appList);
 			} catch (JSONException e) {
 				Log.e(TAG, e.getMessage());
 				return;
 			}
 
 		}
 	}
 
 }
