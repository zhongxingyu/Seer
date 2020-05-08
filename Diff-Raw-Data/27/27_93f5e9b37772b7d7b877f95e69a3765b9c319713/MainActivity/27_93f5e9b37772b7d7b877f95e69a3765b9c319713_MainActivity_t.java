 package com.ezhang.pop.ui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Locale;
 import java.util.Observable;
 import java.util.Observer;
 
 import android.widget.*;
 import com.ezhang.pop.R;
 import com.ezhang.pop.core.ICallable;
 import com.ezhang.pop.core.LocationService;
 import com.ezhang.pop.model.FuelDistanceItem;
 import com.ezhang.pop.navigation.NavigationLaunch;
 import com.ezhang.pop.network.RequestManager;
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
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class MainActivity extends Activity implements Observer {
     /**
      * There's no need to continue if switched to another activity
      */
     enum EmIndication{
         EmContinue,
         EmStop
     }
 
     /**
      * Manage requests that query data from remote service.
      * */
 	private RequestManager m_reqManager;
 	private LocationManager m_locationManager;
 	private FuelStateMachine m_fuelStateMachine;
 	private ArrayList<FuelDistanceItem> m_fuelInfoList = new ArrayList<FuelDistanceItem>();
 	private ListView m_fuelDistanceItemlistView = null;
 
     private Button m_refreshButton = null;
     private AnimationDrawable m_refreshButtonAnimation = null;
     private AppSettings m_settings = null;
     private AlertDialog m_networkAlertDlg = null;
     private AlertDialog m_locationAccessDlg = null;
     private AlertDialog m_gpsOrCustomLocationDlg = null;
 
     /**
      * The user will be required to choose the location provider type: location service or set the location manually.
      * This variable is to prevent the selection dialog appearing again on again as the app might be resumed several times during the first session.
      * */
     private boolean m_locationTypeSelected = false;
     private Toast m_toast;
     private Bundle m_savedInstanceState;
 
     @Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_activity);
 
 		m_reqManager = RequestManager.from(this);
 		m_refreshButton = (Button) findViewById(R.id.RefreshButtton);
 		m_refreshButtonAnimation = (AnimationDrawable) m_refreshButton
 				.getBackground();
 
 		if (savedInstanceState != null) {
             m_savedInstanceState = savedInstanceState;
 		}
 
         InitFuelDistanceItemListView();
         m_settings = new AppSettings(this);
 	}
 
     private void InitFuelDistanceItemListView() {
         m_fuelDistanceItemlistView = (ListView) this.findViewById(R.id.fuelDistanceItemlistView);
         m_fuelDistanceItemlistView.setAdapter(new FuelListAdapter(this, m_fuelInfoList));
         m_fuelDistanceItemlistView.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> a, View v, int position,
                                     long id) {
                 Object o = m_fuelDistanceItemlistView.getItemAtPosition(position);
                 FuelDistanceItem fullObject = (FuelDistanceItem) o;
                 NavigationLaunch launch = new NavigationLaunch(
                         MainActivity.this, fullObject.latitude, fullObject.longitude);
                 launch.Launch();
             }
         });
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
 
 	public void OnChangeLocationClicked(View v) {
 		Intent intent = new Intent(this, CustomLocationActivity.class);
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
        if(m_fuelStateMachine != null){
            m_fuelStateMachine.SaveInstanceState(outState);
        }
 		super.onSaveInstanceState(outState);
 	}
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         m_savedInstanceState = savedInstanceState;
         super.onRestoreInstanceState(savedInstanceState);
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
 
         PromptEnableNetwork();
 
         if (PromptLocationTypeSelection() == EmIndication.EmStop){
             return;
         }
 
         OnLocationTypeSelected();
 	}
 
     private EmIndication PromptLocationTypeSelection() {
         boolean isFirstRun = this.m_settings.IsFirstRun();
         if(isFirstRun && !m_locationTypeSelected)
         {
             CreateGPSOrCustomLocationAlertDlg();
             if(!m_gpsOrCustomLocationDlg.isShowing()){
                 m_gpsOrCustomLocationDlg.show();
             }
            return EmIndication.EmStop;
         }
         return EmIndication.EmContinue;
     }
 
     private void OnLocationTypeSelected() {
         boolean useGPS = m_settings.UseGPSAsLocation();
         if (useGPS) {
             if (PromptEnableLocationService() == EmIndication.EmStop){
                 return;
             }
         }
 
         ShowStatusText("Waiting For Location Information");
         SwitchToWaitingStatus();
 
         if (m_fuelStateMachine == null) {
             CreateStateMachine();
         } else {
             RestoreFromSavedInstance();
             m_fuelStateMachine.ToggleGPS(useGPS);
             m_fuelStateMachine.Refresh();
         }
     }
 
     private void RestoreFromSavedInstance() {
        if(m_savedInstanceState != null && m_fuelStateMachine != null){
             m_fuelStateMachine.RestoreFromSaveInstanceState(m_savedInstanceState);
             m_savedInstanceState = null;
         }
     }
 
     private void CreateStateMachine() {
         m_fuelStateMachine = new FuelStateMachine(m_reqManager,
                 m_locationManager, m_settings);
         m_fuelStateMachine.addObserver(this);
         RestoreFromSavedInstance();
        m_fuelStateMachine.ToggleGPS(m_settings.UseGPSAsLocation());
         m_fuelStateMachine.Refresh();
         this.update(null, null);
     }
 
     private void PromptEnableNetwork() {
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
     }
 
     private EmIndication PromptEnableLocationService() {
         boolean isGPSEnabled = m_locationManager
                 .isProviderEnabled(LocationManager.GPS_PROVIDER);
         boolean isNetworkLocationEnabled = m_locationManager
                 .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 
         boolean isOnSimulator = Build.FINGERPRINT.startsWith("generic");
         isNetworkLocationEnabled |= isOnSimulator; // network location is
         // not
         // supported by
         // simulator.
 
         if (!isGPSEnabled || !isNetworkLocationEnabled) {
             CreateLocationAccessAlertDlg();
             if (!m_locationAccessDlg.isShowing()) {
                 m_locationAccessDlg.show();
             }
         }
 
         String provider = LocationService.GetBestProvider(m_locationManager);
 
         if (provider == null) {
             if (!m_locationAccessDlg.isShowing()) {
                 m_locationAccessDlg.show();
             }
             return EmIndication.EmStop;
         }
         return EmIndication.EmContinue;
     }
 
     private void CreateGPSOrCustomLocationAlertDlg(){
         if(m_gpsOrCustomLocationDlg != null){
             return;
         }
         AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
 
         // Setting Dialog Title
         alertDialog.setTitle("Select Location Type");
 
         alertDialog.setCancelable(false);
 
         // Setting Dialog Message
         alertDialog
                 .setMessage("You can use GPS to detect your location or set it yourself.");
 
         // On pressing Settings button
         alertDialog.setPositiveButton("Use GPS",
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                         m_settings.UseGPSAsLocation(true);
                         m_locationTypeSelected = true;
                         OnLocationTypeSelected();
                     }
                 });
 
         // On pressing Settings button
         alertDialog.setNegativeButton("Set it myself",
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                         m_settings.UseGPSAsLocation(true);
                         m_locationTypeSelected = true;
                         OnChangeLocationClicked(null);
                     }
                 });
 
         // Showing Alert Message
         m_gpsOrCustomLocationDlg = alertDialog.create();
     }
 
     private void CreateLocationAccessAlertDlg() {
 		if (m_locationAccessDlg != null) {
 			return;
 		}
 		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
 
 		// Setting Dialog Title
 		alertDialog.setTitle("Location Access Required");
 
 		// Setting Dialog Message
 		alertDialog
 				.setMessage("Location Access is disabled. Press go to the settings menu and enable both \n * GPS satellites\n * Wi-Fi & mobile network");
 
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
 				.setMessage("Network is not enabled. Please either enable Wi-Fi or Mobile Network Data");
 
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
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		if (this.m_fuelStateMachine.GetCurState() == EmState.DistanceReceived) {
 			ShowCurrentAddr();
 			this.m_fuelInfoList.clear();
 			this.m_fuelInfoList
 					.addAll(this.m_fuelStateMachine.m_fuelDistanceItems);
 			Collections.sort(m_fuelInfoList, FuelDistanceItem.GetComparer());
 			((BaseAdapter) m_fuelDistanceItemlistView.getAdapter()).notifyDataSetChanged();
 			SwitchToStopWaiting();
 
 			if (this.m_fuelInfoList.size() != 0) {
                 ShowStatusText("Completed");
 			} else {
                 ShowStatusText("Unfortunately, no fuel info was found");
 			}
 		}
 		if (this.m_fuelStateMachine.GetCurState() == EmState.SuburbReceived) {
             ShowStatusText("Suburb Received: "
                     + this.m_fuelStateMachine.m_suburb);
 			ShowCurrentAddr();
 			this.m_fuelInfoList.clear();
 			SwitchToWaitingStatus();
 		}
 
 		if (this.m_fuelStateMachine.GetCurState() == EmState.FuelInfoReceived) {
             ShowStatusText("Fuel Price Info Received");
 			ShowCurrentAddr();
 			SwitchToWaitingStatus();
 			this.m_fuelInfoList.clear();
 			((BaseAdapter) m_fuelDistanceItemlistView.getAdapter()).notifyDataSetChanged();
 			
 		}
 
 		if (this.m_fuelStateMachine.GetCurState() == EmState.Start) {
             ShowStatusText("Waiting For Location Information");
 			HideCurrentAddress();
 			SwitchToWaitingStatus();
 		}
 
 		if (this.m_fuelStateMachine.GetCurState() == EmState.GeoLocationReceived) {
             ShowStatusText("Location Information Received");
 			HideCurrentAddress();
 			SwitchToWaitingStatus();
 		}
 
 		if (this.m_fuelStateMachine.GetCurState() == EmState.Timeout) {
 			if (this.m_fuelStateMachine.m_timeoutEvent == EmEvent.GeoLocationEvent) {
                 ShowStatusText("Unable to get location. Probably because location access is disabled.");
 				HideCurrentAddress();
 			} else if (this.m_fuelStateMachine.m_timeoutEvent == EmEvent.SuburbEvent) {
                 ShowStatusText("Unable to get suburb. Probably because network is disabled.");
 			} else if (this.m_fuelStateMachine.m_timeoutEvent == EmEvent.FuelInfoEvent) {
                 ShowStatusText("Unable to get fuel info. Probably because network is disabled.");
 			}
 
 			this.SwitchToStopWaiting();
 		}
 	}
 
 	private void ShowCurrentAddr() {
 		if (this.m_fuelStateMachine.m_suburb != null
 				&& this.m_fuelStateMachine.m_suburb != "") {
 			int indexOfSuburb = this.m_fuelStateMachine.m_address
 					.indexOf(this.m_fuelStateMachine.m_suburb);
 
 			String addrWithoutSuburb = this.m_fuelStateMachine.m_address
 					.substring(0, indexOfSuburb);
 			TextView v = ((TextView) this.findViewById(R.id.curAddressText));
 			String addrText = String.format(Locale.ENGLISH,
 					"You're at:%s%s.Touch here to change if incorrect.",
 					addrWithoutSuburb, this.m_fuelStateMachine.m_suburb);
 			v.setText(addrText);
 
 			View curAddr = this.findViewById(R.id.layoutCurAddr);
 			curAddr.setVisibility(View.VISIBLE);
 		}
 	}
 
 	private void HideCurrentAddress() {
 		View curAddr = this.findViewById(R.id.layoutCurAddr);
 		curAddr.setVisibility(View.GONE);
 	}
 
 	private void SwitchToStopWaiting() {
 		m_refreshButtonAnimation.stop();
 		m_refreshButton.setEnabled(true);
 	}
 
 	private void SwitchToWaitingStatus() {
 		m_refreshButtonAnimation.start();
 		m_refreshButton.setEnabled(false);
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
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.action_settings:
 			OnSettingsClicked(null);
 			return true;
 		case R.id.share_with_friend_menu:
 			this.OnShareWithFriendClicked(null);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
     private void ShowStatusText(String text) {
         if(m_toast == null){
             m_toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
         }
         else{
             m_toast.setText(text);
         }
         m_toast.show();
     }
 }
