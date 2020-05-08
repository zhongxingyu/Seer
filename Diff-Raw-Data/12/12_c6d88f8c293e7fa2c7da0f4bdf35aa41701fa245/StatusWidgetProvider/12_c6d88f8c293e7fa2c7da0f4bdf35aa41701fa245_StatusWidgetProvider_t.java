 package org.attraktor.android.door;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.json.JSONObject;
 
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.net.http.AndroidHttpClient;
 import android.widget.RemoteViews;
 
 public class StatusWidgetProvider extends AppWidgetProvider {
 	private final static HttpClient httpclient = AndroidHttpClient
 			.newInstance("MexAndroidWidget");
     private Timer timer = null;
     private final static long RATE = 300000;
     private final static String URL = "http://winkekatzen.r3l4x.de/api/status";
     private static MyTime task = null;
 
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
 			int[] appWidgetIds) {
        if (timer == null) {
             timer = new Timer();
 
            if (task == null)
                task = new MyTime(context, appWidgetManager);

            timer.scheduleAtFixedRate(task, 1, RATE);
        }
 	}
 
 	@Override
     public void onEnabled(Context context) {
         super.onEnabled(context);
         if (task != null) {
             if (timer == null)
                 timer = new Timer();
             timer.scheduleAtFixedRate(task, 1, RATE);
         }
     }
 	
     @Override
     public void onDisabled(Context context) {
         super.onDisabled(context);
         
         if (timer != null) {
             timer.cancel();
             timer = null;
         }
     }
 
     private class MyTime extends TimerTask {
 		private final static String ERROR_MSG_FETCH = "Unable to fetch data.",
 				ERROR_MSG_PARSE = "Unable to parse data.",
 				JSON_DOOR_STATE = "doorStateChange",
 				JSON_NET_STATE  = "networkStateChange",
 				JSON_STATE      = "bState",
 				JSON_NET_WLAN	= "iwlanClients",
 				JSON_NET_LAN	= "iClients",
 				TRUE            = "true",
 				FALSE			= "false",
 				MSG_LAN			= " - LAN:",
 				MSG_WLAN		= "WLAN:",
 				N               = "n";
 
 
 		RemoteViews remoteViews;
 		AppWidgetManager appWidgetManager;
 		ComponentName thisWidget;
 
 		public MyTime(Context context, AppWidgetManager appWidgetManager) {
 			this.appWidgetManager = appWidgetManager;
 			remoteViews = new RemoteViews(context.getPackageName(),
 					R.layout.status_widget);
 			thisWidget = new ComponentName(context, StatusWidgetProvider.class);
 		}
 
 		@Override
 		public void run() {
 			String message;
 			int status = -1;
 			
 			final JSONObject root = httpJsonGet();
 			if (root != null) {
 				message = ERROR_MSG_PARSE;
 				try {
 					Object o;
 					
 					if ((o = root.getJSONObject(JSON_DOOR_STATE)) != null) 
 						o = ((JSONObject)o).getString(JSON_STATE);
 					
 					if (o != null && o.toString().equals(TRUE)) {
 						status  = R.drawable.ic_unlocked;
 					} else if (o != null && o.toString().equals(FALSE)) {
 						status  = R.drawable.ic_locked;
 					}
 					
 					final JSONObject jo = root.getJSONObject(JSON_NET_STATE);
 					
 					String client;
 					if (jo != null && (client = jo.getString(JSON_NET_WLAN)) != null)
 						message = MSG_WLAN + client;
 
 					if (jo != null && (client = jo.getString(JSON_NET_LAN)) != null)
 						message = message + MSG_LAN + client;
 				} catch (Exception e) {
 				}
 			} else {
 				message = ERROR_MSG_FETCH;
 			}
 			
 			if (status == -1)
 				status  = R.drawable.ic_unknown;
 
 
 			remoteViews.setTextViewText(R.id.widget_text, message);
 			remoteViews.setImageViewResource(R.id.widget_door_status, status);
 			appWidgetManager.updateAppWidget(thisWidget, remoteViews);
 		}
 
 		public JSONObject httpJsonGet() {
 		    final HttpGet httpGet = new HttpGet(URL);
 			try {
 				final HttpEntity entity = httpclient.execute(httpGet).getEntity();  
 				if (entity == null) 
 					return null;
 				
 				final InputStream instream = entity.getContent();  
 				final BufferedReader reader = new BufferedReader(new InputStreamReader(instream), 2048);  
 				  
 				String line = null;  
 				final StringBuilder sb = new StringBuilder();  
 				while ((line = reader.readLine()) != null)  
 					sb.append(line + N);  
 				  
 				instream.close();
 				  
 				return new JSONObject(sb.toString());  
 			} catch (Exception e) {
 			} finally {
 				httpGet.abort();
 			}
 			return null; // error
 		}
 	}
 }
