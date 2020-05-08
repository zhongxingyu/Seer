 package nl.q42.hue2.widgets;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 
 import nl.q42.hue2.R;
 import nl.q42.hue2.Util;
 import nl.q42.javahueapi.HueService;
 import nl.q42.javahueapi.models.FullConfig;
 import nl.q42.javahueapi.models.Light;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.preference.PreferenceManager;
 import android.util.SparseArray;
 import android.view.View;
 import android.widget.RemoteViews;
 
 public class WidgetUpdateService extends Service {
 	public static class WidgetButton {
 		public int widget;
 		public int button;
 		
 		public WidgetButton(int widget, int button) {
 			this.widget = widget;
 			this.button = button;
 		}
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		// Ignore if screen is off
 		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
 		if (!powerManager.isScreenOn()) {
 			stopSelf();
 			return START_NOT_STICKY;
 		}
 		
 		final AppWidgetManager widgetManager = AppWidgetManager.getInstance(getApplicationContext());
 		final int[] widgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
 		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 		
 		// Fetch light states and update UI of all widgets
 		new AsyncTask<Void, Void, SparseArray<FullConfig>>() {
 			@Override
 			protected SparseArray<FullConfig> doInBackground(Void... params) {
 				// Check if WiFi is connected at all before wasting time
 				ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 				NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 				
 				if (!wifi.isConnected()) {
 					return null;
 				}
 				
 				// Fetch current state of all bridges used in widgets (usually just one)				
 				HashMap<String, FullConfig> ipConfigs = new HashMap<String, FullConfig>();
 				SparseArray<FullConfig> bridgeConfigs = new SparseArray<FullConfig>();
 				
 				for (int wid : widgetIds) {
 					if (!prefs.contains("widget_" + wid + "_ip")) continue;
 					
 					String ip = prefs.getString("widget_" + wid + "_ip", null);
 					
 					try {
 						if (!ipConfigs.containsKey(ip)) {
 							ipConfigs.put(ip, new HueService(ip, Util.getDeviceIdentifier(getApplicationContext())).getFullConfig());
 						}
 						
 						bridgeConfigs.put(wid, ipConfigs.get(ip));
 					} catch (Exception e) {
 						// Ignore network error here and move on to next widget, will be handled later
 					}
 				}
 					
 				return bridgeConfigs;
 			}
 			
 			@Override
 			protected void onPostExecute(SparseArray<FullConfig> configs) {
 				for (int id : widgetIds) {
 					RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget);
 					
 					if (configs.get(id) != null) {
 						// Build map of lights in this widget
 						FullConfig cfg = configs.get(id);
 						HashMap<String, Light> lights = new HashMap<String, Light>();
 						Set<String> widgetLights = prefs.getStringSet("widget_" + id + "_ids", null);
 						
 						for (String lid : widgetLights) {
 							lights.put(lid, cfg.lights.get(lid));
 						}
 						
 						// Update widget UI
 						updateWidget(widgetIds, id, views, lights, cfg.config.ipaddress);
 					} else {
 						// Replace content with loading spinner
 						views.setViewVisibility(R.id.widget_spinner, View.VISIBLE);
 						views.setViewVisibility(R.id.widget_content, View.GONE);
 					}
 					
 					widgetManager.updateAppWidget(id, views);
 				}
 				
 				stopSelf();
 			}
 		}.execute();
 		
 		return START_NOT_STICKY;
 	}
 	
 	private void updateWidget(int[] widgetIds, int id, RemoteViews views, HashMap<String, Light> lights, String ip) {
 		// Used to preserve light name order
 		ArrayList<String> sids = Util.getSortedLights(lights);
 		
 		// Replace loading spinner with content
 		views.setViewVisibility(R.id.widget_spinner, View.GONE);
 		views.setViewVisibility(R.id.widget_content, View.VISIBLE);
 		
 		// Update views
		int idDivider = -1;
		
 		for (int i = 0; i < 6; i++) {
 			int idButton = getResources().getIdentifier("widget_light" + (i + 1) + "_button", "id", getPackageName());
 			int idName = getResources().getIdentifier("widget_light" + (i + 1) + "_name", "id", getPackageName());
 			int idColor = getResources().getIdentifier("widget_light" + (i + 1) + "_color", "id", getPackageName());
 			int idIndicator = getResources().getIdentifier("widget_light" + (i + 1) + "_indicator", "id", getPackageName());
 			
 			if (sids.size() > i && lights.get(sids.get(i)).state.reachable && lights.containsKey(sids.get(i))) {
 				views.setViewVisibility(idButton, View.VISIBLE);
 				views.setOnClickPendingIntent(idButton, createToggleIntent(WidgetUpdateService.this, widgetIds, ip, sids.get(i), id, i + 1));
 				views.setTextViewText(idName, lights.get(sids.get(i)).name);
 				views.setTextColor(idName, lights.get(sids.get(i)).state.on ? Color.WHITE : Color.rgb(101, 101, 101));
 				views.setInt(idColor, "setBackgroundColor", Util.getRGBColor(lights.get(sids.get(i))));
 				views.setInt(idIndicator, "setBackgroundResource", lights.get(sids.get(i)).state.on ? R.drawable.appwidget_settings_ind_on_c_holo : R.drawable.appwidget_settings_ind_off_c_holo);
				
				idDivider = getResources().getIdentifier("widget_light" + (i + 1) + "_divider", "id", getPackageName());
				views.setViewVisibility(idDivider, View.VISIBLE);
 			} else {
 				views.setViewVisibility(idButton, View.GONE);
 			}
 		}
		
		if (idDivider != -1) {
			views.setViewVisibility(idDivider, View.GONE);
		}
 	}
 	
 	private PendingIntent createToggleIntent(Context context, int[] widgetIds, String ip, String light, int widget, int button) {
 		// This is needed so that intents are not re-used with wrong extras data
 		int requestCode = ip.hashCode() + light.hashCode();
 		
 		Intent intent = new Intent(context, WidgetToggleService.class);
 		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
 		intent.putExtra("ip", ip);
 		intent.putExtra("light", light);
 		intent.putExtra("widget", widget);
 		intent.putExtra("button", button);
 		PendingIntent pendingIntent = PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 		
 		return pendingIntent;
 	}
 
 	// Unused, but has to be implemented
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 }
