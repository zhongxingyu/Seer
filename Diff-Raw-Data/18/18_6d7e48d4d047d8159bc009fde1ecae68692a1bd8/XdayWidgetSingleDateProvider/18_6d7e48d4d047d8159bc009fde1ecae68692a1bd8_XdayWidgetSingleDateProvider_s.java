 package com.asdamp.widget;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.database.CursorIndexOutOfBoundsException;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.util.Log;
 import android.view.View;
 import android.widget.RemoteViews;
 import com.asdamp.database.DBAdapter;
 import com.asdamp.exception.WidgetConfigurationNotFoundException;
 import com.asdamp.utility.UtilityDate;
 import com.asdamp.x_day.Costanti;
 import com.asdamp.x_day.Data;
 import com.asdamp.x_day.R;
 
 public class XdayWidgetSingleDateProvider extends AppWidgetProvider {
 
 	/*@Override
 	public void onEnabled(Context context) {
 		DBAdapter dbadapter = Costanti.getDB();
 		AppWidgetManager appwidgetmanager = AppWidgetManager
 				.getInstance(context);
 		int a[] = appwidgetmanager
 				.getAppWidgetIds(new ComponentName(context,
 						"XdayWidgetSingleDateProvider"));
 		for (int id : a) {
 			Data data;
 			try {
 				data = dbadapter.AssociaDataAWidget(id);
 				aggiornaWidget(appwidgetmanager, data, context, id);
 			} catch (WidgetConfigurationNotFoundException e) {
 				ConfigurationNotFound(appwidgetmanager, context, id);
 			}
 		}
 	}
 */
 	public XdayWidgetSingleDateProvider() {
 		sWorkerThread = new HandlerThread("XdayWidgetSingleDateProvider-worker");
 		sWorkerThread.start();
 		sWorkerQueue = new Handler(sWorkerThread.getLooper());
 	}
  
 	public static void aggiornaWidget(AppWidgetManager appwidgetmanager,
 			Data data, Context context, int i) {
 		RemoteViews remoteviews = new RemoteViews(context.getPackageName(),
 				R.layout.widget_single_data_layout);
 		Intent intent = new Intent("com.asdamp.widget.REFRESH_WIDGET_SOLO");
 		intent.putExtra("appWidgetId", i);
 		remoteviews.setOnClickPendingIntent(R.id.refresh_widget_solo,
 				PendingIntent.getBroadcast(context, 0, intent, 0));
 		Intent intent1 = new Intent("com.asdamp.widget.OPEN_APP_SOLO");
 		intent1.putExtra("appWidgetId", i);
 		remoteviews.setOnClickPendingIntent(
 				R.id.relative_layout_widget_single_data,
 				PendingIntent.getBroadcast(context, 0, intent1, 0));
 		String s = UtilityDate.convertiDataInStringaBasandosiSuConfigurazione(
 				UtilityDate.creaData(data.getAnno(), data.getMese(),
 						data.getGiorno(), data.getMinuto(), data.getOra()),
 				Costanti.dt);
 		remoteviews.setTextViewText(idData, s);
 		remoteviews.setTextViewText(idMancante, data.aggiorna());
 		String s1 = data.getDescrizione();
 		if (s1.equalsIgnoreCase("")) {
 			remoteviews.setViewVisibility(idDescrizionePersonale, 8);
 		} else {
 			remoteviews.setViewVisibility(idDescrizionePersonale, 0);
 			remoteviews.setTextViewText(idDescrizionePersonale, s1);
 		}
 		if (data.getPercentuale() == 1000) {
 			remoteviews.setTextViewText(R.id.mancanoopassato2,
 					context.getText(R.string.Passato));
 			remoteviews.setTextViewText(R.id.alladata2,
 					context.getText(R.string.DallaData));
 			remoteviews.setViewVisibility(idProgressi, View.GONE);
 		} else {
 			remoteviews.setProgressBar(idProgressi, 1000,
 					data.getPercentuale(), false);
 			remoteviews.setViewVisibility(idProgressi, 0);
 		}
 		
 		appwidgetmanager.updateAppWidget(i, remoteviews);
 	}
	public void ConfigurationNotFound(AppWidgetManager appwidgetmanager, Context context, int i){
 		RemoteViews remoteviews = new RemoteViews(context.getPackageName(),
 				R.layout.empty_view);
 		remoteviews.setTextViewText(R.id.empty_view_text, context.getString(R.string.ConfigurationNotFoundWidgetSolo));
 		Intent intent1 = new Intent(OPEN_CONFIGURATION);
		intent1.putExtra("appWidgetId", i);
 		remoteviews.setOnClickPendingIntent(R.id.empty_view_text,
				PendingIntent.getBroadcast(context, 0, intent1, 0));
		appwidgetmanager.updateAppWidget(i, remoteviews);
 
 	}
 	public void onReceive(final Context context, final Intent intent) {
 		super.onReceive(context, intent);
 		if (intent.getAction().equals(OPEN_APP)) {
 			PackageManager pm = context.getPackageManager();
 			Intent i = pm.getLaunchIntentForPackage(context.getPackageName());
 			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			context.startActivity(i);
 
 		} else if (intent.getAction().equals(
 				AppWidgetManager.ACTION_APPWIDGET_DELETED)) {
 			Log.d("widgetAction", "eliminazione widget avvenuta:");
 			DBAdapter db = Costanti.getDB(); 
 			boolean a = db.eliminaDataWidget(intent.getExtras().getInt(
 					AppWidgetManager.EXTRA_APPWIDGET_ID,
 					AppWidgetManager.INVALID_APPWIDGET_ID));
 			Log.d("widgetAction", "" + a);
 		}
 		else if (intent.getAction().equals(REFRESH_WIDGET)){
 			sWorkerQueue.post(new Runnable() {
 				public void run() {
 					Log.d("widgetAction", "in aggiornamento");
 					DBAdapter dbadapter = Costanti.getDB()/*.apri()*/;
 					AppWidgetManager appwidgetmanager = AppWidgetManager
 							.getInstance(context);
 					int a[]=appwidgetmanager.getAppWidgetIds(new ComponentName(context, XdayWidgetSingleDateProvider.class));
 					for(int id:a) {
						
 						Data data;
 						try {
 							data = dbadapter.AssociaDataAWidget(id);
 							aggiornaWidget(appwidgetmanager, data, context, id);
 						} catch (WidgetConfigurationNotFoundException e) {
 							ConfigurationNotFound(appwidgetmanager, context, id);
 						}
 						
 						
 						
 					}
 					
 				}
 			});}
 			else if(intent.getAction().equals(OPEN_CONFIGURATION)){
 				Intent add = new Intent("com.asdamp.widget.CONFIGURE_WIDGET_SOLO");
 				add.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, intent.getExtras().getInt("appWidgetId"));
 				add.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				add.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				context.startActivity(add);
 			
 			}
 	}
 
 	public void onUpdate(Context context, AppWidgetManager appwidgetmanager,
 			int ai[]) {
 
 		for (int i = 0; i < ai.length; i++) {
 			 try {
 				DBAdapter dbadapter = Costanti.getDB();
 				for (int id : ai) {
 					Data data;
 					try {
 						data = dbadapter.AssociaDataAWidget(id);
 						aggiornaWidget(appwidgetmanager, data, context, id);
 					} catch (WidgetConfigurationNotFoundException e) {
 						ConfigurationNotFound(appwidgetmanager, context, id);
 					}
 				}
 			} catch (ArrayIndexOutOfBoundsException e) { 
 				Log.d("widgetAction", "nuovo widget");
 			}
 			 catch (CursorIndexOutOfBoundsException e) { 
 				Log.d("widgetAction", "nuovo widget");
 			}
 		}
 
 	}
 
 	private static final String OPEN_APP = "com.asdamp.widget.OPEN_APP_SOLO";
 	private static final String OPEN_CONFIGURATION = "com.asdamp.widget.OPEN_CONFIGURATION_SOLO";
 
 	private static final String REFRESH_WIDGET = "com.asdamp.widget.REFRESH_WIDGET_SOLO";
 	private static int idData = R.id.data2;
 	private static int idDescrizionePersonale = R.id.descrizionePersonale2;
 	private static int idMancante = R.id.mancante2;
 	private static int idProgressi = R.id.progressi2;
 	private static Handler sWorkerQueue;
 	private static HandlerThread sWorkerThread;
 
 }
