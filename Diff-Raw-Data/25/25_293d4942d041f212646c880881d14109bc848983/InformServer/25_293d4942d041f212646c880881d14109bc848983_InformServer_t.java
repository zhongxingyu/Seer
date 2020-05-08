 package edu.buffalo.cse.phonelab.utilities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.util.Log;
 
 /**
  * Use this class to inform server about any bit of information.
  *  
  * @author Rishi
  *
  */
 public class InformServer {
 
 	final static private String devId = "dev_id";
 	final static private String applicationId = "app_id";
 	final static private String action = "action";
 	final static private String install = "I";
 	final static private String uninstall = "U";
 	final static private String result = "result";
 	final static private String success = "S";
 	final static private String failure = "F";
	final static private String failureAlreadyInstalled = "F1";
	final static private String failureNoSuchApplication = "F2";
	
 
 	final static public int INSTALL = 1;
 	final static public int UNINSTALL = 2;
 	final static public int SUCCESS = 1;
 	final static public int FAILURE = 2;
	final static public int FAILUREALREADYINSTALLED = 3;
	final static public int FAILURENOSUCHAPP = 4;
 
 	public static void sendMessage(Context context, final String AppId,int actionCommand, int resultCommand) { // TODO better name than done
 
 		/*AsyncHttpClient client = new AsyncHttpClient();
 		RequestParams params = new RequestParams();
 
 		try {
 			params.put(devId, Util.getDeviceId(context));
 			params.put(applicationId, AppId);
 
 			if(command == INSTALL)				params.put(action, install);
 			else if (command == UNINSTALL)		params.put(action, uninstall);
 
 			if(command == SUCCESS)				params.put(result, success);
 			else if (command == FAILURE)		params.put(result, failure);
 
 			client.post(Util.APPLICATION_ACTION_URL, params, new AsyncHttpResponseHandler() {
 				@Override
 				public void onSuccess(String response) {
 					Log.i("PhoneLab-" + getClass().getSimpleName(), "Backend has been informed about uninstallation success : " + AppId);
 				}
 
 				@Override
 				public void onFailure(Throwable e) {
 					Log.i("PhoneLab-" + getClass().getSimpleName(), "Backend could not be informed about uninstallation success : " + AppId +
 							"\nReason : " + e.getMessage());
 				}
 			});
 		} catch (Exception e) {
 			Log.e("PhoneLab-" + InformServer.class.getClass().getSimpleName(), e.getMessage());
 		}*/
 		
 		try {
 			DefaultHttpClient httpclient = new DefaultHttpClient();
 			HttpPost httpost = new HttpPost(Util.APPLICATION_ACTION_URL);
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 			String response = "";
 			ResponseHandler<String> responseHandler = new BasicResponseHandler();
 			nameValuePairs.add(new BasicNameValuePair(devId, Util.getDeviceId(context)));
 			nameValuePairs.add(new BasicNameValuePair(applicationId, AppId));
 			if(actionCommand == INSTALL) {
 				nameValuePairs.add(new BasicNameValuePair(action, install));
 				Log.i ("PhoneLab-" + InformServer.class.getClass().getSimpleName(), "Action: I");
 			}
 			else if (actionCommand == UNINSTALL) {
 				nameValuePairs.add(new BasicNameValuePair(action, uninstall));
 				Log.i ("PhoneLab-" + InformServer.class.getClass().getSimpleName(), "Action: U");
 			}
 
 			if(resultCommand == SUCCESS) {
 				nameValuePairs.add(new BasicNameValuePair(result, success));
 				Log.i ("PhoneLab-" + InformServer.class.getClass().getSimpleName(), "Result: S");
 			}
 			else if (resultCommand == FAILURE) {
 				nameValuePairs.add(new BasicNameValuePair(result, failure));
 				Log.i ("PhoneLab-" + InformServer.class.getClass().getSimpleName(), "Result: F");
			} else if (resultCommand == FAILUREALREADYINSTALLED) {
				nameValuePairs.add(new BasicNameValuePair(result, failureAlreadyInstalled));
				Log.i ("PhoneLab-" + InformServer.class.getClass().getSimpleName(), "Result: F1");
			} else if (resultCommand == FAILURENOSUCHAPP) {
				nameValuePairs.add(new BasicNameValuePair(result, failureNoSuchApplication));
				Log.i ("PhoneLab-" + InformServer.class.getClass().getSimpleName(), "Result: F2");
 			}
 			
 			httpost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			response = httpclient.execute(httpost,responseHandler);
 			
 			JSONObject responseJ = new JSONObject(response);
 			
 			if (responseJ.getString("error").equals("")) {//success
 				Log.i("PhoneLab-" + InformServer.class.getClass().getSimpleName(), "Backend has been informed about success : " + AppId);
 			} else {//error
 				Log.e("PhoneLab-" + InformServer.class.getClass().getSimpleName(), "Backend could not be informed about failure (server error) : " + AppId);
 			}
 		} catch (Exception e) {
 			Log.e("PhoneLab-" + InformServer.class.getClass().getSimpleName(), "Backend could not be informed about failure : " + AppId +
 					"\nReason : " + e.getMessage());
 		}
 	}
 }
 
