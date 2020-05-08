 package mobi.monaca.framework.task;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import mobi.monaca.framework.psedo.BuildConfig;
 import mobi.monaca.framework.util.MyLog;
 import mobi.monaca.utils.APIUtil;
 import mobi.monaca.utils.MonacaConst;
 import mobi.monaca.utils.MonacaDevice;
 import mobi.monaca.utils.MyAsyncTask;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.util.Log;
 
 /**
  * Send GCM Registration ID to Monaca Server
  *
  */
 public abstract class GCMRegistrationIdSenderTask extends MyAsyncTask<Void, Void, JSONObject> {
 	private static final String TAG = GCMRegistrationIdSenderTask.class.getSimpleName();
 	private static final String KEY_PREF = "gcm_pref";
 	/**
	 * should use "KEY_PREF_ALREADY_REGISTERED + GCMRegistrar.getRegistrationId(this)" form
 	 */
 	static final String KEY_PREF_ALREADY_REGISTERED = "already_registered";
 	final private String REGISTRATION_API_URL;
 
 	private Context context;
 	private String regId;
 	private SharedPreferences configPreference;
 
 	private boolean alreadyRegistered;
 
 	private String env;
 	private String isCustom;
 
 	/**
 	 *constructor
 	 * @param context
 	 * @param regAPIUrl
 	 * @param regId
 	 */
 	public GCMRegistrationIdSenderTask(Context context,String regAPIUrl, String regId) {
 		this.context = context;
 		this.regId = regId;
 		this.configPreference = context.getSharedPreferences(KEY_PREF, Context.MODE_PRIVATE);
 		this.REGISTRATION_API_URL = regAPIUrl;
 
 		this.env = MonacaConst.getEnv(context);
 		this.isCustom = MonacaConst.getIsCustom(context);
 
 		alreadyRegistered = configPreference.getBoolean(KEY_PREF_ALREADY_REGISTERED + regId, false);
 	}
 
 	@Override
 	final protected void onPostExecute(JSONObject resultJson) {
 		if (alreadyRegistered) {
			MyLog.d(TAG, "already registered to server");
 			// do nothing
 		} else {
 			try {
 				if (resultJson.get("status").equals("ok")) {
 					preOnSucceededRegistration(resultJson);
 				} else if (resultJson.get("status").equals("no_status")){
 					if (resultJson.getInt("response_code") == 200) {
 						preOnSucceededRegistration(resultJson);
 					} else {
 						preOnFailedRegistration(resultJson);
 					}
 				} else {
 					preOnFailedRegistration(resultJson);
 				}
 			} catch (JSONException e) {
 				preOnFailedRegistration(resultJson);
 			}
 		}
 
 		onClosedTask();
 	}
 
 	private void preOnSucceededRegistration(JSONObject resultJson) {
		MyLog.d(TAG, "succeeded GCM registration to server!");
 		Editor e = configPreference.edit();
 		e.putBoolean(KEY_PREF_ALREADY_REGISTERED + regId, true);
 		e.commit();
 
 		onSucceededRegistration(resultJson);
 	}
 
 	private void preOnFailedRegistration(JSONObject resultJson) {
		MyLog.d(TAG, "Failed GCM registration to server");
 		onFailedRegistration(resultJson);
 	}
 
 	@Override
 	public void cancel(boolean b) {
 		onClosedTask();
 		super.cancel(b);
 	}
 
 	public static void clearAlreadyRegisteredPreference(Context context, String regId) {
 		Editor e = context.getSharedPreferences(KEY_PREF, Context.MODE_PRIVATE).edit();
 		e.putBoolean(KEY_PREF_ALREADY_REGISTERED + regId, false);
 		e.commit();
 	}
 
 	public GCMRegistrationIdSenderTask setEnv(String env) {
 		this.env = env;
 		return this;
 	}
 	public GCMRegistrationIdSenderTask setIsCustom(String isCustom) {
 		this.isCustom = isCustom;
 		return this;
 	}
 
 	abstract protected void onClosedTask();
 	abstract protected void onSucceededRegistration(JSONObject resultJson);
 	abstract protected void onFailedRegistration(JSONObject resultJson);
 
 	@Override
 	final protected JSONObject doInBackground(Void... a) {
 		if (alreadyRegistered) {
 			return null;
 		}
 		try {
 			int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
 
 			List<NameValuePair> list = new ArrayList<NameValuePair>();
 			list.add(new BasicNameValuePair("platform", "android"));
 			list.add(new BasicNameValuePair("deviceId", MonacaDevice.getDeviceId(context)));
 			list.add(new BasicNameValuePair("env", env));
 			list.add(new BasicNameValuePair("isCustom", isCustom));
 			list.add(new BasicNameValuePair("version",  Integer.toString(versionCode)));
 			list.add(new BasicNameValuePair("registrationId", regId));
 			list.add(new BasicNameValuePair("buildType", BuildConfig.DEBUG ? "debug" : "release"));
 
 			URL url = new URL(REGISTRATION_API_URL);
 			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 			connection.setDoOutput(true);
 			connection.setConnectTimeout(10 * 1000);// 10sec
 
 			OutputStream os = connection.getOutputStream();
 			String postStr = APIUtil.getQuery(list);
 
 			PrintStream ps = new PrintStream(os);
 			ps.print(postStr);
 			ps.close();
 			os.close();
 
 			connection.connect();
 
 			try {
 				InputStream is = connection.getInputStream();
 				String resultJsonString = IOUtils.toString(is);
 
 				return new JSONObject(resultJsonString).put("response_code", connection.getResponseCode());
 			} catch (Exception e1) {
 				JSONObject result = new JSONObject();
 				result.put("status", "no_status");
 				result.put("response_code", connection.getResponseCode());
 				return result;
 			}
 		} catch (MalformedURLException e1) {
 			e1.printStackTrace();
 		} catch (JSONException e1) {
 			e1.printStackTrace();
 		} catch (NameNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		JSONObject result = new JSONObject();
 		try {
 			result.put("status", "fail_with_exception");
 		} catch (JSONException e) {
 		}
 		return result;
 	}
 }
