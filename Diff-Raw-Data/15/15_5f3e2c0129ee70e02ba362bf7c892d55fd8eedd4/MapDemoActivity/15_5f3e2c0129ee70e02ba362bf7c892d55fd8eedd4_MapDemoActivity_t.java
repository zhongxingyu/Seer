 package edu.vanderbilt.cs282.feisele;
 
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Editable;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 /**
  * An activity which prompts the user for latitude and longitude. These values
  * are used as part of a location intent. The resulting intent is used to start
  * an activity.
  * 
  * <p>
  * This behavior can be investigated with the activity manager "am".
  * 
  * @author Fred Eisele <phreed@gmail.com>
  * 
  */
 public class MapDemoActivity extends LifecycleLoggingActivity {
 	static private final String TAG = "MapDemo";
 
 	/**
 	 * Extract the latitude determining if it is valid.
 	 * <p> Recall that the Mercator projection
 	 * does not extend to the poles.
 	 */
 	private double LATITUDE_MIN = -90.0;
 	private double LATITUDE_MERC_MIN = -85.05;
 	private double LATITUDE_MERC_MAX = 85.05;
 	private double LATITUDE_MAX = 90.0;
 
 	private Double extractLatitude(EditText view) {
 		final Editable latitudeEditable = view.getText();
 		final String latitudeString = latitudeEditable.toString();
 		final double latitude;
 		try {
 			latitude = Double.parseDouble(latitudeString);
 		} catch (NumberFormatException ex) {
 			final String faultMsg = this.getResources().getString(R.string.latitude_format_error );
 			view.setError(faultMsg);
 			return Double.NaN;
 		}
 		if (latitude < LATITUDE_MIN) {
 			final String faultMsg = this.getResources().getString(R.string.latitude_lower_bound_error );
 			view.setError(faultMsg);
 			return Double.NaN;
 		}
 		if (LATITUDE_MAX < latitude) {
 			final String faultMsg = this.getResources().getString(R.string.latitude_upper_bound_error );
 			view.setError(faultMsg);
 			return Double.NaN;
 		}
 		if (latitude < LATITUDE_MERC_MIN) {
                        final Drawable dr = this.getResources().
                                getDrawable(R.drawable.indicator_input_warn);

                        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight())
			final String faultMsg = this.getResources().
                                getString(R.string.latitude_mercator_lower_bound_error );
			view.setError(faultMsg, dr);
 			Toast.makeText(MapDemoActivity.this,
 					R.string.latitude_mercator_lower_bound_warn,
 					Toast.LENGTH_LONG).show();
 		}
 		if (LATITUDE_MERC_MAX < latitude) {
                        final Drawable dr = this.getResources().
                                getDrawable(R.drawable.indicator_input_warn);

                        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight())
			final String faultMsg = this.getResources().
                                getString(R.string.latitude_mercator_upper_bound_error );
			view.setError(faultMsg, dr);
 			Toast.makeText(MapDemoActivity.this,
 					R.string.latitude_mercator_upper_bound_warn,
 					Toast.LENGTH_LONG).show();
 		}
 		return latitude;
 	}
 
 	/**
 	 * Extract the longitude determining if it is valid.
 	 */
 	private double LONGITUDE_MIN = -180.0;
 	private double LONGITUDE_MAX = 180.0;
 
 	private Double extractLongitude(EditText view) {
 		final Editable longitideEditable = view.getText();
 		final String longitudeString = longitideEditable.toString();
 		final double longitude;
 		try {
 			longitude = Double.parseDouble(longitudeString);
 		} catch (NumberFormatException ex) {
 			final String faultMsg = this.getResources().getString(R.string.longitude_format_error );
 			view.setError(faultMsg);
 			return Double.NaN;
 		}
 		if (longitude < LONGITUDE_MIN) {
 			final String faultMsg = this.getResources().getString(R.string.longitude_lower_bound_error );
 			view.setError(faultMsg);
 			return Double.NaN;
 		}
 		if (LONGITUDE_MAX < longitude) {
 			final String faultMsg = this.getResources().getString(R.string.longitude_upper_bound_error );
 			view.setError(faultMsg);
 			return Double.NaN;
 		}
 		return longitude;
 	}
 
 	/**
 	 * There are some choices for the location schema.
 	 * <p>
 	 * <h2>Geo URI</h2>
 	 * <p>
 	 * A Geo URI is "a Uniform Resource Identifier (URI) for geographic
 	 * locations using the 'geo' scheme name. A 'geo' URI identifies a physical
 	 * location in a two- or three-dimensional coordinate reference system in a
 	 * compact, simple, human-readable, and protocol-independent way." -- RFC
 	 * 5870 e.g. geo:36.16,-86.16
 	 * <h2>Google Maps</h2>
 	 * <p>
 	 * This is not a standard but it is in common use. e.g.
 	 * http://maps.google.com/?q=36.16,-86.16
 	 * 
 	 */
 
 	static final private String geoUriPrefix = "geo:";
 	static final private String gmapsUriPrefix = "http://maps.google.com/?q=";
 
 	static private Uri getLocationUri(String prefix, double latitude,
 			double longitude) {
 		final StringBuilder sb = new StringBuilder(prefix)
 				.append(latitude).append(",").append(longitude);
 		return Uri.parse(sb.toString());
 	}
 
 	private EditText latitudeView;
 	private EditText longitudeView;
 
 	/**
 	 * The fields are checked that they are numerical and are within the normal
 	 * bounds for latitude and longitude.
 	 * 
 	 * @param savedInstanceState
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map_demo);
 
 		this.latitudeView = (EditText) findViewById(R.id.edit_latitude);
 		this.longitudeView = (EditText) findViewById(R.id.edit_longitude);
 
 	}
 
 	/**
 	 * Performed when the show location button is clicked.
 	 * - extract and validate the latitude and longitude
 	 * - try starting activities with various intents
 	 * 
 	 * @param view
 	 *            the button view object (unused)
 	 */
 	public void showLocation(View view) {
 
 		final Double latitude = extractLatitude(this.latitudeView);
 		final Double longitude = extractLongitude(this.longitudeView);
 		if (longitude.isNaN() || latitude.isNaN())
 			return;
 
 		final Uri geoUri = getLocationUri(geoUriPrefix, latitude, longitude);
 		Log.d(TAG, "starting activity with " + geoUri.toString());
 
 		try {
 			final Intent locateIntent = new Intent(
 					android.content.Intent.ACTION_VIEW, geoUri);
 			startActivity(locateIntent);
 			// Activity started successfully
 			return;
 		} catch (ActivityNotFoundException ex) {
 			Log.w(TAG, "no application to handle the intent " + geoUri.toString());
 		}
 
 		final Uri gmapsUri = getLocationUri(gmapsUriPrefix, latitude, longitude);
 		Log.d(TAG, "starting activity with " + gmapsUri.toString());
 
 		try {
 			final Intent locateIntent = new Intent(
 					android.content.Intent.ACTION_VIEW, gmapsUri);
 			startActivity(locateIntent);
 			// Activity started successfully
 			return;
 		} catch (ActivityNotFoundException ex) {
 			Log.w(TAG, "no application to handle the intent " + gmapsUri.toString());
 			Toast.makeText(MapDemoActivity.this, R.string.no_app_capable,
 					Toast.LENGTH_LONG).show();
 		}
 	}
 
 }
