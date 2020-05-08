 package com.yakov.goldberg.lpg;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.util.Log;
 import android.view.View.OnClickListener;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 /*  Uncomment then use with Fragment.
  *  Need also change
  *  com.google.android.gms.maps.SupportMapFragment
  *  to
  *  com.google.android.gms.maps.MapFragment 
  *  */
 //import android.app.Activity;
 //import com.google.android.gms.maps.MapFragment;
 
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.content.LocalBroadcastManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.res.AssetManager;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 /*
  import android.os.StrictMode;
  import android.os.StrictMode.ThreadPolicy;
  */
 
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.PopupWindow;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
 
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptor;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 
 public class MainActivity extends FragmentActivity implements
 		OnMarkerClickListener, OnInfoWindowClickListener {
 
 	private GoogleMap mMap;
 	private TextView tw;
 	private Button but_prev;
 	private Button but_next;
 	private LinearLayout message_bar;
 	private GPSLocation gps;
 	private LPGData ld;
 	static final String filename = "lpg_db";
 	private Marker cur_marker;
 	private double min_price = 2.5;
 	private double max_price = 0;
 	private double dprice = 2.0;
 	private SharedPreferences sharedPref;
 	Map<Integer, String> map;
 	private int mInterval = 2000; // 5 seconds by default, can be changed later
 	private Handler mHandler;
 	private final ResponseReceiver rr = new ResponseReceiver();
 
 	Runnable mStatusChecker = new Runnable() {
 		@Override
 		public void run() {
 			updateStatus(); // this function can change value of mInterval.
 		}
 	};
 
 	int count = 0;
 
 	void startRepeatingTask() {
 		count = 0;
 		mStatusChecker.run();
 	}
 
 	void updateStatus() {
 		Log.v("", "status: count " + Integer.toString(count));
 		LPGApp h = (LPGApp) this.getApplication();
 		if (h.updated_prices.length() != 0) {
 			Log.v("", "status: count " + Integer.toString(count) + " show");
 			showPopup3(h.updated_prices);
 			h.updated_prices = "";
 			return;
 		}
 
 		if (count < 30) {
 			mHandler.postDelayed(mStatusChecker, mInterval);
 		}
 		count += 1;
 	}
 
 	private void setMinMaxPrice(double _min_price) {
 		min_price = _min_price;
 		max_price = _min_price + dprice;
 	}
 
 	private void runDbFetchService() {
 		Intent srv;
 		srv = new Intent(this, BackgroundService.class);
 		String uriString = "lpg://?op=get_all";
 		srv.setData(Uri.parse(uriString));
 		startService(srv);
 	}
 
 	private void runTimeFetchService() {
 		Intent srv;
 		srv = new Intent(this, BackgroundService.class);
 		String uriString = "lpg://?op=get_timestamp";
 		srv.setData(Uri.parse(uriString));
 		startService(srv);
 	}
 
 	private void runPriceSetService(String lat, String lng, String price) {
 		Intent srv;
 		srv = new Intent(this, BackgroundService.class);
 		String uriString = String.format(
 				"lpg://?op=update_price&lat=%s&lng=%s&price=%s", lat, lng,
 				price);
 		srv.setData(Uri.parse(uriString));
 		startService(srv);
 	}
 
 	private class ResponseReceiver extends BroadcastReceiver {
 		private ResponseReceiver() {
 		}
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			// TODO Auto-generated method stub
 
 			String uriString = intent.getStringExtra("extra");
 			Uri u = Uri.parse(uriString);
 			String answer = u.getQueryParameter("answer");
 
 			if (answer.equals("time")) {
 				Log.v("", "status: time");
 
 				JSONObject jo = null;
 				try {
 					jo = new JSONObject(u.getQueryParameter("config_data"));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 				int timestamp = 0;
 				try {
 					timestamp = jo.getInt("timestamp");
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				if (timestamp > ld.getTimestamp()) {
 					runDbFetchService();
 				}
 			}
 
 			else if (answer.equals("all")) {
 				Log.v("", "status: all");
 				String res = "";
 				JSONArray arr = null;
 				JSONObject config_data = null;
 				try {
 					arr = new JSONArray(u.getQueryParameter("data"));
 					config_data = new JSONObject(
 							u.getQueryParameter("config_data"));
 					setMinMaxPrice(config_data.getDouble("min_price"));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				/* here need to compare new data 'arr' and old */
 				for (int i = 0; i < arr.length(); i++) {
 					try {
 						JSONObject item = arr.getJSONObject(i);
						int type = item.getInt("type");
						if (type != 1) continue;
 						int id = item.getInt("id");
 						double price_new = item.getDouble("price");
 						JSONObject item_old = ld.getRecordByKey(id);
 						if (item_old != null) {
 							String l = "";
 							String name = item.getString("name");
 							double price_old = item_old.getDouble("price");
 							if ((price_old == 0) && (price_new != 0)) {
 								l = name + ": \u200e\u20aa "
 										+ Double.toString(price_new) + "\n";
 								res += l;
 							} else if (price_old > price_new) {
 								l = name + ": \u200e\u20aa "
 										+ Double.toString(price_new) + "\n";
 								res += l;
 							} else if (price_old < price_new) {
 								l = name + ": \u200e\u20aa "
 										+ Double.toString(price_new) + "\n";
 								res += l;
 							}
 						}
 
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 				LPGApp h = (LPGApp) context.getApplicationContext();
 				h.updated_prices = res;
 				Log.v("", "status: res: " + res);
 
 				try {
 					FileOutputStream fos;
 					fos = openFileOutput(filename, Context.MODE_PRIVATE);
 					ld.setConfigDataAndData(fos, config_data, arr);
 					fos.close();
 					drawMap(ld.getArr());
 					// status("redrawing map");
 				} catch (Exception ee) {
 					ee.printStackTrace();
 				}
 			} else if (answer.equals("price")) {
 				status("price updated");
 				// runTimeFetchService();
 			}
 		}
 	}
 
 	/* Custom infoWindow. */
 	private class LPGWindow implements InfoWindowAdapter {
 		private final View window = getLayoutInflater().inflate(
 				R.layout.info_window, null);
 
 		public View getInfoContents(Marker marker) {
 			// TODO Auto-generated method stub
 
 			// View v = getLayoutInflater().inflate(R.layout.info_window, null);
 			int idx = Integer.parseInt(marker.getTitle());
 			try {
 				JSONObject jo = ld.getRecordByKey(idx);
 				int type = jo.getInt("type");
 				TextView tw_name = (TextView) window
 						.findViewById(R.id.txtInfoWindowName);
 				TextView tw_time = (TextView) window
 						.findViewById(R.id.txtInfoWindowTime);
 				TextView tw_price = (TextView) window
 						.findViewById(R.id.txtInfoWindowPrice);
 				TextView tw_update = (TextView) window
 						.findViewById(R.id.txtInfoWindowUpdate);
 				TextView tw_additional = (TextView) window
 						.findViewById(R.id.txtInfoWindowAdditional);
 				tw_name.setText(jo.getString("name"));
 				tw_time.setText(jo.getString("time"));
 
 				if (type == 1) {
 					tw_name.setText(jo.getString("name"));
 					tw_time.setText(jo.getString("time"));
 					String price = Double.toString(jo.getDouble("price"));
 					if (price.length() == 1)
 						price = price + ".00";
 					if (price.length() == 3)
 						price = price + "0";
 
 					tw_price.setText("\u200eמחיר: \u20aa" + price);
 					tw_price.setTypeface(null, Typeface.BOLD);
 					tw_price.setTextSize(14);
 					tw_update
 							.setText("\u200eעודכן: " + jo.getString("updated"));
 					tw_price.setVisibility(View.VISIBLE);
 					tw_update.setVisibility(View.VISIBLE);
 
 					String text = jo.getString("address");
 					if (jo.getString("phone").length() > 0)
 						text += ", " + jo.getString("phone");
 					if (text.length() == 0) {
 						tw_additional.setVisibility(View.GONE);
 					} else {
 						tw_additional.setVisibility(View.VISIBLE);
 						tw_additional.setText("\u200e" + text);
 					}
 				}
 				if (type == 2) {
 					tw_name.setText(jo.getString("name"));
 					tw_time.setText("\u200e" + jo.getString("address"));
 					tw_price.setText("\u200e" + jo.getString("phone"));
 					tw_price.setTypeface(null, Typeface.NORMAL);
 					tw_price.setTextSize(12);
 					tw_update.setText("\u200e" + jo.getString("time"));
 					tw_additional.setVisibility(View.GONE);
 				}
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			return window;
 		}
 
 		@Override
 		public View getInfoWindow(Marker marker) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		/*
 		 * //ThreadPolicy oldThreadPolicy = StrictMode.getThreadPolicy();
 		 * //StrictMode.setThreadPolicy(new
 		 * StrictMode.ThreadPolicy.Builder(oldThreadPolicy
 		 * ).permitNetwork().permitDiskReads().permitDiskWrites().build());
 		 */
 
 		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		setContentView(R.layout.activity_main);
 		mHandler = new Handler();
 
 		tw = (TextView) findViewById(R.id.textView1);
 		but_prev = (Button) findViewById(R.id.button_prev);
 		but_next = (Button) findViewById(R.id.button_next);
 		message_bar = (LinearLayout) findViewById(R.id.message_bar);
 		boolean show_messages = sharedPref.getBoolean("pref_message_bar", true);
 		map = new HashMap<Integer, String>();
 		map.put(1, "da_marker_and.png");
 		map.put(2, "paz_marker_and.png");
 		map.put(3, "sonol_marker_and.png");
 		map.put(4, "delek_marker_and.png");
 		map.put(5, "tapuz_marker_and.png");
 		map.put(6, "amisragas_marker_and.png");
 		map.put(7, "yaad_marker_and.png");
 		map.put(8, "gaz_igal_marker_and.png");
 		map.put(9, "supergas_marker_and.png");
 		map.put(10, "ten_marker_and.png");
 		map.put(201, "nanagas_marker_and.png");
 		map.put(202, "gaspro_marker_and.png");
 
 		ld = new LPGData();
 		/* Load data from file. */
 		try {
 			FileInputStream fis;
 			fis = openFileInput(filename);
 			ld.loadDataFromFile(fis);
 			fis.close();
 		} catch (FileNotFoundException e) {
 			/* If there is no such file, create it during first launch. */
 			try {
 				FileOutputStream fos;
 				AssetManager am = getAssets();
 				fos = openFileOutput(filename, Context.MODE_PRIVATE);
 				ld.fromAssetsToFile(am, fos);
 				fos.close();
 
 				FileInputStream fis;
 				fis = openFileInput(filename);
 				ld.loadDataFromFile(fis);
 				fis.close();
 			} catch (Exception ee) {
 				ee.printStackTrace();
 			}
 
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		LatLng CurLoc = new LatLng(32.076757, 34.786835);
 		int zoom = 10;
 
 		gps = new GPSLocation(this);
 		FragmentManager fm;
 		SupportMapFragment mapFragment;
 		// fm = getFragmentManager();
 		fm = getSupportFragmentManager();
 		mapFragment = ((SupportMapFragment) fm.findFragmentById(R.id.map));
 		mMap = mapFragment.getMap();
 		mMap.setOnMarkerClickListener(this);
 		mMap.setOnInfoWindowClickListener(this);
 		mMap.setInfoWindowAdapter(new LPGWindow());
 		mMap.setMyLocationEnabled(true);
 		registerForContextMenu(tw);
 		if (gps.canGetLocation()) {
 			CurLoc = new LatLng(gps.getLatitude(), gps.getLongitude());
 			zoom = 11;
 		}
 
 		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(CurLoc, zoom));
 
 		// status(Integer.toString(ld.getTimestamp()));
 
 		setMinMaxPrice(ld.getMinPrice());
 		/* Put markers on map. */
 		drawMap(ld.getArr());
 		/* If version was changed, show popup window to get liked */
 		if (app_version_changed()) {
 			showPopup();
 		}
 		runTimeFetchService();
 	}
 
 	public void drawMap(ArrayList<JSONObject> marker_data) {
 		boolean show_logo = sharedPref.getBoolean("pref_logo", true);
 		boolean show_stations = sharedPref.getBoolean("pref_stations", true);
 		boolean show_garages = sharedPref.getBoolean("pref_garages", true);
 		try {
 			mMap.clear();
 			for (int i = 0; i < marker_data.size(); i++) {
 				JSONObject item = marker_data.get(i);
 
 				int type = item.getInt("type");
 				if ((type == 2) && (!show_garages))
 					continue;
 				if ((type == 1) && (!show_stations))
 					continue;
 				if (type == 0)
 					continue;
 
 				int id = item.getInt("id");
 				String idx = Integer.toString(id);
 				double lat = item.getDouble("lat");
 				double lng = item.getDouble("lng");
 				int owner = item.getInt("owner");
 
 				BitmapDescriptor mar = null;
 
 				if (show_logo && map.containsKey(owner)) {
 					mar = BitmapDescriptorFactory.fromAsset(map.get(owner));
 				} else {
 					float color = 0;
 					switch (type) {
 					case 1:
 						color = BitmapDescriptorFactory.HUE_AZURE;
 						break;
 					case 2:
 						color = BitmapDescriptorFactory.HUE_MAGENTA;
 						break;
 					}
 					mar = BitmapDescriptorFactory.defaultMarker(color);
 				}
 
 				MarkerOptions m = new MarkerOptions().title(idx)
 						.position(new LatLng(lat, lng)).icon(mar);
 				mMap.addMarker(m);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		LocalBroadcastManager.getInstance(this).unregisterReceiver(rr);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (sharedPref.getBoolean("pref_message_bar", true)) {
 			message_bar.setVisibility(View.VISIBLE);
 		} else {
 			message_bar.setVisibility(View.GONE);
 		}
 		drawMap(ld.getArr());
 
 		/* Initialize receiver to handle messages from service intent. */
 		IntentFilter mStatusIntentFilter = new IntentFilter("AnswerIntent");
 		LocalBroadcastManager.getInstance(this).registerReceiver(rr,
 				mStatusIntentFilter);
 
 		if (sharedPref.getBoolean("pref_show_price_updates", true)) {
 			startRepeatingTask();
 		}
 		// FIXME: this moves camera to location, when I cancel NavApp choosing
 		/*
 		 * if(gps.canGetLocation()) { LatLng LocTmp = new
 		 * LatLng(gps.getLatitude(), gps.getLongitude());
 		 * mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LocTmp, 11)); }
 		 */
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		EasyTracker.getInstance().activityStart(this);
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		EasyTracker.getInstance().activityStop(this); // Add this method.
 		mHandler.removeCallbacks(mStatusChecker);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	public boolean onMarkerClick(Marker marker) {
 		return false;
 	}
 
 	@Override
 	public void onInfoWindowClick(Marker marker) {
 		cur_marker = marker;
 		openContextMenu(tw);
 	}
 
 	public void status(String str) {
 		tw.setText(str);
 	}
 
 	/* Menu clicks */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.action_help: {
 			Intent intent = new Intent(this, HelpActivity.class);
 			startActivity(intent);
 			return true;
 		}
 		case R.id.action_settings: {
 			Intent intent = new Intent(this, SettingsActivity.class);
 			startActivity(intent);
 			return true;
 		}
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.point_menu, menu);
 		/* if marker is garage, make "Update price" item invisible. */
 		final int idx = Integer.parseInt(cur_marker.getTitle());
 		try {
 			JSONObject jo = ld.getRecordByKey(idx);
 			int type = jo.getInt("type");
 			if (type == 1) {
 				MenuItem it = menu.findItem(R.id.it_web);
 				it.setVisible(false);
 			} else if (type == 2) {
 				MenuItem it = menu.findItem(R.id.it_price);
 				it.setVisible(false);
 				String web = jo.getString("time");
 				if (web.length() == 0) {
 					it = menu.findItem(R.id.it_web);
 					it.setVisible(false);
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	/* Context menu clicks */
 	public boolean onContextItemSelected(MenuItem item) {
 		final int idx = Integer.parseInt(cur_marker.getTitle());
 		switch (item.getItemId()) {
 		case R.id.it_navigate:
 			final LatLng m_pos = cur_marker.getPosition();
 			cur_marker = null;
 			/* Show place in waze */
 			// String s = String.format("waze://?ll=%s, %s&z=8", m_pos.latitude,
 			// m_pos.longitude);
 			/* Open window to choose map application */
 			String ss = String.format("geo: %s, %s", m_pos.latitude,
 					m_pos.longitude);
 			try {
 				String url = ss;
 				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 				startActivity(intent);
 			} catch (ActivityNotFoundException ex) {
 				Intent intent = new Intent(Intent.ACTION_VIEW,
 						Uri.parse("market://details?id=com.waze"));
 				startActivity(intent);
 			}
 			return true;
 		case R.id.it_web:
 			try {
 				JSONObject jo = ld.getRecordByKey(idx);
 				String url = jo.getString("time");
 				if (!url.startsWith("http://") && !url.startsWith("https://"))
 					url = "http://" + url;
 				Intent browserIntent = new Intent(Intent.ACTION_VIEW,
 						Uri.parse(url));
 				startActivity(browserIntent);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return true;
 		case R.id.it_price:
 			String price = "0.00",
 			t1 = "",
 			t2 = "";
 			try {
 				JSONObject jo = ld.getRecordByKey(idx);
 				price = Double.toString(jo.getDouble("price"));
 				if (price.length() == 1)
 					price = price + ".00";
 				if (price.length() == 3)
 					price = price + "0";
 
 				t1 = Double.toString(jo.getDouble("lat"));
 				t2 = Double.toString(jo.getDouble("lng"));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			final String str_lat = t1;
 			final String str_lng = t2;
 			LayoutInflater inflater = this.getLayoutInflater();
 			View updateView = inflater.inflate(R.layout.price_update_dialog,
 					null);
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setView(updateView);
 			TextView tww = (TextView) updateView
 					.findViewById(R.id.textview_price_memo);
 			String update_memo = getResources().getString(
 					R.string.it_price_memo);
 			update_memo += String.format(" \u20aa%s-%s", min_price, max_price);
 			tww.setText(update_memo);
 
 			final EditText userInput = (EditText) updateView
 					.findViewById(R.id.editTextUpdatePrice);
 
 			builder.setPositiveButton(R.string.ok,
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int id) {
 							String price = userInput.getText().toString();
 							ld.updatePriceByKey(idx, Double.parseDouble(price));
 
 							try {
 								FileOutputStream fos;
 								fos = openFileOutput(filename,
 										Context.MODE_PRIVATE);
 								ld.dumpToFile(fos);
 								fos.close();
 							} catch (Exception ee) {
 								ee.printStackTrace();
 							}
 
 							drawMap(ld.getArr());
 							runPriceSetService(str_lat, str_lng, price);
 						}
 					});
 			builder.setNegativeButton(R.string.cancel,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.cancel();
 						}
 					});
 
 			userInput.setText(price);
 
 			final AlertDialog updateDialog = builder.create();
 
 			// userInput.setTextColor(Color.GREEN);
 			userInput.addTextChangedListener(new TextWatcher() {
 				public void afterTextChanged(Editable s) {
 					String str = s.toString();
 					if (str.length() > 0) {
 						if (str.equals("."))
 							return;
 						double price = Double.parseDouble(str);
 						Button bt2 = updateDialog
 								.getButton(AlertDialog.BUTTON_POSITIVE);
 
 						if ((price < min_price) || (price > max_price)) {
 							// userInput.setTextColor(Color.RED);
 							bt2.setEnabled(false);
 						} else {
 							// userInput.setTextColor(Color.GREEN);
 							bt2.setEnabled(true);
 						}
 					}
 				}
 
 				public void beforeTextChanged(CharSequence s, int start,
 						int count, int after) {
 				}
 
 				public void onTextChanged(CharSequence s, int start,
 						int before, int count) {
 				}
 			});
 			updateDialog.show();
 			Button bt = updateDialog.getButton(AlertDialog.BUTTON_POSITIVE);
 			bt.setEnabled(false);
 
 			return true;
 		case R.id.it_send_message:
 			try {
 				JSONObject jo = ld.getRecordByKey(idx);
 				String name = jo.getString("name");
 				Intent i = (new Helper()).send_mail_to_developer(
 						"Report about mistake: " + name, "");
 				startActivity(Intent.createChooser(i, "Send Email"));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	public void prev_but_clicked(View view) {
 		status("Yahoo");
 		showPopup();
 	}
 
 	/*
 	 * Check if saved version code == current code. Used to show popup window on
 	 * first run after install or update
 	 */
 	public boolean app_version_changed() {
 		int saved_versionCode = 0;
 		int current_versionCode = 0;
 		boolean ret = false;
 
 		PackageInfo packageInfo;
 		try {
 			packageInfo = this.getPackageManager().getPackageInfo(
 					this.getPackageName(), 0);
 			current_versionCode = packageInfo.versionCode;
 		} catch (Exception ee) {
 			ee.printStackTrace();
 		}
 
 		saved_versionCode = sharedPref.getInt(
 				getString(R.string.pref_version_code), 0);
 		if (current_versionCode != saved_versionCode) {
 			ret = true;
 			SharedPreferences.Editor editor = sharedPref.edit();
 			editor.putInt(getString(R.string.pref_version_code),
 					current_versionCode);
 			editor.commit();
 		}
 		return ret;
 	}
 
 	public void showPopup() {
 		// Inflate the popup_layout.xml
 		LinearLayout viewGroup = (LinearLayout) this.findViewById(R.id.popup);
 		LayoutInflater layoutInflater = (LayoutInflater) this
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		final View layout = layoutInflater.inflate(R.layout.popup_start,
 				viewGroup);
 
 		Button close_but;
 		ImageButton rate_but, fb_but;
 		final TextView rate_tw;
 		TextView tw;
 		final PopupWindow popup = new PopupWindow(this);
 
 		// popup.setBackgroundDrawable(new BitmapDrawable());
 		tw = (TextView) layout.findViewById(R.id.popup_header_textview);
 		tw.setVisibility(View.GONE);
 		close_but = (Button) layout.findViewById(R.id.later_but);
 		rate_but = (ImageButton) layout.findViewById(R.id.rate_button);
 		fb_but = (ImageButton) layout.findViewById(R.id.facebook_button);
 		rate_tw = (TextView) layout.findViewById(R.id.popup_textview);
 
 		close_but.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				popup.dismiss();
 			}
 		});
 
 		rate_but.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				String str = "https://play.google.com/store/apps/details?id=com.yakov.goldberg.lpg";
 				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(str)));
 			}
 		});
 
 		final Context ctx = this;
 		fb_but.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				Intent i = (new Helper()).getOpenFacebookIntent(ctx);
 				startActivity(i);
 			}
 		});
 
 		popup.setContentView(layout);
 		layout.post(new Runnable() {
 			public void run() {
 				int w;
 				View ll = (View) findViewById(R.id.main_layout);
 				popup.showAtLocation(ll, Gravity.CENTER, 0, 0);
 				w = ll.getWidth();
 				if (w < 500) {
 					w = w - 20;
 					rate_tw.setTextSize(13);
 					popup.update(0, 0, w, 400);
 				} else {
 					popup.update(0, 0, 500, 550);
 				}
 
 			}
 		});
 	}
 
 	public void showPopup3(String text_to_show) {
 		LayoutInflater inflater = this.getLayoutInflater();
 		View updatedPrices = inflater
 				.inflate(R.layout.price_update_popup, null);
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setView(updatedPrices);
 		TextView tw1 = (TextView) updatedPrices
 				.findViewById(R.id.popup_header_textview2);
 		TextView tw2 = (TextView) updatedPrices
 				.findViewById(R.id.popup_textview2);
 
 		tw1.setText(getResources().getString(R.string.popup_header));
 		tw2.setText(text_to_show);
 
 		builder.setPositiveButton(R.string.ok,
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 					}
 				});
 
 		final AlertDialog updateDialog = builder.create();
 		updateDialog.show();
 	}
 }
