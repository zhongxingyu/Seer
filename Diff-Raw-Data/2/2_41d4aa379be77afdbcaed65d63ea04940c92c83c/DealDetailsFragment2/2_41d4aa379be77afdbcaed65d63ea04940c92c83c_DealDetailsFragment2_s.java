 package nabhack.localz.activity;
 
 import java.util.concurrent.TimeUnit;
 
 import nabhack.localz.LocalzApp;
 import nabhack.localz.R;
 import nabhack.localz.models.Deal;
 import android.content.IntentSender;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.LocationSource;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.App;
 import com.googlecode.androidannotations.annotations.EFragment;
 import com.googlecode.androidannotations.annotations.FragmentById;
 import com.googlecode.androidannotations.annotations.ViewById;
 
 /**
  * A dummy fragment representing a section of the app, but that simply displays
  * dummy text.
  */
 @EFragment(R.layout.fragment_deal_details2)
 public class DealDetailsFragment2 extends Fragment implements
 		GooglePlayServicesClient.ConnectionCallbacks,
 		GooglePlayServicesClient.OnConnectionFailedListener {
 
 	private static final String TAG = DealDetailsFragment2.class
 			.getSimpleName();
 
 	@App
 	LocalzApp application;
 
 	@ViewById(R.id.title)
 	TextView title;
 
 	@ViewById(R.id.image)
 	ImageView image;
 
 	@ViewById(R.id.special)
 	TextView special;
 
 	@ViewById(R.id.time)
 	TextView time;
 
 	@ViewById(R.id.remaining)
 	TextView remaining;
 
 	@ViewById(R.id.description)
 	TextView description;
 
 	@ViewById(R.id.where)
 	TextView where;
 
 	@ViewById(R.id.url)
 	TextView url;
 
 	private Deal deal;
 
 	@FragmentById(R.id.shop_map)
 	SupportMapFragment mapFragment;
 
 	private GoogleMap mMap;
 
 	private LocationClient mLocationClient;
 
 	/*
 	 * Define a request code to send to Google Play services This code is
 	 * returned in Activity.onActivityResult
 	 */
 	private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
 
 	public DealDetailsFragment2() {
 	}
 
 	public void setDeal(Deal deal) {
 		this.deal = deal;
 	}
 
 	@Override
 	public void onCreate(Bundle arg0) {
 		mLocationClient = new LocationClient(getActivity(), this, this);
 		super.onCreate(arg0);
 	}
 
 	@AfterViews
 	void setupView() {
 		setUpMapIfNeeded();
 		setUpMap();
 		title.setText(deal.getTitle());
 		description.setText(deal.getDescription());
 		url.setText(deal.getStore().getUrl());
 		where.setText("WHERE IS " + deal.getStore().getName().toUpperCase());
 		
 		// Comment next block when data available online
 		String uri = "drawable/"
 				+ deal.getDescImgs()[0].replaceFirst("[.][^.]+$", "");
 		int imageResource = getResources().getIdentifier(uri, null,
 				getActivity().getPackageName());
 		Drawable drawImage = getActivity().getResources().getDrawable(
 				imageResource);
 		image.setImageDrawable(drawImage);
 
 		if (deal.getQuantityLimit() == 0) {
 			remaining.setVisibility(View.GONE);
 		} else {
 			remaining.setText(deal.getQuantityLimit()
 					+ " Remaining");
 			remaining.setVisibility(View.VISIBLE);
 		}
 		if (deal.getSecondsToExpire() == 0) {
 			time.setVisibility(View.GONE);
 		} else {
 			time.setVisibility(View.VISIBLE);
 			time.setText(getTimeFormat(deal.getSecondsToExpire()));
 			timerCountDown(deal.getSecondsToExpire(), time);
 		}
 	}
 
 	public void timerCountDown(final int secondCount, final TextView textView) {
 
 		Handler handler = new Handler();
 		handler.postDelayed(new Runnable() {
 			public void run() {
 				if (secondCount > 0) {
 					// format time and display
 					textView.setText(getTimeFormat(secondCount));
 					timerCountDown(secondCount - 1, textView);
 				} else {
 
 				}
 			}
 		}, 1000);
 	}
 
 	private String getTimeFormat(int secs) {
 		int hours = secs / 3600, remainder = secs % 3600, minutes = remainder / 60, seconds = remainder % 60;
 
 		String disHour = (hours < 10 ? "0" : "") + hours, disMinu = (minutes < 10 ? "0"
 				: "")
 				+ minutes, disSec = (seconds < 10 ? "0" : "") + seconds;
 		return disHour + ":" + disMinu + ":" + disSec;
 	}
 
 	private void setUpMapIfNeeded() {
 		if (mMap == null) {
 			mMap = mapFragment.getMap();
 			mMap.setLocationSource(new MockLocationSource());
 		}
 	}
 
 	private void setUpMap() {
 		mMap.setIndoorEnabled(true);
 		mMap.setMyLocationEnabled(true);

		application.setCurrentDeal(application.getDeal(0));
 		moveCameraToPositionAndAddMarker();
 	}
 
 	private void moveCameraToPositionAndAddMarker() {
 		mMap.clear();
 		LatLng dealLatLng = new LatLng(application.getCurrentDeal()
 				.getLocation().getLat(), application.getCurrentDeal()
 				.getLocation().getLng());
 
 		mMap.addMarker(
 				new MarkerOptions().position(dealLatLng).title(
 						application.getCurrentDeal().getTitle()))
 				.showInfoWindow();
 
 		CameraPosition cameraPosition = new CameraPosition.Builder()
 				.target(dealLatLng).zoom(18).tilt(60) // Sets the tilt of the
 														// camera
 														// to 60 degrees
 				.build(); // Creates a CameraPosition from the builder
 		mMap.animateCamera(CameraUpdateFactory
 				.newCameraPosition(cameraPosition));
 	}
 
 	@Override
 	public void onConnectionFailed(ConnectionResult connectionResult) {
 		Log.w(TAG, "Failed to connect to Google Play services");
 		/*
 		 * Google Play services can resolve some errors it detects. If the error
 		 * has a resolution, try sending an Intent to start a Google Play
 		 * services activity that can resolve error.
 		 */
 		if (connectionResult.hasResolution()) {
 			try {
 				// Start an Activity that tries to resolve the error
 				connectionResult.startResolutionForResult(getActivity(),
 						CONNECTION_FAILURE_RESOLUTION_REQUEST);
 				/*
 				 * Thrown if Google Play services canceled the original
 				 * PendingIntent
 				 */
 			} catch (IntentSender.SendIntentException e) {
 				// Log the error
 				Log.w(TAG,
 						"Failed to connect to Google Play services"
 								+ e.getMessage());
 			}
 		}
 	}
 
 	@Override
 	public void onConnected(Bundle arg0) {
 		mLocationClient.setMockMode(true);
 	}
 
 	@Override
 	public void onDisconnected() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public class MockLocationSource implements LocationSource {
 
 		private final Handler handler = new Handler();
 		private OnLocationChangedListener listener;
 
 		private void scheduleNewFix() {
 			handler.postDelayed(updateLocationRunnable,
 					TimeUnit.SECONDS.toMillis(2));
 		}
 
 		private final Runnable updateLocationRunnable = new Runnable() {
 
 			@Override
 			public void run() {
 				android.location.Location randomLocation = generateRandomLocation();
 				listener.onLocationChanged(randomLocation);
 				scheduleNewFix();
 			}
 		};
 
 		public android.location.Location generateRandomLocation() {
 
 			android.location.Location mockedLocation = new android.location.Location(
 					getClass().getSimpleName());
 
 			mockedLocation.setLatitude(-37.885795);
 			mockedLocation.setLongitude(145.083812);
 			mockedLocation.setAccuracy(1);
 
 			return mockedLocation;
 		}
 
 		@Override
 		public void activate(OnLocationChangedListener locationChangedListener) {
 			listener = locationChangedListener;
 			scheduleNewFix();
 		}
 
 		@Override
 		public void deactivate() {
 			handler.removeCallbacks(updateLocationRunnable);
 		}
 
 	}
 
 }
