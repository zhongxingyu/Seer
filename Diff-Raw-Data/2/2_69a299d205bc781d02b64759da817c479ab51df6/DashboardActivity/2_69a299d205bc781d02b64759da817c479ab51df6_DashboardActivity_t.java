 package phillykeyspots.frpapp;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import phillykeyspots.frpapp.DatePickerFragment;
 import phillykeyspots.frpapp.EventsActivity;
 import phillykeyspots.frpapp.KEYSPOTSActivity;
 import phillykeyspots.frpapp.KEYSPOTSActivity.NetworkReceiver;
 import phillykeyspots.frpapp.R;
 import phillykeyspots.frpapp.XmlParser.Entry;
 
 import android.annotation.SuppressLint;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.location.Address;
 import android.location.Geocoder;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import android.view.Window;
 import android.webkit.WebView;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.Toast;
 
 @SuppressLint("DefaultLocale")
 public class DashboardActivity extends FragmentActivity {
 	
 	private FinderFragment finder = new FinderFragment();
 	private EventsFragment events = new EventsFragment();
 	private ResourcesFragment resources = new ResourcesFragment();
 	private JomlFragment joml = new JomlFragment();
 	public static OnInfoWindowClickListener window_listener;
 	private DashboardActivity dash = this;
 	public static KeyspotLoader keyspots = null;
 	private NetworkReceiver dashreceive = new NetworkReceiver();
 	private String sPref = null;
 	private static boolean wifiConnected = true;
 	private static boolean mobileConnected = true;
 	public static boolean refreshDisplay = true;
 	private List<Entry> entries;
 	private ProgressDialog progress;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_dashboard);
 		
 		setUpFinderFragment();
 		this.registerReceiver(dashreceive, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
 		Fragment frag = null;
 		
 		switch(Integer.parseInt(getIntent().getExtras().get("ID").toString())){
 		case R.id.b_finder:
 			frag = finder;
 			break;
 		case R.id.b_events:
 			frag = events;
 			break;
 		case R.id.b_resources:
 			frag = resources;
 			break;
 		case R.id.b_joml:
 			frag = joml;
 			break;
 		}
 		getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, frag).commit();
 		
 	}
 	
 	protected void onStart(){
 		super.onStart();
 		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
 		sPref = shared.getString("listPref", "Any");
 		updateConnectedFlags();
 	}
 	
     protected void onDestroy() {
         super.onDestroy();
         if (dashreceive != null) {
             this.unregisterReceiver(dashreceive);
         }
     }
 
     private void updateConnectedFlags() {
         ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
         if (activeInfo != null && activeInfo.isConnected()) {
             wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
             mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
         } else {
             wifiConnected = false;
             mobileConnected = false;
         }
     }
 	
 	private void setUpFinderFragment(){
 		
 		window_listener = new OnInfoWindowClickListener(){
 
 			public void onInfoWindowClick(Marker mark) {
 				Intent intent = new Intent(dash, KeyspotActivity.class);
 				intent.putExtra("Name", mark.getTitle());
 				dash.startActivity(intent);
 			}
 			
 		};
 		keyspots = new KeyspotLoader();
 	}
 		
 	public void search(View view){
 		FinderFragment.mMap.clear();
 		progress = ProgressDialog.show(dash, "", "Loading...");
 		new DownloadXmlTask().execute(((EditText)findViewById(R.id.finder_edit)).getText().toString());
 	}
 	private class DownloadXmlTask extends AsyncTask<String, Void, String>{
 		
 		private String zip = null;
 		
 		@Override
 		protected String doInBackground(String... params) {
 			entries = keyspots.reload();
 			zip = params[0];
 			return null;
 		}
 		protected void onPostExecute(String result){
 
 			if (entries != null){
 				int count = 0;
 				for (Entry entry : entries){
 					if (entry.postal_code.equals(zip)){
 						count++;
 						Geocoder code = new Geocoder(dash);
 						try{
 							List<Address> coded = code.getFromLocationName(entry.latitude + entry.longitude, 1);
 							FinderFragment.mMap.addMarker(new MarkerOptions().position(new LatLng(coded.get(0).getLatitude(), coded.get(0).getLongitude())).title(entry.keyspot).snippet("Click for more info.").icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin)));
 						}catch (Exception e){
 							e.printStackTrace();
 						}
 					}
 				}
 				if (count != 0){
 					Toast.makeText(dash, "Done Loading", Toast.LENGTH_LONG).show();
 				}
 				else {
 					Toast.makeText(dash, "No Results", Toast.LENGTH_LONG).show();
 				}
 			}
 			else{
 				FinderFragment.mMap.addMarker(new MarkerOptions().position(new LatLng(39.96, -75.17)).title("Keyspot Name").snippet("Click for more info").icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin)));	
 			}
 			progress.dismiss();
 		}
 		
 	}
     
 	public class NetworkReceiver extends BroadcastReceiver {
 		public void onReceive(Context context, Intent intent) {
 			ConnectivityManager connMgr =
 					(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 			// Checks the user prefs and the network connection.
 			if ("Wi-Fi".equals(sPref) && networkInfo != null&& networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
 				refreshDisplay = true;
 				Toast.makeText(context, R.string.wifi_connected, Toast.LENGTH_SHORT).show();
 			} else if ("Any".equals(sPref) && networkInfo != null) {
 				refreshDisplay = true;
 			} else {
 				refreshDisplay = false;
 				Toast.makeText(context, R.string.lost_connection, Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 	
 	public void showDatePickerDialog(View view) {
 		DialogFragment newFragment = new DatePickerFragment();
 		newFragment.show(getSupportFragmentManager(),"datePicker");
 	}
 	
 	public void showEventsbyZIP(View view) {
 		Intent intent = new Intent(this, EventsActivity.class);
 		EditText editText = (EditText) findViewById(R.id.enter_zip_code);
 		String query = editText.getText().toString();
 		String fullquery = "https://www.phillykeyspots.org/events.xml/?distance[postal_code]=" + query;
 		intent.putExtra("EXTRA_MESSAGE", fullquery);
 		startActivity(intent);
 	}
 	
 	public void onRadioClicked(View view){
 		//Which view is checked.
 		boolean checked = ((RadioButton) view).isChecked();
 		String query = "https://www.phillykeyspots.org/events.xml/term/";
 
 		switch(view.getId()){
 			case R.id.checkbx_all_levels:
 				if(checked){
 					query = query + "All%20Levels";}
 				break;
 			case R.id.checkbx_first_time:
 				if(checked){
 					query = query + "First-time";}
 				break;
 			case R.id.checkbx_beginner:
 				if(checked){
 					query = query + "Beginner";}
 				break;
 			case R.id.checkbx_intermediate:
 				if(checked){
 					query = query + "Intermediate";}
 				break;
 			case R.id.checkbx_advanced:
 				if(checked){
 					query = query + "Advanced";}
 				break;
 			case R.id.checkbx_tech_expert:
 				if(checked){
 					query = query + "Tech Expert";}
 				break;
 			case R.id.checkbx_web_access:
 				if(checked){
 					query = query + "Web%20Access";}
 				break;
 			case R.id.checkbx_computer_basics:
 				if(checked){
 					query = query + "Computer%20Basics";}
 				break;
 			case R.id.checkbx_internet_basics:
 				if(checked){
 					query = query + "Internet%20Basics";}
 				break;
 			case R.id.checkbx_ms_office:
 				if(checked){
 					query = query + "MS%20Office";}
 				break;
 			case R.id.checkbx_social_media:
 				if(checked){
 					query = query + "Social%20Media";}
 				break;
 			case R.id.checkbx_job_search:
 				if(checked){
 					query = query + "Job%20Search";}
 				break;
 		}
 		Intent intent = new Intent(this, EventsActivity.class);
		intent.putExtra("EXTRA_MESSAGE", query);
 		startActivity(intent);
 	}
 	
 	public void showKEYSPOTS(View view) {
 		Intent intent = new Intent(this, KEYSPOTSActivity.class);
 		String fullquery = "https://www.phillykeyspots.org/keyspots.xml"; 
 		intent.putExtra("EXTRA_MESSAGE_KEYSPOTS", fullquery);
 		startActivity(intent);
 	}
 	
 	// Events view filter accordion style 
 		public void openPanel(View view){
 			LinearLayout p1 = (LinearLayout)findViewById(R.id.panel1);
 			LinearLayout p2 = (LinearLayout)findViewById(R.id.panel2);
 			LinearLayout p3 = (LinearLayout)findViewById(R.id.panel3);
 			LinearLayout p4 = (LinearLayout)findViewById(R.id.panel4);
 			switch(view.getId()){
 				case R.id.b_panel1:
 					p1.setVisibility(View.VISIBLE);
 					p2.setVisibility(View.GONE);
 					p3.setVisibility(View.GONE);
 					p4.setVisibility(View.GONE);
 					break;
 				case R.id.b_panel2:				
 					p2.setVisibility(View.VISIBLE);
 					p1.setVisibility(View.GONE);
 					p3.setVisibility(View.GONE);
 					p4.setVisibility(View.GONE);
 					break;
 				case R.id.b_panel3:
 					p3.setVisibility(View.VISIBLE);
 					p1.setVisibility(View.GONE);
 					p2.setVisibility(View.GONE);
 					p4.setVisibility(View.GONE);
 					break;
 				case R.id.b_panel4:
 					p4.setVisibility(View.VISIBLE);
 					p1.setVisibility(View.GONE);
 					p2.setVisibility(View.GONE);
 					p3.setVisibility(View.GONE);
 					break;
 			}
 		}
 }
