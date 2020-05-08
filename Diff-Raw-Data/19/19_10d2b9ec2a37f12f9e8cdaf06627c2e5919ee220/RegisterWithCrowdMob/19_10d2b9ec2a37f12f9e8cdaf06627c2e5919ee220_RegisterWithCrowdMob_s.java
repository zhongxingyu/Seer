 /*---------------------------------------------------------------------------*\
  |  RegisterWithCrowdMob.java                                                |
  |                                                                           |
  |  Copyright (c) 2012, CrowdMob, Inc., original authors.                    |
  |                                                                           |
  |      File created/modified by:                                            |
  |          Raj Shah <raj@crowdmob.com>                                      |
 \*---------------------------------------------------------------------------*/
 
 package com.crowdmob.installs;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.net.http.AndroidHttpClient;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.AsyncTask;
 import android.provider.Settings.Secure;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 public class RegisterWithCrowdMob {
 	// private static final String TAG = "RegisterWithCrowdMob";
 
 	public static void trackAppInstallation(Context context, String secretKey, String permalink) {
         // Register this Android app installation with CrowdMob.  Only register on the first run of this app.
         if (FirstRun.isFirstRun(context)) {
         	String[] uniqueDeviceId = UniqueDeviceId.getUniqueDeviceId(context);
         	String uuidType = uniqueDeviceId[0];
         	String uuid = uniqueDeviceId[1];
         	String securityHash = computeSecurityHash(secretKey, permalink, uuidType, uuid);
         	new AsyncRegisterWithCrowdMob().execute(permalink, uuidType, uuid, securityHash);
         }
 	}
 
     private static String computeSecurityHash(String secretKey, String permalink, String uuidType, String uuid) {
     	String message = secretKey + permalink + ',' + uuidType + ',' + uuid;
 		String securityHash = Hash.hash("SHA-256", "", message);
 		return securityHash;
     }
 }
 
 // This class only contains static methods.  So we don't incur any penalty for
 // instantiating a new object.  It's just a way to organize all of the methods
 // to do with determining if an app has been run before.
 class FirstRun {
 	private static final String PREFS_NAME = "RegisterWithCrowdMobPrefsFile";
 	private static final String TAG = "FirstRun";
 
 	static boolean isFirstRun(Context context) {
     	Log.i(TAG, "has app been run before?");
         SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
         boolean firstRun = settings.getBoolean("firstRun", true);
         Log.i(TAG, firstRun ? "app hasn't been run before" : "app has been run before");
         return firstRun;
     }
 
     static void completedFirstRun(Context context) {
     	Log.i(TAG, "successfully completed first run");
         SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
     	SharedPreferences.Editor editor = settings.edit();
     	editor.putBoolean("firstRun", false);
     	editor.commit();
     	Log.i(TAG, "saved successful completion of first run");
     }
 }
 
 // This class only contains static methods.  So we don't incur any penalty for
 // instantiating a new object.  It's just a way to organize all of the methods
 // to do with computing a unique Android device ID.
 class UniqueDeviceId {
 	private static final String TAG = "UniqueDeviceId";
 
 	// This class contains methods corresponding to strategies for determining
 	// a unique Android device ID.  We use reflection to iterate through the
 	// methods one by one (in alphabetical order by method name), and we use
 	// the device ID returned by the first successful method call.
 	//
 	// For more info, see: http://android-developers.blogspot.com/2011/03/identifying-app-installations.html
 	class Strategies {
 		private Context context;
 
 		public Strategies(Context context) {
 			this.context = context;
 		}
 
 		String[] aAndroidId() {
 			Log.i(TAG, "trying to get Android ID");
 			String androidId = null;
 			try {
 				androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
 			} catch (Exception e) {
 				Log.w(TAG, "couldn't get Android ID (exception thrown, stack trace follows)");
 				e.printStackTrace();
 				return null;
 			}
 			Log.i(TAG, "got Android ID " + androidId);
			
 			String[] return_values = new String[2];
			return_values[0] = "android-id";
 			return_values[1] = androidId;
 			return return_values;
 		}
 
 		String[] bSerialNumber() {
 			Log.i(TAG, "trying to get serial number");
 			String serialNumber = null;
 			try {
 		        Class<?> c = Class.forName("android.os.SystemProperties");
 		        Method get = c.getMethod("get", String.class);
 		        serialNumber = (String) get.invoke(c, "ro.serialno");
 		    } catch (Exception e) {
 				e.printStackTrace();
 		    }
 			Log.i(TAG, "got serial number " + serialNumber);
 			
 			String[] return_values = new String[2];
			return_values[0] = "android-serial-number";
 			return_values[1] = serialNumber;
 			return return_values;
 		}
 
 		String[] cMacAddressHash() {
 			Log.i(TAG, "trying to get MAC address");
 
 	    	Log.d(TAG, "getting wifi manager");
 	    	WifiManager wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
 	    	Log.d(TAG, "got wifi manager: " + wifiManager);
 
 	    	Log.d(TAG, "getting wifi info");
 	    	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
 	    	Log.d(TAG, "got wifi info: " + wifiInfo);
 
 	    	Log.d(TAG, "getting MAC address");
 	    	String macAddress = wifiInfo.getMacAddress();
 	    	Log.d(TAG, "got MAC address: " + macAddress);
 
 	    	if (macAddress == null) {
 	    		Log.w(TAG, "couldn't get MAC address (wifi disabled?)");
 	    		return null;
 	    	}
 
 	       	Log.i(TAG, "got MAC address " + macAddress);
 
 			String[] return_values = new String[2];
			return_values[0] = "mac-address";
 			return_values[1] = macAddress;
 			return return_values;
 		}
 
 		String[] dTelephonyDeviceId() {
 			Log.i(TAG, "trying to get telephony ID");
 
 			Log.d(TAG, "getting telephony manager");
 			TelephonyManager telephonyManager = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);
 			Log.d(TAG, "got telephony manager: " + telephonyManager);
 
 			Log.d(TAG, "getting telephony device ID");
 			String telephonyDeviceId = telephonyManager.getDeviceId();
 			Log.d(TAG, "got telephony device ID: " + telephonyDeviceId);
 
 			Log.i(TAG,  "got telephony ID " + telephonyDeviceId);
 
 			String[] return_values = new String[2];
			return_values[0] = "android-telephony-id";
 			return_values[1] = telephonyDeviceId;
 			return return_values;
 		}
 	}
 
 	static String[] getUniqueDeviceId(Context context) {
 		Strategies strategies = new UniqueDeviceId().new Strategies(context);
 		String[] uniqueDeviceId = null;
 		for (Method method : strategies.getClass().getDeclaredMethods()) {
 			try {
 				uniqueDeviceId = (String[]) method.invoke(strategies);
 			} catch (InvocationTargetException e) {
 				e.getTargetException().printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			if (uniqueDeviceId != null) {
 				break;
 			}
 		}
 		return uniqueDeviceId;
 	}
 }
 
 // This class only contains static methods.  So we don't incur any penalty for
 // instantiating a new object.  It's just a way to organize all of the methods
 // to do with generating cryptographic hashes.
 class Hash {
 	private static final String TAG = "Hash";
 
     static String hash(String algorithm, String salt, String message) {
     	Log.d(TAG, algorithm + " hashing salt " + salt + " and message " + message);
     	MessageDigest digest = null;
     	try {
     		digest = MessageDigest.getInstance(algorithm);
     	} catch (NoSuchAlgorithmException e) {
     		Log.w(TAG, "no such algorithm: " + algorithm);
     		e.printStackTrace();
     		return null;
     	}
     	digest.reset();
     	if (salt.length() > 0) {
     		digest.update(salt.getBytes());
     	}
     	byte[] messageDigest = digest.digest(message.getBytes());
 
     	// Convert the message digest to hex.  For more info, see: http://stackoverflow.com/a/332101
     	StringBuffer hexBuffer = new StringBuffer();
     	for (int j = 0; j < messageDigest.length; j++) {
     		String hexByte = Integer.toHexString(0xFF & messageDigest[j]);
     		if (hexByte.length() == 1) {
     			hexBuffer.append("0");
     		}
     		hexBuffer.append(hexByte);
     	}
     	String hexString = hexBuffer.toString();
     	Log.d(TAG, algorithm + " hashed to hex string " + hexString);
     	return hexString;
     }
 }
 
 class AsyncRegisterWithCrowdMob extends AsyncTask<String, Void, Integer> {
	// private static final String CROWDMOB_URL = "http://deals.crowdmob.com/loot/verify_install.json";
	private static final String CROWDMOB_URL = "http://deals.mobstaging.com/loot/verify_install.json";
 	private static final Integer[] successCrowdMobStatusCodes = {2001, 2002};
 	private static final String TAG = "AsyncRegisterWithCrowdMob";
 
 	@Override
 	protected Integer doInBackground(String... params) {
 		// Issue a POST request to register the app installation with CrowdMob.
 		Log.i(TAG, "registering app installation with CrowdMob");
 
 		String content = null;
 		Integer httpStatusCode = null;
     	AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
     	HttpPost post = new HttpPost(CROWDMOB_URL);
 		List<NameValuePair> pairs = populateParams(params);
 
     	try {
 			Log.d(TAG, "issuing POST request");
 			post.setEntity(new UrlEncodedFormEntity(pairs));
 			HttpResponse response = client.execute(post);
 			httpStatusCode = response.getStatusLine().getStatusCode();
 			HttpEntity entity = response.getEntity();
 			InputStream stream = entity.getContent();
 			content = streamToString(stream);
 			try {
 				stream.close();
 			} catch (IOException e) {
 			}
 			entity.consumeContent();
 			Log.d(TAG, "issued POST request, got content " + content);
 		} catch (UnsupportedEncodingException e) {
 			Log.e(TAG, "caught UnsupportedEncodingException");
     		e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			Log.e(TAG, "caught ClientProtocolException");
     		e.printStackTrace();
 		} catch (IOException e) {
 			Log.e(TAG, "caught IOException (no internet access or SSL error?)");
     		e.printStackTrace();
 		}
 		client.close();
 
     	Integer crowdMobStatusCode = parseJson(content);
 		Log.i(TAG, "registered app installation with CrowdMob, HTTP status code " + httpStatusCode);
     	Log.d(TAG, "HTTP status code: " + httpStatusCode);
     	Log.d(TAG, "CrowdMob status code: " + crowdMobStatusCode);
     	return crowdMobStatusCode;
 	}
 
 	@Override
 	protected void onPostExecute(Integer crowdMobStatusCode) {
 		Log.i(TAG, "handling CrowdMob installation registration status code " + crowdMobStatusCode);
 		if (Arrays.asList(successCrowdMobStatusCodes).contains(crowdMobStatusCode)) {
 			Log.d(TAG, "CrowdMob status code indicates success; registering successful first run");
 			// FirstRun.completedFirstRun(context);
 		} else {
 			Log.d(TAG, "CrowdMob status code indicates failure; not registering successful first run");
 		}
 		Log.i(TAG, "handled CrowdMob installation registration status code");
 	}
 
 	private List<NameValuePair> populateParams(String... params) {
 		String permalink = params[0];
 		String uuidType = params[1];
 		String uuid = params[2];
 		String securityHash = params[3];
 
 		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
     	pairs.add(new BasicNameValuePair("verify[permalink]", permalink));
        	pairs.add(new BasicNameValuePair("verify[uuid_type]", uuidType));
     	pairs.add(new BasicNameValuePair("verify[uuid]", uuid));
     	pairs.add(new BasicNameValuePair("verify[secret_hash]", securityHash));
 
     	return pairs;
 	}
 
 	private String streamToString(InputStream stream) {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
 		String line;
 		StringBuilder builder = new StringBuilder();
 		try {
 			while ((line = reader.readLine()) != null) {
 			    builder.append(line);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return builder.toString();
 	}
 
 	private Integer parseJson(String content) {
 		Integer crowdMobStatusCode = null;
 		Object json = null;
 
 		if (content != null) {
 	    	try {
 				json = new JSONObject(content);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			if (json != null) {
 				try {
 					crowdMobStatusCode = Integer.parseInt(((JSONObject) json).getString("install_status"));
 				} catch (NumberFormatException e) {
 					e.printStackTrace();
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
     	}
 		return crowdMobStatusCode;
 	}
 }
