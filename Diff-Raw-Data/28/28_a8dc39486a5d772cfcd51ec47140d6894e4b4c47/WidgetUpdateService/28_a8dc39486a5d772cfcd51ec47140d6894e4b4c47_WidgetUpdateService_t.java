 package com.ecctm.networkwidget;
 
 import android.app.PendingIntent;
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.provider.Settings;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 /*
  * TODO:
  * 1) Set Network Logo from a separate .java file, to allow easy adding of new
  * networks.
  * 2) Data Transfer info?
  * 2a) Check if Wireless is enabled
  * 2b) Get signal coverage & display on the right
  * 2c) Get up/down speeds & display
  */
 
 public class WidgetUpdateService extends Service
 {
 
 	// Variables
 	int networkId;
	String networkName, netConnecting, airplaneModeOn, simLocked, simUnknown,
			simAbsent;
 	RemoteViews remoteViews;
 	private static final String LOG = "com.ecctm.networkwidget";
 
 	@Override
 	public void onStart(Intent intent, int startId)
 	{
 		// Begin Logging
 		Log.d(LOG, "WidgetUpdateService.onStart called.");
 		// super.onStart(intent, startId);
 
 		// Set up our widget manager
 		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
 
 		// get ids for ALL running instances of our widget
 		int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
 
 		// target this widget
 		ComponentName networkWidget = new ComponentName(getApplicationContext(), WidgetProvider.class);
 
 		// get ids for ALL running instances of our widget
 		int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(networkWidget);
 
 		// Log these ids
 		Log.d(LOG, "Intent " + String.valueOf(allWidgetIds.length));
 		Log.d(LOG, "Direct " + String.valueOf(allWidgetIds2.length));
 
 		// begin our update
 		for (int widgetId : allWidgetIds)
 		{
 			// Get our set preferences
 			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
 			int WidgetBackgroundRes = preferences.getInt(WidgetProvider.WIDGET_BACKGROUND_VALUE + "_" + widgetId, 0);
 			Log.d(LOG, "WidgetUpdateService.onStart - received preferences.");
 			Log.d(LOG, "WidgetUpdateService.onStart - WidgetBackgroundRes = " + WidgetBackgroundRes);
 
 			// set up TelephonyManager, ConnectivityManager & NetworkInfo to
 			// check our network info
 			TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
 
 			// check which layout we're using
 			if (WidgetBackgroundRes == 1)
 			{
 				// Inflate the widget's light layout
 				remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget_network_layout_light);
 			}
 			else
 			{
 				// Inflate the widget's dark layout
 				remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget_network_layout_dark);
 			}
 
 			// TODO make sure we're calling this with the correct type of phone

 			// Check if a sim card is present
 			if (telephonyManager.getSimState() != TelephonyManager.SIM_STATE_ABSENT)
 			{
 				// Log that Sim card is present
 				Log.d(LOG, "WidgetUpdateService.onStart - Sim card present.");
 
 				// Check AirplaneMode
 				if (!isAirplaneModeOn(this))
 				{
 					// Log that Airplane mode is OFF
 					Log.d(LOG, "WidgetUpdateService.onStart - Airplane Mode OFF.");
 
 					// Check if sim card is ready!
 					if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY)
 					{
 						// Log that Sim card is ready (and connected)
 						Log.d(LOG, "WidgetUpdateService.onStart - Sim Card Ready.");
 
						try
 						{
 							// Get info about currently connected network
 							networkId = Integer.parseInt(telephonyManager.getNetworkOperator());
 							networkName = telephonyManager.getNetworkOperatorName();
 							Log.d(LOG, "MobileOperatorID: " + networkId);
 							Log.d(LOG, "MobileOperatorName: " + networkName);
 
 							// Set the text
 							remoteViews.setTextViewText(R.id.widget_network_layout_network_title, networkName);
 
 							// Set the mobile Logo
 							switch (networkId)
 							{
 								case 27201: // Vodafone IE
 								case 23403: // Airtel-Vodafone UK
 								case 23415: // Vodafone UK
 								case 23591:
 									remoteViews.setImageViewResource(R.id.widget_network_layout_network_logo, R.drawable.image_network_vodafone);
 									break;
 								case 27202: // o2 IE
 								case 23402: // o2 UK
 								case 23410:
 								case 23411:
 									remoteViews.setImageViewResource(R.id.widget_network_layout_network_logo, R.drawable.image_network_o2);
 									break;
 								case 27205: // Three IE
 								case 23420: // Three UK
 								case 23594:
 									remoteViews.setImageViewResource(R.id.widget_network_layout_network_logo, R.drawable.image_network_three);
 									break;
 								case 23433: // Orange UK
 								case 23434:
 								case 23501:
 								case 23502:
 									remoteViews.setImageViewResource(R.id.widget_network_layout_network_logo, R.drawable.image_network_orange);
 									break;
 								case 23430: // T-Mobile UK
 									remoteViews.setImageViewResource(R.id.widget_network_layout_network_logo, R.drawable.image_network_tmobile);
 									break;
 								default:
 									// Unknown Network
 									remoteViews.setImageViewResource(R.id.widget_network_layout_network_logo, R.drawable.image_network_default);
 									break;
 							}
 						}
						catch (Exception e)
 						{
							// Log our error!
							Log.d(LOG, "WidgetUpdateService.onStart - Exception: " + e.getMessage());
 						}
 					}
 					else if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_PIN_REQUIRED)
 					{
 						// Log that Sim requires PIN
 						Log.d(LOG, "WidgetUpdateService.onStart - Sim is PIN Locked.");
 
 						// Set the text (telling us we've no sim card)
 						simLocked = getString(R.string.widget_network_layout_network_title_simcard_locked);
 						remoteViews.setTextViewText(R.id.widget_network_layout_network_title, simLocked);
 
 						// Set the default network icon
 						remoteViews.setImageViewResource(R.id.widget_network_layout_network_logo, R.drawable.image_network_default);
 					}
 					else
 					{
 						// Log that Sim state is probably Unknown
 						Log.d(LOG, "WidgetUpdateService.onStart - Sim state Unknown.");
 
 						// Set the text (telling us we've no sim card)
 						simUnknown = getString(R.string.widget_network_layout_network_title_default);
 						remoteViews.setTextViewText(R.id.widget_network_layout_network_title, simUnknown);
 
 						// Set the default network icon
 						remoteViews.setImageViewResource(R.id.widget_network_layout_network_logo, R.drawable.image_network_default);
 					}
 				}
 				else
 				{
 					// Log that Airplane mode is ON
 					Log.d(LOG, "WidgetUpdateService.onStart - Airplane Mode ON.");
 
 					// Set the text (telling us were in airplane mode)
 					airplaneModeOn = getString(R.string.widget_network_layout_network_title_airplane);
 					remoteViews.setTextViewText(R.id.widget_network_layout_network_title, airplaneModeOn);
 
 					// Set the default network icon
 					remoteViews.setImageViewResource(R.id.widget_network_layout_network_logo, R.drawable.image_network_default);
 				}
 			}
 			else
 			{
 				// Log that Sim card is absent
 				Log.d(LOG, "WidgetUpdateService.onStart - Sim card absent.");
 
 				// Set the text (telling us we've no sim card)
 				simAbsent = getString(R.string.widget_network_layout_network_title_simcard_absent);
 				remoteViews.setTextViewText(R.id.widget_network_layout_network_title, simAbsent);
 
 				// Set the default network icon
 				remoteViews.setImageViewResource(R.id.widget_network_layout_network_logo, R.drawable.image_network_default);
 			}
 
 			// When User clicks on the label, update all widgets
 			Intent clickIntent = get_ACTION_APPWIDGET_UPDATE_Intent(this.getApplicationContext());
 			PendingIntent clickPendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), widgetId, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 
 			remoteViews.setOnClickPendingIntent(R.id.widget_network_layout_container, clickPendingIntent);
 
 			Log.d(LOG, "WidgetUpdateService.onStart - Updated ID: " + widgetId);
 
 			// Call the AppWidgetManager to make sure changes take effect
 			appWidgetManager.updateAppWidget(widgetId, remoteViews);
 		}
 		stopSelf();
 	}
 
 	// Get AirplaneMode status
 	private static boolean isAirplaneModeOn(Context context)
 	{
 		// Returns '1' if active
 		return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
 	}
 
 	/*
 	 * Utility method to ensure that when we want an Intent that fires
 	 * ACTION_APPWIDGET_UPDATE, the extras are correct.
 	 * 
 	 * The default implementation of onReceive() will discard it if we don't add
 	 * the ids of all the instances.
 	 */
 	protected Intent get_ACTION_APPWIDGET_UPDATE_Intent(Context context)
 	{
 		// Set up AppWidgetManager and ComponentName
 		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
 		ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WidgetProvider.class.getName());
 
 		// Get values for appWidgetIds
 		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
 
 		// Package and return them
 		Intent returnIntent = new Intent(context, WidgetProvider.class);
 		returnIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
 		returnIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
 
 		return returnIntent;
 	}
 
 	// Required IBinder method
 	@Override
 	public IBinder onBind(Intent intent)
 	{
 		return null;
 	}
 }
