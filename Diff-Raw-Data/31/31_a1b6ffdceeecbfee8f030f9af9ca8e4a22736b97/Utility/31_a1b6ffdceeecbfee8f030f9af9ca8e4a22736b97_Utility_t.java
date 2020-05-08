 package mobidev.parkingfinder;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.json.JSONException;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnClickListener;
 import android.graphics.drawable.Drawable;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Log;
 
 public class Utility {
 
 	private static final String MY_PREFERENCES = "MyPref";
 	private static final String PREFERENCE_RANGE = "my_range";
 	private static final int FIVE_MINUTES = 300000;
 	private static final String PREFERENCE_FILTER_UNDEFINED = "undefined";
 	private static final String PREFERENCE_FILTER_FREE = "free";
 	private static final String PREFERENCE_FILTER_TOLL = "toll";
 	private static final String PREFERENCE_FILTER_RESIDENT = "resident";
 	private static final String PREFERENCE_FILTER_DISABLED = "disabled";
 	private static final String PREFERENCE_FILTER_TIMED = "timed";
 
 	public static String getDigest(String pw) {
 		MessageDigest digester;
 		try {
 			digester = MessageDigest.getInstance("MD5");
 			digester.update(pw.getBytes());
 			return String.format("%032x", new BigInteger(1, digester.digest()));
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static boolean isOnline(Context c) {
 		ConnectivityManager cm = (ConnectivityManager) c
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
 			return true;
 		}
 		return false;
 	}
 
 	public static void showDialog(String title, String message, Context c) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(c);
 		builder.setMessage(message).setCancelable(false).setTitle(title)
 				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 		builder.show();
 	}
 
	public static void showDialog(String title, String message, Context c, OnClickListener positiveAction) {
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setMessage(message).setCancelable(false).setTitle(title)
				.setPositiveButton(R.string.ok, positiveAction);
		builder.show();
	}
	
 	public static void showDialog(String title, String message, Context c,
 			OnClickListener positiveAction, OnClickListener negativeAction) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(c);
 		builder.setMessage(message).setTitle(title)
				.setPositiveButton(R.string.ok, positiveAction)
 				.setNegativeButton(R.string.cancel, negativeAction);
 		builder.show();
 	}
 
 	public static GeoPoint location2geopoint(Location loc) {
 		return new GeoPoint((int) (loc.getLatitude() * 1E6),
 				(int) (loc.getLongitude() * 1E6));
 	}
 
 	public static void centerMap(GeoPoint gp, MapView mapview, boolean release) {
 		MapController mapc = mapview.getController();
 		mapview.invalidate();
 
 		List<Overlay> overlays = mapview.getOverlays();
 
 		if (release && overlays.size() > 1) { // mostrare la mia posizione e
 												// quella dell'auto
 			MyItemizedOverlay itemizedOverlay = (MyItemizedOverlay) overlays
 					.get(1);
 			mapc.animateTo(itemizedOverlay.getItem(0).getPoint());
 			mapc.setZoom(17);
 		} else {
 			mapc.animateTo(gp);
 			mapc.setZoom(18);
 		}
 	}
 
 	public static void centerMap(Location loc, MapView mapview, boolean release) {
 		GeoPoint gp = location2geopoint(loc);
 		centerMap(gp, mapview, release);
 	}
 
 	public static String getStreetName(Context c, double latitude,
 			double longitude) {
 		Geocoder geocoder = new Geocoder(c, Locale.ITALIAN);
 
 		try {
 			List<Address> addresses = geocoder.getFromLocation(latitude,
 					longitude, 1);
 
 			if (addresses != null) {
 				Address returnedAddress = addresses.get(0);
 				String strReturnedAddress = returnedAddress.getAddressLine(0);
 				return strReturnedAddress;
 			} else {
 				return "N/A";
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			return "N/A";
 		}
 	}
 
 	public static void askParkings(Location myLocation, MapView map,
 			MyItemizedOverlay itemizedoverlay) {
 		Context c = map.getContext();
 		SharedPreferences prefs = c.getSharedPreferences(MY_PREFERENCES,
 				Context.MODE_PRIVATE);
 
 		float range = prefs.getInt(PREFERENCE_RANGE, 3);
 
 		String response;
 		try {
 			response = CommunicationController.sendRequest("searchParking",
 					DataController.marshallParkingRequest(
 							myLocation.getLatitude(),
 							myLocation.getLongitude(), range / 1000));
 
 			itemizedoverlay.clear();
 
 			ArrayList<Parking> parkings;
 
 			try {
 
 				Log.i("SIZE", String.valueOf(map.getOverlays().size()));
 
 				parkings = DataController.unMarshallParking(response);
 
 				for (Parking parking : parkings)
 					showParking(map, parking, itemizedoverlay);
 
 				map.postInvalidate();
 
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 	private static void showParking(MapView mapView, Parking p,
 			MyItemizedOverlay item) {
 		Drawable drawable;
 		Context c = mapView.getContext();
 		SharedPreferences prefs = c.getSharedPreferences(MY_PREFERENCES,
 				Context.MODE_PRIVATE);
 
 		switch (p.getType()) {
 		case 1:
 			if (!prefs.getBoolean(PREFERENCE_FILTER_FREE, true))
 				return;
 			drawable = c.getResources().getDrawable(R.drawable.free_park);
 			break;
 		case 2:
 			if (!prefs.getBoolean(PREFERENCE_FILTER_TOLL, true))
 				return;			
 			drawable = c.getResources().getDrawable(R.drawable.toll_park);
 			break;
 		case 3:
 			if (!prefs.getBoolean(PREFERENCE_FILTER_RESIDENT, true))
 				return;			
 			drawable = c.getResources().getDrawable(R.drawable.reserved_park);
 			break;
 		case 4:
 			if (!prefs.getBoolean(PREFERENCE_FILTER_DISABLED, true))
 				return;			
 			drawable = c.getResources().getDrawable(R.drawable.disabled_park);
 			break;
 		case 5:
 			if (!prefs.getBoolean(PREFERENCE_FILTER_TIMED, true))
 				return;			
 			drawable = c.getResources().getDrawable(R.drawable.timed_park);
 			break;
 		default:
 			if (!prefs.getBoolean(PREFERENCE_FILTER_UNDEFINED, true))
 				return;			
 			drawable = c.getResources().getDrawable(R.drawable.undefined_park);
 			break;
 		}
 
 		long duration = System.currentTimeMillis() - p.getDate().getTime();
 
		if (duration < FIVE_MINUTES)
			drawable.setAlpha(255);
		else if (duration > FIVE_MINUTES && duration < FIVE_MINUTES * 2)
 			drawable.setAlpha(200);
 		else if (duration > FIVE_MINUTES * 2)
 			drawable.setAlpha(100);
		
 		String title = "Parking #" + p.getId();
 		String snippet = "Lat: " + p.getLatitude() + "\nLon: "
 				+ p.getLongitude() + "\nFree since: "
 				+ TimeUtils.millisToLongDHMS(duration);
 
 		item.addOverlayItem((int) (p.getLatitude() * 1E6),
 				(int) (p.getLongitude() * 1E6), title, snippet, drawable);
 	}
 }
