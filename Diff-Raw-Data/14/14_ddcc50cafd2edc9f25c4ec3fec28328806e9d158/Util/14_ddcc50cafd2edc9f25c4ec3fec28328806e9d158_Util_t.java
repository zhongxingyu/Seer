 package whyq.utils;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TimeZone;
 import java.util.UUID;
 import java.util.regex.Pattern;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import whyq.WhyqApplication;
 import whyq.activity.LoginHome;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Rect;
 import android.graphics.Typeface;
 import android.location.Address;
 import android.location.Criteria;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.Settings;
 import android.text.TextPaint;
 import android.text.format.DateUtils;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.whyq.R;
 
 public class Util {
 	public static final int TIME_OUT = 30000;
 
 	public Util() {
 
 	}
 
 	public static Typeface sTypefaceRegular = Typeface.createFromAsset(
 			WhyqApplication.Instance().getApplicationContext().getAssets(),
 			"Roboto-Regular.ttf");;
 	public static Typeface sTypefaceBold = Typeface.createFromAsset(
 			WhyqApplication.Instance().getApplicationContext().getAssets(),
 			"Roboto-Bold.ttf");;
 	public static Typeface sTypefaceItalic = Typeface.createFromAsset(
 			WhyqApplication.Instance().getApplicationContext().getAssets(),
 			"Roboto-Italic.ttf");;
 
 	/**
 	 * @Description: check to see whether email address is valid or not
 	 * 
 	 * @Return boolean
 	 */
 	public static boolean isValidEmail(String inputMail) {
 
 		if (inputMail == null || inputMail == "") {
 			return false;
 		}
 		Pattern pattern = Pattern
 				.compile("^([a-zA-Z0-9_.-])+@([a-zA-Z0-9_.-])+\\.([a-zA-Z])+([a-zA-Z])+");
 		return pattern.matcher(inputMail).matches();
 	}
 
 	public String convertStreamToString(InputStream is) {
 		/*
 		 * To convert the InputStream to String we use the Reader.read(char[]
 		 * buffer) method. We iterate until the Reader return -1 which means
 		 * there's no more data to read. We use the StringWriter class to
 		 * produce the string.
 		 */
 
 		if (is != null) {
 			Writer writer = new StringWriter();
 
 			char[] buffer = new char[1024];
 			try {
 				Reader reader = new BufferedReader(new InputStreamReader(is,
 						"UTF-8"));
 				int n;
 				while ((n = reader.read(buffer)) != -1) {
 					writer.write(buffer, 0, n);
 				}
 			} catch (IOException e) {
 				return "";
 			} finally {
 				try {
 					is.close();
 				} catch (IOException e) {
 
 				}
 			}
 			return writer.toString();
 		} else {
 			return "";
 		}
 	}
 
 	public static String getFileName(String path) {
 		String imageFileName = path.substring(path.lastIndexOf('/') + 1);
 		return imageFileName;
 	}
 
 	public static void writeToFile(String rawData, String path, String fileName)
 			throws IOException {
 		File filePath = new File(path);
 		filePath.mkdirs();
 		File file = new File(path, fileName);
 		BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
 		writer.write(rawData);
 		writer.flush();
 		writer.close();
 	}
 
 	public static boolean isNetworkAvailable() {
 		WhyqApplication mobionApp = WhyqApplication.Instance();
 		android.net.ConnectivityManager connMgr = (android.net.ConnectivityManager) mobionApp
 				.getApplicationContext().getSystemService(
 						Context.CONNECTIVITY_SERVICE);
 		android.net.NetworkInfo info = connMgr.getActiveNetworkInfo();
 		return (info != null && info.isConnected());
 	}
 
 	public static void showNetworkError(Context context) {
 		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
 				context);
 		builder.setTitle(context.getString(R.string.AlertTitleWebExtLoadError));
 		builder.setMessage(context.getString(R.string.AlertMsgWebExtLoadError));
 		final android.app.AlertDialog alertError = builder.create();
 		alertError.setButton("OK", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				alertError.dismiss();
 			}
 		});
 		alertError.show();
 	}
 
 	public static Handler loginAgain = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			int value = msg.arg1;
 			View view = (View) msg.obj;
 			String mes = String.valueOf(msg.arg2);
 			Log.d("loginAgain", "=" + value);
 			loginAgain(WhyqApplication.Instance().getApplicationContext(), mes);
 		}
 	};
 
 	public static void loginAgain(final Context context, String mes) {
 		WhyqApplication.Instance().clearToken();
 		WhyqApplication.Instance().setToken(null);
 		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
 				context);
 		builder.setTitle(context.getString(R.string.app_name_title));
 		builder.setMessage(mes);
 		final android.app.AlertDialog alertError = builder.create();
 		alertError.setButton("Login", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 
 				Intent intent = new Intent(context, LoginHome.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
 				context.startActivity(intent);
 			}
 		});
 		alertError.setButton2("Cancel", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				alertError.dismiss();
 			}
 		});
 		alertError.show();
 
 	}
 
 	public static void showDialog(final Context context, String mes) {
 		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
 				context);
 		builder.setTitle(context.getString(R.string.app_name_title));
 		builder.setMessage(mes);
 		final android.app.AlertDialog alertError = builder.create();
 		alertError.setButton("Ok", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				alertError.dismiss();
 			}
 		});
 
 		alertError.show();
 
 	}
 
 	static int clearCacheFolder(final File dir, final int numDays) {
 
 		int deletedFiles = 0;
 		if (dir != null && dir.isDirectory()) {
 			try {
 				for (File child : dir.listFiles()) {
 
 					// first delete subdirectories recursively
 					if (child.isDirectory()) {
 						deletedFiles += clearCacheFolder(child, numDays);
 					}
 
 					// then delete the files and subdirectories in this dir
 					// only empty directories can be deleted, so subdirs have
 					// been done first
 					if (child.lastModified() < new Date().getTime() - numDays
 							* DateUtils.DAY_IN_MILLIS) {
 						if (child.delete()) {
 							deletedFiles++;
 						}
 					}
 				}
 			} catch (Exception e) {
 				// Log.e(TAG,
 				// String.format("Failed to clean the cache, error %s",
 				// e.getMessage()));
 			}
 		}
 		return deletedFiles;
 	}
 
 	static public boolean checkInternetConnection() {
 		final ConnectivityManager conMgr = (ConnectivityManager) WhyqApplication
 				.Instance().getApplicationContext()
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
 		if (activeNetwork != null && activeNetwork.isConnected()) {
 			// notify user you are online
 			return true;
 		} else {
 			// notify user you are not online
 			return false;
 		}
 	}
 
 	public static String generateDeviceId() {
 		final String macAddr, androidId;
 
 		WifiManager wifiMan = (WifiManager) WhyqApplication.Instance()
 				.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
 		WifiInfo wifiInf = wifiMan.getConnectionInfo();
 
 		macAddr = wifiInf.getMacAddress();
 		androidId = ""
 				+ android.provider.Settings.Secure.getString(WhyqApplication
 						.Instance().getApplicationContext()
 						.getContentResolver(),
 						android.provider.Settings.Secure.ANDROID_ID);
 
 		UUID deviceUuid = new UUID(androidId.hashCode(), macAddr.hashCode());
 		return deviceUuid.toString();
 
 		// Maybe save this: deviceUuid.toString()); to the preferences.
 	}
 
 	public static void getDeviceInfo() {
 		String debug2 = System.getProperty("os.version");
 		String debug3 = android.os.Build.VERSION.RELEASE;
 		String debug4 = android.os.Build.DEVICE;
 		String debug5 = android.os.Build.MODEL;
 		String debug6 = android.os.Build.PRODUCT;
 		String debug7 = android.os.Build.BRAND;
 		String debug8 = android.os.Build.DISPLAY;
 		String debug9 = android.os.Build.CPU_ABI;
 		String debug10 = android.os.Build.CPU_ABI2;
 		String debug11 = android.os.Build.UNKNOWN;
 		String debug12 = android.os.Build.HARDWARE;
 		String debug13 = android.os.Build.ID;
 		String debug14 = android.os.Build.MANUFACTURER;
 		String debug15 = android.os.Build.SERIAL;
 		String debug16 = android.os.Build.USER;
 		String debug17 = android.os.Build.HOST;
 	}
 
 	public static String encryptToken(String token) throws InvalidKeyException,
 			NoSuchAlgorithmException, NoSuchPaddingException,
 			IllegalBlockSizeException, BadPaddingException,
 			UnsupportedEncodingException {
 		RSA rsa = new RSA();
 		return rsa.RSAEncrypt(token);
 	}
 
 	public static String decryptToken(String token) throws InvalidKeyException,
 			NoSuchAlgorithmException, NoSuchPaddingException,
 			IllegalBlockSizeException, BadPaddingException,
 			UnsupportedEncodingException {
 		RSA rsa = new RSA();
 		return rsa.RSADecrypt(token);
 	}
 
 	public static void applyTypeface(View view, Typeface tf) {
 		if (view instanceof TextView) {
 			((TextView) view).setTypeface(tf);
 		} else if (view instanceof ViewGroup) {
 			ViewGroup group = (ViewGroup) view;
 			for (int i = 0, count = group.getChildCount(); i < count; i++) {
 				applyTypeface(group.getChildAt(i), tf);
 			}
 		}
 	}
 
 	public static Bundle getLocation(Context activity) {
 		Geocoder geocoder;
 		String bestProvider;
 		List<Address> user = null;
 		double lat;
 		double lng;
 		Bundle bundle = new Bundle();
 		LocationManager lm = (LocationManager) activity
 				.getSystemService(Context.LOCATION_SERVICE);
 
 		Criteria criteria = new Criteria();
 		bestProvider = lm.getBestProvider(criteria, false);
 		Location location = lm.getLastKnownLocation(bestProvider);
 
 		if (location == null) {
 			// Toast.makeText(activity,"Location Not found",Toast.LENGTH_LONG).show();
 		} else {
 			geocoder = new Geocoder(activity);
 			try {
 				user = geocoder.getFromLocation(location.getLatitude(),
 						location.getLongitude(), 1);
 				lat = (double) user.get(0).getLatitude();
 				lng = (double) user.get(0).getLongitude();
 				bundle.putString("lat", "" + String.valueOf(lat));
 				bundle.putString("lon", "" + String.valueOf(lng));
 				System.out.println(" DDD lat: " + lat + ",  longitude: " + lng);
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return bundle;
 	}
 
 	public static boolean checkLocationSetting(Context context) {
 		LocationManager locationManager = (LocationManager) context
 				.getSystemService(Context.LOCATION_SERVICE);
 		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {// ||
 																				// locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
 		// Toast.makeText(context, "GPS is Enabled in your devide",
 		// Toast.LENGTH_SHORT).show();
 			return true;
 		} else {
 //			turnGPSOn();
			turnGPSOff();
 			showGPSDisabledAlertToUser(context);
 			return false;
 		}
 	}
 
 	private static void showGPSDisabledAlertToUser(final Context context) {
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
 				context);
 		alertDialogBuilder
 				.setMessage(
 						"GPS is disabled in your device. Would you like to enable it?")
 				.setCancelable(false)
 				.setPositiveButton("Goto Settings Page To Enable GPS",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								Intent callGPSSettingIntent = new Intent(
 										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 								context.startActivity(callGPSSettingIntent);
 								dialog.cancel();
 							}
 						});
 		alertDialogBuilder.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 		AlertDialog alert = alertDialogBuilder.create();
 		alert.show();
 	}
 
 	/**
 	 * Convenience method to adjust font size to fit the given height.
 	 * 
 	 * @param tv
 	 *            The text view to be adjust text size.
 	 * @param height
 	 *            the expect height of text.
 	 */
 	public static void adjustTextSizeByTextHeight(TextView tv, int height) {
 		final String text = "dp";
 		float textSize = tv.getTextSize();
 
 		TextPaint tp = tv.getPaint();
 		Rect bound = new Rect();
 		tp.getTextBounds(text, 0, text.length(), bound);
 
 		if (Math.abs(bound.bottom - bound.top) > height) {
 			while (Math.abs(bound.bottom - bound.top) > height) {
 				textSize--;
 				tp.setTextSize(textSize);
 				tp.getTextBounds(text, 0, text.length(), bound);
 			}
 		}
 		if (Math.abs(bound.bottom - bound.top) < height) {
 			while (Math.abs(bound.bottom - bound.top) < height) {
 				textSize++;
 				tp.setTextSize(textSize);
 				tp.getTextBounds(text, 0, text.length(), bound);
 			}
 			textSize--;
 		}
 	}
 
 	public static String getTimeZone(String value) {
 		TimeZone tz = TimeZone.getDefault();
 		System.out.println("TimeZone   "
 				+ tz.getDisplayName(false, TimeZone.SHORT) + " Timezon id :: "
 				+ tz.getID());
 		return tz.getID();
 	}
 
 	public static HashMap<String, String> getLatLonFromAddress(String address) {
 		JSONObject obj = getLocationInfo(address);
 		if (obj != null) {
 			return getLatLong(obj);
 		} else {
 			return null;
 		}
 	}
 
 	public static JSONObject getLocationInfo(String address) {
 		StringBuilder stringBuilder = new StringBuilder();
 		try {
 
 			address = address.replaceAll(" ", "%20");
 
 			HttpPost httppost = new HttpPost(
 					"http://maps.google.com/maps/api/geocode/json?address="
 							+ address + "&sensor=false");
 			HttpClient client = new DefaultHttpClient();
 			HttpResponse response;
 			stringBuilder = new StringBuilder();
 
 			response = client.execute(httppost);
 			HttpEntity entity = response.getEntity();
 			InputStream stream = entity.getContent();
 			int b;
 			while ((b = stream.read()) != -1) {
 				stringBuilder.append((char) b);
 			}
 		} catch (ClientProtocolException e) {
 		} catch (IOException e) {
 		}
 
 		JSONObject jsonObject = new JSONObject();
 		try {
 			jsonObject = new JSONObject(stringBuilder.toString());
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return jsonObject;
 	}
 
 	public static HashMap<String, String> getLatLong(JSONObject jsonObject) {
 
 		try {
 			HashMap<String, String> data = new HashMap<String, String>();
 			double longitute = ((JSONArray) jsonObject.get("results"))
 					.getJSONObject(0).getJSONObject("geometry")
 					.getJSONObject("location").getDouble("lng");
 
 			double latitude = ((JSONArray) jsonObject.get("results"))
 					.getJSONObject(0).getJSONObject("geometry")
 					.getJSONObject("location").getDouble("lat");
 			data.put("lat", "" + latitude);
 			data.put("lon", "" + longitute);
 			return data;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return null;
 		}
 
 	}
 //
	public static void turnGPSOn() {
 
 		Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
 		intent.putExtra("enabled", true);
 		WhyqApplication.Instance().sendBroadcast(intent);
 
 		String provider = Settings.Secure.getString(WhyqApplication.Instance()
 				.getContentResolver(),
 				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
 		if (!provider.contains("gps")) { // if gps is disabled
 			final Intent poke = new Intent();
 			poke.setClassName("com.android.settings",
 					"com.android.settings.widget.SettingsAppWidgetProvider");
 			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
 			poke.setData(Uri.parse("3"));
 			WhyqApplication.Instance().sendBroadcast(poke);
 
 		}
 	}
 
	public static void turnGPSOff() {
 		String provider = Settings.Secure.getString(WhyqApplication.Instance()
 				.getContentResolver(),
 				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
 		if (provider.contains("gps")) { // if gps is enabled
 			final Intent poke = new Intent();
 			poke.setClassName("com.android.settings",
 					"com.android.settings.widget.SettingsAppWidgetProvider");
 			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
 			poke.setData(Uri.parse("3"));
 			WhyqApplication.Instance().sendBroadcast(poke);
 		}
 	}
 }
