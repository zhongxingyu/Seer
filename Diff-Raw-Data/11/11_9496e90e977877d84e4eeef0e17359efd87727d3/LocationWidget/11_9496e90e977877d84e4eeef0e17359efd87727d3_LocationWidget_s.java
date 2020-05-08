 package com.mridang.location;
 
 import java.util.List;
 import java.util.Random;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.util.Log;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 
 /*
  * This class is the main class that provides the widget
  */
 public class LocationWidget extends DashClockExtension {
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
 	 */
 	public void onCreate() {
 
 		super.onCreate();
 		Log.d("LocationWidget", "Created");
 		BugSenseHandler.initAndStartSession(this, "b6e8d53d");
 
 	}
 
 	/*
 	 * @see
 	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
 	 * (int)
 	 */
 	@Override
	protected void onUpdateData(int arg0) {

		setUpdateWhenScreenOn(true);
 
 		Log.d("LocationWidget", "Getting the location of the device");
 		final ExtensionData edtInformation = new ExtensionData();
		edtInformation.visible(false);
 
 		try {
 
 			Log.d("LocationWidget", "Fetching the most recent and accurate fix");
 			LocationManager mgrLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 			List<String> lstProviders = mgrLocation.getProviders(true);
 			if (lstProviders != null) {
 
 				Location locNewest = null;
 				for (String strProvider : lstProviders) {
 
 					Location locLocation = mgrLocation.getLastKnownLocation(strProvider);
 					if (locLocation != null) {
 
 						if (locNewest == null) {
 
 							locNewest = locLocation;
 
 						} else {
 
 							if (locLocation.getTime() > locNewest.getTime()) {
 
 								locNewest = locLocation;
 
 							}
 
 						}
 
 						//mgrLocation.requestLocationUpdates(provider, 0, 0, this);
 
 					}
 
 				}
 
 				final Float fltAccuracy = locNewest.getAccuracy();
 
 				Log.d("LocationWidget", "Making the rrequest to reverse geocode the coordinates");
 				new AsyncHttpClient()
 				.get("http://maps.googleapis.com/maps/api/geocode/json?latlng=" + locNewest.getLatitude() + "," + locNewest.getLongitude() + "&sensor=false",
 						new AsyncHttpResponseHandler() {
 
 					@Override
 					public void onSuccess(String strResponse) {
 
 						Log.v("LocationWidget", "Server reponded with: " + strResponse);
 
 						try {
 
 							JSONObject jsoResponse = new JSONObject(strResponse);
 							if (jsoResponse.getString("status").equalsIgnoreCase("OK")) {
 
 								edtInformation.expandedBody("");
 								edtInformation.expandedTitle(getString(R.string.location_info, fltAccuracy.intValue()));
 
 								JSONArray jsoResults = jsoResponse.getJSONArray("results");
 								Integer intMinimum = Integer.MAX_VALUE;
 
 								for (Integer intResult = 0; intResult < jsoResults.length(); intResult++) {
 
 									JSONObject jsoBounds = jsoResults.getJSONObject(intResult).getJSONObject("geometry").getJSONObject("viewport");
 
 									float[] fltDistances = new float[1];
 
 									Location.distanceBetween(
 											Double.parseDouble(jsoBounds.getJSONObject("northeast").getString("lat")),
 											Double.parseDouble(jsoBounds.getJSONObject("northeast").getString("lng")),
 											Double.parseDouble(jsoBounds.getJSONObject("southwest").getString("lat")),
 											Double.parseDouble(jsoBounds.getJSONObject("southwest").getString("lng")),
 											fltDistances);
 
 									if (((int) fltDistances[0] - fltAccuracy) < intMinimum) {
 
 										intMinimum = (int) (fltDistances[0] - fltAccuracy);
 										edtInformation.expandedBody(jsoResults.getJSONObject(intResult).getString("formatted_address"));
 
 									}
 
 								}
 
 								edtInformation.visible(true);
 								publishUpdate(edtInformation);
 
 							} else {
 								Log.w("LocationWidget", "Server encountered an error");
 								throw new Exception("Server encountered an error");
 							}
 
 						} catch (Exception e) {
 							Log.e("LocationWidget", "Encountered an error", e);
 							BugSenseHandler.sendException(e);
 						}
 
 					}
 
 				});
 
 			}
 
 			if (new Random().nextInt(5) == 0) {
 
 				PackageManager mgrPackages = getApplicationContext().getPackageManager();
 
 				try {
 
 					mgrPackages.getPackageInfo("com.mridang.donate", PackageManager.GET_META_DATA);
 
 				} catch (NameNotFoundException e) {
 
 					Integer intExtensions = 0;
 
 					for (PackageInfo pkgPackage : mgrPackages.getInstalledPackages(0)) {
 
 						intExtensions = intExtensions + (pkgPackage.applicationInfo.packageName.startsWith("com.mridang.") ? 1 : 0); 
 
 					}
 
 					if (intExtensions > 1) {
 
 						edtInformation.visible(true);
 						edtInformation.clickIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=com.mridang.donate")));
 						edtInformation.expandedTitle("Please consider a one time purchase to unlock.");
 						edtInformation.expandedBody("Thank you for using " + intExtensions + " extensions of mine. Click this to make a one-time purchase or use just one extension to make this disappear.");
 						setUpdateWhenScreenOn(true);
 
 					}
 
 				}
 
 			} else {
 				setUpdateWhenScreenOn(true);
 			}
 
 		} catch (Exception e) {
 			Log.e("LocationWidget", "Encountered an error", e);
 			BugSenseHandler.sendException(e);
 		}
 
 		edtInformation.icon(R.drawable.ic_dashclock);
 		publishUpdate(edtInformation);
 		Log.d("LocationWidget", "Done");
 
 	}
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
 	 */
 	public void onDestroy() {
 
 		super.onDestroy();
 		Log.d("LocationWidget", "Destroyed");
 		BugSenseHandler.closeSession(this);
 
 	}
 
 }
