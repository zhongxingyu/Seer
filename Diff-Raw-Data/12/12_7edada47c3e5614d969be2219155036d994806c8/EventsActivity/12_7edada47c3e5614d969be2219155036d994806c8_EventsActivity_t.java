 package phillykeyspots.frpapp;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 
 import javax.net.ssl.HttpsURLConnection;
 import org.xmlpull.v1.XmlPullParserException;
 
 import phillykeyspots.frpapp.R;
 import phillykeyspots.frpapp.HTTPSettingsActivity;
 import phillykeyspots.frpapp.XmlParser;
 import phillykeyspots.frpapp.XmlParser.Entry;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EventsActivity extends Activity {
 	public static final String WIFI = "Wi-Fi";
 	public static final String ANY = "Any";
 	public static String URL; 
 	
 	//Connections
 	private static boolean wifiConnected = false;
 	private static boolean mobileConnected = true;
 	//Display refresh
 	public static boolean refreshDisplay = true;
	// Flag to avoid refreshing list when user clicks back from single list item
	public static boolean visited = false;
 	//User's current network setting preferences
 	public static String sPref = null;
 	
 	// The BroadcastReceiver that tracks network connectivity changes.
     private NetworkReceiver receiver = new NetworkReceiver();
     
     public ArrayList<HashMap<String, String>> eventTitles = new ArrayList<HashMap<String, String>>();
     
     public ProgressDialog progress;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         progress = ProgressDialog.show(EventsActivity.this,"","Loading Events!",true);
         Intent intent_evnt = getIntent();
         String evnt_msg = intent_evnt.getStringExtra("EXTRA_MESSAGE");
         URL = evnt_msg;      
         // Register BroadcastReceiver to track connection changes.
         IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
         receiver = new NetworkReceiver();
         this.registerReceiver(receiver, filter);
     }
 	
 	// Refreshes the display if the network connection and the preference settings allow it.
     @Override
     public void onStart() {
         super.onStart();
         // Gets the user's network preference settings
         SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
         // Retrieves a string value for the preferences. The second parameter is the default value 
         sPref = sharedPrefs.getString("listPref", "Any");
         updateConnectedFlags();
         // Only loads the page if refreshDisplay is true. Otherwise, keeps previous display. 
        if (refreshDisplay && !visited) {
        	visited = true;
             loadPage();
         }
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
        visited = false;
         if (receiver != null) {
         	progress.dismiss();
             this.unregisterReceiver(receiver);
         }
     }
 
     // Checks the network connection and sets the wifiConnected and mobileConnected variables accordingly.
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
 	
 	//Use AsyncTask
 	public void loadPage(){
 		if((sPref.equals(ANY)) && (wifiConnected || mobileConnected)){
 			new DownloadXMLTask().execute(URL);
 		} else if ((sPref.equals(WIFI)) && (wifiConnected)){
 			new DownloadXMLTask().execute(URL);
 		} else {
 			showErrorPage();
 		}
 	}
 	
 	// Displays an error if the app is unable to load content.
     private void showErrorPage() {
         setContentView(R.layout.activity_events);
         progress.dismiss();
         // The specified network connection is not available. Displays error message.
         Toast.makeText(getBaseContext(), getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
     }
 
     // Populates the activity's options menu.
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.http_feed, menu);
         return true;
     }
 
     // Handles the user's menu selection.
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.settings:
                 Intent settingsActivity = new Intent(getBaseContext(), HTTPSettingsActivity.class);
                 startActivity(settingsActivity);
                 return true;
         case R.id.refresh:
                 loadPage();
                 return true;
         default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
 	//Asynctask implementation
 	private class DownloadXMLTask extends AsyncTask<String, Void, String>{
 		@Override
 		protected String doInBackground(String... urls) {
 			try {
 				return loadXmlFromNetwork(urls[0]);
 			} catch (IOException e) {
 				return getResources().getString(R.string.connection_error);
 			} catch (XmlPullParserException e) {
 				return getResources().getString(R.string.xml_error);
 			}
 		}
 		@Override
 		protected void onPostExecute(String result) {
 			progress.dismiss();
 			setContentView(R.layout.activity_events);
 			loadList();
 		}
 	}
 	
 	public void loadList() {
 		ListAdapter adapter = new SimpleAdapter(this,eventTitles,
 				R.layout.activity_events,
 				new String[]{"title","date","keyspot","address","partner","other_info"},
 				new int[]{R.id.title,R.id.date,R.id.keyspot,R.id.e_k_address,R.id.e_partner,R.id.e_other_info});
 		//selecting single ListView item
 		ListView lv = (ListView)findViewById(android.R.id.list);
 		lv.setAdapter(adapter);
 			//listening to single listitem click
 			lv.setOnItemClickListener(new OnItemClickListener(){
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
 				//getting values from selected ListItem
 				String title = ((TextView) view.findViewById(R.id.title)).getText().toString();
 				String date = ((TextView) view.findViewById(R.id.date)).getText().toString();
 				String keyspot = ((TextView) view.findViewById(R.id.keyspot)).getText().toString();
 				String address = ((TextView) view.findViewById(R.id.e_k_address)).getText().toString();
 				String partner = ((TextView) view.findViewById(R.id.e_partner)).getText().toString();
 				String other_info = ((TextView) view.findViewById(R.id.e_other_info)).getText().toString();
 						
 				//Start the new intent to view single item
 				Intent in = new Intent(getApplicationContext(), SingleEventItemActivity.class);
 				in.putExtra("title", title);
 				in.putExtra("date", date);
 				in.putExtra("keyspot", keyspot);
 				in.putExtra("address", address);
 				in.putExtra("partner", partner);
 				in.putExtra("other_info", other_info);
 				startActivity(in);
 			}
 		});
 	}
 	
 	private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
 		InputStream stream = null;
 		
 		//Instantiate the parser
 		XmlParser eventsparser = new XmlParser();
 		List<Entry> entries = null;
 		
 		Calendar rightNow = Calendar.getInstance();
 		DateFormat formatter = new SimpleDateFormat("MMM dd h:mmaa", Locale.US);
 		
 		StringBuilder htmlString = new StringBuilder();
 		htmlString.append("<b>" + getResources().getString(R.string.page_title_events) + "</b><br/>");
 		htmlString.append("<em>" + getResources().getString(R.string.updated) + " " + formatter.format(rightNow.getTime()) + "</em>");
 		
 		try {
 			stream = downloadUrl(urlString);
 			entries = eventsparser.parse(stream);
 			//close input stream after application is finished with it.
 		} finally {
 			if (stream != null){
 				stream.close();
 			}
 		}
 		
 		//Return the list of entries
 		for (Entry entry : entries){
 			HashMap<String, String> map = new HashMap<String, String>();
 			map.put("title", entry.topics + " : " + entry.type + " >> " + entry.title);
 			map.put("date",entry.training_dates);
 			map.put("keyspot", entry.keyspot);
 			map.put("address", entry.street + ",\n" + entry.city + ",\n" + entry.province + ", " + entry.postal_code);
 			map.put("partner", entry.managing_partner + "\nContact : " + entry.contact + "\nEmail : " + entry.email);
 			map.put("other_info", "Ideal for : \n" + entry.level + "\nMore Info : \n" + entry.more_info);
 			//adding HashList to ArrayList
 			eventTitles.add(map);
 		}
 		
 		return htmlString.toString();
 	}
 
 
 	private InputStream downloadUrl(String urlString) throws IOException {
 		URL url = new URL(urlString);
 		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
 		conn.setReadTimeout(10000 /*milliseconds*/);
 		conn.setConnectTimeout(15000 /*milliseconds*/);
 		conn.setRequestMethod("GET");
 		conn.setDoInput(true);
 		conn.connect();
 		InputStream stream = conn.getInputStream();
         return stream;
 	}
 	
    // Monitor the connection
    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connMgr =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            // Checks the user prefs and the network connection.
            if (WIFI.equals(sPref) && networkInfo != null
                    && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                refreshDisplay = true;
                Toast.makeText(context, R.string.wifi_connected, Toast.LENGTH_SHORT).show();
            } else if (ANY.equals(sPref) && networkInfo != null) {
                refreshDisplay = true;
                // Otherwise, the app can't download content--there is no network connection (mobile or Wi-Fi)
            } else {
                refreshDisplay = false;
                Toast.makeText(context, R.string.lost_connection, Toast.LENGTH_SHORT).show();
            }
        }
    }
 }
