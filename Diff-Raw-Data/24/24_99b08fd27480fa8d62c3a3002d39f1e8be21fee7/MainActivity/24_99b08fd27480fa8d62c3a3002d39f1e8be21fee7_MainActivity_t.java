 package com.ezhang.pop.ui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Observable;
 import java.util.Observer;
 
 import com.ezhang.pop.R;
 import com.ezhang.pop.core.ICallable;
 import com.ezhang.pop.model.FuelDistanceItem;
 import com.ezhang.pop.navigation.NavigationLaunch;
 import com.ezhang.pop.rest.PopRequestManager;
 import com.ezhang.pop.settings.AppSettings;
 import com.ezhang.pop.settings.SettingsActivity;
 import com.ezhang.pop.ui.FuelStateMachine.EmEvent;
 import com.ezhang.pop.ui.FuelStateMachine.EmState;
 
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Build;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.AnimationDrawable;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements Observer {
 	private PopRequestManager m_restReqManager;
 	private LocationManager m_locationManager;
 	private FuelStateMachine m_fuelStateMachine;
 	private ArrayList<FuelDistanceItem> m_fuelInfoList = new ArrayList<FuelDistanceItem>();
 	private ListView m_listView = null;
 	private static final String SAVED_DISTANCE_MATRIX_REQS = "com.ezhang.pop.saved.distance.matrix.reqs";
 
 	Button m_refreshButton = null;
 	AnimationDrawable m_refreshButtonAnimation = null;
 	TextView m_statusText = null;
 	AppSettings m_settings = null;
 	AlertDialog m_networkAlertDlg = null;
 	AlertDialog m_locationAccessDlg = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_activity);
 
 		m_restReqManager = PopRequestManager.from(this);
 		m_refreshButton = (Button) findViewById(R.id.RefreshButtton);
 		m_refreshButtonAnimation = (AnimationDrawable) m_refreshButton
 				.getBackground();
 		m_statusText = (TextView) this.findViewById(R.id.statusText);
 		if (savedInstanceState != null) {
 			ArrayList<FuelDistanceItem> cached = savedInstanceState
 					.getParcelableArrayList(SAVED_DISTANCE_MATRIX_REQS);
 			m_fuelInfoList.addAll(cached);
 		}
 
 		m_listView = (ListView) this.findViewById(R.id.listView);
 		m_listView.setAdapter(new FuelListAdapter(this, m_fuelInfoList));
 		m_listView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> a, View v, int position,
 					long id) {
 				Object o = m_listView.getItemAtPosition(position);
 				FuelDistanceItem fullObject = (FuelDistanceItem) o;
 				if (m_fuelStateMachine != null
 						&& m_fuelStateMachine.m_location != null) {
 
 					NavigationLaunch launch = new NavigationLaunch(
 							MainActivity.this, String
 									.valueOf(m_fuelStateMachine.m_location
 											.getLatitude()), String
 									.valueOf(m_fuelStateMachine.m_location
 											.getLongitude()),
 							fullObject.latitude, fullObject.longitude);
 					launch.Launch();
 				}
 			}
 		});
 		m_settings = new AppSettings(this);
 	}
 
 	public void OnRefreshClicked(View v) {
 		if (this.m_fuelStateMachine != null) {
 			this.m_fuelStateMachine.Refresh();
 		}
 	}
 
 	public void OnSettingsClicked(View v) {
 		Intent intent = new Intent(this, SettingsActivity.class);
 		this.startActivity(intent);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putParcelableArrayList(SAVED_DISTANCE_MATRIX_REQS,
 				m_fuelInfoList);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		m_settings.LoadDiscountSettings(new ICallable<Object, Object>() {
 			public Object Call(Object o) {
 				OnDiscountInfoLoaded();
 				return o;
 			}
 		});
 	}
 
 	private void OnDiscountInfoLoaded() {
 		if (m_locationManager == null) {
 			m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		}
 
 		ConnectivityManager nInfo = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo network = nInfo.getActiveNetworkInfo();
 		boolean hasNetwork = false;
 		if (network != null) {
 			hasNetwork = network.isConnectedOrConnecting();
 		}
 		if (!hasNetwork) {
 			CreateNetworkAlertDlg();
 			if (!m_networkAlertDlg.isShowing()) {
 				m_networkAlertDlg.show();
 			}
 		}
 
 		boolean isGPSEnabled = m_locationManager
 				.isProviderEnabled(LocationManager.GPS_PROVIDER);
 		boolean isNetworkLocationEnabled = m_locationManager
 				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 
 		boolean isOnSimulator = Build.FINGERPRINT.startsWith("generic");
 		isNetworkLocationEnabled |= isOnSimulator; // network location is not
 													// supported by simulator.
 
 		if (!isGPSEnabled || !isNetworkLocationEnabled) {
 			CreateLocationAccessAlertDlg();
 			if (!m_locationAccessDlg.isShowing()) {
 				m_locationAccessDlg.show();
 			}
 		}
 
 		if (m_fuelStateMachine == null) {
 			m_fuelStateMachine = new FuelStateMachine(this.m_restReqManager,
 					this.m_locationManager, this.m_settings);
 			m_fuelStateMachine.addObserver(this);
 		} else {
 			m_fuelStateMachine.Refresh();
 		}
 		m_statusText.setText("Waiting For Location Information");
 		SwitchToWaitingStatus();
 	}
 
 	private void CreateLocationAccessAlertDlg() {
 		if (m_locationAccessDlg != null) {
 			return;
 		}
 		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
 
 		// Setting Dialog Title
 		alertDialog.setTitle("Locaion Access Required");
 
 		// Setting Dialog Message
 		alertDialog
 				.setMessage("Locaion Access is not enabled. Press go to the settings menu and enbale both \n * GPS satellites\n * Wi-Fi & mobile network");
 
 		// On pressing Settings button
 		alertDialog.setPositiveButton("Go",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						Intent intent = new Intent(
 								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 						MainActivity.this.startActivity(intent);
 					}
 				});
 
 		// Showing Alert Message
 		m_locationAccessDlg = alertDialog.create();
 	}
 
 	private void CreateNetworkAlertDlg() {
 		if (m_networkAlertDlg != null) {
 			return;
 		}
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
 		// Setting Dialog Title
 		alertDialogBuilder.setTitle("Network Access Required");
 
 		// Setting Dialog Message
 		alertDialogBuilder
 				.setMessage("Network is not enabled. Press either enable Wi-Fi or Mobile Network Data");
 
 		// On pressing Settings button
 		alertDialogBuilder.setPositiveButton("Wi-Fi",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						Intent intent = new Intent(
 								Settings.ACTION_WIFI_SETTINGS);
 						MainActivity.this.startActivity(intent);
 					}
 				});
 
 		// On pressing Settings button
 		alertDialogBuilder.setNegativeButton("MobileNetwork",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						Intent intent = new Intent(
 								Settings.ACTION_DATA_ROAMING_SETTINGS);
 						// intent.setClassName("com.android.phone",
 						// "com.android.phone.Settings");
 						MainActivity.this.startActivity(intent);
 					}
 				});
 
 		// Showing Alert Message
 		m_networkAlertDlg = alertDialogBuilder.create();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		// TODO
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		if (this.m_fuelStateMachine.GetCurState() == EmState.DistanceRecieved) {
 			this.m_fuelInfoList.clear();
 			this.m_fuelInfoList
 					.addAll(this.m_fuelStateMachine.m_fuelDistanceItems);
 			Collections.sort(m_fuelInfoList, FuelDistanceItem.GetComparer());
 			m_statusText.setText("Completed");
 			((BaseAdapter) m_listView.getAdapter()).notifyDataSetChanged();
 			SwitchToStopWaiting();
 		}
 		if (this.m_fuelStateMachine.GetCurState() == EmState.SuburbRecieved) {
 			m_statusText.setText("Suburb Recieved: "
 					+ this.m_fuelStateMachine.m_suburb);
 			if (this.m_fuelStateMachine.m_suburb != null
 					&& this.m_fuelStateMachine.m_suburb != "") {
 				int indexOfSuburb = this.m_fuelStateMachine.m_address
 						.indexOf(this.m_fuelStateMachine.m_suburb);
 
 				String addrWithoutSuburb = this.m_fuelStateMachine.m_address
 						.substring(0, indexOfSuburb);
 				TextView v = ((TextView) this.findViewById(R.id.curAddressText));
 				v.setText("You're at:" + addrWithoutSuburb
 						+ this.m_fuelStateMachine.m_suburb);
 				v.setVisibility(View.VISIBLE);
 			}
 			SwitchToWaitingStatus();
 		}
 
 		if (this.m_fuelStateMachine.GetCurState() == EmState.FuelInfoRecieved) {
 			m_statusText.setText("Fuel Price Info Recieved");
 			SwitchToWaitingStatus();
 		}
 
 		if (this.m_fuelStateMachine.GetCurState() == EmState.Start) {
 			m_statusText.setText("Waiting For Location Information");
 			HideCurrentAddress();
 			SwitchToWaitingStatus();
 		}
 
 		if (this.m_fuelStateMachine.GetCurState() == EmState.GeoLocationRecieved) {
 			m_statusText.setText("Location Information Recieved");
 			HideCurrentAddress();
 			SwitchToWaitingStatus();
 		}
 
 		if (this.m_fuelStateMachine.GetCurState() == EmState.Timeout) {
 			if (this.m_fuelStateMachine.m_timeoutEvent == EmEvent.GeoLocationEvent) {
				m_statusText
						.setText("Unable to get location. Probably because location access is disabled.");
 				HideCurrentAddress();
			} else if (this.m_fuelStateMachine.m_timeoutEvent == EmEvent.SuburbEvent) {
				m_statusText
						.setText("Unable to get suburb. Probably because network is disabled.");
			} else if (this.m_fuelStateMachine.m_timeoutEvent == EmEvent.FuelInfoEvent) {
				m_statusText
						.setText("Unable to get fuel info. Probably because network is disabled.");
 			}

 			this.SwitchToStopWaiting();
 			m_statusText.setVisibility(View.VISIBLE);
 		}
 	}
 
 	private void HideCurrentAddress() {
 		TextView v = ((TextView) this.findViewById(R.id.curAddressText));
 		v.setVisibility(View.GONE);
 	}
 
 	private void SwitchToStopWaiting() {
 		m_refreshButtonAnimation.stop();
 		m_refreshButton.setEnabled(true);
 		m_statusText.setVisibility(View.GONE);
 	}
 
 	private void SwitchToWaitingStatus() {
 		m_refreshButtonAnimation.start();
 		m_refreshButton.setEnabled(false);
 		m_statusText.setVisibility(View.VISIBLE);
 	}
 
 	public void OnShareWithFriendClicked(View v) {
 		String url = String.format(
 				"https://play.google.com/store/apps/details?id=%s",
 				getApplicationContext().getPackageName());
 		String content = "Hey, I'm using WaFuelFuel, an awesome app can help find the cheapest fuel station nearby.\nCheck it out in GooglePlay: "
 				+ url;
 		Intent shareIntent = new Intent(Intent.ACTION_SEND);
 		shareIntent.setType("text/plain");
 		shareIntent.putExtra(Intent.EXTRA_TEXT, content);
 		startActivity(shareIntent);
 	}
 }
