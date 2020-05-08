 package nl.q42.hue2.widgets;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import nl.q42.hue2.PHUtilitiesImpl;
 import nl.q42.hue2.PresetsDataSource;
 import nl.q42.hue2.R;
 import nl.q42.hue2.Util;
 import nl.q42.hue2.models.Bridge;
 import nl.q42.hue2.models.Preset;
 import nl.q42.javahueapi.HueService;
 import nl.q42.javahueapi.models.FullConfig;
 import nl.q42.javahueapi.models.Group;
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
 
 public class GroupWidgetUpdateService extends Service {
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
 		
 		// Fetch group states and update UI of all widgets
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
 						e.printStackTrace();
 						// Ignore network error here and move on to next widget, will be handled later
 					}
 				}
 					
 				return bridgeConfigs;
 			}
 			
 			@Override
 			protected void onPostExecute(SparseArray<FullConfig> configs) {
 				PresetsDataSource datasource = new PresetsDataSource(GroupWidgetUpdateService.this);
 				datasource.open();
 				
 				for (int id : widgetIds) {
 					RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_group);
 					
 					if (configs.get(id) != null) {
 						// Get presets for group
 						FullConfig cfg = configs.get(id);
 						HashMap<String, ArrayList<Preset>> bridgePresets =
 								datasource.getGroupPresets(new Bridge(null, cfg.config.mac.replace(":", "").toLowerCase(), null, null));
 						
 						// Update widget UI
 						String widgetGroup = prefs.getString("widget_" + id + "_id", null);
 						
 						updateWidget(GroupWidgetUpdateService.this, widgetIds, id, views, widgetGroup, cfg.groups.get(widgetGroup), bridgePresets.get(widgetGroup), cfg.lights, cfg.config.ipaddress);
 					} else {
 						// Replace content with loading spinner
 						views.setViewVisibility(R.id.widget_group_spinner, View.VISIBLE);
 						views.setViewVisibility(R.id.widget_group_content, View.GONE);
 					}
 					
 					widgetManager.updateAppWidget(id, views);
 				}
 				
 				stopSelf();
 			}
 		}.execute();
 		
 		return START_NOT_STICKY;
 	}
 	
 	public static void updateWidget(Context context, int[] widgetIds, int id, RemoteViews views, String gid, Group group, ArrayList<Preset> presets, Map<String, Light> lights, String ip) {
 		// Replace loading spinner with content
 		views.setViewVisibility(R.id.widget_group_spinner, View.GONE);
 		views.setViewVisibility(R.id.widget_group_content, View.VISIBLE);
 		
 		// Handle exception of "all lights" group
 		if (group == null) {
 			group = new Group();
 			group.name = context.getString(R.string.widget_group_config_all_lights);
 			group.lights = new ArrayList<String>();
 			group.lights.addAll(lights.keySet());
 		}
 		
 		// Determine current group on/off state and light color
 		int lightsOn = 0, totalRed = 0, totalGreen = 0, totalBlue = 0;
 		
 		for (String lid : group.lights) {
 			Light light = lights.get(lid);
 			if (light.state.on) {
 				lightsOn++;
 				
 				int col = Util.getRGBColor(light);
 				totalRed += Color.red(col);
 				totalGreen += Color.green(col);
 				totalBlue += Color.blue(col);
 			}
 		}
 		
 		// Light is considered on if at least one of its lights is on
 		// This creates behaviour where toggling a group with one light on turns that light off, which seems
 		// more reasonable than turning the remaining lights on.
 		boolean groupOn = lightsOn > 0;
 		int averageColor = groupOn ? Color.rgb(totalRed / lightsOn, totalGreen / lightsOn, totalBlue / lightsOn) : Color.BLACK;
 		
 		// Update views
 		views.setOnClickPendingIntent(R.id.widget_group_content, createToggleIntent(context, widgetIds, ip, gid, id, !groupOn, presets));
 		views.setTextViewText(R.id.widget_group_name, group.name);
 		views.setTextColor(R.id.widget_group_name, groupOn ? Color.WHITE : Color.rgb(101, 101, 101));
 		views.setInt(R.id.widget_group_color, "setBackgroundColor", averageColor);		
 		views.setInt(R.id.widget_group_indicator, "setBackgroundResource", groupOn ? R.drawable.appwidget_settings_ind_on_c_holo : R.drawable.appwidget_settings_ind_off_c_holo);
 		
 		// Show up to first 3 presets
 		for (int i = 0; i < 3; i++) {
 			int idPresetView = context.getResources().getIdentifier("widget_group_preset" + (i + 1), "id", context.getPackageName());
 			
			if (presets != null && presets.size() > i) {
 				Preset preset = presets.get(i);
 				
 				views.setViewVisibility(idPresetView, View.VISIBLE);
 				
 				if (preset.color_mode.equals("xy")) {
 					views.setInt(idPresetView, "setBackgroundColor", PHUtilitiesImpl.colorFromXY(preset.xy, null));
 				} else {
 					views.setInt(idPresetView, "setBackgroundColor", Util.temperatureToColor(1000000 / (int) preset.ct));
 				}
 				
 				views.setOnClickPendingIntent(idPresetView, createPresetIntent(context, widgetIds, ip, gid, id, groupOn, preset, presets));
 			} else {
 				views.setViewVisibility(idPresetView, View.GONE);
 			}
 		}
 	}
 	
 	private static PendingIntent createToggleIntent(Context context, int[] widgetIds, String ip, String group, int widget, boolean on, ArrayList<Preset> presets) {
 		// This is needed so that intents are not re-used with wrong extras data
 		int requestCode = (int) (System.currentTimeMillis() / 1000 + ip.hashCode() + group.hashCode());
 		
 		Intent intent = new Intent(context, GroupWidgetChangeService.class);
 		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
 		intent.putExtra("ip", ip);
 		intent.putExtra("group", group);
 		intent.putExtra("on", on);
 		intent.putExtra("presets", presets);
 		intent.putExtra("widget", widget);
 		PendingIntent pendingIntent = PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 		
 		return pendingIntent;
 	}
 	
 	private static PendingIntent createPresetIntent(Context context, int[] widgetIds, String ip, String group, int widget, boolean on, Preset preset, ArrayList<Preset> presets) {
 		// This is needed so that intents are not re-used with wrong extras data
 		int requestCode = (int) (System.currentTimeMillis() / 1000 + ip.hashCode() + group.hashCode() + preset.hashCode());
 		
 		Intent intent = new Intent(context, GroupWidgetChangeService.class);
 		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
 		intent.putExtra("ip", ip);
 		intent.putExtra("group", group);
 		intent.putExtra("on", on);
 		intent.putExtra("preset", preset);
 		intent.putExtra("presets", presets);
 		intent.putExtra("widget", widget);
 		PendingIntent pendingIntent = PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 		
 		return pendingIntent;
 	}
 	
 	// Unused, but has to be implemented
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 }
