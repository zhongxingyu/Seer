 package com.papagiannis.tuberun;
 
 import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 import android.location.Address;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.ListFragment;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TabHost;
 import android.widget.TextView;
 
 import com.papagiannis.tuberun.fetchers.Observer;
 import com.papagiannis.tuberun.fetchers.ReverseGeocodeFetcher;
 import com.papagiannis.tuberun.plan.Plan;
 import com.papagiannis.tuberun.stores.DestinationStore;
 
 public class PlanActivity extends FragmentActivity implements LocationListener  {
 	private final static int ADD_HOME_ERROR = -5;
 	final PlanActivity self = this;
 	private static Plan plan = new Plan();
 	DestinationStore<Destination> store = DestinationStore.getInstance();
 	
 	ReverseGeocodeFetcher geocoder=new ReverseGeocodeFetcher(this, null);
 	Observer geolocationObserver=new Observer() {
 		@Override
 		public void update() {
 			displayLocation(geocoder.getResult());
 		}
 	};
 
 	LinearLayout mainmenu_layout;
 	Button back_button;
 	Button logo_button;
 	TextView title_textview;
 	Button go_home_empty_button;
 	Button go_home_full_button;
 	TextView location_textview;
 	TextView location_accuracy_textview;
 	LinearLayout location_layout;
 	ProgressBar location_progressbar;
 	
 
 	TabHost mTabHost;
 	ViewPager mViewPager;
 	TabsAdapter mTabsAdapter;
 	PlanFragment planFragment;
 	ListFragment savedFragment;
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.plan);
 		setupTabHost(savedInstanceState);
 		plan = new Plan();
 		create();
 		updateHomeButton();
 		
 	}
 	
 	
 	private void setupTabHost(Bundle savedInstanceState) {
 		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		
 		mTabHost.setup();
     	mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
     	mTabsAdapter.addTab(
 				mTabHost.newTabSpec("New").setIndicator("new"),
 				PlanFragment.class, null);
 		mTabsAdapter.addTab(
 				mTabHost.newTabSpec("Saved").setIndicator("old"),
 				PlanStoredFragment.class, null);
 		
 		planFragment = (PlanFragment)  mTabsAdapter.getItem(0);
 		savedFragment = (PlanStoredFragment) mTabsAdapter.getItem(1);
 		
 		if (savedInstanceState != null) {
 			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
 		}
 	}
 	
 	
 	private void createReferences() {
 		mainmenu_layout = (LinearLayout) findViewById(R.id.mainmenu_layout);
 		back_button = (Button) findViewById(R.id.back_button);
 		logo_button = (Button) findViewById(R.id.logo_button);
 		title_textview = (TextView) findViewById(R.id.title_textview);
 		title_textview = (TextView) findViewById(R.id.title_textview);
 		go_home_empty_button = (Button) findViewById(R.id.go_home_empty_button);
 		go_home_full_button = (Button) findViewById(R.id.go_home_full_button);
 		location_textview = (TextView) findViewById(R.id.location_textview);
 		location_accuracy_textview = (TextView) findViewById(R.id.location_accuracy_textview);
 		location_progressbar = (ProgressBar) findViewById(R.id.location_progressbar);
 		location_layout = (LinearLayout) findViewById(R.id.location_layout);
 	}
 
 	private void create() {
 		createReferences();
 
 		// Setup handlers for the titlebar actions
 		OnClickListener back_listener = new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				self.finish();
 			}
 		};
 		back_button.setOnClickListener(back_listener);
 		logo_button.setOnClickListener(back_listener);
 
 		// Setup the location manager
 		locationManager = (LocationManager) this
 				.getSystemService(Context.LOCATION_SERVICE);
 		requestLocationUpdates();
 		lastKnownLocation = locationManager
 				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		if (lastKnownLocation == null)
 			lastKnownLocation = locationManager
 					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		if (lastKnownLocation != null) {
 			// never trust old accuracies
 			if (lastKnownLocation.getAccuracy() < 50)
 				lastKnownLocation.setAccuracy(100);
 			// remeber to reverse gocode the old address.
 			reverseGeocode(lastKnownLocation);
 		}
 		else {
 			//Make sure that lastKnowLocaton is never null
 			lastKnownLocation=new Location("FAKE");
 		}
		if (lastKnownLocation==null) lastKnownLocation=new Location("FAKE");
		if (lastKnownLocation!=null) plan.setStartingLocation(lastKnownLocation);
 
 		// updateHistoryView();
 		go_home_full_button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				planFragment.restoreDestination(store.getHome(self));
 				planFragment.onClick(planFragment.go_button);
 			}
 		});
 		go_home_empty_button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				planFragment.showDialog(ADD_HOME_ERROR);
 			}
 		});
 		
 		planFragment.planActivity=this;
 	}
 
 	
 
 	void updateHomeButton() {
 		Destination d = store.getHome(this);
 		boolean existsHome = d != null && !d.getDestination().equals("")
 				&& d.isHome();
 		if (existsHome) {
 			go_home_empty_button.setVisibility(View.GONE);
 			go_home_full_button.setVisibility(View.VISIBLE);
 		} else {
 			go_home_empty_button.setVisibility(View.VISIBLE);
 			go_home_full_button.setVisibility(View.GONE);
 		}
 	}
 
 	void storeDestination() {
 		Destination d = new Destination(plan.getDestination(),
 				plan.getDestinationType());
 		store.add(d, self);
 	}
 
 	
 	public static Plan getPlan() {
 		return plan;
 	}
 	public static Plan setPlan(Plan p) {
 		plan=p;
 		return plan;
 	}
 
 	// LocationListener Methods
 	LocationManager locationManager;
 	Location lastKnownLocation;
 	Date started;
 
 	
 	
 	private void displayLocation(List<Address> result) {
 		if ( result==null || result.size() <1) {
 			location_textview.setText("");
 			location_accuracy_textview.setText(("accuracy="
					+ lastKnownLocation.getAccuracy() + "m"));
 			planFragment.updateLocationDialog(null,"", ""+lastKnownLocation.getAccuracy());
 		}
 		else {
 			String geoc_result=result.get(0).getAddressLine(0);
 			location_textview.setText(geoc_result);
 			location_accuracy_textview.setText("accuracy="
 					+ lastKnownLocation.getAccuracy() + "m");
 			planFragment.updateLocationDialog(null,geoc_result,	""+lastKnownLocation.getAccuracy());
 		} 
 	}
 	
 	private void reverseGeocode(Location l) {
 		geocoder.abort();
 		geocoder=new ReverseGeocodeFetcher(this,l);
 		geocoder.registerCallback(geolocationObserver).update();
 	}
 
 	@Override
 	public void onLocationChanged(Location l) {
 		if (SelectBusStationActivity.isBetterLocation(l, lastKnownLocation)) {
 			lastKnownLocation = l;
 			plan.setStartingLocation(lastKnownLocation);
 			reverseGeocode(l);
 		}
 	}
 	
 	public Location getCurrentLocation(){
 		return lastKnownLocation;
 	}
 	
 
 	@Override
 	public void onProviderDisabled(String arg0) {
 	}
 
 	@Override
 	public void onProviderEnabled(String arg0) {
 	}
 
 	@Override
 	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 	}
 
 	void requestLocationUpdates() {
 		if (locationManager != null) {
 			location_progressbar.setVisibility(View.VISIBLE);
 			locationManager.requestLocationUpdates(
 					LocationManager.NETWORK_PROVIDER, 2 * 1000, 5, this);
 			locationManager.requestLocationUpdates(
 					LocationManager.GPS_PROVIDER, 3 * 1000, 5, this);
 		}
 	}
 
 	void stopLocationUpdates() {
 		if (locationManager != null)
 			location_progressbar.setVisibility(View.GONE);
 		locationManager.removeUpdates(this);
 	}
 
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		stopLocationUpdates();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (locationManager != null && !planFragment.is_wait_dialog)
 			requestLocationUpdates();
 	}
 
 
 }
