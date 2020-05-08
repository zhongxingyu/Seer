 package tw.ipis.routetaiwan;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.net.URLEncoder;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.Format;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import tw.ipis.routetaiwan.planroute.DirectionResponseObject.Route.Bound;
 import tw.ipis.routetaiwan.planroute.DirectionResponseObject.Route.Leg.Step;
 import tw.ipis.routetaiwan.planroute.DirectionResponseObject.Route.Leg.Step.Poly;
 import tw.ipis.routetaiwan.planroute.DirectionResponseObject.Route.Leg.Step.ValueText;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.location.Criteria;
 import android.location.GpsStatus;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.ScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.gson.Gson;
 
 public class planroute extends Activity {
 
 	//	ProgressBar planning;
 	String TAG = "~~planroute~~";
 	private ProgressBar planning;
 	private EditText from;
 	private EditText to;
 	private LocationManager locationMgr;
 	private DownloadWebPageTask task = null;
 	private boolean isrequested = false;
 	public DirectionResponseObject dires = null;
 	String provider = null;
 	private static final String projectdir = Environment.getExternalStorageDirectory() + "/.routetaiwan/";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.planroute);
 
 		ConnectivityManager connMgr = (ConnectivityManager) 
 		getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 		if (networkInfo != null && networkInfo.isConnected()) {
 			Toast.makeText(this, getResources().getString(R.string.info_network_using) + networkInfo.getTypeName() , Toast.LENGTH_SHORT).show();
 		}
 		else {
 			Toast.makeText(this, getResources().getString(R.string.warninig_no_network) , Toast.LENGTH_LONG).show();
 		}
 
 		start_positioning();
 		
 		/* Intent from showmap class */
 		Bundle Data = this.getIntent().getExtras();
 		if(Data != null) {
 			String start = Data.getString("start");
 			String dest = Data.getString("end");
 			if(start != null) {
 				from = (EditText)findViewById(R.id.from);
 				from.setText(start, TextView.BufferType.EDITABLE);
 			}
 			if(dest != null) {
 				to = (EditText)findViewById(R.id.to);
 				to.setText(dest, TextView.BufferType.EDITABLE);
 			}
 		}
 	}
 
 	@Override
 	protected void onResume() {
 
 		super.onResume();
 
 		start_positioning();
 	}
 
 	@Override
 	protected void onStop() {
 		locationMgr.removeUpdates(locationListener);
 		if(task != null && task.getStatus() != DownloadWebPageTask.Status.FINISHED)
 			task.cancel(true);
 		super.onStop();
 	}
 
 	public void start_planing(View v) {
 		if(task != null && task.getStatus() != DownloadWebPageTask.Status.FINISHED)
 			task.cancel(true);
 		foreground_cosmetic();
 
 		isrequested = true;
 
 		from = (EditText)findViewById(R.id.from);
 		String start = from.getText().toString();	// Get user input "From"
 		Location currentloc = GetCurrentPosition();
 		if (!start.isEmpty() || currentloc.getProvider().contentEquals("network")) // Wait for positioning
 			Getroute();
 	}
 
 	public void start_positioning() {
 		String locprovider = 	initLocationProvider();
 		if(locprovider == null) {
 			Toast.makeText(this, getResources().getString(R.string.warning_no_loc_provider) , Toast.LENGTH_LONG).show();
 			return;
 		}
 		locationMgr.requestLocationUpdates(locprovider, 0, 0, locationListener);
 		if(locprovider.contentEquals("gps")) {
 //			Toast.makeText(this, getResources().getString(R.string.info_positioning_by_gps) , Toast.LENGTH_SHORT).show();
 			locationMgr.addGpsStatusListener(gpsListener);
 		}
 //		else
 //			Toast.makeText(this, getResources().getString(R.string.info_positioning_by_network) , Toast.LENGTH_SHORT).show();
 	}
 
 	private void foreground_cosmetic() {
 		from = (EditText)findViewById(R.id.from);
 		to = (EditText)findViewById(R.id.to);
 
 		InputMethodManager imm = (InputMethodManager)getSystemService(
 				Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(from.getWindowToken(), 0);
 
 		ScrollView sv = (ScrollView) this.findViewById(R.id.routes);
 		sv.removeAllViews();	// Clear screen
 		planning = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
 		planning.setIndeterminate(true);
 		sv.addView(planning);	// Add processbar
 
 		planning.setVisibility(ProgressBar.VISIBLE);
 	}
 
 	public void Getroute() {
 		String request = "";
 		from = (EditText)findViewById(R.id.from);
 		to = (EditText)findViewById(R.id.to);
 		String start = from.getText().toString();	// Get user input "From"
 		String destination = to.getText().toString();	// Get user input "to"
 		String Mapapi = "https://maps.googleapis.com/maps/api/directions/json?origin={0}&destination={1}&sensor={3}&departure_time={2}&mode={4}&alternatives=true&region=tw";
 
 		isrequested = false;
 
 		if(Locale.getDefault().getDisplayLanguage().contentEquals("中文"))
 			Mapapi = new StringBuilder().append(Mapapi).append("&language=zh-tw").toString();
 
 		long now = System.currentTimeMillis() / 1000;
 		if(destination.isEmpty())
 			destination = "Taipei 101";
 
 		try {
 			if (start.isEmpty()) {
 				Location current = GetCurrentPosition();
 				String curr = current.getLatitude() + "," + current.getLongitude();
 				request = MessageFormat.format(Mapapi, URLEncoder.encode(curr, "UTF-8"), 
 						URLEncoder.encode(destination, "UTF-8"), URLEncoder.encode(new Long(now).toString(), "UTF-8"), 
 						URLEncoder.encode("true", "UTF-8"), URLEncoder.encode("transit", "UTF-8"));
 			}
 			else {
 				request = MessageFormat.format(Mapapi, URLEncoder.encode(start, "UTF-8"),
 						URLEncoder.encode(destination, "UTF-8"), URLEncoder.encode(new Long(now).toString(), "UTF-8"), 
 						URLEncoder.encode("true", "UTF-8"), URLEncoder.encode("transit", "UTF-8"));
 			}
 			Log.d(TAG, request);
 		}
 		catch (Exception e)
 		{
 			planning.setVisibility(ProgressBar.GONE);
 			e.printStackTrace();
 			return;
 		}
 
 		/* Use the url for http request */
 		task = new DownloadWebPageTask();
 		task.execute(new String[] {request});
 	}
 
 	private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
 		@Override
 		protected String doInBackground(String... urls) {
 			String response = "";
 			for (String url : urls) {
 				HttpGet httpGet = new HttpGet(url);
 				httpGet.addHeader("accept", "application/json");
 				//				StringBuilder builder = new StringBuilder();
 				HttpClient client = new DefaultHttpClient();
 				try {
 					HttpResponse result = client.execute(httpGet);
 					StatusLine statusLine = result.getStatusLine();
 					int statusCode = statusLine.getStatusCode();
 					if (statusCode == 200) {
 						HttpEntity entity = result.getEntity();
 
 						response = EntityUtils.toString(entity);
 					} else {
 						Log.e(TAG, "Failed to download file");
 					}
 					return response;
 				}
 				catch (Exception e) {
 					planning.setVisibility(ProgressBar.GONE);
 					e.printStackTrace();
 					return "";
 				}
 			}
 			return "";
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			decode(result);
 		}
 	}
 
 	public String convertTime(long time){
 		time = time * 1000;	// Change to milli-seconds
 		Date date = new Date(time);
 		Format format = new SimpleDateFormat("HH:mm");
 		return format.format(date).toString();
 	}
 
 	private TextView createTextView(String content, TableRow parent, int textcolor, float weight, int gravity, String text, LatLng_s departure, LatLng_s destination) {
 		TextView tv = new TextView(this);
 		tv.setText(content);
 		tv.setTextColor(textcolor);
 		tv.setTextSize(16);
 		tv.setHorizontallyScrolling(false);
 		tv.setWidth(0);
 		tv.setGravity(gravity);
 		tv.setTag(R.id.tag_zero, text);
 		String dept = String.valueOf(departure.lat) + "," + String.valueOf(departure.lng);
 		String det = String.valueOf(destination.lat) + "," + String.valueOf(destination.lng);
 		tv.setTag(R.id.tag_first, dept);
 		tv.setTag(R.id.tag_second, det);
 		if(weight != 0)
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, weight));
 		parent.addView(tv);
 		return tv;
 	}
 
 	private TextView createTextView(String content, TableRow parent, int textcolor, float weight, int gravity, String text, String departure, String destination) {
 		TextView tv = new TextView(this);
 		tv.setText(content);
 		tv.setTextColor(textcolor);
 		tv.setTextSize(16);
 		tv.setHorizontallyScrolling(false);
 		tv.setWidth(0);
 		tv.setGravity(gravity);
 		tv.setTag(R.id.tag_zero, text);
 		tv.setTag(R.id.tag_first, departure);
 		tv.setTag(R.id.tag_second, destination);
 		if(weight != 0)
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, weight));
 		parent.addView(tv);
 		return tv;
 	}
 
 	private TextView createTextView(String content, TableRow parent, int textcolor, float weight, int gravity, String text, List<MarkP> allP) {
 		TextView tv = new TextView(this);
 		tv.setText(content);
 		tv.setTextColor(textcolor);
 		tv.setTextSize(16);
 		tv.setHorizontallyScrolling(false);
 		tv.setWidth(0);
 		tv.setGravity(gravity);
 		tv.setTag(R.id.tag_zero, text);
 		tv.setTag(R.id.tag_first, allP);
 		if(weight != 0)
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, weight));
 		parent.addView(tv);
 		return tv;
 	}
 
 	private ImageView createImageViewbyR(int R, TableRow parent, int height, int width) {
 		ImageView iv = new ImageView(this);
 		iv.setImageBitmap(null);
 		iv.setImageResource(R);
 		iv.setMaxHeight(height);
 		iv.setMaxWidth(width);
 		iv.setAdjustViewBounds(true);
 		parent.addView(iv);
 		return iv;
 	}
 
 	private TableRow CreateTableRow(TableLayout parent, float weight, final int num){
 		TableRow tr = new TableRow(this);	// 1st row
 
 		OnClickListener popup = new OnClickListener() {
 			@Override
 			public void onClick(View onclick) {
 				int childcount = ((ViewGroup) onclick).getChildCount();
 				TextView act = (TextView)((ViewGroup) onclick).getChildAt(childcount - 1);
 
 				if(act != null) {
 					showPopup(planroute.this, act);
 				}
 			}
 		};
 		
 		OnLongClickListener save_to_favorite = new OnLongClickListener() {
 			@Override
 			public boolean onLongClick(View arg0) {
 				/* Pop-up a dialog to ask for permission */
 				Intent launchpop = new Intent(planroute.this, diag_save.class);
 				Bundle bundle=new Bundle();
 				Gson gson = new Gson();
 				String json = gson.toJson(dires.routes[num]);
 				/* File name is the MD5SUM of the content */
 				String filename = projectdir + getMD5EncryptedString(json) + ".json";
 
 				File file = new File(filename);
 				if (!file.exists()) {
 					bundle.putString("filename", filename);
 					bundle.putString("content", json);
 					launchpop.putExtras(bundle);
 
 					startActivity(launchpop);
 				}
 				else {
 					Toast.makeText(planroute.this, getResources().getString(R.string.file_already_existed) , Toast.LENGTH_SHORT).show();
 				}
 				
 				return true;
 			}
 		};
 
 		if(num % 2 == 0)
 			tr.setBackgroundColor(Color.WHITE);
 		else
 			tr.setBackgroundColor(Color.LTGRAY);
 		if(weight != 0)
 			tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, weight));
 		else
 			tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 		tr.setGravity(Gravity.CENTER_VERTICAL);
 		tr.setClickable(true);
 		/* Short click: open a popup activity for MAP or REALTIME INFO */
 		tr.setOnClickListener(popup);
 		/* Long click: open a dialog menu asking for saving to My Favorite */
 		tr.setOnLongClickListener(save_to_favorite);
 
 		parent.addView(tr);
 		return tr;
 	}
 	
 	public static String getMD5EncryptedString(String encTarget){
 		MessageDigest mdEnc = null;
 		try {
 			mdEnc = MessageDigest.getInstance("MD5");
 		} catch (NoSuchAlgorithmException e) {
 			System.out.println("Exception while encrypting to md5");
 			e.printStackTrace();
 		} // Encryption algorithm
 		mdEnc.update(encTarget.getBytes(), 0, encTarget.length());
 		String md5 = new BigInteger(1, mdEnc.digest()).toString(16) ;
 		return md5;
 	}
 
 	public boolean dumpdetails(DirectionResponseObject dires) {
 		ScrollView sv = (ScrollView) this.findViewById(R.id.routes);
 
 		// Create a LinearLayout element
 		TableLayout tl = new TableLayout(this);
 		tl.setOrientation(TableLayout.VERTICAL);
 		sv.removeAllViews();
 
 		if(!dires.status.contentEquals("OK")) {
 			Toast.makeText(this, getResources().getString(R.string.info_no_result) , Toast.LENGTH_LONG).show();
 			return false;
 		}
 
 		// Add text
 		for (int i = 0; i < dires.routes.length; i++) {
 			int transit = 0;
 			for (int j = 0; j < dires.routes[i].legs.length; j++)	{
 				boolean pure_walk_flag = false;
 				TableRow tr = null;
 				TableRow time_row = CreateTableRow(tl, 0, i);	// 1st row
 
 				String title = "";
 				long duration = 0;
 
 				dires.routes[i].legs[j].mark = new ArrayList<MarkP>();
 
 				if(dires.routes[i].legs[j].arrival_time != null && dires.routes[i].legs[j].departure_time != null) {
 					duration = dires.routes[i].legs[j].arrival_time.value - dires.routes[i].legs[j].departure_time.value;
 				}
 				else {
 					pure_walk_flag = true;
 					dires.routes[i].legs[j].departure_time = new Time(0);
 					dires.routes[i].legs[j].arrival_time = new Time(0);
 					dires.routes[i].legs[j].departure_time.value = System.currentTimeMillis() / 1000;
 				}
 
 				TableRow transit_times = CreateTableRow(tl, 0, i);	// 2nd row, leave it for later use
 
 				tr = CreateTableRow(tl, 1.0f, i);
 				createImageViewbyR(R.drawable.start, tr, 50, 50);
 				createTextView(dires.routes[i].legs[j].start_address, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, "map,current", 
 						dires.routes[i].legs[0].start_location, dires.routes[i].legs[0].start_location);
 
 				dires.routes[i].legs[j].mark.add(new MarkP("start", getResources().getString(R.string.str_from), dires.routes[i].legs[0].start_address, dires.routes[i].legs[0].start_location));
 
 				for (int k = 0; k < dires.routes[i].legs[j].steps.length; k++) {
 					Step step = dires.routes[i].legs[j].steps[k];
 					if(step.travel_mode.contentEquals("WALKING")) {
 						String walk = new StringBuilder().append(step.html_instructions).append("\n(" + step.distance.text + ", " +step.duration.text + ")").toString();
 						tr = CreateTableRow(tl, 1.0f, i);
 						createImageViewbyR(R.drawable.walk, tr, 50, 50);
 						createTextView(walk, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, "map," + step.polyline.points, 
 								step.start_location, step.end_location);
 
 						dires.routes[i].legs[j].mark.add(new MarkP("walk", step.html_instructions, step.distance.text, step.start_location));
 						if(pure_walk_flag == true) {
 							duration += step.duration.value;
 						}
 					}
 					else if(step.travel_mode.contentEquals("TRANSIT")) {
 						String type = step.transit_details.line.vehicle.type;
 						String agencyname = step.transit_details.line.agencies[0].name;
 						String text = "transit,";
 
 						String trans = new StringBuilder().append(getResources().getString(R.string.taketransit)).append(step.transit_details.line.short_name).toString();
 						
 						String headsign = step.transit_details.headsign;
 
 						String trans_to = new StringBuilder().append(getResources().getString(R.string.to)).append(step.transit_details.arrival_stop.name).toString();
 						
 						String time_taken = new StringBuilder().append("\n(" + step.transit_details.num_stops + getResources().getString(R.string.stops) + ", " +step.duration.text + ")").toString();
 						transit++;
 
 						tr = CreateTableRow(tl, 1.0f, i);
 						if(type.contentEquals("BUS")) {
 							createImageViewbyR(R.drawable.bus, tr, 50, 50);
							text = new StringBuilder().append(text).append("bus,").append(step.transit_details.line.short_name + ",").append(step.transit_details.line.agencies[0].name).toString();
 							headsign = new StringBuilder().append("(" + getResources().getString(R.string.go_to)).append(headsign + ")").toString();
 							createTextView(trans + headsign + trans_to + time_taken, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, text, step.transit_details.departure_stop.name, step.transit_details.arrival_stop.name);
 
 							dires.routes[i].legs[j].mark.add(new MarkP("bus"
 									,	getResources().getString(R.string.taketransit) + step.transit_details.line.short_name
 									, getResources().getString(R.string.go_to) + step.transit_details.headsign + getResources().getString(R.string.dirction)
 									, step.transit_details.departure_stop.location));
 						}
 						else if(type.contentEquals("SUBWAY")) {
 							if(agencyname.contentEquals("台北捷運")) {
 								createImageViewbyR(R.drawable.trtc, tr, 50, 50);
 								dires.routes[i].legs[j].mark.add(new MarkP("trtc"
 										, getResources().getString(R.string.taketransit) + step.transit_details.line.short_name
 										, getResources().getString(R.string.go_to) + step.transit_details.headsign + getResources().getString(R.string.dirction)
 										, step.transit_details.departure_stop.location));
 							}
 							else if(agencyname.contentEquals("高雄捷運")) {
 								createImageViewbyR(R.drawable.krtc, tr, 50, 50);
 								dires.routes[i].legs[j].mark.add(new MarkP("krtc"
 										, getResources().getString(R.string.taketransit) + step.transit_details.line.short_name
 										, getResources().getString(R.string.go_to) + step.transit_details.headsign + getResources().getString(R.string.dirction)
 										, step.transit_details.departure_stop.location));
 							}
 							else
 								createTextView("車", tr, Color.rgb(0,0,0), 0.1f, Gravity.CENTER, "transit,null", (String)null, (String)null);
 							text = new StringBuilder().append("map,").append(step.polyline.points).toString();
 							headsign = new StringBuilder().append("(" + getResources().getString(R.string.go_to)).append(headsign + ")").toString();
 							createTextView(trans + headsign + trans_to + time_taken, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, text, step.start_location, step.end_location);
 						}
 						else if(type.contentEquals("HEAVY_RAIL")) {
 							if(agencyname.contentEquals("台灣高鐵")) {
 								createImageViewbyR(R.drawable.hsr, tr, 50, 50);
 								text = new StringBuilder().append(text).append("hsr,").append(train_num(step.transit_details.headsign)).toString();
 								dires.routes[i].legs[j].mark.add(new MarkP("thsrc"
 										, getResources().getString(R.string.taketransit) + step.transit_details.line.short_name
 										, step.transit_details.headsign
 										, step.transit_details.departure_stop.location));
 							}
 							else if(agencyname.contentEquals("台灣鐵路管理局")) {
 								createImageViewbyR(R.drawable.train, tr, 50, 50);
 								text = new StringBuilder().append(text).append("tra,").append(train_num(step.transit_details.headsign) + ",").append(step.transit_details.line.short_name).toString();
 								dires.routes[i].legs[j].mark.add(new MarkP("tra"
 										, getResources().getString(R.string.taketransit) + step.transit_details.line.short_name
 										, step.transit_details.headsign
 										, step.transit_details.departure_stop.location));
 							}
 							else
 								createTextView("車", tr, Color.rgb(0,0,0), 0.1f, Gravity.CENTER, "transit,null", step.transit_details.departure_stop.name, step.transit_details.arrival_stop.name);
 							headsign = new StringBuilder().append("(" + headsign + ")").toString();
 							createTextView(trans + headsign + trans_to + time_taken, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, text, 
 									step.start_location, step.end_location);
 						}
 					}
 					if(k == dires.routes[i].legs[j].steps.length - 1) {
 						// Arrived
 						tr = CreateTableRow(tl, 1.0f, i);
 						createImageViewbyR(R.drawable.destination, tr, 50, 50);
 						createTextView(dires.routes[i].legs[j].end_address, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT, "map,destination", 
 								dires.routes[i].legs[0].end_location, dires.routes[i].legs[0].end_location);
 
 						dires.routes[i].legs[j].mark.add(new MarkP("end", getResources().getString(R.string.str_to),dires.routes[i].legs[0].end_address, dires.routes[i].legs[0].end_location));
 					}
 				}
 				String str = getResources().getString(R.string.transit) + ": " + transit + "x";
 				createTextView(str, transit_times, Color.rgb(0,0,0), 1.0f, Gravity.LEFT | Gravity.CENTER_VERTICAL, "all," + dires.routes[i].overview_polyline.points, 
 						dires.routes[i].legs[j].mark);
 				// Set time row
 				if(pure_walk_flag == true) {
 					dires.routes[i].legs[j].arrival_time.value = dires.routes[i].legs[j].departure_time.value + duration;
 				}
 				
 				String dur = String.format(" (%d" + getResources().getString(R.string.hour) + "%d" + getResources().getString(R.string.minute) + ")",
 						TimeUnit.SECONDS.toHours(duration), TimeUnit.SECONDS.toMinutes(duration % 3600));
 				title = new StringBuilder().append(convertTime(dires.routes[i].legs[j].departure_time.value)).append(" - ").append(convertTime(dires.routes[i].legs[j].arrival_time.value)).append(dur).toString();
 				createTextView(title, time_row, Color.rgb(0,0,0), 1.0f, Gravity.LEFT | Gravity.CENTER_VERTICAL,
 						"all," + dires.routes[i].overview_polyline.points, dires.routes[i].legs[j].mark);
 			}
 		}
 		// Add the LinearLayout element to the ScrollView
 		sv.addView(tl);
 
 		return true;
 	}
 
 	private String train_num(String ori) {
 		// ori example: 往苗栗,車次1183,山線 or 往左營 ,車次151
 		return ori.replaceAll("[^0-9]", "");
 	}
 	
 	private void decode(String result) {
 		try {
 			Gson gson = new Gson();
 			dires = gson.fromJson(result,	DirectionResponseObject.class);
 			Log.i(TAG, "Total routes = " + dires.routes.length);
 			planning.setVisibility(ProgressBar.GONE);
 			dumpdetails(dires);
 		} catch (Exception e) {
 			planning.setVisibility(ProgressBar.GONE);
 			e.printStackTrace();
 			Toast.makeText(this, getResources().getString(R.string.info_internal_error) , Toast.LENGTH_LONG).show();
 		}
 	}
 
 	public Location GetCurrentPosition() {
 		if(provider != null) {
 			Log.i(TAG, "Current provider is " + provider);
 			return locationMgr.getLastKnownLocation(provider);
 		}
 		else
 			return locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 	}
 
 	private String initLocationProvider() {
 		locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		//1.選擇最佳提供器
 		Criteria criteria = new Criteria();
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		criteria.setAltitudeRequired(false);
 		criteria.setBearingRequired(false);
 		criteria.setCostAllowed(true);
 		criteria.setPowerRequirement(Criteria.POWER_LOW);
 
 		provider = locationMgr.getBestProvider(criteria, true);
 
 		if (provider != null) {
 			return provider;
 		}
 
 		//2.選擇使用GPS提供器
 		//		if (locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 		//			provider = LocationManager.GPS_PROVIDER;
 		//			Toast.makeText(this,"使用" + provider + "定位..." , Toast.LENGTH_LONG).show();
 		//			return true;
 		//		}
 
 		//3.選擇使用網路提供器
 		// if (locationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
 		//  provider = LocationManager.NETWORK_PROVIDER;
 		//  return true;
 		// }
 
 		return null;
 	}
 
 	GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
 		@Override
 		public void onGpsStatusChanged(int event) {
 			switch (event) {
 			case GpsStatus.GPS_EVENT_STARTED:
 				Log.d(TAG, "GPS_EVENT_STARTED");
 				break;
 			case GpsStatus.GPS_EVENT_STOPPED:
 				Log.d(TAG, "GPS_EVENT_STOPPED");
 				break;
 			case GpsStatus.GPS_EVENT_FIRST_FIX:
 				Log.d(TAG, "GPS_EVENT_FIRST_FIX");
 				break;
 			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
 				Log.d(TAG, "GPS_EVENT_SATELLITE_STATUS");
 				break;
 			}
 		}
 	};
 	LocationListener locationListener = new LocationListener(){
 		@Override
 		public void onLocationChanged(Location location) {
 			if (isrequested)
 				Getroute();
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 
 		}
 	};
 
 	public class DirectionResponseObject {
 		public String status;
 		public Route[] routes;
 		String copyrights;
 		Poly overview_polyline;
 		String[] warnings;
 		int[] waypoint_order;
 		Bound bounds;
 
 		public class Route {
 			String summary;
 			String[] warnings;
 			Leg[] legs;
 			Poly overview_polyline;
 			String copyrights;
 
 			public class Leg {
 				public Step[] steps;
 				ValueText duration;
 				Time arrival_time, departure_time;
 				String start_address, end_address;
 				LatLng_s start_location, end_location;
 				List<MarkP> mark;
 
 				public class Step {
 					String travel_mode;
 					LatLng_s start_location, end_location;
 					Poly polyline;
 					ValueText duration;
 					String html_instructions;
 					ValueText distance;
 					Transit transit_details;
 
 					public class Transit {
 						Stop arrival_stop, departure_stop;
 						Time arrival_time, departure_time;
 						String headsign;
 						int num_stops;
 						TransitLine line;
 
 						public class Stop {
 							LatLng_s location;
 							String name;
 						}
 						public class TransitLine {
 							Agency agencies[];
 							String name;
 							String short_name;
 							Vehicle vehicle;
 
 							public class Vehicle {
 								String icon;
 								String name;
 								String type;
 							}
 
 							public class Agency {
 								String name;
 								String url;
 							}
 						}
 					}
 
 					public class Poly {
 						public String points;
 					}
 
 					public class ValueText {
 						int value;
 						String text;
 					}
 				}
 			}
 
 			public class Bound {
 				LatLng_s southwest, northeast;
 			}
 		}
 	}
 
 	public class LatLng_s {
 		double lat;
 		double lng;
 	}
 
 	public class Time {
 		long value;
 		
 		public Time(long v) {
 			value = v;
 		}
 	}
 	
 	public class MarkP {
 		String type;
 		String title;
 		String description;
 		LatLng_s location;
 
 		public MarkP(String s, String t, String d, LatLng_s l) {
 			type = s;
 			title = t;
 			description = d;
 			location = l;
 		}
 	}
 
 	private void showPopup(final Activity context, TextView act) {
 
 		String action = (String) act.getTag(R.id.tag_zero);
 		Log.i(TAG, "tag=" + action);
 		if(action.regionMatches(0, "map", 0, 3)) {
 			Intent launchpop = new Intent(this, pop_map.class);
 			Bundle bundle=new Bundle();
 
 			bundle.putString("poly", action);
 			bundle.putString("departure", (String) act.getTag(R.id.tag_first));
 			bundle.putString("destination", (String) act.getTag(R.id.tag_second));
 			launchpop.putExtras(bundle);
 
 			startActivity(launchpop);
 		}
 		else if(action.regionMatches(0, "all", 0, 3)) {
 			Intent launchpop = new Intent(this, pop_map.class);
 			Bundle bundle=new Bundle();
 			ArrayList<String> types = new ArrayList<String>();
 			ArrayList<String> title = new ArrayList<String>();
 			ArrayList<String> description = new ArrayList<String>();
 			ArrayList<String> locations = new ArrayList<String>();
 
 			@SuppressWarnings("unchecked")
 			List<MarkP> allP = (List<MarkP>)act.getTag(R.id.tag_first);
 
 			Iterator<MarkP> mark =  allP.iterator();
 			while(mark.hasNext()){
 				MarkP e = mark.next();
 				types.add(e.type);
 				title.add(e.title);
 				description.add(e.description);
 				locations.add(e.location.lat + "," + e.location.lng);
 			}
 
 			bundle.putString("poly", action);
 			bundle.putStringArrayList("types", types);
 			bundle.putStringArrayList("title", title);
 			bundle.putStringArrayList("descriptions", description);
 			bundle.putStringArrayList("locations", locations);
 			launchpop.putExtras(bundle);
 			
 			startActivity(launchpop);
 		}
 		else if(action.regionMatches(0, "transit", 0, 7)) {
 			String[] transit_detail = action.split(",");
 			
 			Intent launchpop = new Intent(this, pop_transit.class);
 			Bundle bundle=new Bundle();
 
 			if(transit_detail[1].contentEquals("tra")) {
 				bundle.putString("type", transit_detail[1]);
 				bundle.putString("line", transit_detail[2]);
 				bundle.putString("class", transit_detail[3]);
 				bundle.putLong("time", System.currentTimeMillis());
 			}
 			else if(transit_detail[1].contentEquals("hsr")) {
 				bundle.putString("type", transit_detail[1]);
 				bundle.putString("line", transit_detail[2]);
 			}
 			else if(transit_detail[1].contentEquals("bus")) {
 				bundle.putString("type", transit_detail[1]);
 				bundle.putString("line", transit_detail[2]);
 				bundle.putString("agency", transit_detail[3]);
 			}
 			else {
 				bundle.putString("type", transit_detail[1]);	// type = null
 			}
 			
 			launchpop.putExtras(bundle);
 
 			startActivity(launchpop);
 		}
 	}
 }
