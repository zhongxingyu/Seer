 package com.wifi.sapguestconnect;
 
 import com.wifi.sapguestconnect.autoupdate.AutoUpdater;
 import com.wifi.sapguestconnect.connection.ConnectionErrorMessages;
 import com.wifi.sapguestconnect.connection.ConnectionFacade;
 import com.wifi.sapguestconnect.connection.ConnectionStatus;
 import com.wifi.sapguestconnect.connection.ConnectionFacade.IConnectionAttemptResponse;
 import com.wifi.sapguestconnect.connection.ConnectionFacade.IConnectionStatusResponse;
 import com.wifi.sapguestconnect.data.DataFacade;
 import com.wifi.sapguestconnect.dialog.AboutDialog;
 import com.wifi.sapguestconnect.log.LogManager;
 import com.wifi.sapguestconnect.preferences.PreferencesFacade;
 import com.wifi.sapguestconnect.preferences.SettingsActivity;
 import com.wifi.sapguestconnect.wifi.WatchdogService;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.ScaleAnimation;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class WiFiConnect extends Activity 
 {
 	private static final int BASE_ITEM_ID = 0;
 	private static final int BASE_GROUP_ID = 0;
 	private static final int BASE_ORDER_ID = 0;
 	
 	private Resources mResources = null;
 	
 	private LoginData mLoginData = null;
 	
 	BroadcastReceiver mWifiWatcher = null;
 	
 	private ScaleAnimation mAnimation = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 	    setContentView(R.layout.wifi_connect);
 	    
 	    LogManager.LogFunctionCall("WiFiConnect", "onCreate()");
 		
 		// Init Resources
 		this.mResources = this.getResources();
 		
 		AutoUpdater.CheckForUpdate(this);
 		
 		// Init Layout
 		initLayout();
 		
 		// Init Wifi Watcher
 		initWifiWatcher();
 		
 		// Init Animation
 		initAnimation();
 		
 		WatchdogService.Start(this);
 		
 		// Sets the default location
 	    PreferencesFacade.initLocation(this);
 	}
 	
 	
 	/***
 	 * Broadcast receiver for watching Wifi
 	 */
 	private void initWifiWatcher()
 	{
 	        IntentFilter intentFilter = new IntentFilter();
 	        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
 	        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
 	        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
 	        mWifiWatcher = new BroadcastReceiver() 
 	        {
 				@Override
 				public void onReceive(Context context, Intent intent) 
 				{
 					refreshConnectionStatus();
 				}
 			};
 	        registerReceiver(mWifiWatcher, intentFilter);
 	}
 	
 	private void initAnimation()
 	{
         // Define animation
 		mAnimation = new ScaleAnimation(
                 0.9f, 1, 0.9f, 1, // From x, to x, from y, to y
                 ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                 ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
 		mAnimation.setDuration(600);
 		mAnimation.setFillAfter(true); 
 		mAnimation.setStartOffset(0);
 		mAnimation.setRepeatCount(1);
 		mAnimation.setRepeatMode(Animation.REVERSE);
 
 	}
 	
 	/***
 	 * UI Initializers
 	 */
 	private void initLayout()
 	{
 		LogManager.LogFunctionCall("WiFiConnect", "initLayout()");
 		
 		initConnectBtnLayout();
 	}
 	
 	
 	private void initConnectBtnLayout() 
 	{
 		LogManager.LogFunctionCall("WiFiConnect", "initConnectBtnLayout()");
 		
 	    setViewOnClickListener(R.id.connect_button, new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				WiFiConnect.this.displayToastMessage(mResources.getString(R.string.connecting));
 				
 				ConnectionFacade.ConnectAsync(WiFiConnect.this, mLoginData, new IConnectionAttemptResponse() 
 				{					
 					@Override
 					public void OnResponse(ConnectionErrorMessages response) 
 					{
 						displayConnectionAttemptResponseMessages(response);
 						
 						ConnectionFacade.isConnectedAsync(WiFiConnect.this, new IConnectionStatusResponse() 
 						{
 							@Override
 							public void OnResponse(ConnectionStatus response) 
 							{
 								updateConnectionStatus(response);
 							}
 						});
 					}
 				});
 			}
 		});	
 	}
 	
 	private void displayConnectionAttemptResponseMessages(ConnectionErrorMessages response)
 	{
 		int toastMsgResId = -1;
 		
 		switch(response)
 		{
 			case ALREADY_CONNECTED:
 				toastMsgResId = R.string.already_connected;
 				break;
 			case SUCCESS:
 				toastMsgResId = R.string.connect_success;
 				break;
 			case FAIL:
 				toastMsgResId = R.string.connect_fail;
 				break;
 			case UNKNOWN_WIFI:
 				toastMsgResId = R.string.unknown_wifi;
 				break;
			case NO_CREDENTIALS:
				toastMsgResId = R.string.credentials_not_found;
				break;
 			case WIFI_TURNED_OFF:
 				toastMsgResId = R.string.wifi_disabled;
 				break;
 		}
 		
 		if (toastMsgResId != -1)
 			WiFiConnect.this.displayToastMessage(mResources.getString(toastMsgResId));
 	}
 	
 	private void setViewOnClickListener(int viewId, OnClickListener onClickListener)
 	{
 		LogManager.LogFunctionCall("WiFiConnect", "setViewOnClickListener()");
 
 	    View view = (View) findViewById(viewId);
 	    view.setOnClickListener( onClickListener );
 	}
 
 	@Override
 	protected void onResume() 
 	{
 		super.onResume();
 		
 		LogManager.LogFunctionCall("WiFiConnect", "onResume()");
 		
 		// Refresh Login Data
 		this.mLoginData= DataFacade.LoadLoginData(this);
 		
 		refreshConnectionStatus();
 	}
 	
 	private void refreshConnectionStatus()
 	{
 		ConnectionFacade.isConnectedAsync(this, new IConnectionStatusResponse() 
 		{
 			@Override
 			public void OnResponse(ConnectionStatus response) 
 			{
 				updateConnectionStatus(response);
 			}
 		});
 	}
 	
 	private void updateConnectionStatus(ConnectionStatus connStatus)
 	{
 		LogManager.LogFunctionCall("WiFiConnect", "updateConnectionStatus()");
 		
 		ImageView connStatusImg = (ImageView)findViewById(R.id.connect_button);
 		
 		switch(connStatus)
 		{
 			case NOT_CONNECTED:
 				connStatusImg.setImageResource(R.drawable.wifi_red);
 				break;
 			case CONNECTED:
 				connStatusImg.setImageResource(R.drawable.wifi_green);
 				break;
 			case CONNECTED_UNKNOWN_WIFI:
 				connStatusImg.setImageResource(R.drawable.wifi_orange);
 				break;
 			case WIFI_DISABLED:
 				connStatusImg.setImageResource(R.drawable.wifi_grey);
 				break;
 		}
 		
 		// Animation
 		if (mAnimation != null)
 			connStatusImg.startAnimation(mAnimation);
 	}
 	
     // Display Toast-Message
 	public void displayToastMessage(String message) 
 	{
 		if ((message == null) || (message.trim().length() == 0))
 		{
 			return;
 		}
 		
 		Toast toastMsg = Toast.makeText(this, message, Toast.LENGTH_SHORT);
 		toastMsg.show();
 	}
 	
 	/***
 	 * Menu Part
 	 */
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) 
 	{
 		LogManager.LogFunctionCall("WiFiConnect", "onCreateOptionsMenu()");
 		
 		super.onCreateOptionsMenu(menu);
 		
 		int base_group_id = BASE_GROUP_ID;
 		int base_item_id = BASE_ITEM_ID;
 		int base_order_id = BASE_ORDER_ID;
 		
 		// Wifi Settings Item
 		MenuItem wifiSettingsMenuItem = menu.add(base_group_id++, // Group ID
 									base_item_id++,  // Item ID
 									base_order_id++, 	// Order ID			
 									mResources.getString(R.string.menu_wifi_settings)); // Title
 		
 		
 		wifiSettingsMenuItem.setIcon(R.drawable.wifi_48);
 		
 		wifiSettingsMenuItem.setOnMenuItemClickListener(
 				new OnMenuItemClickListener() {
 							@Override
 							public boolean onMenuItemClick(MenuItem item) {
 					            Intent WifiSettingsActivity = new Intent(getBaseContext(), WifiSettings.class);
 					            startActivity(WifiSettingsActivity);
 					            return true;
 							}
 		});
 		
 		// Settings Item
 		MenuItem settingsMenuItem = menu.add(base_group_id++, // Group ID
 											base_item_id++,  // Item ID
 											base_order_id++, 	// Order ID			
 											mResources.getString(R.string.menu_settings)); // Title
 		
 		
 		settingsMenuItem.setIcon(R.drawable.settings_48);
 		
 		settingsMenuItem.setOnMenuItemClickListener(
 				new OnMenuItemClickListener() {
 							@Override
 							public boolean onMenuItemClick(MenuItem item) {
 					            Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
 					            startActivity(settingsActivity);
 					            return true;
 							}
 		});
 		
 		// About Item
 		MenuItem aboutMenuItem = menu.add(base_group_id++, // Group ID
 										base_item_id++,  // Item ID
 										base_order_id++, 	// Order ID			
 									mResources.getString(R.string.menu_about)); // Title
 
 		aboutMenuItem.setIcon(R.drawable.light_48);
 		
 		aboutMenuItem.setOnMenuItemClickListener(
 				new OnMenuItemClickListener() {
 							@Override
 							public boolean onMenuItemClick(MenuItem item) 
 							{
 								AboutDialog.show(WiFiConnect.this);
 								
 								return true;
 							}
 		});
 
 		return true;
 	}
 }
