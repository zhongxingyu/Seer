 package de.cstimming.konphidroid;
 
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.ColorStateList;
 import android.graphics.Color;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 public class KonPhiActivity extends Activity implements LocationListener, SliceSenderResult {
 
 	private ToggleButton m_togglebuttonGps;
 	private Button m_togglebuttonSender;
 	private Button m_togglebuttonRecord;
 
 	private SimpleDateFormat m_dateFormatter;
 	private SimpleDateFormat m_dateFormatSender;
 	private NumberFormat m_speedFormatter;
 
 	private Location m_lastLocation;
 	private boolean m_lastLocationValid;
 	private long m_recordIntervalSecs;
 	private String m_server = "http://carcomm.cstimming.de/";
 
 	//private WebView m_webview;
 	private TextView m_labelSender;
 	private int m_instanceId;
 	private int m_categoryId;
 	
 	private LocationCollector m_locationCollector;
 
 	private static final int DIALOG_GPSWARNING = 1;
 	private static final int DIALOG_RECORDINTERVAL_MULTICHOICE = 2;
 	private static final int DIALOG_SENDERINTERVAL_MULTICHOICE = 3;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		m_dateFormatSender = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
 		m_dateFormatSender.setTimeZone(TimeZone.getTimeZone("GMT00"));
 		m_dateFormatter = new SimpleDateFormat("HH:mm:ss");
 
 		m_speedFormatter = NumberFormat.getInstance();
 		m_speedFormatter.setMaximumFractionDigits(1);
 
 		m_togglebuttonGps = (ToggleButton) findViewById(R.id.togglebuttonGps);
 		m_togglebuttonSender = (Button) findViewById(R.id.togglebuttonSender);
 		m_togglebuttonRecord = (Button) findViewById(R.id.togglebuttonRecord);
 		m_labelSender = (TextView) findViewById(R.id.TextLabelSender);
 
 		m_recordIntervalSecs = 0;
 		m_locationCollector = new LocationCollector(m_server, this, m_instanceId, m_categoryId, this);
 
		setRecordIntervalSecs(2);
 		setSenderIntervalSecs(60);
		java.util.Random rg = new java.util.Random();
		m_instanceId = rg.nextInt();
		m_categoryId = 1;
 
 		// Print the version number into the UI
 		TextView versionView = (TextView) findViewById(R.id.TextViewVersion);
 		String versionString;
 		try {
 			versionString = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
 		} catch (NameNotFoundException e) {
 			versionString = "Not found";
 			e.printStackTrace();
 		}
 		versionView.setText(" " + versionString);
 		
 		//final LinearLayout ell = (LinearLayout) findViewById(R.id.LayoutWebview);
 		//m_webview = new WebView(this);
 		//ell.addView(m_webview);
 
 		final LocationManager locationManager = (LocationManager) getApplicationContext()
 		.getSystemService(Context.LOCATION_SERVICE);
 
 		m_togglebuttonGps.setChecked(locationManager
 				.isProviderEnabled(LocationManager.GPS_PROVIDER));
 
 		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 			showDialog(DIALOG_GPSWARNING);
 		}
 
 		m_togglebuttonGps.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				// Perform action on clicks
 				final Intent i = new Intent(
 						Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 				i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
 				startActivity(i);
 				m_togglebuttonGps.setChecked(locationManager
 						.isProviderEnabled(LocationManager.GPS_PROVIDER));
 			}
 		});
 		m_togglebuttonSender.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				showDialog(DIALOG_SENDERINTERVAL_MULTICHOICE);
 			}});
 		m_togglebuttonRecord.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				showDialog(DIALOG_RECORDINTERVAL_MULTICHOICE);
 			}});
 
 		registerAtLocationManager(getRecordIntervalSecs());
 	}
 	
 	private void registerAtLocationManager(long intervalSecs) {
 		final LocationManager locationManager = (LocationManager) getApplicationContext()
 		.getSystemService(Context.LOCATION_SERVICE);
 		LocationListener listener = m_locationCollector;//this;
 		if (intervalSecs == 0) {
 			locationManager.removeUpdates(listener);
 		} else {
 			if (!locationManager
 					.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 				showDialog(DIALOG_GPSWARNING);
 			}
 
 			long minTime = 1000 * intervalSecs; // [milliseconds]
 			float minDistance = 0; // 10; // [meters]
 			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 					minTime, minDistance, listener);
 		}
 	}
 
 	public long getRecordIntervalSecs() {
 		return m_recordIntervalSecs;
 	}
 
 	public void setSenderIntervalSecs(long v) {
 		if (v == m_locationCollector.getSendingIntervalSecs())
 			return;
 		final TextView resultview = (TextView) findViewById(R.id.TextViewSender);
 		if (v == 0) {
 			m_togglebuttonSender.setText(R.string.transmission_off);
 			m_labelSender.setText(R.string.labelsender_off);
 			m_labelSender.setTextColor(ColorStateList.valueOf(Color.RED));
 			resultview.setTextColor(ColorStateList.valueOf(Color.GRAY));
 		} else {
 			m_togglebuttonSender.setText(getString(R.string.transmission_on) + " " + String.valueOf(v / 60) + "min");
 			m_labelSender.setText(R.string.labelsender_on);
 			final TextView viewSecs = (TextView) findViewById(R.id.TextViewSecs);
 			m_labelSender.setTextColor(viewSecs.getTextColors());
 		}
 		m_locationCollector.setSendingIntervalSecs(v);
 	}
 
 	public void setRecordIntervalSecs(long v) {
 		//if (v == m_recordIntervalSecs)
 			//return;
 		final TextView resultview = (TextView) findViewById(R.id.TextViewSender);
 		if (v == 0) {
 			m_togglebuttonRecord.setText(R.string.recording_off);
 			m_labelSender.setText(R.string.labelsender_off);
 			m_labelSender.setTextColor(ColorStateList.valueOf(Color.RED));
 			resultview.setTextColor(ColorStateList.valueOf(Color.GRAY));
 		} else {
 			m_togglebuttonRecord.setText(getString(R.string.recording_on) + " " + String.valueOf(v) + "s");
 			m_labelSender.setText(R.string.labelsender_on);
 			final TextView viewSecs = (TextView) findViewById(R.id.TextViewSecs);
 			m_labelSender.setTextColor(viewSecs.getTextColors());
 		}
 		registerAtLocationManager(v);
 		m_recordIntervalSecs = v;
 	}
 
 	protected Dialog onCreateDialog(int d) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		switch (d) {
 		case DIALOG_GPSWARNING:
 			
 			builder.setMessage(R.string.ask_activate_gps_settings)
 			.setCancelable(false)
 			.setPositiveButton(R.string.yes,
 					new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,
 						int id) {
 					final Intent i = new Intent(
 							Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 					i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
 					startActivity(i);
 				}
 			})
 			.setNegativeButton(R.string.no,
 					new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,
 						int id) {
 					dialog.cancel();
 				}
 			});
 			break;
 		case DIALOG_RECORDINTERVAL_MULTICHOICE:
 			builder.setTitle(R.string.choose_record_interval);
 			final CharSequence[] items = {"60", "30", "20", "10", "2", "1", "off"};
			builder.setSingleChoiceItems(items, 1, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					String item = items[which].toString();
 					final int secs = (item == "off" ? 0 : Integer.parseInt(item));
 					setRecordIntervalSecs(secs);
 					dismissDialog(DIALOG_RECORDINTERVAL_MULTICHOICE);
 				}
 			});
 			break;
 		case DIALOG_SENDERINTERVAL_MULTICHOICE:
 			builder.setTitle(R.string.choose_sending_interval);
 			final CharSequence[] items1 = {"10", "5", "2", "1", "off"};
			builder.setSingleChoiceItems(items1, 1, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					String item = items1[which].toString();
 					final int mins = (item == "off" ? 0 : Integer.parseInt(item));
 					setSenderIntervalSecs(60 * mins);
 					dismissDialog(DIALOG_SENDERINTERVAL_MULTICHOICE);
 				}
 			});
 			break;
 		default:
 			return null;
 		}
 		Dialog result = builder.create();
 		return result;
 	}
 
 	@Override
 	public void onLocationChanged(Location loc) {
 		// Toast.makeText(KonPhiActivity.this, "Received location from " +
 		// loc.getProvider(), Toast.LENGTH_SHORT).show();
 
 		printLocInfo(loc);
 
 		m_lastLocationValid = true;
 		m_lastLocation = loc;
 	}
 
 	private void printLocInfo(Location loc) {
 		final TextView viewgpsage = (TextView) findViewById(R.id.TextViewGPSAge);
 		final TextView viewLat = (TextView) findViewById(R.id.TextViewLat);
 		final TextView viewLon = (TextView) findViewById(R.id.TextViewLon);
 		final TextView viewAcc = (TextView) findViewById(R.id.TextViewAcc);
 		final TextView viewDist = (TextView) findViewById(R.id.TextViewDist);
 		final TextView viewSecs = (TextView) findViewById(R.id.TextViewSecs);
 		final TextView viewSpeed = (TextView) findViewById(R.id.TextViewSpeed);
 
 		viewgpsage.setText(m_dateFormatter.format(new Date(loc.getTime())));
 		viewLat.setText(String.valueOf(loc.getLatitude()));
 		viewLon.setText(String.valueOf(loc.getLongitude()));
 		viewAcc.setText(loc.hasAccuracy() ? String.valueOf(loc.getAccuracy())
 				: "...");
 		float speed = loc.getSpeed();
 		viewSpeed.setText(m_speedFormatter.format(3.6 * speed) + " km/h");
 
 		if (m_lastLocationValid) {
 			long msecdiff = loc.getTime() - m_lastLocation.getTime();
 			float distance = m_lastLocation.distanceTo(loc);
 			String secs = String.valueOf(msecdiff / 1000);
 			String dist = String.valueOf(distance);
 			viewDist.setText(dist);
 			viewSecs.setText(secs);
 		} else {
 			viewDist.setText("...");
 			viewSecs.setText("...");
 		}
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// Toast.makeText(KonPhiActivity.this, "Oh: Provider " + provider +
 		// " disabled", Toast.LENGTH_SHORT).show();
 		m_togglebuttonGps.setChecked(false);
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// Toast.makeText(KonPhiActivity.this, "Good: Provider " + provider +
 		// " enabled", Toast.LENGTH_SHORT).show();
 		m_togglebuttonGps.setChecked(true);
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		switch (status) {
 		case LocationProvider.OUT_OF_SERVICE:
 			// Toast.makeText(KonPhiActivity.this, "Oh: Provider " + provider +
 			// " out of service", Toast.LENGTH_SHORT).show();
 			m_togglebuttonGps.setChecked(false);
 			break;
 		case LocationProvider.TEMPORARILY_UNAVAILABLE:
 			// Toast.makeText(KonPhiActivity.this, "Oh: Provider " + provider +
 			// " tmp. unavailable", Toast.LENGTH_SHORT).show();
 			m_togglebuttonGps.setChecked(false);
 			break;
 		case LocationProvider.AVAILABLE:
 			// Toast.makeText(KonPhiActivity.this, "Good: Provider " + provider
 			// + " available", Toast.LENGTH_SHORT).show();
 			m_togglebuttonGps.setChecked(true);
 			break;
 		}
 	}
 
 	@Override
 	public void sliceSenderResult(String text, boolean good, int color) {
 		final TextView resultview = (TextView) findViewById(R.id.TextViewSender);
 		resultview.setText(text);
 		resultview.setTextColor(ColorStateList.valueOf(color));
 	}
 }
