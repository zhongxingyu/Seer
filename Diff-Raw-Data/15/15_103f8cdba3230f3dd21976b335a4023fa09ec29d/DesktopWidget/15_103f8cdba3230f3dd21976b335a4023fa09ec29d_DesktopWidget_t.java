 package com.honkasalo.antelllmf;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 public class DesktopWidget extends AppWidgetProvider {
 	
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
 		super.onUpdate(context, appWidgetManager, appWidgetIds);
 		
 		// Set RemoteView
 		RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_antell_lmf);
 		
 		// Create new intent
 		Intent intent = new Intent(context, DesktopWidget.class);
 		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[0]);
 		intent.setAction("update");
 		
 		// Create new PendingIntent for refresh button
 		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 		v.setOnClickPendingIntent(R.id.refreshWidget, pi);
 		
 		// Manually run the asynchTask
 		new DownloadMenuTask().execute(context);
 		
 		// Update the widget
 		appWidgetManager.updateAppWidget(appWidgetIds, v);
 		
 	}
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		super.onReceive(context, intent);
 		
 		if (intent.getAction().equals("update")) {
 			new DownloadMenuTask().execute(context);
 		}
 	}
 
 	private class DownloadMenuTask extends AsyncTask<Context, Void,String> {
 		private Context context;
 		private RemoteViews v;
 		
 	    @Override
 	    protected String doInBackground(Context... params) {
 	    	context = params[0];
 	    	Calendar cal = Calendar.getInstance();
 			Document doc = null;
 			Element table = null;
 			StringBuilder sb = new StringBuilder();
 			StringBuilder sb2 = new StringBuilder();
 			StringBuilder sb3 = new StringBuilder();
 			Pattern p = Pattern.compile("(?<!^|/)(M|VL|L|G)");
 			Matcher m = null;
 			
 			// Localize and make start from zero DAY_OF_WEEK
 			cal.setTime(new Date());
 			int day = cal.get(Calendar.DAY_OF_WEEK) - 1;
 			if (day == 0) {
 				day = day + 6;
 			}
 			day--;
 			Log.d("WIDGET", "Day of Week: "+Integer.toString(day));
 			
 			// For testing purposes, making it work on Sunday
 			//day = 1;
 			
            	String url = AntellLMF.getFrameUrl("http://www.antell.fi/docs/lunch.php?LM%20Ericsson,%20Helsinki");
            	//Log.d("WIDGET", url);
 			
 			try {
 				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
 				String language = settings.getString("foodMenuLanguage","");
     		
 				//Log.d("WIDGET", url);
 				
 				boolean connection_established=false;
 				for (int tries = 1; tries<5 && !connection_established; tries++) {
 					try {
 						doc = Jsoup.connect("http://www.antell.fi"+url).timeout(100*1000).get();
 						Log.d("CONNECTION", "Connected and fetched page.");
 						connection_established=true;
 					} catch (Exception ex) {
 						ex.printStackTrace();
 					} 
         	    }
 				
 				// Title
 				table = doc.select("div:containsOwn(Lounaslista)").first();
 				sb.append(table.text().split("Lounaslista ")[1]+"\n");
 								
 				if (day < 5) {
 					// Daily menu
 					table = doc.select("table.lunchTable2").get(day);
 
 					for (Element row : table.select("tr")) {
 						Element tds = row.select("td").first();
 						m = p.matcher(tds.text());
 						sb2 = new StringBuilder();
 						while (m.find()) {
 							sb2.append(m.group());
 						}
 						if (language.equals("Finnish")) {
 							sb.append(tds.text().split("/| \\(")[0] + " " + sb2.toString() + "\n");
 						} else {
 							sb.append(tds.text().split("/| \\(")[1] + " " + sb2.toString() + "\n");
 						};
 					}
 
 					// Weekly specials
 					table = doc.select("div:containsOwn(Week)").first();
 					sb2 = new StringBuilder();
					m = p.matcher(table.text().split("Week.+Wok")[0]);
 					while (m.find()) {
 						sb2.append(m.group());
 					}
					m = p.matcher(table.text().split("Week.+Wok")[1]);
 					while (m.find()) {
 						sb3.append(m.group());
 					}
 					if ( language.equals("Finnish")) {
 						sb.append(table.text().split("Week|/|: ")[2].replaceAll("(?<!^|/)(M|VL|L|G).+(M|VL|L|G)", "") + sb2.toString() + "\n");
 						sb.append(table.text().split("Week|/|: ")[5].replaceAll("(?<!^|/)(M|VL|L|G).+(M|VL|L|G)", "") + sb3.toString());
 					} else {
 						sb.append(table.text().split("Week|/|: ")[3] + sb2.toString() + "\n");
 						sb.append(table.text().split("Week|/|: ")[6] + " " + sb3.toString());
 					};
 				} else {
 					sb.append("Weekend, no menu available.");
 				}
 			
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 			
 			
            	return sb.toString();
 	    }
 
 		@Override
 		protected void onPostExecute(String result) {
 			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
 			v = new RemoteViews(context.getPackageName(), R.layout.widget_antell_lmf);
 			//v.setTextViewText(R.id.widgetTitle, "testiotsikko");
 			v.setTextViewText(R.id.widgetText, result);
 			appWidgetManager.updateAppWidget(new ComponentName(context, DesktopWidget.class), v);
 		}
 	}
 
 }
