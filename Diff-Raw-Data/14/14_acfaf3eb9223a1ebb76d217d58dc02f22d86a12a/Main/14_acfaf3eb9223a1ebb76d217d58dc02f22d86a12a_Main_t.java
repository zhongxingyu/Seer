 package com.example.foursquareassig;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.speech.RecognizerIntent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class Main extends Activity {
 
 	public static final String CLIENT_ID = "4JB2U43SBO1DGX04C4GO2NRCRR505SICPUBSK1TEP3DAE5S3";
 	public static final String CLIENT_SECRET = "T5FAM3GXNRWYGNUMGEECYINAP0BRTFTTJIY33AOO5SEGHAHC";
 	public static final String API_URL = "https://api.foursquare.com/v2";
 	public static String urlString;
 	public final static String MESSAGE = "com.example.foursquareassig";
 	public static JSONRequest jr = new JSONRequest();
 	static final int check = 1111;
 	public String MicrophoneResult;
 	public static final String[] CategoryId = { "4d4b7105d754a06374d81259",
 			"4bf58dd8d48988d17f941735", "4bf58dd8d48988d1fd941735",
 			"4d4b7104d754a06370d81259" };
 	public static final String[] keywords = { "food", "cinema", "shopping",
 			"art" };
 	public static final String[] Search = { "Food", "Cinema", "Shopping", "Art" };
 	public String categoryId;
 	public String radius;
 	public double lat = 60.7943;
 	public double lon = 10.6889;
 	String slat = Double.toString(lat);
 	String slon = Double.toString(lon);
 
 	public static String[] from = { "txt", "pl" };
 	// Id-s of views in listview_layout
 	public static int[] to = { R.id.txt, R.id.pl };
 	public ConnectivityManager cm;
 	public NetworkInfo info;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		info = cm.getActiveNetworkInfo();
 		FillSpinner(R.id.spinner1, R.array.search);
 		FillSpinner(R.id.Spinner2, R.array.distance);
 		final ListView listView = (ListView) findViewById(R.id.listview);
 
 		// int radius = 500; //Convert to String
 		// String categoryId = "4d4b7105d754a06374d81259";
 		// String sradius = Integer.toString(radius);
 
 		// urlString = API_URL + "/venues/search?ll=" + slat + "," + slon +
 		// "&intent=browse&radius=" + sradius + "&categoryId=" + categoryId +
 		// "&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET +
 		// "&v=20111218";
 		// urlString =
 		// "https://api.foursquare.com/v2/venues/search?ll=60.7943,10.6889&intent=browse&radius=100000&categoryId=4bf58dd8d48988d1d6941735&client_id=4JB2U43SBO1DGX04C4GO2NRCRR505SICPUBSK1TEP3DAE5S3&client_secret=T5FAM3GXNRWYGNUMGEECYINAP0BRTFTTJIY33AOO5SEGHAHC&v=20111218";
 
 		listView.setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> arg0, View view,
 					int position, long id) {
 				// TODO Auto-generated method stub
 				// selected item
 				// double lat = jr.latitude[p];
 				// double lng = jr.longitude[p];
 				// String restaurant =
 				// ((TextView)view.findViewById(R.id.txt)).getText().toString();
 				// String
 				// dis=((TextView)view.findViewById(R.id.pl)).getText().toString();
 				// Launching new Activity on selecting single List Item
 				String pos = "123";
 				String p = "" + position;
 
 				Intent i = new Intent(getApplicationContext(), ShowMap.class);
 
 				i.putExtra("position", p);
 				// i.putExtra("p_longitutde", lng);
 				// sending data to new activity
 				// i.putExtra("restaurant", restaurant);
 				startActivity(i);
 
 			}
 		});
 
 	}
 
 	// MICROPHONE
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		if (requestCode == check && resultCode == RESULT_OK) {
 			ArrayList<String> results = data
 					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 			MicrophoneResult = results.get(0);
 
 			int r = Filter(MicrophoneResult);
 			switch (r) {
 			case 0:
 				categoryId = CategoryId[0];
 				break;
 			case 1:
 				categoryId = CategoryId[1];
 				break;
 			case 2:
 				categoryId = CategoryId[2];
 				break;
 			case 3:
 				categoryId = CategoryId[3];
 				break;
 			}
 			String sradius = "1000";
 			urlString = API_URL + "/venues/search?ll=" + slat + "," + slon
 					+ "&intent=browse&radius=" + sradius + "&categoryId="
 					+ categoryId + "&client_id=" + CLIENT_ID
 					+ "&client_secret=" + CLIENT_SECRET + "&v=20111218";
 
 			List<HashMap<String, String>> list;
 			jr = new JSONRequest();
 			list = jr.getJSONfromURL(urlString);
 			ListAdapter adapter = new SimpleAdapter(getBaseContext(), list,
 					R.layout.listview_layout, from, to);
 
 			// Getting a reference to listview of main.xml layout file
 			ListView listView = (ListView) findViewById(R.id.listview);
 
 			// Setting the adapter to the listView
 			listView.setAdapter(adapter);
 
 			// lv.setAdapter(new
 			// ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,results));
 		}
 
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	public void Search(View v) {
 		if (info != null) {
 			if (info.isConnected()) {
 				Spinner s1 = (Spinner) findViewById(R.id.spinner1);
 				String category = s1.getSelectedItem().toString();
 				if (Search[0].equals(category)) {
 					categoryId = CategoryId[0];
 				}
				if (Search[1].equals(category)) {
					categoryId = CategoryId[1];
					
				}
				if (Search[2].equals(category)) {
					categoryId = CategoryId[2];
					
				}
				if (Search[3].equals(category)) {
					categoryId = CategoryId[3];
					
				}
				
				
 				Spinner s2 = (Spinner) findViewById(R.id.Spinner2);
 				radius = s2.getSelectedItem().toString();
 				radius = radius.substring(0, radius.length() - 1);
 				urlString = API_URL + "/venues/search?ll=" + slat + "," + slon
 						+ "&intent=browse&radius=" + radius + "&categoryId="
 						+ categoryId + "&client_id=" + CLIENT_ID
 						+ "&client_secret=" + CLIENT_SECRET + "&v=20111218";
 				List<HashMap<String, String>> list;
 				jr = new JSONRequest();
 				list = jr.getJSONfromURL(urlString);
 				ListAdapter adapter = new SimpleAdapter(getBaseContext(), list,
 						R.layout.listview_layout, from, to);
 
 				// Getting a reference to listview of main.xml layout file
 				ListView listView = (ListView) findViewById(R.id.listview);
 
 				// Setting the adapter to the listView
 				listView.setAdapter(adapter);
 			} else {
 				Toast.makeText(this,
 						"Please check your internet connection and try again.",
 						Toast.LENGTH_SHORT).show();
 			}
 		} else {
 			Toast.makeText(this,
 					"Please check your internet connection and try again.",
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	public void VoiceSearch(View v) {
 		if (info != null) {
 			if (info.isConnected()) {
 				Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 				i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
 						RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 				i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak!");
 				startActivityForResult(i, check);
 			} else {
 				Toast.makeText(this,
 						"Please check your internet connection and try again.",
 						Toast.LENGTH_SHORT).show();
 			}
 		} else {
 			Toast.makeText(this,
 					"Please check your internet connection and try again.",
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	public int Filter(String string) {
 		int i = 0;
 		int l = -1;
 		for (i = 0; i <= 3; i++) {
 			Boolean found = Arrays.asList(string.split(" ")).contains(
 					keywords[i]);
 			if (found) {
 				return l = i;
 			}
 		}
 		return l;
 	}
 
 	public void FillSpinner(int i, int j) {
 		Spinner spinner = (Spinner) findViewById(i);
 		// Create an ArrayAdapter using the string array and a default spinner
 		// layout
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 				this, j, android.R.layout.simple_spinner_item);
 		// Specify the layout to use when the list of choices appears
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		// Apply the adapter to the spinner
 		spinner.setAdapter(adapter);
 	}
 }
